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
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.tomcat.util.buf.ByteBufferHolder;

public abstract class SocketWrapperBase<E> {

    private volatile E socket;
    private final AbstractEndpoint<E> endpoint;

    private volatile long lastAccess = System.currentTimeMillis();
    private volatile long lastAsyncStart = 0;
    private volatile long asyncTimeout = -1;
    private long timeout = -1;
    private IOException error = null;
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

    protected volatile ByteBuffer socketWriteBuffer;
    protected volatile boolean writeBufferFlipped;

    /**
     * For "non-blocking" writes use an external set of buffers. Although the
     * API only allows one non-blocking write at a time, due to buffering and
     * the possible need to write HTTP headers, there may be more than one write
     * to the OutputBuffer.
     */
    protected final LinkedBlockingDeque<ByteBufferHolder> bufferedWrites =
            new LinkedBlockingDeque<>();

    /**
     * The max size of the buffered write buffer
     */
    protected int bufferedWriteSize = 64*1024; //64k default write buffer

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
    public void setAsyncTimeout(long timeout) {
        asyncTimeout = timeout;
    }
    public long getAsyncTimeout() {
        return asyncTimeout;
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
    public IOException getError() { return error; }
    public void setError(IOException error) { this.error = error; }
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

    protected boolean hasMoreDataToFlush() {
        return (writeBufferFlipped && socketWriteBuffer.remaining() > 0) ||
        (!writeBufferFlipped && socketWriteBuffer.position() > 0);
    }

    public boolean hasDataToWrite() {
        return hasMoreDataToFlush() || bufferedWrites.size() > 0;
    }

    /**
     * Checks to see if there are any writes pending and if there are calls
     * {@link #registerWriteInterest()} to trigger a callback once the pending
     * writes have completed.
     * <p>
     * Note: Once this method has returned <code>false</code> it <b>MUST NOT</b>
     *       be called again until the pending write has completed and the
     *       callback has been fired.
     *       TODO: Modify {@link #registerWriteInterest()} so the above
     *       restriction is enforced there rather than relying on the caller.
     *
     * @return <code>true</code> if no writes are pending and data can be
     *         written otherwise <code>false</code>
     */
    public boolean isReadyForWrite() {
        boolean result = canWrite();
        if (!result) {
            registerWriteInterest();
        }
        return result;
    }


    public boolean canWrite() {
        return !writeBufferFlipped && socketWriteBuffer.hasRemaining() &&
                bufferedWrites.size() == 0;
    }

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
        error = null;
        keepAliveLeft = 100;
        lastAccess = System.currentTimeMillis();
        lastAsyncStart = 0;
        asyncTimeout = -1;
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


    /**
     * Writes the provided data to the socket, buffering any remaining data if
     * used in non-blocking mode.
     *
     * @param block <code>true<code> if a blocking write should be used,
     *                  otherwise a non-blocking write will be used
     * @param buf   The byte array containing the data to be written
     * @param off   The offset within the byte array of the data to be written
     * @param len   The length of the data to be written
     *
     * @throws IOException If an IO error occurs during the write
     */
    public void write(boolean block, byte[] buf, int off, int len) throws IOException {
        if (len == 0 || buf == null || getSocket() == null) {
            return;
        }

        // While the implementations for blocking and non-blocking writes are
        // very similar they have been split into separate methods to allow
        // sub-classes to override them individually. NIO2, for example,
        // overrides the non-blocking write but not the blocking write.
        if (block) {
            writeBlocking(buf, off, len);
        } else {
            writeNonBlocking(buf, off, len);
        }

        // Prevent timeouts
        access();
    }


    /**
     * Transfers the data to the socket write buffer (writing that data to the
     * socket if the buffer fills up using a blocking write) until all the data
     * has been transferred and space remains in the socket write buffer.
     *
     * @param buf   The byte array containing the data to be written
     * @param off   The offset within the byte array of the data to be written
     * @param len   The length of the data to be written
     *
     * @throws IOException If an IO error occurs during the write
     */
    protected void writeBlocking(byte[] buf, int off, int len) throws IOException {
        // Note: There is an implementation assumption that if the switch from
        //       non-blocking to blocking has been made then any pending
        //       non-blocking writes were flushed at the time the switch
        //       occurred.

        // Keep writing until all the data has been transferred to the socket
        // write buffer and space remains in that buffer
        int thisTime = transfer(buf, off, len, socketWriteBuffer);
        while (socketWriteBuffer.remaining() == 0) {
            len = len - thisTime;
            off = off + thisTime;
            doWrite(true);
            thisTime = transfer(buf, off, len, socketWriteBuffer);
        }
    }


    /**
     * Transfers the data to the socket write buffer (writing that data to the
     * socket if the buffer fills up using a non-blocking write) until either
     * all the data has been transferred and space remains in the socket write
     * buffer or a non-blocking write leaves data in the socket write buffer.
     *
     * @param buf   The byte array containing the data to be written
     * @param off   The offset within the byte array of the data to be written
     * @param len   The length of the data to be written
     *
     * @throws IOException If an IO error occurs during the write
     */
    protected void writeNonBlocking(byte[] buf, int off, int len) throws IOException {
        if (!writeBufferFlipped) {
            int thisTime = transfer(buf, off, len, socketWriteBuffer);
            len = len - thisTime;
            while (socketWriteBuffer.remaining() == 0) {
                off = off + thisTime;
                doWrite(false);
                if (writeBufferFlipped) {
                    thisTime = 0;
                } else {
                    thisTime = transfer(buf, off, len, socketWriteBuffer);
                }
                len = len - thisTime;
            }
        }

        if (len > 0) {
            // Remaining data must be buffered
            addToBuffers(buf, off, len);
        }
    }


    /**
     * Writes as much data as possible from any that remains in the buffers.
     *
     * @param block <code>true<code> if a blocking write should be used,
     *                  otherwise a non-blocking write will be used
     *
     * @return <code>true</code> if data remains to be flushed after this method
     *         completes, otherwise <code>false</code>. In blocking mode
     *         therefore, the return value should always be <code>false</code>
     *
     * @throws IOException If an IO error occurs during the write
     */
    public boolean flush(boolean block) throws IOException {
        if (getSocket() == null) {
            return false;
        }

        if (getError() != null) {
            throw getError();
        }

        boolean result = false;
        if (block) {
            // A blocking flush will always empty the buffer.
            flushBlocking();
        } else {
            result = flushNonBlocking();
        }

        // Prevent timeouts
        access();

        return result;
    }


    protected void flushBlocking() throws IOException {
        doWrite(true);

        if (bufferedWrites.size() > 0) {
            Iterator<ByteBufferHolder> bufIter = bufferedWrites.iterator();
            while (!hasMoreDataToFlush() && bufIter.hasNext()) {
                ByteBufferHolder buffer = bufIter.next();
                buffer.flip();
                while (!hasMoreDataToFlush() && buffer.getBuf().remaining()>0) {
                    transfer(buffer.getBuf(), socketWriteBuffer);
                    if (buffer.getBuf().remaining() == 0) {
                        bufIter.remove();
                    }
                    doWrite(true);
                }
            }
        }

    }


    protected boolean flushNonBlocking() throws IOException {
        boolean dataLeft = hasMoreDataToFlush();

        // Write to the socket, if there is anything to write
        if (dataLeft) {
            doWrite(false);
        }

        dataLeft = hasMoreDataToFlush();

        if (!dataLeft && bufferedWrites.size() > 0) {
            Iterator<ByteBufferHolder> bufIter = bufferedWrites.iterator();
            while (!hasMoreDataToFlush() && bufIter.hasNext()) {
                ByteBufferHolder buffer = bufIter.next();
                buffer.flip();
                while (!hasMoreDataToFlush() && buffer.getBuf().remaining() > 0) {
                    transfer(buffer.getBuf(), socketWriteBuffer);
                    if (buffer.getBuf().remaining() == 0) {
                        bufIter.remove();
                    }
                    doWrite(false);
                }
            }
        }

        return hasMoreDataToFlush();
    }


    /**
     * Write the contents of the socketWriteBuffer to the socket. For blocking
     * writes either then entire contents of the buffer will be written or an
     * IOException will be thrown. Partial blocking writes will not occur.
     *
     * @param block Should the write be blocking or not?
     *
     * @throws IOException If an I/O error such as a timeout occurs during the
     *                     write
     */
    protected abstract void doWrite(boolean block) throws IOException;


    protected void addToBuffers(byte[] buf, int offset, int length) {
        ByteBufferHolder holder = bufferedWrites.peekLast();
        if (holder==null || holder.isFlipped() || holder.getBuf().remaining()<length) {
            ByteBuffer buffer = ByteBuffer.allocate(Math.max(bufferedWriteSize,length));
            holder = new ByteBufferHolder(buffer,false);
            bufferedWrites.add(holder);
        }
        holder.getBuf().put(buf,offset,length);
    }

    public abstract void registerWriteInterest();

    public abstract void regsiterForEvent(boolean read, boolean write);


    // --------------------------------------------------------- Utility methods

    protected static int transfer(byte[] from, int offset, int length, ByteBuffer to) {
        int max = Math.min(length, to.remaining());
        to.put(from, offset, max);
        return max;
    }

    protected static void transfer(ByteBuffer from, ByteBuffer to) {
        int max = Math.min(from.remaining(), to.remaining());
        int fromLimit = from.limit();
        from.limit(from.position() + max);
        to.put(from);
        from.limit(fromLimit);
    }
}
