<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/language/positiveLang.jsp" label="positiveLangTest">
  <validate>
    <!--TEST STRATEGY: Validate that the language attribute can be set to 'java' without an error.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/language/positiveLang.html" ignoreWhitespace="true" label="The only defined and required scripting language value for the language attribute is 'java' JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/language/negativeDuplicateLanguageFatalTranslationError.jsp" label="negativeDuplicateLanguageFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two language attributes. Validate that a fatal translation error occurs.-->
    <statusCode code="500" label="Duplicate language attributes within a given translation unit shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

