<request followRedirects="false" version="1.0" label="DestroyTest" path="/servlet-tests/tests/javax_servlet/GenericServlet/DestroyTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a GenericServlet and take out of service using destroy method-->
    <statusCode label="Called by the servlet container to indicate to a servlet that the servlet is being taken out of service., specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.Destroy() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletConfigTest" path="/servlet-tests/tests/javax_servlet/GenericServlet/GetServletConfigTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a GenericServlet and check for its ServletConfig object existence-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/GenericServlet/GetServletConfigTest.html" ignoreWhitespace="true" label="Returns this servlet's ServletConfig object, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.ServletConfig() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletContextTest" path="/servlet-tests/tests/javax_servlet/GenericServlet/GetServletContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a GenericServlet and check for its ServletContext object existence-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/GenericServlet/GetServletContextTest.html" ignoreWhitespace="true" label="Returns this servlet's ServletContext object, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.ServletContext() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletInfoTest" path="/servlet-tests/tests/javax_servlet/GenericServlet/GetServletInfoTestServlet">
  <validate>
    <!--TEST STRATEGY: Create a GenericServlet and check for its ServletInfo object values-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/GenericServlet/GetServletInfoTest.html" ignoreWhitespace="true" label="Returns this servlet's ServletInfo object, specified in the Java Servlet Pages Specification v2.3, Sec 16 - javax.servlet.GenericServlet.ServletContext() method" />
  </validate>
</request>

