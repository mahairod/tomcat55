<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tree-control.tld" prefix="tree" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<table border="1" cellpadding="0" cellspacing="0" width="100%">

  <!-- Application Header -->
  <tr>
    <td align="center" colspan="2">
      <h2>
        <bean:message key="index.success"/>
        <br>
        <bean:message key="index.logout"/>
        <html:link page="/logOut.do">
          <bean:message key="index.here"/>
        </html:link>
      </h2>
    </td>
  </tr>

  <tr valign="top">
    <!-- Tree Component -->
    <td width="200">
      <tree:render tree="treeControlTest"
                 action="treeControlTest.do?tree=${name}"
                  style="tree-control"
          styleSelected="tree-control-selected"
        styleUnselected="tree-control-unselected"
      />
    </td>
    <!-- Input Form -->
    <td>
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
    </td>
  </tr>
</table>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
