<request followRedirects="false" version="1.0" label="GetFilterNameTest" path="/servlet-tests/tests/javax_servlet/FilterConfig/GetFilterNameTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterConfig/GetFilterNameTest.html" ignoreWhitespace="true" label="This method returns the filter-name of this filter as defined in the deployment descriptor, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.FilterConfig.getFilterName method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetInitParamNamesTest" path="/servlet-tests/tests/javax_servlet/FilterConfig/GetInitParamNamesTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterConfig/GetInitParamNamesTest.html" ignoreWhitespace="true" label="The Filter returns the names of the servlet's initialization parameters as an Enumeration of String objects, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Config.getInitParamterNames method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetInitParamNamesNullTest" path="/servlet-tests/tests/javax_servlet/FilterConfig/GetInitParamNamesNullTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterConfig/GetInitParamNamesNullTest.html" ignoreWhitespace="true" label="The Filter returns an empty Enumeration if the    names of the servlet's initialization parameters do not exist, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Config.getInitParamterNames method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetInitParamTest" path="/servlet-tests/tests/javax_servlet/FilterConfig/GetInitParamTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterConfig/GetInitParamTest.html" ignoreWhitespace="true" label="The Filter returns a String containing the value of the named initialization parameter, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Config.getInitParameter method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetInitParamNullTest" path="/servlet-tests/tests/javax_servlet/FilterConfig/GetInitParamNullTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterConfig/GetInitParamNullTest.html" ignoreWhitespace="true" label="The Filter returns a null if the parameter does not exist, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Config.getInitParameter method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="GetServletContextTest" path="/servlet-tests/tests/javax_servlet/FilterConfig/GetServletContextTestServlet">
  <validate>
    <!--TEST STRATEGY: Client attempts to access a servlet and the filter configured for that servlet should be invoked.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/FilterConfig/GetServletContextTest.html" ignoreWhitespace="true" label="A reference to the ServletContext object is returned, specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.Config.getServletContext method" />
  </validate>
</request>

