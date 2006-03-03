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

package org.apache.catalina.tribes.mcast;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.util.Arrays;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast member.
 * Carries the host, and port of the this or other cluster nodes.
 *
 * @author Filip Hanik
 * @author Peter Rossbach
 * @version $Revision: 304032 $, $Date: 2005-07-27 10:11:55 -0500 (Wed, 27 Jul 2005) $
 */
public class McastMember implements Member, java.io.Externalizable {

    /**
     * Digits, used for "superfast" de-serialization of an
     * IP address
     */
    final transient static char[] digits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9'};

    /**
     * Public properties specific to this implementation
     */
    public static final transient String TCP_LISTEN_PORT = "tcpListenPort";
    public static final transient String TCP_LISTEN_HOST = "tcpListenHost";
    public static final transient String MEMBER_NAME = "memberName";
    public static final transient String MEMBER_DOMAIN = "memberDomain";
    
    /**
     * The listen host for this member
     */
    protected byte[] host;
    protected transient String hostname;
    /**
     * The tcp listen port for this member
     */
    protected int port;

    /**
     * The name of the cluster domain from this node
     */
    protected byte[] domain;
    protected transient String domainname;
    
    /**
     * Counter for how many messages have been sent from this member
     */
    protected int msgCount = 0;
    /**
     * The number of milliseconds since this members was
     * created, is kept track of using the start time
     */
    protected long memberAliveTime = 0;
    
    /**
     * For the local member only
     */
    protected transient long serviceStartTime;
    
    protected transient byte[] dataPkg = null;

    /**
     * Empty constructor for serialization
     */
    public McastMember() {
        
    }

    /**
     * Construct a new member object
     * @param name - the name of this member, cluster unique
     * @param domain - the cluster domain name of this member
     * @param host - the tcp listen host
     * @param port - the tcp listen port
     */
    public McastMember(String domain,
                       String host,
                       int port,
                       long aliveTime) throws IOException {
        setHostname(host);
        this.port = port;
        this.domain = domain.getBytes();
        this.memberAliveTime=aliveTime;
    }

    /**
     *
     * @return a Hashmap containing the following properties:<BR>
     * 1. tcpListenPort - the port this member listens to for messages - string<BR>
     * 2. tcpListenHost - the host address of this member - string<BR>
     * 3. memberName    - the name of this member - string<BR>
     */
    public java.util.HashMap getMemberProperties() {
        java.util.HashMap map = new java.util.HashMap(2);
        map.put(McastMember.TCP_LISTEN_HOST,this.host);
        map.put(McastMember.TCP_LISTEN_PORT,String.valueOf(this.port));
        map.put(McastMember.MEMBER_NAME,getName());
        map.put(McastMember.MEMBER_DOMAIN,domain);
        return map;
    }

    /**
     * Increment the message count.
     */
    protected void inc() {
        msgCount++;
    }

    /**
     * Create a data package to send over the wire representing this member.
     * This is faster than serialization.
     * @return - the bytes for this member deserialized
     * @throws Exception
     */
    public byte[] getData()  {
        return getData(true);
    }
    public byte[] getData(boolean getalive)  {
        //look in cache first
        if ( dataPkg!=null ) {
            if ( getalive ) {
                //you'd be surprised, but System.currentTimeMillis
                //shows up on the profiler
                long alive=System.currentTimeMillis()-getServiceStartTime();
                XByteBuffer.toBytes( (long) alive, dataPkg, 0);
            }
            return dataPkg;
        }
        
        //package looks like
        //alive - 8 bytes
        //port - 4 bytes
        //host - 4 bytes
        //dlen - 4 bytes
        //domain - dlen bytes
        
        byte[] domaind = this.domain;
        byte[] addr = host;
        byte[] data = new byte[8+4+addr.length+4+domaind.length];
        long alive=System.currentTimeMillis()-getServiceStartTime();
        
        
        //reduce byte copying
        //System.arraycopy(XByteBuffer.toBytes((long)alive),0,data,0,8);
        XByteBuffer.toBytes((long)alive,data,0);

        //reduce byte copying
        //System.arraycopy(XByteBuffer.toBytes(port),0,data,8,4);
        XByteBuffer.toBytes(port,data,8);
        
        System.arraycopy(addr,0,data,12,addr.length);

        //reduce byte copying
        //System.arraycopy(XByteBuffer.toBytes(domaind.length),0,data,16,4);
        XByteBuffer.toBytes(domaind.length,data,16);
        
        System.arraycopy(domaind,0,data,20,domaind.length);
        dataPkg = data;
        return data;
    }
    /**
     * Deserializes a member from data sent over the wire
     * @param data - the bytes received
     * @return a member object.
     */
    public static McastMember getMember(byte[] data, McastMember member) {
       //package looks like
       //alive - 8 bytes
       //port - 4 bytes
       //host - 4 bytes
       //dlen - 4 bytes
       //domain - dlen bytes
       byte[] alived = new byte[8];
       System.arraycopy(data, 0, alived, 0, 8);
       byte[] portd = new byte[4];
       System.arraycopy(data, 8, portd, 0, 4);
       byte[] addr = new byte[4];
       System.arraycopy(data, 12, addr, 0, 4);
       //FIXME control the nlen
       //FIXME control the dlen
       byte[] dlend = new byte[4];
       System.arraycopy(data, 16, dlend, 0, 4);
       int dlen = XByteBuffer.toInt(dlend, 0);
       byte[] domaind = new byte[dlen];
       System.arraycopy(data, 20, domaind, 0, domaind.length);
       member.setDomain(new String(domaind));
       member.setHost(addr);
       member.setPort(XByteBuffer.toInt(portd, 0));
       member.setMemberAliveTime(XByteBuffer.toLong(alived, 0));
       return member;
    }

