<html>
<title>checkApplication Test </title>
<body>
<% /**	Name:checkApplicationTest
		Description: Returns true if application
		 	is of type javax.servlet.ServletContext
		 	else returns false. Then checks for the
		 	a short description about the server-side 
		 	information of the application.
		Result: Returns true twice
**/ %>		 		 	
<!-- checking for application object state -->

<%= (application instanceof javax.servlet.ServletContext) %>

<br>
<% 
   if(application.getServerInfo()!=null) {
%> true <% }
   else {
%> false <% } %>

</body>
</html>


