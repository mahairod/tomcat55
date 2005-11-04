/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.coyote.ActionCode;
import org.apache.coyote.ActionHook;
import org.apache.coyote.Adapter;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.RequestInfo;
import org.apache.coyote.Response;
import org.apache.jk.common.AjpConstants;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.jni.Status;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadWithAttributes;


/**
 * Processes HTTP requests.
 *
 * @author Remy Maucherat
 * @author Henri Gomez
 * @author Dan Milstein
 * @author Keith Wannamaker
 * @author Kevin Seguin
 * @author Costin Manolache
 * @author Bill Barker
 */
public class AjpAprProcessor implements ActionHook {


    /**
     * Logger.
     */
    protected static org.apache.commons.logging.Log log
        = org.apache.commons.logging.LogFactory.getLog(AjpAprProcessor.class);

    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    public AjpAprProcessor(AprEndpoint endpoint) {

        this.endpoint = endpoint;
        
        request = new Request();
        request.setInputBuffer(new SocketInputBuffer());

        response = new Response();
        response.setHook(this);
        response.setOutputBuffer(new SocketOutputBuffer());
        request.setResponse(response);

        if (endpoint.getFirstReadTimeout() > 0) {
            readTimeout = endpoint.getFirstReadTimeout() * 1000;
        } else {
            readTimeout = 100 * 1000;
        }

        // Allocate input and output buffers
        inputBuffer = ByteBuffer.allocateDirect(16 * 1024);
        inputBuffer.limit(0);
        outputBuffer = ByteBuffer.allocateDirect(16 * 1024);

        // Cause loading of HexUtils
        int foo = HexUtils.DEC[0];

        // Cause loading of HttpMessages
        HttpMessages.getMessage(200);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated adapter.
     */
    protected Adapter adapter = null;


    /**
     * Request object.
     */
    protected Request request = null;


    /**
     * Response object.
     */
    protected Response response = null;


    /**
     * Header message. Note that this header is merely the one used during the
     * processing of the first message of a "request", so it might not be a request
     * header. It will stay unchanged during the processing of the whole request. 
     */
    protected AjpMessage requestHeaderMessage = new AjpMessage(); 


    /**
     * Message used for response header composition. 
     */
    protected AjpMessage responseHeaderMessage = new AjpMessage(); 


    /**
     * Body message.
     */
    protected AjpMessage bodyMessage = new AjpMessage();

    
    /**
     * Body message.
     */
    protected MessageBytes bodyBytes = MessageBytes.newInstance();

    
    /**
     * State flag.
     */
    protected boolean started = false;


    /**
     * Error flag.
     */
    protected boolean error = false;


    /**
     * Socket associated with the current connection.
     */
    protected long socket;


    /**
     * Host name (used to avoid useless B2C conversion on the host name).
     */
    protected char[] hostNameC = new char[0];


    /**
     * Associated endpoint.
     */
    protected AprEndpoint endpoint;


    /**
     * The socket timeout used when reading the first block of the request
     * header.
     */
    protected long readTimeout;
    
    
    /**
     * Temp message bytes used for processing.
     */
    protected MessageBytes tmpMB = MessageBytes.newInstance();
    
    
    /**
     * Byte chunk for certs.
     */
    protected MessageBytes certificates = MessageBytes.newInstance();
    
    
    /**
     * End of stream flag.
     */
    protected boolean endOfStream = false;
    
    
    /**
     * Body empty flag.
     */
    protected boolean empty = true;
    
    
    /**
     * First read.
     */
    protected boolean first = true;
    
    
    /**
     * Replay read.
     */
    protected boolean replay = false;
    
    
    /**
     * Finished response.
     */
    protected boolean finished = false;
    
    
    /**
     * Direct buffer used for output.
     */
    protected ByteBuffer outputBuffer = null;
    
    
    /**
     * Direct buffer used for input.
     */
    protected ByteBuffer inputBuffer = null;
    
    
    /**
     * Direct buffer used for sending right away a get body message.
     */
    protected static final ByteBuffer getBodyMessageBuffer;
    
    
    /**
     * Direct buffer used for sending right away a pong message.
     */
    protected static final ByteBuffer pongMessageBuffer;
    
    
    /**
     * End message array.
     */
    protected static final byte[] endMessageArray;
    
    
    // ----------------------------------------------------- Static Initializer


    static {

        // Set the get body message buffer
        AjpMessage getBodyMessage = new AjpMessage();
        getBodyMessage.reset();
        getBodyMessage.appendByte(Constants.JK_AJP13_GET_BODY_CHUNK);
        getBodyMessage.appendInt(Constants.MAX_READ_SIZE);
        getBodyMessage.end();
        getBodyMessageBuffer = 
            ByteBuffer.allocateDirect(getBodyMessage.getLen());
        getBodyMessageBuffer.put(getBodyMessage.getBuffer(), 0, 
                getBodyMessage.getLen());

        // Set the read body message buffer
        AjpMessage pongMessage = new AjpMessage();
        pongMessage.reset();
        pongMessage.appendByte(Constants.JK_AJP13_CPONG_REPLY);
        pongMessage.end();
        pongMessageBuffer = ByteBuffer.allocateDirect(pongMessage.getLen());
        pongMessageBuffer.put(pongMessage.getBuffer(), 0, 
                pongMessage.getLen());

        // Allocate the end message array
        AjpMessage endMessage = new AjpMessage();
        endMessage.reset();
        endMessage.appendByte(Constants.JK_AJP13_END_RESPONSE);
        endMessage.appendByte(1);
        endMessage.end();
        endMessageArray = new byte[endMessage.getLen()];
        System.arraycopy(endMessage.getBuffer(), 0, endMessageArray, 0, 
                endMessage.getLen());

    }
    
    
    // ------------------------------------------------------------- Properties


    /**
     * Use Tomcat authentication ?
     */
    protected boolean tomcatAuthentication = true;
    public boolean getTomcatAuthentication() { return tomcatAuthentication; }
    public void setTomcatAuthentication(boolean tomcatAuthentication) { this.tomcatAuthentication = tomcatAuthentication; }
    
    
    /**
     * Required secret.
     */
    protected String requiredSecret = null;
    public void setRequiredSecret(String requiredSecret) { this.requiredSecret = requiredSecret; }
    
    
    // --------------------------------------------------------- Public Methods


    /** Get the request associated with this processor.
     *
     * @return The request
     */
    public Request getRequest() {
        return request;
    }


    /**
     * Process pipelined HTTP requests using the specified input and output
     * streams.
     *
     * @throws IOException error during an I/O operation
     */
    public boolean process(long socket)
        throws IOException {
        ThreadWithAttributes thrA=
                (ThreadWithAttributes)Thread.currentThread();
        RequestInfo rp = request.getRequestProcessor();
        thrA.setCurrentStage(endpoint, "parsing http request");
        rp.setStage(org.apache.coyote.Constants.STAGE_PARSE);

        // Setting up the socket
        this.socket = socket;
        Socket.setrbb(this.socket, inputBuffer);
        Socket.setsbb(this.socket, outputBuffer);

        // Error flag
        error = false;

        long soTimeout = endpoint.getSoTimeout();

        int limit = 0;
        if (endpoint.getFirstReadTimeout() > 0) {
            limit = endpoint.getMaxThreads() / 2;
        }

        boolean openSocket = true;
        boolean keptAlive = false;

        while (started && !error) {

            // Parsing the request header
            try {
                // Get first message of the request
                if (!readMessage(requestHeaderMessage, true, 
                        keptAlive && (endpoint.getCurrentThreadsBusy() > limit))) {
                    // This means that no data is available right now
                    // (long keepalive), so that the processor should be recycled
                    // and the method should return true
                    rp.setStage(org.apache.coyote.Constants.STAGE_ENDED);
                    break;
                }
                // Check message type, process right away and break if 
                // not regular request processing
                int type = requestHeaderMessage.getByte();
                if (type == Constants.JK_AJP13_CPING_REQUEST) {
                    if (Socket.sendb(socket, pongMessageBuffer, 0, 
                            pongMessageBuffer.position()) < 0) {
                        error = true;
                    }
                    continue;
                }

                keptAlive = true;
                request.setStartTime(System.currentTimeMillis());
            } catch (IOException e) {
                error = true;
                break;
            } catch (Throwable t) {
                log.debug(sm.getString("ajpprocessor.header.error"), t);
                // 400 - Bad Request
                response.setStatus(400);
                error = true;
            }

            // Setting up filters, and parse some request headers
            thrA.setCurrentStage(endpoint, "prepareRequest");
            rp.setStage(org.apache.coyote.Constants.STAGE_PREPARE);
            try {
                prepareRequest();
                thrA.setParam(endpoint, request.requestURI());
            } catch (Throwable t) {
                log.debug(sm.getString("ajpprocessor.request.prepare"), t);
                // 400 - Internal Server Error
                response.setStatus(400);
                error = true;
            }

            // Process the request in the adapter
            if (!error) {
                try {
                    thrA.setCurrentStage(endpoint, "service");
                    rp.setStage(org.apache.coyote.Constants.STAGE_SERVICE);
                    adapter.service(request, response);
                } catch (InterruptedIOException e) {
                    error = true;
                } catch (Throwable t) {
                    log.error(sm.getString("ajpprocessor.request.process"), t);
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    error = true;
                }
            }

            // Finish the response if not done yet
            if (!finished) {
                try {
                    finish();
                } catch (Throwable t) {
                    error = true;
                }
            }
            
            // If there was an error, make sure the request is counted as
            // and error, and update the statistics counter
            if (error) {
                response.setStatus(500);
            }
            request.updateCounters();

            thrA.setCurrentStage(endpoint, "ended");
            rp.setStage(org.apache.coyote.Constants.STAGE_KEEPALIVE);
            recycle();
            
        }

        // Add the socket to the poller
        if (!error) {
            endpoint.getPoller().add(socket);
        } else {
            openSocket = false;
        }

        rp.setStage(org.apache.coyote.Constants.STAGE_ENDED);
        recycle();

        return openSocket;
        
    }


    // ----------------------------------------------------- ActionHook Methods


    /**
     * Send an action to the connector.
     *
     * @param actionCode Type of the action
     * @param param Action parameter
     */
    public void action(ActionCode actionCode, Object param) {

        if (actionCode == ActionCode.ACTION_COMMIT) {

            if (response.isCommitted())
                return;

            // Validate and write response headers
            try {
                prepareResponse();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_CLIENT_FLUSH) {

            if (!response.isCommitted()) {
                // Validate and write response headers
                try {
                    prepareResponse();
                } catch (IOException e) {
                    // Set error flag
                    error = true;
                    return;
                }
            }

            try {
                flush();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_CLOSE) {
            // Close

            // End the processing of the current request, and stop any further
            // transactions with the client

            try {
                finish();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_START) {

            started = true;

        } else if (actionCode == ActionCode.ACTION_STOP) {

            started = false;

        } else if (actionCode == ActionCode.ACTION_REQ_SSL_ATTRIBUTE ) {

            if (!certificates.isNull()) {
                ByteChunk certData = certificates.getByteChunk();
                X509Certificate jsseCerts[] = null;
                ByteArrayInputStream bais = 
                    new ByteArrayInputStream(certData.getBytes(),
                            certData.getStart(),
                            certData.getLength());
                // Fill the first element.
                try {
                    CertificateFactory cf =
                        CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate)
                    cf.generateCertificate(bais);
                    jsseCerts = new X509Certificate[1];
                    jsseCerts[0] = cert;
                    request.setAttribute(AprEndpoint.CERTIFICATE_KEY, jsseCerts);
                } catch (java.security.cert.CertificateException e) {
                    log.error(sm.getString("ajpprocessor.certs.fail"), e);
                    return;
                }
            }
            
        } else if (actionCode == ActionCode.ACTION_REQ_HOST_ATTRIBUTE) {

            // Get remote host name using a DNS resolution
            if (request.remoteHost().isNull()) {
                try {
                    request.remoteHost().setString(InetAddress.getByName
                            (request.remoteAddr().toString()).getHostName());
                } catch (IOException iex) {
                    // Ignore
                }
            }

        } else if (actionCode == ActionCode.ACTION_REQ_LOCAL_ADDR_ATTRIBUTE) {

            // Copy from local name for now, which should simply be an address
            request.localAddr().setString(request.localName().toString());

        } else if (actionCode == ActionCode.ACTION_REQ_SET_BODY_REPLAY) {
            
            // Set the given bytes as the content
            ByteChunk bc = (ByteChunk) param;
            bodyBytes.setBytes(bc.getBytes(), bc.getStart(), bc.getLength());
            first = false;
            empty = false;
            replay = true;
            
        }


    }


    // ------------------------------------------------------ Connector Methods


    /**
     * Set the associated adapter.
     *
     * @param adapter the new adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }


    /**
     * Get the associated adapter.
     *
     * @return the associated adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }


    // ------------------------------------------------------ Protected Methods

    
    /**
     * After reading the request headers, we have to setup the request filters.
     */
    protected void prepareRequest() {

        // Translate the HTTP method code to a String.
        byte methodCode = requestHeaderMessage.getByte();
        if (methodCode != Constants.SC_M_JK_STORED) {
            String methodName = Constants.methodTransArray[(int)methodCode - 1];
            request.method().setString(methodName);
        }

        requestHeaderMessage.getBytes(request.protocol()); 
        requestHeaderMessage.getBytes(request.requestURI());

        requestHeaderMessage.getBytes(request.remoteAddr());
        requestHeaderMessage.getBytes(request.remoteHost());
        requestHeaderMessage.getBytes(request.localName());
        request.setLocalPort(requestHeaderMessage.getInt());

        boolean isSSL = requestHeaderMessage.getByte() != 0;
        if (isSSL) {
            request.scheme().setString("https");
        }

        // Decode headers
        MimeHeaders headers = request.getMimeHeaders();

        int hCount = requestHeaderMessage.getInt();
        for(int i = 0 ; i < hCount ; i++) {
            String hName = null;
            
            // Header names are encoded as either an integer code starting
            // with 0xA0, or as a normal string (in which case the first
            // two bytes are the length).
            int isc = requestHeaderMessage.peekInt();
            int hId = isc & 0xFF;
            
            MessageBytes vMB = null;
            isc &= 0xFF00;
            if(0xA000 == isc) {
                requestHeaderMessage.getInt(); // To advance the read position
                hName = Constants.headerTransArray[hId - 1];
                vMB = headers.addValue(hName);
            } else {
                // reset hId -- if the header currently being read
                // happens to be 7 or 8 bytes long, the code below
                // will think it's the content-type header or the
                // content-length header - SC_REQ_CONTENT_TYPE=7,
                // SC_REQ_CONTENT_LENGTH=8 - leading to unexpected
                // behaviour.  see bug 5861 for more information.
                hId = -1;
                requestHeaderMessage.getBytes(tmpMB);
                ByteChunk bc = tmpMB.getByteChunk();
                vMB = headers.addValue(bc.getBuffer(),
                        bc.getStart(), bc.getLength());
            }
            
            requestHeaderMessage.getBytes(vMB);
            
            if (hId == Constants.SC_REQ_CONTENT_LENGTH ||
                    (hId == -1 && tmpMB.equalsIgnoreCase("Content-Length"))) {
                // just read the content-length header, so set it
                request.setContentLength( vMB.getInt() );
            } else if (hId == Constants.SC_REQ_CONTENT_TYPE ||
                    (hId == -1 && tmpMB.equalsIgnoreCase("Content-Type"))) {
                // just read the content-type header, so set it
                ByteChunk bchunk = vMB.getByteChunk();
                request.contentType().setBytes(bchunk.getBytes(),
                        bchunk.getOffset(),
                        bchunk.getLength());
            }
        }

        // Decode extra attributes
        boolean secret = false;
        byte attributeCode;
        while ((attributeCode = requestHeaderMessage.getByte()) 
                != Constants.SC_A_ARE_DONE) {

            switch (attributeCode) {
            
            case Constants.SC_A_REQ_ATTRIBUTE :
                requestHeaderMessage.getBytes(tmpMB);
                String n = tmpMB.toString();
                requestHeaderMessage.getBytes(tmpMB);
                String v = tmpMB.toString();
                request.setAttribute(n, v);
                break;
            
            case Constants.SC_A_CONTEXT :
                requestHeaderMessage.getBytes(tmpMB);
                // nothing
                break;
                
            case Constants.SC_A_SERVLET_PATH :
                requestHeaderMessage.getBytes(tmpMB);
                // nothing 
                break;
                
            case Constants.SC_A_REMOTE_USER :
                if (tomcatAuthentication) {
                    // ignore server
                    requestHeaderMessage.getBytes(tmpMB);
                } else {
                    requestHeaderMessage.getBytes(request.getRemoteUser());
                }
                break;
                
            case Constants.SC_A_AUTH_TYPE :
                if (tomcatAuthentication) {
                    // ignore server
                    requestHeaderMessage.getBytes(tmpMB);
                } else {
                    requestHeaderMessage.getBytes(request.getAuthType());
                }
                break;
                
            case Constants.SC_A_QUERY_STRING :
                requestHeaderMessage.getBytes(request.queryString());
                break;
                
            case Constants.SC_A_JVM_ROUTE :
                requestHeaderMessage.getBytes(request.instanceId());
                break;
                
            case Constants.SC_A_SSL_CERT :
                request.scheme().setString("https");
                // SSL certificate extraction is lazy, moved to JkCoyoteHandler
                requestHeaderMessage.getBytes(certificates);
                break;
                
            case Constants.SC_A_SSL_CIPHER :
                request.scheme().setString("https");
                requestHeaderMessage.getBytes(tmpMB);
                request.setAttribute(AprEndpoint.CIPHER_SUITE_KEY,
                                     tmpMB.toString());
                break;
                
            case Constants.SC_A_SSL_SESSION :
                request.scheme().setString("https");
                requestHeaderMessage.getBytes(tmpMB);
                request.setAttribute(AprEndpoint.SESSION_ID_KEY, 
                                     tmpMB.toString());
                break;
                
            case Constants.SC_A_SSL_KEY_SIZE :
                request.setAttribute(AprEndpoint.KEY_SIZE_KEY,
                                     new Integer(requestHeaderMessage.getInt()));
                break;

            case Constants.SC_A_STORED_METHOD:
                requestHeaderMessage.getBytes(request.method()); 
                break;
                
            case AjpConstants.SC_A_SECRET:
                requestHeaderMessage.getBytes(tmpMB);
                if (requiredSecret != null) {
                    secret = true;
                    if (!tmpMB.equals(requiredSecret)) {
                        response.setStatus(403);
                        error = true;
                    }
                }
                break;
                
            default:
                // Ignore unknown attribute for backward compatibility
                break;
            
            }
            
        }

        // Check if secret was submitted if required
        if ((requiredSecret != null) && !secret) {
            response.setStatus(403);
            error = true;
        }

        // Check for a full URI (including protocol://host:port/)
        ByteChunk uriBC = request.requestURI().getByteChunk();
        if (uriBC.startsWithIgnoreCase("http", 0)) {

            int pos = uriBC.indexOf("://", 0, 3, 4);
            int uriBCStart = uriBC.getStart();
            int slashPos = -1;
            if (pos != -1) {
                byte[] uriB = uriBC.getBytes();
                slashPos = uriBC.indexOf('/', pos + 3);
                if (slashPos == -1) {
                    slashPos = uriBC.getLength();
                    // Set URI as "/"
                    request.requestURI().setBytes
                        (uriB, uriBCStart + pos + 1, 1);
                } else {
                    request.requestURI().setBytes
                        (uriB, uriBCStart + slashPos,
                         uriBC.getLength() - slashPos);
                }
                MessageBytes hostMB = headers.setValue("host");
                hostMB.setBytes(uriB, uriBCStart + pos + 3,
                                slashPos - pos - 3);
            }

        }

        MessageBytes valueMB = request.getMimeHeaders().getValue("host");
        parseHost(valueMB);

    }


    /**
     * Parse host.
     */
    public void parseHost(MessageBytes valueMB) {

        if (valueMB == null || valueMB.isNull()) {
            // HTTP/1.0
            // Default is what the socket tells us. Overriden if a host is
            // found/parsed
            request.setServerPort(endpoint.getPort()/*socket.getLocalPort()*/);
            InetAddress localAddress = endpoint.getAddress()/*socket.getLocalAddress()*/;
            // Setting the socket-related fields. The adapter doesn't know
            // about socket.
            request.setLocalHost(localAddress.getHostName());
            request.serverName().setString(localAddress.getHostName());
            return;
        }

        ByteChunk valueBC = valueMB.getByteChunk();
        byte[] valueB = valueBC.getBytes();
        int valueL = valueBC.getLength();
        int valueS = valueBC.getStart();
        int colonPos = -1;
        if (hostNameC.length < valueL) {
            hostNameC = new char[valueL];
        }

        boolean ipv6 = (valueB[valueS] == '[');
        boolean bracketClosed = false;
        for (int i = 0; i < valueL; i++) {
            char b = (char) valueB[i + valueS];
            hostNameC[i] = b;
            if (b == ']') {
                bracketClosed = true;
            } else if (b == ':') {
                if (!ipv6 || bracketClosed) {
                    colonPos = i;
                    break;
                }
            }
        }

        if (colonPos < 0) {
            if (request.scheme().equalsIgnoreCase("https")) {
                // 443 - Default HTTPS port
                request.setServerPort(443);
            } else {
                // 80 - Default HTTTP port
                request.setServerPort(80);
            }
            request.serverName().setChars(hostNameC, 0, valueL);
        } else {

            request.serverName().setChars(hostNameC, 0, colonPos);

            int port = 0;
            int mult = 1;
            for (int i = valueL - 1; i > colonPos; i--) {
                int charValue = HexUtils.DEC[(int) valueB[i + valueS]];
                if (charValue == -1) {
                    // Invalid character
                    error = true;
                    // 400 - Bad request
                    response.setStatus(400);
                    break;
                }
                port = port + (charValue * mult);
                mult = 10 * mult;
            }
            request.setServerPort(port);

        }

    }


    /**
     * When committing the response, we have to validate the set of headers, as
     * well as setup the response filters.
     */
    protected void prepareResponse()
        throws IOException {

        response.setCommitted(true);
        
        responseHeaderMessage.reset();
        responseHeaderMessage.appendByte(Constants.JK_AJP13_SEND_HEADERS);
        
        // HTTP header contents
        responseHeaderMessage.appendInt(response.getStatus());
        String message = response.getMessage();
        if (message == null){
            message = HttpMessages.getMessage(response.getStatus());
        } else {
            message = message.replace('\n', ' ').replace('\r', ' ');
        }
        tmpMB.setString(message);
        responseHeaderMessage.appendBytes(tmpMB);

        // Special headers
        MimeHeaders headers = response.getMimeHeaders();
        String contentType = response.getContentType();
        if (contentType != null) {
            headers.setValue("Content-Type").setString(contentType);
        }
        String contentLanguage = response.getContentLanguage();
        if (contentLanguage != null) {
            headers.setValue("Content-Language").setString(contentLanguage);
        }
        int contentLength = response.getContentLength();
        if (contentLength >= 0) {
            headers.setValue("Content-Length").setInt(contentLength);
        }
        
        // Other headers
        int numHeaders = headers.size();
        responseHeaderMessage.appendInt(numHeaders);
        for (int i = 0; i < numHeaders; i++) {
            MessageBytes hN = headers.getName(i);
            responseHeaderMessage.appendBytes(hN);
            MessageBytes hV=headers.getValue(i);
            responseHeaderMessage.appendBytes(hV);
        }
        
        // Write to buffer
        responseHeaderMessage.end();
        outputBuffer.put(responseHeaderMessage.getBuffer(), 0, responseHeaderMessage.getLen());

    }

    
    /**
     * Finish AJP response.
     */
    protected void finish()
        throws IOException {

        if (!response.isCommitted()) {
            // Validate and write response headers
            try {
                prepareResponse();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }
        }

        if (finished)
            return;
        
        finished = true;
        
        // Add the end message
        if (outputBuffer.position() + endMessageArray.length > outputBuffer.capacity()) {
            flush();
        }
        outputBuffer.put(endMessageArray);
        flush();
        
    }
    
    
    /**
     * Read at least the specified amount of bytes, and place them 
     * in the input buffer.
     */
    protected boolean read(int n)
        throws IOException {

        if (inputBuffer.capacity() - inputBuffer.limit() <= 
                n - inputBuffer.remaining()) {
            inputBuffer.compact();
            inputBuffer.limit(inputBuffer.position());
            inputBuffer.position(0);
        }
        while (inputBuffer.remaining() < n) {
            int nRead = Socket.recvbb
                (socket, inputBuffer.limit(), 
                        inputBuffer.capacity() - inputBuffer.limit());
            if (nRead > 0) {
                inputBuffer.limit(inputBuffer.limit() + nRead);
            } else {
                throw new IOException(sm.getString("ajpprotocol.failedread"));
            }
        }
        
        return true;
        
    }
    
    
    /**
     * Read at least the specified amount of bytes, and place them 
     * in the input buffer.
     */
    protected boolean readt(int n, boolean useAvailableData)
        throws IOException {
        
        if (useAvailableData && inputBuffer.remaining() == 0) {
            return false;
        }
        if (inputBuffer.capacity() - inputBuffer.limit() <= 
                n - inputBuffer.remaining()) {
            inputBuffer.compact();
            inputBuffer.limit(inputBuffer.position());
            inputBuffer.position(0);
        }
        while (inputBuffer.remaining() < n) {
            int nRead = Socket.recvbbt
                (socket, inputBuffer.limit(), 
                        inputBuffer.capacity() - inputBuffer.limit(), readTimeout);
            if (nRead > 0) {
                inputBuffer.limit(inputBuffer.limit() + nRead);
            } else {
                if ((-nRead) == Status.ETIMEDOUT || (-nRead) == Status.TIMEUP) {
                    return false;
                } else {
                    throw new IOException(sm.getString("ajpprotocol.failedread"));
                }
            }
        }
        
        return true;
        
    }
    
    
    /** Receive a chunk of data. Called to implement the
     *  'special' packet in ajp13 and to receive the data
     *  after we send a GET_BODY packet
     */
    public boolean receive() throws IOException {
        
        first = false;
        bodyMessage.reset();
        readMessage(bodyMessage, false, false);
        
        // No data received.
        if (bodyMessage.getLen() == 0) {
            // just the header
            // Don't mark 'end of stream' for the first chunk.
            return false;
        }
        int blen = bodyMessage.peekInt();
        if (blen == 0) {
            return false;
        }

        bodyMessage.getBytes(bodyBytes);
        empty = false;
        return true;
    }

    /**
     * Get more request body data from the web server and store it in the 
     * internal buffer.
     *
     * @return true if there is more data, false if not.    
     */
    private boolean refillReadBuffer() throws IOException {
        // If the server returns an empty packet, assume that that end of
        // the stream has been reached (yuck -- fix protocol??).
        // FORM support
        if (replay) {
            endOfStream = true; // we've read everything there is
        }
        if (endOfStream) {
            return false;
        }

        // Request more data immediately
        Socket.sendb(socket, getBodyMessageBuffer, 0, 
                getBodyMessageBuffer.position());

        boolean moreData = receive();
        if( !moreData ) {
            endOfStream = true;
        }
        return moreData;
    }
    

    /**
     * Read an AJP message.
     * 
     * @param first is true if the message is the first in the request, which
     *        will cause a short duration blocking read
     * @return true if the message has been read, false if the short read 
     *         didn't return anything
     * @throws IOException any other failure, including incomplete reads
     */
    protected boolean readMessage(AjpMessage message, boolean first, 
            boolean useAvailableData)
        throws IOException {
        
        byte[] buf = message.getBuffer();
        int headerLength = message.getHeaderLength();

        if (first) {
            if (!readt(headerLength, useAvailableData)) {
                return false;
            }
        } else {
            read(headerLength);
        }
        inputBuffer.get(message.getBuffer(), 0, headerLength);
        message.processHeader();
        read(message.getLen());
        inputBuffer.get(message.getBuffer(), headerLength, message.getLen());
        
        return true;
        
    }


    /**
     * Recycle the processor.
     */
    public void recycle() {

        // Recycle Request object
        first = true;
        endOfStream = false;
        empty = true;
        replay = false;
        finished = false;
        request.recycle();
        response.recycle();
        certificates.recycle();

        inputBuffer.clear();
        inputBuffer.limit(0);
        outputBuffer.clear();

    }


    /**
     * Callback to write data from the buffer.
     */
    protected void flush()
        throws IOException {
        if (outputBuffer.position() > 0) {
            if (Socket.sendbb(socket, 0, outputBuffer.position()) < 0) {
                throw new IOException(sm.getString("ajpprotocol.failedwrite"));
            }
            outputBuffer.clear();
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
        public int doRead(ByteChunk chunk, Request req ) 
            throws IOException {

            if (endOfStream) {
                return -1;
            }
            if (first) {
                // Handle special first-body-chunk
                if (!receive()) {
                    return 0;
                }
            } else if (empty) {
                if (!refillReadBuffer()) {
                    return -1;
                }
            }
            ByteChunk bc = bodyBytes.getByteChunk();
            chunk.setBytes(bc.getBuffer(), bc.getStart(), bc.getLength());
            empty = true;
            return chunk.getLength();

        }

    }


    // ----------------------------------- OutputStreamOutputBuffer Inner Class


    /**
     * This class is an output buffer which will write data to an output
     * stream.
     */
    protected class SocketOutputBuffer 
        implements OutputBuffer {


        /**
         * Write chunk.
         */
        public int doWrite(ByteChunk chunk, Response res) 
            throws IOException {

            if (!response.isCommitted()) {
                // Validate and write response headers
                try {
                    prepareResponse();
                } catch (IOException e) {
                    // Set error flag
                    error = true;
                }
            }

            int len = chunk.getLength();
            // 4 - hardcoded, byte[] marshalling overhead 
            int chunkSize = 8*1024 - 4 - 4;
            int off = 0;
            while (len > 0) {
                int thisTime = len;
                if (thisTime > chunkSize) {
                    thisTime = chunkSize;
                }
                len -= thisTime;
                if (outputBuffer.position() + thisTime + 4 + 4 > 
                    outputBuffer.capacity()) {
                    flush();
                }
                outputBuffer.put((byte) 0x41);
                outputBuffer.put((byte) 0x42);
                outputBuffer.putShort((short) (thisTime + 4));
                outputBuffer.put(Constants.JK_AJP13_SEND_BODY_CHUNK);
                outputBuffer.putShort((short) chunk.getLength());
                outputBuffer.put(chunk.getBytes(), chunk.getOffset() + off, thisTime);
                outputBuffer.put((byte) 0x00);
                off += thisTime;
            }
            
            return chunk.getLength();

        }


    }


}
