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


import javax.naming.directory.DirContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Mapper;
import org.apache.catalina.Request;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;
import org.apache.naming.resources.Resource;


/**
 * Implementation of <code>Mapper</code> for a <code>Context</code>,
 * designed to process HTTP requests.  This mapper selects an appropriate
 * <code>Wrapper</code> based on the request URI included in the request.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  This Mapper only works with a
 * <code>StandardContext</code>, because it relies on internal APIs.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class StandardContextMapper
    implements Mapper {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Mapper is associated.
     */
    private StandardContext context = null;


    /**
     * The protocol with which this Mapper is associated.
     */
    private String protocol = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Mapper is associated.
     */
    public Container getContainer() {

        return (context);

    }


    /**
     * Set the Container with which this Mapper is associated.
     *
     * @param container The newly associated Container
     *
     * @exception IllegalArgumentException if this Container is not
     *  acceptable to this Mapper
     */
    public void setContainer(Container container) {

        if (!(container instanceof StandardContext))
            throw new IllegalArgumentException
                (sm.getString("httpContextMapper.container"));
        context = (StandardContext) container;

    }


    /**
     * Return the protocol for which this Mapper is responsible.
     */
    public String getProtocol() {

        return (this.protocol);

    }


    /**
     * Set the protocol for which this Mapper is responsible.
     *
     * @param protocol The newly associated protocol
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the child Container that should be used to process this Request,
     * based upon its characteristics.  If no such child Container can be
     * identified, return <code>null</code> instead.
     *
     * @param request Request being processed
     * @param update Update the Request to reflect the mapping selection?
     *
     * @exception IllegalArgumentException if the relative portion of the
     *  path cannot be URL decoded
     */
    public Container map(Request request, boolean update) {

        int debug = context.getDebug();

        // Has this request already been mapped?
        if (update && (request.getWrapper() != null))
            return (request.getWrapper());

        // Identify the context-relative URI to be mapped
        String contextPath =
            ((HttpServletRequest) request.getRequest()).getContextPath();
        String requestURI = ((HttpRequest) request).getDecodedRequestURI();
        String relativeURI = requestURI.substring(contextPath.length());
       
	/*
	 * Remove any URI params from the relativeURI, so they won't be 
	 * considered by the mapping algorithm.
	 */
        int uriParamsIndex = relativeURI.indexOf(';');
        if (uriParamsIndex >= 0) {
            relativeURI = relativeURI.substring(0, uriParamsIndex);
	}

        if (debug >= 1)
            context.log("Mapping contextPath='" + contextPath +
                        "' with requestURI='" + requestURI +
                        "' and relativeURI='" + relativeURI + "'");

        // Apply the standard request URI mapping rules from the specification
        Wrapper wrapper = null;
        String servletPath = relativeURI;
        String pathInfo = null;
        String name = null;

        // Rule 1 -- Exact Match
        if (wrapper == null) {
            if (debug >= 2)
                context.log("  Trying exact match");
            if (!(relativeURI.equals("/")))
                name = context.findServletMapping(relativeURI);
            if (name != null)
                wrapper = (Wrapper) context.findChild(name);
            if (wrapper != null) {
                servletPath = relativeURI;
                pathInfo = null;
            }
        }

        // Rule 2 -- Prefix Match
        if (wrapper == null) {
            if (debug >= 2)
                context.log("  Trying prefix match");
            servletPath = relativeURI;
            while (true) {
                name = context.findServletMapping(servletPath + "/*");
                if (name != null)
                    wrapper = (Wrapper) context.findChild(name);
                if (wrapper != null) {
                    pathInfo = relativeURI.substring(servletPath.length());
                    if (pathInfo.length() == 0)
                        pathInfo = null;
                    break;
                }
                int slash = servletPath.lastIndexOf('/');
                if (slash < 0)
                    break;
                servletPath = servletPath.substring(0, slash);
            }
        }

        // Rule 3 -- Extension Match
        if (wrapper == null) {
            if (debug >= 2)
                context.log("  Trying extension match");
            int slash = relativeURI.lastIndexOf('/');
            if (slash >= 0) {
                String last = relativeURI.substring(slash);
                int period = last.lastIndexOf('.');
                if (period >= 0) {
                    String pattern = "*" + last.substring(period);
                    name = context.findServletMapping(pattern);
                    if (name != null)
                        wrapper = (Wrapper) context.findChild(name);
                    if (wrapper != null) {
                        servletPath = relativeURI;
                        pathInfo = null;
                    }
                }
            }
        }

        // Rule 4 -- Default Match
        if (wrapper == null) {
            if (debug >= 2)
                context.log("  Trying default match");
            name = context.findServletMapping("/");
            if (name != null)
                wrapper = (Wrapper) context.findChild(name);
            if (wrapper != null) {
                servletPath = relativeURI;
                pathInfo = null;

                if ( relativeURI.endsWith("/") ) {
                    WelcomeURIProcessing wfp = new WelcomeURIProcessing( relativeURI );
                    if ( wfp.mapsToServlet ){
                        wrapper = wfp.wrapper;
                        servletPath = wfp.servletPath;
                        pathInfo = wfp.pathInfo;
                    }
                }
            }
        }

        // Update the Request (if requested) and return this Wrapper
        if ((debug >= 1) && (wrapper != null))
            context.log(" Mapped to servlet '" + wrapper.getName() +
                        "' with servlet path '" + servletPath +
                        "' and path info '" + pathInfo +
                        "' and update=" + update);
        if (update) {
            request.setWrapper(wrapper);
            ((HttpRequest) request).setServletPath(servletPath);
            ((HttpRequest) request).setPathInfo(pathInfo);
        }
        return (wrapper);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * This helper class is used as part of an attempt to satisfy
     * Servlet 2.4 spec, section 9.10.  Section 9.10 now allows a
     * welcome file to map to a servlet
     **/
    private class WelcomeURIProcessing {
        boolean mapsToServlet = false;

        Wrapper wrapper = null;
        String  servletPath = null;
        String  pathInfo = null;

        WelcomeURIProcessing (String relativeURI ) {
            mapsToServlet = search(relativeURI);
        }

        boolean search(String relativeURI) {
            DirContext resources = getResources();

            String[] welcomes = context.findWelcomeFiles();

            for(int i=0;i<welcomes.length;i++){
                String newRelativeURI = relativeURI+welcomes[i];

                try {
                    Object obj =  resources.lookup(newRelativeURI);
                    if ( obj != null && obj instanceof Resource ){
                        return false;
                    }
                } catch (NamingException e){
                    // These happen everytime a resource is not found, which is like very often.
                }

                // exact match
                String name = context.findServletMapping(newRelativeURI);
                if ( name != null){
                    // set wrapper...
                    wrapper = (Wrapper) context.findChild(name);
                    if ( wrapper != null ){
                        servletPath = newRelativeURI;
                        pathInfo = null;
                        return true;
                    }
                }

                // prefix match
                servletPath = newRelativeURI;
                while (true) {
                    name = context.findServletMapping(servletPath + "/*");
                    if (name != null) {
                        wrapper = (Wrapper) context.findChild(name);
                    }
                    if (wrapper != null) {
                        pathInfo = newRelativeURI.substring(servletPath.length());
                        if (pathInfo.length() == 0)
                            pathInfo = null;
                        servletPath = newRelativeURI;
                        return true;
                    }
                    int slash = servletPath.lastIndexOf('/');
                    if (slash < 0)
                        break;
                    servletPath = servletPath.substring(0, slash);
                }

            }
            return false;
        }

        /**
         * JNDI resources name.
         */
        private static final String RESOURCES_JNDI_NAME = "java:/comp/Resources";

        /**
         * Get resources. This method will try to retrieve the resources through
         * JNDI first, then in the servlet context if JNDI has failed (it could be
         * disabled). It will return null.
         *
         * @return A JNDI DirContext, or null.
         */
        private DirContext getResources() {

            DirContext result = null;

            // Try the servlet context
            try {
                result = (DirContext) context.getServletContext()
                    .getAttribute(Globals.RESOURCES_ATTR);
            } catch (ClassCastException e) {
                // Failed : Not the right type
            }

            if (result != null)
                return result;

            // Try JNDI
            try {
                result =
                    (DirContext) new InitialContext().lookup(RESOURCES_JNDI_NAME);
            } catch (NamingException e) {
                // Failed
            } catch (ClassCastException e) {
                // Failed : Not the right type
            }

            return result;

        }
    }

}
