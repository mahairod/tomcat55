<request followRedirects="false" version="1.0" label="GetMajorVersionTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetMajorVersionTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getMajorVersion() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetMajorVersionTest.html" ignoreWhitespace="true" label="Returns the major version of the Java Servlet API that this servlet container supports, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMimeType_1Test" path="/servlet-tests/tests/javax_servlet/ServletContext/GetMimeType_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for getMimeType(). Test the ServletContext.getMimeType() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetMimeType_1Test.html" ignoreWhitespace="true" label="Returns null if the MIME type is not known, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetRealPathTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetRealPathTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getRealPath() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetRealPathTest.html" ignoreWhitespace="true" label="Returns a String containing the real path for a given virtual path, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetResourceAsStreamTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetResourceAsStreamTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for getResourceAs Stream method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetResourceAsStreamTest.html" ignoreWhitespace="true" label="Returns the resource located at the named path as an InputStream object, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetResourceAsStream_1Test" path="/servlet-tests/tests/javax_servlet/ServletContext/GetResourceAsStream_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for getResourceAsStream() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetResourceAsStream_1Test.html" ignoreWhitespace="true" label="Returns null if no resource exists at the specified path, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetResourceTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetResourceTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for ServletContext.getResource(String) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetResourceTest.html" ignoreWhitespace="true" label="Returns a URL to the resource that is mapped to a specified path, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetResource_1Test" path="/servlet-tests/tests/javax_servlet/ServletContext/GetResource_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletContext.getResource(String) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetResource_1Test.html" ignoreWhitespace="true" label="This method returns null if no resource is mapped to the pathname, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServerInfoTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetServerInfoTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for ServletContext.getServerInfo() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetServerInfoTest.html" ignoreWhitespace="true" label="Returns the name and version of the servlet container on which the servlet is running., specified in the Java Servlet Pages Specification V2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Log_StringTest" path="/servlet-tests/tests/javax_servlet/ServletContext/Log_StringTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for log(String message),by passing the message string. The server specific log file can be looked up to see an outting-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/Log_StringTest.html" ignoreWhitespace="true" label="Writes the specified message to a servlet log file, usually an event log., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Log_StringThrowableTest" path="/servlet-tests/tests/javax_servlet/ServletContext/Log_StringThrowableTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for log(String message,Throwable)-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/Log_StringThrowableTest.html" ignoreWhitespace="true" label="Writes an explanatory message and a stack trace for a given Throwable exception to the servlet log file., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Try to get the attributes for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetAttributeTest.html" ignoreWhitespace="true" label="Returns the servlet container attribute with the given name, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetAttribute_1Test" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetAttribute_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletContext.getAttribute(). Test for null attribute values for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetAttribute_1Test.html" ignoreWhitespace="true" label="Returns null if there is no attribute by that name, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetContextTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for ServletContext object for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetContextTest.html" ignoreWhitespace="true" label="Returns a ServletContext object that corresponds to a specified URL on the server, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetInitParameterNamesTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetInitParameterNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getInitParameterNames() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetInitParameterNamesTest.html" ignoreWhitespace="true" label="Returns the names of the context's initialization parameters as an Enumeration of String objects, or an empty Enumeration if the context has no initialization parameters, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetInitParameterTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetInitParameterTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getInitParameter(String) for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetInitParameterTest.html" ignoreWhitespace="true" label="Returns a String containing the value of the named context-wide initialization parameter, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetInitParameter_1Test" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetInitParameter_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for ServletContext.getInitParameter(). Test the ServletContext.getInitParameterNames() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetInitParameter_1Test.html" ignoreWhitespace="true" label="Returns a null if the parameter does not exist, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextRemoveAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextRemoveAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for ServletContext.removeAttribute() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextRemoveAttributeTest.html" ignoreWhitespace="true" label="Removes the attribute with the given name from the servlet context., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextSetAttributeTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextSetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for ServletContext.setAttribute() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextSetAttributeTest.html" ignoreWhitespace="true" label="Binds an object to a given attribute name in this servlet context., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetAttributeNamesTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetAttributeNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet retrieves attributes which it set itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetAttributeNamesTest.html" ignoreWhitespace="true" label="Returns an Enumeration containing the attribute names available within this servlet context specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMinorVersionTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetMinorVersionTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getMinorVersion() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetMinorVersionTest.html" ignoreWhitespace="true" label="Returns the minor version of the Java Servlet API that this servlet container supports, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMimeTypeTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetMimeTypeTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getMimeType() for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetMimeTypeTest.html" ignoreWhitespace="true" label="Returns the MIME type of the specified file, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetRequestDispatcherTest" path="/servlet-tests/tests/javax_servlet/ServletContext/ServletContextGetRequestDispatcherTestServlet">
  <validate>
    <!--TEST STRATEGY: Test the ServletContext.getRequestDispatcher(String) for this servlet itself-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/ServletContextGetRequestDispatcherTest.html" ignoreWhitespace="true" label="Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetNamedDispatcherTest" path="/servlet-tests/tests/javax_servlet/ServletContext/GetNamedDispatcherTestServlet">
  <validate>
    <!--TEST STRATEGY: Servlet verify's that the result from the getNamedDispatcher call and the getServletName call are the same for the servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContext/GetNamedDispatcherTest.html" ignoreWhitespace="true" label="Returns a RequestDispatcher object that acts as a wrapper for the named servlet. This method returns null if the ServletContext cannot return a RequestDispatcher for any reason, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

