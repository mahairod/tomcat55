<request followRedirects="false" version="1.0" label="HttpSessionBindingEventAddedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventAddedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds an attribute. The listener should detect the add and writes the values returned by the getName, getSession(), and getValue() methods to a static log file. Servlet then reads the log and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventAddedTest.html" ignoreWhitespace="true" label="Test that when a new attribute is added to the session, the getName() method returns the name with which the object is bound to, the getSession() method returns the session that changed, and the getValue() method returns the value of the attribute being added - specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http .HttpSessionBindingEvent" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionBindingEventRemovedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventRemovedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/removes an attribute. The listener should detect the changes and writes the values returned by the getName, getSession(), and getValue() methods to a static log file. Servlet then reads the log and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventRemovedTest.html" ignoreWhitespace="true" label="Test that when an attribute is removed from the session, the getName() method returns the name with which the object was bound to, the getSession() method returns the session that changed, and the getValue() method returns the value of the attribute being removed - specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http .HttpSessionBindingEvent" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionBindingEventReplacedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventReplacedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that adds/replaces an attribute. The listener should detect the changes and writes the values returned by the getName, getSession(), and getValue() methods to a static log file. Servlet then reads the log and sends the contents back to the client. the log and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventReplacedTest.html" ignoreWhitespace="true" label="Test that when an attribute is replaced from the session, the getName() method returns the name with which the object is bound to, the getSession() method returns the session that changed, and the getValue() method returns the new value of the attribute - specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http .HttpSessionBindingEvent" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionBindingEventConstructor1Test" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventConstructor1TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates a HttpSessionBindingEvent object using the 2 argument method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventConstructor1Test.html" ignoreWhitespace="true" label="Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the event, the object must implement HttpSessionBindingListener specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionBindingEvent" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionBindingEventConstructor2Test" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventConstructor2TestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates a HttpSessionBindingEvent object using the 2 argument method.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingEvent/HttpSessionBindingEventConstructor2Test.html" ignoreWhitespace="true" label="Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the event, the object must implement HttpSessionBindingListener specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionBindingEvent" />
  </validate>
</request>

