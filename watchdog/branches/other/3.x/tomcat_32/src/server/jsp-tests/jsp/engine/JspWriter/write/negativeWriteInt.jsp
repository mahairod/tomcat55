<html>
<title>negativeWriteInt</title>
<body>
<%
/*
 Name : negativeWriteInt
 Description : Call the method write(int i) , after closing the 'out' stream.
 IOException is expected.
*/
%>
<!-- calling the method write(int i) -->
<%
out.close();
int i= 123;
out.write(i);
%>
</body>
</html>