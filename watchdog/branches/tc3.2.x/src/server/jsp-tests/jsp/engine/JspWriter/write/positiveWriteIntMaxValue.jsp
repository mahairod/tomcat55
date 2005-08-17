<html>
<title>positiveWriteIntMaxValue</title>
<body>
<%
/*
 Name : positiveWriteIntMaxValue
 Description : Call the method in the jsp page write(int i) in a JSP page
 and pass the max value of the integer as an argument.A non-printable ASCII
 character is expected.
*/
%>
<!-- calling the method write(int i) -->
<%
int i= Integer.MAX_VALUE;
out.write(i);
%>
</body>
</html>