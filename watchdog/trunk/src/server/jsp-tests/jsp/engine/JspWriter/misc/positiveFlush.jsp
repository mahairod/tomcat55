<html>
<title>positiveFlush</title>
<body>
<% /**
 Name : positiveFlush
 Description : Write some thing into the buffer and then call the flush() method.
 Expected to flush the buffer to the output stream.
 **/ %>
<!-- this is to test if flush() method works -->
<% out.println("hello"); %>
<% out.flush(); %>



</body>
</html>