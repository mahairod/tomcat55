/*
 * Copyright 1999,2005 The Apache Software Foundation.
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


import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.io.ListenCallback;
import org.apache.catalina.util.StringManager;

/**
* FIXME i18n log messages
* @author Peter Rossbach
* @version $Revision$ $Date$
*/

public abstract class ClusterReceiverBase implements Runnable, ClusterReceiver {
    
    protected static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( ClusterReceiverBase.class );

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    private ListenCallback callback;
    private java.net.InetAddress bind;
    private String tcpListenAddress;
    private int tcpListenPort;
    private boolean sendAck;
    protected boolean doListen = false;

    /**
     * Compress message data bytes
     */
    private boolean compress = true ;

    /**
     * @return Returns the doListen.
     */
    public boolean isDoListen() {
        return doListen;
    }

    /**
     * @return Returns the bind.
     */
    public java.net.InetAddress getBind() {
        return bind;
    }
    
    /**
     * @param bind The bind to set.
     */
    public void setBind(java.net.InetAddress bind) {
        this.bind = bind;
    }
    public void setCatalinaCluster(CatalinaCluster cluster) {
        callback = cluster;
    }

    public CatalinaCluster getCatalinaCluster() {
        return (CatalinaCluster) callback;
    }
    
    /**
     * @return Returns the compress.
     */
    public boolean isCompress() {
        return compress;
    }
    
    /**
     * @param compress The compress to set.
     */
    public void setCompress(boolean compressMessageData) {
        this.compress = compressMessageData;
    }
    
    /**
     * Send ACK to sender
     * 
     * @return
     */
    public boolean isSendAck() {
        return sendAck;
    }

    /**
     * set ack mode or not!
     * 
     * @param sendAck
     */
    public void setSendAck(boolean sendAck) {
        this.sendAck = sendAck;
    }
 
    public String getTcpListenAddress() {
        return tcpListenAddress;
    }
    
    public void setTcpListenAddress(String tcpListenAddress) {
        this.tcpListenAddress = tcpListenAddress;
    }
    
    public int getTcpListenPort() {
        return tcpListenPort;
    }
    
    public void setTcpListenPort(int tcpListenPort) {
        this.tcpListenPort = tcpListenPort;
    }
  
    public String getHost() {
        return getTcpListenAddress();
    }

    public int getPort() {
        return getTcpListenPort();
    }

    /**
     * start cluster receiver
     * 
     * @see org.apache.catalina.cluster.ClusterReceiver#start()
     */
    public void start() {
        try {
            if ("auto".equals(tcpListenAddress)) {
                tcpListenAddress = java.net.InetAddress.getLocalHost()
                        .getHostAddress();
            }
            if (log.isDebugEnabled())
                log.debug("Starting replication listener on address:"
                        + tcpListenAddress);
            bind = java.net.InetAddress.getByName(tcpListenAddress);
            Thread t = new Thread(this, "ClusterReceiver");
            t.setDaemon(true);
            t.start();
        } catch (Exception x) {
            log.fatal("Unable to start cluster receiver", x);
        }
    }

    /**
     * Stop accept
     * 
     * @see org.apache.catalina.cluster.ClusterReceiver#stop()
     * @see #stopListening()
     */
    public void stop() {
        stopListening();
    }
    
    
    /**
     * 
     */
    protected abstract void stopListening() ;

    protected abstract void listen ()
       throws Exception ;

    
    public void run()
    {
        try
        {
            listen();
        }
        catch ( Exception x )
        {
            log.error("Unable to start cluster listener.",x);
        }
    }
    
}
