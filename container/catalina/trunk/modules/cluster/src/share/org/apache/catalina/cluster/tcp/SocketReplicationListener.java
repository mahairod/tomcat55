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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.catalina.cluster.io.SocketObjectReader;

/**
 * @author Peter Rossbach
 * @version $Revision$, $Date$
 */
public class SocketReplicationListener extends ClusterReceiverBase {
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "SocketReplicationListener/1.2";

    private ServerSocket serverSocket = null;

    private int tcpListenMaxPort ;
    
    public SocketReplicationListener() {
    }

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }
    
    /**
     * @return Returns the tcpListenMaxPort.
     */
    public int getTcpListenMaxPort() {
        return tcpListenMaxPort;
    }
    
    /**
     * @param tcpListenMaxPort The tcpListenMaxPort to set.
     */
    public void setTcpListenMaxPort(int maxListenPort) {
        this.tcpListenMaxPort = maxListenPort;
    }
    
    /**
     * Master/Slave Sender handling / bind Server Socket at addres and port
     * 
     * @throws Exception
     */
    protected void listen() {
        if (doListen) {
            log.warn("ServerSocket allready started");
            return;
        }

        // Get the associated ServerSocket to bind it with
        try {
            serverSocket = createServerSocket();
            if(serverSocket != null) {
                doListen = true;
                while (doListen) {
                    try {
                        Socket socket = serverSocket.accept();
                        if (doListen) {
                            SocketReplicationThread t = new SocketReplicationThread(
                                    this, socket, new SocketObjectReader(socket,
                                            this), isSendAck());
                            t.setDaemon(true);
                            t.start();
                        }
                    } catch (IOException iex) {
                        log.warn("Exception to start thread", iex);
                    }
                }
                serverSocket.close();
            } else {
                log.fatal("Fatal error: Receiver socket not bound - address=" +  getTcpListenAddress()
                        + " port=" + getTcpListenPort() + " maxport=" + getTcpListenMaxPort() );
            }                
        } catch (IOException iex) {
            log.warn("Exception at start or close server socket", iex);
        } finally {
            doListen = false;
            serverSocket = null;
        }
    }

    /**
     * create a Server Socket between tcpListenerPort and tcpListenMaxPort
     */
    protected ServerSocket createServerSocket() {
        int startPort = getTcpListenPort() ;
        int maxPort = getTcpListenMaxPort() ;
        InetAddress inet = getBind() ;
        ServerSocket sSocket = null ;
        if (maxPort < startPort)
            maxPort = startPort;
        for( int i=startPort; i<=maxPort; i++ ) {
            try {
                if( inet == null ) {
                    sSocket = new ServerSocket( i, 0 );
                } else {
                    sSocket=new ServerSocket( i, 0, inet );
                }
                setTcpListenPort(i);
                break;
            } catch( IOException ex ) {
                if(log.isDebugEnabled())
                    log.debug("Port busy at [" + inet.getHostAddress() + "." + i + "] - reason: "  + ex.toString());
                continue;
            }
        }
        if(sSocket != null && log.isInfoEnabled())
            log.info("Open Socket at [" + inet.getHostAddress() + "." + getTcpListenPort() + "]");
        return sSocket ;
   }

    /**
     * Need to create a connection to unlock the ServerSocker#accept(). Very
     * fine trick from channelSocket :-)
     * 
     * @see org.apache.jk.common.ChannelSocket#unLockSocket()
     */
    protected void unLockSocket() {
        Socket s = null;
        InetAddress ladr = getBind();

        try {
            if (ladr == null || "0.0.0.0".equals(ladr.getHostAddress())) {
                ladr = InetAddress.getLocalHost();
            }
            s = new Socket(ladr, getTcpListenPort());
            // setting soLinger to a small value will help shutdown the
            // connection quicker
            s.setSoLinger(true, 0);

        } catch (IOException iex) {
            log.warn("UnLocksocket failure", iex);
        } finally {
            try {
                if (s != null)
                    s.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Close serverSockets FIXME the channelSocket to connect own socket to
     * terminate accpet loop!
     */
    protected void stopListening() {
        unLockSocket();
        doListen = false;
    }

}
