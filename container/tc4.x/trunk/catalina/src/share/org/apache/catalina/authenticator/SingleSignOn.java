/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.catalina.authenticator;


import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Container;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.util.StringManager;


/**
 * A <strong>Valve</strong> that supports a "single sign on" user experience,
 * where the security identity of a user who successfully authenticates to one
 * web application is propogated to other web applications in the same
 * security domain.  For successful use, the following requirements must
 * be met:
 * <ul>
 * <li>This Valve must be configured on the Container that represents a
 *     virtual host (typically an implementation of <code>Host</code>).</li>
 * <li>The <code>Realm</code> that contains the shared user and role
 *     information must be configured on the same Container (or a higher
 *     one), and not overridden at the web application level.</li>
 * <li>The web applications themselves must use one of the standard
 *     Authenticators found in the
 *     <code>org.apache.catalina.authenticator</code> package.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class SingleSignOn
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The cache of authenticated Principals, keyed by the cookie value that
     * is used to select them.
     */
    protected HashMap cache = new HashMap();


    /**
     * Descriptive information about this Valve implementation.
     */
    protected static String info =
        "org.apache.catalina.authenticator.SingleSignOn";


    /**
     * The string manager for this package.
     */
    protected final static StringManager sm =
	StringManager.getManager(Constants.Package);


    // ---------------------------------------------------------- Valve Methods


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Perform single-sign-on support processing for this request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        // If this is not an HTTP request and response, just pass them on
        if (!(request instanceof HttpRequest) ||
            !(response instanceof HttpResponse)) {
            invokeNext(request, response);
            return;
        }

        // Has a valid user already been authenticated?
        HttpServletRequest hreq =
            (HttpServletRequest) request.getRequest();
        if (hreq.getUserPrincipal() != null) {
            invokeNext(request, response);
            return;
        }

        // Check for the single sign on cookie
        log("Checking for SSO cookie");
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (Constants.SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }
        if (cookie == null) {
            invokeNext(request, response);
            return;
        }

        // Look up the cached Principal associated with this cookie value
        log("Checking for cached principal");
        Principal principal = lookup(cookie.getValue());
        if (principal != null) {
            log("Found cached principal '" + principal.getName() + "'");
            ((HttpRequest) request).setUserPrincipal(principal);
        }

        // Invoke the next Valve in our pipeline
        invokeNext(request, response);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Deregister the specified cookie value for the single sign on cookie.
     *
     * @param cookie Cookie value for the single sign on cookie to deregister
     */
    public void deregister(String cookie) {

        log("Deregistering cookie value '" + cookie + "'");

        synchronized (cache) {
            cache.remove(cookie);
        }

    }


    /**
     * Register the specified Principal as being associated with the specified
     * value for the single sign on cookie.
     *
     * @param cookie Cookie value for the single sign on cookie
     * @param principal Associated user principal that is identified
     */
    public void register(String cookie, Principal principal) {

        log("Registering cookie value '" + cookie + "' for user '" +
            principal.getName() + "'");

        synchronized (cache) {
            cache.put(cookie, principal);
        }

    }


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("SingleSignOn[");
        sb.append(container.getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

	Logger logger = container.getLogger();
	if (logger != null)
	    logger.log(this.toString() + ": " + message);
	else
	    System.out.println(this.toString() + ": " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

	Logger logger = container.getLogger();
	if (logger != null)
	    logger.log(this.toString() + ": " + message, throwable);
	else {
	    System.out.println(this.toString() + ": " + message);
	    throwable.printStackTrace(System.out);
	}

    }


    /**
     * Look up and return the cached Principal associated with this cookie
     * value, if there is one; otherwise return <code>null</code>.
     *
     * @param cookie Cookie value to look up
     */
    protected Principal lookup(String cookie) {

        // FIXME - No timeout checking on cached Principals
        synchronized (cache) {
            return ((Principal) cache.get(cookie));
        }

    }


}
