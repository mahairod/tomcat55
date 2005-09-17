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


package org.apache.catalina.realm;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.MD5Encoder;
import org.apache.catalina.util.xml.SaxContext;
import org.apache.catalina.util.xml.XmlAction;
import org.apache.catalina.util.xml.XmlMapper;
import org.xml.sax.AttributeList;


/**
 * Simple implementation of <b>Realm</b> that reads an XML file to configure
 * the valid users, passwords, and roles.  The file format (and default file
 * location) are identical to those currently supported by Tomcat 3.X.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public abstract class RealmBase
    implements Lifecycle, Realm {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Realm is associated.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info =
	"org.apache.catalina.realm.RealmBase/1.0";


    /**
     * Short name for this Realm Implementation
     */
    protected static final String name = "RealmBase";

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
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
     * MD5 message digest provider.
     */
    protected static MessageDigest md5Helper;


    /**
     * The MD5 helper object for this class.
     */
    protected static final MD5Encoder md5Encoder = new MD5Encoder();


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {

	return (container);

    }


    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

	Container oldContainer = this.container;
	this.container = container;
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
	this.debug = debug;
    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return info;

    }

    /**
     * Return short name of this Realm implementation
     */
    public String getName() {

	return name;

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
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {
        
        String serverCredentials = getPassword(username);
        
        if ( (serverCredentials == null) 
             || (!serverCredentials.equals(credentials)) )
            return null;
        
        return getPrincipal(username);
        
    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, byte[] credentials) {

	return (authenticate(username, credentials.toString()));

    }


    /**
     * Return the Principal associated with the specified username, which
     * matches the digest calculated using the given parameters using the 
     * method described in RFC 2069; otherwise return <code>null</code>.
     * 
     * @param username Username of the Principal to look up
     * @param clientDigest Digest which has been submitted by the client
     * @param nOnce Unique (or supposedly unique) token which has been used
     * for this request
     * @param realm Realm name
     * @param md5a2 Second MD5 digest used to calculate the digest : 
     * MD5(Method + ":" + uri)
     */
    public Principal authenticate(String username, String clientDigest,
                                  String nOnce, String nc, String cnonce,
                                  String qop, String realm,
                                  String md5a2) {
        
        /*
          System.out.println("Digest : " + clientDigest);
          
          System.out.println("************ Digest info");
          System.out.println("Username:" + username);
          System.out.println("ClientSigest:" + clientDigest);
          System.out.println("nOnce:" + nOnce);
          System.out.println("nc:" + nc);
          System.out.println("cnonce:" + cnonce);
          System.out.println("qop:" + qop);
          System.out.println("realm:" + realm);
          System.out.println("md5a2:" + md5a2);
        */
        

        String md5a1 = getDigest(username, realm);
        if (md5a1 == null)
            return null;
        String serverDigestValue = md5a1 + ":" + nOnce + ":" + nc + ":" 
            + cnonce + ":" + qop + ":" + md5a2;
        String serverDigest = 
            md5Encoder.encode(md5Helper.digest(serverDigestValue.getBytes()));
        //System.out.println("Server digest : " + serverDigest);
        
        if (serverDigest.equals(clientDigest))
            return getPrincipal(username);
        else
            return null;
    }



    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public abstract boolean hasRole(Principal principal, String role);


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

	support.removePropertyChangeListener(listener);

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
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start()
        throws LifecycleException {

	// Validate and update our current component state
	if (started)
	    throw new LifecycleException
		(sm.getString("memoryRealm.alreadyStarted"));
	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop()
        throws LifecycleException {

	// Validate and update our current component state
	if (!started)
	    throw new LifecycleException
		(sm.getString("memoryRealm.notStarted"));
	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the digest associated with given principal's user name.
     */
    protected String getDigest(String username, String realmName) {
        if (md5Helper == null) {
            try {
                md5Helper = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }
        String digestValue = username + ":" + realmName + ":" 
            + getPassword(username);
        byte[] digest = 
            md5Helper.digest(digestValue.getBytes());
        return md5Encoder.encode(digest);
    }


    /**
     * Return the password associated with the given principal's user name.
     */
    protected abstract String getPassword(String username);


    /**
     * Return the Principal associated with the given user name.
     */
    protected abstract Principal getPrincipal(String username);


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
	Logger logger = null;

	if (container != null) {
	    logger = container.getLogger();
        }

	if (logger != null) {
	    logger.log(getName()+"[" + container.getName() + "]: " + message);
        } else {
	    String containerName = null;
	    if (container != null) {
		containerName = container.getName();
            }
	    System.out.println(getName()+"[" + containerName + "]: " + message);
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

	if (container != null) {
	    logger = container.getLogger();
        }

	if (logger != null) {
	    logger.log(getName()+"[" + container.getName() + "]: " + message, throwable);
        } else {
	    String containerName = null;
	    if (container != null) {
		containerName = container.getName();
            }
	    System.out.println(getName()+"[" + containerName + "]: " + message);
	    System.out.println("" + throwable);
	    throwable.printStackTrace(System.out);
	}
    }


}
