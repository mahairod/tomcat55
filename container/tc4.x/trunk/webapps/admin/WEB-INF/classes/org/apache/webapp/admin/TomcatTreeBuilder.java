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
import java.util.StringTokenizer;
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
 * @author Manveen Kaur
 * @version $Revision$ $Date$
 */


public class TomcatTreeBuilder implements TreeBuilder{
    
    // This SERVER_LABEL needs to be localized
    private final static String SERVER_LABEL = "Tomcat Server";
    
    public final static String SERVER_TYPE = "Catalina:type=Server";
    public final static String SERVICE_TYPE = "Catalina:type=Service";
    public final static String ENGINE_TYPE = "Catalina:type=Engine";
    public final static String CONNECTOR_TYPE = "Catalina:type=Connector";
    public final static String HOST_TYPE = "Catalina:type=Host";
    public final static String CONTEXT_TYPE = "Catalina:type=Context";
    public final static String LOADER_TYPE = "Catalina:type=Loader";
    public final static String MANAGER_TYPE = "Catalina:type=Manager";
    public final static String LOGGER_TYPE = "Catalina:type=Logger";

    public final static String WILDCARD = ",*";
    
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
        // character in parameter values. Decoding name not needed
        // because Tomcat does this automatically
        
        String encodedServerName =  URLEncoder.encode(serverObjName);
        String encodedNodeLabel =  URLEncoder.encode(SERVER_LABEL);
        
        TreeControlNode serverNode =
        new TreeControlNode(serverObjName,
        "folder_16_pad.gif", SERVER_LABEL,
        "setUpServer.do?select=" + encodedServerName
        +"&nodeLabel=" + encodedNodeLabel,
        "content", true);
        
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
            // character in parameter values. Decoding name not needed
            // because Tomcat does this automatically
            
            encodedServiceName =  URLEncoder.encode(service.getObjectName().toString());
            
            String nodeLabel = "Service (" + serviceName + ")";
            String encodedNodeLabel =  URLEncoder.encode(nodeLabel);
            
            TreeControlNode serviceNode =
            new TreeControlNode(service.getObjectName().toString(),
            "folder_16_pad.gif",
            nodeLabel,
            "setUpService.do?select=" + encodedServiceName
            +"&nodeLabel=" + encodedNodeLabel,
            "content", true);
            
            serverNode.addChild(serviceNode);
            
