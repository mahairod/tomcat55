<html>
<title>Positive test for an absolute URI</title>
<body>
<% 
	/** 	
	Name : positive_ABS_URI
	Description : tests if the correct tld file is found by
	              the server if the URI in taglib directive is
                      an Absolute URI
	            
	Result :   No Translation error should ocuur and the appropriate
                   message should be printed
	**/  
%>	 


<%@ taglib uri="http://java.apache.org/tlds/tld-absolute" prefix="absolute" %>

<absolute:tldtag uri="Absolute URI" />
</body>
</html>
