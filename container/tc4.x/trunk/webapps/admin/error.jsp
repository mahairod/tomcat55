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

<h2>
  <bean:message key="error.login"/>
  <br>
  <bean:message key="error.tryagain"/>
  <html:link page="/logOut.do">
    <bean:message key="error.here"/>
  </html:link>
</h2>

</center>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
