<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- Standard Struts Entries -->

<%@ page language="java" import="java.net.URLEncoder" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>

<html:html locale="true">

<%@ include file="../users/header.jsp" %>

<!-- Body -->
<body bgcolor="white" background="../images/PaperTexture.gif">

<!--Form -->

<html:errors/>

<html:form method="POST" action="/SaveService">

  <bean:define id="serviceName" name="serviceForm" property="serviceName"/>
  <bean:define id="thisObjectName" type="java.lang.String"
               name="serviceForm" property="objectName"/>
  <bean:define id="thisServiceName" type="java.lang.String"
               name="serviceForm" property="serviceName"/>
  <html:hidden property="adminServiceName"/>
  <html:hidden property="objectName"/>
  <html:hidden property="engineObjectName"/>
  <html:hidden property="adminAction"/>
  <bean:define id="adminServiceName" type="java.lang.String"
               name="serviceForm" property="adminServiceName"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td width="81%">
        <div class="page-title-text" align="left">
          <logic:equal name="serviceForm" property="adminAction" value="Create">
            <bean:message key="actions.services.create"/>
          </logic:equal>
          <logic:equal name="serviceForm" property="adminAction" value="Edit">
            <bean:write name="serviceForm" property="nodeLabel"/>
          </logic:equal>
        </div>
      </td>
      <td align="right" nowrap>
        <div class="page-title-text">
          <controls:actions label="Service Actions">
            <controls:action selected="true">
              -----<bean:message key="actions.available.actions"/>-----
            </controls:action>
            <controls:action disabled="true">
              -------------------------------------
            </controls:action>
            <logic:notEqual name="serviceForm" property="adminAction" value="Create">
              <controls:action url='<%= "/AddConnector.do?select=" +
                                        URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.connectors.create"/>
              </controls:action>
              <controls:action url='<%= "/DeleteConnector.do?select=" +
                                        URLEncoder.encode(thisObjectName,"UTF-8")%>'>
                <bean:message key="actions.connectors.deletes"/>
              </controls:action>
              <controls:action>
                -------------------------------------
              </controls:action>
              <controls:action disabled="true">
                -------------------------------------
              </controls:action>
              <controls:action url='<%= "/AddHost.do?select=" +
                                        URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.hosts.create"/>
              </controls:action>
              <controls:action url='<%= "/DeleteHost.do?select=" +
                                        URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.hosts.deletes"/>
              </controls:action>
              <controls:action disabled="true">
                -------------------------------------
              </controls:action>
               <%-- cannot delete or add a Realm for the service the admin app runs on --%>
              <logic:notEqual name="serviceName" value='<%= adminServiceName %>'>
              <controls:action disabled="true">
                -------------------------------------
              </controls:action>
              <%--
              <controls:action url='<%= "/AddRealm.do?parent=" +
                                  URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.realms.create"/>
             </controls:action>
             <controls:action url='<%= "/DeleteRealm.do?parent=" +
                                  URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.realms.deletes"/>
              </controls:action>
              --%>
              </logic:notEqual>
              <controls:action disabled="true">
                -------------------------------------
              </controls:action>
              <controls:action url='<%= "/AddValve.do?parent=" +
                                  URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.valves.create"/>
              </controls:action>
              <controls:action url='<%= "/DeleteValve.do?parent=" +
                                  URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.valves.deletes"/>
               </controls:action>
               <%-- cannot delete the service the admin app runs on --%>
               <logic:notEqual name="serviceName" value='<%= adminServiceName %>'>
               <controls:action disabled="true">
                -------------------------------------
                </controls:action>
                 <controls:action url='<%= "/DeleteService.do?select=" +
                                        URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.services.delete"/>
              </controls:action>
              </logic:notEqual>
            </logic:notEqual>
          </controls:actions>
        </div>
      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>

  <%-- Service Properties --%>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr><td><div class="table-title-text">
      <bean:message key="service.properties"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0"
         cellspacing="0" cellpadding="1" width="100%">
    <tr>
      <td>
        <controls:table tableStyle="front-table" lineStyle="line-row">
          <controls:row header="true"
              labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>
              <bean:message key="service.property"/>
            </controls:label>
            <controls:data>
              <bean:message key="service.value"/>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text" styleId="serviceName">
            <controls:label>
              <bean:message key="service.name"/>:
            </controls:label>
            <controls:data>
              <logic:equal name="serviceForm" property="adminAction" value="Create">
                <html:text property="serviceName" size="50" maxlength="50" styleId="serviceName"/>
              </logic:equal>
              <logic:equal name="serviceForm" property="adminAction" value="Edit">
                <html:hidden property="serviceName"/>
                <bean:write name="serviceForm" property="serviceName"/>
              </logic:equal>
            </controls:data>
          </controls:row>
        </controls:table>
      </td>
    </tr>
  </table>

  <br>

  <%-- Engine Properties --%>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr><td><div class="table-title-text">
      <bean:message key="service.engine.props"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0"
         cellspacing="0" cellpadding="1" width="100%">
    <tr>
      <td>
        <controls:table tableStyle="front-table" lineStyle="line-row">
          <controls:row header="true"
              labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>
              <bean:message key="service.property"/>
            </controls:label>
            <controls:data>
              <bean:message key="service.value"/>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text" styleId="engineName">
            <controls:label>
              <bean:message key="service.name"/>:
            </controls:label>
            <controls:data>
              <logic:equal name="serviceForm" property="adminAction" value="Create">
                <html:text property="engineName" size="50" maxlength="50" styleId="engineName"/>
              </logic:equal>
              <logic:equal name="serviceForm" property="adminAction" value="Edit">
                <html:hidden property="engineName"/>
                <bean:write name="serviceForm" property="engineName"/>
              </logic:equal>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text" styleId="hostNameVals">
            <controls:label>
              <bean:message key="service.defaulthostname"/>:
            </controls:label>
            <controls:data>
              <bean:define id="hostNameVals" name="serviceForm"
                           property="hostNameVals"/>
              <html:select property="defaultHost" styleId="hostNameVals">
                <html:options collection="hostNameVals" property="value"
                              labelProperty="label"/>
              </html:select>
            </controls:data>
          </controls:row>
        </controls:table>
      </td>
    </tr>
  </table>

  <br>

  <%@ include file="../buttons.jsp" %>

</html:form>
</body>
</html:html>
