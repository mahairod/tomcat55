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

<%@ page language="java" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="7171A5" background="images/BlueTile.gif">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td align="left" valign="middle">
        <div class="masthead-title-text" align="left"><img src="images/TomcatBanner.jpg" alt="Tomcat Web Server Administration Tool" height="120"></div>
      </td>
      <form method='post' action='<%=request.getContextPath()%>/commitChanges.do' target='_self'>
      <td align="right" valign="middle">
        <html:submit onclick="if(confirm('Are you sure?  Committing changes will restart modified web applications.')) { return true; } else { return false; }">
          <bean:message key="button.commit"/>
        </html:submit>
      </td>
      </form>
      <td width="1%">
        <div class="table-normal-text" align="left">&nbsp </div>
      </td>
    <form method='post' action='<%=request.getContextPath()%>/logOut.do' target='_top'>
      <td align="right" valign="middle">
        <html:submit>
          <bean:message key="button.logout"/>
        </html:submit>
      </td>
      <td width="1%">
        <div class="table-normal-text" align="left">&nbsp </div>
      </td>
    </form>
  </tr>
</table>

<!-- Select language -->
<!--

<h2><bean:message key="login.changeLanguage"/></h2>

<html:form action="/setLocale" method="POST" target="_self">
  <table border="0" cellspacing="5">
    <tr>
      <td align="right">
        <html:select property="locale">
          <html:options name="applicationLocales"
                    property="localeValues"
                   labelName="applicationLocales"
               labelProperty="localeLabels"/>
        </html:select>
      </td>
      <td align="left">
        <html:submit>
          <bean:message key="button.change"/>
        </html:submit>
      </td>
    </tr>
  </table>
</html:form>
-->

</body>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</html:html>