    public static McastMember getMember(byte[] data) {
       return getMember(data,new McastMember());
    }

    /**
     * Return the name of this object
     * @return a unique name to the cluster
     */
    public String getName() {
        return "tcp://"+getHostname()+":"+getPort();
    }
    
    /**
     * Return the domain of this object
     * @return a cluster domain to the cluster
     */
    public String getDomain() {
        if ( this.domainname == null ) this.domainname = new String(domain);
        return this.domainname;
    }
    
    /**
     * Return the listen port of this member
     * @return - tcp listen port
     */
    public int getPort()  {
        return this.port;
    }

    /**
     * Return the TCP listen host for this member
     * @return IP address or host name
     */
    public byte[] getHost()  {
        return host;
    }
    
    public String getHostname() {
        if ( this.hostname != null ) return hostname;
        else {
            try {
                this.hostname = java.net.InetAddress.getByAddress(host).getHostName();
                return this.hostname;
            }catch ( IOException x ) {
                throw new RuntimeException("Unable to parse hostname.",x);
            }
        }
    }

    /**
     * Contains information on how long this member has been online.
     * The result is the number of milli seconds this member has been
     * broadcasting its membership to the cluster.
     * @return nr of milliseconds since this member started.
     */
    public long getMemberAliveTime() {
       return memberAliveTime;
    }

    public long getServiceStartTime() {
        return serviceStartTime;
    }

    public void setMemberAliveTime(long time) {
       memberAliveTime=time;
    }



    /**
     * String representation of this object
     */
    public String toString()  {
        return "org.apache.catalina.tribes.mcast.McastMember["+getName()+","+getDomain()+","+getHostname()+","+port+", alive="+memberAliveTime+"]";
    }

    /**
     * @see java.lang.Object#hashCode()
     * @return The hash code
     */
    public int hashCode() {
        return getHost()[0]+getHost()[1]+getHost()[2]+getHost()[3];
    }

    /**
     * Returns true if the param o is a McastMember with the same name
     * @param o
     */
    public boolean equals(Object o) {
        if ( o instanceof McastMember )    {
            return Arrays.equals(this.getHost(),((McastMember)o).getHost()) &&
                   this.getPort() == ((McastMember)o).getPort();
        }
        else
            return false;
    }
    

    /**
     * Converts for bytes (ip address) to a string representation of it<BR>
     * Highly optimized method.
     * @param address (4 bytes ip address)
     * @return string representation of that ip address
     */
    private static final String addressToString(byte[] address) {
        int q, r = 0;
        int charPos = 15;
        char[] buf = new char[15];
        char dot = '.';

        int i = address[3] & 0xFF;
        for (; ; )
        {
            q = (i * 52429) >>> (19);
            r = i - ( (q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0)
                break;
        }
        buf[--charPos] = dot;
        i = address[2] & 0xFF;
        for (; ; )
        {
            q = (i * 52429) >>> (19);
            r = i - ( (q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0)
                break;
        }
        buf[--charPos] = dot;

        i = address[1] & 0xFF;
        for (; ; )
        {
            q = (i * 52429) >>> (19);
            r = i - ( (q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0)
                break;
        }

        buf[--charPos] = dot;
        i = address[0] & 0xFF;

        for (; ; )
        {
            q = (i * 52429) >>> (19);
            r = i - ( (q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0)
                break;
        }
        return new String(buf, charPos, 15 - charPos);
    }
    public void setHost(byte[] host) {
        this.host = host;
    }
    
    public void setHostname(String host) throws IOException {
        hostname = host;
        this.host = java.net.InetAddress.getByName(host).getAddress();
    }
    
    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public void setDomain(String domain) {
        this.domain = domain.getBytes();
        this.dataPkg = null;
    }
    public void setPort(int port) {
        this.port = port;
        this.dataPkg = null;
    }

    public void setServiceStartTime(long serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int length = in.readInt();
        byte[] message = new byte[length];
        in.read(message);
        getMember(message,this);
        
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] data = this.getData();
        out.writeInt(data.length);
        out.write(data);
    }

}
