<request followRedirects="false" version="1.0" label="GetUnavailableSecondsTest" path="/servlet-tests/tests/javax_servlet/UnavailableException/GetUnavailableSecondsTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for UnavailableException.getUnavailableSeconds() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/UnavailableException/GetUnavailableSecondsTest.html" ignoreWhitespace="true" label="Returns the number of seconds the servlet expects to be temporarily unavailable., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsPermanentTest" path="/servlet-tests/tests/javax_servlet/UnavailableException/IsPermanentTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for UnavailableException.isPermanent() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/UnavailableException/IsPermanentTest.html" ignoreWhitespace="true" label="Returns a boolean indicating whether the servlet is permanently unavailable., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="UnavailableException_Constructor1Test" path="/servlet-tests/tests/javax_servlet/UnavailableException/UnavailableException_Constructor1TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for UnavailableException(String mesg). It constructs an UnavailabaleException object for the specified servlet. This constructor tests for permanent unavailability-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/UnavailableException/UnavailableException_Constructor1Test.html" ignoreWhitespace="true" label="Constructs a new exception with a descriptive message indicating that the servlet is permanently unavailable., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="UnavailableException_Constructor2Test" path="/servlet-tests/tests/javax_servlet/UnavailableException/UnavailableException_Constructor2TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for UnavailableException(String mesg). It constructs an UnavailabaleException object for the specified servlet. This constructor tests for temporarily unavailability-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/UnavailableException/UnavailableException_Constructor2Test.html" ignoreWhitespace="true" label="Constructs a new exception with a descriptive message indicating that the servlet is temporarily unavailable and giving an estimate of how long it will be unavailable., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

