<request followRedirects="false" version="1.0" label="WithLeadingSlashTest" path="/servlet-compat/tests/compat/WithLeadingSlashTestServlet">
  <validate>
    <!--TEST STRATEGY: The DD url-pattern has a '/' at the beginning of the string. The web app should deploy and be able to be called by a client.-->
    <goldenFile fileName="${servlet-wgdir}/compat/WithLeadingSlashTest.html" ignoreWhitespace="true" label="A 2.2 web application deployment descriptor who's url mapping begins with a '/' can be deployed in a 2.3 environment, specified in the Java Servlet Pages Specification v2.3, Sec 11" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="WithoutLeadingSlashTest" path="/servlet-compat/tests/compat/WithoutLeadingSlashTestServlet">
  <validate>
    <!--TEST STRATEGY: The DD url-pattern that does not have a '/' at the beginning of the string. The web app should deploy and be able to be called by a client.-->
    <goldenFile fileName="${servlet-wgdir}/compat/WithoutLeadingSlashTest.html" ignoreWhitespace="true" label="A 2.2 web application deployment descriptor who's url mapping does not begin with a '/' can be deployed in a 2.3 environment, specified in the Java Servlet Pages Specification v2.3, Sec 11" />
  </validate>
</request>

