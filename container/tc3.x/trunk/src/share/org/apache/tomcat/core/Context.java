/*
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


package org.apache.tomcat.core;

import org.apache.tomcat.server.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.deployment.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 */

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//

public class Context {
    
    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private boolean initialized = false;
    private ContextManager server;
    private String description = null;
    private boolean isDistributable = false;
    private String engineHeader = null;
    private ClassLoader classLoader = null;
    private String classPath = ""; // classpath used by the classloader.
    //private Hashtable sessions = new Hashtable();
    // XXX XXX XXX hardcoded ! 
    private SessionManager sessionManager;
    private ServletContextFacade contextFacade;
    private Hashtable initializationParameters = new Hashtable();
    private Hashtable attributes = new Hashtable();
    private MimeMap mimeTypes = new MimeMap();
    private int sessionTimeOut = -1;
    private Vector welcomeFiles = new Vector();
    private Hashtable errorPages = new Hashtable();
    private Hashtable loadableServlets = new Hashtable();
    private URL docBase;
    private String path = "";
    //private String sessionCookieName;
    private boolean isInvokerEnabled = false;
    private File workDir =
        new File(System.getProperty("user.dir", ".") +
            System.getProperty("file.separator") + Constants.WorkDir);
    private boolean isWorkDirPersistent = false;
    private File warDir = null;
    private boolean isWARExpanded = false;
    private boolean isWARValidated = false;
    private Vector initInterceptors = new Vector();
    private Vector serviceInterceptors = new Vector();
    private Vector destroyInterceptors = new Vector();
    private RequestSecurityProvider rsProvider =
        DefaultRequestSecurityProvider.getInstance();

    // from Container
    private ServletClassLoader servletLoader;
    private Hashtable servlets = new Hashtable();
    private Hashtable prefixMappedServlets = new Hashtable();
    private Hashtable extensionMappedServlets = new Hashtable();
    private Hashtable pathMappedServlets = new Hashtable();
    private ServletWrapper defaultServlet = null;
    private URL servletBase = null;
    private Vector classPaths = new Vector();
    private Vector libPaths = new Vector();

    
    public Context() {
    }
	
    public Context(ContextManager server, String path) {
        this.server = server;
	this.path = path;
        contextFacade = new ServletContextFacade(server, this);
    }

    public String getEngineHeader() {
        if( engineHeader==null) {
	    /*
	     * Whoever modifies this needs to check this modification is
	     * ok with the code in com.jsp.runtime.ServletEngine or talk
	     * to akv before you check it in. 
	     */
	    // Default value for engine header
	    // no longer use core.properties - the configuration comes from
	    // server.xml or web.xml - no more properties.
	    StringBuffer sb=new StringBuffer();
	    sb.append(Constants.Context.EngineHeader);
	    sb.append( "; Java " );
	    sb.append(System.getProperty("java.version")).append("; ");
	    sb.append(System.getProperty("os.name") + " ");
	    sb.append(System.getProperty("os.version") + " ");
	    sb.append(System.getProperty("os.arch") + "; java.vendor=");
	    sb.append(System.getProperty("java.vendor")).append(")");
	    engineHeader=sb.toString();
	}
	return engineHeader;
    }

    public void setEngineHeader(String s) {
        engineHeader=s;
    }

    public ContextManager getContextManager() {
	return server;
    }
    
    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public boolean isInvokerEnabled() {
        return isInvokerEnabled;
    }

    public void setInvokerEnabled(boolean isInvokerEnabled) {
        this.isInvokerEnabled = isInvokerEnabled;
    }

    public void setRequestSecurityProvider(
	RequestSecurityProvider rsProvider) {
	this.rsProvider = rsProvider;
    }

    public RequestSecurityProvider getRequestSecurityProvider() {
	return this.rsProvider;
    }

    public File getWorkDir() {
        return this.workDir;
    }

    public void setWorkDir(String workDir, boolean isWorkDirPersistent) {
        File f = null;

        try {
	    f = new File(workDir);
	} catch (Throwable e) {
	}

	setWorkDir(f, isWorkDirPersistent);
    }

    public void setWorkDir(File workDir, boolean isWorkDirPersistent) {
        this.isWorkDirPersistent = isWorkDirPersistent;

	if (workDir == null) {
	    workDir = this.workDir;
	}

	if (! isWorkDirPersistent) {
	    clearDir(workDir);
        }

	this.workDir = workDir;

	setAttribute(Constants.Attribute.WorkDirectory, this.workDir);
    }

