<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<!-- Make sure window is not in a frame -->

<script language="JavaScript" type="text/javascript">

  <!--
    if (window.self != window.top) {
      window.open(window.location, "_top");
    }
  // -->

</script>

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<body bgcolor="white">

<center>

<!-- Login -->

<h2><bean:message key="login.enter"/></h2>

<form method="POST" action='<%= response.encodeURL("j_security_check") %>'
 name="loginForm">
  <table border="0" cellspacing="5">
    <tr>
      <th align="right">
        <bean:message key="prompt.username"/>
      </th>
      <td align="left">
        <input type="text" name="j_username" size="16" maxlength="16"/>
      </td>
    </tr>
    <tr>
      <th align="right">
        <bean:message key="prompt.password"/>
      </th>
      <td align="left">
        <input type="password" name="j_password" size="16" maxlength="16"/>
      </td>
    </tr>
    <tr>
      <td align="right">
        <input type="submit" value='<bean:message key="button.login"/>'>
      </td>
      <td align="left">
        <input type="reset" value='<bean:message key="button.reset"/>'>
      </td>
    </tr>
  </table>
</form>

<script language="JavaScript" type="text/javascript">
  <!--
    document.forms["loginForm"].elements["j_username"].focus()
  // -->
</script>

</body>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</html:html>
