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
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteHostValve;
import org.apache.catalina.valves.RequestDumperValve;
import org.apache.commons.modeler.BaseModelMBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardEngine</code> component.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class StandardEngineMBean extends BaseModelMBean {


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


    /**
     * Return the parent (Service) that owns this Engine.
     */
    public Service getParent() {

        if (this.resource == null)
            return (null);
        Engine engine = (Engine) this.resource;
        return (engine.getService());

    }


    // ------------------------------------------------------------- Operations


    /**
     * Create a new AccessLogger.
     */
    public AccessLogValve createAccessLogger(int debug, String directory,
        String pattern, String prefix, boolean resolveHosts, String suffix) {

        AccessLogValve accessLogger = new AccessLogValve();

        //accessLogger.setDebug(debug); FIX ME - no debug property existS
        accessLogger.setDirectory(directory);
        accessLogger.setPattern(pattern);
        accessLogger.setPrefix(prefix);
        accessLogger.setResolveHosts(resolveHosts);
        accessLogger.setSuffix(suffix);

        return accessLogger;

    }


    /**
     * Create a new Host.
     */
    public Host createHost(String appBase, int debug, String name,
                                                        boolean unpackWARs) {

        StandardHost host = new StandardHost();

        host.setAppBase(appBase);
        host.setDebug(debug);
        host.setName(name);
        host.setUnpackWARs(unpackWARs);

        return host;

    }


    /**
     * Create a new RequestFilterValve.
     */
    public Valve createRequestFilterValve(String type, String allow,
                                                    int debug, String deny) {

        Valve valve = null;
        if (type.equals("RemoteAddrValve")) {
            valve = new RemoteAddrValve();
            ((RemoteAddrValve)valve).setAllow(allow);
            ((RemoteAddrValve)valve).setDeny(deny);
        } else if (type.equals("RemostHostValve")) {
            valve = new RemoteHostValve();
            ((RemoteHostValve)valve).setAllow(allow);
            ((RemoteHostValve)valve).setDeny(deny);
        } else if (type.equals("RequestDumperValve")) {
            valve = new RequestDumperValve();
        }

        return valve;

    }


}
