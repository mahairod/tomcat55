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

package org.apache.jk.status;

import java.util.Iterator;
import java.util.List;

import org.apache.catalina.ant.BaseRedirectorHelperTask;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tools.ant.BuildException;

/**
 * Ant task that implements the show <code>/jkstatus</code> command, supported
 * by the mod_jk status (1.2.13) application.
 * 
 * @author Peter Rossbach
 * @version $Revision:$
 * @since 5.5.10
 */
public class JkStatusTask extends BaseRedirectorHelperTask {

    /**
     * Store status as <code>resultProperty</code> prefix.
     */
    protected String resultproperty;

    /**
     * Echo status at ant console
     */
    protected boolean echo = false;

    /**
     * The login password for the <code>mod_jk status</code> page.
     */
    protected String password = null;

    /**
     * The URL of the <code>mod_jk status</code> page to be used.
     */
    protected String url = "http://localhost:80/jkstatus";

    /**
     * The login username for the <code>mod_jk status</code> page.
     */
    protected String username = null;

    private String errorProperty;

    private String worker;

    private String loadbalancer;

    public String getPassword() {
        return (this.password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return (this.url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return (this.username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Returns the echo.
     */
    public boolean isEcho() {
        return echo;
    }

    /**
     * @param echo
     *            The echo to set.
     */
    public void setEcho(boolean echo) {
        this.echo = echo;
    }

    /**
     * @return Returns the resultproperty.
     */
    public String getResultproperty() {
        return resultproperty;
    }

    /**
     * @param resultproperty
     *            The resultproperty to set.
     */
    public void setResultproperty(String resultproperty) {
        this.resultproperty = resultproperty;
    }

    /**
     * @return Returns the loadbalancer.
     */
    public String getLoadbalancer() {
        return loadbalancer;
    }
    /**
     * @param loadbalancer The loadbalancer to set.
     */
    public void setLoadbalancer(String loadbalancer) {
        this.loadbalancer = loadbalancer;
    }
    /**
     * @return Returns the worker.
     */
    public String getWorker() {
        return worker;
    }
    /**
     * @param worker The worker to set.
     */
    public void setWorker(String worker) {
        this.worker = worker;
    }
    // --------------------------------------------------------- Public Methods

    /**
     * Get jkstatus from server.
     * 
     * @exception BuildException
     *                if a validation error occurs
     */
    public void execute() throws BuildException {

        if (url == null) {
            throw new BuildException("Must specify an 'url'");
        }
        boolean isWorkerOnly = worker != null && !"".equals(worker);
        boolean isLoadbalancerOnly = loadbalancer != null
                && !"".equals(loadbalancer);

        StringBuffer error = new StringBuffer();
        try {
            JkStatusAccessor accessor = new JkStatusAccessor();
            JkStatus status = accessor.status(url, username, password);
            if (!isWorkerOnly && !isLoadbalancerOnly) {
                JkServer server = status.getServer();
                if (resultproperty != null) {
                    createProperty(server, "name");
                    createProperty(server, "port");
                    createProperty(server, "version");
                    createProperty(server, "software");
                }
                if (isEcho()) {
                    handleOutput("server name=" + server.getName() + "."
                            + server.getPort() + " - " + server.getSoftware());
                }
            }
            List balancers = status.getBalancers();
            for (Iterator iter = balancers.iterator(); iter.hasNext();) {
                JkBalancer balancer = (JkBalancer) iter.next();
                String balancerIndex = null;
                if (isLoadbalancerOnly) {
                    if (loadbalancer.equals(balancer.getName())) {
                        if (resultproperty != null) {
                            setPropertyBalancerOnly(balancer);
                        }
                        echoBalancer(balancer);
                        return;
                    }
                } else {
                    if (!isWorkerOnly) {
                        if (resultproperty != null) {
                            balancerIndex = Integer.toString(balancer.getId());
                            setPropertyBalancer(balancer,balancerIndex);
                        }
                        echoBalancer(balancer);
                    }
                    List members = balancer.getBalancerMembers();
                    for (Iterator iterator = members.iterator(); iterator
                            .hasNext();) {
                        JkBalancerMember member = (JkBalancerMember) iterator
                                .next();
                        if (isWorkerOnly) {
                            if (worker.equals(member.getName())) {
                                if (resultproperty != null) {
                                    setPropertyWorkerOnly(balancer, member);
                                }
                                echoWorker(member);
                                return;
                            }
                        } else {
                            if (resultproperty != null) {
                                setPropertyWorker(balancerIndex, member);
                            }
                            echoWorker(member);
                            if (!"OK".equals(member.getStatus())) {
                                error.append(" worker name=" + member.getName()
                                        + " status=" + member.getStatus()
                                        + " host=" + member.getAddress());
                            }
                        }
                    }
                    if (!isWorkerOnly) {
                        if (resultproperty != null && members.size() > 0) {
                            getProject().setNewProperty(
                                    resultproperty + ".balancer."
                                            + balancerIndex + ".member.length",
                                    Integer.toString(members.size()));
                        }
                        List mappings = balancer.getBalancerMappings();
                        int j = 0;
                        for (Iterator iterator = mappings.iterator(); iterator
                                .hasNext(); j++) {
                            JkBalancerMapping mapping = (JkBalancerMapping) iterator
                                    .next();
                            if (resultproperty != null) {
                                String stringIndex2 = Integer.toString(j);
                                createProperty(mapping, balancerIndex,
                                        stringIndex2, "type");
                                createProperty(mapping, balancerIndex,
                                        stringIndex2, "uri");
                                createProperty(mapping, balancerIndex,
                                        stringIndex2, "context");
                            }
                            if (isEcho()) {
                                handleOutput("balancer name="
                                        + balancer.getName() + " mappingtype="
                                        + mapping.getType() + " uri="
                                        + mapping.getUri() + " context="
                                        + mapping.getContext());
                            }
                        }
                        if (resultproperty != null && mappings.size() > 0) {
                            getProject().setNewProperty(
                                    resultproperty + ".balancer."
                                            + balancerIndex + ".map.length",
                                    Integer.toString(mappings.size()));
                        }
                    }
                }
            }
            if (!isWorkerOnly && !isLoadbalancerOnly) {
                if (resultproperty != null && balancers.size() > 0) {
                    getProject().setNewProperty(
                            resultproperty + ".balancer.length",
                            Integer.toString(balancers.size()));
                }
            }
        } catch (Throwable t) {
            error.append(t.getMessage());
            if (getErrorProperty() != null) {
                getProject().setNewProperty(errorProperty, error.toString());
            }
            if (isFailOnError()) {
                throw new BuildException(t);
            } else {
                handleErrorOutput(t.getMessage());
                return;
            }
        }
        if (error.length() != 0) {
            if (getErrorProperty() != null) {
                getProject().setNewProperty(errorProperty, error.toString());
            }
            if (isFailOnError()) {
                // exception should be thrown only if failOnError == true
                // or error line will be logged twice
                throw new BuildException(error.toString());
            }
        }

    }

    /**
     * @param member
     */
    private void echoWorker(JkBalancerMember member) {
        if (isEcho()) {
            handleOutput("worker name=" + member.getName() + " status="
                    + member.getStatus() + " host=" + member.getAddress());
        }
    }

    /**
     * @param balancer
     */
    private void echoBalancer(JkBalancer balancer) {
        if (isEcho()) {
            handleOutput("balancer name=" + balancer.getName() + " type="
                    + balancer.getType());
        }
    }

    /**
     * @param balancer
     */
    private void setPropertyBalancerOnly(JkBalancer balancer) {
        String prefix = resultproperty + "." + balancer.getName();
        getProject().setNewProperty(prefix + ".id",
                Integer.toString(balancer.getId()));
        getProject().setNewProperty(prefix + ".type", balancer.getType());
        getProject().setNewProperty(prefix + ".sticky",
                Boolean.toString(balancer.isSticky()));
        getProject().setNewProperty(prefix + ".stickyforce",
                Boolean.toString(balancer.isStickyforce()));
        getProject().setNewProperty(prefix + ".retries",
                Integer.toString(balancer.getRetries()));
        getProject().setNewProperty(prefix + ".recover",
                Integer.toString(balancer.getRecover()));
    }

    /**
     * @param balancer
     * @return
     */
    private void setPropertyBalancer(JkBalancer balancer,String balancerIndex) {
        createProperty(balancer, balancerIndex, "id");
        createProperty(balancer, balancerIndex, "name");
        createProperty(balancer, balancerIndex, "type");
        createProperty(balancer, balancerIndex, "sticky");
        createProperty(balancer, balancerIndex, "stickyforce");
        createProperty(balancer, balancerIndex, "retries");
        createProperty(balancer, balancerIndex, "recover");
    }

    /**
     * @param balancerIndex
     * @param member
     */
    private void setPropertyWorker(String balancerIndex, JkBalancerMember member) {
        String workerIndex = Integer.toString(member.getId());
        createProperty(member, balancerIndex, workerIndex, "id");
        createProperty(member, balancerIndex, workerIndex, "name");
        createProperty(member, balancerIndex, workerIndex, "type");
        createProperty(member, balancerIndex, workerIndex, "host");
        createProperty(member, balancerIndex, workerIndex, "port");
        createProperty(member, balancerIndex, workerIndex, "address");
        createProperty(member, balancerIndex, workerIndex, "status");
        createProperty(member, balancerIndex, workerIndex, "lbfactor");
        createProperty(member, balancerIndex, workerIndex, "lbvalue");
        createProperty(member, balancerIndex, workerIndex, "elected");
        createProperty(member, balancerIndex, workerIndex, "readed");
        createProperty(member, balancerIndex, workerIndex, "busy");
        createProperty(member, balancerIndex, workerIndex, "transferred");
        createProperty(member, balancerIndex, workerIndex, "errors");
        if (member.getDomain() != null)
            createProperty(member, balancerIndex, workerIndex, "domain");
        else
            getProject().setNewProperty(resultproperty + ".balancer." + balancerIndex + ".member." + workerIndex +
                    ".domain", "");          
        if (member.getRedirect() != null)
            createProperty(member, balancerIndex, workerIndex, "redirect");
        else
            getProject().setNewProperty(resultproperty + ".balancer." + balancerIndex + ".member." + workerIndex +
                    ".redirect", "");          
    }

    /**
     * @param balancer
     * @param member
     */
    private void setPropertyWorkerOnly(JkBalancer balancer,
            JkBalancerMember member) {
        String prefix = resultproperty + "." + member.getName();
        getProject().setNewProperty(prefix + ".lb.id",
                Integer.toString(balancer.getId()));
        getProject().setNewProperty(prefix + ".lb.name", balancer.getName());
        getProject().setNewProperty(prefix + ".id",
                Integer.toString(member.getId()));
        getProject().setNewProperty(prefix + ".type", member.getType());
        getProject().setNewProperty(prefix + ".status", member.getStatus());
        getProject().setNewProperty(prefix + ".host", member.getHost());
        getProject().setNewProperty(prefix + ".address", member.getAddress());
        getProject().setNewProperty(prefix + ".port",
                Integer.toString(member.getPort()));
        getProject().setNewProperty(prefix + ".lbfactor",
                Integer.toString(member.getLbfactor()));
        getProject().setNewProperty(prefix + ".lbvalue",
                Long.toString(member.getLbvalue()));
        getProject().setNewProperty(prefix + ".elected",
                Long.toString(member.getElected()));
        getProject().setNewProperty(prefix + ".readed",
                Long.toString(member.getReaded()));
        getProject().setNewProperty(prefix + ".transferred",
                Long.toString(member.getTransferred()));
        getProject().setNewProperty(prefix + ".busy",
                Integer.toString(member.getBusy()));
        getProject().setNewProperty(prefix + ".errors",
                Long.toString(member.getErrors()));
        if (member.getDomain() != null)
            getProject().setNewProperty(prefix + ".domain", member.getDomain());
        else
            getProject().setNewProperty(prefix + ".domain", "");
        if (member.getRedirect() != null)
            getProject().setNewProperty(prefix + ".redirect",
                    member.getRedirect());
        else
            getProject().setNewProperty(prefix + ".redirect", "");
            
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.ant.BaseRedirectorHelperTask#setErrorProperty(java.lang.String)
     */
    public void setErrorProperty(String arg0) {
        errorProperty = arg0;
        super.setErrorProperty(arg0);
    }

    /**
     * @return Returns the errorProperty.
     */
    public String getErrorProperty() {
        return errorProperty;
    }

    protected void createProperty(Object result, String attribute) {
        createProperty(result, null, null, attribute);
    }

    protected void createProperty(Object result, String arraymark,
            String attribute) {
        createProperty(result, arraymark, null, attribute);
    }

    /**
     * create result as property with name from attribute resultproperty
     */
    protected void createProperty(Object result, String arraymark,
            String arraymark2, String attribute) {
        if (resultproperty != null) {
            Object value = IntrospectionUtils.getProperty(result, attribute);
            if (value != null) {
                StringBuffer propertyname = new StringBuffer(resultproperty);

                if (result instanceof JkServer) {
                    propertyname.append(".server");
                } else if (result instanceof JkBalancer) {
                    propertyname.append(".balancer");
                    if (arraymark != null) {
                        propertyname.append(".");
                        propertyname.append(arraymark);
                    }
                } else if (result instanceof JkBalancerMember) {
                    propertyname.append(".balancer");
                    if (arraymark != null) {
                        propertyname.append(".");
                        propertyname.append(arraymark);
                    }
                    propertyname.append(".member");
                    if (arraymark2 != null) {
                        propertyname.append(".");
                        propertyname.append(arraymark2);
                    }

                } else if (result instanceof JkBalancerMapping) {
                    propertyname.append(".balancer");
                    if (arraymark != null) {
                        propertyname.append(".");
                        propertyname.append(arraymark);
                    }
                    propertyname.append(".map");
                    if (arraymark2 != null) {
                        propertyname.append(".");
                        propertyname.append(arraymark2);
                    }
                }
                propertyname.append(".");
                propertyname.append(attribute);
                getProject().setNewProperty(propertyname.toString(),
                        value.toString());
            }
        }
    }

}