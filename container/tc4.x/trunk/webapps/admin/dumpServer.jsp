<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<table border="1" cellpadding="5">

  <tr>
    <th align="center" colspan="1">
      Registered MBean Names
    </th>
  </tr>

  <tr>
    <th align="center">Name</th>
  </tr>

  <logic:iterate id="name" name="names">
    <tr>
      <td><bean:write name="name"/></td>
    </tr>
  </logic:iterate>

</table>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
