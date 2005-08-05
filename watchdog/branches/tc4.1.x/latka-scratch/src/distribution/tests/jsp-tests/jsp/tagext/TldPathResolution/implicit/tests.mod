<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/implicit/positiveImplicitJarMultiTld.jsp" label="positiveImplicitJarMultiTldTest">
  <validate>
    <!--TEST STRATEGY: Define two distinct tags for this page, and use the tags so that output will be generated.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TldPathResolution/implicit/positiveImplicitJarMultiTld.html" ignoreWhitespace="true" label="Verify that the container, with no explicit taglib mappings within the web.xml, can recognize multiple tlds stored in a jar, and create the appropriate  mapping so that the client can use the tags. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.4 JSP 7.3.8" />
  </validate>
</request>

