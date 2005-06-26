/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.cluster.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterManager;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;

/**
 * The DeltaManager manages replicated sessions by only replicating the deltas
 * in data. For applications written to handle this, the DeltaManager is the
 * optimal way of replicating data.
 * 
 * This code is almost identical to StandardManager with a difference in how it
 * persists sessions and some modifications to it.
 * 
 * <b>IMPLEMENTATION NOTE </b>: Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 * 
 * @author Filip Hanik
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @author Peter Rossbach
 * @version $Revision$ $Date$
 */

public class DeltaManager extends ManagerBase implements Lifecycle,
        PropertyChangeListener, ClusterManager {

    // ---------------------------------------------------- Security Classes

    public static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(DeltaManager.class);

    /**
     * The string manager for this package.
     */
    protected static StringManager sm = StringManager
            .getManager(Constants.Package);

    // ----------------------------------------------------- Instance Variables

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "DeltaManager/2.0";

    /**
     * Has this component been started yet?
     */
    private boolean started = false;

    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static String managerName = "DeltaManager";

    protected String name = null;
    
    private CatalinaCluster cluster = null;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    private int maxActiveSessions = -1;
    
    private boolean expireSessionsOnShutdown = false;

    private boolean notifyListenersOnReplication = true;

    private boolean notifySessionListenersOnReplication = true;

    private boolean stateTransferred = false ;

    private int stateTransferTimeout = 60;

    private boolean sendAllSessions = true;

    private boolean sendClusterDomainOnly = true ;
    
    private int sendAllSessionsSize = 1000 ;
    
    /**
     * wait time between send session block (default 2 sec) 
     */
    private int sendAllSessionsWaitTime = 2 * 1000 ; 

    private ArrayList receivedMessageQueue = new ArrayList() ;
    
    private boolean receiverQueue = false ;

    private boolean stateTimestampDrop = true ;

    private long stateTransferCreateSendTime; 
    
    // ------------------------------------------------------------------ stats attributes
    
    int rejectedSessions = 0;

    private long sessionReplaceCounter = 0 ;

    long processingTime = 0;

    private long counterReceive_EVT_GET_ALL_SESSIONS = 0 ;

    private long counterSend_EVT_ALL_SESSION_DATA = 0 ;

    private long counterReceive_EVT_ALL_SESSION_DATA = 0 ;

    private long counterReceive_EVT_SESSION_CREATED = 0 ;

    private long counterReceive_EVT_SESSION_EXPIRED = 0;

    private long counterReceive_EVT_SESSION_ACCESSED = 0 ;

    private long counterReceive_EVT_SESSION_DELTA = 0;

    private long counterSend_EVT_GET_ALL_SESSIONS = 0 ;

    private long counterSend_EVT_SESSION_CREATED = 0;

    private long counterSend_EVT_SESSION_DELTA = 0 ;

    private long counterSend_EVT_SESSION_ACCESSED = 0;

    private long counterSend_EVT_SESSION_EXPIRED = 0;

    private int counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0 ;

    private int counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0 ;

    private int counterNoStateTransfered = 0 ;


    // ------------------------------------------------------------- Constructor
  
    public DeltaManager() {
        super();
    }

    // ------------------------------------------------------------- Properties
    
    /**
     * Return descriptive information about this Manager implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }

    /**
     * @return Returns the counterSend_EVT_GET_ALL_SESSIONS.
     */
    public long getCounterSend_EVT_GET_ALL_SESSIONS() {
        return counterSend_EVT_GET_ALL_SESSIONS;
    }
    
    /**
     * @return Returns the counterSend_EVT_SESSION_ACCESSED.
     */
    public long getCounterSend_EVT_SESSION_ACCESSED() {
        return counterSend_EVT_SESSION_ACCESSED;
    }
    
    /**
     * @return Returns the counterSend_EVT_SESSION_CREATED.
     */
    public long getCounterSend_EVT_SESSION_CREATED() {
        return counterSend_EVT_SESSION_CREATED;
    }

    /**
     * @return Returns the counterSend_EVT_SESSION_DELTA.
     */
    public long getCounterSend_EVT_SESSION_DELTA() {
        return counterSend_EVT_SESSION_DELTA;
    }

    /**
     * @return Returns the counterSend_EVT_SESSION_EXPIRED.
     */
    public long getCounterSend_EVT_SESSION_EXPIRED() {
        return counterSend_EVT_SESSION_EXPIRED;
    }
 
    /**
     * @return Returns the counterSend_EVT_ALL_SESSION_DATA.
     */
    public long getCounterSend_EVT_ALL_SESSION_DATA() {
        return counterSend_EVT_ALL_SESSION_DATA;
    }

    /**
     * @return Returns the counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE.
     */
    public int getCounterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE() {
        return counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE;
    }
 
    /**
     * @return Returns the counterReceive_EVT_ALL_SESSION_DATA.
     */
    public long getCounterReceive_EVT_ALL_SESSION_DATA() {
        return counterReceive_EVT_ALL_SESSION_DATA;
    }
    
    /**
     * @return Returns the counterReceive_EVT_GET_ALL_SESSIONS.
     */
    public long getCounterReceive_EVT_GET_ALL_SESSIONS() {
        return counterReceive_EVT_GET_ALL_SESSIONS;
    }
    
    /**
     * @return Returns the counterReceive_EVT_SESSION_ACCESSED.
     */
    public long getCounterReceive_EVT_SESSION_ACCESSED() {
        return counterReceive_EVT_SESSION_ACCESSED;
    }
    
    /**
     * @return Returns the counterReceive_EVT_SESSION_CREATED.
     */
    public long getCounterReceive_EVT_SESSION_CREATED() {
        return counterReceive_EVT_SESSION_CREATED;
    }
    
    /**
     * @return Returns the counterReceive_EVT_SESSION_DELTA.
     */
    public long getCounterReceive_EVT_SESSION_DELTA() {
        return counterReceive_EVT_SESSION_DELTA;
    }
    
    /**
     * @return Returns the counterReceive_EVT_SESSION_EXPIRED.
     */
    public long getCounterReceive_EVT_SESSION_EXPIRED() {
        return counterReceive_EVT_SESSION_EXPIRED;
    }
    
    
    /**
     * @return Returns the counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE.
     */
    public int getCounterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE() {
        return counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE;
    }
    
    /**
     * @return Returns the processingTime.
     */
    public long getProcessingTime() {
        return processingTime;
    }
 
    /**
     * @return Returns the sessionReplaceCounter.
     */
    public long getSessionReplaceCounter() {
        return sessionReplaceCounter;
    }
    
    /**
     * Number of session creations that failed due to maxActiveSessions
     * 
     * @return The count
     */
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }

    /**
     * @return Returns the counterNoStateTransfered.
     */
    public int getCounterNoStateTransfered() {
        return counterNoStateTransfered;
    }
    
    public int getReceivedQueueSize() {
        return receivedMessageQueue.size() ;
    }
    
    /**
     * @return Returns the stateTransferTimeout.
     */
    public int getStateTransferTimeout() {
        return stateTransferTimeout;
    }
    /**
     * @param stateTransferTimeout The stateTransferTimeout to set.
     */
    public void setStateTransferTimeout(int timeoutAllSession) {
        this.stateTransferTimeout = timeoutAllSession;
    }

    public boolean getStateTransferred() {
        return stateTransferred;
    }

    public void setStateTransferred(boolean stateTransferred) {
        this.stateTransferred = stateTransferred;
    }
    
    /**
     * @return Returns the sendAllSessionsWaitTime in msec
     */
    public int getSendAllSessionsWaitTime() {
        return sendAllSessionsWaitTime;
    }
    
    /**
     * @param sendAllSessionsWaitTime The sendAllSessionsWaitTime to set at msec.
     */
    public void setSendAllSessionsWaitTime(int sendAllSessionsWaitTime) {
        this.sendAllSessionsWaitTime = sendAllSessionsWaitTime;
    }
    
    /**
     * @return Returns the sendClusterDomainOnly.
     */
    public boolean isSendClusterDomainOnly() {
        return sendClusterDomainOnly;
    }
    
    /**
     * @param sendClusterDomainOnly The sendClusterDomainOnly to set.
     */
    public void setSendClusterDomainOnly(boolean sendClusterDomainOnly) {
        this.sendClusterDomainOnly = sendClusterDomainOnly;
    }

    /**
     * @return Returns the stateTimestampDrop.
     */
    public boolean isStateTimestampDrop() {
        return stateTimestampDrop;
    }
    
    /**
     * @param stateTimestampDrop The stateTimestampDrop to set.
     */
    public void setStateTimestampDrop(boolean isTimestampDrop) {
        this.stateTimestampDrop = isTimestampDrop;
    }
    
    /**
     * Return the maximum number of active Sessions allowed, or -1 for no limit.
     */
    public int getMaxActiveSessions() {

        return (this.maxActiveSessions);

    }

    /**
     * Set the maximum number of actives Sessions allowed, or -1 for no limit.
     * 
     * @param max
     *            The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {

        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        support.firePropertyChange("maxActiveSessions", new Integer(
                oldMaxActiveSessions), new Integer(this.maxActiveSessions));

    }
    
    /**
     * @return Returns the sendAllSessions.
     */
    public boolean isSendAllSessions() {
        return sendAllSessions;
    }
    
    /**
     * @param sendAllSessions The sendAllSessions to set.
     */
    public void setSendAllSessions(boolean sendAllSessions) {
        this.sendAllSessions = sendAllSessions;
    }
    
    /**
     * @return Returns the sendAllSessionsSize.
     */
    public int getSendAllSessionsSize() {
        return sendAllSessionsSize;
    }
    
    /**
     * @param sendAllSessionsSize The sendAllSessionsSize to set.
     */
    public void setSendAllSessionsSize(int sendAllSessionsSize) {
        this.sendAllSessionsSize = sendAllSessionsSize;
    }
    
    /**
     * @return Returns the notifySessionListenersOnReplication.
     */
    public boolean isNotifySessionListenersOnReplication() {
        return notifySessionListenersOnReplication;
    }
    
    /**
     * @param notifySessionListenersOnReplication The notifySessionListenersOnReplication to set.
     */
    public void setNotifySessionListenersOnReplication(
            boolean notifyListenersCreateSessionOnReplication) {
        this.notifySessionListenersOnReplication = notifyListenersCreateSessionOnReplication;
    }
    
    
    public boolean isExpireSessionsOnShutdown() {
        return expireSessionsOnShutdown;
    }

    public void setExpireSessionsOnShutdown(boolean expireSessionsOnShutdown) {
        this.expireSessionsOnShutdown = expireSessionsOnShutdown;
    }
    
    public boolean isNotifyListenersOnReplication() {
        return notifyListenersOnReplication;
    }

    public void setNotifyListenersOnReplication(
            boolean notifyListenersOnReplication) {
        this.notifyListenersOnReplication = notifyListenersOnReplication;
    }

    public CatalinaCluster getCluster() {
        return cluster;
    }

    public void setCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Set the Container with which this Manager has been associated. If it is a
     * Context (the usual case), listen for changes to the session timeout
     * property.
     * 
     * @param container
     *            The associated Container
     */
    public void setContainer(Container container) {

        // De-register from the old Container (if any)
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);

        // Default processing provided by our superclass
        super.setContainer(container);

        // Register with the new Container (if any)
        if ((this.container != null) && (this.container instanceof Context)) {
            setMaxInactiveInterval(((Context) this.container)
                    .getSessionTimeout() * 60);
            ((Context) this.container).addPropertyChangeListener(this);
        }

    }
    
    // --------------------------------------------------------- Public Methods

    /**
     * Construct and return a new session object, based on the default settings
     * specified by this Manager's properties. The session id will be assigned
     * by this method, and available via the getId() method of the returned
     * session. If a new session cannot be created for any reason, return
     * <code>null</code>.
     * 
     * @exception IllegalStateException
     *                if a new session cannot be instantiated for any reason
     * 
     * Construct and return a new session object, based on the default settings
     * specified by this Manager's properties. The session id will be assigned
     * by this method, and available via the getId() method of the returned
     * session. If a new session cannot be created for any reason, return
     * <code>null</code>.
     * 
     * @exception IllegalStateException
     *                if a new session cannot be instantiated for any reason
     */
    public Session createSession(String sessionId) {
        return createSession(sessionId, true);
    }

    /**
     * create new session with check maxActiveSessions and send session creation
     * to other cluster nodes.
     * 
     * @param distribute
     * @return
     */
    public Session createSession(String sessionId, boolean distribute) {

        if ((maxActiveSessions >= 0) && (sessions.size() >= maxActiveSessions)) {
            rejectedSessions++;
            throw new IllegalStateException(sm
                    .getString("deltaManager.createSession.ise"));
        }

        DeltaSession session = (DeltaSession) super.createSession(sessionId) ;
        session.resetDeltaRequest();
        if (distribute) {
            sendCreateSession(session.getId(), session);
        }
        if (log.isDebugEnabled())
            log.debug(sm.getString("deltaManager.createSession.newSession",
                    session.getId(), new Integer(sessions.size())));

        return (session);

    }

    /**
     * Send create session evt to all backup node
     * @param sessionId
     * @param session
     */
    protected void sendCreateSession(String sessionId, DeltaSession session) {
        if(cluster.getMembers().length > 0 ) {
            SessionMessage msg = new SessionMessageImpl(getName(),
                    SessionMessage.EVT_SESSION_CREATED, null, sessionId,
                    sessionId + System.currentTimeMillis());
            if (log.isDebugEnabled())
                log.debug(sm.getString("deltaManager.sendMessage.newSession",
                        name, sessionId));
            counterSend_EVT_SESSION_CREATED++;
            send(msg);
        }
        session.resetDeltaRequest();
    }
    
    /**
     * Send messages to other backup member (domain or all)
     * @param msg Session message
     */
    protected void send(SessionMessage msg) {
        if(isSendClusterDomainOnly())
            cluster.sendClusterDomain(msg);
        else
            cluster.send(msg);
    }

    /**
     * Create DeltaSession
     * @see org.apache.catalina.Manager#createEmptySession()
     */
    public Session createEmptySession() {
        return getNewDeltaSession() ;
    }
    
    /**
     * Get new session class to be used in the doLoad() method.
     */
    protected DeltaSession getNewDeltaSession() {
        return new DeltaSession(this);
    }

    /**
     * Load Deltarequest from external node
     * Load the Class at container classloader
     * @see DeltaRequest#readExternal(java.io.ObjectInput)
     * @param session
     * @param data message data
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    protected DeltaRequest loadDeltaRequest(DeltaSession session, byte[] data)
            throws ClassNotFoundException, IOException {
        ByteArrayInputStream fis = null;
        ReplicationStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        //fix to be able to run the DeltaManager
        //stand alone without a container.
        //use the Threads context class loader
        if (container != null)
            loader = container.getLoader();
        if (loader != null)
            classLoader = loader.getClassLoader();
        else
            classLoader = Thread.currentThread().getContextClassLoader();
        //end fix
        fis = new ByteArrayInputStream(data);
        ois = new ReplicationStream(fis, classLoader);
        session.getDeltaRequest().readExternal(ois);
        ois.close();
        return session.getDeltaRequest();
    }

    /**
     * serialize DeltaRequest
     * @see DeltaRequest#writeExternal(java.io.ObjectOutput)
     * 
     * @param deltaRequest
     * @return serialized delta request
     * @throws IOException
     */
    protected byte[] unloadDeltaRequest(DeltaRequest deltaRequest)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        deltaRequest.writeExternal(oos);
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }

    /**
     * Load sessions from other cluster node.
     * FIXME replace currently sessions with same id without notifcation.
     * FIXME SSO handling is not really correct with the session replacement!
     * @exception ClassNotFoundException
     *                if a serialized class cannot be found during the reload
     * @exception IOException
     *                if an input/output error occurs
     */
    protected void deserializeSessions(byte[] data) throws ClassNotFoundException,
            IOException {

        // Initialize our internal data structures
        //sessions.clear(); //should not do this
        // Open an input stream to the specified pathname, if any
        ClassLoader originalLoader = Thread.currentThread()
                .getContextClassLoader();
        ObjectInputStream ois = null;
        // Load the previously unloaded active sessions
        try {
            ois = openDeserializeObjectStream(data);
            Integer count = (Integer) ois.readObject();
            int n = count.intValue();
            for (int i = 0; i < n; i++) {
                DeltaSession session = (DeltaSession) createEmptySession();
                session.readObjectData(ois);
                session.setManager(this);
                session.setValid(true);
                session.setPrimarySession(false);
                //in case the nodes in the cluster are out of
                //time synch, this will make sure that we have the
                //correct timestamp, isValid returns true, cause
                // accessCount=1
                session.access();
                //make sure that the session gets ready to expire if
                // needed
                session.setAccessCount(0);
                session.resetDeltaRequest();
                // FIXME How inform other session id cache like SingleSignOn
                // increment sessionCounter to correct stats report
                if (findSession(session.getIdInternal()) != null ) {
                    sessionCounter++;
                } else {
                    sessionReplaceCounter++;
                    // FIXME better is to grap this sessions again !
                    if (log.isWarnEnabled())
                        log.warn(sm.getString(
                                "deltaManager.loading.existing.session",
                                session.getIdInternal()));
                }
                add(session);
            }
        } catch (ClassNotFoundException e) {
            log.error(sm.getString("deltaManager.loading.cnfe", e), e);
            throw e;
        } catch (IOException e) {
            log.error(sm.getString("deltaManager.loading.ioe", e), e);
            throw e;
        } finally {
            // Close the input stream
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException f) {
                // ignored
            }
            ois = null;
            if (originalLoader != null)
                Thread.currentThread().setContextClassLoader(originalLoader);
        }

    }

    /**
     * Open Stream and use correct ClassLoader (Container) Switch
     * ThreadClassLoader
     * 
     * @param data
     * @return
     * @throws IOException
     */
    protected ObjectInputStream openDeserializeObjectStream(byte[] data) throws IOException {
        ObjectInputStream ois = null;
        ByteArrayInputStream fis = null;
        try {
            Loader loader = null;
            ClassLoader classLoader = null;
            fis = new ByteArrayInputStream(data);
            BufferedInputStream bis = new BufferedInputStream(fis);
            if (container != null)
                loader = container.getLoader();
            if (loader != null)
                classLoader = loader.getClassLoader();
            if (classLoader != null) {
                if (log.isTraceEnabled())
                    log.trace(sm.getString(
                            "deltaManager.loading.withContextClassLoader",
                            getName()));
                ois = new CustomObjectInputStream(bis, classLoader);
                Thread.currentThread().setContextClassLoader(classLoader);
            } else {
                if (log.isTraceEnabled())
                    log.trace(sm.getString(
                            "deltaManager.loading.withoutClassLoader",
                            getName()));
                ois = new ObjectInputStream(bis);
            }
        } catch (IOException e) {
            log.error(sm.getString("deltaManager.loading.ioe", e), e);
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
        return ois;
    }

    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any. If persistence is not supported, this method returns
     * without doing anything.
     * 
     * @exception IOException
     *                if an input/output error occurs
     */
    protected byte[] serializeSessions(Session[] currentSessions) throws IOException {

        // Open an output stream to the specified pathname, if any
        ByteArrayOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(new Integer(currentSessions.length));
            for(int i=0 ; i < currentSessions.length;i++) {
                ((DeltaSession)currentSessions[i]).writeObjectData(oos);                
            }
            // Flush and close the output stream
            oos.flush();
        } catch (IOException e) {
            log.error(sm.getString("deltaManager.unloading.ioe", e), e);
            throw e;
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
        }
        // send object data as byte[]
        return fos.toByteArray();
    }

    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Add a lifecycle event listener to this component.
     * 
     * @param listener
     *            The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }

    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }

    /**
     * Remove a lifecycle event listener from this component.
     * 
     * @param listener
     *            The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component. This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     * 
     * @exception LifecycleException
     *                if this component detects a fatal error that prevents this
     *                component from being used
     */
    public void start() throws LifecycleException {
        if (!initialized)
            init();

        // Validate and update our current component state
        if (started) {
            return;
        }
        started = true;
        lifecycle.fireLifecycleEvent(START_EVENT, null);

        // Force initialization of the random number generator
        String dummy = generateSessionId();

        // Load unloaded sessions, if any
        try {
            //the channel is already running
            Cluster cluster = getCluster() ;
            // stop remove cluster binding
            if(cluster == null) {
                Container context = getContainer() ;
                if(context != null && context instanceof Context) {
                     Container host = context.getParent() ;
                     if(host != null && host instanceof Host) {
                         cluster = host.getCluster();
                         if(cluster != null && cluster instanceof CatalinaCluster) {
                             setCluster((CatalinaCluster) cluster) ;
                         } else {
                             Container engine = host.getParent() ;
                             if(engine != null && engine instanceof Engine) {
                                 cluster = engine.getCluster();
                                 if(cluster != null && cluster instanceof CatalinaCluster) {
                                     setCluster((CatalinaCluster) cluster) ;
                                 }
                             } else {
                                     cluster = null ;
                             }
                         }
                     }
                }
            }
            if (cluster == null) {
                log.error(sm.getString("deltaManager.noCluster", getName()));
                return;
            }
            if (log.isInfoEnabled())
                log.info(sm
                        .getString("deltaManager.startClustering", getName()));
            //to survice context reloads, as only a stop/start is called, not
            // createManager
            ((CatalinaCluster)cluster).addManager(getName(), this);

            getAllClusterSessions();

        } catch (Throwable t) {
            log.error(sm.getString("deltaManager.managerLoad"), t);
        }
    }

    /**
     * get from first session master the backup from all clustered sessions
     * @see #findSessionMasterMember()
     */
    public synchronized void getAllClusterSessions() {
        if (cluster != null && cluster.getMembers().length > 0) {
            long beforeSendTime = System.currentTimeMillis();
            Member mbr = findSessionMasterMember();
            if(mbr == null) { // No domain member found
                 return;
            }
            SessionMessage msg = new SessionMessageImpl(this.getName(),
                    SessionMessage.EVT_GET_ALL_SESSIONS, null, "GET-ALL",
                    "GET-ALL-" + getName());
            msg.setResend(ClusterMessage.FLAG_FORBIDDEN);
            // set reference time
            msg.setTimestamp(beforeSendTime);
            stateTransferCreateSendTime = beforeSendTime ;
            // request session state
            counterSend_EVT_GET_ALL_SESSIONS++;
            stateTransferred = false ;
            // FIXME This send call block the deploy thread, when sender waitForAck is enabled
            try {
                synchronized(receivedMessageQueue) {
                     receiverQueue = true ;
                }
                cluster.send(msg, mbr);
                if (log.isWarnEnabled())
                    log.warn(sm.getString("deltaManager.waitForSessionState",
                            getName(), mbr));
                // FIXME At sender ack mode this method check only the state transfer and resend is a problem!
                waitForSendAllSessions(beforeSendTime);
            } finally {
                synchronized(receivedMessageQueue) {
                    for (Iterator iter = receivedMessageQueue.iterator(); iter
                            .hasNext();) {
                        SessionMessage smsg = (SessionMessage) iter.next();
                        if (!stateTimestampDrop) {
                            messageReceived(smsg,
                                    smsg.getAddress() != null ? (Member) smsg
                                            .getAddress() : null);
                        } else {
                            if (smsg.getEventType() != SessionMessage.EVT_GET_ALL_SESSIONS
                                    && smsg.getTimestamp() >= stateTransferCreateSendTime) {
                                // FIXME handle EVT_GET_ALL_SESSIONS later
                                messageReceived(
                                        smsg,
                                        smsg.getAddress() != null ? (Member) smsg
                                                .getAddress()
                                                : null);
                            } else {
                                if (log.isWarnEnabled()) {
                                    log.warn(sm.getString(
                                            "deltaManager.dropMessage",
                                            getName(), smsg
                                                    .getEventTypeString(),
                                            new Date(stateTransferCreateSendTime), new Date(
                                                    smsg.getTimestamp())));
                                }
                            }
                        }
                    }        
                    receivedMessageQueue.clear();
                    receiverQueue = false ;
                }
           }
        } else {
            if (log.isInfoEnabled())
                log.info(sm.getString("deltaManager.noMembers", getName()));
        }
    }

    /**
     * Find the master �f the session state
     * @return master member of sessions 
     */
    protected Member findSessionMasterMember() {
        Member mbr = null;
        Member mbrs[] = cluster.getMembers();
        String localMemberDomain = cluster.getMembershipService().getLocalMember().getDomain();
        if(isSendClusterDomainOnly()) {
            for (int i = 0; mbr == null && i < mbrs.length; i++) {
                Member member = mbrs[i];
                if(localMemberDomain.equals(member.getDomain()))
                    mbr = member ;
            }
        } else {
            // FIXME Why only the first Member?
            if(mbrs.length != 0 )
                mbr = mbrs[0];
        }
        if(mbr == null && log.isWarnEnabled())
           log.warn(sm.getString("deltaManager.noMasterMember",
                    getName(), localMemberDomain));
        if(mbr != null && log.isDebugEnabled())
            log.warn(sm.getString("deltaManager.foundMasterMember",
                     getName(), mbr));
        return mbr;
    }

    /**
     * Wait that cluster session state is transfer or timeout after 60 Sec
     * With stateTransferTimeout == -1 wait that backup is transfered (forever mode)
     */
    protected void waitForSendAllSessions(long beforeSendTime) {
        long reqStart = System.currentTimeMillis();
        long reqNow = reqStart ;
        boolean isTimeout = false;
        if(getStateTransferTimeout() > 0) {
            // wait that state is transfered with timeout check
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception sleep) {
                }
                reqNow = System.currentTimeMillis();
                isTimeout = ((reqNow - reqStart) > (1000 * getStateTransferTimeout()));
            } while ((!getStateTransferred()) && (!isTimeout));
        } else {
            if(getStateTransferTimeout() == -1) {
                // wait that state is transfered
                do {
                    try {
                        Thread.sleep(100);
                    } catch (Exception sleep) {
                    }
                } while ((!getStateTransferred()));
                reqNow = System.currentTimeMillis();
            }
        }
        if (isTimeout || (!getStateTransferred())) {
            counterNoStateTransfered++ ;
            log.error(sm.getString("deltaManager.noSessionState",
                    getName(),new Date(beforeSendTime),new Long(reqNow - beforeSendTime)));
        } else {
            if (log.isInfoEnabled())
                log.info(sm.getString("deltaManager.sessionReceived",
                        getName(), new Date(beforeSendTime), new Long(reqNow - beforeSendTime)));
        }
    }

    /**
     * Gracefully terminate the active use of the public methods of this
     * component. This method should be the last one called on a given instance
     * of this component.
     * 
     * @exception LifecycleException
     *                if this component detects a fatal error that needs to be
     *                reported
     */
    public void stop() throws LifecycleException {

        if (log.isDebugEnabled())
            log.debug(sm.getString("deltaManager.stopped", getName()));

        getCluster().removeManager(getName());

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException(sm
                    .getString("deltaManager.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Expire all active sessions
        if (log.isInfoEnabled())
            log.info(sm.getString("deltaManager.expireSessions", getName()));
        Session sessions[] = findSessions();
        for (int i = 0; i < sessions.length; i++) {
            DeltaSession session = (DeltaSession) sessions[i];
            if (!session.isValid())
                continue;
            try {
                session.expire(true, isExpireSessionsOnShutdown());
            } catch (Throwable ignore) {
                ;
            } 
        }

        // Require a new random number generator if we are restarted
        this.random = null;

        if (initialized) {
            destroy();
        }
    }

    // ----------------------------------------- PropertyChangeListener Methods

    /**
     * Process property change events from our associated Context.
     * 
     * @param event
     *            The property change event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        // Validate the source of this event
        if (!(event.getSource() instanceof Context))
            return;
        Context context = (Context) event.getSource();

        // Process a relevant property change
        if (event.getPropertyName().equals("sessionTimeout")) {
            try {
                setMaxInactiveInterval(((Integer) event.getNewValue())
                        .intValue() * 60);
            } catch (NumberFormatException e) {
                log.error(sm.getString("deltaManager.sessionTimeout", event
                        .getNewValue()));
            }
        }

    }

    // -------------------------------------------------------- Replication
    // Methods

    /**
     * A message was received from another node, this is the callback method to
     * implement if you are interested in receiving replication messages.
     * 
     * @param msg -
     *            the message received.
     */
    public void messageDataReceived(ClusterMessage cmsg) {
        if (cmsg != null && cmsg instanceof SessionMessage) {
            SessionMessage msg = (SessionMessage) cmsg;
            switch (msg.getEventType()) {
            case SessionMessage.EVT_GET_ALL_SESSIONS:
            case SessionMessage.EVT_SESSION_CREATED: 
            case SessionMessage.EVT_SESSION_EXPIRED: 
            case SessionMessage.EVT_SESSION_ACCESSED:
            case SessionMessage.EVT_SESSION_DELTA: {
                synchronized(receivedMessageQueue) {
                    if(receiverQueue) {
                        receivedMessageQueue.add(msg);
                        return ;
                    }
                }
               break;
            }
            default: {
                //we didn't queue, do nothing
                break;
            }
            } //switch
            
            messageReceived(msg, msg.getAddress() != null ? (Member) msg
                    .getAddress() : null);
        }
    }

    /**
     * When the request has been completed, the replication valve will notify
     * the manager, and the manager will decide whether any replication is
     * needed or not. If there is a need for replication, the manager will
     * create a session message and that will be replicated. The cluster
     * determines where it gets sent.
     * 
     * @param sessionId -
     *            the sessionId that just completed.
     * @return a SessionMessage to be sent,
     */
    public ClusterMessage requestCompleted(String sessionId) {
        try {
            DeltaSession session = (DeltaSession) findSession(sessionId);
            DeltaRequest deltaRequest = session.getDeltaRequest();
            SessionMessage msg = null;
            if (deltaRequest.getSize() > 0) {

                counterSend_EVT_SESSION_DELTA++;
                byte[] data = unloadDeltaRequest(deltaRequest);
                msg = new SessionMessageImpl(name,
                        SessionMessage.EVT_SESSION_DELTA, data, sessionId,
                        sessionId + System.currentTimeMillis());
                session.resetDeltaRequest();
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString(
                            "deltaManager.createMessage.delta",
                            getName(), sessionId));
                }
                
            } else if (!session.isPrimarySession()) {
                counterSend_EVT_SESSION_ACCESSED++;
                msg = new SessionMessageImpl(getName(),
                        SessionMessage.EVT_SESSION_ACCESSED, null, sessionId,
                        sessionId + System.currentTimeMillis());
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString(
                            "deltaManager.createMessage.accessChangePrimary",
                            getName(), sessionId));
                }
            }
            session.setPrimarySession(true);
            //check to see if we need to send out an access message
            if ((msg == null)) {
                long replDelta = System.currentTimeMillis()
                        - session.getLastTimeReplicated();
                if (replDelta > (getMaxInactiveInterval() * 1000)) {
                    counterSend_EVT_SESSION_ACCESSED++;
                    msg = new SessionMessageImpl(getName(),
                            SessionMessage.EVT_SESSION_ACCESSED, null,
                            sessionId, sessionId + System.currentTimeMillis());
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString(
                                "deltaManager.createMessage.access", getName(),
                                sessionId));
                    }
                }

            }

            //update last replicated time
            if (msg != null)
                session.setLastTimeReplicated(System.currentTimeMillis());
            return msg;
        } catch (IOException x) {
            log.error(sm.getString(
                    "deltaManager.createMessage.unableCreateDeltaRequest",
                    sessionId), x);
            return null;
        }

    }
    /**
     * Reset manager statistics
     */
    public synchronized void resetStatistics() {
        processingTime = 0 ;
        expiredSessions = 0 ;
        rejectedSessions = 0 ;
        sessionReplaceCounter = 0 ;
        counterNoStateTransfered = 0 ;
        maxActive = getActiveSessions() ;
        sessionCounter = getActiveSessions() ;
        counterReceive_EVT_ALL_SESSION_DATA = 0;
        counterReceive_EVT_GET_ALL_SESSIONS = 0;
        counterReceive_EVT_SESSION_ACCESSED = 0 ;
        counterReceive_EVT_SESSION_CREATED = 0 ;
        counterReceive_EVT_SESSION_DELTA = 0 ;
        counterReceive_EVT_SESSION_EXPIRED = 0 ;
        counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0;
        counterSend_EVT_ALL_SESSION_DATA = 0;
        counterSend_EVT_GET_ALL_SESSIONS = 0;
        counterSend_EVT_SESSION_ACCESSED = 0 ;
        counterSend_EVT_SESSION_CREATED = 0 ;
        counterSend_EVT_SESSION_DELTA = 0 ;
        counterSend_EVT_SESSION_EXPIRED = 0 ;
        counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0;
        
    }
   
    //  -------------------------------------------------------- persistence handler

    public void load() {

    }

    public void unload() {

    }

    //  -------------------------------------------------------- expire

    /**
     * send session expired to other cluster nodes
     * 
     * @param id
     *            session id
     */
    protected void sessionExpired(String id) {
        counterSend_EVT_SESSION_EXPIRED++ ;
        SessionMessage msg = new SessionMessageImpl(getName(),
                SessionMessage.EVT_SESSION_EXPIRED, null, id, id
                        + "-EXPIRED-MSG");
        if (log.isDebugEnabled())
            log.debug(sm.getString("deltaManager.createMessage.expire",
                    getName(), id));
        send(msg);
    }

    /**
     * Exipre all find sessions.
     */
    public void expireAllLocalSessions()
    {
        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();
        int expireDirect  = 0 ;
        int expireIndirect = 0 ;
        
        if(log.isDebugEnabled())
            log.debug("Start expire all sessions " + getName() + " at " + timeNow + " sessioncount " + sessions.length);
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i] instanceof DeltaSession) {
                DeltaSession session = (DeltaSession) sessions[i];
                if (session.isPrimarySession()) {
                    if (session.isValid()) {
                        session.expire();
                        expireDirect++;
                    } else {
                        expireIndirect++;
                    }
                }
            }
        }
        long timeEnd = System.currentTimeMillis();
        if(log.isDebugEnabled())
             log.debug("End expire sessions " + getName() + " exipre processingTime " + (timeEnd - timeNow) + " expired direct sessions: " + expireDirect + " expired direct sessions: " + expireIndirect);
      
    }
    
    /**
     * When the manager expires session not tied to a request. The cluster will
     * periodically ask for a list of sessions that should expire and that
     * should be sent across the wire.
     * 
     * @return
     */
    public String[] getInvalidatedSessions() {
        return new String[0];
    }

    //  -------------------------------------------------------- message receive

    /**
     * Test that sender and local domain is the same
     */
    protected boolean checkSenderDomain(SessionMessage msg,Member sender) {
        String localMemberDomain = cluster.getMembershipService().getLocalMember().getDomain();
        boolean sameDomain= localMemberDomain.equals(sender.getDomain());
        if (!sameDomain && log.isWarnEnabled()) {
                log.warn(sm.getString("deltaManager.receiveMessage.fromWrongDomain",
                        new Object[] {getName(), 
                        msg.getEventTypeString(), 
                        sender,
                        sender.getDomain(),
                        localMemberDomain }
                ));
        }
        return sameDomain ;
    }

    /**
     * This method is called by the received thread when a SessionMessage has
     * been received from one of the other nodes in the cluster.
     * 
     * @param msg -
     *            the message received
     * @param sender -
     *            the sender of the message, this is used if we receive a
     *            EVT_GET_ALL_SESSION message, so that we only reply to the
     *            requesting node
     */
    protected void messageReceived(SessionMessage msg, Member sender) {
        if(isSendClusterDomainOnly() && !checkSenderDomain(msg,sender)) {
            return;
        }
        try {
            if (log.isDebugEnabled())
                log.debug(sm.getString("deltaManager.receiveMessage.eventType",
                        getName(), msg.getEventTypeString(), sender));
 
            switch (msg.getEventType()) {
            case SessionMessage.EVT_GET_ALL_SESSIONS: {
                handleGET_ALL_SESSIONS(msg,sender);
                break;
            }
            case SessionMessage.EVT_ALL_SESSION_DATA: {
                handleALL_SESSION_DATA(msg,sender);
                break;
            }
            case SessionMessage.EVT_ALL_SESSION_TRANSFERCOMPLETE: {
                handleALL_SESSION_TRANSFERCOMPLETE(msg,sender);
                break;
            }
            case SessionMessage.EVT_SESSION_CREATED: {
                handleSESSION_CREATED(msg,sender);
                break;
            }
            case SessionMessage.EVT_SESSION_EXPIRED: {
                handleSESSION_EXPIRRED(msg,sender);
                break;
            }
            case SessionMessage.EVT_SESSION_ACCESSED: {
                handleSESSION_ACCESSED(msg,sender);
                break;
            }
            case SessionMessage.EVT_SESSION_DELTA: {
               handleSESSION_DELTA(msg,sender);
               break;
            }
            default: {
                //we didn't recognize the message type, do nothing
                break;
            }
            } //switch
        } catch (Exception x) {
            log.error(sm.getString("deltaManager.receiveMessage.error",
                    getName()), x);
        }
    }

    // -------------------------------------------------------- message receiver handler


    /**
     * handle receive session state is complete transfered
     * @param msg
     * @param sender
     */
    protected void handleALL_SESSION_TRANSFERCOMPLETE(SessionMessage msg, Member sender) {
        counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE++ ;
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.receiveMessage.transfercomplete",
                    getName(), sender.getHost(), new Integer(sender.getPort())));
        stateTransferCreateSendTime = msg.getTimestamp() ;
        stateTransferred = true ;
    }

    /**
     * handle receive session delta
     * @param msg
     * @param sender
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected void handleSESSION_DELTA(SessionMessage msg, Member sender)
            throws IOException, ClassNotFoundException {
        counterReceive_EVT_SESSION_DELTA++;
        byte[] delta = msg.getSession();
        DeltaSession session = (DeltaSession) findSession(msg.getSessionID());
        if (session != null) {
            log.debug(sm.getString("deltaManager.receiveMessage.delta",
                    getName(), msg.getSessionID()));
            DeltaRequest dreq = loadDeltaRequest(session, delta);
            dreq.execute(session, notifyListenersOnReplication);
            session.setPrimarySession(false);
        }
    }

    /**
     * handle receive session is access at other node ( primary session is now false)
     * @param msg
     * @param sender
     * @throws IOException
     */
    protected void handleSESSION_ACCESSED(SessionMessage msg,Member sender) throws IOException {
        counterReceive_EVT_SESSION_ACCESSED++;
        DeltaSession session = (DeltaSession) findSession(msg
                .getSessionID());
        if (session != null) {
            if (log.isDebugEnabled())
                log.debug(sm.getString(
                        "deltaManager.receiveMessage.accessed",
                        getName(), msg.getSessionID()));
            session.access();
            session.setPrimarySession(false);
            session.endAccess();
        }
    }

    /**
     * handle receive session is expire at other node ( expire session also here)
     * @param msg
     * @param sender
     * @throws IOException
     */
    protected void handleSESSION_EXPIRRED(SessionMessage msg,Member sender) throws IOException {
        counterReceive_EVT_SESSION_EXPIRED++;
        DeltaSession session = (DeltaSession) findSession(msg
                .getSessionID());
        if (session != null) {
            if (log.isDebugEnabled())
                log.debug(sm.getString(
                        "deltaManager.receiveMessage.expired",
                        getName(), msg.getSessionID()));
            session.expire(notifySessionListenersOnReplication, false);
        }
    }

    /**
     * handle receive new session is created at other node (create backup - primary false)
     * @param msg
     * @param sender
     */
    protected void handleSESSION_CREATED(SessionMessage msg,Member sender) {
        counterReceive_EVT_SESSION_CREATED++;
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.receiveMessage.createNewSession",
                    getName(), msg.getSessionID()));
        DeltaSession session = (DeltaSession) createSession(msg
                .getSessionID(), false);
        if(notifySessionListenersOnReplication)
            session.setId(msg.getSessionID());
        else
            session.getDeltaRequest().setSessionId(msg.getSessionID());
        session.setNew(false);
        session.setPrimarySession(false);
        session.resetDeltaRequest();
    }

    /**
     * handle receive sessions from other not ( restart )
     * @param msg
     * @param sender
     * @throws ClassNotFoundException
     * @throws IOException
     */
    protected void handleALL_SESSION_DATA(SessionMessage msg,Member sender) throws ClassNotFoundException, IOException {
        counterReceive_EVT_ALL_SESSION_DATA++;
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.receiveMessage.allSessionDataBegin",
                    getName()));
        byte[] data = msg.getSession();
        deserializeSessions(data);
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.receiveMessage.allSessionDataAfter",
                    getName()));
        //stateTransferred = true;
    }

    /**
     * handle receive that other node want all sessions ( restart )
     * a) send all sessions with one message
     * b) send session at blocks
     * After sending send state is complete transfered
     * @param msg
     * @param sender
     * @throws IOException
     */
    protected void handleGET_ALL_SESSIONS(SessionMessage msg, Member sender)
            throws IOException {
        counterReceive_EVT_GET_ALL_SESSIONS++;
        //get a list of all the session from this manager
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.receiveMessage.unloadingBegin", getName()));
        // Write the number of active sessions, followed by the details
        // get all sessions and serialize without sync
        Session[] currentSessions = findSessions();
        long findSessionTimestamp = System.currentTimeMillis() ;
        if (isSendAllSessions()) {
            sendSessions(sender, currentSessions, findSessionTimestamp);
        } else {
            // send session at blocks
            int len = currentSessions.length < getSendAllSessionsSize() ? currentSessions.length
                    : getSendAllSessionsSize();
            Session[] sendSessions = new Session[len];
            for (int i = 0; i < currentSessions.length; i += getSendAllSessionsSize()) {
                len = i + getSendAllSessionsSize() > currentSessions.length ? currentSessions.length
                        - i 
                        : getSendAllSessionsSize();
                System.arraycopy(currentSessions, i, sendSessions, 0, len);
                sendSessions(sender, sendSessions,findSessionTimestamp);
                if (getSendAllSessionsWaitTime() > 0) {
                    try {
                        Thread.sleep(getSendAllSessionsWaitTime());
                    } catch (Exception sleep) {
                    }
                }
            }
        }
        
        SessionMessage newmsg = new SessionMessageImpl(name,
                SessionMessage.EVT_ALL_SESSION_TRANSFERCOMPLETE, null,
                "SESSION-STATE-TRANSFERED", "SESSION-STATE-TRANSFERED"
                        + getName());
        newmsg.setTimestamp(findSessionTimestamp);
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.createMessage.allSessionTransfered",
                    getName()));
        counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE++;
        cluster.send(newmsg, sender);
    }


    /**
     * send a block of session to sender
     * @param sender
     * @param currentSessions
     * @param sendTimeStamp
     * @throws IOException
     */
    protected void sendSessions(Member sender, Session[] currentSessions,
            long sendTimestamp) throws IOException {
        byte[] data = serializeSessions(currentSessions);
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.receiveMessage.unloadingAfter",
                    getName()));
        SessionMessage newmsg = new SessionMessageImpl(name,
                SessionMessage.EVT_ALL_SESSION_DATA, data,
                "SESSION-STATE", "SESSION-STATE-" + getName());
        newmsg.setTimestamp(sendTimestamp);
        //if(isSendSESSIONSTATEcompressed()) {
        //    newmsg.setCompress(ClusterMessage.FLAG_ALLOWED);
        //}
        if (log.isDebugEnabled())
            log.debug(sm.getString(
                    "deltaManager.createMessage.allSessionData",
                    getName()));
        counterSend_EVT_ALL_SESSION_DATA++;
        cluster.send(newmsg, sender);
    }

}