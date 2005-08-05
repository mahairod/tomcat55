<request followRedirects="false" version="1.0" label="DoDestroyedTest" path="/servlet-tests/tests/javax_servlet/Servlet/DoDestroyedTestServlet">
  <validate>
    <!--TEST STRATEGY: Testing that destroy method is not called during service method execution-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Servlet/DoDestroyedTest.html" ignoreWhitespace="true" label="Called by the servlet container to indicate to a servlet that the servlet is being taken out of service, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.destroy() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="DoInit1Test" path="/servlet-tests/tests/javax_servlet/Servlet/DoInit1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for the init method. We will throw UnavailableException from inside init.The Servlet should not be initialized-->
    <statusCode code="503" label="Called by the servlet container to indicate to a servlet that the servlet is being placed into service., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.init() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="DoInit2Test" path="/servlet-tests/tests/javax_servlet/Servlet/DoInit2TestServlet">
  <validate>
    <!--TEST STRATEGY: Inside CoreServletTest, which is the parent servlet, we are implementing init() and setting a boolean variable to true. We'll check for the variables here in the DoInit2Test-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Servlet/DoInit2Test.html" ignoreWhitespace="true" label="Called by the servlet container to indicate to a servlet that the servlet is being placed into service., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.init() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="DoServletConfigTest" path="/servlet-tests/tests/javax_servlet/Servlet/DoServletConfigTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and test for the getServletConfig() method to be a non-null value and an initial paramter can be retrieved-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Servlet/DoServletConfigTest.html" ignoreWhitespace="true" label="Returns a ServletConfig object, which contains initialization and startup parameters for this servlet., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.getServletConfig() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="DoServletInfoTest" path="/servlet-tests/tests/javax_servlet/Servlet/DoServletInfoTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and test that information is returned-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Servlet/DoServletInfoTest.html" ignoreWhitespace="true" label="Returns information about the servlet, such as author, version, and copyright., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.getServletInfo() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="PUTest" path="/servlet-tests/tests/javax_servlet/Servlet/PUTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet, throw UnavailableException and test if isPermanent() method is true-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Servlet/PUTest.html" ignoreWhitespace="true" label="Servlet lifecycle test, check if UnavailableException.isPermanent() is true, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="DoServiceTest" path="/servlet-tests/tests/javax_servlet/Servlet/DoServiceTestServlet">
  <validate>
    <!--TEST STRATEGY: Inside CoreServletTest, which is the parent servlet, we will override init method and assign some value to the String. We'll check for that value in the service method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Servlet/DoServiceTest.html" ignoreWhitespace="true" label="Called by the servlet container to allow the servlet to respond to a request, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.service() method" />
  </validate>
</request>

