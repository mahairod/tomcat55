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
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.RequestDispatcher;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.Nio2Channel;
import org.apache.tomcat.util.net.Nio2Endpoint;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.net.SocketWrapper;

/**
 * Output buffer implementation for NIO2.
 */
public class InternalNio2InputBuffer extends AbstractInputBuffer<Nio2Channel> {

    private static final Log log =
            LogFactory.getLog(InternalNio2InputBuffer.class);

    // -------------------------------------------------------------- Constants

    enum HeaderParseStatus {
        DONE, HAVE_MORE_HEADERS, NEED_MORE_DATA
    }

    enum HeaderParsePosition {
        /**
         * Start of a new header. A CRLF here means that there are no more
         * headers. Any other character starts a header name.
         */
        HEADER_START,
        /**
         * Reading a header name. All characters of header are HTTP_TOKEN_CHAR.
         * Header name is followed by ':'. No whitespace is allowed.<br />
         * Any non-HTTP_TOKEN_CHAR (this includes any whitespace) encountered
         * before ':' will result in the whole line being ignored.
         */
        HEADER_NAME,
        /**
         * Skipping whitespace before text of header value starts, either on the
         * first line of header value (just after ':') or on subsequent lines
         * when it is known that subsequent line starts with SP or HT.
         */
        HEADER_VALUE_START,
        /**
         * Reading the header value. We are inside the value. Either on the
         * first line or on any subsequent line. We come into this state from
         * HEADER_VALUE_START after the first non-SP/non-HT byte is encountered
         * on the line.
         */
        HEADER_VALUE,
        /**
         * Before reading a new line of a header. Once the next byte is peeked,
         * the state changes without advancing our position. The state becomes
         * either HEADER_VALUE_START (if that first byte is SP or HT), or
         * HEADER_START (otherwise).
         */
        HEADER_MULTI_LINE,
        /**
         * Reading all bytes until the next CRLF. The line is being ignored.
         */
        HEADER_SKIPLINE
    }

    // ----------------------------------------------------------- Constructors


    /**
     * Alternate constructor.
     */
    public InternalNio2InputBuffer(Request request, int headerBufferSize) {

        this.request = request;
        headers = request.getMimeHeaders();

        this.headerBufferSize = headerBufferSize;

        inputStreamInputBuffer = new SocketInputBuffer();

        filterLibrary = new InputFilter[0];
        activeFilters = new InputFilter[0];
        lastActiveFilter = -1;

        parsingHeader = true;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerParsePos = HeaderParsePosition.HEADER_START;
        headerData.recycle();
        swallowInput = true;

    }

    /**
     * Parsing state - used for non blocking parsing so that
     * when more data arrives, we can pick up where we left off.
     */
    private boolean parsingRequestLine;
    private int parsingRequestLinePhase = 0;
    private boolean parsingRequestLineEol = false;
    private int parsingRequestLineStart = 0;
    private int parsingRequestLineQPos = -1;
    private HeaderParsePosition headerParsePos;

    /**
     * Underlying socket.
     */
    private SocketWrapper<Nio2Channel> socket;

    /**
     * Maximum allowed size of the HTTP request line plus headers plus any
     * leading blank lines.
     */
    private final int headerBufferSize;

    /**
     * Known size of the NioChannel read buffer.
     */
    private int socketReadBufferSize;

    /**
     * The completion handler used for asynchronous read operations
     */
    private CompletionHandler<Integer, SocketWrapper<Nio2Channel>> completionHandler;

    /**
     * The associated endpoint.
     */
    protected AbstractEndpoint<Nio2Channel> endpoint = null;

    /**
     * Read pending flag.
     */
    protected volatile boolean readPending = false;

    /**
     * Exception that occurred during writing.
     */
    protected IOException e = null;

    /**
     * Track if the byte buffer is flipped
     */
    protected volatile boolean flipped = false;

    // --------------------------------------------------------- Public Methods

    @Override
    protected final Log getLog() {
        return log;
    }


