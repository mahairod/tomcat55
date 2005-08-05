<request followRedirects="false" version="1.0" label="HttpSessionValueUnBoundTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingListener/HttpSessionValueUnBoundTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that sets/removes an attribute from the session. That attribute happens to be a Binding listener. The Listeners valueBound/valueUnbound methods should be called and messages written to a static log file. The servlet then reads the log file and sends the data back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingListener/HttpSessionValueUnBoundTest.html" ignoreWhitespace="true" label="Test for notification that the object is being unbound to a session specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionBindingListener. valueUnBound method" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="HttpSessionValueBoundTest" path="/servlet-tests/tests/javax_servlet_http/HttpSessionBindingListener/HttpSessionValueBoundTestServlet">
  <validate>
    <!--TEST STRATEGY: Client calls a servlet that sets an attribute to the session. That attribute happens to be a Binding listener. The Listeners valueBound method should be called and a message is written to a static log file. The servlet then reads the log file and sends the data back to the client.-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet_http/HttpSessionBindingListener/HttpSessionValueBoundTest.html" ignoreWhitespace="true" label="Test for notification that the object is being bound to a session specified in the Java Servlet Pages Specification v2.3, Sec 14 - javax.servlet.http.HttpSessionBindingListener. valueBound method" />
  </validate>
</request>

