<html>
<title>positiveWriteCharArray2</title>
<body>
<%
/*
 Name : positiveWriteCharArray2
*/
%>
<!-- this is to test if write(char[] c,int off,int len) method works -->
<%! char test[]={'m','a','d','r','a','s'}; %>
<% out.write(test,3,3); %>
</body>
</html>