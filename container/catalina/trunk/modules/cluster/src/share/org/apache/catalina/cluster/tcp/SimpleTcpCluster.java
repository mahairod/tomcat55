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

package org.apache.catalina.cluster.tcp;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterManager;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.tcp.Constants;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.MembershipListener;
import org.apache.catalina.cluster.MembershipService;
import org.apache.catalina.cluster.MessageListener;
import org.apache.catalina.cluster.io.ListenCallback;
import org.apache.catalina.cluster.session.ReplicationStream;
import org.apache.catalina.cluster.session.SessionMessage;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.apache.tomcat.util.IntrospectionUtils;

/**
 * A <b>Cluster </b> implementation using simple multicast. Responsible for
 * setting up a cluster and provides callers with a valid multicast
 * receiver/sender.
 * 
 * FIXME remove install/remove/start/stop context dummys
 * FIXME better stats
 * FIXME factor out receiver handling
 * FIXME optimize message package creation
 * FIXME better compress message handling
 * FIXME Clearer implementation from notifyListenersOnReplication flag
 * FIXME Handle more than one Replication valve use one pipeline and Programme als Delgation valve to cluster pipeline.
 * 
 * FIXME wrote testcases
 * FIXME Factor out session MessageListener
 * 
 * @author Filip Hanik
 * @author Remy Maucherat
 * @author Peter Rossbach
 * @version $Revision$, $Date$
 */

