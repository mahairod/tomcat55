/*
 * Copyright 1999,2004-2005 The Apache Software Foundation.
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

/**
 * Abstract class for the interceptor base class.
 * @author Filip Hanik
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */

public abstract class ChannelInterceptorBase implements MembershipListener, MessageListener {
    
    private ChannelInterceptorBase next;
    private ChannelInterceptorBase previous;
    
    public ChannelInterceptorBase() {
        
    }
    
    protected final void setNext(ChannelInterceptorBase next) {
        this.next = next;
    }
    
    public final ChannelInterceptorBase getNext() {
        return next;
    }
    
    protected final void setPrevious(ChannelInterceptorBase previous) {
        this.previous = previous;
    }

    public final ChannelInterceptorBase getPrevious() {
        return previous;
    }

    public ClusterMessage[] sendMessage(Member[] destination, ClusterMessage msg, int options) {
        return getNext().sendMessage(destination, msg,options);
    }
    
    public void messageReceived(ClusterMessage msg) {
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
    
    

    
    

    
    
    

}
