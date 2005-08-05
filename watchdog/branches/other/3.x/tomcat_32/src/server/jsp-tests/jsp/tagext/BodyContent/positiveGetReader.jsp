<%@ page import="javax.servlet.jsp.tagext.*" %><html>
<title>positiveGetReader</title>
<body>
<% 
	/** 	
	Name : positiveGetReader
	Description : Create a BodyContent object and call
	              the getReader() method. 
	Result :      Resulting object should be of type
	             java.io.Reader
	**/  
%>	 
<%	
	BodyContent bc = pageContext.pushBody();
	bc.println("Moon");
%>
<!-- checking for Reader object -->
<%= (bc.getReader()) instanceof java.io.Reader %>

</body>
</html>