<html>
<title>Positive test for a relative URI</title>
<body>
<% 
	/** 	
	Name : positive_REL_URI
	Description : tests if the correct tld file is found by
	              the server if the URI in taglib directive is
                      a Relative URI
	            
	Result :   No Translation error should ocuur and the appropriate
                   message should be printed

	**/  
%>	 


<%@ taglib uri="/tld_root_relative" prefix="relative" %>

<relative:tldtag uri="Relative URI" />
</body>
</html>
