/* * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.coyote.tomcat4;


import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.Cookies;
import org.apache.tomcat.util.http.ServerCookie;

import org.apache.coyote.ActionCode;
import org.apache.coyote.ActionHook;
import org.apache.coyote.Adapter;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Processor;
import org.apache.coyote.Request;
import org.apache.coyote.Response;

import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.StringParser;


/**
 * Implementation of a request processor which delegates the processing to a
 * Coyote processor.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

final class CoyoteProcessor
    implements Lifecycle, Runnable, Adapter {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new CoyoteProcessor associated with the specified connector.
     *
     * @param connector CoyoteConnector that owns this processor
     * @param id Identifier of this CoyoteProcessor (unique per connector)
     */
    public CoyoteProcessor(CoyoteConnector connector, int id) {

        super();
        this.connector = connector;
        this.debug = connector.getDebug();
        this.id = id;
        this.proxyName = connector.getProxyName();
        this.proxyPort = connector.getProxyPort();
        this.request = (CoyoteRequest) connector.createRequest();
        this.response = (CoyoteResponse) connector.createResponse();
        this.request.setResponse(this.response);
        this.response.setRequest(this.request);
        this.serverPort = connector.getPort();
        this.threadName =
            "CoyoteProcessor[" + connector.getPort() + "][" + id + "]";

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Is there a new socket available?
     */
    private boolean available = false;


    /**
     * The CoyoteConnector with which this processor is associated.
     */
    private CoyoteConnector connector = null;


    /**
     * Coyote processor.
     */
    private Processor processor = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The identifier of this processor, unique per connector.
     */
    private int id = 0;


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The match string for identifying a session ID parameter.
     */
    private static final String match =
        ";" + Globals.SESSION_PARAMETER_NAME + "=";


    /**
     * The match string for identifying a session ID parameter.
     */
    private static final char[] SESSION_ID = match.toCharArray();


    /**
     * The string parser we will use for parsing request lines.
     */
    private StringParser parser = new StringParser();


    /**
     * The proxy server name for our Connector.
     */
    private String proxyName = null;


    /**
     * The proxy server port for our Connector.
     */
    private int proxyPort = 0;


    /**
     * The Coyote request object we will pass to our associated container.
     */
    private CoyoteRequest request = null;


    /**
     * The Coyote response object we will pass to our associated container.
     */
    private CoyoteResponse response = null;


    /**
     * The actual server port for our Connector.
     */
    private int serverPort = 0;


    /**
     * The string manager for this package.
     */
    protected StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The socket we are currently processing a request for.  This object
     * is used for inter-thread communication only.
     */
    private Socket socket = null;


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The shutdown signal to our background thread
     */
    private boolean stopped = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The name to register for the background thread.
     */
    private String threadName = null;


    /**
     * The thread synchronization object.
     */
    private Object threadSync = new Object();


    /**
     * Processor state
     */
    private int status = Constants.PROCESSOR_IDLE;


    // -------------------------------------------------------- Adapter Methods


    /**
     * Service method.
     */
    public void service(Request req, Response res)
        throws Exception {

        // Wrapping the Coyote requests
        request.setCoyoteRequest(req);
        response.setCoyoteResponse(res);

        try {
            // Parse and set Catalina and configuration specific 
            // request parameters
            postParseRequest(req, res);
            // Calling the container
            connector.getContainer().invoke(request, response);
            response.finishResponse();
        } catch (IOException e) {
            ;
        } catch (Throwable t) {
            log(sm.getString("coyoteProcessor.service"), t);
        } finally {
            // Recycle the wrapper request and response
            request.recycle();
            response.recycle();
        }

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return a String value representing this object.
     */
    public String toString() {

        return (this.threadName);

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Process an incoming TCP/IP connection on the specified socket.  Any
     * exception that occurs during processing must be logged and swallowed.
     * <b>NOTE</b>:  This method is called from our Connector's thread.  We
     * must assign it to our own thread so that multiple simultaneous
     * requests can be handled.
     *
     * @param socket TCP socket to process
     */
    synchronized void assign(Socket socket) {

        // Wait for the Processor to get the previous Socket
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Store the newly available Socket and notify our thread
        this.socket = socket;
        available = true;
        notifyAll();

        if ((debug >= 1) && (socket != null))
            log(" An incoming request is being assigned");

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse additional request parameters.
     */
    protected void postParseRequest(Request req, Response res)
        throws IOException {

        request.setSocket(socket);
        request.setSecure(connector.getSecure());
        req.scheme().setString(connector.getScheme());

        request.setAuthorization
            (req.getHeader(Constants.AUTHORIZATION_HEADER));

        if (proxyPort != 0)
            request.setServerPort(proxyPort);
        else
            request.setServerPort(serverPort);

        if (!normalize(req.decodedURI())) {
            res.setStatus(400);
            res.setMessage("Invalid URI");
            throw new IOException("Invalid URI");
        }

        parseSessionId(req);

        // Additional URI normalization and validation is needed for security 
        // reasons on Tomcat 4.0.x
        if (connector.getUseURIValidationHack()) {
            String uri = validate(request.getRequestURI());
            if (uri == null) {
                res.setStatus(400);
                res.setMessage("Invalid URI");
                throw new IOException("Invalid URI");
            } else {
                req.requestURI().setString(uri);
                // Redoing the URI decoding
                req.decodedURI().duplicate(req.requestURI());
                req.getURLDecoder().convert(req.decodedURI(), true);
            }
        }

        parseHost();
        parseCookies(req);

    }


    /**
     * Parse host.
     */
    protected void parseHost()
        throws IOException {

        String value = request.getHeader("host");
        if (value != null) {
            int n = value.indexOf(':');
            if (n < 0) {
                if (connector.getScheme().equals("http")) {
                    request.setServerPort(80);
                } else if (connector.getScheme().equals("https")) {
                    request.setServerPort(443);
                }
                if (proxyName != null)
                    request.setServerName(proxyName);
                else
                    request.setServerName(value);
            } else {
                if (proxyName != null)
                    request.setServerName(proxyName);
                else
                    request.setServerName(value.substring(0, n).trim());
                if (proxyPort != 0)
                    request.setServerPort(proxyPort);
                else {
                    int port = 80;
                    try {
                        port =
                            Integer.parseInt(value.substring(n + 1).trim());
                    } catch (Exception e) {
                        throw new IOException
                            (sm.getString
                             ("coyoteProcessor.parseHeaders.portNumber"));
                    }
                    request.setServerPort(port);
                }
            }
        }

    }


    /**
     * Parse session id in URL.
     * FIXME: Optimize this.
     */
    protected void parseSessionId(Request req) {

        ByteChunk uriBC = req.decodedURI().getByteChunk();
        int semicolon = uriBC.indexOf(match, 0, match.length(), 0);

        if (semicolon > 0) {

            // Parse session ID, and extract it from the decoded request URI
            String uri = uriBC.toString();
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            req.decodedURI().setString(uri.substring(0, semicolon) + rest);

            // Extract session ID from request URI
            uri = req.requestURI().toString();
            semicolon = uri.indexOf(match);

            if (semicolon > 0) {
                rest = uri.substring(semicolon + match.length());
                semicolon2 = rest.indexOf(';');
                if (semicolon2 >= 0) {
                    rest = rest.substring(semicolon2);
                } else {
                    rest = "";
                }
                req.requestURI().setString(uri.substring(0, semicolon) + rest);
            }

        } else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

    }


    /**
     * Parse cookies.
     * Note: Using Coyote native cookie parser to parse cookies would be faster
     * but a conversion to Catalina own cookies would then be needed, which 
     * would take away most if not all of the performance benefit.
     */
    protected void parseCookies(Request req) {

        Cookies serverCookies = req.getCookies();
        int count = serverCookies.getCookieCount();
        if (count <= 0)
            return;

        Cookie[] cookies = new Cookie[count];

        for (int i = 0; i < count; i++) {
            ServerCookie scookie = serverCookies.getCookie(i);
            if (scookie.getName().equals(Globals.SESSION_COOKIE_NAME)) {
                // Override anything requested in the URL
                if (!request.isRequestedSessionIdFromCookie()) {
                    // Accept only the first session id cookie
                    request.setRequestedSessionId
                        (scookie.getValue().toString());
                    request.setRequestedSessionCookie(true);
                    request.setRequestedSessionURL(false);
                    if (debug >= 1)
                        log(" Requested cookie session id is " +
                            ((HttpServletRequest) request.getRequest())
                            .getRequestedSessionId());
                }
            }
            Cookie cookie = new Cookie(scookie.getName().toString(),
                                       scookie.getValue().toString());
            cookies[i] = cookie;
        }

        request.setCookies(cookies);

    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be validated
     */
    protected String validate(String path) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") ||
            normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0)
            || (normalized.indexOf("%2F") >= 0)
            || (normalized.indexOf("%2E") >= 0)
            || (normalized.indexOf("%5C") >= 0)
            || (normalized.indexOf("%2f") >= 0)
            || (normalized.indexOf("%2e") >= 0)
            || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);

    }


    /**
     * Normalize URI.
     * <p>
     * This method normalizes '/./' and '/../'.
     * 
     * @param uri URI to be normalized
     */
    public boolean normalize(MessageBytes uriMB) {

        ByteChunk uriBC = uriMB.getByteChunk();
        byte[] b = uriBC.getBytes();
        int start = uriBC.getStart();
        int end = uriBC.getEnd();

        int pos = 0;
        int index = 0;

        // Replace '\' with '/'
        for (pos = start; pos < end; pos++) {
            if (b[pos] == (byte) '\\')
                b[pos] = (byte) '/';
        }

        // Replace "//" with "/"
        for (pos = start; pos < (end - 1); pos++) {
            if ((b[pos] == (byte) '/') && (b[pos + 1] == (byte) '/')) {
                copyBytes(b, pos, pos + 1, end - pos - 1);
                end--;
            }
        }

        // If the URI ends with "/." or "/..", then we append an extra "/"
        // Note: It is possible to extend the URI by 1 without any side effect
        // as the next character in a non-significant WS.
        if (((end - start) > 2) && (b[end - 1] == (byte) '.')) {
            if ((b[end - 2] == (byte) '/') 
                || ((b[end - 2] == (byte) '.') 
                    && (b[end - 3] == (byte) '/'))) {
                b[end] = (byte) '/';
                end++;
            }
        }

        uriBC.setEnd(end);

        index = 0;

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            index = uriBC.indexOf("/./", 0, 3, index);
            if (index < 0)
                break;
            copyBytes(b, start + index, start + index + 2, 
                      end - start - index - 2);
            end = end - 2;
            uriBC.setEnd(end);
        }

        index = 0;

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            index = uriBC.indexOf("/../", 0, 4, index);
            if (index < 0)
                break;
            // Prevent from going outside our context
            if (index == 0)
                return false;
            int index2 = -1;
            for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos --) {
                if (b[pos] == (byte) '/') {
                    index2 = pos;
                }
            }
            copyBytes(b, start + index2, start + index + 3,
                      end - start - index - 3);
            end = end + index2 - index - 3;
            uriBC.setEnd(end);
            index = index2;
        }

        uriBC.setBytes(b, start, end);

        return true;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Copy an array of bytes to a different position. Used during 
     * normalization.
     */
    protected void copyBytes(byte[] b, int dest, int src, int len) {
        for (int pos = 0; pos < len; pos++) {
            b[pos + dest] = b[pos + src];
        }
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Await a newly assigned Socket from our Connector, or <code>null</code>
     * if we are supposed to shut down.
     */
    private synchronized Socket await() {

        // Wait for the Connector to provide a new Socket
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Notify the Connector that we have received this Socket
        Socket socket = this.socket;
        available = false;
        notifyAll();

        if ((debug >= 1) && (socket != null))
            log("  The incoming request has been awaited");

        return (socket);

    }



    /**
     * Process an incoming HTTP request on the Socket that has been assigned
     * to this Processor.  Any exceptions that occur during processing must be
     * swallowed and dealt with.
     *
     * @param socket The socket on which we are connected to the client
     */
    private void process(Socket socket) {

        InputStream input = null;
        OutputStream output = null;

        // Process requests
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            processor.process(input, output);
        } catch (IOException e) {
            ;
        } catch (Throwable t) {
            log(sm.getString("coyoteProcessor.process"), t);
        }

        // Consuming leftover bytes
        try {
            int available = input.available();
            // skip any unread (bogus) bytes
            if (available > 0) {
                input.skip(available);
            }
        } catch (IOException e) {
            ;
        }

        // Closing the socket
        try {
            socket.close();
        } catch (IOException e) {
            ;
        }
        socket = null;

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = connector.getContainer().getLogger();
        if (logger != null)
            logger.log(threadName + " " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = connector.getContainer().getLogger();
        if (logger != null)
            logger.log(threadName + " " + message, throwable);

    }


    // ---------------------------------------------- Background Thread Methods


    /**
     * The background thread that listens for incoming TCP/IP connections and
     * hands them off to an appropriate processor.
     */
    public void run() {

        // Process requests until we receive a shutdown signal
        while (!stopped) {

            // Wait for the next socket to be assigned
            Socket socket = await();
            if (socket == null)
                continue;

            status = Constants.PROCESSOR_ACTIVE;

            // Process the request from this socket
            try {
                process(socket);
            } catch (Throwable t) {
                log(sm.getString("coyoteProcessor.run"), t);
            } finally {
                status = Constants.PROCESSOR_IDLE;
            }

            // Finish up this request
            connector.recycle(this);

        }

        // Tell threadStop() we have shut ourselves down successfully
        synchronized (threadSync) {
            threadSync.notifyAll();
        }

    }


    /**
     * Start the background processing thread.
     */
    private void threadStart() {

        log(sm.getString("coyoteProcessor.starting"));

        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

        if (debug >= 1)
            log(" Background thread has been started");

    }


    /**
     * Stop the background processing thread.
     */
    private void threadStop() {

        log(sm.getString("coyoteProcessor.stopping"));

        stopped = true;
        assign(null);

        if (status != Constants.PROCESSOR_IDLE) {
            // Only wait if the processor is actually processing a command
            synchronized (threadSync) {
                try {
                    threadSync.wait(5000);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }
        thread = null;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return null;//lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Start the background thread we will use for request processing.
     *
     * @exception LifecycleException if a fatal startup error occurs
     */
    public void start() throws LifecycleException {

        if (started)
            throw new LifecycleException
                (sm.getString("coyoteProcessor.alreadyStarted"));

        // Instantiate the Coyote processor
        String className = connector.getProcessorClassName();
        try {
            Class clazz = Class.forName(className);
            processor = (Processor) clazz.newInstance();
        } catch (Exception e) {
            throw new LifecycleException
                (sm.getString
                 ("coyoteProcessor.processorInstantiationFailed", e));
        }
        processor.setAdapter(this);

        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        threadStart();

        if (processor instanceof ActionHook) {
            ((ActionHook) processor).action(ActionCode.ACTION_START, null);
        }

    }


    /**
     * Stop the background thread we will use for request processing.
     *
     * @exception LifecycleException if a fatal shutdown error occurs
     */
    public void stop() throws LifecycleException {

        if (!started)
            throw new LifecycleException
                (sm.getString("coyoteProcessor.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        if (processor instanceof ActionHook) {
            ((ActionHook) processor).action(ActionCode.ACTION_STOP, null);
        }

        threadStop();

    }


}
