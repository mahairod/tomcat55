/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.authenticator;


import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.coyote.ActionCode;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;


/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of FORM BASED
 * Authentication, as described in the Servlet API Specification, Version 2.2.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class FormAuthenticator
    extends AuthenticatorBase {
    
    private static final Log log = LogFactory.getLog(FormAuthenticator.class);

    // ----------------------------------------------------- Instance Variables


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.authenticator.FormAuthenticator/1.0";

    /**
     * Character encoding to use to read the username and password parameters
     * from the request. If not set, the encoding of the request body will be
     * used.
     */
    protected String characterEncoding = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    @Override
    public String getInfo() {

        return (info);

    }


    /**
     * Return the character encoding to use to read the username and password.
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    
    /**
     * Set the character encoding to be used to read the username and password. 
     */
    public void setCharacterEncoding(String encoding) {
        characterEncoding = encoding;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.  Return <code>true</code> if any specified
     * constraint has been satisfied, or <code>false</code> if we have
     * created a response challenge already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config    Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public boolean authenticate(Request request,
                                HttpServletResponse response,
                                LoginConfig config)
        throws IOException {

        // References to objects we will need later
        Session session = null;

        // Have we already authenticated someone?
        Principal principal = request.getUserPrincipal();
        String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            if (log.isDebugEnabled())
                log.debug("Already authenticated '" +
                    principal.getName() + "'");
            // Associate the session with any existing SSO session
            if (ssoId != null)
                associate(ssoId, request.getSessionInternal(true));
            return (true);
        }

        // Is there an SSO session against which we can try to reauthenticate?
        if (ssoId != null) {
            if (log.isDebugEnabled())
                log.debug("SSO Id " + ssoId + " set; attempting " +
                          "reauthentication");
            // Try to reauthenticate using data cached by SSO.  If this fails,
            // either the original SSO logon was of DIGEST or SSL (which
            // we can't reauthenticate ourselves because there is no
            // cached username and password), or the realm denied
            // the user's reauthentication for some reason.
            // In either case we have to prompt the user for a logon */
            if (reauthenticateFromSSO(ssoId, request))
                return true;
        }

        // Have we authenticated this user before but have caching disabled?
        if (!cache) {
            session = request.getSessionInternal(true);
            if (log.isDebugEnabled())
                log.debug("Checking for reauthenticate in session " + session);
            String username =
                (String) session.getNote(Constants.SESS_USERNAME_NOTE);
            String password =
                (String) session.getNote(Constants.SESS_PASSWORD_NOTE);
            if ((username != null) && (password != null)) {
                if (log.isDebugEnabled())
                    log.debug("Reauthenticating username '" + username + "'");
                principal =
                    context.getRealm().authenticate(username, password);
                if (principal != null) {
                    session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);
                    if (!matchRequest(request)) {
                        register(request, response, principal,
                                 Constants.FORM_METHOD,
                                 username, password);
                        return (true);
                    }
                }
                if (log.isDebugEnabled())
                    log.debug("Reauthentication failed, proceed normally");
            }
        }

        // Is this the re-submit of the original request URI after successful
        // authentication?  If so, forward the *original* request instead.
        if (matchRequest(request)) {
            session = request.getSessionInternal(true);
            if (log.isDebugEnabled())
                log.debug("Restore request from session '"
                          + session.getIdInternal() 
                          + "'");
            principal = (Principal)
                session.getNote(Constants.FORM_PRINCIPAL_NOTE);
            register(request, response, principal, Constants.FORM_METHOD,
                     (String) session.getNote(Constants.SESS_USERNAME_NOTE),
                     (String) session.getNote(Constants.SESS_PASSWORD_NOTE));
            // If we're caching principals we no longer need the username
            // and password in the session, so remove them
            if (cache) {
                session.removeNote(Constants.SESS_USERNAME_NOTE);
                session.removeNote(Constants.SESS_PASSWORD_NOTE);
            }
            if (restoreRequest(request, session)) {
                if (log.isDebugEnabled())
                    log.debug("Proceed to restored request");
                return (true);
            } else {
                if (log.isDebugEnabled())
                    log.debug("Restore of original request failed");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return (false);
            }
        }

        // Acquire references to objects we will need to evaluate
        MessageBytes uriMB = MessageBytes.newInstance();
        CharChunk uriCC = uriMB.getCharChunk();
        uriCC.setLimit(-1);
        String contextPath = request.getContextPath();
        String requestURI = request.getDecodedRequestURI();

        // Is this the action request from the login page?
        boolean loginAction =
            requestURI.startsWith(contextPath) &&
            requestURI.endsWith(Constants.FORM_ACTION);

        // No -- Save this request and redirect to the form login page
        if (!loginAction) {
            session = request.getSessionInternal(true);
            if (log.isDebugEnabled())
                log.debug("Save request in session '" + session.getIdInternal() + "'");
            try {
                saveRequest(request, session);
            } catch (IOException ioe) {
                log.debug("Request body too big to save during authentication");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        sm.getString("authenticator.requestBodyTooBig"));
                return (false);
            }
            forwardToLoginPage(request, response, config);
            return (false);
        }

        // Yes -- Validate the specified credentials and redirect
        // to the error page if they are not correct
        Realm realm = context.getRealm();
        if (characterEncoding != null) {
            request.setCharacterEncoding(characterEncoding);
        }
        String username = request.getParameter(Constants.FORM_USERNAME);
        String password = request.getParameter(Constants.FORM_PASSWORD);
        if (log.isDebugEnabled())
            log.debug("Authenticating username '" + username + "'");
        principal = realm.authenticate(username, password);
        if (principal == null) {
            forwardToErrorPage(request, response, config);
            return (false);
        }

        if (log.isDebugEnabled())
            log.debug("Authentication of '" + username + "' was successful");

        if (session == null)
            session = request.getSessionInternal(false);
        if (session == null) {
            if (containerLog.isDebugEnabled())
                containerLog.debug
                    ("User took so long to log on the session expired");
            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
                               sm.getString("authenticator.sessionExpired"));
            return (false);
        }

        // Save the authenticated Principal in our session
        session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);

        // Save the username and password as well
        session.setNote(Constants.SESS_USERNAME_NOTE, username);
        session.setNote(Constants.SESS_PASSWORD_NOTE, password);

        // Redirect the user to the original request URI (which will cause
        // the original request to be restored)
        requestURI = savedRequestURL(session);
        if (log.isDebugEnabled())
            log.debug("Redirecting to original '" + requestURI + "'");
        if (requestURI == null)
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               sm.getString("authenticator.formlogin"));
        else
            response.sendRedirect(response.encodeRedirectURL(requestURI));
        return (false);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Called to forward to the login page
     * 
     * @param request Request we are processing
     * @param response Response we are populating
     * @param config    Login configuration describing how authentication
     *              should be performed
     */
    protected void forwardToLoginPage(Request request,
            HttpServletResponse response, LoginConfig config) {
        RequestDispatcher disp =
            context.getServletContext().getRequestDispatcher
            (config.getLoginPage());
        try {
            disp.forward(request.getRequest(), response);
        } catch (Throwable t) {
            log.warn("Unexpected error forwarding to login page", t);
        }
    }


    /**
     * Called to forward to the error page
     * 
     * @param request Request we are processing
     * @param response Response we are populating
     * @param config    Login configuration describing how authentication
     *              should be performed
     */
    protected void forwardToErrorPage(Request request,
            HttpServletResponse response, LoginConfig config) {
        RequestDispatcher disp =
            context.getServletContext().getRequestDispatcher
            (config.getErrorPage());
        try {
            disp.forward(request.getRequest(), response);
        } catch (Throwable t) {
            log.warn("Unexpected error forwarding to error page", t);
        }
    }


    /**
     * Does this request match the saved one (so that it must be the redirect
     * we signaled after successful authentication?
     *
     * @param request The request to be verified
     */
    protected boolean matchRequest(Request request) {

      // Has a session been created?
      Session session = request.getSessionInternal(false);
      if (session == null)
          return (false);

      // Is there a saved request?
      SavedRequest sreq = (SavedRequest)
          session.getNote(Constants.FORM_REQUEST_NOTE);
      if (sreq == null)
          return (false);

      // Is there a saved principal?
      if (session.getNote(Constants.FORM_PRINCIPAL_NOTE) == null)
          return (false);

      // Does the request URI match?
      String requestURI = request.getRequestURI();
      if (requestURI == null)
          return (false);
      return (requestURI.equals(sreq.getRequestURI()));

    }


    /**
     * Restore the original request from information stored in our session.
     * If the original request is no longer present (because the session
     * timed out), return <code>false</code>; otherwise, return
     * <code>true</code>.
     *
     * @param request The request to be restored
     * @param session The session containing the saved information
     */
    protected boolean restoreRequest(Request request, Session session) {

        // Retrieve and remove the SavedRequest object from our session
        SavedRequest saved = (SavedRequest)
            session.getNote(Constants.FORM_REQUEST_NOTE);
        session.removeNote(Constants.FORM_REQUEST_NOTE);
        session.removeNote(Constants.FORM_PRINCIPAL_NOTE);
        if (saved == null)
            return (false);

        // Modify our current request to reflect the original one
        request.clearCookies();
        Iterator<Cookie> cookies = saved.getCookies();
        while (cookies.hasNext()) {
            request.addCookie(cookies.next());
        }

        MimeHeaders rmh = request.getCoyoteRequest().getMimeHeaders();
        rmh.recycle();
        boolean cachable = "GET".equalsIgnoreCase(saved.getMethod()) ||
                           "HEAD".equalsIgnoreCase(saved.getMethod());
        Iterator<String> names = saved.getHeaderNames();
        while (names.hasNext()) {
            String name = names.next();
            // The browser isn't expecting this conditional response now.
            // Assuming that it can quietly recover from an unexpected 412.
            // BZ 43687
            if(!("If-Modified-Since".equalsIgnoreCase(name) ||
                 (cachable && "If-None-Match".equalsIgnoreCase(name)))) {
                Iterator<String> values = saved.getHeaderValues(name);
                while (values.hasNext()) {
                    rmh.addValue(name).setString(values.next());
                }
            }
        }
        
        request.clearLocales();
        Iterator<Locale> locales = saved.getLocales();
        while (locales.hasNext()) {
            request.addLocale(locales.next());
        }
        
        request.getCoyoteRequest().getParameters().recycle();
        request.getCoyoteRequest().getParameters().setQueryStringEncoding(
                request.getConnector().getURIEncoding());

        if ("POST".equalsIgnoreCase(saved.getMethod())) {
            ByteChunk body = saved.getBody();
            
            if (body != null) {
                request.getCoyoteRequest().action
                    (ActionCode.ACTION_REQ_SET_BODY_REPLAY, body);
    
                // Set content type
                MessageBytes contentType = MessageBytes.newInstance();
                
                //If no content type specified, use default for POST
                String savedContentType = saved.getContentType();
                if (savedContentType == null) {
                    savedContentType = "application/x-www-form-urlencoded";
                }

                contentType.setString(savedContentType);
                request.getCoyoteRequest().setContentType(contentType);
            }
        }
        request.getCoyoteRequest().method().setString(saved.getMethod());

        request.getCoyoteRequest().queryString().setString
            (saved.getQueryString());

        request.getCoyoteRequest().requestURI().setString
            (saved.getRequestURI());
        return (true);

    }


    /**
     * Save the original request information into our session.
     *
     * @param request The request to be saved
     * @param session The session to contain the saved information
     * @throws IOException
     */
    protected void saveRequest(Request request, Session session)
        throws IOException {

        // Create and populate a SavedRequest object for this request
        SavedRequest saved = new SavedRequest();
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                saved.addCookie(cookies[i]);
        }
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                saved.addHeader(name, value);
            }
        }
        Enumeration<Locale> locales = request.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = locales.nextElement();
            saved.addLocale(locale);
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            ByteChunk body = new ByteChunk();
            body.setLimit(request.getConnector().getMaxSavePostSize());

            byte[] buffer = new byte[4096];
            int bytesRead;
            InputStream is = request.getInputStream();
        
            while ( (bytesRead = is.read(buffer) ) >= 0) {
                body.append(buffer, 0, bytesRead);
            }
            saved.setContentType(request.getContentType());
            saved.setBody(body);
        }

        saved.setMethod(request.getMethod());
        saved.setQueryString(request.getQueryString());
        saved.setRequestURI(request.getRequestURI());

        // Stash the SavedRequest in our session for later use
        session.setNote(Constants.FORM_REQUEST_NOTE, saved);

    }


    /**
     * Return the request URI (with the corresponding query string, if any)
     * from the saved request so that we can redirect to it.
     *
     * @param session Our current session
     */
    protected String savedRequestURL(Session session) {

        SavedRequest saved =
            (SavedRequest) session.getNote(Constants.FORM_REQUEST_NOTE);
        if (saved == null)
            return (null);
        StringBuilder sb = new StringBuilder(saved.getRequestURI());
        if (saved.getQueryString() != null) {
            sb.append('?');
            sb.append(saved.getQueryString());
        }
        return (sb.toString());

    }


}
