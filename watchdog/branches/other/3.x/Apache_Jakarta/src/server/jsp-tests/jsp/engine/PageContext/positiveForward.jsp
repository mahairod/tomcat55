<html>
<title>positiveForward</title>
<body>
<%
/*
 Name : positiveForward
 Description : Call the method forward() with the pageContext
 object, and pass a valid jsp page as an argument in
 the method.
*/
%>
<%
try{
        pageContext.forward("/tests/engine/PageContext/forward.jsp");
    }catch(Exception e){
        out.println(e);
    }
%>
</body>
</html>
