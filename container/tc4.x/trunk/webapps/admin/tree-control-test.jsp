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

<body bgcolor="white">

<!-- Tree Component -->

<td width="200">
  <tree:render tree="treeControlTest"
             action="treeControlTest.do?tree=${name}"
              style="tree-control"
      styleSelected="tree-control-selected"
    styleUnselected="tree-control-unselected"
  />

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
