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
package org.apache.coyote.http11.upgrade;

import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public abstract class UpgradeServletInputStream extends ServletInputStream {

    // Start in blocking-mode
    private volatile Boolean ready = Boolean.TRUE;
    private volatile ReadListener listener = null;


    @Override
    public final boolean isFinished() {
        // The only way to finish an HTTP Upgrade connection is to close the
        // socket.
        return false;
    }


    @Override
    public final boolean isReady() {
        // If we already know the current state, return it.
        if (ready != null) {
            return ready.booleanValue();
        }

        try {
            ready = Boolean.valueOf(doIsReady());
        } catch (IOException e) {
            listener.onError(e);
        }
        return ready.booleanValue();
    }


    @Override
    public final void setReadListener(ReadListener listener) {
        if (listener == null) {
            // TODO i18n
            throw new IllegalArgumentException();
        }
        this.listener = listener;
        // Switching to non-blocking. Don't know if data is available.
        ready = null;
    }


    @Override
    public final int read() throws IOException {
        preReadChecks();

        return readInternal();
    }


    @Override
    public final int readLine(byte[] b, int off, int len) throws IOException {
        preReadChecks();

        if (len <= 0) {
            return 0;
        }
        int count = 0, c;

        while ((c = readInternal()) != -1) {
            b[off++] = (byte) c;
            count++;
            if (c == '\n' || count == len) {
                break;
            }
        }
        return count > 0 ? count : -1;
    }


    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        preReadChecks();

        return doRead(listener == null, b, off, len);
    }


    private void preReadChecks() {
        if (listener != null && (ready == null || !ready.booleanValue())) {
            // TODO i18n
            throw new IllegalStateException();
        }
        // No longer know if data is available
        ready = null;
    }


    private int readInternal() throws IOException {
        // Single byte reads for non-blocking need special handling so all
        // single byte reads run through this method.
        ReadListener readListener = this.listener;
        byte[] b = new byte[1];
        int result = doRead(readListener == null, b, 0, 1);
        if (result == 0) {
            return -1;
        } else if (result == -1) {
            // Will never happen with a network socket. An IOException will be
            // thrown when the client closes the connection.
            // Echo back the -1 to be safe.
            return -1;
        } else {
            return b[0] & 0xFF;
        }
    }


    protected final void onDataAvailable() {
        ready = Boolean.TRUE;
        listener.onDataAvailable();
    }


    protected abstract boolean doIsReady() throws IOException;

    protected abstract int doRead(boolean block, byte[] b, int off, int len)
            throws IOException;
}
