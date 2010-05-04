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


package org.apache.catalina.valves;


import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.catalina.util.LifecycleBase;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;


/**
 * Convenience base class for implementations of the <b>Valve</b> interface.
 * A subclass <strong>MUST</strong> implement an <code>invoke()</code>
 * method to provide the required functionality, and <strong>MAY</strong>
 * implement the <code>Lifecycle</code> interface to provide configuration
 * management and lifecycle support.
 *
 * @author Craig R. McClanahan
 * @version $Id$
 */

public abstract class ValveBase extends LifecycleMBeanBase
    implements Contained, Valve {

    //------------------------------------------------------ Constructor
    
    public ValveBase() {
        this(false);
    }
    
    public ValveBase(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    //------------------------------------------------------ Instance Variables
    /**
     * Does this valve support async reporting
     */
    protected boolean asyncSupported;
    
    /**
     * The Container whose pipeline this Valve is a component of.
     */
    protected Container container = null;


    /**
     * Container log
     */
    protected Log containerLog = null;


    /**
     * Descriptive information about this Valve implementation.  This value
     * should be overridden by subclasses.
     */
    protected static String info =
        "org.apache.catalina.core.ValveBase/1.0";


    /**
     * The next Valve in the pipeline this Valve is a component of.
     */
    protected Valve next = null;


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    //-------------------------------------------------------------- Properties

    
    /**
     * Return the Container with which this Valve is associated, if any.
     */
    public Container getContainer() {

        return (container);

    }


    public boolean isAsyncSupported() {
        return asyncSupported;
    }


    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }


    /**
     * Set the Container with which this Valve is associated, if any.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the next Valve in this pipeline, or <code>null</code> if this
     * is the last Valve in the pipeline.
     */
    public Valve getNext() {

        return (next);

    }


    /**
     * Set the Valve that follows this one in the pipeline it is part of.
     *
     * @param valve The new next valve
     */
    public void setNext(Valve valve) {

        this.next = valve;

    }


    //---------------------------------------------------------- Public Methods


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess() {
        // NOOP by default
    }


    /**
     * The implementation-specific logic represented by this Valve.  See the
     * Valve description for the normal design patterns for this method.
     * <p>
     * This method <strong>MUST</strong> be provided by a subclass.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public abstract void invoke(Request request, Response response)
        throws IOException, ServletException;


    /**
     * Process a Comet event. This method will rarely need to be provided by
     * a subclass, unless it needs to reassociate a particular object with 
     * the thread that is processing the request.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs, or is thrown
     *  by a subsequently invoked Valve, Filter, or Servlet
     * @exception ServletException if a servlet error occurs, or is thrown
     *  by a subsequently invoked Valve, Filter, or Servlet
     */
    public void event(Request request, Response response, CometEvent event)
        throws IOException, ServletException {
        // Perform the request
        getNext().event(request, response, event);
    }


    /**
     * Start this component and implement the requirements
     * of {@link LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        
        setState(LifecycleState.STARTING);
    }


    /**
     * Stop this component and implement the requirements
     * of {@link LifecycleBase#stopInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void stopInternal() throws LifecycleException {

        setState(LifecycleState.STOPPING);
    }
    
    
    /**
     * Return a String rendering of this object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append('[');
        if (container == null) {
            sb.append("Container is null");
        } else {
            sb.append(container.getName());
        }
        sb.append(']');
        return sb.toString();
    }


    // -------------------- JMX and Registration  --------------------
    @Override
    public String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder("type=Valve");
        
        Container container = getContainer();
        int unknown = 0;

        // Work up container hierarchy, add a component to the name for
        // each container
        while (!(container instanceof Engine)) {
            if (container instanceof Wrapper) {
                name.append(",servlet=");
                name.append(container.getName());
            } else if (container instanceof Context) {
                String path = ((Context)container).getPath();
                if (path.length() < 1) {
                    path = "/";
                }
                name.append(",path=");
                name.append(path);
            } else if (container instanceof Host) {
                name.append(",host=");
                name.append(container.getName());
            } else {
                // Should never happen...
                name.append(",unknown");
                name.append(unknown++);
                name.append('=');
                name.append(container.getName());
            }
            container = container.getParent();
        }

        int seq = 0;
        for (Valve valve : container.getPipeline().getValves()) {
            // Skip null valves
            if (valve == null) {
                continue;
            }
            // Only compare valves in pipeline until we find this valve
            if (valve == this) {
                break;
            }
            if (valve.getClass() == this.getClass()) {
                // Duplicate valve earlier in pipeline
                // increment sequence number
                seq ++;
            }
        }
        
        if (seq > 0) {
            name.append(",seq=");
            name.append(seq);
        }

        String className = this.getClass().getName();
        int period = className.lastIndexOf('.');
        if (period >= 0) {
            className = className.substring(period + 1);
        }
        name.append(",name=");
        name.append(className);
        
        return name.toString();
    }

    @Override
    public String getDomainInternal() {
        return MBeanUtils.getDomain(getContainer());
    }
}
