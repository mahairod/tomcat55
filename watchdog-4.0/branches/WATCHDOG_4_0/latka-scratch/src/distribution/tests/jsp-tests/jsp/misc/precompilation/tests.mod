<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/misc/precompilation/precompile.jsp?jsp_precompile" label="precompileNoValueTest">
  <validate>
    <!--TEST STRATEGY: Validate that no response body is returned when jsp-tests has no value.-->
    <statusCode label="If the jsp-tests request parameter has no value, the request will not be delivered to the target JSP page. JavaServer Pages Specification v1.2, Sec. 8.4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/misc/precompilation/precompile.jsp?jsp_precompile=false" label="precompileFalseTest">
  <validate>
    <!--TEST STRATEGY: Validate that no response body is returned when jsp-tests is set to false.-->
    <statusCode label="If the jsp-tests request parameter is set to false, the request will not be delivered to the target JSP page. JavaServer Pages Specification v1.2, Sec. 8.4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/misc/precompilation/precompile.jsp?jsp_precompile=true" label="precompileTrueTest">
  <validate>
    <!--TEST STRATEGY: Validate that no response body is returned when jsp-tests is set to true.-->
    <statusCode label="If the jsp-tests request parameter is set to true, the request will not be delivered to the target JSP page. JavaServer Pages Specification v1.2, Sec. 8.4.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/misc/precompilation/precompile.jsp?jsp_precompile=any_invalid_value" label="precompileNegativeTest">
  <validate>
    <!--TEST STRATEGY: Set the jsp-tests request paramter to a non valid value and validate that a 500 error occurs.-->
    <statusCode code="500" label="Valid parameter values for jsp-tests are 'true', 'false', and no value.  Any other value will result in an HTTP Error; 500 (Server Error). JavaServer Pages specification v1.2, Sec. 8.4.2" />
  </validate>
</request>

