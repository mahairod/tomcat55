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

package org.apache.catalina.tribes.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.catalina.tribes.ChannelMessage;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import org.apache.catalina.tribes.io.XByteBuffer;

/**
 * This class is NOT thread safe and should never be used with more than one thread at a time
 * 
 * This is a state machine, handled by the process method
 * States are:
 * - NOT_CONNECTED -> connect() -> CONNECTED
 * - CONNECTED -> setMessage() -> READY TO WRITE
 * - READY_TO_WRITE -> write() -> READY TO WRITE | READY TO READ
 * - READY_TO_READ -> read() -> READY_TO_READ | TRANSFER_COMPLETE
 * - TRANSFER_COMPLETE -> CONNECTED
 * 
 * @author Filip Hanik
 * @version 1.0
 */
public class NioSender  {

    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(NioSender.class);

    
    protected long ackTimeout = 15000;
    protected InetAddress address;
    protected String domain = "";
    protected int port;
    protected boolean suspect = false;
    protected boolean connected = false;
    protected boolean waitForAck = false;
    protected int rxBufSize = 25188;
    protected int txBufSize = 43800;
    protected Selector selector;
    
    
    protected SocketChannel socketChannel;
    protected ByteBuffer buf = null;
    protected boolean direct = false;
    protected ChannelMessage current = null;
    protected int curPos=0;
    protected XByteBuffer ackbuf = new XByteBuffer(128,true);

    public NioSender() {
        
    }
    
    public boolean process(SelectionKey key) throws IOException {
        int ops = key.readyOps();
        key.interestOps(key.interestOps() & ~ops);
        if ( key.isConnectable() ) {
            if ( socketChannel.finishConnect() ) {
                //we connected, register ourselves for writing
                this.connected = true;
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                return false;
            }
        } else if ( key.isWritable() ) {
            boolean writecomplete = write(key);
            if ( writecomplete ) {
                //we are completed, should we read an ack?
                if ( waitForAck ) key.interestOps(key.interestOps()|SelectionKey.OP_READ);
                //if not, we are ready, setMessage will reregister us for write interest
                else return true;
            } else {
                //we are not complete, lets write some more
                key.interestOps(key.interestOps()|SelectionKey.OP_WRITE);
            }
        } else if ( key.isReadable() ) {
            //TODO, HANDLE ACK TIMEOUT-and reconnect
            boolean readcomplete = read(key);
            if ( readcomplete ) return true;
            else key.interestOps(key.interestOps()|SelectionKey.OP_READ);
        } else {
            //unknown state
            log.warn("Data is in unknown state. readyOps="+ops);
        }
        return false;
        
    }

    protected boolean read(SelectionKey key) throws IOException {
        //if there is no message here, we are done
        if ( current == null ) return true;
        int read = socketChannel.read(buf);
        //end of stream
        if ( read == -1 ) return true;
        //no data read
        else if ( read == 0 ) return false;
        throw new UnsupportedOperationException();
    }

    
    protected boolean write(SelectionKey key) throws IOException {
        if ( (!connected) || (this.socketChannel==null)) {
            throw new IOException("NioSender is not connected, this should not occur.");
        }
        if ( current != null ) {
            int remaining = buf.remaining();
            if ( remaining > 0 ) {
                //write the rest of the buffer
                remaining -= socketChannel.write(buf);
            }            
            if ( remaining == 0 ) {
                //weve written everything, or we are starting a new package
                XByteBuffer msg = current.getMessage();
                remaining = msg.getLength() - curPos;
                buf.clear();
                //protect against buffer overwrite
                int length = Math.min(remaining,txBufSize);
                buf.put(msg.getBytesDirect(),curPos,length);
                
                //if the entire message fits in the buffer
                //reset the position counter
                curPos += length;
                if ( curPos >= msg.getLength() ) curPos = 0;
                remaining -= socketChannel.write(buf);
            }
            //the write 
            return (remaining==0 && curPos == 0);
        }
        //no message to send, we can consider that complete
        return true;
    }

    /**
     * connect - blocking in this operation
     *
     * @throws IOException
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public synchronized void connect() throws IOException {
        if ( connected ) throw new IOException("NioSender is already in connected state.");
        if ( buf == null ) {
            if ( direct ) buf = ByteBuffer.allocateDirect(txBufSize);
            else buf = ByteBuffer.allocate(txBufSize);
        }
        InetSocketAddress addr = new InetSocketAddress(address,port);
        if ( socketChannel != null ) throw new IOException("Socket channel has already been established. Connection might be in progress.");
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(addr);
        socketChannel.register(getSelector(),SelectionKey.OP_CONNECT,this);
        this.connected = true;
    }
    

    /**
     * disconnect
     *
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void disconnect() {
        try {
            this.connected = false;
            if ( buf != null ) buf.clear();
            socketChannel.close();
            socketChannel = null;
            curPos = 0;
        } catch ( Exception x ) {
            log.error("Unable to disconnect.",x);
        }

    }
    
    /**
    * sendMessage
    *
    * @param data ChannelMessage
    * @throws IOException
    * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
    */
   public synchronized void setMessage(ChannelMessage data) throws IOException {
       this.current = data;
       if ( data != null ) {
           if (!this.connected) {
               connect();
           } else {
               socketChannel.register(getSelector(), SelectionKey.OP_WRITE, this);
           }
       }
   }


    /**
     * checkKeepAlive
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public boolean checkKeepAlive() {
        return false;
    }

    /**
     * getAckTimeout
     *
     * @return long
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public long getAckTimeout() {
        return this.ackTimeout;
    }

    /**
     * getAddress
     *
     * @return InetAddress
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * getDomain
     *
     * @return String
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public String getDomain() {
        return domain;
    }

    /**
     * getPort
     *
     * @return int
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public int getPort() {
        return port;
    }

    /**
     * getSuspect
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public boolean getSuspect() {
        return suspect;
    }

    /**
     * isConnected
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * isWaitForAck
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public boolean getWaitForAck() {
        return waitForAck;
    }

    public Selector getSelector() {
        return selector;
    }

    public boolean getDirect() {
        return direct;
    }

   
    /**
     * setAckTimeout
     *
     * @param timeout long
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setAckTimeout(long timeout) {
        this.ackTimeout = timeout;
    }

    /**
     * setAddress
     *
     * @param address InetAddress
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * setDomain
     *
     * @param domain String
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * setPort
     *
     * @param port int
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * setRxBufSize
     *
     * @param size int
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setRxBufSize(int size) {
        this.rxBufSize = size;
    }

    /**
     * setSuspect
     *
     * @param suspect boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setSuspect(boolean suspect) {
        this.suspect = suspect;
    }

    /**
     * setTxBufSize
     *
     * @param size int
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setTxBufSize(int size) {
        this.txBufSize= size;
    }

    /**
     * setWaitForAck
     *
     * @param isWaitForAck boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void setWaitForAck(boolean waitForAck) {
        this.waitForAck=waitForAck;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void setDirect(boolean directBuffer) {
        this.direct = directBuffer;
    }

}