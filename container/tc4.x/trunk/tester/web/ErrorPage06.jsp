<%@ page contentType="text/plain" %>ErrorPage06 PASSED - JSP
<%
  Exception e = (Exception)
   request.getAttribute("javax.servlet.error.exception");
  out.println("EXCEPTION:  " + e);
  Class et = (Class)
   request.getAttribute("javax.servlet.error.exception_type");
  out.println("EXCEPTION_TYPE:  " + et.getName());
  String m = (String)
   request.getAttribute("javax.servlet.error.message");
  out.println("MESSAGE:  " + m);
  String ru = (String)
   request.getAttribute("javax.servlet.error.request_uri");
  out.println("REQUEST_URI:  " + ru);
  String sn = (String)
   request.getAttribute("javax.servlet.error.servlet_name");
  out.println("SERVLET_NAME:  " + sn);
%>
