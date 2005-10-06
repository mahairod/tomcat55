/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * 
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
import javax.servlet.jsp.tagext.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

/**
 * Class implementing a parser for a JSP document, that is, a JSP page in XML
 * syntax.
 *
 * @author Jan Luehe
 */

class JspDocumentParser extends DefaultHandler
            implements LexicalHandler, TagConstants {

    private static final String JSP_VERSION = "version";
    private static final String LEXICAL_HANDLER_PROPERTY
	= "http://xml.org/sax/properties/lexical-handler";

    private ParserController parserController;
    private JspCompilationContext ctxt;    
    private PageInfo pageInfo;

    // XML document source
    private InputSource inputSource;

    private String path;

    // Node representing the XML element currently being parsed
    private Node current;

    // Document locator
    private Locator locator;

    // Flag indicating whether we are inside DTD declarations
    private boolean inDTD;

    private ErrorDispatcher err;
    private boolean isTagFile;
    private boolean directivesOnly;
    private boolean isTop;

    /*
     * Constructor
     */
    public JspDocumentParser(ParserController pc,
			     String path,
			     InputStream inStream,
			     boolean isTagFile,
			     boolean directivesOnly) {
	this.parserController = pc;
	this.ctxt = pc.getJspCompilationContext();
	this.pageInfo = pc.getCompiler().getPageInfo();
	this.err = pc.getCompiler().getErrorDispatcher();
	this.path = path;
	this.inputSource = new InputSource(inStream);
	this.isTagFile = isTagFile;
	this.directivesOnly = directivesOnly;
	this.isTop = true;
    }

    /*
     * Parses a JSP document by responding to SAX events.
     *
     * @throws JasperException
     */
    public static Node.Nodes parse(ParserController pc,
				   String path,
				   InputStream inStream,
				   Node parent,
				   boolean isTagFile,
				   boolean directivesOnly,
				   String pageEnc,
				   String jspConfigPageEnc,
				   boolean isEncodingSpecifiedInProlog)
	        throws JasperException {

	JspDocumentParser jspDocParser = new JspDocumentParser(pc, path,
							       inStream,
							       isTagFile,
							       directivesOnly);
	Node.Nodes pageNodes = null;

	try {

	    // Create dummy root and initialize it with given page encodings
	    Node.Root dummyRoot = new Node.Root(null, parent, true);
	    dummyRoot.setPageEncoding(pageEnc);
	    dummyRoot.setJspConfigPageEncoding(jspConfigPageEnc);
	    dummyRoot.setIsEncodingSpecifiedInProlog(isEncodingSpecifiedInProlog);
	    jspDocParser.current = dummyRoot;
	    if (parent == null) {
		jspDocParser.addInclude(dummyRoot,
			jspDocParser.pageInfo.getIncludePrelude());
	    } else {
		jspDocParser.isTop = false;
	    }

	    // Use the default (non-validating) parser
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    // Preserve xmlns attributes
	    factory.setFeature("http://xml.org/sax/features/namespace-prefixes",
			       true);

	    // Configure the parser
	    SAXParser saxParser = factory.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setProperty(LEXICAL_HANDLER_PROPERTY, jspDocParser);
	    xmlReader.setErrorHandler(jspDocParser);

	    // Parse the input
	    saxParser.parse(jspDocParser.inputSource, jspDocParser);

	    if (parent == null) {
		jspDocParser.addInclude(dummyRoot,
			jspDocParser.pageInfo.getIncludeCoda());
	    }

	    // Create Node.Nodes from dummy root
	    pageNodes = new Node.Nodes(dummyRoot);

	} catch (IOException ioe) {
	    jspDocParser.err.jspError("jsp.error.data.file.read", path, ioe);
	} catch (Exception e) {
	    jspDocParser.err.jspError(e);
	}

	return pageNodes;
    }

    /*
     * Processes the given list of included files.
     *
     * This is used to implement the include-prelude and include-coda
     * subelements of the jsp-config element in web.xml
     */
    private void addInclude(Node parent, List files) throws SAXException {
	if (files != null) {
	    Iterator iter = files.iterator();
	    while (iter.hasNext()) {
		String file = (String) iter.next();
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("", "file", "file", "CDATA", file);

		// Create a dummy Include directive node
		Node includeDir = new Node.IncludeDirective(attrs, 
							   null, // XXX
							   parent);
		processIncludeDirective(file, includeDir);
	    }
	}
    }

    /*
     * Receives notification of the start of an element.
     *
     * This method assigns the given tag attributes to one of 3 buckets:
     * 
     * - "xmlns" attributes that represent (standard or custom) tag libraries.
     * - "xmlns" attributes that do not represent tag libraries.
     * - all remaining attributes.
     *
     * For each "xmlns" attribute that represents a custom tag library, the
     * corresponding TagLibraryInfo object is added to the set of custom
     * tag libraries.
     */
    public void startElement(String uri,
			     String localName,
			     String qName,
			     Attributes attrs) throws SAXException {

	AttributesImpl taglibAttrs = null;
	AttributesImpl nonTaglibAttrs = null;
	AttributesImpl nonTaglibXmlnsAttrs = null;

	checkPrefixes(uri, qName, attrs);

	if (directivesOnly && !localName.startsWith(DIRECTIVE_ACTION)) {
	    return;
	}

	// jsp:text must not have any subelements
	if (TEXT_ACTION.equals(current.getLocalName())) {
	    throw new SAXParseException(
	            Localizer.getMessage("jsp.error.text.has_subelement"),
		    locator);
	}

	Mark start = new Mark(path, locator.getLineNumber(),
			      locator.getColumnNumber());

	if (attrs != null) {
	    /*
	     * Notice that due to a bug in the underlying SAX parser, the
	     * attributes must be enumerated in descending order. 
	     */
	    boolean isTaglib = false;
	    for (int i=attrs.getLength()-1; i>=0; i--) {
		isTaglib = false;
		String attrQName = attrs.getQName(i);
		if (!attrQName.startsWith("xmlns")) {
		    if (nonTaglibAttrs == null) {
			nonTaglibAttrs = new AttributesImpl();
		    }
		    nonTaglibAttrs.addAttribute(attrs.getURI(i),
						attrs.getLocalName(i),
						attrs.getQName(i),
						attrs.getType(i),
						attrs.getValue(i));
		} else {
		    if (attrQName.startsWith("xmlns:jsp")) {
			isTaglib = true;
		    } else {
			String attrUri = attrs.getValue(i);
			// TaglibInfo for this uri already established in
			// startPrefixMapping
			isTaglib = pageInfo.hasTaglib(attrUri);
		    }
		    if (isTaglib) {
			if (taglibAttrs == null) {
			    taglibAttrs = new AttributesImpl();
			}
			taglibAttrs.addAttribute(attrs.getURI(i),
						 attrs.getLocalName(i),
						 attrs.getQName(i),
						 attrs.getType(i),
						 attrs.getValue(i));
		    } else {
			if (nonTaglibXmlnsAttrs == null) {
			    nonTaglibXmlnsAttrs = new AttributesImpl();
			}
			nonTaglibXmlnsAttrs.addAttribute(attrs.getURI(i),
							 attrs.getLocalName(i),
							 attrs.getQName(i),
							 attrs.getType(i),
							 attrs.getValue(i));
		    }
		}
	    }
	}

	Node node = null;

	if ("http://java.sun.com/JSP/Page".equals(uri)) {
	    node = parseStandardAction(qName, localName, nonTaglibAttrs,
				       nonTaglibXmlnsAttrs, taglibAttrs,
				       start, current);
	} else {
	    node = parseCustomAction(qName, localName, uri, nonTaglibAttrs,
				     nonTaglibXmlnsAttrs, taglibAttrs, start,
				     current);
	    if (node == null) {
		node = new Node.UninterpretedTag(qName, localName,
						 nonTaglibAttrs,
						 nonTaglibXmlnsAttrs,
						 taglibAttrs, start, current);
	    }
	}

	current = node;
    }

    /*
     * Receives notification of character data inside an element.
     *
     * @param buf The characters
     * @param offset The start position in the character array
     * @param len The number of characters to use from the character array
     *
     * @throws SAXException
     */
    public void characters(char[] buf,
			   int offset,
			   int len) throws SAXException {
	/*
	 * JSP.6.1.1: All textual nodes that have only white space are to be
	 * dropped from the document, except for nodes in a jsp:text element,
	 * and any leading and trailing white-space-only textual nodes in a
	 * jsp:attribute whose 'trim' attribute is set to FALSE, which are to
	 * be kept verbatim.
	 * JSP.6.2.3 defines white space characters.
	 */
	boolean isAllSpace = true;
	if (!(current instanceof Node.JspText)
	        && !(current instanceof Node.NamedAttribute)) {
	    for (int i=offset; i<offset+len; i++) {
		if (!(buf[i] == ' ' || buf[i] == '\n' || buf[i] == '\r' ||
			buf[i] == '\t' )) {
		    isAllSpace = false;
		    break;
		}
	    }
	}
	if ((current instanceof Node.JspText)
	        || (current instanceof Node.NamedAttribute) || !isAllSpace) {
	    Mark start = new Mark(path, locator.getLineNumber(),
				  locator.getColumnNumber());

	    CharArrayWriter ttext = new CharArrayWriter();
	    int limit = offset + len;
	    int lastCh = 0;
	    for (int i = offset; i < limit; i++) {
		int ch = buf[i];
		if (lastCh == '$' && ch == '{') {
		    if (ttext.size() > 0) {
			new Node.TemplateText(ttext.toString(), start,
					      current);
		        ttext = new CharArrayWriter();
		    }
		    // following "${" to first unquoted "}"
		    i++;
		    boolean singleQ = false;
		    boolean doubleQ = false;
		    lastCh = 0;
		    for (; ; i++) {
			if (i >= limit) {
			    throw new SAXParseException(
				Localizer.getMessage("jsp.error.unterminated",
						     "${"),
				locator);

			}
			ch = buf[i];
			if (lastCh == '\\' && (singleQ || doubleQ)) {
			    ttext.write(ch);
			    lastCh = 0;
			    continue;
			}
			if (ch == '}') {
			    new Node.ELExpression(ttext.toString(), start,
						  current);
			    ttext = new CharArrayWriter();
			    break;
			}
			if (ch == '"')
			    doubleQ = !doubleQ;
			else if (ch == '\'')
			    singleQ = !singleQ;

			ttext.write(ch);
			lastCh = ch;
		    }
		} else {
		    if( (lastCh == '$') && (ch != '{') ) {
                        ttext.write( '$' );
                    }
                    if( ch != '$' ) {
                        ttext.write( ch );
                    }
                }
                lastCh = ch;
	    }
	    if (lastCh == '$') {
		ttext.write('$');
	    }
	    if (ttext.size() > 0) {
		new Node.TemplateText(ttext.toString(), start, current);
	    }
	}
    }

    /*
     * Receives notification of the end of an element.
     */
    public void endElement(String uri,
			   String localName,
			   String qName) throws SAXException {

	if (directivesOnly && !localName.startsWith(DIRECTIVE_ACTION)) {
	    return;
	}

	if (current instanceof Node.NamedAttribute) {
	    boolean isTrim = ((Node.NamedAttribute) current).isTrim();
	    Node.Nodes subElems = ((Node.NamedAttribute) current).getBody();
	    for (int i=0; subElems != null && i<subElems.size(); i++) {
		Node subElem = subElems.getNode(i);
		if (!(subElem instanceof Node.TemplateText)) {
		    continue;
		}
		// Ignore any whitespace (including spaces, carriage returns,
		// line feeds, and tabs, that appear at the beginning and at
		// the end of the body of the <jsp:attribute> action, if the
		// action's 'trim' attribute is set to TRUE (default).
		// In addition, any textual nodes in the <jsp:attribute> that
		// have only white space are dropped from the document, with
		// the exception of leading and trailing white-space-only
		// textual nodes in a <jsp:attribute> whose 'trim' attribute
		// is set to FALSE, which must be kept verbatim.
		if (i == 0) {
		    if (isTrim) {
			((Node.TemplateText) subElem).ltrim();
		    }
		} else if (i == subElems.size()-1) {
		    if (isTrim) {
			((Node.TemplateText) subElem).rtrim();
		    }
		} else {
		    if (((Node.TemplateText) subElem).isAllSpace()) {
			subElems.remove(subElem);
		    }
		}
	    }
	} else if (current instanceof Node.ScriptingElement) {
	    checkScriptingBody((Node.ScriptingElement) current);
	}

	if (current.getParent() != null) {
	    current = current.getParent();
	}
    }

    /*
     * Receives the document locator.
     *
     * @param locator the document locator
     */
    public void setDocumentLocator(Locator locator) {
	this.locator = locator;
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void comment(char[] buf, int offset, int len) throws SAXException {
	// ignore comments in the DTD
	if (!inDTD) {
	    Mark start = new Mark(path, locator.getLineNumber(),
				  locator.getColumnNumber());
	    new Node.Comment(new String(buf, offset, len), start, current);
	}
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void startCDATA() throws SAXException {
	// do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void endCDATA() throws SAXException {
	// do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void startEntity(String name) throws SAXException {
	// do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void endEntity(String name) throws SAXException {
	// do nothing
    }

    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void startDTD(String name, String publicId,
			 String systemId) throws SAXException {   
	inDTD = true;
    }
          
    /*
     * See org.xml.sax.ext.LexicalHandler.
     */
    public void endDTD() throws SAXException {
	inDTD = false;
    }

    /*
     * Receives notification of a non-recoverable error.
     */
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    /*
     * Receives notification of a recoverable error.
     */
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    /*
     * Receives notification of the start of a Namespace mapping. 
     */
    public void startPrefixMapping(String prefix, String uri)
	     throws SAXException {
        TagLibraryInfo taglibInfo;
        try {
            taglibInfo = getTaglibInfo(prefix, uri);
        } catch (JasperException je) {
            throw new SAXParseException(
                Localizer.getMessage("jsp.error.could.not.add.taglibraries"),
                locator, je);
        }

        if (taglibInfo != null) {
            pageInfo.addTaglib(uri, taglibInfo);
            pageInfo.pushPrefixMapping(prefix, uri);
        } else {
            pageInfo.pushPrefixMapping(prefix, null);
        }
     }

     /*
      * Receives notification of the end of a Namespace mapping. 
      */
    public void endPrefixMapping(String prefix) throws SAXException {
        pageInfo.popPrefixMapping(prefix);
    }


    //*********************************************************************
    // Private utility methods

    private Node parseStandardAction(String qName, String localName,
				     Attributes nonTaglibAttrs,
				     Attributes nonTaglibXmlnsAttrs,
				     Attributes taglibAttrs,
				     Mark start, Node parent)
	        throws SAXException {

	Node node = null;

	if (localName.equals(ROOT_ACTION)) {
	    node = new Node.JspRoot(qName, nonTaglibAttrs, nonTaglibXmlnsAttrs,
				    taglibAttrs, start, current);
	    if (isTop) {
		pageInfo.setHasJspRoot(true);
	    }
	} else if (localName.equals(PAGE_DIRECTIVE_ACTION)) {
	    if (isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.istagfile",
					 localName),
		    locator);
	    }
	    node = new Node.PageDirective(qName, nonTaglibAttrs,
					  nonTaglibXmlnsAttrs, taglibAttrs,
					  start, current);
	    String imports = nonTaglibAttrs.getValue("import");
	    // There can only be one 'import' attribute per page directive
	    if (imports != null) {
		((Node.PageDirective) node).addImport(imports);
	    }
	} else if (localName.equals(INCLUDE_DIRECTIVE_ACTION)) {
	    node = new Node.IncludeDirective(qName, nonTaglibAttrs,
					     nonTaglibXmlnsAttrs, taglibAttrs,
					     start, current);
	    processIncludeDirective(nonTaglibAttrs.getValue("file"), node);
	} else if (localName.equals(DECLARATION_ACTION)) {
	    node = new Node.Declaration(qName, nonTaglibXmlnsAttrs,
					taglibAttrs, start, current);
	} else if (localName.equals(SCRIPTLET_ACTION)) {
	    node = new Node.Scriptlet(qName, nonTaglibXmlnsAttrs, taglibAttrs,
				      start, current);
	} else if (localName.equals(EXPRESSION_ACTION)) {
	    node = new Node.Expression(qName, nonTaglibXmlnsAttrs, taglibAttrs,
				       start, current);
	} else if (localName.equals(USE_BEAN_ACTION)) {
	    node = new Node.UseBean(qName, nonTaglibAttrs, nonTaglibXmlnsAttrs,
				    taglibAttrs, start, current);
	} else if (localName.equals(SET_PROPERTY_ACTION)) {
	    node = new Node.SetProperty(qName, nonTaglibAttrs,
					nonTaglibXmlnsAttrs, taglibAttrs,
					start, current);
	} else if (localName.equals(GET_PROPERTY_ACTION)) {
	    node = new Node.GetProperty(qName, nonTaglibAttrs,
					nonTaglibXmlnsAttrs, taglibAttrs,
					start, current);
	} else if (localName.equals(INCLUDE_ACTION)) {
	    node = new Node.IncludeAction(qName, nonTaglibAttrs,
					  nonTaglibXmlnsAttrs, taglibAttrs,
					  start, current);
	} else if (localName.equals(FORWARD_ACTION)) {
	    node = new Node.ForwardAction(qName, nonTaglibAttrs,
					  nonTaglibXmlnsAttrs, taglibAttrs,
					  start, current);
	} else if (localName.equals(PARAM_ACTION)) {
	    node = new Node.ParamAction(qName, nonTaglibAttrs,
					nonTaglibXmlnsAttrs, taglibAttrs,
					start, current);
	} else if (localName.equals(PARAMS_ACTION)) {
	    node = new Node.ParamsAction(qName, nonTaglibXmlnsAttrs,
					 taglibAttrs, start, current);
	} else if (localName.equals(PLUGIN_ACTION)) {
	    node = new Node.PlugIn(qName, nonTaglibAttrs, nonTaglibXmlnsAttrs,
				   taglibAttrs, start, current);
	} else if (localName.equals(TEXT_ACTION)) {
	    node = new Node.JspText(qName, nonTaglibXmlnsAttrs, taglibAttrs,
				    start, current);
	} else if (localName.equals(BODY_ACTION)) {
	    node = new Node.JspBody(qName, nonTaglibXmlnsAttrs, taglibAttrs,
				    start, current);
	} else if (localName.equals(ATTRIBUTE_ACTION)) {
	    node = new Node.NamedAttribute(qName, nonTaglibAttrs,
					   nonTaglibXmlnsAttrs, taglibAttrs,
					   start, current);
	} else if (localName.equals(OUTPUT_ACTION)) {
	    node = new Node.JspOutput(qName, nonTaglibAttrs,
				      nonTaglibXmlnsAttrs, taglibAttrs,
				      start, current);
	} else if (localName.equals(TAG_DIRECTIVE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.TagDirective(qName, nonTaglibAttrs,
					 nonTaglibXmlnsAttrs, taglibAttrs,
					 start, current);
	    String imports = nonTaglibAttrs.getValue("import");
	    // There can only be one 'import' attribute per tag directive
	    if (imports != null) {
		((Node.TagDirective) node).addImport(imports);
	    }
	} else if (localName.equals(ATTRIBUTE_DIRECTIVE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.AttributeDirective(qName, nonTaglibAttrs,
					       nonTaglibXmlnsAttrs,
					       taglibAttrs, start, current);
	} else if (localName.equals(VARIABLE_DIRECTIVE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.VariableDirective(qName, nonTaglibAttrs,
					      nonTaglibXmlnsAttrs,
					      taglibAttrs, start, current);
	} else if (localName.equals(INVOKE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.InvokeAction(qName, nonTaglibAttrs,
					 nonTaglibXmlnsAttrs, taglibAttrs,
					 start, current);
	} else if (localName.equals(DOBODY_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.DoBodyAction(qName, nonTaglibAttrs,
					 nonTaglibXmlnsAttrs, taglibAttrs,
					 start, current);
	} else if (localName.equals(ELEMENT_ACTION)) {
	    node = new Node.JspElement(qName, nonTaglibAttrs,
				       nonTaglibXmlnsAttrs, taglibAttrs,
				       start, current);
	} else if (localName.equals(FALLBACK_ACTION)) {
	    node = new Node.FallBackAction(qName, nonTaglibXmlnsAttrs,
					   taglibAttrs, start, current);
	} else {
	    throw new SAXParseException(
		    Localizer.getMessage("jsp.error.xml.badStandardAction",
					 localName),
		    locator);
	}

	return node;
    }

    /*
     * Checks if the XML element with the given tag name is a custom action,
     * and returns the corresponding Node object.
     */
    private Node parseCustomAction(String qName,
				   String localName,
				   String uri,
				   Attributes nonTaglibAttrs,
				   Attributes nonTaglibXmlnsAttrs,
				   Attributes taglibAttrs,
				   Mark start,
				   Node parent) throws SAXException {

	// Check if this is a user-defined (custom) tag
        TagLibraryInfo tagLibInfo = pageInfo.getTaglib(uri);
        if (tagLibInfo == null) {
            return null;
	}

	TagInfo tagInfo = tagLibInfo.getTag(localName);
        TagFileInfo tagFileInfo = tagLibInfo.getTagFile(localName);
	if (tagInfo == null && tagFileInfo == null) {
	    throw new SAXException(Localizer.getMessage("jsp.error.xml.bad_tag",
							localName, uri));
	}
	Class tagHandlerClass = null;
	if (tagInfo != null) {
	    String handlerClassName = tagInfo.getTagClassName();
	    try {
	        tagHandlerClass = ctxt.getClassLoader().loadClass(handlerClassName);
	    } catch (Exception e) {
	        throw new SAXException(
		        Localizer.getMessage("jsp.error.loadclass.taghandler",
					     handlerClassName, qName));
	    }
	}

	String prefix = "";
	int colon = qName.indexOf(':');
	if (colon != -1) {
	    prefix = qName.substring(0, colon);
	}
       
	Node.CustomTag ret = null;
	if (tagInfo != null) {
	    ret = new Node.CustomTag(qName, prefix, localName, uri,
				     nonTaglibAttrs, nonTaglibXmlnsAttrs,
				     taglibAttrs, start, parent, tagInfo,
				     tagHandlerClass);
	} else {
	    ret = new Node.CustomTag(qName, prefix, localName, uri,
				     nonTaglibAttrs, nonTaglibXmlnsAttrs,
				     taglibAttrs, start, parent, tagFileInfo);
	}

	return ret;
    }

    /*
     * Creates the tag library associated with the given uri namespace, and
     * returns it.
     *
     * @param prefix The prefix of the xmlns attribute
     * @param uri The uri namespace (value of the xmlns attribute)
     *
     * @return The tag library associated with the given uri namespace
     */
    private TagLibraryInfo getTaglibInfo(String prefix, String uri)
                throws JasperException {

	TagLibraryInfo result = null;

	if (uri.startsWith(URN_JSPTAGDIR)) {
	    // uri (of the form "urn:jsptagdir:path") references tag file dir
	    String tagdir = uri.substring(URN_JSPTAGDIR.length());
	    result = new ImplicitTagLibraryInfo(ctxt, parserController, prefix,
						tagdir, err);
	} else {
	    // uri references TLD file
	    boolean isPlainUri = false;
	    if (uri.startsWith(URN_JSPTLD)) {
		// uri is of the form "urn:jsptld:path"
		uri = uri.substring(URN_JSPTLD.length());
	    } else {
		isPlainUri = true;
	    }

	    String[] location = ctxt.getTldLocation(uri);
	    if (location != null || !isPlainUri) {
		/*
		 * If the uri value is a plain uri, a translation error must
		 * not be generated if the uri is not found in the taglib map.
		 * Instead, any actions in the namespace defined by the uri
		 * value must be treated as uninterpreted.
		 */
		result = new TagLibraryInfoImpl(ctxt, parserController, prefix,
						uri, location, err);
	    }
	}

	return result;
    }

    /*
     * Ensures that the given body only contains nodes that are instances of
     * TemplateText.
     *
     * This check is performed only for the body of a scripting (that is:
     * declaration, scriptlet, or expression) element, after the end tag of a
     * scripting element has been reached.
     */
    private void checkScriptingBody(Node.ScriptingElement scriptingElem)
	                    throws SAXException {
	Node.Nodes body = scriptingElem.getBody();
	if (body != null) {
	    int size = body.size();
	    for (int i=0; i<size; i++) {
		Node n = body.getNode(i);
		if (!(n instanceof Node.TemplateText)) {
		    String elemType = SCRIPTLET_ACTION;
		    if (scriptingElem instanceof Node.Declaration)
			elemType = DECLARATION_ACTION;
		    if (scriptingElem instanceof Node.Expression)
			elemType = EXPRESSION_ACTION;
		    String msg = Localizer.getMessage(
                        "jsp.error.parse.xml.scripting.invalid.body",
			elemType);
		    throw new SAXException(msg);
		}
	    }
	}
    }

    /*
     * Parses the given file included via an include directive.
     *
     * @param fname The path to the included resource, as specified by the
     * 'file' attribute of the include directive
     * @param parent The Node representing the include directive
     */
    private void processIncludeDirective(String fname, Node parent) 
		throws SAXException {

	if (fname == null) {
	    return;
	}

	try {
	    parserController.parse(fname, parent, null);
	} catch (FileNotFoundException fnfe) {
	    throw new SAXParseException(
                    Localizer.getMessage("jsp.error.file.not.found", fname),
		    locator, fnfe);
	} catch (Exception e) {
	    throw new SAXException(e);
	}
    }

    /*
     * Checks an element's given URI, qname, and attributes to see if any
     * of them hijack the 'jsp' prefix, that is, bind it to a namespace other
     * than http://java.sun.com/JSP/Page.
     *
     * @param uri The element's URI
     * @param qName The element's qname
     * @param attrs The element's attributes
     */
    private void checkPrefixes(String uri, String qName, Attributes attrs) {
	
	checkPrefix(uri, qName);

	int len = attrs.getLength();
	for (int i=0; i<len; i++) {
	    checkPrefix(attrs.getURI(i), attrs.getQName(i));
	}
    }

    /*
     * Checks the given URI and qname to see if they hijack the 'jsp' prefix,
     * which would be the case if qName contained the 'jsp' prefix and
     * uri was different from http://java.sun.com/JSP/Page.
     *
     * @param uri The URI to check
     * @param qName The qname to check
     */
    private void checkPrefix(String uri, String qName) {

	int index = qName.indexOf(':');
	if (index != -1) {
	    String prefix = qName.substring(0, index);
	    pageInfo.addPrefix(prefix);
	    if ("jsp".equals(prefix) && !JSP_URI.equals(uri)) {
		pageInfo.setIsJspPrefixHijacked(true);
	    }
	}
    }
}
