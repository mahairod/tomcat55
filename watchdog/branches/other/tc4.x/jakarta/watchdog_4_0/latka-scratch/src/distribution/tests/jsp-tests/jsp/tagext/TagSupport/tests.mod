<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveGetValues.jsp" label="positiveGetValuesTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport getValues() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveGetValues.html" ignoreWhitespace="true" label="Test for getValues() method which returns an enumeration  and print the contents, specified in the Java Server  Pages Specification v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveGetValue.jsp" label="positiveGetValueTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport getValue() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveGetValue.html" ignoreWhitespace="true" label="Test for getValue() method, specified in the Java Server  Pages Specification v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveRemoveValue.jsp" label="positiveRemoveValueTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport removeValue() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveRemoveValue.html" ignoreWhitespace="true" label="Test for removeValue() method, specified in the Java  Server Pages Specification v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveSetValue.jsp" label="positiveSetValueTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport setValue() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveSetValue.html" ignoreWhitespace="true" label="Test for setValue() method, specified in the Java Server  Pages Specification v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveGetParent.jsp" label="positiveGetParentTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport getParent() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveGetParent.html" ignoreWhitespace="true" label="Test for getParent() method which returns the parent class  name, specified in the Java Server Pages Specification  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/default_return_values.jsp" label="default_return_valuesTest">
  <validate>
    <!--TEST STRATEGY: testing TagSupport default return values by calling super.FunctionName-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/default_return_values.html" ignoreWhitespace="true" label="Tests if the default values are returned from the methods of TagSupport class  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveDoEndTag.jsp" label="positiveTSDoEndTagTest">
  <validate>
    <!--TEST STRATEGY: testing TagSupport doEndTag method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveDoEndTag.html" ignoreWhitespace="true" label="Tests that doEndTag metjod is called when the end of the tag is found  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveSetGetId.jsp" label="positiveSetGetIdTest">
  <validate>
    <!--TEST STRATEGY: testing TagSupport getId and setId in the same test-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveSetGetId.html" ignoreWhitespace="true" label="Test for setId and getId methods of TagSupport  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positivefindAncestorWithClass.jsp" label="positivefindAncestorWithClassTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport.findAncestorWithClass method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positivefindAncestorWithClass.html" ignoreWhitespace="true" label="Test for Nested classes. Tests the findAncestorWithClass method for TagSupport  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveDoStartTag.jsp" label="positiveDoStartTagTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport doStartTag() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveDoStartTag.html" ignoreWhitespace="true" label="Test for checking if doStartTag() is called correctly  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveSetPageContext.jsp" label="positiveSetPageContextTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport setPageContext() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveSetPageContext.html" ignoreWhitespace="true" label="Test if setPageContext() can be used to set the page context.Using this page context obj set an attribute which is passed to the jsp page  v1.2, Sec 10.1.4" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagSupport/positiveDoAfterBody.jsp" label="positiveTSDoAfterBodyTest">
  <validate>
    <!--TEST STRATEGY: Testing TagSupport setPageContext() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagSupport/positiveDoAfterBody.html" ignoreWhitespace="true" label="Test the DoAfterBody method of the TagSupport class.     Create a Tag handler that outputs a message a given number of times from the DoAFterBody  v1.2, Sec 10.1.4" />
  </validate>
</request>

