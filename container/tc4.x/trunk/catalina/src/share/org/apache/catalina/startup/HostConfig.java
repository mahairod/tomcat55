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


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.util.StringManager;


/**
 * Startup event listener for a <b>Host</b> that configures the properties
 * of that Host, and the associated defined contexts.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class HostConfig
    implements LifecycleListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Java class name of the Context configuration class we should use.
     */
    private String configClass = "org.apache.catalina.startup.ContextConfig";


    /**
     * The Java class name of the Context implementation we should use.
     */
    private String contextClass = "org.apache.catalina.core.StandardContext";


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The Host we are associated with.
     */
    private Host host = null;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
	StringManager.getManager(Constants.Package);


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
     * Process the START event for an associated Host.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

	// Identify the host we are associated with
	try {
	    host = (Host) event.getLifecycle();
	} catch (ClassCastException e) {
	    log(sm.getString("hostConfig.cce", event.getLifecycle()), e);
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
     * Return a File object representing the "application root" directory
     * for our associated Host.
     */
    private File appBase() {

	File file = new File(host.getAppBase());
	if (!file.isAbsolute())
	    file = new File(System.getProperty("catalina.home"),
	    		    host.getAppBase());
	return (file);

    }


    /**
     * Deploy any directories found in our "application root" directory that
     * look like they contain a web application.  For the purposes of this
     * method, the directory must contain a WEB-INF subdirectory that contains
     * a web application deployment descriptor file (<code>web.xml</code>).
     */
    private void deploy() {

	if (debug >= 1)
	    log(sm.getString("hostConfig.deploying"));

	// Discover and deploy web application directories as necessary
	File appBase = appBase();
	if (!appBase.exists() || !appBase.isDirectory())
	    return;
	String files[] = appBase.list();

	for (int i = 0; i < files.length; i++) {

	    File dir = new File(appBase, files[i]);
	    if (!dir.isDirectory())
	        continue;
	    File webXml = new File(dir, "/WEB-INF/web.xml");
	    if (!webXml.exists() || !webXml.isFile() ||
	        !webXml.canRead())
	        continue;
	    String contextPath = "/" + files[i];
	    if (files[i].equals("ROOT"))
	        contextPath = "";
	    if (host.findChild(contextPath) != null)
	        continue;

	    log(sm.getString("hostConfig.deploy", files[i]));
	    try {
		Class clazz = Class.forName(contextClass);
		Context context =
		  (Context) clazz.newInstance();
		context.setPath(contextPath);
		context.setDocBase(files[i]);
		if (context instanceof Lifecycle) {
		    clazz = Class.forName(configClass);
		    LifecycleListener listener =
		      (LifecycleListener) clazz.newInstance();
		    ((Lifecycle) context).addLifecycleListener(listener);
		}
		host.addChild(context);
	    } catch (Exception e) {
		log(sm.getString("hostConfig.deploy.error", files[i]), e);
	    }

	}

    }


    /**
     * Expand any JAR files found in our "application root" directory that
     * do not have a corresponding directory without the ".jar" extension.
     */
    private void expand() {

	if (debug >= 1)
	    log(sm.getString("hostConfig.expanding"));

	// Discover and expand WAR files as necessary
	File appBase = appBase();
	if (!appBase.exists() || !appBase.isDirectory())
	    return;
	String files[] = appBase.list();

	for (int i = 0; i < files.length; i++) {

	    // Is this file a WAR that needs to be expanded?
	    File file = new File(appBase, files[i]);
	    if (file.isDirectory())
		continue;
	    String filename = files[i].toLowerCase();
	    if (!filename.endsWith(".war"))
		continue;

	    // Has this WAR been expanded previously?
	    File dir = new File(appBase,
				files[i].substring(0, files[i].length() - 4));
	    if (dir.exists())
		continue;

	    log(sm.getString("hostConfig.expand", files[i]));
	    JarFile jarFile = null;
	    try {
		dir.mkdirs();
		jarFile = new JarFile(new File(appBase, files[i]));
		Enumeration jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
		    JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
		    String name = jarEntry.getName();
		    int slash = name.lastIndexOf("/");
		    if (slash >= 0) {
			File parent = new File(dir,
					       name.substring(0, slash));
			if (debug >= 2)
			    log(" Creating parent directory " + parent);
			parent.mkdirs();
		    }
		    if (name.endsWith("/"))
		        continue;
		    if (debug >= 2)
		        log(" Creating expanded file " + name);
		    InputStream input = jarFile.getInputStream(jarEntry);
		    expand(input, dir, name);
		}
	    } catch (Exception e) {
		log(sm.getString("hostConfig.expand.error", files[i]), e);
		if (jarFile != null) {
		    try {
			jarFile.close();
		    } catch (Exception f) {
			;
		    }
		}
	    }

	}

    }


    /**
     * Expand the specified input stream into the specified directory, into
     * a file named from the specified relative filename path.
     *
     * @param input InputStream to be copied
     * @param directory Base directory into which the file is created
     * @param path Relative pathname of the file to be created
     *
     * @exception IOException if any processing exception occurs
     */
    private void expand(InputStream input, File directory, String path)
        throws IOException {

	File file = new File(directory, path);
	BufferedOutputStream output =
	  new BufferedOutputStream(new FileOutputStream(file));
	byte buffer[] = new byte[2048];
	while (true) {
	    int n = input.read(buffer);
	    if (n <= 0)
	        break;
	    output.write(buffer, 0, n);
	}
	output.close();
	input.close();

    }


    /**
     * Log a message on the Logger associated with our Host (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	Logger logger = null;
	if (host != null)
	    logger = host.getLogger();
	if (logger != null)
	    logger.log("HostConfig[" + host.getName() + "]: " + message);
	else
	    System.out.println("HostConfig[" + host.getName() + "]: "
			       + message);

    }


    /**
     * Log a message on the Logger associated with our Host (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

	Logger logger = null;
	if (host != null)
	    logger = host.getLogger();
	if (logger != null)
	    logger.log("HostConfig[" + host.getName() + "] "
		       + message, throwable);
	else {
	    System.out.println("HostConfig[" + host.getName() + "]: "
			       + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}

    }


    /**
     * Process a "start" event for this Host.
     */
    private void start() {

	if (debug > 0)
	    log(sm.getString("hostConfig.start"));

	expand();
	deploy();

    }


    /**
     * Process a "stop" event for this Host.
     */
    private void stop() {

	if (debug > 0)
	    log(sm.getString("hostConfig.stop"));

    }


}
