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


package org.apache.catalina.startup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Resources;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.loader.Extension;
import org.apache.catalina.loader.StandardClassLoader;
import org.apache.catalina.loader.StandardLoader;
import org.apache.catalina.resources.FileResources;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.xml.SaxContext;
import org.apache.catalina.util.xml.XmlAction;
import org.apache.catalina.util.xml.XmlMapper;
import org.apache.catalina.valves.ValveBase;
import org.xml.sax.SAXParseException;


/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class ContextConfig
    implements LifecycleListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of Authenticators that we know how to configure.  The key is
     * the name of the implemented authentication method, and the value is
     * the fully qualified Java class name of the corresponding Valve.
     */
    private static ResourceBundle authenticators = null;


    /**
     * The Context we are associated with.
     */
    private Context context = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * Track any fatal errors during startup configuration processing.
     */
    private boolean ok = false;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
	StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

	return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

	this.debug = debug;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

	// Identify the context we are associated with
	try {
	    context = (Context) event.getLifecycle();
	} catch (ClassCastException e) {
	    log(sm.getString("contextConfig.cce", event.getLifecycle()), e);
	    return;
	}

	// Process the event that has occurred
	if (event.getType().equals(Lifecycle.START_EVENT))
	    start();
	else if (event.getType().equals(Lifecycle.STOP_EVENT))
	    stop();

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Process the application configuration file, if it exists.
     *
     * @param mapper The XmlMapper to be used
     */
    private void applicationConfig(XmlMapper mapper) {

	// Open the application web.xml file, if it exists
	InputStream stream = null;
	Resources resources = context.getResources();
	if (resources != null)
	    stream =
		resources.getResourceAsStream(Constants.ApplicationWebXml);
	if (stream == null) {
	    log(sm.getString("contextConfig.applicationMissing"));
	    return;
	}

	// Process the application web.xml file
	try {
	    mapper.readXml(stream, context);
	} catch (InvocationTargetException e) {
	    log(sm.getString("contextConfig.applicationConfig"),
		e.getTargetException());
            ok = false;
        } catch (SAXParseException e) {
            log(sm.getString("contextConfig.applicationParse"), e);
            log(sm.getString("contextConfig.applicationPosition",
                             "" + e.getLineNumber(),
                             "" + e.getColumnNumber()));
            ok = false;
	} catch (Exception e) {
	    log(sm.getString("contextConfig.applicationParse"), e);
            ok = false;
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
		log(sm.getString("contextConfig.applicationClose"), e);
	    }
	}

    }


    /**
     * Set up an Authenticator automatically if required, and one has not
     * already been configured.
     */
    private synchronized void authenticatorConfig() {

	// Does this Context require an Authenticator?
	SecurityConstraint constraints[] = context.findConstraints();
	if ((constraints == null) || (constraints.length == 0))
	    return;
	LoginConfig loginConfig = context.getLoginConfig();
	if (loginConfig == null)
	    return;

	// Has an authenticator been configured already?
	if (context instanceof Authenticator)
	    return;
	if (context instanceof ContainerBase) {
	    Valve basic = ((ContainerBase) context).getBasic();
	    if ((basic != null) && (basic instanceof Authenticator))
		return;
	}
	if (context instanceof Pipeline) {
	    Valve valve = ((Pipeline) context).findValves();
	    while (valve != null) {
		if (valve instanceof Authenticator)
		    return;
		if (valve instanceof ValveBase)
		    valve = ((ValveBase) valve).getNext();
		else
		    valve = null;
	    }
	} else {
	    return;	// Cannot install a Valve even if it would be needed
	}

	// Load our mapping properties if necessary
	if (authenticators == null) {
	    try {
		authenticators = ResourceBundle.getBundle
		    ("org.apache.catalina.startup.Authenticators");
	    } catch (MissingResourceException e) {
		log(sm.getString("contextConfig.authenticatorResources"), e);
                ok = false;
		return;
	    }
	}

	// Identify the class name of the Valve we should configure
	String authenticatorName = null;
	try {
	    authenticatorName =
		authenticators.getString(loginConfig.getAuthMethod());
	} catch (MissingResourceException e) {
	    authenticatorName = null;
	}
	if (authenticatorName == null) {
	    log(sm.getString("contextConfig.authenticatorMissing",
			     loginConfig.getAuthMethod()));
            ok = false;
	    return;
	}

	// Instantiate and install an Authenticator of the requested class
	Valve authenticator = null;
	try {
	    Class authenticatorClass = Class.forName(authenticatorName);
	    authenticator = (Valve) authenticatorClass.newInstance();
	    ((Pipeline) context).addValve(authenticator);
	    log(sm.getString("contextConfig.authenticatorConfigured",
			     loginConfig.getAuthMethod()));
	} catch (Throwable t) {
	    log(sm.getString("contextConfig.authenticatorInstantiate",
			     authenticatorName), t);
            ok = false;
	}

    }


    /**
     * Create and deploy a Valve to expose the SSL certificates presented
     * by this client, if any.  If we cannot instantiate such a Valve
     * (because the JSSE classes are not available), silently continue.
     */
    private void certificatesConfig() {

        // Validate that the JSSE classes are present
        try {
            Class clazz = this.getClass().getClassLoader().loadClass
                ("javax.net.ssl.SSLSocket");
            if (clazz == null)
                return;
        } catch (Throwable t) {
            return;
        }

        // Instantiate a new CertificatesValve if possible
        Valve certificates = null;
        try {
            Class clazz =
                Class.forName("org.apache.catalina.valves.CertificatesValve");
            certificates = (Valve) clazz.newInstance();
        } catch (Throwable t) {
            return;     // Probably JSSE classes not present
        }

        // Add this Valve to our Pipeline
        try {
            ((Pipeline) context).addValve(certificates);
            log(sm.getString("contextConfig.certificatesConfig.added"));
        } catch (Throwable t) {
            log(sm.getString("contextConfig.certificatesConfig.error"), t);
            ok = false;
        }

    }


    /**
     * Create and return an XmlMapper configured to process the web application
     * deployment descriptor (web.xml).
     */
    private XmlMapper createMapper() {

	XmlMapper mapper = new XmlMapper();
	if (debug > 0)
	    mapper.setDebug(3);
	mapper.setValidating(true);
	File resourceFile = new File(System.getProperty("catalina.home"),
				     Constants.WebDtdResourcePath_22);
	mapper.registerDTDFile(Constants.WebDtdPublicId_22,
			       resourceFile.toString());
	resourceFile = new File(System.getProperty("catalina.home"),
				Constants.WebDtdResourcePath_23);
	mapper.registerDTDFile(Constants.WebDtdPublicId_23,
			       resourceFile.toString());

	mapper.addRule("web-app/context-param",
		       mapper.methodSetter("addParameter", 2));
	mapper.addRule("web-app/context-param/param-name",
		       mapper.methodParam(0));
	mapper.addRule("web-app/context-param/param-value",
		       mapper.methodParam(1));

	mapper.addRule("web-app/distributable",
		       mapper.methodSetter("setDistributable", 0));

	mapper.addRule("web-app/ejb-ref",
		 mapper.objectCreate("org.apache.catalina.deploy.ContextEjb"));
	mapper.addRule("web-app/ejb-ref",
		       mapper.addChild("addEjb",
				     "org.apache.catalina.deploy.ContextEjb"));
	mapper.addRule("web-app/ejb-ref/description",
		       mapper.methodSetter("setDescription", 0));
	mapper.addRule("web-app/ejb-ref/ejb-ref-name",
		       mapper.methodSetter("setName", 0));
	mapper.addRule("web-app/ejb-ref/ejb-ref-type",
		       mapper.methodSetter("setType", 0));
	mapper.addRule("web-app/ejb-ref/home",
		       mapper.methodSetter("setHome", 0));
	mapper.addRule("web-app/ejb-ref/remote",
		       mapper.methodSetter("setRemote", 0));
	mapper.addRule("web-app/ejb-ref/ejb-link",
		       mapper.methodSetter("setLink", 0));
	mapper.addRule("web-app/ejb-ref/runAs",
		       mapper.methodSetter("setRunAs", 0));

	mapper.addRule("web-app/env-entry",
	 mapper.objectCreate("org.apache.catalina.deploy.ContextEnvironment"));
	mapper.addRule("web-app/env-entry",
		       mapper.addChild("addEnvironment",
			     "org.apache.catalina.deploy.ContextEnvironment"));
	mapper.addRule("web-app/env-entry/env-entry-description",
		       mapper.methodSetter("setDescription", 0));
	mapper.addRule("web-app/env-entry/env-entry-name",
		       mapper.methodSetter("setName", 0));
	mapper.addRule("web-app/env-entry/env-entry-type",
		       mapper.methodSetter("setType", 0));
	mapper.addRule("web-app/env-entry/env-entry-value",
		       mapper.methodSetter("setValue", 0));

	mapper.addRule("web-app/error-page",
		  mapper.objectCreate("org.apache.catalina.deploy.ErrorPage"));
	mapper.addRule("web-app/error-page",
		       mapper.addChild("addErrorPage",
			       "org.apache.catalina.deploy.ErrorPage"));
	mapper.addRule("web-app/error-page/error-code",
		       mapper.methodSetter("setErrorCode", 0));
	mapper.addRule("web-app/error-page/exception-type",
		       mapper.methodSetter("setExceptionType", 0));
	mapper.addRule("web-app/error-page/location",
		       mapper.methodSetter("setLocation", 0));

	mapper.addRule("web-app/filter",
		  mapper.objectCreate("org.apache.catalina.deploy.FilterDef"));
	mapper.addRule("web-app/filter",
		       mapper.addChild("addFilterDef",
				      "org.apache.catalina.deploy.FilterDef"));
	mapper.addRule("web-app/filter/description",
		       mapper.methodSetter("setDescription", 0));
	mapper.addRule("web-app/filter/display-name",
		       mapper.methodSetter("setDisplayName", 0));
	mapper.addRule("web-app/filter/filter-class",
		       mapper.methodSetter("setFilterClass", 0));
	mapper.addRule("web-app/filter/filter-name",
		       mapper.methodSetter("setFilterName", 0));
	mapper.addRule("web-app/filter/icon/large-icon",
		       mapper.methodSetter("setLargeIcon", 0));
	mapper.addRule("web-app/filter/icon/small-icon",
		       mapper.methodSetter("setSmallIcon", 0));
	mapper.addRule("web-app/filter/init-param",
		       mapper.methodSetter("addInitParameter", 2));
	mapper.addRule("web-app/filter/init-param/param-name",
		       mapper.methodParam(0));
	mapper.addRule("web-app/filter/init-param/param-value",
		       mapper.methodParam(1));

	mapper.addRule("web-app/filter-mapping",
		  mapper.objectCreate("org.apache.catalina.deploy.FilterMap"));
	mapper.addRule("web-app/filter-mapping",
		       mapper.addChild("addFilterMap",
				      "org.apache.catalina.deploy.FilterMap"));
	mapper.addRule("web-app/filter-mapping/filter-name",
		       mapper.methodSetter("setFilterName", 0));
	mapper.addRule("web-app/filter-mapping/servlet-name",
		       mapper.methodSetter("setServletName", 0));
	mapper.addRule("web-app/filter-mapping/url-pattern",
		       mapper.methodSetter("setURLPattern", 0));

	mapper.addRule("web-app/listener/listener-class",
		       mapper.methodSetter("addApplicationListener", 0));

	mapper.addRule("web-app/login-config",
		mapper.objectCreate("org.apache.catalina.deploy.LoginConfig"));
	mapper.addRule("web-app/login-config",
		       mapper.addChild("setLoginConfig",
				    "org.apache.catalina.deploy.LoginConfig"));
	mapper.addRule("web-app/login-config/auth-method",
		       mapper.methodSetter("setAuthMethod", 0));
	mapper.addRule("web-app/login-config/realm-name",
		       mapper.methodSetter("setRealmName", 0));
	mapper.addRule("web-app/login-config/form-login-config/form-login-page",
		       mapper.methodSetter("setLoginPage", 0));
	mapper.addRule("web-app/login-config/form-login-config/form-error-page",
		       mapper.methodSetter("setErrorPage", 0));

	mapper.addRule("web-app/mime-mapping",
		       mapper.methodSetter("addMimeMapping", 2));
	mapper.addRule("web-app/mime-mapping/extension",
		       mapper.methodParam(0));
	mapper.addRule("web-app/mime-mapping/mime-type",
		       mapper.methodParam(1));


	mapper.addRule("web-app/resource-ref",
            mapper.objectCreate("org.apache.catalina.deploy.ContextResource"));
        mapper.addRule("web-app/resource-ref",
		mapper.addChild("addResource",
			        "org.apache.catalina.deploy.ContextResource"));
	mapper.addRule("web-app/resource-ref/description",
		       mapper.methodSetter("setDescription", 0));
	mapper.addRule("web-app/resource-ref/res-auth",
		       mapper.methodSetter("setAuth", 0));
	mapper.addRule("web-app/resource-ref/res-ref-name",
		       mapper.methodSetter("setName", 0));
	mapper.addRule("web-app/resource-ref/res-type",
		       mapper.methodSetter("setType", 0));

	mapper.addRule("web-app/security-constraint",
		       mapper.objectCreate("org.apache.catalina.deploy.SecurityConstraint"));
	mapper.addRule("web-app/security-constraint",
		       mapper.addChild("addConstraint",
				       "org.apache.catalina.deploy.SecurityConstraint"));
	mapper.addRule("web-app/security-constraint/auth-constraint/role-name",
		       mapper.methodSetter("addAuthRole", 0));
	mapper.addRule("web-app/security-constraint/user-data-constraint/transport-guarantee",
		       mapper.methodSetter("setUserConstraint", 0));
	mapper.addRule("web-app/security-constraint/web-resource-collection",
		       mapper.objectCreate("org.apache.catalina.deploy.SecurityCollection"));
	mapper.addRule("web-app/security-constraint/web-resource-collection",
		       mapper.addChild("addCollection",
				       "org.apache.catalina.deploy.SecurityCollection"));
	mapper.addRule("web-app/security-constraint/web-resource-collection/http-method",
		       mapper.methodSetter("addMethod", 0));
	mapper.addRule("web-app/security-constraint/web-resource-collection/url-pattern",
		       mapper.methodSetter("addPattern", 0));
	mapper.addRule("web-app/security-constraint/web-resource-collection/web-resource-name",
		       mapper.methodSetter("setName", 0));

	mapper.addRule("web-app/security-role",
		       mapper.methodSetter("addSecurityRole", 1));
	mapper.addRule("web-app/security-role/role-name",
		       mapper.methodParam(0));

	mapper.addRule("web-app/servlet",
		       new WrapperCreate(context));
	mapper.addRule("web-app/servlet",
		       mapper.addChild("addChild",
				       "org.apache.catalina.Container"));
	mapper.addRule("web-app/servlet/init-param",
		       mapper.methodSetter("addInitParameter", 2));
	mapper.addRule("web-app/servlet/init-param/param-name",
		       mapper.methodParam(0));
	mapper.addRule("web-app/servlet/init-param/param-value",
		       mapper.methodParam(1));
	mapper.addRule("web-app/servlet/jsp-file",
		       mapper.methodSetter("setJspFile", 0));
	mapper.addRule("web-app/servlet/load-on-startup",
		       mapper.methodSetter("setLoadOnStartupString", 0));
	mapper.addRule("web-app/servlet/security-role-ref",
		       mapper.methodSetter("addSecurityReference", 2));
	mapper.addRule("web-app/servlet/security-role-ref/role-link",
		       mapper.methodParam(1));
	mapper.addRule("web-app/servlet/security-role-ref/role-name",
		       mapper.methodParam(0));
	mapper.addRule("web-app/servlet/servlet-class",
		       mapper.methodSetter("setServletClass", 0));
	mapper.addRule("web-app/servlet/servlet-name",
		       mapper.methodSetter("setName", 0));

	mapper.addRule("web-app/servlet-mapping",
		       mapper.methodSetter("addServletMapping", 2));
	mapper.addRule("web-app/servlet-mapping/servlet-name",
		       mapper.methodParam(1));
	mapper.addRule("web-app/servlet-mapping/url-pattern",
		       mapper.methodParam(0));

	mapper.addRule("web-app/session-config",
		       mapper.methodSetter("setSessionTimeout", 1,
					   new String[]{"int"}));
	mapper.addRule("web-app/session-config/session-timeout",
		       mapper.methodParam(0));

	mapper.addRule("web-app/taglib",
		       mapper.methodSetter("addTaglib", 2));
	mapper.addRule("web-app/taglib/taglib-location",
		       mapper.methodParam(1));
	mapper.addRule("web-app/taglib/taglib-uri",
		       mapper.methodParam(0));

	mapper.addRule("web-app/welcome-file-list/welcome-file",
		       mapper.methodSetter("addWelcomeFile", 0));

	return (mapper);

    }


    /**
     * Process the default configuration file, if it exists.
     *
     * @param mapper The XmlMapper to be used

     */
    private void defaultConfig(XmlMapper mapper) {

	// Open the default web.xml file, if it exists
	File file = new File(Constants.DefaultWebXml);
	if (!file.isAbsolute())
	    file = new File(System.getProperty("catalina.home") +
			    File.separator + Constants.DefaultWebXml);
	FileInputStream stream = null;
	try {
	    stream = new FileInputStream(file.getAbsolutePath());
	} catch (FileNotFoundException e) {
	    log(sm.getString("context.Config.defaultMissing"));
	    return;
	}

	// Process the default web.xml file
	try {
	    mapper.readXml(stream, context);
	} catch (InvocationTargetException e) {
	    log(sm.getString("contextConfig.defaultConfig"),
		e.getTargetException());
            ok = false;
        } catch (SAXParseException e) {
            log(sm.getString("contextConfig.defaultParse"), e);
            log(sm.getString("contextConfig.defaultPosition",
                             "" + e.getLineNumber(),
                             "" + e.getColumnNumber()));
            ok = false;
	} catch (Exception e) {
	    log(sm.getString("contextConfig.defaultParse"), e);
            ok = false;
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
		log(sm.getString("contextConfig.defaultClose"), e);
	    }
	}

    }


    /**
     * Configure the set of instantiated application event listeners
     * for this Context.
     */
    private void listenerConfig() {

        if (debug >= 1)
	    log("Configuring application event listeners");

        ClassLoader loader = context.getLoader().getClassLoader();
	String listeners[] = context.findApplicationListeners();
        Object results[] = new Object[listeners.length];
	boolean error = false;
	for (int i = 0; i < results.length; i++) {
	    if (debug >= 2)
		log(" Configuring event listener class '" +
		    listeners[i] + "'");
	    try {
		Class clazz = loader.loadClass(listeners[i]);
		results[i] = clazz.newInstance();
	    } catch (Throwable t) {
		log(sm.getString("contextConfig.applicationListener",
				 listeners[i]), t);
		error = true;
                ok = false;
	    }
	}
	if (!error)
	    context.setApplicationListeners(results);

    }


    /**
     * Send an application start event to all interested listeners.
     */
    private void listenerStartEvent() {

        if (debug >= 1)
	    log("Sending application start events");

        Object listeners[] = context.getApplicationListeners();
        if (listeners == null)
	    return;
	ServletContextEvent event =
	  new ServletContextEvent(context.getServletContext());
	for (int i = 0; i < listeners.length; i++) {
	    if (!(listeners[i] instanceof ServletContextListener))
	        continue;
	    try {
	        ServletContextListener listener =
		  (ServletContextListener) listeners[i];
		listener.contextInitialized(event);
	    } catch (Throwable t) {
	        log(sm.getString("contextConfig.applicationStart"), t);
                ok = false;
	    }
	}

    }


    /**
     * Send an application stop event to all interested listeners.
     */
    private void listenerStopEvent() {

        if (debug >= 1)
	    log("Sending application stop events");

        Object listeners[] = context.getApplicationListeners();
        if (listeners == null)
	    return;
	ServletContextEvent event =
	  new ServletContextEvent(context.getServletContext());
	for (int i = 0; i < listeners.length; i++) {
	    int j = (listeners.length - 1) - i;
	    if (!(listeners[j] instanceof ServletContextListener))
	        continue;
	    try {
	        ServletContextListener listener =
		  (ServletContextListener) listeners[j];
		listener.contextDestroyed(event);
	    } catch (Throwable t) {
	        log(sm.getString("contextConfig.applicationStop"), t);
                ok = false;
	    }
	}

    }


    /**
     * Configure the repositories for the Loader associated with
     * this Context.
     */
    private void loaderConfig() {

	if (debug > 0)
	    log("Configuring class loader repositories");

	// Identify the components we will need
	// FIXME - dependency on StandardLoader versus Loader?
	StandardLoader loader = null;
	try {
	    loader = (StandardLoader) context.getLoader();
        } catch (ClassCastException e) {
	    ;
        }
	if (loader == null)
	    return;
	Resources resources = context.getResources();

	// Add the WEB-INF/classes subdirectory
	URL classesURL = null;
	try {
	    classesURL = resources.getResource("/WEB-INF/classes");
        } catch (MalformedURLException e) {
	    ;
	}
        if (classesURL != null)
            loader.addRepository(classesURL.toString() + "/");

	// Add the WEB-INF/lib/*.jar files
	URL libURL = null;
	try {
	    libURL = resources.getResource("/WEB-INF/lib");
	} catch (MalformedURLException e) {
	    ;
	}
        // FIXME - This still requires disk directory!  Scan JARs if present
	if ((libURL != null) && "file".equals(libURL.getProtocol())) {
	    File libFile = new File(libURL.getFile());
	    if (libFile.exists() && libFile.canRead() &&
	        libFile.isDirectory()) {
		String filenames[] = libFile.list();
		for (int i = 0; i < filenames.length; i++) {
		    if (!filenames[i].endsWith(".jar"))
		        continue;
		    File jarFile = new File(libFile, filenames[i]);
		    if (debug > 0)
		        log(" Adding '" + "file: " +
                            jarFile.getAbsolutePath() + "'");
                    loader.addRepository("file:" + jarFile.getAbsolutePath());
		}
	    }
	}

        // Validate the required and available optional packages
        ClassLoader classLoader = loader.getClassLoader();
        if (classLoader instanceof StandardClassLoader) {

            Extension available[] =
                ((StandardClassLoader) classLoader).findAvailable();
            Extension required[] =
                ((StandardClassLoader) classLoader).findRequired();
            if (debug >= 1)
                log("Optional Packages:  available=" +
                    available.length + ", required=" +
                    required.length);

            for (int i = 0; i < required.length; i++) {
                if (debug >= 1)
                    log("Checking for required package " + required[i]);
                boolean found = false;
                for (int j = 0; j < available.length; j++) {
                    if (available[j].isCompatibleWith(required[i])) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    throw new IllegalStateException
                        ("start:  Missing extension " + required[i]);
            }

        }

    }


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	Logger logger = null;
	if (context != null)
	    logger = context.getLogger();
	if (logger != null)
	    logger.log("ContextConfig[" + context.getName() + "]: " + message);
	else
	    System.out.println("ContextConfig[" + context.getName() + "]: "
			       + message);

    }


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

	Logger logger = null;
	if (context != null)
	    logger = context.getLogger();
	if (logger != null)
	    logger.log("ContextConfig[" + context.getName() + "] "
		       + message, throwable);
	else {
	    System.out.println("ContextConfig[" + context.getName() + "]: "
			       + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}

    }


    /**
     * Process a "start" event for this Context.
     */
    private void start() {

	if (debug > 0)
	    log(sm.getString("contextConfig.start"));
        ok = true;

	// Configure a mapper to read a web application deployment descriptor
	XmlMapper mapper = createMapper();

	// Add missing Loader component if necessary
	if (context.getLoader() == null) {
	    if (debug > 0)
		log(sm.getString("contextConfig.defaultLoader"));
	    context.setLoader
	      (new StandardLoader(context.getParentClassLoader()));
	}

	// Add missing Manager component if necessary
	if (context.getManager() == null) {
	    if (debug > 0)
		log(sm.getString("contextConfig.defaultManager"));
	    context.setManager(new StandardManager());
	}

	// Add missing Resources component if necessary
	if (context.getResources() == null) {
	    if (debug > 0)
		log(sm.getString("contextConfig.defaultResources"));
	    context.setResources(new FileResources());
	}

	// Configure the Loader for this Context
	loaderConfig();

	// Process the default and application web.xml files
	defaultConfig(mapper);
	applicationConfig(mapper);

        // Configure a certificates exposer valve, if required
        if (ok)
            certificatesConfig();

	// Configure an authenticator if we need one
        if (ok)
            authenticatorConfig();

	// Configure the application event listeners for this Context
	// PREREQUISITE:  class loader has been configured
        if (ok)
            listenerConfig();

	// Send an application start event to all interested listeners
        if (ok)
            listenerStartEvent();

        // Dump the contents of this pipeline if requested
        if (debug >= 1) {
            log("Pipline Configuration:");
            Valve valve = ((Pipeline) context).findValves();
            while (valve != null) {
                log("  " + valve.getInfo());
                valve = valve.getNext();
            }
            log("======================");
        }

        // Make our application available if no problems were encountered
        if (ok)
            context.setAvailable(true);
        else {
            log(sm.getString("contextConfig.unavailable"));
            context.setAvailable(false);
        }

    }


    /**
     * Process a "stop" event for this Context.
     */
    private void stop() {

	if (debug > 0)
	    log(sm.getString("contextConfig.stop"));
        ok = true;

	// Send an application stop event to all interested listeners
	listenerStopEvent();

	// Release references to the listener objects themselves
	context.setApplicationListeners(null);

    }


}


// ----------------------------------------------------------- Private Classes


/**
 * An XmlAction that calls the factory method on the specified context to
 * create the object that is to be added to the stack.
 */

final class WrapperCreate extends XmlAction {

    private Context context = null;

    public WrapperCreate(Context context) {
        this.context = context;
    }

    public void start(SaxContext ctx) {
        Stack stack = ctx.getObjectStack();
        Wrapper wrapper = context.createWrapper();
	stack.push(wrapper);
	if (ctx.getDebug() > 0)
	    ctx.log("new " + wrapper.getClass().getName());
    }

    public void cleanup(SaxContext ctx) {
        Stack stack = ctx.getObjectStack();
	Wrapper wrapper = (Wrapper) stack.pop();
	if (ctx.getDebug() > 0)
	    ctx.log("pop " + wrapper.getClass().getName());
    }

}
