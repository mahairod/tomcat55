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
import java.util.Collection;
import java.util.Iterator;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;


/**
 * Implementation of <code>LifecycleListener</code> that
 * instantiates the set of MBeans associated with the components of a
 * running instance of Catalina.
 *
 * @author Craig R. McClanahan
 * @author Amy Roh
 * @version $Revision$ $Date$
 */

public class ServerLifecycleListener
    implements ContainerListener, LifecycleListener {


    // ------------------------------------------------------------- Properties


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }


    // ---------------------------------------------- ContainerListener Methods


    /**
     * Handle a <code>ContainerEvent</code> from one of the Containers we are
     * interested in.
     *
     * @param event The event that has occurred
     */
    public void containerEvent(ContainerEvent event) {

        String type = event.getType();
        if (Container.ADD_CHILD_EVENT.equals(type)) {
            processContainerAddChild(event.getContainer(),
                                     (Container) event.getData());
        } else if (Container.ADD_VALVE_EVENT.equals(type)) {
            processContainerAddValve(event.getContainer(),
                                     (Valve) event.getData());
        } else if (Container.REMOVE_CHILD_EVENT.equals(type)) {
            processContainerRemoveChild(event.getContainer(),
                                        (Container) event.getData());
        } else if (Container.REMOVE_VALVE_EVENT.equals(type)) {
            processContainerRemoveValve(event.getContainer(),
                                        (Valve) event.getData());
        }

    }


    // ---------------------------------------------- LifecycleListener Methods


    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        if (Lifecycle.START_EVENT.equals(event.getType())) {
            createMBeans();
        } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Create the MBeans that correspond to every node of our tree.
     */
    protected void createMBeans() {

        try {

            if (debug >= 1)
                log("Creating MBeans for existing components");

            // Create the MBean for the top-level Server object
            Server top = ServerFactory.getServer();
            if (debug >= 2)
                log("Creating MBean for Server " + top);
            MBeanUtils.createMBean(top);

            // Create the MBean for each associated Service and friends
            Service services[] = top.findServices();
            for (int i = 0; i < services.length; i++) {

                // FIXME - Warp object hierarchy not currently supported
                if (services[i].getContainer().getClass().getName().equals
                    ("org.apache.catalina.connector.warp.WarpEngine")) {
                    if (debug >= 2) {
                        log("Skipping MBean for Service " +
                            services[i]);
                    }
                    continue;
                }

                // The MBean for the Service itself
                if (debug >= 2)
                    log("Creating MBean for Service " + services[i]);
                MBeanUtils.createMBean(services[i]);

                // The MBean for the corresponding Engine
                Engine engine = (Engine) services[i].getContainer();
                if (debug >= 2)
                    log("Creating MBean for Engine " + engine);
                MBeanUtils.createMBean(engine);
                engine.addContainerListener(this);

                // The MBeans for the corresponding Connectors
                Connector connectors[] = services[i].findConnectors();
                for (int j = 0; j < connectors.length; j++) {
                    if (debug >= 3)
                        log("Creating MBean for Connector " + connectors[j]);
                    MBeanUtils.createMBean(connectors[j]);
                }

                // The MBeans for the corresponding Hosts and friends
                Container hosts[] = engine.findChildren();
                for (int j = 0; j < hosts.length; j++) {

                    // The MBean for the Host itself
                    Host host = (Host) hosts[j];
                    if (debug >= 3)
                        log("Creating MBean for Host " + host);
                    MBeanUtils.createMBean(host);
                    host.addContainerListener(this);

                    // The MBeans for the corresponding Contexts
                    Container contexts[] = host.findChildren();
                    for (int k = 0; k < contexts.length; k++) {
                        Context context = (Context) contexts[k];
                        if (debug >= 4)
                            log("Creating MBean for Context " + context);
                        MBeanUtils.createMBean(context);
                        // context.addContainerListener(this);
                        // If the context is privileged, give a reference to it
                        // in a servlet context attribute
                        if (context.getPrivileged()) {
                            context.getServletContext().setAttribute
                                (Globals.MBEAN_REGISTRY_ATTR,
                                 MBeanUtils.createRegistry());
                            context.getServletContext().setAttribute
                                (Globals.MBEAN_SERVER_ATTR, 
                                 MBeanUtils.createServer());
                        }
                        Loader loader = context.getLoader();
                        if (loader != null) {
                            if (debug >= 5)
                                log("Creating MBean for Loader " + loader);
                            MBeanUtils.createMBean(loader);
                            // FIX ME
                            //loader.addLifecycleListener(this);
                        }
                        Logger cLogger = context.getLogger();
                        if (cLogger != null) {
                            if (debug >= 3)
                                log("Creating MBean for Logger " + cLogger);
                            MBeanUtils.createMBean(cLogger);
                        }
                        Manager manager = context.getManager();
                        if (manager != null) {
                            if (debug >= 5)
                                log("Creating MBean for Manager" + manager);
                            MBeanUtils.createMBean(manager);
                            // FIX ME
                            //manager.addLifecycleListener(this);
                        }
                        Realm cRealm = context.getRealm();
                        if (cRealm != null) {
                            if (debug >= 3)
                                log("Creating MBean for Realm " + cRealm);
                            MBeanUtils.createMBean(cRealm);
                        }
                        if (context instanceof StandardContext) {
                            Valve cValves[] = ((StandardContext)context).getValves();
                            for (int l=0; l<=cValves.length; l++) {
                                if (debug >= 3)
                                    log("Creating MBean for Valve " + cValves[l]);
                                MBeanUtils.createMBean(cValves[l]);
                            }
                        }

                    }
                    Logger hLogger = host.getLogger();
                    if (hLogger != null) {
                        if (debug >= 3)
                            log("Creating MBean for Logger " + hLogger);
                        MBeanUtils.createMBean(hLogger);
                    }
                    Realm hRealm = host.getRealm();
                    if (hRealm != null) {
                        if (debug >= 3)
                            log("Creating MBean for Realm " + hRealm);
                        MBeanUtils.createMBean(hRealm);
                    }
                    if (host instanceof StandardHost) {
                        Valve hValves[] = ((StandardHost)host).getValves();
                        for (int k=0; k<=hValves.length; k++) {
                            if (debug >= 3)
                                log("Creating MBean for Valve " + hValves[k]);
                            MBeanUtils.createMBean(hValves[k]);
                        }
                    }

                }
                Logger eLogger = engine.getLogger();
                if (eLogger != null) {
                    if (debug >= 3)
                        log("Creating MBean for Logger " + eLogger);
                    MBeanUtils.createMBean(eLogger);
                }
                Realm eRealm = engine.getRealm();
                if (eRealm != null) {
                    if (debug >= 3)
                        log("Creating MBean for Realm " + eRealm);
                    MBeanUtils.createMBean(eRealm);
                }
                if (engine instanceof StandardEngine) {
                    Valve eValves[] = ((StandardEngine)engine).getValves();
                    for (int j=0; j<=eValves.length; j++) {
                        if (debug >= 3)
                            log("Creating MBean for Valve " + eValves[j]);
                        MBeanUtils.createMBean(eValves[j]);
                    }
                }


            }

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("createMBeans: MBeanException", e);

        } catch (Throwable t) {

            log("createMBeans: Throwable", t);

        }

    }


    /**
     * Log a message.
     *
     * @param message The message to be logged
     */
    protected void log(String message) {

        System.out.print("ServerLifecycleListener: ");
        System.out.println(message);

    }


    /**
     * Log a message and associated exception.
     *
     * @param message The message to be logged
     * @param throwable The exception to be logged
     */
    protected void log(String message, Throwable throwable) {

        log(message);
        throwable.printStackTrace(System.out);

    }


    /**
     * Process the addition of a new child Container to a parent Container.
     *
     * @param parent Parent container
     * @param child Child container
     */
    protected void processContainerAddChild(Container parent,
                                            Container child) {

        if (debug >= 1)
            log("Process addChild[parent=" + parent + ",child=" + child + "]");

        try {
            if (child instanceof Context) {
                Context context = (Context) child;
                if (context.getPrivileged()) {
                    context.getServletContext().setAttribute
                        (Globals.MBEAN_REGISTRY_ATTR, 
                         MBeanUtils.createRegistry());
                    context.getServletContext().setAttribute
                        (Globals.MBEAN_SERVER_ATTR, 
                         MBeanUtils.createServer());
                }
                if (debug >= 4)
                    log("  Creating MBean for Context " + context);
                MBeanUtils.createMBean(context);
                // context.addContainerListener(this);
            } else if (child instanceof Host) {
                Host host = (Host) child;
                if (debug >= 3)
                    log("  Creating MBean for Host " + host);
                MBeanUtils.createMBean(host);
                host.addContainerListener(this);
            }
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("processContainerAddChild: MBeanException", e);
        } catch (Throwable t) {
            log("processContainerAddChild: Throwable", t);
        }

    }


    /**
     * Process the addition of a new Valve to a Container.
     *
     * @param container The affected Container
     * @param valve The new Valve
     */
    protected void processContainerAddValve(Container container,
                                            Valve valve) {

        if (debug >= 1)
            log("Process addValve[container=" + container + ",valve=" +
                valve + "]");

        ; // FIXME - processContainerAddValve()

    }


    /**
     * Process the removal of a child Container from a parent Container.
     *
     * @param parent Parent container
     * @param child Child container
     */
    protected void processContainerRemoveChild(Container parent,
                                               Container child) {

        if (debug >= 1)
            log("Process removeChild[parent=" + parent + ",child=" +
                child + "]");

        try {
            if (child instanceof Context) {
                Context context = (Context) child;
                if (context.getPrivileged()) {
                    context.getServletContext().removeAttribute
                        (Globals.MBEAN_REGISTRY_ATTR);
                    context.getServletContext().removeAttribute
                        (Globals.MBEAN_SERVER_ATTR);
                }
                if (debug >= 4)
                    log("  Removing MBean for Context " + context);
                MBeanUtils.destroyMBean(context);
                ; // FIXME - child component MBeans?
            } else if (child instanceof Host) {
                Host host = (Host) child;
                MBeanUtils.destroyMBean(host);
                ; // FIXME - child component MBeans?
            }
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("processContainerRemoveChild: MBeanException", e);
        } catch (Throwable t) {
            log("processContainerRemoveChild: Throwable", t);
        }

    }


    /**
     * Process the removal of a Valve from a Container.
     *
     * @param container The affected Container
     * @param valve The old Valve
     */
    protected void processContainerRemoveValve(Container container,
                                               Valve valve) {

        if (debug >= 1)
            log("Process removeValve[container=" + container + ",valve=" +
                valve + "]");

        ; // FIXME - processContainerRemoveValve()

    }


}
