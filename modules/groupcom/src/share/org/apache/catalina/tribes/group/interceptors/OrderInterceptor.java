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
 */

package org.apache.catalina.tribes.group.interceptors;

import java.util.HashMap;

import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.InterceptorPayload;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.io.XByteBuffer;



/**
 *
 * 
 * @author Filip Hanik
 * @version 1.0
 */
public class OrderInterceptor extends ChannelInterceptorBase {
    private HashMap outcounter = new HashMap();
    private HashMap incounter = new HashMap();
    private HashMap incoming = new HashMap();
    private long expire = 3000;
    private boolean forwardExpired = true;

    public void sendMessage(Member[] destination, ChannelMessage msg, InterceptorPayload payload) throws ChannelException {
        for ( int i=0; i<destination.length; i++ ) {
            ChannelMessage tmp = msg.clone();
            int nr = incCounter(destination[i]);
            tmp.getMessage().append(XByteBuffer.toBytes(nr),0,4);
            getNext().sendMessage(new Member[] {destination[i]}, tmp, payload);
        }
    }

    public void messageReceived(ChannelMessage msg) {
        int msgnr = XByteBuffer.toInt(msg.getMessage().getBytesDirect(),msg.getMessage().getLength()-4);
        msg.getMessage().trim(4);
        MessageOrder order = new MessageOrder(msgnr,msg);
        if ( processIncoming(order) ) processLeftOvers(msg.getAddress(),false);
        //getPrevious().messageReceived(msg);
    }
    
    public synchronized void processLeftOvers(Member member, boolean force) {
        MessageOrder tmp = (MessageOrder)incoming.get(member);
        if ( force ) {
            Counter cnt = getInCounter(member);
            cnt.setCounter(Integer.MAX_VALUE);
        }
        if ( tmp!= null ) processIncoming(tmp);
    }
    /**
     * 
     * @param order MessageOrder
     * @return boolean - true if a message expired and was processed
     */
    public synchronized boolean processIncoming(MessageOrder order) {
        boolean result = false;
        Member member = order.getMessage().getAddress();
        Counter cnt = getInCounter(member);
        
        MessageOrder tmp = (MessageOrder)incoming.get(member);
        if ( tmp != null ) {
            order = MessageOrder.add(tmp,order);
        }
        
//        if ( order.getMsgNr() != cnt.getCounter() ) {
//            System.out.println("Found out of order message.");
//        }
        
        while ( (order!=null) && (order.getMsgNr() <= cnt.getCounter()) ) {
            //we are right on target. process orders
            if ( order.getMsgNr() == cnt.getCounter() ) cnt.inc();
            super.messageReceived(order.getMessage());
            order.setMessage(null);
            order = order.next;
        }
        MessageOrder head = order;
        MessageOrder prev = null;
        tmp = order;
        while ( tmp != null ) {
            //process expired messages
            //TODO, when a message expires, what do we do?
            //just send one?
            if ( tmp.isExpired(expire) ) {
                //reset the head
                if ( tmp == head ) head = tmp.next;
                cnt.setCounter(tmp.getMsgNr()+1);
                if ( getForwardExpired() ) super.messageReceived(tmp.getMessage());
                tmp.setMessage(null);
                tmp = tmp.next;
                if ( prev != null ) prev.next = tmp;  
                result = true;
            } else {
                prev = tmp;
                tmp = tmp.next;
            }
        }
        if ( head == null ) incoming.remove(member);
        else incoming.put(member, head);
        return result;
    }
    
    public void memberAdded(Member member) {
        //notify upwards
        getInCounter(member);
        getOutCounter(member);
        super.memberAdded(member);
    }

    public void memberDisappeared(Member member) {
        //notify upwards
        outcounter.remove(member);
        incounter.remove(member);
        //clear the remaining queue
        processLeftOvers(member,true);
        super.memberDisappeared(member);
    }
    
    public int incCounter(Member mbr) { 
        Counter cnt = getOutCounter(mbr);
        return cnt.inc();
    }
    
    public synchronized Counter getInCounter(Member mbr) {
        Counter cnt = (Counter)incounter.get(mbr);
        if ( cnt == null ) {
            cnt = new Counter();
            cnt.inc(); //always start at 1 for incoming
            incounter.put(mbr,cnt);
        }
        return cnt;
    }

    public synchronized Counter getOutCounter(Member mbr) {
        Counter cnt = (Counter)outcounter.get(mbr);
        if ( cnt == null ) {
            cnt = new Counter();
            outcounter.put(mbr,cnt);
        }
        return cnt;
    }

    public static class Counter {
        private int value = 0;
        
        public int getCounter() {
            return value;
        }
        
        public synchronized void setCounter(int counter) {
            this.value = counter;
        }
        
        public synchronized int inc() {
            return ++value;
        }
    }
    
    public static class MessageOrder {
        private long received = System.currentTimeMillis();
        private MessageOrder next;
        private int msgNr;
        private ChannelMessage msg = null;
        public MessageOrder(int msgNr,ChannelMessage msg) {
            this.msgNr = msgNr;
            this.msg = msg;
        }
        
        public boolean isExpired(long expireTime) {
            return (System.currentTimeMillis()-received) > expireTime;
        }
        
        public ChannelMessage getMessage() {
            return msg;
        }
        
        public void setMessage(ChannelMessage msg) {
            this.msg = msg;
        }
        
        public void setNext(MessageOrder order) {
            this.next = order;
        }
        public MessageOrder getNext() {
            return next;
        }
        
        public int count() {
            int counter = 1;
            MessageOrder tmp = next;
            while ( tmp != null ) {
                counter++;
                tmp = tmp.next;
            }
            return counter;
        }
        
        public static MessageOrder add(MessageOrder head, MessageOrder add) {
            if ( head == null ) return add;
            if ( add == null ) return head;
            if ( head == add ) return add;

            if ( head.getMsgNr() > add.getMsgNr() ) {
                add.next = head;
                return add;
            }
            
            MessageOrder iter = head;
            MessageOrder prev = null;
            while ( iter.getMsgNr() < add.getMsgNr() && (iter.next !=null ) ) {
                prev = iter;
                iter = iter.next;
            }
            if ( iter.getMsgNr() < add.getMsgNr() ) {
                //add after
                add.next = iter.next;
                iter.next = add;
            } else if (iter.getMsgNr() > add.getMsgNr()) {
                //add before
                prev.next = add;
                add.next = iter;
                
            } else {
                throw new ArithmeticException("Message added has the same counter, synchronization bug. Disable the order interceptor");
            }
            
            return head;
        }
        
        public int getMsgNr() {
            return msgNr;
        }
        
        
        
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setForwardExpired(boolean forwardExpired) {
        this.forwardExpired = forwardExpired;
    }

    public long getExpire() {
        return expire;
    }

    public boolean getForwardExpired() {
        return forwardExpired;
    }

}
