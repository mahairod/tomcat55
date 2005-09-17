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


package org.apache.catalina.session;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Concrete implementation of the <b>Store</b> interface that utilizes
 * a file per saved Session in a configured directory.  Sessions that are
 * saved are still subject to being expired based on inactivity.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class FileStore
    implements Lifecycle, Runnable, Store {



    // ----------------------------------------------------- Instance Variables


    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int checkInterval = 60;


    /**
     * The pathname of the directory in which Sessions are stored.
     */
    private String directoryPath = "./STORE";


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "FileStore/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The string manager for this package.
     */
    private StringManager sm =
	StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The property change support for this component.
     */
    private PropertyChangeSupport support = new PropertyChangeSupport(this);


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
    private String threadName = "FileStore";


    // ------------------------------------------------------------- Properties


    /**
     * Return the check interval (in seconds) for this Manager.
     */
    public int getCheckInterval() {

	return (this.checkInterval);

    }


    /**
     * Set the check interval (in seconds) for this Manager.
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
     * Return the directory path for this Store.
     */
    public String getDirectoryPath() {

	return (directoryPath);

    }


    /**
     * Set the directory path for this Store.
     *
     * @param path The new directory path
     */
    public void setDirectoryPath(String path) {

	String oldDirectoryPath = this.directoryPath;
	this.directoryPath = path;
	support.firePropertyChange("directoryPath", oldDirectoryPath,
				   this.directoryPath);

    }


    /**
     * Return descriptive information about this Store implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Return the number of Sessions present in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public int getSize() throws IOException {

	return (0);	// FIXME: getSize()

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
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] keys() throws IOException {

	return (new String[0]);	// FIXME: keys()

    }


    /**
     * Load and return the Session associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored Session, return <code>null</code>.
     *
     * @param id Session identifier of the session to load
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     * @exception IOException if an input/output error occurs
     */
    public Session load(String id)
        throws ClassNotFoundException, IOException {

	return (null);	// FIXME: load()

    }


    /**
     * Remove the Session with the specified session identifier from
     * this Store, if present.  If no such Session is present, this method
     * takes no action.
     *
     * @param id Session identifier of the Session to be removed
     *
     * @exception IOException if an input/output error occurs
     */
    public void remove(String id) throws IOException {

	;	// FIXME: remove()

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
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */
    public void save(Session session) throws IOException {

	;	// FIXME: save()

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
     * @param listener The listener to add
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
		(sm.getString("fileStore.alreadyStarted"));
	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

	// Start the background reaper thread
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

	// Validate and update our current component state
	if (!started)
	    throw new LifecycleException
		(sm.getString("fileStore.notStarted"));
	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	// Stop the background reaper thread
	threadStop();

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Invalidate all sessions that have expired.
     */
    private void processExpires() {

	long timeNow = System.currentTimeMillis();
	/*
	Session sessions[] = findSessions();

	for (int i = 0; i < sessions.length; i++) {
	    StandardSession session = (StandardSession) sessions[i];
	    if (!session.isValid())
		continue;
	    int maxInactiveInterval = session.getMaxInactiveInterval();
	    if (maxInactiveInterval < 0)
		continue;
	    int timeIdle = // Truncate, do not round up
		(int) ((timeNow - session.getLastAccessedTime()) / 1000L);
	    if (timeIdle >= maxInactiveInterval)
		session.expire();
	}
	*/

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
     */
    private void threadStart() {

	if (thread != null)
	    return;

	threadDone = false;
	thread = new Thread(this, threadName);
	thread.setDaemon(true);
	thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    private void threadStop() {

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


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

	// Loop until the termination semaphore is set
	while (!threadDone) {
	    threadSleep();
	    processExpires();
	}

    }


}
