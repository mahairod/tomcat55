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


import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Host;
import org.apache.catalina.Request;
import org.apache.catalina.Response;


/**
 * Standard implementation of the <b>Host</b> interface.  Each
 * child container must be a Context implementation to process the
 * requests directed to a particular web application.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class StandardHost
    extends ContainerBase
    implements Host {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardHost component with the default basic Valve.
     */
    public StandardHost() {

	super();
	setBasic(new StandardHostValve());

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of aliases for this Host.
     */
    private String[] aliases = new String[0];


    /**
     * The application root for this Host.
     */
    private String appBase = ".";


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
	"org.apache.catalina.core.StandardHost/1.0";


    /**
     * The Java class name of the default Mapper class for this Container.
     */
    private String mapperClass =
	"org.apache.catalina.core.StandardHostMapper";


    // ------------------------------------------------------------- Properties


    /**
     * Return the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getAppBase() {

	return (this.appBase);

    }


    /**
     * Set the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param appBase The new application root
     */
    public void setAppBase(String appBase) {

	String oldAppBase = this.appBase;
	this.appBase = appBase;
	support.firePropertyChange("appBase", oldAppBase, this.appBase);

    }


    /**
     * Return the canonical, fully qualified, name of the virtual host
     * this Container represents.
     */
    public String getName() {

	return (name);

    }


    /**
     * Set the canonical, fully qualified, name of the virtual host
     * this Container represents.
     *
     * @param name Virtual host name
     *
     * @exception IllegalArgumentException if name is null
     */
    public void setName(String name) {

	if (name == null)
	    throw new IllegalArgumentException
		(sm.getString("standardHost.nullName"));

	name = name.toLowerCase();	// Internally all names are lower case

	String oldName = this.name;
	this.name = name;
	support.firePropertyChange("name", oldName, this.name);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add an alias name that should be mapped to this same Host.
     *
     * @param alias The alias to be added
     */
    public void addAlias(String alias) {

	alias = alias.toLowerCase();

	// Skip duplicate aliases
	for (int i = 0; i < aliases.length; i++) {
	    if (aliases[i].equals(alias))
		return;
	}

	// Add this alias to the list
	String newAliases[] = new String[aliases.length + 1];
	for (int i = 0; i < aliases.length; i++)
	    newAliases[i] = aliases[i];
	newAliases[aliases.length] = alias;

	// Inform interested listeners
	fireContainerEvent(ADD_ALIAS_EVENT, alias);

    }


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Context.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

	if (!(child instanceof Context))
	    throw new IllegalArgumentException
		(sm.getString("standardHost.notContext"));
	super.addChild(child);

    }


    /**
     * Return the set of alias names for this Host.  If none are defined,
     * a zero length array is returned.
     */
    public String[] findAliases() {

	return (this.aliases);

    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Return the Context that would be used to process the specified
     * host-relative request URI, if any; otherwise return <code>null</code>.
     *
     * @param uri Request URI to be mapped
     */
    public Context map(String uri) {

	if (debug > 0)
	    log("Mapping request URI '" + uri + "'");
	if (uri == null)
	    return (null);

	// Match on the longest possible context path prefix
	if (debug > 1)
	    log("  Trying the longest context path prefix");
	Context context = null;
	while (true) {
	    context = (Context) findChild(uri);
	    if (context != null)
		break;
	    int slash = uri.lastIndexOf("/");
	    if (slash < 0)
		break;
	    uri = uri.substring(0, slash);
	}

	// If no Context matches, select the default Context
	if (context == null) {
	    if (debug > 1)
		log("  Trying the default context");
	    context = (Context) findChild("");
	}

	// Complain if no Context has been selected
	if (context == null) {
	    log(sm.getString("standardHost.mappingError", uri));
	    return (null);
	}

	// Return the mapped Context (if any)
	if (debug > 0)
	    log(" Mapped to context '" + context.getPath() + "'");
	return (context);

    }


    /**
     * Remove the specified alias name from the aliases for this Host.
     *
     * @param alias Alias name to be removed
     */
    public void removeAlias(String alias) {

	alias = alias.toLowerCase();

	synchronized (aliases) {

	    // Make sure this alias is currently present
	    int n = -1;
	    for (int i = 0; i < aliases.length; i++) {
		if (aliases[i].equals(alias)) {
		    n = i;
		    break;
		}
	    }
	    if (n < 0)
		return;

	    // Remove the specified alias
	    int j = 0;
	    String results[] = new String[aliases.length - 1];
	    for (int i = 0; i < aliases.length; i++) {
		if (i != n)
		    results[j++] = aliases[i];
	    }
	    aliases = results;

	}

	// Inform interested listeners
	fireContainerEvent(REMOVE_ALIAS_EVENT, alias);

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

	StringBuffer sb = new StringBuffer();
	if (getParent() != null) {
	    sb.append(getParent().toString());
	    sb.append(".");
	}
	sb.append("StandardHost[");
	sb.append(getName());
	sb.append("]");
	return (sb.toString());

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Add a default Mapper implementation if none have been configured
     * explicitly.
     *
     * @param mapperClass Java class name of the default Mapper
     */
    protected void addDefaultMapper(String mapperClass) {

	super.addDefaultMapper(this.mapperClass);

    }


}
