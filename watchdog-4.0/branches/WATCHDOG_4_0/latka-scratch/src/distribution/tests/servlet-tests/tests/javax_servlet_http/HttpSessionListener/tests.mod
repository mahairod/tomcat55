<request followRedirects="false" version="1.0" label="HttpSessionCreatedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionListener/HttpSessionCreatedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates a session. The listener should detect the creation and write a message to a static log file. The Servlet then reads the log file and sends the contents back to the client. As a result of the test, the javax.servlet.http.HttpSessionEvent.getSession() method is tested.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionListener/HttpSessionCreatedTest.html" ignoreWhitespace="true" label="Test for notification that a session was created, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionListener.sessionCreated method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionDestroyedTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionListener/HttpSessionDestroyedTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates and the invalidates a session. The listener should detect the changes and write a message to a static log file. The Servlet then reads the log file and sends the contents back to the client. As a result of the test, the javax.servlet.http.HttpSessionEvent.getSession() method is tested.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionListener/HttpSessionDestroyedTest.html" ignoreWhitespace="true" label="Test for notification that a session was invalidated, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionListener.sessionCreated method" />
  </validate>
</request>