    public boolean isWorkDirPersistent() {
        return this.isWorkDirPersistent;
    }

    File getWARDir() {
        return this.warDir;
    }

    public boolean isWARExpanded() {
        return this.isWARExpanded;
    }

    public void setIsWARExpanded(boolean isWARExpanded) {
        this.isWARExpanded = isWARExpanded;
    }

    public boolean isWARValidated() {
        return this.isWARValidated;
    }

    public void setIsWARValidated(boolean isWARValidated) {
        this.isWARValidated = isWARValidated;
    }

    public ClassLoader getClassLoader() {
      return this.classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    public String getClassPath() {
        String cp = this.classPath.trim();
        String servletLoaderClassPath =
            this.getLoader().getClassPath();

        if (servletLoaderClassPath != null &&
            servletLoaderClassPath.trim().length() > 0) {
            cp += ((cp.length() > 0) ? File.pathSeparator : "") +
                servletLoaderClassPath;
        }

        return cp;
    }
    
    public void setClassPath(String classPath) {
        if (this.classPath.trim().length() > 0) {
	    this.classPath += File.pathSeparator;
	}

        this.classPath += classPath;
    }
    
    /**
     * Adds an interceptor for init() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> init()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */

    public void addInitInterceptor(LifecycleInterceptor interceptor) {
	initInterceptors.addElement(interceptor);
    }

    /**
     * Adds an interceptor for destroy() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> destroy()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */

    public void addDestroyInterceptor(LifecycleInterceptor interceptor) {
	destroyInterceptors.addElement(interceptor);
    }

    /**
     * Adds an interceptor for service() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> service()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */

    public void addServiceInterceptor(ServiceInterceptor interceptor) {
	serviceInterceptors.addElement(interceptor);
    }

    Vector getInitInterceptors() {
	return initInterceptors;
    }

    Vector getDestroyInterceptors() {
	return destroyInterceptors;
    }

    Vector getServiceInterceptors() {
	return serviceInterceptors;
    }
    
    /**
     * Initializes this context to take on requests. This action
     * will cause the context to load it's configuration information
     * from the webapp directory in the docbase.
     *
     * <p>This method may only be called once and must be called
     * before any requests are handled by this context.
     */
    
    public synchronized void init() {
	// check to see if we've already been init'd

	if (this.initialized) {
	    String msg = sm.getString("context.init.alreadyinit");

	    throw new IllegalStateException(msg);
	}

	this.initialized = true;
	
	if (this.docBase == null) {
	    //String msg = sm.getString("context.init.nodocbase");
	    //throw new IllegalStateException(msg);

	    // XXX
	    // for now we are going to pretend it doens't matter
	}

	// set up work dir attribute

	if (this.workDir != null) {
	    setAttribute(Constants.Context.Attribute.WorkDir.Name,
	        this.workDir);

	    if (! this.workDir.exists()) {
	        this.workDir.mkdirs();
	    }
	}

	// expand WAR

	URL servletBase = this.docBase;

	if (docBase.getProtocol().equalsIgnoreCase(
	    Constants.Request.WAR)) {
	    if (isWARExpanded()) {
	        this.warDir = new File(getWorkDir(),
		    Constants.Context.WARExpandDir);

		if (! this.warDir.exists()) {
		    this.warDir.mkdirs();

		    try {
		        WARUtil.expand(this.warDir, getDocumentBase());
		    } catch (MalformedURLException mue) {
		    } catch (IOException ioe) {
		    }

		    try {
                        servletBase = URLUtil.resolve(this.warDir.toString());
		    } catch (Exception e) {
		    }
		}
	    }
	}

        this.setServletBase(servletBase);

        for (int i = 0; i < Constants.Context.CLASS_PATHS.length; i++) {
            this.addClassPath(Constants.Context.CLASS_PATHS[i]);
	}

        for (int i = 0; i < Constants.Context.LIB_PATHS.length; i++) {
            this.addLibPath(Constants.Context.LIB_PATHS[i]);
	}

	// process base configuration

	try {
	    Class webApplicationDescriptor = Class.forName(
	        "org.apache.tomcat.deployment.WebApplicationDescriptor");
	    InputStream is =
	        webApplicationDescriptor.getResourceAsStream(
	            org.apache.tomcat.deployment.Constants.ConfigFile);
	    String msg = sm.getString("context.getConfig.msg", "default");

    	    System.out.println(msg);

	    processWebApp(is, true);
	} catch (Exception e) {
	    String msg = sm.getString("context.getConfig.e", "default");

	    System.out.println(msg);
	}

	// process webApp configuration

	String s = docBase.toString();

	if (docBase.getProtocol().equalsIgnoreCase(
	    Constants.Request.WAR)) {
	    if (s.endsWith("/")) {
	        s = s.substring(0, s.length() - 1);
	    }

	    s += "!/";
	}

	URL webURL = null;

	try {
	    webURL = new URL(s + Constants.Context.ConfigFile);

	    InputStream is = webURL.openConnection().getInputStream();
	    String msg = sm.getString("context.getConfig.msg",
	        webURL.toString());

	    System.out.println(msg);

	    processWebApp(is);
	} catch (Exception e) {
	    String msg = sm.getString("context.getConfig.e",
	        (webURL != null) ? webURL.toString() : "not available");

            // go silent on this one
	    // System.out.println(msg);
	}

	if (! this.isInvokerEnabled) {
	    // Put in a special "no invoker" that handles
	    // /servlet requests and explains why no servlet
	    // is being invoked

	    this.addServlet(Constants.Servlet.NoInvoker.Name,
	        Constants.Servlet.NoInvoker.Class);
	    this.addMapping(Constants.Servlet.NoInvoker.Name,
	        Constants.Servlet.NoInvoker.Map);
	}

	// load-on-startup

        if (! loadableServlets.isEmpty()) {
	    loadServlets();
        }
    }

    public SessionManager getSessionManager() {
	if( sessionManager==null ) {
	    // default - will change when a better one exists
	    sessionManager = org.apache.tomcat.session.ServerSessionManager.getManager();
	}
	return sessionManager;
    }

    public void setSessionManager( SessionManager manager ) {
	sessionManager= manager;
    }
    
    public void shutdown() {
	// shut down container
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper wrapper = (ServletWrapper)servlets.get(key);

	    servlets.remove(key);
	    wrapper.destroy();
	}
	// shut down any sessions

	getSessionManager().removeSessions(this);

	if (! isWorkDirPersistent) {
            clearDir(workDir);
	}

	System.out.println("Context: " + this + " down");
    }
    
