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

package org.apache.catalina.mbeans;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Server;
import org.apache.catalina.core.StandardServer;
import org.apache.commons.modeler.BaseModelMBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardServer</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision$ $Date$
 */

public class StandardServerMBean extends BaseModelMBean {

    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = MBeanUtils.createServer();


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public StandardServerMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes


    /**
     * Set the value of a specific attribute of this MBean.
     *
     * @param attribute The identification of the attribute to be set
     *  and the new value
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public void setAttribute(javax.management.Attribute attribute)
        throws javax.management.AttributeNotFoundException,
               MBeanException,
               javax.management.ReflectionException {

        // KLUDGE - This is only here to force calling store()
        // until the admin webapp calls it directly
        super.setAttribute(attribute);
        try {
            store();
        } catch (InstanceNotFoundException e) {
            throw new MBeanException(e);
        }

    }


    // ------------------------------------------------------------- Operations


    /**
     * Write the configuration information for this entire <code>Server</code>
     * out to the server.xml configuration file.
     *
     * @exception InstanceNotFoundException if the managed resource object
     *  cannot be found
     * @exception MBeanException if the initializer of the object throws
     *  an exception, or persistence is not supported
     * @exception RuntimeOperationsException if an exception is reported
     *  by the persistence mechanism
     */
    public synchronized void store() throws InstanceNotFoundException,
        MBeanException, RuntimeOperationsException {

        // Calculate file objects for the old and new configuration files.
        String configFile = "conf/server.xml"; // FIXME - configurable?
        File configOld = new File(configFile);
        if (!configOld.isAbsolute()) {
            configOld = new File(System.getProperty("catalina.base"),
                                 configFile);
        }
        File configNew = new File(configFile + ".new");
        if (!configNew.isAbsolute()) {
            configNew = new File(System.getProperty("catalina.base"),
                                 configFile + ".new");
        }

        // Open an output writer for the new configuration file
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(configNew));
        } catch (IOException e) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    ;
                }
            }
            throw new MBeanException(e, "Creating conf/server.xml.new");
        }

        // Store the state of this Server MBean
        // (which will recursively store everything
        ObjectName oname = null;
        try {
            oname =
                MBeanUtils.createObjectName("Catalina",
                                            (Server) getManagedResource());
            storeServer(writer, 0, oname);
        } catch (Exception e) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    ;
                }
            }
            throw new MBeanException(e, "Writing conf/server.xml.new");
        }

        // Close the output file and rename to the original
        try {
            writer.flush();
        } catch (Exception e) {
            throw new MBeanException(e, "Flushing conf/server.xml.new");
        }
        try {
            writer.close();
        } catch (Exception e) {
            throw new MBeanException(e, "Closing conf/server.xml.new");
        }
        ; // FIXME - do not rename until 100% of server.xml is being written!

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Store the relevant attributes of the specified MBean.
     *
     * @param writer PrintWriter to which we are storing
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeAttributes(PrintWriter writer,
                                 ObjectName oname) throws Exception {

        // Acquire the set of attributes we should be saving
        MBeanInfo minfo = mserver.getMBeanInfo(oname);
        MBeanAttributeInfo ainfo[] = minfo.getAttributes();
        if (ainfo == null) {
            ainfo = new MBeanAttributeInfo[0];
        }

        // Save the value of each relevant attribute
        for (int i = 0; i < ainfo.length; i++) {

            // Make sure this is an attribute we want to save
            String aname = ainfo[i].getName();
            if ("managedResource".equals(aname)) {
                continue; // KLUDGE - these should be removed
            }
            if (!ainfo[i].isReadable()) {
                continue; // We cannot read the current value
            }
            if (!"className".equals(aname) && !ainfo[i].isWritable()) {
                continue; // We will not be able to configure this attribute
            }

            // Acquire the value of this attribute
            Object value = mserver.getAttribute(oname, aname);
            if (value == null) {
                continue; // No need to explicitly record this
            }
            if (!(value instanceof String)) {
                value = value.toString();
            }

            // Add this attribute value to our output
            writer.print(" ");
            writer.print(aname);
            writer.print("=\"");
            writer.print((String) value);
            writer.print("\"");

        }


    }


    /**
     * Store the specified Connector properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeConnector(PrintWriter writer, int indent,
                                ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Connector");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store nested <Listener> elements
        ; // FIXME

        // Store nested <Factory> element
        ; // FIXME

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Connector>");

    }


    /**
     * Store the specified Context properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeContext(PrintWriter writer, int indent,
                              ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Context");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store nested <InstanceListener> elements
        ; // FIXME

        // Store nested <Listener> elements
        ; // FIXME

        // Store nested <Loader> element
        ; // FIXME

        // Store nested <Logger> element
        StringBuffer loggerSearch =
            new StringBuffer("Catalina:type=Logger,path=");
        loggerSearch.append(oname.getKeyProperty("path"));
        loggerSearch.append(",host=");
        loggerSearch.append(oname.getKeyProperty("host"));
        loggerSearch.append(",service=");
        loggerSearch.append(oname.getKeyProperty("service"));
        ObjectName loggerQuery = new ObjectName(loggerSearch.toString());
        Iterator loggerNames =
            mserver.queryNames(loggerQuery, null).iterator();
        while (loggerNames.hasNext()) {
            storeLogger(writer, indent + 2,
                        (ObjectName) loggerNames.next());
        }

        // Store nested <Manager> element
        ; // FIXME

        // Store nested <Parameter> elements
        ; // FIXME

        // Store nested <Realm> element
        StringBuffer realmSearch =
            new StringBuffer("Catalina:type=Realm,path=");
        realmSearch.append(oname.getKeyProperty("path"));
        realmSearch.append(",host=");
        realmSearch.append(oname.getKeyProperty("host"));
        realmSearch.append(",service=");
        realmSearch.append(oname.getKeyProperty("service"));
        ObjectName realmQuery = new ObjectName(realmSearch.toString());
        Iterator realmNames =
            mserver.queryNames(realmQuery, null).iterator();
        while (realmNames.hasNext()) {
            storeRealm(writer, indent + 2,
                        (ObjectName) realmNames.next());
        }

        // Store nested <ResourceLink> elements
        ; // FIXME

        // Store nested <Resources> element
        ; // FIXME

        // Store nested <Valve> elements
        ; // FIXME

        // Store nested <WrapperLifecycle> elements
        ; // FIXME

        // Store nested <WrapperListener> elements
        ; // FIXME

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Context>");

    }


    /**
     * Store the specified Engine properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeEngine(PrintWriter writer, int indent,
                             ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Engine");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store nested <Default> element
        ; // FIXME

        // Store nested <DefaultContext> element
        ; // FIXME

        // Store nested <Host> elements
        StringBuffer hostSearch =
            new StringBuffer("Catalina:type=Host,service=");
        hostSearch.append(oname.getKeyProperty("service"));
        hostSearch.append(",*");
        ObjectName hostQuery = new ObjectName(hostSearch.toString());
        Iterator hostNames =
            mserver.queryNames(hostQuery, null).iterator();
        while (hostNames.hasNext()) {
            storeHost(writer, indent + 2,
                      (ObjectName) hostNames.next());
        }

        // Store nested <Listener> elements
        ; // FIXME

        // Store nested <Logger> element
        StringBuffer loggerSearch =
            new StringBuffer("Catalina:type=Logger,service=");
        loggerSearch.append(oname.getKeyProperty("service"));
        ObjectName loggerQuery = new ObjectName(loggerSearch.toString());
        Iterator loggerNames =
            mserver.queryNames(loggerQuery, null).iterator();
        while (loggerNames.hasNext()) {
            storeLogger(writer, indent + 2,
                        (ObjectName) loggerNames.next());
        }

        // Store nested <Realm> element
        StringBuffer realmSearch =
            new StringBuffer("Catalina:type=Realm,service=");
        realmSearch.append(oname.getKeyProperty("service"));
        ObjectName realmQuery = new ObjectName(realmSearch.toString());
        Iterator realmNames =
            mserver.queryNames(realmQuery, null).iterator();
        while (realmNames.hasNext()) {
            storeRealm(writer, indent + 2,
                        (ObjectName) realmNames.next());
        }

        // Store nested <Valve> elements
        ; // FIXME

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Engine>");

    }


    /**
     * Store the specified Host properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeHost(PrintWriter writer, int indent,
                           ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Host");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store nested <Alias> elements
        ; // FIXME

        // Store nested <Context> elements
        StringBuffer contextSearch =
            new StringBuffer("Catalina:type=Context,host=");
        contextSearch.append(oname.getKeyProperty("host"));
        contextSearch.append(",service=");
        contextSearch.append(oname.getKeyProperty("service"));
        contextSearch.append(",*");
        ObjectName contextQuery = new ObjectName(contextSearch.toString());
        Iterator contextNames =
            mserver.queryNames(contextQuery, null).iterator();
        while (contextNames.hasNext()) {
            storeContext(writer, indent + 2,
                         (ObjectName) contextNames.next());
        }

        // Store nested <Cluster> elements
        ; // FIXME

        // Store nested <Default> element
        ; // FIXME

        // Store nested <DefaultContext> element
        ; // FIXME

        // Store nested <Listener> elements
        ; // FIXME

        // Store nested <Logger> element
        StringBuffer loggerSearch =
            new StringBuffer("Catalina:type=Logger,host=");
        loggerSearch.append(oname.getKeyProperty("host"));
        loggerSearch.append(",service=");
        loggerSearch.append(oname.getKeyProperty("service"));
        ObjectName loggerQuery = new ObjectName(loggerSearch.toString());
        Iterator loggerNames =
            mserver.queryNames(loggerQuery, null).iterator();
        while (loggerNames.hasNext()) {
            storeLogger(writer, indent + 2,
                        (ObjectName) loggerNames.next());
        }

        // Store nested <Realm> element
        StringBuffer realmSearch =
            new StringBuffer("Catalina:type=Realm,host=");
        realmSearch.append(oname.getKeyProperty("host"));
        realmSearch.append(",service=");
        realmSearch.append(oname.getKeyProperty("service"));
        ObjectName realmQuery = new ObjectName(realmSearch.toString());
        Iterator realmNames =
            mserver.queryNames(realmQuery, null).iterator();
        while (realmNames.hasNext()) {
            storeRealm(writer, indent + 2,
                        (ObjectName) realmNames.next());
        }

        // Store nested <Valve> elements
        ; // FIXME

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Host>");

    }


    /**
     * Store the specified Logger properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeLogger(PrintWriter writer, int indent,
                             ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Logger");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Logger>");

    }


    /**
     * Store the specified Realm properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeRealm(PrintWriter writer, int indent,
                            ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Realm");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Realm>");

    }


    /**
     * Store the specified Server properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeServer(PrintWriter writer, int indent,
                             ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Server");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store nested <GlobalNamingResources> element
        ; // FIXME

        // Store nested <Listener> elements
        ; // FIXME

        // Store nested <Service> elements
        StringBuffer serviceSearch =
            new StringBuffer("Catalina:type=Service,*");
        ObjectName serviceQuery = new ObjectName(serviceSearch.toString());
        Iterator serviceNames =
            mserver.queryNames(serviceQuery, null).iterator();
        while (serviceNames.hasNext()) {
            storeService(writer, indent + 2,
                         (ObjectName) serviceNames.next());
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Server>");

    }


    /**
     * Store the specified Service properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param oname ObjectName of the MBean for the object we are storing
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeService(PrintWriter writer, int indent,
                              ObjectName oname) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Service");
        storeAttributes(writer, oname);
        writer.println(">");

        // Store nested <Connector> elements
        StringBuffer connectorSearch =
            new StringBuffer("Catalina:type=Connector,service=");
        connectorSearch.append(oname.getKeyProperty("name"));
        connectorSearch.append(",*");
        ObjectName connectorQuery = new ObjectName(connectorSearch.toString());
        Iterator connectorNames =
            mserver.queryNames(connectorQuery, null).iterator();
        while (connectorNames.hasNext()) {
            storeConnector(writer, indent + 2,
                           (ObjectName) connectorNames.next());
        }

        // Store nested <Engine> element
        StringBuffer engineSearch =
            new StringBuffer("Catalina:type=Engine,service=");
        engineSearch.append(oname.getKeyProperty("name"));
        engineSearch.append(",*");
        ObjectName engineQuery = new ObjectName(engineSearch.toString());
        Iterator engineNames =
            mserver.queryNames(engineQuery, null).iterator();
        while (engineNames.hasNext()) {
            storeEngine(writer, indent + 2,
                        (ObjectName) engineNames.next());
        }

        // Store nested <Listener> elements
        ; // FIXME

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Service>");

    }


}
