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

package org.apache.webapp.admin.realm;

import java.util.Iterator;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.QueryExp;
import javax.management.Query;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.JMException;
import org.apache.struts.util.MessageResources;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.LabelValueBean;

/**
 * Implementation of <strong>Action</strong> that validates
 * actions on a Memory Realm.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public final class MemoryRealmAction extends Action {
    
    /**
     * The MBeanServer we will be interacting with.
     */
    private static MBeanServer mBServer = null;
    
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
        
        if (resources == null) {
            resources = getServlet().getResources();
        }
        HttpSession session = request.getSession();
        Locale locale = (Locale) session.getAttribute(Action.LOCALE_KEY);
        
        try{
            
            // front end validation and checking.
            // ===================================================
            // Validate the request parameters specified by the user
            ActionErrors errors = new ActionErrors();
            
            // Report any errors we have discovered back to the original form
            if (!errors.empty()) {
                saveErrors(request, errors);
                return (new ActionForward(mapping.getInput()));
            }
            
            if(mBServer == null) {
                ApplicationServlet servlet = (ApplicationServlet)getServlet();
                mBServer = servlet.getServer();
            }
            
            /**
             * Get the realm Name from the form.
             * This is used to lookup the MBeanServer and
             * retrieve this realm's MBean.
             */
            String realmName = request.getParameter("realmName");
            
            Iterator realmItr =
            mBServer.queryMBeans(new
            ObjectName(realmName), null).iterator();
            
            ObjectInstance objInstance = (ObjectInstance)realmItr.next();
            ObjectName realmObjName = (objInstance).getObjectName();
            
            /**
             * Extracting the values from the form and
             * updating the MBean with the new values.
             */
            String debugLvlText = request.getParameter("debugLvl");
            if(debugLvlText != null) {
                Integer debugLvl = new Integer(debugLvlText);
                mBServer.setAttribute(realmObjName,
                new Attribute(SetUpMemoryRealmAction.DEBUG_PROP_NAME,
                debugLvl));
            }
            
            String pathName = request.getParameter("pathName");
            if(pathName != null) {
                mBServer.setAttribute(realmObjName,
                new Attribute(SetUpMemoryRealmAction.PATH_NAME_PROP_NAME,
                pathName));
            }
            
        }catch(Throwable t){
            getServlet().log
            (resources.getMessage(locale, "error.set.attributes"), t);
            response.sendError
            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            resources.getMessage(locale, "error.set.attributes"));
            return (null);
        }
        
        if (servlet.getDebug() >= 1)
            servlet.log(" Forwarding to success page");
        // Forward back to the test page
        return (mapping.findForward("Save Successful"));
        
    }
    
}
