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

<html:form method="POST" action="/users/saveUser" focus="username">

  <html:hidden property="databaseName"/>
  <html:hidden property="objectName"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
          <bean:write name="userForm" property="nodeLabel"/>
        </div>
      </td>
      <td align="right" nowrap> 
        <div class="page-title-text">
          <controls:actions>
            <controls:action selected="true">
              ----<bean:message key="actions.available.actions"/>----
            </controls:action>
            <controls:action>
              ---------------------------------
            </controls:action>
            <!-- will add the urls later once those screens get implemented -->
<%--
            <controls:action url="">
              <bean:message key="users.actions.user.create"/>
            </controls:action>
            <controls:action url="">
              <bean:message key="users.actions.user.delete"/>
            </controls:action>
--%>
          </controls:actions>
        </div>
      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>
<br>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1"
         width="100%">
    <tr> 
      <td> 

        <controls:table tableStyle="front-table" lineStyle="line-row">

          <controls:row header="true" 
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>
              <bean:message key="users.user.properties"/>
            </controls:label>
            <controls:data>
              &nbsp;
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="users.prompt.username"/>
            </controls:label>
            <controls:data>
              <logic:present name="userForm" property="objectName">
                <bean:write name="userForm" property="username"/>
                <html:hidden property="username"/>
              </logic:present>
              <logic:notPresent name="userForm" property="objectName">
                <html:text property="username" size="24" maxlength="32"/>
              </logic:notPresent>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="users.prompt.password"/>
            </controls:label>
            <controls:data>
              <html:text property="password" size="24" maxlength="32"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="users.prompt.fullName"/>
            </controls:label>
            <controls:data>
              <html:text property="fullName" size="24" maxlength="64"/>
            </controls:data>
          </controls:row>

        </controls:table>

      </td>

    </tr>

  </table>

  <bean:define id="checkboxes" scope="page" value="true"/>
  <br>
  <%@ include file="groups.jspf" %>
  <br>
  <%@ include file="roles.jspf" %>

  <%@ include file="../buttons.jsp" %>

</html:form>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
