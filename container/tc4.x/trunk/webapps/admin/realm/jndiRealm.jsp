<!-- Standard Struts Entries -->
<%@ page language="java" import="java.net.URLEncoder" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/controls.tld" prefix="controls" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">

<%@ include file="../users/header.jsp" %>

<!-- Body -->
<body bgcolor="white" background="../images/PaperTexture.gif">

<!--Form -->

<html:errors/>

<html:form method="POST" action="/SaveJNDIRealm">
  <bean:define id="thisObjectName" type="java.lang.String"
               name="jndiRealmForm" property="objectName"/>
  <html:hidden property="adminAction"/>
  <html:hidden property="objectName"/>
  <html:hidden property="parentObjectName"/>
  <html:hidden property="allowDeletion"/>

  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr bgcolor="7171A5">
      <td width="81%">
       <div class="page-title-text" align="left"> 
         <logic:equal name="jndiRealmForm" property="adminAction" value="Create">
            <bean:message key="actions.realms.create"/>
          </logic:equal>
          <logic:equal name="jndiRealmForm" property="adminAction" value="Edit">
            <bean:write name="jndiRealmForm" property="nodeLabel"/>
          </logic:equal>
       </div>
      </td>
      <td width="19%"> 
        <div align="right">
      <controls:actions>
            <controls:action selected="true"> ----<bean:message key="actions.available.actions"/>---- </controls:action>
            <controls:action> --------------------------------- </controls:action>
            <logic:notEqual name="jndiRealmForm" property="adminAction" value="Create">              
                <logic:notEqual name="jndiRealmForm" property="allowDeletion" value="false">  
                <controls:action url='<%= "/DeleteRealm.do?select=" + 
                                        URLEncoder.encode(thisObjectName) %>'>  
                <bean:message key="actions.realms.delete"/> 
            </controls:action>           
           </logic:notEqual>  
        </logic:notEqual>
       </controls:actions>   
         </div>
      </td>
    </tr>
  </table>
    <%@ include file="../buttons.jsp" %>
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
                 <logic:equal name="jndiRealmForm" property="adminAction" value="Create">
                    <html:select property="realmType" onchange="IA_jumpMenu('self',this)">
                     <bean:define id="realmTypeVals" name="jndiRealmForm" property="realmTypeVals"/>
                     <html:options collection="realmTypeVals" property="value" labelProperty="label"/>
                    </html:select>
                </logic:equal>
                <logic:equal name="jndiRealmForm" property="adminAction" value="Edit">
                  <bean:write name="jndiRealmForm" property="realmType" scope="session"/>
                </logic:equal>
            </controls:data>
        </controls:row>
      
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.connName"/>:</controls:label>
            <controls:data>
              <html:text property="connectionName" size="30"/>
            </controls:data>
        </controls:row>
      
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.connPassword"/>:</controls:label>
            <controls:data>
                <html:text property="connectionPassword" size="30"/>
            </controls:data>
        </controls:row>
      
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.connURL"/>:</controls:label>
            <controls:data>
                <html:text property="connectionURL" size="30"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.connFactory"/>:</controls:label>
            <controls:data>
                <html:text property="contextFactory" size="30"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="server.debuglevel"/>:</controls:label>
            <controls:data>
               <html:select property="debugLvl">
                     <bean:define id="debugLvlVals" name="jndiRealmForm" property="debugLvlVals"/>
                     <html:options collection="debugLvlVals" property="value"
                        labelProperty="label"/>
                </html:select>
            </controls:data>
        </controls:row>

       <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.digest"/>:</controls:label>
            <controls:data>
                <html:text property="digest" size="30"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.roleBase"/>:</controls:label>
            <controls:data>
                <html:text property="roleBase" size="30"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.Attribute"/>:</controls:label>
            <controls:data>
                <html:text property="roleAttribute" size="30"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.pattern"/>:</controls:label>
            <controls:data>
                <html:text property="rolePattern" size="30"/>
            </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.search"/>:</controls:label>
            <controls:data>
             <html:select property="search">
                     <bean:define id="searchVals" name="jndiRealmForm" property="searchVals"/>
                     <html:options collection="searchVals" property="value"
                        labelProperty="label"/>
                </html:select>
              </controls:data>
        </controls:row>

        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.userPassword"/>:</controls:label>
            <controls:data>
                <html:text property="userPassword" size="30"/>
            </controls:data>
        </controls:row>
      
        <controls:row labelStyle="table-label-text" dataStyle="table-normal-text">
            <controls:label><bean:message key="realm.userPattern"/>:</controls:label>
            <controls:data>
                <html:text property="userPattern" size="30"/>
            </controls:data>
        </controls:row>
      
      </controls:table>
      </td>
    </tr>
  </table>
  
    <%@ include file="../buttons.jsp" %>
  <br>
  </html:form>
<p>&nbsp;</p>
</body>
</html:html>
