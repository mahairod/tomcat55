<request followRedirects="false" version="1.0" label="DoFilterTest" path="/servlet-tests/tests/javax_servlet/Filter/DoFilterTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Filter/DoFilterTest.html" ignoreWhitespace="true" label="The doFilter method of the Filter is called by the container each time a request/response pair is passed through the stack due to a client request for the Servlet in the stack, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Filter.doFilter method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="InitFilterConfigTest" path="/servlet-tests/tests/javax_servlet/Filter/InitFilterConfigTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Filter/InitFilterConfigTest.html" ignoreWhitespace="true" label="The container calls this method when the Filter is instantiated and passes in a FilterConfig object. specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.setFilter method" />
  </validate>
</request>

