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
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Service;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardEngine;
import org.apache.commons.modeler.BaseModelMBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardEngine</code> component.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class StandardEngineMBean extends BaseModelMBean {

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
    public StandardEngineMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes



    // ------------------------------------------------------------- Operations


    /**
     * Add a new Host to those assoicated with this Engine
     *
     * @param host MBean Name of the new Host to be added
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void addHost(String host)
        throws Exception {

        StandardEngine engine = (StandardEngine) this.resource;
        ObjectName oname = new ObjectName(host);
        Object obj = mserver.getAttribute(oname, "managedResource");
        Host hostObj = null;
        if (obj instanceof Host) {
            hostObj = (Host) obj;
        }
        engine.addChild(hostObj);

    }


    /**
     * Remove the specified Host from those associated with this Engine
     *
     * @param host MBean Name of the specified Host to be removed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void removeHost(String host)
        throws Exception {

        StandardEngine engine = (StandardEngine) this.resource;
        ObjectName oname = new ObjectName(host);
        Object obj = mserver.getAttribute(oname, "managedResource");
        Host hostObj = null;
        if (obj instanceof Host) {
            hostObj = (Host) obj;
        }
        engine.removeChild(hostObj);

    }


    /**
     * Add a new Valve to those assoicated with this Engine
     *
     * @param valve MBean Name of the new Valve to be added
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void addValve(String valve)
        throws Exception {

        StandardEngine engine = (StandardEngine) this.resource;
        ObjectName oname = new ObjectName(valve);
        Object obj = mserver.getAttribute(oname, "managedResource");
        Valve valveObj = null;
        if (obj instanceof Valve) {
            valveObj = (Valve) obj;
        }
        engine.addValve(valveObj);

    }


    /**
     * Remove the specified Valve from those assoicated with this Engine
     *
     * @param valve MBean Name of the Valve to be removed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void removeValve(String valve)
        throws Exception {

        StandardEngine engine = (StandardEngine) this.resource;
        ObjectName oname = new ObjectName(valve);
        Object obj = mserver.getAttribute(oname, "managedResource");
        Valve valveObj = null;
        if (obj instanceof Valve) {
            valveObj = (Valve) obj;
        }
        engine.removeValve(valveObj);

    }


    /**
     * Associate the specified Logger with this Engine
     *
     * @param logger MBean Name of the Logger with this Engine
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setLogger(String logger)
        throws Exception {

        StandardEngine engine = (StandardEngine) this.resource;
        ObjectName oname = new ObjectName(logger);
        Object obj = mserver.getAttribute(oname, "managedResource");
        Logger loggerObj = null;
        if (obj instanceof Logger) {
            loggerObj = (Logger) obj;
        }
        engine.setLogger(loggerObj);

    }


    /**
     * Associate the specified Realm with this Engine
     *
     * @param realm MBean Name of the Realm with this Engine
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setRealm(String realm)
        throws Exception {

        StandardEngine engine = (StandardEngine) this.resource;
        ObjectName oname = new ObjectName(realm);
        Object obj = mserver.getAttribute(oname, "managedResource");
        Realm realmObj = null;
        if (obj instanceof Realm) {
            realmObj = (Realm) obj;
        }
        engine.setRealm(realmObj);

    }


}
