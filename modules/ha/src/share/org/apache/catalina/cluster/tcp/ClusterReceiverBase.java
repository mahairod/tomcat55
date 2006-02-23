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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.StringManager;

/**
* @author Filip Hanik
* @author Peter Rossbach
* @version $Revision: 379550 $ $Date: 2006-02-21 12:06:35 -0600 (Tue, 21 Feb 2006) $
*/

public class ClusterReceiverBase extends ReplicationListener {
    
    protected static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( ClusterReceiverBase.class );

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    private CatalinaCluster cluster;
    

    /**
     * total bytes to recevied
     */
    protected long totalReceivedBytes = 0;
    
    /**
     * doProcessingStats
     */
    protected boolean doReceivedProcessingStats = false;

    /**
     * proessingTime
     */
    protected long receivedProcessingTime = 0;
    
    /**
     * min proessingTime
     */
    protected long minReceivedProcessingTime = Long.MAX_VALUE ;

    /**
     * max proessingTime
     */
    protected long maxReceivedProcessingTime = 0;
    
    /**
     * Sending Stats
     */
    private long nrOfMsgsReceived = 0;

    private long receivedTime = 0;

    private long lastChecked = System.currentTimeMillis();



    /**
     * Transmitter Mbean name
     */
    private ObjectName objectName;

