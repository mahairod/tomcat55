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

<html:form method="POST" action="/SaveService">

  <bean:define id="serviceName" name="serviceForm" property="serviceName"/>
  <bean:define id="thisObjectName" type="java.lang.String"
               name="serviceForm" property="objectName"/>
  <html:hidden property="adminAction"/>
  <html:hidden property="objectName"/>
  <html:hidden property="engineObjectName"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td width="81%"> 
        <div class="page-title-text" align="left">
          <logic:equal name="serviceForm" property="adminAction" value="Create">
            <bean:message key="actions.services.create"/>
          </logic:equal>
          <logic:equal name="serviceForm" property="adminAction" value="Edit">
            <bean:message key="actions.services.edit"/>
          </logic:equal>
        </div>
      </td>
      <td width="19%"> 
        <div align="right">
          <controls:actions>
            <controls:action selected="true">
              -----<bean:message key="actions.available.actions"/>-----
            </controls:action>
            <controls:action disabled="true">
              -------------------------------------
            </controls:action>
            <logic:notEqual name="serviceForm" property="adminAction" value="Create">
              <%--
              <controls:action url="">
                <bean:message key="actions.accesslogger.create"/>
              </controls:action>
              <controls:action url="">
                <bean:message key="actions.accesslogger.delete"/>
              </controls:action>
              <controls:action>
                -------------------------------------
              </controls:action>
              <controls:action url="">
                <bean:message key="actions.connector.create"/>
              </controls:action>
              <controls:action url="">
                <bean:message key="actions.connector.delete"/>
              </controls:action>
              <controls:action>
                -------------------------------------
              </controls:action>
              --%>
              <controls:action url='<%= "/AddHost.do?serviceName=" +
                                        serviceName %>'>  
                <bean:message key="actions.hosts.create"/>
              </controls:action>              
              <controls:action url='<%= "/DeleteHost.do?serviceName=" +
                                        serviceName %>'>
                <bean:message key="actions.hosts.deletes"/> 
              </controls:action>
              <controls:action disabled="true">
                -------------------------------------
              </controls:action>
              <controls:action url='<%= "/AddLogger.do?parent=" + 
                                  URLEncoder.encode(thisObjectName) %>'>
                <bean:message key="actions.loggers.create"/>
              </controls:action>
              <controls:action url='<%= "/DeleteLogger.do?parent=" + 
                                  URLEncoder.encode(thisObjectName) %>'> 
                <bean:message key="actions.loggers.deletes"/> 
              </controls:action>
              <controls:action disabled="true"> 
                ------------------------------------- 
              </controls:action>
              <%--
              <controls:action url="">
                <bean:message key="actions.requestfilter.create"/>
              </controls:action>
              <controls:action url="">
                <bean:message key="actions.requestfilter.delete"/>
              </controls:action>
              <controls:action>
                -------------------------------------
              </controls:action>
            <controls:action url='<%= "/AddRealm.do?parent=" + 
                                  URLEncoder.encode(thisObjectName) %>'>
                <bean:message key="actions.realms.create"/>
            </controls:action>              
              --%>
              <controls:action url='<%= "/DeleteRealm.do?parent=" + 
                                  URLEncoder.encode(thisObjectName) %>'> 
                <bean:message key="actions.realms.deletes"/> 
              </controls:action>
              <controls:action disabled="true"> 
                ------------------------------------- 
              </controls:action>
              <%--
              <controls:action url="">
                <bean:message key="actions.valves.create"/>
              </controls:action>
              <controls:action url="">
                <bean:message key="actions.valves.deletes"/>
              </controls:action>
              <controls:action>
                -------------------------------------
              </controls:action>
              --%>
              <controls:action url='<%= "/DeleteService.do?select=" +
                                        URLEncoder.encode(thisObjectName) %>'>
                <bean:message key="actions.services.delete"/>
              </controls:action>
            </logic:notEqual>
          </controls:actions>
        </div>
      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>

  <%-- Service Properties --%>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr><td><div class="table-title-text">  
      <bean:message key="service.properties"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0"
         cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <controls:table tableStyle="front-table" lineStyle="line-row">
          <controls:row header="true" 
              labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>
              <bean:message key="service.property"/>
            </controls:label>
            <controls:data>
              <bean:message key="service.value"/>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="service.name"/>:
            </controls:label>
            <controls:data>
              <logic:equal name="serviceForm" property="adminAction" value="Create">

                <html:text property="serviceName" size="24" maxlength="24"/>
              </logic:equal>
              <logic:equal name="serviceForm" property="adminAction" value="Edit">
                <html:hidden property="serviceName"/>
                <bean:write name="serviceForm" property="serviceName"/>
              </logic:equal>
            </controls:data>
          </controls:row>
        </controls:table>
      </td>
    </tr>
  </table>

  <br>

  <%-- Engine Properties --%>

  <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr><td><div class="table-title-text">  
      <bean:message key="service.engine.props"/>
    </div></td></tr>
  </table>

  <table class="back-table" border="0"
         cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <controls:table tableStyle="front-table" lineStyle="line-row">
          <controls:row header="true" 
              labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>
              <bean:message key="service.property"/>
            </controls:label>
            <controls:data>
              <bean:message key="service.value"/>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="service.name"/>:
            </controls:label>
            <controls:data>
              <logic:equal name="serviceForm" property="adminAction" value="Create">

                <html:text property="engineName" size="24" maxlength="24"/>
              </logic:equal>
              <logic:equal name="serviceForm" property="adminAction" value="Edit">
                <html:hidden property="engineName"/>
                <bean:write name="serviceForm" property="engineName"/>
              </logic:equal>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="server.debuglevel"/>:
            </controls:label>
            <controls:data>
              <bean:define id="debugLvlVals" name="serviceForm"
                           property="debugLvlVals"/>
              <html:select property="debugLvl">
                <html:options collection="debugLvlVals" property="value"
                              labelProperty="label"/>
              </html:select>
            </controls:data>
          </controls:row>
          <controls:row header="false"
              labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label>
              <bean:message key="service.defaulthostname"/>:
            </controls:label>
            <controls:data>
              <bean:define id="hostNameVals" name="serviceForm"
                           property="hostNameVals"/>
              <html:select property="defaultHost">
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

  <%@ include file="../buttons.jsp" %>

</html:form>
</body>
</html:html>
