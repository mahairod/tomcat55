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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ClusterSender;
import org.apache.catalina.cluster.ClusterValve;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.MembershipListener;
import org.apache.catalina.cluster.MembershipService;
import org.apache.catalina.cluster.MessageListener;
import org.apache.catalina.cluster.session.ClusterSessionListener;
import org.apache.catalina.cluster.session.DeltaManager;
import org.apache.catalina.cluster.util.IDynamicProperty;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.apache.tomcat.util.IntrospectionUtils;

/**
 * A <b>Cluster </b> implementation using simple multicast. Responsible for
 * setting up a cluster and provides callers with a valid multicast
 * receiver/sender.
 * 
 * FIXME remove install/remove/start/stop context dummys
 * 
 * FIXME wrote testcases FIXME Propertychange support
 * 
 * @author Filip Hanik
 * @author Remy Maucherat
 * @author Peter Rossbach
 * @version $Revision$, $Date$
 */
public class SimpleTcpCluster implements CatalinaCluster, Lifecycle,
        MembershipListener, LifecycleListener, IDynamicProperty {

    public static Log log = LogFactory.getLog(SimpleTcpCluster.class);

    // ----------------------------------------------------- Instance Variables

    /**
     * Descriptive information about this component implementation.
     */
    protected static final String info = "SimpleTcpCluster/2.1";

    public static final String BEFORE_MEMBERREGISTER_EVENT = "before_member_register";

    public static final String AFTER_MEMBERREGISTER_EVENT = "after_member_register";

    public static final String BEFORE_MANAGERREGISTER_EVENT = "before_manager_register";

    public static final String AFTER_MANAGERREGISTER_EVENT = "after_manager_register";

    public static final String BEFORE_MANAGERUNREGISTER_EVENT = "before_manager_unregister";

    public static final String AFTER_MANAGERUNREGISTER_EVENT = "after_manager_unregister";

    public static final String BEFORE_MEMBERUNREGISTER_EVENT = "before_member_unregister";

    public static final String AFTER_MEMBERUNREGISTER_EVENT = "after_member_unregister";

    public static final String SEND_MESSAGE_FAILURE_EVENT = "send_message_failure";

    public static final String RECEIVE_MESSAGE_FAILURE_EVENT = "receive_message_failure";

    /**
     * the service that provides the membership
     */
    protected MembershipService membershipService = null;

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
    protected Map managers = new HashMap();

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

    private List valves = new ArrayList();

    private org.apache.catalina.cluster.ClusterDeployer clusterDeployer;

    /**
     * Listeners of messages
     */
    protected List clusterListeners = new ArrayList();

    /**
     * Comment for <code>notifyLifecycleListenerOnFailure</code>
     */
    private boolean notifyLifecycleListenerOnFailure = false;

    private ObjectName objectName = null;

    /**
     * dynamic sender <code>properties</code>
     */
    private Map properties = new HashMap();

    /**
     * The cluster log device name to log at level info
     */
    private String clusterLogName = "org.apache.catalina.cluster.tcp.SimpleTcpCluster";

    private boolean doClusterLog = false;

    private Log clusterLog = null;

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
     * @param notifyLifecycleListenerOnFailure
     *            The notifyLifecycleListenerOnFailure to set.
     */
    public void setNotifyLifecycleListenerOnFailure(
            boolean notifyListenerOnFailure) {
        boolean oldNotifyListenerOnFailure = this.notifyLifecycleListenerOnFailure;
        this.notifyLifecycleListenerOnFailure = notifyListenerOnFailure;
        support.firePropertyChange("notifyLifecycleListenerOnFailure",
                oldNotifyListenerOnFailure,
                this.notifyLifecycleListenerOnFailure);
    }

    public String getManagerClassName() {
        return managerClassName;
    }

    public void setManagerClassName(String managerClassName) {
        this.managerClassName = managerClassName;
    }

    public ClusterSender getClusterSender() {
        return clusterSender;
    }

    public void setClusterSender(ClusterSender clusterSender) {
        this.clusterSender = clusterSender;
    }

    public ClusterReceiver getClusterReceiver() {
        return clusterReceiver;
    }

    public void setClusterReceiver(ClusterReceiver clusterReceiver) {
        this.clusterReceiver = clusterReceiver;
    }

    public MembershipService getMembershipService() {
        return membershipService;
    }

    public void setMembershipService(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    public void addValve(Valve valve) {
        if (valve instanceof ClusterValve)
            valves.add(valve);
    }

    public Valve[] getValves() {
        return (Valve[]) valves.toArray(new Valve[valves.size()]);
    }

    /**
     * Get the cluster listeners associated with this cluster. If this Array has
     * no listeners registered, a zero-length array is returned.
     */
    public MessageListener[] findClusterListeners() {
        if (clusterListeners.size() > 0) {
            MessageListener[] listener = new MessageListener[clusterListeners
                    .size()];
            clusterListeners.toArray(listener);
            return listener;
        } else
            return new MessageListener[0];

    }

    /**
     * add cluster message listener and register cluster to this listener
     * 
     * @see org.apache.catalina.cluster.CatalinaCluster#addClusterListener(org.apache.catalina.cluster.MessageListener)
     */
    public void addClusterListener(MessageListener listener) {
        if (listener != null && !clusterListeners.contains(listener)) {
            clusterListeners.add(listener);
            listener.setCluster(this);
        }
    }

    /**
     * remove message listener and deregister Cluster from listener
     * 
     * @see org.apache.catalina.cluster.CatalinaCluster#removeClusterListener(org.apache.catalina.cluster.MessageListener)
     */
    public void removeClusterListener(MessageListener listener) {
        if (listener != null) {
            clusterListeners.remove(listener);
            listener.setCluster(null);
        }
    }

    public org.apache.catalina.cluster.ClusterDeployer getClusterDeployer() {
        return clusterDeployer;
    }

    public void setClusterDeployer(
            org.apache.catalina.cluster.ClusterDeployer clusterDeployer) {
        this.clusterDeployer = clusterDeployer;
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

    // ------------------------------------------------------------- dynamic
    // manager property handling

    /**
     * JMX hack to direct use at jconsole
     * 
     * @param name
     * @param value
     */
    public void setProperty(String name, String value) {
        setProperty(name, (Object) value);
    }

    /**
     * set config attributes with reflect and propagate to all managers
     * 
     * @param name
     * @param value
     */
    public void setProperty(String name, Object value) {
        if (log.isTraceEnabled())
            log.trace(sm.getString("SimpleTcpCluster.setProperty", name, value,
                    properties.get(name)));

        properties.put(name, value);
        for (Iterator iter = managers.values().iterator(); iter.hasNext();) {
            Manager manager = (Manager) iter.next();
            IntrospectionUtils.setProperty(manager, name, value.toString());
        }
    }

    /**
     * get current config
     * 
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        if (log.isTraceEnabled())
            log.trace(sm.getString("SimpleTcpCluster.getProperty", key));
        return properties.get(key);
    }

    /**
     * Get all properties keys
     * 
     * @return
     */
    public Iterator getPropertyNames() {
        return properties.keySet().iterator();
    }

    /**
     * remove a configured property.
     * 
     * @param key
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * @return Returns the managers.
     */
    public Map getManagers() {
        return managers;
    }

    /**
     * Create new Manager without add to cluster (comes with start the manager)
     * 
     * @param name
     *            Context Name of this manager
     * @see org.apache.catalina.Cluster#createManager(java.lang.String)
     * @see #addManager(String, ClusterManager)
     * @see DeltaManager#start()
     */
    public synchronized Manager createManager(String name) {
        if (log.isDebugEnabled())
            log.debug("Creating ClusterManager for context " + name
                    + " using class " + getManagerClassName());
        Manager manager = null;
        try {
            manager = (Manager) getClass().getClassLoader().loadClass(
                    getManagerClassName()).newInstance();
        } catch (Exception x) {
            log.error("Unable to load class for replication manager", x);
            manager = new org.apache.catalina.cluster.session.DeltaManager();
        } finally {
            manager.setDistributable(true);
            if (manager instanceof ClusterManager) {
                ((ClusterManager) manager).setName(name);
                ((ClusterManager) manager).setCluster(this);
            }
        }
        return manager;
    }

    /**
     * remove an application form cluster replication bus
     * 
     * @see org.apache.catalina.cluster.CatalinaCluster#removeManager(java.lang.String)
     */
    public void removeManager(String name) {
        Manager manager = getManager(name);
        if (manager != null) {
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(BEFORE_MANAGERUNREGISTER_EVENT,
                    manager);
            managers.remove(name);
            if (manager instanceof ClusterManager)
                ((ClusterManager) manager).setCluster(null);
            // Notify our interested LifecycleListeners
            lifecycle
                    .fireLifecycleEvent(AFTER_MANAGERUNREGISTER_EVENT, manager);
        }
    }

    /**
     * add an application to cluster replication bus
     * 
     * @param name
     *            of the context
     * @param manager
     *            manager to register
     * @see org.apache.catalina.cluster.CatalinaCluster#addManager(java.lang.String,
     *      org.apache.catalina.Manager)
     */
    public void addManager(String name, Manager manager) {
        if (!manager.getDistributable()) {
            log.warn("Manager with name " + name
                    + " is not distributable, can't add as cluster manager");
            return;
        }
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_MANAGERREGISTER_EVENT, manager);
        if (manager instanceof ClusterManager) {
            ((ClusterManager) manager).setName(name);
            ((ClusterManager) manager).setCluster(this);
        }
        transferManagerProperty(manager);
        managers.put(name, manager);
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_MANAGERREGISTER_EVENT, manager);
    }

    /*
     * Get Manager
     * 
     * @see org.apache.catalina.cluster.CatalinaCluster#getManager(java.lang.String)
     */
    public Manager getManager(String name) {
        return (Manager) managers.get(name);
    }

    /**
     * Transfer all properties from transmitter to concrete sender
     * 
     * @param sender
     */
    protected void transferManagerProperty(Manager manager) {
        for (Iterator iter = getPropertyNames(); iter.hasNext();) {
            String pkey = (String) iter.next();
            Object value = getProperty(pkey);
            IntrospectionUtils.setProperty(manager, pkey, value.toString());
        }
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
        if (clusterSender != null)
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
     * Use as base to handle start/stop/periodic Events from host. Currently
     * only log the messages as trace level.
     * 
     * @see org.apache.catalina.LifecycleListener#lifecycleEvent(org.apache.catalina.LifecycleEvent)
     */
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (log.isTraceEnabled())
            log.trace(sm.getString("SimpleTcpCluster.event.log", lifecycleEvent
                    .getType(), lifecycleEvent.getData()));
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
        getClusterLog();
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, this);

        try {
            registerClusterValve();
            registerMBeans();
            // setup the normal Cluster Session Listener (DeltaManager support)
            if (clusterListeners.size() == 0) {
                if (log.isInfoEnabled()) {
                    log.info(sm.getString(
                            "SimpleTcpCluster.auto.addClusterListener",
                            clusterReceiver.getHost(), new Integer(
                                    clusterReceiver.getPort())));
                }
                addClusterListener(new ClusterSessionListener());
            }
            clusterReceiver.setSendAck(clusterSender.isWaitForAck());
            clusterReceiver.setCompress(clusterSender.isCompress());
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
     * @throws Exception
     * @throws ClassNotFoundException
     */
    protected void registerClusterValve() throws Exception {
        for (Iterator iter = valves.iterator(); iter.hasNext();) {
            ClusterValve valve = (ClusterValve) iter.next();
            if (log.isDebugEnabled())
                log.debug("Invoking addValve on " + getContainer()
                        + " with class=" + valve.getClass().getName());
            if (valve != null) {
                IntrospectionUtils.callMethodN(getContainer(), "addValve",
                        new Object[] { valve }, new Class[] { Thread
                                .currentThread().getContextClassLoader()
                                .loadClass("org.apache.catalina.Valve") });

            }
            valve.setCluster(this);
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
        // FIXME unregister cluster valves ?

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
        clusterLog = null ;
    }

    /**
     * send message to all cluster members
     * 
     * @see org.apache.catalina.cluster.CatalinaCluster#send(org.apache.catalina.cluster.ClusterMessage)
     */
    public void send(ClusterMessage msg) {
        send(msg, null);
    }

    /**
     * send a cluster message to one member
     * 
     * @param msg
     *            message to transfer
     * @param dest
     *            Receiver member
     * @see org.apache.catalina.cluster.CatalinaCluster#send(org.apache.catalina.cluster.ClusterMessage,
     *      org.apache.catalina.cluster.Member)
     */
    public void send(ClusterMessage msg, Member dest) {
        long start = 0;
        if (doClusterLog)
            start = System.currentTimeMillis();
        try {
            msg.setAddress(membershipService.getLocalMember());
            if (dest != null) {
                if (!membershipService.getLocalMember().equals(dest)) {
                    clusterSender.sendMessage(msg, dest);
                } else
                    log.error("Unable to send message to local member " + msg);
            } else {
                clusterSender.sendMessage(msg);
            }
        } catch (Exception x) {
            if (notifyLifecycleListenerOnFailure) {
                // Notify our interested LifecycleListeners
                lifecycle.fireLifecycleEvent(SEND_MESSAGE_FAILURE_EVENT,
                        new SendMessageData(msg, dest, x));
            }
            log.error("Unable to send message through cluster sender.", x);
        }
        if (doClusterLog)
            logSendMessage(msg, start, dest);
    }

    /**
     * New cluster member is registered
     * 
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

    /**
     * Cluster member is gone
     * 
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

    // --------------------------------------------------------- receiver
    // messages

    /**
     * notify all listeners from receiving a new message is not ClusterMessage
     * emitt Failure Event to LifecylceListener
     * 
     * @param message
     *            receveived Message
     */
    public void receive(ClusterMessage message) {

        long start = 0;
        if (doClusterLog)
            start = System.currentTimeMillis();
        if (log.isDebugEnabled() && message != null)
            log.debug("Assuming clocks are synched: Replication for "
                    + message.getUniqueId() + " took="
                    + (System.currentTimeMillis() - (message).getTimestamp())
                    + " ms.");

        //invoke all the listeners
        boolean accepted = false;
        if (message != null) {
            for (Iterator iter = clusterListeners.iterator(); iter.hasNext();) {
                MessageListener listener = (MessageListener) iter.next();
                if (listener.accept(message)) {
                    accepted = true;
                    listener.messageReceived(message);
                }
            }
        }
        if (!accepted && log.isDebugEnabled()) {
            if (notifyLifecycleListenerOnFailure) {
                Member dest = message.getAddress();
                // Notify our interested LifecycleListeners
                lifecycle.fireLifecycleEvent(RECEIVE_MESSAGE_FAILURE_EVENT,
                        new SendMessageData(message, dest, null));
            }
            log.debug("Message " + message.toString() + " from type "
                    + message.getClass().getName()
                    + " transfered but no listener registered");
        }
        if (doClusterLog)
            logReceiveMessage(message, start, accepted);
    }

    // --------------------------------------------------------- Logger

    /**
     * @return Returns the clusterLogName.
     */
    public String getClusterLogName() {
        return clusterLogName;
    }
    
    /**
     * @param clusterLogName The clusterLogName to set.
     */
    public void setClusterLogName(String clusterLogName) {
        this.clusterLogName = clusterLogName;
    }
    
    /**
     * @return Returns the doClusterLog.
     */
    public boolean isDoClusterLog() {
        return doClusterLog;
    }
    
    /**
     * @param doClusterLog The doClusterLog to set.
     */
    public void setDoClusterLog(boolean doClusterLog) {
        this.doClusterLog = doClusterLog;
    }    
    public Log getLogger() {
        return log;
    }

    public Log getClusterLog() {
        if (clusterLog == null && clusterLogName != null
                && !"".equals(clusterLogName))
            clusterLog = LogFactory.getLog(clusterLogName);

        return clusterLog;
    }

    /**
     * @param message
     * @param start
     * @param accepted
     */
    protected void logReceiveMessage(ClusterMessage message, long start,
            boolean accepted) {
        if (clusterLog != null && clusterLog.isInfoEnabled()) {
            clusterLog.info(sm.getString("SimpleTcpCluster.log.receive", new Object[] {
                    new Date(start),
                    new Long(System.currentTimeMillis() - start),
                    message.getAddress().getHost(),
                    new Integer(message.getAddress().getPort()),
                    message.getUniqueId(), new Boolean(accepted) }));
        }
    }

    /**
     * @param message
     * @param start
     * @param dest
     */
    protected void logSendMessage(ClusterMessage message, long start,
            Member dest) {
        if (clusterLog != null && clusterLog.isInfoEnabled()) {
            if (dest != null) {
                clusterLog.info(sm.getString("SimpleTcpCluster.log.send",
                        new Object[] { new Date(start),
                                new Long(System.currentTimeMillis() - start),
                                dest.getHost(), new Integer(dest.getPort()),
                                message.getUniqueId() }));
            } else {
                clusterLog.info(sm.getString("SimpleTcpCluster.log.send.all",
                        new Object[] { new Date(start),
                                new Long(System.currentTimeMillis() - start),
                                message.getUniqueId() }));
            }
        }
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
            registry.loadMetadata(this.getClass().getResourceAsStream(
                    "mbeans-descriptors.xml"));
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
        objectName = name;
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

    // ------------------------------------------------------------- deprecated

    /**
     * 
     * @see org.apache.catalina.Cluster#setProtocol(java.lang.String)
     */
    public void setProtocol(String protocol) {
    }

    /**
     * @see org.apache.catalina.Cluster#getProtocol()
     */
    public String getProtocol() {
        return null;
    }

    /**
     * @see org.apache.catalina.Cluster#startContext(java.lang.String)
     */
    public void startContext(String contextPath) throws IOException {
        
    }

    /**
     * @see org.apache.catalina.Cluster#installContext(java.lang.String, java.net.URL)
     */
    public void installContext(String contextPath, URL war) {
        
    }

    /**
     * @see org.apache.catalina.Cluster#stop(java.lang.String)
     */
    public void stop(String contextPath) throws IOException {
        
    }


}