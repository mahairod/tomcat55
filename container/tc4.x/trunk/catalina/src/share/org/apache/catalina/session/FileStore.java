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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Container;
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


    // ----------------------------------------------------- Constants
    
    
    /**
     * The extension to use for serialized session filenames.
     */
    private static final String FILE_EXT = ".session";


    // ----------------------------------------------------- Instance Variables


    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int checkInterval = 60;


    /**
     * The pathname of the directory in which Sessions are stored.
     * Relative to the temp directory for the web application.
     */
    private String directory = ".";

    /**
     * A File representing the directory in which Sessions are stored.
     */
    private File directoryFile = null;


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
     * The Manager with which this FileStore is associated.
     */
    protected Manager manager;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


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
    public String getDirectory() {

        return (directory);

    }


    /**
     * Set the directory path for this Store.
     *
     * @param path The new directory path
     */
    public void setDirectory(String path) {

        String oldDirectory = this.directory;
        this.directory = path;
        this.directoryFile = null;
        support.firePropertyChange("directory", oldDirectory,
                                   this.directory);

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

        String[] files = getDirectoryFile().list();
        
        // Figure out which files are sessions
        int keycount = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(FILE_EXT))
                keycount++;
        }
        
        return (keycount);

    }


    /**
     * Return the Manager with which the FileStore is associated.
     */
    public Manager getManager() {
    
        return (this.manager);
            
    }


    /**
     * Set the Manager with which this FileStore is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager) {

        Manager oldManager = this.manager;
        this.manager = manager;
        support.firePropertyChange("manager", oldManager, this.manager);

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

        String[] files = getDirectoryFile().list();

        // Figure out which files contain sessions
        int keycount = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(FILE_EXT))
                keycount++;
            else
                files[i] = null;
        }

        // Get keys from relevant filenames.
        String[] keys = new String[keycount];
        if (keycount > 0) {
            keycount = 0;
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null) {
                    keys[keycount] = files[i].substring (0, files[i].lastIndexOf('.'));
                    keycount++;
                }
            }
        }

        return (keys);

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

        // Open an input stream to the specified pathname, if any
        File file = file(id);
        if (file == null)
            return (null);
        if (debug >= 1)
            log(sm.getString("fileStore.loading", id, file.getAbsolutePath()));

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        try {
            fis = new FileInputStream(file.getAbsolutePath());
            BufferedInputStream bis = new BufferedInputStream(fis);
            Container container = manager.getContainer();
            if (container != null)
                loader = container.getLoader();
            if (loader != null)
                classLoader = loader.getClassLoader();
            if (classLoader != null)
                ois = new CustomObjectInputStream(bis, classLoader);
            else
                ois = new ObjectInputStream(bis);
        } catch (FileNotFoundException e) {
            if (debug >= 1)
                log("No persisted data file found");
            return (null);
        } catch (IOException e) {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
                ois = null;
            }
            throw e;
        }

        try {
            StandardSession session = (StandardSession) manager.createSession();
            session.readObjectData(ois);
            session.setManager(manager);
            return (session);
        } finally {
            // Close the input stream
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
            }
        }
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

        // Open an input stream to the specified pathname, if any
        File file = file(id);
        if (file == null)
            return;
        if (debug >= 1)
            log(sm.getString("fileStore.removing", id, file.getAbsolutePath()));
        file.delete();
    }


    /**
     * Remove all of the Sessions in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public void clear()
        throws IOException {

        String[] keys = keys();
        for (int i = 0; i < keys.length; i++) {
            remove(keys[i]);
        }

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

        // Open an output stream to the specified pathname, if any
        File file = file(session.getId());
        if (file == null)
            return;
        if (debug >= 1)
            log(sm.getString("fileStore.saving", session.getId(), file.getAbsolutePath()));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        } catch (IOException e) {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
            }
            throw e;
        }

        try {
            ((StandardSession)session).writeObjectData(oos);
        } finally {
            oos.close();
        }

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


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    void log(String message) {

        Logger logger = null;
        Container container = manager.getContainer();
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("Manager[" + container.getName() + "]: "
                       + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("Manager[" + containerName
                               + "]: " + message);
        }

    }


    // -------------------------------------------------------- Private Methods

    /**
     * Return a File object representing the pathname to our
     * session persistence file, if any.
     *
     * @param id The ID of the Session to be retrieved. This is
     *    used in the file naming.
     */
    private File file(String id) {

        if (directory == null)
            return (null);

        String pathname = directory + "/" + id + FILE_EXT;
        File file = new File(pathname);
        if (!file.isAbsolute()) {
            File tempdir = getDirectoryFile();
            if (tempdir != null)
                file = new File(tempdir, pathname);
        }
        return (file);

// FIXME: It would be nice to keep this check, but
// it doesn't work under Windows on paths that start
// with a drive letter.
//        if (!file.isAbsolute())
//            return (null);
//        return (file);

    }

    /**
     * Return a File object for the directory property.
     */
    private File getDirectoryFile() {
    
        if (directoryFile == null) {
            Container container = manager.getContainer();
            if (container instanceof Context) {
                ServletContext servletContext =
                    ((Context) container).getServletContext();
                directoryFile = (File)
                    servletContext.getAttribute(Globals.WORK_DIR_ATTR);
            } else {
                    throw new IllegalArgumentException("directory not set, I can't work with this Container");
            }
        }

        return directoryFile;

    }
                
    /**
     * Invalidate all sessions that have expired.
     */
    private void processExpires() {
    
        if(!started)
            return;

        long timeNow = System.currentTimeMillis();
        String[] keys = null;
        
        try {
            keys = keys();
        } catch (IOException e) {
            log (e.toString());
            e.printStackTrace();
            return;
        }
        
        for (int i = 0; i < keys.length; i++) {
            try {
                StandardSession session = (StandardSession) load(keys[i]);
                if (!session.isValid())
                    continue;
                int maxInactiveInterval = session.getMaxInactiveInterval();
                if (maxInactiveInterval < 0)
                    continue;
                int timeIdle = // Truncate, do not round up
                (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                if (timeIdle >= maxInactiveInterval) {
                    session.expire();
                    remove(session.getId());
                }
            } catch (IOException e) {
                    log (e.toString());
                    e.printStackTrace();
            } catch (ClassNotFoundException e) {
                    log (e.toString());
                    e.printStackTrace();
            }
        }

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

    // -------------------------------------------------------- Private Classes


    /**
     * Custom subclass of <code>ObjectInputStream</code> that loads from the
     * class loader for this web application.  This allows classes defined only
     * with the web application to be found correctly.
     */
    private static final class CustomObjectInputStream
        extends ObjectInputStream {


        /**
         * The class loader we will use to resolve classes.
         */
        private ClassLoader classLoader = null;


        /**
         * Construct a new instance of CustomObjectInputStream
         *
         * @param stream The input stream we will read from
         * @param classLoader The class loader used to instantiate objects
         *
         * @exception IOException if an input/output error occurs
         */
        public CustomObjectInputStream(InputStream stream,
                                       ClassLoader classLoader)
            throws IOException {

            super(stream);
            this.classLoader = classLoader;

        }


        /**
         * Load the local class equivalent of the specified stream class
         * description, by using the class loader assigned to this Context.
         *
         * @param classDesc Class description from the input stream
         *
         * @exception ClassNotFoundException if this class cannot be found
         * @exception IOException if an input/output error occurs
         */
        protected Class resolveClass(ObjectStreamClass classDesc)
            throws ClassNotFoundException, IOException {

            return (classLoader.loadClass(classDesc.getName()));

        }


    }

}
