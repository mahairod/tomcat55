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
 * Channel using 'plain' TCP sockets or UNIX sockets.
 * Based on jk_sockbuf. It uses a an APR-based mechanism.
 * The UNIX sockets are not yet in APR (the code has to been written).
 * 
 * Properties:
 *  - host/filename
 *  - port
 *  - ndelay (Where the hell we set it?)
 *
 * This channel should 'live' as much as the workerenv. It is stateless.
 * It allocates memory for endpoint private data ( using endpoint's pool ).
 *
 * @author:  Gal Shachor <shachor@il.ibm.com>                           
 * @author: Costin Manolache
 * @author: Jean-Frederic Clere <jfrederic.clere@fujitsu-siemens.com>
 */

#include "jk_map.h"
#include "jk_env.h"
#include "jk_channel.h"
#include "jk_global.h"

#include <string.h>
#include "jk_registry.h"

#include "apr_network_io.h"
#include "apr_errno.h"
#include "apr_general.h"


#define DEFAULT_HOST "127.0.0.1"
#define TYPE_UNIX 1 /* to be move in APR. */
#define TYPE_NET  2 /* to be move in APR. */

/** Information specific for the socket channel
 */
struct jk_channel_socket_private {
    int ndelay;
    apr_sockaddr_t *addr;
    struct sockaddr_un unix_addr;
    int type; /* AF_INET or AF_UNIX */
    char *host;
    short port;
};

/** Informations for each connection
 */
typedef struct jk_channel_socket_data {
    int type; /* AF_INET or AF_UNIX */
    apr_socket_t *sock;
    int unixsock;
} jk_channel_socket_data_t;

typedef struct jk_channel_socket_private jk_channel_socket_private_t;

/*
  We use the _privateInt field directly. Long term we can define our own
  jk_channel_socket_t structure and use the _private field, etc - but we 
  just need to store an int.

  XXX We could also use properties or 'notes'
*/

int JK_METHOD jk_channel_socket_factory(jk_env_t *env, jk_pool_t *pool,
                                        void **result,
                                        const char *type, const char *name);

static int JK_METHOD jk_channel_socket_resolve(jk_env_t *env, char *host,
                                               short port,
                                               jk_channel_socket_private_t *rc);

static int JK_METHOD jk_channel_socket_close(jk_env_t *env, jk_channel_t *_this,
                                             jk_endpoint_t *endpoint);

static int JK_METHOD jk_channel_socket_getProperty(jk_env_t *env,
                                                   jk_channel_t *_this, 
                                                   char *name, char **value)
{
    return JK_FALSE;
}

static int JK_METHOD jk_channel_socket_setProperty(jk_env_t *env,
                                                   jk_channel_t *_this, 
                                                   char *name, char *value)
{
    jk_channel_socket_private_t *socketInfo=
        (jk_channel_socket_private_t *)(_this->_privatePtr);

    if( strcmp( "host", name ) != 0 ) {
        socketInfo->host=value;
    } else if( strcmp( "defaultPort", name ) != 0 ) {
    } else if( strcmp( "port", name ) != 0 ) {
    } else {
        return JK_FALSE;
    }
    return JK_TRUE;
}

/** resolve the host IP ( jk_resolve ) and initialize the channel.
 */
static int JK_METHOD jk_channel_socket_init(jk_env_t *env,
                                            jk_channel_t *_this, 
                                            jk_map_t *props,
                                            char *worker_name, 
                                            jk_worker_t *worker )
{
    int err;
    jk_channel_socket_private_t *socketInfo=
        (jk_channel_socket_private_t *)(_this->_privatePtr);
    char *host=socketInfo->host;
    short port=socketInfo->port;
    jk_workerEnv_t *workerEnv=worker->workerEnv;
    char *tmp;
    
    host = jk_map_getStrProp( env, props,
                              "worker", worker_name, "host", host);
    tmp = jk_map_getStrProp( env, props,
                          "worker", worker_name, "port", NULL );
    if( tmp != NULL )
        port=jk_map_str2int( env, tmp);

    _this->worker=worker;
    _this->properties=props;

    if( host==NULL )
        host=DEFAULT_HOST;
    
    err=jk_channel_socket_resolve( env, host, port, socketInfo );
    if( err!= JK_TRUE ) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR, "jk_channel_socket_init: "
               "can't resolve %s:%d errno=%d\n", host, port, errno );
    }
    env->l->jkLog(env, env->l, JK_LOG_INFO,
                  "channel_socket.init(): %s:%d for %s\n", host,
                  port, worker->name );

    return err;
}

/** private: resolve the address on init
 */
