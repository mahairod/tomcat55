<request followRedirects="false" version="1.0" label="ServletContextAttributeAddedEventTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeAddedEventTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds an attribute. The listener should detect the add and write the name and value out to a static log file. Servlet then reads the log file and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeAddedEventTest.html" ignoreWhitespace="true" label="Test that a notification is generated that a new attribute was added to the servlet context, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet. ServletContextAttributeListener.attributeAdded method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextAttributeRemovedEventTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeRemovedEventTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/removes an attribute. The listener should detect the two actions and write the name and value out to a static log file. Servlet then reads the log file and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeRemovedEventTest.html" ignoreWhitespace="true" label="Test that a notification is generated that an existing attribute has been removed from the servlet context, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextAttributeListener. attributeRemoved method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextAttributeReplacedEventTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeReplacedEventTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/replaces an attribute. The listener should detect the two actions and write the name and value out to a static log file. Servlet then reads the log file and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeReplacedEventTest.html" ignoreWhitespace="true" label="Test that a notification is generated that an existing attribute has been replaced from the servlet context, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextAttributeListener. attributeReplaced method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextAttributeEventConstructorTest" path="/servlet-tests/tests/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeEventConstructorTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates a ServletContextAttributeEvent object.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextAttributeEvent/ServletContextAttributeEventConstructorTest.html" ignoreWhitespace="true" label="Test that ServletContextAttributeEvent can be constructed from the given context for the given attribute name and value specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextAttributeEvent." />
  </validate>
</request>

