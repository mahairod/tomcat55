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


import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
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
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.modeler.BaseModelMBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardServer</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision$ $Date$
 */

public class StandardServerMBean extends BaseModelMBean {


    // ------------------------------------------------------- Static Variables


    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = MBeanUtils.createServer();


    /**
     * The set of class/property combinations that should <strong>NOT</strong>
     * be persisted because they are automatically calculated.
     */
    private static String exceptions[][] = {
        { "org.apache.catalina.core.StandardContext", "configured" },
        { "org.apache.catalina.core.StandardContext", "publicId" },
        { "org.apache.catalina.core.StandardContext", "workDir" },
        { "org.apache.catalina.session.StandardManager", "distributable" },
        { "org.apache.catalina.session.StandardManager", "entropy" },
    };


    /**
     * The set of classes that represent persistable properties.
     */
    private static Class persistables[] = {
        String.class,
        Integer.class, Integer.TYPE,
        Boolean.class, Boolean.TYPE,
        Byte.class, Byte.TYPE,
        Character.class, Character.TYPE,
        Double.class, Double.TYPE,
        Float.class, Float.TYPE,
        Long.class, Long.TYPE,
        Short.class, Short.TYPE,
    };


    /**
     * The set of class names that should be skipped when persisting state,
     * because the corresponding listeners, valves, etc. are configured
     * automatically at startup time.
     */
    private static String skippables[] = {
        "org.apache.catalina.authenticator.BasicAuthenticator",
        "org.apache.catalina.authenticator.DigestAuthenticator",
        "org.apache.catalina.authenticator.FormAuthenticator",
        "org.apache.catalina.authenticator.NonLoginAuthenticator",
        "org.apache.catalina.authenticator.SSLAuthenticator",
        "org.apache.catalina.core.NamingContextListener",
        "org.apache.catalina.core.StandardContextValve",
        "org.apache.catalina.core.StandardEngineValve",
        "org.apache.catalina.core.StandardHostValve",
        "org.apache.catalina.startup.ContextConfig",
        "org.apache.catalina.startup.EngineConfig",
        "org.apache.catalina.startup.HostConfig",
        "org.apache.catalina.valves.CertificatesValve",
        "org.apache.catalina.valves.ErrorDispatcherValve",
        "org.apache.catalina.valves.ErrorReportValve",
    };


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
            storeServer(writer, 0, ServerFactory.getServer());
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
     * Is the specified class name + property name combination an
     * exception that should not be persisted?
     *
     * @param className The class name to check
     * @param property The property name to check
     */
    private boolean isException(String className, String property) {

        for (int i = 0; i < exceptions.length; i++) {
            if (className.equals(exceptions[i][0]) &&
                property.equals(exceptions[i][1])) {
                return (true);
            }
        }
        return (false);

    }


    /**
     * Is the specified property type one for which we should generate
     * a persistence attribute?
     *
     * @param clazz Java class to be tested
     */
    private boolean isPersistable(Class clazz) {

        for (int i = 0; i < persistables.length; i++) {
            if (persistables[i] == clazz) {
                return (true);
            }
        }
        return (false);

    }


    /**
     * Is the specified class name one that should be skipped because
     * the corresponding component is configured automatically at
     * startup time?
     *
     * @param className Class name to be tested
     */
    private boolean isSkippable(String className) {

        for (int i = 0; i < skippables.length; i++) {
            if (skippables[i] == className) {
                return (true);
            }
        }
        return (false);

    }


