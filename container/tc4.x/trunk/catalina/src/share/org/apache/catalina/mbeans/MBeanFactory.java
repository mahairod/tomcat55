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


import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.core.StandardDefaultContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.logger.FileLogger;
import org.apache.catalina.logger.SystemErrLogger;
import org.apache.catalina.logger.SystemOutLogger;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.realm.JNDIRealm;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteHostValve;
import org.apache.catalina.valves.RequestDumperValve;
import org.apache.commons.modeler.BaseModelMBean;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardServer</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision$ $Date$
 */

public class MBeanFactory extends BaseModelMBean {

    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = MBeanUtils.createServer();

    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = MBeanUtils.createRegistry();


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
    public MBeanFactory()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes




    // ------------------------------------------------------------- Operations

    /**
     * Return the managed bean definition for the specified bean type
     *
     * @param type MBean type
     */
    public String findObjectName(String type) {

        if (type.equals("org.apache.catalina.core.StandardContext")) {
            return "StandardContext";
        } else if (type.equals("org.apache.catalina.core.StandardDefaultContext")) {
            return "DefaultContext";
        } else if (type.equals("org.apache.catalina.core.StandardEngine")) {
            return "StandardEngine";
        } else if (type.equals("org.apache.catalina.core.StandardHost")) {
            return "StandardHost";
        } else return null;

    }


