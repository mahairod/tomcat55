<request followRedirects="false" version="1.0" label="GetRootCauseTest" path="/servlet-tests/tests/javax_servlet/ServletException/GetRootCauseTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for getRootCause method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletException/GetRootCauseTest.html" ignoreWhitespace="true" label="Returns the exception that caused this servlet exception., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletExceptionConstructor1Test" path="/servlet-tests/tests/javax_servlet/ServletException/ServletExceptionConstructor1TestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for ServletException() constructor method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletException/ServletExceptionConstructor1Test.html" ignoreWhitespace="true" label="Constructs a new servlet exception, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletExceptionConstructor2Test" path="/servlet-tests/tests/javax_servlet/ServletException/ServletExceptionConstructor2TestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for ServletException(String) constructor method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletException/ServletExceptionConstructor2Test.html" ignoreWhitespace="true" label="A Test for ServletException(String) constructor method, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletExceptionConstructor3Test" path="/servlet-tests/tests/javax_servlet/ServletException/ServletExceptionConstructor3TestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for ServletException(Throwable) constructor method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletException/ServletExceptionConstructor3Test.html" ignoreWhitespace="true" label="Constructs a new servlet exception when the servlet needs to throw an exception and include a message about the 'root cause' exception that interfered with its normal operation, including a description message., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletExceptionConstructor4Test" path="/servlet-tests/tests/javax_servlet/ServletException/ServletExceptionConstructor4TestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for ServletException(String,Throwable) constructor method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletException/ServletExceptionConstructor4Test.html" ignoreWhitespace="true" label="Constructs a new servlet exception when the servlet needs to throw an exception and include a message about the 'root cause' exception that interfered with its normal operation., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

