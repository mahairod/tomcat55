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


import java.io.InputStream;
import java.net.URL;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import com.sun.management.jmx.Trace;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.connector.http.HttpConnector;
import org.apache.catalina.connector.warp.WarpConnector;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;


/**
 * Public utility methods in support of the server side MBeans implementation.
 *
 * @author Craig R. McClanahan
 * @author Amy Roh
 * @version $Revision$ $Date$
 */

public class MBeanUtils {


    // ------------------------------------------------------- Static Variables


    /**
     * The set of exceptions to the normal rules used by
     * <code>createManagedBean()</code>.  The first element of each pair
     * is a class name, and the second element is the managed bean name.
     */
    private static String exceptions[][] = {
        { "org.apache.catalina.connector.http10.HttpConnector",
          "Http10Connector" },
        { "org.apache.catalina.connector.http.HttpConnector",
          "Http11Connector" },
    };


    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = createServer();


    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = createRegistry();


    // --------------------------------------------------------- Static Methods


    /**
     * Create and return the name of the <code>ManagedBean</code> that
     * corresponds to this Catalina component.
     *
     * @param component The component for which to create a name
     */
    public static String createManagedName(Object component) {

        // Deal with exceptions to the standard rule
        String className = component.getClass().getName();
        for (int i = 0; i < exceptions.length; i++) {
            if (className.equals(exceptions[i][0]))
                return (exceptions[i][1]);
        }

        // Perform the standard transformation
        int period = className.lastIndexOf('.');
        if (period >= 0)
            className = className.substring(period + 1);
        return (className);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Connector</code> object.
     *
     * @param connector The Connector to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public static ModelMBean createMBean(Connector connector)
        throws Exception {

        String mname = createManagedName(connector);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(connector);
        ObjectName oname = createObjectName(domain, connector);
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Context</code> object.
     *
     * @param context The Context to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public static ModelMBean createMBean(Context context)
        throws Exception {

        String mname = createManagedName(context);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(context);
        ObjectName oname = createObjectName(domain, context);
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Engine</code> object.
     *
     * @param engine The Engine to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public static ModelMBean createMBean(Engine engine)
        throws Exception {

        String mname = createManagedName(engine);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(engine);
        ObjectName oname = createObjectName(domain, engine);
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Host</code> object.
     *
     * @param host The Host to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public static ModelMBean createMBean(Host host)
        throws Exception {

        String mname = createManagedName(host);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(host);
        ObjectName oname = createObjectName(domain, host);
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Server</code> object.
     *
     * @param server The Server to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public static ModelMBean createMBean(Server server)
        throws Exception {

        String mname = createManagedName(server);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(server);
        ObjectName oname = createObjectName(domain, server);
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Service</code> object.
     *
     * @param service The Service to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public static ModelMBean createMBean(Service service)
        throws Exception {

        String mname = createManagedName(service);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(service);
        ObjectName oname = createObjectName(domain, service);
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Connector</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param connector The Connector to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                        Connector connector)
        throws MalformedObjectNameException {

        ObjectName name = null;
        if (connector instanceof HttpConnector) {
            HttpConnector httpConnector = (HttpConnector) connector;
            name = new ObjectName(domain + ":type=Connector,port=" +
                                  httpConnector.getPort() + ",address=" +
                                  httpConnector.getAddress());
            return (name);
        } else if (connector instanceof org.apache.catalina.connector.http10.HttpConnector) {
            org.apache.catalina.connector.http10.HttpConnector httpConnector =
                (org.apache.catalina.connector.http10.HttpConnector) connector;
            name = new ObjectName(domain + ":type=Connector,port=" +
                                  httpConnector.getPort() + ",address=" +
                                  httpConnector.getAddress());
            return (name);
        } else if (connector instanceof WarpConnector) {
            WarpConnector warpConnector = (WarpConnector) connector;
            name = new ObjectName(domain + ":type=Connector,port=" +
                                  warpConnector.getPort() + ",address=" +
                                  warpConnector.getAddress());
            return (name);
        } else {
            throw new MalformedObjectNameException
                ("Cannot create object name for " + connector);
        }

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Context</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param context The Context to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              Context context)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Host host = (Host)context.getParent();
        Service service = ((Engine)host.getParent()).getService();
        String path = context.getPath();
        if (path.length() < 1)
            path = "/";
        name = new ObjectName(domain + ":type=Context,path=" +
                              path + ",host=" +
                              host.getName() + ",service=" +
                              service.getName());
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Engine</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param engine The Engine to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              Engine engine)
        throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Engine,service=" +
                              engine.getService().getName());
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Host</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param host The Host to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              Host host)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Engine engine = (Engine)host.getParent();
        Service service = engine.getService();
        name = new ObjectName(domain + ":type=Host,name=" +
                              host.getName() + ",service=" +
                              service.getName());
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Server</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param server The Server to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              Server server)
        throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Server");
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Service</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param service The Service to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              Service service)
        throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Service,name=" +
                              service.getName());
        return (name);

    }


    /**
     * Create and configure (if necessary) and return the registry of
     * managed object descriptions.
     */
    public synchronized static Registry createRegistry() {

        if (registry == null) {
            try {
                URL url = ServerLifecycleListener.class.getResource
                    ("/org/apache/catalina/mbeans/mbeans-descriptors.xml");
                InputStream stream = url.openStream();
                //                Registry.setDebug(1);
                Registry.loadRegistry(stream);
                stream.close();
                registry = Registry.getRegistry();
            } catch (Throwable t) {
                t.printStackTrace(System.out);
                System.exit(1);
            }
        }
        return (registry);

    }


    /**
     * Create and configure (if necessary) and return the
     * <code>MBeanServer</code> with which we will be
     * registering our <code>ModelMBean</code> implementations.
     */
    public synchronized static MBeanServer createServer() {

        if (mserver == null) {
            try {
                Trace.parseTraceProperties();
                mserver = MBeanServerFactory.createMBeanServer();
            } catch (Throwable t) {
                t.printStackTrace(System.out);
                System.exit(1);
            }
        }
        return (mserver);

    }


}
