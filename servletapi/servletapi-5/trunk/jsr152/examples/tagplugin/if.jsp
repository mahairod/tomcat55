<html>
  <head>
    <title>Tag Plugin Examples: if</title>
  </head>
  <body>
    <h1>Tag Plugin Examples - &lt;c:if></h1>

    <hr>
    </br>
    <a href="notes.html">Plugin Introductory Notes<font <font color="#0000FF"></a>
    <br/>
    <a href="howto.html">Brief Instructions for Wrieting Plugins<font color="#0000FF"></a>
    <br/> <br/>
    <hr>

    <font color="#000000"/>
    </br>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <h3>Set the test result to a variable</h3>
    <c:if test="${1==1}" var="theTruth" scope="session"/>
    The result of testing for (1==1) is: ${theTruth}

    <h3>Conditionally execute the body</h3>
    <c:if test="${2>0}">
	It's true that (2>0)!
    </c:if>
  </body>
</html> 
