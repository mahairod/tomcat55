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
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.StringManager;


/**
 * Internal class containing the (possibly wrapped) request and response
 * objects that will be passed on to the servlet that is ultimately
 * invoked by a request dispatcher.
 * <p>
 * <strong>WARNING</strong> - This implementation assumes that the application
 * is obeying the restriction (from the specification) that any request or
 * response wrappers it creates implement the servlet API wrapper interfaces.
 * If this assumption is violated, ClassCastException errors will be thrown.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

class ApplicationDispatcherContext {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ApplicationDispatcherContext, with the specified
     * application request and response objects.
     *
     * @param dispatcher The ApplicationDispatcher we are associated with
     * @param request The servlet request we are processing
     * @param response The servlet response we are processing
     */
    ApplicationDispatcherContext(ApplicationDispatcher dispatcher,
                                 ServletRequest request,
                                 ServletResponse response) {

        super();
        this.appRequest = request;
        this.appResponse = response;
        this.dispatcher = dispatcher;
        this.outerRequest = request;
        this.outerResponse = response;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The request specified by the dispatching application.
     */
    private ServletRequest appRequest = null;


    /**
     * The response specified by the dispatching application.
     */
    private ServletResponse appResponse = null;


    /**
     * The ApplicationDispatcher we are associated with.
     */
    private ApplicationDispatcher dispatcher = null;


    /**
     * The outermost request that will be passed on to the invoked servlet.
     */
    private ServletRequest outerRequest = null;


    /**
     * The outermost response that will be passed on to the invoked servlet.
     */
    private ServletResponse outerResponse = null;


    /**
     * The StringManager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * The request wrapper we have created and installed (if any).
     */
    private ServletRequest wrapRequest = null;


    /**
     * The response wrapper we have created and installed (if any).
     */
    private ServletResponse wrapResponse = null;


    // -------------------------------------------------------- Package Methods


    /**
     * Invoke the servlet specified by this wrapper, using the request and
     * response chains we have constructed.
     */
    void invoke(Wrapper wrapper)
      throws IOException, ServletException {

        // Initialize local variables we will need
        HttpServletRequest hrequest = null;
        if (outerRequest instanceof HttpServletRequest)
            hrequest = (HttpServletRequest) outerRequest;
        HttpServletResponse hresponse = null;
        if (outerResponse instanceof HttpServletResponse)
            hresponse = (HttpServletResponse) outerResponse;
        Servlet servlet = null;
        IOException ioException = null;
        ServletException servletException = null;
        RuntimeException runtimeException = null;
        boolean unavailable = false;

	// Check for the servlet being marked unavailable
	if (wrapper.isUnavailable()) {
	    dispatcher.log(sm.getString("applicationDispatcher.isUnavailable",
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
	    dispatcher.log(sm.getString("applicationDispatcher.allocateException",
                                        wrapper.getName()), e);
	    servletException = e;
	    servlet = null;
	} catch (Throwable e) {
	    dispatcher.log(sm.getString("applicationDispatcher.allocateException",
                                        wrapper.getName()), e);
	    servletException = new ServletException
                (sm.getString("applicationDispatcher.allocateException",
                              wrapper.getName()), e);
	    servlet = null;
	}

	// Call the service() method for the allocated servlet instance
	try {
	    if (servlet != null) {
                if ((hrequest != null) && (hresponse != null)) {
                    servlet.service((HttpServletRequest) outerRequest,
                                    (HttpServletResponse) outerResponse);
                } else {
                    servlet.service(outerRequest, outerResponse);
                }
	    }
	} catch (IOException e) {
            dispatcher.log(sm.getString("applicationDispatcher.serviceException",
                                        wrapper.getName()), e);
	    ioException = e;
	} catch (UnavailableException e) {
	    dispatcher.log(sm.getString("applicationDispatcher.serviceException",
                                        wrapper.getName()), e);
	    servletException = e;
	    wrapper.unavailable(e);
	} catch (ServletException e) {
	    dispatcher.log(sm.getString("applicationDispatcher.serviceException",
                                        wrapper.getName()), e);
	    servletException = e;
	} catch (RuntimeException e) {
	    dispatcher.log(sm.getString("applicationDispatcher.serviceException",
                                        wrapper.getName()), e);
            runtimeException = e;
	}

	// Deallocate the allocated servlet instance
	try {
	    if (servlet != null)
		wrapper.deallocate(servlet);
	} catch (ServletException e) {
            dispatcher.log(sm.getString("applicationDispatcher.deallocateException",
                                        wrapper.getName()), e);
	    servletException = e;
	} catch (Throwable e) {
	    dispatcher.log(sm.getString("applicationDispatcher.deallocateException",
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
	if (runtimeException != null)
	    throw runtimeException;

    }


    /**
     * Unwrap the request if we have wrapped it.
     */
    void unwrapRequest() {

        if (wrapRequest == null)
            return;

        ServletRequest previous = null;
        ServletRequest current = outerRequest;
        while (current != null) {

            // If we run into the container request we are done
            if (current instanceof Request)
                break;

            // Remove the current request if it is our wrapper
            if (current == wrapRequest) {
                ServletRequest next =
                  ((ServletRequestWrapper) current).getRequest();
                if (previous == null)
                    outerRequest = null;
                else
                    ((ServletRequestWrapper) previous).setRequest(next);
                break;
            }

            // Advance to the next request in the chain
            previous = current;
            current = ((ServletRequestWrapper) current).getRequest();

        }

    }


    /**
     * Unwrap the response if we have wrapped it.
     */
    void unwrapResponse() {

        if (wrapResponse == null)
            return;

        ServletResponse previous = null;
        ServletResponse current = outerResponse;
        while (current != null) {

            // If we run into the container response we are done
            if (current instanceof Response)
                break;

            // Remove the current response if it is our wrapper
            if (current == wrapResponse) {
                ServletResponse next =
                  ((ServletResponseWrapper) current).getResponse();
                if (previous == null)
                    outerResponse = null;
                else
                    ((ServletResponseWrapper) previous).setResponse(next);
                break;
            }

            // Advance to the next response in the chain
            previous = current;
            current = ((ServletResponseWrapper) current).getResponse();

        }

    }


    /**
     * Create and return a request wrapper that has been inserted in the
     * appropriate spot in the request chain.
     */
    ApplicationHttpRequest wrapRequest() {

        // Locate the request we should insert in front of
        ServletRequest previous = null;
        ServletRequest current = outerRequest;
        while (current != null) {
            if (current instanceof ApplicationHttpRequest)
                break;
            if (current instanceof Request)
                break;
            previous = current;
            current = ((ServletRequestWrapper) current).getRequest();
        }

        // Instantiate a new wrapper at this point and insert it in the chain
        ApplicationHttpRequest wrapper =
          new ApplicationHttpRequest((HttpServletRequest) current);
        if (previous == null)
            outerRequest = wrapper;
        else
            ((ServletRequestWrapper) previous).setRequest(wrapper);
        wrapRequest = wrapper;
        return (wrapper);

    }


    /**
     * Create and return a response wrapper that has been inserted in the
     * appropriate spot in the response chain.
     */
    ApplicationHttpResponse wrapResponse() {

        // Locate the response we should insert in front of
        ServletResponse previous = null;
        ServletResponse current = outerResponse;
        while (current != null) {
            if (current instanceof ApplicationHttpResponse)
                break;
            if (current instanceof Response)
                break;
            previous = current;
            current = ((ServletResponseWrapper) current).getResponse();
        }

        // Instantiate a new wrapper at this point and insert it in the chain
        ApplicationHttpResponse wrapper =
          new ApplicationHttpResponse((HttpServletResponse) current);
        if (previous == null)
            outerResponse = wrapper;
        else
            ((ServletResponseWrapper) previous).setResponse(wrapper);
        wrapResponse = wrapper;
        return (wrapper);

    }


}
