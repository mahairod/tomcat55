/*
 * $Header$
 * $Revision$
 * $Date$
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
 */ 

package org.apache.jasper.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.security.CodeSource;
import java.security.PermissionCollection;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JspEngineContext;
import org.apache.jasper.runtime.*;

import org.apache.jasper.compiler.Compiler;

import org.apache.jasper.logging.Logger;

/**
 * The JSP engine (a.k.a Jasper).
 *
 * The servlet container is responsible for providing a
 * URLClassLoader for the web application context Jasper
 * is being used in. Jasper will try get the Tomcat
 * ServletContext attribute for its ServletContext class
 * loader, if that fails, it uses the parent class loader.
 * In either case, it must be a URLClassLoader.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Remy Maucherat
 * @author Kin-man Chung
 * @author Glenn Nielsen
 */

class JspServletWrapper {

    private Servlet theServlet;
    private String jspUri;
    private Class servletClass;
    private URLClassLoader loader;
    private JspCompilationContext ctxt;
    private long available = 0L;
    private ServletConfig config;
    private Options options;
    private Compiler compiler;
    private PermissionCollection permissionCollection;
    private CodeSource codeSource;
    private URLClassLoader parentClassLoader;

    JspServletWrapper(ServletConfig config, Options options, String jspUri,
                      boolean isErrorPage, String classpath,
                      URLClassLoader parentClassLoader,
                      PermissionCollection permissionCollection,
                      CodeSource codeSource) throws JasperException {

        this.jspUri = jspUri;
        this.theServlet = null;
        this.config = config;
        this.options = options;
        this.parentClassLoader = parentClassLoader;
        this.permissionCollection = permissionCollection;
        this.codeSource = codeSource;
        ctxt = new JspEngineContext
            (parentClassLoader, classpath, config.getServletContext(),
             jspUri, isErrorPage, options);
        compiler = ctxt.createCompiler();
    }

    private void load() throws JasperException, ServletException {

        try {
            // This is to maintain the original protocol.
            destroy();
            theServlet = (Servlet) servletClass.newInstance();
        } catch (Exception ex) {
            throw new JasperException(ex);
        }
        theServlet.init(config);
    }

    public void service(HttpServletRequest request, 
                        HttpServletResponse response,
                        boolean precompile)
	    throws ServletException, IOException, FileNotFoundException {
        try {

            if ((available > 0L) && (available < Long.MAX_VALUE)) {
                response.setDateHeader("Retry-After", available);
                response.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     Constants.getString("jsp.error.unavailable"));
            }

            if (loadJSP(request, response) || theServlet == null) {
                load();
            }

            // If a page is to only to be precompiled return.
            if (precompile) {
                return;
            }

            if (theServlet instanceof SingleThreadModel) {
               // sync on the wrapper so that the freshness
               // of the page is determined right before servicing
               synchronized (this) {
                   theServlet.service(request, response);
                }
            } else {
                theServlet.service(request, response);
            }

        } catch (UnavailableException ex) {
            String includeRequestUri = (String)
                request.getAttribute("javax.servlet.include.request_uri");
            if (includeRequestUri != null) {
                // This file was included. Throw an exception as
                // a response.sendError() will be ignored by the
                // servlet engine.
                throw ex;
            } else {
                int unavailableSeconds = ex.getUnavailableSeconds();
                if (unavailableSeconds <= 0) {
                    unavailableSeconds = 60;        // Arbitrary default
                }
                available = System.currentTimeMillis() +
                    (unavailableSeconds * 1000L);
                response.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                     ex.getMessage());
            }
        } catch (FileNotFoundException ex) {
            String includeRequestUri = (String)
                request.getAttribute("javax.servlet.include.request_uri");
            if (includeRequestUri != null) {
                // This file was included. Throw an exception as
                // a response.sendError() will be ignored by the
                // servlet engine.
                throw new ServletException(ex);
            } else {
                try {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                                      ex.getMessage());
                } catch (IllegalStateException ise) {
                    Constants.jasperLog.log(
                        Constants.getString("jsp.error.file.not.found",
			                    new Object[] { ex.getMessage() }),
                        ex, Logger.ERROR);
                }
            }
        }
    }

    public void destroy() {
        if (theServlet != null) {
            theServlet.destroy();
        }
    }


    /*  Check if we need to reload a JSP page.
     *
     *  Side-effect: re-compile the JSP page.
     *
     *  @return true if JSP has been recompiled
     */
    boolean loadJSP(HttpServletRequest req, HttpServletResponse res) 
	throws JasperException, FileNotFoundException {

	boolean outDated = false; 
        
        if (options.getReloading() || (servletClass == null)) {
            try {
                synchronized (this) {

                    // Synchronizing on jsw enables simultaneous 
                    // compilations of different pages, but not the 
                    // same page.
                    outDated = compiler.isOutDated();
                    if (outDated) {
                        compiler.compile();
                    }

                    if ((servletClass == null) || outDated) {
                        URL [] urls = new URL[1];
			File outputDir = 
                            new File(normalize(ctxt.getOutputDir()));
			urls[0] = outputDir.toURL();
			loader = new JasperLoader
                            (urls,ctxt.getServletClassName(),
                             parentClassLoader, permissionCollection,
                             codeSource);
			servletClass = loader.loadClass
                            (Constants.JSP_PACKAGE_NAME + "." 
                             + ctxt.getServletClassName());
                    }

                }
            } catch (FileNotFoundException ex) {
                compiler.removeGeneratedFiles();
                throw ex;
            } catch (ClassNotFoundException cex) {
		throw new JasperException(
		    Constants.getString("jsp.error.unable.load"),cex);
	    } catch (MalformedURLException mue) {
                throw new JasperException(
		    Constants.getString("jsp.error.unable.load"),mue);
	    } catch (JasperException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new JasperException
                    (Constants.getString("jsp.error.unable.compile"), ex);
            }
        }

	return outDated;
    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    protected String normalize(String path) {

        if (path == null) {
            return null;
        }

        String normalized = path;
        
	// Normalize the slashes and add leading slash if necessary
	if (normalized.indexOf('\\') >= 0) {
	    normalized = normalized.replace('\\', '/');
        }
	if (!normalized.startsWith("/")) {
	    normalized = "/" + normalized;
        }

	// Resolve occurrences of "//" in the normalized path
	while (true) {
	    int index = normalized.indexOf("//");
	    if (index < 0) {
		break;
            }
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 1);
	}

	// Resolve occurrences of "/./" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/./");
	    if (index < 0) {
		break;
            }
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 2);
	}

	// Resolve occurrences of "/../" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/../");
	    if (index < 0) {
		break;
            }
	    if (index == 0) {
		return (null);	// Trying to go outside our context
            }
	    int index2 = normalized.lastIndexOf('/', index - 1);
	    normalized = normalized.substring(0, index2) +
		normalized.substring(index + 3);
	}

	// Return the normalized path that we have completed
	return (normalized);

    }

}
