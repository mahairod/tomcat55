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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of <code>RequestDispatcher</code> that allows a
 * request to be forwarded to a different resource to create the ultimate
 * response, or to include the output of another resource in the response
 * from this resource.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

final class ApplicationDispatcher
    implements RequestDispatcher {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, configured according to the
     * specified parameters.  If both servletPath and pathInfo are
     * <code>null</code>, it will be assumed that this RequestDispatcher
     * was acquired by name, rather than by path.
     *
     * @param wrapper The Wrapper associated with the resource that will
     *  be forwarded to or included
     * @param servletPath The revised servlet path to this resource
     * @param pathInfo The revised extra path information to this resource
     * @param queryString Query string parameters included with this request
     */
    public ApplicationDispatcher(Wrapper wrapper,
    			         String servletPath, String pathInfo,
    			         String queryString) {

	super();
	this.wrapper = wrapper;
	this.context = (Context) wrapper.getParent();
	this.servletPath = servletPath;
	this.pathInfo = pathInfo;
	this.queryString = queryString;

	if (debug >= 1)
	    log("servletPath=" + this.servletPath + ", pathInfo=" +
		this.pathInfo + ", queryString=" + queryString);

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
        throws IOException, ServletException {

	// Reset any output that has already been buffered
	if (response.isCommitted())
	    throw new IllegalStateException
	      (sm.getString("applicationDispatcher.forward.ise"));
	((Response) response).resetBuffer();

	// Cast the servlet request and response to our internal objects
	Request srequest = (Request) request;
	Response sresponse = (Response) response;
	HttpRequest hrequest = null;
	if (request instanceof HttpRequest)
	    hrequest = (HttpRequest) request;
	HttpResponse hresponse = null;
	if (response instanceof HttpResponse)
	    hresponse = (HttpResponse) response;

	// Handle a non-HTTP forward by passing on the existing request and response
	if ((hrequest == null) || (hresponse == null)) {
	    if (debug >= 1)
		log(" Non-HTTP Forward");
	    try {
		wrapper.invoke(srequest, sresponse);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		  (sm.getString("applicationDispatcher.forward.throw"), t);
	    }
	}

	// Handle an HTTP named dispatcher forward (no wrapping is required)
        else if ((servletPath == null) && (pathInfo == null)) {
	    if (debug >= 1)
		log(" Named Dispatcher Forward");
	    try {
		wrapper.invoke(hrequest, hresponse);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		  (sm.getString("applicationDispatcher.forward.throw"), t);
	    }
	}

	// Handle an HTTP path-based forward with no new query parameters
	else if (queryString == null) {
	    if (debug >= 1)
		log(" Non-Wrapped Path Forward");
	    StringBuffer sb = new StringBuffer();
	    if (context.getPath() != null)
	        sb.append(context.getPath());
	    if (servletPath != null)
	        sb.append(servletPath);
	    if (pathInfo != null)
	        sb.append(pathInfo);
	    String requestURI = sb.toString();
	    hrequest.setRequestURI(requestURI);
	    hrequest.setServletPath(servletPath);
	    hrequest.setPathInfo(pathInfo);
	    // Keep original query string
	    if (debug >= 1)
		log(" requestURI=" + requestURI);
	    try {
		wrapper.invoke(hrequest, hresponse);
	    } catch (IOException e) {
		throw e;
	    } catch (ServletException e) {
		throw e;
	    } catch (Throwable t) {
		throw new ServletException
		  (sm.getString("applicationDispatcher.forward.throw"), t);
	    }
	}


	// Handle an HTTP path-based dispatcher include with request wrapping
	else {
	    if (debug >= 1)
		log(" Wrapped Path Forward");
	    WrappedRequest wrequest = new WrappedRequest(hrequest, queryString);
	    StringBuffer sb = new StringBuffer();
	    if (context.getPath() != null)
	        sb.append(context.getPath());
	    if (servletPath != null)
	        sb.append(servletPath);
	    if (pathInfo != null)
	        sb.append(pathInfo);
	    String requestURI = sb.toString();
	    if (debug >= 1)
		log(" requestURI=" + requestURI);
	    wrequest.setRequestURI(requestURI);
	    wrequest.setServletPath(servletPath);
	    wrequest.setPathInfo(pathInfo);
	    wrequest.setQueryString(queryString);
	    try {
		wrapper.invoke(wrequest, hresponse);
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
	sresponse.finishResponse();


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
        throws IOException, ServletException {

	// Flush the response to avoid allowing the included resource
	// to set headers
	//	response.flushBuffer();

	// Cast the servlet request and response to our internal objects
	Request srequest = (Request) request;
	Response sresponse = (Response) response;
	HttpRequest hrequest = null;
	if (request instanceof HttpRequest)
	    hrequest = (HttpRequest) request;
	HttpResponse hresponse = null;
	if (response instanceof HttpResponse)
	    hresponse = (HttpResponse) response;
	boolean oldIncluded = sresponse.getIncluded();

	// Handle a non-HTTP include by passing on the existing request and response
	if ((hrequest == null) || (hresponse == null)) {
	    if (debug >= 1)
		log(" Non-HTTP Include");
	    try {
		wrapper.invoke(srequest, sresponse);
		sresponse.setIncluded(oldIncluded);
	    } catch (IOException e) {
		sresponse.setIncluded(oldIncluded);
		throw e;
	    } catch (ServletException e) {
		sresponse.setIncluded(oldIncluded);
		throw e;
	    } catch (Throwable t) {
		sresponse.setIncluded(oldIncluded);
		throw new ServletException
		  (sm.getString("applicationDispatcher.include.throw"), t);
	    }
	    return;
	}

	// Handle an HTTP named dispatcher include (no wrapping is required)
	if ((servletPath == null) && (pathInfo == null)) {
	    if (debug >= 1)
		log(" Named Dispatcher Include");
	    try {
		wrapper.invoke(hrequest, hresponse);
		sresponse.setIncluded(oldIncluded);
	    } catch (IOException e) {
		sresponse.setIncluded(oldIncluded);
		throw e;
	    } catch (ServletException e) {
		sresponse.setIncluded(oldIncluded);
		throw e;
	    } catch (Throwable t) {
		sresponse.setIncluded(oldIncluded);
		throw new ServletException
		  (sm.getString("applicationDispatcher.include.throw"), t);
	    }
	    return;
	}

	// Handle an HTTP path-based dispatcher include (with request wrapping)
	if (debug >= 1)
	    log(" Wrapped Path Include");
	WrappedRequest wrequest = new WrappedRequest(hrequest, queryString);
	String contextPath = context.getPath();
	StringBuffer sb = new StringBuffer();
	if (contextPath != null)
	    sb.append(contextPath);
	if (servletPath != null)
	    sb.append(servletPath);
	if (pathInfo != null)
	    sb.append(pathInfo);
	ServletRequest sr = wrequest.getRequest();
	if (sb.length() > 0) {
	    if (debug >= 1)
		log("  requestURI=" + sb.toString());
	    sr.setAttribute("javax.servlet.include.request_uri",
			    sb.toString());
	}
	if (contextPath != null) {
	    if (debug >= 1)
		log("  contextPath=" + contextPath);
	    sr.setAttribute("javax.servlet.include.context_path", contextPath);
	}
	if (servletPath != null) {
	    if (debug >= 1)
		log("  servletPath=" + servletPath);
	    sr.setAttribute("javax.servlet.include.servlet_path", servletPath);
	}
	if (pathInfo != null) {
	    if (debug >= 1)
		log("  pathInfo=" + pathInfo);
	    sr.setAttribute("javax.servlet.include.path_info", pathInfo);
	}
	if (queryString != null) {
	    if (debug >= 1)
		log("  queryString=" + queryString);
	    sr.setAttribute("javax.servlet.include.query_string", queryString);
	}

	try {
	    wrapper.invoke(wrequest, hresponse);
	    sresponse.setIncluded(oldIncluded);
	} catch (IOException e) {
	    sresponse.setIncluded(oldIncluded);
	    throw e;
	} catch (ServletException e) {
	    sresponse.setIncluded(oldIncluded);
	    throw e;
	} catch (Throwable t) {
	    sresponse.setIncluded(oldIncluded);
	    throw new ServletException
	      (sm.getString("applicationDispatcher.include.throw"), t);
	}

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	Logger logger = null;
	if (context != null)
	    logger = context.getLogger();
	if (logger != null)
	    logger.log("ApplicationDispatcher[" + context.getPath() + "]: "
		       + message);
	else
	    System.out.println("ApplicationDispatcher[" + context.getPath()
			       + "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

	Logger logger = null;
	if (context != null)
	    logger = context.getLogger();
	if (logger != null)
	    logger.log("ApplicationDispatcher[" + context.getPath() + "] "
		       + message, throwable);
	else {
	    System.out.println("ApplicationDispatcher[" + context.getPath()
			       + "]: " + message);
	    throwable.printStackTrace(System.out);
	}

    }


}
