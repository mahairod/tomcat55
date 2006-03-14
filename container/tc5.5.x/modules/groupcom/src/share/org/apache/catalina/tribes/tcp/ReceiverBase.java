/*
 * Copyright 1999,2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.catalina.tribes.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.MessageListener;
import org.apache.catalina.tribes.io.ListenCallback;


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
public abstract class ReceiverBase implements ChannelReceiver, ListenCallback {

    public static final int OPTION_SEND_ACK = 0x0001;
    public static final int OPTION_SYNCHRONIZED = 0x0002;
    public static final int OPTION_DIRECT_BUFFER = 0x0004;


    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ReceiverBase.class);
    
    protected MessageListener listener;
    protected String host;
    protected InetAddress bind;
    protected int port;
    protected boolean sendack;
    protected boolean sync;
    protected int rxBufSize = 43800;
    protected int txBufSize = 25188;
    protected int tcpThreadCount;
    protected boolean doListen = false;
    protected ThreadPool pool;
    protected boolean direct = true;
    protected long tcpSelectorTimeout;

    public ReceiverBase() {
    }
    
    /**
     * getMessageListener
     *
     * @return MessageListener
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public MessageListener getMessageListener() {
        return listener;
    }

    /**
     *
     * @return The port
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public int getPort() {
        return port;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }

    public int getTxBufSize() {
        return txBufSize;
    }

    public int getTcpThreadCount() {
        return tcpThreadCount;
    }

    /**
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public boolean getSendAck() {
        return sendack;
    }

    /**
     * setMessageListener
     *
     * @param listener MessageListener
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }


    /**
     *
     * @param isSendAck boolean
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public void setSendAck(boolean sendAck) {
        this.sendack = sendAck;
    }
    public void setTcpListenPort(int tcpListenPort) {
        this.port = tcpListenPort;
    }

    public void setTcpListenAddress(String tcpListenHost) {
        this.host = tcpListenHost;
    }

    public void setSynchronized(boolean sync) {
        this.sync = sync;
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public void setTxBufSize(int txBufSize) {
        this.txBufSize = txBufSize;
    }

    public void setTcpThreadCount(int tcpThreadCount) {
        this.tcpThreadCount = tcpThreadCount;
    }

    /**
     * @return Returns the bind.
     */
    public InetAddress getBind() {
        if (bind == null) {
            try {
                if ("auto".equals(host)) {
                    host = java.net.InetAddress.getLocalHost().getHostAddress();
                }
                if (log.isDebugEnabled())
                    log.debug("Starting replication listener on address:"+ host);
                bind = java.net.InetAddress.getByName(host);
            } catch (IOException ioe) {
                log.error("Failed bind replication listener on address:"+ host, ioe);
            }
        }
        return bind;
    }
    
    /**
     * recursive bind to find the next available port
     * @param socket ServerSocket
     * @param portstart int
     * @param retries int
     * @return int
     * @throws IOException
     */
    protected int bind(ServerSocket socket, int portstart, int retries) throws IOException {
        while ( retries > 0 ) {
            try {
                InetSocketAddress addr = new InetSocketAddress(getBind(), portstart);
                socket.bind(addr);
                setTcpListenPort(portstart);
                log.info("Receiver Server Socket bound to:"+addr);
                return 0;
            }catch ( IOException x) {
                retries--;
                if ( retries <= 0 ) throw x;
                portstart++;
                retries = bind(socket,portstart,retries);
            }
        }
        return retries;
    }
    
    public void messageDataReceived(ChannelMessage data) {
        if ( this.listener != null ) {
            listener.messageReceived(data);
        }
    }
    
    public int getWorkerThreadOptions() {
        int options = 0;
        if ( getSynchronized() ) options = options |OPTION_SYNCHRONIZED;
        if ( getSendAck() ) options = options |OPTION_SEND_ACK;
        if ( getDirect() ) options = options | OPTION_DIRECT_BUFFER;
        return options;
    }


    /**
     * @param bind The bind to set.
     */
    public void setBind(java.net.InetAddress bind) {
        this.bind = bind;
    }


    public int getTcpListenPort() {
        return this.port;
    }

    public boolean isSync() {
        return sync;
    }

    public boolean getDirect() {
        return direct;
    }



    public void setDirect(boolean direct) {
        this.direct = direct;
    }


    public boolean getSynchronized() {
        return this.sync;
    }



    public String getHost() {
        getBind();
        return this.host;
    }

    public long getTcpSelectorTimeout() {
        return tcpSelectorTimeout;
    }

    public void setTcpSelectorTimeout(long selTimeout) {
        tcpSelectorTimeout = selTimeout;
    }
    /* (non-Javadoc)
     * @see org.apache.catalina.tribes.io.ListenCallback#sendAck()
     */
    public void sendAck() throws IOException {
        // do nothing
    }


}