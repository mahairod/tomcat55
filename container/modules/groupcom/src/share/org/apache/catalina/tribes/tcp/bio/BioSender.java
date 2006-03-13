/*
 * Copyright 1999,2005 The Apache Software Foundation.
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.tcp.Constants;
import org.apache.catalina.tribes.tcp.DataSender;
import org.apache.catalina.tribes.tcp.SenderState;
import org.apache.catalina.util.StringManager;
import java.net.InetSocketAddress;

/**
 * Send cluster messages with only one socket. Ack and keep Alive Handling is
 * supported
 * 
 * @author Peter Rossbach
 * @author Filip Hanik
 * @version $Revision: 377484 $ $Date: 2006-02-13 15:00:05 -0600 (Mon, 13 Feb 2006) $
 * @since 5.5.16
 */
public class BioSender implements DataSender {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(BioSender.class);

    /**
     * The string manager for this package.
     */
    protected static StringManager sm = StringManager.getManager(Constants.Package);

    // ----------------------------------------------------- Instance Variables

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "DataSender/3.0";

    /**
     * receiver address
     */
    private InetAddress address;

    /**
     * receiver port
     */
    private int port;

    
    /**
     * current sender socket
     */
    private Socket socket = null;

    /**
     * is Socket really connected
     */
    private boolean connected = false;

    /**
     * sender is in suspect state (last transfer failed)
     */
    private SenderState senderState = new SenderState();

    /**
     * wait time for ack
     */
    private long timeout;

    /**
     * waitAckTime
     */
    protected long waitAckTime = 0;
    

    /**
     * keep socket open for no more than one min
     */
    private long keepAliveTimeout = 60 * 1000;

    /**
     * max requests before reconnecting (default -1 unlimited)
     */
    private int keepAliveMaxRequestCount = -1;

    /**
     * Last connect timestamp
     */
    protected long keepAliveConnectTime = 0;

    /**
     * keepalive counter
     */
    protected int keepAliveCount = 0;

    /**
     * wait for receiver Ack
     */
    private boolean waitForAck = false;

    /**
     * After failure make a resend
     */
    private boolean resend = false ;
    /**
     * @todo make this configurable
     */
    protected int rxBufSize = 43800;
    /**
     * We are only sending acks
     */
    protected int txBufSize = 25188;
    
    protected XByteBuffer ackbuf = new XByteBuffer(Constants.ACK_COMMAND.length,true);


    // ------------------------------------------------------------- Constructor
    
    public BioSender(InetAddress host, int port) {
        this.address = host;
        this.port = port;
        if (log.isDebugEnabled())
            log.debug(sm.getString("IDataSender.create",address, new Integer(port)));
    }

    public BioSender(InetAddress host, int port, SenderState state) {
        this(host,port);
        if ( state != null ) this.senderState = state;
    }
    public BioSender(InetAddress host, int port, SenderState state, int rxBufSize, int txBufSize) {
        this(host,port,state);
        this.rxBufSize = rxBufSize;
        this.txBufSize = txBufSize;
    }

