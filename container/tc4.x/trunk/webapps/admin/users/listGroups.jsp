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

<html:form action="/users/listGroups">

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <bean:message key="users.listGroups.title"/>
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
              <controls:action url='<%= "/users/setUpGroup.do?databaseName=" +
               URLEncoder.encode(request.getParameter("databaseName")) %>'>
                <bean:message key="users.actions.group.create"/>
              </controls:action>
<%--
              <controls:action url="">
                <bean:message key="users.actions.group.delete"/>
              </controls:action>
--%>
              <!-- add the urls later once those screens get implemented -->
            </controls:actions>
        </div>
      </td>
    </tr>
  </table>

</html:form>

<%--    <%@ include file="../buttons.jsp" %>    --%>
  <br>

  <%-- Groups List --%>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <table class="front-table" border="1"
         cellspacing="0" cellpadding="0" width="100%">
          <tr class="header-row">
            <td><div align="left" class="table-header-text">
              <bean:message key="users.list.groupname"/>
            </div></td>
            <td><div align="left" class="table-header-text">
              <bean:message key="users.list.description"/>
            </div></td>
          </tr>
          <logic:iterate name="groups" id="group" type="java.lang.String">
            <tr class="line-row">
              <td><div align="left" class="table-normal-text">&nbsp;
                <html:link page='<%= "/users/setUpGroup.do?objectName=" + 
                                     URLEncoder.encode(group) %>'>
                  <controls:attribute name="group" attribute="groupname"/>
                </html:link>
              </div></td>
              <td><div align="left" class="table-normal-text">&nbsp;
                <controls:attribute name="group" attribute="description"/>
              </div></td>
            </tr>
          </logic:iterate>
        </table>
      </td>
    </tr>
  </table>

<%--   <%@ include file="../buttons.jsp" %>  --%>

  <br>

<p>&nbsp;</p>
</body>
</html:html>
