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

<html:form method="POST" action="/SaveHost">

  <bean:define id="hostName" name="hostForm" property="hostName"/>
  <bean:define id="thisObjectName" type="java.lang.String"
               name="hostForm" property="objectName"/>
  <html:hidden property="adminAction"/>
  <html:hidden property="objectName"/>
  <html:hidden property="serviceName"/>
  
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text" align="left">
          <logic:equal name="hostForm" property="adminAction" value="Create">
            <bean:message key="actions.hosts.create"/>
          </logic:equal>
          <logic:equal name="hostForm" property="adminAction" value="Edit">
            <bean:message key="actions.hosts.edit"/>
          </logic:equal>
        </div>
      </td>
      <td width="19%"> 
       <div align="right">
        <controls:actions>
            <controls:action selected="true"> -----<bean:message key="actions.available.actions"/>----- </controls:action>
            <controls:action disabled="true"> ------------------------------------- </controls:action>
            <logic:notEqual name="hostForm" property="adminAction" value="Create">              
            <%--
            <controls:action url="">  <bean:message key="actions.accesslogger.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.accesslogger.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.alias.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.alias.delete"/> </controls:action>
            </controls:action>
            --%>
            <controls:action url='<%= "/AddLogger.do?parent=" + 
                                  URLEncoder.encode(thisObjectName) %>'>
                <bean:message key="actions.loggers.create"/>
            </controls:action>              
            <controls:action url='<%= "/DeleteLogger.do?parent=" + 
                                  URLEncoder.encode(thisObjectName) %>'> 
                <bean:message key="actions.loggers.deletes"/> 
            </controls:action>
            <controls:action disabled="true"> ------------------------------------- </controls:action>
            <%--
            <controls:action url="">  <bean:message key="actions.requestfilter.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.requestfilter.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.realms.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.realms.deletes"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.valves.create"/> </controls:action>
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
            <controls:action url='<%= "/DeleteHost.do?select=" + 
                                        URLEncoder.encode(thisObjectName) %>'>  
                <bean:message key="actions.hosts.delete"/> 
            </controls:action>           
           </logic:notEqual>          
         </controls:actions>
       </div>
      </td>
    </tr>
  </table>

  <%@ include file="../buttons.jsp" %>
  <br>

 <%-- Host Properties --%>
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td> <div class="table-title-text"> 
        <bean:message key="host.properties"/>
    </div> </td> </tr>
  </table>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
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

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label>
                <bean:message key="host.name"/>:
            </controls:label>
            <controls:data>
            <%-- input only allowed on create transaction --%>
             <logic:equal name="hostForm" property="adminAction" value="Create">
              <html:text property="hostName" size="24" maxlength="24"/>
             </logic:equal>
             <logic:equal name="hostForm" property="adminAction" value="Edit">
              <bean:write name="hostForm" property="hostName"/>
              <html:hidden property="hostName"/>
             </logic:equal>
            </controls:data>
        </controls:row>


        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="host.base"/>:</controls:label>
            <controls:data>
             <logic:equal name="hostForm" property="adminAction" value="Create">
              <html:text property="appBase" size="24"/>
             </logic:equal>
             <logic:equal name="hostForm" property="adminAction" value="Edit">
              <bean:write name="hostForm" property="appBase"/>
              <html:hidden property="appBase"/>
             </logic:equal>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
                <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="hostForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="host.wars"/>:</controls:label>
            <controls:data>
               <html:select property="unpackWARs">
                     <bean:define id="booleanVals" name="hostForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>
      </controls:table>

      </td>
    </tr>
  </table>

<br>
<br>

<%-- Aliases List --%>
 <logic:notEqual name="hostForm" property="adminAction" value="Create">              
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td> 
        <div class="table-title-text"> 
            <bean:message key="host.aliases"/> 
        </div>
    </td> </tr>
  </table>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> <td>
        <table class="front-table" border="1" cellspacing="0" cellpadding="0" width="100%">
          <tr class="header-row"> 
            <td width="27%"> 
              <div class="table-header-text" align="left"><bean:message key="host.alias.name"/> </div>
            </td> </tr>

            <logic:iterate id="aliasVal" name="hostForm" property="aliasVals">
            <tr> <td width="27%" valign="top" colspan=2> 
                <div class="table-normal-text"> <%= aliasVal %> </div>
            </td> </tr>
            </logic:iterate>
         </table>

    </td> </tr>
  </table>
 </logic:notEqual>

  <%@ include file="../buttons.jsp" %>

</html:form>
</body>

</html:html>
