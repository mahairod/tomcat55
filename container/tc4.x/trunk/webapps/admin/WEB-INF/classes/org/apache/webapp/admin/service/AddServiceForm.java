/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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
 */

package org.apache.webapp.admin.service;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.servlet.ServletException;
import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TomcatTreeBuilder;

import org.apache.commons.modeler.Registry;

/**
 * Form bean for the add service page.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public final class AddServiceForm extends ActionForm {
    
    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = null;
    
    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mBServer = null;
    
    // ----------------------------------------------------- Instance Variables
    
    /**
     * The text for the serviceName.
     */
    private String serviceName = null;
    
    /**
     * The text for the engine Name.
     */
    private String engineName = null;
    
    /**
     * The text for the debug level.
     */
    private String debugLvl = "0";
    
    /**
     * The text for the defaultHost Name.
     */
    private String defaultHost = null;
    
    private ArrayList debugLvlVals = null;
    
    // ------------------------------------------------------------- Properties
    
    
    /**
     * Return the debugVals.
     */
    public ArrayList getDebugLvlVals() {
        
        return this.debugLvlVals;
        
    }
    
    /**
     * Set the debugVals.
     */
    public void setDebugLvlVals(ArrayList debugLvlVals) {
        
        this.debugLvlVals = debugLvlVals;
        
    }
    
    /**
     * Return the engineName.
     */
    
    public String getEngineName() {
        
        return this.engineName;
        
    }
    
    /**
     * Set the engineName.
     */
    
    public void setEngineName(String engineName) {
        
        this.engineName = engineName;
        
    }
    
    /**
     * Return the Debug Level Text.
     */
    
    public String getDebugLvl() {
        
        return this.debugLvl;
        
    }
    
    /**
     * Set the Debug Level Text.
     */
    public void setDebugLvl(String debugLvl) {
        
        this.debugLvl = debugLvl;
        
    }
    
    /**
     * Return the Service Name.
     */
    public String getServiceName() {
        
        return this.serviceName;
        
    }
    
    /**
     * Set the Service Name.
     */
    public void setServiceName(String serviceName) {
        
        this.serviceName = serviceName;
        
    }
    
    /**
     * Return the default Host.
     */
    public String getDefaultHost() {
        
        return this.defaultHost;
        
    }
    
    /**
     * Set the default Host.
     */
    public void setDefaultHost(String defaultHost) {
        
        this.defaultHost = defaultHost;
        
    }
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        
        this.serviceName = null;
        this.engineName = null;
        this.debugLvl = "0";
        this.defaultHost = null;
    }
    
    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
    HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        String submit = request.getParameter("submit");
        
        if (submit != null) {
            
            if ((serviceName == null) || (serviceName.length() < 1)) {
                errors.add("serviceName",
                new ActionError("error.serviceName.required"));
            }
            
            if ((engineName == null) || (engineName.length() < 1)) {
                errors.add("engineName",
                new ActionError("error.engineName.required"));
            }
            
            if ((defaultHost == null) || (defaultHost.length() < 1)) {
                errors.add("defaultHost",
                new ActionError("error.defaultHost.required"));
            }
            
            Iterator serviceItr  = null;           
            // service name must be unique.
            try {
                
                ApplicationServlet servlet = (ApplicationServlet)getServlet();
                mBServer = servlet.getServer();
                
                serviceItr =
                mBServer.queryMBeans(new ObjectName(
                TomcatTreeBuilder.SERVICE_TYPE + TomcatTreeBuilder.WILDCARD),
                null).iterator();
                
            } catch (Exception e) {
               getServlet().log("Error getting service mBean", e);
            }
            
            try {
                // check if a service with this name already exists
                while(serviceItr.hasNext()){
                    ObjectInstance service = (ObjectInstance)serviceItr.next();
                    
                    String name =
                    (String)mBServer.getAttribute(service.getObjectName(),"name");
                    
                    // error service name already exists
                    if (name.equalsIgnoreCase(serviceName)) {
                        errors.add("serviceName",
                        new ActionError("error.serviceName.exists"));
                    }
                }
            } catch (Exception e) {
               getServlet().log("Error getting attribute name", e);
            }
        }
        return errors;
    }
}

