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

<html:form method="POST" action="/resources/saveDataSource">

  <html:hidden property="objectName"/>
  <html:hidden property="type"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
          <bean:write name="dataSourceForm" property="nodeLabel"/>
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

            <controls:action url="/resources/setUpDataSource.do">
                <bean:message key="resources.actions.datasrc.create"/>
            </controls:action>
            <controls:action url='<%= "/resources/listDataSources.do?forward=" 
                        + URLEncoder.encode("DataSources Delete List") %>'>
                <bean:message key="resources.actions.datasrc.delete"/>
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
        <bean:message key="resources.treeBuilder.datasources"/>
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
              <bean:message key="resources.datasrc.url"/>:
            </controls:label>
            <controls:data>
              <logic:present name="dataSourceForm" property="objectName">
                <bean:write name="dataSourceForm" property="url"/>
                <html:hidden property="url"/>
              </logic:present>
              <logic:notPresent name="dataSourceForm" property="objectName">
                <html:textarea property="url" cols="35" rows="2"/>
              </logic:notPresent>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.datasrc.jdbcclass"/>:
            </controls:label>
            <controls:data>
              <html:text property="driverClass" size="45" maxlength="256"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="users.prompt.username"/>
            </controls:label>
            <controls:data>
              <html:text property="username" size="15" maxlength="25"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="users.prompt.password"/>
            </controls:label>
            <controls:data>
              <html:password property="password" size="15" maxlength="25"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.datasrc.active"/>:
            </controls:label>
            <controls:data>
              <html:text property="active" size="5" maxlength="5"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.datasrc.idle"/>:
            </controls:label>
            <controls:data>
              <html:text property="idle" size="5" maxlength="5"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.datasrc.wait"/>:
            </controls:label>
            <controls:data>
              <html:text property="wait" size="5" maxlength="5"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.datasrc.validation"/>:
            </controls:label>
            <controls:data>
              <html:textarea property="query" cols="35" rows="3"/>
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
