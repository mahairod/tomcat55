<!-- Standard Struts Entries -->
<%@ page language="java" import="java.net.URLEncoder" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>

<html:html locale="true">

<%@ include file="../users/header.jsp" %>

<!-- Body -->
<body bgcolor="white" background="../images/PaperTexture.gif">

<!--Form -->

<html:errors/>

<html:form action="/resources/listEnvEntries">

  <bean:define id="resourcetypeInfo" type="java.lang.String"
               name="envEntriesForm" property="resourcetype"/>
  <html:hidden property="resourcetype"/>

  <bean:define id="pathInfo" type="java.lang.String"
               name="envEntriesForm" property="path"/>
  <html:hidden property="path"/>

  <bean:define id="hostInfo" type="java.lang.String"
               name="envEntriesForm" property="host"/>
  <html:hidden property="host"/>

  <bean:define id="serviceInfo" type="java.lang.String"
               name="envEntriesForm" property="service"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <bean:message key="resources.actions.env.delete"/>
        </div>
      </td>
      <td width="19%"> 
        <div align="right">
          <%@ include file="listEnvEntries.jspf" %>
        </div>
      </td>
    </tr>
  </table>

</html:form>

<br>

<bean:define id="checkboxes" scope="page" value="true"/>

<html:form action="/resources/deleteEnvEntries">

  <bean:define id="resourcetypeInfo" type="java.lang.String"
               name="envEntriesForm" property="resourcetype"/>
  <html:hidden property="resourcetype"/>

  <bean:define id="pathInfo" type="java.lang.String"
               name="envEntriesForm" property="path"/>
  <html:hidden property="path"/>

  <bean:define id="hostInfo" type="java.lang.String"
               name="envEntriesForm" property="host"/>
  <html:hidden property="host"/>

  <bean:define id="serviceInfo" type="java.lang.String"
               name="envEntriesForm" property="service"/>

  <%@ include file="../buttons.jsp" %>
  <br>
  <%@ include file="envEntries.jspf" %>
  <%@ include file="../buttons.jsp" %>
</html:form>
<br>

<%@ include file="../users/footer.jsp" %>

</body>
</html:html>
