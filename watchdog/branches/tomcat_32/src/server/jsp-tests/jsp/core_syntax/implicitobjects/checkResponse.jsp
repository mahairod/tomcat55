<html>
<title>checkResponse</title>
<body>
<% /**	Name:checkResponse
		Description: Checks whether response is an object
			 of type javax.servlet.ServletResponse.The other part 
			 checks whether a character encoding is used by the
			 response object or not
		Result: returns true
**/ %>					 
<!-- checking for response object type -->
<%= (response instanceof javax.servlet.ServletResponse) %>
<br>
<% 
   if(response.getCharacterEncoding()!=null) {
%> true <% }
   else {
%> false <% } %>

</body>
</html>
