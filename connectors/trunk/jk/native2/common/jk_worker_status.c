/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *          Copyright (c) 1999-2001 The Apache Software Foundation.          *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Jk",  and  "Apache  Software     *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

/**
 * Status worker. It'll not connect to tomcat, but just generate response
 * itself, containing a simple xhtml page with the current jk info.
 *
 * Note that the html tags are using 'class' attribute. Someone with some
 * color taste can do a nice CSS and display it nicely, but more important is
 * that it should be easy to grep/xpath it programmatically.
 * 
 * @author Costin Manolache
 */

#include "jk_pool.h"
#include "jk_service.h"
#include "jk_worker.h"
#include "jk_logger.h"
#include "jk_env.h"
#include "jk_requtil.h"
#include "jk_registry.h"

#define JK_BUF_SIZE 4096

typedef struct jk_buff {
    int pos;
    int size;
    
    char *buf;

    jk_ws_service_t *s;

    void (*jkprintf)( jk_env_t *env, struct jk_buff *buf, char *frm,... );
    void (*flush)( jk_env_t *env );
} jk_buff_t;

static void jk_printf( jk_env_t *env, jk_buff_t *buf, char *fmt,... )
{
    va_list vargs;
    int ret=0;

    va_start(vargs,fmt);
    buf->pos=0; /* Temp - we don't buffer */
    ret=apr_vsnprintf(buf->buf + buf->pos, buf->size - buf->pos, fmt, vargs);
    va_end(vargs);

    buf->s->write( env, buf->s, buf->buf, strlen(buf->buf) );
    fprintf(stderr, "Writing %d %s \n", ret, buf->buf);
}

static void jk_worker_status_displayWorkers( jk_env_t *env, jk_buff_t *buf,
                                             jk_workerEnv_t *wenv )
{
    jk_map_t *map=wenv->worker_map;
    int i;
    
    jk_printf(env, buf, "<H2>Workers</H2>\n" );
    jk_printf(env, buf, "<table border>\n");

    jk_printf(env, buf, "<tr><th>Name</th><th>Retries</th>"
              "<th>Err</th><th>Recovery</th></tr>");
    
    for( i=0; i< map->size( env, map ) ; i++ ) {
        char *name=map->nameAt( env, map, i );
        jk_worker_t *worker=(jk_worker_t *)map->valueAt( env, map,i );

        jk_printf(env, buf, "<tr id='worker.%s'>", name );
        jk_printf(env, buf, "<td class='name'>%s</td>", name );
        jk_printf(env, buf, "<td class='connect_retry'>%d</td>",
                  worker->connect_retry_attempts );
        jk_printf(env, buf, "<td class='in_error'>%d</td>",
                  worker->in_error_state );
        jk_printf(env, buf, "<td class='in_error'>%d</td>",
                  worker->in_recovering );

        /* Endpoint cache ? */

        /* special case for status worker */
        
        jk_printf(env, buf, "</tr>" );
    }
    jk_printf(env, buf, "</table>\n");
}

static void jk_worker_status_displayWorkerEnv( jk_env_t *env, jk_buff_t *buf,
                                               jk_workerEnv_t *wenv )
{
    jk_map_t *map=wenv->init_data;
    int i;

    jk_printf(env, buf, "<H2>Worker Env Info</H2>\n");

    jk_printf(env, buf, "<table border>\n");
    jk_printf(env, buf, "<tr><th>num_workers</th>"
              "<td id='workersCount'>%d</td></tr>\n",
              wenv->num_of_workers);        
    /* Could be modified dynamically */
    jk_printf(env, buf, "<tr><th>LogLevel</th>"
              "<td id='logLevel'>%d</td></tr>\n",
              wenv->log_level);
    
    jk_printf(env, buf, "</table>\n");

    jk_printf(env, buf, "<H3>Properties</H3>\n");
    jk_printf(env, buf, "<table border>\n");
    jk_printf(env, buf, "<tr><th>Name</th><th>Value</td></tr>\n");
    for( i=0; i< map->size( env, map ) ; i++ ) {
        char *name=map->nameAt( env, map, i );
        char *value=(char *)map->valueAt( env, map,i );

        jk_printf(env, buf, "<tr><td>%s</td><td>%s</td></tr>", name,
                  value);
    }
    jk_printf(env, buf, "</table>\n");

}

static void jk_worker_status_displayWebapps( jk_env_t *env, jk_buff_t *buf,
                                             jk_workerEnv_t *wenv )
{
    jk_map_t *map=wenv->webapps;
    int i;

    jk_printf(env, buf, "<H2>Webapps</H2>\n");

    if( map==NULL ) {
        jk_printf(env, buf, "None\n");
        return;
    }
    
    jk_printf(env, buf, "<table border>\n");
    
    jk_printf(env, buf, "<tr><th>Name</th><th>DocBase</th>"
              "<th>Mappings</th></tr>");
    
    for( i=0; i< map->size( env, map ) ; i++ ) {
        char *name=map->nameAt( env, map, i );
        jk_webapp_t *webapp=(jk_webapp_t *)map->valueAt( env, map,i );

        jk_printf(env, buf, "<tr id='webapp.%s'>", name );
        jk_printf(env, buf, "<td class='name'>%s</td>", name );
        jk_printf(env, buf, "</tr>" );
    }

    
    jk_printf(env, buf, "</table>\n");
}

/* Channels and connections, including 'pooled' ones
 */
