<html>
<title>checkResponse</title>
<body>
<% /**	Name:checkResponse
		Description: Checks whether response is an object
			 of type javax.servlet.ServletResponse.The other part 
			 verifes that the expected value is returned from
			 response.getCharacterEncoding().
		Result: returns true
**/ %>
<!-- checking for response object type -->
<%= (response instanceof javax.servlet.ServletResponse) %><br>
<% 
   if(response.getCharacterEncoding().equals( "ISO-8859-1" ) ) {
%>true<br><% }
   else {
%>false<br><% } %>

</body>
</html>
