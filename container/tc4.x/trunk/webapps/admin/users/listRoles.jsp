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

<html:form action="/users/listRoles">

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <bean:message key="users.listRoles.title"/>
        </div>
      </td>
      <td width="19%"> 
        <div align="right">
            <controls:actions>
              <controls:action selected="true">
                ----<bean:message key="actions.available.actions"/>----
              </controls:action>
              <controls:action>
                ---------------------------------
              </controls:action>
              <controls:action url='<%= "/users/setUpRole.do?databaseName=" +
               URLEncoder.encode(request.getParameter("databaseName")) %>'>
                <bean:message key="users.actions.role.create"/>
              </controls:action>
<%--
              <controls:action url="">
                <bean:message key="users.actions.role.delete"/>
              </controls:action>
--%>
              <!-- add the urls later once those screens get implemented -->
            </controls:actions>
        </div>
      </td>
    </tr>
  </table>

</html:form>

<br>
<%@ include file="roles.jspf" %>
<br>

</body>
</html:html>
