/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
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

    private InetAddress address;
    private int port;
    private Socket sc = null;
    private boolean isSocketConnected = false;
    private boolean suspect;
    private long ackTimeout = 5*1000;
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
        //sc.setSoTimeout((int)ackTimeout);
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

    /**
     * Blocking send
     * @param data
     * @throws java.io.IOException
     */
    public synchronized void sendMessage(String sessionId, byte[] data) throws java.io.IOException
    {
        long ctime = System.currentTimeMillis() - this.keepAliveConnectTime;
        if ( (ctime > this.keepAliveTimeout) ||
             (this.keepAliveCount >= this.keepAliveMaxRequestCount) ) {
            disconnect();
        }
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
    }

    private void waitForAck(long timeout)  throws java.io.IOException,
        java.net.SocketTimeoutException {
        int i = sc.getInputStream().read();
        while ( (i!=-1) && (i!=3) ) {
            i = sc.getInputStream().read();
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
