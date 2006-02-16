/*
 * Copyright 1999,2004-2006 The Apache Software Foundation.
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
package org.apache.catalina.cluster.group;


import org.apache.catalina.cluster.ChannelException;
import org.apache.catalina.cluster.ClusterChannel;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ClusterSender;
import org.apache.catalina.cluster.MembershipService;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.Member;


/**
 * Channel interface
 * A channel is an object that manages a group of members.
 * It manages a complete cluster group, both membership and replication.
 * @author Filip Hanik
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */
public class GroupChannel implements ClusterChannel {
    private ChannelCoordinator coordinator = new ChannelCoordinator();
    private ChannelInterceptorBase interceptors = null;

    public GroupChannel() {
    }
    
    
    /**
     * Adds an interceptor to the stack for message processing
     * @param interceptor ChannelInterceptorBase
     */
    public void addInterceptor(ChannelInterceptorBase interceptor) { 
        if ( interceptors == null ) {
            this.interceptors = interceptor;
            this.interceptors.setNext(coordinator);
            coordinator.setPrevious(this.interceptors);
        } else {
            ChannelInterceptorBase last = interceptors;
            while ( last.getNext() != coordinator ) {
                last = last.getNext();
            }
            last.setNext(interceptor);
            interceptor.setNext(coordinator);
            interceptor.setPrevious(last);
            coordinator.setPrevious(interceptor);
        }
    }
    
    
    
    /**
     * Send a message to one or more members in the cluster
     * @param destination Member[] - the destinations, null or zero length means all
     * @param msg ClusterMessage - the message to send
     * @param options int - sender options, see class documentation
     * @return ClusterMessage[] - the replies from the members, if any.
     */
    public ClusterMessage[] send(Member[] destination, ClusterMessage msg, int options) throws ChannelException {
        throw new UnsupportedOperationException("Method send not yet implemented.");
    }
    
    /**
     * Starts up the channel. This can be called multiple times for individual services to start
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * DEFAULT - will start all services <BR>
     * MBR_RX_SEQ - starts the membership receiver <BR>
     * MBR_TX_SEQ - starts the membership broadcaster <BR>
     * SND_TX_SEQ - starts the replication transmitter<BR>
     * SND_RX_SEQ - starts the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     */
    public void start(int svc) throws ChannelException {
        coordinator.start(svc);
    }

    /**
     * Shuts down the channel. This can be called multiple times for individual services to shutdown
     * The svc parameter can be the logical or value of any constants
     * @param svc int value of <BR>
     * DEFAULT - will shutdown all services <BR>
     * MBR_RX_SEQ - starts the membership receiver <BR>
     * MBR_TX_SEQ - starts the membership broadcaster <BR>
     * SND_TX_SEQ - starts the replication transmitter<BR>
     * SND_RX_SEQ - starts the replication receiver<BR>
     * @throws ChannelException if a startup error occurs or the service is already started.
     */
    public void stop(int svc) throws ChannelException {
        coordinator.stop(svc);
    }

    public ClusterReceiver getClusterReceiver() {
        return coordinator.getClusterReceiver();
    }

    public ClusterSender getClusterSender() {
        return coordinator.getClusterSender();
    }

    public MembershipService getMembershipService() {
        return coordinator.getMembershipService();
    }

    public void setClusterReceiver(ClusterReceiver clusterReceiver) {
        coordinator.setClusterReceiver(clusterReceiver);
    }

    public void setClusterSender(ClusterSender clusterSender) {
        coordinator.setClusterSender(clusterSender);
    }

    public void setMembershipService(MembershipService membershipService) {
        coordinator.setMembershipService(membershipService);
    }

}
