<request followRedirects="false" version="1.0" label="Print_StringTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_StringTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(java.lang.String s) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_StringTest.html" ignoreWhitespace="true" label="Writes a String to the client, without a carriage return-line feed (CRLF) character at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Print_booleanTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_booleanTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(boolean b) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_booleanTest.html" ignoreWhitespace="true" label="Writes a boolean value to the client, with no carriage return-line feed (CRLF) character at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Print_charTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_charTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(char c) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_charTest.html" ignoreWhitespace="true" label="Writes a character to the client, with no carriage return-line feed (CRLF) at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Print_doubleTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_doubleTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(double d) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_doubleTest.html" ignoreWhitespace="true" label="Writes a double to the client, with no carriage return-line feed (CRLF) at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Print_floatTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_floatTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(float f) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_floatTest.html" ignoreWhitespace="true" label="Writes a float to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Print_intTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_intTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(integer i) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_intTest.html" ignoreWhitespace="true" label="Writes an integer to the client, with no carriage return-line feed (CRLF) at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Print_longTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Print_longTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(long l) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Print_longTest.html" ignoreWhitespace="true" label="Writes a long to the client, with no carriage return-line feed (CRLF) at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="PrintlnTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/PrintlnTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println () method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/PrintlnTest.html" ignoreWhitespace="true" label="Writes a carriage return-line feed (CRLF) to the client., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_StringTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_StringTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(java.lang.String s) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_StringTest.html" ignoreWhitespace="true" label="Writes a String to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_booleanTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_booleanTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(boolean b) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_booleanTest.html" ignoreWhitespace="true" label="Writes a boolean to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_charTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_charTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(char c) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_charTest.html" ignoreWhitespace="true" label="Writes a char to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_doubleTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_doubleTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(double d) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_doubleTest.html" ignoreWhitespace="true" label="Writes a double to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_floatTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_floatTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for print(float f) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_floatTest.html" ignoreWhitespace="true" label="Writes a float to the client, with no carriage return-line feed (CRLF) at the end., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_intTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_intTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(integer i) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_intTest.html" ignoreWhitespace="true" label="Writes an integer to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

<request followRedirects="false" version="1.0" label="Println_longTest" path="/servlet-tests/tests/javax_servlet/ServletOutputStream/Println_longTestServlet">
  <validate>
    <!--TEST STRATEGY: Test for println(long l) method-->
    <goldenFile fileName="${servlet-wgdir}/javax_servlet/ServletOutputStream/Println_longTest.html" ignoreWhitespace="true" label="Writes a long to the client, followed by a carriage return-line feed (CRLF)., specified in the Java Servlet Pages Specification v2.3, Sec 14" />
  </validate>
</request>

