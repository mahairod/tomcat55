<html>
  <head>
    <title>Tag Examples - choose</title>
  </head>
  <body>
    <h1>Tag Plugin Examples - &lt;c:choose></h1>

    <hr>
    </br>
    <a href="notes.html">Plugin Introductory Notes<font <font color="#0000FF"></
a>
    <br/>
    <a href="howto.html">Brief Instructions for Writing Plugins<font color="#000
0
FF"></a>
    <br/> <br/>
    <hr>

    <font color="#000000"/>
    </br>

    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <c:forEach var="index" begin="0" end="4">
      # ${index}: 
      <c:choose>
	<c:when test="${index == 1}">
          One!</br>
	</c:when>
	<c:when test="${index == 4}">
          Four!</br>
	</c:when>
	<c:when test="${index == 3}">
          Three!</br>
	</c:when>
	<c:otherwise>
          Huh?</br>
	</c:otherwise>
      </c:choose>
    </c:forEach>
  </body>
</html> 
