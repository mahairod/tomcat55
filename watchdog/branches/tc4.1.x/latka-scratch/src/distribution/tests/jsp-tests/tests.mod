<request followRedirects="false" version="1.0" path="/jsp-tests/CheckConfig" label="checkConfigTest">
  <validate>
    <!--TEST STRATEGY: Validate that the object associated with the  config scripting variable is of type  javax.servlet.ServletConfig and that a method can be called against it.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/implicitobjects/checkConfig.html" ignoreWhitespace="true" label="The config scripting variable is implictly made made available to the scripting environment and and is associated with an object of type javax.servlet.ServletConfig.  JavaServer Pages Specification v1.2, Sec. 2.8.3" />
  </validate>
</request>

