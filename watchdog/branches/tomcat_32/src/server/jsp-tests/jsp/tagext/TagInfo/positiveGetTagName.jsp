<%@ page import="javax.servlet.jsp.tagext.*" %><html>
<title>positiveGetTagName</title>
<body>
<% 
	/** 	
	Name : positiveGetTagName
	Description : Create an object of TagInfo and call the getTagName()
	Result : Should return the name of the tag("foo" here)
	**/  
%>	 

<%	
	TagInfo ti = new TagInfo("foo","examples.FooTag",null,"info",null,null,null);
	out.println(ti.getTagName());
%>
</body>
</html>