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

<html:form method="POST" action="/resources/saveResourceLink">

  <html:hidden property="objectName"/>
  <html:hidden property="resourcetype"/>
  <html:hidden property="path"/>
  <html:hidden property="host"/>
  <html:hidden property="service"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
          <bean:write name="resourceLinkForm" property="nodeLabel"/>
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

            <controls:action url="/resources/setUpResourceLink.do">
                <bean:message key="resources.actions.resourcelk.create"/>
            </controls:action>
            <controls:action url='<%= "/resources/listResourceLinks.do?forward=" + 
                               URLEncoder.encode("ResourceLinks Delete List") %>'>
                <bean:message key="resources.actions.resourcelk.delete"/>
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
        <bean:message key="resources.treeBuilder.resourcelinks"/>
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
              <bean:message key="resources.resourcelk.name"/>:
            </controls:label>
            <controls:data>
              <logic:present name="resourceLinkForm" property="objectName">
                <bean:write name="resourceLinkForm" property="name"/>
                <html:hidden property="name"/>
              </logic:present>
              <logic:notPresent name="resourceLinkForm" property="objectName">
                <html:text property="name" size="35" maxlength="56"/>
              </logic:notPresent>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.resourcelk.global"/>:
            </controls:label>
            <controls:data>
                <html:text property="global" size="35" maxlength="56"/>
            </controls:data>
          </controls:row>

          <controls:row labelStyle="table-label-text"
                         dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="resources.resourcelk.type"/>:
            </controls:label>
            <controls:data>
              <html:text property="type" size="45" maxlength="256"/>
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
