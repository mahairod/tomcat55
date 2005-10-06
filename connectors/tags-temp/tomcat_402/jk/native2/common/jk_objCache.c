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

/* See jk_objCache.h for docs */

#include "jk_global.h"
#include "jk_pool.h"
#include "jk_logger.h"
#include "jk_env.h"

#include "jk_objCache.h"

static int 
jk_objCache_put(jk_env_t *env, jk_objCache_t *_this, void *obj)
{
    int rc;
    
    if(_this->size <= 0 )
        return JK_FALSE;

    
    JK_ENTER_CS(&_this->cs, rc);
    if(rc==JK_TRUE) {

        rc=JK_FALSE;

        if( _this->count >= _this->size && _this->maxSize== -1 ) {
            /* Realloc */
            void **oldData=_this->data;
            _this->data =
                (void **)_this->pool->calloc(env, _this->pool,
                                             2 * sizeof(void *) * _this->size);
            memcpy( _this->data, oldData, _this->size );
            _this->size *= 2;
            /* XXX Can we destroy the old data ? Probably not.
               It may be better to have a list - but it's unlikely
               we'll have too many reallocations */
        }

        if( _this->count < _this->size ) {
            _this->data[_this->count++]=obj;
        }

        JK_LEAVE_CS(&_this->cs, rc);
        if(rc) {
            /*   l->jkLog(l, JK_LOG_DEBUG, "Return endpoint to pool\n"); */
            return JK_TRUE;
        }
    }

    /* Can't enter CS */ 
    return JK_FALSE; 
}

static int
jk_objCache_init(jk_env_t *env, jk_objCache_t *_this, int cacheSize ) {
    int i;

    if( cacheSize <= 0 ) {
        _this->size= 64;
    } else {
        _this->size= cacheSize ;
    }
    _this->count=0;
    _this->maxSize=cacheSize;
    
    _this->data =
        (void **)_this->pool->calloc(env, _this->pool,
                                     sizeof(void *) * _this->size);

    if( _this->data==NULL )
        return JK_FALSE;
    
    JK_INIT_CS(&(_this->cs), i);
    if (!i) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "objCache.create(): Can't init CS\n");
        return JK_FALSE;
    }

    return JK_TRUE;
}

static int  
jk_objCache_destroy(jk_env_t *env, jk_objCache_t *_this ) {
    int i;

    JK_DELETE_CS(&(_this->cs), i);

    _this->count=0;
    /* Nothing to free, we use pools */

    return JK_TRUE;
}


static void * 
jk_objCache_get(jk_env_t *env, jk_objCache_t *_this )
{
    int rc;
    void *ae=NULL;

    JK_ENTER_CS(&_this->cs, rc);
    if (rc) {
        if( _this->count > 0 ) {
            _this->count--;
            ae=_this->data[ _this->count ];
        }

        JK_LEAVE_CS(&_this->cs, rc);

        return ae;
    }
    return NULL;
}

jk_objCache_t *jk_objCache_create(jk_env_t *env, jk_pool_t *pool ) {
    jk_objCache_t *_this=pool->calloc( env, pool, sizeof( jk_objCache_t ));

    _this->pool=pool;

    _this->count=0;
    _this->size=0;
    _this->maxSize=-1;
    
    _this->get=jk_objCache_get;
    _this->put=jk_objCache_put;
    _this->init=jk_objCache_init;
    _this->destroy=jk_objCache_destroy;
    
    return _this;
}
