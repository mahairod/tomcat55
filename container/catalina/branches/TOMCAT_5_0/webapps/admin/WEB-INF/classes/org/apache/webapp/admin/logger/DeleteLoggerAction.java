/*
 * Copyright 2001-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.webapp.admin.logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Set;
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
import org.apache.struts.util.MessageResources;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TomcatTreeBuilder;

/**
 * The <code>Action</code> that sets up <em>Delete Loggers</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class DeleteLoggerAction extends Action {
    
    
    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mBServer = null;
    
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
    public ActionForward execute(ActionMapping mapping,
    ActionForm form,
    HttpServletRequest request,
    HttpServletResponse response)
    throws IOException, ServletException {
        
        
        // Acquire the resources that we need
        MessageResources resources = getResources(request);
        HttpSession session = request.getSession();
        Locale locale = getLocale(request);
        
        // Acquire a reference to the MBeanServer containing our MBeans
        try {
            mBServer = ((ApplicationServlet) getServlet()).getServer();
        } catch (Throwable t) {
            throw new ServletException
            ("Cannot acquire MBeanServer reference", t);
        }
        
        String pattern = null;
        // Set up a form bean containing the currently selected
        // objects to be deleted
        LoggersForm loggersForm = new LoggersForm();
        String select = request.getParameter("select");
        if (select != null) {
            String loggers[] = new String[1];
            loggers[0] = select;
            loggersForm.setLoggers(loggers);
            pattern = select;
        }
        request.setAttribute("loggersForm", loggersForm);
        
        // Accumulate a list of all available loggers
        ArrayList list = new ArrayList();
        String parent = request.getParameter("parent");
        
        if (parent != null) {
            try {
                pattern = getObjectName(parent, TomcatTreeBuilder.LOGGER_TYPE);
            } catch (Exception e) {
                getServlet().log
                (resources.getMessage(locale, "users.error.select"));
                response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                resources.getMessage(locale, "users.error.select"));
                return (null);
            }
        }
        
        try {
            Iterator items =
            mBServer.queryNames(new ObjectName(pattern), null).iterator();
            while (items.hasNext()) {
                list.add(items.next().toString());
            }
        } catch (Exception e) {
            getServlet().log
            (resources.getMessage(locale, "users.error.select"));
            response.sendError
            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            resources.getMessage(locale, "users.error.select"));
            return (null);
        }
        
        Collections.sort(list);
        request.setAttribute("loggersList", list);
        
        // Forward to the list display page
        return (mapping.findForward("Loggers"));
        
    }
    
    public static String getObjectName(String parent, String MBeanType)
    throws Exception{
        
        // Form the pattern that gets the logger for this particular
        // service, host or context.
        ObjectName poname = new ObjectName(parent);
        String domain = poname.getDomain();
        StringBuffer sb = new StringBuffer(domain+MBeanType);
        String type = poname.getKeyProperty("type");
        String j2eeType = poname.getKeyProperty("j2eeType");
        String path = "";
        String host = "";
        String name = poname.getKeyProperty("name");
        if ((name != null) && (name.length() > 0)) {
            name = name.substring(2);
            int i = name.indexOf("/");
            host = name.substring(0,i);
            path = name.substring(i); 
        }
        if ("WebModule".equalsIgnoreCase(j2eeType)) { // container is context            
            sb.append(",path="+path);
            sb.append(",host="+host);
        }
        if ("Host".equalsIgnoreCase(type)) {    // container is host
            sb.append(",host=");
            sb.append(poname.getKeyProperty("host"));
        }
        if ("Service".equalsIgnoreCase(type)) {  // container is service
        }
        return sb.toString();  
    }
}
