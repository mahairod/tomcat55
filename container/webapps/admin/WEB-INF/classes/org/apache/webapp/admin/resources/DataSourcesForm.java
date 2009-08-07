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

package org.apache.webapp.admin.resources;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMapping;


/**
 * Form bean for the delete data sources page.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 * @since 4.1
 */

public final class DataSourcesForm extends BaseForm {


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    /**
     * The object names of the specified environment entries.
     */
    private String dataSources[] = null;

    public String[] getDataSources() {
        return (this.dataSources);
    }

    public void setDataSources(String dataSources[]) {
        this.dataSources = dataSources;
    }

    /**
     * The resource type of this data source.
     */
    private String resourcetype = null;
    
    /**
     * Return the resource type of the data source this bean refers to.
     */
    public String getResourcetype() {
        return this.resourcetype;
    }

    /**
     * Set the resource type of the data source this bean refers to.
     */
    public void setResourcetype(String resourcetype) {
        this.resourcetype = resourcetype;
    }
       
    /**
     * The path of this data source.
     */
    private String path = null;
    
    /**
     * Return the path of the data source this bean refers to.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the path of the data source this bean refers to.
     */
    public void setPath(String path) {
        this.path = path;
    }
       
    /**
     * The host of this data source.
     */
    private String host = null;
    
    /**
     * Return the host of the data source this bean refers to.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Set the host of the data source this bean refers to.
     */
    public void setHost(String host) {
        this.host = host;
    }    
    
       
    /**
     * The domain of this data source.
     */
    private String domain = null;
    
    /**
     * Return the domain of the data source this bean refers to.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Set the domain of the data source this bean refers to.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        super.reset(mapping, request);
        this.dataSources = null;

    }


}
