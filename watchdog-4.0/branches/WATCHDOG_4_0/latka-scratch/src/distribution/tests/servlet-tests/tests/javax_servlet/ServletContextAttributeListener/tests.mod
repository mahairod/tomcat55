<request followRedirects="false" version="1.0" label="ServletContextAttributeAddedTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeListener/ServletContextAttributeAddedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds an attribute. The listener should detect the add and write a message out to a static log file. Servlet then reads the file and sends the files contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeListener/ServletContextAttributeAddedTest.html" ignoreWhitespace="true" label="Test that a notification is generated that a new attribute was added to the servlet context, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet. ServletContextAttributeListener.attributeAdded method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextAttributeRemovedTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeListener/ServletContextAttributeRemovedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/removes an attribute. The listener should detect the two actions and write a message out to a static log file. Servlet then reads the file and sends the files contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeListener/ServletContextAttributeRemovedTest.html" ignoreWhitespace="true" label="Test that a notification is generated that an existing attribute has been removed from the servlet context, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextAttributeListener. attributeRemoved method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextAttributeReplacedTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeListener/ServletContextAttributeReplacedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/replaces an attribute. The listener should detect the two actions and write a message to a static log file. Servlet then reads the file and sends the files contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeListener/ServletContextAttributeReplacedTest.html" ignoreWhitespace="true" label="Test that a notification is generated that an existing attribute has been replaced from the servlet context, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextAttributeListener. attributeReplaced method" />
  </validate>
</request>

