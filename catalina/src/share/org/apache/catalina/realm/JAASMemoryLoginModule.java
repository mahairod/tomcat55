/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
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


package org.apache.catalina.realm;


import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Realm;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.RequestUtil;
import org.apache.commons.digester.Digester;


/**
 * <p>Implementation of the JAAS <strong>LoginModule</strong> interface,
 * primarily for use in testing <code>JAASRealm</code>.  It utilizes an
 * XML-format data file of username/password/role information identical to
 * that supported by <code>org.apache.catalina.realm.MemoryRealm</code>
 * (except that digested passwords are not supported).</p>
 *
 * <p>This class recognizes the following string-valued options, which are
 * specified in the configuration file (and passed to our constructor in
 * the <code>options</code> argument:</p>
 * <ul>
 * <li><strong>debug</strong> - Set to "true" to get debugging messages
 *     generated to System.out.  The default value is <code>false</code>.</li>
 * <li><strong>pathname</strong> - Relative (to the pathname specified by the
 *     "catalina.base" system property) or absolute pahtname to the
 *     XML file containing our user information, in the format supported by
 *     {@link MemoryRealm}.  The default value matches the MemoryRealm
 *     default.</li>
 * </ul>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - This class implements
 * <code>Realm</code> only to satisfy the calling requirements of the
 * <code>GenericPrincipal</code> constructor.  It does not actually perform
 * the functionality required of a <code>Realm</code> implementation.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class JAASMemoryLoginModule implements LoginModule, Realm {


    // ----------------------------------------------------- Instance Variables


    /**
     * The callback handler responsible for answering our requests.
     */
    protected CallbackHandler callbackHandler = null;


    /**
     * Has our own <code>commit()</code> returned successfully?
     */
    protected boolean committed = false;


    /**
     * Should we log debugging messages?
     */
    protected boolean debug = false;


    /**
     * The configuration information for this <code>LoginModule</code>.
     */
    protected Map options = null;


    /**
     * The absolute or relative pathname to the XML configuration file.
     */
    protected String pathname = "conf/tomcat-users.xml";


    /**
     * The <code>Principal</code> identified by our validation, or
     * <code>null</code> if validation falied.
     */
    protected Principal principal = null;


    /**
     * The set of <code>Principals</code> loaded from our configuration file.
     */
    protected HashMap principals = new HashMap();

    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The state information that is shared with other configured
     * <code>LoginModule</code> instances.
     */
    protected Map sharedState = null;


    /**
     * The subject for which we are performing authentication.
     */
    protected Subject subject = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new user to the in-memory database.
     *
     * @param username User's username
     * @param password User's password (clear text)
     * @param roles Comma-delimited set of roles associated with this user
     */
    void addUser(String username, String password, String roles) {

        // Accumulate the list of roles for this user
        ArrayList list = new ArrayList();
        roles += ",";
        while (true) {
            int comma = roles.indexOf(',');
            if (comma < 0)
                break;
            String role = roles.substring(0, comma).trim();
            list.add(role);
            roles = roles.substring(comma + 1);
        }

        // Construct and cache the Principal for this user
        GenericPrincipal principal =
            new GenericPrincipal(this, username, password, list);
        principals.put(username, principal);

    }


    /**
     * Phase 2 of authenticating a <code>Subject</code> when Phase 1
     * fails.  This method is called if the <code>LoginContext</code>
     * failed somewhere in the overall authentication chain.
     *
     * @return <code>true</code> if this method succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the abort fails
     */
    public boolean abort() throws LoginException {

        // If our authentication was not successful, just return false
        if (principal == null)
            return (false);

        // Clean up if overall authentication failed
        if (committed)
            logout();
        else {
            committed = false;
            principal = null;
        }
        return (true);

    }


    /**
     * Phase 2 of authenticating a <code>Subject</code> when Phase 1
     * was successful.  This method is called if the <code>LoginContext</code>
     * succeeded in the overall authentication chain.
     *
     * @return <code>true</code> if the authentication succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the commit fails
     */
    public boolean commit() throws LoginException {

        // If authentication was not successful, just return false
        if (principal == null)
            return (false);

        // Add our Principal to the Subject if needed
        if (!subject.getPrincipals().contains(principal))
            subject.getPrincipals().add(principal);
        committed = true;
        return (true);

    }

    
    /**
     * Return the SecurityConstraint configured to guard the request URI for
     * this request, or <code>null</code> if there is no such constraint.
     *
     * @param request Request we are processing
     */
    public SecurityConstraint findSecurityConstraint(HttpRequest request,
                                                     Context context) {

        // Are there any defined security constraints?
        SecurityConstraint constraints[] = context.findConstraints();
        if ((constraints == null) || (constraints.length == 0)) {
            if (debug)
                log("  No applicable constraints defined");
            return (null);
        }

        // Check each defined security constraint
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        String uri = request.getDecodedRequestURI();
        String contextPath = hreq.getContextPath();
        if (contextPath.length() > 0)
            uri = uri.substring(contextPath.length());
        uri = RequestUtil.URLDecode(uri); // Before checking constraints
        String method = hreq.getMethod();
        for (int i = 0; i < constraints.length; i++) {
            if (debug)
                log("  Checking constraint '" + constraints[i] +
                    "' against " + method + " " + uri + " --> " +
                    constraints[i].included(uri, method));
            if (constraints[i].included(uri, method))
                return (constraints[i]);
        }

        // No applicable security constraint was found
        if (debug)
            log("  No applicable constraint located");
        return (null);

    }
    
    
    /**
     * Initialize this <code>LoginModule</code> with the specified
     * configuration information.
     *
     * @param subject The <code>Subject</code> to be authenticated
     * @param callbackHandler A <code>CallbackHandler</code> for communicating
     *  with the end user as necessary
     * @param sharedState State information shared with other
     *  <code>LoginModule</code> instances
     * @param options Configuration information for this specific
     *  <code>LoginModule</code> instance
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {

        // Save configuration values
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        // Perform instance-specific initialization
        this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
        if (options.get("pathname") != null)
            this.pathname = (String) options.get("pathname");

        // Load our defined Principals
        load();

    }


    /**
     * Phase 1 of authenticating a <code>Subject</code>.
     *
     * @return <code>true</code> if the authentication succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the authentication fails
     */
    public boolean login() throws LoginException {

        // Set up our CallbackHandler requests
        if (callbackHandler == null)
            throw new LoginException("No CallbackHandler specified");
        Callback callbacks[] = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        // Interact with the user to retrieve the username and password
        String username = null;
        String password = null;
        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            password =
                new String(((PasswordCallback) callbacks[1]).getPassword());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.toString());
        }

        // Validate the username and password we have received
        principal = null; // FIXME - look up and check password

        // Report results based on success or failure
        if (principal != null) {
            return (true);
        } else {
            throw new
                FailedLoginException("Username or password is incorrect");
        }

    }


    /**
     * Log out this user.
     *
     * @return <code>true</code> in all cases because thie
     *  <code>LoginModule</code> should not be ignored
     *
     * @exception LoginException if logging out failed
     */
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(principal);
        committed = false;
        principal = null;
        return (true);

    }


    // ---------------------------------------------------------- Realm Methods


    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {

        return (null);

    }


    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        ;

    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (null);

    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        ;

    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        return (null);

    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, byte[] credentials) {

        return (null);

    }


    /**
     * Return the Principal associated with the specified username, which
     * matches the digest calculated using the given parameters using the
     * method described in RFC 2069; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param digest Digest which has been submitted by the client
     * @param nonce Unique (or supposedly unique) token which has been used
     * for this request
     * @param realm Realm name
     * @param md5a2 Second MD5 digest used to calculate the digest :
     * MD5(Method + ":" + uri)
     */
    public Principal authenticate(String username, String digest,
                                  String nonce, String nc, String cnonce,
                                  String qop, String realm,
                                  String md5a2) {

        return (null);

    }


    /**
     * Return the Principal associated with the specified chain of X509
     * client certificates.  If there is none, return <code>null</code>.
     *
     * @param certs Array of client certificates, with the first one in
     *  the array being the certificate of the client itself.
     */
    public Principal authenticate(X509Certificate certs[]) {

        return (null);

    }


    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {

        return (false);

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        ;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Load the contents of our configuration file.
     */
    protected void load() {

        // Validate the existence of our configuration file
        File file = new File(pathname);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), pathname);
        if (!file.exists() || !file.canRead()) {
            log("Cannot load configuration file " + file.getAbsolutePath());
            return;
        }

        // Load the contents of our configuration file
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addRuleSet(new MemoryRuleSet());
        try {
            digester.push(this);
            digester.parse(file);
        } catch (Exception e) {
            log("Error processing configuration file " +
                file.getAbsolutePath(), e);
            return;
        }

    }


    /**
     * Log a message.
     *
     * @param message The message to be logged
     */
    protected void log(String message) {

        System.out.print("JAASMemoryLoginModule: ");
        System.out.println(message);

    }


    /**
     * Log a message and associated exception.
     *
     * @param message The message to be logged
     * @param exception The associated exception
     */
    protected void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }
    
    /**
     * Perform access control based on the specified authorization constraint.
     * Return <code>true</code> if this constraint is satisfied and processing
     * should continue, or <code>false</code> otherwise.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param constraint Security constraint we are enforcing
     * @param The Context to which client of this class is attached.
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean hasResourcePermission(HttpRequest request,
                                         HttpResponse response,
                                         SecurityConstraint constraint,
                                         Context context)
        throws IOException {

        if (constraint == null)
            return (true);

        // Specifically allow access to the form login and form error pages
        // and the "j_security_check" action
        LoginConfig config = context.getLoginConfig();
        if ((config != null) &&
            (Constants.FORM_METHOD.equals(config.getAuthMethod()))) {
            String requestURI = request.getDecodedRequestURI();
            String loginPage = context.getPath() + config.getLoginPage();
            if (loginPage.equals(requestURI)) {
                if (debug)
                    log(" Allow access to login page " + loginPage);
                return (true);
            }
            String errorPage = context.getPath() + config.getErrorPage();
            if (errorPage.equals(requestURI)) {
                if (debug)
                    log(" Allow access to error page " + errorPage);
                return (true);
            }
            if (requestURI.endsWith(Constants.FORM_ACTION)) {
                if (debug)
                    log(" Allow access to username/password submission");
                return (true);
            }
        }

        // Which user principal have we already authenticated?
        Principal principal =
            ((HttpServletRequest) request.getRequest()).getUserPrincipal();
        if (principal == null) {
            if (debug)
                log("  No user authenticated, cannot grant access");
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 sm.getString("realmBase.notAuthenticated"));
            return (false);
        }

        String roles[] = constraint.findAuthRoles();
        if (roles == null)
            roles = new String[0];

        if (constraint.getAllRoles())
            return (true);
        if ((roles.length == 0) && (constraint.getAuthConstraint())) {
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_FORBIDDEN,
                 sm.getString("realmBase.forbidden"));
            return (false); // No listed roles means no access at all
        }
        for (int i = 0; i < roles.length; i++) {
            if (hasRole(principal, roles[i]))
                return (true);
        }

        // Return a "Forbidden" message denying access to this resource
        ((HttpServletResponse) response.getResponse()).sendError
            (HttpServletResponse.SC_FORBIDDEN,
             sm.getString("realmBase.forbidden"));
        return (false);

    } 
    
    /**
     * Enforce any user data constraint required by the security constraint
     * guarding this request URI.  Return <code>true</code> if this constraint
     * was not violated and processing should continue, or <code>false</code>
     * if we have created a response already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param constraint Security constraint being checked
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean hasUserDataPermission(HttpRequest request,
                                         HttpResponse response,
                                         SecurityConstraint constraint)
        throws IOException {

        // Is there a relevant user data constraint?
        if (constraint == null) {
            if (debug)
                log("  No applicable security constraint defined");
            return (true);
        }
        String userConstraint = constraint.getUserConstraint();
        if (userConstraint == null) {
            if (debug)
                log("  No applicable user data constraint defined");
            return (true);
        }
        if (userConstraint.equals(Constants.NONE_TRANSPORT)) {
            if (debug)
                log("  User data constraint has no restrictions");
            return (true);
        }

        // Validate the request against the user data constraint
        if (request.getRequest().isSecure()) {
            if (debug)
                log("  User data constraint already satisfied");
            return (true);
        }

        // Initialize variables we need to determine the appropriate action
        HttpServletRequest hrequest =
            (HttpServletRequest) request.getRequest();
        HttpServletResponse hresponse =
            (HttpServletResponse) response.getResponse();
        int redirectPort = request.getConnector().getRedirectPort();

        // Is redirecting disabled?
        if (redirectPort <= 0) {
            if (debug)
                log("  SSL redirect is disabled");
            hresponse.sendError
                (HttpServletResponse.SC_FORBIDDEN,
                 hrequest.getRequestURI());
            return (false);
        }

        // Redirect to the corresponding SSL port
        String protocol = "https";
        String host = hrequest.getServerName();
        StringBuffer file = new StringBuffer(hrequest.getRequestURI());
        String requestedSessionId = hrequest.getRequestedSessionId();
        if ((requestedSessionId != null) &&
            hrequest.isRequestedSessionIdFromURL()) {
            file.append(";jsessionid=");
            file.append(requestedSessionId);
        }
        String queryString = hrequest.getQueryString();
        if (queryString != null) {
            file.append('?');
            file.append(queryString);
        }
        URL url = null;
        try {
            url = new URL(protocol, host, redirectPort, file.toString());
            if (debug)
                log("  Redirecting to " + url.toString());
            hresponse.sendRedirect(url.toString());
            return (false);
        } catch (MalformedURLException e) {
            if (debug)
                log("  Cannot create new URL", e);
            hresponse.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 hrequest.getRequestURI());
            return (false);
        }

    }


}
