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


package org.apache.catalina.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.StringManager;


/**
 * Servlet that enables remote management of the web applications installed
 * within the same virtual host as this web application is.  Normally, this
 * functionality will be protected by a security constraint in the web
 * application deployment descriptor.  However, this requirement can be
 * relaxed during testing.
 * <p>
 * This servlet examines the value returned by <code>getPathInfo()</code>
 * and related query parameters to determine what action is being requested.
 * The following actions and parameters (starting after the servlet path)
 * are supported:
 * <ul>
 * <li><b>/list</b> - Return a list of the context paths of all currently
 *     running web applications in this virtual host.
 * <li><b>/deploy?path=/xxx&war={war-url}</b> - Deploy a new web application
 *     attached to context path <code>/xxx</code>, based on the contents of
 *     the web application archive found at the specified URL.
 * <li><b>/reload?path=/xxx</b> - Reload the Java classes and resources for
 *     the application at the specified path, but do not reread the web.xml
 *     configuration files.
 * <li><b>/undeploy?path=/xxx</b> - Remove any web application attached to
 *     context path <code>/xxx</code> from this virtual host.
 * </ul>
 * <p>
 * <b>NOTE</b> - Attempting to reload or undeploy the application containing
 * this servlet itself will not succeed.  Therefore, this servlet should
 * generally be deployed as a separate web application within the virtual host
 * to be managed.
 * <p>
 * The following servlet initialization parameters are recognized:
 * <ul>
 * <li><b>debug</b> - The debugging detail level that controls the amount
 *     of information that is logged by this servlet.  Default is zero.
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class ManagerServlet
    extends HttpServlet {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context container associated with our web application.
     */
    private Context context = null;


    /**
     * The debugging detail level for this servlet.
     */
    private int debug = 1;


    /**
     * The Host container that contains our own web application's Context,
     * along with the associated Contexts for web applications that we
     * are managing.
     */
    private Host host = null;


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
      StringManager.getManager(Constants.Package);


    // --------------------------------------------------------- Public Methods


    /**
     * Finalize this servlet.
     */
    public void destroy() {

	;	// No actions necessary

    }


    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws IOException, ServletException {

	// Identify the request parameters that we need
	String command = request.getPathInfo();
	if (command == null)
	    command = request.getServletPath();
	String path = request.getParameter("path");
	String war = request.getParameter("war");

	// Prepare our output writer to generate the response message
	response.setContentType("text/plain");
	PrintWriter writer = response.getWriter();

	// Process the requested command
	if (command == null) {
	    writer.println(sm.getString("managerServlet.noCommand"));
	} else if (command.equals("/deploy")) {
	    deploy(writer, path, war);
	} else if (command.equals("/list")) {
	    list(writer);
	} else if (command.equals("/reload")) {
	    reload(writer, path);
	} else if (command.equals("/undeploy")) {
	    undeploy(writer, path);
	} else {
	    writer.println(sm.getString("managerServlet.unknownCommand",
					command));
	}

	// Finish up the response
	writer.flush();
	writer.close();

    }


    /**
     * Initialize this servlet.
     */
    public void init() throws ServletException {

	// Set our properties from the initialization parameters
	String value = null;
	try {
	    value = getServletConfig().getInitParameter("debug");
	    debug = Integer.parseInt(value);
	} catch (Throwable t) {
	    ;
	}

	// Identify the internal container resources we need
	Wrapper wrapper = (Wrapper) getServletConfig();
	context = (Context) wrapper.getParent();
	host = (Host) context.getParent();

	// Log debugging messages as necessary
	if (debug >= 1) {
	    log("init: Associated with Host '" + host.getName() + "'");
	}

    }



    // -------------------------------------------------------- Private Methods


    /**
     * Deploy an application for the specified path from the specified
     * web application archive.
     *
     * @param writer Writer to render results to
     * @param path Context path of the application to be deployed
     * @param war URL of the web application archive to be deployed
     */
    private void deploy(PrintWriter writer, String path, String war) {

        if (debug >= 1)
	    log("deploy: Deploying web application at '" + path +
		"' from '" + war + "'");

	try {
	  Context context = (Context) host.findChild(path);
	  if (context != null) {
	      writer.println(sm.getString("managerServlet.alreadyContext",
					  path));
	      return;
	  }
	  ; // FIXME - deploy()
	  writer.println(sm.getString("managerServlet.deployed", path));
	} catch (Throwable t) {
	    getServletContext().log("ManagerServlet.deploy[" + path + "]", t);
	    writer.println(sm.getString("managerServlet.exception",
					t.toString()));
	}

    }


    /**
     * Render a list of the currently active Contexts in our virtual host.
     *
     * @param writer Writer to render to
     */
    private void list(PrintWriter writer) {

        if (debug >= 1)
	    log("list: Listing contexts for virtual host '" +
		host.getName() + "'");

        writer.println(sm.getString("managerServlet.listed", host.getName()));
	Container children[] = host.findChildren();
	for (int i = 0; i < children.length; i++)
	    writer.println(children[i].getName());


    }


    /**
     * Reload the web application at the specified context path.
     *
     * @param writer Writer to render to
     * @param path Context path of the application to be restarted
     */
    private void reload(PrintWriter writer, String path) {

        if (debug >= 1)
	    log("restart: Reloading web application at '" + path + "'");

        try {
	    Context context = (Context) host.findChild(path);
	    if (context == null) {
	        writer.println(sm.getString("managerServlet.noContext", path));
		return;
	    }
	    context.reload();
	    writer.println(sm.getString("managerServlet.reloaded", path));
	} catch (Throwable t) {
	    getServletContext().log("ManagerServlet.reload[" + path + "]", t);
	    writer.println(sm.getString("managerServlet.exception",
					t.toString()));
	}

    }


    /**
     * Undeploy the web application at the specified context path.
     *
     * @param writer Writer to render to
     * @param path Context path of the application to be undeployed
     */
    private void undeploy(PrintWriter writer, String path) {

        if (debug >= 1)
	    log("undeploy: Undeploying web application at '" + path + "'");

        try {
	    Context context = (Context) host.findChild(path);
	    if (context == null) {
	        writer.println(sm.getString("managerServlet.noContext", path));
		return;
	    }
	    host.removeChild(context);
	    writer.println(sm.getString("managerServlet.undeployed", path));
	} catch (Throwable t) {
	    getServletContext().log("ManagerServlet.undeploy[" + path + "]",
				    t);
	    writer.println(sm.getString("managerServlet.exception",
					t.toString()));
	}

    }


}
