<html>
<title>checkException Test </title>
<body>
<% /**	Name:checkExceptionTest
		Description: checks whether 'exception' is of 
			 type java.lang.Throwable
        Result: Errorpage is calledup, where this check is done.Should return true.					 
**/ %>		
<!-- errorpage -->
<%@ page errorPage="Errorpage.jsp" %>

<%
 int i=0; 
 int j=9;
 int k=j/i;
%>
</body>
</html>


