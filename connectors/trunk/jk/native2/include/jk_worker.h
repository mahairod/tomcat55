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
 * Description: Workers controller header file                             *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           * 
 * Version:     $Revision$                                           *
 ***************************************************************************/

#ifndef JK_WORKER_H
#define JK_WORKER_H

#include "jk_env.h"
#include "jk_pool.h"
#include "jk_logger.h"
#include "jk_service.h"
#include "jk_endpoint.h"
#include "jk_map.h"
#include "jk_mt.h"
#include "jk_uriMap.h"
#include "jk_objCache.h"
#include "jk_msg.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

struct jk_worker;
struct jk_endpoint;
struct jk_env;
struct jk_objCache;
struct jk_msg;
struct jk_map;
typedef struct jk_worker jk_worker_t;

/*
 * The worker 'class', which represents something to which the web server
 * can delegate requests. 
 *
 * This can mean communicating with a particular servlet engine instance,
 * using a particular protocol.  A single web server instance may have
 * multiple workers communicating with a single servlet engine (it could be
 * using ajp12 for some requests and ajp13/ajp14 for others).  Or, a single web
 * server instance could have multiple workers communicating with different
 * servlet engines using the same protocol (it could be load balancing
 * among many engines, using ajp13/ajp14 for all communication).
 *
 * There is also a load balancing worker (jk_lb_worker.c), which itself
 * manages a group of workers.
 *
 * Web servers are configured to forward requests to a given worker.  To
 * handle those requests, the worker's get_endpoint method is called, and
 * then the service() method of that endpoint is called.
 *
 * As with all the core jk classes, this is essentially an abstract base
 * class which is implemented/extended by classes which are specific to a
 * particular protocol (or request-handling system).  By using an abstract
 * base class in this manner, plugins can be written for different servers
 * (e.g. IIS, Apache) without the plugins having to worry about which
 * protocol they are talking.
 *
 * This particular OO-in-C system uses a 'worker_private' pointer to
 * point to the protocol-specific data/functions.  So in the subclasses, the
 * methods do most of their work by getting their hands on the
 * worker_private pointer and then using that to get at the functions for
 * their protocol.
 *
 * Try imagining this as a 'public abstract class', and the
 * worker_private pointer as a sort of extra 'this' reference.  Or
 * imagine that you are seeing the internal vtables of your favorite OO
 * language.  Whatever works for you.
 *
 * See jk_ajp14_worker.c, jk_ajp13_worker.c and jk_ajp12_worker.c for examples.  
 */
struct jk_worker {

    struct jk_workerEnv *workerEnv;
    char *name;

    /** Pool for worker specific informations.
        In future we may start/stop/reload workers at runtime, but that's
        far away
    */
    struct jk_pool *pool;
    
    /* 
     * A 'this' pointer which is used by the subclasses of this class to
     * point to data/functions which are specific to a given protocol 
     * (e.g. ajp12 or ajp13 or ajp14).  
     */
    void *worker_private;
    
    /* XXX Add name and all other common properties !!! 
     */

    /** Communication channle used by the worker 
     */
    struct jk_channel *channel;

    /* XXX Stuff from ajp, some is generic, some not - need to
       sort out after */
    struct sockaddr_in worker_inet_addr; /* Contains host and port */
    int connect_retry_attempts;

    /** Reuse the endpoint and it's connection
     */
    struct jk_objCache *endpointCache;
    
    /* 
     * Open connections cache...
     *
     * 1. Critical section object to protect the cache.
     * 2. Cache size. 
     * 3. An array of "open" endpoints.
     */
    /*     JK_CRIT_SEC cs; */
    /*     int ep_cache_sz; */
    /*     struct jk_endpoint **ep_cache; */

    int proto;
    /* Password for ajp14+ connections. If null we default to ajp13.*/
    char * secret;

    /* Each worker can be part of a load-balancer scheme.
     * The information can be accessed by other components -
     * for example to report status, etc.
     */
    double  lb_factor;
    double  lb_value;
    int     in_error_state;
    int     in_recovering;
    time_t  error_time;

    /** If num_of_workers > 0 this is an load balancing worker
     */
    jk_worker_t **lb_workers;
    int num_of_workers;
    
    /*
     * For all of the below (except destroy), the first argument is
     * essentially a 'this' pointer.  
     */

    /*
     * Given a worker which is in the process of being created, and a list
     * of configuration options (or 'properties'), check to see if it the
     * options are.  This will always be called before the init() method.
     *
     * This is different from init - see the apache config process.
     * Validate should only do static checks on data ( if it has all
     * the info it needs and if it's valid ). Init() can do any
     * 'active' opertions.
     *
     * You can skip this by setting it to NULL.
     */
    int (JK_METHOD *validate)(struct jk_env *env, jk_worker_t *_this,
                              struct jk_map *props,
                              struct jk_workerEnv *we);

    /*
     * Do whatever initialization needs to be done to start this worker up.
     * Configuration options are passed in via the props parameter.  
     */
    int (JK_METHOD *init)(struct jk_env *env, jk_worker_t *_this,
                          struct jk_map *props,
                          struct jk_workerEnv *we );

    /*
     * Obtain an endpoint to service a particular request.  A pointer to
     * the endpoint is stored in pend. The done() method in the
     * endpoint will be called when the endpoint is no longer needed.
     */
    int (JK_METHOD *get_endpoint)(struct jk_env *env, jk_worker_t *_this,
                                  struct jk_endpoint **pend );

    /*
     * Called when this particular endpoint has finished processing a
     * request.  For some protocols (e.g. ajp12), this frees the memory
     * associated with the endpoint.  For others (e.g. ajp13/ajp14), this can
     * return the endpoint to a cache of already opened endpoints.  
     */
/*     int (JK_METHOD *done)(jk_env_t *env, */
/*                           jk_worker_t *_this, */
/*                           struct jk_endpoint *p ); */

    
    /*
     * Shutdown this worker. 
     */
    int (JK_METHOD *destroy)(struct jk_env *env, jk_worker_t *_thisP );
};

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* JK_WORKER_H */
