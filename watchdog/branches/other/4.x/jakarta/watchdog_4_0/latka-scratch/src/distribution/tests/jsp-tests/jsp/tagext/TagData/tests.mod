<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagData/positiveTagGetAttribute.jsp" label="positiveTagGetAttributeTest">
  <validate>
    <!--TEST STRATEGY: Testing TagData release() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagData/positiveTagGetAttribute.html" ignoreWhitespace="true" label="Test a TagData object by passing a hashtable and printing  the contents of the hashtable using the getAttribute()  method of TagData., specified in the Java Server Pages  Specification v1.2, Sec 10.5.7" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagData/positiveTagSetAttribute.jsp" label="positiveTagSetAttributeTest">
  <validate>
    <!--TEST STRATEGY: Testing TagData setAttribute() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagData/positiveTagSetAttribute.html" ignoreWhitespace="true" label="Test a TagData object by passing a hashtable created using  setAttribute() method of the TagData and print the contents of the hashtable, specified in the Java Server Pages  Specification v1.2, Sec 10.5.7" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TagData/positiveTagGetAttributeString.jsp" label="positiveTagGetAttributeStringTest">
  <validate>
    <!--TEST STRATEGY: Testing TagData getAttributeString() method-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TagData/positiveTagGetAttributeString.html" ignoreWhitespace="true" label="Test a TagData object by passing a hashtable created using  setAttribute() method of the TagData and print the  contents of the hashtable using the getAttributeString()  method, specified in the Java Server Pages Specification  v1.2, Sec 10.5.7" />
  </validate>
</request>

