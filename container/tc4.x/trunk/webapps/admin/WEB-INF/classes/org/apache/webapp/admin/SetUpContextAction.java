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
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR contextS; LOSS OF
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
 * a context is chosen.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class SetUpContextAction extends Action {
    
    private static MBeanServer mBServer = null;
    
    // ---- Context Properties ----
    public final static String COOKIES_PROP_NAME = "cookies";
    public final static String CROSS_CONTEXT_PROP_NAME = "crossContext";
    public final static String DEBUG_PROP_NAME = "debug";
    public final static String DOC_BASE_PROP_NAME = "docBase";
    public final static String OVERRIDE_PROP_NAME = "override";
    public final static String PATH_PROP_NAME = "path";
    public final static String RELOADABLE_PROP_NAME = "reloadable";
    public final static String USENAMING_PROP_NAME = "useNaming";
    public final static String WORKDIR_PROP_NAME = "workDir";
    
    // -- Loader properties --
    public final static String CHECKINTERVAL_PROP_NAME = "checkInterval";
    
    // -- Session manager properties --
    public final static String SESSIONID_INIT_PROP_NAME = "entropy";
    public final static String MAXACTIVE_SESSIONS_PROP_NAME = "maxActiveSessions";
    
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
            getServlet().log(" Creating new ContextForm bean under key "
            + mapping.getAttribute());
            form = new ContextForm();
            
            if ("request".equals(mapping.getScope()))
                request.setAttribute(mapping.getAttribute(), form);
            else
                session.setAttribute(mapping.getAttribute(), form);
            
        }
        
        String selectedName = request.getParameter("select");
        // label of the node that was clicked on.
        String nodeLabel = request.getParameter("nodeLabel");
        
        ContextForm contextFm = (ContextForm) form;
        
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
        
        /* Boolean (true.false) list for cookies */
        if(booleanList == null) {
            booleanList = new ArrayList();
            booleanList.add(new LabelValueBean("True", "true"));
            booleanList.add(new LabelValueBean("False", "false"));
        }
        
        String contextName = null;
        String loaderName = null;
        String managerName = null;
        
        // context properties
        Boolean cookies = null;
        Boolean crossContext = null;
        Integer debug = null;
        String docBase = null;
        Boolean override = null;
        String path = null;
        Boolean reloadable = null;
        Boolean useNaming = null;
        String workDir = null;
        
        // loader properties
        Integer ldrCheckInterval = null;
        Integer ldrDebug = null;
        Boolean ldrReloadable = null;
        
        // session properties
        Integer mgrCheckInterval = null;
        Integer mgrDebug = null;
        String mgrSessionIDInit = null;
        Integer mgrMaxSessions = null; 
       
        try{
            
            if(mBServer == null) {
                ApplicationServlet servlet = (ApplicationServlet)getServlet();
                mBServer = servlet.getServer();
            }
            
            Iterator contextItr =
            mBServer.queryMBeans(new
            ObjectName(selectedName), null).iterator();
            
            ObjectInstance objInstance = (ObjectInstance)contextItr.next();
            ObjectName contextObjName = (objInstance).getObjectName();
            
            // Extracting the attribute values for the Context from the MBean
            
            cookies = (Boolean) mBServer.getAttribute(contextObjName,
            COOKIES_PROP_NAME);
            
            crossContext = (Boolean) mBServer.getAttribute(contextObjName,
            CROSS_CONTEXT_PROP_NAME);
            
            debug = (Integer) mBServer.getAttribute(contextObjName,
            DEBUG_PROP_NAME);
            
            docBase = (String) mBServer.getAttribute(contextObjName,
            DOC_BASE_PROP_NAME);
            
            override = (Boolean) mBServer.getAttribute(contextObjName,
            OVERRIDE_PROP_NAME);
            
            path = (String) mBServer.getAttribute(contextObjName,
            PATH_PROP_NAME);
            
            reloadable = (Boolean) mBServer.getAttribute(contextObjName,
            RELOADABLE_PROP_NAME);
            
            useNaming = (Boolean) mBServer.getAttribute(contextObjName,
            USENAMING_PROP_NAME);
            
            workDir = (String) mBServer.getAttribute(contextObjName,
            WORKDIR_PROP_NAME);
            
            // Loader properties
            // Get the corresponding Loader mBean
            int i = selectedName.indexOf(",");
            if (i != -1) 
                loaderName = TomcatTreeBuilder.LOADER_TYPE +
                selectedName.substring(i, selectedName.length());

            Iterator loaderItr =
            mBServer.queryMBeans(new ObjectName(loaderName), null).iterator();
            
            objInstance = (ObjectInstance)loaderItr.next();
            ObjectName loaderObjName = (objInstance).getObjectName();
           
            ldrCheckInterval = (Integer) mBServer.getAttribute(loaderObjName,
            CHECKINTERVAL_PROP_NAME);
                    
            ldrDebug = (Integer) mBServer.getAttribute(loaderObjName,
            DEBUG_PROP_NAME);
            
            ldrReloadable = (Boolean) mBServer.getAttribute(loaderObjName,
            RELOADABLE_PROP_NAME);                
            
            // Session manager properties         
            // Get the corresponding Session Manager mBean
            i = selectedName.indexOf(",");
            if (i != -1) 
                managerName = TomcatTreeBuilder.MANAGER_TYPE + 
                              selectedName.substring(i, selectedName.length());
                       
            Iterator managerItr =
            mBServer.queryMBeans(new ObjectName(managerName), null).iterator();
            
            objInstance = (ObjectInstance)managerItr.next();
            ObjectName managerObjName = (objInstance).getObjectName();
           
            mgrCheckInterval = (Integer) mBServer.getAttribute(managerObjName,
            CHECKINTERVAL_PROP_NAME);                
                  
            mgrDebug = (Integer) mBServer.getAttribute(managerObjName,
            DEBUG_PROP_NAME);         
            
            mgrSessionIDInit = (String) mBServer.getAttribute(managerObjName,
            SESSIONID_INIT_PROP_NAME);   
             
            mgrMaxSessions = (Integer) mBServer.getAttribute(managerObjName,
            MAXACTIVE_SESSIONS_PROP_NAME);         
            
        } catch(Throwable t){
            t.printStackTrace(System.out);
            //forward to error page
        }
        
        //setting values obtained from the mBean to be displayed in the form.
        
        contextFm.setNodeLabel(nodeLabel);
        contextFm.setContextName(selectedName);
        contextFm.setLoaderName(loaderName);
        contextFm.setManagerName(managerName);
        
        contextFm.setDebugLvlVals(debugLvlList);
        contextFm.setBooleanVals(booleanList);
        
        // -- Context Properties --
        contextFm.setCookies(cookies.toString());
        contextFm.setCrossContext(crossContext.toString());
        contextFm.setDebugLvl(debug.toString());
        contextFm.setDocBase(docBase);
        contextFm.setOverride(override.toString());
        contextFm.setPath(path);
        contextFm.setReloadable(reloadable.toString());
        contextFm.setUseNaming(useNaming.toString());
        contextFm.setWorkDir(workDir);
        
        // -- Loader properties --
        contextFm.setLdrCheckInterval(ldrCheckInterval.toString());
        contextFm.setLdrDebugLvl(ldrDebug.toString());
        contextFm.setLdrReloadable(ldrReloadable.toString());
        
        // Session manager properties --
        contextFm.setMgrCheckInterval(mgrCheckInterval.toString());
        contextFm.setMgrDebugLvl(mgrDebug.toString());
        contextFm.setMgrSessionIDInit(mgrSessionIDInit.toString());
        contextFm.setMgrMaxSessions(mgrMaxSessions.toString());
       
        // Forward back to the test page
        return (mapping.findForward("Context"));
    }
}


