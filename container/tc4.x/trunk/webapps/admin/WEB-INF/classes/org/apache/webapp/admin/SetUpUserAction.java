/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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


package org.apache.webapp.admin;


import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
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
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanInfo;
import org.apache.struts.util.MessageResources;


/**
 * <p>Implementation of <strong>Action</strong> that sets up and stashes
 * a <code>UserForm</code> bean in request scope.  The form bean will have
 * a null <code>objectName</code> property if this form represents a user
 * being added, or a non-null value for an existing user.</p>
 *
 * <p>The object name of the user to be edited (if any) is specified by
 * a request parameter named <code>objectName</code> included on the
 * request that selected this action.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class SetUpUserAction extends Action {
    

    // ----------------------------------------------------- Instance Variables


    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mserver = null;
    

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
        
        // Look up the components we will be using as needed
        if (mserver == null) {
            mserver = ((ApplicationServlet) getServlet()).getServer();
        }
        if (resources == null) {
            resources = getServlet().getResources();
        }
        HttpSession session = request.getSession();
        Locale locale = (Locale) session.getAttribute(Action.LOCALE_KEY);

        // Set up the form bean based on the creating or editing state
        String objectName = request.getParameter("objectName");
        UserForm userForm = new UserForm();
        if (objectName == null) {
            userForm.setNodeLabel
                (resources.getMessage(locale, "user.newUser"));
        } else {
            userForm.setNodeLabel
                (resources.getMessage(locale, "user.oldUser"));
            userForm.setObjectName(objectName);
            try {
                ObjectName oname = new ObjectName(objectName);
                userForm.setUsername
                    ((String) mserver.getAttribute(oname, "username"));
                userForm.setPassword
                    ((String) mserver.getAttribute(oname, "password"));
                userForm.setFullName
                    ((String) mserver.getAttribute(oname, "fullName"));
                String groups[] =
                    (String[]) mserver.getAttribute(oname, "groups");
                if (groups == null) {
                    groups = new String[0];
                }
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < groups.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(groups[i]);
                }
                if (groups.length > 0) {
                    userForm.setGroups(sb.toString());
                }
                String roles[] =
                    (String[]) mserver.getAttribute(oname, "roles");
                if (roles == null) {
                    roles = new String[0];
                }
                sb = new StringBuffer();
                for (int i = 0; i < roles.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(roles[i]);
                }
                if (roles.length > 0) {
                    userForm.setRoles(sb.toString());
                }
            } catch (Throwable t) {
                getServlet().log
                    (resources.getMessage(locale, "error.get.attributes"), t);
                response.sendError
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     resources.getMessage(locale, "error.get.attributes"));
                return (null);
            }
        }

        // Stash the form bean and forward to the display page
        request.setAttribute("userForm", userForm);
        return (mapping.findForward("User"));
        
    }
    
}
