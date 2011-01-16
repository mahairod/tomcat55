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
package org.apache.catalina.tribes.test.membership;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.group.GroupChannel;

public class TestMemberArrival
    extends TestCase {
    private static int count = 10;
    private ManagedChannel[] channels = new ManagedChannel[count];
    private TestMbrListener[] listeners = new TestMbrListener[count];

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new GroupChannel();
            channels[i].getMembershipService().setPayload( ("Channel-" + (i + 1)).getBytes("ASCII"));
            listeners[i] = new TestMbrListener( ("Listener-" + (i + 1)));
            channels[i].addMembershipListener(listeners[i]);

        }
    }

    public void clear() {
        for (int i = 0; i < channels.length; i++) {
            listeners[i].members.clear();
        }
    }

    public void testMemberArrival() throws Exception {
        //purpose of this test is to make sure that we have received all the members
        //that we can expect before the start method returns
        Thread[] threads = new Thread[channels.length];
        for (int i=0; i<channels.length; i++ ) {
            final Channel channel = channels[i];
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        channel.start(Channel.DEFAULT);
                    }catch ( Exception x ) {
                        throw new RuntimeException(x);
                    }
                }
            };
            threads[i] = t;
        }
        for (int i=0; i<threads.length; i++ ) threads[i].start();
        for (int i=0; i<threads.length; i++ ) threads[i].join();
        Thread.sleep(2000);
        System.out.println("All channels started.");
        for (int i=listeners.length-1; i>=0; i-- ) assertEquals("Checking member arrival length",channels.length-1,listeners[i].members.size());
    }

    @Override
    protected void tearDown() throws Exception {

        for (int i = 0; i < channels.length; i++) {
            try {
                channels[i].stop(Channel.DEFAULT);
            } catch (Exception ignore) {
                // Ignore
            }
        }
        super.tearDown();
    }

    public static class TestMbrListener
        implements MembershipListener {
        public String name = null;
        public TestMbrListener(String name) {
            this.name = name;
        }

        public ArrayList<Member> members = new ArrayList<Member>();
        @Override
        public void memberAdded(Member member) {
            if (!members.contains(member)) {
                members.add(member);
                try {
                    System.out.println(name + ":member added[" + new String(member.getPayload(), "ASCII") + "; Thread:"+Thread.currentThread().getName()+"]");
                } catch (Exception x) {
                    System.out.println(name + ":member added[unknown]");
                }
            }
        }

        @Override
        public void memberDisappeared(Member member) {
            if (members.contains(member)) {
                members.remove(member);
                try {
                    System.out.println(name + ":member disappeared[" + new String(member.getPayload(), "ASCII") + "; Thread:"+Thread.currentThread().getName()+"]");
                } catch (Exception x) {
                    System.out.println(name + ":member disappeared[unknown]");
                }
            }
        }

    }

}
