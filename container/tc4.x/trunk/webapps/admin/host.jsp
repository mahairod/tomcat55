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

<html:form method="POST" action="/host" focus="name">

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text">
            <html:hidden property="hostName"/>
            <bean:write name="hostForm" property="nodeLabel" scope="session"/>
        </div>
      </td>
      <td align="right" nowrap> 
       <div align="right">
        <controls:actions>
            <controls:action selected="true"> -----<bean:message key="actions.available.actions"/>----- </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <!-- will add the urls later once those screens get implemented -->
            <controls:action url="">  <bean:message key="actions.accesslogger.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.accesslogger.delete"/> </controls:action>
            <controls:action> ------------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.alias.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.alias.delete"/> </controls:action>
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
            <controls:action url="">  <bean:message key="actions.host.delete"/> </controls:action>
        </controls:actions>
          </div>
      </td>
    </tr>
  </table>

  <%@ include file="buttons.jsp" %>
<br>

 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td> 
        <div class="table-title-text"> 
            <bean:message key="host.properties"/> 
        </div>
    </td> </tr>
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

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="host.name"/>:</controls:label>
            <controls:data>
              <html:text property="name" size="24" maxlength="24"/>
            </controls:data>
        </controls:row>


        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="host.base"/>:</controls:label>
            <controls:data>
              <html:text property="appBase" size="24" maxlength="24"/>
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

 <!-- Aliases -->
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
  <!-- Alias table end -->

  <%@ include file="buttons.jsp" %>

</html:form>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
