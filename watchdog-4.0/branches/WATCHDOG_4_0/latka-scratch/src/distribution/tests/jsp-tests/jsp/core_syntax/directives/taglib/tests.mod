<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/taglib/positiveTagLib.jsp" label="positiveTagLibTest">
  <validate>
    <!--TEST STRATEGY: Validate that the taglib directive is recognized by the container by declaring a new tag and  calling an action against that tag.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/taglib/positiveTagLib.html" ignoreWhitespace="true" label="The taglib directive declares that the page  uses a tag library, uniquely identifies  the tag library using a URI and associates a  tag prefix that will distinguish usage of the  actions in the library.  JavaServer Pages Specification v1.2, Sec. 2.10.2" />
  </validate>
</request>