    public Enumeration getWelcomeFiles() {
	return welcomeFiles.elements();
    }
    
    public String getInitParameter(String name) {
        return (String)initializationParameters.get(name);
    }

    public Enumeration getInitParameterNames() {
        return initializationParameters.keys();
    }

    public Object getAttribute(String name) {
        if (name.equals("org.apache.tomcat.jsp_classpath"))
	  return getClassPath();
	else if(name.equals("org.apache.tomcat.classloader")) {
	  return this.getLoader();
        }else {
            Object o = attributes.get(name);
            return attributes.get(name);
        }
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    public URL getDocumentBase() {
        return docBase;
    }

    public void setDocumentBase(URL docBase) {
	String file = docBase.getFile();

	if (! file.endsWith("/")) {
	    try {
		docBase = new URL(docBase.getProtocol(),
                    docBase.getHost(), docBase.getPort(), file + "/");
	    } catch (MalformedURLException mue) {
		System.out.println("SHOULD NEVER HAPPEN: " + mue);
	    }
	}

	this.docBase = docBase;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDistributable() {
        return this.isDistributable;
    }

    public void setDistributable(boolean isDistributable) {
        this.isDistributable = isDistributable;
    }


    public int getSessionTimeOut() {
        return this.sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }
    
    public MimeMap getMimeMap() {
        return mimeTypes;
    }

    public String getErrorPage(int errorCode) {
        return getErrorPage(String.valueOf(errorCode));
    }

    public String getErrorPage(String errorCode) {
        return (String)errorPages.get(errorCode);
    }

    ServletContextFacade getFacade() {
        return contextFacade;
    }

    private Properties getProperties(String propertyFileName) {
        Properties props = new Properties();

        try {
	    props.load(this.getClass().getResourceAsStream(propertyFileName));
	} catch (IOException ioe) {
	}

	return props;
    }

    private void clearDir(File dir) {
        String[] files = dir.list();

        if (files != null) {
	    for (int i = 0; i < files.length; i++) {
	        File f = new File(dir, files[i]);

	        if (f.isDirectory()) {
		    clearDir(f);
	        }

	        try {
	            f.delete();
	        } catch (Exception e) {
	        }
	    }

	    try {
	        dir.delete();
	    } catch (Exception e) {
	    }
        }
    }

    private void processWebApp(InputStream is) {
        processWebApp(is, false);
    }

    private void processWebApp(InputStream is, boolean internal) {
        if (is != null) {
	    try {
	        WebApplicationDescriptor webDescriptor =
		    (new WebApplicationReader()).getDescriptor(is,
		        new WebDescriptorFactoryImpl(),
			isWARValidated());

		processDescription(webDescriptor.getDescription());
		processDistributable(webDescriptor.isDistributable());
		processInitializationParameters(
		    webDescriptor.getContextParameters());
		processSessionTimeOut(webDescriptor.getSessionTimeout());
		processServlets(webDescriptor.getWebComponentDescriptors());
		processMimeMaps(webDescriptor.getMimeMappings());
		processWelcomeFiles(webDescriptor.getWelcomeFiles(),
                    internal);
		processErrorPages(webDescriptor.getErrorPageDescriptors());
	    } catch (Throwable e) {
                String msg = "config parse: " + e.getMessage();

                System.out.println(msg);
	    }
	}
    }

    private void processDescription(String description) {
        this.description = description;
    }

    private void processDistributable(boolean isDistributable) {
        this.isDistributable = isDistributable;
    }

    private void processInitializationParameters(
	Enumeration contextParameters) {
        while (contextParameters.hasMoreElements()) {
	    ContextParameter contextParameter =
	        (ContextParameter)contextParameters.nextElement();
	    initializationParameters.put(contextParameter.getName(),
	        contextParameter.getValue());
	}
    }

    private void processSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    private void processServlets(Enumeration servlets) {
        // XXX
        // oh my ... this has suddenly turned rather ugly
        // perhaps the reader should do this normalization work

        while (servlets.hasMoreElements()) {
	    WebComponentDescriptor webComponentDescriptor =
	        (WebComponentDescriptor)servlets.nextElement();
	    String name = webComponentDescriptor.getCanonicalName();
	    String description = webComponentDescriptor.getDescription();
	    String resourceName = null;
	    boolean removeResource = false;

	    if (webComponentDescriptor instanceof ServletDescriptor) {
		resourceName =
		    ((ServletDescriptor)webComponentDescriptor).getClassName();

		if (containsServletByName(name)) {
		    String msg = sm.getString("context.dd.dropServlet",
		        name + "(" + resourceName + ")" );

		    System.out.println(msg);
		    
		    removeResource = true;
		    removeServletByName(name);
		}

		addServlet(name, resourceName, description);
	    } else if (webComponentDescriptor instanceof JspDescriptor) {
		resourceName =
		    ((JspDescriptor)webComponentDescriptor).getJspFileName();

		if (! resourceName.startsWith("/")) {
		    resourceName = "/" + resourceName;
		}

		if (containsJSP(resourceName)) {
		    String msg = sm.getString("context.dd.dropServlet",
		        resourceName);

		    System.out.println(msg);

		    removeResource = true;
		    removeJSP(resourceName);
		}

		addJSP(name, resourceName, description);
	    }

	    if (removeResource) {
	        Enumeration enum = loadableServlets.keys();

		while (enum.hasMoreElements()) {
		    Integer key = (Integer)enum.nextElement();
		    Vector v = (Vector)loadableServlets.get(key);

		    Enumeration e = v.elements();
		    Vector buf = new Vector();

		    while (e.hasMoreElements()) {
		        String servletName = (String)e.nextElement();

			if (containsServletByName(servletName)) {
			    buf.addElement(servletName);
			}
		    }

		    loadableServlets.put(key, buf);
		}
	    }

	    int loadOnStartUp = webComponentDescriptor.getLoadOnStartUp();

            if (loadOnStartUp > Integer.MIN_VALUE) {
	        Integer key = new Integer(loadOnStartUp);
		Vector v = (Vector)((loadableServlets.containsKey(key)) ?
		    loadableServlets.get(key) : new Vector());

		v.addElement(name);
		loadableServlets.put(key, v);
	    }

	    Enumeration enum =
	        webComponentDescriptor.getInitializationParameters();
	    Hashtable initializationParameters = new Hashtable();

	    while (enum.hasMoreElements()) {
	        InitializationParameter initializationParameter =
		    (InitializationParameter)enum.nextElement();

		initializationParameters.put(
		    initializationParameter.getName(),
		    initializationParameter.getValue());
	    }

	    setServletInitParams(webComponentDescriptor.getCanonicalName(),
				 initializationParameters);

	    enum = webComponentDescriptor.getUrlPatterns();

	    while (enum.hasMoreElements()) {
	        String mapping = (String)enum.nextElement();

		if (! mapping.startsWith("*.") &&
		    ! mapping.startsWith("/")) {
		    mapping = "/" + mapping;
		}

		if (! containsServlet(mapping) &&
		    ! containsJSP(mapping)) {
		    if (containsMapping(mapping)) {
		        String msg = sm.getString("context.dd.dropMapping",
			    mapping);

			System.out.println(msg);

			removeMapping(mapping);
		    }

                    addMapping(name, mapping);
		} else {
		    String msg = sm.getString("context.dd.ignoreMapping",
		        mapping);

		    System.out.println(msg);
		}
	    }
	}
    }

    private void processMimeMaps(Enumeration mimeMaps) {
        while (mimeMaps.hasMoreElements()) {
	    MimeMapping mimeMapping = (MimeMapping)mimeMaps.nextElement();

	    this.mimeTypes.addContentType(
	        mimeMapping.getExtension(), mimeMapping.getMimeType());
	}
    }

    private void processWelcomeFiles(Enumeration welcomeFiles) {
        processWelcomeFiles(welcomeFiles, false);
    }

    private void processWelcomeFiles(Enumeration welcomeFiles,
        boolean internal) {
        if (! internal &&
            ! this.welcomeFiles.isEmpty() &&
            welcomeFiles.hasMoreElements()) {
            this.welcomeFiles.removeAllElements();
        }

	while (welcomeFiles.hasMoreElements()) {
	    this.welcomeFiles.addElement(welcomeFiles.nextElement());
	}
    }

    private void processErrorPages(Enumeration errorPages) {
        while (errorPages.hasMoreElements()) {
	    ErrorPageDescriptor errorPageDescriptor =
	        (ErrorPageDescriptor)errorPages.nextElement();
	    String key = null;

	    if (errorPageDescriptor.getErrorCode() > -1) {
	        key = String.valueOf(errorPageDescriptor.getErrorCode());
	    } else {
	        key = errorPageDescriptor.getExceptionType();
	    }

	    this.errorPages.put(key, errorPageDescriptor.getLocation());
	}
    }

    private void loadServlets() {
	Vector orderedKeys = new Vector();
	Enumeration e = loadableServlets.keys();
	
	// order keys

	while (e.hasMoreElements()) {
	    Integer key = (Integer)e.nextElement();
	    int slot = -1;

	    for (int i = 0; i < orderedKeys.size(); i++) {
	        if (key.intValue() <
		    ((Integer)(orderedKeys.elementAt(i))).intValue()) {
		    slot = i;

		    break;
		}
	    }

	    if (slot > -1) {
	        orderedKeys.insertElementAt(key, slot);
	    } else {
	        orderedKeys.addElement(key);
	    }
	}

	// loaded ordered servlets

	// Priorities IMO, should start with 0.
	// Only System Servlets should be at 0 and rest of the
	// servlets should be +ve integers.
	// WARNING: Please do not change this without talking to:
	// harishp@eng.sun.com (J2EE impact)

	for (int i = 0; i < orderedKeys.size(); i ++) {
	    Integer key = (Integer)orderedKeys.elementAt(i);
	    e = ((Vector)(loadableServlets.get(key))).elements();

	    while (e.hasMoreElements()) {
		String servletName = (String)e.nextElement();
		ServletWrapper  result = getServletByName(servletName);
		
		if(result==null)
		    System.out.println("Warning: we try to load an undefined servlet " + servletName);
		
		try {
		    if(result!=null)
			result.loadServlet();
		} catch (Exception ee) {
		    String msg = sm.getString("context.loadServlet.e",
		        servletName);

		    System.out.println(msg);
		} 
	    }
	}
    }


    // -------------------- From Container
    ServletClassLoader getLoader() {
	if(servletLoader == null) {
	    servletLoader = new ServletClassLoader(this);
	}

	return servletLoader;
    }

    public URL getServletBase() {
        return this.servletBase;
    }

    public void setServletBase(URL servletBase) {
        this.servletBase = servletBase;
    }

    public Enumeration getClassPaths() {
        return this.classPaths.elements();
    }

    public void addClassPath(String path) {
        this.classPaths.addElement(path);
    }

    public Enumeration getLibPaths() {
        return this.libPaths.elements();
    }

    public void addLibPath(String path) {
        this.libPaths.addElement(path);
    }

    /**
     * Add a servlet with the given name to the container. The
     * servlet will be loaded by the container's class loader
     * and instantiated using the given class name.
     */
    
    public void addServlet(String name, String className) {
        addServlet(name, null, className, null);
    }
 
    public void addServlet(String name, String className,
        String description) {
        addServlet(name, description, className, null);
    }

    public void addServlet(String name, Class clazz) {
        addServlet(name, null, null, clazz);
    }

    public void addServlet(String name, Class clazz,
	String description) {
        addServlet(name, description, null, clazz);
    }

    public void addJSP(String name, String path) {
        addJSP(name, null, path);
    }

    public void addJSP(String name, String path, String description) {
        // XXX
        // check for duplicates!

        ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletName(name);
	wrapper.setServletDescription(description);
	wrapper.setPath(path);

	servlets.put(name, wrapper);
    }

    /** True if we have a servlet with className.
     */
    boolean containsServlet(String className) {
        ServletWrapper[] sw = getServlets(className);

        return (sw != null &&
	    sw.length > 0);
    }

    /** Check if we have a servlet with the specified name
     */
    boolean containsServletByName(String name) {
	return (servlets.containsKey(name));
    }

    /** Remove all servlets with a specific class name
     */
    void removeServlet(String className) {
        removeServlets(getServlets(className));
    }

    /** Remove the servlet with a specific name
     */
    void removeServletByName(String servletName) {
	ServletWrapper wrapper=(ServletWrapper)servlets.get(servletName);
	if( wrapper != null ) {
	    removeServlet( wrapper );
	}
    }

    boolean containsJSP(String path) {
        ServletWrapper[] sw = getServletsByPath(path);

        return (sw != null &&
	    sw.length > 0);
    }

    void removeJSP(String path) {
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);

	    if (sw.getPath() != null &&
	        sw.getPath().equals(path)) {
	        removeServlet( sw );
	    }
	}
    }

