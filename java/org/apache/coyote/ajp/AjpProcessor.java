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
package org.apache.coyote.ajp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.coyote.ActionCode;
import org.apache.coyote.RequestInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.net.SocketWrapper;


/**
 * Processes AJP requests.
 *
 * @author Remy Maucherat
 * @author Henri Gomez
 * @author Dan Milstein
 * @author Keith Wannamaker
 * @author Kevin Seguin
 * @author Costin Manolache
 * @author Bill Barker
 */
public class AjpProcessor extends AbstractAjpProcessor<Socket> {


    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(AjpProcessor.class);
    @Override
    protected Log getLog() {
        return log;
    }

    // ----------------------------------------------------------- Constructors


    public AjpProcessor(int packetSize, JIoEndpoint endpoint) {

        super(packetSize, endpoint);

        response.setOutputBuffer(new SocketOutputBuffer());
    }


    // ----------------------------------------------------- Instance Variables

    /**
     * Input stream.
     */
    protected InputStream input;


    /**
     * Output stream.
     */
    protected OutputStream output;


    // --------------------------------------------------------- Public Methods


    /**
     * Process pipelined HTTP requests using the specified input and output
     * streams.
     *
     * @throws IOException error during an I/O operation
     */
    @Override
    public SocketState process(SocketWrapper<Socket> socket)
        throws IOException {
        RequestInfo rp = request.getRequestProcessor();
        rp.setStage(org.apache.coyote.Constants.STAGE_PARSE);

        // Setting up the socket
        this.socketWrapper = socket;
        input = socket.getSocket().getInputStream();
        output = socket.getSocket().getOutputStream();
        int soTimeout = -1;
        if (keepAliveTimeout > 0) {
            soTimeout = socket.getSocket().getSoTimeout();
        }
        boolean cping = false;

        // Error flag
        error = false;

        while (!error && !endpoint.isPaused()) {
            // Parsing the request header
            try {
                // Get first message of the request
                if (!readMessage(requestHeaderMessage, true)) {
                    // This means a connection timeout
                    break;
                }
                // Set back timeout if keep alive timeout is enabled
                if (keepAliveTimeout > 0) {
                    socket.getSocket().setSoTimeout(soTimeout);
                }
                // Check message type, process right away and break if
                // not regular request processing
                int type = requestHeaderMessage.getByte();
                if (type == Constants.JK_AJP13_CPING_REQUEST) {
                    if (endpoint.isPaused()) {
                        recycle(true);
                        break;
                    }
                    cping = true;
                    try {
                        output.write(pongMessageArray);
                    } catch (IOException e) {
                        error = true;
                    }
                    continue;
                } else if(type != Constants.JK_AJP13_FORWARD_REQUEST) {
                    // Unexpected packet type. Unread body packets should have
                    // been swallowed in finish().
                    if (log.isDebugEnabled()) {
                        log.debug("Unexpected message: " + type);
                    }
                    error = true;
                    break;
                }
                request.setStartTime(System.currentTimeMillis());
            } catch (IOException e) {
                error = true;
                break;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                log.debug(sm.getString("ajpprocessor.header.error"), t);
                // 400 - Bad Request
                response.setStatus(400);
                getAdapter().log(request, response, 0);
                error = true;
            }

            if (!error) {
                // Setting up filters, and parse some request headers
                rp.setStage(org.apache.coyote.Constants.STAGE_PREPARE);
                try {
                    prepareRequest();
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    log.debug(sm.getString("ajpprocessor.request.prepare"), t);
                    // 400 - Internal Server Error
                    response.setStatus(400);
                    getAdapter().log(request, response, 0);
                    error = true;
                }
            }

            if (!error && !cping && endpoint.isPaused()) {
                // 503 - Service unavailable
                response.setStatus(503);
                getAdapter().log(request, response, 0);
                error = true;
            }
            cping = false;

            // Process the request in the adapter
            if (!error) {
                try {
                    rp.setStage(org.apache.coyote.Constants.STAGE_SERVICE);
                    getAdapter().service(request, response);
                } catch (InterruptedIOException e) {
                    error = true;
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    log.error(sm.getString("ajpprocessor.request.process"), t);
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    getAdapter().log(request, response, 0);
                    error = true;
                }
            }

            if (isAsync() && !error) {
                break;
            }

            // Finish the response if not done yet
            if (!finished) {
                try {
                    finish();
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    error = true;
                }
            }

            // If there was an error, make sure the request is counted as
            // and error, and update the statistics counter
            if (error) {
                response.setStatus(500);
            }
            request.updateCounters();

            rp.setStage(org.apache.coyote.Constants.STAGE_KEEPALIVE);
            // Set keep alive timeout if enabled
            if (keepAliveTimeout > 0) {
                socket.getSocket().setSoTimeout(keepAliveTimeout);
            }

            recycle(false);
        }

        rp.setStage(org.apache.coyote.Constants.STAGE_ENDED);

        if (isAsync() && !error && !endpoint.isPaused()) {
            return SocketState.LONG;
        } else {
            input = null;
            output = null;
            return SocketState.CLOSED;
        }

    }

