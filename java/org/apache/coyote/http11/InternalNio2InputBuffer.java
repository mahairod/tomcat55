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
package org.apache.coyote.http11;

import java.io.EOFException;
import java.io.IOException;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.Nio2Channel;
import org.apache.tomcat.util.net.SocketWrapperBase;

/**
 * Output buffer implementation for NIO2.
 */
public class InternalNio2InputBuffer extends AbstractInputBuffer<Nio2Channel> {

    private static final Log log =
            LogFactory.getLog(InternalNio2InputBuffer.class);


    // ----------------------------------------------------------- Constructors

    public InternalNio2InputBuffer(Request request, int headerBufferSize) {
        super(request, headerBufferSize);
        inputStreamInputBuffer = new SocketInputBuffer();
    }


    // ----------------------------------------------------- Instance Variables

    private SocketWrapperBase<Nio2Channel> wrapper;


    // --------------------------------------------------------- Public Methods

    /**
     * Recycle the input buffer. This should be called when closing the
     * connection.
     */
    @Override
    public void recycle() {
        wrapper = null;
        super.recycle();
    }


    // ------------------------------------------------------ Protected Methods

    @Override
    protected final Log getLog() {
        return log;
    }


    @Override
    protected void init(SocketWrapperBase<Nio2Channel> socketWrapper,
            AbstractEndpoint<Nio2Channel> associatedEndpoint) throws IOException {

        wrapper = socketWrapper;

        int bufLength = headerBufferSize +
                wrapper.getSocket().getBufHandler().getReadBuffer().capacity();
        if (buf == null || buf.length < bufLength) {
            buf = new byte[bufLength];
        }
    }


    @Override
    protected boolean fill(boolean block) throws IOException, EOFException {

        if (parsingHeader) {
            if (lastValid > headerBufferSize) {
                throw new IllegalArgumentException
                    (sm.getString("iib.requestheadertoolarge.error"));
            }
        } else {
            lastValid = pos = end;
        }

        int nRead = wrapper.read(block, buf, pos, buf.length - pos);
        if (nRead > 0) {
            lastValid = pos + nRead;
            return true;
        }

        return false;
    }


    // ------------------------------------- InputStreamInputBuffer Inner Class

    /**
     * This class is an input buffer which will read its data from an input
     * stream.
     */
    protected class SocketInputBuffer implements InputBuffer {

        /**
         * Read bytes into the specified chunk.
         */
        @Override
        public int doRead(ByteChunk chunk, Request req ) throws IOException {

            if (pos >= lastValid) {
                if (!fill(true)) //read body, must be blocking, as the thread is inside the app
                    return -1;
            }

            int length = lastValid - pos;
            chunk.setBytes(buf, pos, length);
            pos = lastValid;

            return length;
        }
    }
}
