<html>
<title>positiveGetBodyContent</title>
<body>
<% 
	/** 	
	Name : positiveGetBodyContent
	Description : Create a TagInfo object passing the bodyContent string.
	              Call the getBodyContent() method.
	Result :     Should print the contents of bodyContent string.
	**/  
%>	 

<%			
	TagInfo ti = new TagInfo("foo","examples.FooTag","To Browser","info",null,null,null);
	out.println(ti.getBodyContent());
%>
</body>
</html>