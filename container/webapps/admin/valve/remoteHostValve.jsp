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
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<html:html locale="true">

<%@ include file="../users/header.jsp" %>

<!-- Body -->
<body bgcolor="white" background="../images/PaperTexture.gif">

<!--Form -->

<html:errors/>

<html:form method="POST" action="/SaveRemoteHostValve">

  <bean:define id="thisObjectName" type="java.lang.String"
               name="remoteHostValveForm" property="objectName"/>
  <bean:define id="thisParentName" type="java.lang.String"
               name="remoteHostValveForm" property="parentObjectName"/>
  <html:hidden property="adminAction"/>
  <html:hidden property="parentObjectName"/>
  <html:hidden property="objectName"/>
  <html:hidden property="valveType"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%">
       <div class="page-title-text" align="left">
         <logic:equal name="remoteHostValveForm" property="adminAction" value="Create">
            <bean:message key="actions.valves.create"/>
          </logic:equal>
          <logic:equal name="remoteHostValveForm" property="adminAction" value="Edit">
            <bean:write name="remoteHostValveForm" property="nodeLabel"/>
          </logic:equal>
       </div>
      </td>
      <td align="right" nowrap>
        <div class="page-title-text">
      <controls:actions label="Valve Actions">
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
            <logic:notEqual name="remoteHostValveForm" property="adminAction" value="Create">
             <controls:action url='<%= "/DeleteValve.do?"  +
                                 "select=" + URLEncoder.encode(thisObjectName,"UTF-8") +
                                 "&parent="+ URLEncoder.encode(thisParentName,"UTF-8") %>'>
                <bean:message key="actions.valves.delete"/>
              </controls:action>
              </logic:notEqual>
       </controls:actions>
         </div>
      </td>
    </tr>
  </table>
    <%@ include file="../buttons.jsp" %>
  <br>

 <%-- RemoteHost Valve Properties --%>
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td> <div class="table-title-text">
        <bean:message key="valve.remotehost.properties"/>
    </div> </td> </tr>
  </table>

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
                 <logic:equal name="remoteHostValveForm" property="adminAction" value="Create">
                    <html:select property="valveType" onchange="IA_jumpMenu('self',this)" styleId="type">
                     <bean:define id="valveTypeVals" name="remoteHostValveForm" property="valveTypeVals"/>
                     <html:options collection="valveTypeVals" property="value" labelProperty="label"/>
                    </html:select>
                </logic:equal>
                <logic:equal name="remoteHostValveForm" property="adminAction" value="Edit">
                  <bean:write name="remoteHostValveForm" property="valveType" scope="session"/>
                </logic:equal>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text" styleId="allowHosts">
            <controls:label><bean:message key="valve.allowHosts"/>:</controls:label>
            <controls:data>
                <html:textarea property="allow" cols="30" rows="3" styleId="allowHosts"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text" styleId="denyHosts">
            <controls:label><bean:message key="valve.denyHosts"/>:</controls:label>
            <controls:data>
                <html:textarea property="deny" cols="30" rows="3" styleId="denyHosts"/>
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