public class SimpleTcpCluster implements CatalinaCluster, Lifecycle,
        MembershipListener, ListenCallback, LifecycleListener {

    public static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(SimpleTcpCluster.class);

    // ----------------------------------------------------- Instance Variables

    /**
     * Descriptive information about this component implementation.
     */
    protected static final String info = "SimpleTcpCluster/1.1";

    public static final String BEFORE_MEMBERREGISTER_EVENT = "before_member_register";

    public static final String AFTER_MEMBERREGISTER_EVENT = "after_member_register";

    public static final String BEFORE_MANAGERREGISTER_EVENT = "before_manager_register";

    public static final String AFTER_MANAGERREGISTER_EVENT = "after_manager_register";

    public static final String BEFORE_MANAGERUNREGISTER_EVENT = "before_manager_unregister";

    public static final String AFTER_MANAGERUNREGISTER_EVENT = "after_manager_unregister";

    public static final String BEFORE_MEMBERUNREGISTER_EVENT = "before_member_unregister";

    public static final String AFTER_MEMBERUNREGISTER_EVENT = "after_member_unregister";

    public static final String SEND_MESSAGE_FAILURE_EVENT = "send_message_failure";
    
    /**
     * the service that provides the membership
     */
    protected MembershipService membershipService = null;

    /**
     * Whether to expire sessions when shutting down
     */
    protected boolean expireSessionsOnShutdown = true;

    /**
     * Print debug to std.out?
     */
    protected boolean printToScreen = false;

    /**
     * Replicate only sessions that have been marked dirty false=replicate
     * sessions after each request
     */
    protected boolean useDirtyFlag = false;

    /**
     * Name for logging purpose
     */
    protected String clusterImpName = "SimpleTcpCluster";

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * The cluster name to join
     */
    protected String clusterName = null;

    /**
     * The Container associated with this Cluster.
     */
    protected Container container = null;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * Globale MBean Server
     */
    private MBeanServer mserver = null;

    /**
     * Current Catalina Registry
     */
    private Registry registry = null;

    /**
     * Has this component been started?
     */
    protected boolean started = false;

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The context name <->manager association for distributed contexts.
     */
    protected HashMap managers = new HashMap();

    /**
     * Sending Stats
     */
    private long nrOfMsgsReceived = 0;

    private long msgSendTime = 0;

    private long lastChecked = System.currentTimeMillis();

    //sort members by alive time
    protected MemberComparator memberComparator = new MemberComparator();

    private String managerClassName = "org.apache.catalina.cluster.session.DeltaManager";

    /**
     * Sender to send data with
     */
    private org.apache.catalina.cluster.ClusterSender clusterSender;

    /**
     * Receiver to register call back with
     */
    private org.apache.catalina.cluster.ClusterReceiver clusterReceiver;

    private org.apache.catalina.Valve valve;

    private org.apache.catalina.cluster.ClusterDeployer clusterDeployer;

    /**
     * Listeners of messages
     */
    protected Vector clusterListeners = new Vector();

    /**
     * Currently only implemented for the delta manager
     */
    private boolean notifyListenersOnReplication = true;

    private ObjectName objectName = null;

    private boolean notifyLifecycleListenerOnFailure = false;

    
    // ------------------------------------------------------------- Properties

    public SimpleTcpCluster() {
    }

    /**
     * Return descriptive information about this Cluster implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }

    /**
     * Set the name of the cluster to join, if no cluster with this name is
     * present create one.
     * 
     * @param clusterName
     *            The clustername to join
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Return the name of the cluster that this Server is currently configured
     * to operate within.
     * 
     * @return The name of the cluster associated with this server
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Set the Container associated with our Cluster
     * 
     * @param container
     *            The Container to use
     */
    public void setContainer(Container container) {
        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);
    }

    /**
     * Get the Container associated with our Cluster
     * 
     * @return The Container associated with our Cluster
     */
    public Container getContainer() {
        return (this.container);
    }

    
    /**
     * @return Returns the notifyLifecycleListenerOnFailure.
     */
    public boolean isNotifyLifecycleListenerOnFailure() {
        return notifyLifecycleListenerOnFailure;
    }
    
    /**
     * @param notifyLifecycleListenerOnFailure The notifyLifecycleListenerOnFailure to set.
     */
    public void setNotifyLifecycleListenerOnFailure(boolean notifyListenerOnFailure) {
        boolean oldNotifyListenerOnFailure = this.notifyLifecycleListenerOnFailure;
        this.notifyLifecycleListenerOnFailure = notifyListenerOnFailure;
        support.firePropertyChange("notifyLifecycleListenerOnFailure", oldNotifyListenerOnFailure, this.notifyLifecycleListenerOnFailure);
    }
    
    /**
     * Sets the configurable protocol stack. This is a setting in server.xml
     * where you can configure your protocol.
     * 
     * @param protocol
     *            the protocol stack - this method is called by the server
     *            configuration at startup
     * @see <a href="www.javagroups.com">JavaGroups </a> for details
     */
    public void setProtocol(String protocol) {
    }

    /**
     * Returns the protocol.
     */
    public String getProtocol() {
        return null;
    }

    public Member[] getMembers() {
        Member[] members = membershipService.getMembers();
        //sort by alive time
        java.util.Arrays.sort(members, memberComparator);
        return members;
    }

    /**
     * Return the member that represents this node.
     * 
     * @return Member
     */
    public Member getLocalMember() {
        return membershipService.getLocalMember();
    }

    // --------------------------------------------------------- Public Methods

    
    /** Create new Manager and add to cluster
     * @param name Context Name of this manager
     * @see org.apache.catalina.Cluster#createManager(java.lang.String)
     * @see #addManager(String, ClusterManager)
     */
    public synchronized Manager createManager(String name) {
        if (log.isDebugEnabled())
            log.debug("Creating ClusterManager for context " + name
                    + " using class " + getManagerClassName());
        ClusterManager manager = null;
        try {
            manager = (ClusterManager) getClass().getClassLoader().loadClass(
                    getManagerClassName()).newInstance();
        } catch (Exception x) {
            log.error("Unable to load class for replication manager", x);
            manager = new org.apache.catalina.cluster.session.SimpleTcpReplicationManager();
        }
        addManager(name, manager);
        return manager;
    }

    /** remove an application form cluster replication bus
     * @see org.apache.catalina.cluster.CatalinaCluster#removeManager(java.lang.String)
     */
    public void removeManager(String name) {
        Manager manager = getManager(name);
        if(manager != null ) {
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(BEFORE_MANAGERUNREGISTER_EVENT, manager);
            managers.remove(name);
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(AFTER_MANAGERUNREGISTER_EVENT, manager);
        }
     }

    /** add an application to cluster replication bus
     * @param name of the context 
     * @param manager manager to register 
     * @see org.apache.catalina.cluster.CatalinaCluster#addManager(java.lang.String, org.apache.catalina.cluster.ClusterManager)
     */
    public void addManager(String name, ClusterManager manager) {
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_MANAGERREGISTER_EVENT, manager);
        manager.setName(name);
        manager.setCluster(this);
        manager.setDistributable(true);
        manager.setExpireSessionsOnShutdown(expireSessionsOnShutdown);
        manager.setUseDirtyFlag(useDirtyFlag);
        manager.setNotifyListenersOnReplication(notifyListenersOnReplication);
        managers.put(name, manager);
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_MANAGERREGISTER_EVENT, manager);
    }

    public Manager getManager(String name) {
        return (Manager) managers.get(name);
    }

    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess() {
        if (clusterDeployer != null)
            clusterDeployer.backgroundProcess();
        if( clusterSender != null)
            clusterSender.backgroundProcess();

    }

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
     * Use as base to handle start/stop/periodic Events from host.
     * Currently only log the messages as trace level.
     * 
     * @see org.apache.catalina.LifecycleListener#lifecycleEvent(org.apache.catalina.LifecycleEvent)
     */
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if(log.isTraceEnabled())
            log.trace(sm.getString("SimpleTcpCluster.event.log",lifecycleEvent.getType(), lifecycleEvent.getData())) ;
    }

    // ------------------------------------------------------ public
    
    /**
     * Prepare for the beginning of active use of the public methods of this
     * component. This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized. <BR>
     * Starts the cluster communication channel, this will connect with the
     * other nodes in the cluster, and request the current session state to be
     * transferred to this node.
     * 
     * @exception IllegalStateException
     *                if this component has already been started
     * @exception LifecycleException
     *                if this component detects a fatal error that prevents this
     *                component from being used
     */
    public void start() throws LifecycleException {
        if (started)
            throw new LifecycleException(sm.getString("cluster.alreadyStarted"));
        if (log.isInfoEnabled())
            log.info("Cluster is about to start");
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, this);

        try {
            if (log.isDebugEnabled())
                log.debug("Invoking addValve on " + getContainer()
                        + " with class=" + valve.getClass().getName());
            if (valve != null) {
                IntrospectionUtils.callMethodN(getContainer(), "addValve",
                        new Object[] { valve }, new Class[] { Thread
                                .currentThread().getContextClassLoader()
                                .loadClass("org.apache.catalina.Valve") });

            }
            registerMBeans();
            clusterReceiver.setSendAck(clusterSender.isWaitForAck());
            clusterReceiver.setCatalinaCluster(this);
            clusterReceiver.start();
            clusterSender.setCatalinaCluster(this);
            clusterSender.start();
            membershipService.setLocalMemberProperties(clusterReceiver
                    .getHost(), clusterReceiver.getPort());
            membershipService.addMembershipListener(this);
            membershipService.start();
            //set the deployer.
            try {
                if (clusterDeployer != null) {
                    clusterDeployer.setCluster(this);
                    clusterDeployer.start();
                }
            } catch (Throwable x) {
                log
                        .fatal(
                                "Unable to retrieve the container deployer. Cluster deployment disabled.",
                                x);
            } //catch
            this.started = true;
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(AFTER_START_EVENT, this);
        } catch (Exception x) {
            log.error("Unable to start cluster.", x);
            throw new LifecycleException(x);
        }
    }

    /**
     * Gracefully terminate the active use of the public methods of this
     * component. This method should be the last one called on a given instance
     * of this component. <BR>
     * This will disconnect the cluster communication channel and stop the
     * listener thread.
     * 
     * @exception IllegalStateException
     *                if this component has not been started
     * @exception LifecycleException
     *                if this component detects a fatal error that needs to be
     *                reported
     */
    public void stop() throws LifecycleException {

        if (!started)
            throw new IllegalStateException(sm.getString("cluster.notStarted"));
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, this);
        
        unregisterMBeans();

        membershipService.stop();
        membershipService.removeMembershipListener();
        try {
            clusterSender.stop();
        } catch (Exception x) {
            log.error("Unable to stop cluster sender.", x);
        }
        try {
            clusterReceiver.stop();
            clusterReceiver.setCatalinaCluster(null);
        } catch (Exception x) {
            log.error("Unable to stop cluster receiver.", x);
        }
        if (clusterDeployer != null) {
            clusterDeployer.stop();
        }
        started = false;
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, this);
    }

    /**
     * send a cluster message to one member
     * 
     * @param msg message to transfer
     * @param dest Receiver member
     * @see org.apache.catalina.cluster.CatalinaCluster#send(org.apache.catalina.cluster.ClusterMessage,
     *      org.apache.catalina.cluster.Member)
     */
    public void send(ClusterMessage msg, Member dest) {
        try {
            msg.setAddress(membershipService.getLocalMember());
            Member destination = dest;

            if (msg instanceof SessionMessage) {
                SessionMessage smsg = (SessionMessage) msg;
                //if we request session state, send to the oldest of members
                if ((destination == null)
                        && (smsg.getEventType() == SessionMessage.EVT_GET_ALL_SESSIONS)
                        && (membershipService.getMembers().length > 0)) {
                    destination = membershipService.getMembers()[0];
                }
            }
            byte[] data = createMessageData(msg);
            if (destination != null) {
                Member tcpdest = dest;
                if ((tcpdest != null)
                        && (!membershipService.getLocalMember().equals(tcpdest))) {
                    clusterSender.sendMessage(msg.getUniqueId(), data, tcpdest);
                }
            } else {
                clusterSender.sendMessage(msg.getUniqueId(), data);
            }
        } catch (Exception x) {
            if(notifyLifecycleListenerOnFailure) {
                // Notify our interested LifecycleListeners
                lifecycle.fireLifecycleEvent(SEND_MESSAGE_FAILURE_EVENT, 
                 new SendMessageData(msg,dest,x));                
            }
            log.error("Unable to send message through cluster sender.", x);
        }
    }

    /**
     * Send Message create Timestamp and generate message bytes form msg
     * @param msg cluster message
     * @return cluster message as byte array
     * @throws IOException
     */
    protected byte[] createMessageData(ClusterMessage msg) throws IOException {
        msg.setTimestamp(System.currentTimeMillis());
        java.io.ByteArrayOutputStream outs = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                outs);
        out.writeObject(msg);
        byte[] data = outs.toByteArray();
        return data;
    }

    /**
     * send message to all cluster members
     * 
     * @see org.apache.catalina.cluster.CatalinaCluster#send(org.apache.catalina.cluster.ClusterMessage)
     */
    public void send(ClusterMessage msg) {
        send(msg, null);
    }


    /* New cluster member is registered
     * FIXME notify someone (JMX(Listener)
     * @see org.apache.catalina.cluster.MembershipListener#memberAdded(org.apache.catalina.cluster.Member)
     */
    public void memberAdded(Member member) {
        try {
            if (log.isInfoEnabled())
                log.info("Replication member added:" + member);
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(BEFORE_MEMBERREGISTER_EVENT, member);
            clusterSender.add(member);
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(AFTER_MEMBERREGISTER_EVENT, member);
        } catch (Exception x) {
            log.error("Unable to connect to replication system.", x);
        }

    }

    /** Cluster member is gone
     * @see org.apache.catalina.cluster.MembershipListener#memberDisappeared(org.apache.catalina.cluster.Member)
     */
    public void memberDisappeared(Member member) {
        if (log.isInfoEnabled())
            log.info("Received member disappeared:" + member);
        try {
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(BEFORE_MEMBERUNREGISTER_EVENT, member);
            clusterSender.remove(member);
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(AFTER_MEMBERUNREGISTER_EVENT, member);
        } catch (Exception x) {
            log.error("Unable remove cluster node from replication system.", x);
        }

    }

    public void setExpireSessionsOnShutdown(boolean expireSessionsOnShutdown) {
        this.expireSessionsOnShutdown = expireSessionsOnShutdown;
    }

    public void setPrintToScreen(boolean printToScreen) {
        this.printToScreen = printToScreen;
    }

    /**
     * @return Returns the msgSendTime.
     */
    public long getMsgSendTime() {
        return msgSendTime;
    }

    /**
     * @return Returns the lastChecked.
     */
    public long getLastChecked() {
        return lastChecked;
    }

    /**
     * @return Returns the nrOfMsgsReceived.
     */
    public long getNrOfMsgsReceived() {
        return nrOfMsgsReceived;
    }

    /**
     * @return Returns the expireSessionsOnShutdown.
     */
    public boolean isExpireSessionsOnShutdown() {
        return expireSessionsOnShutdown;
    }

    /**
     * @return Returns the printToScreen.
     */
    public boolean isPrintToScreen() {
        return printToScreen;
    }

    /**
     * @return Returns the useDirtyFlag.
     */
    public boolean isUseDirtyFlag() {
        return useDirtyFlag;
    }

    public void setUseDirtyFlag(boolean useDirtyFlag) {
        this.useDirtyFlag = useDirtyFlag;
    }

    
    // --------------------------------------------------------- receiver messages

    public void messageDataReceived(byte[] data) {
        long timeSent = System.currentTimeMillis();
        try {
            ReplicationStream stream = new ReplicationStream(
                    new java.io.ByteArrayInputStream(data), getClass()
                            .getClassLoader());
            Object myobj = stream.readObject();
            if (log.isDebugEnabled()
                    && myobj != null && myobj instanceof ClusterMessage)
                log.debug("Assuming clocks are synched: Replication for " 
                        + ((ClusterMessage) myobj).getUniqueId() + " took="
                        + (System.currentTimeMillis() - ((ClusterMessage) myobj).getTimestamp())
                        + " ms.");
            if (myobj != null && myobj instanceof SessionMessage) {

                SessionMessage msg = (SessionMessage) myobj;
                String ctxname = msg.getContextName();
                //check if the message is a EVT_GET_ALL_SESSIONS,
                //if so, wait until we are fully started up
                if (ctxname == null) {
                    java.util.Iterator i = managers.keySet().iterator();
                    while (i.hasNext()) {
                        String key = (String) i.next();
                        ClusterManager mgr = (ClusterManager) managers.get(key);
                        if (mgr != null)
                            mgr.messageDataReceived(msg);
                        else {
                            //this happens a lot before the system has started
                            // up
                            if (log.isDebugEnabled())
                                log.debug("Context manager doesn't exist:"
                                        + key);
                        }
                    }
                } else {
                    ClusterManager mgr = (ClusterManager) managers.get(ctxname);
                    if (mgr != null)
                        mgr.messageDataReceived(msg);
                    else if (log.isWarnEnabled())
                        log.warn("Context manager doesn't exist:" + ctxname);
                }
            } else {
                //invoke all the listeners
                for (int i = 0; i < clusterListeners.size(); i++) {
                    MessageListener listener = (MessageListener) clusterListeners
                            .elementAt(i);
                    if (myobj != null && myobj instanceof ClusterMessage
                            && listener.accept((ClusterMessage) myobj)) {
                      listener.messageReceived((ClusterMessage) myobj);
                    } else
                        if(log.isDebugEnabled())
                            log.debug("Message " + myobj.toString() 
                                    + " from type " + myobj.getClass().getName() 
                                    + " transfered but no listener registered");
                }
            }

        } catch (Exception x) {
            log.error("Unable to deserialize session message.", x);
        } finally {
            perfMessageRecvd(timeSent);
        }
    }

 
    // --------------------------------------------------------- Cluster Wide Deployments

    /**
     * Start an existing web application, attached to the specified context path
     * in all the other nodes in the cluster. Only starts a web application if
     * it is not running.
     * 
     * @param contextPath
     *            The context path of the application to be started
     * 
     * @exception IllegalArgumentException
     *                if the specified context path is malformed (it must be ""
     *                or start with a slash)
     * @exception IllegalArgumentException
     *                if the specified context path does not identify a
     *                currently installed web application
     * @exception IOException
     *                if an input/output error occurs during startup
     * @deprecated since 5.5.10
     */
    public void startContext(String contextPath) throws IOException {
        return;
    }

    /**
     * Install a new web application, whose web application archive is at the
     * specified URL, into this container with the specified context path. A
     * context path of "" (the empty string) should be used for the root
     * application for this container. Otherwise, the context path must start
     * with a slash.
     * <p>
     * If this application is successfully installed, a ContainerEvent of type
     * <code>PRE_INSTALL_EVENT</code> will be sent to registered listeners
     * before the associated Context is started, and a ContainerEvent of type
     * <code>INSTALL_EVENT</code> will be sent to all registered listeners
     * after the associated Context is started, with the newly created
     * <code>Context</code> as an argument.
     * 
     * @param contextPath
     *            The context path to which this application should be installed
     *            (must be unique)
     * @param war
     *            A URL of type "jar:" that points to a WAR file, or type
     *            "file:" that points to an unpacked directory structure
     *            containing the web application to be installed
     * 
     * @exception IllegalArgumentException
     *                if the specified context path is malformed (it must be ""
     *                or start with a slash)
     * @exception IllegalStateException
     *                if the specified context path is already attached to an
     *                existing web application
     * @deprecated since 5.5.10
     */
    public void installContext(String contextPath, URL war) {
        if (log.isDebugEnabled())
            log.debug("\n\n\n\nCluster Install called for context:"
                    + contextPath + "\n\n\n\n");
    }

    /**
     * Stop an existing web application, attached to the specified context path.
     * Only stops a web application if it is running.
     * 
     * @param contextPath
     *            The context path of the application to be stopped
     * 
     * @exception IllegalArgumentException
     *                if the specified context path is malformed (it must be ""
     *                or start with a slash)
     * @exception IllegalArgumentException
     *                if the specified context path does not identify a
     *                currently installed web application
     * @exception IOException
     *                if an input/output error occurs while stopping the web
     *                application
     * @deprecated since 5.5.10
     */
    public void stop(String contextPath) throws IOException {
        return;
    }

    public Log getLogger() {
        return log;
    }

    // --------------------------------------------- Inner Class

    // --------------------------------------------- Performance

    private void perfMessageRecvd(long timeSent) {
        nrOfMsgsReceived++;
        long current = System.currentTimeMillis();
        msgSendTime += (current - timeSent);
        if (log.isDebugEnabled()) {
            if ((current - lastChecked) > 5000) {
                log.debug("Calc msg send time total=" + msgSendTime
                        + "ms num request=" + nrOfMsgsReceived
                        + " average per msg="
                        + (msgSendTime / nrOfMsgsReceived) + "ms.");
                lastChecked=current ;
            }
        }
    }

    public String getManagerClassName() {
        return managerClassName;
    }

    public void setManagerClassName(String managerClassName) {
        this.managerClassName = managerClassName;
    }

    public org.apache.catalina.cluster.ClusterSender getClusterSender() {
        return clusterSender;
    }

    public void setClusterSender(
            org.apache.catalina.cluster.ClusterSender clusterSender) {
        this.clusterSender = clusterSender;
    }

    public org.apache.catalina.cluster.ClusterReceiver getClusterReceiver() {
        return clusterReceiver;
    }

    public void setClusterReceiver(
            org.apache.catalina.cluster.ClusterReceiver clusterReceiver) {
        this.clusterReceiver = clusterReceiver;
    }

    public MembershipService getMembershipService() {
        return membershipService;
    }

    public void setMembershipService(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    public void addValve(Valve valve) {
        this.valve = valve;
    }

    public Valve getValve() {
        return valve;
    }

    /**
     * Get the cluster listeners associated with this cluster. If this
     * Array has no listeners registered, a zero-length array is returned.
     */
    public MessageListener[] findClusterListeners() {
        if(clusterListeners.size() > 0) {
            MessageListener[] listener = new MessageListener[clusterListeners.size()];
            clusterListeners.toArray(listener);
            return listener ;
        } else
            return new MessageListener[0] ;

    }

    public void addClusterListener(MessageListener listener) {
        if (!clusterListeners.contains(listener)) {
            clusterListeners.addElement(listener);
        }
    }

    public void removeClusterListener(MessageListener listener) {
        clusterListeners.removeElement(listener);
    }

    public org.apache.catalina.cluster.ClusterDeployer getClusterDeployer() {
        return clusterDeployer;
    }

    public void setClusterDeployer(
            org.apache.catalina.cluster.ClusterDeployer clusterDeployer) {
        this.clusterDeployer = clusterDeployer;
    }

    public boolean getNotifyListenersOnReplication() {
        return notifyListenersOnReplication;
    }

    public void setNotifyListenersOnReplication(
            boolean notifyListenersOnReplication) {
        this.notifyListenersOnReplication = notifyListenersOnReplication;
    }

    // --------------------------------------------- JMX MBeans

    /**
     * register Means at cluster
     * 
     * @param host
     *            clustered host
     */
    protected void registerMBeans() {
        try {
            getMBeanServer();
            String domain = mserver.getDefaultDomain();
            String name = ":type=Cluster";
            if (container instanceof StandardHost) {
                domain = ((StandardHost) container).getDomain();
                name += ",host=" + container.getName();
            }
            ObjectName clusterName = new ObjectName(domain + name);

            if (mserver.isRegistered(clusterName)) {
                if (log.isWarnEnabled())
                    log.warn(sm.getString("cluster.mbean.register.allready",
                            clusterName));
                return;
            }
            setObjectName(clusterName);
            mserver.registerMBean(getManagedBean(this), getObjectName());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    protected void unregisterMBeans() {
        if (mserver != null) {
            try {
                mserver.unregisterMBean(getObjectName());
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * Get current Catalina MBean Server and load mbean registry
     * 
     * @return
     * @throws Exception
     */
    protected MBeanServer getMBeanServer() throws Exception {
        if (mserver == null) {
            if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
                mserver = (MBeanServer) MBeanServerFactory
                        .findMBeanServer(null).get(0);
            } else {
                mserver = MBeanServerFactory.createMBeanServer();
            }
            registry = Registry.getRegistry(null, null);
            registry.loadMetadata(this.getClass()
                    .getResourceAsStream("mbeans-descriptors.xml"));
        }
        return (mserver);
    }

    /**
     * Returns the ModelMBean
     * 
     * @param object
     *            The Object to get the ModelMBean for
     * @return The ModelMBean
     * @throws Exception
     *             If an error occurs this constructors throws this exception
     */
    protected ModelMBean getManagedBean(Object object) throws Exception {
        ModelMBean mbean = null;
        if (registry != null) {
            ManagedBean managedBean = registry.findManagedBean(object
                    .getClass().getName());
            mbean = managedBean.createMBean(object);
        }
        return mbean;
    }

    public void setObjectName(ObjectName name) {
        objectName = name ;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    // --------------------------------------------- Inner Class

    private class MemberComparator implements java.util.Comparator {

        public int compare(Object o1, Object o2) {
            try {
                return compare((Member) o1, (Member) o2);
            } catch (ClassCastException x) {
                return 0;
            }
        }

        public int compare(Member m1, Member m2) {
            //longer alive time, means sort first
            long result = m2.getMemberAliveTime() - m1.getMemberAliveTime();
            if (result < 0)
                return -1;
            else if (result == 0)
                return 0;
            else
                return 1;
        }
    }

}