<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/scripting/expressions/positiveExpr.jsp" label="positiveExprTest">
  <validate>
    <!--TEST STRATEGY: Validate that the container can correctly support a basic expression by validating the output returned.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/scripting/expressions/positiveExpr.html" ignoreWhitespace="true" label="An expression element in a JSP page is a scripting  language expression that is evaluated and the result is coerced to a String.  The result is subsequently emitted into the current out JspWriter object. JavaServer Pages Specification v1.2, Sec. 2.11.3" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/scripting/expressions/positiveExprComment.jsp" label="positiveExprCommentTest">
  <validate>
    <!--TEST STRATEGY: Validate that an HTML stye comment with an embedded expression returns the value of the expression within the comment.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/scripting/expressions/positiveExprComment.html" ignoreWhitespace="true" label="Expressions can be embedded in HTML comments to produce comments returned in the output stream containing dynamic content. JavaServer Pages Specification v1.2, Sec. 2.5.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/scripting/expressions/positiveExprWhiteSpace.jsp" label="positiveExprWhiteSpaceTest">
  <validate>
    <!--TEST STRATEGY: Validate that the container correctly handles different whitespace values with an expression element.-->
    <goldenFile fileName="${jsp-wgdir}/core_syntax/scripting/expressions/positiveExprWhiteSpace.html" ignoreWhitespace="true" label="Whitespace is optional after the &lt;%\= and before the %&gt; delimiters of the expression element. JavaServer Pages Specification v1.2, Sec. 2.11" />
  </validate>
</request>

