/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.jasper.compiler;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import javax.servlet.jsp.tagext.*;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.apache.jasper.*;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.xmlparser.XMLEncodingDetector;

/**
 * Controller for the parsing of a JSP page.
 * <p>
 * A translation unit (JSP source file and any files included via the
 * include directive) may involve the processing of JSP pages
 * written with different syntaxes (currently the original JSP syntax,
 * as well as the XML syntax (as of JSP 1.2)). This class encapsulates 
 * the behavior related to the selection and invocation of 
 * the proper parser.
 *
 * @author Pierre Delisle
 * @author Jan Luehe
 */
class ParserController {

    private static final String CHARSET = "charset=";

    private JspCompilationContext ctxt;
    private Compiler compiler;
    private PageInfo pageInfo;
    private ErrorDispatcher err;

    /*
     * Document information which tells us what
     * kind of document we are dealing with.
     */
    private boolean isXml;

    /*
     * A stack to keep track of the 'current base directory'
     * for include directives that refer to relative paths.
     */
    private Stack baseDirStack = new Stack();
    
    /*
     * Static information used in the process of figuring out
     * the kind of document we're dealing with.
     */
    private static final String JSP_ROOT_TAG = "<jsp:root";

    /*
     * Tells if the file being processed is the "top" file
     * in the translation unit.
     */
    private boolean isTopFile = true;

    /*
     * Constructor
     */
    public ParserController(JspCompilationContext ctxt, Compiler compiler) {
        this.ctxt = ctxt; 
	this.compiler = compiler;
	this.pageInfo = compiler.getPageInfo();
	this.err = compiler.getErrorDispatcher();
    }

    public JspCompilationContext getJspCompilationContext () {
	return ctxt;
    }

    public Compiler getCompiler () {
	return compiler;
    }

    /**
     * Parses a JSP page or tag file. This is invoked by the compiler.
     *
     * @param inFileName The path to the JSP page or tag file to be parsed.
     */
    public Node.Nodes parse(String inFileName)
	        throws FileNotFoundException, JasperException, IOException {
	// If we're parsing a packaged tag file or a resource included by it
	// (using an include directive), ctxt.getTagFileJar() returns the 
	// JAR file from which to read the tag file or included resource,
	// respectively.
	return parse(inFileName, null, ctxt.getTagFileJar());
    }

    /**
     * Processes an include directive with the given path.
     *
     * @param inFileName The path to the resource to be included.
     * @param parent The parent node of the include directive.
     * @param jarFile The JAR file from which to read the included resource,
     * or null of the included resource is to be read from the filesystem
     */
    public Node.Nodes parse(String inFileName, Node parent, JarFile jarFile)
	        throws FileNotFoundException, JasperException, IOException {
	return parse(inFileName, parent, ctxt.isTagFile(), false, jarFile);
    }

    /**
     * Extracts tag file directive information from the tag file with the
     * given name.
     *
     * This is invoked by the compiler 
     *
     * @param inFileName The name of the tag file to be parsed.
     */
    public Node.Nodes parseTagFileDirectives(String inFileName)
	        throws FileNotFoundException, JasperException, IOException {
	isTopFile = true;
	return parse(inFileName, null, true, true,
		     (JarFile) ctxt.getTagFileJars().get(inFileName));
    }

    /**
     * Parses the JSP page or tag file with the given path name.
     *
     * @param inFileName The name of the JSP page or tag file to be parsed.
     * @param parent The parent node (non-null when processing an include
     * directive)
     * @param isTagFile true if file to be parsed is tag file, and false if it
     * is a regular JSP page
     * @param directivesOnly true if the file to be parsed is a tag file and
     * we are only interested in the directives needed for constructing a
     * TagFileInfo.
     * @param jarFile The JAR file from which to read the JSP page or tag file,
     * or null if the JSP page or tag file is to be read from the filesystem
     */
    private Node.Nodes parse(String inFileName,
			     Node parent,
			     boolean isTagFile,
			     boolean directivesOnly,
			     JarFile jarFile)
	        throws FileNotFoundException, JasperException, IOException {

	Node.Nodes parsedPage = null;
	String absFileName = resolveFileName(inFileName);

	// Figure out what type of JSP document and encoding type we are
	// dealing with
	String encoding = figureOutJspDocument(absFileName, jarFile);

	if (isTopFile) {
	    pageInfo.setIsXml(isXml);
	    isTopFile = false;
	} else {
	    compiler.getPageInfo().addDependant(absFileName);
	}

	// Dispatch to the proper parser
	if (isXml) {
	    InputStream inStream = null;
	    try {
		inStream = JspUtil.getInputStream(absFileName, jarFile, ctxt,
						  err);
		parsedPage = JspDocumentParser.parse(this, absFileName,
						     inStream, parent,
						     isTagFile,
						     directivesOnly);
	    } finally {
		if (inStream != null) {
		    try {
			inStream.close();
		    } catch (Exception any) {
		    }
		}
	    }
	} else {
	    InputStreamReader inStreamReader = null;
	    try {
		inStreamReader = JspUtil.getReader(absFileName, encoding,
						   jarFile, ctxt, err);
		JspReader jspReader = new JspReader(ctxt, absFileName,
						    encoding, inStreamReader,
						    err);
                parsedPage = Parser.parse(this, jspReader, parent, isTagFile,
					  directivesOnly, jarFile);
            } finally {
		if (inStreamReader != null) {
		    try {
			inStreamReader.close();
		    } catch (Exception any) {
		    }
		}
	    }
	}

	baseDirStack.pop();

	return parsedPage;
    }

