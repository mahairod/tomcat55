<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/include/positiveIncludeCtxRelative.jsp" label="positiveIncludeCtxRelativeTest">
  <validate>
    <!--TEST STRATEGY: Include content, using a context-relative path, from JSP page into the current JSP page.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/include/positiveIncludeCtxRelative.html" ignoreWhitespace="true" label="jsp\:include provides for the inclusion  of dynamic resources, within the same context, using a context-relative path. JavaServer Pages Specification v1.2, Sec. 4.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/include/positiveIncludeCtxRelativeHtml.jsp" label="positiveIncludeCtxRelativeHtmlTest">
  <validate>
    <!--TEST STRATEGY: Include content, using a context-relative path, from a static HTML page into the current JSP page.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/include/positiveIncludeCtxRelativeHtml.html" ignoreWhitespace="true" label="jsp\:include provides for the inclusion  of static resources, within the same context, using a context-relative path. JavaServer Pages Specification v1.2, Sec. 4.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/include/positiveIncludePageRelative.jsp" label="positiveIncludePageRelativeTest">
  <validate>
    <!--TEST STRATEGY: Include content, using a page-relative path, from a JSP page into the current JSP page.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/include/positiveIncludePageRelative.html" ignoreWhitespace="true" label="jsp\:include provides for the inclusion  of dynamic resources, within the same context, using a page-relative path. JavaServer Pages Specification v1.2, Sec. 4.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/include/positiveRequestAttrCtxRelative.jsp" label="positiveIncludeRequestAttrCtxRelativeTest">
  <validate>
    <!--TEST STRATEGY: Validate the page attribute of jsp\:include can correctly accept request-time attribute values which contain context-relative paths.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/include/positiveRequestAttrCtxRelative.html" ignoreWhitespace="true" label="The page attribute of jsp\:include accepts request-time attributes containing context-relative path values." />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/include/positiveRequestAttrPageRelative.jsp" label="positiveIncludeRequestAttrPageRelativeTest">
  <validate>
    <!--TEST STRATEGY: Validate the page attribute of jsp\:include can correctly accept request-time attribute values which contain page-relative paths.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/include/positiveRequestAttrPageRelative.html" ignoreWhitespace="true" label="The page attribute of jsp\:include accepts request-time attributes containing page-relative path values." />
  </validate>
</request>

