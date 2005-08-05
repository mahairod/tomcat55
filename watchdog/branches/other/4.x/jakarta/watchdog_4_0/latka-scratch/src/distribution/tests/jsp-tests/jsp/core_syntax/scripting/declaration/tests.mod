<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/scripting/declaration/positiveDeclaration.jsp" label="positiveDeclarationTest">
  <validate>
    <!--TEST STRATEGY: Validate the scripting declarations are properly recognized, by declaring and assigning a value to an int variable, and displaying the value of the variable.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/scripting/declaration/positiveDeclaration.html" ignoreWhitespace="true" label="Declarations should be a complete declarative statement,  or sequence thereof, according to the syntax of the  scripting language specified. JavaServer Pages Specification v1.2, Sec. 2.11.1" />
  </validate>
</request>

