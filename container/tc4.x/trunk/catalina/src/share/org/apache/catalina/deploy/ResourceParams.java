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


package org.apache.catalina.deploy;

import java.util.Hashtable;

/**
 * Representation of additional parameters which will be used to initialize
 * external resources defined in the web application deployment descriptor.
 *
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public final class ResourceParams {


    // ------------------------------------------------------------- Properties


    /**
     * The name of this resource parameters. Must be the name of the resource
     * in the java: namespace.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    private Hashtable resourceParams = new Hashtable();

    public void addParameter(String name, String value) {
        resourceParams.put(name, value);
    }

    public Hashtable getParameters() {
        return resourceParams;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ResourceParams[");
        sb.append("name=");
        sb.append(name);
        sb.append(", parameters=");
        sb.append(resourceParams.toString());
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * The NamingResources with which we are associated (if any).
     */
    protected NamingResources resources = null;

    public NamingResources getNamingResources() {
        return (this.resources);
    }

    void setNamingResources(NamingResources resources) {
        this.resources = resources;
    }


}