static int JK_METHOD jk_channel_socket_resolve(jk_env_t *env,
char *host, short port,
jk_channel_socket_private_t *rc)
{
    /*
     * If the hostname is an absolut path, we want a UNIX socket.
     * otherwise it is a TCP/IP socket.
     */ 
    env->l->jkLog(env, env->l, JK_LOG_ERROR,
                          "jk_channel_socket_resolve: %s %d\n",
                          host, port);
    if (host[0]=='/') {
        rc->type = TYPE_UNIX;
        memset(&rc->unix_addr, 0, sizeof(struct sockaddr_un));
        rc->unix_addr.sun_family = AF_UNIX;
        strcpy(rc->unix_addr.sun_path, host);
    } else {
        rc->type = TYPE_NET;
        if ( apr_sockaddr_info_get(&rc->addr, host, APR_UNSPEC, port, 0,
            (apr_pool_t *)env->globalPool->_private)!=APR_SUCCESS) {
            return JK_FALSE;
        }
    }
    return JK_TRUE;

}

static int jk_close_socket(jk_env_t *env, apr_socket_t *s)
{
    if (apr_socket_close(s)==APR_SUCCESS)
        return(0);
    else
        return(-1);
}


/** connect to Tomcat (jk_open_socket)
 */
static int JK_METHOD jk_channel_socket_open(jk_env_t *env,
                                            jk_channel_t *_this,
                                            jk_endpoint_t *endpoint)
{
    int err;
    jk_channel_socket_private_t *socketInfo=
        (jk_channel_socket_private_t *)(_this->_privatePtr);

    apr_sockaddr_t *remote_sa=socketInfo->addr;
    int ndelay=socketInfo->ndelay;
    jk_channel_socket_data_t *sd=endpoint->channelData;

    apr_socket_t *sock;
    apr_status_t ret;
    apr_interval_time_t timeout = 2 * APR_USEC_PER_SEC;
    char msg[128];

    int unixsock;

    /* UNIX socket (to be moved in APR) */
    if (socketInfo->type==TYPE_UNIX) {
        unixsock = socket(AF_UNIX, SOCK_STREAM, 0);
        if (unixsock<0) {
            env->l->jkLog(env, env->l, JK_LOG_ERROR,
                          "channelSocket.open(): can't create socket %d %s\n",
                          errno, strerror( errno ) );
            return JK_FALSE;
        }
        if (connect(unixsock,(struct sockaddr *)&(socketInfo->unix_addr),
                    sizeof(struct sockaddr_un))<0) {
            close(unixsock);
            env->l->jkLog(env, env->l, JK_LOG_ERROR,
                          "channelSocket.connect() connect failed %d %s\n",
                          errno, strerror( errno ) );
            return JK_FALSE;
        }
        /* store the channel information */
        if( sd==NULL ) {
            sd=(jk_channel_socket_data_t *)
                endpoint->pool->calloc( env, endpoint->pool,
                                        sizeof( jk_channel_socket_data_t ));
            endpoint->channelData=sd;
        }

        sd->unixsock = unixsock;
        sd->type = socketInfo->type;
        return JK_TRUE;
    }


    if (apr_socket_create(&sock, remote_sa->family, SOCK_STREAM,
                          (apr_pool_t *)env->globalPool->_private)
                         != APR_SUCCESS) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                 "channelSocket.open(): can't create socket %d %s\n",
                 errno, strerror( errno ) );
        return JK_FALSE;
    } 

    if (apr_setsocketopt(sock, APR_SO_TIMEOUT, timeout)!= APR_SUCCESS) {
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                 "channelSocket.open(): can't set timeout %d %s\n",
                 errno, strerror( errno ) );
        return JK_FALSE;
    }

    /* Tries to connect to JServ (continues trying while error is EINTR) */
    do {
        env->l->jkLog(env, env->l, JK_LOG_INFO,
                      "channelSocket.open() connect on %d\n",sock);
        ret = apr_connect(sock, remote_sa);
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "jk_channel_socket_open:%d\n",ret);

    } while (ret == APR_EINTR);

    /* Check if we connected */
    if(ret != APR_SUCCESS ) {
        apr_socket_close(sock);
        env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "channelSocket.connect() connect failed %d %s\n",
                      ret, apr_strerror( ret, msg, sizeof(msg) ) );
        return JK_FALSE;
    }

    /* XXX needed?
    if(ndelay) {
        int set = 1;
        setsockopt(sock, IPPROTO_TCP, TCP_NODELAY,(char *)&set,sizeof(set));
    }
        
    env->l->jkLog(env, env->l, JK_LOG_INFO,
                  "channelSocket.connect(), sock = %d\n", sock);
    */

    /* store the channel information */
    if( sd==NULL ) {
        sd=(jk_channel_socket_data_t *)
            endpoint->pool->calloc( env, endpoint->pool,
                                    sizeof( jk_channel_socket_data_t ));
        endpoint->channelData=sd;
    }
    sd->sock = sock;
    sd->type = socketInfo->type; /* APR should handle it. */

    return JK_TRUE;
}


/** close the socket  ( was: jk_close_socket )
*/
static int JK_METHOD jk_channel_socket_close(jk_env_t *env,jk_channel_t *_this,
                                             jk_endpoint_t *endpoint)
{
    apr_socket_t *sd;
    jk_channel_socket_data_t *chD=endpoint->channelData;
    if( chD==NULL ) 
        return JK_FALSE;

    sd=chD->sock;
    chD->sock=NULL; /* XXX check it. */
    /* nothing else to clean, the socket_data was allocated ouf of
     *  endpoint's pool
     */
    return jk_close_socket(env, sd);
}

