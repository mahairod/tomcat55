<!-- Standard Struts Entries -->
<%@ page language="java" %>
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

<html:form action="/listUsers">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <bean:message key="listUsers.title"/>
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
              <controls:action url="">
                <bean:message key="actions.user.create"/>
              </controls:action>
              <controls:action url="">
                <bean:message key="actions.user.delete"/>
              </controls:action>
              <!-- add the urls later once those screens get implemented -->
            </controls:actions>
        </div>
      </td>
    </tr>
  </table>
</html:form>

<%--    <%@ include file="buttons.jsp" %>    --%>
  <br>

  <%-- Users List --%>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <table class="front-table" border="1"
         cellspacing="0" cellpadding="0" width="100%">
          <tr class="header-row">
            <td><div align="left" class="table-header-text">
              <bean:message key="listUsers.username"/>
            </div></td>
            <td><div align="left" class="table-header-text">
              <bean:message key="listUsers.fullName"/>
            </div></td>
          </tr>
          <logic:iterate name="users" id="user">
            <tr class="line-row">
              <td><div align="left" class="table-normal-text">&nbsp;
                <controls:attribute name="user" attribute="username"/>
              </div></td>
              <td><div align="left" class="table-normal-text">&nbsp;
                <controls:attribute name="user" attribute="fullName"/>
              </div></td>
            </tr>
          </logic:iterate>
        </table>
      </td>
    </tr>
  </table>

<%--   <%@ include file="buttons.jsp" %>  --%>

  <br>

<p>&nbsp;</p>
</body>
</html:html>
