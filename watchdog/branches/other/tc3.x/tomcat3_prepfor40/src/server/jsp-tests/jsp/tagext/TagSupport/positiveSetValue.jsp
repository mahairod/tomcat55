<html>
<title>positiveSetValue</title>
<body>
<%
    /*
    
    Name:  positiveSetValue
    Description: Create a TagSupport object and set values using
                  the setValue() method.Call the getValue() method
                  and print the contents.
     Result:     The values which were set should be printed.             
 */
 
 %>   



<%

	TagSupport ts = new TagSupport();
	
	ts.setValue("Color1","red");
	ts.setValue("Color2","green");

	for(int i=1;i<=2;i++) {
		out.println("Colors are  " + ts.getValue("Color"+i));

%> 
<br> <% }
%>

</body>
</html>