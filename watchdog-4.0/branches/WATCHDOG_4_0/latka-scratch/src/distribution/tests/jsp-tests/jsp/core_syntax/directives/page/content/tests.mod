<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/content/positiveContenttype.jsp" label="positiveContentTypeTest">
  <validate>
    <!--TEST STRATEGY: Using the page directive, set the  contentType attribute to 'text/plain;charset\=ISO-8859-1'. Verify on the client side that the  Content-Type header was properly set in the  response.-->
    <responseHeader headerName="Content-Type" headerValue="text/plain;charset=ISO-8859-1" label="When the contentType attribute of the page  directive is specified, it will set the character encoding and MIME type in the response to the client. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/content/negativeDuplicateContentFatalTranslationError.jsp" label="negativeDuplicateContentFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with contentType attributes specified.-->
    <statusCode code="500" label="Duplicate contentType attribute/values within a given translation unit shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

