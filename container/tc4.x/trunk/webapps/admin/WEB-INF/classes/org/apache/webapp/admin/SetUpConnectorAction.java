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
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR connectorS; LOSS OF
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
 * Test <code>Action</code> that handles events from the tree control when
 * a connector is chosen.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class SetUpConnectorAction extends Action {
    
    private static MBeanServer mBServer = null;
    
    public final static String SCHEME_PROP_NAME = "scheme";
    
    public final static String ACCEPT_COUNT_PROP_NAME = "acceptCount";
    public final static String CONN_TO_PROP_NAME = "connectionTimeout";
    public final static String DEBUG_PROP_NAME = "debug";
    public final static String BUFF_SIZE_PROP_NAME = "bufferSize";
    public final static String ENABLE_LOOKUPS_PROP_NAME = "enableLookups";
    public final static String ADDRESS_PROP_NAME = "address";
    
    public final static String PORT_PROP_NAME = "port";
    public final static String REDIRECT_PORT_PROP_NAME = "redirectPort";
    
    public final static String PROC_MIN_PROP_NAME = "minProcessors";
    public final static String PROC_MAX_PROP_NAME = "maxProcessors";
    
    public final static String PROXY_NAME_PROP_NAME = "proxyName";
    public final static String PROXY_PORT_PROP_NAME = "proxyPort";
    
    private ArrayList debugLvlList = null;
    private ArrayList booleanList = null;
    
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
            getServlet().log(" Creating new ConnectorForm bean under key "
            + mapping.getAttribute());
            form = new ConnectorForm();
            
            if ("request".equals(mapping.getScope()))
                request.setAttribute(mapping.getAttribute(), form);
            else
                session.setAttribute(mapping.getAttribute(), form);
            
        }
        
        String selectedName = request.getParameter("select");
        
        ConnectorForm connectorFm = (ConnectorForm) form;
        
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
        
        /* Boolean (true.false) list for enableLookups */
        if(booleanList == null) {
            booleanList = new ArrayList();
            booleanList.add(new LabelValueBean("True", "true"));
            booleanList.add(new LabelValueBean("False", "false"));
        }
        
        String connectorName = null;
        String scheme = null;
        Integer debug = null;
        String acceptCountText = null;
        Integer connTimeOut = null;
        Integer bufferSize = null;
        String address = null;
        Boolean enableLookups = null;
        
        String portText = null;
        String redirectPortText = null;
        
        Integer maxProcessors = null;
        Integer minProcessors = null;
        
        String proxyName = null;
        String proxyPortText = null;
        
        try{
            
            if(mBServer == null) {
                ApplicationServlet servlet = (ApplicationServlet)getServlet();
                mBServer = servlet.getServer();
            }
            
            Iterator connectorItr =
            mBServer.queryMBeans(new
            ObjectName(selectedName), null).iterator();
            
            ObjectInstance objInstance = (ObjectInstance)connectorItr.next();
            ObjectName connectorObjName = (objInstance).getObjectName();
            
            /*
            ModelMBeanInfo info = (ModelMBeanInfo) mBServer.getMBeanInfo(connectorObjName);
            MBeanAttributeInfo attrs[] = info.getAttributes();
            for (int i = 0; i < attrs.length; i++)
                System.out.println("  AttributeInfo=" + attrs[i]);
            
            MBeanOperationInfo opers[] = info.getOperations();
            for (int i = 0; i < opers.length; i++)
                System.out.println("  Operation=" + opers[i]);
            */
            
            // Extracting the attribute values for the Connector from the MBean
            scheme = (String) mBServer.getAttribute(connectorObjName,
            SCHEME_PROP_NAME);
            
            Integer acceptCount = (Integer) mBServer.getAttribute(connectorObjName,
            ACCEPT_COUNT_PROP_NAME);
            acceptCountText = acceptCount.toString();
            
            connTimeOut = (Integer) mBServer.getAttribute(connectorObjName,
            CONN_TO_PROP_NAME);

            debug = (Integer) mBServer.getAttribute(connectorObjName,
            DEBUG_PROP_NAME);
            
            bufferSize = (Integer) mBServer.getAttribute(connectorObjName,
            BUFF_SIZE_PROP_NAME);
            
            address = (String) mBServer.getAttribute(connectorObjName,
            ADDRESS_PROP_NAME);
            
            enableLookups = (Boolean) mBServer.getAttribute(connectorObjName,
            ENABLE_LOOKUPS_PROP_NAME);
            
            Integer port = (Integer) mBServer.getAttribute(connectorObjName,
            PORT_PROP_NAME);
            if (port != null) portText = port.toString();
            
            Integer redirectPort = (Integer) mBServer.getAttribute(connectorObjName,
            REDIRECT_PORT_PROP_NAME);
            redirectPortText = redirectPort.toString();
  
            minProcessors = (Integer) mBServer.getAttribute(connectorObjName,
            PROC_MIN_PROP_NAME);
            
            maxProcessors = (Integer) mBServer.getAttribute(connectorObjName,
            PROC_MAX_PROP_NAME);
            
            proxyName = (String) mBServer.getAttribute(connectorObjName,
            PROXY_NAME_PROP_NAME);
            
            Integer proxyPort = (Integer) mBServer.getAttribute(connectorObjName,
            PROXY_PORT_PROP_NAME);
            proxyPortText = proxyPort.toString();
            
        } catch(Throwable t){
            t.printStackTrace(System.out);
            //forward to error page
        }
        
        //setting values obtained from the mBean to be displayed in the form.
        connectorFm.setScheme(scheme);
        connectorFm.setAcceptCountText(acceptCountText);
        connectorFm.setConnTimeOutText(connTimeOut.toString());
        
        connectorFm.setDebugLvl(debug.toString());
        connectorFm.setDebugLvlVals(debugLvlList);
        
        connectorFm.setEnableLookups(enableLookups.toString());
        connectorFm.setBooleanVals(booleanList);
        
        connectorFm.setBufferSizeText(bufferSize.toString());
        connectorFm.setAddress(address);
        
        connectorFm.setMinProcessorsText(minProcessors.toString());
        connectorFm.setMaxProcessorsText(maxProcessors.toString());
        
        connectorFm.setPortText(portText);
        connectorFm.setRedirectPortText(redirectPortText);
        
        connectorFm.setProxyName(proxyName);
        connectorFm.setProxyPortText(proxyPortText);
        
        connectorFm.setConnectorName(selectedName);
        
        // Forward back to the test page
        return (mapping.findForward("Connector"));
    }
}

