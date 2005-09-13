<html>
<title>positiveScriptlet </title>
<body>
<%  /**  Name:positiveScriptlet
         Description:Create a valid scriplet in the
              JSP page.Declare a variable and increment it.          
         Result: The page contents with the incremented
                 value of the variable
**/ %>                
<!--variable i=5 is incremented -->
<%! int i=5; %>
<% i++; %>
<% out.println(i); %>
</body>
</html>
