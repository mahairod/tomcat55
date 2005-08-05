<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/include/positiveIncludeCtxRelativeDirective.jsp" label="positiveIncludeCtxRelativeDirectiveTest">
  <validate>
    <!--TEST STRATEGY: Using an include directive, include content referenced by a context-relative path.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/include/positiveIncludeCtxRelativeDirective.html" ignoreWhitespace="true" label="The include directive inserts the text of the resource specified by the file attribute.  Included content can be referenced using a context-relative path. JavaServer Pages Specification v1.2, Sec 2.10.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/include/positiveIncludePageRelativeDirective.jsp" label="positiveIncludePageRelativeDirectiveTest">
  <validate>
    <!--TEST STRATEGY: Using an include directive, include content referenced by a page-relative path.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/include/positiveIncludePageRelativeDirective.html" ignoreWhitespace="true" label="The include directive inserts the text of the resource specified by the file attribute.  Included content can be referenced using a page-relative path. JavaServer Pages Specification v1.2, Sec 2.10.3" />
  </validate>
</request>

