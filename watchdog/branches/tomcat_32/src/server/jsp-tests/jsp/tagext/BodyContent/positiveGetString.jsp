<%@ page import="javax.servlet.jsp.tagext.*" %><html>
<title>positiveGetString</title>
<body>
<% 
	/** 	
	Name : positiveGetString
	Description : Try to print the contains of BodyContent object 
				 using getString() method.
	Result :  Expected to print contents of BodyContent object. 
	**/  
%>	 

<%	
	// using pageContext.pushBody() to create a BodyContent object
	BodyContent bc = pageContext.pushBody();
	bc.println("Checking For getString() method");
	out.println(bc.getString());
		
%>



</body>
</html>