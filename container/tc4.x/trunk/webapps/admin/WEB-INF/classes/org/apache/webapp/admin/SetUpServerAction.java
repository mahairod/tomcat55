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
 * @author Jazmin Jonson
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class SetUpServerAction extends Action {
    
    private static MBeanServer mBServer = null;
    private MessageResources resources = null;

    public final static String PORT_PROP_NAME = "port";
    public final static String SHUTDOWN_PROP_NAME = "shutdown";
    public final static String DEBUG_PROP_NAME = "debug";
    
    private ArrayList debugLvlList = null;
    
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
        Locale locale = (Locale) session.getAttribute(Action.LOCALE_KEY);
        
        if (resources == null) {
            resources = getServlet().getResources();
        }        
        if (form == null) {
            getServlet().log(" Creating new ServerForm bean under key "
            + mapping.getAttribute());
            form = new ServerForm();
            
            if ("request".equals(mapping.getScope()))
                request.setAttribute(mapping.getAttribute(), form);
            else
                request.getSession().setAttribute(mapping.getAttribute(), form);
            
        }
        
        // label of the node that was clicked on.
        String nodeLabel = request.getParameter("nodeLabel");
        
        ServerForm serverFm = (ServerForm) form;
        
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
        
        Integer portNumb = null;
        Integer debug = null;
        String shutdown = null;
        
        // Acquire a reference to the MBeanServer containing our MBeans
        try {
            mBServer = ((ApplicationServlet) getServlet()).getServer();
        } catch (Throwable t) {
            throw new ServletException
            ("Cannot acquire MBeanServer reference", t);
        }
        
        ObjectName serverObjName = null;
        try{
            Iterator serverItr =
            mBServer.queryMBeans(new ObjectName(TomcatTreeBuilder.SERVER_TYPE +
            TomcatTreeBuilder.WILDCARD),
            null).iterator();
            
            serverObjName =
            ((ObjectInstance)serverItr.next()).getObjectName();            
        } catch (Throwable t) {
            throw new ServletException
            ("Cannot acquire Server MBean reference ", t);
        }
        
        String attribute = null;
        try {
            portNumb = (Integer)mBServer.getAttribute(serverObjName,
            attribute=PORT_PROP_NAME);
            
            debug = (Integer)mBServer.getAttribute(serverObjName,
            attribute=DEBUG_PROP_NAME);
            
            shutdown = (String)mBServer.getAttribute(serverObjName,
            attribute=SHUTDOWN_PROP_NAME);
            
        } catch(Exception e){
            getServlet().log
            (resources.getMessage(locale, "users.error.attribute.get",
            attribute), e);
            response.sendError
            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            resources.getMessage(locale, "users.error.attribute.get",
            attribute));
            return (null);
        }
        
        //setting values obtained from the mBean to be displayed in the form.
        serverFm.setNodeLabel(nodeLabel);        
        serverFm.setPortNumberText(portNumb.toString());
        serverFm.setDebugLvl(debug.toString());
        serverFm.setShutdownText(shutdown);
        serverFm.setDebugLvlVals(debugLvlList);
        
        //forward to the server jsp.
        return (mapping.findForward("Server"));        
    }
        
}
