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
 */


package org.apache.webapp.admin;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.QueryExp;
import javax.management.Query;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanInfo;

import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

import org.apache.struts.util.MessageResources;

/**
 * Test <code>Action</code> that handles events from the tree control test
 * page.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class SetUpServiceAction extends Action {
    
    private static MBeanServer mBServer = null;
    
    public final static String NAME_PROP_NAME = "name";
    public final static String HOST_PROP_NAME = "defaultHost";
    public final static String DEBUG_PROP_NAME = "debug";
    
    private ArrayList debugLvlList = null;
    private ArrayList hostNameList = null;
    
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward perform(ActionMapping mapping,
    ActionForm form,
    HttpServletRequest request,
    HttpServletResponse response)
    throws IOException, ServletException {

        HttpSession session = request.getSession();
        
        if (form == null) {
            getServlet().log(" Creating new ServiceForm bean under key "
            + mapping.getAttribute());
            form = new ServiceForm();
            
            if ("request".equals(mapping.getScope()))
                request.setAttribute(mapping.getAttribute(), form);
            else
                session.setAttribute(mapping.getAttribute(), form);
            
        }
        
        // The message resources for this package.
    //    MessageResources messages = getResources();
    //    Locale locale = (Locale)session.getAttribute(Action.LOCALE_KEY);
        
        String selectedName = request.getParameter("select");
        
        ServiceForm serviceFm = (ServiceForm) form;
        
        if(debugLvlList == null) {
            debugLvlList = new ArrayList();
            debugLvlList.add(new LabelValueBean("0", "0"));
            debugLvlList.add(new LabelValueBean("1", "1"));
            debugLvlList.add(new LabelValueBean("2", "2"));
            debugLvlList.add(new LabelValueBean("3", "3"));
            debugLvlList.add(new LabelValueBean("4", "4"));
            debugLvlList.add(new LabelValueBean("5", "5"));
            debugLvlList.add(new LabelValueBean("6", "6"));
            debugLvlList.add(new LabelValueBean("7", "7"));
            debugLvlList.add(new LabelValueBean("8", "8"));
            debugLvlList.add(new LabelValueBean("9", "9"));
            
        }
        
        String serviceName = null;
        String engineName = null;
        Integer debug = null;
        String defaultHost = null;
        
        try{
            
            if(mBServer == null) {
                ApplicationServlet servlet = (ApplicationServlet)getServlet();
                mBServer = servlet.getServer();
            }
            
            Iterator serviceItr =
            mBServer.queryMBeans(new
            ObjectName(selectedName), null).iterator();
            
            ObjectInstance objInstance = (ObjectInstance)serviceItr.next();
            ObjectName serviceObjName = (objInstance).getObjectName();
            
            /*
            System.out.println("There are " + mBServer.getMBeanCount().intValue() +
            " registered MBeans");
            Iterator instances = mBServer.queryMBeans(null, null).iterator();
            while (instances.hasNext()) {
                ObjectInstance instance = (ObjectInstance) instances.next();
                System.out.println("  objectName=" + instance.getObjectName() +
                ", className=" + instance.getClassName());
             }
             */
            
            serviceName = (String) mBServer.getAttribute(serviceObjName,
            NAME_PROP_NAME);
            
            String search =  TomcatTreeBuilder.ENGINE_TYPE +
            ",service=" + serviceName;
            
            Iterator engineItr =
            mBServer.queryMBeans(new ObjectName(search), null).iterator();
            
            ObjectName engineObjName = ((ObjectInstance)engineItr.next()).getObjectName();
            
            /*set values from engine mBean*/
            engineName = (String) mBServer.getAttribute(engineObjName,
            NAME_PROP_NAME);
            
            debug = (Integer) mBServer.getAttribute(engineObjName,
            DEBUG_PROP_NAME);
            
            defaultHost = (String) mBServer.getAttribute(engineObjName,
            HOST_PROP_NAME);
            
            // defaultHost is an optional attribute of Engine,
            // display blank if this value was not set.
            if (defaultHost == null) defaultHost =" ";
            
            /* Now Extracting all Hostnames configured for this engine */
            search =  TomcatTreeBuilder.HOST_TYPE
            + TomcatTreeBuilder.WILDCARD
            + ",service=" + serviceName;
            
            Iterator hostItr =
            mBServer.queryMBeans(new ObjectName(search), null).iterator();
            
            hostNameList = new ArrayList();
            //add a blank entry here for this attribute not set
            hostNameList.add(new LabelValueBean("(none)", " "));
            
            while(hostItr.hasNext()) {
                
                ObjectName hostObjName = ((ObjectInstance)hostItr.next()).getObjectName();
                String hostName = (String) mBServer.getAttribute(hostObjName,
                NAME_PROP_NAME);
                // add this to the list that will be displayed in
                // the pulldown menu..
                hostNameList.add(new LabelValueBean(hostName, hostName));
            }
            
            
        }catch(Throwable t){
            t.printStackTrace(System.out);
            //forward to error page
        }
        
        serviceFm.setServiceName(serviceName);
        serviceFm.setDefaultHost(defaultHost);
        serviceFm.setDebugLvl(debug.toString());
        serviceFm.setEngineName(engineName);
        serviceFm.setDebugLvlVals(debugLvlList);
        serviceFm.setHostNameVals(hostNameList);
        
        // Forward back to the test page
        return (mapping.findForward("Service"));
        
    }
}
