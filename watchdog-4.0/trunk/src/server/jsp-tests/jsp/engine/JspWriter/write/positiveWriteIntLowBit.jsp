<html>
<title>positiveWriteIntLowBit</title>
<body>
<%
/*
 Name : positiveWriteIntLowBit
 Description : Call the method write(int i) in a JSP  page and pass the integer,
 where the upper two bytes are non-zero & lower two bytes constitute a valid ASCII
 character,as an argument.Character corresponding to the value of lower two bytes
 is expected.
*/
%>
<!-- calling the method write(int i) -->
<%
    int i=676397122;
    out.write(i);
%>
</body>
</html>