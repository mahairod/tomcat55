<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<body bgcolor="white">

<h2 align="center"><bean:message key="index.success"/></h2>
<p>
<h3 align="center">
  <bean:message key="index.logout"/>
  <html:link page="/logOut.do" target="_top">
    <bean:message key="index.here"/>
  </html:link>
</h3>

<!-- Select language -->

<!--

<h2><bean:message key="login.changeLanguage"/></h2>

<html:form action="/setLocale" method="POST">
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
