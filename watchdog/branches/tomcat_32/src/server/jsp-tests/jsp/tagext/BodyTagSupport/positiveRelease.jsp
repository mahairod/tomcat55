<html>
<title>positiveRelease</title>
<body>
<% 
	/** 	
	Name : positiveRelease
	Description : Pass an attribute to the test tag and
	              print it in the release() method,indicating
	              that the method is called.
	Result :   The attribute passed,should be printed.
	**/  
%>	 


<%@ taglib uri="http://java.apache.org/tomcat/examples-taglib" prefix="eg" %>

<% try { %>
<eg:test toBrowser="true" att1="release">
Test For <% throw new java.lang.Exception(); %></eg:test>
<% } catch (java.lang.Exception e) {} %>
</body>
</html>
