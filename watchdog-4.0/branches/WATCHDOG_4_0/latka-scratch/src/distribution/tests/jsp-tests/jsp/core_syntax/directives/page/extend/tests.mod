<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/extend/positiveExtends.jsp" label="positiveExtendsTest">
  <validate>
    <!--TEST STRATEGY: Provide the extends attribute with a fully qualified class.  The resulting JSP implementation class will use instanceof to validate that this page instance is an instance of the class that it extends.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/extend/positiveExtends.html" ignoreWhitespace="true" label="The extends attribute of the page directive identifies a fully qualfied class name into which the JSP page transformed. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/extend/negativeDuplicateExtendsFatalTranslationError.jsp" label="negativeDuplicateExtendsFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two extends attributes. Validate that a fatal translation error occurs.-->
    <statusCode code="500" label="Duplicate extends attributes within a given translation unit shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

