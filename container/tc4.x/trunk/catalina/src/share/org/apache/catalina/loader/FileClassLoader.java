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


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;


/**
 * Implementation of <b>java.lang.ClassLoader</b> that knows how to load
 * classes from disk directories and JAR files.  It also implements
 * the <code>Reloader</code> interface, to provide automatic reloading
 * support to <code>StandardLoader</code>.
 * <p>
 * This code was partially based on the <code>AdaptiveClassLoader</code>
 * module originally copied from Apache JServ, and used in Tomcat 3.x.
 * However, it does class loading in a different order (webapp first then
 * system classes), and allows the set of associated repositories to be
 * modified at runtime.
 * <p>
 * Besides the usual functions of loading and caching the bytecodes for
 * requested classes, this class loader also supports optional in-memory
 * caching of resources acquired via <code>getResourceAsStream()</code>.
 * Because most applications probably read their resource files only once
 * (during initialization), this is disabled by default, but it can be
 * enabled by setting the <code>maxCount</code>, <code>maxSize</code>,
 * and <code>minSize</code> properties.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class FileClassLoader
    extends ClassLoader
    implements Reloader{


    // ---------------------------------------------------------- Constructors


    /**
     * Construct a new ClassLoader instance with no defined repositories
     * and no parent ClassLoader.
     */
    public FileClassLoader() {

	super();

    }


    /**
     * Construct a new ClassLoader instance with no defined repositories
     * and the specified parent ClassLoader.
     *
     * @param parent The parent ClassLoader
     */
    public FileClassLoader(ClassLoader parent) {

	super(parent);

    }


    /**
     * Construct a new ClassLoader instance with the specified repositories
     * and no parent ClassLoader.
     *
     * @param repositories Initial list of repositories
     */
    public FileClassLoader(String repositories[]) {

        super();
	for (int i = 0; i < repositories.length; i++)
	    addRepository(repositories[i]);

    }


    /**
     * Construct a new ClassLoader instance with the specified repositories
     * and parent ClassLoader.
     *
     * @param parent The parent ClassLoader
     * @param repositories Initial list of repositories
     */
    public FileClassLoader(ClassLoader parent, String respositories[]) {

	super(parent);
	for (int i = 0; i < repositories.length; i++)
	    addRepository(repositories[i]);

    }


    // ---------------------------------------------------- Instance Variables


    /**
     * The set of fully qualified class or resource names to which access
     * will be allowed (if they exist) by this class loader, even if the
     * class or resource name would normally be restricted.
     */
    private String allowed[] = new String[0];


    /**
     * The set of ClassCacheEntries for classes loaded by this class loader,
     * keyed by the fully qualified class name.
     */
    private HashMap classCache = new HashMap();


    /**
     * The debugging detail level of this component.
     */
    private int debug = 0;


    /**
     * The set of File entries for repositories that are directories,
     * in the order that they were added.
     */
    private File directories[] = new File[0];


    /**
     * The set of File entries for repositories that are JARs,
     * in the order that they were added.
     */
    private File jarFiles[] = new File[0];


    /**
     * The maximum number of resources to be cached.
     */
    private int maxCount = 0;


    /**
     * The maximum size of resources to be cached.
     */
    private long maxSize = 0L;


    /**
     * The minimum size of resources to be cached.
     */
    private long minSize = 0L;


    /**
     * The set of repositories (both directories and JARs, in the order
     * that they were added.
     */
    private String repositories[] = new String[0];


    /**
     * The set of ResourceCacheEntries for resources loaded by this class
     * loader, keyed by the fully qualified resource name.
     */
    private HashMap resourceCache = new HashMap();


    /**
     * The set of class name prefixes to which access should be restricted.
     * A request for a class or resource that starts with this prefix will
     * fail with an appropriate exception or <code>null</code> return value,
     * unless that specific class or resource name is on the allowed list.
     */
    private String restricted[] = new String[0];


    /**
     * The set of class and resource name prefixes that should be allowed,
     * but only from the underlying system class loader.
     */
    private String systems[] = { "java." };


    // ------------------------------------------------------------ Properties


    /**
     * Return the debugging detail level of this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level of this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the maximum count of resources to be cached.
     */
    public int getMaxCount() {

	return (this.maxCount);

    }


    /**
     * Set the maximum count of resources to be cached.
     *
     * @param maxCount The new maximum count
     */
    public void setMaxCount(int maxCount) {

	this.maxCount = maxCount;

    }


    /**
     * Return the maximum size of resources to be cached.
     */
    public long getMaxSize() {

	return (this.maxSize);

    }


    /**
     * Set the maximum size of resources to be cached.
     */
    public void setMaxSize(long maxSize) {

	this.maxSize = maxSize;

    }


    /**
     * Return the minimum size of resources to be cached.
     */
    public long getMinSize() {

	return (this.minSize);

    }


    /**
     * Set the minimum size of resources to be cached.
     */
    public void setMinSize(long minSize) {

	this.minSize = minSize;

    }


    // ------------------------------------------------------- Reloader Methods


    /**
     * Add a new fully qualified class or resource name to which access will be
     * allowed, even if the class or resource name would otherwise match one
     * of the restricted patterns.
     *
     * @param name Class or resource name to allow access for
     */
    public void addAllowed(String name) {

	if (debug >= 1)
	    log("addAllowed(" + name + ")");

	synchronized (allowed) {
	    String results[] = new String[allowed.length + 1];
	    for (int i = 0; i < allowed.length; i++)
		results[i] = allowed[i];
	    results[allowed.length] = name;
	    allowed = results;
	}

    }


    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a
     *  directory pathname, a JAR file pathname, or a ZIP file pathname
     *
     * @exception IllegalArgumentException if the specified repository is
     *  invalid or does not exist
     */
    public synchronized void addRepository(String repository) {

	if (debug >= 1)
	    log("addRepository(" + repository + ")");

	// Validate the existence, readability, and type of this repository
	File file = new File(repository);
	if (!file.exists()) {
	    if (debug >= 1)
		log("  Repository does not exist");
	    throw new IllegalArgumentException
		("Repository '" + repository + "' does not exist");
	}
	if (!file.canRead()) {
	    if (debug >= 1)
		log("  Repository cannot be read");
	    throw new IllegalArgumentException
		("Repository '" + repository + "' cannot be read");
	}

	// Add this repository to the appropriate arrays
	if (file.isDirectory()) {
	    if (debug >= 1)
		log("  Repository is a directory");
	    synchronized (directories) {
		File results[] = new File[directories.length + 1];
		for (int i = 0; i < directories.length; i++)
		    results[i] = directories[i];
		results[directories.length] = file;
		directories = results;
	    }
	} else {
	    JarFile jarFile = null;
	    try {
	        jarFile = new JarFile(file);
		Enumeration entries = jarFile.entries();
		while (entries.hasMoreElements()) {
		    JarEntry entry = (JarEntry) entries.nextElement();
		    InputStream is = jarFile.getInputStream(entry);
		    is.close();
		    break;
		}
		jarFile.close();
	    } catch (Throwable t) {
		if (debug >= 1)
		    log("  Problem with this ZIP/JAR file", t);
	        throw new IllegalArgumentException
		    ("Cannot read JAR file '" + repository + "'");
	    }
	    if (debug >= 1)
		log("  Repository is a ZIP/JAR file");
	    synchronized (jarFiles) {
		File results[] = new File[jarFiles.length + 1];
		for (int i = 0; i < jarFiles.length; i++)
		    results[i] = jarFiles[i];
		results[jarFiles.length] = file;
		jarFiles = results;
	    }
	}
	synchronized (repositories) {
	    String results[] = new String[repositories.length + 1];
	    for (int i = 0; i < repositories.length; i++)
		results[i] = repositories[i];
	    results[repositories.length] = repository;
	    repositories = results;
	}

    }


    /**
     * Add a fully qualified class or resource name prefix that, if it matches
     * the name of a requested class or resource, will cause access to that
     * class or resource to fail (unless the complete name is on the allowed
     * list).
     *
     * @param prefix The restricted prefix
     */
    public void addRestricted(String prefix) {

	if (debug >= 1)
	    log("addRestricted(" + prefix + ")");

	synchronized (restricted) {
	    String results[] = new String[restricted.length + 1];
	    for (int i = 0; i < restricted.length; i++)
		results[i] = restricted[i];
	    results[restricted.length] = prefix;
	    restricted = results;
	}

    }


    /**
     * Add a fully qualified class or resource name prefix that, if it matches
     * the name of a requested class or resource, will cause access to that
     * class or resource to be attempted in the system class loader only
     * (bypassing the repositories defined in this class loader).  By default,
     * the <code>java.</code> prefix is defined as a system prefix.
     *
     * @param prefix The system prefix
     */
    public void addSystem(String prefix) {

	if (debug >= 1)
	    log("addSystem(" + prefix + ")");

	synchronized (systems) {
	    String results[] = new String[systems.length + 1];
	    for (int i = 0; i < systems.length; i++)
		results[i] = systems[i];
	    results[systems.length] = prefix;
	    systems = results;
	}

    }


    /**
     * Return a String array of the allowed class or resource name list
     * for this class loader.  If there are none, a zero-length array
     * is returned.
     */
    public String[] findAllowed() {

	return ((String[]) allowed.clone());

    }


    /**
     * Return a String array of the current repositories for this class
     * loader.  If there are no repositories, a zero-length array is
     * returned.
     */
    public String[] findRepositories() {

	return ((String[]) repositories.clone());

    }


    /**
     * Return a String array of the restricted class or resource name prefixes
     * for this class loader.  If there are none, a zero-length array
     * is returned.
     */
    public String[] findRestricted() {

	return ((String[]) restricted.clone());

    }


    /**
     * Return a Striong array of the sytsem class or resource name prefixes
     * for this class loader.  If there are none, a zero-length array
     * is returned.
     */
    public String[] findSystem() {

	return ((String[]) systems.clone());

    }


    /**
     * Have one or more classes or resources been modified so that a reload
     * is appropriate?
     */
    public boolean modified() {

	//	if (debug >= 2)
	//	    log("modified()");

	if (classCache.size() < 1)
	    return (false);

	// Build a list of the classes we currently have cached
	ClassCacheEntry entries[] = new ClassCacheEntry[0];
	synchronized (classCache) {
	    entries =
		(ClassCacheEntry[]) classCache.values().toArray(entries);
	}

	// Check for modifications to any of the cached classes
	for (int i = 0; i < entries.length; i++) {
	    // System classes cannot be checked
	    if (entries[i].origin == null)
		continue;
	    if (entries[i].lastModified != entries[i].origin.lastModified()) {
		//		if (debug >= 2)
		//		    log("  Class " +
		//			entries[i].loadedClass.getName() +
		//			" was modified");
		return (true);
	    }
	}

	// Build a list of the resources we currently have cached
	ResourceCacheEntry resources[] = new ResourceCacheEntry[0];
	synchronized (resourceCache) {
	    resources =
	      (ResourceCacheEntry[]) resourceCache.values().toArray(resources);
	}

	// Check for modifications to any of the cached resources
	for (int i = 0; i < resources.length; i++) {
	    // System resources cannot be checked
	    if (resources[i].origin == null)
		continue;
	    if (resources[i].lastModified !=
		resources[i].origin.lastModified()) {
		return (true);
	    }
	}

	//	if (debug >= 2)
	//	    log("  No classes or resources were modified");
        return (false);

    }


    /**
     * Remove a fully qualified class or resource name from the allowed list.
     *
     * @param name The name to remove
     */
    public void removeAllowed(String name) {

	if (debug >= 1)
	    log("removeAllowed(" + name + ")");

	synchronized (allowed) {
	    int j = -1;
	    for (int i = 0; i < allowed.length; i++) {
		if (name.equals(allowed[i])) {
		    j = i;
		    break;
		}
	    }
	    if (j < 0)
		return;
	    int k = 0;
	    String results[] = new String[allowed.length - 1];
	    for (int i = 0; i < allowed.length; i++) {
		if (i != j)
		    results[k++] = allowed[i];
	    }
	    allowed = results;
	}

    }


    /**
     * Remove a class or resource name prefix from the restricted list.
     *
     * @param prefix Prefix to be removed
     */
    public void removeRestricted(String prefix) {

	if (debug >= 1)
	    log("removeRestricted(" + prefix + ")");

	synchronized (restricted) {
	    int j = -1;
	    for (int i = 0; i < restricted.length; i++) {
		if (prefix.equals(restricted[i])) {
		    j = i;
		    break;
		}
	    }
	    if (j < 0)
		return;
	    int k = 0;
	    String results[] = new String[restricted.length - 1];
	    for (int i = 0; i < restricted.length; i++) {
		if (i != j)
		    results[k++] = restricted[i];
	    }
	    restricted = results;
	}

    }


    /**
     * Remove a class or resource name prefix from the system list.
     *
     * @param prefix Prefix to be removed
     */
    public void removeSystem(String prefix) {

	if (debug >= 1)
	    log("removeSystem(" + prefix + ")");

	synchronized (systems) {
	    int j = -1;
	    for (int i = 0; i < systems.length; i++) {
		if (prefix.equals(systems[i])) {
		    j = i;
		    break;
		}
	    }
	    if (j < 0)
		return;
	    int k = 0;
	    String results[] = new String[systems.length - 1];
	    for (int i = 0; i < systems.length; i++) {
		if (i != j)
		    results[k++] = systems[i];
	    }
	    systems = results;
	}

    }


    // --------------------------------------------------- ClassLoader Methods


    /**
     * Find a resource with a given name.  The return is a URL to the resource.
     * Doing a <code>getContent()</code> on the URL may return an Image, an
     * AudioClip, or an InputStream.
     *
     * @param name The name of the resource, to be used as is
     */
    public URL getResource(String name) {

	//	if (debug >= 2)
	//	    log("getResource(" + name + ")");

	// Handle requests for restricted resources by returning null
	if (restricted(name)) {
	    //	    if (debug >= 2)
	    //	        log("  Rejecting restricted resource " + name);
	    return (null);
	}

	// Attempt to load the requested resource from our repositories
	if (!system(name)) {

	    // Search for this resource in all of our directory repositories
	    for (int i = 0; i < directories.length; i++) {
		//		if (debug >= 2)
		//		    log("  Checking repository " + directories[i]);
		URL theURL = loadResourceFromDirectory(directories[i], name);
		if (theURL != null) {
		    //		    if (debug >= 2)
		    //			log("  Returning URL " + theURL);
		    return (theURL);
		}
	    }

	    // Search for this resource in all of our JAR file repositories
	    for (int i = 0; i < jarFiles.length; i++) {
		//		if (debug >= 2)
		//		    log("  Checking repository " + jarFiles[i]);
		URL theURL = loadResourceFromJarFile(jarFiles[i], name);
		if (theURL != null) {
		    //		    if (debug >= 2)
		    //			log("  Returning URL " + theURL);
		    return (theURL);
		}
	    }

	}

        // Load this resource from our parent class loader
	//	if (debug >= 2)
	//	    log("  Checking parent class loader");
	ClassLoader parent = getParent();
	if (parent == null)
	    parent = ClassLoader.getSystemClassLoader();
	URL theURL = parent.getResource(name);
	//	if (debug >= 2) {
	//	    if (theURL != null)
	//	        log("  Returning URL " + theURL);
	//	    else
	//	        log("  Cannot find this resource");
	//        }
	return (theURL);

    }


    /**
     * Get an InputStream on a given resource.  Will return <code>null</code>
     * if no resource with this name is found.
     *
     * @param name The name of the resource
     */
    public InputStream getResourceAsStream(String name) {

	//	if (debug >= 2)
	//	    log("getResourceAsStream(" + name + ")");

	// Handle requests for restricted resources by returning null
	if (restricted(name)) {
	    //	    if (debug >= 2)
	    //	        log("  Rejecting restricted resource " + name);
	    return (null);
	}

	// Has this resource already been loaded and cached?
	ResourceCacheEntry entry = null;
	synchronized (resourceCache) {
	    entry = (ResourceCacheEntry) resourceCache.get(name);
	}
	if (entry != null) {
	    //	    if (debug >= 2)
	    //		log("  Found the resource in our cache");
	    return (new ByteArrayInputStream(entry.loadedResource));
	}

	// Attempt to load the requested resource from our repositories
	if (!system(name)) {

	    // Search for this resource in all of our directory repositories
	    for (int i = 0; i < directories.length; i++) {
		//		if (debug >= 2)
		//		    log("  Checking repository " + directories[i]);
		InputStream theStream =
		    loadStreamFromDirectory(directories[i], name);
		if (theStream != null) {
		    //		    if (debug >= 2)
		    //			log("  Returning stream from directory");
		    return (theStream);
		}
	    }

	    // Search for this resource in all of our JAR file repositories
	    for (int i = 0; i < jarFiles.length; i++) {
		//		if (debug >= 2)
		//		    log("  Checking repository " + jarFiles[i]);
		InputStream theStream =
		    loadStreamFromJarFile(jarFiles[i], name);
		if (theStream != null) {
		    //		    if (debug >= 2)
		    //			log("  Returning stream from JAR file");
		    return (theStream);
		}
	    }

	}

        // Load this resource from our parent class loader
	//	if (debug >= 2)
	//	    log("  Checking parent class loader");
	ClassLoader parent = getParent();
	if (parent == null)
	    parent = ClassLoader.getSystemClassLoader();
	InputStream theStream = parent.getResourceAsStream(name);
	//	if (debug >= 2) {
	//	    if (theStream != null)
	//	        log("  Returning stream from parent class loader");
	//	    else
	//	        log("  Cannot find this resource");
	//        }
	return (theStream);

    }


    /**
     * Requests the class loader to load and resolve a class with the
     * specified name.  The <code>loadClass</code> method is called by the
     * Java Virtual Machine when a class loaded by a class loader first
     * references another class.  Every subclass of <code>ClassLoader</code>
     * must define this method.
     *
     * @param name Name of the desired Class
     * @param resolve <code>true</code> if this Class needs to be resolved.
     *
     * @return The resulting Class, or <code>null</code> if it was not found
     *
     * @exception ClassNotFoundException if the class loader cannot find
     *  a definition for this class
     */
    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

	//	if (debug >= 2)
	//	    log("loadClass(" + name + ", " + resolve + ")");

	// Handle requests for restricted classes by throwing an exception
	if (restricted(name)) {
	    //	    if (debug >= 2)
	    //	        log("  Rejecting restricted class " + name);
	    throw new ClassNotFoundException
		("Cannot load restricted class '" + name + "'");
	}

	// Has this class already been loaded?
	ClassCacheEntry entry = null;
	synchronized (classCache) {
	    entry = (ClassCacheEntry) classCache.get(name);
	}
	if (entry != null) {
	    Class theClass = entry.loadedClass;
	    if (resolve)
	        resolveClass(theClass);
	    //	    if (debug >= 2)
	    //	    	log("  Found the class in our cache");
	    return (theClass);
	}

	// Attempt to load the requested class from our repositories
	if (!system(name)) {

	    // Search for this class in all of our directory repositories
	    for (int i = 0; i < directories.length; i++) {
		//		if (debug >= 2)
		//		    log("  Checking repository " + directories[i]);
		Class theClass =
		    loadClassFromDirectory(directories[i], name, resolve);
		if (theClass != null) {
		    //		    if (debug >= 2)
		    //			log("  Returning class from directory");
		    return (theClass);
		}
	    }

	    // Search for this class in all of our JAR file repositories
	    for (int i = 0; i < jarFiles.length; i++) {
		//		if (debug >= 2)
		//		    log("  Checking repository " + jarFiles[i]);
		Class theClass =
		    loadClassFromJarFile(jarFiles[i], name, resolve);
		if (theClass != null) {
		    //		    if (debug >= 2)
		    //			log("  Returning class from JAR file");
		    return (theClass);
		}
	    }

	}
		
	// Load this class from our parent class loader
	//	if (debug >= 2)
	//	    log("  Checking parent class loader");
	Class theClass = loadClassFromParent(name, resolve);
	if (theClass != null) {
	    //	    if (debug >= 2)
	    //		log("  Loaded from parent class loader");
	    return (theClass);
	} else {
	    //	    if (debug >= 2)
	    //		log("  Cannot load this class");
	    throw new ClassNotFoundException(name);
	}

    }


    /**
     * Return a String representation of this class.
     */
    public String toString() {

	StringBuffer sb = new StringBuffer("FileClassLoader[");
	for (int i = 0; i < repositories.length; i++) {
	    if (i > 0)
		sb.append(File.pathSeparator);
	    sb.append(repositories[i]);
	}
	sb.append("]");
	return (sb.toString());

    }


    // ------------------------------------------------------- Private Methods


    /**
     * Create and return a cache entry for containing the data for this
     * resource, if caching is appropriate.  Otherwise, return
     * <code>null</code>.
     *
     * @param stream Input stream containing the resource data (which is
     *  read if we decided to cache, or undisturbed otherwise)
     * @param size Size of this resource in bytes, or -1 if not known
     * @param origin Origin file from which this resource was loaded
     * @param lastModified Last modified timestamp of the origin
     */
    private ResourceCacheEntry cacheResource(InputStream stream, long size,
					     File origin, long lastModified) {

	// Should we cache this resource?
	if ((size < 0) || (size < minSize) || (size > maxSize))
	    return (null);
	if (resourceCache.size() >= maxCount)
	    return (null);

	// Create, register, and return a cache entry for this resource
	byte data[] = new byte[(int) size];
	try {
	    for (int i = 0; i < data.length; i++)
		data[i] = (byte) stream.read();
	    return (new ResourceCacheEntry(data, origin, lastModified));
	} catch (IOException e) {
	    return (null);
	}

    }


    /**
     * Load the specified class from the specified directory, if it exists;
     * otherwise return <code>null</code>.  If found, a cache entry will be
     * automatically added before returning.
     *
     * @param directory File for the directory to be searched
     * @param name Name of the desired class
     * @param resolve <code>true</code> if this Class needs to be resolved
     */
    private Class loadClassFromDirectory(File directory, String name,
					 boolean resolve) {

        // Translate the class name to a filename
        String filename =
	  name.replace('.', File.separatorChar) + ".class";

	// Validate the existence and readability of this class file
	File classFile = new File(directory, filename);
	if (!classFile.exists() || !classFile.canRead())
	    return (null);

	// Load the bytes for this class, and define it
	Class theClass = null;
	byte buffer[] = new byte[(int) classFile.length()];
	InputStream is = null;
	try {
	    is = new BufferedInputStream(new FileInputStream(classFile));
	    is.read(buffer);
	    is.close();
	    is = null;
	    theClass = defineClass(name, buffer, 0, buffer.length);
	    if (resolve)
	        resolveClass(theClass);
	    synchronized (classCache) {
		classCache.put(name,
			       new ClassCacheEntry(theClass, classFile,
						   classFile.lastModified()));
	    }
	    return (theClass);
	} catch (Throwable t) {
	    if (is != null) {
	        try {
		    is.close();
		} catch (Throwable u) {
		    ;
		}
	    }
	    return (null);
	}

    }


    /**
     * Load the specified class from the specified JAR file, if it exists;
     * otherwise return <code>null</code>.  If found, a cache entry will be
     * automatically added before returning.
     *
     * @param jar JAR file to be searched
     * @param name Name of the desired class
     * @param resolve <code>true</code> if this Class needs to be resolved
     */
    private Class loadClassFromJarFile(File jar, String name,
				       boolean resolve) {

        // Translate the class name to a filename
        String filename =
	  name.replace('.', '/') + ".class";

	// Load the bytes for this class, and define it
	JarFile jarFile = null;
	JarEntry jarEntry = null;
	Class theClass = null;
	InputStream is = null;
	try {
	    jarFile = new JarFile(jar);
	    if (jarFile == null) {
		return (null);
	    }
	    jarEntry = jarFile.getJarEntry(filename);
	    if (jarEntry == null) {
	        jarFile.close();
		return (null);
	    }
	    byte buffer[] = new byte[(int) jarEntry.getSize()];
	    is = new BufferedInputStream(jarFile.getInputStream(jarEntry));
	    is.read(buffer);
	    is.close();
	    theClass = defineClass(name, buffer, 0, buffer.length);
	    if (resolve)
	        resolveClass(theClass);
	    synchronized (classCache) {
		classCache.put(name,
			       new ClassCacheEntry(theClass, jar,
						   jar.lastModified()));
	    }
	    return (theClass);
	} catch (Throwable t) {
	    if (is != null) {
	        try {
		    is.close();
		} catch (Throwable u) {
		    ;
		}
	    }
	    if (jarFile != null) {
	        try {
		    jarFile.close();
		} catch (Throwable u) {
		    ;
		}
	    }
	    return (null);
	}

    }


    /**
     * Load the specified class from the parent class loader, if it exists;
     * otherwise return <code>null</code>.  If found, a cache entry will be
     * automatically added before returning.
     *
     * @param name Name of the desired class
     * @param resolve <code>true</code> if this Class needs to be resolved
     */
    private Class loadClassFromParent(String name, boolean resolve) {

	ClassLoader parent = getParent();
	if (parent == null)
	    parent = ClassLoader.getSystemClassLoader();
	Class theClass = null;
	try {
	    theClass = parent.loadClass(name);
	} catch (ClassNotFoundException e) {
	    theClass = null;
	}
	if (theClass != null) {
	    if (resolve)
	        resolveClass(theClass);
	    classCache.put(name,
			   new ClassCacheEntry(theClass, null,
					       Long.MAX_VALUE));
	}
	return (theClass);

    }


    /**
     * Load the specified resource from the specified directory, if it exists;
     * otherwise return <code>null</code>.
     *
     * @param directory File for the directory to be searched
     * @param name Name of the desired resource
     */
    private URL loadResourceFromDirectory(File directory, String name) {

        // Translate the resource name to a filename
	String filename = name.replace('/', File.separatorChar);

	// Validate the existence and readability of this resource file
	File resourceFile = new File(directory, filename);
	if (!resourceFile.exists() || !resourceFile.canRead())
	    return (null);

	// Return a URL that points to this resource file
	try {
	    return (new URL("file:" + resourceFile.getAbsolutePath()));
	} catch (MalformedURLException e) {
	    return (null);
	}

    }


    /**
     * Load the specified resource from the specified JAR file, if it exists;
     * otherwise return <code>null</code>.
     *
     * @param jar JAR file to be searched
     * @param name Name of the desired resource
     */
    private URL loadResourceFromJarFile(File jar, String name) {

        // Translate the resource name to a filename
	//        String filename = name.replace('.', '/');
	String filename = name;

	// Create an input stream for this resource
	JarFile jarFile = null;
	JarEntry jarEntry = null;
	InputStream is = null;
	try {
	    jarFile = new JarFile(jar);
	    jarEntry = jarFile.getJarEntry(filename);
	    jarFile.close();
	    if (jarEntry == null)
		return (null);
	    try {
		return (new URL("jar:file:" + jar.getAbsolutePath() +
				"!/" + name));
	    } catch (MalformedURLException e) {
		return (null);
	    }
	} catch (Throwable t) {
	    if (jarFile != null) {
	        try {
		    jarFile.close();
		} catch (Throwable u) {
		    ;
		}
	    }
	    return (null);
	}

    }


    /**
     * Load the specified resource from the specified directory, if it exists;
     * otherwise return <code>null</code>.
     *
     * @param directory File for the directory to be searched
     * @param name Name of the desired resource
     */
    private InputStream loadStreamFromDirectory(File directory, String name) {

        // Translate the resource name to a filename
	String filename = name.replace('/', File.separatorChar);

	// Validate the existence and readability of this resource file
	File resourceFile = new File(directory, filename);
	if (!resourceFile.exists() || !resourceFile.canRead())
	    return (null);

	// Return an input stream for this resource file
	try {
	    InputStream stream =
		new BufferedInputStream(new FileInputStream(resourceFile));
	    ResourceCacheEntry entry =
		cacheResource(stream, resourceFile.length(),
			      resourceFile, resourceFile.lastModified());
	    if (entry != null) {
		stream.close();
		return (new ByteArrayInputStream(entry.loadedResource));
	    } else {
		return (stream);
	    }
	} catch (IOException e) {
	    return (null);
	}

    }


    /**
     * Load the specified resource from the specified JAR file, if it exists;
     * otherwise return <code>null</code>.
     *
     * @param directory File for the directory to be searched
     * @param name Name of the desired resource
     */
    private InputStream loadStreamFromJarFile(File jar, String name) {

        // Translate the resource name to a filename
	//        String filename = name.replace('.', '/');
	String filename = name;

	// Create an input stream for this resource
	JarFile jarFile = null;
	JarEntry jarEntry = null;
	InputStream is = null;
	try {
	    jarFile = new JarFile(jar);
	    jarEntry = jarFile.getJarEntry(filename);
	    if (jarEntry == null) {
	        jarFile.close();
		return (null);
	    }
	    is = new BufferedInputStream(jarFile.getInputStream(jarEntry));
	    jarFile.close();
	    ResourceCacheEntry entry = cacheResource(is, jarEntry.getSize(),
						     jar, jar.lastModified());
	    if (entry != null) {
		is.close();
		return (new ByteArrayInputStream(entry.loadedResource));
	    } else {
		return (is);
	    }
	} catch (Throwable t) {
	    if (jarFile != null) {
	        try {
		    jarFile.close();
		} catch (Throwable u) {
		    ;
		}
	    }
	    return (null);
	}

    }


    /**
     * Log a debugging output message.
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	System.out.println("FileClassLoader: " + message);

    }


    /**
     * Log a debugging output message with an exception.
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    private void log(String message, Throwable throwable) {

	System.out.println("FileClassLoader: " + message);
	throwable.printStackTrace(System.out);

    }


    /**
     * Is this a class or resource that should not be allowed to load
     * in this class loader?
     *
     * @param name Name of the class or resource to be checked
     */
    private boolean restricted(String name) {

	for (int i = 0; i < allowed.length; i++) {
	    if (name.equals(allowed[i]))
		return (false);
	}

	for (int i = 0; i < restricted.length; i++) {
	    if (name.startsWith(restricted[i]))
		return (true);
	}

	return (false);

    }


    /**
     * Is this a class or resource that should be loaded only by the
     * system class loader?
     *
     * @param name Name of the class or resource to be checked
     */
    private boolean system(String name) {

	for (int i = 0; i < systems.length; i++) {
	    if (name.startsWith(systems[i]))
		return (true);
	}

	return (false);

    }


    // ------------------------------------------------------- Private Classes


    /**
     * The cache entry for a particular class loaded by this class loader.
     */
    private static class ClassCacheEntry {

        /**
	 * The "last modified" time of the origin file at the time this class
	 * was loaded, in milliseconds since the epoch.
	 */
        long lastModified;

        /**
	 * The actual loaded class.
	 */
        Class loadedClass;

        /**
	 * The File (for a directory) or JarFile (for a JAR) from which this
	 * class was loaded, or <code>null</code> if loaded from the system.
	 */
        File origin;

        /**
	 * Construct a new instance of this class.
	 */
        public ClassCacheEntry(Class loadedClass, File origin,
			       long lastModified) {

	    this.loadedClass = loadedClass;
	    this.origin = origin;
	    this.lastModified = lastModified;

	}

    }


    /**
     * The cache entry for a particular resource loaded by this class loader.
     */
    private static class ResourceCacheEntry {

        /**
	 * The "last modified" time of the origin file at the time this
	 * resource was loaded, in milliseconds since the epoch.
	 */
        long lastModified;

        /**
	 * The actual loaded resource.
	 */
	byte loadedResource[];

        /**
	 * The File (for a directory) or JarFile (for a JAR) from which this
	 * class was loaded, or <code>null</code> if loaded from the system.
	 */
        File origin;

        /**
	 * Construct a new instance of this class.
	 */
        public ResourceCacheEntry(byte loadedResource[], File origin,
				  long lastModified) {

	    this.loadedResource = loadedResource;
	    this.origin = origin;
	    this.lastModified = lastModified;

	}

    }


}