    /**
     * Create a new AccessLoggerValve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createAccessLoggerValve(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        AccessLogValve accessLogger = new AccessLogValve();
        accessLogger.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, accessLogger);
        MBeanUtils.createMBean(accessLogger);
        accessLogger.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new DefaultContext.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createDefaultContext(String parent)
        throws Exception {

        String tname = "StandardHost";
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName pname = new ObjectName(parent);
        Object obj = mserver.getAttribute(pname, "managedResource");
        Host host = null;
        if (obj instanceof Host) {
            host = (Host) obj;
        }
        StandardDefaultContext context = new StandardDefaultContext();
        context.setParent(host);
        ObjectName oname = MBeanUtils.createObjectName(domain, context);
        MBeanUtils.createMBean(context);
        context.setParent(null);

        return (oname.toString());

    }


    /**
     * Create a new FileLogger.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createFileLogger(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        FileLogger fileLogger = new FileLogger();
        fileLogger.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, fileLogger);
        MBeanUtils.createMBean(fileLogger);
        fileLogger.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new HTTP/1.0 Connector.
     *
     * @param parent MBean Name of the associated parent component
     * @param address The IP address on which to bind
     * @param port TCP port number to listen on
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createHttp10Connector(String parent, String address, int port)
        throws Exception {

        String tname = "StandardService";
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName pname = new ObjectName(parent);
        Object obj = mserver.getAttribute(pname, "managedResource");
        Service service = null;
        if (obj instanceof Service) {
            service = (Service) obj;
        }
        org.apache.catalina.connector.http10.HttpConnector connector =
            new org.apache.catalina.connector.http10.HttpConnector();
        connector.setAddress(address);
        connector.setPort(port);
        connector.setService(service);
        ObjectName oname = MBeanUtils.createObjectName(domain, connector);
        MBeanUtils.createMBean(connector);
        connector.setService(null);

        return (oname.toString());

    }


    /**
     * Create a new HTTP/1.1 Connector.
     *
     * @param parent MBean Name of the associated parent component
     * @param address The IP address on which to bind
     * @param port TCP port number to listen on
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createHttp11Connector(String parent, String address, int port)
        throws Exception {

        String tname = "StandardService";
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName pname = new ObjectName(parent);
        Object obj = mserver.getAttribute(pname, "managedResource");
        Service service = null;
        if (obj instanceof Service) {
            service = (Service) obj;
        }
        org.apache.catalina.connector.http.HttpConnector connector =
            new org.apache.catalina.connector.http.HttpConnector();
        connector.setAddress(address);
        connector.setPort(port);
        connector.setService(service);
        ObjectName oname = MBeanUtils.createObjectName(domain, connector);
        MBeanUtils.createMBean(connector);
        connector.setService(null);

        return (oname.toString());

    }


    /**
     * Create a new JDBC Realm.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createJDBCRealm(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        JDBCRealm realm = new JDBCRealm();
        realm.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, realm);
        MBeanUtils.createMBean(realm);
        realm.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new JNDI Realm.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createJNDIRealm(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        JNDIRealm realm = new JNDIRealm();
        realm.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, realm);
        MBeanUtils.createMBean(realm);
        realm.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new Memory Realm.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createMemoryRealm(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        MemoryRealm realm = new MemoryRealm();
        realm.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, realm);
        MBeanUtils.createMBean(realm);
        realm.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new Remote Address Filter Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createRemoteAddrValve(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        RemoteAddrValve valve = new RemoteAddrValve();
        valve.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, valve);
        MBeanUtils.createMBean(valve);
        valve.setContainer(null);

        return (oname.toString());

    }


     /**
     * Create a new Remote Host Filter Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createRemoteHostValve(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        RemoteHostValve valve = new RemoteHostValve();
        valve.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, valve);
        MBeanUtils.createMBean(valve);
        valve.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new Request Dumper Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createRequestDumperValve(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        RequestDumperValve valve = new RequestDumperValve();
        valve.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, valve);
        MBeanUtils.createMBean(valve);
        valve.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new Single Sign On Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createSingleSignOn(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        SingleSignOn singleSignOn = new SingleSignOn();
        singleSignOn.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, singleSignOn);
        MBeanUtils.createMBean(singleSignOn);
        singleSignOn.setContainer(null);

        return (oname.toString());

    }


   /**
     * Create a new StandardContext.
     *
     * @param parent MBean Name of the associated parent component
     * @param path The context path for this Context
     * @param docBase Document base directory (or WAR) for this Context
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardContext(String parent, String path,
                                        String docBase)
        throws Exception {

        // Create a new StandardContext instance
        StandardContext context = new StandardContext();
        context.setPath(path);
        context.setDocBase(docBase);

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
        host.addChild(context);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardContext");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), context);
        return (oname.toString());

    }



   /**
     * Create a new StandardEngine.
     *
     * @param parent MBean Name of the associated parent component
     * @param name Unique name of this Engine
     * @param defaultHost Default hostname of this Engine
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardEngine(String parent, String name,
                                       String defaultHost)
        throws Exception {

        // Create a new StandardEngine instance
        StandardEngine engine = new StandardEngine();
        engine.setName(name);
        engine.setDefaultHost(defaultHost);

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("name"));
        service.setContainer(engine);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardEngine");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), engine);
        return (oname.toString());

    }


    /**
     * Create a new StandardHost.
     *
     * @param parent MBean Name of the associated parent component
     * @param name Unique name of this Host
     * @param appBase Application base directory name
     * @param unpackWARs Should we unpack WARs when auto deploying?
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardHost(String parent, String name,
                                     String appBase, boolean unpackWARs)
        throws Exception {

        // Create a new StandardHost instance
        StandardHost host = new StandardHost();
        host.setName(name);
        host.setAppBase(appBase);
        host.setUnpackWARs(unpackWARs);

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        engine.addChild(host);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardHost");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), host);
        return (oname.toString());

    }


    /**
     * Create a new StandardManager.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardManager(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        StandardManager manager = new StandardManager();
        manager.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, manager);
        MBeanUtils.createMBean(manager);
        manager.setContainer(null);

        return (oname.toString());

    }



    /**
     * Create a new StandardService.
     *
     * @param parent MBean Name of the associated parent component
     * @param name Unique name of this StandardService
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardService(String parent, String name)
        throws Exception {

        // Create a new StandardService instance
        StandardService service = new StandardService();
        service.setName(name);

        // Add the new instance to its parent component
        Server server = ServerFactory.getServer();
        server.addService(service);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardService");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), service);
        return (oname.toString());

    }



    /**
     * Create a new System Error Logger.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createSystemErrLogger(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        SystemErrLogger logger = new SystemErrLogger();
        logger.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, logger);
        MBeanUtils.createMBean(logger);
        logger.setContainer(null);

        return (oname.toString());

    }


    /**
     * Create a new System Output Logger.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createSystemOutLogger(String parent)
        throws Exception {

        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        String tname = findObjectName(type);
        ManagedBean managed = registry.findManagedBean(tname);
        String domain = null;
        if (managed != null)
            domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Object obj = mserver.getAttribute(pname, "managedResource");
        Container container = null;
        if (obj instanceof Container) {
            container = (Container) obj;
        }
        SystemOutLogger logger = new SystemOutLogger();
        logger.setContainer(container);
        ObjectName oname = MBeanUtils.createObjectName(domain, logger);
        MBeanUtils.createMBean(logger);
        logger.setContainer(null);

        return (oname.toString());

    }




}
