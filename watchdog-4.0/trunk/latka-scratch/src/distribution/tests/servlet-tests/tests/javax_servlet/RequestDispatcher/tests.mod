<request followRedirects="false" version="1.0" label="ForwardTest" path="/servlet-tests/tests/javax_servlet/RequestDispatcher/ForwardTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet, get its RequestDispatcher and use it to forward to a servlet-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/RequestDispatcher/ForwardTest.html" ignoreWhitespace="true" label="Forwards a request from a servlet to another resource (servlet, JSP file, or HTML file) on the server, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.RequestDispatcher.ForwardTest() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Forward_1Test" path="/servlet-tests/tests/javax_servlet/RequestDispatcher/Forward_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for RequestDispatcher.forward() method. Create a servlet, print a string to the buffer, flush the buffer to commit the string, get its RequestDispatcher and use it to forward to a servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/RequestDispatcher/Forward_1Test.html" ignoreWhitespace="true" label="Throws a java.lang.IllegalStateException, if the response was already committed, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.RequestDispatcher.ForwardTest() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IncludeTest" path="/servlet-tests/tests/javax_servlet/RequestDispatcher/IncludeTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet, get its RequestDispatcher and use it to include a servlet-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/RequestDispatcher/IncludeTest.html" ignoreWhitespace="true" label="Includes the content of a resource (servlet, JSP page, HTML file) in the response, enabling programmatic server-side includes., specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.RequestDispatcher.Include() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Include_1Test" path="/servlet-tests/tests/javax_servlet/RequestDispatcher/Include_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for RequestDispatcher.include() test. Create a servlet, set its Content-Type to be 'text/plain', get its RequestDispatcher and use it to include a servlet. The included servlet tries to change the Content-Type to be text/html. Test at the client side for correct Content-Type.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/RequestDispatcher/Include_1Test.html" ignoreWhitespace="true" label="The included servlet cannot change the response status code or set headers; any attempt to make a change is ignored. The request and response parameters must be the same objects as were passed to the calling servlet's service method., specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.RequestDispatcher.Include() method" />
    <responseHeader headerName="Content-Type" headerValue="text/plain" label="The included servlet cannot change the response status code or set headers; any attempt to make a change is ignored. The request and response parameters must be the same objects as were passed to the calling servlet's service method., specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.RequestDispatcher.Include() method" />
  </validate>
</request>

