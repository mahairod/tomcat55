<html>
<title>Positive test for TLD in a Jar file</title>
<body>
<% 
	/** 	
	Name : positive_JAR_URI
	Description : tests if the correct tld file is found by
	              the server if the URI in taglib directive 
                      points to a Jar file
	            
	Result : No Translation error should ocuur and the appropriate
                   message should be printed

	**/  
%>	 


<%@ taglib uri="tld_jar" prefix="jar" %>

<jar:tldtag uri="Jar URI" />
</body>
</html>
