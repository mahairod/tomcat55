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

/***************************************************************************
 * Description: Workers controller                                         *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Author:      Henri Gomez <hgomez@slib.fr>                               *
 * Version:     $Revision$                                           *
 ***************************************************************************/

#include "jk_workerEnv.h" 
#include "jk_env.h"
#include "jk_worker.h"

#define DEFAULT_WORKER              ("ajp13")

int JK_METHOD jk_workerEnv_factory( jk_env_t *env, jk_pool_t *pool, void **result,
                                    const char *type, const char *name);

static void jk_workerEnv_close(jk_workerEnv_t *_this);

/**
 *  Init the workers, prepare the we.
 * 
 *  Replaces wc_open
 */
static int jk_workerEnv_init(jk_workerEnv_t *_this)
{
    jk_map_t *init_data=_this->init_data;
    char **worker_list  = NULL;
    jk_logger_t *l=_this->l;
    int i;
    int err;

    /*     _this->init_data=init_data; */

    worker_list = map_get_string_list(init_data,
                                      NULL, 
                                      "worker.list", 
                                      &_this->num_of_workers, 
                                      DEFAULT_WORKER );
     if(worker_list==NULL || _this->num_of_workers<= 0 ) {
        /* assert() - we pass default worker, we should get something back */
        return JK_FALSE;
    }

    for(i = 0 ; i < _this->num_of_workers ; i++) {
        jk_worker_t *w = NULL;
        jk_worker_t *oldw = NULL;
        const char *name=(const char*)worker_list[i];

        w=_this->createWorker(_this, name, init_data);
        if( w==NULL ) {
            l->jkLog(_this->l, JK_LOG_ERROR,
                   "init failed to create worker %s\n", 
                   worker_list[i]);
            /* Ignore it, other workers may be ok.
               return JK_FALSE; */
         } else {
             map_put(_this->worker_map, worker_list[i], w, (void *)&oldw);
            
            if(oldw!=NULL) {
                l->jkLog(_this->l, JK_LOG_DEBUG, 
                       "build_worker_map, removing old %s worker \n",
                       worker_list[i]);
                oldw->destroy(&oldw, _this->l);
            }
            if( _this->defaultWorker == NULL )
                _this->defaultWorker=w;
        }
    }

    l->jkLog(_this->l, JK_LOG_DEBUG, "build_worker_map, done\n"); 

    l->jkLog(_this->l, JK_LOG_DEBUG,
           "workerEnv.init() done: %d %s\n", _this->num_of_workers, worker_list[0]); 
    return JK_TRUE;
}


static void jk_workerEnv_close(jk_workerEnv_t *_this)
{
    jk_logger_t *l=_this->l;
    int sz;
    int i;
    
    sz = map_size(_this->worker_map);

    if(sz <= 0) {
        map_free(&_this->worker_map);
        return;
    }
    for(i = 0 ; i < sz ; i++) {
        jk_worker_t *w = map_value_at(_this->worker_map, i);
        if(w) {
            l->jkLog(l, JK_LOG_DEBUG,
                   "destroy worker %s\n",
                   map_name_at(_this->worker_map, i));
            w->destroy(&w, l);
        }
    }
    l->jkLog(_this->l, JK_LOG_DEBUG, "workerEnv.close() done %d\n", sz); 
    map_free(&_this->worker_map);
}

static jk_worker_t *jk_workerEnv_getWorkerForName(jk_workerEnv_t *_this,
                                                  const char *name )
{
    jk_worker_t * rc;
    jk_logger_t *l=_this->l;
    
    if(!name) {
        l->jkLog(l, JK_LOG_ERROR, "wc_get_worker_for_name NULL name\n");
        return NULL;
    }

    rc = map_get(_this->worker_map, name, NULL);

    if( rc==NULL ) {
        l->jkLog(l, JK_LOG_ERROR, "getWorkerForName: no worker found for %s \n", name);
    } 
    return rc;
}


static jk_webapp_t *jk_workerEnv_createWebapp(jk_workerEnv_t *_this,
                                              const char *vhost,
                                              const char *name, 
                                              jk_map_t *init_data)
{
    jk_pool_t *webappPool;
    jk_webapp_t *webapp;

    webappPool=(jk_pool_t *)_this->pool->create( _this->pool,
                                                 HUGE_POOL_SIZE);

    webapp=(jk_webapp_t *)webappPool->calloc(webappPool,
                                             sizeof( jk_webapp_t ));

    webapp->pool=webappPool;

    webapp->context=_this->pool->pstrdup( _this->pool, name);
    webapp->virtual=_this->pool->pstrdup( _this->pool, vhost);

    if( name==NULL ) {
        webapp->ctxt_len=0;
    } else {
        webapp->ctxt_len = strlen(name);
    }
    

    /* XXX Find it if it's already allocated */

    /* Add vhost:name to the map */
    
    return webapp;
    
}


