<request followRedirects="false" version="1.0" label="GetCreationTimeTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/GetCreationTimeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getCreationTime() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/GetCreationTimeTest.html" ignoreWhitespace="true" label="Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetIdTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/GetIdTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getId() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/GetIdTest.html" ignoreWhitespace="true" label="Returns a string containing the unique identifier assigned to this session.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetLastAccessedTimeTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/GetLastAccessedTimeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getLastAccessedTime() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/GetLastAccessedTimeTest.html" ignoreWhitespace="true" label="Returns the last time the client sent a request associated with this session, as the number of milliseconds since midnight January 1, 1970 GMT.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMaxInactiveIntervalTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/GetMaxInactiveIntervalTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getMaxInactiveInterval() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/GetMaxInactiveIntervalTest.html" ignoreWhitespace="true" label="Returns the maximum time interval, in seconds, that the servlet container will keep this session open between client accesses.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetMaxInactiveIntervalTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/SetMaxInactiveIntervalTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.setMaxInactiveInterval() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/SetMaxInactiveIntervalTest.html" ignoreWhitespace="true" label="Specifies the time, in seconds, between client requests before the servlet container will invalidate this session ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionGetAttributeNamesTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/HttpSessionGetAttributeNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getAttributeNames() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/HttpSessionGetAttributeNamesTest.html" ignoreWhitespace="true" label="Returns an Enumeration of String objects containing the names of all the objects bound to this session.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionGetAttributeNamesEmptyTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/HttpSessionGetAttributeNamesEmptyTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getAttributeNames() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/HttpSessionGetAttributeNamesEmptyTest.html" ignoreWhitespace="true" label="Returns an empty Enumeration since there were no attributes objects bound to this session.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionGetAttributeNames_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpSession/HttpSessionGetAttributeNames_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpSession.getAttributeNames() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/HttpSessionGetAttributeNames_01Test.html" ignoreWhitespace="true" label="Throws IllegalStateException if this method is called on an invalidated session,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionGetAttributeTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/HttpSessionGetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getAttribute() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/HttpSessionGetAttributeTest.html" ignoreWhitespace="true" label="Returns the object bound with the specified name in this session, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionGetAttribute_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpSession/HttpSessionGetAttribute_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpSession.getAttribute() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/HttpSessionGetAttribute_01Test.html" ignoreWhitespace="true" label="Returns null if no object is bound under the name., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="InvalidateTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/InvalidateTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.invalidate() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/InvalidateTest.html" ignoreWhitespace="true" label="Invalidates this session and unbinds any objects bound to it.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsNewTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/IsNewTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.getIsNew() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/IsNewTest.html" ignoreWhitespace="true" label="Returns true if the client does not yet know about the session or if the client chooses not to join the session., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="IsNew_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpSession/IsNew_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpSession.IsNew() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/IsNew_01Test.html" ignoreWhitespace="true" label="Throws IllegalStateException if this method is called on an already invalidated session, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="RemoveAttributeTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/RemoveAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.removeAttribute() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/RemoveAttributeTest.html" ignoreWhitespace="true" label="Removes the object bound with the specified name from this session.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="RemoveAttribute_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpSession/RemoveAttribute_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpSession.removeAttribute() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/RemoveAttribute_01Test.html" ignoreWhitespace="true" label="Throws IllegalStateException if this method is called on an invalidated session ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetAttributeTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/SetAttributeTestServlet">
  <validate>
    <!--TEST STRATEGY: A test for HttpSession.setAttribute() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/SetAttributeTest.html" ignoreWhitespace="true" label="Binds an object to this session, using the name specified. If an object of the same name is already bound to the session, the object is replaced, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetAttribute_01Test" path="/servlet-tests/tests/javax_servlet_http/HttpSession/SetAttribute_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for HttpSession.setAttribute() method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/SetAttribute_01Test.html" ignoreWhitespace="true" label="Throws IllegalStateException if this method is called on an invalidated session, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionGetServletContextTest" path="/servlet-tests/tests/javax_servlet_http/HttpSession/HttpSessionGetServletContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Call a servlet that makes API call for servlet context-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSession/HttpSessionGetServletContextTest.html" ignoreWhitespace="true" label="Returns the ServletContext to which this session belongs, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

