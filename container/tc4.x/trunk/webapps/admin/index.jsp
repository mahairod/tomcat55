<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<frameset rows="100,*" border="0">
  <frame name="banner" src="banner.jsp" scrolling="no">
  <frameset cols="300,*" border="0">
    <frame name="tree" src="tree-control-test.jsp" scrolling="auto">
    <frame name="content" src="sample.jsp" scrolling="auto">
  </frameset>
</frameset>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</html:html>
