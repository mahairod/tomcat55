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

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Logger;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of <code>RequestDispatcher</code> that allows a
 * request to be forwarded to a different resource to create the ultimate
 * response, or to include the output of another resource in the response
 * from this resource.  This implementation allows application level servlets
 * to wrap the request and/or response objects that are passed on to the
 * called resource, as long as the wrapping classes extend
 * <code>javax.servlet.ServletRequestWrapper</code> and
 * <code>javax.servlet.ServletResponseWrapper</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

final class ApplicationDispatcher
    implements RequestDispatcher {


    protected class PrivilegedForward implements PrivilegedExceptionAction {
        private ServletRequest request;
        private ServletResponse response;

        PrivilegedForward(ServletRequest request, ServletResponse response)
        {   
	    this.request = request;
	    this.response = response;
        }   
            
        public Object run() throws ServletException, IOException {
	    doForward(request,response);
	    return null;
        }   
    }

    protected class PrivilegedInclude implements PrivilegedExceptionAction {
        private ServletRequest request;
        private ServletResponse response;

        PrivilegedInclude(ServletRequest request, ServletResponse response)
        {  
            this.request = request;
            this.response = response;
        }
        
        public Object run() throws ServletException, IOException {
            doInclude(request,response);
            return null;
        }              
    }

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, configured according to the
     * specified parameters.  If both servletPath and pathInfo are
     * <code>null</code>, it will be assumed that this RequestDispatcher
     * was acquired by name, rather than by path.
     *
     * @param wrapper The Wrapper associated with the resource that will
     *  be forwarded to or included (required)
     * @param servletPath The revised servlet path to this resource (if any)
     * @param pathInfo The revised extra path information to this resource
     *  (if any)
     * @param queryString Query string parameters included with this request
     *  (if any)
     * @param name Servlet name (if a named dispatcher was created)
     *  else <code>null</code>
     */
    public ApplicationDispatcher
	(Wrapper wrapper, String servletPath,
	 String pathInfo, String queryString, String name) {

	super();

	// Save all of our configuration parameters
	this.wrapper = wrapper;
	this.context = (Context) wrapper.getParent();
	this.servletPath = servletPath;
	this.pathInfo = pathInfo;
	this.queryString = queryString;
        this.name = name;

	if (debug >= 1)
	    log("servletPath=" + this.servletPath + ", pathInfo=" +
		this.pathInfo + ", queryString=" + queryString +
                ", name=" + this.name);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context this RequestDispatcher is associated with.
     */
    private Context context = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The servlet name for a named dispatcher.
     */
    private String name = null;


    /**
     * The extra path information for this RequestDispatcher.
     */
    private String pathInfo = null;


    /**
     * The query string parameters for this RequestDispatcher.
     */
    private String queryString = null;


    /**
     * The servlet path for this RequestDispatcher.
     */
    private String servletPath = null;


    /**
     * The StringManager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * The Wrapper associated with the resource that will be forwarded to
     * or included.
     */
    private Wrapper wrapper = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Forward this request and response to another resource for processing.
     *
     * @param request The servlet request to be forwarded
     * @param response The servlet response to be forwarded
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */

    public void forward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        if( System.getSecurityManager() != null ) {
            try {
		PrivilegedForward dp = new PrivilegedForward(request,response);
                AccessController.doPrivileged(dp);
            } catch( PrivilegedActionException pe) {
                Exception e = pe.getException();
                if( e.getClass().getName().equals("javax.servlet.ServletException") )
                    throw (ServletException)e;
                throw (IOException)e;
            }
        } else {
            doForward(request,response);
        }
    }    
     
    private void doForward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
	// Reset any output that has been buffered, but keep headers/cookies
	if (response.isCommitted())
	    throw new IllegalStateException
		(sm.getString("applicationDispatcher.forward.ise"));
        response.resetBuffer();

	// Identify the HTTP-specific request and response objects (if any)
	HttpServletRequest hrequest = null;
	if (request instanceof HttpServletRequest)
	    hrequest = (HttpServletRequest) request;
	HttpServletResponse hresponse = null;
	if (response instanceof HttpServletResponse)
	    hresponse = (HttpServletResponse) response;

	// Handle a non-HTTP forward by passing the existing request/response
	if ((hrequest == null) || (hresponse == null)) {

	    if (debug >= 1)
		log(" Non-HTTP Forward");

	    try {
		invoke(request, response);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		    (sm.getString("applicationDispatcher.forward.throw"), t);
	    }

	}

	// Handle an HTTP named dispatcher forward
	else if ((servletPath == null) && (pathInfo == null)) {

	    if (debug >= 1)
		log(" Named Dispatcher Forward");

	    try {
		invoke(request, response);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		    (sm.getString("applicationDispatcher.forward.throw"), t);
	    }

	}

	// Handle an HTTP path-based forward
	else {

	    if (debug >= 1)
		log(" Path Based Forward");

	    ApplicationHttpRequest wrequest =
		new ApplicationHttpRequest((HttpServletRequest) request);
	    StringBuffer sb = new StringBuffer();
	    String contextPath = context.getPath();
	    if (contextPath != null)
		sb.append(contextPath);
	    if (servletPath != null)
		sb.append(servletPath);
	    if (pathInfo != null)
		sb.append(pathInfo);
	    wrequest.setContextPath(contextPath);
	    wrequest.setRequestURI(sb.toString());
	    wrequest.setServletPath(servletPath);
	    wrequest.setPathInfo(pathInfo);
	    if (queryString != null) {
		wrequest.setQueryString(queryString);
		wrequest.mergeParameters(queryString);
	    }

	    try {
		invoke(wrequest, response);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		    (sm.getString("applicationDispatcher.forward.throw"), t);
	    }

	}

	// Commit and close the response before we return
	response.flushBuffer();
	try {
	    PrintWriter writer = response.getWriter();
	    writer.flush();
	    writer.close();
	} catch (IllegalStateException e) {
	    try {
		ServletOutputStream stream = response.getOutputStream();
		stream.flush();
		stream.close();
	    } catch (IllegalStateException f) {
		;
	    } catch (IOException f) {
		;
	    }
	} catch (IOException e) {
	    ;
	}

    }


    /**
     * Include the response from another resource in the current response.
     *
     * @param request The servlet request that is including this one
     * @param response The servlet response to be appended to
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void include(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        if( System.getSecurityManager() != null ) {
            try {
                PrivilegedInclude dp = new PrivilegedInclude(request,response);
                AccessController.doPrivileged(dp);
            } catch( PrivilegedActionException pe) {
                Exception e = pe.getException();
		pe.printStackTrace();
                if( e.getClass().getName().equals("javax.servlet.ServletException") )
                    throw (ServletException)e;
                throw (IOException)e;
            }
        } else {
            doInclude(request,response);
        }
    }    
     
    private void doInclude(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
	// Create a wrapped response to use for this request
	ServletResponse wresponse = null;
	if (response instanceof HttpServletResponse) {
	    wresponse =
		new ApplicationHttpResponse((HttpServletResponse) response,
					    true);
	} else {
	    wresponse = new ApplicationResponse(response, true);
	}

	// Handle a non-HTTP include
	if (!(request instanceof HttpServletRequest) ||
	    !(response instanceof HttpServletResponse)) {

	    if (debug >= 1)
		log(" Non-HTTP Include");

	    try {
		invoke(request, wresponse);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		    (sm.getString("applicationDispatcher.include.throw"), t);
	    }

	}

	// Handle an HTTP named dispatcher include
	else if (name != null) {

	    if (debug >= 1)
		log(" Named Dispatcher Include");

	    ApplicationHttpRequest wrequest =
		new ApplicationHttpRequest((HttpServletRequest) request);
            wrequest.setAttribute(Globals.NAMED_DISPATCHER_ATTR, name);

	    try {
		invoke(wrequest, wresponse);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		    (sm.getString("applicationDispatcher.include.throw"), t);
	    }

	}

	// Handle an HTTP path based include
	else {

	    if (debug >= 1)
		log(" Path Based Include");

	    ApplicationHttpRequest wrequest =
		new ApplicationHttpRequest((HttpServletRequest) request);
	    StringBuffer sb = new StringBuffer();
	    String contextPath = context.getPath();
	    if (contextPath != null)
		sb.append(contextPath);
	    if (servletPath != null)
		sb.append(servletPath);
	    if (pathInfo != null)
		sb.append(pathInfo);
	    if (sb.length() > 0)
		wrequest.setAttribute(Globals.REQUEST_URI_ATTR,
				      sb.toString());
	    if (contextPath != null)
		wrequest.setAttribute(Globals.CONTEXT_PATH_ATTR,
				      contextPath);
	    if (servletPath != null)
		wrequest.setAttribute(Globals.SERVLET_PATH_ATTR,
				      servletPath);
	    if (pathInfo != null)
		wrequest.setAttribute(Globals.PATH_INFO_ATTR,
				      pathInfo);
	    if (queryString != null) {
		wrequest.setAttribute(Globals.QUERY_STRING_ATTR,
				      queryString);
		wrequest.mergeParameters(queryString);
	    }

	    try {
		invoke(wrequest, wresponse);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		    (sm.getString("applicationDispatcher.include.throw"), t);
	    }

	}

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Ask the resource represented by this RequestDispatcher to process
     * the associated request, and create (or append to) the associated
     * response.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: This implementation assumes
     * that no filters are applied to a forwarded or included resource,
     * because they were already done for the original request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void invoke(ServletRequest request, ServletResponse response)
	throws IOException, ServletException {

	// Initialize local variables we may need
	HttpServletRequest hrequest = null;
	if (request instanceof HttpServletRequest)
	    hrequest = (HttpServletRequest) request;
	HttpServletResponse hresponse = null;
	if (response instanceof HttpServletResponse)
	    hresponse = (HttpServletResponse) response;
	Servlet servlet = null;
        IOException ioException = null;
        ServletException servletException = null;
	boolean unavailable = false;

	// Check for the servlet being marked unavailable
	if (wrapper.isUnavailable()) {
	    log(sm.getString("applicationDispatcher.isUnavailable",
			     wrapper.getName()));
	    if (hresponse == null) {
		;	// NOTE - Not much we can do generically
	    } else {
		long available = wrapper.getAvailable();
		if ((available > 0L) && (available < Long.MAX_VALUE))
		    hresponse.setDateHeader("Retry-After", available);
		hresponse.sendError
		    (HttpServletResponse.SC_SERVICE_UNAVAILABLE,
		     sm.getString("applicationDispatcher.isUnavailable",
				  wrapper.getName()));
	    }
	    unavailable = true;
	}

	// Allocate a servlet instance to process this request
	try {
	    if (!unavailable) {
		servlet = wrapper.allocate();
	    }
	} catch (ServletException e) {
	    log(sm.getString("applicationDispatcher.allocateException",
			     wrapper.getName()), e);
	    servletException = e;
	    servlet = null;
	} catch (Throwable e) {
	    log(sm.getString("applicationDispatcher.allocateException",
			     wrapper.getName()), e);
	    servletException = new ServletException
                (sm.getString("applicationDispatcher.allocateException",
                              wrapper.getName()), e);
	    servlet = null;
	}

	// Call the service() method for the allocated servlet instance
	try {
	    if (servlet != null) {
		if ((servlet instanceof HttpServlet) &&
		    (hrequest != null) && (hresponse != null))
		    ((HttpServlet) servlet).service(hrequest, hresponse);
		else
		    servlet.service(request, response);
	    }
	} catch (IOException e) {
	    log(sm.getString("applicationDispatcher.serviceException",
			     wrapper.getName()), e);
	    ioException = e;
	} catch (UnavailableException e) {
	    log(sm.getString("applicationDispatcher.serviceException",
			     wrapper.getName()), e);
	    servletException = e;
	    wrapper.unavailable(e);
	} catch (ServletException e) {
	    log(sm.getString("applicationDispatcher.serviceException",
			     wrapper.getName()), e);
	    servletException = e;
	} catch (Throwable e) {
	    log(sm.getString("applicationDispatcher.serviceException",
			     wrapper.getName()), e);
            servletException = new ServletException
                (sm.getString("applicationDispatcher.serviceException",
                              wrapper.getName()), e);
	}

	// Deallocate the allocated servlet instance
	try {
	    if (servlet != null)
		wrapper.deallocate(servlet);
	} catch (ServletException e) {
	    log(sm.getString("applicationDispatcher.deallocateException",
			     wrapper.getName()), e);
	    servletException = e;
	} catch (Throwable e) {
	    log(sm.getString("applicationDispatcher.deallocateException",
			     wrapper.getName()), e);
            servletException = new ServletException
                (sm.getString("applicationDispatcher.deallocateException",
                              wrapper.getName()), e);
	}

        // Rethrow an exception if one was thrown by the invoked servlet
        if (ioException != null)
            throw ioException;
        if (servletException != null)
            throw servletException;

    }


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	Logger logger = context.getLogger();
	if (logger != null)
	    logger.log("ApplicationDispatcher[" + context.getPath() +
		       "]: " + message);
	else
	    System.out.println("ApplicationDispatcher[" +
			       context.getPath() + "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

	Logger logger = context.getLogger();
	if (logger != null)
	    logger.log("ApplicationDispatcher[" + context.getPath() +
		       "] " + message, throwable);
	else {
	    System.out.println("ApplicationDispatcher[" +
			       context.getPath() + "]: " + message);
	    throwable.printStackTrace(System.out);
	}

    }


}
