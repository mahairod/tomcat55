<html>
<title>positiveJSPInitExtends</title>
<body>
<%  /**
       Name : positiveJSPInitExtends
       Description: define a class which implements the HttpJspPage and in the 
	       jsp page use extends directive to make the defined java class
	       as the super class. now we check the result of
	       getServletConfig
       Result: we should get the expected string output
  */ %> 
<!-- to test HttpJspPage implementation -->
<%@ page extends="engine.HttpJspPage.SuperPage" %>
<% javax.servlet.ServletConfig con=getServletConfig(); %>
<% if(con!=null) {
      out.println("it works");
    }else {
      out.println("ServletConfig is null");
    }
%>

</body>
</html>