<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/getProperty/positiveGetProps.jsp" label="positiveGetPropsTest">
  <validate>
    <!--TEST STRATEGY: Create a bean using jsp\:useBean tag, use jsp\:getProperty to  access and validate the property.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/getProperty/positiveGetProps.html" ignoreWhitespace="true" label="jsp\:getProperty action places the value of a Bean instance property, converted to a String, into the implicit out  object, which is displayed as output. Java Server Pages Specification v1.2, Sec 4.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/getProperty/negativeGetPropObjectNotFoundException.jsp" label="negativeGetPropObjectNotFoundExceptionTest">
  <validate>
    <!--TEST STRATEGY: Access a property of a non-existant bean and catch an exception.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/getProperty/negativeGetPropObjectNotFoundException.html" ignoreWhitespace="true" label="If accessing a property, and the object is not found, a request-time exception is raised. Java Server Pages Specification v1.2, Sec 4.3" />
  </validate>
</request>

