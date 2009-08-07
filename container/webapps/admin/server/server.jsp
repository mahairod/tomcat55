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

<html:form method="POST" action="/SaveServer" focus="portNumberText">
  <bean:define id="thisObjectName" type="java.lang.String"
               name="serverForm" property="objectName"/>
  <html:hidden property="objectName"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
           <bean:write name="serverForm" property="nodeLabel"/>
        </div>
      </td>
      <td align="right" nowrap>
        <div class="page-title-text">
        <controls:actions label="Server Actions">
          <controls:action selected="true">
            ----<bean:message key="actions.available.actions"/>----
          </controls:action>
          <controls:action>
            ---------------------------------
          </controls:action>
          <controls:action url='<%= "/AddService.do?select=" +
                                      URLEncoder.encode(thisObjectName,"UTF-8") %>'>
            <bean:message key="actions.services.create"/>
          </controls:action>
          <controls:action url='<%= "/DeleteService.do?select=" +
                                      URLEncoder.encode(thisObjectName,"UTF-8") %>'>
            <bean:message key="actions.services.deletes"/>
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
      <bean:message key="server.properties"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
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

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text" styleId="portNumber">
            <controls:label><bean:message key="server.portnumber"/>:</controls:label>
            <controls:data>
              <html:text property="portNumberText" size="24" maxlength="24" styleId="portNumber"/>
            </controls:data>
        </controls:row>

       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text" styleId="shutdown">
            <controls:label><bean:message key="server.shutdown"/>:</controls:label>
            <controls:data>
               <html:text property="shutdownText" size="24" maxlength="24" styleId="shutdown"/>
            </controls:data>
        </controls:row>
      </controls:table>

      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>

</html:form>

<!-- Standard Footer -->

<%@ include file="../footer.jsp" %>

</body>

</html:html>
