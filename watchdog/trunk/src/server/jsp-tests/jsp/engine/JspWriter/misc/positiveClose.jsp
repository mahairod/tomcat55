<html>
<title>positiveClose</title>
<body>
<%
/*
 Name : positiveClose
 Description : Write something to the stream and close the stream. After closing
 the stream, try to write something into the stream. 
 */
 %>
<!-- this is to test if close method works -->
<!-- we should get 'out' object to be null after closing output -->
<%@ page import="java.io.*;" %>
<% out.println("hello"); %>
<% out.close(); %>
<!-- To report that 'out' is null, we dont have a stream to client available -->
<!- we create a file in the directory where jsp is kept which is seen by javatest->
<%! String dir; %>
<% String path=request.getPathTranslated(); %>
<% if(path!=null) {
                 int where=path.lastIndexOf("positiveClose.jsp"); 
                 dir=path.substring(0,where);
                 }else {
             dir=System.getProperty("user.home");
             }  
                 java.io.File file=new java.io.File(dir+System.getProperty("file.separator")+"positiveClose.err"); 
                 java.io.FileWriter fw=new java.io.FileWriter(file);


   

if(out==null){fw.write("out is null");fw.flush();fw.close(); }
else{fw.write("out is not null");fw.flush();fw.close();} %>

</body>
</html>
