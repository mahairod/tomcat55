<html>
<title>Positive test for a Non Root Relative URI</title>
<body>
<% 
	/** 	
	Name : positive_NONREL_URI
	Description : tests if the correct tld file is found by
	              the server if the URI in taglib directive is
                      a Non Relative URI
	            
	Result :   No Translation error should ocuur and the appropriate
                   message should be printed

	**/  
%>	 


<%@ taglib uri="tld_nonroot_relative"  prefix="nonrelative" %>

<nonrelative:tldtag uri="Non Relative URI" />
</body>
</html>
