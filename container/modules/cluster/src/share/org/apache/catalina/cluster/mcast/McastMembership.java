/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.cluster.mcast;


import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast membership.
 * This class is responsible for maintaining a list of active cluster nodes in the cluster.
 * If a node fails to send out a heartbeat, the node will be dismissed.
 *
 * @author Filip Hanik
 * @author Peter Rossbach
 * @version $Revision$, $Date$
 */
public class McastMembership
{
    protected static final McastMember[] EMPTY_MEMBERS = new McastMember[0];
    
    private final Object membersLock = new Object();

    /**
     * The name of this membership, has to be the same as the name for the local
     * member
     */
    protected String name;
    
    /**
     * A map of all the members in the cluster.
     */
    protected Map map = new HashMap();
    
    /**
     * A list of all the members in the cluster.
     */
    protected McastMember[] members = EMPTY_MEMBERS;
    
    /**
      * sort members by alive time
      */
    protected MemberComparator memberComparator = new MemberComparator();

    /**
     * Constructs a new membership
     * @param name - has to be the name of the local member. Used to filter the local member from the cluster membership
     */
    public McastMembership(String name) {
        this.name = name;
    }

    /**
     * Reset the membership and start over fresh.
     * Ie, delete all the members and wait for them to ping again and join this membership
     */
    public synchronized void reset() {
        map.clear();
        members = EMPTY_MEMBERS ;
    }

    /**
     * Notify the membership that this member has announced itself.
     *
     * @param member - the member that just pinged us
     * @return - true if this member is new to the cluster, false otherwise.
     * @return - false if this member is the local member or updated.
     */
    public synchronized boolean memberAlive(McastMember member) {
        boolean result = false;
        //ignore ourselves
        if ( member.getName().equals(name) ) return result;

        //return true if the membership has changed
        MbrEntry entry = (MbrEntry)map.get(member.getName());
        if ( entry == null ) {
            entry = new MbrEntry(member);
            map.put(member.getName(),entry);
            addMcastMember(member);
            result = true;
       } else {
            //update the member alive time
            McastMember updateMember = entry.getMember() ;
            if(updateMember.getMemberAliveTime() != member.getMemberAliveTime()) {
                updateMember.setMemberAliveTime(member.getMemberAliveTime());
                Arrays.sort(members, memberComparator);
            }
        }
        entry.accessed();
 
        return result;
    }

    /**
     * Add a member to this component and sort array with memberComparator
     * @param member The member to add
     */
    protected void addMcastMember(McastMember member) {
      synchronized (membersLock) {
          McastMember results[] =
            new McastMember[members.length + 1];
          for (int i = 0; i < members.length; i++)
              results[i] = members[i];
          results[members.length] = member;
          members = results;
          Arrays.sort(members, memberComparator);
      }
    }
    
    /**
     * Remove a member from this component.
     * 
     * @param member The member to remove
     */
    protected void removeMcastMember(McastMember member) {
        synchronized (membersLock) {
            int n = -1;
            for (int i = 0; i < members.length; i++) {
                if (members[i] == member) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;
            McastMember results[] =
              new McastMember[members.length - 1];
            int j = 0;
            for (int i = 0; i < members.length; i++) {
                if (i != n)
                    results[j++] = members[i];
            }
            members = results;
        }
    }

    /**
     * Runs a refresh cycle and returns a list of members that has expired.
     * This also removes the members from the membership, in such a way that
     * getMembers() = getMembers() - expire()
     * @param maxtime - the max time a member can remain unannounced before it is considered dead.
     * @return the list of expired members
     */
    public synchronized McastMember[] expire(long maxtime) {
        if(!hasMembers() )
           return EMPTY_MEMBERS;
       
        ArrayList list = null;
        Iterator i = map.values().iterator();
        while(i.hasNext()) {
            MbrEntry entry = (MbrEntry)i.next();
            if( entry.hasExpired(maxtime) ) {
                if(list == null) // only need a list when members are expired (smaller gc)
                    list = new java.util.ArrayList();
                list.add(entry.getMember());
            }
        }
        
        if(list != null) {
            McastMember[] result = new McastMember[list.size()];
            list.toArray(result);
            for( int j=0; j<result.length; j++) {
                map.remove(result[j].getName());
                removeMcastMember(result[j]);
            }
            return result;
        } else {
            return EMPTY_MEMBERS ;
        }
    }

    /**
     * Returning that service has members or not
     */
    public synchronized boolean hasMembers() {
        return members.length > 0 ;
    }
 
    /**
     * Returning a list of all the members in the membership
     * We not need a copy: add and remove generate new arrays.
     */
    public synchronized McastMember[] getMembers() {
        if(hasMembers()) {
            return members;
        } else {
            return EMPTY_MEMBERS;
        }
    }

    /**
     * get a copy from all member entries
     */
    protected synchronized MbrEntry[] getMemberEntries()
    {
        MbrEntry[] result = new MbrEntry[map.size()];
        java.util.Iterator i = map.entrySet().iterator();
        int pos = 0;
        while ( i.hasNext() )
            result[pos++] = ((MbrEntry)((java.util.Map.Entry)i.next()).getValue());
        return result;
    }
    
    // --------------------------------------------- Inner Class

    private class MemberComparator implements java.util.Comparator {

        public int compare(Object o1, Object o2) {
            try {
                return compare((McastMember) o1, (McastMember) o2);
            } catch (ClassCastException x) {
                return 0;
            }
        }

        public int compare(McastMember m1, McastMember m2) {
            //longer alive time, means sort first
            long result = m2.getMemberAliveTime() - m1.getMemberAliveTime();
            if (result < 0)
                return -1;
            else if (result == 0)
                return 0;
            else
                return 1;
        }
    }
    
    /**
     * Inner class that represents a member entry
     */
    protected static class MbrEntry
    {

        protected McastMember mbr;
        protected long lastHeardFrom;

        public MbrEntry(McastMember mbr) {
           this.mbr = mbr;
        }

        /**
         * Indicate that this member has been accessed.
         */
        public void accessed(){
           lastHeardFrom = System.currentTimeMillis();
        }

        /**
         * Return the actual McastMember object
         */
        public McastMember getMember() {
            return mbr;
        }

        /**
         * Check if this dude has expired
         * @param maxtime The time threshold
         */
        public boolean hasExpired(long maxtime) {
            long delta = System.currentTimeMillis() - lastHeardFrom;
            return delta > maxtime;
        }
    }
}
