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

package org.apache.catalina.cluster;

import java.util.Map;

import org.apache.catalina.Cluster;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.commons.logging.Log;

/**
 * A <b>CatalinaCluster</b> interface allows to plug in and out the 
 * different cluster implementations
 *
 * @author Filip Hanik
 * @version $Revision$, $Date$
 */

public interface CatalinaCluster
    extends Cluster{
    // ----------------------------------------------------- Instance Variables

    /**
     * Descriptive information about this component implementation.
     */
    public String info = "CatalinaCluster/2.0";
    
    /**
     * Start the cluster, the owning container will invoke this
     * @throws Exception - if failure to start cluster
     */
    public void start() throws Exception;
    
    /**
     * Stops the cluster, the owning container will invoke this
     * @throws LifecycleException
     */
    public void stop() throws LifecycleException;
    
    /**
     * Returns the associates logger with this cluster
     * @return Log
     */
    public Log getLogger();
    
    /**
     * receive a message to all the members in the cluster
     * @param msg ClusterMessage
     */
    public void receive(ClusterMessage msg);
 
    /**
     * Sends a message to all the members in the cluster
     * @param msg ClusterMessage
     */
    public void send(ClusterMessage msg);
    
    /**
     * Sends a message to a specific member in the cluster
     * @param msg ClusterMessage
     * @param dest Member
     */
    public void send(ClusterMessage msg, Member dest);
    
    /**
     * Sends a message to a all members at local cluster domain
     * @param msg ClusterMessage
     */
    public void sendClusterDomain(ClusterMessage message);

    /**
     * returns all the members currently participating in the cluster
     * @return Member[]
     */
    public Member[] getMembers();
    
    /**
     * Return the member that represents this node.
     * @return Member
     */
    public Member getLocalMember();
    
    public void setClusterSender(ClusterSender sender);
    
    public ClusterSender getClusterSender();
    
    public void setClusterReceiver(ClusterReceiver receiver);
    
    public ClusterReceiver getClusterReceiver();
    
    public void setMembershipService(MembershipService service);
    
    public MembershipService getMembershipService();
    
    public void addValve(Valve valve);
    
    public void addClusterListener(MessageListener listener);
    
    public void removeClusterListener(MessageListener listener);
    
    public void setClusterDeployer(ClusterDeployer deployer);
    
    public ClusterDeployer getClusterDeployer();
    
    /**
     * @return
     */
    public Map getManagers();
    public Manager getManager(String name);
    public void removeManager(String name,Manager manager);
    public void addManager(String name,Manager manager);
    public Valve[] getValves();

}
