<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/explicit/positiveAbsTld.jsp" label="positiveAbsTldTest">
  <validate>
    <!--TEST STRATEGY: Using a taglib directive, set the uri attribute to an absolute URI.  If successful, no fatal translation error should occur.-->
    <statusCode label="Verify that the requested tld, using an absoulte URI,  resolves to the appropriate tld. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.2 JSP 7.3.3 JSP 7.3.6  JSP 7.3.6.1 JSP 7.3.6.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/explicit/positiveRelTld.jsp" label="positiveRelTldTest">
  <validate>
    <!--TEST STRATEGY: Using a taglib directive, set the uri attribute to a relative URI.  Verify that the tag can be successfully used.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TldPathResolution/explicit/positiveRelTld.html" ignoreWhitespace="true" label="Verify that the requested tld, using a relative URI,  resolves to the appropriate tld. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.2 JSP 7.3.3 JSP 7.3.6  JSP 7.3.6.1 JSP 7.3.6.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/explicit/positiveAbsJar.jsp" label="positiveAbsJarTest">
  <validate>
    <!--TEST STRATEGY: Using a taglib directive, set the uri attribute to an absolute URI.  If successful, no fatal translation error should occur.-->
    <statusCode label="Verify that the requested tld, using an absoulte URI,  resolves to jar which in turn resolves to a single TLD  (taglib.tld) within the jar. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.2 JSP 7.3.3 JSP 7.3.6  JSP 7.3.6.1 JSP 7.3.6.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/explicit/positiveRelJar.jsp" label="positiveRelJarTest">
  <validate>
    <!--TEST STRATEGY: Using a taglib directive, set the uri attribute to a relative URI.  Verify that the tag can be successfully used.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TldPathResolution/explicit/positiveRelJar.html" ignoreWhitespace="true" label="Verify that the requested tld, using a relative URI,  resolves to a jar which in ture resolves to a single TLD (taglib.tld) within the jar. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.2 JSP 7.3.3 JSP 7.3.6  JSP 7.3.6.1 JSP 7.3.6.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/explicit/positiveDirectTldReference.jsp" label="positiveDirectTldReferenceTest">
  <validate>
    <!--TEST STRATEGY: Using a taglib directive, reference the desired TLD directly.  Verify that the tag can be successfully used.-->
    <goldenFile fileName="${jsp-wgdir}/tagext/TldPathResolution/explicit/positiveDirectTldReference.html" ignoreWhitespace="true" label="Verify that a tag can be used when the URI of the taglib directive refers to the TLD directly. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.6.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/tagext/TldPathResolution/explicit/negativeAbsFatalTranslationError.jsp" label="negativeAbsFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Using a taglib directive, set the uri attribute to an incorrect absolute URI. A fatal translation error should occur.-->
    <statusCode code="500" label="Verify that a fatal translation error is generated when using an absoulte URI,  fails to resolve to a tld. JavaServer Pages Specification 1.2 Sections\: JSP 7.3.6.2" />
  </validate>
</request>

