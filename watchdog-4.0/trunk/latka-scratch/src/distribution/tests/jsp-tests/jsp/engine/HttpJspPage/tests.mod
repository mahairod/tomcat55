<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/HttpJspPage/positiveJSPInitJSP.jsp" label="positiveJSPInitJSPTest">
  <validate>
    <!--TEST STRATEGY: Override the jspInit() method in a declaration. Validate that a call to getServletConfig returns a non-null value.-->
    <goldenFile fileName="${jsp-wgdir}/engine/HttpJspPage/positiveJSPInitJSP.html" ignoreWhitespace="true" label="Container implementations must ensure that getServletConfig() will return the desired value in cases where page authors override jspInit().  JavaServer Pages Specification v1.2, Sec. 9.1.1" />
  </validate>
</request>

