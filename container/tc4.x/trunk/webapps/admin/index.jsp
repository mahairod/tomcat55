<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<center>

<h2><bean:message key="index.success"/></h2>
<p>
<h2>
  <bean:message key="index.logout"/>
  <html:link page="/logOut.do">
    <bean:message key="index.here"/>
  </html:link>
</h2>

<html:errors/>

<html:form method="POST" action="/sample" focus="someText">
  <table border="0" cellspacing="5">
    <tr>
      <th align="right">
        <bean:message key="prompt.someText"/>
      </th>
      <td align="left">
        <html:text property="someText" size="16" maxlength="16"/>
      </td>
    </tr>
    <tr>
      <th align="right">
        <bean:message key="prompt.moreText"/>
      </th>
      <td align="left">
        <html:text property="moreText" size="16" maxlength="16"/>
      </td>
    </tr>
    <tr>
      <td align="right">
        <html:submit>
          <bean:message key="button.save"/>
        </html:submit>
      </td>
      <td align="left">
        <html:reset>
          <bean:message key="button.reset"/>
        </html:reset>
      </td>
    </tr>
  </table>
</html:form>

</center>

<!-- Select language -->

<hr>

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

</center>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
