/*
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.lang.RuntimePermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.jasper.EmbededServletOptions;
import org.apache.jasper.runtime.*;

import org.apache.jasper.logging.Logger;
import org.apache.jasper.logging.DefaultLogger;
import org.apache.jasper.logging.JasperLogger;

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
public class JspServlet extends HttpServlet {

    private Logger.Helper loghelper;

    private ServletContext context = null;
    private Map jsps = new FastHashMap();
    private ServletConfig config;
    private Options options;
    private URLClassLoader parentClassLoader;
    private ServletEngine engine;
    private String serverInfo;
    private PermissionCollection permissionCollection;
    private CodeSource codeSource;
    private String classpath;

    static boolean firstTime = true;

    public void init(ServletConfig config)
	throws ServletException
    {
	super.init(config);
	this.config = config;
	this.context = config.getServletContext();
        this.serverInfo = context.getServerInfo();
        
	// Setup logging 
        //        Constants.jasperLog = new JasperLogger(this.context);
        Constants.jasperLog = new DefaultLogger(this.context);
	Constants.jasperLog.setName("JASPER_LOG");
	Constants.jasperLog.setTimestamp("false");
	Constants.jasperLog.setVerbosityLevel(
		   config.getInitParameter("logVerbosityLevel"));
	loghelper = new Logger.Helper("JASPER_LOG", "JspServlet");

	options = new EmbededServletOptions(config, context);

        ((FastHashMap)jsps).setFast(true);

        // Get the classpath to use for compiling
        classpath = (String) context.getAttribute(Constants.SERVLET_CLASSPATH);

        if (classpath == null || classpath.equals("")) {
            classpath = options.getClassPath();
        }

	// Get the parent class loader
	parentClassLoader =
	    (URLClassLoader) Thread.currentThread().getContextClassLoader();
        if (parentClassLoader == null)
            parentClassLoader = (URLClassLoader)this.getClass().getClassLoader();
        if (parentClassLoader != null) {
            Constants.message("jsp.message.parent_class_loader_is",
                              new Object[] {
                                  parentClassLoader.toString()
                              }, Logger.DEBUG);
            }
        else {
            Constants.message("jsp.message.parent_class_loader_is",
                              new Object[] {
                                  "<none>"  
                              }, Logger.DEBUG);
        }

	// Setup the PermissionCollection for this web app context
	// based on the permissions configured for the root of the
	// web app context directory, then add a file read permission
	// for that directory.
	Policy policy = Policy.getPolicy();
	if( policy != null ) {
            try {          
		// Get the permissions for the web app context
                String contextDir = context.getRealPath("/");
                if( contextDir == null )
                    contextDir = options.getScratchDir().toString();
		URL url = new URL("file:" + contextDir);
		codeSource = new CodeSource(url,null);
		permissionCollection = policy.getPermissions(codeSource);
		// Create a file read permission for web app context directory
		if (contextDir.endsWith(File.separator))
		    contextDir = contextDir + "-";
		else
		    contextDir = contextDir + File.separator + "-";
		permissionCollection.add( new FilePermission(contextDir,"read") );
		// Allow the JSP to access org.apache.jasper.runtime.HttpJspBase
		permissionCollection.add( new RuntimePermission(
		    "accessClassInPackage.org.apache.jasper.runtime") );
                if (parentClassLoader instanceof URLClassLoader) {
                    URL [] urls = parentClassLoader.getURLs();
                    String jarUrl = null;
                    String jndiUrl = null;
                    for (int i=0; i<urls.length; i++) {
                        if (jndiUrl == null && urls[i].toString().startsWith("jndi:") ) {
                            jndiUrl = urls[i].toString() + "-";
                        }
                        if (jarUrl == null && urls[i].toString().startsWith("jar:jndi:") ) {
                            jarUrl = urls[i].toString();
                            jarUrl = jarUrl.substring(0,jarUrl.length() - 2);
                            jarUrl = jarUrl.substring(0,jarUrl.lastIndexOf('/')) + "/-";
                        }
                    }
                    if (jarUrl != null) {
                        permissionCollection.add( new FilePermission(jarUrl,"read") );
                        permissionCollection.add( new FilePermission(jarUrl.substring(4),"read") );
                    }
                    if (jndiUrl != null)
                        permissionCollection.add( new FilePermission(jndiUrl,"read") );
                }
	    } catch(MalformedURLException mfe) {
	    }
	}

	if (firstTime) {
	    firstTime = false;
	    if( System.getSecurityManager() != null ) {
		// Make sure classes needed at runtime by a JSP servlet
		// are already loaded by the class loader so that we
		// don't get a defineClassInPackage security exception.
		String basePackage = "org.apache.jasper.";
		try {
		    parentClassLoader.loadClass( basePackage +
			"runtime.JspFactoryImpl$PrivilegedGetPageContext");
		    parentClassLoader.loadClass( basePackage +
			"runtime.JspFactoryImpl$PrivilegedReleasePageContext");
		    parentClassLoader.loadClass( basePackage +
			"runtime.JspRuntimeLibrary");
                    parentClassLoader.loadClass( basePackage +
                        "runtime.JspRuntimeLibrary$PrivilegedIntrospectHelper");
                    parentClassLoader.loadClass( basePackage +
			"runtime.ServletResponseWrapperInclude");
		    this.getClass().getClassLoader().loadClass( basePackage +
                        "servlet.JspServletWrapper");
		} catch (ClassNotFoundException ex) {
		    System.out.println(
			"Jasper JspServlet preload of class failed: " +
			ex.getMessage());
		}
	    }
	    Constants.message("jsp.message.scratch.dir.is", 
			      new Object[] { 
				  options.getScratchDir().toString() 
			      }, Logger.INFORMATION );
	    Constants.message("jsp.message.dont.modify.servlets", Logger.INFORMATION);
	    JspFactory.setDefaultFactory(new JspFactoryImpl());
	}
    }

    private void serviceJspFile(HttpServletRequest request, 
      				HttpServletResponse response, String jspUri, 
				Throwable exception, boolean precompile) 
	throws ServletException, IOException
    {

	JspServletWrapper wrapper;
	synchronized (this) {
	    wrapper = (JspServletWrapper) jsps.get(jspUri);
	    if (wrapper == null) {
                // First check if the requested JSP page exists, to avoid
                // creating unnecessary directories and files.
                if (context.getResourceAsStream(jspUri) == null)
                    throw new FileNotFoundException(jspUri);
                boolean isErrorPage = exception != null;
		wrapper = new JspServletWrapper(config, options, jspUri,
                                                isErrorPage, classpath,
                                                parentClassLoader,
                                                permissionCollection,
                                                codeSource);
		jsps.put(jspUri, wrapper);
	    }
	}
	
	wrapper.service(request, response, precompile);
    }


    /**
     * <p>Look for a <em>precompilation request</em> as described in
     * Section 8.4.2 of the JSP 1.2 Specification.  <strong>WARNING</strong> -
     * we cannot use <code>request.getParameter()</code> for this, because
     * that will trigger parsing all of the request parameters, and not give
     * a servlet the opportunity to call
     * <code>request.setCharacterEncoding()</code> first.</p>
     *
     * @param request The servlet requset we are processing
     *
     * @exception ServletException if an invalid parameter value for the
     *  <code>jsp_precompile</code> parameter name is specified
     */
    boolean preCompile(HttpServletRequest request) 
        throws ServletException 
    {

        String queryString = request.getQueryString();
        if (queryString == null)
            return (false);
        int start = queryString.indexOf(Constants.PRECOMPILE);
        if (start < 0)
            return (false);
        queryString =
            queryString.substring(start + Constants.PRECOMPILE.length());
        if (queryString.length() == 0)
            return (true);             // ?jsp_precompile
        if (queryString.startsWith("&"))
            return (true);             // ?jsp_precompile&foo=bar...
        if (!queryString.startsWith("="))
            return (false);            // part of some other name or value
        int limit = queryString.length();
        int ampersand = queryString.indexOf("&");
        if (ampersand > 0)
            limit = ampersand;
        String value = queryString.substring(1, limit);
        if (value.equals("true"))
            return (true);             // ?jsp_precompile=true
        else if (value.equals("false"))
            return (true);             // ?jsp_precompile=false
        else
            throw new ServletException("Cannot have request parameter " +
                                       Constants.PRECOMPILE + " set to " +
                                       value);

    }
    
    

    public void service (HttpServletRequest request, 
    			 HttpServletResponse response)
	throws ServletException, IOException
    {
	try {
            String includeUri 
                = (String) request.getAttribute(Constants.INC_SERVLET_PATH);

            String jspUri;

            if (includeUri == null)
		jspUri = request.getServletPath();
            else
                jspUri = includeUri;
            String jspFile = (String) request.getAttribute(Constants.JSP_FILE);
            if (jspFile != null)
                jspUri = jspFile;

            boolean precompile = preCompile(request);

	    Logger jasperLog = Constants.jasperLog;
	    
            if (jasperLog != null &&
		jasperLog.matchVerbosityLevel(Logger.INFORMATION))
		{
		    jasperLog.log("JspEngine --> "+jspUri);
		    jasperLog.log("\t     ServletPath: "+request.getServletPath());
		    jasperLog.log("\t        PathInfo: "+request.getPathInfo());
		    jasperLog.log("\t        RealPath: "
				  +getServletConfig().getServletContext().getRealPath(jspUri));
		    jasperLog.log("\t      RequestURI: "+request.getRequestURI());
		    jasperLog.log("\t     QueryString: "+request.getQueryString());
		    jasperLog.log("\t  Request Params: ");
		    Enumeration e = request.getParameterNames();
		    while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			jasperLog.log("\t\t "+name+" = "+request.getParameter(name));
		    }
		}
            serviceJspFile(request, response, jspUri, null, precompile);
	} catch (RuntimeException e) {
	    throw e;
	} catch (ServletException e) {
	    throw e;
	} catch (IOException e) {
	    throw e;
	} catch (Throwable e) {
	    throw new ServletException(e);
	}

    }

    public void destroy() {
	if (Constants.jasperLog != null)
	    Constants.jasperLog.log("JspServlet.destroy()", Logger.INFORMATION);

	Iterator servlets = jsps.values().iterator();
	while (servlets.hasNext()) 
	    ((JspServletWrapper) servlets.next()).destroy();
    }

}
