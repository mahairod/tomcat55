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
 * 4. The names  "The  Jakarta  Project",  "WebApp",  and  "Apache  Software *
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
 * @package Request
 * @author  Pier Fumagalli <mailto:pier.fumagalli@eng.sun.com>
 * @version $Id$
 */
#ifndef _WA_REQUEST_H_
#define _WA_REQUEST_H_

/* The host description data type. */
typedef struct wa_hostdata wa_hostdata;

/**
 * The host description structure.
 */
struct wa_hostdata {
	/**
	 * The host name.
	 */
	char *host;
	/**
	 * The host address (as string so that we don't have to deal with
	 * IPv4/IPv6 differences).
	 */
	char *addr;
	/**
	 * The port number.
	 */
	int port;
};

/**
 * The WebApp Library HTTP request structure.
 * <br>
 * This structure encapsulates an HTTP request to be handled within the scope
 * of one of the configured applications.
 */
struct wa_request {
	/**
	 * The APR memory pool where this request is allocated.
	 */
	apr_pool_t *pool;
	/* The web-server specific callback data passed when the functions
	 * specified in the <code>wa_callback</code> structure given at
	 * initialization are accessed.
	 */
	void *data;
	/**
	 * The HTTP method (ex. GET, POST...).
	 */
	char *meth;
	/**
	 * The HTTP request URI (ex. /webapp/index.html).
	 */
	char *ruri;
	/**
	 * The URL-encoded list of HTTP query arguments from the request.
	 */
    char *args;
    /**
     * The HTTP protocol name and version (ex. HTTP/1.0, HTTP/1.1...).
     */
    char *prot;
	/**
	 * The HTTP request URL scheme (the part before ://, ex http, https).
	 */
    char *schm;
    /**
     * The server host data.
     */
    wa_hostdata *serv;
	/**
	 * The client host data.
	 */
	wa_hostdata *clnt;
	/**
	 * The remote user name, if this request was authenticated by the web
	 * server, or <b>NULL</b>.
	 */
	char *user;
	/**
	 * The authentication method used by the web server to authenticate the
	 * remote user, or <b>NULL</b>.
	 */
    char *auth;
	/**
	 * The content length of this request.
	 */
    long clen;
	/**
	 * The number of bytes read out of this request body.
	 */
    long rlen;
	/**
	 * The current headers table.
	 */
	apr_table_t *hdrs;
};

/**
 * Allocate a new request structure.
 *
 * @param r A pointer to where the newly allocated <code>wa_request</code>
 *          structure must be allocated.
 * @param d The web-server specific data for this request.
 * @return An error message on faliure or <b>NULL</b>.
 */
const char *WA_AllocRequest(wa_request **r, void *d);

/**
 * Clean up and free the memory used by a request structure.
 *
 * @param r The request structure to destroy.
 * @return An error message on faliure or <b>NULL</b>.
 */
const char *WA_FreeRequest(wa_request *r);

/**
 * Invoke a request in a web application.
 *
 * @param r The WebApp Library request structure.
 * @param a The application to which this request needs to be forwarded.
 * @return The HTTP result code of this operation.
 */
int WA_InvokeRequest(wa_request *r, wa_application *a);

/**
 * Report the set up of a list of web applications to the client thru an
 * HTTP request.
 *
 * @param r The WebApp Library request structure.
 * @param a A <b>NULL</b> terminated list of applications for which a
 *          description should be generated.
 * @return The HTTP result code of this operation.
 */
int WA_InfoRequest(wa_request *r, wa_application **a);

#endif /* ifndef _WA_REQUEST_H_ */
