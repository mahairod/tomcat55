<request followRedirects="false" version="1.0" label="ClassFileTest" path="/servlet-tests/tests/webapps/dirstruct/ClassFileTestServlet">
  <validate>
    <!--TEST STRATEGY: The serlvet which is in the WEB-INF/classes directory is called by the client and should execute.-->
    <goldenFile fileName="${servlet-wgdir}/webapps/dirstruct/ClassFileTest.html" ignoreWhitespace="true" label="This class which is in the WEB-INF/classes directory is available to the application class loader specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="ClassFileTest" path="/servlet-tests/tests/webapps/dirstruct/JarFileTestServlet">
  <validate>
    <!--TEST STRATEGY: The serlvet which is in a jar file in the WEB-INF/lib directory is called by the client and should execute.-->
    <goldenFile fileName="${servlet-wgdir}/webapps/dirstruct/JarFileTest.html" ignoreWhitespace="true" label="This class which is in a jar file the WEB-INF/lib directory is available to the application class loader specified in the Java Servlet Pages Specification v2.3, Chapter 9" />
  </validate>
</request>

