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
import java.net.UnknownHostException;

import org.apache.catalina.tribes.Member;

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
public abstract class AbstractSender implements DataSender {
    
    private boolean connected = false;
    private int rxBufSize = 25188;
    private int txBufSize = 43800;
    private boolean directBuffer = false;
    private int keepAliveCount = -1;
    private int requestCount = 0;
    private long connectTime;
    private long keepAliveTime = -1;
    private long timeout = 15000;
    private Member destination;
    private InetAddress address;
    private int port;
    private int maxRetryAttempts = 0;//zero resends
    private int attempt;
    public AbstractSender() {
        
    }
    
    public AbstractSender(Member destination) throws UnknownHostException {
        this.destination = destination;
        this.address = InetAddress.getByAddress(destination.getHost());
        this.port = destination.getPort();
    }
    
    public AbstractSender(Member destination, int rxBufSize, int txBufSize) throws UnknownHostException {
        this(destination);
        this.rxBufSize = rxBufSize;
        this.txBufSize = txBufSize;
    }

    /**
     * connect
     *
     * @throws IOException
     * @todo Implement this org.apache.catalina.tribes.tcp.DataSender method
     */
    public abstract void connect() throws IOException;

    /**
     * disconnect
     *
     * @todo Implement this org.apache.catalina.tribes.tcp.DataSender method
     */
    public abstract void disconnect();

    /**
     * keepalive
     *
     * @return boolean
     * @todo Implement this org.apache.catalina.tribes.tcp.DataSender method
     */
    public boolean keepalive() {
        boolean disconnect = false;
       if ( keepAliveCount >= 0 && requestCount>keepAliveCount ) disconnect = true;
       else if ( keepAliveTime >= 0 && keepAliveTime> (System.currentTimeMillis()-connectTime) ) disconnect = true;
       if ( disconnect ) disconnect();
       return disconnect;

    }
    
    protected void setConnected(boolean connected){
        this.connected = connected;
    }
    
    public boolean isConnected() {
        return connected;
    }

    public long getConnectTime() {
        return connectTime;
    }

    public Member getDestination() {
        return destination;
    }


    public int getKeepAliveCount() {
        return keepAliveCount;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getTxBufSize() {
        return txBufSize;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setDirectBuffer(boolean directBuffer) {
        this.directBuffer = directBuffer;
    }

    public boolean getDirectBuffer() {
        return this.directBuffer;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setKeepAliveCount(int keepAliveCount) {
        this.keepAliveCount = keepAliveCount;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setTxBufSize(int txBufSize) {
        this.txBufSize = txBufSize;
    }

    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }
}