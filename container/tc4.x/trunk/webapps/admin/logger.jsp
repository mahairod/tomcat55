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

<html:form method="POST" action="/logger">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
       <html:hidden property="loggerName"/>
       <html:hidden property="loggerType"/>
       <div class="page-title-text" align="left"> 
          <bean:write name="loggerForm" property="nodeLabel" scope="session"/>
       </div>
      </td>
      <td width="19%"> 
        <div align="right">
      <controls:actions>
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
            <!-- will add the urls later once those screens get implemented -->
            <controls:action url="">  <bean:message key="actions.thislogger.delete"/> </controls:action>
       </controls:actions>   
         </div>
      </td>
    </tr>
  </table>
    <%@ include file="buttons.jsp" %>
  <br>

  <table class="back-table" border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr> 
      <td> 
       <controls:table tableStyle="front-table" lineStyle="line-row">
            <controls:row header="true" 
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label><bean:message key="service.property"/></controls:label>
            <controls:data><bean:message key="service.value"/></controls:data>
        </controls:row>

      <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.type"/>:</controls:label>
            <controls:data>
              <bean:write name="loggerForm" property="loggerType" 
                    scope="session"/>
            </controls:data>
        </controls:row>
        
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
               <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="loggerForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>
 
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label>Verbosity Level:</controls:label>
            <controls:data>
               <html:select property="verbosityLvl">
                     <bean:define id="verbosityLvlVals" name="loggerForm" property="verbosityLvlVals"/>
                     <html:options collection="verbosityLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>   
      </controls:table>
      </td>
    </tr>
  </table>

    <%-- Display the following fields only if it is a FileLogger --%>
    <%-- These are the properties specific to a FileLogger --%>
     <logic:equal name="loggerForm" property="loggerType" scope="session" 
                  value="FileLogger">
     <br>
        
     <table border="0" cellspacing="0" cellpadding="0" width="100%">
        <tr> <td>  <div class="table-title-text">  
            <bean:message key="logger.filelogger.properties"/>
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
            <controls:label><bean:message key="logger.directory"/>:</controls:label>
            <controls:data>
               <html:text property="directory" size="25"/> 
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="logger.prefix"/>:</controls:label>
            <controls:data>
               <html:text property="prefix" size="25"/> 
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="logger.suffix"/>:</controls:label>
            <controls:data>
               <html:text property="suffix" size="15"/> 
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="logger.timestamp"/>:</controls:label>
            <controls:data>
                <html:select property="timestamp">
                     <bean:define id="booleanVals" name="loggerForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row> 
   </controls:table>
   </td>
  </tr>
  </table>
 </logic:equal>
  
    
    <%@ include file="buttons.jsp" %>
  <br>
  </html:form>
<p>&nbsp;</p>
</body>
</html:html>
