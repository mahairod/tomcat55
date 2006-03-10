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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.io.DirectByteArrayOutputStream;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.mcast.McastMember;

/**
 * @todo implement periodic sync/transfer 
 * @author Filip Hanik
 * @version 1.0
 */
public class LazyReplicatedMap extends LinkedHashMap 
    implements RpcCallback, ChannelListener, MembershipListener {
    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LazyReplicatedMap.class);
    protected static long TIME_OUT = 15000;//hard coded timeout
    
//------------------------------------------------------------------------------    
//              INSTANCE VARIABLES
//------------------------------------------------------------------------------   

    private transient Channel channel;
    private transient RpcChannel rpcChannel;
    private transient byte[] mapContextName;
    private transient boolean stateTransferred = false;
    
    
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
            this.mapContextName = mapContextName.getBytes(chset);
        }catch (UnsupportedEncodingException x) {
            log.warn("Unable to encode mapContextName["+mapContextName+"] using getBytes("+chset+") using default getBytes()",x);
            this.mapContextName = mapContextName.getBytes();
        }
        this.rpcChannel = new RpcChannel(this.mapContextName, channel, this);
        this.channel.addChannelListener(this);
        this.channel.addMembershipListener(this);
        transferState();
        
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
        super.clear();
        this.stateTransferred = false;
    }
    
    /**
     * Replicates any changes to the object since the last time
     * The object has to be primary, ie, if the object is a proxy or a backup, it will not be replicated<br>
     * @param complete - if set to true, the object is replicated to its backup
     * if set to false, only objects that implement ReplicatedMapEntry and the isDirty() returns true will
     * be replicated
     */
    public void replicate(Object key, boolean complete) {
        MapEntry entry = (MapEntry) super.get(key);
        if (entry!=null && entry.isPrimary() ) {
            Object value = entry.getValue();
            boolean repl = complete || ((value instanceof ReplicatedMapEntry) && ((ReplicatedMapEntry)value).isDirty());
            if (!repl) return;

            boolean diff = ((value instanceof ReplicatedMapEntry) && ((ReplicatedMapEntry)value).isDiffable());
            MapMessage msg = null;
            if ( diff ) {
                try {
                    msg = new MapMessage(mapContextName, MapMessage.MSG_BACKUP,
                                         true, (Serializable) entry.getKey(), null,
                                         ( (ReplicatedMapEntry) entry.getValue()).getDiff(),
                                         entry.getBackupNode());
                }catch (IOException x ) {
                    log.error("Unable to diff object. Will replicate the entire object instead.",x);
                }
            }
            if ( msg == null ) {
                msg = new MapMessage(mapContextName, MapMessage.MSG_BACKUP,
                                     false, (Serializable) entry.getKey(), 
                                     (Serializable)entry.getValue(),
                                     null,entry.getBackupNode());

            }
            try {
                channel.send(new Member[] {entry.getBackupNode()}, msg);
            } catch ( ChannelException x ) {
                log.error("Unable to replicate data.",x);
            }
        }//end if

    }
    
    /**
     * This can be invoked by a periodic thread to replicate out any changes.
     * For maps that don't store objects that implement ReplicatedMapEntry, this
     * method should be used infrequently to avoid large amounts of data transfer
     * @param complete boolean
     */
    public void replicate(boolean complete) {
        Iterator i = super.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            replicate(e.getKey(),complete);
        } //while

    }
    