static jk_worker_t *jk_workerEnv_createWorker(jk_workerEnv_t *_this,
                                              const char *name, 
                                              jk_map_t *init_data)
{
    int err;
    char *type;
    jk_env_objectFactory_t fac;
    jk_logger_t *l=_this->l;
    jk_worker_t *w = NULL;
    jk_pool_t *workerPool;

    workerPool=_this->pool->create(_this->pool, HUGE_POOL_SIZE);

    type=map_getStrProp( init_data,"worker",name,"type",NULL );

    /* Each worker has it's own pool */
    

    w=(jk_worker_t *)_this->env->getInstance(_this->env, workerPool, "worker", type );
    
    if( w == NULL ) {
        l->jkLog(l, JK_LOG_ERROR,
               "workerEnv.createWorker(): factory can't create worker %s:%s\n",
               type, name); 
        return NULL;
    }

    w->pool=workerPool;
    w->name=(char *)name;
    w->workerEnv=_this;

    err=w->validate(w, init_data, _this, l);
    
    if( err!=JK_TRUE ) {
        l->jkLog(l, JK_LOG_ERROR,
               "workerEnv.createWorker(): validate failed for %s:%s\n", 
               type, name); 
        w->destroy(&w, l);
        return NULL;
    }
    
    l->jkLog(l, JK_LOG_DEBUG,
           "workerEnv.createWorker(): validated %s:%s\n",
           type, name);
    
    err=w->init(w, init_data, _this, l);
    if(err!=JK_TRUE) {
        w->destroy(&w, l);
        l->jkLog(l, JK_LOG_ERROR, "workerEnv.createWorker() init failed for %s\n", 
               name); 
        return NULL;
    }
    
    return w;
}

int JK_METHOD jk_workerEnv_factory( jk_env_t *env, jk_pool_t *pool, void **result,
                                    const char *type, const char *name)
{
    jk_logger_t *l=env->logger;
    jk_workerEnv_t *_this;
    int err;
    jk_pool_t *uriMapPool;

    l->jkLog(l, JK_LOG_DEBUG, "Creating workerEnv \n");

    _this=(jk_workerEnv_t *)pool->calloc( pool, sizeof( jk_workerEnv_t ));
    _this->pool=pool;
    *result=_this;

    _this->init_data = NULL;
    map_alloc(& _this->init_data, pool);
    

    _this->worker_file     = NULL;
    _this->log_file        = NULL;
    _this->log_level       = -1;
    _this->l             = NULL;
    _this->mountcopy       = JK_FALSE;
    _this->was_initialized = JK_FALSE;
    _this->options         = JK_OPT_FWDURIDEFAULT;

    /*
     * By default we will try to gather SSL info.
     * Disable this functionality through JkExtractSSL
     */
    _this->ssl_enable  = JK_TRUE;
    /*
     * The defaults ssl indicators match those in mod_ssl (seems
     * to be in more use).
     */
    _this->https_indicator  = "HTTPS";
    _this->certs_indicator  = "SSL_CLIENT_CERT";

    /*
     * The following (comented out) environment variables match apache_ssl!
     * If you are using apache_sslapache_ssl uncomment them (or use the
     * configuration directives to set them.
     *
    _this->cipher_indicator = "HTTPS_CIPHER";
    _this->session_indicator = NULL;
     */

    /*
     * The following environment variables match mod_ssl! If you
     * are using another module (say apache_ssl) comment them out.
     */
    _this->cipher_indicator = "SSL_CIPHER";
    _this->session_indicator = "SSL_SESSION_ID";
    _this->key_size_indicator = "SSL_CIPHER_USEKEYSIZE";

    /*     if(!map_alloc(&(_this->automount))) { */
    /*         jk_error_exit(APLOG_MARK, APLOG_EMERG, s, p, "Memory error"); */
    /*     } */

    _this->uriMap = NULL;
    _this->secret_key = NULL; 

    _this->envvars_in_use = JK_FALSE;
    map_alloc(&_this->envvars, pool);

    _this->l=l;
    _this->env=env;
    
    if(!map_alloc(&_this->worker_map, _this->pool)) {
        return JK_FALSE;
    }

    uriMapPool = _this->pool->create(_this->pool, HUGE_POOL_SIZE);
    
    _this->uriMap=_this->env->getInstance( _this->env,
                                           uriMapPool,
                                           "uriMap",
                                           "default");

    if( _this->uriMap==NULL ) {
        l->jkLog(l, JK_LOG_ERROR, "Error getting uriMap implementation\n");
        return JK_FALSE;
    }

    _this->uriMap->workerEnv = _this;
    _this->perThreadWorker=0;
    
    /* methods */
    _this->init=&jk_workerEnv_init;
    _this->getWorkerForName=&jk_workerEnv_getWorkerForName;
    _this->close=&jk_workerEnv_close;
    _this->createWorker=&jk_workerEnv_createWorker;
    _this->createWebapp=&jk_workerEnv_createWebapp;

    _this->rootWebapp=_this->createWebapp( _this, NULL, "/", NULL );
    
    return JK_TRUE;
}
