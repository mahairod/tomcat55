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

import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.tribes.Channel;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import org.apache.catalina.tribes.io.DirectByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.util.Set;
import java.util.LinkedHashMap;
import org.apache.catalina.tribes.ChannelListener;
import java.util.Collection;
import org.apache.catalina.tribes.MembershipListener;

/**
 * @author Filip Hanik
 * @version 1.0
 */
public class LazyReplicatedMap extends LinkedHashMap 
    implements RpcCallback, ChannelListener, MembershipListener {
    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LazyReplicatedMap.class);
    
//------------------------------------------------------------------------------    
//              INSTANCE VARIABLES
//------------------------------------------------------------------------------   

    private Channel channel;
    private RpcChannel rpcChannel;
    
    
//------------------------------------------------------------------------------    
//              CONSTRUCTORS / DESTRUCTORS
//------------------------------------------------------------------------------   
    public LazyReplicatedMap(Channel channel, String mapContextName, int initialCapacity, float loadFactor) {
        super(initialCapacity,loadFactor);
        init(channel,mapContextName);
    }

    public LazyReplicatedMap(Channel channel, String mapContextName, int initialCapacity) {
        super(initialCapacity);
        init(channel,mapContextName);
    }

    public LazyReplicatedMap(Channel channel, String mapContextName) {
        super();
        init(channel,mapContextName);
    }
    
    void init(Channel channel, String mapContextName) {
        final String chset = "ISO-8859-1";
        this.channel = channel;
        try {
            this.rpcChannel = new RpcChannel(mapContextName.getBytes(chset), channel, this);
        }catch (UnsupportedEncodingException x) {
            log.warn("Unable to encode mapContextName["+mapContextName+"] using getBytes("+chset+") using default getBytes()",x);
            this.rpcChannel = new RpcChannel(mapContextName.getBytes(), channel, this);
        }
        this.channel.addChannelListener(this);
        this.channel.addMembershipListener(this);
        
    }
    
    public void breakDown() {
        finalize();
    }
    
    public void finalize() {
        if ( this.rpcChannel!=null ) {
            this.rpcChannel.breakDown();
        }
        if ( this.channel != null ) {
            this.channel.removeChannelListener(this);
            this.channel.removeMembershipListener(this);
        }
        this.rpcChannel = null;
        this.channel = null;
    }
    
//------------------------------------------------------------------------------    
//              GROUP COM INTERFACES
//------------------------------------------------------------------------------   
    /**
     * 
     * @param msg Serializable
     * @return Serializable - null if no reply should be sent
     */
    public Serializable replyRequest(Serializable msg, Member sender) {
        throw new UnsupportedOperationException();
    }

    /**
     * If the reply has already been sent to the requesting thread,
     * the rpc callback can handle any data that comes in after the fact.
     * @param msg Serializable
     * @param sender Member
     */
    public void leftOver(Serializable msg, Member sender) {
        throw new UnsupportedOperationException();
    }

    public void messageReceived(Serializable msg, Member sender) {
        throw new UnsupportedOperationException();
    }
    
    public boolean accept(Serializable msg, Member sender) {
        throw new UnsupportedOperationException();
    }
    
    public void memberAdded(Member member) {
        
    }
    public void memberDisappeared(Member member) {
        
    }
    
//------------------------------------------------------------------------------    
//              METHODS TO OVERRIDE    
//------------------------------------------------------------------------------

    public Object get(Object key) {
        return super.get(key);
    }

    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    public Object put(Object key, Object value) {
        return super.put(key,value);
    }

    public void putAll(Map m) {
        super.putAll(m);
    }

    public Object remove(Object key) {
        return super.remove(key);
    }

    public void clear() {
        super.clear();
    }

    public boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    public Object clone() {
        return super.clone();
    }
    
    public Set entrySet() {
        return super.entrySet();
    }
    
    public Set keySet() {
        return super.keySet();
    }
    
    public int size() {
        return super.size();
    }
    
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return false;
    }
    
    public boolean isEmpty() {
        return super.isEmpty();
    }
    
    public Collection values() {
        return super.values();
    }
    

//------------------------------------------------------------------------------    
//                Map Entry class    
//------------------------------------------------------------------------------
    public static class MapEntry implements Map.Entry {
        private boolean backup;
        private boolean proxy;
        private Member  backupNode;
        
        private Serializable key;
        private Serializable value;
        
        public MapEntry(Serializable key, Serializable value) {
            this.key = key;
            this.value = value;
        }
        
        public boolean isBackup() {
            return backup;
        }
        
        public void setBackup(boolean backup) {
            this.backup = backup;
        }
        
        public boolean isProxy() {
            return proxy;
        }

        public void setProxy(boolean proxy) {
            this.proxy = proxy;
        }

        public boolean isDiffable() {
            return (value instanceof Diffable);
        }
        
        public void setBackupNode(Member node) {
            this.backupNode = node;
        }
        
        public Member getBackupNode() {
            return backupNode;
        }
        
        
        
        public Object getValue() {
            return value;
        }
        
        public Object setValue(Object value) {
            Object old = this.value;
            this.value = (Serializable)value;
            return old;
        }
        
        public Object getKey() {
            return key;
        }


        public byte[] getDiff() throws IOException {
            if ( isDiffable() ) {
                return ((Diffable)value).getDiff();
            } else {
                return getData();
            }
        }
        
        public int hashCode() {
            return key.hashCode();
        }
        
        public boolean equals(Object o) {
            return key.equals(o);
        }
        
        /**
         * returns the entire object as a byte array
         * @return byte[]
         * @throws IOException
         */
        public byte[] getData() throws IOException {
            return (new ObjectStreamable(value)).getBuf().getArray();
        }
        
        /**
         * apply a diff, or an entire object
         * @param data byte[]
         * @param offset int
         * @param length int
         * @param diff boolean
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public void apply(byte[] data, int offset, int length, boolean diff) throws IOException, ClassNotFoundException {
            if ( isDiffable() && diff ) {
                ((Diffable)value).applyDiff(data,offset,length);
            } else if ( length == 0 ) {
                value = null;
                proxy = true;
            } else {
                value = XByteBuffer.deserialize(data,offset,length);
            }
        }
        
    }

//------------------------------------------------------------------------------    
//                streamable class    
//------------------------------------------------------------------------------
    
    public static class ObjectStreamable implements Streamable {
        private DirectByteArrayOutputStream buf;
        private int pos=0;
        public ObjectStreamable(Serializable value) throws IOException {
            buf = new DirectByteArrayOutputStream(1024);
            ObjectOutputStream out = new ObjectOutputStream(buf);
            out.writeObject(value);
            out.flush();
        }
        
        /**
         * returns true if the stream has reached its end
         * @return boolean
         */
        public synchronized boolean eof() {
            return (pos>=buf.size());
    
        }
    
        /**
         * write data into the byte array starting at offset, maximum bytes read are (data.length-offset)
         * @param data byte[] - the array to read data into
         * @param offset int - start position for writing data
         * @return int - the number of bytes written into the data buffer
         */
        public synchronized int write(byte[] data, int offset) throws IOException {
            int length = Math.min(data.length-offset,buf.size()-pos);
            System.arraycopy(buf.getArrayDirect(),pos,data,offset,length);
            pos = pos + length;
            return length;
        }
        
        public DirectByteArrayOutputStream getBuf() {
            return buf;
        }
        
        public int size() {
            return buf.size();
        }
        
        public int pos() {
            return pos;
        }

    }

}