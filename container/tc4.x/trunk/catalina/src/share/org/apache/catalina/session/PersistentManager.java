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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletContext;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.util.LifecycleSupport;


/**
 * Implementation of the <b>Manager</b> interface that makes use of
 * a Store to swap active Sessions to disk. It can be configured to
 * achieve several different goals:
 *
 * <li>Persist sessions across restarts of the Container</li>
 * <li>Fault tolerance, keep sessions backed up on disk to allow
 *     recovery in the event of unplanned restarts.</li>
 * <li>Limit the number of active sessions kept in memory by
 *     swapping less active sessions out to disk.</li>
 *
 * @version $Revision$
 * @author Kief Morris (kief@kief.com)
 */

public final class PersistentManager
    extends PersistentManagerBase
    implements Lifecycle, PropertyChangeListener, Runnable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "PersistentManager/1.0";


    /**
     * Whether to save and reload sessions when the Manager <code>unload</code>
     * and <code>load</code> methods are called.
     */
    private boolean saveOnRestart = true;
    
    
    /**
     * How long a session must be idle before it should be backed up.
     * -1 means sessions won't be backed up.
     */
    private int maxIdleBackup = -1;
    
    
    /**
     * Minimum time a session must be idle before it is swapped to disk.
     * This overrides maxActiveSessions, to prevent thrashing if there are lots
     * of active sessions. Setting to -1 means it's ignored.
     */
    private int minIdleSwap = -1;

    /**
     * The maximum time a session may be idle before it should be swapped
     * to file just on general principle. Setting this to -1 means sessions
     * should not be forced out.
     */
    private int maxIdleSwap = -1;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Indicates how many seconds old a session can get, after its last 
     * use in a request, before it should be backed up to the store. -1
     * means sessions are not backed up.
     */
    public int getMaxIdleBackup() {

        return maxIdleBackup;

    }
    
    
    /**
     * Sets the option to back sessions up to the Store after they
     * are used in a request. Sessions remain available in memory
     * after being backed up, so they are not passivated as they are 
     * when swapped out. The value set indicates how old a session 
     * may get (since its last use) before it must be backed up: -1 
     * means sessions are not backed up.
     * <p>
     * Note that this is not a hard limit: sessions are checked 
     * against this age limit periodically according to <b>checkInterval</b>.
     * This value should be considered to indicate when a session is
     * ripe for backing up.
     * <p>
     * So it is possible that a session may be idle for maxIdleBackup +
     * checkInterval seconds, plus the time it takes to handle other
     * session expiration, swapping, etc. tasks.
     *
     * @param backup The number of seconds after their last accessed
     * time when they should be written to the Store. 
     */
    public void setMaxIdleBackup (int backup) {

        if (backup == this.maxIdleBackup)
            return;
        int oldBackup = this.maxIdleBackup;
        this.maxIdleBackup = backup;
        support.firePropertyChange("maxIdleBackup",
                                   new Integer(oldBackup),
                                   new Integer(this.maxIdleBackup));
                                   
    }
    
    
    /**
     * The time in seconds after which a session should be swapped out of
     * memory to disk.
     */
    public int getMaxIdleSwap() {

        return maxIdleSwap;

    }
    
    
    /**
     * Sets the time in seconds after which a session should be swapped out of
     * memory to disk.
     */
    public void setMaxIdleSwap(int max) {

        if (max == this.maxIdleSwap)
            return;
        int oldMaxIdleSwap = this.maxIdleSwap;
        this.maxIdleSwap = max;
        support.firePropertyChange("maxIdleSwap",
                                   new Integer(oldMaxIdleSwap),
                                   new Integer(this.maxIdleSwap));

    }


    /**
     * The minimum time in seconds that a session must be idle before
     * it can be swapped out of memory, or -1 if it can be swapped out
     * at any time.
     */
    public int getMinIdleSwap() {

        return minIdleSwap;

    }
    
    
    /**
     * Sets the minimum time in seconds that a session must be idle before
     * it can be swapped out of memory due to maxActiveSession. Set it to -1 
     * if it can be swapped out at any time.
     */
    public void setMinIdleSwap(int min) {

        if (this.minIdleSwap == min)
            return;
        int oldMinIdleSwap = this.minIdleSwap;
        this.minIdleSwap = min;
        support.firePropertyChange("minIdleSwap",
                                   new Integer(oldMinIdleSwap),
                                   new Integer(this.minIdleSwap));

    }
    
    
    /**
     * Indicates whether sessions are saved when the Manager is shut down
     * properly. This requires the unload() method to be called.
     */
    public boolean getSaveOnRestart() {

        return saveOnRestart;

    }
    
    
    /**
     * Set the option to save sessions to the Store when the Manager is
     * shut down, then loaded when the Manager starts again. If set to 
     * false, any sessions found in the Store may still be picked up when 
     * the Manager is started again.
     *
     * @param save true if sessions should be saved on restart, false if
     *     they should be ignored.
     */
    public void setSaveOnRestart(boolean saveOnRestart) {

        if (saveOnRestart == this.saveOnRestart)
            return;

        boolean oldSaveOnRestart = this.saveOnRestart;
        this.saveOnRestart = saveOnRestart;
        support.firePropertyChange("saveOnRestart",
                                   new Boolean(oldSaveOnRestart),
                                   new Boolean(this.saveOnRestart));
                                   
    }
    
    
    // --------------------------------------------------------- Public Methods


    /**
     * Called by the background thread after active sessions have
     * been checked for expiration, to allow sessions to be
     * swapped out, backed up, etc.
     */
    public void processPersistenceChecks() {
    
            processMaxIdleSwaps();
            processMaxActiveSwaps();
            processMaxIdleBackups();

    }


    // ------------------------------------------------------ Lifecycle Methods


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

        if (debug >= 1)
            log("Stopping");

        // Validate and update our current component state
        if (!isStarted())
            throw new LifecycleException
                (sm.getString("standardManager.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        setStarted(false);

        // Stop the background reaper thread
        threadStop();

        if (getStore() != null && saveOnRestart) {
            unload();
        } else {
            // Expire all active sessions
            Session sessions[] = findSessions();
            for (int i = 0; i < sessions.length; i++) {
                StandardSession session = (StandardSession) sessions[i];
                if (!session.isValid())
                    continue;
                session.expire();
            }
        }
        
        if (getStore() != null && getStore() instanceof Lifecycle)
            ((Lifecycle)getStore()).stop();

        // Require a new random number generator if we are restarted
        this.random = null;

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Swap idle sessions out to Store if they are idle too long.
     */
    private void processMaxIdleSwaps() {

        if (!isStarted() || maxIdleSwap < 0)
            return;

        Session sessions[] = findSessions();
        long timeNow = System.currentTimeMillis();

        // Swap out all sessions idle longer than maxIdleSwap
        // FIXME: What's preventing us from mangling a session during
        // a request?
        if (maxIdleSwap >= 0) {
            for (int i = 0; i < sessions.length; i++) {
                StandardSession session = (StandardSession) sessions[i];
                if (!session.isValid())
                    continue;
                int timeIdle = // Truncate, do not round up
                    (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                if (timeIdle > maxIdleSwap && timeIdle > minIdleSwap) {
                    if (debug > 1)
                        log(sm.getString
                            ("persistentManager.swapMaxIdle", 
                             session.getId(), new Integer(timeIdle)));
                    try {
                        swapOut(session);
                    } catch (IOException e) {
                        ;   // This is logged in writeSession()
                    }
                }
            }
        }
        
    }


    /**
     * Swap idle sessions out to Store if too many are active
     */
    private void processMaxActiveSwaps() {

        if (!isStarted() || getMaxActiveSessions() < 0)
            return;

        Session sessions[] = findSessions();
        
        // FIXME: Smarter algorithm (LRU)
        if (getMaxActiveSessions() >= sessions.length)
            return;

        if(debug > 0)
            log(sm.getString
                ("persistentManager.tooManyActive", 
                 new Integer(sessions.length)));

        int toswap = sessions.length - getMaxActiveSessions();
        long timeNow = System.currentTimeMillis();
        
        for (int i = 0; i < sessions.length && toswap > 0; i++) {
            int timeIdle = // Truncate, do not round up
                (int) ((timeNow - sessions[i].getLastAccessedTime()) / 1000L);
            if (timeIdle > minIdleSwap) {
                if(debug > 1)
                    log(sm.getString
                        ("persistentManager.swapTooManyActive", 
                         sessions[i].getId(), new Integer(timeIdle)));
                try {
                    swapOut(sessions[i]);
                } catch (IOException e) {
                    ;   // This is logged in writeSession()
                }
                toswap--;
            }
        }

    }


    /**
     * Back up idle sessions.
     */
    private void processMaxIdleBackups() {

        if (!isStarted() || maxIdleBackup < 0)
            return;
        
        Session sessions[] = findSessions();
        long timeNow = System.currentTimeMillis();

        // Back up all sessions idle longer than maxIdleBackup
        if (maxIdleBackup >= 0) {
            for (int i = 0; i < sessions.length; i++) {
                StandardSession session = (StandardSession) sessions[i];
                if (!session.isValid())
                    continue;
                int timeIdle = // Truncate, do not round up
                    (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                if (timeIdle > maxIdleBackup) {
                    if (debug > 1)
                        log(sm.getString
                            ("persistentManager.backupMaxIdle",
                            session.getId(), new Integer(timeIdle)));
        
                    try {
                        writeSession(session);
                    } catch (IOException e) {
                        ;   // This is logged in writeSession()
                    }
                }
            }
        }

    }


}

