<html>
<title>positiveGetBufferSize</title>
<body>
<%
/*
 Name : positiveGetBufferSize
 Description : Set the buffer size,with buffer directive,then use the 
 getBufferSize() method.
 */
 %>
<!-- this is to test if getBufferSize method works -->
<!-- setting buffer size to 5kb using directive -->
<%@ page buffer="5kb" %>
<!-- calling getBufferSize() method -->
<%= out.getBufferSize() %>
<!-- expected to return 5120 as buffer size --> 
</body>
</html>