/** send a long message
 * @param sd  opened socket.
 * @param b   buffer containing the data.
 * @param len length to send.
 * @return    -2: send returned 0 ? what this that ?
 *            -3: send failed.
 *            >0: total size send.
 * @bug       this fails on Unixes if len is too big for the underlying
 *             protocol.
 * @was: jk_tcp_socket_sendfull
 */
static int JK_METHOD jk_channel_socket_send(jk_env_t *env, jk_channel_t *_this,
                                            jk_endpoint_t *endpoint,
                                            char *b, int len) 
{
    apr_socket_t *sock;
    apr_status_t stat;
    apr_size_t length;
    char msg[128];

    int  sent=0;
    int this_time;
    int unixsock;

    jk_channel_socket_data_t *chD=endpoint->channelData;
    if( chD==NULL ) 
        return JK_FALSE;
    sock=chD->sock;
    unixsock=chD->unixsock;

    env->l->jkLog(env, env->l, JK_LOG_ERROR,
                      "jk_channel_socket_send %d\n",chD->type);

    if (chD->type==TYPE_NET) {
        length = (apr_size_t) len;
        stat = apr_send(sock, b, &length);
        if (stat!= APR_SUCCESS) {
            env->l->jkLog(env, env->l, JK_LOG_ERROR,
                          "jk_channel_socket_send send failed %d %s\n",
                          stat, apr_strerror( stat, msg, sizeof(msg) ) );
            return -3; /* -2 is not possible... */
        }
        return JK_TRUE;
    }

    while(sent < len) {
        this_time = send(unixsock, (char *)b + sent , len - sent,  0);
            
        if(0 == this_time) {
            return -2;
        }
        if(this_time < 0) {
            return -3;
        }
        sent += this_time;
    }
    /*     return sent; */
    return JK_TRUE;
}


/** receive len bytes.
 * @param sd  opened socket.
 * @param b   buffer to store the data.
 * @param len length to receive.
 * @return    -1: receive failed or connection closed.
 *            >0: length of the received data.
 * Was: tcp_socket_recvfull
 */
static int JK_METHOD jk_channel_socket_recv( jk_env_t *env, jk_channel_t *_this,
                                             jk_endpoint_t *endpoint,
                                             char *b, int len ) 
{
    jk_channel_socket_data_t *chD=endpoint->channelData;

    apr_socket_t *sock;
    apr_size_t length;
    apr_status_t stat;

    int sd;
    int rdlen;

    if( chD==NULL ) 
        return JK_FALSE;
    sd=chD->unixsock;
    sock=chD->sock;
    rdlen = 0;
  
    /* this should be moved in APR */ 
    if (chD->type==TYPE_UNIX) { 
        while(rdlen < len) {
            int this_time = recv(sd, 
                                 (char *)b + rdlen, 
                                 len - rdlen, 
                                 0);        
                   if(-1 == this_time) {
                if(EAGAIN == errno) {
                    continue;
                } 
                return -1;
            }
            if(0 == this_time) {
                return -1; 
            }
            rdlen += this_time;
        }
        return rdlen; 
    }

    length = (apr_size_t) len;
    stat =  apr_recv(sock, b, &length);

    if ( stat == APR_EOF)
        return -1; /* socket closed. */
    else if ( stat == APR_SUCCESS) {
        rdlen = (int) length;
        return rdlen; 
    } else
        return -1; /* any error. */
}



int JK_METHOD jk_channel_apr_socket_factory(jk_env_t *env,
                                        jk_pool_t *pool, 
                                        void **result,
                                        const char *type, const char *name)
{
    jk_channel_t *_this;
    
    if( strcmp( "channel", type ) != 0 ) {
        /* Wrong type  XXX throw */
        *result=NULL;
        return JK_FALSE;
    }
    _this=(jk_channel_t *)pool->calloc(env, pool, sizeof( jk_channel_t));
    
    _this->_privatePtr= (jk_channel_socket_private_t *)
        pool->calloc( env, pool, sizeof( jk_channel_socket_private_t));

    _this->recv= jk_channel_socket_recv; 
    _this->send= jk_channel_socket_send; 
    _this->init= jk_channel_socket_init; 
    _this->open= jk_channel_socket_open; 
    _this->close= jk_channel_socket_close; 
    _this->getProperty= jk_channel_socket_getProperty; 
    _this->setProperty= jk_channel_socket_setProperty; 

    _this->supportedProperties=( char ** )pool->alloc( env, pool,
                                                       4 * sizeof( char * ));
    _this->supportedProperties[0]="host";
    _this->supportedProperties[1]="port";
    _this->supportedProperties[2]="defaultPort";
    _this->supportedProperties[3]="\0";

    _this->name="file";

    *result= _this;
    
    return JK_TRUE;
}
