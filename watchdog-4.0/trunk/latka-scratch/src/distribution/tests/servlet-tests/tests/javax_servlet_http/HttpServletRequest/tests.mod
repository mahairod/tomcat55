<request followRedirects="false" version="1.0" label="GetAuthTypeWithoutProtectionTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetAuthTypeWithoutProtectionTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for HttpServletRequest.getAuthType() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetAuthTypeWithoutProtectionTest.html" ignoreWhitespace="true" label="Returns null if the request was not authenticated,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetContextPathTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetContextPathTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for HttpServletRequest.getContextPath() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetContextPathTest.html" ignoreWhitespace="true" label="Returns the portion of the request URI that indicates the context of the request.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetCookiesTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetCookiesTestServlet">
  <requestHeader headerName="Cookie" headerValue="BestLanguage=Java" />
  <validate>
    <!--TEST STRATEGY: A Test for HttpServletRequest.getCookies() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetCookiesTest.html" ignoreWhitespace="true" label="Returns an array containing all of the Cookie objects the client sent with this request.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetCookies_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetCookies_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getCookies() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetCookies_01Test.html" ignoreWhitespace="true" label="Returns returns null if no cookies were sent with the request ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeaderTestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="Sat, 01 Jan 2000 00:00:01 GMT" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeaderTest.html" ignoreWhitespace="true" label="Returns the value of the specified request header as a long value that represents a Date object.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeaderLCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeaderLCaseTestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="sat, 01 jan 2000 00:00:01 gmt" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeaderLCaseTest.html" ignoreWhitespace="true" label="Interprets and returns the lower case value of the specified request header as a long value that represents a Date object.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeaderMxCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeaderMxCaseTestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="SaT, 01 jAn 2000 00:00:01 GmT" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeaderMxCaseTest.html" ignoreWhitespace="true" label="Interprets and returns the mixed case value of the specified request header as a long value that represents a Date object.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeader_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeader_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method. We sent no Header from the client side, so we should get a value of '-1'-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeader_01Test.html" ignoreWhitespace="true" label="If the request did not have a header of the specified name, this method returns -1,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeader_02Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeader_02TestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="java" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method. We sent a Header which is not of 'Date' format so we should get IllegalArgumentException.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeader_02Test.html" ignoreWhitespace="true" label="Throws illegalArgumentException, If the header value can't be converted to a date, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeader_02LCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeader_02LCaseTestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="java" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method. We sent a Header which is not of 'Date' format so we should get IllegalArgumentException.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeader_02LCaseTest.html" ignoreWhitespace="true" label="Throws illegalArgumentException, even when using a lowercase header it's value can't be converted to a date, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDateHeader_02MxCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetDateHeader_02MxCaseTestServlet">
  <requestHeader headerName="If-Modified-Since" headerValue="java" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getDateHeader() method. We sent a Header which is not of 'Date' format so we should get IllegalArgumentException.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetDateHeader_02MxCaseTest.html" ignoreWhitespace="true" label="Throws illegalArgumentException, even when using a mixed case header it's value can't be converted to a date, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeaderNamesTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeaderNamesTestServlet">
  <requestHeader headerName="Cookie" headerValue="BestLanguage=java" />
  <requestHeader headerName="If-Modified-Since" headerValue="Sat, 01 Jan 2000 00:00:01 GMT" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getHeaderNames() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeaderNamesTest.html" ignoreWhitespace="true" label="Returns an enumeration of all the header names this request contains., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeader_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeader_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeader_01Test.html" ignoreWhitespace="true" label="Returns null if the request does not have a header of that name,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeadersTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeadersTestServlet">
  <requestHeader headerName="Accept-Language" headerValue="en-us" />
  <requestHeader headerName="Accept-Language" headerValue="ga-us" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getHeaders() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeadersTest.html" ignoreWhitespace="true" label="Returns all the values of the specified request header as an Enumeration of String objects.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeadersEmptyTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeadersEmptyTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getHeaders() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeadersEmptyTest.html" ignoreWhitespace="true" label="If the specified request header doesn't exist an empty Enumeration is returned.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeaders_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeaders_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getHeaders() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeaders_01Test.html" ignoreWhitespace="true" label="Returns an empty enumeration if the request does not have a header of the specified name, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetIntHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetIntHeaderTestServlet">
  <requestHeader headerName="MyIntHeader" headerValue="123" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getIntHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetIntHeaderTest.html" ignoreWhitespace="true" label="Returns the value of the specified request header as an integer.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetIntHeaderLCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetIntHeaderLCaseTestServlet">
  <requestHeader headerName="MyIntHeader" headerValue="123" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getIntHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetIntHeaderLCaseTest.html" ignoreWhitespace="true" label="Returns the value of the specified lower cased request header as an integer.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetIntHeaderMxCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetIntHeaderMxCaseTestServlet">
  <requestHeader headerName="MyIntHeader" headerValue="123" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getIntHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetIntHeaderMxCaseTest.html" ignoreWhitespace="true" label="Returns the value of the specified mixed case request header as an integer.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetIntHeader_1Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetIntHeader_1TestServlet">
  <requestHeader headerName="MyNonIntHeader" headerValue="Java" />
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getIntHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetIntHeader_1Test.html" ignoreWhitespace="true" label="Throws NumberFormatException, if the header value can't be converted to an int,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetIntHeader_2Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetIntHeader_2TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getIntHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetIntHeader_2Test.html" ignoreWhitespace="true" label="Returns -1 if the request doesn't have a header of this name ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMethodTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetMethodTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getMethod() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetMethodTest.html" ignoreWhitespace="true" label="Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" method="head" version="1.0" label="GetMethod_HEADTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetMethod_HEADTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getMethod() HEAD method.-->
    <statusCode label="Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="status" headerValue="GetMethod_HEADTest PASSED" label="Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <!--Converted GTest attribute expectedResponseBody: false-->
    <byteLength min="-1" max="-1" label="Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" method="post" version="1.0" label="GetMethod_POSTTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetMethod_POSTTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getMethod() POST method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetMethod_POSTTest.html" ignoreWhitespace="true" label="Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetPathInfoTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetPathInfoTestServlet/pathinfostring1/pathinfostring2">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getPathInfo() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetPathInfoTest.html" ignoreWhitespace="true" label="Returns any extra path information associated with the URL the client sent when it made this request.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetPathInfo_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetPathInfo_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getPathInfo() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetPathInfo_01Test.html" ignoreWhitespace="true" label="Returns null if there was no extra path information sent with this request.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetPathTranslatedTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetPathTranslatedTestServlet/javax_servlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getPathTranslated() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetPathTranslatedTest.html" ignoreWhitespace="true" label="Returns any extra path information after the servlet name but before the query string, and translates it to a real path.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetPathTranslatedNullTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetPathTranslatedNullTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getPathTranslated() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetPathTranslatedNullTest.html" ignoreWhitespace="true" label="Returns null if the URL has no extra path information after the servlet name but before the query string,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetPathTranslated_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetPathTranslated_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getPathTranslated() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetPathTranslated_01Test.html" ignoreWhitespace="true" label="Returns null if the URL does not have any extra path information.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetQueryStringTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetQueryStringTestServlet?language=Java">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getQueryString() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetQueryStringTest.html" ignoreWhitespace="true" label="Returns the query string that is contained in the request URL after the path, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetQueryString_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetQueryString_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getQueryString() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetQueryString_01Test.html" ignoreWhitespace="true" label="Returns null if the URL contains no query string, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRemoteUser_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetRemoteUser_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getRemoteUser() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetRemoteUser_01Test.html" ignoreWhitespace="true" label="Returns null if the user has not been authenticated., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRequestURITest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetRequestURITestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getRequestURI() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetRequestURITest.html" ignoreWhitespace="true" label="Returns the part of this request's URL from the protocol name up to the query string in the first line of the HTTP request.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRequestURIWithQSTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetRequestURIWithQSTestServlet?language=java">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getRequestURI() testing with query string.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetRequestURIWithQSTest.html" ignoreWhitespace="true" label="Returns the part of this request's URL from the protocol name up to the query string in the first line of the HTTP request.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRequestedSessionId_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetRequestedSessionId_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.getRequestedSessionId() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetRequestedSessionId_01Test.html" ignoreWhitespace="true" label="Returns null if the request did not specify a session ID, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletPathTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetServletPathTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getServletPath() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetServletPathTest.html" ignoreWhitespace="true" label="Returns the part of this request's URL that calls the servlet.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsRequestedSessionIdFromCookie_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/IsRequestedSessionIdFromCookie_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletRequest.isRequestedSessionIdFromCookie() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/IsRequestedSessionIdFromCookie_01Test.html" ignoreWhitespace="true" label="Returns false if session Id did not come in as a cookie ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsRequestedSessionIdFromURL_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/IsRequestedSessionIdFromURL_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.isRequestedSessionIdFromURL() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/IsRequestedSessionIdFromURL_01Test.html" ignoreWhitespace="true" label="Returns false if session Id did not come in as part of a URL, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsRequestedSessionIdValid_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/IsRequestedSessionIdValid_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.isRequestedSessionIdValid() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/IsRequestedSessionIdValid_01Test.html" ignoreWhitespace="true" label="Returns false if this request does not have an id for a valid session in the current session context, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeaderTestServlet">
  <requestHeader headerName="User-Agent" headerValue="Mozilla/4.0" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getHeader() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeaderTest.html" ignoreWhitespace="true" label="Returns the value of the specified request header as a String, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeaderLCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeaderLCaseTestServlet">
  <requestHeader headerName="User-Agent" headerValue="Mozilla/4.0" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getHeader() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeaderLCaseTest.html" ignoreWhitespace="true" label="Returns the value of the specified lower cased request header as a String, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetHeaderMxCaseTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetHeaderMxCaseTestServlet">
  <requestHeader headerName="User-Agent" headerValue="Mozilla/4.0" />
  <validate>
    <!--TEST STRATEGY: A test for HttpServletRequest.getHeader() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetHeaderMxCaseTest.html" ignoreWhitespace="true" label="Returns the value of the specified mixed case request header as a String, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetSession_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletRequest/GetSession_01TestServlet">
  <validate>
    <!--TEST STRATEGY: Tests that getSession(false) returns null-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletRequest/GetSession_01Test.html" ignoreWhitespace="true" label="Returns null if create is false and the request has no valid HttpSession, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