            getConnectors(serviceNode, serviceName);
            getHosts(serviceNode, serviceName);
            getLoggers(serviceNode, serviceName, null, null, 0);
        }
    }
    
    public void getConnectors(TreeControlNode serviceNode, String serviceName)
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
            
            encodedConnectorName =  URLEncoder.encode(connectorObj.getObjectName().toString());
            
            String nodeLabel = "Connector (" + connectorName + ")";
            String encodedNodeLabel =  URLEncoder.encode(nodeLabel);
            
            connectorNode =
                new TreeControlNode(connectorObj.getObjectName().toString(),
                "folder_16_pad.gif",
                nodeLabel,
                "setUpConnector.do?select=" + encodedConnectorName
                + "&nodeLabel="+ encodedNodeLabel,
                "content", true);
                
            serviceNode.addChild(connectorNode);
        }        
    }
    
    public void getHosts(TreeControlNode serviceNode, String serviceName)
    throws JMException{
 
        Iterator HostItr =
        (mBServer.queryMBeans(new ObjectName(HOST_TYPE + WILDCARD +
        ",service=" + serviceName), null)).iterator();
        
        TreeControlNode hostNode = null;
        String encodedHostName;
        
        while(HostItr.hasNext()){
            
            ObjectInstance hostObj = (ObjectInstance)HostItr.next();
            
            String hostName =
            (String)mBServer.getAttribute(hostObj.getObjectName(),
            "name");
            
            encodedHostName =  URLEncoder.encode(hostObj.getObjectName().toString());
            
            String nodeLabel="Host (" + hostName + ")";
            String encodedNodeLabel =  URLEncoder.encode(nodeLabel);
            
            hostNode =
            new TreeControlNode(hostObj.getObjectName().toString(),
            "folder_16_pad.gif",
            nodeLabel,
            "setUpHost.do?select=" + encodedHostName
            +"&nodeLabel="+ encodedNodeLabel,
            "content", true);
            
            serviceNode.addChild(hostNode);
            
            getContexts(hostNode, hostName, serviceName);
            getLoggers(hostNode, serviceName, hostName, null, 1);
        }
        
    }
    
    public void getContexts(TreeControlNode hostNode, String hostName, String serviceName)
    throws JMException{
    
        Iterator contextItr =
        (mBServer.queryMBeans(new ObjectName(CONTEXT_TYPE + WILDCARD +
        ",host=" + hostName + ",service=" + serviceName), null)).iterator();
        
        TreeControlNode contextNode = null;
        String encodedContextName;
        
        while(contextItr.hasNext()){
            
            ObjectInstance contextObj = (ObjectInstance)contextItr.next();
            
            String contextName =
            (String)mBServer.getAttribute(contextObj.getObjectName(), "path");
            
            encodedContextName =  URLEncoder.encode(contextObj.getObjectName().toString());
            
            String nodeLabel="Context (" + contextName + ")";
            String encodedNodeLabel =  URLEncoder.encode(nodeLabel);
            
            contextNode =
            new TreeControlNode(contextObj.getObjectName().toString(),
            "folder_16_pad.gif",
            nodeLabel,
            "setUpContext.do?select=" + encodedContextName
            +"&nodeLabel="+ encodedNodeLabel,
            "content", true);
            
            hostNode.addChild(contextNode);
            //get all loggers for this context
            if (contextName.length() > 0)
                getLoggers(contextNode, serviceName, hostName, contextName, 2);            
        }        
    }
    
        
    /**
     * Add the required logger nodes to the specified node instance.
     *
     * @param node The <code>TreeControlNode</code> to which we should
     * add our logger nodes.
     * @param serviceName The service to which this logger belongs.
     * @param hostName The host to which this logger belongs.
     * @param contextName The context to which this logger belongs.
     * @param type (0,1,2)  Get all loggers for a particular service(0),
     * host(1), context (2).
     */
    public void getLoggers(TreeControlNode node, String serviceName,
    String hostName, String contextName, int type)
    throws JMException{
        
        Iterator loggerItr = null;
        
        if (type == 0) {
            loggerItr =
            (mBServer.queryMBeans(new ObjectName(LOGGER_TYPE +
            ",service=" + serviceName), null)).iterator();
        }  else if (type == 1) {
            loggerItr =
            (mBServer.queryMBeans(new ObjectName(LOGGER_TYPE +
            ",host=" + hostName + ",service=" + serviceName), null)).iterator();
        } else if (type == 2) {
            loggerItr =
            (mBServer.queryMBeans(new ObjectName(LOGGER_TYPE +
            ",path=" + contextName + ",host=" + hostName +
            ",service=" + serviceName), null)).iterator();
        }
        
        TreeControlNode loggerNode = null;
        String encodedLoggerName;
        
        while(loggerItr.hasNext()){
            
            ObjectInstance loggerObj = (ObjectInstance)loggerItr.next();
            ObjectName loggerObjName = loggerObj.getObjectName();            
            encodedLoggerName =  URLEncoder.encode(loggerObj.getObjectName().toString());
            
            String className =
            (String)mBServer.getAttribute(loggerObj.getObjectName(), 
            SetUpLoggerAction.CLASSNAME_PROP_NAME);
            
            String loggerType = null;
            StringTokenizer st = new StringTokenizer(className, ".");
            while (st.hasMoreTokens()) {
                loggerType = st.nextToken().trim();
            }
           
            String encodedLoggerType =  URLEncoder.encode(loggerType);
            
            String nodeLabel="Logger";
            String encodedNodeLabel =  URLEncoder.encode(nodeLabel);
            
            loggerNode =
            new TreeControlNode(loggerObj.getObjectName().toString(),
            "folder_16_pad.gif",
            nodeLabel,
            "setUpLogger.do?select=" + encodedLoggerName
            +"&nodeLabel="+ encodedNodeLabel
            +"&type="+ encodedLoggerType,
            "content", true);
            
            node.addChild(loggerNode);
        }
    }    
}
