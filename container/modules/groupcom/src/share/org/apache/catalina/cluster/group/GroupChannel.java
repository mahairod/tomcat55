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
import org.apache.catalina.cluster.ChannelInterceptor;
import org.apache.catalina.cluster.Channel;
import org.apache.catalina.cluster.ChannelMessage;
import org.apache.catalina.cluster.ChannelReceiver;
import org.apache.catalina.cluster.ChannelSender;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.MembershipListener;
import org.apache.catalina.cluster.MembershipService;
import org.apache.catalina.cluster.MessageListener;
import org.apache.catalina.cluster.io.ClusterData;
import org.apache.catalina.cluster.io.XByteBuffer;
import java.io.Serializable;
import org.apache.catalina.cluster.ChannelListener;

/**
 * The GroupChannel manages the replication channel. It coordinates
 * message being sent and received with membership announcements.
 * The channel has an chain of interceptors that can modify the message or perform other logic.
 * It manages a complete cluster group, both membership and replication.
 * @author Filip Hanik
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */
public class GroupChannel extends ChannelInterceptorBase implements Channel {
    private ChannelCoordinator coordinator = new ChannelCoordinator();
    private ChannelInterceptor interceptors = null;
    private MembershipListener membershipListener;
    private ChannelListener channelListener;

    public GroupChannel() {
        addInterceptor(this);
    }
    
    
    /**
     * Adds an interceptor to the stack for message processing
     * @param interceptor ChannelInterceptorBase
     */
    public void addInterceptor(ChannelInterceptor interceptor) { 
        if ( interceptors == null ) {
            this.interceptors = interceptor;
            this.interceptors.setNext(coordinator);
            this.interceptors.setPrevious(null);
        } else {
            ChannelInterceptor last = interceptors;
            while ( last.getNext() != coordinator ) {
                last = last.getNext();
            }
            last.setNext(interceptor);
            interceptor.setNext(coordinator);
            interceptor.setPrevious(last);
            coordinator.setPrevious(interceptor);
        }
    }
    
    public void heartbeat() {
        super.heartbeat();
    }
    
    /**
     * Send a message to one or more members in the cluster
     * @param destination Member[] - the destinations, null or zero length means all
     * @param msg ClusterMessage - the message to send
     * @param options int - sender options, see class documentation
     * @return ClusterMessage[] - the replies from the members, if any.
     */
    public ChannelMessage[] send(Member[] destination, Serializable msg, int options) throws ChannelException {
        if ( msg == null ) return null;
        try {
            ClusterData data = XByteBuffer.serialize(msg, options,getMembershipService().getLocalMember());
            return getFirstInterceptor().sendMessage(destination, data, null);
        }catch ( Exception x ) {
            throw new ChannelException(x);
        }
    }
    
    public void messageReceived(ChannelMessage msg) {
        if ( msg == null ) return;
        else if ( msg instanceof ClusterData ) {
            try {
                Serializable fwd = XByteBuffer.deserialize( (ClusterData) msg);
                if ( channelListener != null ) channelListener.messageReceived(fwd,msg.getAddress());
            }catch ( Exception x ) {
                log.error("Unable to deserialize channel message.",x);
            }
        } else {
            log.error("Recieved a message that is not a ClusterData instance. class="+msg.getClass().getName()+ " obj="+msg);
        }
    }
    
    public void memberAdded(Member member) {
        //notify upwards
        if (membershipListener != null) membershipListener.memberAdded(member);
    }
    
    public void memberDisappeared(Member member) {
        //notify upwards
        if (membershipListener != null) membershipListener.memberDisappeared(member);
    }    
    
    public ChannelInterceptor getFirstInterceptor() {
        if (interceptors != null) return interceptors;
        else return coordinator;
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

    public ChannelReceiver getClusterReceiver() {
        return coordinator.getClusterReceiver();
    }

    public ChannelSender getClusterSender() {
        return coordinator.getClusterSender();
    }

    public MembershipService getMembershipService() {
        return coordinator.getMembershipService();
    }

    public void setClusterReceiver(ChannelReceiver clusterReceiver) {
        coordinator.setClusterReceiver(clusterReceiver);
    }

    public void setClusterSender(ChannelSender clusterSender) {
        coordinator.setClusterSender(clusterSender);
    }

    public void setMembershipService(MembershipService membershipService) {
        coordinator.setMembershipService(membershipService);
    }

    public void setMembershipListener(MembershipListener membershipListener) {
        this.membershipListener = membershipListener;
    }

    public void setChannelListener(ChannelListener channelListener) {

        this.channelListener = channelListener;
    }

    public MembershipListener getMembershipListener() {
        return membershipListener;
    }

    public ChannelListener getChannelListener() {

        return channelListener;
    }

    /**
     * has members
     */
    public boolean hasMembers() {
        return coordinator.getMembershipService().hasMembers();
    }

    /**
     * Get all current cluster members
     * @return all members or empty array
     */
    public Member[] getMembers() {
        return coordinator.getMembershipService().getMembers();
    }

    /**
     * Return the member that represents this node.
     *
     * @return Member
     */
    public Member getLocalMember() {
        return coordinator.getMembershipService().getLocalMember();
    }

}
