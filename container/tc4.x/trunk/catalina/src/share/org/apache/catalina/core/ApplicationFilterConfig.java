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


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.util.Enumerator;


/**
 * Implementation of a <code>javax.servlet.FilterConfig</code> useful in
 * constructing stacks of filters for a particular request.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

final class ApplicationFilterConfig implements FilterConfig {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ApplicationFilterConfig for the specified filter
     * definition.
     *
     * @param filterDef Filter definition for which a FilterConfig is to be
     *  constructed
     * @param wrapper The Wrapper with which we are associated
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     */
    public ApplicationFilterConfig(FilterDef filterDef, Wrapper wrapper)
	throws ClassCastException, ClassNotFoundException,
	       IllegalAccessException, InstantiationException {

	super();
	setWrapper(wrapper);
	setFilterDef(filterDef);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context with which we are associated.
     */
    private Context context = null;


    /**
     * The application Filter we are configured for.
     */
    private Filter filter = null;


    /**
     * The <code>FilterDef</code> that defines our associated Filter.
     */
    private FilterDef filterDef = null;


    /**
     * The <code>ApplicationFilterConfig</code> of the next filter in our
     * configured filter stack.
     */
    private ApplicationFilterConfig nextConfig = null;


    /**
     * The Wrapper with which we are associated.
     */
    private Wrapper wrapper = null;


    // --------------------------------------------------- FilterConfig Methods


    /**
     * Return the name of the filter we are configuring.
     */
    public String getFilterName() {

	return (filterDef.getFilterName());

    }


    /**
     * Return the remaining Filter objects in the Filter stack, in the order
     * they have been configured.
     */
    public Iterator getFilters() {

	// NOTE - The list of filters we are about to accumulate includes
	// the container-provided filter at the end that wraps the call to
	// the servlet's service() method
	ArrayList list = new ArrayList();
	ApplicationFilterConfig next = getNextConfig();
	while (next != null) {
	    list.add(next.getFilter());
	    next = next.getNextConfig();
	}

	// NOTE - The iterator we are about to return probably does support
	// remove(), but calling it has no impact on the actual functionality
	// of filter processing so I don't see it as a big issue
	return (list.iterator());

    }


    /**
     * Return a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter
     * does not exist.
     *
     * @param name Name of the requested initialization parameter
     */
    public String getInitParameter(String name) {

	Map map = filterDef.getParameterMap();
	if (map == null)
	    return (null);
	else
	    return ((String) map.get(name));

    }


    /**
     * Return an <code>Enumeration</code> of the names of the initialization
     * parameters for this Filter.
     */
    public Enumeration getInitParameterNames() {

	Map map = filterDef.getParameterMap();
	if (map == null)
	    return (new Enumerator(new ArrayList()));
	else
	    return (new Enumerator(map.keySet()));

    }


    /**
     * Return the next Filter object in the filter stack.
     */
    public Filter getNext() {

	if (nextConfig == null)
	    return (null);
	else
	    return (nextConfig.getFilter());

    }


    /**
     * Return the ServletContext of our associated web application.
     */
    public ServletContext getServletContext() {

	return (this.context.getServletContext());

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

	StringBuffer sb = new StringBuffer("ApplicationFilterConfig[");
	sb.append("wrapper=");
	sb.append(wrapper.getName());
	sb.append(", filterClass=");
	sb.append(filterDef.getFilterClass());
	sb.append("]");
	return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return the application Filter we are configured for.
     */
    Filter getFilter() {

	return (this.filter);

    }


    /**
     * Set the application Filter we are configured for.
     *
     * @param filter The new application Filter
     */
    void setFilter(Filter filter) {

	this.filter = filter;

    }


    /**
     * Return the filter definition we are configured for.
     */
    FilterDef getFilterDef() {

	return (this.filterDef);

    }


    /**
     * Set the filter definition we are configured for.  This has the side
     * effect of instantiating an instance of the corresponding filter class.
     *
     * @param filterDef The new filter definition
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     */
    void setFilterDef(FilterDef filterDef)
	throws ClassCastException, ClassNotFoundException,
	       IllegalAccessException, InstantiationException {

	this.filterDef = filterDef;
	if (filterDef == null) {

	    // Release any previously allocated filter instance
	    if (this.filter != null)
		this.filter.setFilterConfig(null);
	    this.filter = null;

	} else {

	    // Identify the class loader we will be using
	    String filterClass = filterDef.getFilterClass();
	    ClassLoader classLoader = null;
	    // FIXME - share this test with StandardWrapper somehow
	    if (filterClass.startsWith("org.apache.catalina."))
		classLoader = this.getClass().getClassLoader();
	    else
		classLoader = context.getLoader().getClassLoader();

	    // Instantiate a new instance of this filter
	    Class clazz = classLoader.loadClass(filterClass);
	    this.filter = (Filter) clazz.newInstance();
	    filter.setFilterConfig(this);
	}

    }


    /**
     * Return the <code>ApplicationFilterConfig</code> of the next filter
     * in our filter stack.
     */
    ApplicationFilterConfig getNextConfig() {

	return (nextConfig);

    }


    /**
     * Set the <code>ApplicationFilterConfig</code> of the next filter
     * in our filter stack.
     *
     * @param nextConfig The next filter configuration object
     */
    void setNextConfig(ApplicationFilterConfig nextConfig) {

	this.nextConfig = nextConfig;
	if (nextConfig == null) {
	    this.filter = null;
	    this.context = null;
	}

    }


    /**
     * Return the Wrapper we are configured for.
     */
    Wrapper getWrapper() {

	return (this.wrapper);

    }


    /**
     * Set the Wrapper we are configured for.
     *
     * @param wrapper The new Wrapper
     */
    void setWrapper(Wrapper wrapper) {

	this.wrapper = wrapper;
	if (this.wrapper == null)
	    this.context = null;
	else
	    this.context = (Context) this.wrapper.getParent();

    }


    // -------------------------------------------------------- Private Methods


}
