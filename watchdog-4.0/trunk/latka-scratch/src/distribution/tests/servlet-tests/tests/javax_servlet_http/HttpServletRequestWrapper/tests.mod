<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperConstructorTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperConstructorTestServlet">
  <validate>
    <!--TEST STRATEGY: Construct a request object wrapping the given request.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperConstructorTest.html" ignoreWhitespace="true" label="A HttpServletRequestWrapper object should be returned when the request object is passed into the constructor. Java Specification v2.3 Sec 14." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetContextPathTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetContextPathTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetContextPathTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getContextPath() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetCookiesTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetCookiesTestServlet">
  <requestHeader headerName="Cookie" headerValue="BestLanguage=Java" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetCookiesTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getCookies() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetDateHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetDateHeaderTestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="Sat, 01 Jan 2000 00:00:01 GMT" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetDateHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getDateHeader(String name) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetHeaderNamesTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetHeaderNamesTestServlet">
  <requestHeader headerName="Accept-Language" headerValue="en-us" />
  <requestHeader headerName="Accept-Language2" headerValue="ga-us" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetHeaderNamesTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getHeaders(String name) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetHeadersTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetHeadersTestServlet">
  <requestHeader headerName="MyHeader" headerValue="myheadervalue1" />
  <requestHeader headerName="MyHeader" headerValue="myheadervalue2" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetHeadersTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getHeaders(String name) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetIntHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetIntHeaderTestServlet">
  <requestHeader headerName="MyIntHeader" headerValue="123" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetIntHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getIntHeader(String name) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetMethodTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetMethodTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetMethodTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getMethod() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetPathInfoTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetPathInfoTestServlet/pathinfostring1/pathinfostring2">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetPathInfoTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getPathInfo() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetPathTranslatedTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetPathTranslatedTestServlet/javax_servlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetPathTranslatedTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getPathTranslated() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetQueryStringTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetQueryStringTestServlet?language=Java">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetQueryStringTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getQueryString() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetRequestURITest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRequestURITestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRequestURITest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getRequestURI() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetServletPathTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetServletPathTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetServletPathTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getServletPath() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetHeaderTestServlet">
  <requestHeader headerName="User-Agent" headerValue="Mozilla/4.0" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getHeader(String name) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetAuthTypeTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetAuthTypeTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetAuthTypeTest.html" ignoreWhitespace="true" label="Test if method returns the default getAuthType on the wrapped request object,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetRemoteUserTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRemoteUserTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRemoteUserTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getRemoteUser() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetRequestedSessionIdTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRequestedSessionIdTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRequestedSessionIdTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getRequestedSessionId() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetRequestURLTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRequestURLTestServlet">
  <requestHeader headerName="Cookie" headerValue="prefix=http" />
  <requestHeader headerName="Cookie" headerValue="server=${host}" />
  <requestHeader headerName="Cookie" headerValue="port=${port}" />
  <requestHeader headerName="Cookie" headerValue="servletpath=_servlet-tests_hsreqw_HttpServletRequestWrapperGetRequestURLTest" />
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetRequestURLTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getRequestURL() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetSessionTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetSessionTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's request has been wrapped. The wrapper object writes a message to a static log file and calls the wrapped objects method. Servlet the tests the returned value and returns the result of the test plus the contents of the static log file.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetSessionTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getSession() on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperGetSessionBooleanTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetSessionBooleanTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequestWrapper.getSession(boolean) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperGetSessionBooleanTest.html" ignoreWhitespace="true" label="Test for default behavior of this method to return getSession(boolean) on the wrapped request object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperIsRequestedSessionIdFromCookie_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperIsRequestedSessionIdFromCookie_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequestWrapper.isRequestedSessionIdFromCookie() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperIsRequestedSessionIdFromCookie_01Test.html" ignoreWhitespace="true" label="Test for a false return from this method on the wrapped request object, specified in the Java Servlet Pages Specification v2.3 Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperIsRequestedSessionIdFromURL_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperIsRequestedSessionIdFromURL_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequestWrapper.isRequestedSessionIdFromURL() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperIsRequestedSessionIdFromURL_01Test.html" ignoreWhitespace="true" label="Test for a false return from this method on the wrapped request object, specified in the Java Servlet Pages Specification v2.3 Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletRequestWrapperIsRequestedSessionIdValid_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperIsRequestedSessionIdValid_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequestWrapper.isRequestedSessionIdValid() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequestWrapper/HttpServletRequestWrapperIsRequestedSessionIdValid_01Test.html" ignoreWhitespace="true" label="Test for a false return from this method on the wrapped request object, specified in the Java Servlet Pages Specification v2.3 Sec 14" />
  </validate>
</request>

