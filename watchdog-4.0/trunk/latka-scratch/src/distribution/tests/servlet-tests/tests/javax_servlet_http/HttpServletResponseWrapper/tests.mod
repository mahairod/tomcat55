<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperConstructorTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperConstructorTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who constructs a Wrapper object from the response object.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperConstructorTest.html" ignoreWhitespace="true" label="Construct a request object wrapping the given request, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperAddCookieTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddCookieTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses addCookie method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddCookieTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call addCookie(Cookie cookie) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.addCookie() method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addCookie" label="Test for default behavior of this method is to call addCookie(Cookie cookie) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.addCookie() method" />
    <responseHeader headerName="Set-Cookie" headerValue="BestLanguage=Java" label="Test for default behavior of this method is to call addCookie(Cookie cookie) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.addCookie() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperAddDateHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddDateHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses addDateHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddDateHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call addDateHeader(String long) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.addDateHeader() method" />
    <responseHeader headerName="MyDateHeader" headerValue="Mon, 12 Jan 1970 10:20:54 GMT" label="Test for default behavior of this method is to call addDateHeader(String long) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.addDateHeader() method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addDateHeader" label="Test for default behavior of this method is to call addDateHeader(String long) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.addDateHeader() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperAddHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses addHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
    <responseHeader headerName="MyStrHeader" headerValue="Java" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
    <responseHeader headerName="MyStrHeader" headerValue="Java2" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addHeader" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addHeader" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addHeader" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
    <responseHeader headerName="MyStrHeader2" headerValue="Java3" label="Test for default behavior of this method is to return addHeader(String name, String value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addHeader() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperAddIntHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddIntHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses addIntHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperAddIntHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call addIntHeader(String name, int value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addIntHeader(String,Int) method" />
    <responseHeader headerName="MyIntHeader" headerValue="2" label="Test for default behavior of this method is to call addIntHeader(String name, int value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addIntHeader(String,Int) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addIntHeader" label="Test for default behavior of this method is to call addIntHeader(String name, int value) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. addIntHeader(String,Int) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperContainsHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperContainsHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses containsHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperContainsHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call containsHeader(String name) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. containsHeader(String) method" />
    <responseHeader headerName="MyStrHeader" headerValue="HttpServletResponseWrapperContainsHeaderTest" label="Test for default behavior of this method is to call containsHeader(String name) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. containsHeader(String) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="setHeader" label="Test for default behavior of this method is to call containsHeader(String name) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. containsHeader(String) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="containsHeader" label="Test for default behavior of this method is to call containsHeader(String name) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. containsHeader(String) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperEncodeURLTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperEncodeURLTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses encodeURL method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperEncodeURLTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call encodeURL(String url) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.encodeURL() method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="encodeURL" label="Test for default behavior of this method is to call encodeURL(String url) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.encodeURL() method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperEncodeRedirectURLTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperEncodeRedirectURLTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses encodeRedirectURL method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperEncodeRedirectURLTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to return encodeRedirectURL(String url) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. encodeRedirectURL(String) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="encodeRedirectURL" label="Test for default behavior of this method is to return encodeRedirectURL(String url) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. encodeRedirectURL(String) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSendErrorTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSendErrorTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses sendError method.-->
    <statusCode code="410" label="Test for default behavior of this method is to call sendError(int sc) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.sendError(int) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="sendError" label="Test for default behavior of this method is to call sendError(int sc) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.sendError(int) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSendErrorMsgIgnoreHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSendErrorMsgIgnoreHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet whose response has been wrapped. The wrapper object will call sendError() and then add a header. The header should not be visible to the client.-->
    <statusCode code="410" label="Test that headers added after a call to sendError(int,String), will be ignored by the container and will not be sent to the client. See Servlet Specification 2.3 section SRV.5.2." />
    <responseHeader headerName="GenericResponseWrapper" headerValue="sendErrorMsgIgnoreHeader" cond="false" label="Test that headers added after a call to sendError(int,String), will be ignored by the container and will not be sent to the client. See Servlet Specification 2.3 section SRV.5.2." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSendErrorIgnoreHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSendErrorIgnoreHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet whose response has been wrapped. The wrapper object will call sendError() and then add a header. The header should not be visible to the client.-->
    <statusCode code="410" label="Test that headers added after a call to sendError(int), will be ignored by the container and will not be sent to the client. See Servlet Specification 2.3 section SRV.5.2." />
    <responseHeader headerName="GenericResponseWrapper" headerValue="sendErrorIgnoreHeader" cond="false" label="Test that headers added after a call to sendError(int), will be ignored by the container and will not be sent to the client. See Servlet Specification 2.3 section SRV.5.2." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSendRedirectTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSendRedirectTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses sendRedirect method.-->
    <statusCode code="302" label="Test for default behavior of this method is to return sendRedirect(String) on the wrapped response object of a URL that is based on the server root, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. sendRedirect(String location) method" />
    <responseHeader headerName="Location" headerValue="http://${host}:${port}/HttpServletResponseWrapperRedirectedTest" label="Test for default behavior of this method is to return sendRedirect(String) on the wrapped response object of a URL that is based on the server root, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. sendRedirect(String location) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSendRedirectIgnoreHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSendRedirectIgnoreHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet whose response has been wrapped. The wrapper object will call sendRedirect() and then add a header. The header should not be visible to the client.-->
    <statusCode code="302" label="Test that headers added after a call to sendRedirect(), will be ignored by the container and will not be sent to the client. See Servlet Specification 2.3 section SRV.5.2." />
    <responseHeader headerName="GenericResponseWrapper" headerValue="sendRedirectIgnoreHeader" cond="false" label="Test that headers added after a call to sendRedirect(), will be ignored by the container and will not be sent to the client. See Servlet Specification 2.3 section SRV.5.2." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSendRedirectForWebAppTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSendRedirectForWebAppTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses sendRedirect method.-->
    <statusCode code="302" label="Test for default behavior of this method is to return sendRedirect(String) on the wrapped response object of a URL that is based on the context-root, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. sendRedirect(String location) method" />
    <responseHeader headerName="Location" headerValue="http://${host}:${port}/servlet-tests/hsresw/HttpServletResponseWrapperRedirectedTest" label="Test for default behavior of this method is to return sendRedirect(String) on the wrapped response object of a URL that is based on the context-root, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. sendRedirect(String location) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSetDateHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetDateHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses setDateHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetDateHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call setDateHeader(String, long) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setDateHeader(String name, long date) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="setDateHeader" label="Test for default behavior of this method is to call setDateHeader(String, long) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setDateHeader(String name, long date) method" />
    <responseHeader headerName="DateInfo" headerValue="Sat, 25 Apr 1970 07:29:03 GMT" label="Test for default behavior of this method is to call setDateHeader(String, long) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setDateHeader(String name, long date) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSetHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses setHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to return setHeader(String, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setHeader(String name, String value) method" />
    <responseHeader headerName="MyStrHeader" headerValue="Java" label="Test for default behavior of this method is to return setHeader(String, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setHeader(String name, String value) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addHeader" label="Test for default behavior of this method is to return setHeader(String, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setHeader(String name, String value) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="setHeader" label="Test for default behavior of this method is to return setHeader(String, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setHeader(String name, String value) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSetIntHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetIntHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses setIntHeader method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetIntHeaderTest.html" ignoreWhitespace="true" label="Test for default behavior of this method is to call setIntHeader(String, int) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setIntHeader(String name, int value) method" />
    <responseHeader headerName="MyIntHeader" headerValue="2" label="Test for default behavior of this method is to call setIntHeader(String, int) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setIntHeader(String name, int value) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="addIntHeader" label="Test for default behavior of this method is to call setIntHeader(String, int) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setIntHeader(String name, int value) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="setIntHeader" label="Test for default behavior of this method is to call setIntHeader(String, int) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setIntHeader(String name, int value) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSetStatusMsgTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetStatusMsgTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses setStatus method.-->
    <statusCode label="Test for default behavior of this method is to call setStatus(int, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setStatus(int sc, String msg) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="setStatusMsg" label="Test for default behavior of this method is to call setStatus(int, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setStatus(int sc, String msg) method" />
    <statusText text="in HttpServletResponseWrapperSetStatusMsgTest servlet" label="Test for default behavior of this method is to call setStatus(int, String) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper. setStatus(int sc, String msg) method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpServletResponseWrapperSetStatusTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponseWrapper/HttpServletResponseWrapperSetStatusTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet who's response has been wrapped. The wrapper object adds a specific header to the response object and calls the responses setStatus method.-->
    <statusCode label="Test for default behavior of this method is to call setStatus(int) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.setStatus(int sc) method" />
    <responseHeader headerName="GenericResponseWrapper" headerValue="setStatus" label="Test for default behavior of this method is to call setStatus(int) on the wrapped response object, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpServletResponseWrapper.setStatus(int sc) method" />
  </validate>
</request>

