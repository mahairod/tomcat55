<request followRedirects="false" version="1.0" label="ServletResponseWrapperFlushBufferTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperFlushBufferTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperFlushBufferTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call flushBuffer() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperGetBufferSizeTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetBufferSizeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetBufferSizeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getBufferSize() on the wrapped response object , specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperGetCharacterEncodingTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetCharacterEncodingTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetCharacterEncodingTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getCharacterEncoding() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperGetOutputStream_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetOutputStream_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetOutputStream_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getOutputStream() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperGetWriter_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetWriter_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetWriter_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to return getWriter() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperIsCommittedTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperIsCommittedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperIsCommittedTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return isCommitted() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperResetBufferTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperResetBufferTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperResetBufferTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call resetBuffer() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Content-Type" headerValue="text/html" label="The default behavior of this method is to call resetBuffer() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperResetTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperResetTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <statusCode label="The default behavior of this method is to call reset() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperResetTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call reset() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperReset_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperReset_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The servlet attempts to reset the buffer after it has already been flushed. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperReset_1Test.html" ignoreWhitespace="true" label="When the response has been committed, this method throws an IllegalStateException, specified in the Java Servlet Pages Specification v2.3, Sec 14." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperSetBufferSizeTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetBufferSizeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetBufferSizeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call setBufferSize(int size) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperSetBufferSize_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetBufferSize_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetBufferSize_1Test.html" ignoreWhitespace="true" label="The default behavior of this method is to call setBufferSize(int size) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperSetContentLengthTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetContentLengthTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetContentLengthTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call setContentLength(int len) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Content-Length" headerValue="106" label="The default behavior of this method is to call setContentLength(int len) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperSetContentTypeTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetContentTypeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetContentTypeTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call setContentType(String type) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Content-Type" headerValue="text/html" label="The default behavior of this method is to call setContentType(String type) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperSetLocaleTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetLocaleTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperSetLocaleTest.html" ignoreWhitespace="true" label="The default behavior of this method is to call setLocale(Locale loc) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Content-Language" headerValue="en-US" label="The default behavior of this method is to call setLocale(Locale loc) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseWrapperGetLocaleTest" path="/servlet-tests/tests/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetLocaleTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet then tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponseWrapper/ServletResponseWrapperGetLocaleTest.html" ignoreWhitespace="true" label="The default behavior of this method is to return getLocale() on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

