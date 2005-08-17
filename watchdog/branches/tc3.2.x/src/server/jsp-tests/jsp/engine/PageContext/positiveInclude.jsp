<html>
<title>positiveInclude</title>
<body>
<%
/*
 Name : positiveInclude
*/
%>
<!-- this is to test if include() method works -->
<!-- using pageContext object to include -->
<%
try{
    pageContext.include("forward.jsp");
    }catch(Exception e){
    out.println(e);
}
%>


</body>
</html>