//------------------------------------------------------------------------------    
//              GROUP COM INTERFACES
//------------------------------------------------------------------------------   
    public void transferState() {
        try {
            Member backup = channel.getMembers().length>0?channel.getMembers()[0]:null;
            if ( backup != null ) {
                MapMessage msg = new MapMessage(mapContextName,MapMessage.MSG_STATE,false,
                                                null,null,null,null);
                Response[] resp = rpcChannel.send(new Member[] {backup},msg,rpcChannel.FIRST_REPLY,TIME_OUT);
                if ( resp.length > 0 ) {
                    msg = (MapMessage)resp[0].getMessage();
                    ArrayList list = (ArrayList)msg.getValue();
                    for (int i=0; i<list.size(); i++ ) {
                        MapMessage m = (MapMessage)list.get(i);
                        MapEntry entry = new MapEntry(m.getKey(),m.getValue());
                        entry.setBackup(false);
                        entry.setProxy(true);
                        entry.setBackupNode(m.getBackupNode());
                        super.put(entry.getKey(),entry);
                    }
                }
            }
        } catch ( ChannelException x ) {
            log.error("Unable to transfer LazyReplicatedMap state.",x);
        }
        stateTransferred = true;
    }
    
    /**
     * @todo implement state transfer
     * @param msg Serializable
     * @return Serializable - null if no reply should be sent
     */
    public Serializable replyRequest(Serializable msg, Member sender) {
        if ( !(msg instanceof MapMessage) ) return null;
        MapMessage mapmsg = (MapMessage)msg;
        
        //backup request
        if ( mapmsg.getMsgType() == mapmsg.MSG_RETRIEVE_BACKUP ) {
            MapEntry entry = (MapEntry)super.get(mapmsg.getKey());
            if (entry == null)return null;
            mapmsg.setValue( (Serializable) entry.getValue());
            return mapmsg;
        }
        
        //state transfer request
        if ( mapmsg.getMsgType() == mapmsg.MSG_STATE ) {
            ArrayList list = new ArrayList();
            Iterator i = super.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                MapEntry entry = (MapEntry) e.getValue();
                MapMessage me = new MapMessage(mapContextName,MapMessage.MSG_PROXY,
                                               false,(Serializable)entry.getKey(),(Serializable)entry.getValue(),
                                               null,entry.getBackupNode());
                list.add(me);
            }
            mapmsg.setValue(list);
            return mapmsg;
        }
        
        return null;

    }

    /**
     * If the reply has already been sent to the requesting thread,
     * the rpc callback can handle any data that comes in after the fact.
     * @param msg Serializable
     * @param sender Member
     */
    public void leftOver(Serializable msg, Member sender) {
        //ignore left over responses
    }

    public void messageReceived(Serializable msg, Member sender) {
        //todo implement all the messages that we can receive
        //messages we can receive are MSG_PROXY, MSG_BACKUP
        if ( !(msg instanceof MapMessage) ) return;

        MapMessage mapmsg = (MapMessage)msg;
        if ( mapmsg.getMsgType() == MapMessage.MSG_PROXY ) {
            MapEntry entry = new MapEntry(mapmsg.getKey(),mapmsg.getValue());
            entry.setBackup(false);
            entry.setProxy(true);
            entry.setBackupNode(mapmsg.getBackupNode());
            super.put(entry.getKey(),entry);
        }
        
        if ( mapmsg.getMsgType() == MapMessage.MSG_BACKUP ) {
            MapEntry entry = (MapEntry)super.get(mapmsg.getKey());
            if ( entry == null ) {
                entry = new MapEntry(mapmsg.getKey(),mapmsg.getValue());
                entry.setBackup(true);
                entry.setProxy(false);
                entry.setBackupNode(mapmsg.getBackupNode());
                super.put(entry.getKey(), entry);
            } else {
                if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                    ReplicatedMapEntry diff = (ReplicatedMapEntry)entry.getValue();
                    if ( mapmsg.isDiff() ) {
                        try {
                            diff.applyDiff(mapmsg.getDiffValue(), 0, mapmsg.getDiffValue().length);
                        }catch ( IOException x ) {
                            log.error("Unable to apply diff to key:"+entry.getKey(),x);
                        }
                    } else {
                        entry.setValue(mapmsg.getValue());
                    }//end if
                } else {
                    entry.setValue(mapmsg.getValue());
                }//end if
            }//end if
        }//end if
        
    }
    
    public boolean accept(Serializable msg, Member sender) {
        if ( msg instanceof MapMessage ) {
            return Arrays.equals(mapContextName,((MapMessage)msg).getMapId());
        }
        return false;
    }
    
    public void memberAdded(Member member) {
        //do nothing, we don't care
    }
    public void memberDisappeared(Member member) {
        //todo move all sessions that are primary here to and have this member as 
        //a backup
        Iterator i = super.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry e = (Map.Entry)i.next();
            MapEntry entry = (MapEntry)e.getValue();
            if ( entry.isPrimary() && member.equals(entry.getBackupNode())) {
                try {
                    Member backup = publishEntryInfo(entry.getKey(), entry.getValue());
                    entry.setBackupNode(backup);
                }catch ( ChannelException x ) {
                    log.error("Unable to relocate["+entry.getKey()+"] to a new backup node",x);
                }
            }//end if
        }//while
    }
    
    int currentNode = 0;
    public Member getNextBackupNode() {
        Member[] members = channel.getMembers();
        if ( members.length == 0 ) return null;
        int node = currentNode++;
        if ( node >= members.length ) {
            node = 0;
            currentNode = 0;
        }
        return members[node];
    }
    
    
    
