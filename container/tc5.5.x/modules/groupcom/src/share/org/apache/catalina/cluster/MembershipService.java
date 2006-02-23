/*
 * Copyright 1999,2004 The Apache Software Foundation.
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

package org.apache.catalina.cluster;


/**
 * The membership service helps the cluster determine the membership
 * logic in the cluster.
 * @author Filip Hanik
 * @version $Revision: 378093 $, $Date: 2006-02-15 15:13:45 -0600 (Wed, 15 Feb 2006) $
 */


public interface MembershipService {
    
    public static final int MBR_RX = 1;
    public static final int MBR_TX = 2;
    
    /**
     * Sets the properties for the membership service. This must be called before
     * the <code>start()</code> method is called.
     * The properties are implementation specific.
     * @param properties - to be used to configure the membership service.
     */
    public void setProperties(java.util.Properties properties);
    /**
     * Returns the properties for the configuration used.
     */
    public java.util.Properties getProperties();
    /**
     * Starts the membership service. If a membership listeners is added
     * the listener will start to receive membership events.
     * Performs a start level 1 and 2
     * @throws java.lang.Exception if the service fails to start.
     */
    public void start() throws java.lang.Exception;

    /**
     * Starts the membership service. If a membership listeners is added
     * the listener will start to receive membership events.
     * @param level - level 1 starts listening for members, level 2 
     * starts broad casting the server
     * @throws java.lang.Exception if the service fails to start.
     */
    public void start(int level) throws java.lang.Exception;


    /**
     * Stops the membership service
     */
    public void stop();
    
    /**
     * Returns that cluster has members.
     */
    public boolean hasMembers();
    
    /**
     * Returns a list of all the members in the cluster.
     */
    
    public Member[] getMembers();
    
    /**
     * Returns the member object that defines this member
     */
    public Member getLocalMember();

    /**
     * Return all members by name
     */
    public String[] getMembersByName() ; 
    
    /**
     * Return the member by name
     */
    public Member findMemberByName(String name) ;

    /**
     * Sets the local member properties for broadcasting
     */
    public void setLocalMemberProperties(String listenHost, int listenPort);
    
    /**
     * Sets the membership listener, only one listener can be added.
     * If you call this method twice, the last listener will be used.
     * @param listener The listener
     */
    public void setMembershipListener(MembershipListener listener);
    
    /**
     * removes the membership listener.
     */
    public void removeMembershipListener();

}
