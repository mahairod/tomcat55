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
 */
public class ParserController {
    /*
     * The compilation context
     */
    private JspCompilationContext ctxt;

    /*
     * The Compiler
     */
    private Compiler compiler;

    /*
     * A stack to keep track of the 'current base directory'
     * for include directives that refer to relative paths.
     */
    private Stack baseDirStack = new Stack();

    /*
     * Document information which tells us what
     * kind of document we are dealing with.
     */
    private boolean isXml;
    
    /*
     * Static information used in the process of figuring out
     * the kind of document we're dealing with.
     */
    private static final String JSP_ROOT_TAG   = "<jsp:root";

    /*
     * Tells if the file being processed is the "top" file
     * in the translation unit.
     */
    private boolean isTopFile = true;

    /*
     * The encoding of the "top" file. This encoding is used
     * for included files by default.
     * Defaults to "ISO-8859-1" per JSP spec.
     */
    private String topFileEncoding = "ISO-8859-1"; 
    
    /*
     * The 'new' encoding required to read a page.
     */
    private String newEncoding;


    //*********************************************************************
    // Constructor

    public ParserController(JspCompilationContext ctxt, Compiler compiler) {
        this.ctxt = ctxt; // @@@ can we assert that ctxt is not null?
	this.compiler = compiler;
    }

    public JspCompilationContext getJspCompilationContext () {
	return ctxt;
    }

    public Compiler getCompiler () {
	return compiler;
    }

    //*********************************************************************
    // Parse

    /**
     * Parses a jsp file.  This is invoked by the compiler.
     *
     * @param inFileName The name of the JSP file to be parsed.
     */
    public Node.Nodes parse(String inFileName)
	        throws FileNotFoundException, JasperException, IOException {
	return parse(inFileName, null);
    }

    /**
     * Parses a jsp file.  This is invoked to process an include file.
     *
     * @param inFileName The name of the JSP file to be parsed.
     * @param parent The node for the 'include' directive.
     */
    public Node.Nodes parse(String inFileName, Node parent)
	        throws FileNotFoundException, JasperException, IOException {
	return parseFile(inFileName, parent, ctxt.isTagFile(), false);
    }

    /**
     * Parses a tag file.  This is invoked by the compiler to extract tag
     * file directive information.
     *
     * @param inFileName The name of the tag file to be parsed.
     */
    public Node.Nodes parseTagFile(String inFileName)
	        throws FileNotFoundException, JasperException, IOException {
	isTopFile = true;
	return parseFile(inFileName, null, true, true);
    }