//------------------------------------------------------------------------------    
//              METHODS TO OVERRIDE    
//------------------------------------------------------------------------------
    /**
     * publish info about a map pair (key/value) to other nodes in the cluster
     * @param key Object
     * @param value Object
     * @return Member
     * @throws ChannelException
     */
    protected Member publishEntryInfo(Object key, Object value) throws ChannelException {
        //select a backup node
        Member backup = getNextBackupNode();
        //publish the data out to all nodes
        MapMessage msg = new MapMessage(this.mapContextName, MapMessage.MSG_PROXY, false,
                                        (Serializable) key, null, null, backup);
        channel.send(channel.getMembers(), msg);

        //publish the backup data to one node
        msg = new MapMessage(this.mapContextName, MapMessage.MSG_BACKUP, false,
                             (Serializable) key, (Serializable) value, null, backup);
        channel.send(new Member[] {backup}, msg);
        return backup;
    }
    
    public Object get(Object key) {
        MapEntry entry = (MapEntry)super.get(key);
        if ( entry == null ) return null;
        if ( !entry.isPrimary() ) {
            try {
                MapMessage msg = new MapMessage(mapContextName, MapMessage.MSG_RETRIEVE_BACKUP, false,
                                                (Serializable) key, null, null, null);
                Response[] resp = rpcChannel.send(new Member[] {entry.getBackupNode()},
                                                  msg, this.rpcChannel.FIRST_REPLY, TIME_OUT);
                if (resp == null || resp.length == 0) {
                    //no responses
                    log.warn("Unable to retrieve object for key:" + key);
                    return null;
                }
                msg = (MapMessage) resp[0].getMessage();
                
                Member backup = entry.getBackupNode();
                if (entry.isBackup()) {
                    //select a new backup node
                    backup = publishEntryInfo(key, msg.getValue());
                }
                entry.setBackupNode(backup);
                entry.setBackup(false);
                entry.setProxy(false);
                entry.setValue(msg.getValue());
            } catch (ChannelException x) {
                log.error("Unable to replicate out data for a LazyReplicatedMap.get operation", x);
                return null;
            }
        }
        return entry.getValue();
    }

    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    public Object put(Object key, Object value) {
        if ( !(key instanceof Serializable) ) throw new IllegalArgumentException("Key is not serializable:"+key.getClass().getName());
        if ( value == null ) return remove(key);
        if ( !(value instanceof Serializable) ) throw new IllegalArgumentException("Value is not serializable:"+value.getClass().getName());

        MapEntry entry = new MapEntry((Serializable)key,(Serializable)value);
        entry.setBackup(false);
        entry.setProxy(false);
        
        Object old = null;
        
        //make sure that any old values get removed
        if ( containsKey(key) ) old = (MapEntry)remove(key);
        try {
            Member backup = publishEntryInfo(key, value);
            entry.setBackupNode(backup);
        } catch (ChannelException x) {
            log.error("Unable to replicate out data for a LazyReplicatedMap.put operation", x);
        }
        super.put(key,entry);
        return old;
    }

    

    public void putAll(Map m) {
        Iterator i = m.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry entry = (Map.Entry)i.next();
            put(entry.getKey(),entry.getValue());
        }
    }

    public Object remove(Object key) {
        MapEntry entry = (MapEntry)super.remove(key);
        MapMessage msg = new MapMessage(mapContextName,MapMessage.MSG_REMOVE,false,(Serializable)key,null,null,null);
        try {
            channel.send(channel.getMembers(), msg);
        } catch ( ChannelException x ) {
            log.error("Unable to replicate out data for a LazyReplicatedMap.remove operation",x);
        }
        return entry!=null?entry.getValue():null;
    }

    public void clear() {
        //only delete active keys
        Iterator keys = keySet().iterator();
        while ( keys.hasNext() ) remove(keys.next());
    }

    public boolean containsValue(Object value) {
        if ( value == null ) {
            return super.containsValue(value);
        } else {
            Iterator i = super.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                MapEntry entry = (MapEntry) e.getValue();
                if (entry.isPrimary() && value.equals(entry.getValue())) return true;
            }//while
            return false;
        }//end if
    }

    public Object clone() {
        throw new UnsupportedOperationException("This operation is not valid on a replicated map");
    }
    
    /**
     * Returns the entire contents of the map
     * Map.Entry.getValue() will return a LazyReplicatedMap.MapEntry object containing all the information 
     * about the object.
     * @return Set
     */
    public Set entrySetFull() {
        return super.entrySet();
    }
    
    public Set keySetFull() {
        return super.keySet();
    }
    
    public Set entrySet() {
        LinkedHashSet set = new LinkedHashSet(super.size());
        Iterator i = super.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry e = (Map.Entry)i.next();
            MapEntry entry = (MapEntry)e.getValue();
            if ( entry.isPrimary() ) set.add(entry.getValue());
        }
        return set;
    }
    
    public Set keySet() {
        //todo implement
        //should only return keys where this is active.
        LinkedHashSet set = new LinkedHashSet(super.size());
        Iterator i = super.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry e = (Map.Entry)i.next();
            MapEntry entry = (MapEntry)e.getValue();
            if ( entry.isPrimary() ) set.add(entry.getKey());
        }
        return set;
    }
    
    public int size() {
        //todo, implement a counter variable instead
        //only count active members in this node
        int counter = 0;
        Iterator i = super.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry e = (Map.Entry)i.next();
            MapEntry entry = (MapEntry)e.getValue();
            if ( entry.isPrimary() ) counter++;
        }
        return counter;
    }
    
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return false;
    }
    
    public boolean isEmpty() {
        return size()==0;
    }
    
    public Collection values() {
        ArrayList values = new ArrayList(super.size());
        Iterator i = super.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry e = (Map.Entry)i.next();
            MapEntry entry = (MapEntry)e.getValue();
            if ( entry.isPrimary() ) values.add(entry.getValue());
        }
        return values;
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
        
        public boolean isPrimary() {
            return ((!proxy) && (!backup));
        }

        public void setProxy(boolean proxy) {
            this.proxy = proxy;
        }

        public boolean isDiffable() {
            return (value instanceof ReplicatedMapEntry);
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
                return ((ReplicatedMapEntry)value).getDiff();
            } else {
                return getData();
            }
        }
        
        public int hashCode() {
            return value.hashCode();
        }
        
        public boolean equals(Object o) {
            return value.equals(o);
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
                ((ReplicatedMapEntry)value).applyDiff(data,offset,length);
            } else if ( length == 0 ) {
                value = null;
                proxy = true;
            } else {
                value = XByteBuffer.deserialize(data,offset,length);
            }
        }
        
    }
