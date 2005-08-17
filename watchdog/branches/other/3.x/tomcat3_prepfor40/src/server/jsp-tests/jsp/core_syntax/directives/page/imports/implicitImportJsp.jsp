<html>
<body>
<% /**	Name: implicitImportJsp
	Description:Use  jsp page directive with language ="java" 
		    Do not specify the import attribute.javax.servlet.jsp
		     package should be available implicitly.Check 
		     for some class.
			    
	Result:No error
**/ %>		


<!-- language =java and we check if implicit  import works-->

<%@ page language="java"  %>

<%

   JspFactory jfac=JspFactory.getDefaultFactory();

 %>
 
 <%= jfac instanceof javax.servlet.jsp.JspFactory %>
  
  
 </body>
 </html>