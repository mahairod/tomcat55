<html>
<title>checkConfig Test </title>
<body>
<% /**	Name:checkConfig
		Description: Checks whether configuration information
			 is being passed to the server page. Also checks whether
			 it returns the initialization parameters as an Enumeration
			 of Strings or as an empty enumeration.
		Result: Returns true twice	
**/ %>				 
<!-- checking for config object state -->
<%= (config instanceof javax.servlet.ServletConfig) %>
<br>
<% 
   if(config.getInitParameterNames()!=null) {
%> true <% }
   else {
%> false <% } %>
</body>
</html>


