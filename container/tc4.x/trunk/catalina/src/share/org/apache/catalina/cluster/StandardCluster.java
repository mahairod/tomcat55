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
import java.util.Collection;
import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.util.LifecycleSupport;

/**
 * A <b>Cluster</b> implementation. Responsible for setting up
 * a cluster and provides callers with a valid multicast receiver/sender.
 *
 * @author Bip Thelin
 * @version $Revision$, $Date$
 */

public final class StandardCluster
    implements Cluster, Lifecycle, Runnable {

    // ----------------------------------------------------- Instance Variables

    /**
     * Descriptive information about this component implementation.
     */
    private static final String info = "StandardCluster/1.0";

    /**
     * Name to register for the background thread.
     */
    private String threadName = "StandardCluster";

    /**
     * Name for logging purpose
     */
    private String clusterImpName = "StandardCluster";

    /**
     * The background thread.
     */
    private Thread thread = null;

    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;

    /**
     * The cluster name to join
     */
    private String clusterName = null;

    /**
     * The Container associated with this Cluster.
     */
    private Container container = null;

    /**
     * The MulticastPort to use with this cluster
     */
    private int multicastPort;

    /**
     * The MulticastAdress to use with this cluster
     */
    private InetAddress multicastAddress = null;

    /**
     * Our MulticastSocket
     */
    private MulticastSocket multicastSocket = null;

    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * Has this component been started?
     */
    private boolean started = false;

    /**
     * The property change support for this component.
     */
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The debug level for this Container
     */
    private int debug = 0;

    /**
     * The interval for the background thread to sleep
     */
    private int checkInterval = 60;

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
     * Return a <code>String</code> containing the name of this
     * Cluster implementation, used for logging
     *
     * @return The Cluster implementation
     */
    protected String getName() {
        return(this.clusterImpName);
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
     * Set the Port associated with our Cluster
     *
     * @param port The Port to use
     */
    public void setMulticastPort(int multicastPort) {
        int oldMulticastPort = this.multicastPort;
        this.multicastPort = multicastPort;
        support.firePropertyChange("multicastPort",
                                   oldMulticastPort,
                                   this.multicastPort);
    }

    /**
     * Get the Port associated with our Cluster
     *
     * @return The Port associated with our Cluster
     */
    public int getMulticastPort() {
        return(this.multicastPort);
    }

    /**
     * Set the Groupaddress associated with our Cluster
     *
     * @param port The Groupaddress to use
     */
    public void setMulticastAddress(String multicastAddress) {
        try {
            InetAddress oldMulticastAddress = this.multicastAddress;
            this.multicastAddress = InetAddress.getByName(multicastAddress);
            support.firePropertyChange("multicastAddress",
                                       oldMulticastAddress,
                                       this.multicastAddress);
        } catch (UnknownHostException e) {
            log("Invalid multicastAddress: "+multicastAddress);
        }
    }

    /**
     * Get the Groupaddress associated with our Cluster
     *
     * @return The Groupaddress associated with our Cluster
     */
    public InetAddress getMulticastAddress() {
        return(this.multicastAddress);
    }

    /**
     * Set the time in seconds for this component to
     * Sleep before it checks for new received data in the Cluster
     *
     * @param checkInterval The time to sleep
     */
    public void setCheckInterval(int checkInterval) {
        int oldCheckInterval = this.checkInterval;
        this.checkInterval = checkInterval;
        support.firePropertyChange("checkInterval",
                                   oldCheckInterval,
                                   this.checkInterval);
    }

    /**
     * Get the time in seconds this Cluster sleeps
     *
     * @return The time in seconds this Cluster sleeps
     */
    public int getCheckInterval() {
        return(this.checkInterval);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Returns a collection containing <code>ClusterMemberInfo</code>
     * on the remote members of this Cluster. This method does
     * not include the local host, to retrieve
     * <code>ClusterMemberInfo</code> on the local host
     * use <code>getLocalClusterInfo()</code> instead.
     *
     * @return Collection with all members in the Cluster
     */
    public Collection getRemoteClusterMembers() {
        return(null);
    }

    /**
     * Return cluster information about the local host
     *
     * @return Cluster information
     */
    public ClusterMemberInfo getLocalClusterInfo() {
        return(null);
    }

    /**
     * Returns a <code>ClusterSender</code> which is the interface
     * to use when sending information in the Cluster. senderId is
     * used as a identifier so that information sent through this
     * instance can only be used with the respectice
     * <code>ClusterReceiver</code>
     *
     * @return The ClusterSender
     */
    public ClusterSender getClusterSender(String senderId) {
        Logger logger = null;
        MulticastSender send = new MulticastSender(senderId,
                                                   multicastSocket,
                                                   multicastAddress,
                                                   multicastPort);
        if (container != null)
            logger = container.getLogger();

        send.setLogger(logger);
        send.setDebug(debug);

        return(send);
    }

    /**
     * Returns a <code>ClusterReceiver</code> which is the interface
     * to use when receiving information in the Cluster. senderId is
     * used as a indentifier, only information send through the
     * <code>ClusterSender</code> with the same senderId can be received.
     *
     * @return The ClusterReceiver
     */
    public ClusterReceiver getClusterReceiver(String senderId) {
        Logger logger = null;
        MulticastReceiver recv = new MulticastReceiver(senderId,
                                                       multicastSocket,
                                                       multicastAddress,
                                                       multicastPort);

        if (container != null)
            logger = container.getLogger();

        recv.setDebug(debug);
        recv.setLogger(logger);
        recv.setCheckInterval(checkInterval);
        recv.start();

        return(recv);
    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        Logger logger = null;

        if (container != null)
            logger = container.getLogger();

        if (logger != null) {
            logger.log(getName() + "[" + container.getName() + "]: "
                       + message);
        } else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();

            System.out.println(getName() + "[" + containerName
                               + "]: " + message);
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
     * @param listener The listener to remove
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
        if (debug > 1)
            log("Started");

        try {
            multicastSocket = new MulticastSocket(multicastPort);

            if(multicastSocket != null && multicastAddress != null) {
                multicastSocket.joinGroup(multicastAddress);

                if (debug > 1)
                    log("Joining group: "+multicastAddress);
            } else {
                log("multicastSocket || multicastAddress can't be null");
            }
        } catch (IOException e) {
            log("An error occured when trying to join group");
        }

        // Validate and update our current component state
        if (started)
            ;

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
        if (debug > 1)
            log("Stopping");

        try {
            multicastSocket.leaveGroup(multicastAddress);
            multicastSocket = null;
        } catch (IOException e) {
            ;
        }

        if (debug > 1)
            log("Leaving group: "+multicastAddress);

        // Validate and update our current component state
        if (!started)
            ;

        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop the background reaper thread
        threadStop();
    }

    // ------------------------------------------------------ Background Thread

    /**
     * The background thread.
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
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
     * Start the background thread.
     */
    private void threadStart() {
        if (thread != null)
            return;

        threadDone = false;
        threadName = "StandardCluster[" + getClusterName() + "]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stop the background thread.
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
}