    /**
     * Parse the JSP page provided as an argument.
     * This is invoked recursively to handle 'include' directives.
     *
     * @param inFileName The name of the jsp file to be parsed.
     * @param parent The parent node for the parser, and is non-null when
     *               parsing an included file
     * @param isTagFile true if file to be parsed is tag file, and false if it
     * is a regular jsp page
     * @param directivesOnly true if the file to be parsed is a tag file and
     * we are only interested in the directives needed for constructing a
     * TagFileInfo.
     */
    private Node.Nodes parseFile(String inFileName, Node parent,
				 boolean isTagFile, boolean directivesOnly)
	        throws FileNotFoundException, JasperException, IOException {

	Node.Nodes parsedPage = null;
	String encoding = topFileEncoding;
        InputStreamReader reader = null;
	String absFileName = resolveFileName(inFileName);

	JarFile jarFile = (JarFile) ctxt.getTagFileJars().get(inFileName);

        try {
            // Figure out what type of JSP document we are dealing with
            reader = getReader(absFileName, encoding, jarFile);
            figureOutJspDocument(absFileName, encoding, reader);
	    if (newEncoding != null)
		encoding = newEncoding;
	    if (isTopFile) {
		// Set the "top level" file encoding that will be used
		// for all included files where encoding is not defined.
		topFileEncoding = encoding;
		isTopFile = false;
	    } else {
                compiler.getPageInfo().addDependant(absFileName);
            }
	    try {
		reader.close();
	    } catch (IOException ex) {
	    }

            // dispatch to the proper parser
	    
            reader = getReader(absFileName, encoding, jarFile);
            if (isXml) {
                parsedPage = JspDocumentParser.parse(this, absFileName,
						     reader, parent,
						     isTagFile);
            } else {
		JspReader r = new JspReader(ctxt, absFileName, encoding,
					    reader,
					    compiler.getErrorDispatcher());
                parsedPage = Parser.parse(this, r, parent, isTagFile,
					  directivesOnly);
            }
	    baseDirStack.pop();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
		} catch (Exception any) {
		}
	    }
        }

	return parsedPage;
    }

    /** *******************************************************************
     * Discover the properties of the page by scanning it.
     * Properties to find out are:
     *   * Is it in XML syntax?
     *   * What is the the page encoding
     * If these properties are already specified in the jsp-config element
     * in web.xml, then they are used.
     */

    private void figureOutJspDocument(String file, 
				      String encoding,
				      InputStreamReader reader)
	 throws JasperException
    {
	newEncoding = null;
	PageInfo pageInfo = compiler.getPageInfo();
	boolean isXmlFound = false;
	if (pageInfo.isXmlSpecified()) {
	    // If <is-xml> is specified in a <jsp-property-group>, it is used.
	    isXml = pageInfo.isXml();
	    isXmlFound = true;
	} else if (file.endsWith(".jspx")) {
	    isXml = true;
	    isXmlFound = true;
	}
	
	if (pageInfo.getPageEncoding() != null) {
	    newEncoding = pageInfo.getPageEncoding();
	}

	if (isXmlFound && newEncoding != null)
	    return;	// No need to scan the file

	JspReader jspReader;
	try {
	    jspReader = new JspReader(ctxt, file, encoding, reader,
				      compiler.getErrorDispatcher());
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
	    } else {
	        isXml = false;
	    }
	}

	if (newEncoding != null) {
	    // encoding specified in jsp-config
	    return;
	}

	// Figure out the encoding of the page
	// FIXME: We assume xml parser will take care of
        // encoding for page in XML syntax. Correct?
	if (!isXml) {
	    jspReader.reset(startMark);
	    while (jspReader.skipUntil("<%@") != null) {
		jspReader.skipSpaces();
		if (jspReader.matches( "tag " ) || jspReader.matches("page")) {
		    jspReader.skipSpaces();
		    Attributes attrs = Parser.parseAttributes(this, jspReader);
		    String attribute = "pageEncoding";
		    newEncoding = attrs.getValue("pageEncoding");
		    if (newEncoding == null) {
			String contentType = attrs.getValue("contentType");
			if (contentType != null) {
			    int loc = contentType.indexOf("charset=");
			    if (loc != -1) {
				newEncoding = contentType.substring(loc+8);
				return;
			    }
			}
			if (newEncoding == null)
			    newEncoding = "ISO-8859-1";
		    } else {
			return;
		    }
		}
	    }
	}
    }
    
    //*********************************************************************
    // Utility methods

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

    private InputStreamReader getReader(String file, String encoding,
					JarFile jarFile)
	throws FileNotFoundException, JasperException, IOException {

        InputStream in;
        InputStreamReader reader;

	if (jarFile != null) {
	    ZipEntry jarEntry =
		jarFile.getEntry(file.substring(1, file.length()));
	    in = jarFile.getInputStream(jarEntry);
	} else {
	    in = ctxt.getResourceAsStream(file);
	}
	if (in == null) {
	    throw new FileNotFoundException(file);
	}

	try {
            return new InputStreamReader(in, encoding);
	} catch (UnsupportedEncodingException ex) {
	    throw new JasperException(
                Constants.getString("jsp.error.unsupported.encoding",
				    new Object[]{encoding}));
	}
    }
}
