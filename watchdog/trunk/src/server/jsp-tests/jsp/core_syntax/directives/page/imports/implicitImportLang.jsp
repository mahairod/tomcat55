<html>
<title>implicitImportLang</title>
<body>
<% /**	Name: implicitImportLang
		Description:Use a jsp directive with language ="java" 
			    Do not specify the import attribute.Lang package should
			    be available implicitly.Check for some class in 
			    Lang.
			    
		Result:No error
**/ %>		
<!-- language =java and  check if implicit import works-->



<%  
    String str="sun";
     out.println(str);
    Integer i=new Integer(5);
    String x=i.toString();
    out.println(x);


 %>
 
</body>
</html>