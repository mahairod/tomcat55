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

<html:form method="POST" action="/connector">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%"> 
       <html:hidden property="connectorName"/>
       <div class="page-title-text" align="left">Connector ( 
         <bean:write name="connectorForm" property="scheme" scope="session"/>
        ) </div>
      </td>
      <td width="19%"> 
        <div align="right">
      <controls:actions>
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
            <!-- will add the urls later once those screens get implemented -->
            <controls:action url="">  <bean:message key="actions.thisconnector.delete"/> </controls:action>
       </controls:actions>   
         </div>
      </td>
    </tr>
  </table>
    <%@ include file="buttons.jsp" %>
  <br>

  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
       <controls:table tableStyle="front-table" lineStyle="line-row">
            <controls:row header="true" 
                labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>General</controls:label>
            <controls:data>&nbsp;</controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.type"/>:</controls:label>
            <controls:data>
              <bean:write name="connectorForm" property="scheme" 
                    scope="session"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.accept.count"/>:</controls:label>
            <controls:data>
              <html:text property="acceptCountText" size="5" maxlength="5"/>
            </controls:data>
        </controls:row>

       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.connection.timeout"/><br>
                (milliseconds) :</controls:label>
            <controls:data>
               <html:text property="connTimeOutText" size="10"/>
            </controls:data>
        </controls:row>
 
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
               <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="connectorForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>
 
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.default.buffer"/>:</controls:label>
            <controls:data>
               <html:text property="bufferSizeText" size="5"/>   
            </controls:data>
        </controls:row>
 
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.enable.dns"/>:</controls:label>
            <controls:data>
                <html:select property="enableLookups">
                     <bean:define id="booleanVals" name="connectorForm" property="booleanVals"/>
                     <html:options collection="booleanVals" property="value"
                   labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.address.ip"/>:</controls:label>
            <controls:data>
               <html:text property="address" size="20"/>    
            </controls:data>
        </controls:row>
 
        <controls:row header="true" labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>Ports</controls:label>
            <controls:data>&nbsp;</controls:data>
        </controls:row>
    
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.portnumber"/>:</controls:label>
            <controls:data>
               <html:text property="portText" size="5"/> 
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.redirect.portnumber"/>:</controls:label>
            <controls:data>
               <html:text property="redirectPortText" size="5"/> 
            </controls:data>
        </controls:row>

        <controls:row header="true" labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>Processors</controls:label>
            <controls:data>&nbsp;</controls:data>
        </controls:row>
    
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.min"/>:</controls:label>
            <controls:data>
               <html:text property="minProcessorsText" size="5"/>  
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.max"/>:</controls:label>
            <controls:data>
               <html:text property="maxProcessorsText" size="5"/>
            </controls:data>
        </controls:row>

        <controls:row header="true" labelStyle="table-header-text" dataStyle="table-header-text">
            <controls:label>Proxy</controls:label>
            <controls:data>&nbsp;</controls:data>
        </controls:row>
    
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.proxy.name"/>:</controls:label>
            <controls:data>
               <html:text property="proxyName" size="30"/> 
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="connector.proxy.portnumber"/>:</controls:label>
            <controls:data>
                <html:text property="proxyPortText" size="5"/> 
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
