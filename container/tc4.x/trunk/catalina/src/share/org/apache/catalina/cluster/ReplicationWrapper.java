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

package org.apache.catalina.cluster;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * A ReplicationWrapper, used when sending and receiving multicast
 * data, wrapped is the data and the senderId which is used for
 * identification.
 *
 * @author Bip Thelin
 * @version $Revision$, $Date$
 */
public final class ReplicationWrapper implements Serializable {

    /**
     * Our buffer to hold the stream
     */
    private byte[] _buf = null;

    /**
     * Our sender Id
     */
    private String senderId = null;

    /**
     * Construct a new ReplicationWrapper
     *
     */
    public ReplicationWrapper(byte[] b, String senderId) {
        this.senderId = senderId;
        _buf = b;
    }

    /**
     * Write our stream to the <code>OutputStream</code> provided.
     *
     * @param out the OutputStream to write this stream to
     * @exception IOException if an input/output error occurs
     */
    public final void writeTo(OutputStream out) throws IOException {
        out.write(_buf);
    }

    /**
     * return our internal data as a array of bytes
     *
     * @return a our data
     */
    public final byte[] getDataStream() {
        return(_buf);
    }

    /**
     * Set the sender id for this wrapper
     *
     * @param senderId The sender id
     */
    public final void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * get the sender id for this wrapper
     *
     * @return The sender Id associated with this wrapper
     */
    public final String getSenderId() {
        return(this.senderId);
    }
}
