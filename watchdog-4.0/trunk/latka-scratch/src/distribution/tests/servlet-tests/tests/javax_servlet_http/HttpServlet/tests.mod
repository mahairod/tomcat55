<request followRedirects="false" version="1.0" label="HttpServletDoDestroyedTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDoDestroyedTestServlet">
  <validate>
    <!--TEST STRATEGY: Testing that destroy method is not called during service method execution-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletDoDestroyedTest.html" ignoreWhitespace="true" label="Called by the servlet container to indicate to a servlet that the servlet is being taken out of service, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.destroy() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletDoInit1Test" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDoInit1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for the init method. We will throw UnavailableException from inside init.The Servlet should not be initialized-->
    <statusCode code="503" label="Called by the servlet container to indicate to a servlet that the servlet is being placed into service., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.init() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletDoInit2Test" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDoInit2TestServlet">
  <validate>
    <!--TEST STRATEGY: Inside CoreServletTest, which is the parent servlet, we are implementing init() and setting a boolean variable to true. We'll check for the variables here in the HttpServletDoInit2Test-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletDoInit2Test.html" ignoreWhitespace="true" label="Called by the servlet container to indicate to a servlet that the servlet is being placed into service., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.init() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletDoServletConfigTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDoServletConfigTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and test for the getServletConfig() method to be a non-null value and an initial paramter can be retrieved-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletDoServletConfigTest.html" ignoreWhitespace="true" label="Returns a ServletConfig object, which contains initialization and startup parameters for this servlet., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.getServletConfig() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletDoServletInfoTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDoServletInfoTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and test that information is returned-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletDoServletInfoTest.html" ignoreWhitespace="true" label="Returns information about the servlet, such as author, version, and copyright., specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.getServletInfo() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletDoServiceTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDoServiceTestServlet">
  <validate>
    <!--TEST STRATEGY: Inside HttpServletCoreServletTest, which is the parent servlet, we will override init method and assign some value to the String. We'll check for that value in the service method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletDoServiceTest.html" ignoreWhitespace="true" label="Called by the servlet container to allow the servlet to respond to a request, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Servlet.service() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletPUTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletPUTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet, throw UnavailableException and test if isPermanent() method is true-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletPUTest.html" ignoreWhitespace="true" label="Servlet lifecycle test, check if UnavailableException.isPermanent() is true, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletDestroyTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletDestroyTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and take out of service using destroy method-->
    <statusCode label="Called by the servlet container to indicate to a servlet that the servlet is being taken out of service., specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.Destroy() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletGetServletConfigTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletGetServletConfigTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and check for its ServletConfig object existence-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletGetServletConfigTest.html" ignoreWhitespace="true" label="Returns this servlet's ServletConfig object, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.ServletConfig() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletGetServletContextTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletGetServletContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and check for its ServletContext object existence-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletGetServletContextTest.html" ignoreWhitespace="true" label="Returns this servlet's ServletContext object, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.ServletContext() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletGetServletInfoTest" path="/servlet-tests/tests/javax_servlet_http/HttpServlet/HttpServletGetServletInfoTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a servlet and check for its ServletInfo object values-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServlet/HttpServletGetServletInfoTest.html" ignoreWhitespace="true" label="Returns this servlet's ServletInfo object, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.ServletContext() method" />
  </validate>
</request>

