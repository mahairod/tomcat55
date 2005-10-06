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
 * Scoreboard - used for communication on multi-process servers.
 *
 * This is an optional component - will be enabled only if APR is used. 
 * The code is cut&pasted from apache and mod_jserv.
 *
 * 
 * 
 * @author Jserv and Apache people
 */

#include "jk_global.h"
#include "jk_map.h"
#include "jk_pool.h"
#include "jk_shm.h"

#ifdef HAS_APR

#include "apr.h"
#include "apr_strings.h"
#include "apr_general.h"
#include "apr_portable.h"
#include "apr_lib.h"

#define APR_WANT_STRFUNC
#include "apr_want.h"

#if APR_HAVE_SYS_TYPES_H
#include <sys/types.h>
#endif

#if APR_HAS_SHARED_MEMORY

#include "apr_shm.h"
#include "apr_atomic.h"

/* Inter-process synchronization - to create the slots */
#include "apr_proc_mutex.h"

static apr_pool_t *globalShmPool;

#define SHM_SET_ATTRIBUTE 0
#define SHM_WRITE_SLOT 2
#define SHM_ATTACH 3
#define SHM_DETACH 4
#define SHM_RESET 5
#define SHM_DUMP 6
#define SHM_DESTROY 7


static int jk2_shm_destroy(jk_env_t *env, jk_shm_t *shm)
{
    apr_shm_t *aprShm=(apr_shm_t *)shm->privateData;

    return apr_shm_destroy(aprShm);
}

static int JK_METHOD jk2_shm_detach(jk_env_t *env, jk_shm_t *shm)
{
    apr_shm_t *aprShm=(apr_shm_t *)shm->privateData;

    return apr_shm_detach(aprShm);
}

static int jk2_shm_attach(jk_env_t *env, jk_shm_t *shm)
{
    return apr_shm_attach((apr_shm_t **)&shm->privateData, shm->fname, globalShmPool );
}

/** Reset the scoreboard, in case it gets corrupted.
 *  Will remove all slots and set the head in the original state.
 */
static int jk2_shm_reset(jk_env_t *env, jk_shm_t *shm)
{
    if( shm->head == NULL ) {
        return JK_ERR;
    }
    memset(shm->image, 0, shm->size);

    shm->head->slotSize = shm->slotSize;
    shm->head->slotMaxCount = shm->slotMaxCount;
    shm->head->lastSlot = 1;
    
    env->l->jkLog(env, env->l, JK_LOG_INFO, 
                  "shm.init() Initalized %s %p\n",
                  shm->fname, shm->image);

    return JK_OK;
}

static int jk2_shm_dump(jk_env_t *env, jk_shm_t *shm, char *name)
{
    FILE *f;
    int i;

    env->l->jkLog(env, env->l, JK_LOG_INFO, "shm.dump(): Struct Size=%d slotSize=%d slotCnt=%d head=%p\n",
                  shm->size, shm->slotSize, shm->slotMaxCount, shm->head );

    if( shm->head==NULL ) return JK_ERR;

    env->l->jkLog(env, env->l, JK_LOG_INFO, "shm.dump(): shmem  slotSize=%d slotCnt=%d lastSlot=%d ver=%d\n",
                  shm->head->slotSize, shm->head->slotMaxCount, shm->head->lastSlot, shm->head->lbVer );

    for( i=1; i< shm->head->lastSlot; i++ ) {
        jk_shm_slot_t *slot=shm->getSlot( env, shm, i );
        jk_msg_t *msg;

        if( slot==NULL ) continue;
        msg=jk2_msg_ajp_create2( env, env->tmpPool, slot->data, slot->size);

        env->l->jkLog(env, env->l, JK_LOG_INFO, "shm.dump(): slot %d ver=%d size=%d name=%s\n",
                      i, slot->ver, slot->size, slot->name );

        msg->dump( env, msg, "Slot content ");
    }
    
    if( name==NULL ) return JK_ERR;
    
    f=fopen(name, "a+");
    fwrite( shm->head, 1, shm->size, f ); 
    fclose( f );

    env->l->jkLog(env, env->l, JK_LOG_INFO, "shm.dump(): Dumped %d in %s\n",
                  shm->size, name);
    
    return JK_OK;
}


/* Create or reinit an existing scoreboard. The MPM can control whether
 * the scoreboard is shared across multiple processes or not
 */
