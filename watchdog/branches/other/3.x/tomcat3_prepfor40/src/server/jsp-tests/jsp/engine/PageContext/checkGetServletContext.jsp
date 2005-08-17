<html>
<title>checkGetServletContext</title>
<body>
<%
/*
 Name : checkGetServletContext
*/
%>
<!-- checking for getServletContext method -->

<%= (pageContext.getServletContext()) instanceof javax.servlet.ServletContext %>

</body>
</html>
