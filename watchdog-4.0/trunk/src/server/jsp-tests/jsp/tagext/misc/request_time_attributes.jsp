<html>

<title>Request time attributes evaluation</title>
<body>
<% /**	Name:request_time_attributes
		Description: This tests the order of evaluation of 
                request time attributes in actions. The order of evaluation
	        is from left to right.
		
		Result:  Should calculate the expressions from left to right
 			 and return the correct values
		
**/ %>			 			 

<%! int i=10; %>
<%@ taglib  uri="/Testlib.tld"  prefix="request" %>
<request:ReqTime attr1="<%= i %>" />
<request:ReqTime attr1='<%= i++ %>' />
<request:ReqTime attr1="<%= i++ %>" attr2="<%= i++ %>"  />
<request:ReqTime attr1="<%= ++i %>" attr2="<%= ++i %>" attr3="<%= ++i %>"  />
<request:ReqTime attr1="<%= --i %>" attr2="<%= --i %>" attr3="<%= --i %>"  />

</body>
</html>
 
