<html>
<title>positiveIsAutoFlush</title>
<body>
<%
/*
 Name : positiveIsAutoFlush
 Description : Set the autoflush directive to 'true' and then
call the isAutoFlush() method .
*/
%>
<!-- this is to test positiveIsAutoFlush -->

<!-- using isAutoFlush() method to get check flush state -->
<%= out.isAutoFlush() %>
<!-- expected to return true  -->
</body>
</html>