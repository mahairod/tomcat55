<request followRedirects="false" version="1.0" label="ServletContextGetSourceTest" path="/servlet-tests/tests/javax_servlet/ServletContextEvent/ServletContextGetSourceTestServlet">
  <validate>
    <!--TEST STRATEGY: Deploy a servlet context event listener. When the context gets initialized, write a status message to a static log. Client calls servlet which reads the static log looking for a specific message and returns the message to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextEvent/ServletContextGetSourceTest.html" ignoreWhitespace="true" label="Test for the object on which the Event initially occurred specified in the java.util.EventObject.getSource method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletContextGetServletContextTest" path="/servlet-tests/tests/javax_servlet/ServletContextEvent/ServletContextGetServletContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Deploy a servlet context event listener. When the context gets initialized, write a status message to a static log. Client calls servlet which reads the static log looking for a specific message and returns the message to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletContextEvent/ServletContextGetServletContextTest.html" ignoreWhitespace="true" label="Returns the ServletContext that has changed, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.ServletContextEvent.getServletContext method" />
  </validate>
</request>

