<html>
<title>positiveBeanNameClass</title>
<body>
<% /** 	Name : positiveBeanNameClass
	Description : Declaring the bean with bean name as a class file 
	Result :We should get page output without error
**/ %>	 
<!-- Declaring the bean with bean name as a class file -->
<jsp:useBean id="myBean" scope="request" beanName="core_syntax.beantests.useBean.NewCounter" 
type="core_syntax.beantests.useBean.NewCounter" />
<!-- accessing the bean thru a scriptlet -->
<%
 out.println(myBean.getCount());
%>
</body>
</html> 
