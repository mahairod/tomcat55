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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public abstract class SocketWrapperBase<E> {

    private volatile E socket;
    private final AbstractEndpoint<E> endpoint;

    private volatile long lastAccess = System.currentTimeMillis();
    private volatile long lastAsyncStart = 0;
    private long timeout = -1;
    private boolean error = false;
    private volatile int keepAliveLeft = 100;
    private volatile boolean async = false;
    private boolean keptAlive = false;
    private volatile boolean upgraded = false;
    private boolean secure = false;
    /*
     * Following cached for speed / reduced GC
     */
    private String localAddr = null;
    private String localName = null;
    private int localPort = -1;
    private String remoteAddr = null;
    private String remoteHost = null;
    private int remotePort = -1;
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

    private Set<DispatchType> dispatches = new CopyOnWriteArraySet<>();

    public SocketWrapperBase(E socket, AbstractEndpoint<E> endpoint) {
        this.socket = socket;
        this.endpoint = endpoint;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock = lock.writeLock();
    }

    public E getSocket() {
        return socket;
    }

    public AbstractEndpoint<E> getEndpoint() {
        return endpoint;
    }

    public boolean isAsync() { return async; }
    /**
     * Sets the async flag for this connection. If this call causes the
     * connection to transition from non-async to async then the lastAsyncStart
     * property will be set using the current time. This property is used as the
     * start time when calculating the async timeout. As per the Servlet spec
     * the async timeout applies once the dispatch where startAsync() was called
     * has returned to the container (which is when this method is currently
     * called).
     *
     * @param async The new value of for the async flag
     */
    public void setAsync(boolean async) {
        if (!this.async && async) {
            lastAsyncStart = System.currentTimeMillis();
        }
        this.async = async;
    }
    /**
     * Obtain the time that this connection last transitioned to async
     * processing.
     *
     * @return The time (as returned by {@link System#currentTimeMillis()}) that
     *         this connection last transitioned to async
     */
    public long getLastAsyncStart() {
       return lastAsyncStart;
    }
    public boolean isUpgraded() { return upgraded; }
    public void setUpgraded(boolean upgraded) { this.upgraded = upgraded; }
    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }
    public long getLastAccess() { return lastAccess; }
    public void access() {
        access(System.currentTimeMillis());
    }
    void access(long access) { lastAccess = access; }
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
    public Object getWriteThreadLock() { return writeThreadLock; }
    public void addDispatch(DispatchType dispatchType) {
        synchronized (dispatches) {
            dispatches.add(dispatchType);
        }
    }
    public Iterator<DispatchType> getIteratorAndClearDispatches() {
        // Note: Logic in AbstractProtocol depends on this method only returning
        // a non-null value if the iterator is non-empty. i.e. it should never
        // return an empty iterator.
        Iterator<DispatchType> result;
        synchronized (dispatches) {
            // Synchronized as the generation of the iterator and the clearing
            // of dispatches needs to be an atomic operation.
            result = dispatches.iterator();
            if (result.hasNext()) {
                dispatches.clear();
            } else {
                result = null;
            }
        }
        return result;
    }
    public void clearDispatches() {
        synchronized (dispatches) {
            dispatches.clear();
        }
    }

    public void reset(E socket, long timeout) {
        async = false;
        blockingStatus = true;
        dispatches.clear();
        error = false;
        keepAliveLeft = 100;
        lastAccess = System.currentTimeMillis();
        lastAsyncStart = 0;
        localAddr = null;
        localName = null;
        localPort = -1;
        remoteAddr = null;
        remoteHost = null;
        remotePort = -1;
        this.socket = socket;
        this.timeout = timeout;
        upgraded = false;
    }

    /**
     * Overridden for debug purposes. No guarantees are made about the format of
     * this message which may vary significantly between point releases.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ":" + String.valueOf(socket);
    }


    public abstract int read(boolean block, byte[] b, int off, int len) throws IOException;
    public abstract boolean isReady() throws IOException;
    /**
     * Return input that has been read to the input buffer for re-reading by the
     * correct component. There are times when a component may read more data
     * than it needs before it passes control to another component. One example
     * of this is during HTTP upgrade. If an (arguably misbehaving client) sends
     * data associated with the upgraded protocol before the HTTP upgrade
     * completes, the HTTP handler may read it. This method provides a way for
     * that data to be returned so it can be processed by the correct component.
     *
     * @param input The input to return to the input buffer.
     */
    public abstract void unRead(ByteBuffer input);
    public abstract void close() throws IOException;

    public abstract int write(boolean block, byte[] b, int off, int len) throws IOException;
    public abstract void flush() throws IOException;
}
