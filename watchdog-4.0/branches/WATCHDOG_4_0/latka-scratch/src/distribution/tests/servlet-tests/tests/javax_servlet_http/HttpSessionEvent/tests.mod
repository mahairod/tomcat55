<request followRedirects="false" version="1.0" label="HttpSessionEventGetSessionTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionEvent/HttpSessionEventGetSessionTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates a session. The listener writes the sessionid of the event to a static log file. The Servlet then reads the log file and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionEvent/HttpSessionEventGetSessionTest.html" ignoreWhitespace="true" label="Test for the returned session that has changed, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.HttpSessionEvent.getSession method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionEventGetSourceTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionEvent/HttpSessionEventGetSourceTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that creates a session. The listener writes the source of the event to a static log file. The Servlet then reads the log file and sends the contents back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionEvent/HttpSessionEventGetSourceTest.html" ignoreWhitespace="true" label="Test for the object on which the Event initially occured, specified in the java.util.EventObject.getSource method" />
  </validate>
</request>

