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


package org.apache.catalina.core;


import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.naming.directory.DirContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributesListener;
import javax.servlet.http.HttpServletRequest;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.DirContextURLConnection;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.HttpRequestBase;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ResourceSet;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of <code>ServletContext</code> that represents
 * a web application's execution environment.  An instance of this class is
 * associated with each instance of <code>StandardContext</code>.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public final class ApplicationContext
    implements ServletContext {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContext(String basePath, StandardContext context) {

	super();
	this.context = context;
        this.basePath = basePath;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The context attributes for this context.
     */
    private HashMap attributes = new HashMap();


    /**
     * The Context instance with which we are associated.
     */
    private StandardContext context = null;


    /**
     * Empty collection to serve as the basis for empty enumerations.
     * <strong>DO NOT ADD ANY ELEMENTS TO THIS COLLECTION!</strong>
     */
    private static final ArrayList empty = new ArrayList();


    /**
     * The merged context initialization parameters for this Context.
     */
    private HashMap parameters = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * Base path.
     */
    private String basePath = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Return the resources object that is mapped to a specified path.
     * The path must begin with a "/" and is interpreted as relative to the
     * current context root.
     */
    public DirContext getResources() {

	return context.getResources();

    }


    // ------------------------------------------------- ServletContext Methods


    /**
     * Return the value of the specified context attribute, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the context attribute to return
     */
    public Object getAttribute(String name) {

	synchronized (attributes) {
	    return (attributes.get(name));
	}

    }


    /**
     * Return an enumeration of the names of the context attributes
     * associated with this context.
     */
    public Enumeration getAttributeNames() {

	synchronized (attributes) {
	    return (new Enumerator(attributes.keySet()));
	}

    }


    /**
     * Return a <code>ServletContext</code> object that corresponds to a
     * specified URI on the server.  This method allows servlets to gain
     * access to the context for various parts of the server, and as needed
     * obtain <code>RequestDispatcher</code> objects or resources from the
     * context.  The given path must be absolute (beginning with a "/"),
     * and is interpreted based on our virtual host's document root.
     *
     * @param uri Absolute URI of a resource on the server
     */
    public ServletContext getContext(String uri) {

	// Validate the format of the specified argument
	if ((uri == null) || (!uri.startsWith("/")))
	    return (null);

	// Return the current context if requested
	String contextPath = context.getPath();
	if ((contextPath.length() > 0) &&
	    (uri.startsWith(contextPath))) {
	    return (this);
	}

	// Return other contexts only if allowed
	if (!context.getCrossContext())
	    return (null);
	try {
	    Host host = (Host) context.getParent();
	    Context child = host.map(uri);
	    if (child != null)
		return (child.getServletContext());
	    else
		return (null);
	} catch (Throwable t) {
	    return (null);
	}

    }


    /**
     * Return the value of the specified initialization parameter, or
     * <code>null</code> if this parameter does not exist.
     *
     * @param name Name of the initialization parameter to retrieve
     */
    public String getInitParameter(String name) {

        mergeParameters();
        synchronized (parameters) {
            return ((String) parameters.get(name));
        }

    }


    /**
     * Return the names of the context's initialization parameters, or an
     * empty enumeration if the context has no initialization parameters.
     */
    public Enumeration getInitParameterNames() {

        mergeParameters();
        synchronized (parameters) {
            return (new Enumerator(parameters.keySet()));
        }

    }


    /**
     * Return the major version of the Java Servlet API that we implement.
     */
    public int getMajorVersion() {

	return (Constants.MAJOR_VERSION);

    }


    /**
     * Return the minor version of the Java Servlet API that we implement.
     */
    public int getMinorVersion() {

	return (Constants.MINOR_VERSION);

    }


    /**
     * Return the MIME type of the specified file, or <code>null</code> if
     * the MIME type cannot be determined.
     *
     * @param file Filename for which to identify a MIME type
     */
    public String getMimeType(String file) {

	if (file == null)
	    return (null);
	int period = file.lastIndexOf(".");
	if (period < 0)
	    return (null);
	String extension = file.substring(period + 1);
	if (extension.length() < 1)
	    return (null);
	return (context.findMimeMapping(extension));

    }


    /**
     * Return a <code>RequestDispatcher</code> object that acts as a
     * wrapper for the named servlet.
     *
     * @param name Name of the servlet for which a dispatcher is requested
     */
    public RequestDispatcher getNamedDispatcher(String name) {

	// Validate the name argument
	if (name == null)
	    return (null);

	// Create and return a corresponding request dispatcher
	Wrapper wrapper = (Wrapper) context.findChild(name);
	if (wrapper == null)
	    return (null);
	ApplicationDispatcher dispatcher =
	  new ApplicationDispatcher(wrapper, null, null, null, name);
	return ((RequestDispatcher) dispatcher);

    }


    /**
     * Return the real path for a given virtual path, if possible; otherwise
     * return <code>null</code>.
     *
     * @param path The path to the desired resource
     */
    public String getRealPath(String path) {

        // Here, we return a fake path
        File file = new File(basePath, path);
        return (file.getAbsolutePath());

    }


    /**
     * Return a <code>RequestDispatcher</code> instance that acts as a
     * wrapper for the resource at the given path.  The path must begin
     * with a "/" and is interpreted as relative to the current context root.
     *
     * @param path The path to the desired resource.
     */
    public RequestDispatcher getRequestDispatcher(String path) {

        // Validate the path argument
        if (path == null)
            return (null);
        if (!path.startsWith("/"))
            throw new IllegalArgumentException
              (sm.getString("applicationContext.requestDispatcher.iae", path));

        // Construct a "fake" request to be mapped by our Context
        String contextPath = context.getPath();
        if (contextPath == null)
            contextPath = "";
        String relativeURI = path;
        String queryString = null;
        int question = path.indexOf("?");
        if (question >= 0) {
            relativeURI = path.substring(0, question);
            queryString = path.substring(question + 1);
        }
        HttpRequestBase request = new HttpRequestBase();
        request.setContext(context);
        request.setContextPath(context.getPath());
        request.setRequestURI(contextPath + relativeURI);
        request.setQueryString(queryString);
        Wrapper wrapper = (Wrapper) context.map(request, true);
        if (wrapper == null)
            return (null);

        // Construct a RequestDispatcher to process this request
        HttpServletRequest hrequest =
            (HttpServletRequest) request.getRequest();
        ApplicationDispatcher dispatcher =
          new ApplicationDispatcher(wrapper,
                                    hrequest.getServletPath(),
                                    hrequest.getPathInfo(),
                                    hrequest.getQueryString(),
                                    null);
        return ((RequestDispatcher) dispatcher);

    }



    /**
     * Return the URL to the resource that is mapped to a specified path.
     * The path must begin with a "/" and is interpreted as relative to the
     * current context root.
     *
     * @param path The path to the desired resource
     *
     * @exception MalformedURLException if the path is not given
     *  in the correct form
     */
    public URL getResource(String path) throws MalformedURLException {

	DirContext resources = context.getResources();
	if (resources != null) {
            try {
                resources.lookup(path);
                return new URL("jndi", null, 0, path, 
                               new DirContextURLStreamHandler(resources));
            } catch (Exception e) {
            }
        }
        return (null);

    }


    /**
     * Return the requested resource as an <code>InputStream</code>.  The
     * path must be specified according to the rules described under
     * <code>getResource</code>.  If no such resource can be identified,
     * return <code>null</code>.
     *
     * @param path The path to the desired resource.
     */
    public InputStream getResourceAsStream(String path) {

        DirContext resources = context.getResources();
        if (resources != null) {
            try {
                Object resource = resources.lookup(path);
                if (resource instanceof Resource)
                    return (((Resource) resource).streamContent());
            } catch (Exception e) {
            }
        }
        return (null);

    }


    /**
     * Return a Set containing the resource paths of all resources defined
     * within this web application.  Each path will be a String starting with
     * a "/" character.  The returned set is immutable.
     */
    public Set getResourcePaths() {

        ResourceSet set = new ResourceSet();
        // FIXME !
        /*
        Resources resources = context.getResources();
        if (resources == null) {
            set.setLocked(true);
            return (set);
        }
        String paths[] = resources.getResourcePaths();
        if (paths == null)
            paths = new String[0];
        for (int i = 0; i < paths.length; i++)
            set.add(paths[i]);
        */
        set.setLocked(true);
        return (set);

    }


    /**
     * Return the name and version of the servlet container.
     */
    public String getServerInfo() {

	return (Globals.SERVER_INFO);

    }


    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Servlet getServlet(String name) {

	return (null);

    }


    /**
     * Return the display name of this web application.
     */
    public String getServletContextName() {

        return (context.getDisplayName());

    }


    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Enumeration getServletNames() {

	return (new Enumerator(empty));

    }


    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Enumeration getServlets() {

	return (new Enumerator(empty));

    }


    /**
     * Writes the specified message to a servlet log file.
     *
     * @param message Message to be written
     */
    public void log(String message) {

	Logger logger = context.getLogger();
	if (logger != null)
	    logger.log(message);

    }


    /**
     * Writes the specified exception and message to a servlet log file.
     *
     * @param exception Exception to be reported
     * @param message Message to be written
     *
     * @deprecated As of Java Servlet API 2.1, use
     *  <code>log(String, Throwable)</code> instead
     */
    public void log(Exception exception, String message) {

	Logger logger = context.getLogger();
	if (logger != null)
	    logger.log(exception, message);

    }


    /**
     * Writes the specified message and exception to a servlet log file.
     *
     * @param message Message to be written
     * @param throwable Exception to be reported
     */
    public void log(String message, Throwable throwable) {

	Logger logger = context.getLogger();
	if (logger != null)
	    logger.log(message, throwable);

    }


    /**
     * Remove the context attribute with the specified name, if any.
     *
     * @param name Name of the context attribute to be removed
     */
    public void removeAttribute(String name) {

        Object value = null;
        boolean found = false;

	// Remove the specified attribute
	synchronized (attributes) {
            found = attributes.containsKey(name);
            if (found) {
                value = attributes.get(name);
                attributes.remove(name);
            } else {
                return;
            }
	}

	// Notify interested application event listeners
	Object listeners[] = context.getApplicationListeners();
	if ((listeners == null) || (listeners.length == 0))
	    return;
	ServletContextAttributeEvent event =
	  new ServletContextAttributeEvent(context.getServletContext(),
					    name, value);
	for (int i = 0; i < listeners.length; i++) {
	    if (!(listeners[i] instanceof ServletContextAttributesListener))
	        continue;
            ServletContextAttributesListener listener =
	        (ServletContextAttributesListener) listeners[i];
	    try {
                context.fireContainerEvent("beforeContextAttributeRemoved",
                                           listener);
		listener.attributeRemoved(event);
                context.fireContainerEvent("afterContextAttributeRemoved",
                                           listener);
	    } catch (Throwable t) {
                context.fireContainerEvent("afterContextAttributeRemoved",
                                           listener);
	        // FIXME - should we do anything besides log these?
	        log(sm.getString("applicationContext.attributeEvent"), t);
	    }
	}

    }


    /**
     * Bind the specified value with the specified context attribute name,
     * replacing any existing value for that name.
     *
     * @param name Attribute name to be bound
     * @param value New attribute value to be bound
     */
    public void setAttribute(String name, Object value) {

        boolean replaced = false;

	// Add or replace the specified attribute
	synchronized (attributes) {
	    if (attributes.get(name) != null)
	        replaced = true;
	    attributes.put(name, value);
	}

	// Notify interested application event listeners
	Object listeners[] = context.getApplicationListeners();
	if ((listeners == null) || (listeners.length == 0))
	    return;
	ServletContextAttributeEvent event =
	  new ServletContextAttributeEvent(context.getServletContext(),
					    name, value);
	for (int i = 0; i < listeners.length; i++) {
	    if (!(listeners[i] instanceof ServletContextAttributesListener))
	        continue;
            ServletContextAttributesListener listener =
	        (ServletContextAttributesListener) listeners[i];
	    try {
		if (replaced) {
                    context.fireContainerEvent("beforeContextAttributeReplaced",
                                               listener);
		    listener.attributeReplaced(event);
                    context.fireContainerEvent("afterContextAttributeReplaced",
                                               listener);
		} else {
                    context.fireContainerEvent("beforeContextAttributeAdded",
                                               listener);
		    listener.attributeAdded(event);
                    context.fireContainerEvent("afterContextAttributeAdded",
                                               listener);
                }
	    } catch (Throwable t) {
                if (replaced)
                    context.fireContainerEvent("afterContextAttributeReplaced",
                                               listener);
                else
                    context.fireContainerEvent("afterContextAttributeAdded",
                                               listener);
	        // FIXME - should we do anything besides log these?
	        log(sm.getString("applicationContext.attributeEvent"), t);
	    }
	}

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Merge the context initialization parameters specified in the application
     * deployment descriptor with the application parameters described in the
     * server configuration, respecting the <code>override</code> property of
     * the application parameters appropriately.
     */
    private void mergeParameters() {

        if (parameters != null)
            return;
        HashMap results = new HashMap();
        String names[] = context.findParameters();
        for (int i = 0; i < names.length; i++)
            results.put(names[i], context.findParameter(names[i]));
        ApplicationParameter params[] =
            context.findApplicationParameters();
        for (int i = 0; i < params.length; i++) {
            if (params[i].getOverride()) {
                if (results.get(params[i].getName()) == null)
                    results.put(params[i].getName(), params[i].getValue());
            } else {
                results.put(params[i].getName(), params[i].getValue());
            }
        }
        parameters = results;

    }


}
