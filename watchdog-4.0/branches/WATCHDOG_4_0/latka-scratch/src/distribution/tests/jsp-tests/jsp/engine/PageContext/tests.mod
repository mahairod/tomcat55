<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveForward.jsp" label="positiveForwardTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.forward(String) with a page-relative path and validate the result.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveForward.html" ignoreWhitespace="true" label="Calling PageContext.forward(java.lang.String relUrlPath) with a relativeUrlPath that doesn't begin with a leading '/' will forward the current ServletRequest and ServletResponse to another active component in the application relative to the URL of the request that was mapped to the calling JSP. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveGetAttributeAvbl.jsp" label="positiveGetAttributeAvblTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getAttribute() to retrieve an object located in the page scope.  Validate that methods can be successfully called against the returned object.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveGetAttributeAvbl.html" ignoreWhitespace="true" label="PageContext.getAttribute(java.lang.String name) will return the object associated with the name in the page scope. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveGetAttributeNotAvbl.jsp" label="positiveGetAttributeNotAvblTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getAttribute(String) passing in a name in which there is no associated object in the page scope.  Validate that the value returned is null.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveGetAttributeNotAvbl.html" ignoreWhitespace="true" label="PageContext.getAttribute(java.lang.String name) will return null of there is no objected assocated with the passed name in the page scope. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveGetAttributeNamesInScope.jsp" label="positiveGetAttributeNamesInScopeTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getAttributeInScope(int) passing in PageContext.PAGE_SCOPE.  Validate that the expected object is found within the returned enumeration.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveGetAttributeNamesInScope.html" ignoreWhitespace="true" label="PageContext.getAttributeNamesInScope(int scope) will return an enumeration of all attributes in the given scope. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveGetAttributeScope.jsp" label="positiveGetAttributeScopeTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getAttributeScope(String) against an attribute in page scope.  Validate that the proper scope value is returned.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveGetAttributeScope.html" ignoreWhitespace="true" label="PageContext.getAttributeScope(java.lang.String name) returns scope where the attribute is defined. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/checkGetSession.jsp" label="checkGetSessionTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getSession() and verify that the HttpSession instance has the same reference as that referenced by the session scripting variable.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/checkGetSession.html" ignoreWhitespace="true" label="PageContext.getSession() returns the current HttpSession object. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/checkGetResponse.jsp" label="checkGetResponseTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getResponse() and verify that the ServletResponse instance has the same reference as that referenced by the response scripting variable.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/checkGetResponse.html" ignoreWhitespace="true" label="PageContext.getResponse() returns the current ServletResponse object. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/checkGetRequest.jsp" label="checkGetRequestTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getRequest() and verify that the ServletRequest instance has the same reference as that referenced by the request scripting variable.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/checkGetRequest.html" ignoreWhitespace="true" label="PageContext.getRequest() returns the current ServletRequest object. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/checkGetServletContext.jsp" label="checkGetServletContextTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getServletContext() and verify that the ServletContext instance has the same reference as that referenced by the application scripting variable.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/checkGetServletContext.html" ignoreWhitespace="true" label="PageContext.getServletContext() returns the current ServletContext object. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/checkGetServletConfig.jsp" label="checkGetServletConfigTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getServletConfig() and verify that the ServletConfig instance has the same reference as that referenced by the config scripting variable.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/checkGetServletConfig.html" ignoreWhitespace="true" label="PageContext.getServletConfig() returns the current ServletConfig object. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/checkGetOut.jsp" label="checkGetOutTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.getOut() and verify that the JspWriter instance has the same reference as that referenced by the out scripting variable.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/checkGetOut.html" ignoreWhitespace="true" label="PageContext.getOut() returns the current JspWriter object. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveInclude.jsp" label="positiveIncludeTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.include(String) with a page-relative path and validate the result.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveInclude.html" ignoreWhitespace="true" label="Calling PageContext.include(java.lang.String relUrlPath) with a relativeUrlPath that doesn't begin with a leading '/' will cause the specified content, relative to the URL of the request that was mapped to the calling JSP, to be processed as part of the current ServletRequest and ServletResponse.  JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveRemoveAttribute.jsp" label="positiveRemoveAttributeTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.removeAttribute(String) and verify that the  attribute is indeed removed.-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveRemoveAttribute.html" ignoreWhitespace="true" label="PageContext.removeAttribute(java.lang.String name) will remove the  object reference associated with the passed name, from all scopes. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveSetAttribute.jsp" label="positiveSetAttributeTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.setAttribute(String,Object) and validate that attribute can be obtained via a call to PageContext.getAttribute(String).-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveSetAttribute.html" ignoreWhitespace="true" label="PageContext.setAttribute(java.lang.String name, java.lang.Object attribute) will register the name and object specified within the page scope. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/PageContext/positiveSetAttributeInScope.jsp" label="positiveSetAttributeInScopeTest">
  <validate>
    <!--TEST STRATEGY: Call PageContext.setAttribute(String,Object,int) and validate that the attribute is in the scope specified via a call to PageContext.getAttributeScope(String).-->
    <goldenFile fileName="${jsp-wgdir}/engine/PageContext/positiveSetAttributeInScope.html" ignoreWhitespace="true" label="PageContext.setAttributeInScope(java.lang.String name, java.lang.Object attribute, int) will register the name and object within the specified scope. JavaServer Pages Specification v1.2, Sec. 9.2.1" />
  </validate>
</request>

