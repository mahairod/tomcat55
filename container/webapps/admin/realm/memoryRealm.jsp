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

<html:form method="GET" action="/SaveMemoryRealm">

  <bean:define id="thisObjectName" type="java.lang.String"
               name="memoryRealmForm" property="objectName"/>
  <html:hidden property="parentObjectName"/>
  <html:hidden property="adminAction"/>
  <html:hidden property="objectName"/>
  <html:hidden property="allowDeletion"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%">
        <div class="page-title-text" align="left">
         <logic:equal name="memoryRealmForm" property="adminAction" value="Create">
            <bean:message key="actions.realms.create"/>
          </logic:equal>
          <logic:equal name="memoryRealmForm" property="adminAction" value="Edit">
            <bean:write name="memoryRealmForm" property="nodeLabel"/>
          </logic:equal>
        </div>
      </td>
      <td align="right" nowrap>
        <div class="page-title-text">
      <controls:actions label="Label Actions">
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
            <logic:notEqual name="memoryRealmForm" property="adminAction" value="Create">
                <logic:notEqual name="memoryRealmForm" property="allowDeletion" value="false">
                <controls:action url='<%= "/DeleteRealm.do?select=" +
                                        URLEncoder.encode(thisObjectName,"UTF-8") %>'>
                <bean:message key="actions.realms.delete"/>
                </controls:action>
            </logic:notEqual>
            </logic:notEqual>
       </controls:actions>
         </div>
      </td>
    </tr>
  </table>
    <%@ include file="../buttons.jsp" %>
  <br>

  <table class="back-table" border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr>
      <td>
       <controls:table tableStyle="front-table" lineStyle="line-row">
        <controls:row header="true"
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label><bean:message key="service.property"/></controls:label>
            <controls:data><bean:message key="service.value"/></controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text" styleId="type">
        <controls:label><bean:message key="connector.type"/>:</controls:label>
            <controls:data>
                 <logic:equal name="memoryRealmForm" property="adminAction" value="Create">
                    <html:select property="realmType" onchange="IA_jumpMenu('self',this)" styleId="type">
                     <bean:define id="realmTypeVals" name="memoryRealmForm" property="realmTypeVals"/>
                     <html:options collection="realmTypeVals" property="value" labelProperty="label"/>
                    </html:select>
                </logic:equal>
                <logic:equal name="memoryRealmForm" property="adminAction" value="Edit">
                  <bean:write name="memoryRealmForm" property="realmType" scope="session"/>
                </logic:equal>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text" styleId="pathName">
            <controls:label><bean:message key="realm.pathName"/>:</controls:label>
            <controls:data>
                <html:text property="pathName" size="25" styleId="pathName"/>
            </controls:data>
        </controls:row>

      </controls:table>
      </td>
    </tr>
  </table>

    <%@ include file="../buttons.jsp" %>
  <br>
  </html:form>
<p>&nbsp;</p>
</body>
</html:html>
