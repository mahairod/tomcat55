/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Description: Logger implementation using apache's native logging.
 *
 * This is the result of lazyness - a single log file to watch ( error.log )
 * instead of 2, no need to explain/document/decide where to place mod_jk
 * logging, etc.
 *
 * Normal apache logging rules apply.
 *
 * XXX Jk will use per/compoment logging level. All logs will be WARN level
 * in apache, and the filtering will happen on each component level.
 *
 * XXX Add file/line
 *
 * XXX Use env, use the current request structure ( so we can split the log
 * based on vhost configs ).
 *
 * @author Costin Manolache
 */ 

#include "jk_apache2.h"
#include <stdio.h>

#define HUGE_BUFFER_SIZE (8*1024)


static int JK_METHOD jk2_logger_apache2_log(jk_env_t *env, jk_logger_t *l,                                 
                                 int level,
                                 const char *what)
{
    return JK_OK;
}


static int JK_METHOD jk2_logger_apache2_init(jk_env_t *env, jk_logger_t *_this)
{
    return JK_OK;
}

static int JK_METHOD jk2_logger_apache2_close(jk_env_t *env, jk_logger_t *_this)
{
    return JK_OK;
}

static int JK_METHOD jk2_logger_apache2_jkVLog(jk_env_t *env, jk_logger_t *l,
                                     const char *file,
                                     int line,
                                     int level,
                                     const char *fmt,
                                     va_list args)
{
    /* XXX map jk level to apache level */
    server_rec *s=(server_rec *)l->logger_private;
    /* If we use apache2 logger, we should also use APR pools.
       It is possible to do some workarounds, but it would be stupid, especially
       since the idea is to use apr pools long term, with the old jk_pool as
       a workaround for apache13 and where apr is not available */
    apr_pool_t *aprPool=env->tmpPool->_private;
    int rc;
    char *buf;

    /* XXX XXX Change this to "SMALLSTACK" or something, I don't think it's
       netware specific */

    if( level < l->level )
        return JK_OK;

    if( s==NULL ) {
        return JK_ERR;
    }

    buf=apr_pvsprintf( aprPool, fmt, args );
    
    rc=strlen( buf );
    /* Remove trailing \n. XXX need to change the log() to not include \n */
    if( buf[rc-1] == '\n' )
        buf[rc-1]='\0';
    
    if( level == JK_LOG_DEBUG_LEVEL ) {
        ap_log_error( file, line, APLOG_DEBUG | APLOG_NOERRNO, 0, s, buf);
    } else if( level == JK_LOG_INFO_LEVEL ) {
        ap_log_error( file, line, APLOG_NOTICE | APLOG_NOERRNO, 0, s, buf);
    } else {
        ap_log_error( file, line, APLOG_ERR | APLOG_NOERRNO, 0, s, buf);
    }

    return rc ;
}

static int jk2_logger_apache2_jkLog(jk_env_t *env, jk_logger_t *l,
                                 const char *file,
                                 int line,
                                 int level,
                                 const char *fmt, ...)
{
    va_list args;
    int rc;
    
    va_start(args, fmt);
    rc=jk2_logger_apache2_jkVLog( env, l, file, line, level, fmt, args );
    va_end(args);

    return rc;
}


static int JK_METHOD
jk2_logger_file_setProperty(jk_env_t *env, jk_bean_t *mbean, 
                            char *name,  void *valueP )
{
    jk_logger_t *_this=mbean->object;
    char *value=valueP;

    if( strcmp( name, "level" )==0 ) {
        _this->level = jk2_logger_file_parseLogLevel(env, value);
        if( _this->level == JK_LOG_DEBUG_LEVEL ) {
            env->debug = 1;
            /*             _this->jkLog( env, _this, JK_LOG_ERROR, */
            /*                           "Level %s %d \n", value, _this->level ); */
        }
        return JK_OK;
    }
    return JK_ERR;
}



int JK_METHOD 
jk2_logger_apache2_factory(jk_env_t *env, jk_pool_t *pool, jk_bean_t *result,
                           const char *type, const char *name)
{
    jk_logger_t *l = (jk_logger_t *)pool->calloc(env, pool,
                                                 sizeof(jk_logger_t));

    if(l==NULL ) {
        return JK_ERR;
    }
    
    l->log = jk2_logger_apache2_log;
    l->logger_private = NULL;
    l->init =jk2_logger_apache2_init;
    l->jkLog = jk2_logger_apache2_jkLog;
    l->jkVLog = jk2_logger_apache2_jkVLog;

    l->level=JK_LOG_ERROR_LEVEL;
    
    result->object=(void *)l;
    l->mbean=result;
    result->setAttribute = jk2_logger_file_setProperty;

    return JK_OK;
}

