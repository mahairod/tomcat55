<!-- Standard Struts Entries -->

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>

<html:html locale="true">

<%@ include file="header.jsp" %>

<!-- Body -->
<body bgcolor="white">

<!--Form -->

<html:errors/>

<html:form method="POST" action="/service">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td width="81%"> 
        <div class="page-title-text" align="left">
        <bean:message key="service.service"/> (
        <bean:write name="serviceForm" property="serviceName" 
                          scope="session"/>
         ) </div>
      </td>
      <td width="19%"> 
        <div align="right">
        <controls:actions>
            <controls:action selected="true"> -----<bean:message key="actions.available.actions"/>----- </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <!-- will add the urls later once those screens get implemented -->
            <controls:action url="">  <bean:message key="actions.accesslogger.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.accesslogger.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.connector.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.connector.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.host.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.host.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.logger.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.logger.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.requestfilter.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.requestfilter.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.userrealm.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.userrealm.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.valve.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.valve.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.service.delete"/> </controls:action>
        </controls:actions>
          </div>
      </td>
    </tr>
  </table>

  <%@ include file="buttons.jsp" %>

 <%-- Heading --%>
 
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td>  <div class="table-title-text">  
            <bean:message key="service.properties"/>
    </div> </td> </tr>
  </table>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <controls:table tableStyle="front-table" lineStyle="line-row">
            <controls:row header="true" 
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label><bean:message key="service.property"/></controls:label>
            <controls:data><bean:message key="service.value"/> </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="service.name"/>:</controls:label>
            <controls:data>
              <html:hidden property="serviceName"/>
              <bean:write name="serviceForm" property="serviceName" 
                          scope="session"/> 
            </controls:data>
        </controls:row>

    </controls:table>

    </td>
    </tr>
  </table>

<br>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td> <div class="table-title-text">  
        <bean:message key="service.engine.props"/> 
    </div> </td> </tr>
  </table>
 
  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <controls:table tableStyle="front-table" lineStyle="line-row">
            <controls:row header="true" 
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label><bean:message key="service.property"/></controls:label>
            <controls:data><bean:message key="service.value"/> </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="service.name"/>:</controls:label>
            <controls:data>
              <html:text property="engineName" size="24" maxlength="24"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
                <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="serviceForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="service.defaulthostname"/>:</controls:label>
            <controls:data>
                <html:select property="defaultHost">
                     <bean:define id="hostNameVals" 
                            name="serviceForm" property="hostNameVals"/>
                     <html:options collection="hostNameVals" property="value"
                            labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

    </controls:table>
    </td>
    </tr>
  </table>
  <br>

  <%@ include file="buttons.jsp" %>

  </html:form>
<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
