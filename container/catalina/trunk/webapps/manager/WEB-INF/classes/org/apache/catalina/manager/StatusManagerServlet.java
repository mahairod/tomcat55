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


package org.apache.catalina.manager;


import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.modeler.Registry;

import org.apache.tomcat.util.compat.JdkCompat;

import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;

/**
 * This servlet will display a complete status of the HTTP/1.1 connector.
 *
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class StatusManagerServlet
    extends HttpServlet implements NotificationListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The debugging detail level for this servlet.
     */
    private int debug = 0;


    /**
     * MBean server.
     */
    protected MBeanServer mBeanServer = null;


    /**
     * Vector of protocol handlers object names.
     */
    protected Vector protocolHandlers = new Vector();


    /**
     * Vector of thread pools object names.
     */
    protected Vector threadPools = new Vector();


    /**
     * Vector of request processors object names.
     */
    protected Vector requestProcessors = new Vector();


    /**
     * Vector of global request processors object names.
     */
    protected Vector globalRequestProcessors = new Vector();


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // --------------------------------------------------------- Public Methods


    /**
     * Initialize this servlet.
     */
    public void init() throws ServletException {

        // Retrieve the MBean server
        mBeanServer = Registry.getServer();

        // Set our properties from the initialization parameters
        String value = null;
        try {
            value = getServletConfig().getInitParameter("debug");
            debug = Integer.parseInt(value);
        } catch (Throwable t) {
            ;
        }

        try {

            // Query protocol handlers
            String onStr = "*:type=ProtocolHandler,*";
            ObjectName objectName = new ObjectName(onStr);
            Set set = mBeanServer.queryMBeans(objectName, null);
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = (ObjectInstance) iterator.next();
                protocolHandlers.addElement(oi.getObjectName());
            }

            // Query Thread Pools
            onStr = "*:type=ThreadPool,*";
            objectName = new ObjectName(onStr);
            set = mBeanServer.queryMBeans(objectName, null);
            iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = (ObjectInstance) iterator.next();
                threadPools.addElement(oi.getObjectName());
            }

            // Query Global Request Processors
            onStr = "*:type=GlobalRequestProcessor,*";
            objectName = new ObjectName(onStr);
            set = mBeanServer.queryMBeans(objectName, null);
            iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = (ObjectInstance) iterator.next();
                globalRequestProcessors.addElement(oi.getObjectName());
            }

            // Query Request Processors
            onStr = "*:type=RequestProcessor,*";
            objectName = new ObjectName(onStr);
            set = mBeanServer.queryMBeans(objectName, null);
            iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = (ObjectInstance) iterator.next();
                requestProcessors.addElement(oi.getObjectName());
            }

            // Register with MBean server
            onStr = "JMImplementation:type=MBeanServerDelegate";
            objectName = new ObjectName(onStr);
            mBeanServer.addNotificationListener(objectName, this, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Finalize this servlet.
     */
    public void destroy() {

        ;       // No actions necessary

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

        response.setContentType("text/html");

        PrintWriter writer = response.getWriter();

        boolean completeStatus = false;
        if ((request.getPathInfo() != null) 
            && (request.getPathInfo().equals("/all"))) {
            completeStatus = true;
        }

        // HTML Header Section
        writer.print(Constants.HTML_HEADER_SECTION);

        // Body Header Section
        Object[] args = new Object[2];
        args[0] = request.getContextPath();
        if (completeStatus) {
            args[1] = sm.getString("statusServlet.complete");
        } else {
            args[1] = sm.getString("statusServlet.title");
        }
        writer.print(MessageFormat.format
                     (Constants.BODY_HEADER_SECTION, args));

        // Manager Section
        args = new Object[9];
        args[0] = sm.getString("htmlManagerServlet.manager");
        args[1] = response.encodeURL(request.getContextPath() + "/html/list");
        args[2] = sm.getString("htmlManagerServlet.list");
        args[3] = response.encodeURL
            (request.getContextPath() + "/" +
             sm.getString("htmlManagerServlet.helpHtmlManagerFile"));
        args[4] = sm.getString("htmlManagerServlet.helpHtmlManager");
        args[5] = response.encodeURL
            (request.getContextPath() + "/" +
             sm.getString("htmlManagerServlet.helpManagerFile"));
        args[6] = sm.getString("htmlManagerServlet.helpManager");
        if (completeStatus) {
            args[7] = response.encodeURL
                (request.getContextPath() + "/status");
            args[8] = sm.getString("statusServlet.title");
        } else {
            args[7] = response.encodeURL
                (request.getContextPath() + "/status/all");
            args[8] = sm.getString("statusServlet.complete");
        }
        writer.print(MessageFormat.format(Constants.MANAGER_SECTION, args));

        // Server Header Section
        args = new Object[7];
        args[0] = sm.getString("htmlManagerServlet.serverTitle");
        args[1] = sm.getString("htmlManagerServlet.serverVersion");
        args[2] = sm.getString("htmlManagerServlet.serverJVMVersion");
        args[3] = sm.getString("htmlManagerServlet.serverJVMVendor");
        args[4] = sm.getString("htmlManagerServlet.serverOSName");
        args[5] = sm.getString("htmlManagerServlet.serverOSVersion");
        args[6] = sm.getString("htmlManagerServlet.serverOSArch");
        writer.print(MessageFormat.format
                     (Constants.SERVER_HEADER_SECTION, args));

        // Server Row Section
        args = new Object[6];
        args[0] = ServerInfo.getServerInfo();
        args[1] = System.getProperty("java.runtime.version");
        args[2] = System.getProperty("java.vm.vendor");
        args[3] = System.getProperty("os.name");
        args[4] = System.getProperty("os.version");
        args[5] = System.getProperty("os.arch");
        writer.print(MessageFormat.format(Constants.SERVER_ROW_SECTION, args));

        try {

            // Display virtual machine statistics
            writeVMState(writer);

            Enumeration enum = threadPools.elements();
            while (enum.hasMoreElements()) {
                ObjectName objectName = (ObjectName) enum.nextElement();
                String name = objectName.getKeyProperty("name");
                writeConnectorState(writer, objectName, name);
            }

            if ((request.getPathInfo() != null) 
                && (request.getPathInfo().equals("/all"))) {
                // Warning: slow
                writeDetailedState(writer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // HTML Tail Section
        writer.print(Constants.HTML_TAIL_SECTION);

    }


    /**
     * Write virtual machine state.
     */
    protected void writeVMState(PrintWriter writer)
        throws Exception {

        writer.print("<h1>JVM</h1>");

        writer.print("<p>");
        writer.print(" Free memory: ");
        writer.print(formatSize(new Long(Runtime.getRuntime().freeMemory()), 
                                true));
        writer.print(" Total memory: ");
        writer.print(formatSize(new Long(Runtime.getRuntime().totalMemory()),
                                true));
        writer.print(" Max memory: ");
        writer.print(formatSize
                     (new Long(JdkCompat.getJdkCompat().getMaxMemory()), 
                      true));
        writer.print("</p>");

    }


    /**
     * Write connector state.
     */
    protected void writeConnectorState(PrintWriter writer, 
                                       ObjectName tpName, String name)
        throws Exception {

        writer.print("<h1>");
        writer.print(name);
        writer.print("</h1>");

        writer.print("<p>");
        writer.print(" Max threads: ");
        writer.print(mBeanServer.getAttribute(tpName, "maxThreads"));
        writer.print(" Min spare threads: ");
        writer.print(mBeanServer.getAttribute(tpName, "minSpareThreads"));
        writer.print(" Max spare threads: ");
        writer.print(mBeanServer.getAttribute(tpName, "maxSpareThreads"));
        writer.print(" Current thread count: ");
        writer.print(mBeanServer.getAttribute(tpName, "currentThreadCount"));
        writer.print(" Current thread busy: ");
        writer.print(mBeanServer.getAttribute(tpName, "currentThreadsBusy"));

        writer.print("<br/>");

        ObjectName grpName = null;

        Enumeration enum = globalRequestProcessors.elements();
        while (enum.hasMoreElements()) {
            ObjectName objectName = (ObjectName) enum.nextElement();
            if (name.equals(objectName.getKeyProperty("name"))) {
                grpName = objectName;
            }
        }

        if (grpName == null) {
            return;
        }

        writer.print(" Max processing time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (grpName, "maxTime"), false));
        writer.print(" Processing time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (grpName, "processingTime"), true));
        writer.print(" Request count: ");
        writer.print(mBeanServer.getAttribute(grpName, "requestCount"));
        writer.print(" Error count: ");
        writer.print(mBeanServer.getAttribute(grpName, "errorCount"));
        writer.print(" Bytes received: ");
        writer.print(formatSize(mBeanServer.getAttribute
                                (grpName, "bytesReceived"), true));
        writer.print(" Bytes sent: ");
        writer.print(formatSize(mBeanServer.getAttribute
                                (grpName, "bytesSent"), true));
        writer.print("</p>");

        writer.print("<table border=\"0\"><tr><th>Stage</th><th>Time</th><th>B Sent</th><th>B Recv</th><th>Client</th><th>VHost</th><th>Request</th></tr>");

        enum = requestProcessors.elements();
        while (enum.hasMoreElements()) {
            ObjectName objectName = (ObjectName) enum.nextElement();
            if (name.equals(objectName.getKeyProperty("worker"))) {
                writer.print("<tr>");
                writeProcessorState(writer, objectName);
                writer.print("</tr>");
            }
        }

        writer.print("</table>");

        writer.print("<p>");
        writer.print("P: Parse and prepare request S: Service F: Finishing R: Ready K: Keepalive");
        writer.print("</p>");

    }


    /**
     * Write processor state.
     */
    protected void writeProcessorState(PrintWriter writer, ObjectName pName)
        throws Exception {

        Integer stageValue = 
            (Integer) mBeanServer.getAttribute(pName, "stage");
        int stage = stageValue.intValue();
        boolean fullStatus = true;
        boolean showRequest = true;

        writer.write("<td><strong>");

        switch (stage) {

        case (1/*org.apache.coyote.Constants.STAGE_PARSE*/):
            writer.write("P");
            fullStatus = false;
            break;
        case (2/*org.apache.coyote.Constants.STAGE_PREPARE*/):
            writer.write("P");
            fullStatus = false;
            break;
        case (3/*org.apache.coyote.Constants.STAGE_SERVICE*/):
            writer.write("S");
            break;
        case (4/*org.apache.coyote.Constants.STAGE_ENDINPUT*/):
            writer.write("F");
            break;
        case (5/*org.apache.coyote.Constants.STAGE_ENDOUTPUT*/):
            writer.write("F");
            break;
        case (7/*org.apache.coyote.Constants.STAGE_ENDED*/):
            writer.write("R");
            fullStatus = false;
            break;
        case (6/*org.apache.coyote.Constants.STAGE_KEEPALIVE*/):
            writer.write("K");
            fullStatus = true;
            showRequest = false;
            break;
        case (0/*org.apache.coyote.Constants.STAGE_NEW*/):
            writer.write("R");
            fullStatus = false;
            break;
        default:
            writer.write("?");
            fullStatus = false;

        }

        writer.write("</strong></td>");

        if (fullStatus) {
            writer.write("<td>");
            writer.print(formatTime(mBeanServer.getAttribute
                                    (pName, "requestProcessingTime"), false));
            writer.write("</td>");
            writer.write("<td>");
            if (showRequest) {
                writer.print(formatSize(mBeanServer.getAttribute
                                        (pName, "requestBytesSent"), false));
            } else {
                writer.write("?");
            }
            writer.write("</td>");
            writer.write("<td>");
            if (showRequest) {
                writer.print(formatSize(mBeanServer.getAttribute
                                        (pName, "requestBytesReceived"), 
                                        false));
            } else {
                writer.write("?");
            }
            writer.write("</td>");
            writer.write("<td>");
            writer.print(filter(mBeanServer.getAttribute
                                (pName, "remoteAddr")));
            writer.write("</td>");
            writer.write("<td nowrap>");
            writer.write(filter(mBeanServer.getAttribute
                                (pName, "virtualHost")));
            writer.write("</td>");
            writer.write("<td nowrap>");
            if (showRequest) {
                writer.write(filter(mBeanServer.getAttribute
                                    (pName, "method")));
                writer.write(" ");
                writer.write(filter(mBeanServer.getAttribute
                                    (pName, "currentUri")));
                String queryString = (String) mBeanServer.getAttribute
                    (pName, "currentQueryString");
                if ((queryString != null) && (!queryString.equals(""))) {
                    writer.write("?");
                    writer.print(queryString);
                }
                writer.write(" ");
                writer.write(filter(mBeanServer.getAttribute
                                    (pName, "protocol")));
            } else {
                writer.write("?");
            }
            writer.write("</td>");
        } else {
            writer.write("<td>?</td><td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>");
        }

    }


    /**
     * Write applications state.
     */
    protected void writeDetailedState(PrintWriter writer)
        throws Exception {

        ObjectName queryHosts = new ObjectName("*:j2eeType=WebModule,*");
        Set hostsON = mBeanServer.queryNames(queryHosts, null);
        Iterator iterator = hostsON.iterator();
        while (iterator.hasNext()) {
            ObjectName contextON = (ObjectName) iterator.next();
            writeContext(writer, contextON);
        }

    }


    /**
     * Write context state.
     */
    protected void writeContext(PrintWriter writer, ObjectName objectName)
        throws Exception {

        String webModuleName = objectName.getKeyProperty("name");
        String name = webModuleName;
        if (name == null) {
            return;
        }

        String hostName = null;
        String contextName = null;
        if (name.startsWith("//")) {
            name = name.substring(2);
        }
        int slash = name.indexOf("/");
        if (slash != -1) {
            hostName = name.substring(0, slash);
            contextName = name.substring(slash);
        } else {
            return;
        }
        // Special case for the root context
        if (contextName.equals("/")) {
            contextName = "";
        }

        writer.print("<h1>");
        writer.print(name);
        writer.print("</h1>");

        writer.print("<p>");
        writer.print(" Startup time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (objectName, "startupTime"), false));
        writer.print(" TLD scan time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (objectName, "tldScanTime"), false));
        writer.print("</p>");

        String onStr = "*:j2eeType=Servlet,WebModule=" + webModuleName + ",*";
        ObjectName servletObjectName = new ObjectName(onStr);
        Set set = mBeanServer.queryMBeans(servletObjectName, null);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            ObjectInstance oi = (ObjectInstance) iterator.next();
            writeWrapper(writer, oi.getObjectName());
        }

    }


    /**
     * Write detailed information about a wrapper.
     */
    public void writeWrapper(PrintWriter writer, ObjectName objectName)
        throws Exception {

        String servletName = objectName.getKeyProperty("name");

        String[] mappings = (String[]) 
            mBeanServer.invoke(objectName, "findMappings", null, null);

        writer.print("<h2>");
        writer.print(servletName);
        if ((mappings != null) && (mappings.length > 0)) {
            writer.print(" [ ");
            for (int i = 0; i < mappings.length; i++) {
                writer.print(mappings[i]);
                if (i < mappings.length - 1) {
                    writer.print(" , ");
                }
            }
            writer.print(" ] ");
        }
        writer.print("</h2>");

        writer.print("<p>");
        writer.print(" Processing time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (objectName, "processingTime"), true));
        writer.print(" Max time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (objectName, "maxTime"), false));
        writer.print(" Request count: ");
        writer.print(mBeanServer.getAttribute(objectName, "requestCount"));
        writer.print(" Error count: ");
        writer.print(mBeanServer.getAttribute(objectName, "errorCount"));
        writer.print(" Load time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (objectName, "loadTime"), false));
        writer.print(" Classloading time: ");
        writer.print(formatTime(mBeanServer.getAttribute
                                (objectName, "classLoadTime"), false));
        writer.print("</p>");

    }


    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML.  This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     */
    public static String filter(Object obj) {

        if (obj == null)
            return ("?");
        String message = obj.toString();

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());

    }


    /**
     * Display the given size in bytes, either as KB or MB.
     *
     * @param mb true to display megabytes, false for kilobytes
     */
    public static String formatSize(Object obj, boolean mb) {

        long bytes = -1L;

        if (obj instanceof Long) {
            bytes = ((Long) obj).longValue();
        } else if (obj instanceof Integer) {
            bytes = ((Integer) obj).intValue();
        }

        if (mb) {
            long mbytes = bytes / 1000000;
            long rest = (bytes / 10000) - mbytes * 100;
            return (mbytes + "." + rest + " MB");
        } else {
            return ((bytes / 1000) + " KB");
        }

    }


    /**
     * Display the given time in ms, either as ms or s.
     *
     * @param seconds true to display seconds, false for milliseconds
     */
    public static String formatTime(Object obj, boolean seconds) {

        long time = -1L;

        if (obj instanceof Long) {
            time = ((Long) obj).longValue();
        } else if (obj instanceof Integer) {
            time = ((Integer) obj).intValue();
        }

        if (seconds) {
            return ((time / 1000) + " s");
        } else {
            return (time + " ms");
        }

    }


    // ------------------------------------------- NotificationListener Methods


    public void handleNotification(Notification notification,
                                   java.lang.Object handback) {

        if (notification instanceof MBeanServerNotification) {
            ObjectName objectName = 
                ((MBeanServerNotification) notification).getMBeanName();
            if (notification.getType().equals
                (MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
                String type = objectName.getKeyProperty("type");
                if (type != null) {
                    if (type.equals("ProtocolHandler")) {
                        protocolHandlers.addElement(objectName);
                    } else if (type.equals("ThreadPool")) {
                        threadPools.addElement(objectName);
                    } else if (type.equals("GlobalRequestProcessor")) {
                        globalRequestProcessors.addElement(objectName);
                    } else if (type.equals("RequestProcessor")) {
                        requestProcessors.addElement(objectName);
                    }
                }
            } else if (notification.getType().equals
                       (MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                String type = objectName.getKeyProperty("type");
                if (type != null) {
                    if (type.equals("ProtocolHandler")) {
                        protocolHandlers.removeElement(objectName);
                    } else if (type.equals("ThreadPool")) {
                        threadPools.removeElement(objectName);
                    } else if (type.equals("GlobalRequestProcessor")) {
                        globalRequestProcessors.removeElement(objectName);
                    } else if (type.equals("RequestProcessor")) {
                        requestProcessors.removeElement(objectName);
                    }
                }
                String j2eeType = objectName.getKeyProperty("j2eeType");
                if (j2eeType != null) {
                    
                }
            }
        }

    }


}
