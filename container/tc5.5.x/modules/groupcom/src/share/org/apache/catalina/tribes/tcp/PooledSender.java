/*
 * Copyright 1999,2004-2005 The Apache Software Foundation.
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

import java.util.List;

import org.apache.catalina.tribes.ChannelException;

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
public abstract class PooledSender implements DataSender {
    
    private SenderQueue queue = null;
    private boolean connected;
    private int rxBufSize;
    private int txBufSize;
    private boolean waitForAck;
    private long timeout;
    private int poolSize = 25;

    public PooledSender() {
        queue = new SenderQueue(this,poolSize);
    }
    
    public abstract DataSender getNewDataSender();
    
    public DataSender getSender() {
        return queue.getSender(0);
    }
    
    public void returnSender(DataSender sender) {
        sender.checkKeepAlive();
        queue.returnSender(sender);
    }
    
    public synchronized void connect() throws ChannelException {
        //do nothing, happens in the socket sender itself
        queue.open();
        setConnected(true);
    }
    
    public synchronized void disconnect() {
        queue.close();
        setConnected(false);
    }
    
    
    public int getInPoolSize() {
        return queue.getInPoolSize();
    }

    public int getInUsePoolSize() {
        return queue.getInUsePoolSize();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public void setTxBufSize(int txBufSize) {
        this.txBufSize = txBufSize;
    }

    public void setWaitForAck(boolean waitForAck) {
        this.waitForAck = waitForAck;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        queue.setLimit(poolSize);
    }

    public boolean isConnected() {
        return connected;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }

    public int getTxBufSize() {
        return txBufSize;
    }

    public boolean getWaitForAck() {
        return waitForAck;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public boolean checkKeepAlive() {
        //do nothing, the pool checks on every return
        return false;
    }

    

    //  ----------------------------------------------------- Inner Class

    private class SenderQueue {
        private int limit = 25;

        PooledSender parent = null;

        private List notinuse = null;

        private List inuse = null;

        private boolean isOpen = true;

        public SenderQueue(PooledSender parent, int limit) {
            this.limit = limit;
            this.parent = parent;
            notinuse = new java.util.LinkedList();
            inuse = new java.util.LinkedList();
        }

        /**
         * @return Returns the limit.
         */
        public int getLimit() {
            return limit;
        }
        /**
         * @param limit The limit to set.
         */
        public void setLimit(int limit) {
            this.limit = limit;
        }
        /**
         * @return
         */
        public int getInUsePoolSize() {
            return inuse.size();
        }

        /**
         * @return
         */
        public int getInPoolSize() {
            return notinuse.size();
        }

        public synchronized DataSender getSender(long timeout) {
            long start = System.currentTimeMillis();
            while ( true ) {
                if (!isOpen)throw new IllegalStateException("Queue is closed");
                DataSender sender = null;
                if (notinuse.size() == 0 && inuse.size() < limit) {
                    sender = parent.getNewDataSender();
                } else if (notinuse.size() > 0) {
                    sender = (DataSender) notinuse.remove(0);
                }
                if (sender != null) {
                    inuse.add(sender);
                    return sender;
                }//end if
                long delta = System.currentTimeMillis() - start;
                if ( delta > timeout && timeout>0) return null;
                else {
                    try {
                        wait(Math.max(timeout - delta,1));
                    }catch (InterruptedException x){}
                }//end if
            }
        }

        public synchronized void returnSender(DataSender sender) {
            if ( !isOpen) {
                sender.disconnect();
                return;
            }
            //to do
            inuse.remove(sender);
            //just in case the limit has changed
            if ( notinuse.size() < this.getLimit() ) notinuse.add(sender);
            else try {sender.disconnect(); } catch ( Exception ignore){}
            notify();
        }

        public synchronized void close() {
            isOpen = false;
            Object[] unused = notinuse.toArray();
            Object[] used = inuse.toArray();
            for (int i = 0; i < unused.length; i++) {
                DataSender sender = (DataSender) unused[i];
                sender.disconnect();
            }//for
            for (int i = 0; i < used.length; i++) {
                DataSender sender = (DataSender) used[i];
                sender.disconnect();
            }//for
            notinuse.clear();
            inuse.clear();
            notify();
            


        }

        public synchronized void open() {
            isOpen = true;
            notify();
        }
    }
    
    public static void printArr(Object[] arr) {
        System.out.print("[");
        for (int i=0; i<arr.length; i++ ) {
            System.out.print(arr[i]);
            if ( (i+1)<arr.length )System.out.print(", ");
        }
        System.out.println("]");
    }

    
}