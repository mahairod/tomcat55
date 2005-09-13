<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/buffer/positiveBuffAutoflush.jsp" label="positiveBuffAutoflushTest">
  <validate>
    <!--TEST STRATEGY: Leaving the defaults for autoFlush and buffer, validate that the buffer is automatically flushed once the  buffer is full.-->
    <statusCode label="When the page buffer is full, content will automatically be flushed. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/buffer/positiveBuffCreate.jsp" label="positiveBuffCreateTest">
  <validate>
    <!--TEST STRATEGY: Validate that the page can configure a buffer and set the autoFlush attribute to false.   Write data to the output stream and manually  flush the content-->
    <statusCode label="If the buffer attribute is set, it is legal to set autoFlush to 'false'. Doing so requires a manual flush from the page writer. JavaServer Pages Specification v1.2, Sec. 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/buffer/negativeDuplicateBufferFatalTranslationError.jsp" label="negativeDuplicateBufferFatalTranslationErrorTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with two buffer attributes.-->
    <statusCode code="500" label="Duplicate buffer attributes within a given translation unit results in a fatal translation error. JavaServer Pages v1.2, Sec 2.10.1" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/core_syntax/directives/page/buffer/negativeBufferOverflowException.jsp" label="negativeBufferOverflowExceptionTest">
  <validate>
    <!--TEST STRATEGY: Declare a page directive with autoFlush set to false.   Overflow the buffer and verify the Exception is caught.-->
    <statusCode label="If the 'autoFlush' attribute is false, an Exception will be raised if an overflow occurs. JavaServer Pages v1.2, Sec 2.10.1" />
    <goldenFile fileName="${jsp-wgdir}/core_syntax/directives/page/buffer/negativeBufferOverflowException.html" ignoreWhitespace="true" label="If the 'autoFlush' attribute is false, an Exception will be raised if an overflow occurs. JavaServer Pages v1.2, Sec 2.10.1" />
  </validate>
</request>

