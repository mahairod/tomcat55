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

#ifndef JK_CHANNEL_H
#define JK_CHANNEL_H

#include "jk_global.h"
#include "jk_logger.h"
#include "jk_pool.h"
#include "jk_msg.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

struct jk_worker;
struct jk_endpoint;
struct jk_channel;

typedef struct jk_channel jk_channel_t;

/**
 * Abstraction (interface) for sending/receiving blocks of data ( packets ).
 * This will be used to replace the current TCP/socket code and allow other
 * transports. JNI, shmem, doors and other mechanisms can be tryed out, and 
 * we can also have a gradual transition to APR. The current tcp-based transport
 * will be refactored to this interface.
 * 
 * XXX Experimental
 * 
 * Issues:
 *  - Should the transport also check the packet integrity ( envelope ) ?
 *  - This is supposed to work for send/receive sequences, and the buffer
 *   shouldn't be modified until a send/receive is completed ( we want to
 *   avoid memcpy )
 *  - We need to extend/generalize the mechanisms in 'worker' to support other
 *   types of internal interfaces.
 *  - this interface is using get/setProperty model, like in Java beans ( well, 
 *    just a generic get/set method, no individual setters ). This is different
 *    from worker, which gets a map at startup. This model should also allow 
 *    run-time queries for status, management, etc - but it may be too complex.
 * 
 * @author Costin Manolache
 */
struct jk_channel {
    char *name;

    /** List of properties this channel 'knows'. 
     *  This will permit admin code to 'query' each channel
     *  and the setting code can better report errors.
     */
    char **supportedProperties;
    
    struct jk_worker *worker; 
    jk_map_t *properties;
    
    /** Prepare the channel, check the properties. This 
     * will resolve the host and do all the validations.
     * ( needed to remove the dependencies on sockets in ajp)
     * 
     * The channel may save or use data from the worker ( like cache
     *  the inet addr, etc )
     *  XXX revisit this - we may pass too much that is not needed
     */
    int (JK_METHOD *init)(jk_channel_t *_this,
			  jk_map_t *properties,
			  char *worker_name,
			  struct jk_worker *worker, 
			  jk_logger_t *l );
    
    /** Open the communication channel
     */
    int (JK_METHOD *open)(jk_channel_t *_this, 
			  struct jk_endpoint *endpoint );
    
    /** Close the communication channel
     */
    int (JK_METHOD *close)(jk_channel_t *_this, 
			   struct jk_endpoint *endpoint );
    
  /** Send a packet
   */
    int (JK_METHOD *send)(jk_channel_t *_this,
			  struct jk_endpoint *endpoint,
			  char *b, int len );
    
    /** Receive a packet
     */
    int (JK_METHOD *recv)(jk_channel_t *_this,
			  struct jk_endpoint *endpoint,
			  char *b, int len );
    
    /** Set a channel property. Properties are used to configure the 
     * communication channel ( example: port, host, file, shmem_name, etc).
     */
    int (JK_METHOD *setProperty)(jk_channel_t *_this, 
				 char *name, char *value);
    
    /** Get a channel property 
     */
    int (JK_METHOD *getProperty)(jk_channel_t *_this, 
			       char *name, char **value);
    
    void *_privatePtr;
};
    
#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif 
