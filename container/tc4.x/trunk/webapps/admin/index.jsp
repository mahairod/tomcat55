<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<!-- Standard Content -->

<%@ include file="header.jsp" %>

<!-- Body -->

<frameset rows="100,*" border="1">
  <frame name="banner" src='<%= response.encodeURL("banner.jsp") %>' scrolling="no">
  <frameset cols="300,*" border="1">
    <frame name="tree" src='<%= response.encodeURL("tree-control-test.jsp") %>' scrolling="auto">
    <frame name="content" src='<%= response.encodeURL("sample.jsp") %>' scrolling="auto">
  </frameset>
</frameset>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</html:html>
