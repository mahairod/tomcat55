<html>
<title>theadsafeFalse</title>
<body>
<!-- this is going to make any client to wait for infinite time -->
<!--if isthreadsafe is false , following condition should happen -->
<!- jsp processor dispatches multiple outstanding requests one at a time-->
<!-- here we keep buffer as 'none' so that output goes directly to the stream-->
<%@ page isThreadSafe="false" buffer="none" %>
<%! int i=2; %>
<% out.println(i); %>
<% for(int j=1;j<i; j++) { i++;  } %>
</body>
</html>
