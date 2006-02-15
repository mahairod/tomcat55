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

package org.apache.webapp.admin.connector;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.LabelValueBean;
import org.apache.webapp.admin.Lists;

/**
 * The <code>Action</code> that sets up <em>Add Connector</em> transactions.
 *
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */

public class AddConnectorAction extends Action {
    
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
        
        // Acquire the resources that we need
        HttpSession session = request.getSession();
        if (resources == null) {
            resources = getResources(request);
        }
        
        // the service Name is needed to retrieve the engine mBean to
        // which the new connector mBean will be added.
        String serviceName = request.getParameter("serviceName");
        
        // Fill in the form values for display and editing
        ConnectorForm connectorFm = new ConnectorForm();
        session.setAttribute("connectorForm", connectorFm);
        connectorFm.setAdminAction("Create");
        connectorFm.setObjectName("");
        connectorFm.setConnectorName("");
        String type = request.getParameter("type");
        if (type == null)
            type = "HTTP";    // default type is HTTP
        connectorFm.setConnectorType(type);
        connectorFm.setDebugLvl("0");
        connectorFm.setServiceName(serviceName);
        if ("HTTPS".equalsIgnoreCase(type)) {
            connectorFm.setScheme("https");
        } else {
            connectorFm.setScheme("http");       
        }
        connectorFm.setAcceptCountText("10");
        connectorFm.setConnTimeOutText("60000");
        connectorFm.setBufferSizeText("2048");
        connectorFm.setEnableLookups("true");
        connectorFm.setAddress("");
        connectorFm.setPortText("");
        connectorFm.setRedirectPortText("-1");
        connectorFm.setMinProcessorsText("5");
        connectorFm.setMaxProcessorsText("20");

        //supported only by HTTPS
        connectorFm.setClientAuthentication("false");
        connectorFm.setKeyStoreFileName("");
        connectorFm.setKeyStorePassword("");
                       
        // supported only by Coyote connectors
        connectorFm.setProxyName("");
        connectorFm.setProxyPortText("0");        
        
        connectorFm.setDebugLvlVals(Lists.getDebugLevels());
        connectorFm.setBooleanVals(Lists.getBooleanValues());                
        connectorFm.setClientAuthVals(Lists.getClientAuthValues());
        
        String schemeTypes[]= new String[3];
        schemeTypes[0] = "HTTP";
        schemeTypes[1] = "HTTPS";                
        schemeTypes[2] = "AJP";
        
        ArrayList types = new ArrayList();    
        // the first element in the select list should be the type selected
        types.add(new LabelValueBean(type,
                "AddConnector.do?serviceName=" + URLEncoder.encode(serviceName) 
                + "&type=" + type));        
         for (int i=0; i< schemeTypes.length; i++) {
            if (!type.equalsIgnoreCase(schemeTypes[i])) {
                types.add(new LabelValueBean(schemeTypes[i],
                "AddConnector.do?serviceName=" + URLEncoder.encode(serviceName)
                + "&type=" + schemeTypes[i]));        
            }
        }
        connectorFm.setConnectorTypeVals(types);
        
        // Forward to the connector display page
        return (mapping.findForward("Connector"));
        
    }        
}