    /**
     * Recycle the input buffer. This should be called when closing the
     * connection.
     */
    @Override
    public void recycle() {
        super.recycle();
        socket = null;
        headerParsePos = HeaderParsePosition.HEADER_START;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerData.recycle();
        readPending = false;
        flipped = false;
        e = null;
    }


    /**
     * End processing of current HTTP request.
     * Note: All bytes of the current request should have been already
     * consumed. This method only resets all the pointers so that we are ready
     * to parse the next HTTP request.
     */
    @Override
    public void nextRequest() {
        super.nextRequest();
        headerParsePos = HeaderParsePosition.HEADER_START;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerData.recycle();
    }

    /**
     * Read the request line. This function is meant to be used during the
     * HTTP request header parsing. Do NOT attempt to read the request body
     * using it.
     *
     * @throws IOException If an exception occurs during the underlying socket
     * read operations, or if the given buffer is not big enough to accommodate
     * the whole line.
     * @return true if data is properly fed; false if no data is available
     * immediately and thread should be freed
     */
    @Override
    public boolean parseRequestLine(boolean useAvailableDataOnly)
        throws IOException {

        //check state
        if ( !parsingRequestLine ) return true;
        //
        // Skipping blank lines
        //
        if ( parsingRequestLinePhase < 2 ) {
            byte chr = 0;
            do {

                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (useAvailableDataOnly) {
                        return false;
                    }
                    // Do a simple read with a short timeout
                    if (!fill(false)) {
                        // A read is pending, so no longer in initial state
                        parsingRequestLinePhase = 1;
                        return false;
                    }
                }
                chr = buf[pos++];
            } while ((chr == Constants.CR) || (chr == Constants.LF));
            pos--;

            parsingRequestLineStart = pos;
            parsingRequestLinePhase = 2;
            if (log.isDebugEnabled()) {
                log.debug("Received ["
                        + new String(buf, pos, lastValid - pos,
                                StandardCharsets.ISO_8859_1)
                        + "]");
            }
        }
        if ( parsingRequestLinePhase == 2 ) {
            //
            // Reading the method name
            // Method name is always US-ASCII
            //
            boolean space = false;
            while (!space) {
                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill(false)) //request line parsing
                        return false;
                }
                // Spec says no CR or LF in method name
                if (buf[pos] == Constants.CR || buf[pos] == Constants.LF) {
                    throw new IllegalArgumentException(
                            sm.getString("iib.invalidmethod"));
                }
                if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                    space = true;
                    request.method().setBytes(buf, parsingRequestLineStart, pos - parsingRequestLineStart);
                }
                pos++;
            }
            parsingRequestLinePhase = 3;
        }
        if ( parsingRequestLinePhase == 3 ) {
            // Spec says single SP but also be tolerant of multiple and/or HT
            boolean space = true;
            while (space) {
                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill(false)) //request line parsing
                        return false;
                }
                if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                    pos++;
                } else {
                    space = false;
                }
            }
            parsingRequestLineStart = pos;
            parsingRequestLinePhase = 4;
        }
        if (parsingRequestLinePhase == 4) {
            // Mark the current buffer position

            int end = 0;
            //
            // Reading the URI
            //
            boolean space = false;
            while (!space) {
                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill(false)) //request line parsing
                        return false;
                }
                if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                    space = true;
                    end = pos;
                } else if ((buf[pos] == Constants.CR)
                           || (buf[pos] == Constants.LF)) {
                    // HTTP/0.9 style request
                    parsingRequestLineEol = true;
                    space = true;
                    end = pos;
                } else if ((buf[pos] == Constants.QUESTION)
                           && (parsingRequestLineQPos == -1)) {
                    parsingRequestLineQPos = pos;
                }
                pos++;
            }
            request.unparsedURI().setBytes(buf, parsingRequestLineStart, end - parsingRequestLineStart);
            if (parsingRequestLineQPos >= 0) {
                request.queryString().setBytes(buf, parsingRequestLineQPos + 1,
                                               end - parsingRequestLineQPos - 1);
                request.requestURI().setBytes(buf, parsingRequestLineStart, parsingRequestLineQPos - parsingRequestLineStart);
            } else {
                request.requestURI().setBytes(buf, parsingRequestLineStart, end - parsingRequestLineStart);
            }
            parsingRequestLinePhase = 5;
        }
        if ( parsingRequestLinePhase == 5 ) {
            // Spec says single SP but also be tolerant of multiple and/or HT
            boolean space = true;
            while (space) {
                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill(false)) //request line parsing
                        return false;
                }
                if (buf[pos] == Constants.SP || buf[pos] == Constants.HT) {
                    pos++;
                } else {
                    space = false;
                }
            }
            parsingRequestLineStart = pos;
            parsingRequestLinePhase = 6;

            // Mark the current buffer position
            end = 0;
        }
        if (parsingRequestLinePhase == 6) {
            //
            // Reading the protocol
            // Protocol is always US-ASCII
            //
            while (!parsingRequestLineEol) {
                // Read new bytes if needed
                if (pos >= lastValid) {
                    if (!fill(false)) //request line parsing
                        return false;
                }

                if (buf[pos] == Constants.CR) {
                    end = pos;
                } else if (buf[pos] == Constants.LF) {
                    if (end == 0)
                        end = pos;
                    parsingRequestLineEol = true;
                }
                pos++;
            }

            if ( (end - parsingRequestLineStart) > 0) {
                request.protocol().setBytes(buf, parsingRequestLineStart, end - parsingRequestLineStart);
            } else {
                request.protocol().setString("");
            }
            parsingRequestLine = false;
            parsingRequestLinePhase = 0;
            parsingRequestLineEol = false;
            parsingRequestLineStart = 0;
            return true;
        }
        throw new IllegalStateException("Invalid request line parse phase:"+parsingRequestLinePhase);
    }

    private void expand(int newsize) {
        if ( newsize > buf.length ) {
            if (parsingHeader) {
                throw new IllegalArgumentException(
                        sm.getString("iib.requestheadertoolarge.error"));
            }
            // Should not happen
            log.warn("Expanding buffer size. Old size: " + buf.length
                    + ", new size: " + newsize, new Exception());
            byte[] tmp = new byte[newsize];
            System.arraycopy(buf,0,tmp,0,buf.length);
            buf = tmp;
        }
    }

    /**
     * Parse the HTTP headers.
     */
    @Override
    public boolean parseHeaders()
        throws IOException {
        if (!parsingHeader) {
            throw new IllegalStateException(
                    sm.getString("iib.parseheaders.ise.error"));
        }

        HeaderParseStatus status = HeaderParseStatus.HAVE_MORE_HEADERS;

        do {
            status = parseHeader();
            // Checking that
            // (1) Headers plus request line size does not exceed its limit
            // (2) There are enough bytes to avoid expanding the buffer when
            // reading body
            // Technically, (2) is technical limitation, (1) is logical
            // limitation to enforce the meaning of headerBufferSize
            // From the way how buf is allocated and how blank lines are being
            // read, it should be enough to check (1) only.
            if (pos > headerBufferSize
                    || buf.length - pos < socketReadBufferSize) {
                throw new IllegalArgumentException(
                        sm.getString("iib.requestheadertoolarge.error"));
            }
        } while ( status == HeaderParseStatus.HAVE_MORE_HEADERS );
        if (status == HeaderParseStatus.DONE) {
            parsingHeader = false;
            end = pos;
            return true;
        } else {
            return false;
        }
    }


    /**
     * Parse an HTTP header.
     *
     * @return false after reading a blank line (which indicates that the
     * HTTP header parsing is done
     */
    private HeaderParseStatus parseHeader()
        throws IOException {

        //
        // Check for blank line
        //

        byte chr = 0;
        while (headerParsePos == HeaderParsePosition.HEADER_START) {

            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill(false)) {//parse header
                    headerParsePos = HeaderParsePosition.HEADER_START;
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }

            chr = buf[pos];

            if (chr == Constants.CR) {
                // Skip
            } else if (chr == Constants.LF) {
                pos++;
                return HeaderParseStatus.DONE;
            } else {
                break;
            }

            pos++;

        }

        if ( headerParsePos == HeaderParsePosition.HEADER_START ) {
            // Mark the current buffer position
            headerData.start = pos;
            headerParsePos = HeaderParsePosition.HEADER_NAME;
        }

        //
        // Reading the header name
        // Header name is always US-ASCII
        //

        while (headerParsePos == HeaderParsePosition.HEADER_NAME) {

            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill(false)) { //parse header
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }

            chr = buf[pos];
            if (chr == Constants.COLON) {
                headerParsePos = HeaderParsePosition.HEADER_VALUE_START;
                headerData.headerValue = headers.addValue(buf, headerData.start, pos - headerData.start);
                pos++;
                // Mark the current buffer position
                headerData.start = pos;
                headerData.realPos = pos;
                headerData.lastSignificantChar = pos;
                break;
            } else if (!HTTP_TOKEN_CHAR[chr]) {
                // If a non-token header is detected, skip the line and
                // ignore the header
                headerData.lastSignificantChar = pos;
                return skipLine();
            }

            // chr is next byte of header name. Convert to lowercase.
            if ((chr >= Constants.A) && (chr <= Constants.Z)) {
                buf[pos] = (byte) (chr - Constants.LC_OFFSET);
            }
            pos++;
        }

        // Skip the line and ignore the header
        if (headerParsePos == HeaderParsePosition.HEADER_SKIPLINE) {
            return skipLine();
        }

        //
        // Reading the header value (which can be spanned over multiple lines)
        //

        while (headerParsePos == HeaderParsePosition.HEADER_VALUE_START ||
               headerParsePos == HeaderParsePosition.HEADER_VALUE ||
               headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE) {

            if ( headerParsePos == HeaderParsePosition.HEADER_VALUE_START ) {
                // Skipping spaces
                while (true) {
                    // Read new bytes if needed
                    if (pos >= lastValid) {
                        if (!fill(false)) {//parse header
                            //HEADER_VALUE_START
                            return HeaderParseStatus.NEED_MORE_DATA;
                        }
                    }

                    chr = buf[pos];
                    if (chr == Constants.SP || chr == Constants.HT) {
                        pos++;
                    } else {
                        headerParsePos = HeaderParsePosition.HEADER_VALUE;
                        break;
                    }
                }
            }
            if ( headerParsePos == HeaderParsePosition.HEADER_VALUE ) {

                // Reading bytes until the end of the line
                boolean eol = false;
                while (!eol) {

                    // Read new bytes if needed
                    if (pos >= lastValid) {
                        if (!fill(false)) {//parse header
                            //HEADER_VALUE
                            return HeaderParseStatus.NEED_MORE_DATA;
                        }
                    }

                    chr = buf[pos];
                    if (chr == Constants.CR) {
                        // Skip
                    } else if (chr == Constants.LF) {
                        eol = true;
                    } else if (chr == Constants.SP || chr == Constants.HT) {
                        buf[headerData.realPos] = chr;
                        headerData.realPos++;
                    } else {
                        buf[headerData.realPos] = chr;
                        headerData.realPos++;
                        headerData.lastSignificantChar = headerData.realPos;
                    }

                    pos++;
                }

                // Ignore whitespaces at the end of the line
                headerData.realPos = headerData.lastSignificantChar;

                // Checking the first character of the new line. If the character
                // is a LWS, then it's a multiline header
                headerParsePos = HeaderParsePosition.HEADER_MULTI_LINE;
            }
            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill(false)) {//parse header
                    //HEADER_MULTI_LINE
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }

            chr = buf[pos];
            if ( headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE ) {
                if ( (chr != Constants.SP) && (chr != Constants.HT)) {
                    headerParsePos = HeaderParsePosition.HEADER_START;
                    break;
                } else {
                    // Copying one extra space in the buffer (since there must
                    // be at least one space inserted between the lines)
                    buf[headerData.realPos] = chr;
                    headerData.realPos++;
                    headerParsePos = HeaderParsePosition.HEADER_VALUE_START;
                }
            }
        }
        // Set the header value
        headerData.headerValue.setBytes(buf, headerData.start,
                headerData.lastSignificantChar - headerData.start);
        headerData.recycle();
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    }

    public int getParsingRequestLinePhase() {
        return parsingRequestLinePhase;
    }

    private HeaderParseStatus skipLine() throws IOException {
        headerParsePos = HeaderParsePosition.HEADER_SKIPLINE;
        boolean eol = false;

        // Reading bytes until the end of the line
        while (!eol) {

            // Read new bytes if needed
            if (pos >= lastValid) {
                if (!fill(false)) {
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }

            if (buf[pos] == Constants.CR) {
                // Skip
            } else if (buf[pos] == Constants.LF) {
                eol = true;
            } else {
                headerData.lastSignificantChar = pos;
            }

            pos++;
        }
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("iib.invalidheader", new String(buf,
                    headerData.start,
                    headerData.lastSignificantChar - headerData.start + 1,
                    StandardCharsets.ISO_8859_1)));
        }

        headerParsePos = HeaderParsePosition.HEADER_START;
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    }

    private final HeaderParseData headerData = new HeaderParseData();
    public static class HeaderParseData {
        /**
         * When parsing header name: first character of the header.<br />
         * When skipping broken header line: first character of the header.<br />
         * When parsing header value: first character after ':'.
         */
        int start = 0;
        /**
         * When parsing header name: not used (stays as 0).<br />
         * When skipping broken header line: not used (stays as 0).<br />
         * When parsing header value: starts as the first character after ':'.
         * Then is increased as far as more bytes of the header are harvested.
         * Bytes from buf[pos] are copied to buf[realPos]. Thus the string from
         * [start] to [realPos-1] is the prepared value of the header, with
         * whitespaces removed as needed.<br />
         */
        int realPos = 0;
        /**
         * When parsing header name: not used (stays as 0).<br />
         * When skipping broken header line: last non-CR/non-LF character.<br />
         * When parsing header value: position after the last not-LWS character.<br />
         */
        int lastSignificantChar = 0;
        /**
         * MB that will store the value of the header. It is null while parsing
         * header name and is created after the name has been parsed.
         */
        MessageBytes headerValue = null;
        public void recycle() {
            start = 0;
            realPos = 0;
            lastSignificantChar = 0;
            headerValue = null;
        }
    }


    // ------------------------------------------------------ Protected Methods

    @Override
    protected void init(SocketWrapper<Nio2Channel> socketWrapper,
            AbstractEndpoint<Nio2Channel> associatedEndpoint) throws IOException {

        endpoint = associatedEndpoint;
        socket = socketWrapper;
        if (socket == null) {
            // Socket has been closed in another thread
            throw new IOException(sm.getString("iib.socketClosed"));
        }
        socketReadBufferSize =
            socket.getSocket().getBufHandler().getReadBuffer().capacity();

        int bufLength = headerBufferSize + socketReadBufferSize;
        if (buf == null || buf.length < bufLength) {
            buf = new byte[bufLength];
        }

        // Initialize the completion handler
        this.completionHandler = new CompletionHandler<Integer, SocketWrapper<Nio2Channel>>() {

            @Override
            public void completed(Integer nBytes, SocketWrapper<Nio2Channel> attachment) {
                boolean notify = false;
                synchronized (completionHandler) {
                    if (nBytes < 0) {
                        failed(new ClosedChannelException(), attachment);
                        return;
                    }
                    readPending = false;
                    if (!Nio2Endpoint.isInline()) {
                        notify = true;
                    }
                }
                if (notify) {
                    endpoint.processSocket(attachment, SocketStatus.OPEN_READ, false);
                }
            }

            @Override
            public void failed(Throwable exc, SocketWrapper<Nio2Channel> attachment) {
                attachment.setError(true);
                if (exc instanceof IOException) {
                    e = (IOException) exc;
                } else {
                    e = new IOException(exc);
                }
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e);
                readPending = false;
                endpoint.processSocket(attachment, SocketStatus.OPEN_READ, true);
            }
        };
    }

    @Override
    protected boolean fill(boolean block) throws IOException, EOFException {
        if (e != null) {
            throw e;
        }
        if (parsingHeader) {
            if (lastValid > headerBufferSize) {
                throw new IllegalArgumentException
                    (sm.getString("iib.requestheadertoolarge.error"));
            }
        } else {
            lastValid = pos = end;
        }
        // Now fill the internal buffer
        int nRead = 0;
        ByteBuffer byteBuffer = socket.getSocket().getBufHandler().getReadBuffer();
        if (block) {
            if (!flipped) {
                byteBuffer.flip();
                flipped = true;
            }
            int nBytes = byteBuffer.remaining();
            // This case can happen when a blocking read follows a non blocking
            // fill that completed asynchronously
            if (nBytes > 0) {
                expand(nBytes + pos);
                byteBuffer.get(buf, pos, nBytes);
                lastValid = pos + nBytes;
                byteBuffer.clear();
                flipped = false;
                return true;
            } else {
                byteBuffer.clear();
                flipped = false;
                try {
                    nRead = socket.getSocket().read(byteBuffer).get(socket.getTimeout(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException
                        | TimeoutException e) {
                    throw new EOFException(sm.getString("iib.eof.error"));
                }
                if (nRead > 0) {
                    if (!flipped) {
                        byteBuffer.flip();
                        flipped = true;
                    }
                    expand(nRead + pos);
                    byteBuffer.get(buf, pos, nRead);
                    lastValid = pos + nRead;
                    return true;
                } else if (nRead == -1) {
                    //return false;
                    throw new EOFException(sm.getString("iib.eof.error"));
                } else {
                    return false;
                }
            }
        } else {
            synchronized (completionHandler) {
                if (!readPending) {
                    if (!flipped) {
                        byteBuffer.flip();
                        flipped = true;
                    }
                    int nBytes = byteBuffer.remaining();
                    if (nBytes > 0) {
                        expand(nBytes + pos);
                        byteBuffer.get(buf, pos, nBytes);
                        lastValid = pos + nBytes;
                        byteBuffer.clear();
                        flipped = false;
                    } else {
                        byteBuffer.clear();
                        flipped = false;
                        readPending = true;
                        Nio2Endpoint.startInline();
                        try {
                            socket.getSocket().read(byteBuffer, socket.getTimeout(),
                                    TimeUnit.MILLISECONDS, socket, completionHandler);
                        } catch (ReadPendingException e) {
                            // Ignore ?
                        }
                        Nio2Endpoint.endInline();
                        // Return the number of bytes that have been placed into the buffer
                        if (!readPending) {
                            // If the completion handler completed immediately
                            if (!flipped) {
                                byteBuffer.flip();
                                flipped = true;
                            }
                            nBytes = byteBuffer.remaining();
                            if (nBytes > 0) {
                                expand(nBytes + pos);
                                byteBuffer.get(buf, pos, nBytes);
                                lastValid = pos + nBytes;
                            }
                            byteBuffer.clear();
                            flipped = false;
                        }
                    }
                    return (lastValid - pos) > 0;
                } else {
                    return false;
                }
            }
        }
    }


    // ------------------------------------- InputStreamInputBuffer Inner Class


    /**
     * This class is an input buffer which will read its data from an input
     * stream.
     */
    protected class SocketInputBuffer
        implements InputBuffer {


        /**
         * Read bytes into the specified chunk.
         */
        @Override
        public int doRead(ByteChunk chunk, Request req )
            throws IOException {

            if (pos >= lastValid) {
                if (!fill(true)) //read body, must be blocking, as the thread is inside the app
                    return -1;
            }
            if (isBlocking()) {
                int length = lastValid - pos;
                chunk.setBytes(buf, pos, length);
                pos = lastValid;
                return (length);
            } else {
                synchronized (completionHandler) {
                    int length = lastValid - pos;
                    chunk.setBytes(buf, pos, length);
                    pos = lastValid;
                    return (length);
                }
            }
        }
    }
}
