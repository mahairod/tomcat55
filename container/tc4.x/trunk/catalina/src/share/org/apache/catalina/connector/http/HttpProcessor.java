/*
 * $Header$
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


package org.apache.catalina.connector.http;


import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.StringParser;


/**
 * Implementation of a request processor (and its associated thread) that may
 * be used by an HttpConnector to process individual requests.  The connector
 * will allocate a processor from its pool, assign a particular socket to it,
 * and the processor will then execute the processing required to complete
 * the request.  When the processor is completed, it will recycle itself.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

final class HttpProcessor
    implements Lifecycle, Runnable {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new HttpProcessor associated with the specified connector.
     *
     * @param connector HttpConnector that owns this processor
     * @param id Identifier of this HttpProcessor (unique per connector)
     */
    public HttpProcessor(HttpConnector connector, int id) {

	super();
	this.connector = connector;
	this.debug = connector.getDebug();
	this.id = id;
	this.request = (HttpRequest) connector.createRequest();
	this.response = (HttpResponse) connector.createResponse();
	this.threadName =
	  "HttpProcessor[" + connector.getPort() + "][" + id + "]";

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Is there a new socket available?
     */
    private boolean available = false;


    /**
     * The HttpConnector with which this processor is associated.
     */
    private HttpConnector connector = null;


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
     * The string parser we will use for parsing request lines.
     */
    private StringParser parser = new StringParser();


    /**
     * The HTTP request object we will pass to our associated container.
     */
    private HttpRequest request = null;


    /**
     * The HTTP response object we will pass to our associated container.
     */
    private HttpResponse response = null;


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
    private String threadSync = "";


    /**
     * Keep alive indicator.
     */
    private boolean keepAlive = false;


    /**
     * HTTP/1.1 client.
     */
    private boolean http11 = true;


    /**
     * True if the client has asked to recieve a request acknoledgement. If so
     * the server will send a preliminary 100 Continue response just after it
     * has successfully parsed the request headers, and before starting 
     * reading the request entity body.
     */
    private boolean sendAck = false;


    /**
     * Ack string when pipelining HTTP requests.
     */
    private static final byte[] ack = 
        (new String("HTTP/1.1 100 Continue\r\n\r\n")).getBytes();


    /**
     * CRLF.
     */
    private static final byte[] CRLF = (new String("\r\n")).getBytes();


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


    /**
     * Parse the value of an <code>Accept-Language</code> header, and add
     * the corresponding Locales to the current request.
     *
     * @param value The value of the <code>Accept-Language</code> header.
     */
    private void parseAcceptLanguage(String value) {

	// Store the accumulated languages that have been requested in
	// a local collection, sorted by the quality value (so we can
	// add Locales in descending order).  The values will be ArrayLists
	// containing the corresponding Locales to be added
	TreeMap locales = new TreeMap();

	// Preprocess the value to remove all whitespace
	int white = value.indexOf(' ');
	if (white < 0)
	    white = value.indexOf('\t');
	if (white >= 0) {
	    StringBuffer sb = new StringBuffer();
	    int len = value.length();
	    for (int i = 0; i < len; i++) {
		char ch = value.charAt(i);
		if ((ch != ' ') && (ch != '\t'))
		    sb.append(ch);
	    }
	    value = sb.toString();
	}

	// Process each comma-delimited language specification
	parser.setString(value);	// ASSERT: parser is available to us
	int length = parser.getLength();
	while (true) {

	    // Extract the next comma-delimited entry
	    int start = parser.getIndex();
	    if (start >= length)
		break;
	    int end = parser.findChar(',');
	    String entry = parser.extract(start, end).trim();
	    parser.advance();	// For the following entry

	    // Extract the quality factor for this entry
	    double quality = 1.0;
	    int semi = entry.indexOf(";q=");
	    if (semi >= 0) {
		try {
		    quality = Double.parseDouble(entry.substring(semi + 3));
		} catch (NumberFormatException e) {
		    quality = 0.0;
		}
		entry = entry.substring(0, semi);
	    }

	    // Skip entries we are not going to keep track of
	    if (quality < 0.00005)
		continue;	// Zero (or effectively zero) quality factors
	    if ("*".equals(entry))
		continue;	// FIXME - "*" entries are not handled

	    // Extract the language and country for this entry
	    String language = null;
	    String country = null;
	    int dash = entry.indexOf("-");
	    if (dash < 0) {
		language = entry;
		country = "";
	    } else {
		language = entry.substring(0, dash);
		country = entry.substring(dash + 1);
	    }

	    // Add a new Locale to the list of Locales for this quality level
	    Locale locale = new Locale(language, country);
	    Double key = new Double(-quality);	// Reverse the order
	    ArrayList values = (ArrayList) locales.get(key);
	    if (values == null) {
		values = new ArrayList();
		locales.put(key, values);
	    }
	    values.add(locale);

	}

	// Process the quality values in highest->lowest order (due to
	// negating the Double value when creating the key)
	Iterator keys = locales.keySet().iterator();
	while (keys.hasNext()) {
	    Double key = (Double) keys.next();
	    ArrayList list = (ArrayList) locales.get(key);
	    Iterator values = list.iterator();
	    while (values.hasNext()) {
		Locale locale = (Locale) values.next();
		if (debug >= 1)
		    log(" Adding locale '" + locale + "'");
		request.addLocale(locale);
	    }
	}

    }


    /**
     * Parse and record the connection parameters related to this request.
     *
     * @param socket The socket on which we are connected
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a parsing error occurs
     */
    private void parseConnection(Socket socket)
        throws IOException, ServletException {

	if (debug >= 2)
	    log("  parseConnection: address=" + socket.getInetAddress() +
		", port=" + connector.getPort());
	((HttpRequestImpl) request).setInet(socket.getInetAddress());
	request.setServerPort(connector.getPort());

    }


    /**
     * Parse the incoming HTTP request headers, and set the appropriate
     * request headers.
     *
     * @param input The input stream connected to our socket
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a parsing error occurs
     */
    private void parseHeaders(InputStream input)
        throws IOException, ServletException {

	while (true) {

	    // Read the next header line
	    String line = read(input);
	    if ((line == null) || (line.length() < 1))
		break;

	    // Parse the header name and value
	    int colon = line.indexOf(":");
	    if (colon < 0)
		throw new ServletException
		    (sm.getString("httpProcessor.parseHeaders.colon"));
	    String name = line.substring(0, colon).trim();
	    String match = name.toLowerCase();
	    String value = line.substring(colon + 1).trim();
	    if (debug >= 1)
	        log(" Header " + name + " = " + value);

	    // Set the corresponding request headers
	    if (match.equals("authorization")) {
		request.setAuthorization(value);
	    } else if (match.equals("accept-language")) {
		parseAcceptLanguage(value);
	    } else if (match.equals("cookie")) {
		Cookie cookies[] = RequestUtil.parseCookieHeader(value);
		for (int i = 0; i < cookies.length; i++) {
		    if (cookies[i].getName().equals
			(Globals.SESSION_COOKIE_NAME)) {

			// Override anything requested in the URL
			request.setRequestedSessionId(cookies[i].getValue());
			request.setRequestedSessionCookie(true);
			request.setRequestedSessionURL(false);
			if (debug >= 1)
			  log(" Requested cookie session id is " +
			      ((HttpServletRequest) request.getRequest())
                              .getRequestedSessionId());
		    } else
			request.addCookie(cookies[i]);
		}
	    } else if (match.equals("content-length")) {
		int n = -1;
		try {
		    n = Integer.parseInt(value);
		} catch (Exception e) {
		    throw new ServletException
			(sm.getString
                         ("httpProcessor.parseHeaders.contentLength"));
		}
		request.setContentLength(n);
	    } else if (match.equals("content-type")) {
		request.setContentType(value);
	    } else if (match.equals("host")) {
		int n = value.indexOf(":");
		if (n < 0)
		    request.setServerName(value);
		else {
		    request.setServerName(value.substring(0, n).trim());
		    int port = 80;
		    try {
			port = Integer.parseInt(value.substring(n+1).trim());
		    } catch (Exception e) {
			throw new ServletException
			    (sm.getString
                             ("httpProcessor.parseHeaders.portNumber"));
		    }
		    request.setServerPort(port);
		}
	    } else if (match.equals("connection")) {
                if ("close".equalsIgnoreCase(value)) {
                    keepAlive = false;
                }
                /*
                  if ("keep-alive".equalsIgnoreCase(value)) {
                  keepAlive = true;
                  }
                */
            } else if (match.equals("expect")) {
                if ("100-continue".equalsIgnoreCase(value))
                    sendAck = true;
                else
                    throw new ServletException
                        (sm.getString
                         ("httpProcessor.parseHeaders.unknownExpectation"));
	    }
	    if (!match.equals("authorization"))
		request.addHeader(name, value);
	}

    }


    /**
     * Parse the incoming HTTP request and set the corresponding HTTP request
     * properties.
     *
     * @param input The input stream attached to our socket
     * @param output The output stream of the socket
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a parsing error occurs
     */
    private void parseRequest(InputStream input, OutputStream output)
        throws IOException, ServletException {

	// Parse the incoming request line
	String line = read(input);
	if (line == null)
	    throw new EOFException
		(sm.getString("httpProcessor.parseRequest.read"));
	parser.setString(line);

	int methodStart = parser.skipWhite();
	int methodEnd = parser.findWhite();
	int uriStart = parser.skipWhite();
	int uriEnd = parser.findWhite();
	int protocolStart = parser.skipWhite();
	int protocolEnd = parser.findWhite();

	String method = parser.extract(methodStart, methodEnd);
	String uri = parser.extract(uriStart, uriEnd);
	String protocol = parser.extract(protocolStart, protocolEnd);
	if (protocol.length() == 0)
	    protocol = "HTTP/0.9";

        // Now check if the connection should be kept alive after parsing the
        // request.
        if ( protocol.equals("HTTP/1.1") ) {
            http11 = true;
            sendAck = false;
        } else {
            http11 = false;
            sendAck = false;
            // For HTTP/1.0, connection are not persistent by default,
            // unless specified with a Connection: Keep-Alive header.
            keepAlive = false;
        }
        
	// Validate the incoming request line
	if (method.length() < 1) {
	    throw new ServletException
		(sm.getString("httpProcessor.parseRequest.method"));
	} else if (uri.length() < 1) {
	    throw new ServletException
		(sm.getString("httpProcessor.parseRequest.uri"));
	}

	// Parse any query parameters out of the request URI
	int question = uri.indexOf("?");
	if (question >= 0) {
	    request.setQueryString(uri.substring(question + 1));
	    if (debug >= 1)
	        log(" Query string is " +
		    ((HttpServletRequest) request.getRequest())
                    .getQueryString());
	    uri = uri.substring(0, question);
	} else
	    request.setQueryString(null);

	// Parse any requested session ID out of the request URI
	int semicolon = uri.indexOf(match);
	if (semicolon >= 0) {
	    String rest = uri.substring(semicolon + match.length());
	    int semicolon2 = rest.indexOf(";");
	    if (semicolon2 >= 0) {
		request.setRequestedSessionId(rest.substring(0, semicolon2));
		rest = rest.substring(semicolon2);
	    } else {
		request.setRequestedSessionId(rest);
		rest = "";
	    }
	    request.setRequestedSessionURL(true);
	    uri = uri.substring(0, semicolon) + rest;
	    if (debug >= 1)
	        log(" Requested URL session id is " +
		    ((HttpServletRequest) request.getRequest())
                    .getRequestedSessionId());
	} else {
	    request.setRequestedSessionId(null);
	    request.setRequestedSessionURL(false);
	}

	// Perform decoding on the request URI if necessary
	if ((uri.indexOf('%') >= 0) || (uri.indexOf('+') >= 0)) {
	    try {
		uri = URLDecoder.decode(uri);
	    } catch (Exception e) {
		throw new ServletException
		    (sm.getString("httpProcessor.parseRequest.decode"));
	    }
	}

	// Set the corresponding request properties
	((HttpRequest) request).setMethod(method);
	request.setProtocol(protocol);
	((HttpRequest) request).setRequestURI(uri);
	request.setSecure(connector.getSecure());
	request.setScheme(connector.getScheme());

	if (debug >= 1)
	    log(" Request is '" + method + "' for '" + uri +
		"' with protocol '" + protocol + "'");

    }


    /**
     * Send a confirmation that a request has been processed when pipelining.
     * HTTP/1.1 100 Continue is sent back to the client.
     *
     * @param output Socket output stream
     */
    private void ackRequest(OutputStream output)
        throws IOException {
        if (sendAck)
            output.write(ack);
    }


    /**
     * Process an incoming HTTP request on the Socket that has been assigned
     * to this Processor.  Any exceptions that occur during processing must be
     * swallowed and dealt with.
     *
     * @param socket The socket on which we are connected to the client
     */
    private void process(Socket socket) {

	boolean ok = true;
	InputStream input = null;
	OutputStream output = null;

	// Construct and initialize the objects we will need
	try {
	    input = new BufferedInputStream(socket.getInputStream(),
	    				    connector.getBufferSize());
	} catch (Exception e) {
	    log("process.create", e);
	    ok = false;
	}

        keepAlive = true;

        while (ok && keepAlive) {
            
            //System.out.println("Parsing request");
            
            try {
                request.setStream(input);
                request.setResponse(response);
                output = socket.getOutputStream();
                response.setStream(output);
                response.setRequest(request);
                ((HttpServletResponse) response.getResponse()).setHeader
                    ("Server", Constants.ServerInfo);
            } catch (Exception e) {
                log("process.create", e);
                ok = false;
            }

            // Parse the incoming request
            try {
                if (ok) {
                    parseConnection(socket);
                    parseRequest(input, output);
                    if (!request.getRequest().getProtocol()
                        .startsWith("HTTP/0"))
                        parseHeaders(input);
                    if (http11) {
                        // Sending a request acknowledge back to the client if
                        // requested.
                        ackRequest(output);
                        // If the protocol is HTTP/1.1, chunking is allowed.
                        ((HttpResponseImpl) response).setAllowChunking(true);
                    }
                }
            } catch (EOFException e) {
                //                log("process.parse:  EOFException:  " + e);
                ok = false;
            } catch (Exception e) {
                try {
                    log("process.parse", e);
                    ((HttpServletResponse) response.getResponse()).sendError
                        (HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }

            // Ask our Container to process this request
            try {
                ((HttpServletResponse) response).addDateHeader
                    ("Date", System.currentTimeMillis());
                if (ok) {
                    connector.getContainer().invoke(request, response);
                }
            } catch (ServletException e) {
                log("process.invoke", e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError
                        (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            } catch (Throwable e) {
                log("process.invoke", e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError
                        (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }
            
            // Finish up the handling of the request
            try {
                if (ok) {
                    ((HttpResponseImpl) response).finishResponse();
                    ((HttpRequestImpl) request).finishRequest();
                }
                if (output != null)
                    output.flush();
            } catch (IOException e) {
                ok = false;
            }
            
            // We have to check if the connection closure has been requested
            // by the application or the response stream (in case of HTTP/1.0
            // and keep-alive).
            if ( "close".equals(((HttpResponseImpl) response)
                                .getHeader("Connection")) ) {
                keepAlive = false;
            }
            // If the status is an error, the connection is closed
            if ( ((HttpResponseImpl) response).getStatus() >= 400 ) {
                keepAlive = false;
            }
            
            // Recycling the request and the response objects
            request.recycle();
            response.recycle();
            
        }
        
	try {
            socket.close();
	} catch (IOException e) {
	    ;
	}
	socket = null;

    }


    /**
     * Read a line from the specified input stream, and strip off the
     * trailing carriage return and newline (if any).  Return the remaining
     * characters that were read as a String.
     *
     * @param input The input stream connected to our socket
     *
     * @return The line that was read, or <code>null</code> if end-of-file
     *  was encountered
     *
     * @exception IOException if an input/output error occurs
     */
    private String read(InputStream input) throws IOException {

	StringBuffer sb = new StringBuffer();
	while (true) {
	    int ch = input.read();
	    if (ch < 0) {
		if (sb.length() == 0) {
		    return (null);
		} else {
		    break;
		}
	    } else if (ch == '\r') {
		continue;
	    } else if (ch == '\n') {
		break;
	    }
	    sb.append((char) ch);
	}
	if (debug >= 2)
	    log("  Read: " + sb.toString());
	return (sb.toString());

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

	    // Process the request from this socket
	    process(socket);

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

	log(sm.getString("httpProcessor.starting"));

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

	log(sm.getString("httpProcessor.stopping"));

	stopped = true;
        assign(null);
	synchronized (threadSync) {
	    try {
		threadSync.wait(5000);
	    } catch (InterruptedException e) {
		;
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
		(sm.getString("httpProcessor.alreadyStarted"));
	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

	threadStart();

    }


    /**
     * Stop the background thread we will use for request processing.
     *
     * @exception LifecycleException if a fatal shutdown error occurs
     */
    public void stop() throws LifecycleException {

	if (!started)
	    throw new LifecycleException
		(sm.getString("httpProcessor.notStarted"));
	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	threadStop();

    }


}
