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

import java.io.IOException;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.MessageListener;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.util.StringManager;

/**
 * Receive SessionID cluster change from other backup node after primary session node is failed.
 * 
 * @author Peter Rossbach
 * @version 1.0
 */
public class JvmRouteSessionIDBinderListener implements MessageListener {
    /*--Static Variables----------------------------------------*/
    public static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(JvmRouteSessionIDBinderListener.class);

    /**
     * The descriptive information about this implementation.
     */
    protected static final String info = "org.apache.catalina.session.JvmRouteSessionIDBinderListener/1.0";

    /*--Instance Variables--------------------------------------*/

    /**
     * The string manager for this package.
     */
    private StringManager sm = StringManager
            .getManager("org.apache.catalina.cluster.session");

    protected CatalinaCluster cluster = null;

    protected boolean started = false;

    /**
     * number of session that goes to this cluster node
     */
    private long numberOfSessions = 0;

    /*--Constructor---------------------------------------------*/
    
    public JvmRouteSessionIDBinderListener() {
    }

    /*--Logic---------------------------------------------------*/
    
    /**
     * Return descriptive information about this implementation.
     */
    public String getInfo() {

        return (info);

    }

    /**
     * @return Returns the numberOfSessions.
     */
    public long getNumberOfSessions() {
        return numberOfSessions;
    }

    /**
     * Add this Mover as Cluster Listener ( receiver)
     * @throws LifecycleException
     */
    public void start() throws LifecycleException {
        if (started)
            return;
        getCluster().addClusterListener(this);
        started = true;
        if (log.isInfoEnabled())
            log.info("Cluster JvmRouteSessionIDBinderListener started.");
    }

    /**
     * Remove this from Cluster Listener
     * @throws LifecycleException
     */
    public void stop() throws LifecycleException {
        started = false;
        getCluster().removeClusterListener(this);
        if (log.isInfoEnabled())
            log.info("Cluster JvmRouteSessionIDBinderListener stopped.");
    }

    /**
     * Callback from the cluster, when a message is received, The cluster will
     * broadcast it invoking the messageReceived on the receiver.
     * 
     * @param msg
     *            ClusterMessage - the message received from the cluster
     */
    public void messageReceived(ClusterMessage msg) {
        if (msg instanceof SessionIDMessage && msg != null) {
            SessionIDMessage sessionmsg = (SessionIDMessage) msg;
            if (log.isDebugEnabled())
                log
                        .debug("Cluster JvmRouteSessionIDBinderListener received session ID "
                                + sessionmsg.getOrignalSessionID()
                                + " set to "
                                + sessionmsg.getBackupSessionID()
                                + " for context path"
                                + sessionmsg.getContextPath());
            Container host = getCluster().getContainer();
            if (host != null) {
                Context context = (Context) host.findChild(sessionmsg
                        .getContextPath());
                if (context != null) {
                    try {
                        Session session = context.getManager().findSession(
                                sessionmsg.getOrignalSessionID());
                        if (session != null) {
                            session.setId(sessionmsg.getBackupSessionID());
                        } else if (log.isInfoEnabled())
                            log.info("Lost Session "
                                    + sessionmsg.getOrignalSessionID());
                    } catch (IOException e) {
                        log.error(e);
                    }

                } else if (log.isErrorEnabled())
                    log.error("Context " + sessionmsg.getContextPath()
                            + "not found at "
                            + ((StandardEngine) host.getParent()).getJvmRoute()
                            + "!");
            } else if (log.isErrorEnabled())
                log.error("No host found " + sessionmsg.getContextPath());
        }
    }

    /**
     * Accept only SessionIDMessages
	 * 
     * @param msg
     *            ClusterMessage
     * @return boolean - returns true to indicate that messageReceived should be
     *         invoked. If false is returned, the messageReceived method will
     *         not be invoked.
     */
    public boolean accept(ClusterMessage msg) {
        return (msg instanceof SessionIDMessage);
    }

    /*--Instance Getters/Setters--------------------------------*/
    public CatalinaCluster getCluster() {
        return cluster;
    }

    public void setCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }

    public boolean equals(Object listener) {
        return super.equals(listener);
    }

    public int hashCode() {
        return super.hashCode();
    }

}

