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


package org.apache.catalina.servlets;


import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Wrapper;


/**
 * The default servlet-invoking servlet for most web applications,
 * used to serve requests to servlets that have not been registered
 * in the web application deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class InvokerServlet
    extends HttpServlet {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context container associated with our web application.
     */
    private Context context = null;


    /**
     * The debugging detail level for this servlet.
     */
    private int debug = 0;


    // --------------------------------------------------------- Public Methods


    /**
     * Finalize this servlet.
     */
    public void destroy() {

	;	// No actions necessary

    }


    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws IOException, ServletException {

	serveRequest(request, response);

    }


    /**
     * Process a HEAD request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doHead(HttpServletRequest request,
		       HttpServletResponse response)
	throws IOException, ServletException {

	serveRequest(request, response);

    }


    /**
     * Process a POST request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doPost(HttpServletRequest request,
		       HttpServletResponse response)
	throws IOException, ServletException {

	serveRequest(request, response);

    }


    /**
     * Initialize this servlet.
     */
    public void init() throws ServletException {

	// Set our properties from the initialization parameters
	String value = null;
	try {
	    value = getServletConfig().getInitParameter("debug");
	    debug = Integer.parseInt(value);
	} catch (Throwable t) {
	    ;
	}

	// Identify the internal container resources we need
	Wrapper wrapper = (Wrapper) getServletConfig();
	context = (Context) wrapper.getParent();

	if (debug >= 1)
	    log("init: Associated with Context '" + context.getPath() + "'");

    }



    // -------------------------------------------------------- Private Methods


    /**
     * Serve the specified request, creating the corresponding response.
     * After the first time a particular servlet class is requested, it will
     * be served directly (like any registered servlet) because it will have
     * been registered and mapped in our associated Context.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void serveRequest(HttpServletRequest request,
		             HttpServletResponse response)
	throws IOException, ServletException {

	// Identify the class name of the servlet we want
	if (debug >= 1)
	    log("serveRequest: Serving request " + request.getMethod() +
		" " + request.getRequestURI());
	String pathInfo = request.getPathInfo();
	if (pathInfo == null) {
	    if (debug >= 1)
	        log("serveRequest:  Invalid pathInfo '" + pathInfo + "'");
	    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
	                       request.getRequestURI());
	    return;
        }
	String servletClass = pathInfo.substring(1);

	// Identify the servlet name or class, and related information we will need
	int slash = servletClass.indexOf("/");
	if (slash >= 0) {
	    pathInfo = servletClass.substring(slash);
	    servletClass = servletClass.substring(0, slash);
        } else {
	    pathInfo = "";
        }
	String name = "org.apache.catalina.INVOKER." + servletClass;
	String pattern = request.getServletPath() + "/" + servletClass + "/*";
	Wrapper wrapper = null;

	// Are we referencing an existing servlet name?
	wrapper = (Wrapper) context.findChild(servletClass);
	if (wrapper != null) {
	    if (debug >= 1)
	        log("serveRequest:  Using wrapper for servlet '" +
		    wrapper.getName() + "' with mapping '" + pattern + "'");
	    context.addServletMapping(pattern, wrapper.getName());
	}

	// No, create a new wrapper for the specified servlet class
        else {
	    if (debug >= 1)
		log("serveRequest:  Creating wrapper for '" + servletClass +
		    "' with mapping '" + pattern + "'");
	    try {
	        wrapper = context.createWrapper();
		wrapper.setName(name);
		wrapper.setLoadOnStartup(1);
		wrapper.setServletClass(servletClass);
		context.addChild(wrapper);
		context.addServletMapping(pattern, name);
            } catch (Throwable t) {
		log("serveRequest", t);
		response.sendError(HttpServletResponse.SC_NOT_FOUND,
				   request.getRequestURI());
		return;
            }
	}

	// Pass this request on to the identified or newly created wrapper
	StringBuffer sb = new StringBuffer(request.getServletPath());
	sb.append("/");
	sb.append(servletClass);
	sb.append(pathInfo);
	String dispatcherPath = sb.toString();
	if (debug >= 1)
	    log("serveRequest:  Forwarding to '" + dispatcherPath + "'");
	RequestDispatcher rd =
	    getServletContext().getRequestDispatcher(dispatcherPath);
	rd.forward(request, response);

    }


}
