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

package org.apache.catalina.tribes.tcp.bio;
import java.io.IOException;

import org.apache.catalina.tribes.io.ObjectReader;
import org.apache.catalina.tribes.tcp.Constants;
import org.apache.catalina.tribes.tcp.nio.WorkerThread;
import java.net.Socket;
import java.io.InputStream;
import org.apache.catalina.tribes.tcp.ReceiverBase;

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
    public static final int OPTION_SEND_ACK = ReceiverBase.OPTION_SEND_ACK;
    public static final int OPTION_SYNCHRONIZED = ReceiverBase.OPTION_SYNCHRONIZED;
    public static final int OPTION_DIRECT_BUFFER = ReceiverBase.OPTION_DIRECT_BUFFER;


    protected static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog( TcpReplicationThread.class );
    
    protected Socket socket;
    protected ObjectReader reader;
    
    public TcpReplicationThread ()
    {
    }

    // loop forever waiting for work to do
    public synchronized void run()
    {
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
            if ( this.socket != null ) {
                try {
                    drainSocket();
                } catch ( Exception x ) {
                    log.error("Unable to service bio socket");
                }finally {
                    try {reader.close();}catch ( Exception x){}
                    try {socket.close();}catch ( Exception x){}
                    reader = null;
                    socket = null;
                }
            }
            // done, ready for more, return to pool
            if ( this.pool != null ) this.pool.returnWorker (this);
            else doRun = false;
        }
    }

    
    public synchronized void serviceSocket(Socket socket, ObjectReader reader) {
        this.socket = socket;
        this.reader = reader;
        this.notify();		// awaken the thread
    }
    
    protected void execute(ObjectReader reader) throws Exception{
        int pkgcnt = reader.count();
        /**
         * Use send ack here if you want to ack the request to the remote 
         * server before completing the request
         * This is considered an asynchronized request
         */
        if (sendAckAsync()) {
            while ( pkgcnt > 0 ) {
                sendAck();
                pkgcnt--;
            }
        }
        //check to see if any data is available
        pkgcnt = reader.execute();

        /**
         * Use send ack here if you want the request to complete on this 
         * server before sending the ack to the remote server
         * This is considered a synchronized request
         */
        if (sendAckSync()) {
            while ( pkgcnt > 0 ) {
                sendAck();
                pkgcnt--;
            }
        }        
       
    }

    /**
     * The actual code which drains the channel associated with
     * the given key.  This method assumes the key has been
     * modified prior to invocation to turn off selection
     * interest in OP_READ.  When this method completes it
     * re-enables OP_READ and calls wakeup() on the selector
     * so the selector will resume watching this channel.
     */
    protected void drainSocket () throws Exception {
        InputStream in = socket.getInputStream();
        // loop while data available, channel is non-blocking
        byte[] buf = new byte[1024];
        int length = in.read(buf);
        while ( length >= 0 ) {
            int count = reader.append(buf,0,length,true);
            if ( count > 0 ) execute(reader);
            if ( in.available() == 0 && reader.bufferSize() == 0 ) length = -1;
            else length = in.read(buf);
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
    protected void sendAck() {
        try {
            this.socket.getOutputStream().write(Constants.ACK_COMMAND);
            if (log.isTraceEnabled()) {
                log.trace("ACK sent to " + socket.getPort());
            }
        } catch ( java.io.IOException x ) {
            log.warn("Unable to send ACK back through channel, channel disconnected?: "+x.getMessage());
        }
    }
}