//------------------------------------------------------------------------------    
//                map message to send to and from other maps    
//------------------------------------------------------------------------------
    
    public static class MapMessage implements Externalizable {
        public static final int MSG_BACKUP = 1;
        public static final int MSG_RETRIEVE_BACKUP = 2;
        public static final int MSG_PROXY = 3;
        public static final int MSG_REMOVE = 4;
        public static final int MSG_STATE = 5;
        
        private byte[] mapId;
        private int msgtype;
        private boolean diff;
        private Serializable key;
        private Serializable value;
        private byte[] diffvalue;
        private Member node;
        
        public MapMessage(byte[] mapId,
                          int msgtype, boolean diff, 
                          Serializable key,Serializable value,
                          byte[] diffvalue, Member node) {
            this.mapId = mapId;
            this.msgtype = msgtype;
            this.diff = diff;
            this.key = key;
            this.value = value;
            this.diffvalue = diffvalue;
            this.node = node;
        }
        
        public int getMsgType() {
            return msgtype;
        }
        
        public boolean isDiff() {
            return diff;
        }
        
        public Serializable getKey() {
            return key;
        }
        
        public Serializable getValue() {
            return value;
        }
        
        public byte[] getDiffValue() {
            return diffvalue;
        }
        
        public Member getBackupNode() {
            return node;
        }
        
        public byte[] getMapId() {
            return mapId;
        }
        
        public void setValue(Serializable value) {
            this.value = value;
        }
        
        public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
            mapId = new byte[in.readInt()];
            in.read(mapId);
            msgtype = in.readInt();
            switch (msgtype) {
                case MSG_BACKUP:
                case MSG_STATE: {
                    diff = in.readBoolean();
                    key = (Serializable)in.readObject();
                    if ( diff ) {
                        diffvalue = new byte[in.readInt()];
                        in.read(diffvalue);
                    } else {
                        value = (Serializable)in.readObject();
                    }//endif
                    break;
                }
                case MSG_RETRIEVE_BACKUP:
                case MSG_REMOVE : {
                    key = (Serializable)in.readObject();
                    break;
                }
                case MSG_PROXY: {
                    key = (Serializable)in.readObject();
                    byte[] d = new byte[in.readInt()];
                    in.read(d);
                    node = McastMember.getMember(d);
                    break;
                }
            }//switch
        }//readExternal

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mapId.length);
            out.write(mapId);
            out.writeInt(msgtype);
            switch (msgtype) {
                case MSG_BACKUP:
                case MSG_STATE: {
                    out.writeBoolean(diff);
                    out.writeObject(key);
                    if ( diff ) {
                        out.writeInt(diffvalue.length);
                        out.write(diffvalue);
                    } else {
                        out.writeObject(value);
                    }//endif
                    break;
                }
                case MSG_RETRIEVE_BACKUP:
                case MSG_REMOVE : {
                    out.writeObject(key);
                    break;
                }
                case MSG_PROXY: {
                    out.writeObject(key);
                    byte[] d = ((McastMember)node).getData(false);
                    out.writeInt(d.length);
                    out.write(d);
                    break;
                }
            }//switch
        }//writeExternal
        
        public Object clone() {
            return new MapMessage(this.mapId,this.msgtype,this.diff,this.key,this.value,this.diffvalue,this.node);
        }
    }//MapMessage

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