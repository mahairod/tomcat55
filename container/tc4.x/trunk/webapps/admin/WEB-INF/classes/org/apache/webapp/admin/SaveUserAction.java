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
import java.util.ArrayList;
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
 * <p>Implementation of <strong>Action</strong> that saves a new or
 * updated User back to the underlying database.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class SaveUserAction extends Action {
    

    // ----------------------------------------------------- Instance Variables


    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;


    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mserver = null;
    

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

        // Has this transaction been cancelled?
        if (isCancelled(request)) {
            ; // FIXME - confirmation message?
            return (mapping.findForward("List Users Setup"));
        }

        // Perform any extra validation that is required
        ; // FIXME - (set and) check transaction token
        UserForm userForm = (UserForm) form;
        String objectName = userForm.getObjectName();

        // Perform an "Add User" transaction
        if (objectName == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Add User not yet supported");
            return (null);
        }

        // Perform an "Update User" transaction
        else {

            try {

                ObjectName oname = new ObjectName(objectName);

                // Update the specified user
                ; // NOTE - cannot change username on an update
                mserver.setAttribute
                    (oname,
                     new Attribute("password", userForm.getPassword()));
                mserver.setAttribute
                    (oname,
                     new Attribute("fullName", userForm.getFullName()));
                /* FIXME - does not work yet
                String values = userForm.getRoles();
                if (values == null) {
                    values = "";
                }
                ArrayList results = new ArrayList();
                while (values.length() > 0) {
                    int comma = values.indexOf(",");
                    if (comma < 0) {
                        break;
                    }
                    results.add(values.substring(0, comma).trim());
                    values = values.substring(comma + 1);
                }
                values = values.trim();
                if (values.length() > 0) {
                    results.add(values);
                }
                String roles[] = new String[results.size()];
                roles = (String[]) results.toArray(roles);
                mserver.setAttribute
                    (oname,
                     new Attribute("roles", roles));
                */

                // Tell the database to save the updated information
                ObjectName dname =
                    new ObjectName(oname.getDomain() +
                                   ":type=UserDatabase,database=" +
                                   oname.getKeyProperty("database"));
                mserver.invoke(dname, "save",
                               new Object[0], new String[0]);

            } catch (Throwable t) {
                getServlet().log
                    (resources.getMessage(locale, "error.set.attributes"), t);
                response.sendError
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     resources.getMessage(locale, "error.set.attributes"));
                return (null);
            }

            return (mapping.findForward("Users List Setup"));

        }
        
    }

    
}
