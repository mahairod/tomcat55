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

package org.apache.catalina.cluster.tcp;
import java.net.InetAddress ;
import java.net.Socket;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SocketSender implements IDataSender
{

    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( SocketSender.class );

    private InetAddress address;
    private int port;
    private Socket sc = null;
    private boolean isSocketConnected = false;
    private boolean suspect;
    private long ackTimeout = 15*1000;  //15 seconds socket read timeout (for acknowledgement)
    private long keepAliveTimeout = 60*1000; //keep socket open for no more than one min
    private int keepAliveMaxRequestCount = 100; //max 100 requests before reconnecting
    private long keepAliveConnectTime = 0;
    private int keepAliveCount = 0;


    public SocketSender(InetAddress host, int port)
    {
        this.address = host;
        this.port = port;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }

    public void connect() throws java.io.IOException
    {
        sc = new Socket(getAddress(),getPort());
        sc.setSoTimeout((int)ackTimeout);
        isSocketConnected = true;
        this.keepAliveCount = 0;
        this.keepAliveConnectTime = System.currentTimeMillis();
    }

    public void disconnect()
    {
        try
        {
            sc.close();
        }catch ( Exception x)
        {}
        isSocketConnected = false;
    }

    public boolean isConnected()
    {
        return isSocketConnected;
    }

    public void checkIfDisconnect() {
        long ctime = System.currentTimeMillis() - this.keepAliveConnectTime;
        if ( (ctime > this.keepAliveTimeout) ||
             (this.keepAliveCount >= this.keepAliveMaxRequestCount) ) {
            disconnect();
        }
    }

    public void setAckTimeout(long timeout) {
        this.ackTimeout = timeout;
    }

    public long getAckTimeout() {
        return ackTimeout;
    }

    /**
     * Blocking send
     * @param data
     * @throws java.io.IOException
     */
    public synchronized void sendMessage(String sessionId, byte[] data) throws java.io.IOException
    {
        checkIfDisconnect();
        if ( !isConnected() ) connect();
        try
        {
            sc.getOutputStream().write(data);
            sc.getOutputStream().flush();
            waitForAck(ackTimeout);
        }
        catch ( java.io.IOException x )
        {
            disconnect();
            connect();
            sc.getOutputStream().write(data);
            sc.getOutputStream().flush();
            waitForAck(ackTimeout);
        }
        this.keepAliveCount++;
        checkIfDisconnect();

    }

    private void waitForAck(long timeout)  throws java.io.IOException {
        try {
            int i = sc.getInputStream().read();
            while ( (i != -1) && (i != 3)) {
                i = sc.getInputStream().read();
            }
        } catch (java.net.SocketTimeoutException x ) {
            log.warn("Wasn't able to read acknowledgement from server["+getAddress()+":"+getPort()+"] in "+this.ackTimeout+" ms."+
                     " Disconnecting socket, and trying again.");
            throw x;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("SocketSender[");
        buf.append(getAddress()).append(":").append(getPort()).append("]");
        return buf.toString();
    }
    public boolean isSuspect() {
        return suspect;
    }

    public boolean getSuspect() {
        return suspect;
    }

    public void setSuspect(boolean suspect) {
        this.suspect = suspect;
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


}