    // ------------------------------------------------------------- Properties

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }

    
    /**
     * @param address The address to set.
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    
    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getPort() {
        return port;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isSuspect() {
        return senderState.isSuspect() || senderState.isFailing();
    }

    public boolean getSuspect() {
        return isSuspect();
    }

    public void setSuspect(boolean suspect) {
        if ( suspect ) 
            this.senderState.setSuspect();
        else
            this.senderState.setReady();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long ackTimeout) {
        this.timeout = ackTimeout;
    }

    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(long keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public int getKeepAliveMaxRequestCount() {
        return keepAliveMaxRequestCount;
    }

    public void setKeepAliveMaxRequestCount(int keepAliveMaxRequestCount) {
        this.keepAliveMaxRequestCount = keepAliveMaxRequestCount;
    }

    /**
     * @return Returns the keepAliveConnectTime.
     */
    public long getKeepAliveConnectTime() {
        return keepAliveConnectTime;
    }

    /**
     * @return Returns the keepAliveCount.
     */
    public int getKeepAliveCount() {
        return keepAliveCount;
    }

    /**
     * @return Returns the waitForAck.
     */
    public boolean getWaitForAck() {
        return waitForAck;
    }

    /**
     * @param waitForAck
     *            The waitForAck to set.
     */
    public void setWaitForAck(boolean waitForAck) {
        this.waitForAck = waitForAck;
    }

    /**
     * @return Returns the resend.
     */
    public boolean isResend() {
        return resend;
    }
    /**
     * @param resend The resend to set.
     */
    public void setResend(boolean resend) {
        this.resend = resend;
    }

    public SenderState getSenderState() {
        return senderState;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }

    public int getTxBufSize() {
        return txBufSize;
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public void setTxBufSize(int txBufSize) {
        this.txBufSize = txBufSize;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Connect other cluster member receiver 
     * @see org.apache.catalina.tribes.tcp.IDataSender#connect()
     */
    public synchronized void connect() throws ChannelException {
        try {
            openSocket();
        }catch ( Exception x ) {
            throw new ChannelException(x);
        }
   }

 
    /**
     * disconnect and close socket
     * 
     * @see IDataSender#disconnect()
     */
    public synchronized void disconnect() {
        boolean connect = isConnected();
        closeSocket();
        if (connect) {
            if (log.isDebugEnabled())
                log.debug(sm.getString("IDataSender.disconnect", address.getHostAddress(), new Integer(port), new Long(0)));
        }
        
    }

    /**
     * Check, if time to close socket! Important for AsyncSocketSender that
     * replication thread is not fork again! <b>Only work when keepAliveTimeout
     * or keepAliveMaxRequestCount greater -1 </b>
     * FIXME Can we close a socket when a message wait for ack?
     * @return true, is socket close
     * @see DataSender#closeSocket()
     */
    public synchronized boolean keepalive() {
        boolean isCloseSocket = true ;
        if(isConnected()) {
            if ((keepAliveTimeout > -1 && (System.currentTimeMillis() - keepAliveConnectTime) > keepAliveTimeout)
                || (keepAliveMaxRequestCount > -1 && keepAliveCount >= keepAliveMaxRequestCount)) {
                    closeSocket();
           } else
                isCloseSocket = false ;
        }
        
        return isCloseSocket;
    }

    /**
     * Send message
     * 
     * @see org.apache.catalina.tribes.tcp.IDataSender#sendMessage(,
     *      ChannelMessage)
     */
    public synchronized void sendMessage(byte[] data) throws IOException {
        pushMessage(data);
    }

    
    /**
     * Name of this SockerSender
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("DataSender[(");
        buf.append(super.toString()).append(")");
        buf.append(getAddress()).append(":").append(getPort()).append("]");
        return buf.toString();
    }

    // --------------------------------------------------------- Protected Methods
 
    /**
     * open real socket and set time out when waitForAck is enabled
     * is socket open return directly
     */
    protected synchronized void openSocket() throws IOException {
       if(isConnected()) return ;
       try {
           socket = new Socket();
           InetSocketAddress sockaddr = new InetSocketAddress(getAddress(), getPort());
           socket.connect(sockaddr,(int)timeout);
           socket.setSendBufferSize(getTxBufSize());
           socket.setReceiveBufferSize(getRxBufSize());
           socket.setSoTimeout( (int) timeout);
           connected = true;
           this.keepAliveCount = 0;
           this.keepAliveConnectTime = System.currentTimeMillis();
           if (log.isDebugEnabled())
               log.debug(sm.getString("IDataSender.openSocket", address.getHostAddress(), new Integer(port), new Long(0)));
      } catch (IOException ex1) {
          getSenderState().setSuspect();
          if (log.isDebugEnabled())
              log.debug(sm.getString("IDataSender.openSocket.failure",address.getHostAddress(), new Integer(port),new Long(0)), ex1);
          throw (ex1);
        }
        
     }

    /**
     * close socket
     * 
     * @see DataSender#disconnect()
     * @see DataSender#closeSocket()
     */
    protected synchronized void closeSocket() {
        if(isConnected()) {
             if (socket != null) {
                try {
                    socket.close();
                } catch (IOException x) {
                } finally {
                    socket = null;
                }
            }
            this.keepAliveCount = 0;
            connected = false;
            if (log.isDebugEnabled())
                log.debug(sm.getString("IDataSender.closeSocket",address.getHostAddress(), new Integer(port),new Long(0)));
       }
    }

    /**
     * Push messages with only one socket at a time
     * Wait for ack is needed and make auto retry when write message is failed.
     * After sending error close and reopen socket again.
     * 
     * After successfull sending update stats
     * 
     * WARNING: Subclasses must be very carefull that only one thread call this pushMessage at once!!!
     * 
     * @see #closeSocket()
     * @see #openSocket()
     * @see #writeData(ChannelMessage)
     * 
     * @param data
     *            data to send
     * @since 5.5.10
     */
    
    protected synchronized void pushMessage(byte[] data, boolean reconnect) throws IOException {
        keepalive();
        if ( reconnect ) closeSocket();
        if (!isConnected()) openSocket();
        else if(keepAliveTimeout > -1) this.keepAliveConnectTime = System.currentTimeMillis();
        writeData(data);
    }
    
    protected synchronized void pushMessage( byte[] data) throws IOException {
        boolean messageTransfered = false ;
        IOException exception = null;
        try {
             // first try with existing connection
             pushMessage(data,false);
             messageTransfered = true ;
        } catch (IOException x) {
            exception = x;
            //resend
            if (log.isTraceEnabled()) log.trace(sm.getString("IDataSender.send.again", address.getHostAddress(),new Integer(port)),x);
            try {
                // second try with fresh connection
                pushMessage(data,true);                    
                messageTransfered = true;
                exception = null;
            } catch (IOException xx) {
                exception = xx;
                closeSocket();
            }
        } finally {
            this.keepAliveCount++;
            keepalive();
            if(messageTransfered) {
                
            } else {
                if ( exception != null ) throw exception;
            }
        }
    }

    /**
     * Sent real cluster Message to socket stream
     * FIXME send compress
     * @param data
     * @throws IOException
     * @since 5.5.10
     */
    protected synchronized void writeData(byte[] data) throws IOException { 
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
        if (getWaitForAck()) waitForAck();
    }

    /**
     * Wait for Acknowledgement from other server
     * FIXME Please, not wait only for three charcters, better control that the wait ack message is correct.
     * @param timeout
     * @throws java.io.IOException
     * @throws java.net.SocketTimeoutException
     */
    protected synchronized void waitForAck() throws java.io.IOException {
        try {
            boolean ackReceived = false;
            ackbuf.clear();
            int bytesRead = 0;
            int i = socket.getInputStream().read();
            while ((i != -1) && (bytesRead < Constants.ACK_COMMAND.length)) {
                bytesRead++;
                byte d = (byte)i;
                ackbuf.append(d);
                if (ackbuf.doesPackageExist() ) {
                    ackReceived = Arrays.equals(ackbuf.extractDataPackage(true),Constants.ACK_DATA);
                    break;
                }
                i = socket.getInputStream().read();
            }
            if (!ackReceived) {
                if (i == -1) throw new IOException(sm.getString("IDataSender.ack.eof",getAddress(), new Integer(socket.getLocalPort())));
                else throw new IOException(sm.getString("IDataSender.ack.wrong",getAddress(), new Integer(socket.getLocalPort())));
            }
        } catch (IOException x) {
            String errmsg = sm.getString("IDataSender.ack.missing", getAddress(),new Integer(socket.getLocalPort()), new Long(this.timeout));
            if ( !this.isSuspect() ) {
                this.setSuspect(true);
                if ( log.isWarnEnabled() ) log.warn(errmsg, x);
            } else {
                if ( log.isDebugEnabled() )log.debug(errmsg, x);
            }
            throw x;
        } finally {
            ackbuf.clear();
        }
    }
}