    @Override
    public void recycle(boolean socketClosing) {
        super.recycle(socketClosing);
        if (socketClosing) {
            input = null;
            output = null;
        }
    }

    // ----------------------------------------------------- ActionHook Methods


    /**
     * Send an action to the connector.
     *
     * @param actionCode Type of the action
     * @param param Action parameter
     */
    @Override
    protected void actionInternal(ActionCode actionCode, Object param) {

        if (actionCode == ActionCode.ASYNC_COMPLETE) {
            if (asyncStateMachine.asyncComplete()) {
                ((JIoEndpoint)endpoint).processSocketAsync(this.socketWrapper,
                        SocketStatus.OPEN_READ);
            }
        } else if (actionCode == ActionCode.ASYNC_SETTIMEOUT) {
            if (param == null) return;
            long timeout = ((Long)param).longValue();
            // if we are not piggy backing on a worker thread, set the timeout
            socketWrapper.setTimeout(timeout);
        } else if (actionCode == ActionCode.ASYNC_DISPATCH) {
            if (asyncStateMachine.asyncDispatch()) {
                ((JIoEndpoint)endpoint).processSocketAsync(this.socketWrapper,
                        SocketStatus.OPEN_READ);
            }
        }
    }


    // ------------------------------------------------------ Protected Methods

    @Override
    protected void output(byte[] src, int offset, int length)
            throws IOException {
        output.write(src, offset, length);
    }


    /**
     * Read at least the specified amount of bytes, and place them
     * in the input buffer.
     */
    protected boolean read(byte[] buf, int pos, int n)
        throws IOException {

        int read = 0;
        int res = 0;
        while (read < n) {
            res = input.read(buf, read + pos, n - read);
            if (res > 0) {
                read += res;
            } else {
                throw new IOException(sm.getString("ajpprocessor.failedread"));
            }
        }

        return true;

    }


    /**
     * Read an AJP message.
     *
     * @param message   The message to populate
     * @param ignored   Not used in BIO
     * @return true if the message has been read, false if the short read
     *         didn't return anything
     * @throws IOException any other failure, including incomplete reads
     */
    @Override
    protected boolean readMessage(AjpMessage message, boolean ignored)
        throws IOException {

        byte[] buf = message.getBuffer();
        int headerLength = message.getHeaderLength();

        read(buf, 0, headerLength);

        int messageLength = message.processHeader(true);
        if (messageLength < 0) {
            // Invalid AJP header signature
            throw new IOException(sm.getString("ajpmessage.invalidLength",
                    Integer.valueOf(messageLength)));
        }
        else if (messageLength == 0) {
            // Zero length message.
            return true;
        }
        else {
            if (messageLength > buf.length) {
                // Message too long for the buffer
                // Need to trigger a 400 response
                throw new IllegalArgumentException(sm.getString(
                        "ajpprocessor.header.tooLong",
                        Integer.valueOf(messageLength),
                        Integer.valueOf(buf.length)));
            }
            read(buf, headerLength, messageLength);
            return true;
        }
    }
}
