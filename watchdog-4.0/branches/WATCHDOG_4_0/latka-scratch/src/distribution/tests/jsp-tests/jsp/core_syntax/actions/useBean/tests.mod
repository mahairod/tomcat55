<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveBeanNameType.jsp" label="positiveBeanNameTypeTest">
  <validate>
    <!--TEST STRATEGY: Use jsp\:useBean to create a bean where the beanName and type attributes have the same values.  Verify that the bean can be used by invoking a method on the bean inside a scriplet.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/positiveBeanNameType.html" ignoreWhitespace="true" label="A bean can be declared using the beanName and type attributes, where type is the same class specified by the beanName attribute. JavaServer Pages Specification v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveBeanNameTypeCast.jsp" label="positiveBeanNameTypeCastTest">
  <validate>
    <!--TEST STRATEGY: Use jsp\:useBean to create a bean where the beanName specifies one particular type, and type specifies a superclass of the value specified by beanName.  Verify that the bean can be used by invoking a method on the bean inside a scriplet.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/positiveBeanNameTypeCast.html" ignoreWhitespace="true" label="A bean can be declared using the beanName and type attributes, where type is a superclass of the class specified by the beanName attribute. JavaServer Pages Specification v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveBeanNameSerialized.jsp" label="positiveBeanNameSerializedTest">
  <validate>
    <!--TEST STRATEGY: Create a bean using useBean action where beanName refers to a serialized bean  and call a method on the bean to verify  that the serialized instance returns an expected value.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/positiveBeanNameSerialized.html" ignoreWhitespace="true" label="The container must be able to instantiate  a serialized bean specified in the beanName  attribute of jsp\:useBean.  JavaServer Pages Specification v1.2, JSP 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveBodyNew.jsp" label="positiveBodyNewTest">
  <validate>
    <!--TEST STRATEGY: Using jsp\:useBean, create a new instance. Within the body of the jsp\:useBean action, use jsp\:setProperty to initialize a Bean property.  After closing the jsp\:useBean action, use jsp\:getProperty to validate  that the property was indeed set.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/positiveBodyNew.html" ignoreWhitespace="true" label="Bean properties can be set within the body of the jsp\:useBean action. JavaServer Pages Specification v1.2, Sec 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positivePageScopedObject.jsp" label="positivePageScopedObjectTest">
  <validate>
    <!--TEST STRATEGY: In one JSP page, create a new bean object using jsp\:useBean with the scope set to 'page'.  After the object has been created, forward the request to a second JSP page to validate that an object associated with the same ID used in the first JSP page is not available in the current PageContext.-->
    <statusCode label="Bean objects created with 'page' scope, will be available for the current page only.  The reference to the bean must be discarded upon completion of the current request by the page body. JavaServer Pages Specfication v1.2, Sec. 4.1" />
    <responseHeader headerName="status" headerValue="Test Status=PASSED" label="Bean objects created with 'page' scope, will be available for the current page only.  The reference to the bean must be discarded upon completion of the current request by the page body. JavaServer Pages Specfication v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveRequestScopedObject.jsp" label="positiveRequestScopedObjectTest">
  <validate>
    <!--TEST STRATEGY: In one JSP page, create a new bean object using jsp\:useBean with the scope set to 'request'.  After the object has been created, forward the request to a second JSP page to validate that an object associated with the same ID used in the first JSP page is available in the current HttpServletRequest.-->
    <statusCode label="Bean objects created with 'request' scope, will be available in the current page's ServletRequest object. JavaServer Pages Specfication v1.2, Sec. 4.1" />
    <responseHeader headerName="status" headerValue="Test Status=PASSED" label="Bean objects created with 'request' scope, will be available in the current page's ServletRequest object. JavaServer Pages Specfication v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveSessionScopedObject.jsp" label="positiveSessionScopedObjectTest">
  <validate>
    <!--TEST STRATEGY: In one JSP page, create a new bean object using jsp\:useBean with the scope set to 'session'.  After the object has been created, forward the request to a second JSP page to validate that an object associated with the same ID used in the first JSP page is available in the current HttpSession.-->
    <statusCode label="Bean objects created with 'session' scope, will be available in the current page's HttpSession object. JavaServer Pages Specfication v1.2, Sec. 4.1" />
    <responseHeader headerName="status" headerValue="Test Status=PASSED" label="Bean objects created with 'session' scope, will be available in the current page's HttpSession object. JavaServer Pages Specfication v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveApplicationScopedObject.jsp" label="positiveApplicationScopedObjectTest">
  <validate>
    <!--TEST STRATEGY: In one JSP page, create a new bean object using jsp\:useBean with the scope set to 'application'.  After the object has been created, forward the request to a second JSP page to validate that an object associated with the same ID used in the first JSP page is available in the current ServletContext.-->
    <statusCode label="Bean objects created with 'application' scope, will be available in the current page's ServletContext object. JavaServer Pages Specfication v1.2, Sec. 4.1" />
    <responseHeader headerName="status" headerValue="Test Status=PASSED" label="Bean objects created with 'application' scope, will be available in the current page's ServletContext object. JavaServer Pages Specfication v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveNoBody.jsp" label="positiveNoBodyTest">
  <validate>
    <!--TEST STRATEGY: Explicit test to ensure that the jsp\:useBean action can be used without a body.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/positiveNoBody.html" ignoreWhitespace="true" label="The jsp\:useBean action does not require a body.  JavaServer Pages Specification v1.2, Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/positiveClassTypeCast.jsp" label="positiveClassTypeCastTest">
  <validate>
    <!--TEST STRATEGY: Create a new bean instance with a particulare class set for the class attribute, and a  parent class for the type attribute.  Validate That the instance is cast without an Exception.-->
    <statusCode label="Using 'class' and 'type' together in useBean tag, 'Class' is assignable to 'type'.  JavaServler Pages Specification v1.2, Sec 4.1" />
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/positiveClassTypeCast.html" ignoreWhitespace="true" label="Using 'class' and 'type' together in useBean tag, 'Class' is assignable to 'type'.  JavaServler Pages Specification v1.2, Sec 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/negativeDuplicateIDFatalTranslationError.jsp" label="negativeDuplicateIDFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Create two beans with the same id attribute. Validate that a Fatal Translation error occurs.-->
    <statusCode code="500" label="Duplicate useBean id's found in the same translation unit shall result in a fatal translation error. JavaServer Pages Specification 1.2 Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/negativeSessionScopeFatalTranslationError.jsp" label="negativeSessionScopeFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Use the page directive to set the session attribute to false and then declare a bean with session scope. Validate that a Fatal Translation error occurs.-->
    <statusCode code="500" label="It is a fatal translation error to attempt to use session scope when the JSP page has declared via the page directive, that it does not participate in a session. JavaServer Pages Specification 1.2 Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/negativeClassCastExceptionFwd.jsp" label="negativeClassCastExceptionTest">
  <validate>
    <!--TEST STRATEGY: In one JSP page, declare a bean of a particular type with session scope.  Once declared, this page will forward to a second JSP page which will try to  reference the previously declared bean in the session  scope, but will define the type attribute with  an incompatible type.-->
    <statusCode label="A java.lang.ClassCastException shall occur at request time when the assignment of the object referenced to the scripting variable fails. JavaServer Pages Specification 1.2 Sec. 4.1" />
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/negativeClassCastException.html" ignoreWhitespace="true" label="A java.lang.ClassCastException shall occur at request time when the assignment of the object referenced to the scripting variable fails. JavaServer Pages Specification 1.2 Sec. 4.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/actions/useBean/negativeNotFoundTypeInstantiationException.jsp" label="negativeNotFoundTypeInstantiationExceptionTest">
  <validate>
    <!--TEST STRATEGY: Define a new bean within the JSP page without class or beanName defined.  Catch the Exception and print a message.-->
    <statusCode label="If the object specified by useBean is not found in the specified scope and neither class nor beanName are given, a java.lang.InstantiationException  shall occur. JavaServer Pages Specification 1.2 Sec. 4.1" />
    <goldenFile fileName="${jsp-wgdir}/core_syntax/actions/useBean/negativeNotFoundTypeInstantiationException.html" ignoreWhitespace="true" label="If the object specified by useBean is not found in the specified scope and neither class nor beanName are given, a java.lang.InstantiationException  shall occur. JavaServer Pages Specification 1.2 Sec. 4.1" />
  </validate>
</request>

