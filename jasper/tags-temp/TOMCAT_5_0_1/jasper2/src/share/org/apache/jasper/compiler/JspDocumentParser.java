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

    private static final String XMLNS_ATTR = "xmlns";
    private static final String XMLNS_JSP = "xmlns:jsp";
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

    private Hashtable taglibs;

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
	this.taglibs = this.pageInfo.getTagLibraries();
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
				   boolean directivesOnly)
	        throws JasperException {

	JspDocumentParser handler = new JspDocumentParser(pc, path, inStream,
							  isTagFile,
							  directivesOnly);
	// It's an error to have a prelude or a coda associated with
	// a JSP document
	if (!handler.pageInfo.getIncludePrelude().isEmpty()) {
	    String file = (String) handler.pageInfo.getIncludePrelude().get(0);
	    handler.err.jspError("jsp.error.prelude.xml", path, file);
	}
	if (!handler.pageInfo.getIncludeCoda().isEmpty()) {
	    String file = (String) handler.pageInfo.getIncludeCoda().get(0);
	    handler.err.jspError("jsp.error.coda.xml", path, file);
	}

	Node.Nodes pageNodes = null;
	Node.Root jspRoot = null;

	try {
	    if (parent == null) {
		// create dummy <jsp:root> element
		jspRoot = new Node.Root(true);
		handler.current = jspRoot;
	    } else {
		handler.isTop = false;
		handler.current = parent;
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
	    xmlReader.setProperty(LEXICAL_HANDLER_PROPERTY, handler);
	    xmlReader.setErrorHandler(handler);

	    // Parse the input
	    saxParser.parse(handler.inputSource, handler);

	    if (parent == null) {
		// Create Node.Nodes from dummy (top-level) <jsp:root>
		pageNodes = new Node.Nodes(jspRoot);
	    } else {
		pageNodes = parent.getBody();
	    }
	} catch (IOException ioe) {
	    handler.err.jspError("jsp.error.data.file.read", path, ioe);
	} catch (Exception e) {
	    handler.err.jspError(e);
	}

	return pageNodes;
    }

    /*
     * Receives notification of the start of an element.
     */
    public void startElement(String uri,
			     String localName,
			     String qName,
			     Attributes attrs) throws SAXException {

	if (directivesOnly && !localName.startsWith(DIRECTIVE_ACTION)) {
	    return;
	}

	Mark start = new Mark(path, locator.getLineNumber(),
			      locator.getColumnNumber());

	// XXX - As of JSP 2.0, xmlns: can appear in any node (not just
	// <jsp:root>).  The spec still needs clarification here.
	// What we implement is that it can appear in any node and
	// is valid from that point forward.  Redefinitions cause an
	// error.  This isn't quite consistent with how xmlns: normally
	// works.
	AttributesImpl attrsCopy = null;
	Attributes xmlnsAttrs = null;
	if (attrs != null) {
	    attrsCopy = new AttributesImpl(attrs);
	    xmlnsAttrs = getXmlnsAttributes(attrsCopy);
	    if (xmlnsAttrs != null) {
		try {
		    addCustomTagLibraries(xmlnsAttrs);
		} catch (JasperException je) {
		    throw new SAXParseException(
		        Localizer.getMessage(
                            "jsp.error.could.not.add.taglibraries"),
			locator, je);
		}
	    }
	}

	Node node = null;

	if ("http://java.sun.com/JSP/Page".equals(uri)) {
	    node = parseStandardAction(qName, localName, attrsCopy, xmlnsAttrs,
				       start, current);
	} else {
	    node = parseCustomAction(qName, localName, uri, attrsCopy,
				     xmlnsAttrs, start, current);
	    if (node == null) {
		node = new Node.UninterpretedTag(qName, localName, attrsCopy,
						 xmlnsAttrs, start, current);
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
	 */
	boolean isAllSpace = true;
	if (!(current instanceof Node.JspText)
	        && !(current instanceof Node.NamedAttribute)) {
	    for (int i=offset; i<offset+len; i++) {
		if (!Character.isSpace(buf[i])) {
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

    
    //*********************************************************************
    // Private utility methods

    private Node parseStandardAction(String qName, String localName,
				     Attributes attrs, Attributes xmlnsAttrs,
				     Mark start, Node parent)
	        throws SAXException {

	Node node = null;

	if (localName.equals(ROOT_ACTION)) {
            // give the <jsp:root> element the original attributes set
            // (attrs) instead of the copy without the xmlns: elements 
            // (attrsCopy)
	    node = new Node.JspRoot(qName, attrs, xmlnsAttrs, start, current);
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
	    node = new Node.PageDirective(qName, attrs, xmlnsAttrs, start,
					  current);
	    String imports = attrs.getValue("import");
	    // There can only be one 'import' attribute per page directive
	    if (imports != null) {
		((Node.PageDirective) node).addImport(imports);
	    }
	} else if (localName.equals(INCLUDE_DIRECTIVE_ACTION)) {
	    node = new Node.IncludeDirective(qName, attrs, xmlnsAttrs, start,
					     current);
	    processIncludeDirective(attrs.getValue("file"), node);
	} else if (localName.equals(DECLARATION_ACTION)) {
	    node = new Node.Declaration(qName, xmlnsAttrs, start, current);
	} else if (localName.equals(SCRIPTLET_ACTION)) {
	    node = new Node.Scriptlet(qName, xmlnsAttrs, start, current);
	} else if (localName.equals(EXPRESSION_ACTION)) {
	    node = new Node.Expression(qName, xmlnsAttrs, start, current);
	} else if (localName.equals(USE_BEAN_ACTION)) {
	    node = new Node.UseBean(qName, attrs, xmlnsAttrs, start, current);
	} else if (localName.equals(SET_PROPERTY_ACTION)) {
	    node = new Node.SetProperty(qName, attrs, xmlnsAttrs, start,
					current);
	} else if (localName.equals(GET_PROPERTY_ACTION)) {
	    node = new Node.GetProperty(qName, attrs, xmlnsAttrs, start,
					current);
	} else if (localName.equals(INCLUDE_ACTION)) {
	    node = new Node.IncludeAction(qName, attrs, xmlnsAttrs, start,
					  current);
	} else if (localName.equals(FORWARD_ACTION)) {
	    node = new Node.ForwardAction(qName, attrs, xmlnsAttrs, start,
					  current);
	} else if (localName.equals(PARAM_ACTION)) {
	    node = new Node.ParamAction(qName, attrs, xmlnsAttrs, start,
					current);
	} else if (localName.equals(PARAMS_ACTION)) {
	    node = new Node.ParamsAction(qName, xmlnsAttrs, start, current);
	} else if (localName.equals(PLUGIN_ACTION)) {
	    node = new Node.PlugIn(qName, attrs, xmlnsAttrs, start, current);
	} else if (localName.equals(TEXT_ACTION)) {
	    node = new Node.JspText(qName, xmlnsAttrs, start, current);
	} else if (localName.equals(BODY_ACTION)) {
	    node = new Node.JspBody(qName, xmlnsAttrs, start, current);
	} else if (localName.equals(ATTRIBUTE_ACTION)) {
	    node = new Node.NamedAttribute(qName, attrs, xmlnsAttrs, start,
					   current);
	} else if (localName.equals(OUTPUT_ACTION)) {
	    node = new Node.JspOutput(qName, attrs, xmlnsAttrs, start,
				      current);
	} else if (localName.equals(TAG_DIRECTIVE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.TagDirective(qName, attrs, xmlnsAttrs, start,
					 current);
	    String imports = attrs.getValue("import");
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
	    node = new Node.AttributeDirective(qName, attrs, xmlnsAttrs, start,
					       current);
	} else if (localName.equals(VARIABLE_DIRECTIVE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.VariableDirective(qName, attrs, xmlnsAttrs, start,
					      current);
	} else if (localName.equals(INVOKE_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.InvokeAction(qName, attrs, xmlnsAttrs, start,
					 current);
	} else if (localName.equals(DOBODY_ACTION)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    Localizer.getMessage("jsp.error.action.isnottagfile",
					 localName),
		    locator);
	    }
	    node = new Node.DoBodyAction(qName, attrs, xmlnsAttrs, start,
					 current);
	} else if (localName.equals(ELEMENT_ACTION)) {
	    node = new Node.JspElement(qName, attrs, xmlnsAttrs, start,
				       current);
	} else if (localName.equals(FALLBACK_ACTION)) {
	    node = new Node.FallBackAction(qName, xmlnsAttrs, start, current);
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
				   Attributes attrs,
				   Attributes xmlnsAttrs,
				   Mark start,
				   Node parent) throws SAXException {

	// Check if this is a user-defined (custom) tag
        TagLibraryInfo tagLibInfo = (TagLibraryInfo) taglibs.get(uri);
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
	if (tagFileInfo == null) {
	    String handlerClassName = tagInfo.getTagClassName();
	    try {
	        tagHandlerClass = ctxt.getClassLoader().loadClass(handlerClassName);
	    } catch (Exception e) {
	        throw new SAXException(
		        Localizer.getMessage("jsp.error.loadclass.taghandler",
					     handlerClassName, qName));
	    }
	} else {
            tagInfo = tagFileInfo.getTagInfo();
        }

	String prefix = "";
	int colon = qName.indexOf(':');
	if (colon != -1) {
	    prefix = qName.substring(0, colon);
	}
       
	return new Node.CustomTag(qName, prefix, localName, uri, attrs,
				  xmlnsAttrs, start, parent, tagInfo,
				  tagFileInfo, tagHandlerClass);
    }

    /*
     * Extracts and removes any xmlns attributes from the given Attributes.
     *
     * @param attrs The Attributes from which to extract any xmlns attributes
     *
     * @return The set of xmlns attributes extracted from the given Attributes,
     * or null if the given Attributes do not contain any xmlns attributes
     */
    private Attributes getXmlnsAttributes(AttributesImpl attrs) {

	AttributesImpl result = null;

	if (attrs == null) {
	    return null;
	}

	int len = attrs.getLength();
	for (int i=len-1; i>=0; i--) {
	    String qName = attrs.getQName(i);
	    if (qName.startsWith(XMLNS_ATTR)) {
		if (result == null) {
		    result = new AttributesImpl();
		}
		result.addAttribute(attrs.getURI(i), attrs.getLocalName(i),
				    attrs.getQName(i), attrs.getType(i),
				    attrs.getValue(i));
		attrs.removeAttribute(i);
	    }	    
	}
	
	return result;
    }

    /*
     * Enumerates the xmlns:prefix attributes of the given Attributes object
     * and adds the corresponding TagLibraryInfo objects to the set of custom
     * tag libraries.
     */
    private void addCustomTagLibraries(Attributes xmlnsAttrs)
	    throws JasperException 
    {
        if (xmlnsAttrs == null) {
	    return;
	}

	int len = xmlnsAttrs.getLength();
        for (int i=len-1; i>=0; i--) {
	    String qName = xmlnsAttrs.getQName(i);
	    if (qName.startsWith(XMLNS_JSP)) {
		continue;
	    }

	    String uri = xmlnsAttrs.getValue(i);
	    if (!taglibs.containsKey(uri)) {
		TagLibraryInfo tagLibInfo = getTaglibInfo(qName, uri);
		taglibs.put(uri, tagLibInfo);
	    }
	}
    }

    /*
     * Creates the tag library associated with the given uri namespace, and
     * returns it.
     *
     * @param qName The qualified name of the xmlns attribute
     * (of the form 'xmlns:<prefix>')
     * @param uri The uri namespace (value of the xmlns attribute)
     *
     * @return The tag library associated with the given uri namespace
     */
    private TagLibraryInfo getTaglibInfo(String qName, String uri)
                throws JasperException {

	TagLibraryInfo result = null;

	// Get the prefix
	String prefix = "";
	int colon = qName.indexOf(':');
	if (colon != -1) {
	    prefix = qName.substring(colon + 1);
	}

	if (uri.startsWith(URN_JSPTAGDIR)) {
	    // uri (of the form "urn:jsptagdir:path") references tag file dir
	    String tagdir = uri.substring(URN_JSPTAGDIR.length());
	    result = new ImplicitTagLibraryInfo(ctxt, parserController, prefix,
						tagdir, err);
	} else {
	    // uri references TLD file
	    if (uri.startsWith(URN_JSPTLD)) {
		// uri is of the form "urn:jsptld:path"
		uri = uri.substring(URN_JSPTLD.length());
	    }

	    TldLocationsCache cache = ctxt.getOptions().getTldLocationsCache();
	    result = cache.getTagLibraryInfo(uri);
	    if (result == null) {
		// get the location
		String[] location = ctxt.getTldLocation(uri);
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
     * Parses the given file that is being specified in the given include
     * directive.
     */
    private void processIncludeDirective(String fname, Node includeDir) 
		throws SAXException {

	if (fname == null) {
	    return;
	}

	try {
	    parserController.parse(fname, includeDir, null);
	} catch (FileNotFoundException fnfe) {
	    throw new SAXParseException(
                    Localizer.getMessage("jsp.error.file.not.found", fname),
		    locator, fnfe);
	} catch (Exception e) {
	    throw new SAXException(e);
	}
    }
}
