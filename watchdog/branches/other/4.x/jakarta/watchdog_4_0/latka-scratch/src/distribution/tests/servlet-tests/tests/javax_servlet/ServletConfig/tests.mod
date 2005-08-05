<request followRedirects="false" version="1.0" label="GetServletConfigInitParameterNamesTest" path="/servlet-tests/tests/javax_servlet/ServletConfig/GetServletConfigInitParameterNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: Set init parameters in the web.xml file and check for the enumerated values in the servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletConfig/GetServletConfigInitParameterNamesTest.html" ignoreWhitespace="true" label="Returns the names of the servlet's initialization parameters as an Enumeration of String objects, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletConfigInitParameterNames_1Test" path="/servlet-tests/tests/javax_servlet/ServletConfig/GetServletConfigInitParameterNames_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletConfig.getInitParameterNames() Do not set init parameters in the web.xml file and check for null value in the servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletConfig/GetServletConfigInitParameterNames_1Test.html" ignoreWhitespace="true" label="If no initialization parameters are set, an empty Enumeration if the servlet has no initialization parameters, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletConfigInitParameterTest" path="/servlet-tests/tests/javax_servlet/ServletConfig/GetServletConfigInitParameterTestServlet">
  <validate>
    <!--TEST STRATEGY: Set init parameters in the web.xml file and check for the value in the servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletConfig/GetServletConfigInitParameterTest.html" ignoreWhitespace="true" label="Returns a String containing the value of the named initialization parameter, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletConfigInitParameter_1Test" path="/servlet-tests/tests/javax_servlet/ServletConfig/GetServletConfigInitParameter_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletConfig.getInitParameter(). Do not set init parameters in the web.xml file and check for null value after in the servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletConfig/GetServletConfigInitParameter_1Test.html" ignoreWhitespace="true" label="If no initialization parameter is set, this method returns a null value, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletConfigGetServletContextTest" path="/servlet-tests/tests/javax_servlet/ServletConfig/ServletConfigGetServletContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Try to get the ServletContext for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletConfig/ServletConfigGetServletContextTest.html" ignoreWhitespace="true" label="Returns a reference to the ServletContext in which the servlet is executing, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletNameTest" path="/servlet-tests/tests/javax_servlet/ServletConfig/GetServletNameTestServlet">
  <validate>
    <!--TEST STRATEGY: Try to get the ServletName for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletConfig/GetServletNameTest.html" ignoreWhitespace="true" label="Returns the name of this servlet instance, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

