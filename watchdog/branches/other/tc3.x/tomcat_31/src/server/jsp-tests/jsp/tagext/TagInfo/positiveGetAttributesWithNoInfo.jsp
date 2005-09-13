<html>
<title>positiveGetAttributesWithNoInfo</title>
<body>
<% 
	/** 	
	Name : positiveGetAttributesWithNoInfo
	Description : Create a TagInfo object and call getAttribute() method
	Result : A null is returned since no attribute is set
	**/  
%>	 

<!-- Calling getAttribute() method without any attributes set -->
<%			
	TagInfo ti = new TagInfo("foo","examples.FooTag","To Browser","info",null,null,null);
	out.println(ti.getAttributes());
%>
</body>
</html>