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

import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.MembershipListener;
import org.apache.catalina.cluster.MessageListener;
import java.io.IOException;
import org.apache.catalina.cluster.ChannelInterceptor;
import org.apache.catalina.cluster.InterceptorPayload;
import org.apache.catalina.cluster.io.ClusterData;

/**
 * Abstract class for the interceptor base class.
 * @author Filip Hanik
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */

public abstract class ChannelInterceptorBase implements ChannelInterceptor{
    
    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ChannelInterceptorBase.class);
    
    private ChannelInterceptor next;
    private ChannelInterceptor previous;
    
    public ChannelInterceptorBase() {
        
    }
    
    public final void setNext(ChannelInterceptor next) {
        this.next = next;
    }
    
    public final ChannelInterceptor getNext() {
        return next;
    }
    
    public final void setPrevious(ChannelInterceptor previous) {
        this.previous = previous;
    }

    public final ChannelInterceptor getPrevious() {
        return previous;
    }

    public ClusterMessage[] sendMessage(Member[] destination, ClusterData msg, InterceptorPayload payload) throws IOException {
        return getNext().sendMessage(destination, msg,payload);
    }
    
    public void messageReceived(ClusterData msg) {
        getPrevious().messageReceived(msg);
    }

    public boolean accept(ClusterMessage msg) {
        return true;
    }

    
    public void memberAdded(Member member) {
        //notify upwards
        if ( getPrevious()!=null ) getPrevious().memberAdded(member);
    }
    
    public void memberDisappeared(Member member) {
        //notify upwards
        if ( getPrevious()!=null ) getPrevious().memberDisappeared(member);
    }
    
    

    public void heartbeat() {
        if ( getNext() != null ) getNext().heartbeat();
    }
    

    
    
    

}