    /**
     * Store the relevant attributes of the specified JavaBean, plus a
     * <code>className</code> attribute defining the fully qualified
     * Java class name of the bean.
     *
     * @param writer PrintWriter to which we are storing
     * @param bean Bean whose properties are to be rendered as attributes,
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeAttributes(PrintWriter writer,
                                 Object bean) throws Exception {

        storeAttributes(writer, true, bean);

    }


    /**
     * Store the relevant attributes of the specified JavaBean.
     *
     * @param writer PrintWriter to which we are storing
     * @param include Should we include a <code>className</code> attribute?
     * @param bean Bean whose properties are to be rendered as attributes,
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeAttributes(PrintWriter writer, boolean include,
                                 Object bean) throws Exception {

        // Render a className attribute if requested
        if (include) {
            writer.print(" className=\"");
            writer.print(bean.getClass().getName());
            writer.print("\"");
        }

        // Acquire the list of properties for this bean
        PropertyDescriptor descriptors[] =
            PropertyUtils.getPropertyDescriptors(bean);
        if (descriptors == null) {
            descriptors = new PropertyDescriptor[0];
        }

        // Render the relevant properties of this bean
        String className = bean.getClass().getName();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i] instanceof IndexedPropertyDescriptor) {
                continue; // Indexed properties are not persisted
            }
            if (!isPersistable(descriptors[i].getPropertyType()) ||
                (descriptors[i].getReadMethod() == null) ||
                (descriptors[i].getWriteMethod() == null)) {
                continue; // Must be a read-write primitive or String
            }
            Object value =
                PropertyUtils.getSimpleProperty(bean,
                                                descriptors[i].getName());
            if (value == null) {
                continue; // Null values are not persisted
            }
            if (isException(className, descriptors[i].getName())) {
                continue; // Skip the specified exceptions
            }
            if (!(value instanceof String)) {
                value = value.toString();
            }
            writer.print(' ');
            writer.print(descriptors[i].getName());
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
     * @param connector Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeConnector(PrintWriter writer, int indent,
                                Connector connector) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Connector");
        storeAttributes(writer, connector);
        writer.println(">");

        // Store nested <Factory> element
        ServerSocketFactory factory = connector.getFactory();
        if (factory != null) {
            storeFactory(writer, indent + 2, factory);
        }

        // Store nested <Listener> elements
        if (connector instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) connector).findLifecycleListeners();
            if (listeners == null) {
                listeners = new LifecycleListener[0];
            }
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

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
     * @param context  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeContext(PrintWriter writer, int indent,
                              Context context) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Context");
        storeAttributes(writer, context);
        writer.println(">");

        // Store nested <InstanceListener> elements
        ; // FIXME

        // Store nested <Listener> elements
        if (context instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) context).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Loader> element
        Loader loader = context.getLoader();
        if (loader != null) {
            storeLoader(writer, indent + 2, loader);
        }

        // Store nested <Logger> element
        Logger logger = context.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (context.getParent() != null) {
                parentLogger = context.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }

        // Store nested <Manager> element
        Manager manager = context.getManager();
        if (manager != null) {
            storeManager(writer, indent + 2, manager);
        }

        // Store nested <Parameter> elements
        ; // FIXME

        // Store nested <Realm> element
        Realm realm = context.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (context.getParent() != null) {
                parentRealm = context.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }

        // Store nested <ResourceLink> elements (and resource params?)
        ; // FIXME

        // Store nested <Resources> element
        ; // FIXME

        // Store nested <Valve> elements
        if (context instanceof Pipeline) {
            Valve valves[] = ((Pipeline) context).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

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
     * @param engine  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeEngine(PrintWriter writer, int indent,
                             Engine engine) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Engine");
        storeAttributes(writer, engine);
        writer.println(">");

        // Store nested <Default> element
        ; // FIXME

        // Store nested <DefaultContext> element
        ; // FIXME

        // Store nested <Host> elements (or other relevant containers)
        Container children[] = engine.findChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Context) {
                storeContext(writer, indent + 2, (Context) children[i]);
            } else if (children[i] instanceof Engine) {
                storeEngine(writer, indent + 2, (Engine) children[i]);
            } else if (children[i] instanceof Host) {
                storeHost(writer, indent + 2, (Host) children[i]);
            }
        }

        // Store nested <Listener> elements
        if (engine instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) engine).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Logger> element
        Logger logger = engine.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (engine.getParent() != null) {
                parentLogger = engine.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }

        // Store nested <Realm> element
        Realm realm = engine.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (engine.getParent() != null) {
                parentRealm = engine.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }

        // Store nested <Valve> elements
        if (engine instanceof Pipeline) {
            Valve valves[] = ((Pipeline) engine).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Engine>");

    }


    /**
     * Store the specified ServerSocketFactory properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param factory Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeFactory(PrintWriter writer, int indent,
                              ServerSocketFactory factory) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Factory");
        storeAttributes(writer, factory);
        writer.println("/>");

    }


    /**
     * Store the specified Host properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param host  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeHost(PrintWriter writer, int indent,
                           Host host) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Host");
        storeAttributes(writer, host);
        writer.println(">");

        // Store nested <Alias> elements
        ; // FIXME

        // Store nested <Cluster> elements
        ; // FIXME

        // Store nested <Context> elements (or other relevant containers)
        Container children[] = host.findChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Context) {
                storeContext(writer, indent + 2, (Context) children[i]);
            } else if (children[i] instanceof Engine) {
                storeEngine(writer, indent + 2, (Engine) children[i]);
            } else if (children[i] instanceof Host) {
                storeHost(writer, indent + 2, (Host) children[i]);
            }
        }

        // Store nested <Default> element
        ; // FIXME

        // Store nested <DefaultContext> element
        ; // FIXME

        // Store nested <Listener> elements
        if (host instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) host).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Logger> element
        Logger logger = host.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (host.getParent() != null) {
                parentLogger = host.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }

        // Store nested <Realm> element
        Realm realm = host.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (host.getParent() != null) {
                parentRealm = host.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }

        // Store nested <Valve> elements
        if (host instanceof Pipeline) {
            Valve valves[] = ((Pipeline) host).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Host>");

    }


    /**
     * Store the specified Listener properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param listener Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeListener(PrintWriter writer, int indent,
                               LifecycleListener listener) throws Exception {

        if (isSkippable(listener.getClass().getName())) {
            return;
        }

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Listener");
        storeAttributes(writer, listener);
        writer.println("/>");

    }


    /**
     * Store the specified Loader properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param loader Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeLoader(PrintWriter writer, int indent,
                             Loader loader) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Loader");
        storeAttributes(writer, loader);
        writer.println("/>");

    }


    /**
     * Store the specified Logger properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param logger Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeLogger(PrintWriter writer, int indent,
                             Logger logger) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Logger");
        storeAttributes(writer, logger);
        writer.println("/>");

    }


    /**
     * Store the specified Manager properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param manager Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeManager(PrintWriter writer, int indent,
                              Manager manager) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Manager");
        storeAttributes(writer, manager);
        writer.println(">");

        // Store nested <Store> element
        ; // FIXME

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Manager>");

    }


    /**
     * Store the specified Realm properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param realm Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeRealm(PrintWriter writer, int indent,
                            Realm realm) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Realm");
        storeAttributes(writer, realm);
        writer.println("/>");

    }


    /**
     * Store the specified Server properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param server Object to be stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeServer(PrintWriter writer, int indent,
                             Server server) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Server");
        storeAttributes(writer, server);
        writer.println(">");

        // Store nested <GlobalNamingResources> element
        ; // FIXME

        // Store nested <Listener> elements
        if (server instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) server).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Service> elements
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            storeService(writer, indent + 2, services[i]);
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
     * @param server Object to be stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeService(PrintWriter writer, int indent,
                              Service service) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Service");
        storeAttributes(writer, service);
        writer.println(">");

        // Store nested <Connector> elements
        Connector connectors[] = service.findConnectors();
        for (int i = 0; i < connectors.length; i++) {
            storeConnector(writer, indent + 2, connectors[i]);
        }

        // Store nested <Engine> element (or other appropriate container)
        Container container = service.getContainer();
        if (container != null) {
            if (container instanceof Context) {
                storeContext(writer, indent + 2, (Context) container);
            } else if (container instanceof Engine) {
                storeEngine(writer, indent + 2, (Engine) container);
            } else if (container instanceof Host) {
                storeHost(writer, indent + 2, (Host) container);
            }
        }

        // Store nested <Listener> elements
        if (service instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) service).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Service>");

    }


    /**
     * Store the specified Store properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param store Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeStore(PrintWriter writer, int indent,
                             Store store) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Store");
        storeAttributes(writer, store);
        writer.println("/>");

    }


    /**
     * Store the specified Valve properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param valve Object whose properties are being valved
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeValve(PrintWriter writer, int indent,
                             Valve valve) throws Exception {

        if (isSkippable(valve.getClass().getName())) {
            return;
        }

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Valve");
        storeAttributes(writer, valve);
        writer.println("/>");

    }


}
