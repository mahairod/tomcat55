<html>
<title>negativeWriteStrOffset</title>
<body>
<%
/*
 Nmae : negativeWriteStrOffset
 Description : Call the method write(java.lang.String str,int off, int len),
 make sure that the the argument int len exceeds the length of the string.
 ArrayIndexOutOfBoundsException is expected.
*/
%>
<!-- calling the method write(String s, int off,int len) -->
<!-- trying to print a string exceeding the length of the string --> 

<%
    String  s= "webpage";
    out.write(s, 3,6);
%>

</body>
</html>