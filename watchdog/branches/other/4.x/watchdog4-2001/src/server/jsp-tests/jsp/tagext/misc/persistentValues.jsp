<html>
<title>Persistent Values of instance variables inside tags</title>
<body>
<% 
	/** 	
	Name : persistentValues
	Description : Tests to see if the values of instance variaables remains                       persistent .
		     
	            
	Result :  Message should be printed  at the beginning of each tag starting from the first tag and then at the end of each tag starting from the innnermost tag
	**/  
%>	 


<%@ taglib uri="/TestLib.tld" prefix="test"  %>
<test:persistence tagid="one" >
<test:persistence tagid="two" >
<test:persistence tagid="three" >
</test:persistence>
</test:persistence>
</test:persistence>

</body>
</html>
