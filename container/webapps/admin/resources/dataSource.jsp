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

<html:form method="POST" action="/resources/saveDataSource">

  <html:hidden property="objectName"/>

  <bean:define id="resourcetypeInfo" type="java.lang.String"
               name="dataSourceForm" property="resourcetype"/>
  <html:hidden property="resourcetype"/>

  <bean:define id="pathInfo" type="java.lang.String"
               name="dataSourceForm" property="path"/>
  <html:hidden property="path"/>

  <bean:define id="hostInfo" type="java.lang.String"
               name="dataSourceForm" property="host"/>
  <html:hidden property="host"/>

  <bean:define id="domainInfo" type="java.lang.String"
               name="dataSourceForm" property="domain"/>
  <html:hidden property="domain"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
          <bean:write name="dataSourceForm" property="nodeLabel"/>
        </div>
      </td>
      <td align="right" nowrap>
        <div class="page-title-text">
          <controls:actions label="Data Source Actions">
            <controls:action selected="true">
              ----<bean:message key="actions.available.actions"/>----
            </controls:action>
            <controls:action>
              ---------------------------------
            </controls:action>

        <controls:action url='<%= "/resources/setUpDataSource.do?resourcetype=" +
                            URLEncoder.encode(resourcetypeInfo,"UTF-8") + "&path="+
                            URLEncoder.encode(pathInfo,"UTF-8") + "&host="+
                            URLEncoder.encode(hostInfo,"UTF-8") + "&domain="+
                            URLEncoder.encode(domainInfo,"UTF-8") %>'>
                <bean:message key="resources.actions.datasrc.create"/>
            </controls:action>
            <controls:action url='<%= "/resources/listDataSources.do?resourcetype=" +
                            URLEncoder.encode(resourcetypeInfo,"UTF-8") + "&path="+
                            URLEncoder.encode(pathInfo,"UTF-8") + "&host="+
                            URLEncoder.encode(hostInfo,"UTF-8") + "&domain="+
                            URLEncoder.encode(domainInfo,"UTF-8") + "&forward=" +
                            URLEncoder.encode("DataSources Delete List","UTF-8") %>'>
                <bean:message key="resources.actions.datasrc.delete"/>
            </controls:action>
         </controls:actions>
        </div>
      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>
<br>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr><td><div class="table-title-text">
        <bean:message key="resources.treeBuilder.datasources"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr>
      <td>

        <controls:table tableStyle="front-table" lineStyle="line-row">
          <controls:row header="true"
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label><bean:message key="service.property"/></controls:label>
            <controls:data><bean:message key="service.value"/></controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="jndi">
            <controls:label>
              <bean:message key="resources.datasrc.jndi"/>:
            </controls:label>
            <controls:data>
              <logic:present name="dataSourceForm" property="objectName">
                <bean:write name="dataSourceForm" property="jndiName"/>
                <html:hidden property="jndiName"/>
              </logic:present>
              <logic:notPresent name="dataSourceForm" property="objectName">
                <html:text property="jndiName" size="35" maxlength="56" styleId="jndi"/>
              </logic:notPresent>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="url">
            <controls:label>
              <bean:message key="resources.datasrc.url"/>:
            </controls:label>
            <controls:data>
                <html:textarea property="url" cols="35" rows="2" styleId="url"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="jdbcclass">
            <controls:label>
              <bean:message key="resources.datasrc.jdbcclass"/>:
            </controls:label>
            <controls:data>
              <html:text property="driverClass" size="45" maxlength="256" styleId="jdbcclass"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="username">
            <controls:label>
              <bean:message key="users.prompt.username"/>
            </controls:label>
            <controls:data>
              <html:text property="username" size="15" maxlength="25" styleId="username"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="password">
            <controls:label>
              <bean:message key="users.prompt.password"/>
            </controls:label>
            <controls:data>
              <html:password property="password" size="15" maxlength="25" styleId="password"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="active">
            <controls:label>
              <bean:message key="resources.datasrc.active"/>:
            </controls:label>
            <controls:data>
              <html:text property="active" size="5" maxlength="5" styleId="active"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="idle">
            <controls:label>
              <bean:message key="resources.datasrc.idle"/>:
            </controls:label>
            <controls:data>
              <html:text property="idle" size="5" maxlength="5" styleId="idle"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="wait">
            <controls:label>
              <bean:message key="resources.datasrc.wait"/>:
            </controls:label>
            <controls:data>
              <html:text property="wait" size="5" maxlength="5" styleId="wait"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text" styleId="validation">
            <controls:label>
              <bean:message key="resources.datasrc.validation"/>:
            </controls:label>
            <controls:data>
              <html:textarea property="query" cols="35" rows="3" styleId="validation"/>
            </controls:data>
          </controls:row>

        </controls:table>

      </td>

    </tr>

  </table>

  <%@ include file="../buttons.jsp" %>

</html:form>

<!-- Standard Footer -->

<%@ include file="../users/footer.jsp" %>

</body>

</html:html>
