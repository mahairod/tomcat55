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

        try {

            // Display virtual machine statistics
            writeVMState(writer);

            Enumeration enum = threadPools.elements();
            while (enum.hasMoreElements()) {
                ObjectName objectName = (ObjectName) enum.nextElement();
                String name = objectName.getKeyProperty("name");
                writeConnectorState(writer, objectName, name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Write virtual machine state.
     */
    protected void writeVMState(PrintWriter writer)
        throws Exception {

        writer.print("<h1>JVM</h1>");
        writer.print("<br/>");

        writer.print(" Free memory: ");
        writer.print(Runtime.getRuntime().freeMemory());
        writer.print(" Total memory: ");
        writer.print(Runtime.getRuntime().totalMemory());
        writer.print(" Max memory: ");
        writer.print(Runtime.getRuntime().maxMemory());

        writer.print("<br/>");

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

        writer.print("<br/>");

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
        writer.print(mBeanServer.getAttribute(grpName, "maxTime"));
        writer.print(" Processing time:");
        writer.print(mBeanServer.getAttribute(grpName, "processingTime"));
        writer.print(" Request count: ");
        writer.print(mBeanServer.getAttribute(grpName, "requestCount"));
        writer.print(" Error count: ");
        writer.print(mBeanServer.getAttribute(grpName, "errorCount"));
        writer.print(" Bytes received: ");
        writer.print(mBeanServer.getAttribute(grpName, "bytesReceived"));
        writer.print(" Bytes sent: ");
        writer.print(mBeanServer.getAttribute(grpName, "bytesSent"));

        writer.print("<br/>");

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

        writer.write("<td><b>");

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
            fullStatus = false;
            break;
        case (0/*org.apache.coyote.Constants.STAGE_NEW*/):
            writer.write("R");
            fullStatus = false;
            break;
        default:
            writer.write("?");
            fullStatus = false;

        }

        writer.write("</b></td>");

        if (fullStatus) {
            writer.write("<td>");
            writer.print(mBeanServer.getAttribute
                         (pName, "requestProcessingTime"));
            writer.write("</td>");
            writer.write("<td>");
            writer.print(mBeanServer.getAttribute
                         (pName, "requestBytesSent"));
            writer.write("</td>");
            writer.write("<td>");
            writer.print(mBeanServer.getAttribute
                         (pName, "requestBytesReceived"));
            writer.write("</td>");
            writer.write("<td>");
            writer.print("" + mBeanServer.getAttribute(pName, "remoteAddr"));
            writer.write("</td>");
            writer.write("<td nowrap>");
            writer.write("" + filter(mBeanServer.getAttribute
                                     (pName, "virtualHost").toString()));
            writer.write("</td>");
            writer.write("<td nowrap>");
            writer.write("" + filter(mBeanServer.getAttribute
                                     (pName, "method").toString()));
            writer.write("" + filter(mBeanServer.getAttribute
                                     (pName, "currentUri").toString()));
            String queryString = (String) mBeanServer.getAttribute
                (pName, "currentQueryString");
            if ((queryString != null) && (!queryString.equals(""))) {
                writer.write("?");
                writer.print(queryString);
            }
            writer.write("</td>");
        } else {
            writer.write("<td>?</td><td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>");
        }

    }


    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML.  This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     */
    public static String filter(String message) {

        if (message == null)
            return (null);

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
            }
        }

    }


}
