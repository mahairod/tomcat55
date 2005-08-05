<html>
<title>positiveImport</title>
<body>
<% /**	Name: positiveImport
		Description:Use a jsp directive with language ="java" and a
			  valid import attribute. Use the imported package in the
			  page. 
		Result:No error
**/ %>		
<!-- language =java and we import a java package to check if import works-->
<%@ page language="java" import="java.util.Properties" %>

<%  Properties props=new Properties(); 
    props.put("name","harry");
    String name=(String)props.getProperty("name");
    out.println(name);
 %>
 
</body>
</html>