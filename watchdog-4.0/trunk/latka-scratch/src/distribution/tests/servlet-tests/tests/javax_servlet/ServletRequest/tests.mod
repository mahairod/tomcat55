<request followRedirects="false" version="1.0" label="GetContentLengthTest" method="post" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetContentLengthTestServlet">
  <requestHeader headerName="Content-Type" headerValue="text/plain" />
  <!--This request body was generated from the GTest attribute 'content'.-->
  <requestBody>12345678901234567890</requestBody>
  <validate>
    <!--TEST STRATEGY: A Test For ServletRequest.getContentLength() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetContentLengthTest.html" ignoreWhitespace="true" label="Returns the length, in bytes, of the request body and made available by the input stream, or -1 if the length is not known., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetContentTypeTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetContentTypeTestServlet">
  <requestHeader headerName="Content-Type" headerValue="text/plain" />
  <validate>
    <!--TEST STRATEGY: A Test For ServletRequest.getContentType() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetContentTypeTest.html" ignoreWhitespace="true" label="Returns the MIME type of the body of the request, or null if the type is not known., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetInputStreamTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetInputStreamTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test For ServletRequest.getInputStream() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetInputStreamTest.html" ignoreWhitespace="true" label="Retrieves the body of the request as binary data using a ServletInputStream., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetInputStream_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetInputStream_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet attempts to call getInputStream after getReader has already been called-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetInputStream_1Test.html" ignoreWhitespace="true" label="The exception IllegalStateException will be thrown if the getReader method has already been called for this request, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetLocaleTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetLocaleTestServlet">
  <requestHeader headerName="Accept-Language" headerValue="en-US" />
  <validate>
    <!--TEST STRATEGY: Client sets the locale that it will accept and calls servlet. Servlet verifies it received the correct locale-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetLocaleTest.html" ignoreWhitespace="true" label="Returns the preferred Locale that the client will accept content in, based on the Accept-Language header, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetLocalesTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetLocalesTestServlet">
  <requestHeader headerName="Accept-Language" headerValue="en-US,en-GB" />
  <validate>
    <!--TEST STRATEGY: Client sets the locales that it will accept and calls servlet. Servlet verifies it received the correct locale-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetLocalesTest.html" ignoreWhitespace="true" label="Returns an Enumeration of Locale objects indicating, in decreasing order starting with the preferred locale, the locales that are acceptable to the client based on the Accept-Language header. servlet., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetParameterNamesTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetParameterNamesTestServlet?BestLanguage=Java&amp;BestJSP=Java2">
  <validate>
    <!--TEST STRATEGY: Client passes 2 parameters to the servlet. Servlet verifies it receives the correct parameters.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetParameterNamesTest.html" ignoreWhitespace="true" label="Returns an Enumeration of String objects containing the names of the parameters contained in this request., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetParameterNames_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetParameterNames_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletRequest.getParameterNames() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetParameterNames_1Test.html" ignoreWhitespace="true" label="Returns an empty Enumerationif no input parameter names are given to the servlet., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetParameterTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetParameterTestServlet?BestLanguage=Java">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getParameter(String) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetParameterTest.html" ignoreWhitespace="true" label="Returns the value of a request parameter as a String specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetParameterValuesTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetParameterValuesTestServlet?Containers=JSP&amp;Containers=Servlet">
  <validate>
    <!--TEST STRATEGY: Client sends a single parameter that has 2 values to the servlet. Servlet verifies it received both values.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetParameterValuesTest.html" ignoreWhitespace="true" label="Returns an array of String objects containing all of the values the given request parameter has, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetParameterValues_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetParameterValues_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletRequest.getParameterValues() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetParameterValues_1Test.html" ignoreWhitespace="true" label="Returns null as the parameter does not exist., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetParameter_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetParameter_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletRequest.getParameter() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetParameter_1Test.html" ignoreWhitespace="true" label="Returns null as the parameter does not exist., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetProtocolTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetProtocolTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getProtocol() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetProtocolTest.html" ignoreWhitespace="true" label="Returns the name and version of the protocol the request uses in the form protocol/majorVersion.minorVersion, for example, HTTP/1.1., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetReaderTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetReaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Get an InputStream object using ServletRequest.getInputStream()-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetReaderTest.html" ignoreWhitespace="true" label="Retrieves the body of the request as character data using a BufferedReader., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetReader_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetReader_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletRequest.getReader() method. Get an InputStream object using ServletRequest.getInputStream() then try to get the Reader Object. An IllegalStateException should be thrown-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetReader_1Test.html" ignoreWhitespace="true" label="Retrieves the body of the request as character data using a BufferedReader., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRemoteAddrTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetRemoteAddrTestServlet?Address=${client-IP}">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getRemoteAddress() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetRemoteAddrTest.html" ignoreWhitespace="true" label="Returns the Internet Protocol (IP) address of the client that sent the request., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRemoteHostTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetRemoteHostTestServlet?Address=${client-host}&amp;Address=${client-IP}">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getRemoteHost() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetRemoteHostTest.html" ignoreWhitespace="true" label="Returns the fully qualified name of the client that sent the request, or the IP address of the client if the name cannot be determined., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetSchemeTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetSchemeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getScheme() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetSchemeTest.html" ignoreWhitespace="true" label="Returns the name of the scheme used to make this request, for example, http, https, or ftp.., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServerNameTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetServerNameTestServlet?hostname=${host}">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getServerName() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetServerNameTest.html" ignoreWhitespace="true" label="Returns the host name of the server that received the request specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServerPortTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/GetServerPortTestServlet?port=${port}">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getServerPort() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/GetServerPortTest.html" ignoreWhitespace="true" label="Returns the port number on which this request was received., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestGetAttributeNamesTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestGetAttributeNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test For ServletRequest.getAttributeNames() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestGetAttributeNamesTest.html" ignoreWhitespace="true" label="Returns an Enumeration containing the names of the attributes available to this request specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestGetAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestGetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test For ServletRequest.getAttributeName(String) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestGetAttributeTest.html" ignoreWhitespace="true" label="Returns the value of the named attribute as an Object specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestGetAttribute_01Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestGetAttribute_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A Test For ServletRequest.getAttributeName(String) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestGetAttribute_01Test.html" ignoreWhitespace="true" label="Returns null if no attribute of the given name exists., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestGetCharacterEncodingTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestGetCharacterEncodingTestServlet">
  <requestHeader headerName="Content-Type" headerValue="text/plain; charset=ISO-8859-1" />
  <validate>
    <!--TEST STRATEGY: Servlet verifies is receives the default encoding of IS0-8858-1 method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestGetCharacterEncodingTest.html" ignoreWhitespace="true" label="Returns the name of the character encoding used in the body of this request encoding, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestGetCharacterEncoding_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestGetCharacterEncoding_1TestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet verifies it receives a null result-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestGetCharacterEncoding_1Test.html" ignoreWhitespace="true" label="Returns a null if the request does not specify a character encoding, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestSetCharacterEncoding_1Test" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestSetCharacterEncoding_1TestServlet">
  <validate>
    <!--TEST STRATEGY: servlet attempt to set an invalid encoding and exception should be thrown-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestSetCharacterEncoding_1Test.html" ignoreWhitespace="true" label="Throws java.io.UnsupportedEncodingException if the encoding specified is not valid encoding, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestGetRequestDispatcherTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestGetRequestDispatcherTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for ServletRequest.getRequestDispatcher() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestGetRequestDispatcherTest.html" ignoreWhitespace="true" label="Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletRequestSetAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletRequest/ServletRequestSetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet sets an attribute and then verifies it can be read back-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletRequest/ServletRequestSetAttributeTest.html" ignoreWhitespace="true" label="Stores an attribute in this request, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

