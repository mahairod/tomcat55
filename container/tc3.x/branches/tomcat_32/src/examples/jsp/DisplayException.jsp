<html>
<!--
  Copyright (c) 2000 The Apache Software Foundation.  All rights 
  reserved.
-->
<%@ page isErrorPage="true" import="java.io.*" %>
<head><title>Error: 500</title><head>
<body>
<h1>Error: 500</h1>
<h2>Location: <%= request.getRequestURI() %></h2>
<%
    if (exception == null) {
%>
<p>No exception found.<br><br>
For debugging purposes, this page can be used to display the stack traces from an exception.<br>
To make this page the default error page for exceptions in a webapp, add:
<pre>
    &lt;error-page&gt;
        &lt;exception-type&gt;java.lang.Throwable&lt;/exception-type&gt;
        &lt;location&gt;/jsp/DisplayException.jsp&lt;/location&gt;
    &lt;/error-page&gt;
</pre>
to the web.xml, adjusting &lt;location&gt; to refer to this page.</p>
<%
    }
    else {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
%>
<p><b>Internal Servlet Error:</b> <%= exception.getMessage() %></p>
<pre><%= sw.toString() %></pre>
<%
        if (exception instanceof ServletException) {
            Throwable cause = ((ServletException)exception).getRootCause();
            if (cause != null) {
            sw=new StringWriter();
            pw=new PrintWriter(sw);
            cause.printStackTrace( pw );
%>
<p><b>Root cause:</b> <%= cause.getMessage() %></p>
<pre><%= sw.toString() %></pre>
<%
            }
        }
    }
%>
</body>
</html>