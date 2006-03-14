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
package org.apache.catalina.tribes.tcp.bio;

import java.io.IOException;

import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.MessageListener;
import java.net.InetAddress;
import org.apache.catalina.tribes.tcp.nio.ThreadPool;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import org.apache.catalina.tribes.io.ListenCallback;
import org.apache.catalina.tribes.ChannelMessage;
import java.net.Socket;
import org.apache.catalina.tribes.io.ObjectReader;

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
public class BioReceiver implements Runnable, ChannelReceiver, ListenCallback {

    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(BioReceiver.class);


    protected MessageListener listener;
    protected String host;
    protected InetAddress bind;
    protected int port;
    protected boolean sendack;
    protected boolean sync;
    protected int rxBufSize = 43800;
    protected int txBufSize = 25188;    
    protected int tcpThreadCount;
    protected ServerSocket serverSocket;
    protected boolean doRun = true;
    
    protected ThreadPool pool;

    public BioReceiver() {
    }

    /**
     *
     * @return The host
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public String getHost() {
        return host;
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

    /**
     *
     * @throws IOException
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public void start() throws IOException {
        this.doRun = true;
        try {
            TcpReplicationThread[] receivers = new TcpReplicationThread[tcpThreadCount];
            for ( int i=0; i<receivers.length; i++ ) {
                receivers[i] = getReplicationThread();
            }
            pool = new ThreadPool(new Object(), receivers);
        } catch (Exception e) {
            log.error("ThreadPool can initilzed. Listener not started", e);
            return;
        }
        try {
            getBind();
            bind();
            Thread t = new Thread(this, "BioReceiver");
            t.setDaemon(true);
            t.start();
        } catch (Exception x) {
            log.fatal("Unable to start cluster receiver", x);
        }
    }
    
    protected TcpReplicationThread getReplicationThread() {
        TcpReplicationThread result = new TcpReplicationThread();
        result.setOptions(getWorkerThreadOptions());
        return result;
    }

    /**
     *
     * @todo Implement this org.apache.catalina.tribes.ChannelReceiver method
     */
    public void stop() {
        this.doRun = false;
        try {
            this.serverSocket.close();
        }catch ( Exception x ) {}
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
    
    public void setTcpSelectorTimeout(long timeout) {
        //do nothing
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
    
    protected int bind(ServerSocket socket, int portstart, int retries) throws IOException {
        while ( retries > 0 ) {
            try {
                InetSocketAddress addr = new InetSocketAddress(getBind(), portstart);
                socket.bind(addr);
                setTcpListenPort(portstart);
                log.info("Bio Server Socket bound to:"+addr);
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


    
    protected void bind() throws IOException {
        // allocate an unbound server socket channel
        serverSocket = new ServerSocket();
        // set the port the server channel will listen to
        //serverSocket.bind(new InetSocketAddress(getBind(), getTcpListenPort()));
        bind(serverSocket,getPort(),10);
    }
    
    public void messageDataReceived(ChannelMessage data) {
        if ( this.listener != null ) {
            listener.messageReceived(data);
        }
    }
    
    public void run() {
        try {
            listen();
        } catch (Exception x) {
            log.error("Unable to run replication listener.", x);
        }

    }
    
    public void listen() throws Exception {
        while ( doRun ) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            }catch ( Exception x ) {
                if ( doRun ) throw x;
            }
            if ( !doRun ) break;
            if ( socket == null ) continue;
            socket.setReceiveBufferSize(rxBufSize);
            socket.setSendBufferSize(txBufSize);
            TcpReplicationThread thread = (TcpReplicationThread)pool.getWorker();
            ObjectReader reader = new ObjectReader(socket,this);

            if ( thread == null ) {
                //we are out of workers, process the request on the listening thread
                thread = getReplicationThread();
                thread.socket = socket;
                thread.reader = reader;
                thread.run();
            } else { 
                thread.serviceSocket(socket,reader);
            }//end if
        }//while
    }
    
    public int getWorkerThreadOptions() {
        int options = 0;
        if ( sync ) options = options |TcpReplicationThread.OPTION_SYNCHRONIZED;
        if ( getSendAck() ) options = options |TcpReplicationThread.OPTION_SEND_ACK;
        return options;
    }




}