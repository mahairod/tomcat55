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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.runtime.JspSourceDependent;
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

public class JspServletWrapper {

    private Servlet theServlet;
    private String jspUri;
    private Class servletClass;
    private Class tagHandlerClass;
    private JspCompilationContext ctxt;
    private long available = 0L;
    private ServletConfig config;
    private Options options;
    private boolean isTagFile;
    private int tripCount;

    /*
     * JspServletWrapper for JSP pages.
     */
    JspServletWrapper(ServletConfig config, Options options, String jspUri,
                      boolean isErrorPage, JspRuntimeContext rctxt)
            throws JasperException {

	this.isTagFile = false;
        this.config = config;
        this.options = options;
        this.jspUri = jspUri;
        ctxt = new JspCompilationContext(jspUri, isErrorPage, options,
					 config.getServletContext(),
					 this, rctxt);
        ctxt.createOutdir(jspUri);
    }

    /*
     * JspServletWrapper for tag files.
     */
    public JspServletWrapper(ServletContext servletContext, Options options,
			     String tagFilePath, TagInfo tagInfo,
			     TagData tagData, JspRuntimeContext rctxt,
			     Hashtable tagFileJars)
            throws JasperException {

	this.isTagFile = true;
        this.config = null;	// not used
        this.options = options;
	this.jspUri = tagFilePath;
	this.tripCount = 0;
        ctxt = new JspCompilationContext(jspUri, tagInfo, tagData, options,
					 servletContext, this, rctxt,
					 tagFileJars);

	// Store tag file .java and .class files in standard location
	// (/tagfiles/org/apache/jsp/), regardless of the original tag file
	// path
	String standard = null;
	if (tagFilePath.indexOf('/') != -1) {
	    standard = "/tagfiles/org/apache/jsp/tagfile/"
		+ tagFilePath.substring(tagFilePath.lastIndexOf("/") + 1);
	} else {
	    standard = "/tagfiles/org/apache/jsp/tagfile/" + tagFilePath;
	}
        ctxt.createOutdir(standard);
    }

    public JspCompilationContext getJspEngineContext() {
        return ctxt;
    }

    public Servlet getServlet()
        throws ServletException, IOException, FileNotFoundException
    {
        if (ctxt.isReload()) {
            synchronized (this) {
                // Synchronizing on jsw enables simultaneous loading
                // of different pages, but not the same page.
                if (ctxt.isReload()) {
                    // This is to maintain the original protocol.
                    destroy();
                    
                    try {
                        servletClass = ctxt.load();
                        theServlet = (Servlet) servletClass.newInstance();
                    } catch( IllegalAccessException ex1 ) {
                        throw new JasperException( ex1 );
                    } catch( InstantiationException ex ) {
                        throw new JasperException( ex );
                    }
                    
                    theServlet.init(config);
                }
            }    
        }
        return theServlet;
    }

    public ServletContext getServletContext() {
        return config.getServletContext();
    }

    /**
     * Compile (if needed) and load a tag file
     */
    public Class loadTagFile() throws JasperException {

        try {
            if (ctxt.isRemoved()) {
                throw new FileNotFoundException(jspUri);
            }
            if (options.getDevelopment()) {
                synchronized (this) {
                    ctxt.compile();
                }
            }
            if (ctxt.isReload()) {
                tagHandlerClass = ctxt.load();
            }
        } catch (FileNotFoundException ex) {
            throw new JasperException(ex);
	}

	return tagHandlerClass;
    }

    /**
     * Compile and load a prototype for the Tag file.  This is needed
     * when compiling tag files with circular dependencies.  A prototpe
     * (skeleton) with no dependencies on other other tag files is
     * generated and compiled.
     */
    public Class loadTagFilePrototype() throws JasperException {

	ctxt.setPrototypeMode(true);
	try {
	    return loadTagFile();
	} finally {
	    ctxt.setPrototypeMode(false);
	}
    }

    /**
     * Get a list of files that the current page has source dependency on.
     */
    public java.util.List getDependants() {
	try {
	    Object target;
	    if (isTagFile) {
                if (ctxt.isReload()) {
                    tagHandlerClass = ctxt.load();
                }
		target = tagHandlerClass.newInstance();
	    } else {
		target = getServlet();
	    }
	    if (target != null && target instanceof JspSourceDependent) {
		return ((JspSourceDependent) target).getDependants();
	    }
	} catch (Throwable ex) {
	}
	return null;
    }

    public boolean isTagFile() {
	return this.isTagFile;
    }

    public int incTripCount() {
	return tripCount++;
    }

    public int decTripCount() {
	return tripCount--;
    }

    public void service(HttpServletRequest request, 
                        HttpServletResponse response,
                        boolean precompile)
	    throws ServletException, IOException, FileNotFoundException {
        try {

            if (ctxt.isRemoved()) {
                throw new FileNotFoundException(jspUri);
            }

            if ((available > 0L) && (available < Long.MAX_VALUE)) {
                response.setDateHeader("Retry-After", available);
                response.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     Constants.getString("jsp.error.unavailable"));
            }

            if (options.getDevelopment()) {
                synchronized (this) {
                    ctxt.compile();
                }
            }

            if (ctxt.isReload()) {
                getServlet();
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
        } catch (ServletException ex) {
	    throw ex;
        } catch (IOException ex) {
            throw ex;
        // Jikes won't compile this ( JasperException extends ServletException
        //} catch (JasperException ex) {
        //    throw ex;
        } catch (Exception ex) {
            throw new JasperException(ex);
        }
    }

    public void destroy() {
        if (theServlet != null) {
            theServlet.destroy();
        }
    }

}
