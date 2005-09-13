<html>
<title>checkGetSession</title>
<body>
<%
/*
 Name : checkGetSession
*/
%>
<!-- checking for getSession method -->

<%=(pageContext.getSession()) instanceof javax.servlet.http.HttpSession %>

</body>
</html>
