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


package org.apache.catalina.core;


import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;



/**
 * Standard implementation of the <b>Server</b> interface, available for use
 * (but not required) when deploying and starting Catalina.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class StandardServer
    implements Lifecycle, Server {


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of Connectors associated with this Server.
     */
    private Connector connectors[] = new Connector[0];


    /**
     * The set of Containers associated with this Server.
     */
    private Container containers[] = new Container[0];


    /**
     * Descriptive information about this Server implementation.
     */
    private static final String info =
	"org.apache.catalina.core.StandardServer/1.0";


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The port number on which we wait for shutdown commands.
     */
    private int port = 8005;


    /**
     * The shutdown command string we are looking for.
     */
    private String shutdown = "SHUTDOWN";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
	StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    private boolean started = false;


    // ------------------------------------------------------------- Properties


    /**
     * Return the port number we listen to for shutdown commands.
     */
    public int getPort() {

        return (this.port);

    }


    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     */
    public void setPort(int port) {

        this.port = port;

    }


    /**
     * Return the shutdown command string we are waiting for.
     */
    public String getShutdown() {

        return (this.shutdown);

    }


    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    public void setShutdown(String shutdown) {

        this.shutdown = shutdown;

    }


    // --------------------------------------------------------- Server Methods


    /**
     * Add a new Connector to the set of defined Connectors.  The newly
     * added Connector will have no associated Container until a later call
     * to <code>addContainer()</code> is made.
     *
     * @param connector The connector to be added
     */
    public void addConnector(Connector connector) {

	connector.setContainer(null);
	synchronized (connectors) {
	    Connector results[] = new Connector[connectors.length + 1];
	    for (int i = 0; i < connectors.length; i++)
		results[i] = connectors[i];
	    results[connectors.length] = connector;
	    connectors = results;
	}
	    
    }


    /**
     * Add a new Container to the set of defined Containers, and assign this
     * Container to all defined Connectors that have not yet been associated
     * with a Container will be associated with this one.
     *
     * @param container The container to be added
     *
     * @exception IllegalStateException if there are no unassociated
     *  Connectors to associate with (implying calls out of order)
     */
    public void addContainer(Container container) {

	synchronized (containers) {
	    Container results[] = new Container[containers.length + 1];
	    for (int i = 0; i < containers.length; i++)
		results[i] = containers[i];
	    results[containers.length] = container;
	    containers = results;
	}

	synchronized (connectors) {
	    for (int i = 0; i < connectors.length; i++) {
		if (connectors[i].getContainer() == null)
		    connectors[i].setContainer(container);
	    }
	}

    }


    /**
     * Wait until a proper shutdown command is received, then return.
     */
    public void await() {

        // Set up a server socket to wait on
        ServerSocket serverSocket = null;
	try {
	    serverSocket = new ServerSocket(port, 1);
	} catch (IOException e) {
	    System.err.println("StandardServer.await: create: " + e);
	    e.printStackTrace();
	    System.exit(1);
	}

	// Loop waiting for a connection and a valid command
	while (true) {

	    // Wait for the next connection
	    Socket socket = null;
	    InputStream stream = null;
	    try {
	        socket = serverSocket.accept();
		socket.setSoTimeout(10 * 1000);  // Ten seconds
		stream = socket.getInputStream();
	    } catch (IOException e) {
	        System.err.println("StandardServer.await: accept: " + e);
		e.printStackTrace();
		System.exit(1);
	    }

	    boolean localAddress = isSameAddress(socket.getLocalAddress(),
						 socket.getInetAddress());
	    if (!localAddress) {
		System.err.println("Invalid shutdown connection from " +
				   socket.getInetAddress() + " ignored");
		try {
		    socket.close();
		} catch (IOException e) {
		    ;
		}
		continue;
	    }

	    // Read a line of characters from the socket
	    StringBuffer command = new StringBuffer();
	    while (true) {
	        int ch = -1;
	        try {
		    ch = stream.read();
		} catch (IOException e) {
		    System.err.println("StandardServer.await: read: " + e);
		    e.printStackTrace();
		    ch = -1;
		}
		if (ch < 32)  // Control character or EOF terminates loop
		    break;
		command.append((char) ch);
	    }

	    // Close the socket now that we are done with it
	    try {
	        socket.close();
	    } catch (IOException e) {
	        ;
	    }

	    // Match against our command string
	    boolean match = command.toString().equals(shutdown);
	    if (match) {
		break;
	    } else
	        System.err.println("StandardServer.await: Invalid command '" +
				   command.toString() + "' received");

	}

	// Close the server socket and return
	try {
	    serverSocket.close();
	} catch (IOException e) {
	    ;
	}

    }


    /**
     * Return descriptive information about this Server implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return <code>true</code> if the specified client and server addresses
     * are the same.  This method works around a bug in the IBM 1.1.8 JVM on
     * Linux, where the address bytes are returned reversed in some
     * circumstances.
     *
     * @param server The server's InetAddress
     * @param client The client's InetAddress
     */
    private boolean isSameAddress(InetAddress server, InetAddress client) {

	// Compare the byte array versions of the two addresses
	byte serverAddr[] = server.getAddress();
	byte clientAddr[] = client.getAddress();
	if (serverAddr.length != clientAddr.length)
	    return (false);
	boolean match = true;
	for (int i = 0; i < serverAddr.length; i++) {
	    if (serverAddr[i] != clientAddr[i]) {
		match = false;
		break;
	    }
	}
	if (match)
	    return (true);

	// Compare the reversed form of the two addresses
	for (int i = 0; serverAddr.length < 4; i++) {
	    if (serverAddr[i] != clientAddr[(serverAddr.length-1)-i])
		return (false);
	}
	return (true);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a LifecycleEvent listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

	lifecycle.addLifecycleListener(listener);

    }


    /**
     * Remove a LifecycleEvent listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

	lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

	// Validate and update our current component state
	if (started)
	    throw new LifecycleException
		(sm.getString("standardServer.start.started"));

	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

	// Start our defined Containers first
	synchronized (containers) {
	    for (int i = 0; i < containers.length; i++) {
		if (containers[i] instanceof Lifecycle)
		    ((Lifecycle) containers[i]).start();
	    }
	}

	// Start our defined Connectors second
	synchronized (connectors) {
	    for (int i = 0; i < connectors.length; i++) {
		if (connectors[i] instanceof Lifecycle)
		    ((Lifecycle) connectors[i]).start();
	    }
	}

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

	// Validate and update our current component state
	if (!started)
	    throw new LifecycleException
		(sm.getString("standardServer.stop.notStarted"));
	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	// Stop our defined Connectors first
	for (int i = 0; i < connectors.length; i++) {
	    if (connectors[i] instanceof Lifecycle)
		((Lifecycle) connectors[i]).stop();
	}

	// Stop our defined Containers second
	for (int i = 0; i < containers.length; i++) {
	    if (containers[i] instanceof Lifecycle)
		((Lifecycle) containers[i]).stop();
	}

    }


}
