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
 * @package Main
 * @author  Pier Fumagalli <mailto:pier.fumagalli@eng.sun.com>
 * @version $Id$
 */
#ifndef _WA_MAIN_H_
#define _WA_MAIN_H_

/**
 * A simple chain structure holding lists of pointers.
 */
struct wa_chain {
    /** The pointer to the current element in the chain. */
    void *curr;
    /**
     * The pointer to the next chain structure containing the next element
     * or <b>NULL</b> if this is the last element in the chain. */
    wa_chain *next;
};

/**
 * Initialize the WebApp Library.
 * This function must be called <b>before</b> any other calls to any other
 * function to set up the APR and WebApp Library internals. If any other
 * function is called before this function has been invoked will result in
 * impredictable results.
 *
 * @return <b>NULL</b> on success or an error message on faliure.
 */
const char *wa_init(void);

/**
 * Clean up the WebApp Library.
 * This function releases all memory and resouces used by the WebApp library
 * and must be called before the underlying web server process exits. Any call
 * to any other WebApp Library function after this function has been invoked
 * will result in impredictable results.
 *
 * @return <b>NULL</b> on success or an error message on faliure.
 */
const char *wa_destroy(void);

/**
 * Deploy a web-application.
 *
 * @param a The <code>wa_application</code> member of the web-application to
 *          deploy.
 * @param h The <code>wa_virtualhost</code> member of the host under which the
 *          web-application has to be deployed.
 * @param c The <code>wa_connection</code> member of the connection used to
 *          reach the application.
 * @return <b>NULL</b> on success or an error message on faliure.
 */
const char *wa_deploy(wa_application *a,
                      wa_virtualhost *h,
                      wa_connection *c);

/**
 * Dump some debugging information.
 *
 * @param f The file where this function was called.
 * @param l The line number where this function was called.
 * @param fmt The format string of the debug message (printf style).
 * @param others The parameters to the format string.
 */
void wa_debug(const char *f, const int l, const char *fmt, ...);

/**
 * The WebApp library memory pool.
 */
extern apr_pool_t *wa_pool;

/**
 * The configuration member of the library.
 * <br>
 * This list of <code>wa_chain</code> structure contain the configuration of
 * the WebApp Library in the form of all deployed <code>wa_application</code>,
 * <code>wa_virtualhost</code> and <code>wa_connection</code> structures.
 * <br>
 * The <code>curr</code> member in the <code>wa_chain</code> structure contains
 * always a <code>wa_virtualhost</code> structure, from which all configured
 * <code>wa_application</code> members and relative <code>wa_connection</code>
 * members can be retrieved.
 */
extern wa_chain *wa_configuration;

#endif /* ifndef _WA_MAIN_H_ */
