<html>
<title>positiveGetBufferSizeUnBuffered</title>
<body>
<%
/*
 Name : positiveGetBufferSizeUnBuffered
 Description : Set the buffer directive to 'none'. 
 Then callthe getBufferSize() method.
 */
 %>
<!-- this is to test if getBufferSize method works with no buffer-->
<!-- setting buffer size to 'none' using directive -->
<%@ page buffer="none" %>
<% out.println("got="+out.getBufferSize()); %>
<!-- expected to return 1 as buffer is set 'none' --> 
</body>
</html>