<!-- Standard Struts Entries -->

<%@ page language="java" import="java.net.URLEncoder" %>
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

<html:form method="POST" action="/resources/saveEnvEntry">

  <html:hidden property="objectName"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
          <bean:write name="envEntryForm" property="nodeLabel"/>
        </div>
      </td>
      <td align="right" nowrap> 
        <div class="page-title-text">
          <controls:actions>
            <controls:action selected="true">
              ----<bean:message key="actions.available.actions"/>----
            </controls:action>
            <controls:action>
              ---------------------------------
            </controls:action>

            <controls:action url="/resources/setUpEnvEntry.do">
                <bean:message key="resources.actions.env.create"/>
            </controls:action>
            <controls:action url='<%= "/resources/listEnvEntries.do?forward=" 
                        + URLEncoder.encode("EnvEntries Delete List") %>'>
                <bean:message key="resources.actions.env.delete"/>
            </controls:action>
         </controls:actions>
        </div>
      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>
<br>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr><td><div class="table-title-text">  
      <bean:message key="resources.env.props"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 

        <controls:table tableStyle="front-table" lineStyle="line-row">            
          <controls:row header="true" 
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label><bean:message key="service.property"/></controls:label>
            <controls:data><bean:message key="service.value"/></controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="service.name"/>:
            </controls:label>
            <controls:data>
              <logic:present name="envEntryForm" property="objectName">
                <bean:write name="envEntryForm" property="name"/>
                <html:hidden property="name"/>
              </logic:present>
              <logic:notPresent name="envEntryForm" property="objectName">
                <html:text property="name" size="24" maxlength="32"/>
              </logic:notPresent>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="connector.type"/>:
            </controls:label>
            <controls:data>
              <html:select property="entryType">
                     <bean:define id="typeVals" name="envEntryForm" property="typeVals"/>
                     <html:options collection="typeVals" property="value" 
                                   labelProperty="label"/>
                </html:select>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="service.value"/>:
            </controls:label>
            <controls:data>
              <html:text property="value" size="24" maxlength="64"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="users.prompt.description"/>
            </controls:label>
            <controls:data>
              <html:text property="description" size="30" maxlength="64"/>
            </controls:data>
          </controls:row>

        </controls:table>

      </td>

    </tr>

  </table>

  <%@ include file="../buttons.jsp" %>

</html:form>

<!-- Standard Footer -->

<%@ include file="../users/footer.jsp" %>

</body>

</html:html>
