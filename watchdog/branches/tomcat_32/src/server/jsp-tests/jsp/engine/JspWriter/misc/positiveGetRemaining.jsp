<html>
<title>positiveGetRemaining</title>
<body>
<%
/*
 Name : positiveGetRemaining
 Description : Set the buffer using the buffer directive and then call the 
 getRemaining() method.  Assumes the line separator written to the buffer
 is the same as the line separator for the system.  Value reported will
 be as if CRLF was used.
*/
%>
<!-- this is to test if getRemaining method works with no buffer-->
<!-- setting buffer size to 5kb using directive -->
<%@ page buffer="5kb" %>

<% out.print("got="+(out.getRemaining()-(System.getProperty("line.separator").length() == 1 ? 8 : 0))); %>
<!-- expected to return remaining buffer size --> 
</body>
</html>