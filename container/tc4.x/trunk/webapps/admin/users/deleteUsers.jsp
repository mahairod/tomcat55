<!-- Standard Struts Entries -->
<%@ page language="java" import="java.net.URLEncoder" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>

<html:html locale="true">

<%@ include file="header.jsp" %>

<!-- Body -->
<body bgcolor="white">

<!--Form -->

<html:errors/>

<html:form action="/users/listUsers">

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <bean:message key="users.deleteUsers.title"/>
        </div>
      </td>
      <td width="19%"> 
        <div align="right">
          <%@ include file="listUsers.jspf" %>
        </div>
      </td>
    </tr>
  </table>

</html:form>

<br>
<bean:define id="checkboxes" scope="page" value="true"/>
<html:form action="/users/deleteUsers">
  <%@ include file="../buttons.jsp" %>
  <br>
  <html:hidden property="databaseName"/>
  <%@ include file="users.jspf" %>
  <%@ include file="../buttons.jsp" %>
</html:form>
<br>

<%@ include file="footer.jsp" %>

</body>
</html:html>
