<html>
<title>positiveGetTagClassName</title>
<body>
<% 
	/** 	
	Name : positiveGetTagClassName
	Description : Create an object of the TagInfo and call getTagClassName()
	Result : Should return the class name of the tag("examples.FooTag" here)
	**/  
%>	 

<%	
	TagInfo ti = new TagInfo("foo","examples.FooTag",null,"info",null,null,null);
	ti.getTagClassName();
	out.println(ti.getTagClassName());
%>
</body>
</html>