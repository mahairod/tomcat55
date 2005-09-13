<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/misc/persistentValues.jsp" label="persistentValuesTest">
  <validate>
    <!--TEST STRATEGY: testing persistence of tag attributes-->
    <goldenFile fileName="${jsp-wgdir}/tagext/misc/persistentValues.html" ignoreWhitespace="true" label="Tests if the attribute values are retaines when tags are nested inside a tag  Java Server Pages Specification v1.2, Sec 10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/misc/request_time_attributes.jsp" label="request_time_attributesTest">
  <validate>
    <!--TEST STRATEGY: Testing associativity of expression evaluation-->
    <goldenFile fileName="${jsp-wgdir}/tagext/misc/request_time_attributes.html" ignoreWhitespace="true" label="Tests the evaluation order of the expressions if attribute values are runtime expressions Java Server Pages Specification v1.2, Sec 2.13.1" />
  </validate>
</request>

