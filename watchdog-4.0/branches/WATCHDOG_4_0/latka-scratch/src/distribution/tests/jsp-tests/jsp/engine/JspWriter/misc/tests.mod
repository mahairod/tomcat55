<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveClear.jsp" label="positiveClearTest">
  <validate>
    <!--TEST STRATEGY: Using a page with the default buffer size of 8kb, write data to the buffer and call clear.  The cleared data should not be present in the response.-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveClear.html" ignoreWhitespace="true" label="JspWriter.clear() will cause the contents of the output  buffer to be cleared. JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveFlush.jsp" label="positiveFlushTest">
  <validate>
    <!--TEST STRATEGY: Obtain the current buffer size being used, then write and flush some sample text. Validate that the stream was indeed flushed by checking the number of bytes remaining in  the buffer.-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveFlush.html" ignoreWhitespace="true" label="JspWriter.flush() will cause the stream to write any saved content to the intended destiation. JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveGetBufferSize.jsp" label="positiveGetBufferSizeTest">
  <validate>
    <!--TEST STRATEGY: Set the buffer of the page to 5kb.  Call getBufferSize() and validate that the value returned is as expected.-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveGetBufferSize.html" ignoreWhitespace="true" label="JspWriter.getBufferSize() returns the size of the buffer in bytes used by the particular JspWriter instance. JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveGetBufferSizeUnBuffered.jsp" label="positiveGetBufferSizeUnBufferedTest">
  <validate>
    <!--TEST STRATEGY: Set the buffer to 'none' and call getBufferSize(). Validate that the value returned is 0 (zero).-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveGetBufferSizeUnBuffered.html" ignoreWhitespace="true" label="When buffering is disabled within a particular  JSP page, a call to JspWriter.getBufferSize()  will return 0 (zero). JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveGetRemaining.jsp" label="positiveGetRemainingTest">
  <validate>
    <!--TEST STRATEGY: Write 6 bytes of data to the buffer and call getRemaining().  The value returned should be 6 bytes less than the value returned by  getBufferSize().-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveGetRemaining.html" ignoreWhitespace="true" label="JspWriter.getRemaining() will return the number of  unused bytes in the buffer. JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveGetRemainingBufferUnset.jsp" label="positiveGetRemainingBufferUnsetTest">
  <validate>
    <!--TEST STRATEGY: Set the buffer of the JSP page to 'none', and validate that a call to getRemaining() returns 0 (zero).-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveGetRemainingBufferUnset.html" ignoreWhitespace="true" label="JspWriter.getRemaining() will return 0 (zero) if the JSP page doesn't use a buffer. JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveIsAutoFlush.jsp" label="positiveIsAutoFlushTest">
  <validate>
    <!--TEST STRATEGY: JSP pages automatically flush their buffer by default.  Call isAutoFlush() and validated that 'true' is returned.-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveIsAutoFlush.html" ignoreWhitespace="true" label="JspWriter.isAutoFlush() will inidicate whether the JspWriter will flush its buffer automatically. JavaServer Pages Specification v1.2, Sec. 9.2.2" />
  </validate>
</request>

<request followRedirects="false" version="1.0" path="/jsp-tests/jsp/engine/JspWriter/misc/positiveNewLine.jsp" label="positiveNewLineTest">
  <validate>
    <!--TEST STRATEGY: Validate the a call to newLine() produces the expected results.-->
    <goldenFile fileName="${jsp-wgdir}/engine/JspWriter/misc/positiveNewLine.html" ignoreWhitespace="true" label="JspWriter.newLine() will write a line separator as defined by the system property 'line.separator'. JavaServer Pages Specification v1.2 , Sec. 9.2.2" />
  </validate>
</request>