static void jk_worker_status_displayConnections( jk_env_t *env, jk_buff_t *buf,
                                                 jk_workerEnv_t *wenv )
{
        jk_printf(env, buf, "<H2>Active connections</H2>\n");
        jk_printf(env, buf, "<table border>\n");
        
    
        jk_printf(env, buf, "</table>\n");

}

static jk_buff_t *jk_worker_status_createBuffer(jk_env_t *env, jk_endpoint_t *e,
                                                jk_ws_service_t *s)
{
    jk_buff_t *buff;
    int bsize=JK_BUF_SIZE;
    
    env->l->jkLog(env, env->l, JK_LOG_INFO, "create buffer()\n");
    buff=(jk_buff_t *)s->pool->alloc( env, s->pool, sizeof( jk_buff_t) );
    buff->s=s;
    buff->size=bsize;
    buff->buf=(char *)s->pool->alloc( env, s->pool, bsize );
    buff->jkprintf=jk_printf;

    return buff;
}

static int JK_METHOD service(jk_env_t *env, jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             int *is_recoverable_error)
{
    jk_buff_t *buff=jk_worker_status_createBuffer(env, e, s );
    
    env->l->jkLog(env, env->l, JK_LOG_INFO, "status.service() %p\n", e);

    /* Generate the header */
    s->status=200;
    s->msg="OK";
    s->headers_out->put(env, s->headers_out,
                        "Content-Type", "text/html", NULL);

    fprintf(stderr, "Writing head \n");
    s->head(env, s );

    /* Body */
    jk_worker_status_displayWorkerEnv(env, buff, s->workerEnv );
    jk_worker_status_displayWorkers(env, buff, s->workerEnv );
    jk_worker_status_displayWebapps(env, buff, s->workerEnv );
    jk_worker_status_displayConnections(env, buff, s->workerEnv );
    
    s->afterRequest( env, s);
    fprintf(stderr, "After req %s \n", buff);
    return JK_TRUE;

}

static int JK_METHOD done(jk_env_t *env, jk_endpoint_t *e)
{
    return JK_TRUE;
}

static int JK_METHOD validate(jk_env_t *env, jk_worker_t *_this,
                              jk_map_t *props, jk_workerEnv_t *we)
{
    return JK_TRUE;
}

static int JK_METHOD init(jk_env_t *env, jk_worker_t *_this,
                          jk_map_t *props, jk_workerEnv_t *we)
{
    return JK_TRUE;
}

static int JK_METHOD get_endpoint(jk_env_t *env, jk_worker_t *_this,
                                  jk_endpoint_t **pend)
{
    jk_endpoint_t *e;
    jk_pool_t *endpointPool;
    
    if (_this->endpointCache != NULL ) {
        e=_this->endpointCache->get( env, _this->endpointCache );
        if (e!=NULL) {
            env->l->jkLog(env, env->l, JK_LOG_INFO,
                     "status.getEndpoint(): Reusing endpoint\n");
            *pend = e;
            return JK_TRUE;
        }
    }
    
    endpointPool=_this->pool->create( env, _this->pool, HUGE_POOL_SIZE);
    
    e = (jk_endpoint_t *)endpointPool->calloc(env, endpointPool,
                                              sizeof(jk_endpoint_t));
    if(e==NULL) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "status_worker.getEndpoint() OutOfMemoryException\n");
        return JK_FALSE;
    }

    e->pool = endpointPool;
    e->cPool=endpointPool->create( env,endpointPool, HUGE_POOL_SIZE );
    e->worker = _this;
    e->service = service;
    e->done = done;
    e->channelData = NULL;
    *pend = e;

    env->l->jkLog(env, env->l, JK_LOG_INFO, "status_worker.getEndpoint() %p\n", e);
    return JK_TRUE;
}


static int JK_METHOD destroy(jk_env_t *env, jk_worker_t *w)
{
    int i = 0;

    if(w==NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "status_worker.destroy() NullPointerException\n");
        return JK_FALSE;
    }

    if( w->endpointCache != NULL ) {
        for( i=0; i< w->endpointCache->ep_cache_sz; i++ ) {
            jk_endpoint_t *e;
            
            e= w->endpointCache->get( env, w->endpointCache );
            if( e==NULL ) {
                // we finished all endpoints in the cache
                break;
            }

            /* Nothing else to clean up ? */
            e->cPool->close( env, e->cPool );
            e->pool->close( env, e->pool );
        }
        w->endpointCache->destroy( env, w->endpointCache );

        env->l->jkLog(env, env->l, JK_LOG_DEBUG,
                      "status.destroy() closed %d cached endpoints\n",i);
    }

    w->pool->close(env, w->pool);    

    return JK_TRUE;
}


int JK_METHOD jk_worker_status_factory(jk_env_t *env, jk_pool_t *pool,
                                       void **result,
                                       const char *type, const char *name)
{
    jk_worker_t *_this;
    
    if(NULL == name ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "status_worker.factory() NullPointerException\n");
        return JK_FALSE;
    }
    
    _this = (jk_worker_t *)pool->calloc(env, pool, sizeof(jk_worker_t));

    if(_this==NULL) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "status_worker.factory() OutOfMemoryException\n");
        return JK_FALSE;
    }

    _this->name=(char *)name;
    _this->pool=pool;

    _this->lb_workers = NULL;
    _this->num_of_workers = 0;
    _this->worker_private = NULL;
    _this->validate       = validate;
    _this->init           = init;
    _this->get_endpoint   = get_endpoint;
    _this->destroy        = destroy;
    
    *result=_this;

    return JK_TRUE;
}

