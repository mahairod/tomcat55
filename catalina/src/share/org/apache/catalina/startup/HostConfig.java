/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.startup;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.util.digester.Digester;


/**
 * Startup event listener for a <b>Host</b> that configures the properties
 * of that Host, and the associated defined contexts.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class HostConfig
    implements LifecycleListener {
    
    protected static org.apache.commons.logging.Log log=
         org.apache.commons.logging.LogFactory.getLog( HostConfig.class );

    // ----------------------------------------------------- Instance Variables


    /**
     * App base.
     */
    protected File appBase = null;


    /**
     * Config base.
     */
    protected File configBase = null;


    /**
     * The Java class name of the Context configuration class we should use.
     */
    protected String configClass = "org.apache.catalina.startup.ContextConfig";


    /**
     * The Java class name of the Context implementation we should use.
     */
    protected String contextClass = "org.apache.catalina.core.StandardContext";


    /**
     * The Host we are associated with.
     */
    protected Host host = null;


    /**
     * The string resources for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Should we deploy XML Context config files?
     */
    protected boolean deployXML = false;


    /**
     * Should we unpack WAR files when auto-deploying applications in the
     * <code>appBase</code> directory?
     */
    protected boolean unpackWARs = false;


    /**
     * Map of deployed applications.
     */
    protected HashMap deployed = new HashMap();

    
    /**
     * List of applications which are being serviced, and shouldn't be 
     * deployed/undeployed/redeployed at the moment.
     */
    protected ArrayList serviced = new ArrayList();
    

    /**
     * Attribute value used to turn on/off XML validation
     */
    protected boolean xmlValidation = false;


    /**
     * Attribute value used to turn on/off XML namespace awarenes.
     */
    protected boolean xmlNamespaceAware = false;


    /**
     * The <code>Digester</code> instance used to parse context descriptors.
     */
    protected static Digester digester = createDigester();


    // ------------------------------------------------------------- Properties


    /**
     * Return the Context configuration class name.
     */
    public String getConfigClass() {

        return (this.configClass);

    }


    /**
     * Set the Context configuration class name.
     *
     * @param configClass The new Context configuration class name.
     */
    public void setConfigClass(String configClass) {

        this.configClass = configClass;

    }


    /**
     * Return the Context implementation class name.
     */
    public String getContextClass() {

        return (this.contextClass);

    }


    /**
     * Set the Context implementation class name.
     *
     * @param contextClass The new Context implementation class name.
     */
    public void setContextClass(String contextClass) {

        this.contextClass = contextClass;

    }


    /**
     * Return the deploy XML config file flag for this component.
     */
    public boolean isDeployXML() {

        return (this.deployXML);

    }


    /**
     * Set the deploy XML config file flag for this component.
     *
     * @param deployXML The new deploy XML flag
     */
    public void setDeployXML(boolean deployXML) {

        this.deployXML= deployXML;

    }


    /**
     * Return the unpack WARs flag.
     */
    public boolean isUnpackWARs() {

        return (this.unpackWARs);

    }


    /**
     * Set the unpack WARs flag.
     *
     * @param unpackWARs The new unpack WARs flag
     */
    public void setUnpackWARs(boolean unpackWARs) {

        this.unpackWARs = unpackWARs;

    }
    
    
     /**
     * Set the validation feature of the XML parser used when
     * parsing xml instances.
     * @param xmlValidation true to enable xml instance validation
     */
    public void setXmlValidation(boolean xmlValidation){
        this.xmlValidation = xmlValidation;
    }

    /**
     * Get the server.xml <host> attribute's xmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getXmlValidation(){
        return xmlValidation;
    }

    /**
     * Get the server.xml <host> attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     *
     */
    public boolean getXmlNamespaceAware(){
        return xmlNamespaceAware;
    }


    /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setXmlNamespaceAware(boolean xmlNamespaceAware){
        this.xmlNamespaceAware=xmlNamespaceAware;
    }    


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Host.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        if (event.getType().equals("check"))
            check();

        // Identify the host we are associated with
        try {
            host = (Host) event.getLifecycle();
            if (host instanceof StandardHost) {
                setDeployXML(((StandardHost) host).isDeployXML());
                setUnpackWARs(((StandardHost) host).isUnpackWARs());
                setXmlNamespaceAware(((StandardHost) host).getXmlNamespaceAware());
                setXmlValidation(((StandardHost) host).getXmlValidation());
            }
        } catch (ClassCastException e) {
            log.error(sm.getString("hostConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT))
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();

    }

    
    /**
     * Add a serviced application to the list.
     */
    public synchronized void addServiced(String name) {
        serviced.add(name);
    }
    
    
    /**
     * Is application serviced ?
     * @return state of the application
     */
    public synchronized boolean isServiced(String name) {
        return (serviced.contains(name));
    }
    

    /**
     * Removed a serviced application from the list.
     */
    public synchronized void removeServiced(String name) {
        serviced.remove(name);
    }

    
    /**
     * Get the instant where an application was deployed.
     * @return 0L if no application with that name is deployed, or the instant
     * on which the application was deployed
     */
    public long getDeploymentTime(String name) {
    	DeployedApplication app = (DeployedApplication) deployed.get(name);
    	if (app == null) {
    		return 0L;
    	} else {
    		return app.timestamp;
    	}
    }
    
    
    // ------------------------------------------------------ Protected Methods

    
    /**
     * Create the digester which will be used to parse context config files.
     */
    protected static Digester createDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        // Add object creation rule
        digester.addObjectCreate("Context", "org.apache.catalina.core.StandardContext",
            "className");
        // Set the properties on that object (it doesn't matter if extra 
        // properties are set)
        digester.addSetProperties("Context");
        return (digester);
    }
    

    /**
     * Return a File object representing the "application root" directory
     * for our associated Host.
     */
    protected File appBase() {

        if (appBase != null) {
            return appBase;
        }

        File file = new File(host.getAppBase());
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"),
                            host.getAppBase());
        try {
            appBase = file.getCanonicalFile();
        } catch (IOException e) {
            appBase = file;
        }
        return (appBase);

    }


    /**
     * Return a File object representing the "configuration root" directory
     * for our associated Host.
     */
    protected File configBase() {

        if (configBase != null) {
            return configBase;
        }

        File file = new File(System.getProperty("catalina.base"), "conf");
        Container parent = host.getParent();
        if ((parent != null) && (parent instanceof Engine)) {
            file = new File(file, parent.getName());
        }
        file = new File(file, host.getName());
        try {
            configBase = file.getCanonicalFile();
        } catch (IOException e) {
            configBase = file;
        }
        return (configBase);

    }


    /**
     * Deploy applications for any directories or WAR files that are found
     * in our "application root" directory.
     */
    protected void deployApps() {

        File appBase = appBase();
        File configBase = configBase();
        do {
            // Deploy XML descriptors from configBase
            deployDescriptors(configBase, configBase.list());
            // Deploy expanded folders
            deployDirectories(appBase, appBase.list());
            // Deploy WARs, and loop if additional descriptors are found
        } while (deployWARs(appBase, appBase.list()));

    }


    /**
     * Deploy XML context descriptors.
     */
    protected void deployDescriptors(File configBase, String[] files) {

        if (files == null)
            return;
        
        for (int i = 0; i < files.length; i++) {

            if (files[i].equalsIgnoreCase("META-INF"))
                continue;
            if (files[i].equalsIgnoreCase("WEB-INF"))
                continue;
            File contextXml = new File(configBase, files[i]);
            if (files[i].toLowerCase().endsWith(".xml")) {

                // Calculate the context path and make sure it is unique
                String file = files[i].substring(0, files[i].length() - 4);
                String contextPath = "/" + file.replace('#', '/');
                if (file.equals("ROOT")) {
                    contextPath = "";
                }

                if (deployed.containsKey(contextPath))
                    continue;

                // Assume this is a configuration descriptor and deploy it
                log.debug(sm.getString("hostConfig.deployDescriptor", files[i]));
                try {
                    Context newContext = null;
                    synchronized (digester) {
                        newContext = (Context) digester.parse(contextXml);
                    }
                    if (newContext instanceof Lifecycle) {
                        Class clazz = Class.forName(host.getConfigClass());
                        LifecycleListener listener =
                            (LifecycleListener) clazz.newInstance();
                        ((Lifecycle) newContext).addLifecycleListener(listener);
                    }
                    newContext.setConfigFile(contextXml.getAbsolutePath());
                    newContext.setPath(contextPath);
                    host.addChild(newContext);
                } catch (Throwable t) {
                    log.error(sm.getString("hostConfig.deployDescriptor.error",
                                           files[i]), t);
                }

                deployed.put(contextPath, new DeployedApplication(contextPath));
                // FIXME: populate needed resources, such as the docBase (WAR, expanded)
                // FIXME: populate watched resources
                
            }

        }

    }


    /**
     * Deploy WAR files.
     */
    protected boolean deployWARs(File appBase, String[] files) {

        if (files == null)
            return false;
        
        boolean checkAdditionalDeployments = false;
        
        for (int i = 0; i < files.length; i++) {

            if (files[i].equalsIgnoreCase("META-INF"))
                continue;
            if (files[i].equalsIgnoreCase("WEB-INF"))
                continue;
            File dir = new File(appBase, files[i]);
            if (files[i].toLowerCase().endsWith(".war")) {

                // Calculate the context path and make sure it is unique
                String contextPath = "/" + files[i];
                int period = contextPath.lastIndexOf(".");
                if (period >= 0)
                    contextPath = contextPath.substring(0, period);
                if (contextPath.equals("/ROOT"))
                    contextPath = "";

                if (deployed.containsKey(contextPath))
                    continue;

                if (isUnpackWARs()) {

                    // Expand and deploy this application as a directory
                    log.debug(sm.getString("hostConfig.expand", files[i]));
                    URL url = null;
                    String path = null;
                    try {
                        url = new URL("jar:file:" +
                                      dir.getCanonicalPath() + "!/");
                        path = ExpandWar.expand(host, url);
                        checkAdditionalDeployments = true;
                    } catch (IOException e) {
                        // JAR decompression failure
                        log.warn(sm.getString
                                 ("hostConfig.expand.error", files[i]));
                    } catch (Throwable t) {
                        log.error(sm.getString
                                  ("hostConfig.expand.error", files[i]), t);
                    }
                    
                    // The webapp will actually be deployed when checking the directories
                    
                } else {

                    // FIXME: Don't do that if disabled on Host (sort of "security" feature)

                    // Checking for a nested /META-INF/context.xml
                    JarFile jar = null;
                    JarEntry entry = null;
                    InputStream istream = null;
                    BufferedOutputStream ostream = null;
                    File xml = new File
                        (configBase, files[i].substring
                         (0, files[i].lastIndexOf(".")) + ".xml");
                    if (!xml.exists()) {
                        try {
                            jar = new JarFile(dir);
                            entry = jar.getJarEntry("META-INF/context.xml");
                            if (entry != null) {
                                istream = jar.getInputStream(entry);
                                
                                configBase.mkdirs();
                                
                                ostream =
                                    new BufferedOutputStream
                                    (new FileOutputStream(xml), 1024);
                                byte buffer[] = new byte[1024];
                                while (true) {
                                    int n = istream.read(buffer);
                                    if (n < 0) {
                                        break;
                                    }
                                    ostream.write(buffer, 0, n);
                                }
                                ostream.flush();
                                ostream.close();
                                ostream = null;
                                istream.close();
                                istream = null;
                                entry = null;
                                jar.close();
                                jar = null;
                                //deployDescriptors(configBase(), configBase.list());
                                checkAdditionalDeployments = true;
                                continue;
                            }
                        } catch (Exception e) {
                            // Ignore and continue
                            if (ostream != null) {
                                try {
                                    ostream.close();
                                } catch (Throwable t) {
                                    ;
                                }
                                ostream = null;
                            }
                            if (istream != null) {
                                try {
                                    istream.close();
                                } catch (Throwable t) {
                                    ;
                                }
                                istream = null;
                            }
                        } finally {
                            entry = null;
                            if (jar != null) {
                                try {
                                    jar.close();
                                } catch (Throwable t) {
                                    ;
                                }
                                jar = null;
                            }
                        }
                    }

                    // Deploy the application in this WAR file
                    log.info(sm.getString("hostConfig.deployJar", files[i]));
                    try {
                        Context context = (Context) Class.forName(contextClass).newInstance();
                        if (context instanceof Lifecycle) {
                            Class clazz = Class.forName(host.getConfigClass());
                            LifecycleListener listener =
                                (LifecycleListener) clazz.newInstance();
                            ((Lifecycle) context).addLifecycleListener(listener);
                        }
                        context.setPath(contextPath);
                        context.setDocBase(files[i]);
                        host.addChild(context);
                    } catch (Throwable t) {
                        log.error(sm.getString("hostConfig.deployJar.error",
                                         files[i]), t);
                    }

                    deployed.put(contextPath, new DeployedApplication(contextPath));
                    // FIXME: populate needed resources, such as the docBase (context file, WAR, expanded)
                    // FIXME: populate watched resources

                }

            }

        }
        
        return checkAdditionalDeployments;

    }


    /**
     * Deploy directories.
     */
    protected void deployDirectories(File appBase, String[] files) {

        if (files == null)
            return;
        
        for (int i = 0; i < files.length; i++) {

            if (files[i].equalsIgnoreCase("META-INF"))
                continue;
            if (files[i].equalsIgnoreCase("WEB-INF"))
                continue;
            File dir = new File(appBase, files[i]);
            if (dir.isDirectory()) {

                // Make sure there is an application configuration directory
                // This is needed if the Context appBase is the same as the
                // web server document root to make sure only web applications
                // are deployed and not directories for web space.
                File webInf = new File(dir, "/WEB-INF");
                if (!webInf.exists() || !webInf.isDirectory() ||
                    !webInf.canRead())
                    continue;

                // Calculate the context path and make sure it is unique
                String contextPath = "/" + files[i];
                if (files[i].equals("ROOT"))
                    contextPath = "";

                if (deployed.containsKey(contextPath))
                    continue;

                // Deploy the application in this directory
                if( log.isDebugEnabled() ) 
                    log.debug(sm.getString("hostConfig.deployDir", files[i]));
                long t1=System.currentTimeMillis();
                try {
                    Context context = (Context) Class.forName(contextClass).newInstance();
                    if (context instanceof Lifecycle) {
                        Class clazz = Class.forName(host.getConfigClass());
                        LifecycleListener listener =
                            (LifecycleListener) clazz.newInstance();
                        ((Lifecycle) context).addLifecycleListener(listener);
                    }
                    context.setPath(contextPath);
                    context.setDocBase(files[i]);
                    // FIXME: Don't set that if disabled on Host (sort of "security" feature)
                    context.setConfigFile((new File(dir, "META-INF/context.xml")).getAbsolutePath());
                    host.addChild(context);
                } catch (Throwable t) {
                    log.error(sm.getString("hostConfig.deployDir.error", files[i]),
                        t);
                }
                long t2=System.currentTimeMillis();
                if( log.isDebugEnabled() && (t2-t1) > 200 )
                    log.debug("Deployed " + files[i] + " " + (t2-t1));

                deployed.put(contextPath, new DeployedApplication(contextPath));
                // FIXME: populate needed resources, such as the docBase (context file, WAR, expanded)
                // FIXME: populate watched resources
            
            }

        }

    }


    /**
     * Check resources for redeployment and reloading.
     */
    protected synchronized void checkResources(DeployedApplication app) {
        if (isServiced(app.name))
            return;
        String[] resources = (String[]) app.redeployResources.toArray(new String[0]);
        for (int i = 0; i < resources.length; i++) {
            File resource = new File(resources[i]);
            if (resource.exists()) { 
                if (resource.lastModified() > app.timestamp) {
                    // Redeploy application
                    host.removeChild(host.findChild(app.name));
                    // FIXME: Remove other redeploy resources; need hack here to remove expanded
                    // folder if updated resource is a WAR
                    deployed.remove(app);
                    return;
                }
            } else {
                // Undeploy application
                host.removeChild(host.findChild(app.name));
                // FIXME: Delete all redeploy resources
                deployed.remove(app);
                return;
            }
        }
        resources = (String[]) app.reloadResources.toArray(new String[0]);
        for (int i = 0; i < resources.length; i++) {
            File resource = new File(resources[i]);
            if ((!resource.exists()) || (resource.lastModified() > app.timestamp)) {
                // Reload application
                Container context = host.findChild(app.name);
                try {
                    ((Lifecycle) context).stop();
                } catch (Exception e) {
                    log.warn(sm.getString
                             ("hostConfig.context.restart", app.name), e);
                }
                // If the context was not started (for example an error 
                // in web.xml) we'll still get to try to start
                try {
                    ((Lifecycle) context).start();
                } catch (Exception e) {
                    log.warn(sm.getString
                             ("hostConfig.context.restart", app.name), e);
                }
                app.timestamp = System.currentTimeMillis();
                return;
            }
        }
    }
    
    
    /**
     * Process a "start" event for this Host.
     */
    public void start() {

        if (log.isDebugEnabled())
            log.debug(sm.getString("hostConfig.start"));

        deployApps();
        
    }


    /**
     * Process a "stop" event for this Host.
     */
    public void stop() {

        if (log.isDebugEnabled())
            log.debug(sm.getString("hostConfig.stop"));

        undeployApps();

        appBase = null;
        configBase = null;

    }


    /**
     * Undeploy all deployed applications.
     */
    protected void undeployApps() {

        if (log.isDebugEnabled())
            log.debug(sm.getString("hostConfig.undeploying"));

        // Soft undeploy all contexts we have deployed
        DeployedApplication[] apps = 
            (DeployedApplication[]) deployed.values().toArray(new DeployedApplication[0]);
        for (int i = 0; i < apps.length; i++) {
            host.removeChild(host.findChild(apps[i].name));
        }
        
        deployed.clear();

    }


    /**
     * Deploy webapps.
     */
    protected void check() {

        if (host.getAutoDeploy()) {
            // Check for resources modification to trigger redeployment
            DeployedApplication[] apps = 
                (DeployedApplication[]) deployed.values().toArray(new DeployedApplication[0]);
            for (int i = 0; i < apps.length; i++) {
                checkResources(apps[i]);
            }
            // Hotdeploy applications
            deployApps();
        }

    }

    
    // ----------------------------------------------------- Instance Variables


    /**
     * This class represents the state of a deployed application, as well as 
     * the monitored resources.
     */
    protected class DeployedApplication {
    	public DeployedApplication(String name) {
    		this.name = name;
    	}
    	
    	/**
    	 * Application context path. The assertion is that 
    	 * (host.getChild(name) != null).
    	 */
    	public String name;
    	
    	/**
    	 * Any modification of the specified (static) resources will cause a 
    	 * redeployment of the application. If any of the specified resources is
    	 * removed, the application will be undeployed. Typically, this will
    	 * contain resources like the context.xml file, a compressed WAR path.
    	 */
    	public ArrayList redeployResources = new ArrayList();

    	/**
    	 * Any modification of the specified (static) resources will cause a 
    	 * reload of the application. This will typically contain resources
    	 * such as the web.xml of a webapp, but can be configured to contain
    	 * additional descriptors.
    	 */
    	public ArrayList reloadResources = new ArrayList();

    	/**
    	 * Instant where the application was last put in service.
    	 */
    	public long timestamp = System.currentTimeMillis();
    }

}
