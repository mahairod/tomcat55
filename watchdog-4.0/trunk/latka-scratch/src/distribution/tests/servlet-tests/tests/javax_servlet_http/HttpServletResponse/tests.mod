<request followRedirects="false" version="1.0" label="AddCookieTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/AddCookieTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.addCookie() method.-->
    <responseHeader headerName="Set-Cookie" headerValue="BestLanguage=Java" label="Adds the specified cookie to the response.,specified in the Java Servl et Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="AddDateHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/AddDateHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.addDateHeader() method.-->
    <responseHeader headerName="DateInfo" headerValue="Sat, 25 Apr 1970 07:29:03 GMT" label="Adds a response header with the given name and date-value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="AddHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/AddHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.addHeader() method.-->
    <responseHeader headerName="MyStrHeader" headerValue="Java" label="Adds a response header with the given name and value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="MyStrHeader" headerValue="Java2" label="Adds a response header with the given name and value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="MyStrHeader2" headerValue="Java3" label="Adds a response header with the given name and value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="AddIntHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/AddIntHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.addIntHeader() method.-->
    <responseHeader headerName="MyIntHeader2" headerValue="4" label="Adds a response header with the given name and integer value.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="MyIntHeader" headerValue="2" label="Adds a response header with the given name and integer value.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="MyIntHeader" headerValue="3" label="Adds a response header with the given name and integer value.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ContainsHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/ContainsHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.containsHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponse/ContainsHeaderTest.html" ignoreWhitespace="true" label="Returns true if the named response header has already been set.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="MyIntHeader" headerValue="20" label="Returns true if the named response header has already been set.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ContainsHeader_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/ContainsHeader_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletResponse.containsHeader() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponse/ContainsHeader_01Test.html" ignoreWhitespace="true" label="Returns false if the named response header has not already been set.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendErrorTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendErrorTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.sendError(int sc) method.-->
    <statusCode code="410" label="Sends an error response to the client using the specified status code,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendErrorIgnoreHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendErrorIgnoreHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Issue a request to target servlet which will call sendError() and then add a header to the response. The added header should not be visible to the client.-->
    <statusCode code="410" label="Verify that headers added after sendError(int) are ignored by the container. Servlet Specification 2.3 section SRV.5.2." />
    <responseHeader headerName="HttpServletResponse" headerValue="sendErrorIgnoreHeader" cond="false" label="Verify that headers added after sendError(int) are ignored by the container. Servlet Specification 2.3 section SRV.5.2." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendError_StringIgnoreHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendError_StringIgnoreHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Issue a request to target servlet which will call sendError() and then add a header to the response. The added header should not be visible to the client.-->
    <statusCode code="410" label="Verify that headers added after sendError(int,String) are ignored by the container. Servlet Specification 2.3 section SRV.5.2." />
    <responseHeader headerName="HttpServletResponse" headerValue="sendErrorMsgIgnoreHeader" cond="false" label="Verify that headers added after sendError(int,String) are ignored by the container. Servlet Specification 2.3 section SRV.5.2." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendRedirectTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendRedirectTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.sendRedirect() method.-->
    <statusCode code="302" label="Sends a temporary redirect response to the client using the specified redirect location URL that is based on the server root,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Location" headerValue="http://${host}:${port}/RedirectedTest" label="Sends a temporary redirect response to the client using the specified redirect location URL that is based on the server root,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendRedirectIgnoreHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendRedirectIgnoreHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: Issue a request to target servlet which will call sendRedirect() and then add a header to the response. The added header should not be visible to the client.-->
    <statusCode code="302" label="Verify that headers added after sendRedirect(String) are ignored by the container. Servlet Specification 2.3 section SRV.5.2." />
    <responseHeader headerName="HttpServletResponse" headerValue="sendRedirectIgnoreHeader" cond="false" label="Verify that headers added after sendRedirect(String) are ignored by the container. Servlet Specification 2.3 section SRV.5.2." />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendRedirectForWebAppTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendRedirectForWebAppTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.sendRedirect() method.-->
    <statusCode code="302" label="Sends a temporary redirect response to the client using the specified redirect location URL that is based on the context-root,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
    <responseHeader headerName="Location" headerValue="http://${host}:${port}/servlet-tests/RedirectedTest" label="Sends a temporary redirect response to the client using the specified redirect location URL that is based on the context-root,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SendRedirect_1Test" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SendRedirect_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpServletResponse.sendRedirect() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpServletResponse/SendRedirect_1Test.html" ignoreWhitespace="true" label="Throws IllegalStateException if the response was committed ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetDateHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SetDateHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.sendDateHeader() method.-->
    <responseHeader headerName="DateInfo" headerValue="Sat, 25 Apr 1970 07:29:03 GMT" label="Sets a response header with the given name and date-value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SetHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.setHeader() method.-->
    <responseHeader headerName="MyStrHeader" headerValue="Java" label="Sets a response header with the given name and value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetIntHeaderTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SetIntHeaderTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.setIntHeader() method.-->
    <responseHeader headerName="MyIntHeader" headerValue="2" label="Sets a response header with the given name and integer value.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetStatusTest" path="/servlet-tests/tests/javax_servlet_http/HttpServletResponse/SetStatusTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpServletResponse.setStatus() method.-->
    <statusCode label="Sets the status code for this response.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

