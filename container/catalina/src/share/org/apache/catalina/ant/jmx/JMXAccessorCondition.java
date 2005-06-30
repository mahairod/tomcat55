package org.apache.catalina.ant.jmx;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 *
 * <b>Definition</b>:
 * <pre> 
 *   &lt;path id="catalina_ant">
 *       &lt;fileset dir="${catalina.home}/server/lib">
 *           &lt;include name="catalina-ant.jar"/>
 *           &lt;include name="catalina-ant-jmx.jar"/>
 *       &lt;/fileset>
 *   &lt;/path>
 *
 *   &lt;typedef
 *       name="jmxCondition"
 *       classname="org.apache.catalina.ant.jmx.JMXAccessorCondition"
 *       classpathref="catalina_ant"/>
 *   &lt;taskdef
 *       name="jmxOpen"
 *       classname="org.apache.catalina.ant.jmx.JMXAccessorTask"
 *       classpathref="catalina_ant"/>
 * </pre>
 * 
 * <b>Usage</b>: Wait for start backup node
 * <pre>
 *     &lt;target name="wait"&gt;
 *       &lt;jmxOpen
 *               host="${jmx.host}" port="${jmx.port}" username="${jmx.username}" password="${jmx.password}" /&gt;
 *        &lt;waitfor maxwait="${maxwait}" maxwaitunit="second" timeoutproperty="server.timeout" &gt;
 *           &lt;and&gt;
 *               &lt;socket server="${server.name}" port="${server.port}"/&gt;
 *               &lt;http url="${url}"/&gt;
 *               &lt;jmxCondition
 *                   name="Catalina:type=IDataSender,host=localhost,senderAddress=192.168.111.1,senderPort=9025"
 *                   operation="==" 
 *                   attribute="connected" value="true"
 *               /&gt;
 *               &lt;jmxCondition
 *                   operation="&amp;lt;"
 *                   name="Catalina:j2eeType=WebModule,name=//${tomcat.application.host}${tomcat.application.path},J2EEApplication=none,J2EEServer=none"
 *                   attribute="startupTime" value="250"
 *               /&gt;
 *           &lt;/and&gt;
 *       &lt;/waitfor&gt;
 *       &lt;fail if="server.timeout" message="Server ${url} don't answer inside ${maxwait} sec" /&gt;
 *       &lt;echo message="Server ${url} alive" /&gt;
 *   &lt;/target&gt;
 *
 * </pre>
 * Allowed operation between jmx attribute and reference value:
 * <ul>
 * <li>==  equals</li>
 * <li>!=  not equals</li>
 * <li>&gt; greater than (&amp;gt;)</li>
 * <li>&gt;= greater than or equals (&amp;gt;=)</li>
 * <li>&lt; lesser than (&amp;lt;)</li>
 * <li>&lt;= lesser than or equals (&amp;lt;=)</li>
 * </ul> 
 * <b>NOTE</b>:  For numeric expressions the type must be set and use xml entities as operations.<br/>
 * As type we currently support <em>long</em> and <em>double</em>.
 * @author Peter Rossbach
 * @version $Revision$ $Date$
 * @since 5.5.10
 *
 */
public class JMXAccessorCondition extends ProjectComponent implements Condition {

    // ----------------------------------------------------- Instance Variables

    private String url = null;
    private String host = "localhost";
    private String port = "8050";
    private String password = null;
    private String username = null;
    private String name = null;
    private String attribute;
    private String value;
    private String operation = "==" ;
    private String type = "long" ;
    private String ref = "jmx.server";
    
    // ----------------------------------------------------- Instance Info

    /**
     * Descriptive information describing this implementation.
     */
    private static final String info = "org.apache.catalina.ant.JMXAccessorCondition/1.1";

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }
    // ----------------------------------------------------- Properties

    /**
     * @return Returns the operation.
     */
    public String getOperation() {
        return operation;
    }
    /**
     * @param operation The operation to set.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @return Returns the attribute.
     */
    public String getAttribute() {
        return attribute;
    }
    /**
     * @param attribute The attribute to set.
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
    /**
     * @return Returns the host.
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String objectName) {
        this.name = objectName;
    }
    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @return Returns the port.
     */
    public String getPort() {
        return port;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(String port) {
        this.port = port;
    }
    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }
    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    // The setter for the "value" attribute
    public void setValue(String value) {
        this.value = value;
    }
 
    /**
     * @return Returns the ref.
     */
    public String getRef() {
        return ref;
    }
    /**
     * @param ref The ref to set.
     */
    public void setRef(String refId) {
        this.ref = refId;
    }
    
    protected MBeanServerConnection getJMXConnection()
            throws MalformedURLException, IOException {
        return JMXAccessorTask.accessJMXConnection(
                getProject(),
                getUrl(), getHost(),
                getPort(), getUsername(), getPassword(), ref);
    }

    /**
     * @return
     */
    protected String accessJMXValue() {
        try {
            Object result = getJMXConnection().getAttribute(
                    new ObjectName(name), attribute);
            if(result != null)
                return result.toString();
        } catch (Exception e) {
            // ignore access or connection open errors
        }
        return null;
    }

    // This method evaluates the condition
    public boolean eval() {
        if (operation == null) {
            throw new BuildException("operation attribute is not set");
        }
        if (value == null) {
            throw new BuildException("value attribute is not set");
        }
        if ((name == null || attribute == null)) {
            throw new BuildException(
                    "Must specify a 'attribute', name for equals condition");
        }
        //FIXME check url or host/parameter
        String jmxValue = accessJMXValue();
        String op = getOperation() ;
        if(jmxValue != null) {
            if("==".equals(op)) {
                return jmxValue.equals(value);
            } else if("!=".equals(op)) {
                return !jmxValue.equals(value);
            } else { 
                if("long".equals(type)) {
                    long jvalue = Long.parseLong(jmxValue);                                       
                    long lvalue = Long.parseLong(value);
                    if(">".equals(op)) {
                        return jvalue > lvalue ;
                    } else if(">=".equals(op)) {
                        return jvalue >= lvalue ;                        
                    } else if("<".equals(op)) {
                        return jvalue < lvalue ;                        
                    } else if ("<=".equals(op)) {
                        return jvalue <= lvalue;
                    }
                } else if("double".equals(type)) {
                    double jvalue = Double.parseDouble(jmxValue);                                       
                    double dvalue = Double.parseDouble(value);
                    if(">".equals(op)) {
                        return jvalue > dvalue ;
                    } else if(">=".equals(op)) {
                        return jvalue >= dvalue ;                        
                    } else if("<".equals(op)) {
                        return jvalue < dvalue ;                        
                    } else if ("<=".equals(op)) {
                        return jvalue <= dvalue;
                    }
                }
            }
        }
        return false;
    }
 }

