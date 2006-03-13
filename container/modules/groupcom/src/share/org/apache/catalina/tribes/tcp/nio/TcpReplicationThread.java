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

package org.apache.catalina.tribes.tcp.nio;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.catalina.tribes.io.ObjectReader;
import org.apache.catalina.tribes.tcp.Constants;

/**
 * A worker thread class which can drain channels and echo-back the input. Each
 * instance is constructed with a reference to the owning thread pool object.
 * When started, the thread loops forever waiting to be awakened to service the
 * channel associated with a SelectionKey object. The worker is tasked by
 * calling its serviceChannel() method with a SelectionKey object. The
 * serviceChannel() method stores the key reference in the thread object then
 * calls notify() to wake it up. When the channel has been drained, the worker
 * thread returns itself to its parent pool.
 * 
 * @author Filip Hanik
 * 
 * @version $Revision: 378050 $, $Date: 2006-02-15 12:30:02 -0600 (Wed, 15 Feb 2006) $
 */
public class TcpReplicationThread extends WorkerThread {
    public static final int OPTION_SEND_ACK = 0x0001;
    public static final int OPTION_SYNCHRONIZED = 0x0002;
    public static final int OPTION_DIRECT_BUFFER = 0x0004;

    
    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog( TcpReplicationThread.class );
    private ByteBuffer buffer = null;
    private SelectionKey key;
    private int rxBufSize;
    public TcpReplicationThread ()
    {
    }

    // loop forever waiting for work to do
    public synchronized void run()
    {
        if ( (getOptions() & OPTION_DIRECT_BUFFER) == OPTION_DIRECT_BUFFER ) {
            buffer = ByteBuffer.allocateDirect(getRxBufSize());
        }else {
            buffer = ByteBuffer.allocate (getRxBufSize());
        }
        while (doRun) {
            try {
                // sleep and release object lock
                this.wait();
            } catch (InterruptedException e) {
                if(log.isInfoEnabled())
                    log.info("TCP worker thread interrupted in cluster",e);
                // clear interrupt status
                Thread.interrupted();
            }
            if (key == null) {
                continue;	// just in case
            }
            try {
                drainChannel (key);
            } catch (Exception e) {
                //this is common, since the sockets on the other
                //end expire after a certain time.
                if ( e instanceof IOException ) {
                    //dont spew out stack traces for IO exceptions unless debug is enabled.
                    if (log.isDebugEnabled()) log.debug ("IOException in replication worker, unable to drain channel. Probable cause: Keep alive socket closed.", e);
                    else log.warn ("IOException in replication worker, unable to drain channel. Probable cause: Keep alive socket closed.");
                } else if ( log.isErrorEnabled() ) {
                    //this is a real error, log it.
                    log.error("Exception caught in TcpReplicationThread.drainChannel.",e);
                } 

                // close channel and nudge selector
                try {
                    key.channel().close();
                } catch (IOException ex) {
                    log.error("Unable to close channel.",ex);
                }
                key.selector().wakeup();
            }
            key = null;
            // done, ready for more, return to pool
            this.pool.returnWorker (this);
        }
    }

    /**
     * Called to initiate a unit of work by this worker thread
     * on the provided SelectionKey object.  This method is
     * synchronized, as is the run() method, so only one key
     * can be serviced at a given time.
     * Before waking the worker thread, and before returning
     * to the main selection loop, this key's interest set is
     * updated to remove OP_READ.  This will cause the selector
     * to ignore read-readiness for this channel while the
     * worker thread is servicing it.
     */
    public synchronized void serviceChannel (SelectionKey key) {
        this.key = key;
        key.interestOps (key.interestOps() & (~SelectionKey.OP_READ));
        key.interestOps (key.interestOps() & (~SelectionKey.OP_WRITE));
        this.notify();		// awaken the thread
    }

    /**
     * The actual code which drains the channel associated with
     * the given key.  This method assumes the key has been
     * modified prior to invocation to turn off selection
     * interest in OP_READ.  When this method completes it
     * re-enables OP_READ and calls wakeup() on the selector
     * so the selector will resume watching this channel.
     */
    protected void drainChannel (SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        int count;
        buffer.clear();			// make buffer empty
        ObjectReader reader = (ObjectReader)key.attachment();
        // loop while data available, channel is non-blocking
        while ((count = channel.read (buffer)) > 0) {
            buffer.flip();		// make buffer readable
            if ( buffer.hasArray() ) 
                reader.append(buffer.array(),0,count,false);
            else 
                reader.append(buffer,count,false);
            
            
            buffer.clear();		// make buffer empty
        }
        
        int pkgcnt = reader.count();

        

        /**
         * Use send ack here if you want to ack the request to the remote 
         * server before completing the request
         * This is considered an asynchronized request
         */
        if (sendAckAsync()) {
            while ( pkgcnt > 0 ) {
                sendAck(key,channel);
                pkgcnt--;
            }
        }

        //check to see if any data is available
        pkgcnt = reader.execute();

        if (log.isTraceEnabled()) {
            log.trace("sending " + pkgcnt + " ack packages to " + channel.socket().getLocalPort() );
        }

        /**
         * Use send ack here if you want the request to complete on this 
         * server before sending the ack to the remote server
         * This is considered a synchronized request
         */
        if (sendAckSync()) {
            while ( pkgcnt > 0 ) {
                sendAck(key,channel);
                pkgcnt--;
            }
        }        

        
        if (count < 0) {
            // close channel on EOF, invalidates the key
            channel.close();
            return;
        }
        
        //acquire the interestOps mutex
        Object mutex = this.getPool().getInterestOpsMutex();
        synchronized (mutex) {
            // cycle the selector so this key is active again
            key.selector().wakeup();
            // resume interest in OP_READ, OP_WRITE
            int resumeOps = key.interestOps() | SelectionKey.OP_READ;
            key.interestOps(resumeOps);
        }
        
    }
    
    
    public boolean sendAckSync() {
        int options = getOptions();
        return ((OPTION_SEND_ACK & options) == OPTION_SEND_ACK) &&
               ((OPTION_SYNCHRONIZED & options) == OPTION_SYNCHRONIZED);
    }
    
    public boolean sendAckAsync() {
        int options = getOptions();
        return ((OPTION_SEND_ACK & options) == OPTION_SEND_ACK) &&
               ((OPTION_SYNCHRONIZED & options) != OPTION_SYNCHRONIZED);
    }


    /**
     * send a reply-acknowledgement (6,2,3)
     * @param key
     * @param channel
     */
    protected void sendAck(SelectionKey key, SocketChannel channel) {
        
        try {
            channel.write(ByteBuffer.wrap(Constants.ACK_COMMAND));
            if (log.isTraceEnabled()) {
                log.trace("ACK sent to " + channel.socket().getPort());
            }
        } catch ( java.io.IOException x ) {
            log.warn("Unable to send ACK back through channel, channel disconnected?: "+x.getMessage());
        }
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }
}
