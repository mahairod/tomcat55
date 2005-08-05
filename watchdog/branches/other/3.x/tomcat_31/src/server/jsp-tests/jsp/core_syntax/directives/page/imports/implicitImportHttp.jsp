<html>
<body>
<% /**	Name: implicitImportJsp
	Description:Use  jsp page directive with language ="java" 
		    Do not specify javax.servlet.http in the import attribute
		     That package should be available implicitly.Check 
		     for some class.
			    
	Result:No error
**/ %>		

<!-- language =java and we check if implicit  import works-->

<%@ page language="java"  import ="java.util.*" %>

<%

  HttpUtils hu=new HttpUtils();
  String parse="."+"key1=value1"+"&"+"key2=value2";
  Hashtable ht=hu.parseQueryString(parse);
    
  
%>

<%= ht instanceof java.util.Hashtable %>
  
</body>
</html>