<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/info/positiveInfo.jsp" label="positiveInfoTest">
  <validate>
    <!--TEST STRATEGY: Set the info attribute of the page directive. Call getServletInfo().-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/info/positiveInfo.html" ignoreWhitespace="true" label="The arbitrary string incorporated into the translated page by using the info attribute of the page directive, is available via Servlet.getServletInfo(); JavaServer Pages Specification v1.2, Sec 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/info/negativeDuplicateInfoFatalTranslationError.jsp" label="negativeDuplicateInfoFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two info attributes. Validate that a fatal translation error occurs.-->
    <statusCode code="500" label="Duplicate info attributes within a given translation unit shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec 2.10.1" />
  </validate>
</request>

