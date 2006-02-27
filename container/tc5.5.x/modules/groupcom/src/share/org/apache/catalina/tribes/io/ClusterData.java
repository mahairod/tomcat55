/*
 * Copyright 1999,2005 The Apache Software Foundation.
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
package org.apache.catalina.tribes.io;

import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import org.apache.catalina.tribes.mcast.McastMember;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * The cluster data class is used to transport around the byte array from
 * a ClusterMessage object. This is just a utility class to avoid having to 
 * serialize and deserialize the ClusterMessage more than once. 
 * @author Peter Rossbach
 * @author Filip Hanik
 * @version $Revision: 377484 $ $Date: 2006-02-13 15:00:05 -0600 (Mon, 13 Feb 2006) $
 * @since 5.5.10
 */
public class ClusterData implements ChannelMessage {

    private int options = 0 ;
    private byte[] message ;
    private long timestamp ;
    private byte[] uniqueId ;
    private Member address;

    public ClusterData() {}
    
    /**
     * @param type message type (class)
     * @param uniqueId unique message id
     * @param message message data
     * @param timestamp message creation date
     */
    public ClusterData(byte[] uniqueId, byte[] message, long timestamp) {
        this.uniqueId = uniqueId;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    /**
     * @return Returns the message.
     */
    public byte[] getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }
    /**
     * @return Returns the timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    /**
     * @return Returns the uniqueId.
     */
    public byte[] getUniqueId() {
        return uniqueId;
    }
    /**
     * @param uniqueId The uniqueId to set.
     */
    public void setUniqueId(byte[] uniqueId) {
        this.uniqueId = uniqueId;
    }
    /**
     * @return Returns the compress.
     */
    public int getOptions() {

        return options;
    }
    /**
     * @param compress The compress to set.
     */
    public void setOptions(int options) {

        this.options = options;
    }
    
    public Member getAddress() {

        return address;
    }


    public void setAddress(Member address) {

        this.address = address;
    }
    
    
    /**
     * 
    private int options = 0 ;
    private long timestamp ;
    private String uniqueId ;
    private Member address;
    private byte[] message ;

     * @return byte[]
     */
    public byte[] getDataPackage() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(getMessage().length*2);
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeInt(options);
        out.writeLong(timestamp);
        out.writeInt(uniqueId.length);
        out.write(uniqueId);
        byte[] addr = ((McastMember)address).getData();
        out.writeInt(addr.length);
        out.write(addr);
        out.writeInt(message.length);
        out.write(message);
        out.flush();
        return bout.toByteArray();
    }
    
    public static ClusterData getDataFromPackage(byte[] dataPackage) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(dataPackage);
        ObjectInputStream in = new ObjectInputStream(bin);
        ClusterData data = new ClusterData();
        data.setOptions(in.readInt());
        data.setTimestamp(in.readLong());
        byte[] uniqueId = new byte[in.readInt()];
        in.read(uniqueId);
        data.setUniqueId(uniqueId);
        byte[] addr = new byte[in.readInt()];
        in.read(addr);
        data.setAddress(McastMember.getMember(addr));
        byte[] message = new byte[in.readInt()];
        in.read(message);
        data.setMessage(message);
        return data;
    }
    
}
