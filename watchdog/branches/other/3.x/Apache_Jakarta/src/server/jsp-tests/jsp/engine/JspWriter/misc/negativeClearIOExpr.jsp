<html>
<title>negativeClearIOExpr</title>
<body>
<%
/*
 Name : negativeClearIOExpr
 Description : Before using the clear() method, close the stream. So an IO error
 should happen for the current situation.
*/
%>
<!- this is to test if clear method throws IOException if stream is closed ->
<%@ page import="java.io.*;" %>
<% out.println("hello"); %>
<% out.close(); %>
<!-- To report that 'out' is null, we dont have a stream to client available -->
<!- we create a file  which is seen by javatest->  
<%! String dir; %>
<% String path=request.getPathTranslated(); %>
<% if(path!=null) {
                  int where=path.lastIndexOf("negativeClearIOErr");
                  dir=path.substring(0,where); 
                  }else {
                  dir=System.getProperty("user.home");
                  }
             
                  java.io.File file=new java.io.File(dir+System.getProperty("file.separator")+"negativeClearIOErr.err"); 
                  java.io.FileWriter fw=new java.io.FileWriter(file);
                  
%>

<!-- now lets try to clear the stream -->
<% try { out.clear(); fw.write("no IOException"); fw.flush();fw.close();
}catch(java.io.IOException ioe) { %>
<% fw.write("we got IOException"); fw.flush();fw.close(); }%>
</body>
</html>
