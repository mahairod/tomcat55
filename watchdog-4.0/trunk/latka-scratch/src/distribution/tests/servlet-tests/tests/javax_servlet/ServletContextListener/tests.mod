<request followRedirects="false" version="1.0" label="ContextInitializedTest" path="/servlet-tests/tests/javax_servlet/ServletContextListener/ContextInitializedTestServlet">
  <validate>
    <!--TEST STRATEGY: A ServletContext Listener is deployed and the listener write a message indicating so to a static log file. The client calls a servlet that reads the log and send the info back to the client-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextListener/ContextInitializedTest.html" ignoreWhitespace="true" label="Test for notification that the web application is ready to process requests after the context is initialized, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextListener.contextInitialized method" />
  </validate>
</request>

