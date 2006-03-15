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

import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.StringManager;
import org.apache.commons.modeler.Registry;
import org.apache.catalina.tribes.tcp.*;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast membership service.
 * This class is responsible for maintaining a list of active cluster nodes in the cluster.
 * If a node fails to send out a heartbeat, the node will be dismissed.
 *
 * @author Filip Hanik
 * @version $Revision: 378093 $, $Date: 2006-02-15 15:13:45 -0600 (Wed, 15 Feb 2006) $
 */


public class McastService implements MembershipService,MembershipListener {

    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( McastService.class );

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "McastService/2.1";

    /**
     * The implementation specific properties
     */
    protected Properties properties = new Properties();
    /**
     * A handle to the actual low level implementation
     */
    protected McastServiceImpl impl;
    /**
     * A membership listener delegate (should be the cluster :)
     */
    protected MembershipListener listener;
    /**
     * The local member
     */
    protected MemberImpl localMember ;
    private int mcastSoTimeout;
    private int mcastTTL;

    /**
     * Transmitter Mbean name
     */
    private ObjectName objectName;

    private Registry registry;

    /**
     * Create a membership service.
     */
    public McastService() {
        properties.setProperty("mcastClusterDomain", "catalina");
    }

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }
    
    /**
     * Transmitter ObjectName
     * 
     * @param name
     */
    public void setObjectName(ObjectName name) {
        objectName = name;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    /**
     *
     * @param properties
     * <BR/>All are required<BR />
     * 1. mcastPort - the port to listen to<BR>
     * 2. mcastAddress - the mcast group address<BR>
     * 3. mcastClusterDomain - the mcast cluster domain<BR>
     * 4. bindAddress - the bind address if any - only one that can be null<BR>
     * 5. memberDropTime - the time a member is gone before it is considered gone.<BR>
     * 6. msgFrequency - the frequency of sending messages<BR>
     * 7. tcpListenPort - the port this member listens to<BR>
     * 8. tcpListenHost - the bind address of this member<BR>
     * @exception java.lang.IllegalArgumentException if a property is missing.
     */
    public void setProperties(Properties properties) {
        hasProperty(properties,"mcastPort");
        hasProperty(properties,"mcastAddress");
        hasProperty(properties,"mcastClusterDomain");
        hasProperty(properties,"memberDropTime");
        hasProperty(properties,"msgFrequency");
        hasProperty(properties,"tcpListenPort");
        hasProperty(properties,"tcpListenHost");
        this.properties = properties;
    }

    /**
     * Return the properties, see setProperties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Return the local member name
     */
    public String getLocalMemberName() {
        return localMember.toString() ;
    }
 
    /**
     * Return the local member
     */
    public Member getLocalMember(boolean alive) {
        if ( alive ) localMember.setMemberAliveTime(System.currentTimeMillis()-impl.getServiceStartTime());
        return localMember;
    }
    
    /**
     * Sets the local member properties for broadcasting
     */
    public void setLocalMemberProperties(String listenHost, int listenPort) {
        properties.setProperty("tcpListenHost",listenHost);
        properties.setProperty("tcpListenPort",String.valueOf(listenPort));
    }
    
    public void setMcastAddr(String addr) {
        properties.setProperty("mcastAddress", addr);
    }

    public String getMcastAddr() {
        return properties.getProperty("mcastAddress");
    }

    public void setMcastBindAddress(String bindaddr) {
        properties.setProperty("mcastBindAddress", bindaddr);
    }

    public String getMcastBindAddress() {
        return properties.getProperty("mcastBindAddress");
    }

    public void setMcastClusterDomain(String clusterDomain) {
        properties.setProperty("mcastClusterDomain", clusterDomain);
    }

    public String getMcastClusterDomain() {
        return properties.getProperty("mcastClusterDomain");
    }

    public void setMcastPort(int port) {
        properties.setProperty("mcastPort", String.valueOf(port));
    }

    public int getMcastPort() {
        String p = properties.getProperty("mcastPort");
        return new Integer(p).intValue();
    }
    
    public void setMcastFrequency(long time) {
        properties.setProperty("msgFrequency", String.valueOf(time));
    }

    public long getMcastFrequency() {
        String p = properties.getProperty("msgFrequency");
        return new Long(p).longValue();
    }

    public void setMcastDropTime(long time) {
        properties.setProperty("memberDropTime", String.valueOf(time));
    }

    public long getMcastDropTime() {
        String p = properties.getProperty("memberDropTime");
        return new Long(p).longValue();
    }

    /**
     * Check if a required property is available.
     * @param properties The set of properties
     * @param name The property to check for
     */
    protected void hasProperty(Properties properties, String name){
        if ( properties.getProperty(name)==null) throw new IllegalArgumentException("Required property \""+name+"\" is missing.");
    }

    /**
     * Start broadcasting and listening to membership pings
     * @throws java.lang.Exception if a IO error occurs
     */
    public void start() throws java.lang.Exception {
        start(MembershipService.MBR_RX);
        start(MembershipService.MBR_TX);
    }
    
    public void start(int level) throws java.lang.Exception {
        if ( impl != null ) {
            impl.start(level);
            return;
        }
        String host = getProperties().getProperty("tcpListenHost");
        String domain = getProperties().getProperty("mcastClusterDomain");
        int port = Integer.parseInt(getProperties().getProperty("tcpListenPort"));
        
        if ( localMember == null ) {
            localMember = new MemberImpl(domain, host, port, 100);
        } else {
            localMember.setDomain(domain);
            localMember.setHostname(host);
            localMember.setPort(port);
            localMember.setMemberAliveTime(100);
        }
        localMember.setServiceStartTime(System.currentTimeMillis());
        java.net.InetAddress bind = null;
        if ( properties.getProperty("mcastBindAddress")!= null ) {
            bind = java.net.InetAddress.getByName(properties.getProperty("mcastBindAddress"));
        }
        int ttl = -1;
        int soTimeout = -1;
        if ( properties.getProperty("mcastTTL") != null ) {
            try {
                ttl = Integer.parseInt(properties.getProperty("mcastTTL"));
            } catch ( Exception x ) {
                log.error("Unable to parse mcastTTL="+properties.getProperty("mcastTTL"),x);
            }
        }
        if ( properties.getProperty("mcastSoTimeout") != null ) {
            try {
                soTimeout = Integer.parseInt(properties.getProperty("mcastSoTimeout"));
            } catch ( Exception x ) {
                log.error("Unable to parse mcastSoTimeout="+properties.getProperty("mcastSoTimeout"),x);
            }
        }

        impl = new McastServiceImpl((MemberImpl)localMember,Long.parseLong(properties.getProperty("msgFrequency")),
                                    Long.parseLong(properties.getProperty("memberDropTime")),
                                    Integer.parseInt(properties.getProperty("mcastPort")),
                                    bind,
                                    java.net.InetAddress.getByName(properties.getProperty("mcastAddress")),
                                    ttl,
                                    soTimeout,
                                    this);
        
        impl.start(level);
		long memberwait = (Long.parseLong(properties.getProperty("msgFrequency"))*4);
        if(log.isInfoEnabled())
            log.info("Sleeping for "+memberwait+" milliseconds to establish cluster membership");
        Thread.sleep(memberwait);

    }

 
    /**
     * Stop broadcasting and listening to membership pings
     */
    public void stop() {
        try  {
            if ( impl != null) impl.stop();
        } catch ( Exception x)  {
            log.error("Unable to stop the mcast service.",x);
        }
        impl = null;
    }

    /**
     * register mbean descriptor for package mcast 
     * @throws Exception
     */
    protected void initMBeans() throws Exception {
      if(registry == null) {
        registry = Registry.getRegistry(null, null);
        registry.loadMetadata(this.getClass().getResourceAsStream(
            "mbeans-descriptors.xml"));
      }
    }
    

    /**
     * Return all the members by name
     */
    public String[] getMembersByName() {
        Member[] currentMembers = getMembers();
        String [] membernames ;
        if(currentMembers != null) {
            membernames = new String[currentMembers.length];
            for (int i = 0; i < currentMembers.length; i++) {
                membernames[i] = currentMembers[i].toString() ;
            }
        } else
            membernames = new String[0] ;
        return membernames ;
    }
 
    /**
     * Return the member by name
     */
    public Member findMemberByName(String name) {
        Member[] currentMembers = getMembers();
        for (int i = 0; i < currentMembers.length; i++) {
            if (name.equals(currentMembers[i].toString()))
                return currentMembers[i];
        }
        return null;
    }

    /**
     * has members?
     */
    public boolean hasMembers() {
       if ( impl == null || impl.membership == null ) return false;
       return impl.membership.hasMembers();
    }
    
    public Member getMember(Member mbr) {
        if ( impl == null || impl.membership == null ) return null;
        return impl.membership.getMember(mbr);
    }

    /**
     * Return all the members
     */
    public Member[] getMembers() {
        if ( impl == null || impl.membership == null ) return null;
        return impl.membership.getMembers();
    }
    /**
     * Add a membership listener, this version only supports one listener per service,
     * so calling this method twice will result in only the second listener being active.
     * @param listener The listener
     */
    public void setMembershipListener(MembershipListener listener) {
        this.listener = listener;
    }
    /**
     * Remove the membership listener
     */
    public void removeMembershipListener(){
        listener = null;
    }

    public void memberAdded(Member member) {
        if ( listener!=null ) listener.memberAdded(member);
    }

    /**
     * Callback from the impl when a new member has been received
     * @param member The member
     */
    public void memberDisappeared(Member member)
    {
        if ( listener!=null ) listener.memberDisappeared(member);
    }

    public int getMcastSoTimeout() {
        return mcastSoTimeout;
    }
    public void setMcastSoTimeout(int mcastSoTimeout) {
        this.mcastSoTimeout = mcastSoTimeout;
        properties.setProperty("mcastSoTimeout", String.valueOf(mcastSoTimeout));
    }
    public int getMcastTTL() {
        return mcastTTL;
    }
    public void setMcastTTL(int mcastTTL) {
        this.mcastTTL = mcastTTL;
        properties.setProperty("mcastTTL", String.valueOf(mcastTTL));
    }

    /**
     * Simple test program
     * @param args Command-line arguments
     * @throws Exception If an error occurs
     */
    public static void main(String args[]) throws Exception {
		if(log.isInfoEnabled())
            log.info("Usage McastService hostname tcpport");
        McastService service = new McastService();
        java.util.Properties p = new java.util.Properties();
        p.setProperty("mcastPort","5555");
        p.setProperty("mcastAddress","224.10.10.10");
        p.setProperty("mcastClusterDomain","catalina");
        p.setProperty("bindAddress","localhost");
        p.setProperty("memberDropTime","3000");
        p.setProperty("msgFrequency","500");
        p.setProperty("tcpListenPort",args[1]);
        p.setProperty("tcpListenHost",args[0]);
        service.setProperties(p);
        service.start();
        Thread.sleep(60*1000*60);
    }
}
