/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.servlet.ServletContext;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.loader.Extension;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.digester.Digester;
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


    /**
     * The <code>Digester</code> we will use to process tag library
     * descriptor files.
     */
    private static Digester tldDigester = null;


    /**
     * The <code>Digester</code> we will use to process web application
     * deployment descriptor files.
     */
    private static Digester webDigester = null;


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
            if (context instanceof StandardContext) {
                int contextDebug = ((StandardContext) context).getDebug();
                if (contextDebug > this.debug)
                    this.debug = contextDebug;
            }
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
     */
    private void applicationConfig() {

        // Open the application web.xml file, if it exists
        InputStream stream = null;
        ServletContext servletContext = context.getServletContext();
        if (servletContext != null)
            stream = servletContext.getResourceAsStream
                (Constants.ApplicationWebXml);
        if (stream == null) {
            log(sm.getString("contextConfig.applicationMissing"));
            return;
        }

        // Process the application web.xml file
        try {
            Digester digester = createWebDigester();
            digester.setDebug(getDebug());
            synchronized (digester) {
                if (context instanceof StandardContext)
                    ((StandardContext) context).setReplaceWelcomeFiles(true);
                digester.push(context);
                digester.parse(stream);
            }
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
        if (loginConfig == null) {
            loginConfig = new LoginConfig("NONE", null, null, null);
            context.setLoginConfig(loginConfig);
        }

        // Has an authenticator been configured already?
        if (context instanceof Authenticator)
            return;
        if (context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                Valve basic = pipeline.getBasic();
                if ((basic != null) && (basic instanceof Authenticator))
                    return;
                Valve valves[] = pipeline.getValves();
                for (int i = 0; i < valves.length; i++) {
                    if (valves[i] instanceof Authenticator)
                        return;
                }
            }
        } else {
            return;     // Cannot install a Valve even if it would be needed
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
            if (context instanceof ContainerBase) {
                Pipeline pipeline = ((ContainerBase) context).getPipeline();
                if (pipeline != null) {
                    ((ContainerBase) context).addValve(authenticator);
                    log(sm.getString("contextConfig.authenticatorConfigured",
                                     loginConfig.getAuthMethod()));
                }
            }
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
            if (context instanceof ContainerBase) {
                Pipeline pipeline = ((ContainerBase) context).getPipeline();
                if (pipeline != null) {
                    ((ContainerBase) context).addValve(certificates);
                    log(sm.getString
                        ("contextConfig.certificatesConfig.added"));
                }
            }
        } catch (Throwable t) {
            log(sm.getString("contextConfig.certificatesConfig.error"), t);
            ok = false;
        }

    }


    /**
     * Create (if necessary) and return a Digester configured to process a tag
     * library descriptor, looking for additional listener classes to be
     * registered.
     */
    private synchronized Digester createTldDigester() {

        URL url = null;
        if (tldDigester == null) {
            tldDigester = new Digester();
            if (debug > 0)
                tldDigester.setDebug(3);
            tldDigester.setValidating(true);
            url = this.getClass().getResource(Constants.TldDtdResourcePath_11);
            tldDigester.register(Constants.TldDtdPublicId_11,
                              url.toString());
            url = this.getClass().getResource(Constants.TldDtdResourcePath_12);
            tldDigester.register(Constants.TldDtdPublicId_12,
                              url.toString());
            tldDigester.addRuleSet(new TldRuleSet());
        }
        return (tldDigester);

    }


    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    private synchronized Digester createWebDigester() {

        URL url = null;
        if (webDigester == null) {
            webDigester = new Digester();
            if (debug > 0)
                webDigester.setDebug(3);
            webDigester.setValidating(true);
            url = this.getClass().getResource(Constants.WebDtdResourcePath_22);
            webDigester.register(Constants.WebDtdPublicId_22,
                              url.toString());
            url = this.getClass().getResource(Constants.WebDtdResourcePath_23);
            webDigester.register(Constants.WebDtdPublicId_23,
                              url.toString());
            webDigester.addRuleSet(new WebRuleSet());
        }
        return (webDigester);

    }


    /**
     * Process the default configuration file, if it exists.
     */
    private void defaultConfig() {

        // Open the default web.xml file, if it exists
        File file = new File(Constants.DefaultWebXml);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"),
                            Constants.DefaultWebXml);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file.getCanonicalPath());
        } catch (FileNotFoundException e) {
            log(sm.getString("contextConfig.defaultMissing"));
            return;
        } catch (IOException e) {
            log(sm.getString("contextConfig.defaultMissing"), e);
            return;
        }

        // Process the default web.xml file
        try {
            Digester digester = createWebDigester();
            digester.setDebug(getDebug());
            synchronized (digester) {
                if (context instanceof StandardContext)
                    ((StandardContext) context).setReplaceWelcomeFiles(true);
                digester.push(context);
                digester.parse(stream);
            }
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
     * Process a "start" event for this Context.
     */
    private synchronized void start() {

        if (debug > 0)
            log(sm.getString("contextConfig.start"));
        context.setConfigured(false);
        ok = true;

        // Set properties based on DefaultContext
        Container container = context.getParent();
        if( !context.getOverride() ) {
            if( container instanceof Host ) {
                ((Host)container).importDefaultContext(context);
                container = container.getParent();
            }
            if( container instanceof Engine ) {
                ((Engine)container).importDefaultContext(context);
            }
        }

        // Process the default and application web.xml files
        defaultConfig();
        applicationConfig();

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
        if ((debug >= 1) && (context instanceof ContainerBase)) {
            log("Pipline Configuration:");
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            Valve valves[] = null;
            if (pipeline != null)
                valves = pipeline.getValves();
            if (valves != null) {
                for (int i = 0; i < valves.length; i++) {
                    log("  " + valves[i].getInfo());
                }
            }
            log("======================");
        }

        // Make our application available if no problems were encountered
        if (ok)
            context.setConfigured(true);
        else {
            log(sm.getString("contextConfig.unavailable"));
            context.setConfigured(false);
        }

    }


    /**
     * Process a "stop" event for this Context.
     */
    private synchronized void stop() {

        if (debug > 0)
            log(sm.getString("contextConfig.stop"));

        int i;

        // Removing children
        Container[] children = context.findChildren();
        for (i = 0; i < children.length; i++) {
            context.removeChild(children[i]);
        }

        // Removing application listeners
        String[] applicationListeners = context.findApplicationListeners();
        for (i = 0; i < applicationListeners.length; i++) {
            context.removeApplicationListener(applicationListeners[i]);
        }

        // Removing application parameters
        ApplicationParameter[] applicationParameters =
            context.findApplicationParameters();
        for (i = 0; i < applicationParameters.length; i++) {
            context.removeApplicationParameter
                (applicationParameters[i].getName());
        }

        // Removing security constraints
        SecurityConstraint[] securityConstraints = context.findConstraints();
        for (i = 0; i < securityConstraints.length; i++) {
            context.removeConstraint(securityConstraints[i]);
        }

        // Removing Ejbs
        ContextEjb[] contextEjbs = context.findEjbs();
        for (i = 0; i < contextEjbs.length; i++) {
            context.removeEjb(contextEjbs[i].getName());
        }

        // Removing environments
        ContextEnvironment[] contextEnvironments = context.findEnvironments();
        for (i = 0; i < contextEnvironments.length; i++) {
            context.removeEnvironment(contextEnvironments[i].getName());
        }

        // Removing errors pages
        ErrorPage[] errorPages = context.findErrorPages();
        for (i = 0; i < errorPages.length; i++) {
            context.removeErrorPage(errorPages[i]);
        }

        // Removing filter defs
        FilterDef[] filterDefs = context.findFilterDefs();
        for (i = 0; i < filterDefs.length; i++) {
            context.removeFilterDef(filterDefs[i]);
        }

        // Removing filter maps
        FilterMap[] filterMaps = context.findFilterMaps();
        for (i = 0; i < filterMaps.length; i++) {
            context.removeFilterMap(filterMaps[i]);
        }

        // Removing instance listeners
        String[] instanceListeners = context.findInstanceListeners();
        for (i = 0; i < instanceListeners.length; i++) {
            context.removeInstanceListener(instanceListeners[i]);
        }

        // Removing local ejbs
        ContextLocalEjb[] contextLocalEjbs = context.findLocalEjbs();
        for (i = 0; i < contextLocalEjbs.length; i++) {
            context.removeLocalEjb(contextLocalEjbs[i].getName());
        }

        // Removing Mime mappings
        String[] mimeMappings = context.findMimeMappings();
        for (i = 0; i < mimeMappings.length; i++) {
            context.removeMimeMapping(mimeMappings[i]);
        }

        // Removing parameters
        String[] parameters = context.findParameters();
        for (i = 0; i < parameters.length; i++) {
            context.removeParameter(parameters[i]);
        }

        // Removing resource env refs
        String[] resourceEnvRefs = context.findResourceEnvRefs();
        for (i = 0; i < resourceEnvRefs.length; i++) {
            context.removeResourceEnvRef(resourceEnvRefs[i]);
        }

        // Removing resource links
        ContextResourceLink[] contextResourceLinks = 
            context.findResourceLinks();
        for (i = 0; i < contextResourceLinks.length; i++) {
            context.removeResourceLink(contextResourceLinks[i].getName());
        }

        // Removing resources
        ContextResource[] contextResources = context.findResources();
        for (i = 0; i < contextResources.length; i++) {
            context.removeResource(contextResources[i].getName());
        }

        // Removing sercurity role
        String[] securityRoles = context.findSecurityRoles();
        for (i = 0; i < securityRoles.length; i++) {
            context.removeSecurityRole(securityRoles[i]);
        }

        // Removing servlet mappings
        String[] servletMappings = context.findServletMappings();
        for (i = 0; i < servletMappings.length; i++) {
            context.removeServletMapping(servletMappings[i]);
        }

        // FIXME : Removing status pages

        // Removing taglibs
        String[] taglibs = context.findTaglibs();
        for (i = 0; i < taglibs.length; i++) {
            context.removeTaglib(taglibs[i]);
        }

        // Removing welcome files
        String[] welcomeFiles = context.findWelcomeFiles();
        for (i = 0; i < welcomeFiles.length; i++) {
            context.removeWelcomeFile(welcomeFiles[i]);
        }

        // Removing wrapper lifecycles
        String[] wrapperLifecycles = context.findWrapperLifecycles();
        for (i = 0; i < wrapperLifecycles.length; i++) {
            context.removeWrapperLifecycle(wrapperLifecycles[i]);
        }

        // Removing wrapper listeners
        String[] wrapperListeners = context.findWrapperListeners();
        for (i = 0; i < wrapperListeners.length; i++) {
            context.removeWrapperListener(wrapperListeners[i]);
        }

        ok = true;

    }


    /**
     * Scan the tag library descriptors of all tag libraries we can find, and
     * register any application descriptor classes that are found there.
     */
    private void tldConfig() {

        // Acquire a Digester to use for parsing
        Digester digester = createTldDigester();

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
            if (!tldConfigJar(resourcePath, digester))
                tldConfigTld(resourcePath, digester);

        }

        DirContext resources = context.getResources();

        // Second, scan tag libraries defined in tld files in /WEB-INF
        if (debug >= 1)
            log("Scanning TLD files in /WEB-INF");
        String webinfName = "/WEB-INF";
        // Looking up directory /WEB-INF in the context
        try {
            NamingEnumeration enum = resources.list(webinfName);
            while (enum.hasMoreElements()) {
                NameClassPair ncPair = (NameClassPair) enum.nextElement();
                String filename = webinfName + "/" + ncPair.getName();
                if (!filename.endsWith(".tld"))
                    continue;
                tldConfigTld(filename, digester);
            }
        } catch (NamingException e) {
            // Silent catch: it's valid that no /WEB-INF directory exists
        }

        // Third, scan tag libraries defined in JAR files
        if (debug >= 1)
            log("Scanning library JAR files");
        String libName = "/WEB-INF/lib";
        // Looking up directory /WEB-INF/lib in the context
        try {
            NamingEnumeration enum = resources.list(libName);
            while (enum.hasMoreElements()) {
                NameClassPair ncPair = (NameClassPair) enum.nextElement();
                String filename = libName + "/" + ncPair.getName();
                if (!filename.endsWith(".jar"))
                    continue;
                tldConfigJar(filename, digester);
            }
        } catch (NamingException e) {
            // Silent catch: it's valid that no /WEB-INF/lib directory exists
        }

    }


    /**
     * Process a TLD (if there is one) in the JAR file (if there is one) at
     * the specified resource path.  Return <code>true</code> if we actually
     * found and processed such a TLD, or <code>false</code> otherwise.
     *
     * @param resourcePath Context-relative resource path
     * @param digester Digester to use for parsing
     */
    private boolean tldConfigJar(String resourcePath, Digester digester) {

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
                synchronized (digester) {
                    digester.push(context);
                    digester.parse(stream);
                }
                stream.close();
                found = true;
            }
            // FIXME jarFile.close();
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
     * @param digester Digester to use for parsing
     */
    private boolean tldConfigTld(String resourcePath, Digester digester) {

        InputStream stream = null;
        try {
            stream =
                context.getServletContext().getResourceAsStream(resourcePath);
            if (stream == null)
                return (false);
            synchronized (digester) {
                digester.push(context);
                digester.parse(stream);
            }
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
