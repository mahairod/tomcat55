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
    <th align="center" colspan="3">
      Registered Managed Beans
    </th>
  </tr>

  <tr>
    <th align="center">Name</th>
    <th align="center">Group</th>
    <th align="center">Description</th>
  </tr>

  <logic:iterate id="bean" name="beans">
    <tr>
      <td><bean:write name="bean" property="name"/></td>
      <td><bean:write name="bean" property="group"/></td>
      <td><bean:write name="bean" property="description"/></td>
    </tr>
  </logic:iterate>

</table>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
