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


package org.apache.catalina.deploy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Representation of a handler reference for a web service, as
 * represented in a <code>&lt;handler&gt;</code> element in the
 * deployment descriptor.
 *
 * @author Fabien Carrion
 */

public class ContextHandler extends ResourceBase implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The Handler reference class.
     */
    private String handlerclass = null;

    public String getHandlerclass() {
        return (this.handlerclass);
    }

    public void setHandlerclass(String handlerclass) {
        this.handlerclass = handlerclass;
    }

    /**
     * A list of QName specifying the SOAP Headers the handler will work on. 
     * -namespace and locapart values must be found inside the WSDL.
     *
     * A service-qname is composed by a namespaceURI and a localpart.
     *
     * soapHeader[0] : namespaceURI
     * soapHeader[1] : localpart
     */
    private HashMap<String, String> soapHeaders = new HashMap<String, String>();

    public Iterator<String> getLocalparts() {
        return soapHeaders.keySet().iterator();
    }

    public String getNamespaceuri(String localpart) {
        return soapHeaders.get(localpart);
    }

    public void addSoapHeaders(String localpart, String namespaceuri) {
        soapHeaders.put(localpart, namespaceuri);
    }

    /**
     * Set a configured property.
     */
    public void setProperty(String name, String value) {
        this.setProperty(name, (Object) value);
    }

    /**
     * The soapRole.
     */
    private ArrayList<String> soapRoles = new ArrayList<String>();

    public String getSoapRole(int i) {
        return this.soapRoles.get(i);
    }

    public int getSoapRolesSize() {
        return this.soapRoles.size();
    }

    public void addSoapRole(String soapRole) {
        this.soapRoles.add(soapRole);
    }

    /**
     * The portName.
     */
    private ArrayList<String> portNames = new ArrayList<String>();

    public String getPortName(int i) {
        return this.portNames.get(i);
    }

    public int getPortNamesSize() {
        return this.portNames.size();
    }

    public void addPortName(String portName) {
        this.portNames.add(portName);
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("ContextHandler[");
        sb.append("name=");
        sb.append(getName());
        if (handlerclass != null) {
            sb.append(", class=");
            sb.append(handlerclass);
        }
        if (this.soapHeaders != null) {
            sb.append(", soap-headers=");
            sb.append(this.soapHeaders);
        }
        if (this.getSoapRolesSize() > 0) {
            sb.append(", soap-roles=");
            sb.append(soapRoles);
        }
        if (this.getPortNamesSize() > 0) {
            sb.append(", port-name=");
            sb.append(portNames);
        }
        if (this.listProperties() != null) {
            sb.append(", init-param=");
            sb.append(this.listProperties());
        }
        sb.append("]");
        return (sb.toString());

    }


}
