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


import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implmentation of <b>Realm</b> that authenticates users via the <em>Java
 * Authentication and Authorization Service</em> (JAAS).  JAAS support requires
 * either JDK 1.4 (which includes it as part of the standard platform) or
 * JDK 1.3 (with the plug-in <code>jaas.jar</code> file).</p>
 *
 * <p>The value configured for the <code>appName</code> property is passed to
 * the <code>javax.security.auth.login.LoginContext</code> constructor, to
 * specify the <em>application name</em> used to select the set of relevant
 * <code>LoginModules</code> required.</p>
 *
 * <p><strong>FIXME</strong> - The following issues need to be addressed before
 * the use of this module is practical:</p>
 * <ul>
 * <li>If the <code>Subject</code> returned by the
 *     underyling JAAS <code>LoginMethod</code> implementation returns more
 *     than one <code>Principal</code>, the selected one exposed to web
 *     applications is arbitrary (it will be the first one returned by an
 *     Iterator over the returned Set).</li>
 * <li>It is not clear how roles should be mapped to Principals in a JAAS
 *     environment.  Therefore, authenticated Principals will appear to have
 *     no associated roles, and <code>hasRole()</code> will always
 *     return false.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class JAASRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The application name passed to the JAAS <code>LoginContext</code>,
     * which uses it to select the set of relevant <code>LoginModules</code>.
     */
    protected String appName = "Tomcat";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info =
        "org.apache.catalina.realm.JAASRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "JAASRealm";


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    // --------------------------------------------------------- Public Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * If there are any errors with the JDBC connection, executing
     * the query or anything we return null (don't authenticate). This
     * event is also logged, and the connection will be closed so that
     * a subsequent request will automatically re-open it.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        // Establish a LoginContext to use for authentication
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext
                (appName, new JAASCallbackHandler(this, username,
                                                  credentials));
        } catch (LoginException e) {
            log(sm.getString("jaasRealm.loginException", username), e);
            return (null);
        }

        // Negotiate a login via this LoginContext
        Subject subject = null;
        try {
            loginContext.login();
            subject = loginContext.getSubject();
            if (subject == null) {
                if (debug >= 2)
                    log(sm.getString("jaasRealm.failedLogin", username));
                return (null);
            }
        } catch (AccountExpiredException e) {
            if (debug >= 2)
                log(sm.getString("jaasRealm.accountExpired", username));
            return (null);
        } catch (CredentialExpiredException e) {
            if (debug >= 2)
                log(sm.getString("jaasRealm.credentialExpired", username));
            return (null);
        } catch (FailedLoginException e) {
            if (debug >= 2)
                log(sm.getString("jaasRealm.failedLogin", username));
            return (null);
        } catch (LoginException e) {
            log(sm.getString("jaasRealm.loginException", username), e);
            return (null);
        }

        // Return the appropriate Principal for this authenticated Subject
        if (debug >= 2)
            log(sm.getString("jaasRealm.authenticateSuccess", username));
        return (selectPrincipal(subject));

    }


    // -------------------------------------------------------- Package Methods


    // ------------------------------------------------------ Protected Methods


    /**
     * Return a short name for this Realm implementation.
     */
    protected String getName() {

        return (this.name);

    }


    /**
     * Return the password associated with the given principal's user name.
     */
    protected String getPassword(String username) {

        return (null);

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (null);

    }


    /**
     * Select and return the appropriate <code>Principal</code> to represent
     * the authenticated <code>Subject</code>.  The default implementation
     * simply returns the first <code>Principal</code> returned by an
     * iteration of the <code>Set</code> returned by calling
     * <code>subject.getPrincipals().iterator()</code>.  Subclasses can be
     * more intelligent, based on knowledge of the login methods in use.
     *
     * @param subject The <code>Subject</code> representing the
     *  authenticated user
     */
    protected Principal selectPrincipal(Subject subject) {

        Iterator principals = subject.getPrincipals().iterator();
        while (principals.hasNext()) {
            return ((Principal) principals.next());
        }
        return (null); // Should never happen

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     *
     * Prepare for active use of the public methods of this Component.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public void start() throws LifecycleException {

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

    }


}
