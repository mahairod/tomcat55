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


package org.apache.catalina.core;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;


/**
 * Wrapped HttpRequest suitable for use in implementations of
 * RequestDispatcher.  The components that are wrapped by this
 * implementation are:
 * <ul>
 * <li>The request attributes (because they will be augmented on an HTTP
 *     path-based include)
 * <li>The request parameters (because they will be merged on an HTTP
 *     path-based include)
 * <li>The request path elements (because they will be modified on an HTTP
 *     path-based forward)
 * </ul>
 *
 * <strong>IMPLEMENTATION NOTE</strong>:  Request parameters are read only
 * once an instance of this class has been constructed, so no synchronization
 * is required to access them.  If this ever changes, be sure to add
 * <code>synchronized</code> blocks around accesses to the
 * <code>parameters</code> instance variables.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

final class WrappedRequest
    implements HttpRequest, HttpServletRequest, ServletRequest {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, wrapping the specified request.
     *
     * @param request The HTTP request we are wrapping
     * @param queryString Optional query string with parameters to merge
     */
    public WrappedRequest(HttpRequest request, String queryString) {

	super();
	this.request = request;
	this.context = request.getContext();
	ServletRequest srequest = request.getRequest();

	// Initialize our attributes from the wrapped request
	Enumeration names = srequest.getAttributeNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    Object value = srequest.getAttribute(name);
	    attributes.put(name, value);
	}
	if (srequest instanceof HttpServletRequest) {
	    HttpServletRequest hrequest = (HttpServletRequest) srequest;
	    requestURI = hrequest.getRequestURI();
	    servletPath = hrequest.getServletPath();
	    pathInfo = hrequest.getPathInfo();
	}
	this.queryString = queryString;

	// Initialize our parameters from the wrapped request.
	names = srequest.getParameterNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    String values[] = srequest.getParameterValues(name);
	    parameters.put(name, values);
	}

	// Merge the additional query parameters (if any)
	if (queryString == null)
	    return;
	Hashtable newParams = HttpUtils.parseQueryString(queryString);
	if (newParams == null)
	    return;
	names = newParams.keys();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    String oldValues[] = (String[]) parameters.get(name);
	    if (oldValues == null)
	        oldValues = new String[0];
	    String newValues[] = null;
	    Object newValue = newParams.get(name);
	    if (newValue instanceof String) {
		newValues = new String[1];
		newValues[0] = (String) newValue;
	    } else if (newValue instanceof String[])
	        newValues = (String[]) newValue;
	    else
	        newValues = new String[0];
	    String mergedValues[] = new String[oldValues.length + newValues.length];
	    for (int i = 0; i < newValues.length; i++)
	        mergedValues[i] = newValues[i];
	    for (int i = newValues.length; i < mergedValues.length; i++)
	        mergedValues[i] = oldValues[i - newValues.length];
	    parameters.put(name, mergedValues);
	}


    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The attributes for this wrapped request (initialized from the underlying
     * request, but possibly amended).
     */
    private HashMap attributes = new HashMap();


    /**
     * The Context within which this request is being processed.
     */
    private Context context = null;


    /**
     * Descriptive information about this implementation.
     */
    private static final String info =
      "org.apache.catalina.core.WrappedRequest/1.0";


    /**
     * The parameters for this wrapped request (initialized from the underlying
     * request, but possibly amended).
     */
    private HashMap parameters = new HashMap();


    /**
     * The extra path information for this request.
     */
    private String pathInfo = null;


    /**
     * The query string for this request.
     */
    private String queryString = null;


    /**
     * The HttpRequest we are wrapping.
     */
    private HttpRequest request = null;


    /**
     * The request URI for this request.
     */
    private String requestURI = null;


    /**
     * The servlet path for this request;
     */
    private String servletPath = null;


    // ---------------------------------------------------- HttpRequest Methods


    /**
     * Add a Cookie to the set of Cookies associated with this Request.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie) {
	request.addCookie(cookie);
    }


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value) {
	request.addHeader(name, value);
    }


    /**
     * Add a Locale to the set of preferred Locales for this Request.  The
     * first added Locale will be the first one returned by getLocales().
     *
     * @param locale The new preferred Locale
     */
    public void addLocale(Locale locale) {
	request.addLocale(locale);
    }


    /**
     * Add a parameter name and corresponding set of values to this Request.
     * (This is used when restoring the original request on a form based
     * login).
     *
     * @param name Name of this request parameter
     * @param values Corresponding values for this request parameter
     */
    public void addParameter(String name, String values[]) {
        request.addParameter(name, values);
    }


    /**
     * Clear the collection of Cookies associated with this Request.
     */
    public void clearCookies() {
	request.clearCookies();
    }


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders() {
	request.clearHeaders();
    }


    /**
     * Clear the collection of Locales associated with this Request.
     */
    public void clearLocales() {
	request.clearLocales();
    }


    /**
     * Clear the collection of parameters associated with this Request.
     */
    public void clearParameters() {
        request.clearParameters();
    }


    /**
     * Set the authentication type used for this request, if any; otherwise
     * set the type to <code>null</code>.  Typical values are "BASIC",
     * "DIGEST", or "SSL".
     *
     * @param type The authentication type used
     */
    public void setAuthType(String type) {
	request.setAuthType(type);
    }


    /**
     * Set the context path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The context path
     */
    public void setContextPath(String path) {
	request.setContextPath(path);
    }


    /**
     * Set the HTTP request method used for this Request.
     *
     * @param method The request method
     */
    public void setMethod(String method) {
	request.setMethod(method);
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
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionCookie(boolean flag) {
	request.setRequestedSessionCookie(flag);
    }


    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id) {
	request.setRequestedSessionId(id);
    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag) {
	request.setRequestedSessionURL(flag);
    }


    /**
     * Set the unparsed request URI for this Request.  This will normally be
     * called by the HTTP Connector, when it parses the request headers.
     *
     * @param uri The request URI
     */
    public void setRequestURI(String uri) {
	this.requestURI = uri;
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
	request.setUserPrincipal(principal);
    }


    // -------------------------------------------- HttpServletRequest Methods


    /**
     * Return the authentication type used for this Request.
     */
    public String getAuthType() {
	return (((HttpServletRequest) request.getRequest()).getAuthType());
    }


    /**
     * Return the portion of the request URI used to select the Context
     * of the Request.
     */
    public String getContextPath() {
	return (((HttpServletRequest) request.getRequest()).getContextPath());
    }


    /**
     * Return the set of Cookies received with this Request.
     */
    public Cookie[] getCookies() {
	return (((HttpServletRequest) request.getRequest()).getCookies());
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
	return (((HttpServletRequest) request.getRequest()).getDateHeader(name));
    }


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param name Name of the requested header
     */
    public String getHeader(String name) {
	return (((HttpServletRequest) request.getRequest()).getHeader(name));
    }


    /**
     * Return all of the values of the specified header, if any; otherwise,
     * return <code>null</code>.
     *
     * @param name Name of the requested header
     */
    public Enumeration getHeaders(String name) {
	return (((HttpServletRequest) request.getRequest()).getHeaders(name));
    }


    /**
     * Return the names of all headers received with this request.
     */
    public Enumeration getHeaderNames() {
	return (((HttpServletRequest) request.getRequest()).getHeaderNames());
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
	return (((HttpServletRequest) request.getRequest()).getIntHeader(name));
    }


    /**
     * Return the HTTP request method used in this Request.
     */
    public String getMethod() {
	return (((HttpServletRequest) request.getRequest()).getMethod());
    }


    /**
     * Return the path information associated with this Request.
     */
    public String getPathInfo() {
	return (this.pathInfo);
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
	return (this.queryString);
    }


    /**
     * Return the name of the remote user that has been authenticated
     * for this Request.
     */
    public String getRemoteUser() {
	return (((HttpServletRequest) request.getRequest()).getRemoteUser());
    }


    /**
     * Return the session identifier included in this request, if any.
     */
    public String getRequestedSessionId() {
	return (((HttpServletRequest) request.getRequest()).getRequestedSessionId());
    }


    /**
     * Return the request URI for this request.
     */
    public String getRequestURI() {
	return (this.requestURI);
    }


    /**
     * Return the reconstructed URL for this request.
     */
    public StringBuffer getRequestURL() {
        return (null); // FIXME - getRequestURL()
    }


    /**
     * Return the portion of the request URI used to select the servlet
     * that will process this request.
     */
    public String getServletPath() {
	return (this.servletPath);
    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary.
     */
    public HttpSession getSession() {
	return (((HttpServletRequest) request.getRequest()).getSession());
    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create Create a new session if one does not exist
     */
    public HttpSession getSession(boolean create) {
	return (((HttpServletRequest) request.getRequest()).getSession(create));
    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from a cookie.
     */
    public boolean isRequestedSessionIdFromCookie() {
	return (((HttpServletRequest) request.getRequest()).isRequestedSessionIdFromCookie());
    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     */
    public boolean isRequestedSessionIdFromURL() {
	return (((HttpServletRequest) request.getRequest()).isRequestedSessionIdFromURL());
    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     *
     * @deprecated As of Version 2.1 of the Java Servlet API, use
     *  <code>isRequestedSessionIdFromURL()</code> instead.
     */
    public boolean isRequestedSessionIdFromUrl() {
	return (((HttpServletRequest) request.getRequest()).isRequestedSessionIdFromUrl());
    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request identifies a valid session.
     */
    public boolean isRequestedSessionIdValid() {
	return (((HttpServletRequest) request.getRequest()).isRequestedSessionIdValid());
    }


    /**
     * Return <code>true</code> if the authenticated user principal
     * possesses the specified role name.
     *
     * @param role Role name to be validated
     */
    public boolean isUserInRole(String role) {
	return (((HttpServletRequest) request.getRequest()).isUserInRole(role));
    }


    /**
     * Return the principal that has been authenticated for this Request.
     */
    public Principal getUserPrincipal() {
	return (((HttpServletRequest) request.getRequest()).getUserPrincipal());
    }


    // -------------------------------------------------------- Request Methods


    /**
     * Return the authorization credentials sent with this request.
     */
    public String getAuthorization() {
	return (request.getAuthorization());
    }


    /**
     * Set the authorization credentials sent with this request.
     *
     * @param authorization The new authorization credentials
     */
    public void setAuthorization(String authorization) {
	request.setAuthorization(authorization);
    }


    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector() {
	return (request.getConnector());
    }


    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {
	request.setConnector(connector);
    }


    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {
	return (request.getContext());
    }


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context) {
	request.setContext(context);
    }


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
	return (info);
    }


    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.
     */
    public ServletRequest getRequest() {
	return ((ServletRequest) this);
    }


    /**
     * Return the Response with which this Request is associated.
     */
    public Response getResponse() {
	return (request.getResponse());
    }


    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(Response response) {
	request.setResponse(response);
    }


    /**
     * Return the Socket (if any) through which this Request was received.
     * This should <strong>only</strong> be used to access underlying state
     * information about this Socket, such as the SSLSession associated with
     * an SSLSocket.
     */
    public Socket getSocket() {

        return (request.getSocket());

    }


    /**
     * Set the Socket (if any) through which this Request was received.
     *
     * @param socket The socket through which this request was received
     */
    public void setSocket(Socket socket) {

        request.setSocket(socket);

    }


    /**
     * Return the input stream associated with this Request.
     */
    public InputStream getStream() {
	return (request.getStream());
    }


    /**
     * Set the input stream associated with this Request.
     *
     * @param stream The new input stream
     */
    public void setStream(InputStream stream) {
	request.setStream(stream);
    }


    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper() {
	return (request.getWrapper());
    }


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     *
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper) {
	request.setWrapper(wrapper);
    }


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream createInputStream() throws IOException {
	return (request.createInputStream());
    }


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishRequest() throws IOException {

	request.finishRequest();

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
	;	// This object will not be recycled
    }


    /**
     * Set the content length associated with this Request.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {
	request.setContentLength(length);
    }


    /**
     * Set the content type (and optionally the character encoding)
     * associated with this Request.  For example,
     * <code>text/html; charset=ISO-8859-4</code>.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {
	request.setContentType(type);
    }


    /**
     * Set the protocol name and version associated with this Request.
     *
     * @param protocol Protocol name and version
     */
    public void setProtocol(String protocol) {
	request.setProtocol(protocol);
    }


    /**
     * Set the remote IP address associated with this Request.  NOTE:  This
     * value will be used to resolve the value for <code>getRemoteHost()</code>
     * if that method is called.
     *
     * @param remote The remote IP address
     */
    public void setRemoteAddr(String remote) {
	request.setRemoteAddr(remote);
    }


    /**
     * Set the name of the scheme associated with this request.  Typical values
     * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
     *
     * @param scheme The scheme
     */
    public void setScheme(String scheme) {
	request.setScheme(scheme);
    }


    /**
     * Set the value to be returned by <code>isSecure()</code>
     * for this Request.
     *
     * @param secure The new isSecure value
     */
    public void setSecure(boolean secure) {
	request.setSecure(secure);
    }


    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name The server name
     */
    public void setServerName(String name) {
	request.setServerName(name);
    }


    /**
     * Set the port number of the server to process this request.
     *
     * @param port The server port
     */
    public void setServerPort(int port) {
	request.setServerPort(port);
    }


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the specified request attribute if it exists; otherwise, return
     * <code>null</code>.
     *
     * @param name Name of the request attribute to return
     */
    public Object getAttribute(String name) {
	synchronized (attributes) {
	    return (attributes.get(name));
	}
    }


    /**
     * Return the names of all request attributes for this Request, or an
     * empty <code>Enumeration</code> if there are none.
     */
    public Enumeration getAttributeNames() {
	synchronized (attributes) {
	    return (new Enumerator(attributes.keySet()));
	}
    }


    /**
     * Return the character encoding for this Request.
     */
    public String getCharacterEncoding() {
	return (request.getRequest().getCharacterEncoding());
    }


    /**
     * Return the content length for this Request.
     */
    public int getContentLength() {
	return (request.getRequest().getContentLength());
    }


    /**
     * Return the content type for this Request.
     */
    public String getContentType() {
	return (request.getRequest().getContentType());
    }


    /**
     * Return the servlet input stream for this Request.  The default
     * implementation returns a servlet input stream created by
     * <code>createInputStream()</code>.
     *
     * @exception IllegalStateException if <code>getReader()</code> has
     *  already been called for this request
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream getInputStream() throws IOException {
	return (request.getRequest().getInputStream());
    }


    /**
     * Return the preferred Locale that the client will accept content in,
     * based on the value for the first <code>Accept-Language</code> header
     * that was encountered.  If the request did not specify a preferred
     * language, the server's default Locale is returned.
     */
    public Locale getLocale() {
	return (request.getRequest().getLocale());
    }


    /**
     * Return the set of preferred Locales that the client will accept
     * content in, based on the values for any <code>Accept-Language</code>
     * headers that were encountered.  If the request did not specify a
     * preferred language, the server's default Locale is returned.
     */
    public Enumeration getLocales() {
	return (request.getRequest().getLocales());
    }


    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.  If there is more than one value defined,
     * return only the first one.
     *
     * @param name Name of the desired request parameter
     */
    public String getParameter(String name) {
	String values[] = (String[]) parameters.get(name);
	if (values != null)
	    return (values[0]);
	else
	    return (null);
    }


    /**
     * Return a Map of the parameters for this request.
     */
    public Map getParameterMap() {
        return (null); // FIXME - getParameterMap()
    }


    /**
     * Return the names of all defined request parameters for this request.
     */
    public Enumeration getParameterNames() {
	return (new Enumerator(parameters.keySet()));
    }


    /**
     * Return the defined values for the specified request parameter, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired request parameter
     */
    public String[] getParameterValues(String name) {
	return ((String[]) parameters.get(name));
    }


    /**
     * Return the protocol and version used to make this Request.
     */
    public String getProtocol() {
	return (request.getRequest().getProtocol());
    }


    /**
     * Read the Reader wrapping the input stream for this Request.  The
     * default implementation wraps a <code>BufferedReader</code> around the
     * servlet input stream returned by <code>createInputStream()</code>.
     *
     * @exception IllegalStateException if <code>getInputStream()</code>
     *  has already been called for this request
     * @exception IOException if an input/output error occurs
     */
    public BufferedReader getReader() throws IOException {
	return (request.getRequest().getReader());
    }


    /**
     * Return the real path of the specified virtual path.
     *
     * @param path Path to be translated
     *
     * @deprecated As of version 2.1 of the Java Servlet API, use
     *  <code>ServletContext.getRealPath()</code>.
     */
    public String getRealPath(String path) {
	return (request.getRequest().getRealPath(path));
    }


    /**
     * Return the remote IP address making this Request.
     */
    public String getRemoteAddr() {
	return (request.getRequest().getRemoteAddr());
    }


    /**
     * Return the remote host name making this Request.
     */
    public String getRemoteHost() {
	return (request.getRequest().getRemoteHost());
    }


    /**
     * Return a RequestDispatcher that wraps the resource at the specified
     * path, which may be interpreted as relative to the current request path.
     *
     * @param path Path of the resource to be wrapped
     */
    public RequestDispatcher getRequestDispatcher(String path) {
	return (request.getRequest().getRequestDispatcher(path));
    }


    /**
     * Return the scheme used to make this Request.
     */
    public String getScheme() {
	return (request.getRequest().getScheme());
    }


    /**
     * Return the server name responding to this Request.
     */
    public String getServerName() {
	return (request.getRequest().getServerName());
    }


    /**
     * Return the server port responding to this Request.
     */
    public int getServerPort() {
	return (request.getRequest().getServerPort());
    }


    /**
     * Was this request received on a secure connection?
     */
    public boolean isSecure() {
	return (request.getRequest().isSecure());
    }


    /**
     * Remove the specified request attribute if it exists.
     *
     * @param name Name of the request attribute to remove
     */
    public void removeAttribute(String name) {
	synchronized (attributes) {
	    attributes.remove(name);
	}
    }


    /**
     * Set the specified request attribute to the specified value.
     *
     * @param name Name of the request attribute to set
     * @param value The associated value
     */
    public void setAttribute(String name, Object value) {
	synchronized (attributes) {
	    attributes.put(name, value);
	}
    }


    /**
     * Set the character encoding to be used for this request.
     */
    public void setCharacterEncoding(String enc) {
        ; // FIXME - setCharacterEncoding()
    }


}
