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

package org.apache.webapp.admin.realm;

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

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.LabelValueBean;

import org.apache.struts.util.MessageResources;

/**
 * Test <code>Action</code> that handles events from the tree control when
 * a realm is chosen.
 * A realm can be one of three types, namely, Memory, JNDI and JDBC realm.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class SetUpJDBCRealmAction extends Action {
    
    private static MBeanServer mBServer = null;
    
    public final static String CONN_NAME_PROP_NAME = "connectionName";
    public final static String CONN_PWD_PROP_NAME = "connectionPassword";
    public final static String CONN_URL_PROP_NAME = "connectionURL";
    
    public final static String DRIVER_PROP_NAME = "driverName";
    public final static String DEBUG_PROP_NAME = "debug";
    public final static String DIGEST_PROP_NAME = "digest";
    public final static String ROLE_COL_PROP_NAME = "roleNameCol";
    public final static String PWD_COL_PROP_NAME = "userCredCol";
    public final static String ROLE_TABLE_PROP_NAME = "userRoleTable";
    public final static String USER_TABLE_PROP_NAME = "userTable";
    
    private String selectedName = null;
    private String realmType = null;
    private String nodeLabel = null;
    private ArrayList debugLvlList = null;
    
    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;
    
    
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
        realmType = request.getParameter("type");
        Locale locale = (Locale) session.getAttribute(Action.LOCALE_KEY);
        
        if (resources == null) {
            resources = getServlet().getResources();
        }
        
        if (form == null) {
            getServlet().log(" Creating new JDBCRealmForm bean under key "
            + mapping.getAttribute());
            
            form = new JDBCRealmForm();
            
            if ("request".equals(mapping.getScope()))
                request.setAttribute(mapping.getAttribute(), form);
            else
                session.setAttribute(mapping.getAttribute(), form);
        }
        
        selectedName = request.getParameter("select");
        realmType = request.getParameter("type");
        nodeLabel = request.getParameter("nodeLabel");
        
        JDBCRealmForm realmFm = (JDBCRealmForm) form;
        
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
        
        try{
            
            if(mBServer == null) {
                ApplicationServlet servlet = (ApplicationServlet)getServlet();
                mBServer = servlet.getServer();
            }
            
            Iterator realmItr =
            mBServer.queryMBeans(new
            ObjectName(selectedName), null).iterator();
            
            ObjectInstance objInstance = (ObjectInstance)realmItr.next();
            ObjectName realmObjName = (objInstance).getObjectName();
            
            realmFm.setDebugLvl(
            ((Integer) mBServer.getAttribute(realmObjName, DEBUG_PROP_NAME)).toString());
            
            realmFm.setDriver(
            ((String) mBServer.getAttribute(realmObjName, DRIVER_PROP_NAME)));
            
            realmFm.setConnectionPassword(
            ((String) mBServer.getAttribute(realmObjName, CONN_PWD_PROP_NAME)));
            
            realmFm.setConnectionName(
            ((String) mBServer.getAttribute(realmObjName, CONN_NAME_PROP_NAME)));
            
            realmFm.setConnectionURL(
            ((String) mBServer.getAttribute(realmObjName, CONN_URL_PROP_NAME)));
            
            // FIXME --digest should be displayed as a pull down menu??
            // But no operation yet to get list of digests.
            realmFm.setDigest(
            ((String) mBServer.getAttribute(realmObjName, DIGEST_PROP_NAME)));
            
            realmFm.setPasswordCol(
            ((String) mBServer.getAttribute(realmObjName, PWD_COL_PROP_NAME)));
            
            realmFm.setRoleNameCol(
            ((String) mBServer.getAttribute(realmObjName, ROLE_COL_PROP_NAME)));
            
            realmFm.setRoleTable(
            ((String) mBServer.getAttribute(realmObjName, ROLE_TABLE_PROP_NAME)));
            
            realmFm.setUserTable(
            ((String) mBServer.getAttribute(realmObjName, USER_TABLE_PROP_NAME)));
            
            
        } catch(Throwable t){
            getServlet().log
            (resources.getMessage(locale, "error.get.attributes"), t);
            response.sendError
            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            resources.getMessage(locale, "error.get.attributes"));
            return (null);
        }
        
        realmFm.setRealmName(selectedName);
        realmFm.setNodeLabel(nodeLabel);
        
        realmFm.setRealmType(realmType);
        realmFm.setDebugLvlVals(debugLvlList);
        
        // Forward back to the appropriate Realm page
        return (mapping.findForward("JDBCRealm"));
        
    }
}

