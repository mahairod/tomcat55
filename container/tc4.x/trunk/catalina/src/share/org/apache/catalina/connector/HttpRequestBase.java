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


package org.apache.catalina.connector;


import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.util.Enumerator;


/**
 * Convenience base implementation of the <b>HttpRequest</b> interface, which
 * can be used for the Request implementation required by most Connectors that
 * implement the HTTP protocol.  Only the connector-specific methods need to
 * be implemented.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class HttpRequestBase
    extends RequestBase
    implements HttpRequest, HttpServletRequest {


    // ----------------------------------------------------- Instance Variables


    /**
     * The authentication type used for this request.
     */
    protected String authType = null;


    /**
     * The context path for this request.
     */
    protected String contextPath = "";


    /**
     * The set of cookies associated with this Request.
     */
    protected ArrayList cookies = new ArrayList();


    /**
     * An empty collection to use for returning empty Enumerations.  Do not
     * add any elements to this collection!
     */
    protected static ArrayList empty = new ArrayList();


    /**
     * The set of SimpleDateFormat formats to use in getDateHeader().
     */
    protected static SimpleDateFormat formats[] = {
	new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
	new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
	new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };


    /**
     * The HTTP headers associated with this Request, keyed by name.  The
     * values are ArrayLists of the corresponding header values.
     */
    protected HashMap headers = new HashMap();


    /**
     * Descriptive information about this HttpRequest implementation.
     */
    protected static final String info =
	"org.apache.catalina.connector.HttpRequestBase/1.0";


    /**
     * The request method associated with this Request.
     */
    protected String method = null;


    /**
     * The parsed parameters for this request.  This is populated only if
     * parameter information is requested via one of the
     * <code>getParameter()</code> family of method calls.  The key is the
     * parameter name, while the value is a String array of values for this
     * parameter.
     */
    protected HashMap parameters = null;


    /**
     * The path information for this request.
     */
    protected String pathInfo = null;


    /**
     * The query string for this request.
     */
    protected String queryString = null;


    /**
     * Was the requested session ID received in a cookie?
     */
    protected boolean requestedSessionCookie = false;


    /**
     * The requested session ID (if any) for this request.
     */
    protected String requestedSessionId = null;


    /**
     * Was the requested session ID received in a URL?
     */
    protected boolean requestedSessionURL = false;


    /**
     * The request URI associated with this request.
     */
    protected String requestURI = null;


    /**
     * Was this request received on a secure channel?
     */
    protected boolean secure = false;


    /**
     * The servlet path for this request.
     */
    protected String servletPath = null;


    /**
     * The currently active session for this request.
     */
    protected Session session = null;


    /**
     * The Principal who has been authenticated for this Request.
     */
    protected Principal userPrincipal = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a Cookie to the set of Cookies associated with this Request.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie) {

	synchronized (cookies) {
	    cookies.add(cookie);
	}

    }


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value) {

	name = name.toLowerCase();
	synchronized (headers) {
	    ArrayList values = (ArrayList) headers.get(name);
	    if (values == null) {
		values = new ArrayList();
		headers.put(name, values);
	    }
	    values.add(value);
	}

    }


    /**
     * Clear the collection of Cookies associated with this Request.
     */
    public void clearCookies() {

	synchronized (cookies) {
	    cookies.clear();
	}

    }


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders() {

	headers.clear();

    }


    /**
     * Clear the collection of Locales associated with this Request.
     */
    public void clearLocales() {

	locales.clear();

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

	super.recycle();
	authType = null;
	contextPath = "";
	cookies.clear();
	headers.clear();
	method = null;
	parameters = null;
	pathInfo = null;
	queryString = null;
	requestedSessionCookie = false;
	requestedSessionId = null;
	requestedSessionURL = false;
	requestURI = null;
	secure = false;
	servletPath = null;
	session = null;
	userPrincipal = null;

    }


    /**
     * Set the authentication type used for this request, if any; otherwise
     * set the type to <code>null</code>.  Typical values are "BASIC",
     * "DIGEST", or "SSL".
     *
     * @param type The authentication type used
     */
    public void setAuthType(String type) {

	this.authType = authType;

    }


    /**
     * Set the context path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The context path
     */
    public void setContextPath(String path) {

	if (path == null)
	    this.contextPath = "";
	else
	    this.contextPath = path;

    }


    /**
     * Set the HTTP request method used for this Request.
     *
     * @param method The request method
     */
    public void setMethod(String method) {

	this.method = method;

    }


    /**
     * Set the path information for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The path information
     */
    public void setPathInfo(String path) {

	this.pathInfo = path;

    }


    /**
     * Set the query string for this Request.  This will normally be called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param query The query string
     */
    public void setQueryString(String query) {

	this.queryString = query;

    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionCookie(boolean flag) {

	this.requestedSessionCookie = flag;

    }


    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id) {

	this.requestedSessionId = id;

    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag) {

	this.requestedSessionURL = flag;

    }


    /**
     * Set the unparsed request URI for this Request.  This will normally
     * be called by the HTTP Connector, when it parses the request headers.
     *
     * @param uri The request URI
     */
    public void setRequestURI(String uri) {

	this.requestURI = uri;

    }


    /**
     * Set the flag indicating whether this Request was received on a secure
     * communications link or not.  This will normally be called by the HTTP
     * Connector, when it parses the request headers.
     *
     * @param secure The new secure flag
     */
    public void setSecure(boolean secure) {

	this.secure = secure;

    }


    /**
     * Set the servlet path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The servlet path
     */
    public void setServletPath(String path) {

	this.servletPath = path;

    }


    /**
     * Set the Principal who has been authenticated for this Request.  This
     * value is also used to calculate the value to be returned by the
     * <code>getRemoteUser()</code> method.
     *
     * @param principal The user Principal
     */
    public void setUserPrincipal(Principal principal) {

	this.userPrincipal = principal;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse the parameters of this request, if it has not already occurred.
     * If parameters are present in both the query string and the request
     * content, they are merged.
     */
    protected void parseParameters() {

	if (parameters != null)
	    return;

	HashMap results = new HashMap();

	// Parse any query string parameters for this request
	Hashtable queryParameters = null;
	try {
	    queryParameters = HttpUtils.parseQueryString(getQueryString());
	} catch (IllegalArgumentException e) {
	    ;
	}

	// Parse any posted parameters in the input stream
	Hashtable postParameters = null;
	if ("POST".equals(getMethod()) &&
	    "application/x-www-form-urlencoded".equals(getContentType())) {
	    try {
		ServletInputStream is = getInputStream();
		postParameters =
		    HttpUtils.parsePostData(getContentLength(), is);
	    } catch (IllegalArgumentException e) {
		;
	    } catch (IOException e) {
		;
	    }
	}

	// Process the query parameters first
	if (queryParameters != null) {
	    Enumeration queryKeys = queryParameters.keys();
	    while (queryKeys.hasMoreElements()) {
		String queryKey = (String) queryKeys.nextElement();
		Object queryValues = queryParameters.get(queryKey);
		ArrayList values = new ArrayList();
		if (queryValues instanceof String)
		    values.add((String) queryValues);
		else if (queryValues instanceof String[]) {
		    String queryStrings[] = (String[]) queryValues;
		    for (int i = 0; i < queryStrings.length; i++)
			values.add(queryStrings[i]);
		}
		results.put(queryKey,
			    (String[]) values.toArray(new String[0]));
	    }
	}

	// Process the post parameters second
	if (postParameters != null) {
	    Enumeration postKeys = postParameters.keys();
	    while (postKeys.hasMoreElements()) {
		String postKey = (String) postKeys.nextElement();
		Object postValues = postParameters.get(postKey);
		ArrayList values = new ArrayList();
		String queryValues[] = (String[]) results.get(postKey);
		if (queryValues != null) {
		    for (int i = 0; i < queryValues.length; i++)
			values.add(queryValues[i]);
		}
		if (postValues instanceof String)
		    values.add((String) postValues);
		else if (postValues instanceof String[]) {
		    String postStrings[] = (String[]) postValues;
		    for (int i = 0; i < postStrings.length; i++)
			values.add(postStrings[i]);
		}
		results.put(postKey,
			    (String[]) values.toArray(new String[0]));
	    }
	}

	// Store the final results
	parameters = results;

    }


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.  If there is more than one value defined,
     * return only the first one.
     *
     * @param name Name of the desired request parameter
     */
    public String getParameter(String name) {

	parseParameters();

	synchronized (parameters) {
	    String values[] = (String[]) parameters.get(name);
	    if (values != null)
		return (values[0]);
	    else
		return (null);
	}

    }


    /**
     * Return the names of all defined request parameters for this request.
     */
    public Enumeration getParameterNames() {

	parseParameters();

	synchronized (parameters) {
	    return (new Enumerator(parameters.keySet()));
	}

    }


    /**
     * Return the defined values for the specified request parameter, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired request parameter
     */
    public String[] getParameterValues(String name) {

	parseParameters();

	synchronized (parameters) {
	    String values[] = (String[]) parameters.get(name);
	    if (values != null)
		return (values);
	    else
		return (null);
	}

    }


    /**
     * Return a RequestDispatcher that wraps the resource at the specified
     * path, which may be interpreted as relative to the current request path.
     *
     * @param path Path of the resource to be wrapped
     */
    public RequestDispatcher getRequestDispatcher(String path) {

	// If the path is already context-relative, just pass it through
	if (path == null)
	    return (null);
	else if (path.startsWith("/"))
	    return (context.getServletContext().getRequestDispatcher(path));

	// Convert a request-relative path to a context-relative one
	String relative = getServletPath() + "/../" + path;
	// FIXME -- Canonicalize any ".." directory references!
	return (context.getServletContext().getRequestDispatcher(relative));

    }


    /**
     * Was this request received on a secure connection?
     */
    public boolean isSecure() {

	return (secure);

    }


    // --------------------------------------------- HttpServletRequest Methods


    /**
     * Return the authentication type used for this Request.
     */
    public String getAuthType() {

	return (authType);

    }


    /**
     * Return the portion of the request URI used to select the Context
     * of the Request.
     */
    public String getContextPath() {

	return (contextPath);

    }


    /**
     * Return the set of Cookies received with this Request.
     */
    public Cookie[] getCookies() {

	synchronized (cookies) {
	    Cookie results[] = new Cookie[cookies.size()];
	    return ((Cookie[]) cookies.toArray(results));
	}

    }


    /**
     * Return the value of the specified date header, if any; otherwise
     * return -1.
     *
     * @param name Name of the requested date header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to a date
     */
    public long getDateHeader(String name) {

	String value = getHeader(name);
	if (value == null)
	    return (-1L);

	// Work around a bug in SimpleDateFormat in pre-JDK1.2b4
	// (Bug Parade bug #4106807)
	value += " ";

	// Attempt to convert the date header in a variety of formats
	for (int i = 0; i < formats.length; i++) {
	    try {
		Date date = formats[i].parse(value);
		return (date.getTime());
	    } catch (ParseException e) {
		;
	    }
	}
	throw new IllegalArgumentException(value);

    }


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param name Name of the requested header
     */
    public String getHeader(String name) {

	name = name.toLowerCase();
	synchronized (headers) {
	    ArrayList values = (ArrayList) headers.get(name);
	    if (values != null)
		return ((String) values.get(0));
	    else
		return (null);
	}

    }


    /**
     * Return all of the values of the specified header, if any; otherwise,
     * return an empty enumeration.
     *
     * @param name Name of the requested header
     */
    public Enumeration getHeaders(String name) {

	name = name.toLowerCase();
	synchronized (headers) {
	    ArrayList values = (ArrayList) headers.get(name);
	    if (values != null)
		return (new Enumerator(values));
	    else
		return (new Enumerator(empty));
	}

    }


    /**
     * Return the names of all headers received with this request.
     */
    public Enumeration getHeaderNames() {

	synchronized (headers) {
	    return (new Enumerator(headers.keySet()));
	}

    }


    /**
     * Return the value of the specified header as an integer, or -1 if there
     * is no such header for this request.
     *
     * @param name Name of the requested header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to an integer
     */
    public int getIntHeader(String name) {

	String value = getHeader(name);
	if (value == null)
	    return (-1);
	else
	    return (Integer.parseInt(value));

    }


    /**
     * Return the HTTP request method used in this Request.
     */
    public String getMethod() {

	return (method);

    }


    /**
     * Return the path information associated with this Request.
     */
    public String getPathInfo() {

	return (pathInfo);

    }


    /**
     * Return the extra path information for this request, translated
     * to a real path.
     */
    public String getPathTranslated() {

	if (pathInfo == null)
	    return (null);
	else
	    return (context.getServletContext().getRealPath(pathInfo));

    }


    /**
     * Return the query string associated with this request.
     */
    public String getQueryString() {

	return (queryString);

    }


    /**
     * Return the name of the remote user that has been authenticated
     * for this Request.
     */
    public String getRemoteUser() {

	if (userPrincipal != null)
	    return (userPrincipal.getName());
	else
	    return (null);

    }


    /**
     * Return the session identifier included in this request, if any.
     */
    public String getRequestedSessionId() {

	return (requestedSessionId);

    }


    /**
     * Return the request URI for this request.
     */
    public String getRequestURI() {

	return (requestURI);

    }


    /**
     * Return the portion of the request URI used to select the servlet
     * that will process this request.
     */
    public String getServletPath() {

	return (servletPath);

    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary.
     */
    public HttpSession getSession() {

	return (getSession(true));

    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create Create a new session if one does not exist
     */
    public HttpSession getSession(boolean create) {

	// Return the current session if it exists and is valid
	if ((session != null) && !session.isValid())
	    session = null;
	if (session != null)
	    return (session.getSession());

	// Return the requested session if it exists and is valid
	Manager manager = context.getManager();
	if ((manager != null) && (requestedSessionId != null)) {
	    try {
		session = manager.findSession(requestedSessionId);
	    } catch (IOException e) {
		session = null;
	    }
	    if ((session != null) && !session.isValid())
	        session = null;
	    if (session != null) {
		session.access();
		return (session.getSession());
	    }
	}

	// Create a new session if requested
	if (!create)
	    return (null);
	session = manager.createSession();
	if (session != null)
	    return (session.getSession());
	else
	    return (null);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from a cookie.
     */
    public boolean isRequestedSessionIdFromCookie() {

	if (requestedSessionId != null)
	    return (requestedSessionCookie);
	else
	    return (false);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     */
    public boolean isRequestedSessionIdFromURL() {

	if (requestedSessionId != null)
	    return (requestedSessionURL);
	else
	    return (false);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     *
     * @deprecated As of Version 2.1 of the Java Servlet API, use
     *  <code>isRequestedSessionIdFromURL()</code> instead.
     */
    public boolean isRequestedSessionIdFromUrl() {

	return (isRequestedSessionIdFromURL());

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request identifies a valid session.
     */
    public boolean isRequestedSessionIdValid() {

	if (requestedSessionId == null)
	    return (false);
	Manager manager = context.getManager();
	if (manager == null)
	    return (false);
	Session session = null;
	try {
	    session = manager.findSession(requestedSessionId);
	} catch (IOException e) {
	    session = null;
	}
	if ((session != null) && session.isValid())
	    return (true);
	else
	    return (false);

    }


    /**
     * Return <code>true</code> if the authenticated user principal
     * possesses the specified role name.
     *
     * @param role Role name to be validated
     */
    public boolean isUserInRole(String role) {

	// Respect role name translations in the deployment descriptor
	String realRole = context.findRoleMapping(role);

	// Determine whether the current user has this role
	if (userPrincipal == null)
	    return (false);
	Realm realm = context.getRealm();
	if (realm == null)
	    return (false);

	return (realm.hasRole(userPrincipal, realRole));

    }


    /**
     * Return the principal that has been authenticated for this Request.
     */
    public Principal getUserPrincipal() {

	return (userPrincipal);

    }


}
