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
import java.net.URLEncoder;
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
import javax.management.MBeanInfo;
 
/**
 * <p> Implementation of TreeBuilder interface for Tomcat Tree Controller 
 *     to build plugin components into the tree
 *
 * @author Jazmin Jonson
 * @version 
 */



public class TomcatTreeBuilder implements TreeBuilder{

    // This SERVER_LABEL needs to be localized
    private final static String SERVER_LABEL = "Tomcat Server";

    private final static String SERVER_TYPE = "Catalina:type=Server";
    private final static String SERVICE_TYPE = "Catalina:type=Service";
    private final static String ENGINE_TYPE = "Engine";
    private final static String CONNECTOR_TYPE = "Catalina:type=Connector";
    private final static String WILDCARD = ",*";

    private static MBeanServer mBServer = null;

    public void buildTree(TreeControl treeControl,
                          ApplicationServlet servlet,
                          HttpServletRequest request) {

        try {
            mBServer = servlet.getServer();
            TreeControlNode root = treeControl.getRoot();
            TreeControlNode server = getServer();
            root.addChild(server);
            getServices(server);
        }catch(Throwable t){
            t.printStackTrace(System.out);
        } 
    }

    public TreeControlNode getServer() 
        throws JMException, ServletException {

        Iterator serverItr = 
            mBServer.queryMBeans(new ObjectName(SERVER_TYPE + WILDCARD),
                                 null).iterator();
	String serverObjName =
            (((ObjectInstance)serverItr.next()).getObjectName()).toString();

        // HACK to take into account special characters like = and &
        // in the node name, could remove this code if encode URL
        // and later request.getParameter() could deal with = and &
        // character in parameter values. Must decode name in users action.
        // TreeControlTest.java in this case
 
	String encodedServerName =  URLEncoder.encode(serverObjName);
        TreeControlNode serverNode =
            new TreeControlNode(serverObjName,
                                "folder_16_pad.gif", SERVER_LABEL,
                                "treeControlTest.do?select=" + encodedServerName,
                                null, true);

        return serverNode;

    }


    public void getServices(TreeControlNode serverNode) 
        throws JMException, ServletException {

        Iterator serviceItr = 
            mBServer.queryMBeans(new ObjectName(SERVICE_TYPE + WILDCARD) , 
                                 null).iterator();

        String encodedServiceName;

        while(serviceItr.hasNext()){
            ObjectInstance service = (ObjectInstance)serviceItr.next();

            String serviceName = 
                (String)mBServer.getAttribute(service.getObjectName(),"name");

            // HACK to take into account special characters like = and &
            // in the node name, could remove this code if encode URL
            // and later request.getParameter() could deal with = and &
            // character in parameter values. Must decode name in users action.
            // TreeControlTest.java in this case
            encodedServiceName =  URLEncoder.encode(service.getObjectName().toString());

            TreeControlNode serviceNode = 
                new TreeControlNode(service.getObjectName().toString(),
                                    "folder_16_pad.gif", 
                                    "Service(" + serviceName + ")",
                                    "treeControlTest.do?select=" + encodedServiceName,
                                    null, true);

            serverNode.addChild(serviceNode);
	    
            getConnectors(serviceNode, serviceName);
        }
    }

    public void getConnectors(TreeControlNode serviceNode, 
                              String serviceName)

        throws JMException{


        Iterator ConnectorItr = 
            (mBServer.queryMBeans(new ObjectName(CONNECTOR_TYPE + WILDCARD +
                                                 ",service=" + serviceName),
                                  null)).iterator();

        TreeControlNode connectorNode = null;
        String encodedConnectorName;

        while(ConnectorItr.hasNext()){ 

            ObjectInstance connectorObj = (ObjectInstance)ConnectorItr.next();

            String connectorName = 
                (String)mBServer.getAttribute(connectorObj.getObjectName(),
                                              "scheme");

            // HACK to take into account special characters like = and &
            // in the node name, could remove this code if encode URL
            // and later request.getParameter() could deal with = and &
            // character in parameter values. Must decode name in users action.
            // TreeControlTest.java in this case
            encodedConnectorName =  URLEncoder.encode(connectorObj.getObjectName().toString());

            connectorNode = 
                new TreeControlNode(connectorObj.getObjectName().toString(),
                                    "folder_16_pad.gif", 
                                    "Connector(" + connectorName + ")",
                                    "treeControlTest.do?select=" + 
                                    encodedConnectorName,
                                    null, true);

            serviceNode.addChild(connectorNode);
        }
	
    }	

}			  
