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
 * a JNDI realm is chosen.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class SetUpJNDIRealmAction extends Action {
    
    private static MBeanServer mBServer = null;
    
    public final static String CONN_NAME_PROP_NAME = "connectionName";
    public final static String CONN_PWD_PROP_NAME = "connectionPassword";
    public final static String CONN_URL_PROP_NAME = "connectionURL";
    
    public final static String DEBUG_PROP_NAME = "debug";
    public final static String DIGEST_PROP_NAME = "digest";
    public final static String CONN_FACT_PROP_NAME = "contextFactory";
    public final static String ROLE_BASE_PROP_NAME = "roleBase";
    public final static String ROLE_NAME_PROP_NAME = "roleName";
    public final static String ROLE_SEARCH_PROP_NAME = "roleSearch";
    public final static String ROLE_SUBTREE_PROP_NAME = "roleSubtree";
    public final static String USER_PWD_PROP_NAME = "userPassword";
    public final static String USER_PATTERN_PROP_NAME = "userPattern";
    
    private String selectedName = null;
    private String realmType = null;
    private String nodeLabel = null;
    private ArrayList debugLvlList = null;
    private ArrayList searchList = null;
    
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
            getServlet().log(" Creating new JNDIRealmForm bean under key "
            + mapping.getAttribute());
            
            form = new JNDIRealmForm();
            
            if ("request".equals(mapping.getScope()))
                request.setAttribute(mapping.getAttribute(), form);
            else
                session.setAttribute(mapping.getAttribute(), form);
        }
        
        selectedName = request.getParameter("select");
        realmType = request.getParameter("type");
        nodeLabel = request.getParameter("nodeLabel");
        
        JNDIRealmForm realmFm = (JNDIRealmForm) form;
        
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
        
        if(searchList == null) {
            searchList = new ArrayList();
                        searchList.add(new LabelValueBean("True", "true"));
            searchList.add(new LabelValueBean("False", "false"));
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
            
            realmFm.setContextFactory(
            ((String) mBServer.getAttribute(realmObjName, CONN_FACT_PROP_NAME)));
            
            realmFm.setConnectionPassword(
            ((String) mBServer.getAttribute(realmObjName, CONN_PWD_PROP_NAME)));
            
            realmFm.setConnectionName(
            ((String) mBServer.getAttribute(realmObjName, CONN_NAME_PROP_NAME)));
            
            realmFm.setConnectionURL(
            ((String) mBServer.getAttribute(realmObjName, CONN_URL_PROP_NAME)));
            
            realmFm.setDigest(
            ((String) mBServer.getAttribute(realmObjName, DIGEST_PROP_NAME)));
            
            realmFm.setRoleBase(
            ((String) mBServer.getAttribute(realmObjName, ROLE_BASE_PROP_NAME)));
            
            realmFm.setRoleAttribute(
            ((String) mBServer.getAttribute(realmObjName, ROLE_NAME_PROP_NAME)));
            
            realmFm.setRolePattern(
            ((String) mBServer.getAttribute(realmObjName, ROLE_SEARCH_PROP_NAME)));
            
            realmFm.setSearch(
            ((String) mBServer.getAttribute(realmObjName, ROLE_SUBTREE_PROP_NAME)));
            
            realmFm.setUserPassword(
            ((String) mBServer.getAttribute(realmObjName, USER_PWD_PROP_NAME)));
            
            realmFm.setUserPattern(
            ((String) mBServer.getAttribute(realmObjName, USER_PATTERN_PROP_NAME)));
            
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
        realmFm.setSearchVals(searchList);
        
        // Forward back to the appropriate Realm page
        return (mapping.findForward("JNDIRealm"));
        
    }
}

