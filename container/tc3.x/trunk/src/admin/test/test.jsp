<html><title>Tomcat Self-Test</title>
<body bgcolor="#FFFFFF">
<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" 
           prefix="ant" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/test/context_log.txt">here</a>

<%@ include file="sanity-form.jsp" %>

<% // This is an ugly hack to make the logs easily accessible.
   // Keep in mind this is just a way to jump-start testing, not a 
   // production-quality test runner.
%>
<!-- trozo  -->
<% out.flush(); 
   if( request.getParameter("target") == null ) return;
%>
<!-- trozo  -->
<adm:admin ctxPath="/test" 
	   action="setLogger" 
	   value="webapps/test/context_log.txt" />

<!-- trozo 1 -->
<ant:gtest />

<ant:ant>
  <ant:target param="target" />
  
  <ant:property name="ant.file" 
		location="/WEB-INF/test-tomcat.xml" 
		webApp="/test" />
  <ant:property name="gdir" 
		location="/Golden" 
		webApp="/test" />
  <ant:property name="debug"  param="debug" />
  <ant:property name="outputType" value="none"  />
  <ant:property name="port" param="port" />
  <ant:property name="http.protocol" param="server.proto" />
  <ant:property name="host" param="host" />
</ant:ant>

<!-- trozo 1 -->
<% // Test completed, display the results ( outType=none means
   // Gtest doesn't generate any output ( but we have to wait untill
   // it's done ), use 'html' for "interactive" results
%>

<h1>Test <%= antProperties.getProperty("revision") %></h1>

<% // -------------------- Failures -------------------- %>
<h1>FAILED Tests</h1>

<adm:iterate name="failures" enumeration="<%= gtestTestFailures.elements() %>" 
               type="org.apache.tomcat.util.test.GTest" >
<% // Need more tags - if, etc 
%>
<a href='<%= failures.getHttpClient().getURI() %>'> 
<font color='red'> FAIL </font></a> ( <%= failures.getDescription() %> )
    <%= failures.getHttpClient().getRequestLine() %>
<br>
TEST: <%= failures.getMatcher().getTestDescription() %>
<br>
<b>Request: </b>
<pre>
  <%= failures.getHttpClient().getFullRequest() %>
</pre>
<b>Comments: </b>
  <%= failures.getComment() %>
<br>
<b>Message: </b>
<pre>
  <%= failures.getMatcher().getMessage() %>
</pre>

<% // use a tag %>
<% if( request.getParameter("debug" ) != null ) { %>
  <b>Response status: </b> 
  <%= failures.getHttpClient().getResponse().getResponseLine() %>
  <br>
  <b>Response headers: </b>
   (I'm not sure how to do embeded iterations, need JSP expert )
  <br>
  
  <b>Response body: </b>
  <pre>
  <%= failures.getHttpClient().getResponse().getResponseBody() %>
  </pre>
<% } %>  


</adm:iterate>

<% // -------------------- Success story -------------------- %>

<h1>PASSED Tests</h1>

<adm:iterate name="success" enumeration="<%= gtestTestSuccess.elements() %>" 
               type="org.apache.tomcat.util.test.GTest" >

<a href='<%= success.getHttpClient().getURI() %>'> 
OK</a> ( <%= success.getDescription() %> ) 
    <%= success.getHttpClient().getRequestLine() %>
<br>
TEST: <%= success.getMatcher().getTestDescription() %>
<br>
</adm:iterate>
</body>
</html>
