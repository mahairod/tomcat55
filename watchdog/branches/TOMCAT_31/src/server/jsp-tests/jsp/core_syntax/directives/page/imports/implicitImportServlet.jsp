<html>
<body>
<% /**	Name: implicitImportServlet
		Description:Use  jsp page directive with language ="java" 
			    Do not specify the import attribute.javax.servlet
			     package should be available implicitly.Check 
			     for some class in servlet
			    
		Result:No error
**/ %>		

<!-- language =java and we check if implicit  import works-->

<%@ page language="java"  %>

<%

try{

 RequestDispatcher rd =getServletContext().getRequestDispatcher( "/jsp/core_syntax/directives/page/imports/implicit.jsp");
 rd.forward(request, response);

  }catch(Exception o){o.printStackTrace();}

%>
</body>
</html>	  
	  