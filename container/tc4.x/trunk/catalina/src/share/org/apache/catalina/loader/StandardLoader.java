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


package org.apache.catalina.loader;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of the <b>Loader</b> interface that wraps the
 * <code>AdaptiveClassLoader</code> implementation imported from the
 * Apache JServ project.  This class loader supports detection of modified
 * Java classes, which can be used to implement auto-reload support.
 * <p>
 * This class loader is configured by adding the pathnames of directories,
 * JAR files, and ZIP files with the <code>addRepository()</code> method,
 * prior to calling <code>start()</code>.  When a new class is required,
 * these repositories will be consulted first to locate the class.  If it
 * is not present, the system class loader will be used instead.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class StandardLoader
    implements Lifecycle, Loader, PropertyChangeListener, Runnable {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new StandardLoader with no defined parent class loader
     * (so that the actual parent will be the system class loader).
     */
    public StandardLoader() {

        this(null);

    }


    /**
     * Construct a new StandardLoader with the specified class loader
     * to be defined as the parent of the ClassLoader we ultimately create.
     *
     * @param parent The parent class loader
     */
    public StandardLoader(ClassLoader parent) {


        super();
	this.parentClassLoader = parent;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The number of seconds between checks for modified classes, if
     * automatic reloading is enabled.
     */
    private int checkInterval = 15;


    /**
     * The class loader being managed by this Loader component.
     */
    private Reloader classLoader = null;


    /**
     * The Container with which this Loader has been associated.
     */
    private Container container = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The descriptive information about this Loader implementation.
     */
    private static final String info =
	"org.apache.catalina.loader.StandardLoader/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The Java class name of the ClassLoader implementation to be used.
     * To be useful, this ClassLoader should also implement the
     * <code>Reloader</code> interface.
     */
    private String loaderClass =
        //	"org.apache.catalina.loader.FileClassLoader";
	"org.apache.catalina.loader.StandardClassLoader";


    /**
     * The parent class loader of the class loader we will create.
     */
    private ClassLoader parentClassLoader = null;


    /**
     * The reloadable flag for this Loader.
     */
    private boolean reloadable = false;


    /**
     * The set of repositories associated with this class loader.
     */
    private String repositories[] = new String[0];


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
	StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    private boolean started = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;


    /**
     * Name to register for the background thread.
     */
    private String threadName = "StandardLoader";


    // ------------------------------------------------------------- Properties


    /**
     * Return the check interval for this Loader.
     */
    public int getCheckInterval() {

	return (this.checkInterval);

    }


    /**
     * Set the check interval for this Loader.
     *
     * @param checkInterval The new check interval
     */
    public void setCheckInterval(int checkInterval) {

        int oldCheckInterval = this.checkInterval;
	this.checkInterval = checkInterval;
	support.firePropertyChange("checkInterval",
				   new Integer(oldCheckInterval),
				   new Integer(this.checkInterval));

    }


    /**
     * Return the Java class loader to be used by this Container.
     */
    public ClassLoader getClassLoader() {

	return ((ClassLoader) classLoader);

    }


    /**
     * Return the Container with which this Logger has been associated.
     */
    public Container getContainer() {

	return (container);

    }


    /**
     * Set the Container with which this Logger has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

	// Deregister from the old Container (if any)
	if ((this.container != null) && (this.container instanceof Context))
	    ((Context) this.container).removePropertyChangeListener(this);

	// Process this property change
	Container oldContainer = this.container;
	this.container = container;
	support.firePropertyChange("container", oldContainer, this.container);

	// Register with the new Container (if any)
	if ((this.container != null) && (this.container instanceof Context)) {
	    setReloadable( ((Context) this.container).getReloadable() );
	    ((Context) this.container).addPropertyChangeListener(this);
	}

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

        int oldDebug = this.debug;
	this.debug = debug;
	support.firePropertyChange("debug", new Integer(oldDebug),
				   new Integer(this.debug));

    }


    /**
     * Return descriptive information about this Loader implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Return the ClassLoader class name.
     */
    public String getLoaderClass() {

	return (this.loaderClass);

    }


    /**
     * Set the ClassLoader class name.
     *
     * @param loaderClass The new ClassLoader class name
     */
    public void setLoaderClass() {

	this.loaderClass = loaderClass;

    }


    /**
     * Return the reloadable flag for this Loader.
     */
    public boolean getReloadable() {

	return (this.reloadable);

    }


    /**
     * Set the reloadable flag for this Loader.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable) {

	// Process this property change
	boolean oldReloadable = this.reloadable;
	this.reloadable = reloadable;
	support.firePropertyChange("reloadable",
				   new Boolean(oldReloadable),
				   new Boolean(this.reloadable));

	// Start or stop our background thread if required
	if (!started)
	    return;
	if (!oldReloadable && this.reloadable)
	    threadStart();
	else if (oldReloadable && !this.reloadable)
	    threadStop();

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

	support.addPropertyChangeListener(listener);

    }


    /**
     * Add a new repository to the set of repositories for this class loader.
     *
     * @param repository Repository to be added
     */
    public void addRepository(String repository) {

        if (debug >= 1)
	    log(sm.getString("standardLoader.addRepository", repository));
	String results[] = new String[repositories.length + 1];
	for (int i = 0; i < repositories.length; i++)
	    results[i] = repositories[i];
	results[repositories.length] = repository;
	repositories = results;
	if (started) {
	    classLoader.addRepository(repository);
	    setClassPath();
	}

    }


    /**
     * Return the set of repositories defined for this class loader.
     * If none are defined, a zero-length array is returned.
     */
    public String[] findRepositories() {

	return (repositories);

    }


    /**
     * Has the internal repository associated with this Loader been modified,
     * such that the loaded classes should be reloaded?
     */
    public boolean modified() {

	return (classLoader.modified());

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

	support.removePropertyChangeListener(listener);

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

	StringBuffer sb = new StringBuffer("StandardLoader[");
	if (container != null)
	    sb.append(container.getName());
	sb.append("]");
	return (sb.toString());

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

	lifecycle.addLifecycleListener(listener);

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

	lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Start this component, initializing our associated class loader.
     *
     * @exception LifecycleException if a lifecycle error occurs
     */
    public void start() throws LifecycleException {

	// Validate and update our current component state
	if (started)
	    throw new LifecycleException
		(sm.getString("standardLoader.alreadyStarted"));
	if (debug >= 1)
	    log(sm.getString("standardLoader.starting"));
	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

	// Construct a class loader based on our current repositories list
	try {
            /*
	    Class clazz = Class.forName(loaderClass);
	    classLoader = (Reloader) clazz.newInstance();
	    */
	    if (parentClassLoader == null)
                //	        classLoader = new FileClassLoader();
	        classLoader = new StandardClassLoader();
	    else
                //	        classLoader = new FileClassLoader(parentClassLoader);
	        classLoader = new StandardClassLoader(parentClassLoader);
	    for (int i = 0; i < repositories.length; i++)
		classLoader.addRepository(repositories[i]);
	    classLoader.addRestricted("org.apache.catalina.");
	    classLoader.addAllowed
		("org.apache.catalina.session.StandardSession");
	    classLoader.addSystem("javax.servlet.");
	    if (classLoader instanceof Lifecycle)
		((Lifecycle) classLoader).start();
	} catch (Throwable t) {
	    throw new LifecycleException("start: ", t);
	}

	// Set up context attributes if appropriate
	setClassLoader();
	setClassPath();

	// Start our background thread if we are reloadable
	if (reloadable) {
	    log(sm.getString("standardLoader.reloading"));
	    try {
		threadStart();
	    } catch (IllegalStateException e) {
		throw new LifecycleException(e);
	    }
	}
	    

    }


    /**
     * Stop this component, finalizing our associated class loader.
     *
     * @exception LifecycleException if a lifecycle error occurs
     */
    public void stop() throws LifecycleException {

	// Validate and update our current component state
	if (!started)
	    throw new LifecycleException
		(sm.getString("standardLoader.notStarted"));
	if (debug >= 1)
	    log(sm.getString("standardLoader.stopping"));
	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	// Stop our background thread if we are reloadable
	if (reloadable)
	    threadStop();

	// Remove context attributes as appropriate
	if (container instanceof Context) {
	    ServletContext servletContext =
		((Context) container).getServletContext();
	    servletContext.removeAttribute(Globals.CLASS_LOADER_ATTR);
	    servletContext.removeAttribute(Globals.CLASS_PATH_ATTR);
	}

	// Throw away our current class loader
	if (classLoader instanceof Lifecycle)
	    ((Lifecycle) classLoader).stop();
	classLoader = null;

    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Process property change events from our associated Context.
     *
     * @param event The property change event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

	// Validate the source of this event
	if (!(event.getSource() instanceof Context))
	    return;
	Context context = (Context) event.getSource();

	// Process a relevant property change
	if (event.getPropertyName().equals("reloadable")) {
	    try {
		setReloadable
		    ( ((Boolean) event.getNewValue()).booleanValue() );
	    } catch (NumberFormatException e) {
		log(sm.getString("standardLoader.reloadable",
				 event.getNewValue().toString()));
	    }
	}

    }


    // ------------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	Logger logger = null;
	if (container != null)
	    logger = container.getLogger();
	if (logger != null)
	    logger.log("StandardLoader[" + container.getName() + "]: "
		       + message);
	else {
	    String containerName = null;
	    if (container != null)
		containerName = container.getName();
	    System.out.println("StandardLoader[" + containerName
			       + "]: " + message);
	}

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

	Logger logger = null;
	if (container != null)
	    logger = container.getLogger();
	if (logger != null) {
	    logger.log("StandardLoader[" + container.getName() + "] "
		       + message, throwable);
	} else {
	    String containerName = null;
	    if (container != null)
		containerName = container.getName();
	    System.out.println("StandardLoader[" + containerName
			       + "]: " + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}

    }


    /**
     * Notify our Context that a reload is appropriate.
     */
    private void notifyContext() {

	ContextNotifier notifier = new ContextNotifier((Context) container);
	(new Thread(notifier)).start();

    }


    /**
     * Set the appropriate context attribute for our class loader.  This
     * is required only because Jasper depends on it.
     */
    private void setClassLoader() {

	if (!(container instanceof Context))
	    return;
	ServletContext servletContext =
	    ((Context) container).getServletContext();
	if (servletContext == null)
	    return;
	servletContext.setAttribute(Globals.CLASS_LOADER_ATTR,
				    getClassLoader());

    }


    /**
     * Set the appropriate context attribute for our class path.  This
     * is required only because Jasper depends on it.
     */
    private void setClassPath() {

	if (!(container instanceof Context))
	    return;
	ServletContext servletContext =
	    ((Context) container).getServletContext();
	if (servletContext == null)
	    return;

	StringBuffer classpath = new StringBuffer();
	for (int i = 0; i < repositories.length; i++) {
	    if (i > 0)
		classpath.append(File.pathSeparator);
	    classpath.append(repositories[i]);
	}
	servletContext.setAttribute(Globals.CLASS_PATH_ATTR,
				    classpath.toString());

    }


    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    private void threadSleep() {

	try {
	    Thread.sleep(checkInterval * 1000L);
	} catch (InterruptedException e) {
	    ;
	}

    }


    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     *
     * @exception IllegalStateException if we should not be starting
     *  a background thread now
     */
    private void threadStart() {

	// Has the background thread already been started?
	if (thread != null)
	    return;

	// Validate our current state
	if (!reloadable)
	    throw new IllegalStateException
		(sm.getString("standardLoader.notReloadable"));
	if (!(container instanceof Context))
	    throw new IllegalStateException
		(sm.getString("standardLoader.notContext"));

	// Start the background thread
	if (debug >= 1)
	    log(" Starting background thread");
	threadDone = false;
	threadName = "StandardLoader[" + container.getName() + "]";
	thread = new Thread(this, threadName);
	thread.setDaemon(true);
	thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * modified classes.
     */
    private void threadStop() {

	if (thread == null)
	    return;

	if (debug >= 1)
	    log(" Stopping background thread");
	threadDone = true;
	thread.interrupt();
	try {
	    thread.join();
	} catch (InterruptedException e) {
	    ;
	}

	thread = null;

    }


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

	if (debug >= 1)
	    log("BACKGROUND THREAD Starting");

	// Loop until the termination semaphore is set
	while (!threadDone) {

	    // Wait for our check interval
	    threadSleep();

	    // Perform our modification check
	    if (!classLoader.modified())
		continue;

	    // Handle a need for reloading
	    notifyContext();
	    break;

	}

	if (debug >= 1)
	    log("BACKGROUND THREAD Stopping");

    }


}


// ------------------------------------------------------------ Private Classes


/**
 * Private thread class to notify our associated Context that we have
 * recognized the need for a reload.
 */

final class ContextNotifier implements Runnable {


    /**
     * The Context we will notify.
     */
    private Context context = null;


    /**
     * Construct a new instance of this class.
     *
     * @param context The Context to be notified
     */
    public ContextNotifier(Context context) {

	super();
	this.context = context;

    }


    /**
     * Perform the requested notification.
     */
    public void run() {

	context.reload();

    }


}
