<request followRedirects="false" version="1.0" label="FilterChainTest" path="/servlet-tests/tests/javax_servlet/FilterChain/FilterChainTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and both filters configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterChain/FilterChainTest.html" ignoreWhitespace="true" label="The next filter in the chain to be invoked, or if the calling filter is the last filter in the chain, causes the resource at the end of the chain to be invoked, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.FilterChain.doFilter method" />
  </validate>
</request>

