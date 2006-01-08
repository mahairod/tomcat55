/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.mbeans;

import java.lang.reflect.Method;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.coyote.tomcat4.CoyoteConnector</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision$ $Date$
 */

public class ConnectorMBean extends ClassNameMBean {


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
    public ConnectorMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes



    // ------------------------------------------------------------- Operations

    
    /**
     * Return Client authentication info
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String getClientAuth()
        throws Exception {
            
        Object clientAuthObj = null;
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // get clientAuth
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("getClientAuth", null);
                clientAuthObj = meth2.invoke(factory, null);
            }
           
        }    
        if (clientAuthObj instanceof String) {
            return (String)clientAuthObj;
        } else return "false";
        
    }
    
    
    /**
     * Set Client authentication info
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setClientAuth(String clientAuth)
        throws Exception {
            
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // set clientAuth
                Class partypes2 [] = new Class[1];
                partypes2[0] = String.class;
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("setClientAuth", partypes2);
                Object arglist2[] = new Object[1];
                arglist2[0] = clientAuth;
                meth2.invoke(factory, arglist2);
            } 
        } 
        
    }

    
    /**
     * Return keystoreFile
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String getKeystoreFile()
        throws Exception {
            
        Object keystoreFileObj = null;
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get keystoreFile
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // get keystoreFile
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("getKeystoreFile", null);
                keystoreFileObj = meth2.invoke(factory, null);
            } 
        }    
        
        if (keystoreFileObj == null) {
            return null;
        } else {
            return keystoreFileObj.toString();
        }
        
    }
    
    
    /**
     * Set keystoreFile
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setKeystoreFile(String keystoreFile)
        throws Exception {
        
        if (keystoreFile == null) {
            keystoreFile = "";
        }
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // set keystoreFile
                Class partypes2 [] = new Class[1];
                String str = new String();
                partypes2[0] = str.getClass();
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("setKeystoreFile", partypes2);
                Object arglist2[] = new Object[1];
                arglist2[0] = keystoreFile;
                meth2.invoke(factory, arglist2);
            }
           
        }    
    }
    
    
    /**
     * Return keystorePass
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String getKeystorePass()
        throws Exception {
            
        Object keystorePassObj = null;
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // get keystorePass
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("getKeystorePass", null);
                keystorePassObj = meth2.invoke(factory, null);
            }
           
        }    
        
        if (keystorePassObj == null) {
            return null;
        } else {
            return keystorePassObj.toString();
        } 
        
    }
    
    
    /**
     * Set keystorePass
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setKeystorePass(String keystorePass)
        throws Exception {
            
        if (keystorePass == null) {
            keystorePass = "";
        }
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // set keystorePass
                Class partypes2 [] = new Class[1];
                String str = new String();
                partypes2[0] = str.getClass();
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("setKeystorePass", partypes2);
                Object arglist2[] = new Object[1];
                arglist2[0] = keystorePass;
                meth2.invoke(factory, arglist2);
            }
        }    
    }
    
    
}
