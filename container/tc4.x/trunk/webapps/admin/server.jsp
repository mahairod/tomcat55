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
<html:form method="POST" action="/server" focus="portNumberText">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="page-title-row">
      <td align="left" nowrap>
        <div class="page-title-text"><bean:message key="server.heading"/></div>
      </td>
      <td align="right" nowrap> 
        <div class="page-title-text">
        <controls:actions>
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
            <!-- will add the urls later once those screens get implemented -->
            <controls:action url="">  <bean:message key="actions.services.create"/> </controls:action>
            <controls:action url="">  <bean:message key="actions.services.delete"/> </controls:action>
        </controls:actions>
        </div>
      </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td colspan="2" align="right" nowrap>
        <html:submit styleClass="button">
           <bean:message key="button.save"/> 
        </html:submit>          
        &nbsp;
        <html:reset styleClass="button">
            <bean:message key="button.cancel"/> 
        </html:reset> 
      </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
    </tr>
  </table>
  <table class="back-table" border="0" cellspacing="0" cellpadding="1" width="100%">
    <tr> 
      <td> 
        <table class="front-table" border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr class="header-row"> 
            <td width="27%"> 
              <div class="table-header-text" align="left">Properties</div>
            </td>
            <td width="73%"> 
              <div class="table-header-text" align="left">&nbsp;</div>
            </td>
          </tr>
          <tr> 
            <td>
              <div class="table-label-text"><bean:message key="server.portnumber"/>:</div>
            </td>
            <td>
              <div class="table-normal-text" >
                <html:text property="portNumberText" size="24" maxlength="24"/>
              </div>
            </td>
          </tr>
          <tr>
            <td class="line-row" colspan="2"><img src="" alt="" width="1" height="1" border="0"></td>
          </tr>
          <tr> 
            <td>
              <div class="table-label-text"><bean:message key="server.debuglevel"/>:</div>
            </td>
            <td>
              <div class="table-normal-text" >
                <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="serverForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                   labelProperty="label"/>
                </html:select>
              </div>
            </td>
          </tr>
          <tr>
            <td class="line-row" colspan="2"><img src="" alt="" width="1" height="1" border="0"></td>
          </tr>
          <tr> 
            <td>
              <div class="table-label-text"><bean:message key="server.shutdown"/>:</div>
            </td>
            <td>
              <div class="table-normal-text" >
                <html:text property="shutdownText" size="24" maxlength="24"/>
              </div>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td colspan="2" align="right" nowrap>
        <html:submit styleClass="button">
           <bean:message key="button.save"/> 
        </html:submit>          
        &nbsp;
        <html:reset styleClass="button">
            <bean:message key="button.cancel"/> 
        </html:reset> 
      </td>
    </tr>
  </table>
</html:form>

<!-- Standard Footer -->

<%@ include file="footer.jsp" %>

</body>

</html:html>
