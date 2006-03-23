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
package org.apache.catalina.tribes.tipis;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.catalina.tribes.tcp.*;

/**
 * A channel to handle RPC messaging
 * @author Filip Hanik
 */
public class RpcChannel implements ChannelListener{
    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(RpcChannel.class);
    
    public static final int FIRST_REPLY = 1;
    public static final int MAJORITY_REPLY = 2;
    public static final int ALL_REPLY = 3;
    
    private Channel channel;
    private RpcCallback callback;
    private byte[] rpcId;
    
    private HashMap responseMap = new HashMap();

    /**
     * Create an RPC channel. You can have several RPC channels attached to a group
     * all separated out by the uniqueness
     * @param rpcId - the unique Id for this RPC group
     * @param channel Channel
     * @param callback RpcCallback
     */
    public RpcChannel(byte[] rpcId, Channel channel, RpcCallback callback) {
        this.channel = channel;
        this.callback = callback;
        this.rpcId = rpcId;
        channel.addChannelListener(this);
    }
    
    
    /**
     * Send a message and wait for the response.
     * @param destination Member[] - the destination for the message, and the members you request a reply from
     * @param message Serializable - the message you are sending out
     * @param options int - FIRST_REPLY, MAJORITY_REPLY or ALL_REPLY
     * @param timeout long - timeout in milliseconds, if no reply is received within this time null is returned
     * @return Response[] - an array of response objects.
     * @throws ChannelException
     */
    public Response[] send(Member[] destination, 
                           Serializable message,
                           int rpcOptions, 
                           int channelOptions,
                           long timeout) throws ChannelException {
        
        if ( destination==null || destination.length == 0 ) return new Response[0];
        
        //avoid dead lock
        channelOptions = channelOptions & ~Channel.SEND_OPTIONS_SYNCHRONIZED_ACK;
        
        RpcCollectorKey key = new RpcCollectorKey(UUIDGenerator.randomUUID(false));
        RpcCollector collector = new RpcCollector(key,rpcOptions,destination.length,timeout);
        try {
            synchronized (collector) {
                responseMap.put(key, collector);
                RpcMessage rmsg = new RpcMessage(rpcId, key.id, message);
                channel.send(destination, rmsg, channelOptions);
                collector.wait(timeout);
            }
        } catch ( InterruptedException ix ) {
            Thread.currentThread().interrupted();
            throw new ChannelException(ix);
        }finally {
            responseMap.remove(key);
        }
        return collector.getResponses();
    }
    
    
    public void messageReceived(Serializable msg, Member sender) {
        RpcMessage rmsg = (RpcMessage)msg;
        RpcCollectorKey key = new RpcCollectorKey(rmsg.uuid);
        if ( rmsg.reply ) {
            RpcCollector collector = (RpcCollector)responseMap.get(key);
            if (collector == null) {
                callback.leftOver(rmsg.message, sender);
            } else {
                synchronized (collector) {
                    //make sure it hasn't been removed
                    if ( responseMap.containsKey(key) ) {
                        collector.addResponse(rmsg.message, sender);
                        if (collector.isComplete()) collector.notifyAll();
                    } else {
                        callback.leftOver(rmsg.message, sender);
                    }
                }//synchronized
            }//end if
        } else{
            Serializable reply = callback.replyRequest(rmsg.message,sender);
            rmsg.reply = true;
            rmsg.message = reply;
            try {
                channel.send(new Member[] {sender}, rmsg,0);
            }catch ( Exception x )  {
                log.error("Unable to send back reply in RpcChannel.",x);
            }
        }//end if
    }
    
    public void breakdown() {
        channel.removeChannelListener(this);
    }
    
    public void finalize() {
        breakdown();
    }
    
    public boolean accept(Serializable msg, Member sender) {
        if ( msg instanceof RpcMessage ) {
            RpcMessage rmsg = (RpcMessage)msg;
            return Arrays.equals(rmsg.rpcId,rpcId);
        }else return false;
    }
    
    public Channel getChannel() {
        return channel;
    }

    public RpcCallback getCallback() {
        return callback;
    }

    public byte[] getRpcId() {
        return rpcId;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setCallback(RpcCallback callback) {
        this.callback = callback;
    }

    public void setRpcId(byte[] rpcId) {
        this.rpcId = rpcId;
    }
    
    public static class RpcMessage implements Externalizable {
        
        private Serializable message;
        private byte[] uuid;
        private byte[] rpcId;
        private boolean reply = false;

        public RpcMessage() {
            //for serialization
        }
        
        public RpcMessage(byte[] rpcId, byte[] uuid, Serializable message) {
            this.rpcId = rpcId;
            this.uuid = uuid;
            this.message = message;
        }
        
        public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
            reply = in.readBoolean();
            int length = in.readInt();
            uuid = new byte[length];
            in.read(uuid, 0, length);
            length = in.readInt();
            rpcId = new byte[length];
            in.read(rpcId, 0, length);
            message = (Serializable)in.readObject();
        }
    
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeBoolean(reply);
            out.writeInt(uuid.length);
            out.write(uuid, 0, uuid.length);
            out.writeInt(rpcId.length);
            out.write(rpcId, 0, rpcId.length);
            out.writeObject(message);
        }

    }
    
    /**
     * 
     * Class that holds all response.
     * @author not attributable
     * @version 1.0
     */
    public static class RpcCollector {
        public ArrayList responses = new ArrayList(); 
        public RpcCollectorKey key;
        public int options;
        public int destcnt;
        public long timeout;
        
        public RpcCollector(RpcCollectorKey key, int options, int destcnt, long timeout) {
            this.key = key;
            this.options = options;
            this.destcnt = destcnt;
            this.timeout = timeout;
        }
        
        public void addResponse(Serializable message, Member sender){
            Response resp = new Response(sender,message);
            responses.add(resp);
        }
        
        public boolean isComplete() {
            switch (options) {
                case ALL_REPLY:
                    return destcnt == responses.size();
                case MAJORITY_REPLY:
                {
                    float perc = ((float)responses.size()) / ((float)destcnt);
                    return perc >= 0.50f;
                }
                case FIRST_REPLY:
                    return responses.size()>0;
                default:
                    return false;
            }
        }
        
        public int hashCode() {
            return key.hashCode();
        }
        
        public boolean equals(Object o) {
            if ( o instanceof RpcCollector ) {
                RpcCollector r = (RpcCollector)o;
                return r.key.equals(this.key);
            } else return false;
        }
        
        public Response[] getResponses() {
            return (Response[])responses.toArray(new Response[responses.size()]);
        }
    }
    
    public static class RpcCollectorKey {
        byte[] id;
        public RpcCollectorKey(byte[] id) {
            this.id = id;
        }
        
        public int hashCode() {
            return id[0]+id[1]+id[2]+id[3];
        }

        public boolean equals(Object o) {
            if ( o instanceof RpcCollectorKey ) {
                RpcCollectorKey r = (RpcCollectorKey)o;
                return Arrays.equals(id,r.id);
            } else return false;
        }
        
    }

}