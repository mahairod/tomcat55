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


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Container;
import org.apache.catalina.Wrapper;

/**
 * Factory for the creation and caching of Filters and creationg of Filter Chains
 *
 *
 * @author Greg Murray
 * @version $Revision: 1.0
 */

public final class ApplicationFilterFactory {

    public static final int FORWARD =1;
    public static final int INCLUDE  =3;
    public static final int REQUEST = 4;

    // ----------------------------------------------------------- Constructors


    /*
     * Prevent instanciation outside of the getInstanceMethod().
     */
    private ApplicationFilterFactory() {
    }


    public static ApplicationFilterFactory getInstance() {
        if (factory == null) {
            factory = new ApplicationFilterFactory();
        }
        return factory;
    }

    /**
     * Construct and return a FilterChain implementation that will wrap the
     * execution of the specified servlet instance.  If we should not execute
     * a filter chain at all, return <code>null</code>.
     * <p>
     * <strong>FIXME</strong> - Pool the chain instances!
     *
     * @param request The servlet request we are processing
     * @param servlet The servlet instance to be wrapped
     */
    public ApplicationFilterChain createFilterChain(ServletRequest request,
                                        Wrapper wrapper, Servlet servlet, int dispatcher) {

        HttpServletRequest hreq = null;
        if (request instanceof HttpServletRequest) hreq = (HttpServletRequest)request;
        // If there is no servlet to execute, return null
        if (servlet == null)
            return (null);

        // Create and initialize a filter chain object
        ApplicationFilterChain filterChain = new ApplicationFilterChain();

        filterChain.setServlet(servlet);

        filterChain.setSupport(((StandardWrapper)wrapper).getInstanceSupport());

        // Acquire the filter mappings for this Context
        StandardContext context = (StandardContext) wrapper.getParent();
        FilterMap filterMaps[] = context.findFilterMaps();

        // If there are no filter mappings, we are done
        if ((filterMaps == null) || (filterMaps.length == 0))
            return (filterChain);
//        if (debug >= 1)
//            log("createFilterChain:  Processing " + filterMaps.length +
//                " filter map entries");

        // Acquire the information we will need to match filter mappings
        String requestPath = null;
        if (hreq != null) {
            String contextPath = hreq.getContextPath();
            if (contextPath == null)
                contextPath = "";
            String requestURI = hreq.getRequestURI();
            //((HttpRequest) request).getDecodedRequestURI();
            if (requestURI.length() >= contextPath.length())
                requestPath = requestURI.substring(contextPath.length());
        }
        String servletName = wrapper.getName();
//        if (debug >= 1) {
//            log(" requestPath=" + requestPath);
//            log(" servletName=" + servletName);
//        }
        int n = 0;

        // Add the relevant path-mapped filters to this filter chain
        for (int i = 0; i < filterMaps.length; i++) {
//            if (debug >= 2)
//                log(" Checking path-mapped filter '" +
//                    filterMaps[i] + "'");
            if (!matchFiltersURL(filterMaps[i], requestPath, dispatcher))
                continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                context.findFilterConfig(filterMaps[i].getFilterName());
            if (filterConfig == null) {
//                if (debug >= 2)
//                    log(" Missing path-mapped filter '" +
//                        filterMaps[i] + "'");
                ;       // FIXME - log configuration problem
                continue;
            }
//            if (debug >= 2)
//                log(" Adding path-mapped filter '" +
//                    filterConfig.getFilterName() + "'");
            filterChain.addFilter(filterConfig);
            n++;
        }

        // Add filters that match on servlet name second
        for (int i = 0; i < filterMaps.length; i++) {
//            if (debug >= 2)
//                log(" Checking servlet-mapped filter '" +
//                    filterMaps[i] + "'");
            if (!matchFiltersServlet(filterMaps[i], servletName, dispatcher))
                continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                context.findFilterConfig(filterMaps[i].getFilterName());
            if (filterConfig == null) {
//                if (debug >= 2)
//                    log(" Missing servlet-mapped filter '" +
//                        filterMaps[i] + "'");
                ;       // FIXME - log configuration problem
                continue;
            }
//            if (debug >= 2)
//                log(" Adding servlet-mapped filter '" +
//                     filterMaps[i] + "'");
            filterChain.addFilter(filterConfig);
            n++;
        }

        // Return the completed filter chain
//        if (debug >= 2)
//            log(" Returning chain with " + n + " filters");
        return (filterChain);

    }

