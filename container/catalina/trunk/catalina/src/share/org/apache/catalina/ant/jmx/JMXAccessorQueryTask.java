/*
 * Copyright 2002,2004 The Apache Software Foundation.
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


package org.apache.catalina.ant.jmx;


import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.tools.ant.BuildException;


/**
 * Query for Mbeans . 
 * <ul>
 * <li>open no existing JSR 160 rmi jmx connection</li>
 * <li>Get all Mbeans attributes</li>
 * <li>Get only the Query Mbeans ObjectNames</li>
 * <li>Show query result as Ant console log</li>
 * <li>Bind query result as Ant properties</li>
 * </ul>
 * <br/>
 * Query a list of Mbean 
 * <code>
 *   &lt;jmxQuery
 *           host="127.0.0.1"
 *           port="9014"
 *           name="Catalina:type=Manager,* 
 *           resultproperty="manager" /&gt;
 * </code>
 * with attribute <em>attributebinding="true"</em> you can get 
 * all attributes also from result objects.<br/>
 * The poperty manager.lenght show the size of the result 
 * and with manager.[0..lenght].name the 
 * resulted ObjectNames are saved. 
 * These tasks require Ant 1.6 or later interface.
 *
 * @author Peter Rossbach
 * @version $Revision$ $Date$
 * @since 5.5.10
 */

public class JMXAccessorQueryTask extends JMXAccessorTask {

    // ----------------------------------------------------- Instance Variables

    private boolean attributebinding = false;

    // ----------------------------------------------------- Instance Info

    /**
     * Descriptive information describing this implementation.
     */
    private static final String info = "org.apache.catalina.ant.JMXAccessorQueryTask/1.0";

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }

    // ------------------------------------------------------------- Properties
    
    /**
     * @return Returns the attributebinding.
     */
    public boolean isAttributebinding() {
        return attributebinding;
    }
    /**
     * @param attributebinding The attributebinding to set.
     */
    public void setAttributebinding(boolean attributeBinding) {
        this.attributebinding = attributeBinding;
    }
  
    // ------------------------------------------------------ protected Methods

    
    /**
     * Execute the specified command, based on the configured properties. The
     * input stream will be closed upon completion of this task, whether it was
     * executed successfully or not.
     * 
     * @exception BuildException
     *                if an error occurs
     */
    protected String jmxExecute(MBeanServerConnection jmxServerConnection)
        throws Exception {

        if (getName() == null) {
            throw new BuildException("Must specify a 'name'");
        }
        return jmxQuery(jmxServerConnection, getName());

    }

       
    /**
     * Call Mbean server for some mbeans with same domain, attributes.
     *  with <em>attributebindung=true</em> you can save all attributes from all found objects
     * as your ant properties
     * @param jmxServerConnection
     * @param qry
     * @return
     */
    protected String jmxQuery(MBeanServerConnection jmxServerConnection,
            String qry) {
        String isError = null;
        Set names = null;
        String resultproperty = getResultproperty();
        try {
            names = jmxServerConnection.queryNames(new ObjectName(qry), null);
            if (resultproperty != null)
                getProject().setNewProperty(resultproperty + ".length",
                        Integer.toString(names.size()));
        } catch (Exception e) {
            if (isEcho())
                handleErrorOutput(e.getMessage());
            return "Can't query mbeans " + qry;
        }

        Iterator it = names.iterator();
        int oindex = 0;
        String pname = null;
        while (it.hasNext()) {
            ObjectName oname = (ObjectName) it.next();
            pname = resultproperty + "." + Integer.toString(oindex) + ".";
            oindex++;
            if (isEcho())
                handleOutput(pname + "name=" + oname.toString());
            if (resultproperty != null) {
                getProject().setNewProperty(pname + "name",
                        oname.toString());
            }
            if (isAttributebinding()) {
                bindAttributes(jmxServerConnection, resultproperty, pname, oname);
            }
        }
        return isError;
    }

    /**
     * @param jmxServerConnection
     * @param resultproperty
     * @param pname
     * @param oname
     */
    protected void bindAttributes(MBeanServerConnection jmxServerConnection, String resultproperty, String pname, ObjectName oname) {
        try {
            MBeanInfo minfo = jmxServerConnection.getMBeanInfo(oname);
            String code = minfo.getClassName();
            if ("org.apache.commons.modeler.BaseModelMBean"
                    .equals(code)) {
                code = (String) jmxServerConnection.getAttribute(oname,
                        "modelerType");
            }
            MBeanAttributeInfo attrs[] = minfo.getAttributes();
            Object value = null;

            for (int i = 0; i < attrs.length; i++) {
                if (!attrs[i].isReadable())
                    continue;
                String attName = attrs[i].getName();
                if (attName.indexOf("=") >= 0
                        || attName.indexOf(":") >= 0
                        || attName.indexOf(" ") >= 0) {
                    continue;
                }

                try {
                    value = jmxServerConnection.getAttribute(oname,
                            attName);
                } catch (Throwable t) {
                    if (isEcho())
                        handleErrorOutput("Error getting attribute "
                                + oname + " " + pname + attName + " "
                                + t.toString());
                    continue;
                }
                if (value == null)
                    continue;
                if ("modelerType".equals(attName))
                    continue;
                String valueString = value.toString();
                if (isEcho())
                    handleOutput(pname + attName + "=" + valueString);
                if (resultproperty != null)
                    getProject().setNewProperty(pname + attName,
                            valueString);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
