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

<html:form method="POST" action="/context">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
       <html:hidden property="contextName"/>
       <html:hidden property="loaderName"/>
       <html:hidden property="managerName"/>
       <div class="page-title-text" align="left">
         <bean:write name="contextForm" property="nodeLabel" scope="session"/>
       </div>
      </td>
      <td width="19%"> 
        <div align="right">
      <controls:actions>
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
           <%--
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
            <controls:action> --------------------------------- </controls:action>
            <controls:action url="">  <bean:message key="actions.thiscontext.delete"/> </controls:action>
           --%>
       </controls:actions>   
         </div>
      </td>
    </tr>
  </table>
    <%@ include file="buttons.jsp" %>
  <br>

 <%-- Context Properties table --%>
 
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td>  <div class="table-title-text">  
            <bean:message key="context.properties"/>
    </div> </td> </tr>
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
            <controls:label><bean:message key="context.cookies"/>:</controls:label>
            <controls:data>
                <html:select property="cookies">
                     <bean:define id="booleanVals" name="contextForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.cross.context"/>:</controls:label>
            <controls:data>
                <html:select property="crossContext">
                     <bean:define id="booleanVals" name="contextForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
               <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="contextForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>
 
       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.docBase"/>:</controls:label>
            <controls:data>
<%-- FIXME - input only allowed on create transaction --%>
<%--
               <html:text property="docBase" size="30"/>
--%>
               <bean:write name="contextForm" property="docBase"/>
               <html:hidden property="docBase"/>
            </controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.override"/>:</controls:label>
            <controls:data>
                <html:select property="override">
                     <bean:define id="booleanVals" name="contextForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>


       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.path"/>:</controls:label>
            <controls:data>
<%-- FIXME - input only allowed on create transaction --%>
<%--
               <html:text property="path" size="30"/>
--%>
               <bean:write name="contextForm" property="path"/>
               <html:hidden property="path"/>
            </controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.reloadable"/>:</controls:label>
            <controls:data>
                <html:select property="reloadable">
                     <bean:define id="booleanVals" name="contextForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.usenaming"/>:</controls:label>
            <controls:data>
                <html:select property="useNaming">
                     <bean:define id="booleanVals" name="contextForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.workdir"/>:</controls:label>
            <controls:data>
<%-- FIXME - input only allowed on create transaction --%>
<%--
               <html:text property="workDir" size="30"/>
--%>
               <bean:write name="contextForm" property="workDir"/>
               <html:hidden property="workDir"/>
            </controls:data>
        </controls:row>
   </controls:table>
    </td>
  </tr>
</table>

<br> 

<%-- Loader Properties table --%>

 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td>  <div class="table-title-text">  
            <bean:message key="context.loader.properties"/>
    </div> </td> </tr>
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
            <controls:label><bean:message key="context.checkInterval"/>:</controls:label>
            <controls:data>
                <html:text property="ldrCheckInterval" size="5"/>
            </controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
               <html:select property="ldrDebugLvl">
                     <bean:define id="debugLvlVals" name="contextForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.reloadable"/>:</controls:label>
            <controls:data>
                <html:select property="ldrReloadable">
                     <bean:define id="booleanVals" name="contextForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>
   </controls:table>
    </td>
  </tr>
</table>

<BR>
<%-- Session Manager Properties table --%>
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> <td>  <div class="table-title-text">  
            <bean:message key="context.sessionmgr.properties"/>
    </div> </td> </tr>
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
            <controls:label><bean:message key="context.checkInterval"/>:</controls:label>
            <controls:data>
                <html:text property="mgrCheckInterval" size="5"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
               <html:select property="mgrDebugLvl">
                     <bean:define id="debugLvlVals" name="contextForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>
 
       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.sessionId"/>:</controls:label>
            <controls:data>
               <html:textarea property="mgrSessionIDInit" cols="30" rows="2"/>
            </controls:data>
        </controls:row>

       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="context.max.sessions"/>:</controls:label>
            <controls:data>
               <html:text property="mgrMaxSessions" size="5"/>
            </controls:data>
        </controls:row>
   </controls:table>
    </td>
  </tr>
</table>

    <%@ include file="buttons.jsp" %>

  <br>
  </html:form>
<p>&nbsp;</p>
</body>
</html:html>
