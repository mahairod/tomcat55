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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;


/**
 * Standard implementation of the <b>Context</b> interface.  Each
 * child container must be a Wrapper implementation to process the
 * requests directed to a particular servlet.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class StandardContext
    extends ContainerBase
    implements Context {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardContext component with the default basic Valve.
     */
    public StandardContext() {

	super();
	setBasic(new StandardContextValve());

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The security constraints for this web application.
     */
    private SecurityConstraint constraints[] = new SecurityConstraint[0];


    /**
     * The ServletContext implementation associated with this Context.
     */
    private ApplicationContext context = null;


    /**
     * Should we attempt to use cookies for session id communication?
     */
    private boolean cookies = true;


    /**
     * The distributable flag for this web application.
     */
    private boolean distributable = false;


    /**
     * The document root for this web application.
     */
    private String docBase = null;


    /**
     * The EJB resource references for this web application, keyed by name.
     */
    private HashMap ejbs = new HashMap();


    /**
     * The environment entries for this web application, keyed by name.
     */
    private HashMap envs = new HashMap();


    /**
     * The exception pages for this web application, keyed by fully qualified
     * class name of the Java exception.
     */
    private HashMap exceptionPages = new HashMap();


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
	"org.apache.catalina.core.StandardContext/1.0";


    /**
     * The set of classnames of InstanceListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String instanceListeners[] = new String[0];


    /**
     * The login configuration descriptor for this web application.
     */
    private LoginConfig loginConfig = null;


    /**
     * The Java class name of the default Mapper class for this Container.
     */
    private String mapperClass =
	"org.apache.catalina.core.StandardContextMapper";


    /**
     * The MIME mappings for this web application, keyed by extension.
     */
    private HashMap mimeMappings = new HashMap();


    /**
     * The context initialization parameters for this web application,
     * keyed by name.
     */
    private HashMap parameters = new HashMap();


    /**
     * The request processing pause flag (while reloading occurs)
     */
    private boolean paused = false;


    /**
     * The reloadable flag for this web application.
     */
    private boolean reloadable = false;


    /**
     * The resource references for this web application, keyed by name.
     */
    private HashMap resources = new HashMap();


    /**
     * The security role mappings for this application, keyed by role
     * name (as used within the application).
     */
    private HashMap roleMappings = new HashMap();


    /**
     * The security roles for this application, keyed by role name.
     */
    private String securityRoles[] = new String[0];


    /**
     * The servlet mappings for this web application, keyed by
     * matching pattern.
     */
    private HashMap servletMappings = new HashMap();


    /**
     * The session timeout (in minutes) for this web application.
     */
    private int sessionTimeout = 30;


    /**
     * The status code error pages for this web application, keyed by
     * HTTP status code (as an Integer).
     */
    private HashMap statusPages = new HashMap();


    /**
     * The JSP tag libraries for this web application, keyed by URI
     */
    private HashMap taglibs = new HashMap();


    /**
     * The welcome files for this application.
     */
    private String welcomeFiles[] = new String[0];


    /**
     * The set of classnames of LifecycleListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String wrapperLifecycles[] = new String[0];


    /**
     * The set of classnames of ContainerListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String wrapperListeners[] = new String[0];


    /**
     * The pathname to the work directory for this context (relative to
     * the server's home if not absolute).
     */
    private String workDir = null;


    /**
     * Java class name of the Wrapper class implementation we use.
     */
    private String wrapperClass = "org.apache.catalina.core.StandardWrapper";


    // ------------------------------------------------------------- Properties


    /**
     * Return the "use cookies for session ids" flag.
     */
    public boolean getCookies() {

	return (this.cookies);

    }


    /**
     * Set the "use cookies for session ids" flag.
     *
     * @param cookies The new flag
     */
    public void setCookies(boolean cookies) {

	boolean oldCookies = this.cookies;
	this.cookies = cookies;
	support.firePropertyChange("cookies",
				   new Boolean(oldCookies),
				   new Boolean(this.cookies));

    }


    /**
     * Return the distributable flag for this web application.
     */
    public boolean getDistributable() {

	return (this.distributable);

    }


    /**
     * Set the distributable flag for this web application.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {

	boolean oldDistributable = this.distributable;
	this.distributable = distributable;
	support.firePropertyChange("distributable",
				   new Boolean(oldDistributable),
				   new Boolean(this.distributable));

    }


    /**
     * Return the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getDocBase() {

	return (this.docBase);

    }


    /**
     * Set the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param docBase The new document root
     */
    public void setDocBase(String docBase) {

	this.docBase = docBase;

    }


    /**
     * Return the login configuration descriptor for this web application.
     */
    public LoginConfig getLoginConfig() {

	return (this.loginConfig);

    }


    /**
     * Set the login configuration descriptor for this web application.
     *
     * @param config The new login configuration
     */
    public void setLoginConfig(LoginConfig config) {

	LoginConfig oldLoginConfig = this.loginConfig;
	this.loginConfig = config;
	support.firePropertyChange("loginConfig",
				   oldLoginConfig, this.loginConfig);

    }


    /**
     * Set the login configuration descriptor for this web application.
     *
     * @param authMethod Authentication method to use (if any)
     * @param realmName Realm name to use in security challenges
     * @param loginPage Context-relative URI of the login page (if any)
     * @param errorPage Context-relative URI of the error page (if any)
     */
    public void setLoginConfig(String authMethod, String realmName,
			       String loginPage, String errorPage) {

        LoginConfig newLoginConfig =
	  new LoginConfig(authMethod, realmName, loginPage, errorPage);
	setLoginConfig(newLoginConfig);

    }


    /**
     * Return the context path for this Context.
     */
    public String getPath() {

	return (getName());

    }


    /**
     * Set the context path for this Context.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The context path is used as the "name" of
     * a Context, because it must be unique.
     *
     * @param path The new context path
     */
    public void setPath(String path) {

	setName(path);

    }


    /**
     * Return the reloadable flag for this web application.
     */
    public boolean getReloadable() {

	return (this.reloadable);

    }


    /**
     * Set the reloadable flag for this web application.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable) {

	boolean oldReloadable = this.reloadable;
	this.reloadable = reloadable;
	support.firePropertyChange("reloadable",
				   new Boolean(oldReloadable),
				   new Boolean(this.reloadable));

    }


    /**
     * Return the servlet context for which this Context is a facade.
     */
    public synchronized ServletContext getServletContext() {

	if (context == null)
	    context = new ApplicationContext(this);
	return (context);

    }


    /**
     * Return the default session timeout (in minutes) for this
     * web application.
     */
    public int getSessionTimeout() {

	return (this.sessionTimeout);

    }


    /**
     * Set the default session timeout (in minutes) for this
     * web application.
     *
     * @param timeout The new default session timeout
     */
    public void setSessionTimeout(int timeout) {

	int oldSessionTimeout = this.sessionTimeout;
	this.sessionTimeout = timeout;
	support.firePropertyChange("sessionTimeout",
				   new Integer(oldSessionTimeout),
				   new Integer(this.sessionTimeout));

    }


    /**
     * Return the work directory for this Context.
     */
    public String getWorkDir() {

	return (this.workDir);

    }


    /**
     * Set the work directory for this Context.
     *
     * @param workDir The new work directory
     */
    public void setWorkDir(String workDir) {

	this.workDir = workDir;

	if (started)
	    setWorkDirectory();

    }


    /**
     * Return the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     */
    public String getWrapperClass() {

	return (this.wrapperClass);

    }


    /**
     * Set the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     *
     * @param wrapperClass The new wrapper class
     */
    public void setWrapperClass(String wrapperClass) {

	this.wrapperClass = wrapperClass;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Wrapper.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

	if (!(child instanceof Wrapper))
	    throw new IllegalArgumentException
		(sm.getString("standardContext.notWrapper"));
	super.addChild(child);

    }


    /**
     * Add a security constraint to the set for this web application.
     */
    public void addConstraint(SecurityConstraint constraint) {

	synchronized (constraints) {
	    SecurityConstraint results[] =
		new SecurityConstraint[constraints.length + 1];
	    for (int i = 0; i < constraints.length; i++)
		results[i] = constraints[i];
	    results[constraints.length] = constraint;
	    constraints = results;
	}

    }



    /**
     * Add an EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addEjb(ContextEjb ejb) {

	synchronized (ejbs) {
	    ejbs.put(ejb.getName(), ejb);
	}
	fireContainerEvent("addEjb", ejb.getName());

    }


    /**
     * Add a new EJB resource reference for this web application.
     *
     * @param name EJB resource reference name
     * @param description EJB resource reference description
     * @param type Java class name of the EJB bean implementation class
     * @param home Java class name of the EJB home implementation class
     * @param remote Java class name of the EJB remote implementation class
     * @param link Optional link to a J2EE EJB definition
     */
    public void addEjb(String name, String description,
		       String type, String home,
		       String remote, String link) {

	addEjb(new ContextEjb(name, description, type, home, remote, link));

    }


    /**
     * Add an environment entry for this web application.
     *
     * @param environment New environment entry
     */
    public void addEnvironment(ContextEnvironment environment) {

	synchronized (envs) {
	    envs.put(environment.getName(), environment);
	}
	fireContainerEvent("addEnvironment", environment.getName());

    }


    /**
     * Add an environment entry for this web application.
     *
     * @param name Name of the environment entry
     * @param description Description of the environment entry
     * @param type Java class of the environment entry
     * @param value Value of the environment entry (as a String)
     */
    public void addEnvironment(String name, String description,
			       String type, String value) {

	addEnvironment(new ContextEnvironment(name, description, type, value));

    }


    /**
     * Add an error page for the specified error or Java exception.
     *
     * @param errorPage The error page definition to be added
     */
    public void addErrorPage(ErrorPage errorPage) {

	String exceptionType = errorPage.getExceptionType();
	if (exceptionType != null) {
	    synchronized (exceptionPages) {
		exceptionPages.put(exceptionType, errorPage);
	    }
	} else {
	    synchronized (statusPages) {
		statusPages.put(new Integer(errorPage.getErrorCode()),
				errorPage);
	    }
	}
	fireContainerEvent("addErrorPage", errorPage);

    }


    /**
     * Add the classname of an InstanceListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of an InstanceListener class
     */
    public void addInstanceListener(String listener) {

	synchronized (instanceListeners) {
	    String results[] =new String[instanceListeners.length + 1];
	    for (int i = 0; i < instanceListeners.length; i++)
		results[i] = instanceListeners[i];
	    results[instanceListeners.length] = listener;
	    instanceListeners = results;
	}
	fireContainerEvent("addInstanceListener", listener);

    }


    /**
     * Add a new MIME mapping, replacing any existing mapping for
     * the specified extension.
     *
     * @param extension Filename extension being mapped
     * @param mimeType Corresponding MIME type
     */
    public void addMimeMapping(String extension, String mimeType) {

	synchronized (mimeMappings) {
	    mimeMappings.put(extension, mimeType);
	}
	fireContainerEvent("addMimeMapping", extension);

    }


    /**
     * Add a new context initialization parameter, replacing any existing
     * value for the specified name.
     *
     * @param name Name of the new parameter
     * @param value Value of the new  parameter
     */
    public void addParameter(String name, String value) {

	synchronized (parameters) {
	    parameters.put(name, value);
	}
	fireContainerEvent("addParameter", name);

    }


    /**
     * Add a resource reference for this web application.
     *
     * @param resource New resource reference
     */
    public void addResource(ContextResource resource) {

	synchronized (resources) {
	    resources.put(resource.getName(), resource);
	}
	fireContainerEvent("addResource", resource.getName());

    }


    /**
     * Add a resource reference for this web application.
     *
     * @param name Name of this resource reference
     * @param description Description of this resource reference
     * @param type Java class of this resource reference
     * @param auth Authentication technique used by this resource reference
     */
    public void addResource(String name, String description,
			    String type, String auth) {

	addResource(new ContextResource(name, description, type, auth));

    }


    /**
     * Add a security role reference for this web application.
     *
     * @param role Security role used in the application
     * @param link Actual security role to check for
     */
    public void addRoleMapping(String role, String link) {

	synchronized (roleMappings) {
	    roleMappings.put(role, link);
	}
	fireContainerEvent("addRoleMapping", role);

    }


    /**
     * Add a new security role for this web application.
     *
     * @param role New security role
     */
    public void addSecurityRole(String role) {

	synchronized (securityRoles) {
	    String results[] =new String[securityRoles.length + 1];
	    for (int i = 0; i < securityRoles.length; i++)
		results[i] = securityRoles[i];
	    results[securityRoles.length] = name;
	    securityRoles = results;
	}
	fireContainerEvent("addSecurityRole", role);

    }


    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     */
    public void addServletMapping(String pattern, String name) {

	synchronized (servletMappings) {
	    servletMappings.put(pattern, name);
	}
	fireContainerEvent("addServletMapping", pattern);

    }


    /**
     * Add a JSP tag library for the specified URI.
     *
     * @param uri URI, relative to the web.xml file, of this tag library
     * @param location Location of the tag library descriptor
     */
    public void addTaglib(String uri, String location) {

	synchronized (taglibs) {
	    taglibs.put(uri, location);
	}
	fireContainerEvent("addTaglib", uri);

    }


    /**
     * Add a new welcome file to the set recognized by this Context.
     *
     * @param name New welcome file name
     */
    public void addWelcomeFile(String name) {

	synchronized (welcomeFiles) {
	    String results[] =new String[welcomeFiles.length + 1];
	    for (int i = 0; i < welcomeFiles.length; i++)
		results[i] = welcomeFiles[i];
	    results[welcomeFiles.length] = name;
	    welcomeFiles = results;
	}
	postWelcomeFiles();
	fireContainerEvent("addWelcomeFile", name);

    }


    /**
     * Add the classname of a LifecycleListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a LifecycleListener class
     */
    public void addWrapperLifecycle(String listener) {

	synchronized (wrapperLifecycles) {
	    String results[] =new String[wrapperLifecycles.length + 1];
	    for (int i = 0; i < wrapperLifecycles.length; i++)
		results[i] = wrapperLifecycles[i];
	    results[wrapperLifecycles.length] = listener;
	    wrapperLifecycles = results;
	}
	fireContainerEvent("addWrapperLifecycle", listener);

    }


    /**
     * Add the classname of a ContainerListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a ContainerListener class
     */
    public void addWrapperListener(String listener) {

	synchronized (wrapperListeners) {
	    String results[] =new String[wrapperListeners.length + 1];
	    for (int i = 0; i < wrapperListeners.length; i++)
		results[i] = wrapperListeners[i];
	    results[wrapperListeners.length] = listener;
	    wrapperListeners = results;
	}
	fireContainerEvent("addWrapperListener", listener);

    }


    /**
     * Factory method to create and return a new Wrapper instance, of
     * the Java implementation class appropriate for this Context
     * implementation.  The constructor of the instantiated Wrapper
     * will have been called, but no properties will have been set.
     */
    public Wrapper createWrapper() {

        Wrapper wrapper = new StandardWrapper();

	synchronized (instanceListeners) {
	    for (int i = 0; i < instanceListeners.length; i++) {
	        try {
	            Class clazz = Class.forName(instanceListeners[i]);
		    InstanceListener listener =
		      (InstanceListener) clazz.newInstance();
		    wrapper.addInstanceListener(listener);
		} catch (Throwable t) {
		    log("createWrapper", t);
		    return (null);
		}
	    }
	}

	synchronized (wrapperLifecycles) {
	    for (int i = 0; i < wrapperLifecycles.length; i++) {
	        try {
	            Class clazz = Class.forName(wrapperLifecycles[i]);
		    LifecycleListener listener =
		      (LifecycleListener) clazz.newInstance();
		    if (wrapper instanceof Lifecycle)
		        ((Lifecycle) wrapper).addLifecycleListener(listener);
		} catch (Throwable t) {
		    log("createWrapper", t);
		    return (null);
		}
	    }
	}

	synchronized (wrapperListeners) {
	    for (int i = 0; i < wrapperListeners.length; i++) {
	        try {
	            Class clazz = Class.forName(wrapperListeners[i]);
		    ContainerListener listener =
		      (ContainerListener) clazz.newInstance();
		    wrapper.addContainerListener(listener);
		} catch (Throwable t) {
		    log("createWrapper", t);
		    return (null);
		}
	    }
	}

	return (wrapper);

    }


    /**
     * Return the security constraints for this web application.
     * If there are none, a zero-length array is returned.
     */
    public SecurityConstraint[] findConstraints() {

	return (constraints);

    }


    /**
     * Return the EJB resource reference with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired EJB resource reference
     */
    public ContextEjb findEjb(String name) {

	synchronized (ejbs) {
	    return ((ContextEjb) ejbs.get(name));
	}

    }


    /**
     * Return the defined EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextEjb[] findEjbs() {

	synchronized (ejbs) {
	    ContextEjb results[] = new ContextEjb[ejbs.size()];
	    return ((ContextEjb[]) ejbs.values().toArray(results));
	}

    }


    /**
     * Return the environment entry with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired environment entry
     */
    public ContextEnvironment findEnvironment(String name) {

	synchronized (envs) {
	    return ((ContextEnvironment) envs.get(name));
	}

    }


    /**
     * Return the set of defined environment entries for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public ContextEnvironment[] findEnvironments() {

	synchronized (envs) {
	    ContextEnvironment results[] = new ContextEnvironment[envs.size()];
	    return ((ContextEnvironment[]) envs.values().toArray(results));
	}

    }


    /**
     * Return the error page entry for the specified HTTP error code,
     * if any; otherwise return <code>null</code>.
     *
     * @param errorCode Error code to look up
     */
    public ErrorPage findErrorPage(int errorCode) {

	return ((ErrorPage) statusPages.get(new Integer(errorCode)));

    }


    /**
     * Return the error page entry for the specified Java exception type,
     * if any; otherwise return <code>null</code>.
     *
     * @param exceptionType Exception type to look up
     */
    public ErrorPage findErrorPage(String exceptionType) {

	synchronized (exceptionPages) {
	    return ((ErrorPage) exceptionPages.get(exceptionType));
	}

    }


    /**
     * Return the set of defined error pages for all specified error codes
     * and exception types.
     */
    public ErrorPage[] findErrorPages() {

	synchronized(exceptionPages) {
	    synchronized(statusPages) {
		ErrorPage results1[] = new ErrorPage[exceptionPages.size()];
		results1 =
		    (ErrorPage[]) exceptionPages.values().toArray(results1);
		ErrorPage results2[] = new ErrorPage[statusPages.size()];
		results2 =
		    (ErrorPage[]) statusPages.values().toArray(results2);
		ErrorPage results[] =
		    new ErrorPage[results1.length + results2.length];
		for (int i = 0; i < results1.length; i++)
		    results[i] = results1[i];
		for (int i = results1.length; i < results.length; i++)
		    results[i] = results2[i - results1.length];
		return (results);
	    }
	}

    }


    /**
     * Return the set of InstanceListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findInstanceListeners() {

        return (instanceListeners);

    }


    /**
     * Return the MIME type to which the specified extension is mapped,
     * if any; otherwise return <code>null</code>.
     *
     * @param extension Extension to map to a MIME type
     */
    public String findMimeMapping(String extension) {

	synchronized (mimeMappings) {
	    return ((String) mimeMappings.get(extension));
	}

    }


    /**
     * Return the extensions for which MIME mappings are defined.  If there
     * are none, a zero-length array is returned.
     */
    public String[] findMimeMappings() {

	synchronized (mimeMappings) {
	    String results[] = new String[mimeMappings.size()];
	    return
		((String[]) mimeMappings.keySet().toArray(results));
	}

    }


    /**
     * Return the value for the specified context initialization
     * parameter name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the parameter to return
     */
    public String findParameter(String name) {

	synchronized (parameters) {
	    return ((String) parameters.get(name));
	}

    }


    /**
     * Return the names of all defined context initialization parameters
     * for this Context.  If no parameters are defined, a zero-length
     * array is returned.
     */
    public String[] findParameters() {

	synchronized (parameters) {
	    String results[] = new String[parameters.size()];
	    return ((String[]) parameters.keySet().toArray(results));
	}

    }


    /**
     * Return the resource reference with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource reference
     */
    public ContextResource findResource(String name) {

	synchronized (resources) {
	    return ((ContextResource) resources.get(name));
	}

    }


    /**
     * Return the defined resource references for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResource[] findResources() {

	synchronized (resources) {
	    ContextResource results[] = new ContextResource[resources.size()];
	    return ((ContextResource[]) resources.values().toArray(results));
	}

    }


    /**
     * For the given security role (as used by an application), return the
     * corresponding role name (as defined by the underlying Realm) if there
     * is one.  Otherwise, return the specified role unchanged.
     *
     * @param role Security role to map
     */
    public String findRoleMapping(String role) {

	String realRole = null;
	synchronized (roleMappings) {
	    realRole = (String) roleMappings.get(role);
	}
	if (realRole != null)
	    return (realRole);
	else
	    return (role);

    }


    /**
     * Return <code>true</code> if the specified security role is defined
     * for this application; otherwise return <code>false</code>.
     *
     * @param role Security role to verify
     */
    public boolean findSecurityRole(String role) {

	synchronized (securityRoles) {
	    for (int i = 0; i < securityRoles.length; i++) {
		if (role.equals(securityRoles[i]))
		    return (true);
	    }
	}
	return (false);

    }


    /**
     * Return the security roles defined for this application.  If none
     * have been defined, a zero-length array is returned.
     */
    public String[] findSecurityRoles() {

	return (securityRoles);

    }


    /**
     * Return the servlet name mapped by the specified pattern (if any);
     * otherwise return <code>null</code>.
     *
     * @param pattern Pattern for which a mapping is requested
     */
    public String findServletMapping(String pattern) {

	synchronized (servletMappings) {
	    return ((String) servletMappings.get(pattern));
	}

    }


    /**
     * Return the patterns of all defined servlet mappings for this
     * Context.  If no mappings are defined, a zero-length array is returned.
     */
    public String[] findServletMappings() {

	synchronized (servletMappings) {
	    String results[] = new String[servletMappings.size()];
	    return
	       ((String[]) servletMappings.keySet().toArray(results));
	}

    }


    /**
     * Return the context-relative URI of the error page for the specified
     * HTTP status code, if any; otherwise return <code>null</code>.
     *
     * @param status HTTP status code to look up
     */
    public String findStatusPage(int status) {

	return ((String) statusPages.get(new Integer(status)));

    }


    /**
     * Return the set of HTTP status codes for which error pages have
     * been specified.  If none are specified, a zero-length array
     * is returned.
     */
    public int[] findStatusPages() {

	synchronized (statusPages) {
	    int results[] = new int[statusPages.size()];
	    Iterator elements = statusPages.keySet().iterator();
	    int i = 0;
	    while (elements.hasNext())
		results[i++] = ((Integer) elements.next()).intValue();
	    return (results);
	}

    }


    /**
     * Return the tag library descriptor location for the specified taglib
     * URI, if any; otherwise, return <code>null</code>.
     *
     * @param uri URI, relative to the web.xml file
     */
    public String findTaglib(String uri) {

	synchronized (taglibs) {
	    return ((String) taglibs.get(uri));
	}

    }


    /**
     * Return the URIs of all tag libraries for which a tag library
     * descriptor location has been specified.  If none are specified,
     * a zero-length array is returned.
     */
    public String[] findTaglibs() {

	synchronized (taglibs) {
	    String results[] = new String[taglibs.size()];
	    return ((String[]) taglibs.keySet().toArray(results));
	}

    }


    /**
     * Return <code>true</code> if the specified welcome file is defined
     * for this Context; otherwise return <code>false</code>.
     *
     * @param name Welcome file to verify
     */
    public boolean findWelcomeFile(String name) {

	synchronized (welcomeFiles) {
	    for (int i = 0; i < welcomeFiles.length; i++) {
		if (name.equals(welcomeFiles[i]))
		    return (true);
	    }
	}
	return (false);

    }


    /**
     * Return the set of welcome files defined for this Context.  If none are
     * defined, a zero-length array is returned.
     */
    public String[] findWelcomeFiles() {

	return (welcomeFiles);

    }


    /**
     * Return the set of LifecycleListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperLifecycles() {

        return (wrapperLifecycles);

    }


    /**
     * Return the set of ContainerListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperListeners() {

        return (wrapperListeners);

    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Process the specified Request, and generate the corresponding Response,
     * according to the design of this particular Container.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred while
     *  processing
     * @exception ServletException if a ServletException was thrown
     *  while processing this request
     */
    public void invoke(Request request, Response response)
	throws IOException, ServletException {

	// Wait if we are reloading
	while (getPaused()) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		;
	    }
	}

	// Normal request processing
	super.invoke(request, response);

    }


    /**
     * Post a copy of our current list of welcome files as a servlet context
     * attribute, so that the default servlet can find them.
     */
    private void postWelcomeFiles() {

	getServletContext().setAttribute("org.apache.catalina.WELCOME_FILES",
					 welcomeFiles);

    }


    /**
     * Reload this web application, if reloading is supported.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  This method is designed to deal with
     * reloads required by changes to classes in the underlying repositories
     * of our class loader.  It does not handle changes to the web application
     * deployment descriptor.  If that has occurred, you should stop this
     * Context and create (and start) a new Context instance instead.
     * <p>
     * <b>FIXME</b>:  What about context attributes that have been created
     * by servlets?  ClassCastException?
     *
     * @exception IllegalStateException if the <code>reloadable</code>
     *  property is set to <code>false</code>.
     */
    public void reload() {

	// Make sure reloading is enabled
        //	if (!reloadable)
        //	    throw new IllegalStateException
        //		(sm.getString("standardContext.notReloadable"));
	log(sm.getString("standardContext.reloadingStarted"));

	// Stop accepting requests temporarily
	setPaused(true);

	// Shut down the current version of the relevant components
	Container children[] = findChildren();

	for (int i = 0; i < children.length; i++) {
	    Wrapper wrapper = (Wrapper) children[i];
	    if (wrapper instanceof Lifecycle) {
		try {
		    ((Lifecycle) wrapper).stop();
		} catch (LifecycleException e) {
		    log(sm.getString("standardContext.stoppingWrapper",
				     wrapper.getName()),
			e);
		}
	    }
	}

	if ((manager != null) && (manager instanceof Lifecycle)) {
	    try {
		((Lifecycle) manager).stop();
	    } catch (LifecycleException e) {
		log(sm.getString("standardContext.stoppingManager"), e);
	    }
	}

	if ((loader != null) && (loader instanceof Lifecycle)) {
	    try {
		((Lifecycle) loader).stop();
	    } catch (LifecycleException e) {
		log(sm.getString("standardContext.stoppingLoader"), e);
	    }
	}

	// Start up the new version of the relevant components
	if ((loader != null) && (loader instanceof Lifecycle)) {
	    try {
		;	// FIXME - check for new WEB-INF/lib/*.jar files?
		((Lifecycle) loader).start();
	    } catch (LifecycleException e) {
		log(sm.getString("standardContext.startingLoader"), e);
	    }
	}

	if ((manager != null) && (manager instanceof Lifecycle)) {
	    try {
		((Lifecycle) manager).start();
	    } catch (LifecycleException e) {
		log(sm.getString("standardContext.startingManager"), e);
	    }
	}

	for (int i = 0; i < children.length; i++) {
	    Wrapper wrapper = (Wrapper) children[i];
	    if (wrapper instanceof Lifecycle) {
		try {
		    ((Lifecycle) wrapper).start();
		} catch (LifecycleException e) {
		    log(sm.getString("standardContext.startingWrapper",
				     wrapper.getName()),
			e);
		}
	    }
	}

	// Start accepting requests again
	setPaused(false);
	log(sm.getString("standardContext.reloadingCompleted"));

    }


    /**
     * Remove the specified security constraint from this web application.
     *
     * @param constraint Constraint to be removed
     */
    public void removeConstraint(SecurityConstraint constraint) {

	synchronized (constraints) {

	    // Make sure this constraint is currently present
	    int n = -1;
	    for (int i = 0; i < constraints.length; i++) {
		if (constraints[i].equals(constraint)) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified constraint
	    int j = 0;
	    SecurityConstraint results[] =
		new SecurityConstraint[constraints.length - 1];
	    for (int i = 0; i < constraints.length; i++) {
		if (i != n)
		    results[j++] = constraints[i];
	    }
	    constraints = results;

	}

	// Inform interested listeners
	fireContainerEvent("removeConstraint", constraint);

    }


    /**
     * Remove any EJB resource reference with the specified name.
     *
     * @param name Name of the EJB resource reference to remove
     */
    public void removeEjb(String name) {

	synchronized (ejbs) {
	    ejbs.remove(name);
	}
	fireContainerEvent("removeEjb", name);

    }


    /**
     * Remove any environment entry with the specified name.
     *
     * @param name Name of the environment entry to remove
     */
    public void removeEnvironment(String name) {

	synchronized (envs) {
	    envs.remove(name);
	}
	fireContainerEvent("removeEnvironment", name);

    }


    /**
     * Remove the error page for the specified error code or
     * Java language exception, if it exists; otherwise, no action is taken.
     *
     * @param errorPage The error page definition to be removed
     */
    public void removeErrorPage(ErrorPage errorPage) {

	String exceptionType = errorPage.getExceptionType();
	if (exceptionType != null) {
	    synchronized (exceptionPages) {
		exceptionPages.remove(exceptionType);
	    }
	} else {
	    synchronized (statusPages) {
		statusPages.remove(new Integer(errorPage.getErrorCode()));
	    }
	}
	fireContainerEvent("removeErrorPage", errorPage);

    }


    /**
     * Remove a class name from the set of InstanceListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of an InstanceListener class to be removed
     */
    public void removeInstanceListener(String listener) {


	synchronized (instanceListeners) {

	    // Make sure this welcome file is currently present
	    int n = -1;
	    for (int i = 0; i < instanceListeners.length; i++) {
		if (instanceListeners[i].equals(listener)) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified constraint
	    int j = 0;
	    String results[] = new String[instanceListeners.length - 1];
	    for (int i = 0; i < instanceListeners.length; i++) {
		if (i != n)
		    results[j++] = instanceListeners[i];
	    }
	    instanceListeners = results;

	}

	// Inform interested listeners
	fireContainerEvent("removeInstanceListener", listener);

    }


    /**
     * Remove the MIME mapping for the specified extension, if it exists;
     * otherwise, no action is taken.
     *
     * @param extension Extension to remove the mapping for
     */
    public void removeMimeMapping(String extension) {

	synchronized (mimeMappings) {
	    mimeMappings.remove(extension);
	}
	fireContainerEvent("removeMimeMapping", extension);

    }


    /**
     * Remove the context initialization parameter with the specified
     * name, if it exists; otherwise, no action is taken.
     *
     * @param name Name of the parameter to remove
     */
    public void removeParameter(String name) {

	synchronized (parameters) {
	    parameters.remove(name);
	}
	fireContainerEvent("removeParameter", name);

    }


    /**
     * Remove any resource reference with the specified name.
     *
     * @param name Name of the resource reference to remove
     */
    public void removeResource(String name) {

	synchronized (resources) {
	    resources.remove(name);
	}
	fireContainerEvent("removeResource", name);

    }


    /**
     * Remove any security role reference for the specified name
     *
     * @param role Security role (as used in the application) to remove
     */
    public void removeRoleMapping(String role) {

	synchronized (roleMappings) {
	    roleMappings.remove(role);
	}
	fireContainerEvent("removeRoleMapping", role);

    }


    /**
     * Remove any security role with the specified name.
     *
     * @param role Security role to remove
     */
    public void removeSecurityRole(String role) {

	synchronized (securityRoles) {

	    // Make sure this security role is currently present
	    int n = -1;
	    for (int i = 0; i < securityRoles.length; i++) {
		if (role.equals(securityRoles[i])) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified security role
	    int j = 0;
	    String results[] = new String[securityRoles.length - 1];
	    for (int i = 0; i < securityRoles.length; i++) {
		if (i != n)
		    results[j++] = securityRoles[i];
	    }
	    securityRoles = results;

	}

	// Inform interested listeners
	fireContainerEvent("removeSecurityRole", role);

    }


    /**
     * Remove any servlet mapping for the specified pattern, if it exists;
     * otherwise, no action is taken.
     *
     * @param pattern URL pattern of the mapping to remove
     */
    public void removeServletMapping(String pattern) {

	synchronized (servletMappings) {
	    servletMappings.remove(pattern);
	}
	fireContainerEvent("removeServletMapping", pattern);

    }


    /**
     * Remove the tag library location forthe specified tag library URI.
     *
     * @param uri URI, relative to the web.xml file
     */
    public void removeTaglib(String uri) {

	synchronized (taglibs) {
	    taglibs.remove(uri);
	}
	fireContainerEvent("removeTaglib", uri);
    }


    /**
     * Remove the specified welcome file name from the list recognized
     * by this Context.
     *
     * @param name Name of the welcome file to be removed
     */
    public void removeWelcomeFile(String name) {

	synchronized (welcomeFiles) {

	    // Make sure this welcome file is currently present
	    int n = -1;
	    for (int i = 0; i < welcomeFiles.length; i++) {
		if (welcomeFiles[i].equals(name)) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified constraint
	    int j = 0;
	    String results[] = new String[welcomeFiles.length - 1];
	    for (int i = 0; i < welcomeFiles.length; i++) {
		if (i != n)
		    results[j++] = welcomeFiles[i];
	    }
	    welcomeFiles = results;

	}

	// Inform interested listeners
	postWelcomeFiles();
	fireContainerEvent("removeWelcomeFile", name);

    }


    /**
     * Remove a class name from the set of LifecycleListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a LifecycleListener class to be removed
     */
    public void removeWrapperLifecycle(String listener) {


	synchronized (wrapperLifecycles) {

	    // Make sure this welcome file is currently present
	    int n = -1;
	    for (int i = 0; i < wrapperLifecycles.length; i++) {
		if (wrapperLifecycles[i].equals(listener)) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified constraint
	    int j = 0;
	    String results[] = new String[wrapperLifecycles.length - 1];
	    for (int i = 0; i < wrapperLifecycles.length; i++) {
		if (i != n)
		    results[j++] = wrapperLifecycles[i];
	    }
	    wrapperLifecycles = results;

	}

	// Inform interested listeners
	fireContainerEvent("removeWrapperLifecycle", listener);

    }


    /**
     * Remove a class name from the set of ContainerListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a ContainerListener class to be removed
     */
    public void removeWrapperListener(String listener) {


	synchronized (wrapperListeners) {

	    // Make sure this welcome file is currently present
	    int n = -1;
	    for (int i = 0; i < wrapperListeners.length; i++) {
		if (wrapperListeners[i].equals(listener)) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified constraint
	    int j = 0;
	    String results[] = new String[wrapperListeners.length - 1];
	    for (int i = 0; i < wrapperListeners.length; i++) {
		if (i != n)
		    results[j++] = wrapperListeners[i];
	    }
	    wrapperListeners = results;

	}

	// Inform interested listeners
	fireContainerEvent("removeWrapperListener", listener);

    }


    /**
     * Set the Loader with which this Context is associated.
     *
     * @param loader The newly associated loader
     */
    public synchronized void setLoader(Loader loader) {

	super.setLoader(loader);

    }


    /**
     * Start this Context component.
     *
     * @param LifecycleException if a startup error occurs
     */
    public void start() throws LifecycleException {

	setWorkDirectory();
	super.start();

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

	StringBuffer sb = new StringBuffer();
	if (getParent() != null) {
	    sb.append(getParent().toString());
	    sb.append(".");
	}
	sb.append("StandardContext[");
	sb.append(getName());
	sb.append("]");
	return (sb.toString());

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Add a default Mapper implementation if none have been configured
     * explicitly.
     *
     * @param mapperClass Java class name of the default Mapper
     */
    protected void addDefaultMapper(String mapperClass) {

	super.addDefaultMapper(this.mapperClass);

    }


    /**
     * Return the request processing paused flag for this Context.
     */
    private boolean getPaused() {

	return (this.paused);

    }


    /**
     * Set the request processing paused flag for this Context.
     *
     * @param paused The new request processing paused flag
     */
    private void setPaused(boolean paused) {

	this.paused = paused;

    }


    /**
     * Set the appropriate context attribute for our work directory.
     */
    private void setWorkDirectory() {

	// Acquire (or calculate) the work directory path
	String workDir = getWorkDir();
	if (workDir == null) {
	    String temp = getPath();
	    if (temp.startsWith("/"))
		temp = temp.substring(1);
	    temp = temp.replace('/', '_');
	    temp = temp.replace('\\', '_');
	    if (temp.length() < 1)
		temp = "_";
	    workDir = "work" + File.separator + temp;
	    setWorkDir(workDir);
	}

	// Create this directory if necessary
	File dir = new File(workDir);
	if (!dir.isAbsolute())
	    dir = new File(System.getProperty("catalina.home"), workDir);
	dir.mkdirs();

	// Set the appropriate servlet context attribute
	getServletContext().setAttribute(Constants.WORKDIR_ATTR, dir);

    }


}
