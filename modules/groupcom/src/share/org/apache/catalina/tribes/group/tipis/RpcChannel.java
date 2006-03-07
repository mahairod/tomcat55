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
package org.apache.catalina.tribes.group.tipis;

import java.io.Serializable;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;

/**
 * A channel to handle RPC messaging
 * @author Filip Hanik
 */
public class RpcChannel {
    
    public static final int FIRST_REPLY = 1;
    public static final int MAJORITY_REPLY = 2;
    public static final int ALL_REPLY = 3;
    
    private Channel channel;
    private RpcCallback callback;
    private String rpcId;

    /**
     * Create an RPC channel. You can have several RPC channels attached to a group
     * all separated out by the uniqueness
     * @param rpcId - the unique Id for this RPC group
     * @param channel Channel
     * @param callback RpcCallback
     */
    public RpcChannel(String rpcId, Channel channel, RpcCallback callback) {
        this.channel = channel;
        this.callback = callback;
        this.rpcId = rpcId;
    }
    
    
    /**
     * Send a message and wait for the response.
     * @param destination Member[] - the destination for the message, and the members you request a reply from
     * @param message Serializable - the message you are sending out
     * @param options int - FIRST_REPLY, MAJORITY_REPLY or ALL_REPLY
     * @param timeout long - timeout in milliseconds, if no reply is received within this time an exception is thrown
     * @return Response[] - an array of response objects.
     * @throws ChannelException
     */
    public Response[] send(Member[] destination, 
                           Serializable message,
                           int options, 
                           long timeout) throws ChannelException {
        throw new UnsupportedOperationException();
    }
    
    public Channel getChannel() {
        return channel;
    }

    public RpcCallback getCallback() {
        return callback;
    }

    public String getRpcId() {
        return rpcId;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setCallback(RpcCallback callback) {
        this.callback = callback;
    }

    public void setRpcId(String rpcId) {
        this.rpcId = rpcId;
    }

}