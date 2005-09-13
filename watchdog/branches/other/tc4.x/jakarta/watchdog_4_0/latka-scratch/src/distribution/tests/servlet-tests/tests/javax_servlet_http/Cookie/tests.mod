<request followRedirects="false" version="1.0" label="CookieCloneTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/CookieCloneTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.clone() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/CookieCloneTest.html" ignoreWhitespace="true" label="Overrides the standard java.lang.Object.clone() method to return a copy of this cookie.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Cookie_ConstructorTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/Cookie_ConstructorTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie(String name,String value) constructor-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/Cookie_ConstructorTest.html" ignoreWhitespace="true" label="Constructs a cookie with a specified name and value., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Cookie_Constructor_1Test" path="/servlet-tests/tests/javax_servlet_http/Cookie/Cookie_Constructor_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative Test for Cookie(String name,String value) constructor. We include some invalid chars in the Cookie name and test for IllegalArgumentException-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/Cookie_Constructor_1Test.html" ignoreWhitespace="true" label="Throws IllegalArgumentException if the cookie name contains illegal characters (for example, a comma, space, or semicolon) or it is one of the tokens reserved for use by the cookie protocol, specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetCommentTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetCommentTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getComment() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetCommentTest.html" ignoreWhitespace="true" label="Returns the comment describing the purpose of this cookie., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetComment_01Test" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetComment_01TestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getComment() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetComment_01Test.html" ignoreWhitespace="true" label="Returns null if the cookie has no comment.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetDomainTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetDomainTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getDomain() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetDomainTest.html" ignoreWhitespace="true" label="Returns the domain name set for this cookie.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMaxAgeTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetMaxAgeTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getMaxAge() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetMaxAgeTest.html" ignoreWhitespace="true" label="Returns the maximum age of the cookie, specified in seconds ,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetMaxAge_1Test" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetMaxAge_1TestServlet">
  <validate>
    <!--TEST STRATEGY: A negative test for Cookie.getMaxAge() method. We will try to get Cookies default maxAge which is '-1'-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetMaxAge_1Test.html" ignoreWhitespace="true" label="Returns by default, -1 indicating the cookie will persist until browser shutdown.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetNameTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetNameTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getName() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetNameTest.html" ignoreWhitespace="true" label="Returns the name of the cookie.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetPathTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetPathTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getPath() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetPathTest.html" ignoreWhitespace="true" label="Returns the path on the server to which the browser returns this cookie.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetSecureTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetSecureTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getSecure() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetSecureTest.html" ignoreWhitespace="true" label="Returns false if the browser can send cookies using any protocol.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetValueTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetValueTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getValue() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetValueTest.html" ignoreWhitespace="true" label="Returns the value of the cookie.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetVersionTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/GetVersionTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.getVersion() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/GetVersionTest.html" ignoreWhitespace="true" label="Returns the version of the protocol this cookie complies with, 0 if the cookie complies with the original Netscape specification,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetCommentTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetCommentTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setComment() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetCommentTest.html" ignoreWhitespace="true" label="Specifies a comment that describes a cookie's purpose., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetDomainTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetDomainTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setDomain() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetDomainTest.html" ignoreWhitespace="true" label="Specifies the domain within which this cookie should be presented.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetMaxAgeTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetMaxAgeTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setMaxAge() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetMaxAgeTest.html" ignoreWhitespace="true" label="Sets the maximum age of the cookie in seconds.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetPathTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetPathTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setPath() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetPathTest.html" ignoreWhitespace="true" label="Specifies a path for the cookie to which the client should return the cookie.,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetSecureTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetSecureTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setSecureTest() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetSecureTest.html" ignoreWhitespace="true" label="Indicates to the browser whether the cookie should only be sent using a secure protocol, such as HTTPS or SSL., the default value is false specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetValueTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetValueTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setValueTest() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetValueTest.html" ignoreWhitespace="true" label="Assigns a new value to a cookie after the cookie is created. specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="SetVersionTest" path="/servlet-tests/tests/javax_servlet_http/Cookie/SetVersionTestServlet">
  <validate>
    <!--TEST STRATEGY: A Test for Cookie.setVersion() method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/Cookie/SetVersionTest.html" ignoreWhitespace="true" label="Sets the version of the cookie protocol this cookie complies with, 0 if the cookie should comply with the original Netscape specification;,specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