    /**
     * Return <code>true</code> if the context-relative request path
     * matches the requirements of the specified filter mapping;
     * otherwise, return <code>null</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param requestPath Context-relative request path of this request
     */
    private boolean matchFiltersURL(FilterMap filterMap,
                                    String requestPath,
                                    int dispatcher) {

//      if (debug >= 3)
//          log("  Matching request path '" + requestPath +
//              "' against mapping " + filterMap);

        if (requestPath == null)
            return (false);

        // Match on context relative request path
        String testPath = filterMap.getURLPattern();
        if (testPath == null)
            return (false);

        // Case 1 - Exact Match
        if (testPath.equals(requestPath))
            return (matchDispatcher(filterMap,dispatcher));

        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*"))
            return (matchDispatcher(filterMap,dispatcher));      // Optimize a common case
        if (testPath.endsWith("/*")) {
            String comparePath = requestPath;
            while (true) {
                if (testPath.equals(comparePath + "/*"))
                    return (matchDispatcher(filterMap,dispatcher));
                int slash = comparePath.lastIndexOf('/');
                if (slash < 0)
                    break;
                comparePath = comparePath.substring(0, slash);
            }
            return (false);
        }

        // Case 3 - Extension Match
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash))
                if  (testPath.equals("*." +
                    requestPath.substring(period + 1))) {
                    return (matchDispatcher(filterMap,dispatcher));
                }
        }

        // Case 4 - "Default" Match
        return (false); // NOTE - Not relevant for selecting filters
    }

    /*
     *  Convienience method which returns true if  the dispatcher type
     *  matches the dispatcher types specified in the FilterMap
     */
    private boolean matchDispatcher(FilterMap filterMap, int dispatcher) {
        switch (dispatcher) {
            case FORWARD : {
                if (filterMap.getDispatcherMapping() == FilterMap.FORWARD ||
                    filterMap.getDispatcherMapping() == FilterMap.FORWARD_INCLUDE ||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_FORWARD ||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_FORWARD_INCLUDE) {
                        return true;
                }
                break;
            }
            case INCLUDE : {
                if (filterMap.getDispatcherMapping() == FilterMap.INCLUDE ||
                    filterMap.getDispatcherMapping() == FilterMap.FORWARD_INCLUDE ||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_INCLUDE||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_FORWARD_INCLUDE) {
                        return true;
                }
                break;
            }
            case REQUEST : {
                if (filterMap.getDispatcherMapping() == FilterMap.REQUEST ||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_INCLUDE ||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_FORWARD ||
                    filterMap.getDispatcherMapping() == FilterMap.REQUEST_FORWARD_INCLUDE) {
                        return true;
                }
                break;
            }
        }
        return false;
    }

   /**
     * Return <code>true</code> if the specified servlet name matches
     * the requirements of the specified filter mapping; otherwise
     * return <code>false</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param servletName Servlet name being checked
     */
    private boolean matchFiltersServlet(FilterMap filterMap,
                                        String servletName,
                                        int dispatcher) {

//      if (debug >= 3)
//          log("  Matching servlet name '" + servletName +
//              "' against mapping " + filterMap);

        if (servletName == null)
            return (false);
        else
            if  (servletName.equals(filterMap.getServletName()))
                return (matchDispatcher(filterMap,dispatcher));
            else return false;

    }


    private static ApplicationFilterFactory factory;
}
