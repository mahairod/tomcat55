/*
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
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.SchemaResolver;
import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision$ $Date$
 */

public final class ContextConfig
    implements LifecycleListener {

    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( ContextConfig.class );

    // ----------------------------------------------------- Instance Variables

    /**
     * The set of Authenticators that we know how to configure.  The key is
     * the name of the implemented authentication method, and the value is
     * the fully qualified Java class name of the corresponding Valve.
     */
    private static Properties authenticators = null;


    /**
     * The Context we are associated with.
     */
    private Context context = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;

    
    /**
     * The default web application's deployment descriptor location.
     */
    private String defaultWebXml = null;
    
    
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
    
    
    /**
     * The <code>Rule</code> used to parse the web.xml
     */
    private static WebRuleSet webRuleSet = new WebRuleSet();

    /**
     * Attribute value used to turn on/off XML validation
     */
     private static boolean xmlValidation = false;


    /**
     * Attribute value used to turn on/off XML namespace awarenes.
     */
    private static boolean xmlNamespaceAware = false;

        
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
    
    
    /**
     * Return the location of the default deployment descriptor
     */
    public String getDefaultWebXml() {
        if( defaultWebXml == null ) defaultWebXml=Constants.DefaultWebXml;
        return (this.defaultWebXml);

    }


    /**
     * Set the location of the default deployment descriptor
     *
     * @param path Absolute/relative path to the default web.xml
     */
    public void setDefaultWebXml(String path) {

        this.defaultWebXml = path;

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
//             if (context instanceof StandardContext) {
//                 int contextDebug = ((StandardContext) context).getDebug();
//                 if (contextDebug > this.debug)
//                     this.debug = contextDebug;
//             }
        } catch (ClassCastException e) {
            log.error(sm.getString("contextConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Called from ContainerBase.addChild() -> StandardContext.start()
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
            log.error(sm.getString("contextConfig.applicationMissing") + " " + context);
            return;
        }
        
        long t1=System.currentTimeMillis();

        if (webDigester == null){
            webDigester = createWebDigester();
        }
        
        URL url=null;
        // Process the application web.xml file
        synchronized (webDigester) {
            try {
                url =
                    servletContext.getResource(Constants.ApplicationWebXml);
                if( url!=null ) {
                    InputSource is = new InputSource(url.toExternalForm());
                    is.setByteStream(stream);
                    webDigester.setDebug(getDebug());
                    if (context instanceof StandardContext) {
                        ((StandardContext) context).setReplaceWelcomeFiles(true);
                    }
                    webDigester.clear();
//                    ClassLoader cl=Thread.currentThread().getContextClassLoader();
//                    if( cl!=null )
//                        webDigester.setClassLoader(cl);
                    webDigester.setUseContextClassLoader(true);
                    webDigester.push(context);
                    webDigester.parse(is);
                } else {
                    log.info("No web.xml, using defaults " + context );
                }
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.applicationParse"), e);
                log.error(sm.getString("contextConfig.applicationPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.applicationParse"), e);
                ok = false;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.applicationClose"), e);
                }
            }
        }
        webRuleSet.recycle();

        long t2=System.currentTimeMillis();
        if (context instanceof StandardContext) {
            ((StandardContext) context).setStartupTime(t2-t1);
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
            log.error(sm.getString("contextConfig.missingRealm"));
            ok = false;
            return;
        }

        // Load our mapping properties if necessary
        if (authenticators == null) {
            try {
                InputStream is=this.getClass().getClassLoader().getResourceAsStream("org/apache/catalina/startup/Authenticators.properties");
                if( is!=null ) {
                    authenticators = new Properties();
                    authenticators.load(is);
                } else {
                    log.error(sm.getString("contextConfig.authenticatorResources"));
                    ok=false;
                    return;
                }
            } catch (IOException e) {
                log.error(sm.getString("contextConfig.authenticatorResources"), e);
                ok = false;
                return;
            }
        }

        // Identify the class name of the Valve we should configure
        String authenticatorName = null;
        authenticatorName =
                authenticators.getProperty(loginConfig.getAuthMethod());
        if (authenticatorName == null) {
            log.error(sm.getString("contextConfig.authenticatorMissing",
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
                    if( log.isDebugEnabled() )
                        log.debug(sm.getString("contextConfig.authenticatorConfigured",
                                     loginConfig.getAuthMethod()));
                }
            }
        } catch (Throwable t) {
            log.error(sm.getString("contextConfig.authenticatorInstantiate",
                             authenticatorName), t);
            ok = false;
        }

    }


    /**
     * Create and deploy a Valve to expose the SSL certificates presented
     * by this client, if any.  If we cannot instantiate such a Valve
     * (because the JSSE classes are not available), silently continue.
     * This is only instantiated for those Contexts being served by
     * a Connector with secure set to true.
     */
    private void certificatesConfig() {

        // Only install this valve if there is a Connector installed
        // which has secure set to true.
        boolean secure = false;
        Container container = context.getParent();
        if (container instanceof Host) {
            xmlValidation = ((Host)container).getXmlValidation();
            xmlNamespaceAware = ((Host)container).getXmlNamespaceAware();
            container = container.getParent();
        }
        if (container instanceof Engine) {
            Service service = ((Engine)container).getService();
            // The service can be null when Tomcat is run in embedded mode
            if (service == null) {
                secure = true;
            } else {
                Connector [] connectors = service.findConnectors();
                for (int i = 0; i < connectors.length; i++) {
                    secure = connectors[i].getSecure();
                    if (secure) {
                        break;
                    }
                }
            }
        }
        if (!secure) {
            return;
        }

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
                    log.error(sm.getString
                        ("contextConfig.certificatesConfig.added"));
                    
                }
            }
        } catch (Throwable t) {
            log.error(sm.getString("contextConfig.certificatesConfig.error"), t);
            ok = false;
        }

    }


    /**
     * Create (if necessary) and return a Digester configured to process a tag
     * library descriptor, looking for additional listener classes to be
     * registered.
     */
    private static Digester createTldDigester() {

        URL url = null;
        Digester tldDigester = new Digester();
        tldDigester.setNamespaceAware(xmlNamespaceAware);
        tldDigester.setValidating(xmlValidation);
        
        if (tldDigester.getFactory().getClass().getName().indexOf("xerces")!=-1) {
            tldDigester = patchXerces(tldDigester);
        }
        // Set the schemaLocation
        url = ContextConfig.class.getResource(Constants.TldSchemaResourcePath_20);
        SchemaResolver tldEntityResolver = new SchemaResolver(url.toString(), 
                                                              tldDigester);
        if( xmlValidation ) {
            tldDigester.setSchema(url.toString());
        }
        
        url = ContextConfig.class.getResource(Constants.TldDtdResourcePath_11);
        tldEntityResolver.register(Constants.TldDtdPublicId_11,
                                   url.toString());
        
        url = ContextConfig.class.getResource(Constants.TldDtdResourcePath_12);
        tldEntityResolver.register(Constants.TldDtdPublicId_12,
                                   url.toString());
        
        tldEntityResolver = registerLocalSchema(tldEntityResolver);
        
        tldDigester.setEntityResolver(tldEntityResolver);
        tldDigester.addRuleSet(new TldRuleSet());
        return (tldDigester);

    }

    
    private static Digester patchXerces(Digester digester){
        // This feature is needed for backward compatibility with old DDs
        // which used Java encoding names such as ISO8859_1 etc.
        // with Crimson (bug 4701993). By default, Xerces does not
        // support ISO8859_1.
        try{
            digester.setFeature(
                "http://apache.org/xml/features/allow-java-encodings", true);
        } catch(ParserConfigurationException e){
                // log("contextConfig.registerLocalSchema", e);
        } catch(SAXNotRecognizedException e){
                // log("contextConfig.registerLocalSchema", e);
        } catch(SAXNotSupportedException e){
                // log("contextConfig.registerLocalSchema", e);
        }
        return digester;
    }
    

    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    private static Digester createWebDigester() {
        URL url = null;
        Digester webDigester = new Digester();
        webDigester.setNamespaceAware(xmlNamespaceAware);
        webDigester.setValidating(xmlValidation);
       
        if (webDigester.getFactory().getClass().getName().indexOf("xerces")!=-1) {
            webDigester = patchXerces(webDigester);
        }
        
        url = ContextConfig.class.getResource(Constants.WebSchemaResourcePath_24);
        SchemaResolver webEntityResolver = new SchemaResolver(url.toString(),
                                                              webDigester);
        if( xmlValidation ) {
            webDigester.setSchema(url.toString());
        }

        url = ContextConfig.class.getResource(Constants.WebDtdResourcePath_22);
        webEntityResolver.register(Constants.WebDtdPublicId_22,
                                   url.toString());
        
        url = ContextConfig.class.getResource(Constants.WebDtdResourcePath_23);
        webEntityResolver.register(Constants.WebDtdPublicId_23,
                                   url.toString());

        webEntityResolver = registerLocalSchema(webEntityResolver);

        webDigester.setEntityResolver(webEntityResolver);
        webDigester.addRuleSet(webRuleSet);
        return (webDigester);
    }

    private String getBaseDir() {
        Container engineC=context.getParent().getParent();
        if( engineC instanceof StandardEngine ) {
            return ((StandardEngine)engineC).getBaseDir();
        }
        return System.getProperty("catalina.base");
    }

    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    private void defaultConfig() {
        long t1=System.currentTimeMillis();

        // Open the default web.xml file, if it exists
        if( defaultWebXml==null && context instanceof StandardContext ) {
            defaultWebXml=((StandardContext)context).getDefaultWebXml();
        }
        // set the default if we don't have any overrides
        if( defaultWebXml==null ) getDefaultWebXml();

        File file = new File(this.defaultWebXml);
        if (!file.isAbsolute()) {
            file = new File(getBaseDir(),
                            this.defaultWebXml);
        }

        InputStream stream = null;
        InputSource source = null;

        try {
            if ( ! file.exists() ) {
                // Use getResource and getResourceAsStream
                stream = getClass().getClassLoader()
                    .getResourceAsStream(defaultWebXml);
                source = new InputSource
                    (getClass().getClassLoader()
                     .getResource(defaultWebXml).toString());
            } else {
                source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.defaultMissing") 
                      + " " + defaultWebXml + " " + file , e);
            return;
        }

        if (webDigester == null){
            webDigester = createWebDigester();
        }
        
        // Process the default web.xml file
        synchronized (webDigester) {
            try {
                source.setByteStream(stream);
                webDigester.setDebug(getDebug());
                
                if (context instanceof StandardContext)
                    ((StandardContext) context).setReplaceWelcomeFiles(true);
                webDigester.clear();
                webDigester.setClassLoader(this.getClass().getClassLoader());
                //log.info( "Using cl: " + webDigester.getClassLoader());
                webDigester.setUseContextClassLoader(false);
                webDigester.push(context);
                webDigester.parse(source);
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                log.error(sm.getString("contextConfig.defaultPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                ok = false;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.defaultClose"), e);
                }
            }
        }
        webRuleSet.recycle();
        
        long t2=System.currentTimeMillis();
        if( (t2-t1) > 200 )
            log.debug("Processed default web.xml " + file + " "  + ( t2-t1));
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
            log.info( message );
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
            log.error( message, throwable );
        }

    }

    /**
     * Utilities used to force the parser to use local schema, when available,
     * instead of the <code>schemaLocation</code> XML element.
     * @param digester The instance on which properties are set.
     * @return an instance ready to parse XML schema.
     */
    protected static SchemaResolver registerLocalSchema(SchemaResolver entityResolver){

        URL url = ContextConfig.class.getResource(Constants.J2eeSchemaResourcePath_14);
        entityResolver.register(Constants.J2eeSchemaPublicId_14,
                                url.toString());

        url = ContextConfig.class.getResource(Constants.W3cSchemaResourcePath_10);
        entityResolver.register(Constants.W3cSchemaPublicId_10,
                                url.toString());

        url = ContextConfig.class.getResource(Constants.JspSchemaResourcePath_20);
        entityResolver.register(Constants.JspSchemaPublicId_20,
                                url.toString());

        url = ContextConfig.class.getResource(Constants.TldSchemaResourcePath_20);
        entityResolver.register(Constants.TldSchemaPublicId_20,
                                url.toString());
        
        url = ContextConfig.class.getResource(Constants.WebSchemaResourcePath_24);
        entityResolver.register(Constants.WebSchemaPublicId_24,
                                url.toString());
        
        url = ContextConfig.class.getResource(Constants.J2eeWebServiceSchemaResourcePath_11);
        entityResolver.register(Constants.J2eeWebServiceSchemaPublicId_11,
                                url.toString());

        url = ContextConfig.class.getResource(Constants.J2eeWebServiceClientSchemaResourcePath_11);
        entityResolver.register(Constants.J2eeWebServiceClientSchemaPublicId_11,
                                url.toString());
        
        return entityResolver;
    }


    /**
     * Process a "start" event for this Context - in background
     */
    private synchronized void start() {
        // Called from StandardContext.start()

        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.start"));
        context.setConfigured(false);
        ok = true;

        // Set properties based on DefaultContext
        Container container = context.getParent();
        if( !context.getOverride() ) {
            if( container instanceof Host ) {
                ((Host)container).importDefaultContext(context);
                xmlValidation = ((Host)container).getXmlValidation();
                xmlNamespaceAware = ((Host)container).getXmlNamespaceAware();

                container = container.getParent();
            }
            if( container instanceof Engine ) {
                ((Engine)container).importDefaultContext(context);
            }
        }

        // Process the default and application web.xml files
        defaultConfig();
        applicationConfig();
        if (ok) {
            validateSecurityRoles();
        }

        // Scan tag library descriptor files for additional listener classes
        if (ok) {
            try {
                tldScan();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                ok = false;
            }
        }


        // Configure a certificates exposer valve, if required
        if (ok)
            certificatesConfig();

        // Configure an authenticator if we need one
        if (ok)
            authenticatorConfig();

        // Dump the contents of this pipeline if requested
        if ((log.isDebugEnabled()) && (context instanceof ContainerBase)) {
            log.debug("Pipline Configuration:");
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            Valve valves[] = null;
            if (pipeline != null)
                valves = pipeline.getValves();
            if (valves != null) {
                for (int i = 0; i < valves.length; i++) {
                    log.debug("  " + valves[i].getInfo());
                }
            }
            log.debug("======================");
        }

        // Make our application available if no problems were encountered
        if (ok)
            context.setConfigured(true);
        else {
            log.error(sm.getString("contextConfig.unavailable"));
            context.setConfigured(false);
        }

    }


    /**
     * Process a "stop" event for this Context.
     */
    private synchronized void stop() {

        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.stop"));

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
        /*
        ContextEjb[] contextEjbs = context.findEjbs();
        for (i = 0; i < contextEjbs.length; i++) {
            context.removeEjb(contextEjbs[i].getName());
        }
        */

        // Removing environments
        /*
        ContextEnvironment[] contextEnvironments = context.findEnvironments();
        for (i = 0; i < contextEnvironments.length; i++) {
            context.removeEnvironment(contextEnvironments[i].getName());
        }
        */

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
        /*
        ContextLocalEjb[] contextLocalEjbs = context.findLocalEjbs();
        for (i = 0; i < contextLocalEjbs.length; i++) {
            context.removeLocalEjb(contextLocalEjbs[i].getName());
        }
        */

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
        /*
        String[] resourceEnvRefs = context.findResourceEnvRefs();
        for (i = 0; i < resourceEnvRefs.length; i++) {
            context.removeResourceEnvRef(resourceEnvRefs[i]);
        }
        */

        // Removing resource links
        /*
        ContextResourceLink[] contextResourceLinks =
            context.findResourceLinks();
        for (i = 0; i < contextResourceLinks.length; i++) {
            context.removeResourceLink(contextResourceLinks[i].getName());
        }
        */

        // Removing resources
        /*
        ContextResource[] contextResources = context.findResources();
        for (i = 0; i < contextResources.length; i++) {
            context.removeResource(contextResources[i].getName());
        }
        */

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
     * Scan for and configure all tag library descriptors found in this
     * web application.
     *
     * @exception Exception if a fatal input/output or parsing error occurs
     */
    private void tldScan() throws Exception {
        long t1=System.currentTimeMillis();

        // Acquire this list of TLD resource paths to be processed
        Set resourcePaths = tldScanResourcePaths();

        // Scan each accumulated resource paths for TLDs to be processed
        Iterator paths = resourcePaths.iterator();
        while (paths.hasNext()) {
            String path = (String) paths.next();
            if (path.endsWith(".jar")) {
                tldScanJar(path);
            } else {
                tldScanTld(path);
            }
        }
        long t2=System.currentTimeMillis();
        if( context instanceof StandardContext ) {
            ((StandardContext)context).setTldScanTime(t2-t1);
        }

    }


    /**
     * Scan the JAR file at the specified resource path for TLDs in the
     * <code>META-INF</code> subdirectory, and scan them for application
     * event listeners that need to be registered.
     *
     * @param resourcePath Resource path of the JAR file to scan
     *
     * @exception Exception if an exception occurs while scanning this JAR
     */
    private void tldScanJar(String resourcePath) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug(" Scanning JAR at resource path '" + resourcePath + "'");
        }

        JarFile jarFile = null;
        String name = null;
        InputStream inputStream = null;
        try {
            URL url = context.getServletContext().getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException
                    (sm.getString("contextConfig.tldResourcePath",
                                  resourcePath));
            }
            url = new URL("jar:" + url.toString() + "!/");
            JarURLConnection conn =
                (JarURLConnection) url.openConnection();
            conn.setUseCaches(false);
            jarFile = conn.getJarFile();
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                name = entry.getName();
                if (!name.startsWith("META-INF/")) {
                    continue;
                }
                if (!name.endsWith(".tld")) {
                    continue;
                }
                if (log.isTraceEnabled()) {
                    log.trace("  Processing TLD at '" + name + "'");
                }
                inputStream = jarFile.getInputStream(entry);
                tldScanStream(inputStream);
                inputStream.close();
                inputStream = null;
                name = null;
            }
            // FIXME - Closing the JAR file messes up the class loader???
            //            jarFile.close();
        } catch (Exception e) {
            // XXX Why do we wrap it ? The signature is 'throws Exception'
            if (name == null) {
                throw new ServletException
                    (sm.getString("contextConfig.tldJarException",
                                  resourcePath, context.getPath()), e);
            } else {
                throw new ServletException
                    (sm.getString("contextConfig.tldEntryException",
                                  name, resourcePath, context.getPath()), e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable t) {
                    ;
                }
                inputStream = null;
            }
            if (jarFile != null) {
            // FIXME - Closing the JAR file messes up the class loader???
            //                try {
            //                    jarFile.close();
            //                } catch (Throwable t) {
            //                    ;
            //                }
                jarFile = null;
            }
        }

    }


    /**
     * Scan the TLD contents in the specified input stream, and register
     * any application event listeners found there.  <b>NOTE</b> - It is
     * the responsibility of the caller to close the InputStream after this
     * method returns.
     *
     * @param resourceStream InputStream containing a tag library descriptor
     *
     * @exception Exception if an exception occurs while scanning this TLD
     */
    private void tldScanStream(InputStream resourceStream)
        throws Exception {

        if (tldDigester == null){
            tldDigester = createTldDigester();
        }
        
        synchronized (tldDigester) {
            tldDigester.clear();
            tldDigester.push(context);
            tldDigester.parse(resourceStream);
        }

    }


    /**
     * Scan the TLD contents at the specified resource path, and register
     * any application event listeners found there.
     *
     * @param resourcePath Resource path being scanned
     *
     * @exception Exception if an exception occurs while scanning this TLD
     */
    private void tldScanTld(String resourcePath) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug(" Scanning TLD at resource path '" + resourcePath + "'");
        }

        InputStream inputStream = null;
        try {
            inputStream =
                context.getServletContext().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException
                    (sm.getString("contextConfig.tldResourcePath",
                                  resourcePath));
            }
            tldScanStream(inputStream);
            inputStream.close();
            inputStream = null;
        } catch (Exception e) {
             throw new ServletException
                 (sm.getString("contextConfig.tldFileException", resourcePath, context.getPath()),
                  e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable t) {
                    ;
                }
                inputStream = null;
            }
        }

    }


    /**
     * Accumulate and return a Set of resource paths to be analyzed for
     * tag library descriptors.  Each element of the returned set will be
     * the context-relative path to either a tag library descriptor file,
     * or to a JAR file that may contain tag library descriptors in its
     * <code>META-INF</code> subdirectory.
     *
     * @exception IOException if an input/output error occurs while
     *  accumulating the list of resource paths
     */
    private Set tldScanResourcePaths() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(" Accumulating TLD resource paths");
        }
        Set resourcePaths = new HashSet();

        // Accumulate resource paths explicitly listed in the web application
        // deployment descriptor
        if (log.isTraceEnabled()) {
            log.trace("  Scanning <taglib> elements in web.xml");
        }
        String taglibs[] = context.findTaglibs();
        for (int i = 0; i < taglibs.length; i++) {
            String resourcePath = context.findTaglib(taglibs[i]);
            // FIXME - Servlet 2.4 DTD implies that the location MUST be
            // a context-relative path starting with '/'?
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/WEB-INF/" + resourcePath;
            }
            if (log.isTraceEnabled()) {
                log.trace("   Adding path '" + resourcePath +
                    "' for URI '" + taglibs[i] + "'");
            }
            resourcePaths.add(resourcePath);
        }

        // Scan TLDs in the /WEB-INF subdirectory of the web application
        if (log.isTraceEnabled()) {
            log.trace("  Scanning TLDs in /WEB-INF subdirectory");
        }
        DirContext resources = context.getResources();
        if( resources!=null ) {
            try {
                NamingEnumeration items = resources.list("/WEB-INF");
                while (items.hasMoreElements()) {
                    NameClassPair item = (NameClassPair) items.nextElement();
                    String resourcePath = "/WEB-INF/" + item.getName();
                    // FIXME - JSP 2.0 is not explicit about whether we should
                    // scan subdirectories of /WEB-INF for TLDs also
                    if (!resourcePath.endsWith(".tld")) {
                        continue;
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("   Adding path '" + resourcePath + "'");
                    }
                    resourcePaths.add(resourcePath);
                }
            } catch (NamingException e) {
                ; // Silent catch: it's valid that no /WEB-INF directory exists
            }
        } else {
            log.info("No resource " + context + " " + context.getClass());
        }

        // Scan JARs in the /WEB-INF/lib subdirectory of the web application
        if (log.isTraceEnabled()) {
            log.trace("  Scanning JARs in /WEB-INF/lib subdirectory");
        }
        try {
            NamingEnumeration items = resources.list("/WEB-INF/lib");
            while (items.hasMoreElements()) {
                NameClassPair item = (NameClassPair) items.nextElement();
                String resourcePath = "/WEB-INF/lib/" + item.getName();
                if (!resourcePath.endsWith(".jar")) {
                    continue;
                }
                if (log.isTraceEnabled()) {
                    log.trace("   Adding path '" + resourcePath + "'");
                }
                resourcePaths.add(resourcePath);
            }
        } catch (NamingException e) {
            ; // Silent catch: it's valid that no /WEB-INF/lib directory exists
        }

        // Return the completed set
        return (resourcePaths);

    }


    /**
     * Validate the usage of security role names in the web application
     * deployment descriptor.  If any problems are found, issue warning
     * messages (for backwards compatibility) and add the missing roles.
     * (To make these problems fatal instead, simply set the <code>ok</code>
     * instance variable to <code>false</code> as well).
     */
    private void validateSecurityRoles() {

        // Check role names used in <security-constraint> elements
        SecurityConstraint constraints[] = context.findConstraints();
        for (int i = 0; i < constraints.length; i++) {
            String roles[] = constraints[i].findAuthRoles();
            for (int j = 0; j < roles.length; j++) {
                if (!"*".equals(roles[j]) &&
                    !context.findSecurityRole(roles[j])) {
                    log.info(sm.getString("contextConfig.role.auth", roles[j]));
                    context.addSecurityRole(roles[j]);
                }
            }
        }

        // Check role names used in <servlet> elements
        Container wrappers[] = context.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            String runAs = wrapper.getRunAs();
            if ((runAs != null) && !context.findSecurityRole(runAs)) {
                log.info(sm.getString("contextConfig.role.runas", runAs));
                context.addSecurityRole(runAs);
            }
            String names[] = wrapper.findSecurityReferences();
            for (int j = 0; j < names.length; j++) {
                String link = wrapper.findSecurityReference(names[j]);
                if ((link != null) && !context.findSecurityRole(link)) {
                    log.info(sm.getString("contextConfig.role.link", link));
                    context.addSecurityRole(link);
                }
            }
        }

    }

}