    /**
     * @return Returns the doListen.
     */
    public boolean isDoListen() {
        return doListen;
    }

    
    public void setCatalinaCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }

    public CatalinaCluster getCatalinaCluster() {
        return (CatalinaCluster) cluster;
    }
    
    /**
     *  set Receiver ObjectName
     * 
     * @param name
     */
    public void setObjectName(ObjectName name) {
        objectName = name;
    }

    /**
     * Receiver ObjectName
     * 
     */
    public ObjectName getObjectName() {
        return objectName;
    }
    
    
    // ------------------------------------------------------------- stats

    /**
     * @return Returns the doReceivedProcessingStats.
     */
    public boolean isDoReceivedProcessingStats() {
        return doReceivedProcessingStats;
    }
    /**
     * @param doReceiverProcessingStats The doReceivedProcessingStats to set.
     */
    public void setDoReceivedProcessingStats(boolean doReceiverProcessingStats) {
        this.doReceivedProcessingStats = doReceiverProcessingStats;
    }
    /**
     * @return Returns the maxReceivedProcessingTime.
     */
    public long getMaxReceivedProcessingTime() {
        return maxReceivedProcessingTime;
    }
    /**
     * @return Returns the minReceivedProcessingTime.
     */
    public long getMinReceivedProcessingTime() {
        return minReceivedProcessingTime;
    }
    /**
     * @return Returns the receivedProcessingTime.
     */
    public long getReceivedProcessingTime() {
        return receivedProcessingTime;
    }
    /**
     * @return Returns the totalReceivedBytes.
     */
    public long getTotalReceivedBytes() {
        return totalReceivedBytes;
    }
    
    /**
     * @return Returns the avg receivedProcessingTime/nrOfMsgsReceived.
     */
    public double getAvgReceivedProcessingTime() {
        return ((double)receivedProcessingTime) / nrOfMsgsReceived;
    }

    /**
     * @return Returns the avg totalReceivedBytes/nrOfMsgsReceived.
     */
    public long getAvgTotalReceivedBytes() {
        return ((long)totalReceivedBytes) / nrOfMsgsReceived;
    }

    /**
     * @return Returns the receivedTime.
     */
    public long getReceivedTime() {
        return receivedTime;
    }

    /**
     * @return Returns the lastChecked.
     */
    public long getLastChecked() {
        return lastChecked;
    }

    /**
     * @return Returns the nrOfMsgsReceived.
     */
    public long getNrOfMsgsReceived() {
        return nrOfMsgsReceived;
    }

    /**
     * start cluster receiver
     * 
     * @see org.apache.catalina.cluster.ClusterReceiver#start()
     */
    public void start() {
        super.start();
        registerReceiverMBean();
    }

 
    /**
     * Stop accept
     * 
     * @see org.apache.catalina.cluster.ClusterReceiver#stop()
     * @see #stopListening()
     */
    public void stop() {
        super.stop();
        unregisterRecevierMBean();
     
    }
    
    /**
     * Register Recevier MBean
     * <domain>:type=ClusterReceiver,host=<host>
     */
    protected void registerReceiverMBean() {
        if (cluster != null && cluster instanceof SimpleTcpCluster) {
            SimpleTcpCluster scluster = (SimpleTcpCluster) cluster;
            ObjectName clusterName = scluster.getObjectName();
            try {
                MBeanServer mserver = scluster.getMBeanServer();
                Container container = cluster.getContainer();
                String name = clusterName.getDomain() + ":type=ClusterReceiver";
                if (container instanceof StandardHost) {
                    name += ",host=" + clusterName.getKeyProperty("host");
                }
                ObjectName receiverName = new ObjectName(name);
                if (mserver.isRegistered(receiverName)) {
                    if (log.isWarnEnabled())
                        log.warn(sm.getString(
                                "cluster.mbean.register.already",
                                receiverName));
                    return;
                }
                setObjectName(receiverName);
                mserver.registerMBean(scluster.getManagedBean(this),getObjectName());
            } catch (Exception e) {
                log.warn("Unable to register JMX bean ClusterReceiverBase",e);
            }
        }
    }
   
    /**
     * UnRegister Recevier MBean
     * <domain>:type=ClusterReceiver,host=<host>
     */
    protected void unregisterRecevierMBean() {
        if (cluster != null && getObjectName() != null
                && cluster instanceof SimpleTcpCluster) {
            SimpleTcpCluster scluster = (SimpleTcpCluster) cluster;
            try {
                MBeanServer mserver = scluster.getMBeanServer();
                mserver.unregisterMBean(getObjectName());
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    
    

    // --------------------------------------------------------- receiver messages

    /**
     * receiver Message from other node.
     * All SessionMessage forward to ClusterManager and other message dispatch to all accept MessageListener.
     *
     * @see ClusterSessionListener#messageReceived(ClusterMessage)
     */
    public void messageDataReceived(ClusterData data) {
    //public void messageDataReceived(byte[] data) {
        long timeSent = 0 ;
        if (doReceivedProcessingStats) {
            timeSent = System.currentTimeMillis();
        }
        try {
            ClusterMessage message = deserialize(data);
            // calc stats really received bytes
            totalReceivedBytes += data.getMessage().length;
            //totalReceivedBytes += data.length;
            nrOfMsgsReceived++;
            cluster.receive(message);
        } catch (Exception x) {
            log
                    .error(
                            "Unable to deserialize session message or unexpected exception from message listener.",
                            x);
        } finally {
            if (doReceivedProcessingStats) {
                addReceivedProcessingStats(timeSent);
            }
        }
    }

    
    
    // --------------------------------------------- Performance Stats

    /**
     * Reset sender statistics
     */
    public synchronized void resetStatistics() {
        nrOfMsgsReceived = 0;
        totalReceivedBytes = 0;
        minReceivedProcessingTime = Long.MAX_VALUE ;
        maxReceivedProcessingTime = 0 ;
        receivedProcessingTime = 0 ;
        receivedTime = 0 ;
    }

    /**
     * Add receiver processing stats times
     * @param startTime
     */
    protected void addReceivedProcessingStats(long startTime) {
        long current = System.currentTimeMillis() ;
        long time = current - startTime ;
        synchronized(this) {
            if(time < minReceivedProcessingTime)
                minReceivedProcessingTime = time ;
            if( time > maxReceivedProcessingTime)
                maxReceivedProcessingTime = time ;
            receivedProcessingTime += time ;
        }
        if (log.isDebugEnabled()) {
            if ((current - lastChecked) > 5000) {
                log.debug("Calc msg send time total=" + receivedTime
                        + "ms num request=" + nrOfMsgsReceived
                        + " average per msg="
                        + (receivedTime / nrOfMsgsReceived) + "ms.");
                lastChecked=current ;
            }
        }
    }
    
    

}
