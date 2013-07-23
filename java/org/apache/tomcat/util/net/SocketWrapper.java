/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.util.net;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class SocketWrapper<E> {

    protected volatile E socket;

    protected volatile long lastAccess = -1;
    private long timeout = -1;
    protected boolean error = false;
    protected volatile int keepAliveLeft = 100;
    private boolean comet = false;
    protected boolean async = false;
    protected boolean keptAlive = false;
    private boolean upgraded = false;
    /*
     * Following cached for speed / reduced GC
     */
    private int localPort = -1;
    private String localName = null;
    private String localAddr = null;
    private int remotePort = -1;
    private String remoteHost = null;
    private String remoteAddr = null;
    /*
     * Used if block/non-blocking is set at the socket level. The client is
     * responsible for the thread-safe use of this field via the locks provided.
     */
    private volatile boolean blockingStatus = true;
    private final Lock blockingStatusReadLock;
    private final WriteLock blockingStatusWriteLock;

    /*
     * In normal servlet processing only one thread is allowed to access the
     * socket at a time. That is controlled by a lock on the socket for both
     * read and writes). When HTTP upgrade is used, one read thread and one
     * write thread are allowed to access the socket concurrently. In this case
     * the lock on the socket is used for reads and the lock below is used for
     * writes.
     */
    private final Object writeThreadLock = new Object();
    public Object getWriteThreadLock() { return writeThreadLock; }

    public SocketWrapper(E socket) {
        this.socket = socket;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock =lock.writeLock();
    }

    public E getSocket() {
        return socket;
    }

    public boolean isComet() { return comet; }
    public void setComet(boolean comet) { this.comet = comet; }
    public boolean isAsync() { return async; }
    public void setAsync(boolean async) { this.async = async; }
    public boolean isUpgraded() { return upgraded; }
    public void setUpgraded(boolean upgraded) { this.upgraded = upgraded; }
    public long getLastAccess() { return lastAccess; }
    public void access() { access(System.currentTimeMillis()); }
    public void access(long access) { lastAccess = access; }
    public void setTimeout(long timeout) {this.timeout = timeout;}
    public long getTimeout() {return this.timeout;}
    public boolean getError() { return error; }
    public void setError(boolean error) { this.error = error; }
    public void setKeepAliveLeft(int keepAliveLeft) { this.keepAliveLeft = keepAliveLeft;}
    public int decrementKeepAlive() { return (--keepAliveLeft);}
    public boolean isKeptAlive() {return keptAlive;}
    public void setKeptAlive(boolean keptAlive) {this.keptAlive = keptAlive;}
    public int getLocalPort() { return localPort; }
    public void setLocalPort(int localPort) {this.localPort = localPort; }
    public String getLocalName() { return localName; }
    public void setLocalName(String localName) {this.localName = localName; }
    public String getLocalAddr() { return localAddr; }
    public void setLocalAddr(String localAddr) {this.localAddr = localAddr; }
    public int getRemotePort() { return remotePort; }
    public void setRemotePort(int remotePort) {this.remotePort = remotePort; }
    public String getRemoteHost() { return remoteHost; }
    public void setRemoteHost(String remoteHost) {this.remoteHost = remoteHost; }
    public String getRemoteAddr() { return remoteAddr; }
    public void setRemoteAddr(String remoteAddr) {this.remoteAddr = remoteAddr; }
    public boolean getBlockingStatus() { return blockingStatus; }
    public void setBlockingStatus(boolean blockingStatus) {
        this.blockingStatus = blockingStatus;
    }
    public Lock getBlockingStatusReadLock() { return blockingStatusReadLock; }
    public WriteLock getBlockingStatusWriteLock() {
        return blockingStatusWriteLock;
    }
}
