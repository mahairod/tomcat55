<request followRedirects="false" version="1.0" label="HttpSessionAttributeAddedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionAttributeListener/HttpSessionAttributeAddedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds an attribute. The listener should detect the add and writes a message out to a static log file. Servlet then reads the log and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionAttributeListener/HttpSessionAttributeAddedTest.html" ignoreWhitespace="true" label="Test that a notification is generated that a new attribute was added to the session, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http .HttpSessionAttributeListener.attributeAdded method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionAttributeRemovedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionAttributeListener/HttpSessionAttributeRemovedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/Removes an attribute. The listener should detect the changes and writes a message out to a static log file. Servlet then reads the log and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionAttributeListener/HttpSessionAttributeRemovedTest.html" ignoreWhitespace="true" label="Test for notification that an attribute has been removed from a session, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionAttributeListener .attributeRemoved method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionAttributeReplacedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionAttributeListener/HttpSessionAttributeReplacedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/Replaces an attribute. The listener should detect the changes and writes a message out to a static log file. Servlet then reads the log and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionAttributeListener/HttpSessionAttributeReplacedTest.html" ignoreWhitespace="true" label="Test for notification that an attribute has been replaced in a session, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextAttributeListener. attributeReplaced method" />
  </validate>
</request>

