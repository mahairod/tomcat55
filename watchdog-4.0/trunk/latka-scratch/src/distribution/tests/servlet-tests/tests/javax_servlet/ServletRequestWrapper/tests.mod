<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetAttributeNamesTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetAttributeNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetAttributeNamesTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call getAttributeNames() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetAttributeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call getAttribute(String name)on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetAttribute_01Test" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetAttribute_01TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests that null is returned for a get of an attribute that does not exist and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetAttribute_01Test.html" ignoreWhitespace="true" label="The default behavior of this method is to call getAttribute(String name)on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetCharacterEncodingTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetCharacterEncodingTestServlet">
  <requestHeader headerName="Content-Type" headerValue="text/plain; charset=ISO-8859-1" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetCharacterEncodingTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getCharacterEncoding() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetContentLengthTest" method="post" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetContentLengthTestServlet">
  <requestHeader headerName="Content-Type" headerValue="text/plain" />
  <!--This request body was generated from the GTest attribute 'content'.-->
  <requestBody>calling ServletRequestWrapperGetContentLengthTest</requestBody>
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetContentLengthTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getContentLength() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetContentTypeTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetContentTypeTestServlet">
  <requestHeader headerName="Content-Type" headerValue="text/plain" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetContentTypeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getContentLength() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetInputStreamTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetInputStreamTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetInputStreamTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getInputStream() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetInputStream_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetInputStream_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then attempts to get a reader object after one has already been gotten, then the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetInputStream_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getInputStream() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetLocaleTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetLocaleTestServlet">
  <requestHeader headerName="Accept-Language" headerValue="en-us" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetLocaleTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getLocale() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetLocalesTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetLocalesTestServlet">
  <requestHeader headerName="Accept-Language" headerValue="en-US,en-GB" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetLocalesTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getLocales() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameterMapTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameterMapTestServlet?BestLanguage=Java&amp;BestJSP=Java2">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameterMapTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameterMap() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameterNamesTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameterNamesTestServlet?BestLanguage=Java&amp;BestJSP=Java2">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameterNamesTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameterNames() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameterNames_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameterNames_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests that no paramters are returned if none are set and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameterNames_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameterNames() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameterTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameterTestServlet?BestLanguage=Java">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameterTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameter(String) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameterValuesTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameterValuesTestServlet?Containers=JSP&amp;Containers=Servlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameterValuesTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameterValues(String) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameterValues_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameterValues_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests that a null is returned for a non existing item and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameterValues_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameterValues(String) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetParameter_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetParameter_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests that null is returned for a non-existing parameter and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetParameter_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getParameter(String) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetProtocolTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetProtocolTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetProtocolTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getProtocol() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetReaderTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetReaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetReaderTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getReader() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetReader_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetReader_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests that an exception is thrown when an attempt to get a reader after one has altready been gotten and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetReader_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getReader() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetRemoteAddrTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetRemoteAddrTestServlet?Address=${client-IP}">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetRemoteAddrTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getRemoteAddr() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetRemoteHostTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetRemoteHostTestServlet?Address=${client-host}&amp;Address=${client-IP}">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetRemoteHostTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getRemoteHost() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetRequestDispatcherTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetRequestDispatcherTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetRequestDispatcherTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getRequestDispatcher() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetSchemeTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetSchemeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetSchemeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getScheme() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetServerNameTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetServerNameTestServlet?hostname=${host}">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetServerNameTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getServerName() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperGetServerPortTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperGetServerPortTestServlet?port=${port}">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperGetServerPortTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getServerPort() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperIsSecureTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperIsSecureTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperIsSecureTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return isSecure() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperRemoveAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperRemoveAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperRemoveAttributeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return removeAttribute() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestWrapperSetAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletRequestWrapper/filtered/ServletRequestWrapperSetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequestWrapper/ServletRequestWrapperSetAttributeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return setAttribute() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

