<html>
<title>positiveGetRemaining</title>
<body>
<%
/*
 Name : positiveGetRemaining
 Description : Set the buffer using the buffer directive and then call the 
 getRemaining() method.  The value will vary depending on the line separator
 used for this file.  Report two values so one will match the expected value
 based on CRLF as the line separator.
*/
%>
<!-- this is to test if getRemaining method works with no buffer-->
<!-- setting buffer size to 5kb using directive -->
<%@ page buffer="5kb" %>

<% int r = out.getRemaining();
   out.println("got="+r);
   out.print("got="+(r-8)); %>
<!-- expected to return remaining buffer size --> 
</body>
</html>