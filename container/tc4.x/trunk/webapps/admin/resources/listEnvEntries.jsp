<!-- Standard Struts Entries -->
<%@ page language="java" import="java.net.URLEncoder" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>

<html:html locale="true">

<%@ include file="../users/header.jsp" %>

<!-- Body -->
<body bgcolor="white" background="../images/PaperTexture.gif">

<!--Form -->

<html:errors/>

<html:form action="/resources/listEnvEntries">

  <bean:define id="parentNameInfo" type="java.lang.String"
               name="envEntriesForm" property="parentName"/>
  <html:hidden property="parentName"/>

  <bean:define id="parentTypeInfo" type="java.lang.String"
               name="envEntriesForm" property="parentType"/>
  <html:hidden property="parentType"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <bean:message key="resources.env.entries"/>
        </div>
      </td>
      <td width="19%"> 
        <div align="right">
          <%@ include file="listEnvEntries.jspf" %>
        </div>
      </td>
    </tr>
  </table>

<br>

<%@ include file="envEntries.jspf" %>

<br>
</html:form>

</body>
</html:html>
