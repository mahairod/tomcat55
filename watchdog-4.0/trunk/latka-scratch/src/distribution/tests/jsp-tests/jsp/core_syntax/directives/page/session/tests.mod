<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/session/positiveSession.jsp" label="positiveSessionTest">
  <validate>
    <!--TEST STRATEGY: Set the session attribute to 'true' and validate that the implicit session variable can be accessed and used.-->
    <statusCode label="If the session attribute of the page directive  is set to 'true', then the implicit script  language variable named 'session' of type javax.servlet.http.HttpSession references the current/new session for the page JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/session/positiveSessionDefault.jsp" label="positiveSessionDefaultTest">
  <validate>
    <!--TEST STRATEGY: Do not set the session attribute in the page. Validate that the implicit session variable can be accessed and used.-->
    <statusCode label="The implicit session session variable will be available to the page by default as the default value for the session attribute is 'true' JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/session/negativeSessionFatalTranslationError.jsp" label="negativeSessionFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Validate that setting the session attribute to false will result in a fatal translation error if the  implicit session variable is referenced.-->
    <statusCode code="500" label="When the session attribute is set to 'false', the JSP page does not participate in a session; the implicit session variable is unavailable, and any reference to it within the body of the JSP page is illegal and shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/session/negativeDuplicateSessionFatalTranslationError.jsp" label="negativeDuplicateSessionFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two session attributes. Validate that a fatal translation error occurs.-->
    <statusCode code="500" label="Duplicate session attributes within a given translation unit shall result in a fatal translation error. JavaServer Pages Specification v1.2, Sec 2.10.1" />
  </validate>
</request>

