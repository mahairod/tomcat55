package org.apache.catalina.tribes.tcp.bio;

import org.apache.catalina.tribes.tcp.DataSender;
import org.apache.catalina.tribes.tcp.PooledSender;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.tcp.MultiPointSender;
import org.apache.catalina.tribes.ChannelMessage;

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
public class PooledMultiSender extends PooledSender {
    
    protected long timeout = 15000;
    protected boolean waitForAck = false;
    protected int retryAttempts=0;
    protected int keepAliveCount = Integer.MAX_VALUE;
    protected boolean directBuf = false;
    protected int rxBufSize = 43800;
    protected int txBufSize = 25188;
    protected boolean suspect = false;
    private boolean autoConnect;
    private boolean useDirectBuffer;

    public PooledMultiSender() {
    }
    
    public void sendMessage(Member[] destination, ChannelMessage msg) throws ChannelException {
        MultiPointSender sender = null;
        try {
            sender = (MultiPointSender)getSender();
            if (sender == null) {
                ChannelException cx = new ChannelException("Unable to retrieve a data sender, time out error.");
                for (int i = 0; i < destination.length; i++) cx.addFaultyMember(destination[i]);
                throw cx;
            } else {
                sender.sendMessage(destination, msg);
            }
            sender.keepalive();
        }finally {
            if ( sender != null ) returnSender(sender);
        }
    }

    /**
     * getNewDataSender
     *
     * @return DataSender
     * @todo Implement this org.apache.catalina.tribes.tcp.PooledSender
     *   method
     */
    public DataSender getNewDataSender() {
        MultipointBioSender sender = new MultipointBioSender();
        sender.setAutoConnect(autoConnect);
        sender.setTimeout(timeout);
        sender.setWaitForAck(waitForAck);
        sender.setMaxRetryAttempts(retryAttempts);
        sender.setRxBufSize(rxBufSize);
        sender.setTxBufSize(txBufSize);
        return sender;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public void setDirectBuf(boolean directBuf) {
        this.directBuf = directBuf;
    }

    public void setKeepAliveCount(int keepAliveCount) {
        this.keepAliveCount = keepAliveCount;
    }

    public void setMaxRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public void setSuspect(boolean suspect) {
        this.suspect = suspect;
    }

    public void setUseDirectBuffer(boolean useDirectBuffer) {
        this.useDirectBuffer = useDirectBuffer;
    }

    public boolean getSuspect() {
        return suspect;
    }

    public boolean isUseDirectBuffer() {
        return useDirectBuffer;
    }

    public void memberAdded(Member member) {

    }

    public void memberDisappeared(Member member) {
        //disconnect senders
    } 

}