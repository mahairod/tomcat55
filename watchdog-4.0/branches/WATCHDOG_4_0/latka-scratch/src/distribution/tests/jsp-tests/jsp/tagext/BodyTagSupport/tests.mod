<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/BodyTagSupport/positiveDoInitBody.jsp" label="positiveDoInitBodyTest">
  <validate>
    <!--TEST STRATEGY: Validate that doInitBody() is called by the container.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/BodyTagSupport/positiveDoInitBody.html" ignoreWhitespace="true" label="BodyTagSupport.doInitBody() will be called before the first body evaluation. JavaServer Pages Specification v1.2, Sec. 10.2.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/BodyTagSupport/positiveDoAfterBody.jsp" label="positiveBTSDoAfterBodyTest">
  <validate>
    <!--TEST STRATEGY: Validate that doAfterBody() is called by the container.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/BodyTagSupport/positiveDoAfterBody.html" ignoreWhitespace="true" label="BodyTagSupport.doAfterBody() will be called after the  body has been evaluated.  The body will not be  reevaluated and the page processing will continue. JavaServerPages Specification v1.2, Sec. 10.2.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/BodyTagSupport/positiveDoEndTag.jsp" label="positiveBTSDoEndTagTest">
  <validate>
    <!--TEST STRATEGY: Validate that doEndTag() is called by the container.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/BodyTagSupport/positiveDoEndTag.html" ignoreWhitespace="true" label="BodyTagSupport.doEndTag() by default will return EVAL_PAGE when processing the end tag. JavaServer Pages Specification v1.2, Sec. 10.2.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/BodyTagSupport/positiveLifeCycle.jsp" label="positiveLifeCycleTest">
  <validate>
    <!--TEST STRATEGY: Validate that the appropriate methods are called in order when processing the tag.  NOTE\:  release() is not tested as as the point release() is called is container dependant.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/BodyTagSupport/positiveLifeCycle.html" ignoreWhitespace="true" label="The complete lifecycle for a tag extending BodyTagSupport will contain calls to doStartTag(), doInitBody(), doAfterBody(), doEndTag(), and release(). JavaServer Pages Specification v1.2, Sec 10.2.3" />
  </validate>
</request>

