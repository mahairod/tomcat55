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
import java.io.FilePermission;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Permissions;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
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

        // Has a Realm been configured for us to authenticate against?
        if (context.getRealm() == null) {
            log(sm.getString("contextConfig.missingRealm"));
            ok = false;
            return;
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
     * Create and return an XmlMapper configured to process a tag library
     * descriptor, looking for additional listener classes to be registered.
     */
    private XmlMapper createTldMapper() {

        XmlMapper mapper = new XmlMapper();
        if (debug > 0)
            mapper.setDebug(3);
        mapper.setValidating(true);
	File resourceFile = new File(System.getProperty("catalina.home"),
				     Constants.TldDtdResourcePath_11);
	mapper.registerDTDFile(Constants.TldDtdPublicId_11,
			       resourceFile.toString());
	resourceFile = new File(System.getProperty("catalina.home"),
				Constants.TldDtdResourcePath_12);
	mapper.registerDTDFile(Constants.TldDtdPublicId_12,
			       resourceFile.toString());

	mapper.addRule("taglib/listener/listener-class",
		       mapper.methodSetter("addApplicationListener", 0));

        return (mapper);

    }


    /**
     * Create and return an XmlMapper configured to process the web application
     * deployment descriptor (web.xml).
     */
    private XmlMapper createWebMapper() {

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

        mapper.addRule("web-app/display-name",
                       mapper.methodSetter("setDisplayName", 0));

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
	mapper.addRule("web-app/ejb-ref/run-as",
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


        mapper.addRule("web-app/resource-env-ref",
                       mapper.methodSetter("addResourceEnvRef", 2));
        mapper.addRule("web-app/resource-env-ref/resource-env-ref-name",
                       mapper.methodParam(0));
        mapper.addRule("web-app/resource-env-ref/resource-env-ref-type",
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
        mapper.addRule("web-app/resource-ref/res-sharing-scope",
                       mapper.methodSetter("setScope", 0));
	mapper.addRule("web-app/resource-ref/res-type",
		       mapper.methodSetter("setType", 0));

	mapper.addRule("web-app/security-constraint",
		       mapper.objectCreate("org.apache.catalina.deploy.SecurityConstraint"));
	mapper.addRule("web-app/security-constraint",
		       mapper.addChild("addConstraint",
				       "org.apache.catalina.deploy.SecurityConstraint"));
        mapper.addRule("web-app/security-constraint/auth-constraint",
                       new SetAuthConstraint());
	mapper.addRule("web-app/security-constraint/auth-constraint/role-name",
		       mapper.methodSetter("addAuthRole", 0));
        mapper.addRule("web-app/security-constraint/display-name",
                       mapper.methodSetter("setDisplayName", 0));
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
     * Configure permissions for this web application if we are running
     * under the control of a security manager.
     */
    private void permissionsConfig() {

        // Has a security manager been installed?
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager == null)
            return;

        // Refresh the standard policy permissions
        if (debug >= 1)
            log("Retrieving global policy permissions");
        Policy policy = Policy.getPolicy();
        policy.refresh();

        // Accumulate the common permissions we will add to all code sources
        if (debug >= 1)
            log("Building common permissions to add");
        Resources resources = context.getResources();
        Permissions commonPerms = new Permissions();
        URL baseURL = null;
        try {
            baseURL = resources.getResource("/");
            if (debug >= 1)
                log(" baseURL=" + baseURL.toString());
        } catch (MalformedURLException e) {
            log("permissionsConfig.baseURL", e);
        }
        String baseFile = baseURL.toString();
        if (baseFile.startsWith("file://"))     // FIXME - file dependency
            baseFile = baseFile.substring(7);
        else if (baseFile.startsWith("file:"))
            baseFile = baseFile.substring(5);
        if (baseFile.endsWith("/"))
            baseFile += "-";
        else
            baseFile += "/-";
        commonPerms.add(new FilePermission(baseFile, "read"));
        File workDir = (File)
            context.getServletContext().getAttribute(Globals.WORK_DIR_ATTR);
        commonPerms.add(new FilePermission(workDir.getAbsolutePath() + "/-",
                                           "read,write,delete"));
        if (debug >= 1)
            log(" commonPerms=" + commonPerms.toString());

        // Build a CodeSource representing our document root code base
        if (debug >= 1)
            log("Building document root code source");
        URL docURL = null;
        try {
            docURL = resources.getResource("/WEB-INF");
            if (debug >= 1)
                log(" docURL=" + docURL.toString());
        } catch (MalformedURLException e) {
            log("permissionsConfig.docURL", e);
        }
        CodeSource docSource = new CodeSource(docURL, null);
        if (debug >= 1)
            log(" docSource=" + docSource.toString());

        // Generate the Permissions for the document root code base
        if (debug >= 1)
            log("Building document root permissions");
        PermissionCollection docPerms = policy.getPermissions(docSource);
        Enumeration docAdds = commonPerms.elements();
        while (docAdds.hasMoreElements())
            docPerms.add((Permission) docAdds.nextElement());
        if (debug >= 1)
            log(" docPerms=" + docPerms);

        // Generate the ProtectionDomain for the document root code base
        if (debug >= 1)
            log("Building document root protection domain");
        ProtectionDomain docPD = new ProtectionDomain(docSource, docPerms);
        if (debug >= 1)
            log(" docPD=" + docPD.toString());

        // Build a CodeSource representing our work directory code base
        if (debug >= 1)
            log("Building work directory code source");
        URL workURL = null;
        try {
            workURL = new URL("file", null, workDir.getAbsolutePath());
            if (debug >= 1)
                log(" workURL=" + workURL.toString());
        } catch (MalformedURLException e) {
            log("permissionsConfig.workURL", e);
        }
        CodeSource workSource = new CodeSource(workURL, null);

        // Generate the Permissions for the work directory code base
        if (debug >= 1)
            log("Building work directory permissions");
        PermissionCollection workPerms = policy.getPermissions(workSource);
        Enumeration workAdds = commonPerms.elements();
        while (workAdds.hasMoreElements())
            workPerms.add((Permission) workAdds.nextElement());
        if (debug >= 1)
            log(" workPerms=" + workPerms);

    }


    /**
     * Process a "start" event for this Context.
     */
    private void start() {

	if (debug > 0)
	    log(sm.getString("contextConfig.start"));
        ok = true;

        // Configure the Permissions for this Context (if necessary)
        //        permissionsConfig();  // FIXME - Method not finished yet

	// Process the default and application web.xml files
	XmlMapper mapper = createWebMapper();
	defaultConfig(mapper);
	applicationConfig(mapper);

        // Scan tag library descriptor files for additional listener classes
        if (ok)
            tldConfig();

        // Configure a certificates exposer valve, if required
        if (ok)
            certificatesConfig();

	// Configure an authenticator if we need one
        if (ok)
            authenticatorConfig();

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

    }


    /**
     * Scan the tag library descriptors of all tag libraries we can find, and
     * register any application descriptor classes that are found there.
     */
    private void tldConfig() {

        // Acquire an XmlMapper to use for parsing
        XmlMapper mapper = createTldMapper();

        // First, scan tag libraries declared in our deployment descriptor
        if (debug >= 1)
            log("Scanning web.xml tag libraries");
        ArrayList resourcePaths = new ArrayList(); // Already processed TLDs
        String taglibs[] = context.findTaglibs();
        for (int i = 0; i < taglibs.length; i++) {

            // Calculate the resource path of the next tag library to check
            String resourcePath = context.findTaglib(taglibs[i]);
            if (!resourcePath.startsWith("/"))
                resourcePath = "/WEB-INF/web.xml/../" + resourcePath;
            if (debug >= 2)
                log("  URI='" + taglibs[i] + "', ResourcePath='" +
                    resourcePath + "'");
            if (resourcePaths.contains(resourcePath)) {
                if (debug >= 2)
                    log("    Already processed");
                continue;
            }
            resourcePaths.add(resourcePath);

            // Process either a JAR file or a TLD at this location
            if (!tldConfigJar(resourcePath, mapper))
                tldConfigTld(resourcePath, mapper);

        }

        // Second, scan tag libraries defined in JAR files
        // FIXME - Yet another dependence on files
        if (debug >= 1)
            log("Scanning library JAR files");
	Resources resources = context.getResources();
        URL libURL = null;
        try {
            libURL = resources.getResource("/WEB-INF/lib");
        } catch (MalformedURLException e) {
            ;
        }

	if ((libURL != null) && "file".equals(libURL.getProtocol())) {
	    File libFile = new File(libURL.getFile());
	    if (libFile.exists() && libFile.canRead() &&
	        libFile.isDirectory()) {
		String filenames[] = libFile.list();
		for (int i = 0; i < filenames.length; i++) {
		    if (!filenames[i].endsWith(".jar"))
		        continue;
                    String resourcePath = "/WEB-INF/lib/" + filenames[i];
                    if (debug >= 2)
                        log("  Trying '" + resourcePath + "'");
                    tldConfigJar("/WEB-INF/lib/" + filenames[i], mapper);
		}
	    }
	}

    }


    /**
     * Process a TLD (if there is one) in the JAR file (if there is one) at
     * the specified resource path.  Return <code>true</code> if we actually
     * found and processed such a TLD, or <code>false</code> otherwise.
     *
     * @param resourcePath Context-relative resource path
     * @param mapper XmlMapper to use for parsing
     */
    private boolean tldConfigJar(String resourcePath, XmlMapper mapper) {

        JarFile jarFile = null;
        InputStream stream = null;
        try {
            URL url = context.getServletContext().getResource(resourcePath);
            if (url == null)
                return (false);
            url = new URL("jar:" + url.toString() + "!/");
            JarURLConnection conn =
                (JarURLConnection) url.openConnection();
            jarFile = conn.getJarFile();
            boolean found = false;
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("META-INF/"))
                    continue;
                if (!name.endsWith(".tld"))
                    continue;
                if (debug >= 2)
                    log("    tldConfigJar(" + resourcePath +
                        "): Processing entry '" + name + "'");
                stream = jarFile.getInputStream(entry);
                mapper.readXml(stream, context);
                stream.close();
                found = true;
            }
            jarFile.close();
            return (found);
        } catch (Exception e) {
            if (debug >= 2)
                log("    tldConfigJar(" + resourcePath + "): " + e);
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    ;
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable t) {
                    ;
                }
            }
            return (false);
        }

    }


    /**
     * Process a TLD (if there is one) at the specified resource path.
     * Return <code>true</code> if we actually found and processed such
     * a TLD, or <code>false</code> otherwise.
     *
     * @param resourcePath Context-relative resource path
     * @param mapper XmlMapper to use for parsing
     */
    private boolean tldConfigTld(String resourcePath, XmlMapper mapper) {

        InputStream stream = null;
        try {
            stream =
                context.getServletContext().getResourceAsStream(resourcePath);
            if (stream == null)
                return (false);
            mapper.readXml(stream, context);
            stream.close();
            return (true);
        } catch (Exception e) {
            if (debug >= 2)
                log("    tldConfigTld(" + resourcePath + "): " + e);
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    ;
                }
            }
            return (false);
        }

    }


}



// ----------------------------------------------------------- Private Classes


/**
 * An XmlAction that calls the <code>setAuthConstraint(true)</code> method of
 * the top item on the stack, which must be of type
 * <code>org.apache.catalina.deploy.SecurityConstraint</code>.
 */

final class SetAuthConstraint extends XmlAction {

    public SetAuthConstraint() {
        super();
    }

    public void start(SaxContext ctx) {
        Stack stack = ctx.getObjectStack();
        SecurityConstraint securityConstraint =
            (SecurityConstraint) stack.peek();
        securityConstraint.setAuthConstraint(true);
	if (ctx.getDebug() > 0)
	    ctx.log("Calling SecurityConstraint.setAuthConstraint(true)");
    }

}


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
