<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/errorpage/positiveDefaultIsErrorPage.jsp" label="positiveDefaultIsErrorPageTest">
  <validate>
    <!--TEST STRATEGY: Verify that the 'isErrorPage' attribute is false by  generating an exception in the called page and then have the error page attempt to access the implicit exception object.-->
    <statusCode code="500" label="The default value of the 'isErrorPage' attribute is false.  JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/errorpage/positiveErrorPage.jsp" label="positiveErrorPageTest">
  <validate>
    <!--TEST STRATEGY: In the initial JSP page, generate a java.lang.Arithmetic Exception by dividing an int value by 0.  Validate the Exception type by using instanceof against the exception object.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/errorpage/positiveErrorPage.html" ignoreWhitespace="true" label="When the isErrorPage attribute is set to true, the implicit exception object will be available and its value is a reference to the offending throwable from the source JSP page in error.  JavaServer Pages Specification v1.2, Sec 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/errorpage/negativeFatalTranslationError.jsp" label="negativeFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Generate an exception from the requested page with the errorPage attribute set.  The Error page has isErrorPage set to false and will attempt to access the exception object.-->
    <statusCode code="500" label="A fatal translation error shall result if a JSP error page has the isErrorPage attribute set to false and an attempt is made to access the implicit exception object. JavaServer Pages Specification v1.2, Sec 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/errorpage/negativeDuplicateErrorPageFatalTranslationError.jsp" label="negativeDuplicateErrorPageFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two errorPage attributes. Validate that a fatal translation error occurs.-->
    <statusCode code="500" label="Duplicate errorPage attributes/values within a given translation unit shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec 2.10.1" />
  </validate>
</request>

