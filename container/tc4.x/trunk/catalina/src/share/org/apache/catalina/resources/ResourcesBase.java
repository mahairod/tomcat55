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


package org.apache.catalina.resources;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Resources;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Convenience base class for implementations of the <b>Resources</b>
 * interface.  It is expected that subclasses of this class will be
 * created for each flavor of document root to be supported.
 * <p>
 * Included in the basic support provided by this class is provisions
 * for caching of resources according to configurable policy properties.
 * This will be especially useful for web applications with relatively
 * small amounts of static content (such as a 100% dynamic JSP based
 * application with just a few images), as well as environments where
 * accessing the underlying resources is relatively time consuming
 * (such as a local or remote JAR file).
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public abstract class ResourcesBase
    implements Resources, Lifecycle, PropertyChangeListener, Runnable {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class with default values.
     */
    public ResourcesBase() {

	super();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The interval (in seconds) at which our background task should check
     * for out-of-date cached resources, or zero for no checks.
     */
    protected int checkInterval = 0;


    /**
     * The Container this component is associated with (normally a Context).
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The document root for this component.
     */
    protected String docBase = null;


    /**
     * Should "directory" entries be expanded?
     */
    protected boolean expand = true;


    /**
     * The descriptive information string for this implementation.
     */
    protected static final String info =
	"org.apache.catalina.resources.ResourcesBase/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The maximum number of resources to cache.
     */
    protected int maxCount = 0;


    /**
     * The maximum size of resources to be cached.
     */
    protected long maxSize = 0L;


    /**
     * The minimum size of resources to be cached.
     */
    protected long minSize = 0L;


    /**
     * The prefix to the log messages we will be creating.
     */
    protected static final String prefix = "ResourcesBase";


    /**
     * The set of ResourceBean entries for this component,
     * keyed by the normalized context-relative resource URL.
     */
    protected HashMap resourcesCache = new HashMap();


    /**
     * The count of ResourceBean entries for which we have actually
     * cached data.  This can be different from the number of elements
     * in the <code>resourcesCache</code> collection.
     */
    protected int resourcesCount = 0;


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
	StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    protected boolean started = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The background thread.
     */
    protected Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;


    /**
     * The name to register for the background thread.
     */
    protected String threadName = "ResourcesBase";


    // ------------------------------------------------------------- Properties


    /**
     * Return the resource cache check interval.
     */
    public int getCheckInterval() {

	return (this.checkInterval);

    }


    /**
     * Set the resource cache check interval.
     *
     * @param checkInterval The new check interval
     */
    public void setCheckInterval(int checkInterval) {

	// Perform the property update
	int oldCheckInterval = this.checkInterval;
	this.checkInterval = checkInterval;
	support.firePropertyChange("checkInterval",
				   new Integer(oldCheckInterval),
				   new Integer(this.checkInterval));

	// Start or stop the background thread (if necessary)
	if (started) {
	    if ((oldCheckInterval > 0) && (this.checkInterval <= 0))
		threadStop();
	    else if ((oldCheckInterval <= 0) && (this.checkInterval > 0))
		threadStart();
	}

    }


    /**
     * Return the Container with which this Resources has been associated.
     */
    public Container getContainer() {

	return (this.container);

    }


    /**
     * Set the Container with which this Resources has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

	Container oldContainer = this.container;
	if ((oldContainer != null) && (oldContainer instanceof Context))
	    ((Context) oldContainer).removePropertyChangeListener(this);

	this.container = container;
	// We are interested in property changes to the document base
	if ((this.container != null) && (this.container instanceof Context)) {
	    ((Context) this.container).addPropertyChangeListener(this);
	    setDocBase(((Context) this.container).getDocBase());
	}

	support.firePropertyChange("container", oldContainer, this.container);

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
     * Return the document root for this component.
     */
    public String getDocBase() {

	return (this.docBase);

    }


    /**
     * Set the document root for this component.
     *
     * @param docBase The new document root
     *
     * @exception IllegalArgumentException if the specified value is not
     *  supported by this implementation
     * @exception IllegalArgumentException if this would create a
     *  malformed URL
     */
    public void setDocBase(String docBase) {

	// Validate the format of the proposed document root
	if (docBase == null)
	    throw new IllegalArgumentException
		(sm.getString("resources.null"));

	// Change the document root property
	String oldDocBase = this.docBase;
	this.docBase = docBase.toString();
	support.firePropertyChange("docBase", oldDocBase, this.docBase);
	if (debug >= 1)
	    log("Setting docBase to '" + this.docBase + "'");

    }


    /**
     * Return the "expand directories" flag.
     */
    public boolean getExpand() {

	return (this.expand);

    }


    /**
     * Set the "expand directories" flag.
     *
     * @param expand The new "expand directories" flag
     */
    public void setExpand(boolean expand) {

	boolean oldExpand = this.expand;
	this.expand = expand;
	support.firePropertyChange("expand", new Boolean(oldExpand),
				   new Boolean(this.expand));

    }


    /**
     * Return descriptive information about this Resources implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Return the maximum number of resources to cache.
     */
    public int getMaxCount() {

	return (this.maxCount);

    }


    /**
     * Set the maximum number of resources to cache.
     *
     * @param maxCount The new maximum count
     */
    public void setMaxCount(int maxCount) {

	int oldMaxCount = this.maxCount;
	this.maxCount = maxCount;
	support.firePropertyChange("maxCount", new Integer(oldMaxCount),
				   new Integer(this.maxCount));

    }


    /**
     * Return the maximum size of resources to be cached.
     */
    public long getMaxSize() {

	return (this.maxSize);

    }


    /**
     * Set the maximum size of resources to be cached.
     *
     * @param maxSize The new maximum size
     */
    public void setMaxSize(long maxSize) {

	long oldMaxSize = this.maxSize;
	this.maxSize = maxSize;
	support.firePropertyChange("maxSize", new Long(oldMaxSize),
				   new Long(this.maxSize));

    }


    /**
     * Return the minimum size of resources to be cached.
     */
    public long getMinSize() {

	return (this.minSize);

    }


    /**
     * Set the minimum size of resources to be cached.
     *
     * @param minSize The new minimum size
     */
    public void setMinSize(long minSize) {

	long oldMinSize = this.minSize;
	this.minSize = minSize;
	support.firePropertyChange("minSize", new Long(oldMinSize),
				   new Long(this.minSize));

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
     * Return the MIME type of the specified file, or <code>null</code> if
     * the MIME type is not known.  The MIME type is determined by the
     * configuration of the servlet container, and may be specified in a
     * web application descriptor.  Common MIME types are
     * <code>"text/html"</code> and <code>"image/gif"</code>.
     * <p>
     * The default implementation consults the MIME type mappings that have
     * been registered in our associated Context, if any.
     *
     * @param file Name of the file whose MIME type is to be determined
     */
    public String getMimeType(String file) {

	if (debug >= 1)
	    log("Calculating MIME type of '" + file + "'");
	if (file == null)
	    return (null);
	if ((container == null) || (!(container instanceof Context)))
	    return (null);
	int period = file.lastIndexOf(".");
	if (period < 0)
	    return (null);
	String extension = file.substring(period + 1);
	if (extension.length() < 1)
	    return (null);
	if (debug >= 1)
	    log(" Mime type of '" + extension + "' is '" +
		((Context) container).findMimeMapping(extension));
	return (((Context) container).findMimeMapping(extension));

    }


    /**
     * Return the real path for a given virtual path.  For example, the
     * virtual path <code>"/index.html"</code> has a real path of whatever
     * file on the server's filesystem would be served by a request for
     * <code>"/index.html"</code>.
     * <p>
     * The real path returned will be in a form appropriate to the computer
     * and operating system on which the servlet container is running,
     * including the proper path separators.  This method returns
     * <code>null</code> if the servlet container cannot translate the
     * virtual path to a real path for any reason (such as when the content
     * is being made available from a <code>.war</code> archive).
     *
     * @param path The virtual path to be translated
     */
    public abstract String getRealPath(String path);


    /**
     * Return a URL to the resource that is mapped to the specified path.
     * The path must begin with a "/" and is interpreted as relative to
     * the current context root.
     * <p>
     * This method allows the Container to make a resource available to
     * servlets from any source.  Resources can be located on a local or
     * remote file system, in a database, or in a <code>.war</code> file.
     * <p>
     * The servlet container must implement the URL handlers and
     * <code>URLConnection</code> objects that are necessary to access
     * the resource.
     * <p>
     * This method returns <code>null</code> if no resource is mapped to
     * the pathname.
     * <p>
     * Some Containers may allow writing to the URL returned by this method,
     * using the methods of the URL class.
     * <p>
     * The resource content is returned directly, so be aware that
     * requesting a <code>.jsp</code> page returns the JSP source code.
     * Use a <code>RequestDispatcher</code> instead to include results
     * of an execution.
     * <p>
     * This method has a different purpose than
     * <code>java.lang.Class.getResource()</code>, which looks up resources
     * based on a class loader.  This method does not use class loaders.
     *
     * @param path The path to the desired resource
     *
     * @exception MalformedURLException if the pathname is not given
     *  in the correct form
     */
    public abstract URL getResource(String path) throws MalformedURLException;


    /**
     * Return the resource located at the named path as an
     * <code>InputStream</code> object.
     * <p>
     * The data in the <code>InputStream</code> can be of any type or length.
     * The path must be specified according to the rules given in
     * <code>getResource()</code>.  This method returns <code>null</code>
     * if no resource exists at the specified path.
     * <p>
     * Meta-information such as content length and content type that is
     * available via the <code>getResource()</code> method is lost when
     * using this method.
     * <p>
     * The servlet container must implement the URL handlers and
     * <code>URLConnection</code> objects that are necessary to access
     * the resource.
     * <p>
     * This method is different from
     * <code>java.lang.Class.getResourceAsStream()</code>, which uses a
     * class loader.  This method allows servlet containers to make a
     * resource available to a servlet from any location, without using
     * a class loader.
     *
     * @param path The path to the desired resource
     */
    public abstract InputStream getResourceAsStream(String path);


    /**
     * Returns true if a resource exists at the specified path, 
     * where <code>path</code> would be suitable for passing as an argument to
     * <code>getResource()</code> or <code>getResourceAsStream()</code>.  
     * If there is no resource at the specified location, return false.
     *
     * @param path The path to the desired resource
     */
    public abstract boolean exists(String path);


    /**
     * Return the last modified date/time of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If there is no resource at the
     * specified location, return -1.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  This method should bypass any cached
     * resources and reference the underlying resource directly, because it
     * will be used by the background thread that is checking for resources
     * that have been modified.
     *
     * @param path The path to the desired resource
     */
    public abstract long getResourceModified(String path);


    /**
     * Return the creation date/time of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If there is no resource at the
     * specified location, return -1. If this time is unknown, the 
     * implementation should return getResourceModified(path).
     *
     * @param path The path to the desired resource
     */
    public abstract long getResourceCreated(String path);


    /**
     * Return the content length of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If the content length
     * of the resource can't be determined, return -1. If no content is 
     * available (when for exemple, the resource is a collection), return 0.
     *
     * @param path The path to the desired resource
     */
    public abstract long getResourceLength(String path);


    /**
     * Return true if the resource at the specified path is a collection. A
     * collection is a special type of resource which has no content but 
     * contains child resources.
     *
     * @param path The path to the desired resource
     */
    public abstract boolean isCollection(String path);


    /**
     * Return the children of the resource at the specified path, if any. This
     * will return null if the resource is not a collection, or if it is a 
     * collection but has no children.
     * 
     * @param path The path to the desired resource
     */
    public abstract String[] getCollectionMembers(String path);


    /**
     * Set the content of the resource at the specified path. If the resource
     * already exists, its previous content is overwritten. If the resource
     * doesn't exist, its immediate parent collection (according to the path 
     * given) exists, then its created, and the given content is associated
     * with it. Return false if either the resource is a collection, or
     * no parent collection exist.
     * 
     * @param path The path to the desired resource
     * @param content InputStream to the content to be set
     */
    public abstract boolean setResource(String path, InputStream content);


    /**
     * Create a collection at the specified path. A parent collection for this
     * collection must exist. Return false if a resource already exist at the
     * path specified, or if the parent collection doesn't exist.
     * 
     * @param path The path to the desired resource
     */
    public abstract boolean createCollection(String path);


    /**
     * Delete the specified resource. Non-empty collections cannot be deleted
     * before deleting all their member resources. Return false is deletion
     * fails because either the resource specified doesn't exist, or the 
     * resource is a non-empty collection.
     * 
     * @param path The path to the desired resource
     */
    public abstract boolean deleteResource(String path);


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

	support.removePropertyChangeListener(listener);

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
	if (event.getPropertyName().equals("docBase"))
	    setDocBase((String) event.getNewValue());

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
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

	// Validate and update our current component state
	if (started)
	    throw new LifecycleException
		(sm.getString("resources.alreadyStarted"));
	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

	// Start the background expiration checking thread (if necessary)
	if (checkInterval > 0)
	    threadStart();

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

	// Validate and update our current state
	if (!started)
	    throw new LifecycleException
		(sm.getString("resources.notStarted"));

	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	// Stop the background expiration checking thread (if necessary)
	threadStop();

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Should the resource specified by our parameters be cached?
     *
     * @param name Name of the proposed resource
     * @param size Size (in bytes) of the proposed resource
     */
    protected boolean cacheable(String name, long size) {

	if ((size < minSize) || (size > maxSize))
	    return (false);
	else if (resourcesCount >= maxCount)
	    return (false);
	else
	    return (true);

    }


    /**
     * Return a File object representing the base directory for the
     * entire servlet container (i.e. the Engine container if present).
     */
    protected File engineBase() {

	return (new File(System.getProperty("catalina.home")));

    }


    /**
     * Return a File object representing the base directory for the
     * current virtual host (i.e. the Host container if present).
     */
    protected File hostBase() {

	// Locate our immediately surrounding Host (if any)
	Container container = this.container;
	while (container != null) {
	    if (container instanceof Host)
		break;
	    container = container.getParent();
	}
	if (container == null)
	    return (engineBase());

	// Use the "appBase" property of this container
	String appBase = ((Host) container).getAppBase();
	File file = new File(appBase);
	if (!file.isAbsolute())
	    file = new File(engineBase(), appBase);
	return (file);

    }



    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

	Logger logger = null;
	if (container != null)
	    logger = container.getLogger();
	if (logger != null)
	    logger.log(prefix + "[" + container.getName() + "]: "
		       + message);
	else {
	    String containerName = null;
	    if (container != null)
		containerName = container.getName();
	    System.out.println(prefix + "[" + containerName
			       + "]: " + message);
	}

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

	Logger logger = null;
	if (container != null)
	    logger = container.getLogger();
	if (logger != null)
	    logger.log(prefix + "[" + container.getName() + "] "
		       + message, throwable);
	else {
	    String containerName = null;
	    if (container != null)
		containerName = container.getName();
	    System.out.println(prefix + "[" + containerName
			       + "]: " + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}

    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    protected String normalize(String path) {

	// Normalize the slashes and add leading slash if necessary
	String normalized = path;
	if (normalized.indexOf('\\') >= 0)
	    normalized = normalized.replace('\\', '/');
	if (!normalized.startsWith("/"))
	    normalized = "/" + normalized;

	// Resolve occurrences of "//" in the normalized path
	while (true) {
	    int index = normalized.indexOf("//");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 1);
	}

	// Resolve occurrences of "%20" in the normalized path
	while (true) {
	    int index = normalized.indexOf("%20");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) + " " +
		normalized.substring(index + 3);
        }

	// Resolve occurrences of "/./" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/./");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 2);
	}

	// Resolve occurrences of "/../" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/../");
	    if (index < 0)
		break;
	    if (index == 0)
		return (null);	// Trying to go outside our context
	    int index2 = normalized.lastIndexOf('/', index - 1);
	    normalized = normalized.substring(0, index2) +
		normalized.substring(index + 3);
	}

	// Return the normalized path that we have completed
	return (normalized);

    }


    /**
     * Scan our cached resources, looking for cases where the underlying
     * resource has been modified since we cached it.
     */
    protected void threadProcess() {

	// Create a list of the cached resources we know about
	ResourceBean entries[] = new ResourceBean[0];
	synchronized(resourcesCache) {
	    entries =
		(ResourceBean[]) resourcesCache.values().toArray(entries);
	}

	// Check the last modified date on each entry
	for (int i = 0; i < entries.length; i++) {
	    if (entries[i].getLastModified() !=
		getResourceModified(entries[i].getName())) {
		synchronized (resourcesCache) {
		    resourcesCache.remove(entries[i].getName());
		}
	    }
	}

    }


    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    protected void threadSleep() {

	try {
	    Thread.sleep(checkInterval * 1000L);
	} catch (InterruptedException e) {
	    ;
	}

    }


    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    protected void threadStart() {

	if (thread != null)
	    return;

	threadDone = false;
	threadName = "ResourcesBase[" + container.getName() + "]";
	thread = new Thread(this, threadName);
	thread.setDaemon(true);
	thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    protected void threadStop() {

	if (thread == null)
	    return;

	threadDone = true;
	thread.interrupt();
	try {
	    thread.join();
	} catch (InterruptedException e) {
	    ;
	}

	thread = null;

    }


    /**
     * Validate the format of the specified path, which should be context
     * relative and begin with a slash character.
     *
     * @param path Context-relative path to be validated
     *
     * @exception IllegalArgumentException if the specified path is null
     *  or does not have a valid format
     */
    protected void validate(String path) {

	if ((path == null) || !path.startsWith("/"))
	    throw new IllegalArgumentException
		(sm.getString("resources.path", path));


    }


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

	// Loop until the termination semaphore is set
	while (!threadDone) {
	    threadSleep();
	    threadProcess();
	}

    }


}