    public void setServletInitParams(String name, Hashtable initParams) {
	ServletWrapper wrapper = (ServletWrapper)servlets.get(name);

	if (wrapper != null) {
	    wrapper.setInitArgs(initParams);
	}
    }
    
    /**
     * Maps a named servlet to a particular path or extension.
     * If the named servlet is unregistered, it will be added
     * and subsequently mapped.
     *
     * Note that the order of resolution to handle a request is:
     *
     *    exact mapped servlet (eg /catalog)
     *    prefix mapped servlets (eg /foo/bar/*)
     *    extension mapped servlets (eg *jsp)
     *    default servlet
     *
     */

    public void addMapping(String servletName, String path) {
        ServletWrapper sw = (ServletWrapper)servlets.get(servletName);

	if (sw == null) {
	    // XXX
	    // this might be a bit aggressive

	    if (! servletName.startsWith("/")) {
	        addServlet(servletName, null, servletName, null);
	    } else {
	        addJSP(servletName, servletName);
	    }

	    sw = (ServletWrapper)servlets.get(servletName);
	}

	path = path.trim();

	if (sw != null &&
	    (path.length() > 0)) {
	    if (path.startsWith("/") &&
                path.endsWith("/*")){
	        prefixMappedServlets.put(path, sw);
	    } else if (path.startsWith("*.")) {
	        extensionMappedServlets.put(path, sw);
	    } else if (! path.equals("/")) {
	        pathMappedServlets.put(path, sw);
	    } else {
	        defaultServlet = sw;
	    }
	}
    }

