package org.apache.catalina.tribes.tcp.bio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.ClusterData;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.tcp.MultiPointSender;
import org.apache.catalina.tribes.tcp.AbstractSender;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MultipointBioSender extends AbstractSender implements MultiPointSender {
    public MultipointBioSender() {
    }
    
    protected long selectTimeout = 1000; 
    protected HashMap bioSenders = new HashMap();
    private boolean autoConnect;

    public synchronized void sendMessage(Member[] destination, ChannelMessage msg) throws ChannelException {
        long start = System.currentTimeMillis();
        byte[] data = XByteBuffer.createDataPackage((ClusterData)msg);
        BioSender[] senders = setupForSend(destination);
        ChannelException cx = null;
        for ( int i=0; i<senders.length; i++ ) {
            try {
                senders[i].sendMessage(data);
            } catch (Exception x) {
                if (cx == null) cx = new ChannelException(x);
                cx.addFaultyMember(destination[i]);
            }
        }
        if (cx!=null ) throw cx;
    }



    protected BioSender[] setupForSend(Member[] destination) throws ChannelException {
        ChannelException cx = null;
        BioSender[] result = new BioSender[destination.length];
        for ( int i=0; i<destination.length; i++ ) {
            try {
                BioSender sender = (BioSender) bioSenders.get(destination[i]);
                if (sender == null) {
                    sender = new BioSender(destination[i], getRxBufSize(), getTxBufSize());
                    sender.setKeepAliveCount(getKeepAliveCount());
                    sender.setKeepAliveTime(getKeepAliveTime());
                    sender.setTimeout(getTimeout());
                    sender.setMaxRetryAttempts(getMaxRetryAttempts());
                    sender.setKeepAliveTime(getKeepAliveTime());
                    bioSenders.put(destination[i], sender);
                }
                sender.setWaitForAck(getWaitForAck());
                result[i] = sender;
                if (!result[i].isConnected() ) result[i].connect();
                result[i].keepalive();
            }catch (Exception x ) {
                if ( cx== null ) cx = new ChannelException(x);
                cx.addFaultyMember(destination[i]);
            }
        }
        if ( cx!=null ) throw cx;
        else return result;
    }

    public void connect() throws IOException {
        //do nothing, we connect on demand
        setConnected(true);
    }


    private synchronized void close() throws ChannelException  {
        ChannelException x = null;
        Object[] members = bioSenders.keySet().toArray();
        for (int i=0; i<members.length; i++ ) {
            Member mbr = (Member)members[i];
            try {
                BioSender sender = (BioSender)bioSenders.get(mbr);
                sender.disconnect();
            }catch ( Exception e ) {
                if ( x == null ) x = new ChannelException(e);
                x.addFaultyMember(mbr);
            }
            bioSenders.remove(mbr);
        }
        if ( x != null ) throw x;
    }

    public void memberAdded(Member member) {

    }

    public void memberDisappeared(Member member) {
        //disconnect senders
        BioSender sender = (BioSender)bioSenders.remove(member);
        if ( sender != null ) sender.disconnect();
    }


    public synchronized void disconnect() {
        try {close(); }catch (Exception x){}
        setConnected(false);
    }

    public void finalize() {
        try {disconnect(); }catch ( Exception ignore){}
    }


    public boolean keepalive() {
        //throw new UnsupportedOperationException("Method ParallelBioSender.checkKeepAlive() not implemented");
        boolean result = false;
        Map.Entry[] entries = (Map.Entry[])bioSenders.entrySet().toArray(new Map.Entry[bioSenders.size()]);
        for ( int i=0; i<entries.length; i++ ) {
            BioSender sender = (BioSender)entries[i].getValue();
            if ( sender.keepalive() ) {
                bioSenders.remove(entries[i].getKey());
            }
        }
        return result;
    }

}