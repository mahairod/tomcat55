/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.core;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleMBeanRegistration;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.util.LifecycleBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import java.util.ArrayList;
import org.apache.catalina.Executor;


/**
 * Standard implementation of the <code>Service</code> interface.  The
 * associated Container is generally an instance of Engine, but this is
 * not required.
 *
 * @author Craig R. McClanahan
 */

public class StandardService extends LifecycleBase
        implements Service, LifecycleMBeanRegistration {

    private static final Log log = LogFactory.getLog(StandardService.class);
   

    // ----------------------------------------------------- Instance Variables


    /**
     * Descriptive information about this component implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardService/1.0";


    /**
     * The name of this service.
     */
    private String name = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The <code>Server</code> that owns this Service, if any.
     */
    private Server server = null;

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The set of Connectors associated with this Service.
     */
    protected Connector connectors[] = new Connector[0];
    
    /**
     * 
     */
    protected ArrayList<Executor> executors = new ArrayList<Executor>();

    /**
     * The Container associated with this Service. (In the case of the
     * org.apache.catalina.startup.Embedded subclass, this holds the most
     * recently added Engine.)
     */
    protected Container container = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     * @param container The new Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        if ((oldContainer != null) && (oldContainer instanceof Engine))
            ((Engine) oldContainer).setService(null);
        this.container = container;
        if ((this.container != null) && (this.container instanceof Engine))
            ((Engine) this.container).setService(this);
        if (getState().isAvailable() && (this.container != null)) {
            try {
                this.container.start();
            } catch (LifecycleException e) {
                // Ignore
            }
        }
        if (getState().isAvailable() && (oldContainer != null)) {
            try {
                oldContainer.stop();
            } catch (LifecycleException e) {
                // Ignore
            }
        }

        // Report this property change to interested listeners
        support.firePropertyChange("container", oldContainer, this.container);

    }

    public ObjectName getContainerName() {
        if( container instanceof ContainerBase ) {
            return ((ContainerBase)container).getJmxName();
        }
        return null;
    }


    /**
     * Return descriptive information about this Service implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the name of this Service.
     */
    public String getName() {

        return (this.name);

    }


    /**
     * Set the name of this Service.
     *
     * @param name The new service name
     */
    public void setName(String name) {

        this.name = name;

    }


    /**
     * Return the <code>Server</code> with which we are associated (if any).
     */
    public Server getServer() {

        return (this.server);

    }


    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    public void setServer(Server server) {

        this.server = server;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     *
     * @param connector The Connector to be added
     */
    public void addConnector(Connector connector) {

        synchronized (connectors) {
            connector.setService(this);
            Connector results[] = new Connector[connectors.length + 1];
            System.arraycopy(connectors, 0, results, 0, connectors.length);
            results[connectors.length] = connector;
            connectors = results;

            if (getState().isAvailable()) {
                try {
                    ((Lifecycle) connector).start();
                } catch (LifecycleException e) {
                    log.error("Connector.start", e);
                }
            }

            // Report this property change to interested listeners
            support.firePropertyChange("connector", null, connector);
        }

    }

    public ObjectName[] getConnectorNames() {
        ObjectName results[] = new ObjectName[connectors.length];
        for (int i=0; i<results.length; i++) {
            results[i] = connectors[i].getObjectName();
        }
        return results;
    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

    }


    /**
     * Find and return the set of Connectors associated with this Service.
     */
    public Connector[] findConnectors() {

        return (connectors);

    }


    /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     *
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector) {

        synchronized (connectors) {
            int j = -1;
            for (int i = 0; i < connectors.length; i++) {
                if (connector == connectors[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;
            if (getState().isAvailable()) {
                try {
                    ((Lifecycle) connectors[j]).stop();
                } catch (LifecycleException e) {
                    log.error("Connector.stop", e);
                }
            }
            connector.setService(null);
            int k = 0;
            Connector results[] = new Connector[connectors.length - 1];
            for (int i = 0; i < connectors.length; i++) {
                if (i != j)
                    results[k++] = connectors[i];
            }
            connectors = results;

            // Report this property change to interested listeners
            support.firePropertyChange("connector", connector, null);
        }

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


    /**
     * Return a String representation of this component.
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("StandardService[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    /**
     * Adds a named executor to the service
     * @param ex Executor
     */
    public void addExecutor(Executor ex) {
        synchronized (executors) {
            if (!executors.contains(ex)) {
                executors.add(ex);
                if (getState().isAvailable())
                    try {
                        ex.start();
                    } catch (LifecycleException x) {
                        log.error("Executor.start", x);
                    }
            }
        }
    }

    /**
     * Retrieves all executors
     * @return Executor[]
     */
    public Executor[] findExecutors() {
        synchronized (executors) {
            Executor[] arr = new Executor[executors.size()];
            executors.toArray(arr);
            return arr;
        }
    }

    /**
     * Retrieves executor by name, null if not found
     * @param name String
     * @return Executor
     */
    public Executor getExecutor(String name) {
        synchronized (executors) {
            for (int i = 0; i < executors.size(); i++) {
                if (name.equals(executors.get(i).getName()))
                    return executors.get(i);
            }
        }
        return null;
    }

    /**
     * Removes an executor from the service
     * @param ex Executor
     */
    public void removeExecutor(Executor ex) {
        synchronized (executors) {
            if ( executors.remove(ex) && getState().isAvailable() ) {
                try {
                    ex.stop();
                } catch (LifecycleException e) {
                    log.error("Executor.stop", e);
                }
            }
        }
    }



    /**
     * Start nested components ({@link Executor}s, {@link Connector}s and
     * {@link Container}s) and implement the requirements of
     * {@link LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected void startInternal() throws LifecycleException {

        if(log.isInfoEnabled())
            log.info(sm.getString("standardService.start.name", this.name));
        setState(LifecycleState.STARTING);

        // Start our defined Container first
        if (container != null) {
            synchronized (container) {
                container.start();
            }
        }

        synchronized (executors) {
            for ( int i=0; i<executors.size(); i++ ) {
                executors.get(i).start();
            }
        }

        // Start our defined Connectors second
        synchronized (connectors) {
            for (int i = 0; i < connectors.length; i++) {
                ((Lifecycle) connectors[i]).start();
            }
        }
    }


    /**
     * Stop nested components ({@link Executor}s, {@link Connector}s and
     * {@link Container}s) and implement the requirements of
     * {@link LifecycleBase#stopInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    @Override
    protected void stopInternal() throws LifecycleException {

        // Stop our defined Connectors first
        synchronized (connectors) {
            for (int i = 0; i < connectors.length; i++) {
                connectors[i].pause();
            }
        }

        // Heuristic: Sleep for a while to ensure pause of the connector
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        if(log.isInfoEnabled())
            log.info(sm.getString("standardService.stop.name", this.name));
        setState(LifecycleState.STOPPING);

        // Stop our defined Container second
        if (container != null) {
            synchronized (container) {
                container.stop();
            }
        }
        // FIXME pero -- Why container stop first? KeepAlive connections can send request! 
        // Stop our defined Connectors first
        synchronized (connectors) {
            for (int i = 0; i < connectors.length; i++) {
                ((Lifecycle) connectors[i]).stop();
            }
        }

        synchronized (executors) {
            for ( int i=0; i<executors.size(); i++ ) {
                executors.get(i).stop();
            }
        }
    }


    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     */
    @Override
    protected void initInternal() throws LifecycleException {

        if (container != null) {
            container.init();
        }

        // Initialize any Executors
        for (Executor executor : findExecutors()) {
            if (executor instanceof LifecycleMBeanRegistration) {
                ((LifecycleMBeanRegistration) executor).setDomain(getDomain());
            }
            executor.init();
        }

        // Initialize our defined Connectors
        synchronized (connectors) {
            for (Connector connector : connectors) {
                try {
                    connector.init();
                } catch (Exception e) {
                    log.error(sm.getString(
                            "standardService.connector.initFailed", connector),
                            e);
                }
            }
        }
    }
    
    @Override
    protected void destroyInternal() throws LifecycleException {
        Registry.getRegistry(null, null).unregisterComponent(oname);
        
        // Destroy our defined Connectors
        synchronized (connectors) {
            for (Connector connector : connectors) {
                try {
                    connector.destroy();
                } catch (Exception e) {
                    log.error(sm.getString(
                            "standardService.connector.destroyfailed",
                            connector), e);
                }
            }
        }

        // Destroy any Executors
        for (Executor executor : findExecutors()) {
            executor.destroy();
        }

        if (container != null) {
            container.destroy();
        }

    }

    protected volatile String domain;
    protected volatile ObjectName oname;

    /**
     * Obtain the MBean domain for this server. The domain is obtained using
     * the following search order:
     * <ol>
     * <li>Name of the {@link Engine}.</li>
     * <li>Name of the {@link Service}.</li>
     * <li>Global default defined by {@link Globals#DEFAULT_MBEAN_DOMAIN}</li>
     * </ol>
     */
    public String getDomain() {
        if (domain == null) {
            Container container = getContainer();
            if (container != null) {
                domain = container.getName();
            } else {
                domain = getName();
            }
            if (domain == null) {
                domain = Globals.DEFAULT_MBEAN_DOMAIN;
            }
        }
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public ObjectName getObjectName() {
        if (oname == null) {
            StringBuilder name = new StringBuilder(getDomain());
            name.append(":type=Service");
            
            try {
                oname = new ObjectName(name.toString());
            } catch (MalformedObjectNameException e) {
                log.warn(sm.getString("standardService.onameFail", name), e);
            } catch (NullPointerException e) {
                // Never going to happen
            }
        }
        
        return oname;
    }
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
        // NOOP
    }

    public void preDeregister() throws Exception {
        // NOOP
    }

    public void postDeregister() {
        // NOOP
    }

}