static int JK_METHOD jk2_shm_init(struct jk_env *env, jk_shm_t *shm) {
    apr_status_t rv=APR_SUCCESS;
    jk_shm_head_t *head;
    
    if( shm->fname==NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, "shm.init(): No file\n");
        return JK_ERR;
    }

    if( shm->size == 0  ) {
        shm->size = shm->slotSize * shm->slotMaxCount;
    }
    
    /* We don't want to have to recreate the scoreboard after
     * restarts, so we'll create a global pool and never clean it.
     */
    apr_initialize(); 
    rv = apr_pool_create(&globalShmPool, NULL);

    if (rv != APR_SUCCESS) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "Unable to create global pool for jk_shm\n");
        return rv;
    }
    
    shm->privateData=NULL;

    rv=apr_shm_attach((apr_shm_t **)&shm->privateData, shm->fname, globalShmPool );
    if( rv ) {
        char error[256];
        apr_strerror( rv, error, 256 );
        
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.create(): error attaching shm, will create %s %d %p %s\n",
                      shm->fname, rv, globalShmPool, error );
        shm->privateData=NULL;
    } else {
        env->l->jkLog(env, env->l, JK_LOG_INFO, 
                      "shm.create(): attaching to existing shm %s\n",
                      shm->fname );
        /* Get the base address, initialize it */
        shm->image = apr_shm_baseaddr_get( (apr_shm_t *)shm->privateData);
        shm->head = (jk_shm_head_t *)shm->image;
        
        if( shm->image==NULL ) {
            env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                          "shm.create(): No base memory %s\n", shm->fname);
            return JK_ERR;
        }
        return JK_OK;
    }

    if( shm->size <= 0 ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.create(): No size %s\n", shm->fname);
        return JK_ERR;
    }
    /* make sure it's an absolute pathname */
    /*  fname = ap_server_root_relative(pconf, ap_scoreboard_fname); */

    /* The shared memory file must not exist before we create the
     * segment. */
    rv = apr_file_remove(shm->fname, globalShmPool ); /* ignore errors */
    if (rv) {
        char error[256];
        apr_strerror( rv, error, 256 );
        
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.create(): error removing shm %s %d %s\n",
                      shm->fname, rv, error );
        shm->privateData=NULL;
    } else {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.create(): file removed ok\n");
    }

    rv = apr_shm_create((apr_shm_t **)&shm->privateData,(apr_size_t)shm->size,
                        shm->fname, globalShmPool);
    
    if (rv!=JK_OK) {
        char error[256];
        apr_strerror( rv, error, 256 );
        
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.create(): error creating shm %d %s %d %s\n",
                      shm->size, 
                      shm->fname, rv, error );
        shm->privateData=NULL;
        return rv;
    }

    if(  shm->privateData==NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "Unable to create %s %d\n", shm->fname, rv);
        return JK_ERR;
    }

    /* Get the base address, initialize it */
    shm->image = apr_shm_baseaddr_get( (apr_shm_t *)shm->privateData);
    shm->head = (jk_shm_head_t *)shm->image;

    if( shm->image==NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.create(): No memory allocated %s\n",
                      shm->fname);
        return JK_ERR;
    }

    jk2_shm_reset( env, shm );

    return JK_OK;
}

/* pos starts with 1 ( 0 is the head )
 */
jk_shm_slot_t * JK_METHOD jk2_shm_getSlot(struct jk_env *env, struct jk_shm *shm, int pos)
{
    if( pos==0 ) return NULL;
    if( shm->image==NULL ) return NULL;
    if( pos > shm->slotMaxCount ) return NULL;
    /* Pointer aritmethic, I hope it's right */
    return (jk_shm_slot_t *)((long)shm->image + (pos * shm->slotSize));
}

jk_shm_slot_t * JK_METHOD jk2_shm_createSlot(struct jk_env *env, struct jk_shm *shm, 
                                  char *name, int size)
{
    /* For now all slots are equal size
     */
    int slotId;
    int i;
    jk_shm_slot_t *slot;
    
    for( i=1; i<shm->head->lastSlot; i++ ) {
        slot= shm->getSlot( env, shm, i );
        if( strncmp( slot->name, name, strlen(name) ) == 0 ) {
            return slot;
        }
    }
    /* New slot */
    /* XXX interprocess sync */
    slotId=shm->head->lastSlot++;
    slot=shm->getSlot( env, shm, slotId );
    
    env->l->jkLog(env, env->l, JK_LOG_INFO, 
                  "shm.createSlot() Create %d %p %p\n", slotId, shm->image, slot );
    strncpy(slot->name, name, 64 );
    
    return slot;
}

/** Get an ID that is unique across processes.
 */
int JK_METHOD jk2_shm_getId(struct jk_env *env, struct jk_shm *shm)
{

    return 0;
}



static int JK_METHOD jk2_shm_setAttribute( jk_env_t *env, jk_bean_t *mbean, char *name, void *valueP ) {
    jk_shm_t *shm=(jk_shm_t *)mbean->object;
    char *value=(char *)valueP;
    
    if( strcmp( "file", name ) == 0 ) {
	shm->fname=value;
    } else if( strcmp( "size", name ) == 0 ) {
	shm->size=atoi(value);
    } else {
	return JK_ERR;
    }
    return JK_OK;   

}

