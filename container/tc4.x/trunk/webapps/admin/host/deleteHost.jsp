<!-- Standard Struts Entries -->
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>

<html:html locale="true">

<%@ include file="../users/header.jsp" %>

<!-- Body -->
<body bgcolor="white">

<!--Form -->

<html:errors/>

<html:form method="post" action="/deleteHost">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <html:hidden property="serviceName"/>
          <bean:message key="actions.host.delete"/>
        </div>
      </td>
      <td width="19%"> 
        <div align="right">
            <controls:actions>
              <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
              <controls:action> --------------------------------- </controls:action>
            </controls:actions>
        </div>
      </td>
    </tr>
  </table>

<%@ include file="../buttons.jsp" %>    
  <br>

  <%-- Hosts List --%>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <table class="front-table" border="1"
         cellspacing="0" cellpadding="0" width="100%">
          <tr class="header-row">
            <td><div align="left" class="table-header-text">
              <bean:message key="actions.delete"/>
            </div></td>
            <td><div align="left" class="table-header-text">
              <bean:message key="host.name"/>
            </div></td>
          </tr>

          <logic:iterate name="hosts" id="host">

            <tr class="line-row">
                <td><div align="left" class="table-normal-text">&nbsp;
                    <input type="checkbox" name="checkbox" 
                     value='<%= host.toString() %>' >                  
                </div></td>
              <td><div align="left" class="table-normal-text">&nbsp;
                <html:link page='<%= "/setUpHost.do?select=" + 
                               java.net.URLEncoder.encode(host.toString())%>'>
                  <controls:attribute name="host" attribute="name"/>
                </html:link>
              </div></td>
            </tr>
          </logic:iterate>
        </table>
      </td>
    </tr>
  </table>

<%@ include file="../buttons.jsp" %>

  <br>
</html:form>

<p>&nbsp;</p>
</body>
</html:html>
