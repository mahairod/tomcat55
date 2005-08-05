<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/scripting/scriptlet/positiveScriptlet.jsp" label="positiveScriptletTest">
  <validate>
    <!--TEST STRATEGY: Correct syntax is used in the scriptlet-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/scripting/scriptlet/positiveScriptlet.html" ignoreWhitespace="true" label="All scriptlet fragments in a given translation  unit are combined in the order they appear in  the JSP page, they must yield a valid statement,  or sequence of statements, in the specified  scripting language. JavaServer Pages Specification v1.2, Sec. 2.11.2" />
  </validate>
</request>

