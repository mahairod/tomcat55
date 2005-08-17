<html>
<title>positiveGetRemaining</title>
<body>
<%
/*
 Name : positiveGetRemaining
 Description : Set the buffer using the buffer directive and then call the 
 getRemaining() method.
*/
%>
<!-- this is to test if getRemaining method works with no buffer-->
<!-- setting buffer size to 5kb using directive -->
<%@ page buffer="5kb" %>

<% out.print("got="+out.getRemaining()); %>
<!-- expected to return remaining buffer size --> 
</body>
</html>
