/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

public class WsFrameClient extends WsFrameBase {

    private final ByteBuffer response;
    private final AsynchronousSocketChannel channel;
    private final CompletionHandler<Integer,Void> handler;

    public WsFrameClient(ByteBuffer response, AsynchronousSocketChannel channel,
            WsSession wsSession) {
        super(wsSession);
        this.response = response;
        this.channel = channel;
        this.handler = new WsFrameClientCompletionHandler();

        try {
            processSocketRead();
        } catch (IOException e) {
            close(e);
        }
    }


    private void processSocketRead() throws IOException {

        while (response.hasRemaining()) {
            int remaining = response.remaining();

            int toCopy = Math.min(remaining, inputBuffer.length - writePos);

            // Copy remaining bytes read in HTTP phase to input buffer used by
            // frame processing
            response.get(inputBuffer, writePos, toCopy);
            writePos += toCopy;

            // Process the data we have
            processInputBuffer();
        }
        response.clear();

        // Get some more data
        channel.read(response, null, handler);
    }


    private final void close(Throwable t) {
        CloseReason cr;
        if (t instanceof WsIOException) {
            cr = ((WsIOException) t).getCloseReason();
        } else {
            cr = new CloseReason(
                CloseCodes.CLOSED_ABNORMALLY, t.getMessage());
        }

        try {
            wsSession.close(cr);
        } catch (IOException ignore) {
            // Ignore
        }
    }


    @Override
    protected boolean isMasked() {
        // Data is from the server so it is not masked
        return false;
    }


    private class WsFrameClientCompletionHandler
            implements CompletionHandler<Integer,Void> {

        @Override
        public void completed(Integer result, Void attachment) {
            response.flip();
            try {
                processSocketRead();
            } catch (IOException e) {
                close(e);
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            close(exc);
        }
    }
}