    /**
     * Determines the properties of the given page or tag file.
     * The properties to be determined are:
     *
     *   - Syntax (JSP or XML).
     *     This information is supplied by setting the instance variable
     *     'isXml'.
     *
     *   - Source Encoding.
     *     This information is supplied as the return value.
     *
     * If these properties are already specified in the jsp-config element
     * in web.xml, then they are used.
     *
     * @return The source encoding 
     */
    private String figureOutJspDocument(String fname, JarFile jarFile)
	        throws JasperException, IOException {

	boolean isXmlFound = false;
	isXml = false;

	if (pageInfo.isXmlSpecified()) {
	    // If <is-xml> is specified in a <jsp-property-group>, it is used.
	    isXml = pageInfo.isXml();
	    isXmlFound = true;
	} else if (fname.endsWith(".jspx")) {
	    isXml = true;
	    isXmlFound = true;
	}
	
	String sourceEnc = null;
	if (isXmlFound && !isXml) {
	    // JSP syntax
	    if (pageInfo.getPageEncoding() != null) {
		// encoding specified in jsp-config (used only by JSP syntax)
		return pageInfo.getPageEncoding();
	    } else {
		// We don't know the encoding
		sourceEnc = "ISO-8859-1";
	    }
	} else {
	    // XML syntax or unknown, autodetect encoding ...
	    Object[] ret = XMLEncodingDetector.getEncoding(fname, jarFile,
							   ctxt, err);
	    sourceEnc = (String) ret[0];
	    boolean isEncodingSetInProlog = ((Boolean) ret[1]).booleanValue();
	    if (isEncodingSetInProlog) {
		// Prolog present only in XML syntax
		isXml = true;
		if (isTopFile) {
		    String jspConfigPageEnc = pageInfo.getPageEncoding();
		    if (jspConfigPageEnc != null
			    && !jspConfigPageEnc.equals(sourceEnc)) {
			err.jspError(
			    "jsp.error.page.prolog_config_encoding_conflict",
			    sourceEnc, jspConfigPageEnc);
		    }
		    pageInfo.setXmlPrologEncoding(sourceEnc);
		}
	    } else if (sourceEnc.equals("UTF-8")) {
		/*
		 * We don't know if we're dealing with an XML document
		 * unless isXml is true, but even if isXml is true, we don't
		 * know if we're dealing with a JSP document that satisfies
		 * the encoding auto-detection rules (the JSP document may not
		 * have an XML prolog and start with <jsp:root ...>). 
		 * We need to be careful, because the page may be encoded in
		 * ISO-8859-1 (or something entirely different), and may
		 * contain byte sequences that will cause a UTF-8 converter to
		 * throw exceptions. 
		 * It is safe to use a source encoding of ISO-8859-1 in this
		 * case, as there are no invalid byte sequences in ISO-8859-1,
		 * and the byte/character sequences we're looking for are
		 * identical in either encoding (both UTF-8 and ISO-8859-1 are
		 * extensions of ASCII).
		 */
		sourceEnc = "ISO-8859-1";
	    }
	}

	if (isXml) {
	    return sourceEnc;
	}

	JspReader jspReader = null;
	try {
	    jspReader = new JspReader(ctxt, fname, sourceEnc, jarFile, err);
	} catch (FileNotFoundException ex) {
	    throw new JasperException(ex);
	}
        jspReader.setSingleFile(true);
        Mark startMark = jspReader.mark();

	if (!isXmlFound) {
	    // Check for the jsp:root tag
	    // No check for xml prolog, since nothing prevents a page
	    // to output XML and still use JSP syntax.
	    jspReader.reset(startMark);
	    Mark mark = jspReader.skipUntil(JSP_ROOT_TAG);
	    if (mark != null) {
	        isXml = true;
		return sourceEnc;
	    } else {
	        isXml = false;
	    }
	}

	// At this point we know it's JSP syntax ...
	if (pageInfo.getPageEncoding() != null) {
	    return pageInfo.getPageEncoding();
	} else {
	    return getSourceEncodingForJspSyntax(jspReader, startMark);
	}
    }
    
    /*
     * Determines page source encoding for JSP page or tag file in JSP syntax
     */
    private String getSourceEncodingForJspSyntax(JspReader jspReader,
						 Mark startMark)
	        throws JasperException {

	String encoding = null;

	jspReader.reset(startMark);
	while (jspReader.skipUntil("<%@") != null) {
	    jspReader.skipSpaces();
	    // compare for "tag ", so we don't match "taglib"
	    if (jspReader.matches("tag ") || jspReader.matches("page")) {
		jspReader.skipSpaces();
		Attributes attrs = Parser.parseAttributes(this, jspReader);
		encoding = attrs.getValue("pageEncoding");
		if (encoding != null) {
		    break;
		}
		String contentType = attrs.getValue("contentType");
		if (contentType != null) {
		    int loc = contentType.indexOf(CHARSET);
		    if (loc != -1) {
			encoding = contentType.substring(loc
							 + CHARSET.length());
			break;
		    }
		}
	    }
	}

	if (encoding == null) {
	    // Default to "ISO-8859-1" per JSP spec
	    encoding = "ISO-8859-1";
	}

	return encoding;
    }

    /*
     * Resolve the name of the file and update
     * baseDirStack() to keep track ot the current
     * base directory for each included file.
     * The 'root' file is always an 'absolute' path,
     * so no need to put an initial value in the
     * baseDirStack.
     */
    private String resolveFileName(String inFileName) {
        String fileName = inFileName.replace('\\', '/');
        boolean isAbsolute = fileName.startsWith("/");
	fileName = isAbsolute ? fileName 
            : (String) baseDirStack.peek() + fileName;
	String baseDir = 
	    fileName.substring(0, fileName.lastIndexOf("/") + 1);
	baseDirStack.push(baseDir);
	return fileName;
    }

}
