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
 * Description: AJP14 Login handler
 * Author:      Henri Gomez <hgomez@slib.fr>
 * Version:     $Revision$                                          
 */

#include "jk_global.h"
#include "jk_pool.h"
#include "jk_msg.h"
#include "jk_md5.h"
#include "jk_logger.h"
#include "jk_service.h"
#include "jk_env.h"
#include "jk_handler.h"
#include "jk_registry.h"

/* Private definitions */

/*
 * Second Login Phase (servlet engine -> web server), md5 seed is received
 */
#define AJP14_LOGSEED_CMD	(unsigned char)0x11

/*
 * Third Login Phase (web server -> servlet engine), md5 of seed + secret is sent
 */
#define AJP14_LOGCOMP_CMD	(unsigned char)0x12

/*
 * Login Accepted (servlet engine -> web server)
 */
#define AJP14_LOGOK_CMD		(unsigned char)0x13

/*
 * Login Rejected (servlet engine -> web server), will be logged
 */
#define AJP14_LOGNOK_CMD	(unsigned char)0x14

/* web-server want context info after login */
#define AJP14_CONTEXT_INFO_NEG      0x80000000 

/* web-server want context updates */
#define AJP14_CONTEXT_UPDATE_NEG    0x40000000 

/* communication could use AJP14 */
#define AJP14_PROTO_SUPPORT_AJP14_NEG   0x00010000 

int JK_METHOD jk_handler_logon_factory( jk_env_t *env,
                                        jk_pool_t *pool,
                                        void **result,
                                        char *type, char *name);

#define AJP14_ENTROPY_SEED_LEN		32  /* we're using MD5 => 32 chars */
#define AJP14_COMPUTED_KEY_LEN		32  /* we're using MD5 also */

/*
 * Decode the Login Command
 *
 * +-------------------------+---------------------------+
 * | LOGIN SEED CMD (1 byte) | MD5 of entropy (String) |
 * +-------------------------+---------------------------+
 *
 * Build the reply: 
 *   byte    LOGCOMP
 *   String  MD5 of random + Secret key
 *   long    negotiation
 *   String  serverName
 *
 */
static int jk_handler_login(jk_msg_t       *msg,
                            jk_ws_service_t *s,
                            jk_endpoint_t *ae,
                            jk_logger_t *l)
{
    int rc;
    char *entropy;
    char computedKey[AJP14_COMPUTED_KEY_LEN];
    char *secret=ae->worker->secret;

    entropy= msg->getString( msg );
    if( entropy==NULL ) {
        l->jkLog(l, JK_LOG_ERROR,
                 "Error ajp14_unmarshal_login_seed - can't get seed\n");
        return JK_HANDLER_FATAL;
    }

    l->jkLog(l, JK_LOG_INFO,
           "handle.logseed() received entropy %s\n", entropy);
    
    jk_md5((const unsigned char *)entropy,
           (const unsigned char *)secret, computedKey);

    l->jkLog(l, JK_LOG_DEBUG,
             "Into ajp14_compute_md5 (%s/%s) -> (%s)\n",
             entropy, secret, computedKey);

    msg->reset( msg );

    l->jkLog(l, JK_LOG_DEBUG, "Into ajp14_marshal_login_comp_into_msgb\n");

    rc=msg->appendByte( msg, AJP14_LOGCOMP_CMD);
    if (rc!=JK_TRUE )
        return JK_HANDLER_FATAL;

    /* COMPUTED-SEED */
    rc= msg->appendString( msg, (const unsigned char *)computedKey);
    if( rc!=JK_TRUE ) {
        l->jkLog(l, JK_LOG_ERROR,
                 "handler.loginSecret() error serializing computed secret\n");
        return JK_HANDLER_FATAL;
    }
    ae->negociation=
        (AJP14_CONTEXT_INFO_NEG | AJP14_PROTO_SUPPORT_AJP14_NEG);
    msg->appendLong(msg, ae->negociation);
    
    rc=msg->appendString(msg, ae->worker->workerEnv->server_name);

    if ( rc != JK_TRUE)
        return JK_HANDLER_FATAL;
    
    return JK_HANDLER_RESPONSE;
}


/*
 * Decode the LogOk Command. After that we're done, the connection is
 * perfect and ready.
 *
 * +--------------------+------------------------+---------------------------
 * | LOGOK CMD (1 byte) | NEGOCIED DATA (32bits) | SERVLET ENGINE INFO(CString)
 * +--------------------+------------------------+---------------------------
 *
 */
static int jk_handler_logok(jk_msg_t       *msg,
                            jk_ws_service_t *s,
                            jk_endpoint_t *ae,
                            jk_logger_t *l)
{
    unsigned long nego;
    char *sname;
    int rc;

    nego = msg->getLong(msg);
    
    if (nego == 0xFFFFFFFF) {
        l->jkLog(l, JK_LOG_ERROR,
                 "Error ajp14_unmarshal_log_ok - can't get negociated data\n");
        return JK_HANDLER_FATAL;
    }
    
    sname = (char *)msg->getString(msg);
    
    if (! sname) {
        l->jkLog(l, JK_LOG_ERROR,
                 "Error ajp14_unmarshal_log_ok - "
                 "can't get servlet engine name\n");
        return JK_HANDLER_FATAL;
    }
    
    /* take care of removing previously allocated data */
    /* XXXXXXXXX NEED A SUB POOL !!!! */
    if (ae->servletContainerName == NULL || 
        strcmp( sname, ae->servletContainerName) != 0 )  {
        ae->servletContainerName=(char *)ae->pool->pstrdup( ae->pool,
                                                            sname );
    }
    
    l->jkLog(l, JK_LOG_DEBUG,
             "Successfully connected to servlet-engine %s\n",
             ae->servletContainerName);

    return JK_HANDLER_LAST;
}


/*
 * Decode the Log Nok Command 
 *
 * +---------------------+-----------------------+
 * | LOGNOK CMD (1 byte) | FAILURE CODE (32bits) |
 * +---------------------+-----------------------+
 *
 */
static int jk_handler_lognok(jk_msg_t       *msg,
                             jk_ws_service_t *s,
                             jk_endpoint_t *ae,
                             jk_logger_t *l)
{
    unsigned long   status;
    
    status = msg->getLong(msg);
    
    l->jkLog(l, JK_LOG_INFO,
             "handler.logonFailure() code %08lx", status);
    
    return JK_HANDLER_FATAL;
}



/** Register handlers
 */
int JK_METHOD jk_handler_logon_factory( jk_env_t *env, jk_pool_t *pool,
                                        void **result,
                                        char *type, char *name)
{
    jk_map_t *map;
    jk_handler_t *h;
        
    map_alloc( &map, pool );
    *result=map;
    
    h=(jk_handler_t *)pool->calloc( pool, sizeof( jk_handler_t));
    h->name="login";
    h->messageId=AJP14_LOGSEED_CMD;
    h->callback=jk_handler_login;
    map_put( map, h->name, h, NULL );

    h=(jk_handler_t *)pool->calloc( pool, sizeof( jk_handler_t));
    h->name="logOk";
    h->messageId=AJP14_LOGOK_CMD;
    h->callback=jk_handler_logok;
    map_put( map, h->name, h, NULL );

    h=(jk_handler_t *)pool->calloc( pool, sizeof( jk_handler_t));
    h->name="logNok";
    h->messageId=AJP14_LOGNOK_CMD;
    h->callback=jk_handler_lognok;
    map_put( map, h->name, h, NULL );

    return JK_TRUE;
}
