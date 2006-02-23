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

import org.apache.catalina.cluster.MembershipService;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.ChannelException;
import org.apache.catalina.cluster.ClusterSender;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ClusterChannel;
import java.io.IOException;


/**
 * The channel coordinator object coordinates the membership service,
 * the sender and the receiver.
 * This is the last interceptor in the chain.
 * @author Filip Hanik
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */
public class ChannelCoordinator extends ChannelInterceptorBase {
    private ClusterReceiver clusterReceiver;
    private ClusterSender clusterSender;
    private MembershipService membershipService;

    public ChannelCoordinator() {
        
    }
    
    public ChannelCoordinator(ClusterReceiver receiver,
                              ClusterSender sender,
                              MembershipService service) {
        this();
        this.setClusterReceiver(receiver);
        this.setClusterSender(sender);
        this.setMembershipService(service);
    }
    
    /**
     * Send a message to one or more members in the cluster
     * @param destination Member[] - the destinations, null or zero length means all
     * @param msg ClusterMessage - the message to send
     * @param options int - sender options, see class documentation
     * @return ClusterMessage[] - the replies from the members, if any.
     */
    public ClusterMessage[] sendMessage(Member[] destination, ClusterMessage msg, int options) throws IOException {
        if ( destination == null ) destination = membershipService.getMembers();
        for ( int i=0; i<destination.length; i++ ) {
            clusterSender.sendMessage(msg,destination[i]);
        }
        return null;
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
        try {
            if ( (svc & ClusterChannel.MBR_RX_SEQ) == ClusterChannel.MBR_RX_SEQ) membershipService.start(MembershipService.MBR_RX);
            if ( (svc & ClusterChannel.SND_RX_SEQ) == ClusterChannel.SND_RX_SEQ) clusterReceiver.start();
            if ( (svc & ClusterChannel.SND_TX_SEQ) == ClusterChannel.SND_TX_SEQ) clusterSender.start();
            if ( (svc & ClusterChannel.MBR_TX_SEQ) == ClusterChannel.MBR_TX_SEQ) membershipService.start(MembershipService.MBR_TX);
        }catch ( ChannelException cx ) {
            throw cx;
        }catch ( Exception x ) {
            throw new ChannelException(x);
        }
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
        try {
            if ( (svc & ClusterChannel.MBR_RX_SEQ) == ClusterChannel.MBR_RX_SEQ) membershipService.stop();
            if ( (svc & ClusterChannel.SND_RX_SEQ) == ClusterChannel.SND_RX_SEQ) clusterReceiver.stop();
            if ( (svc & ClusterChannel.SND_TX_SEQ) == ClusterChannel.SND_TX_SEQ) clusterSender.stop();
            if ( (svc & ClusterChannel.MBR_TX_SEQ) == ClusterChannel.MBR_RX_SEQ) membershipService.stop();
        }catch ( Exception x ) {
            throw new ChannelException(x);
        }

    }
    
    public void memberAdded(Member member){
        if ( clusterSender!=null ) clusterSender.add(member);
        super.memberAdded(member);
    }
    
    public void memberDisappeared(Member member){
        if ( clusterSender!=null ) clusterSender.remove(member);
        super.memberDisappeared(member);
    }


    public ClusterReceiver getClusterReceiver() {
        return clusterReceiver;
    }

    public ClusterSender getClusterSender() {
        return clusterSender;
    }

    public MembershipService getMembershipService() {
        return membershipService;
    }

    public void setClusterReceiver(ClusterReceiver clusterReceiver) {
        if ( clusterReceiver != null ) {
            this.clusterReceiver = clusterReceiver;
            this.clusterReceiver.setMessageListener(this);
        } else {
            if  (this.clusterReceiver!=null ) this.clusterReceiver.setMessageListener(null);
            this.clusterReceiver = null;
        }
    }

    public void setClusterSender(ClusterSender clusterSender) {
        this.clusterSender = clusterSender;
    }

    public void setMembershipService(MembershipService membershipService) {
        this.membershipService = membershipService;
        this.membershipService.setMembershipListener(this);
    }
   
}
