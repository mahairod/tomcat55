<html>
<title>checkPageContext</title>
<body>
<% /**	Name:checkPageContext 
		Description: checking 'page' is of type 
			javax.servlet.jsp.PageContext
		Result: True
**/ %>		
<!-- checking for pageContext object type -->
<%= (pageContext instanceof javax.servlet.jsp.PageContext) %>
</body>
</html>
