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


package org.apache.catalina.startup;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Stack;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.util.xml.SaxContext;
import org.apache.catalina.util.xml.XmlAction;
import org.apache.catalina.util.xml.XmlMapper;
import org.xml.sax.AttributeList;


/**
 * Startup/Shutdown shell program for Catalina.  The following command line
 * options are recognized:
 * <ul>
 * <li><b>-config {pathname}</b> - Set the pathname of the configuration file
 *     to be processed.  If a relative path is specified, it will be
 *     interpreted as relative to the directory pathname specified by the
 *     "catalina.home" system property.   [conf/server.xml]
 * <li><b>-help</b> - Display usage information.
 * <li><b>-stop</b> - Stop the currently running instance of Catalina.
 * </u>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class Catalina {


    // ----------------------------------------------------- Instance Variables


    /**
     * Pathname to the server configuration file.
     */
    private String configFile = "conf/server.xml";


    /**
     * Set the debugging detail level on our XmlMapper.
     */
    private boolean debug = false;


    /**
     * The server component we are starting or stopping
     */
    private Server server = null;


    /**
     * Are we starting a new server?
     */
    private boolean starting = false;


    /**
     * Are we stopping an existing server?
     */
    private boolean stopping = false;


    // ----------------------------------------------------------- Main Program


    /**
     * The application main program.
     *
     * @param args Command line arguments
     */
    public static void main(String args[]) {

	(new Catalina()).process(args);

    }


    /**
     * The instance main program.
     *
     * @param args Command line arguments
     */
    public void process(String args[]) {

	try {
	    if (arguments(args))
		execute();
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	}

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Set the server instance we are configuring.
     *
     * @param server The new server
     */
    public void setServer(Server server) {

        this.server = server;

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Process the specified command line arguments, and return
     * <code>true</code> if we should continue processing; otherwise
     * return <code>false</code>.
     *
     * @param args Command line arguments to process
     */
    private boolean arguments(String args[]) {

	boolean isConfig = false;

	for (int i = 0; i < args.length; i++) {
	    if (isConfig) {
		configFile = args[i];
		isConfig = false;
	    } else if (args[i].equals("-config")) {
	        isConfig = true;
	    } else if (args[i].equals("-debug")) {
		debug = true;
	    } else if (args[i].equals("-help")) {
		usage();
		return (false);
	    } else if (args[i].equals("start")) {
	        starting = true;
	    } else if (args[i].equals("stop")) {
	        stopping = true;
	    } else {
		usage();
	    }
	}

	return (true);

    }


    /**
     * Return a File object representing our configuration file.
     */
    private File configFile() {

	File file = new File(configFile);
	if (!file.isAbsolute())
	    file = new File(System.getProperty("catalina.home") +
			    File.separator + configFile);
	return (file);

    }


    /**
     * Create and configure the XmlMapper we will be using for startup.
     */
    private XmlMapper createStartMapper() {

	// Initialize the mapper
	XmlMapper mapper = new XmlMapper();
	if (debug)
	    mapper.setDebug(999);
	mapper.setValidating(false);

	// Configure the actions we will be using

	mapper.addRule("Server", mapper.objectCreate
		     ("org.apache.catalina.core.StandardServer", "className"));
	mapper.addRule("Server", mapper.setProperties());
        mapper.addRule("Server", mapper.addChild
		       ("setServer", "org.apache.catalina.Server"));

	mapper.addRule("Server/Connector", mapper.objectCreate
		       ("org.apache.catalina.connector.http.HttpConnector",
			"className"));
	mapper.addRule("Server/Connector", mapper.setProperties());
	mapper.addRule("Server/Connector", mapper.addChild
		       ("addConnector", "org.apache.catalina.Connector"));

        mapper.addRule("Server/Connector/Factory", mapper.objectCreate
                       ("org.apache.catalina.net.DefaultServerSocketFactory",
                        "className"));
        mapper.addRule("Server/Connector/Factory", mapper.setProperties());
        mapper.addRule("Server/Connector/Factory", mapper.addChild
                       ("setFactory",
                        "org.apache.catalina.net.ServerSocketFactory"));

	mapper.addRule("Server/Connector/Listener", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Connector/Listener", mapper.setProperties());
	mapper.addRule("Server/Connector/Listener", mapper.addChild
		       ("addLifecycleListener",
			"org.apache.catalina.LifecycleListener"));

	mapper.addRule("Server/Engine", mapper.objectCreate
		       ("org.apache.catalina.core.StandardEngine",
			"className"));
	mapper.addRule("Server/Engine", mapper.setProperties());
	mapper.addRule("Server/Engine",
		       new LifecycleListenerAction
			   ("org.apache.catalina.startup.EngineConfig",
			    "configClass"));
	mapper.addRule("Server/Engine", mapper.addChild
		       ("addContainer", "org.apache.catalina.Container"));

	mapper.addRule("Server/Engine/Host", mapper.objectCreate
		       ("org.apache.catalina.core.StandardHost",
			"className"));
	mapper.addRule("Server/Engine/Host", mapper.setProperties());
	mapper.addRule("Server/Engine/Host",
		       new LifecycleListenerAction
			   ("org.apache.catalina.startup.HostConfig",
			    "configClass"));
	mapper.addRule("Server/Engine/Host", mapper.addChild
		       ("addChild", "org.apache.catalina.Container"));

	mapper.addRule("Server/Engine/Host/Context", mapper.objectCreate
		       ("org.apache.catalina.core.StandardContext",
			"className"));
	mapper.addRule("Server/Engine/Host/Context", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context",
		       new LifecycleListenerAction
			   ("org.apache.catalina.startup.ContextConfig",
			    "configClass"));
	mapper.addRule("Server/Engine/Host/Context", mapper.addChild
		       ("addChild", "org.apache.catalina.Container"));

	mapper.addRule("Server/Engine/Host/Context/InstanceListener",
		       mapper.methodSetter("addInstanceListener", 0));

	mapper.addRule("Server/Engine/Host/Context/Listener",
		       mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Context/Listener",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Listener", mapper.addChild
		       ("addLifecycleListener",
			"org.apache.catalina.LifecycleListener"));

	mapper.addRule("Server/Engine/Host/Context/Loader",
		       mapper.objectCreate
		       ("org.apache.catalina.core.StandardLoader",
			"className"));
	mapper.addRule("Server/Engine/Host/Context/Loader",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Loader", mapper.addChild
		       ("setLoader", "org.apache.catalina.Loader"));

	mapper.addRule("Server/Engine/Host/Context/Logger", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Context/Logger",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Logger", mapper.addChild
		       ("setLogger", "org.apache.catalina.Logger"));

	mapper.addRule("Server/Engine/Host/Context/Manager",
		       mapper.objectCreate
		       ("org.apache.catalina.session.StandardManager",
			"className"));
	mapper.addRule("Server/Engine/Host/Context/Manager",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Manager", mapper.addChild
		       ("setManager", "org.apache.catalina.Manager"));

	mapper.addRule("Server/Engine/Host/Context/Realm", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Context/Realm",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Realm", mapper.addChild
		       ("setRealm", "org.apache.catalina.Realm"));

	mapper.addRule("Server/Engine/Host/Context/Resources",
		       mapper.objectCreate
		       ("org.apache.catalina.core.StandardResources",
			"className"));
	mapper.addRule("Server/Engine/Host/Context/Resources",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Resources", mapper.addChild
		       ("setResources", "org.apache.catalina.Resources"));

	mapper.addRule("Server/Engine/Host/Context/Valve", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Context/Valve",
		       mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Context/Valve", mapper.addChild
		       ("addValve", "org.apache.catalina.Valve"));

	mapper.addRule("Server/Engine/Host/Context/WrapperLifecycle",
		       mapper.methodSetter("addWrapperLifecycle", 0));

	mapper.addRule("Server/Engine/Host/Context/WrapperListener",
		       mapper.methodSetter("addWrapperListener", 0));

	mapper.addRule("Server/Engine/Host/Listener", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Listener", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Listener", mapper.addChild
		       ("addLifecycleListener",
			"org.apache.catalina.LifecycleListener"));

	mapper.addRule("Server/Engine/Host/Loader", mapper.objectCreate
		       ("org.apache.catalina.core.StandardLoader",
			"className"));
	mapper.addRule("Server/Engine/Host/Loader", mapper.setProperties());
	mapper.addRule("Engine/Host/Loader", mapper.addChild
		       ("setLoader", "org.apache.catalina.Loader"));

	mapper.addRule("Server/Engine/Host/Logger", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Logger", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Logger", mapper.addChild
		       ("setLogger", "org.apache.catalina.Logger"));

	mapper.addRule("Server/Engine/Host/Manager", mapper.objectCreate
		       ("org.apache.catalina.session.StandardManager",
			"className"));
	mapper.addRule("Server/Engine/Host/Manager", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Manager", mapper.addChild
		       ("setManager", "org.apache.catalina.Manager"));

	mapper.addRule("Server/Engine/Host/Realm", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Realm", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Realm", mapper.addChild
		       ("setRealm", "org.apache.catalina.Realm"));

	mapper.addRule("Server/Engine/Host/Resources", mapper.objectCreate
		       ("org.apache.catalina.core.StandardResources",
			"className"));
	mapper.addRule("Server/Engine/Host/Resources", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Resources", mapper.addChild
		       ("setResources", "org.apache.catalina.Resources"));

	mapper.addRule("Server/Engine/Host/Valve", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Host/Valve", mapper.setProperties());
	mapper.addRule("Server/Engine/Host/Valve", mapper.addChild
		       ("addValve", "org.apache.catalina.Valve"));

	mapper.addRule("Server/Engine/Listener", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Listener", mapper.setProperties());
	mapper.addRule("Server/Engine/Listener", mapper.addChild
		       ("addLifecycleListener",
			"org.apache.catalina.LifecycleListener"));

	mapper.addRule("Server/Engine/Loader", mapper.objectCreate
		       ("org.apache.catalina.core.StandardLoader",
			"className"));
	mapper.addRule("Server/Engine/Loader", mapper.setProperties());
	mapper.addRule("Server/Engine/Loader", mapper.addChild
		       ("setLoader", "org.apache.catalina.Loader"));

	mapper.addRule("Server/Engine/Logger", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Logger", mapper.setProperties());
	mapper.addRule("Server/Engine/Logger", mapper.addChild
		       ("setLogger", "org.apache.catalina.Logger"));

	mapper.addRule("Server/Engine/Manager", mapper.objectCreate
		       ("org.apache.catalina.session.StandardManager",
			"className"));
	mapper.addRule("Server/Engine/Manager", mapper.setProperties());
	mapper.addRule("Server/Engine/Manager", mapper.addChild
		       ("setManager", "org.apache.catalina.Manager"));

	mapper.addRule("Server/Engine/Realm", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Realm", mapper.setProperties());
	mapper.addRule("Server/Engine/Realm", mapper.addChild
		       ("setRealm", "org.apache.catalina.Realm"));

	mapper.addRule("Server/Engine/Resources", mapper.objectCreate
		       ("org.apache.catalina.core.StandardResources",
			"className"));
	mapper.addRule("Server/Engine/Resources", mapper.setProperties());
	mapper.addRule("Server/Engine/Resources", mapper.addChild
		       ("setResources", "org.apache.catalina.Resources"));

	mapper.addRule("Server/Engine/Valve", mapper.objectCreate
		       (null, "className"));
	mapper.addRule("Server/Engine/Valve", mapper.setProperties());
	mapper.addRule("Server/Engine/Valve", mapper.addChild
		       ("addValve", "org.apache.catalina.Valve"));

	return (mapper);

    }


    /**
     * Create and configure the XmlMapper we will be using for shutdown.
     */
    private XmlMapper createStopMapper() {

        // Initialize the mapper
        XmlMapper mapper = new XmlMapper();
	//        mapper.setDebug(999);

        // Configure the actions we will be using

        mapper.addRule("Server", mapper.objectCreate
		       ("org.apache.catalina.core.StandardServer",
			"className"));
        mapper.addRule("Server", mapper.setProperties());
        mapper.addRule("Server", mapper.addChild
		       ("setServer", "org.apache.catalina.Server"));

	return (mapper);

    }


    /**
     * Execute the processing that has been configured from the command line.
     */
    private void execute() throws Exception {

        if (starting)
	    start();
	else if (stopping)
	    stop();

    }


    /**
     * Start a new server instance.
     */
    private void start() {

        // Create and execute our mapper
        XmlMapper mapper = createStartMapper();
	File file = configFile();
	try {
	    mapper.readXml(file, this);
	} catch (InvocationTargetException e) {
	    System.out.println("Catalina.start: InvocationTargetException");
	    e.getTargetException().printStackTrace(System.out);
	} catch (Exception e) {
	    System.out.println("Catalina.start: " + e);
	    e.printStackTrace(System.out);
	    System.exit(1);
	}

	// Start the new server
	if (server instanceof Lifecycle) {
	    try {
	        ((Lifecycle) server).start();
	    } catch (LifecycleException e) {
	        System.out.println("Catalina.start: " + e);
		e.printStackTrace(System.out);
	    }
	}


	// Wait for the server to be told to shut down
	server.await();

	// Shut down the server
	if (server instanceof Lifecycle) {
	    try {
	        ((Lifecycle) server).stop();
	    } catch (LifecycleException e) {
	        System.out.println("Catalina.stop: " + e);
	        e.printStackTrace(System.out);
	    }
	}

    }


    /**
     * Stop an existing server instance.
     */
    private void stop() {

      // Create and execute our mapper
      XmlMapper mapper = createStopMapper();
      File file = configFile();
	try {
	    mapper.readXml(file, this);
	} catch (Exception e) {
	    System.out.println("Catalina.stop: " + e);
	    e.printStackTrace(System.out);
	    System.exit(1);
	}

      // Stop the existing server
      try {
	  Socket socket = new Socket("localhost", server.getPort());
	  OutputStream stream = socket.getOutputStream();
	  String shutdown = server.getShutdown();
	  for (int i = 0; i < shutdown.length(); i++)
	      stream.write(shutdown.charAt(i));
	  stream.flush();
	  stream.close();
	  socket.close();
      } catch (IOException e) {
	  System.out.println("Catalina.stop: " + e);
	  e.printStackTrace(System.out);
	  System.exit(1);
      }


    }


    /**
     * Print usage information for this application.
     */
    private void usage() {

	System.out.println("usage: java org.apache.catalina.startup.Catalina" +
			" [ -config {pathname} ] [ -debug ] { start | stop }");

    }


}


// --------------------------------------------------- Private Classes


/**
 * Class that adds a LifecycleListener for the top class on the stack.
 */

final class LifecycleListenerAction extends XmlAction {


    /**
     * Construct a new action.
     *
     * @param listenerClass Name of the listener class to create
     */
    public LifecycleListenerAction(String listenerClass) {

	this(listenerClass, null);

    }


    /**
     * Construct a new action.
     *
     * @param listenerClass Name of the listener class to create
     * @param attributeName Name of an attribute optionally overriding
     *  the listener class name
     */
    public LifecycleListenerAction(String listenerClass,
				   String attributeName) {

	super();
	this.listenerClass = listenerClass;
	this.attributeName = attributeName;

    }


    /**
     * The attribute name of the optional override class (if any).
     */
    private String attributeName = null;


    /**
     * The class name of the listener class to create.
     */
    private String listenerClass = null;


    /**
     * Add the requested lifecycle listener.
     */
    public void start(SaxContext context) throws Exception {

	// Create a new listener object
	String className = listenerClass;
	if (attributeName != null) {
	    int top = context.getTagCount() - 1;
	    AttributeList attributes = context.getAttributeList(top);
	    if (attributes.getValue(attributeName) != null)
		className = attributes.getValue(attributeName);
	}
	if (context.getDebug() >= 1)
	    context.log("Add " + className + " listener");
	Class clazz = Class.forName(className);
	LifecycleListener listener =
	    (LifecycleListener) clazz.newInstance();

	// Add it to the top object on the stack
	Stack stack = context.getObjectStack();
	Lifecycle top = (Lifecycle) stack.peek();
	top.addLifecycleListener(listener);

    }


}
