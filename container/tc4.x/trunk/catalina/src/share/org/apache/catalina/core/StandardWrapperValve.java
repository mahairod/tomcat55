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
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.util.InstanceSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardWrapper</code> container implementation.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

final class StandardWrapperValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
	StringManager.getManager(Constants.Package);


    // --------------------------------------------------------- Public Methods


    /**
     * Invoke the servlet we are managing, respecting the rules regarding
     * servlet lifecycle and SingleThreadModel support.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public void invoke(Request request, Response response)
	throws IOException, ServletException {

	// Initialize local variables we may need
	boolean unavailable = false;
	Throwable throwable = null;
	StandardWrapper wrapper = (StandardWrapper) getContainer();
	InstanceSupport support = wrapper.getInstanceSupport();
	ServletRequest sreq = request.getRequest();
	ServletResponse sres = response.getResponse();
	Servlet servlet = null;
	HttpServletRequest hreq = null;
	if (sreq instanceof HttpServletRequest)
	    hreq = (HttpServletRequest) sreq;
	HttpServletResponse hres = null;
	if (sres instanceof HttpServletResponse)
	    hres = (HttpServletResponse) sres;

	// Check for the servlet being marked unavailable
	if (wrapper.isUnavailable()) {
	    log(sm.getString("standardWrapper.isUnavailable",
			     wrapper.getName()));
	    if (hres == null) {
		;	// NOTE - Not much we can do generically
	    } else {
		long available = wrapper.getAvailable();
		if ((available > 0L) && (available < Long.MAX_VALUE))
		    hres.setDateHeader("Retry-After", available);
		hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
			       sm.getString("standardWrapper.isUnavailable",
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
	    log(sm.getString("standardWrapper.allocateException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	    servlet = null;
	} catch (Throwable e) {
	    log(sm.getString("standardWrapper.allocateException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	    servlet = null;
	}

	// Call the service() method of this instance
	try {
	    if (servlet != null) {
		support.fireInstanceEvent(InstanceEvent.BEFORE_SERVICE_EVENT,
					  servlet);
		if ((servlet instanceof HttpServlet) &&
		    (hreq != null) && (hres != null)) {
		    ((HttpServlet) servlet).service(hreq, hres);
		} else {
		    servlet.service(sreq, sres);
		}
		support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
					  servlet);
	    }
	} catch (IOException e) {
	    support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
				      servlet);
	    log(sm.getString("standardWrapper.serviceException",
			     wrapper.getName()), e);
	    ;	// No reporting to the response
	    ;	// No change in availability status
	} catch (UnavailableException e) {
	    support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
				      servlet);
	    log(sm.getString("standardWrapper.serviceException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	    wrapper.unavailable(e);
	} catch (ServletException e) {
	    support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
				      servlet);
	    log(sm.getString("standardWrapper.serviceException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	} catch (Throwable e) {
	    support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
				      servlet);
	    log(sm.getString("standardWrapper.serviceException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	}

	// Deallocate the allocated servlet instance
	try {
	    if (servlet != null) {
		wrapper.deallocate(servlet);
	    }
	} catch (ServletException e) {
	    log(sm.getString("standardWrapper.deallocateException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	} catch (Throwable e) {
	    log(sm.getString("standardWrapper.deallocateException",
			     wrapper.getName()), e);
	    throwable = e;
	    exception(request, response, e);
	}

	// Generate a response for the generated HTTP status and message
	if (throwable == null) {
	    status(request, response);
	}

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Handle an HTTP status code or Java exception by forwarding control
     * to the location included in the specified errorPage object.  It is
     * assumed that the caller has already recorded any request attributes
     * that are to be forwarded to this page.  Return <code>true</code> if
     * we successfully utilized the specified error page location, or
     * <code>false</code> if the default error report should be rendered.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param errorPage The errorPage directive we are obeying
     */
    private boolean custom(Request request, Response response,
			   ErrorPage errorPage) {

	if (debug >= 1)
	    log("Processing " + errorPage);

	// Validate our current environment
	if (!(request instanceof HttpRequest)) {
	    if (debug >= 1)
		log(" Not processing an HTTP request --> default handling");
	    return (false);	// NOTE - Nothing we can do generically
	}
	HttpServletRequest hreq =
	    (HttpServletRequest) request.getRequest();
	if (!(response instanceof HttpResponse)) {
	    if (debug >= 1)
		log("Not processing an HTTP response --> default handling");
	    return (false);	// NOTE - Nothing we can do generically
	}
	HttpServletResponse hres =
	    (HttpServletResponse) response.getResponse();

	try {

	    // Reset the response if possible (else IllegalStateException)
	    hres.reset();

	    // Forward control to the specified location
	    ServletContext servletContext =
		((Context) container.getParent()).getServletContext();
	    RequestDispatcher rd =
		servletContext.getRequestDispatcher(errorPage.getLocation());
	    rd.forward(hreq, hres);

	    // Indicate that we have successfully processed this custom page
	    return (true);

	} catch (Throwable t) {

	    // Report our failure to process this custom page
	    log("Exception Processing " + errorPage, t);
	    return (false);

	}

    }


    /**
     * Handle the specified ServletException encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    private void exception(Request request, Response response,
			   Throwable exception) {

	// Handle a custom error page for this status code
	Context context = (Context) container.getParent();
	Throwable realError = exception;
	if (exception instanceof ServletException) {
	    Throwable rootCause =
		((ServletException) exception).getRootCause();
	    if (rootCause != null)
		realError = rootCause;
	}
        ErrorPage errorPage =
	    context.findErrorPage(realError.getClass().getName());
	if (errorPage != null) {
	    request.getRequest().setAttribute(Constants.EXCEPTION_TYPE,
					      realError.getClass());
	    if (custom(request, response, errorPage))
		return;
	}

	// Reset the response (if possible)
	try {
	    response.getResponse().reset();
	} catch (IllegalStateException e) {
	    ;
	}

	// Indicate an INTERNAL SERVER ERROR status (if possible)
	try {
	    ServletResponse sresponse = response.getResponse();
	    if (sresponse instanceof HttpServletResponse)
		((HttpServletResponse) sresponse).sendError
		    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	} catch (IllegalStateException e) {
	    ;
	} catch (IOException e) {
	    ;
	}

	// Render a default HTML exception report page
	Throwable rootCause = null;
	if (exception instanceof ServletException)
	    rootCause = ((ServletException) exception).getRootCause();
	try {
	    try {
		response.getResponse().setContentType("text/html");
	    } catch (Throwable t) {
		;
	    }
	    PrintWriter writer = response.getReporter();
	    try {
		response.getResponse().flushBuffer();
	    } catch (IOException e) {
		;
	    }
	    writer.println("<html>");
	    writer.println("<head>");
	    writer.println("<title>" +
			   sm.getString("standardWrapper.exception0") +
			   "</title>");
	    writer.println("</head>");
	    writer.println("<body bgcolor=\"white\">");
	    writer.println("<br><br>");
	    writer.println("<h1>" +
			   sm.getString("standardWrapper.exception1") +
			   "</h1>");
	    if (rootCause != null)
		writer.println("<h3>" +
			       sm.getString("standardWrapper.exception2") +
			       "</h3>");
	    writer.println("<pre>");
	    exception.printStackTrace(writer);
	    writer.println("</pre>");
	    if (rootCause != null) {
		writer.println("<h3>" +
			       sm.getString("standardWrapper.exception3") +
			       "</h3>");
		writer.println("<pre>");
		rootCause.printStackTrace(writer);
		writer.println("</pre>");
	    }
	    writer.println("</body>");
	    writer.println("</html>");
	    writer.flush();
	} catch (IllegalStateException e) {
	    ;
	}

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	Logger logger = null;
	if (container != null)
	    logger = container.getLogger();
	if (logger != null)
	    logger.log("StandardWrapperValve[" + container.getName() + "]: "
		       + message);
	else {
	    String containerName = null;
	    if (container != null)
		containerName = container.getName();
	    System.out.println("StandardWrapperValve[" + containerName
			       + "]: " + message);
	}

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

	Logger logger = null;
	if (container != null)
	    logger = container.getLogger();
	if (logger != null)
	    logger.log("StandardWrapperValve[" + container.getName() + "]: "
		       + message, throwable);
	else {
	    String containerName = null;
	    if (container != null)
		containerName = container.getName();
	    System.out.println("StandardWrapperValve[" + containerName
			       + "]: " + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}

    }


    /**
     * Handle the HTTP status code (and corresponding message) generated
     * while processing the specified Request to produce the specified
     * Response.  Any exceptions that occur during generation of the error
     * report are logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     */
    private void status(Request request, Response response) {

	// Do nothing on non-HTTP responses
	if (!(response instanceof HttpResponse))
	    return;
	HttpResponse hresponse = (HttpResponse) response;
	if (!(response.getResponse() instanceof HttpServletResponse))
	    return;
	HttpServletResponse hres =
	    (HttpServletResponse) response.getResponse();
	int statusCode = hresponse.getStatus();
	String message = hresponse.getMessage();
	if (message == null)
	    message = "";

	// Handle a custom error page for this status code
	Context context = (Context) container.getParent();
	ErrorPage errorPage = context.findErrorPage(statusCode);
	if (errorPage != null) {
	    request.getRequest().setAttribute(Constants.STATUS_CODE,
					      new Integer(statusCode));
	    request.getRequest().setAttribute(Constants.MESSAGE,
					      message);
	    if (custom(request, response, errorPage))
		return;
	}

	// Do nothing on an OK status
	if (statusCode == HttpServletResponse.SC_OK)
	    return;
        if (statusCode < 300)
            return;

	// Do nothing if there is no report for the specified status code
	String report = null;
	try {
	    report = sm.getString("http." + statusCode, message);
	} catch (Throwable t) {
	    ;
	}
	if (report == null)
	    return;

	// Reset the response data buffer (if possible)
	try {
	    response.resetBuffer();
	} catch (Throwable e) {
	    ;
	}

	// Render a default HTML status report page
	try {
	    try {
		hres.setContentType("text/html");
	    } catch (Throwable t) {
		;
	    }
	    PrintWriter writer = response.getReporter();
	    try {
		hres.flushBuffer();
	    } catch (IOException e) {
		;
	    }
	    writer.println("<html>");
	    writer.println("<head>");
	    writer.println("<title>" +
			   sm.getString("standardWrapper.statusTitle") +
			   "</title>");
	    writer.println("</head>");
	    writer.println("<body bgcolor=\"white\">");
	    writer.println("<br><br>");
	    writer.println("<h1>" +
			   sm.getString("standardWrapper.statusHeader",
					"" + statusCode, message) +
			   "</h1>");
	    writer.println(report);
	    writer.println("</body>");
	    writer.println("</html>");
	    writer.flush();
	} catch (IllegalStateException e) {
	    ;
	}
        

    }


}