/** Copy a chunk of data into a named slot
 */
static int jk2_shm_writeSlot( jk_env_t *env, jk_shm_t *shm,
                              char *instanceName, char *buf, int len )
{
    jk_shm_slot_t *slot;
    
    env->l->jkLog(env, env->l, JK_LOG_INFO, 
                  "shm.writeSlot() %s %d\n", instanceName, len );
    if( len > shm->slotSize ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.writeSlot() Packet too large %d %d\n",
                      shm->slotSize, len );
        return JK_ERR;
    }
    if( shm->head == NULL ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, 
                      "shm.writeSlot() No head - shm was not initalized\n");
        return JK_ERR;
    }
    slot=shm->createSlot( env, shm, instanceName, 0 );
    
    /*  Copy the body in the slot */
    memcpy( slot->data, buf, len );
    slot->size=len;
    slot->ver++;
    /* Update the head lb version number - that would triger
       reconf on the next request */
    shm->head->lbVer++;

    return JK_OK;
}

/* ==================== Dispatch messages from java ==================== */
    
/** Called by java. Will call the right shm method.
 */
static int JK_METHOD jk2_shm_dispatch(jk_env_t *env, void *target, jk_endpoint_t *ep, jk_msg_t *msg)
{
    jk_bean_t *bean=(jk_bean_t *)target;
    jk_shm_t *shm=(jk_shm_t *)bean->object;
    int rc;

    int code=msg->getByte(env, msg );
    
    if( shm->mbean->debug > 0 )
        env->l->jkLog(env, env->l, JK_LOG_INFO, 
                      "shm.%d() \n", code);
    
    switch( code ) {
    case SHM_SET_ATTRIBUTE: {
        char *name=msg->getString( env, msg );
        char *value=msg->getString( env, msg );
        if( shm->mbean->debug > 0 )
            env->l->jkLog(env, env->l, JK_LOG_INFO, 
                          "shm.setAttribute() %s %s %p\n", name, value, bean->setAttribute);
        if( bean->setAttribute != NULL)
            bean->setAttribute(env, bean, name, value );
        return JK_OK;
    }
    case SHM_ATTACH: {
        if( shm->mbean->debug > 0 )
            env->l->jkLog(env, env->l, JK_LOG_INFO, 
                          "shm.init()\n");
        rc=shm->init(env, shm);
        return rc;
    }
    case SHM_DETACH: {

        return JK_OK;
    }
    case SHM_WRITE_SLOT: {
        char *instanceName=msg->getString( env, msg );
        char *buf=msg->buf;
        int len=msg->len;

        return jk2_shm_writeSlot( env, shm, instanceName, buf, len );
    }
    case SHM_RESET: {
        jk2_shm_reset( env, shm );

        return JK_OK;
    }
    case SHM_DUMP: {
        char *name=msg->getString( env, msg );

        jk2_shm_dump( env, shm, name );
        return JK_OK;
    }
    }/* switch */
    return JK_ERR;
}

static int JK_METHOD jk2_shm_setWorkerEnv( jk_env_t *env, jk_shm_t *shm, jk_workerEnv_t *wEnv ) {
    wEnv->registerHandler( env, wEnv, "shm",
                           "shmDispatch", JK_HANDLE_SHM_DISPATCH,
                           jk2_shm_dispatch, NULL );
}

int JK_METHOD jk2_shm_factory( jk_env_t *env ,jk_pool_t *pool,
                               jk_bean_t *result,
                               const char *type, const char *name)
{
    jk_shm_t *shm;
    jk_workerEnv_t *wEnv;

    shm=(jk_shm_t *)pool->calloc(env, pool, sizeof(jk_shm_t));

    if( shm == NULL )
        return JK_ERR;

    shm->pool=pool;
    shm->privateData=NULL;

    shm->slotSize=DEFAULT_SLOT_SIZE;
    shm->slotMaxCount=DEFAULT_SLOT_COUNT;
    
    result->setAttribute=jk2_shm_setAttribute;
    /* result->getAttribute=jk2_shm_getAttribute; */
    shm->mbean=result; 
    result->object=shm;
    
    shm->getSlot=jk2_shm_getSlot;
    shm->createSlot=jk2_shm_createSlot;
    shm->getId=jk2_shm_getId;
    shm->init=jk2_shm_init;
    shm->destroy=jk2_shm_detach;
    shm->setWorkerEnv=jk2_shm_setWorkerEnv;
    
    return JK_OK;
}

#endif /* APR_HAS_SHARED_MEMORY */
#endif /* HAS_APR */

