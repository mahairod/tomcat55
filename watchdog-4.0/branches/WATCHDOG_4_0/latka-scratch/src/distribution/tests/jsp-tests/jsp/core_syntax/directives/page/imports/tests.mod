<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/imports/positiveImport.jsp" label="positiveImportTest">
  <validate>
    <!--TEST STRATEGY: Use the import attribute to import 'java.util.Properties'.  Validated that a Properties object can be created and used.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/imports/positiveImport.html" ignoreWhitespace="true" label="The import attribute of the page directive  denotes classes that the translated JSP page will import and thus making these classes available to the scripting environment. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/imports/implicitImportLang.jsp" label="implicitImportLangTest">
  <validate>
    <!--TEST STRATEGY: Validate that classes from the java.lang  package are implicitly imported by creating and using a java.lang.Integer object.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/imports/implicitImportLang.html" ignoreWhitespace="true" label="Translated JSP pages must automatically import classes of the java.lang package. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/imports/implicitImportJsp.jsp" label="implicitImportJspTest">
  <validate>
    <!--TEST STRATEGY: Validate that classes from the javax.servlet.jsp package are implicitly imported by calling  JspFactory.getDefaultFactory() method.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/imports/implicitImportJsp.html" ignoreWhitespace="true" label="Translated JSP pages must automatically import classes of the javax.servlet.jsp package. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/imports/implicitImportServlet.jsp" label="implicitImportServletTest">
  <validate>
    <!--TEST STRATEGY: Validate that classes from the javax.servlet package are implicitly imported by creating  and using an instance of RequestDispatcher.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/imports/implicitImportServlet.html" ignoreWhitespace="true" label="Translated JSP pages must automatically import classes of the javax.servlet package. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/imports/implicitImportHttp.jsp" label="implicitImportHttpTest">
  <validate>
    <!--TEST STRATEGY: Validate that classes from the javax.servlet.http package are implicitly imported by creating  and using an instance of HttpUtils.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/imports/implicitImportHttp.html" ignoreWhitespace="true" label="Translated JSP pages must automatically import classes of the javax.servlet.http package. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/imports/positiveMultipleImport.jsp" label="positiveMultipleImportTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two import attributes.-->
    <statusCode label="A fatal translation error shall result if there is more than one occurrence of a page directive attribute with the exception of the import attribute. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

