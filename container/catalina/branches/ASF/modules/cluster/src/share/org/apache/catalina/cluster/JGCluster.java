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

package org.apache.catalina.cluster;

import java.beans.PropertyChangeSupport;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.HashMap;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;

import org.javagroups.View;
import org.javagroups.JChannel;
import org.javagroups.Message;
import org.javagroups.stack.IpAddress;

/**
 * A <b>Cluster</b> implementation using JavaGroups. Responsible for setting 
 * up a cluster and provides callers with a valid multicast receiver/sender.
 *
 * @author Filip Hanik
 * @author Remy Maucherat
 * @version $Revision$, $Date$
 */

public class JGCluster
    implements Cluster, Lifecycle {


    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( JGCluster.class );


    // ----------------------------------------------------- Instance Variables


    /**
     * Descriptive information about this component implementation.
     */
    protected static final String info = "JavaGroupsCluster/1.0";


    /**
     * Name to register for the background thread.
     */
    protected String threadName = "JavaGroupsCluster";


    /**
     * Name for logging purpose
     */
    protected String clusterImpName = "JavaGroupsCluster";


    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);


    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;


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
     * Has this component been started?
     */
    protected boolean started = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The debug level for this Container
     */
    protected int debug = 0;


    /**
     * The context name <-> manager association.
     */
    protected HashMap managers = new HashMap();


    /**
     * A reference to the communication channel.
     */
    protected JChannel channel = null;


    /**
     * The channel configuration.
     */
    protected String protocol = null;


    /**
     * Channel listener thread.
     */
    protected ReceiverThread channelListener = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Cluster implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return(this.info);
    }


    /**
     * Set the debug level for this component
     *
     * @param debug The debug level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }


    /**
     * Get the debug level for this component
     *
     * @return The debug level
     */
    public int getDebug() {
        return(this.debug);
    }


    /**
     * Set the name of the cluster to join, if no cluster with
     * this name is present create one.
     *
     * @param clusterName The clustername to join
     */
    public void setClusterName(String clusterName) {
        String oldClusterName = this.clusterName;
        this.clusterName = clusterName;
        support.firePropertyChange("clusterName",
                                   oldClusterName,
                                   this.clusterName);
    }


    /**
     * Return the name of the cluster that this Server is currently
     * configured to operate within.
     *
     * @return The name of the cluster associated with this server
     */
    public String getClusterName() {
        return(this.clusterName);
    }


    /**
     * Set the Container associated with our Cluster
     *
     * @param container The Container to use
     */
    public void setContainer(Container container) {
        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container",
                                   oldContainer,
                                   this.container);
    }


    /**
     * Get the Container associated with our Cluster
     *
     * @return The Container associated with our Cluster
     */
    public Container getContainer() {
        return(this.container);
    }


    /**
     * Sets the configurable protocol stack. This is a setting in server.xml
     * where you can configure your protocol.
     * 
     * @param protocol the protocol stack - this method is called by 
     * the server configuration at startup
     * @see <a href="www.javagroups.com">JavaGroups</a> for details
     */
    public void setProtocol(String protocol) {
        String oldProtocol = this.protocol;
        this.protocol = protocol;
        support.firePropertyChange("protocol", oldProtocol, this.protocol);
    }


    /**
     * Returns the protocol.
     */
    public String getProtocol() {
        return (this.protocol);
    }


    // --------------------------------------------------------- Public Methods


    public synchronized Manager createManager(String name) {
        JGManager manager = new JGManager();
        manager.setCluster(this);
        managers.put(name, manager);
        return manager;
    }


    /**
     * Sends a SessionMessage to the other nodes in the cluster.<BR>
     * If the SessionMessage type is EVT_GET_ALL_SESSION we only send it 
     * to one node in the cluster because all the nodes look identical.<BR>
     * We choose the send this call to the coordinator, a better solution 
     * would be to send this request using a round robin algorithm to split 
     * up the communication load between nodes.
     * 
     * @param msg - the SessionMessage to be sent, if the message is 
     * of type EVT_GET_ALL_SESSION then we only send to one node, otherwise 
     * we send to everybody 
     * @param sender - the sender of the message
     */
    public void send(SessionMessage msg, IpAddress dest)
        throws Exception {

        //   Check the event type, 
        //   if it is EVT_GET_ALL_SESSION then 
        //   only send the message to the coordinator
        //   otherwise send the message to everybody
        IpAddress destination = dest;
        if (msg.getEventType() == SessionMessage.EVT_GET_ALL_SESSIONS) {
            destination = 
                (IpAddress) channel.getView().getMembers().elementAt(0);
        }

        Message jgmsg = 
            new Message(destination, channel.getLocalAddress(), msg);
        channel.send(jgmsg);

    }


    /**
     * Recieve a message from the JavaGrtoups cluster.
     */
    public void receive(SessionMessage msg, IpAddress sender) {

        // Discard all message from ourself
        if (sender.equals(channel.getLocalAddress())) {
            return;
        }

        JGManager manager = (JGManager) managers.get(msg.getWebapp());
        if (manager != null) {
            manager.messageReceived(msg, sender);
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
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

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
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.<BR>
     * Starts the cluster communication channel, this will connect with the 
     * other nodes in the cluster, and request the current session state to 
     * be transferred to this node.
     * 
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start()
        throws LifecycleException {

        if (started)
            throw new LifecycleException
                (sm.getString("cluster.alreadyStarted"));

        try {

            channel = new JChannel(protocol);
            channelListener = new ReceiverThread(this, channel);
            channel.connect("[TomcatSC]" + container.getName());
            channelListener.start();
            log.info(sm.getString("jgCluster.channelStartSuccess"));

        } catch (Exception x) {
            log.error(sm.getString("jgCluster.channelStartFail"), x);
        }

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.<BR>
     * This will disconnect the cluster communication channel and stop 
     * the listener thread.
     * 
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() 
        throws LifecycleException {

        if (!started)
            throw new IllegalStateException
                (sm.getString("cluster.notStarted"));

        // Stop the javagroup channel
        try {
            channelListener.stopRunning();
            channel.disconnect();   
            channel.close();
        } catch (Exception x) {
            log.error(sm.getString("jgCluster.channelStopFail"), x);
        }

    }


    // --------------------------------------------- ReceiverThread Inner Class


    /**
     * Thread that listens to the cluster communication channel.
     */
    protected class ReceiverThread
        extends Thread {

        protected JGCluster parentCluster = null;
        protected JChannel parentChannel = null;
        protected boolean running = true;

        public ReceiverThread(JGCluster parent, JChannel listenTo) {
            parentCluster = parent;
            parentChannel = listenTo;
        }

        public void stopRunning() {
            running = false;
        }

        public void run() {

            while (running) {

                try {
                    // Receive a message from the channel
                    Object obj = parentChannel.receive(0);
                    // Make sure it is a data message
                    if ((obj != null) && (obj instanceof Message)) {
                        Message msg = (Message)obj;
                        //we are only interested in our own messages
                        ReplicationStream stream = new ReplicationStream
                            (new java.io.ByteArrayInputStream
                             (msg.getBuffer()), getClass().getClassLoader());
                        Object myobj = stream.readObject();
                        if (myobj instanceof SessionMessage) {
                            // notify the cluster
                            parentCluster.receive((SessionMessage) myobj,
                                                  (IpAddress) msg.getSrc());
                        } else {
                            parentCluster.log.info
                                (sm.getString
                                 ("jgCluster.channelIncorrectMessage", obj)); 
                        }
                    } else if (obj instanceof View) {
                        parentCluster.log.info
                            (sm.getString("jgCluster.channelNewMember", obj)); 
                    }
                } catch (Exception x) {
                    parentCluster.log.warn
                        (sm.getString("jgCluster.channelFail"), x);
                }

            }

        }

    }


}
