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
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This is the container-provided Filter that wraps the ultimate call to
 * a servlet's <code>service()</code> method.  It is appended to the filter
 * stack for every request, so that application filters need not take
 * special measures if they are the last filter in the stack.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class ApplicationFilterWrapper implements Filter {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapper filter with default parameters.
     */
    public ApplicationFilterWrapper() {

	this(null);

    }


    /**
     * Construct a new wrapper filter with the specified parameters.
     *
     * @param servlet The servlet being wrapped
     */
    public ApplicationFilterWrapper(Servlet servlet) {

	super();
	this.servlet = servlet;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The filter configuration object for this filter.
     */
    private FilterConfig filterConfig = null;


    /**
     * The servlet that is being wrapped by this filter.
     */
    private Servlet servlet = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Implement the functionality provided by this filter.  In this case,
     * all we are doing is calling the appropriate <code>service()</code>
     * method of the wrapped servlet, depending on whether or not this is
     * an HTTP or non-HTTP environment.
     *
     * @param request The servlet request being processed
     * @param response The servlet response being created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if the servlet itself throws this
     */
    public void doFilter(ServletRequest request, ServletResponse response)
	throws IOException, ServletException {

	// Call the appropriate service() method
	if ((servlet instanceof HttpServlet) &&
	    (request instanceof HttpServletRequest) &&
	    (response instanceof HttpServletResponse)) {
	    HttpServletRequest hrequest = (HttpServletRequest) request;
	    HttpServletResponse hresponse =
		(HttpServletResponse) response;
	    ((HttpServlet) servlet).service(hrequest, hresponse);
	} else {
	    servlet.service(request, response);
	}

	// NOTE - Do *not* call the next filter, because this request
	// has been handled and the corresponding response created

    }


    /**
     * Return the filter configuration object for this Filter.
     */
    public FilterConfig getFilterConfig() {

	return (this.filterConfig);

    }


    /**
     * Set the filter configuration object for this Filter.
     *
     * @param filterConfig The new filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {

	this.filterConfig = filterConfig;
	if (filterConfig == null)
	    this.servlet = null;

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return the servlet instance we are filtering.
     */
    Servlet getServlet() {

	return (this.servlet);

    }


    /**
     * Set the servlet instance we are filtering.
     *
     * @param servlet The new servlet instance
     */
    void setServlet(Servlet servlet) {

	this.servlet = servlet;

    }


}
