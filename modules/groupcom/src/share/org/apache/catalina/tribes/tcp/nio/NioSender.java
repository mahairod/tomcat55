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

package org.apache.catalina.tribes.tcp.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.tcp.DataSender;

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
public class NioSender implements DataSender{

    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(NioSender.class);

    protected boolean connected = false;
    protected boolean waitForAck = false;
    protected int rxBufSize = 25188;
    protected int txBufSize = 43800;
    protected Selector selector;
    protected Member destination;
    
    
    protected SocketChannel socketChannel;

    /*
     * STATE VARIABLES *
     */
    protected ByteBuffer readbuf = null;
    protected boolean direct = false;
    protected byte[] current = null;
    protected int curPos=0;
    protected XByteBuffer ackbuf = new XByteBuffer(128,true);
    protected int remaining = 0;
    protected boolean complete;
    protected int attempt;
    protected int keepAliveCount = -1;
    protected int requestCount = 0;
    protected long connectTime;
    protected long keepAliveTime = -1;
    protected long timeout;

    public NioSender(Member destination) {
        this.destination = destination;
        
    }
    
    /**
     * State machine to send data
     * @param key SelectionKey
     * @return boolean
     * @throws IOException
     */
    public boolean process(SelectionKey key) throws IOException {
        int ops = key.readyOps();
        key.interestOps(key.interestOps() & ~ops);
        if ( key.isConnectable() ) {
            if ( socketChannel.finishConnect() ) {
                //we connected, register ourselves for writing
                connected = true;
                requestCount = 0;
                connectTime = System.currentTimeMillis();
                socketChannel.socket().setSendBufferSize(txBufSize);
                socketChannel.socket().setReceiveBufferSize(rxBufSize);
                socketChannel.socket().setSoTimeout((int)timeout);
                if ( current != null ) key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                return false;
            } else  { 
                //wait for the connection to finish
                key.interestOps(key.interestOps() | SelectionKey.OP_CONNECT);
                return false;
            }//end if
        } else if ( key.isWritable() ) {
            boolean writecomplete = write(key);
            if ( writecomplete ) {
                //we are completed, should we read an ack?
                if ( waitForAck ) {
                    //register to read the ack
                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                } else {
                    //if not, we are ready, setMessage will reregister us for another write interest
                    //do a health check, we have no way of verify a disconnected
                    //socket since we don't register for OP_READ on waitForAck=false
                    read(key);//this causes overhead.
                    requestCount++;
                    return true;
                }
            } else {
                //we are not complete, lets write some more
                key.interestOps(key.interestOps()|SelectionKey.OP_WRITE);
            }//end if
        } else if ( key.isReadable() ) {
            boolean readcomplete = read(key);
            if ( readcomplete ) {
                requestCount++;
                return true;
            } else {
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            }//end if
        } else {
            //unknown state, should never happen
            log.warn("Data is in unknown state. readyOps="+ops);
            throw new IOException("Data is in unknown state. readyOps="+ops);
        }//end if
        return false;
    }
    
    

    protected boolean read(SelectionKey key) throws IOException {
        //if there is no message here, we are done
        if ( current == null ) return true;
        int read = socketChannel.read(readbuf);
        //end of stream
        if ( read == -1 ) throw new IOException("Unable to receive an ack message. EOF on socket channel has been reached.");
        //no data read
        else if ( read == 0 ) return false;
        readbuf.flip();
        ackbuf.append(readbuf,read);
        readbuf.clear();
        if (ackbuf.doesPackageExist() ) {
            return Arrays.equals(ackbuf.extractDataPackage(true),org.apache.catalina.tribes.tcp.Constants.ACK_DATA);
        } else {
            return false;
        }
    }

    
    protected boolean write(SelectionKey key) throws IOException {
        if ( (!connected) || (this.socketChannel==null)) {
            throw new IOException("NioSender is not connected, this should not occur.");
        }
        if ( current != null ) {
            if ( remaining > 0 ) {
                //weve written everything, or we are starting a new package
                //protect against buffer overwrite
                int length = current.length-curPos;
                ByteBuffer writebuf = ByteBuffer.wrap(current,curPos,length);
                int byteswritten = socketChannel.write(writebuf);
                curPos += byteswritten;
                remaining -= byteswritten;
                //if the entire message was written from the buffer
                //reset the position counter
                if ( curPos >= current.length ) {
                    curPos = 0;
                    remaining = 0;
                }
            }
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
        if ( readbuf == null ) {
            readbuf = getReadBuffer();
        } else {
            readbuf.clear();
        }
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByAddress(destination.getHost()),destination.getPort());
        if ( socketChannel != null ) throw new IOException("Socket channel has already been established. Connection might be in progress.");
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(addr);
        socketChannel.register(getSelector(),SelectionKey.OP_CONNECT,this);
    }
    

    /**
     * disconnect
     *
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public void disconnect() {
        try {
            this.connected = false;
            if ( socketChannel != null ) {
                Socket socket = socketChannel.socket();
                //error free close, all the way
                try {socket.shutdownOutput();}catch ( Exception x){}
                try {socket.shutdownInput();}catch ( Exception x){}
                try {socket.close();}catch ( Exception x){}
                try {socketChannel.close();}catch ( Exception x){}
                socket = null;
                socketChannel = null;
            }
        } catch ( Exception x ) {
            log.error("Unable to disconnect NioSender. msg="+x.getMessage());
            if ( log.isDebugEnabled() ) log.debug("Unable to disconnect NioSender. msg="+x.getMessage(),x);
        } finally {
            reset();
        }

    }
    
    public void reset() {
        if ( connected && readbuf == null) {
            readbuf = getReadBuffer();
        }
        if ( readbuf != null ) readbuf.clear();
        current = null;
        curPos = 0;
        ackbuf.clear();
        remaining = 0;
        complete = false;
        attempt = 0;
        requestCount = 0;
        connectTime = -1;
    }

    private ByteBuffer getReadBuffer() {
        return (direct?ByteBuffer.allocateDirect(rxBufSize):ByteBuffer.allocate(rxBufSize));
    }
    
    /**
    * sendMessage
    *
    * @param data ChannelMessage
    * @throws IOException
    * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
    */
   public synchronized void setMessage(byte[] data) throws IOException {
       reset();
       if ( data != null ) {
           current = data;
           remaining = current.length;
           curPos = 0;
           if (connected) {
               socketChannel.register(getSelector(), SelectionKey.OP_WRITE, this);
           }
       } 
   }
   
   public byte[] getMessage() {
       return current;
   }


    /**
     * checkKeepAlive
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.IDataSender method
     */
    public boolean keepalive() {
        boolean disconnect = false;
        if ( keepAliveCount >= 0 && requestCount>keepAliveCount ) disconnect = true;
        else if ( keepAliveTime >= 0 && keepAliveTime> (System.currentTimeMillis()-connectTime) ) disconnect = true;
        if ( disconnect ) disconnect();
        return disconnect;
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

    public Member getDestination() {
        return destination;
    }

    public boolean isComplete() {
        return complete;
    }

    public int getAttempt() {
        return attempt;
    }

    public long getTimeout() {
        return timeout;
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

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public void setKeepAliveCount(int keepAliveCount) {
        this.keepAliveCount = keepAliveCount;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}