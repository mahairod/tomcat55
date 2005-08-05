<request followRedirects="false" version="1.0" label="ServletToServletErrorPageTest" path="/servlet-tests/tests/javax_servlet/Error/ServletToServletErrorPageTestServlet">
  <validate>
    <!--TEST STRATEGY: A servlet throws java.lang.ArithmeticException and the error page (servlet) for that exception is executed.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Error/ServletToServletErrorPageTest.html" ignoreWhitespace="true" label="Returns the error handling request attributes from a servlet error page(SERVLET)., specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletToServletError501PageTest" path="/servlet-tests/tests/javax_servlet/Error/ServletToServletError501PageTestServlet">
  <validate>
    <!--TEST STRATEGY: A servlet throws java.lang.ArithmeticException and the error page (servlet) for that exception is executed.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Error/ServletToServletError501PageTest.html" ignoreWhitespace="true" label="Returns the error handling request attributes from a servlet error page(SERVLET)., specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletToJSPErrorPageTest" path="/servlet-tests/tests/javax_servlet/Error/ServletToJSPErrorPageTestServlet">
  <validate>
    <!--TEST STRATEGY: A servlet throws java.lang.ArrayIndexOutOfBoundsException and the error page (servlet) for that exception is executed.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Error/ServletToJSPErrorPageTest.html" ignoreWhitespace="true" label="Returns the error handling request attributes from an error page (JSP)., specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletToJSPError502PageTest" path="/servlet-tests/tests/javax_servlet/Error/ServletToJSPError502PageTestServlet">
  <validate>
    <!--TEST STRATEGY: A servlet throws java.lang.ArrayIndexOutOfBoundsException and the error page (servlet) for that exception is executed.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Error/ServletToJSPError502PageTest.html" ignoreWhitespace="true" label="Returns the error handling request attributes from an error page (JSP)., specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ServletToHTMLErrorPageTest" path="/servlet-tests/tests/javax_servlet/Error/ServletToHTMLErrorPageTestServlet">
  <validate>
    <!--TEST STRATEGY: A servlet throws java.lang.NumberFormatException and the error page (HTML) for that exception is executed.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/Error/ServletToHTMLErrorPageTest.html" ignoreWhitespace="true" label="Serves back the resource (HTML) as indicated by the location entry, specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

