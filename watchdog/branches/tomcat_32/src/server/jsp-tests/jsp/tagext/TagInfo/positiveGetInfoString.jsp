<%@ page import="javax.servlet.jsp.tagext.*" %><html>
<body>
<% /** 	Name : positiveGetInfoString
	Description : Create a TagInfo object and call the getInfoString() method
	Result : Should return the value of the infostring passed to the constructor 
	         as part of the html
**/  %>	 

<%
   TagInfo tf=new TagInfo("foo","examples.FooTag",null,"info",null,null,null); 
 
   out.println(tf.getInfoString());
    
 %> 
</body>    
</html>  
  