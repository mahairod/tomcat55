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


package org.apache.catalina;


import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A <b>Resources</b> is a generic interface for the resource acquisition
 * and lookup methods of the ServletContext interface.
 * <p>
 * A Resources object is generally attached to a Context, or higher level,
 * Container.  However, a Connector may also provide a Resources object
 * associated with a particular Request, which should be treated by any
 * Container processing this request as overriding any Resources object
 * normally configured for this Container.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public interface Resources {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Resources has been associated.
     */
    public Container getContainer();


    /**
     * Set the Container with which this Resources has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container);


    /**
     * Return the "expand directory resources into listings" property.
     */
    public boolean getExpand();


    /**
     * Set the "expand directory resources into listings" property.
     *
     * @param expand The new expand flag
     */
    public void setExpand(boolean expand);


    /**
     * Return descriptive information about this Resources implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Return the MIME type of the specified file, or <code>null</code> if
     * the MIME type is not known.  The MIME type is determined by the
     * configuration of the servlet container, and may be specified in a
     * web application descriptor.  Common MIME types are
     * <code>"text/html"</code> and <code>"image/gif"</code>.
     *
     * @param file Name of the file whose MIME type is to be determined
     */
    public String getMimeType(String file);


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
    public String getRealPath(String path);


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
    public URL getResource(String path) throws MalformedURLException;


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
    public InputStream getResourceAsStream(String path);


    /**
     * Returns true if a resource exists at the specified path,
     * where <code>path</code> would be suitable for passing as an argument to
     * <code>getResource()</code> or <code>getResourceAsStream()</code>.
     * If there is no resource at the specified location, return false.
     *
     * @param path The path to the desired resource
     */
    public boolean exists(String path);


    /**
     * Return the last modified date/time of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If there is no resource at the
     * specified location, return -1.
     *
     * @param path The path to the desired resource
     */
    public long getResourceModified(String path);


    /**
     * Return the set of context-relative paths of all available resources.
     * Each path will begin with a "/" character.
     */
     public String[] getResourcePaths();


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
    public long getResourceCreated(String path);


    /**
     * Return the content length of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If the content length
     * of the resource can't be determinedof if the resource is a collection,
     * return -1. If no content is available, return 0.
     *
     * @param path The path to the desired resource
     */
    public long getResourceLength(String path);


    /**
     * Return true if the resource at the specified path is a collection. A
     * collection is a special type of resource which has no content but
     * contains child resources.
     *
     * @param path The path to the desired resource
     */
    public boolean isCollection(String path);


    /**
     * Return the children of the resource at the specified path, if any. This
     * will return null if the resource is not a collection, or if it is a
     * collection but has no children.
     *
     * @param path The path to the desired resource
     */
    public String[] getCollectionMembers(String path);


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
    public boolean setResource(String path, InputStream content);


    /**
     * Create a collection at the specified path. A parent collection for this
     * collection must exist. Return false if a resource already exist at the
     * path specified, or if the parent collection doesn't exist.
     *
     * @param path The path to the desired resource
     */
    public boolean createCollection(String path);


    /**
     * Delete the specified resource. Non-empty collections cannot be deleted
     * before deleting all their member resources. Return false is deletion
     * fails because either the resource specified doesn't exist, or the
     * resource is a non-empty collection.
     *
     * @param path The path to the desired resource
     */
    public boolean deleteResource(String path);


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);


}


