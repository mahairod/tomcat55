<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/forward/positiveForwardCtxRelative.jsp" label="positiveForwardCtxRelativeTest">
  <validate>
    <!--TEST STRATEGY: Validate that jsp\:forward can forward a request  to a JSP page within the same context using  a page relative-path.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/forward/positiveForwardCtxRelative.html" ignoreWhitespace="true" label="A request can be forwarded to a JSP page, within the  same context, using a page relative-path.  JavaServer Pages Specification 1.2 Sec. 4.5" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/forward/positiveForwardCtxRelativeHtml.jsp" label="positiveForwardCtxRelativeHtmlTest">
  <validate>
    <!--TEST STRATEGY: Validate that jsp\:forward can forward a request to  a static resource within the same context using  a page-relative path.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/forward/positiveForwardCtxRelativeHtml.html" ignoreWhitespace="true" label="A request can be forwarded to a static resource, within the same context, using a page-relative path.  JavaServer Pages Specification 1.2 Sec. 4.5" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/forward/positiveForwardPageRelative.jsp" label="positiveForwardPageRelativeTest">
  <validate>
    <!--TEST STRATEGY: Validate that jsp\:forward can forward a request  to a JSP page within the same context using  a page-relative path.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/forward/positiveForwardPageRelative.html" ignoreWhitespace="true" label="A request can be forwarded to a JSP page, within the  same context, using a page relative-path.  JavaServer Pages Specification 1.2 Sec. 4.5" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/forward/positiveForwardPageRelativeHtml.jsp" label="positiveForwardPageRelativeHtmlTest">
  <validate>
    <!--TEST STRATEGY: Validate that jsp\:forward can forward a request  to a static resource within the same context using  a page-relative path.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/forward/positiveForwardPageRelativeHtml.html" ignoreWhitespace="true" label="A request can be forwarded to a static resource, within the same context, using a page-relative path.  JavaServer Pages Specification 1.2 Sec. 4.5" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/forward/positiveRequestAttrCtxRelative.jsp" label="positiveForwardRequestAttrCtxRelativeTest">
  <validate>
    <!--TEST STRATEGY: Validate that jsp\:forward can properly accept a  request-time attribute containing a context-relative path value.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/forward/positiveRequestAttrCtxRelative.html" ignoreWhitespace="true" label="The page attribute of jsp\:forward can accept a request-time attribute with a context-relative path value. JavaServer Pages v1.2, Sec. 4.5" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/forward/positiveRequestAttrPageRelative.jsp" label="positiveForwardRequestAttrPageRelativeTest">
  <validate>
    <!--TEST STRATEGY: Validate that jsp\:forward can properly accept a  request-time attribute containing a page-relative path value.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/forward/positiveRequestAttrPageRelative.html" ignoreWhitespace="true" label="The page attribute of jsp\:forward can accept a request-time attribute with a page-relative path value. JavaServer Pages v1.2, Sec. 4.5" />
  </validate>
</request>