    public ServletWrapper getDefaultServlet() {
	return defaultServlet;
    }
    
    public Hashtable getPathMap() {
	return pathMappedServlets;
    }

    public Hashtable getPrefixMap() {
	return prefixMappedServlets;
    }

    public Hashtable getExtensionMap() {
	return extensionMappedServlets;
    }
    
    boolean containsMapping(String mapping) {
        mapping = mapping.trim();

        return (prefixMappedServlets.containsKey(mapping) ||
	    extensionMappedServlets.containsKey(mapping) ||
	    pathMappedServlets.containsKey(mapping));
    }

    void removeMapping(String mapping) {
        mapping = mapping.trim();

	prefixMappedServlets.remove(mapping);
	extensionMappedServlets.remove(mapping);
	pathMappedServlets.remove(mapping);
    }

    Request lookupServletByName(String servletName) {
        Request lookupResult = null;

	ServletWrapper wrapper = (ServletWrapper)servlets.get(servletName);

	if (wrapper != null) {
	    lookupResult = new Request();
	    lookupResult.setWrapper( wrapper );
	    lookupResult.setPathInfo("");
	}

        return lookupResult;
    }

    public ServletWrapper getServletByName(String servletName) {
	return (ServletWrapper)servlets.get(servletName);
    }

    ServletWrapper getServletAndLoadByName(String servletName) {
	// XXX
	// make sure that we aren't tramping over ourselves!
	ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletClass(servletName);

	servlets.put(servletName, wrapper);

	return wrapper;
    }

