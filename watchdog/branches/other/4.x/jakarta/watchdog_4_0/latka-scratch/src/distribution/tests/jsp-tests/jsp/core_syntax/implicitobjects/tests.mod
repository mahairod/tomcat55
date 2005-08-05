<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkSession.jsp" label="checkSessionTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  session scripting variable is of type  javax.servlet.http.HttpSession and that a method can be called against it.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkSession.html" ignoreWhitespace="true" label="The session scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.http.HttpSession.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkException.jsp" label="checkExceptionTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  exception scripting variable is of an instance of the exception type thrown (a subclass of java.lang.Throwable).-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkException.html" ignoreWhitespace="true" label="The exception scripting variable is implictly made made available to the scripting environment (if the JSP page is an error page) and is associated with  an object of type java.lang.Throwable.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkOut.jsp" label="checkOutTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  out scripting variable is of type  javax.servlet.jsp.JspWriter.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkOut.html" ignoreWhitespace="true" label="The out scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.jsp.JspWriter.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkPage.jsp" label="checkPageTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  page scripting variable is of type  java.lang.Object.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkPage.html" ignoreWhitespace="true" label="The page scripting variable is implictly made made available to the scripting environment and and is associated with an object of type java.lang.Object.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkPageContext.jsp" label="checkPageContextTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  pageContext scripting variable is of type  javax.servlet.jsp.PageContext and that a method can be called against it.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkPageContext.html" ignoreWhitespace="true" label="The pageContext scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.jsp.PageContext.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkRequest.jsp?Years=2" label="checkRequestTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  request scripting variable is of type  javax.servlet.Request (parent class of HttpServletRequest) and that a method can be called against it.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkRequest.html" ignoreWhitespace="true" label="The request scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.http.HttpServletRequest or javax.servlet.ServletRequest (protocol dependant).  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkResponse.jsp" label="checkResponseTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  response scripting variable is of type  javax.servlet.Response (parent class of HttpServletResponse) and that a method can be called against it.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkResponse.html" ignoreWhitespace="true" label="The response scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.http.HttpServletResponse or javax.servlet.ServletResponse (protocol dependant).  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/implicitobjects/checkApplication.jsp" label="checkApplicationTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  application scripting variable is of type  javax.servlet.ServletContext that a method can be called against it.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkApplication.html" ignoreWhitespace="true" label="The application scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.ServletContext.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

