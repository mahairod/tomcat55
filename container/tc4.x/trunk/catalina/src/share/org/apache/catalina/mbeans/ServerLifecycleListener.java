/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    implements ContainerListener, LifecycleListener, PropertyChangeListener {


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

        try {
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
        } catch (Exception e) {
            log("Exception processing event " + event, e);
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


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Handle a <code>PropertyChangeEvent</code> from one of the Containers
     * we are interested in.
     *
     * @param event The event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() instanceof Container) {
            try {
                processContainerPropertyChange((Container) event.getSource(),
                                               event.getPropertyName(),
                                               event.getOldValue(),
                                               event.getNewValue());
            } catch (Exception e) {
                log("Exception handling property change", e);
            }
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Create the MBeans that correspond to every existing node of our tree.
     */
    protected void createMBeans() {

        try {

            createMBeans(ServerFactory.getServer());

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
     * Create the MBeans for the specified Context and its nested components.
     *
     * @param context Context for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Context context) throws Exception {

        // Create the MBean for the Context itself
        if (debug >= 4)
            log("Creating MBean for Context " + context);
        MBeanUtils.createMBean(context);
        if (context instanceof StandardContext) {
            ((StandardContext) context).
                addPropertyChangeListener(this);
        }

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

        // Create the MBeans for the associated nested components
        Loader cLoader = context.getLoader();
        if (cLoader != null) {
            if (debug >= 4)
                log("Creating MBean for Loader " + cLoader);
            MBeanUtils.createMBean(cLoader);
        }
        Logger hLogger = context.getParent().getLogger();
        Logger cLogger = context.getLogger();
        if ((cLogger != null) && (cLogger != hLogger)) {
            if (debug >= 4)
                log("Creating MBean for Logger " + cLogger);
            MBeanUtils.createMBean(cLogger);
        }
        Manager cManager = context.getManager();
        if (cManager != null) {
            if (debug >= 4)
                log("Creating MBean for Manager " + cManager);
            MBeanUtils.createMBean(cManager);
        }
        Realm hRealm = context.getParent().getRealm();
        Realm cRealm = context.getRealm();
        if ((cRealm != null) && (cRealm != hRealm)) {
            if (debug >= 4)
                log("Creating MBean for Realm " + cRealm);
            MBeanUtils.createMBean(cRealm);
        }

        // Create the MBeans for the associated Valves
        if (context instanceof StandardContext) {
            Valve cValves[] = ((StandardContext)context).getValves();
            for (int l = 0; l < cValves.length; l++) {
                if (debug >= 4)
                    log("Creating MBean for Valve " + cValves[l]);
                MBeanUtils.createMBean(cValves[l]);
            }
            
        }

    }


    /**
     * Create the MBeans for the specified Engine and its nested components.
     *
     * @param engine Engine for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Engine engine) throws Exception {

        // Create the MBean for the Engine itself
        if (debug >= 2) {
            log("Creating MBean for Engine " + engine);
        }
        MBeanUtils.createMBean(engine);
        engine.addContainerListener(this);
        if (engine instanceof StandardEngine) {
            ((StandardEngine) engine).addPropertyChangeListener(this);
        }

        // Create the MBeans for the associated nested components
        Logger eLogger = engine.getLogger();
        if (eLogger != null) {
            if (debug >= 2)
                log("Creating MBean for Logger " + eLogger);
            MBeanUtils.createMBean(eLogger);
        }
        Realm eRealm = engine.getRealm();
        if (eRealm != null) {
            if (debug >= 2)
                log("Creating MBean for Realm " + eRealm);
            MBeanUtils.createMBean(eRealm);
        }

        // Create the MBeans for the associated Valves
        if (engine instanceof StandardEngine) {
            Valve eValves[] = ((StandardEngine)engine).getValves();
            for (int j = 0; j < eValves.length; j++) {
                if (debug >= 2)
                    log("Creating MBean for Valve " + eValves[j]);
                MBeanUtils.createMBean(eValves[j]);
            }
        }

        // Create the MBeans for each child Host
        Container hosts[] = engine.findChildren();
        for (int j = 0; j < hosts.length; j++) {
            createMBeans((Host) hosts[j]);
        }

    }


    /**
     * Create the MBeans for the specified Host and its nested components.
     *
     * @param host Host for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Host host) throws Exception {

        // Create the MBean for the Host itself
        if (debug >= 3) {
            log("Creating MBean for Host " + host);
        }
        MBeanUtils.createMBean(host);
        host.addContainerListener(this);
        if (host instanceof StandardHost) {
            ((StandardHost) host).addPropertyChangeListener(this);
        }

        // Create the MBeans for the associated nested components
        Logger eLogger = host.getParent().getLogger();
        Logger hLogger = host.getLogger();
        if ((hLogger != null) && (hLogger != eLogger)) {
            if (debug >= 3)
                log("Creating MBean for Logger " + hLogger);
            MBeanUtils.createMBean(hLogger);
        }
        Realm eRealm = host.getParent().getRealm();
        Realm hRealm = host.getRealm();
        if ((hRealm != null) && (hRealm != eRealm)) {
            if (debug >= 3)
                log("Creating MBean for Realm " + hRealm);
            MBeanUtils.createMBean(hRealm);
        }

        // Create the MBeans for the associated Valves
        if (host instanceof StandardHost) {
            Valve hValves[] = ((StandardHost)host).getValves();
            for (int k = 0; k < hValves.length; k++) {
                if (debug >= 3)
                    log("Creating MBean for Valve " + hValves[k]);
                MBeanUtils.createMBean(hValves[k]);
            }
        }

        // Create the MBeans for each child Context
        Container contexts[] = host.findChildren();
        for (int k = 0; k < contexts.length; k++) {
            createMBeans((Context) contexts[k]);
        }

    }


    /**
     * Create the MBeans for the specified Server and its nested components.
     *
     * @param server Server for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Server server) throws Exception {

        // Create the MBean for the Server itself
        if (debug >= 2)
            log("Creating MBean for Server " + server);
        MBeanUtils.createMBean(server);

        // Create the MBeans for each child Service
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            // FIXME - Warp object hierarchy not currently supported
            if (services[i].getContainer().getClass().getName().equals
                ("org.apache.catalina.connector.warp.WarpEngine")) {
                if (debug >= 1) {
                    log("Skipping MBean for Service " + services[i]);
                }
                continue;
            }
            createMBeans(services[i]);
        }

    }


    /**
     * Create the MBeans for the specified Service and its nested components.
     *
     * @param service Service for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Service service) throws Exception {

        // Create the MBean for the Service itself
        if (debug >= 2)
            log("Creating MBean for Service " + service);
        MBeanUtils.createMBean(service);

        // Create the MBeans for the corresponding Connectors
        Connector connectors[] = service.findConnectors();
        for (int j = 0; j < connectors.length; j++) {
            if (debug >= 2)
                log("Creating MBean for Connector " + connectors[j]);
            MBeanUtils.createMBean(connectors[j]);
        }

        // Create the MBean for the associated Engine and friends
        Engine engine = (Engine) service.getContainer();
        createMBeans(engine);

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
                createMBeans((Context) child);
            } else if (child instanceof Engine) {
                createMBeans((Engine) child);
            } else if (child instanceof Host) {
                createMBeans((Host) child);
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
                                            Valve valve)
        throws Exception {

        if (debug >= 1) {
            log("Process addValve[container=" + container + ",valve=" +
                valve + "]");
        }

        if (debug >= 4) {
            log("Creating MBean for Valve " + valve);
        }
        MBeanUtils.createMBean(valve);

    }


    /**
     * Process a property change event on a Container.
     *
     * @param container The container on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processContainerPropertyChange(Container container,
                                                  String propertyName,
                                                  Object oldValue,
                                                  Object newValue)
        throws Exception {

        if (debug >= 6) {
            log("propertyChange[container=" + container +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("loader".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Loader " + oldValue);
                }
                MBeanUtils.destroyMBean((Loader) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Loader " + newValue);
                }
                MBeanUtils.createMBean((Loader) newValue);
            }
        } else if ("logger".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Logger " + oldValue);
                }
                MBeanUtils.destroyMBean((Logger) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Logger " + newValue);
                }
                MBeanUtils.createMBean((Logger) newValue);
            }
        } else if ("manager".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Manager " + oldValue);
                }
                MBeanUtils.destroyMBean((Manager) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Manager " + newValue);
                }
                MBeanUtils.createMBean((Manager) newValue);
            }
        } else if ("realm".equals(propertyName)) {
            // removeService() has non-null oldValue
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Realm " + oldValue);
                }
                MBeanUtils.destroyMBean((Realm) oldValue);
            }
            // addService() has non-null newValue
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Realm " + newValue);
                }
                MBeanUtils.createMBean((Realm) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Service " + oldValue);
                }
                MBeanUtils.destroyMBean((Service) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Service " + newValue);
                }
                MBeanUtils.createMBean((Service) newValue);
            }
        }

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
                if (context instanceof StandardContext) {
                    ((StandardContext) context).
                        removePropertyChangeListener(this);
                }
            } else if (child instanceof Host) {
                Host host = (Host) child;
                MBeanUtils.destroyMBean(host);
                ; // FIXME - child component MBeans?
                if (host instanceof StandardHost) {
                    ((StandardHost) host).
                        removePropertyChangeListener(this);
                }
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

        try {
            MBeanUtils.destroyMBean(valve);
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("processContainerRemoveValve: MBeanException", e);
        } catch (Throwable t) {
            log("processContainerRemoveValve: Throwable", t);
        }

    }


}