    ServletWrapper loadServlet(String servletClassName) {
        // XXX
        // check for duplicates!

        // XXX
        // maybe dispatch to addServlet?
        
        ServletWrapper wrapper = new ServletWrapper(this);

        wrapper.setServletClass(servletClassName);

        servlets.put(servletClassName, wrapper);

        return wrapper;
    }

    private void addServlet(String name, String description,
        String className, Class clazz) {
        // XXX
        // check for duplicates!

        if (servlets.get(name) != null) {
            removeServlet(name);
            removeServletByName(name);
        }

        ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletName(name);
	wrapper.setServletDescription(description);

	if (className != null) {
	    wrapper.setServletClass(className);
	}

	if (clazz != null) {
	    wrapper.setServletClass(clazz);
	}

	servlets.put(name, wrapper);
    }

    private void removeServlet(ServletWrapper sw) {
	if (prefixMappedServlets.contains(sw)) {
	    Enumeration enum = prefixMappedServlets.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();
		
		if (prefixMappedServlets.get(key).equals(sw)) {
		    prefixMappedServlets.remove(key);
		}
	    }
	}
	
	if (extensionMappedServlets.contains(sw)) {
	    Enumeration enum = extensionMappedServlets.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();

		if (extensionMappedServlets.get(key).equals(sw)) {
		    extensionMappedServlets.remove(key);
		}
	    }
	}
	
	if (pathMappedServlets.contains(sw)) {
	    Enumeration enum = pathMappedServlets.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();

		if (pathMappedServlets.get(key).equals(sw)) {
		    pathMappedServlets.remove(key);
		}
	    }
	}
	
	servlets.remove(sw.getServletName());
    }
    
    private void removeServlets(ServletWrapper[] sw) {
	if (sw != null) {
	    for (int i = 0; i < sw.length; i++) {
		removeServlet( sw[i] );
	    }
	}
    }

    /** Return servlets with a specified class name
     */
    private ServletWrapper[] getServlets(String name) {
        Vector servletWrappers = new Vector();
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);


            if (sw.getServletClass() != null &&
                sw.getServletClass().equals(name)) {
	        servletWrappers.addElement(sw);
	    }
	}

	ServletWrapper[] wrappers =
	    new ServletWrapper[servletWrappers.size()];

	servletWrappers.copyInto((ServletWrapper[])wrappers);

        return wrappers;
    }

    // XXX
    // made package protected so that RequestMapper can have access

    public ServletWrapper[] getServletsByPath(String path) {
        Vector servletWrappers = new Vector();
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);

	    if (sw.getPath() != null &&
	        sw.getPath().equals(path)) {
	        servletWrappers.addElement(sw);
	    }
	}

	ServletWrapper[] wrappers =
	    new ServletWrapper[servletWrappers.size()];

	servletWrappers.copyInto((ServletWrapper[])wrappers);

        return wrappers;
    }

    
}
