<request followRedirects="false" version="1.0" label="FlushBufferTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/FlushBufferTestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet writes data in the buffer and flushes it-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/FlushBufferTest.html" ignoreWhitespace="true" label="Forces any content in the buffer to be written to the client specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetBufferSizeTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/GetBufferSizeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.getBufferSize() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/GetBufferSizeTest.html" ignoreWhitespace="true" label="Returns the actual buffer size used for the response., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetOutputStream_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponse/GetOutputStream_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletResponse.getOutputStream() method. We will get a PrintWriter object first and we will try to get an OutPutStream Object. IllegalStateException should be thrown-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/GetOutputStream_1Test.html" ignoreWhitespace="true" label="illegalStateException is thrown if the getWriter method has been called on this response ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetWriter_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponse/GetWriter_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletResponse.getWriter() method. We will get a PrintWriter object first and we will try to get an OutPutStream Object. IllegalStateException should be thrown.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/GetWriter_1Test.html" ignoreWhitespace="true" label="IllegalStateException is thrown if the getOutputStream method has already been called for this response object , specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsCommittedTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/IsCommittedTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.isCommitted() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/IsCommittedTest.html" ignoreWhitespace="true" label="Returns a boolean indicating if the response has been committed., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ResetTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/ResetTestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet writes out to buffer then clears it. Should only get pass message back-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/ResetTest.html" ignoreWhitespace="true" label="Clears any data that exists in the buffer,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Reset_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponse/Reset_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative Test for ServletResponse.reset() method. Commit the response has been committed, and test if this method throws an IllegalStateException.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/Reset_1Test.html" ignoreWhitespace="true" label="Throws IllegalStateException if the response has already been committed, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletResponseGetCharacterEncodingTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/ServletResponseGetCharacterEncodingTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.getCharacterEncoding() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/ServletResponseGetCharacterEncodingTest.html" ignoreWhitespace="true" label="Returns the name of the charset used for the MIME body sent in this response., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetBufferSizeTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/SetBufferSizeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.setBufferSize() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/SetBufferSizeTest.html" ignoreWhitespace="true" label="Sets the preferred buffer size for the body of the response. specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetBufferSize_1Test" path="/servlet-tests/tests/javax_servlet/ServletResponse/SetBufferSize_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletResponse.setBufferSize() method. Invoke setBufferSize method, after the content is written using ServletOutputStream. Test for IllegalStateException error-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/SetBufferSize_1Test.html" ignoreWhitespace="true" label="illegalStateException is thrown if this method is called after content has been written, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetContentLengthTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/SetContentLengthTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.setContentLength() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletResponse/SetContentLengthTest.html" ignoreWhitespace="true" label="Sets the length of the content body in the response, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Content-Length" headerValue="33" label="Sets the length of the content body in the response, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetContentTypeTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/SetContentTypeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.setContentType() method.-->
    <responseHeader headerName="Content-Type" headerValue="text/html" label="Sets the content type of the response being sent to the client., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetLocaleTest" path="/servlet-tests/tests/javax_servlet/ServletResponse/SetLocaleTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletResponse.setLocale() method.-->
    <responseHeader headerName="Content-Language" headerValue="en-US" label="Sets the locale of the response, setting the headers (including the Content-Type's charset) as appropriate., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

