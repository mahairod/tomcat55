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

    private static final String XMLNS = "xmlns:";
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
			     InputStreamReader reader,
			     boolean isTagFile,
			     boolean directivesOnly) {
	this.parserController = pc;
	this.ctxt = pc.getJspCompilationContext();
	this.pageInfo = pc.getCompiler().getPageInfo();
	this.taglibs = this.pageInfo.getTagLibraries();
	this.err = pc.getCompiler().getErrorDispatcher();
	this.path = path;
	this.inputSource = new InputSource(reader);
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
				   InputStreamReader reader,
				   Node parent,
				   boolean isTagFile,
				   boolean directivesOnly)
	        throws JasperException {

	JspDocumentParser handler = new JspDocumentParser(pc, path, reader,
							  isTagFile,
							  directivesOnly);
	Node.Nodes pageNodes = null;
	Node.JspRoot jspRoot = null;

	try {
	    if (parent == null) {
		// create dummy <jsp:root> element
		AttributesImpl rootAttrs = new AttributesImpl();
		rootAttrs.addAttribute("", "", "version", "CDATA", "2.0");
		jspRoot = new Node.JspRoot(rootAttrs, null, null);
		handler.current = jspRoot;
		handler.addInclude(jspRoot,
				   handler.pageInfo.getIncludePrelude());
	    } else {
		handler.isTop = false;
		handler.current = parent;
	    }

	    // Use the default (non-validating) parser
	    SAXParserFactory factory = SAXParserFactory.newInstance();

	    // Configure the parser
	    SAXParser saxParser = factory.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setProperty(LEXICAL_HANDLER_PROPERTY, handler);
	    xmlReader.setErrorHandler(handler);

	    // Parse the input
	    saxParser.parse(handler.inputSource, handler);

	    if (parent == null) {
		handler.addInclude(jspRoot, handler.pageInfo.getIncludeCoda());
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

	if (directivesOnly && !qName.startsWith(JSP_DIRECTIVE)) {
	    return;
	}

	Mark start = new Mark(path, locator.getLineNumber(),
			      locator.getColumnNumber());
	Attributes attrsCopy = null;

	Node node = null;

	// XXX - As of JSP 2.0, xmlns: can appear in any node (not just
	// <jsp:root>).  The spec still needs clarification here.
	// What we implement is that it can appear in any node and
	// is valid from that point forward.  Redefinitions cause an
	// error.  This isn't quite consistent with how xmlns: normally
	// works.
	try {
	    attrsCopy = addCustomTagLibraries(attrs);
	} catch (JasperException je) {
	    throw new SAXParseException( err.getString(
                "jsp.error.could.not.add.taglibraries" ), locator, je );
	}

	if (qName.equals(JSP_ROOT)) {
            // give the <jsp:root> element the original attributes set
            // (attrs) instead of the copy without the xmlns: elements 
            // (attrsCopy)
	    node = new Node.JspRoot(new AttributesImpl(attrs), start, current);
	    if (isTop) {
		pageInfo.setHasJspRoot(true);
	    }
	} else if (qName.equals(JSP_PAGE_DIRECTIVE)) {
	    if (isTagFile) {
		throw new SAXParseException(
		    err.getString("jsp.error.action.istagfile", qName),
		    locator);
	    }
	    node = new Node.PageDirective(attrsCopy, start, current);
	    String imports = attrs.getValue("import");
	    // There can only be one 'import' attribute per page directive
	    if (imports != null) {
		((Node.PageDirective) node).addImport(imports);
	    }
	} else if (qName.equals(JSP_INCLUDE_DIRECTIVE)) {
	    node = new Node.IncludeDirective(attrsCopy, start, current);
	    processIncludeDirective(attrsCopy.getValue("file"), node);
	} else if (qName.equals(JSP_DECLARATION)) {
	    node = new Node.Declaration(start, current);
	} else if (qName.equals(JSP_SCRIPTLET)) {
	    node = new Node.Scriptlet(start, current);
	} else if (qName.equals(JSP_EXPRESSION)) {
	    node = new Node.Expression(start, current);
	} else if (qName.equals(JSP_USE_BEAN)) {
	    node = new Node.UseBean(attrsCopy, start, current);
	} else if (qName.equals(JSP_SET_PROPERTY)) {
	    node = new Node.SetProperty(attrsCopy, start, current);
	} else if (qName.equals(JSP_GET_PROPERTY)) {
	    node = new Node.GetProperty(attrsCopy, start, current);
	} else if (qName.equals(JSP_INCLUDE)) {
	    node = new Node.IncludeAction(attrsCopy, start, current);
	} else if (qName.equals(JSP_FORWARD)) {
	    node = new Node.ForwardAction(attrsCopy, start, current);
	} else if (qName.equals(JSP_PARAM)) {
	    node = new Node.ParamAction(attrsCopy, start, current);
	} else if (qName.equals(JSP_PARAMS)) {
	    node = new Node.ParamsAction(start, current);
	} else if (qName.equals(JSP_PLUGIN)) {
	    node = new Node.PlugIn(attrsCopy, start, current);
	} else if (qName.equals(JSP_TEXT)) {
	    node = new Node.JspText(start, current);
	} else if (qName.equals(JSP_BODY)) {
	    node = new Node.JspBody(start, current);
	} else if (qName.equals(JSP_ATTRIBUTE)) {
	    node = new Node.NamedAttribute(attrsCopy, start, current);
	} else if (qName.equals(JSP_OUTPUT)) {
	    node = new Node.JspOutput(attrsCopy, start, current);
	} else if (qName.equals(JSP_TAG_DIRECTIVE)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    err.getString("jsp.error.action.isnottagfile", qName),
		    locator);
	    }
	    node = new Node.TagDirective(attrsCopy, start, current);
	    String imports = attrs.getValue("import");
	    // There can only be one 'import' attribute per tag directive
	    if (imports != null) {
		((Node.TagDirective) node).addImport(imports);
	    }
	} else if (qName.equals(JSP_ATTRIBUTE_DIRECTIVE)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    err.getString("jsp.error.action.isnottagfile", qName),
		    locator);
	    }
	    node = new Node.AttributeDirective(attrsCopy, start, current);
	} else if (qName.equals(JSP_VARIABLE_DIRECTIVE)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    err.getString("jsp.error.action.isnottagfile", qName),
		    locator);
	    }
	    node = new Node.VariableDirective(attrsCopy, start, current);
	} else if (qName.equals(JSP_INVOKE)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    err.getString("jsp.error.action.isnottagfile", qName),
		    locator);
	    }
	    node = new Node.InvokeAction(attrsCopy, start, current);
	} else if (qName.equals(JSP_DO_BODY)) {
	    if (!isTagFile) {
		throw new SAXParseException(
		    err.getString("jsp.error.action.isnottagfile", qName),
		    locator);
	    }
	    node = new Node.DoBodyAction(attrsCopy, start, current);
	} else if (qName.equals(JSP_ELEMENT)) {
	    node = new Node.JspElement(attrsCopy, start, current);
	} else {
	    node = getCustomTag(qName, attrsCopy, start, current);
	    if (node == null) {
		node = new Node.UninterpretedTag(attrsCopy, start, qName,
						 current);
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
	 * All textual nodes that have only white space are to be dropped from
	 * the document, except for nodes in a jsp:text element, which are 
	 * kept verbatim (JSP 5.2.1).
	 */
	boolean isAllSpace = true;
	if (!(current instanceof Node.JspText)) {
	    for (int i=offset; i<offset+len; i++) {
		if (!Character.isSpace(buf[i])) {
		    isAllSpace = false;
		    break;
		}
	    }
	}
	if ((current instanceof Node.JspText) || !isAllSpace) {
	    Mark start = new Mark(path, locator.getLineNumber(),
				  locator.getColumnNumber());

	    CharArrayWriter ttext = new CharArrayWriter();
	    int limit = offset + len;
	    int lastCh = 0;
	    for (int i = offset; i < limit; i++) {
		int ch = buf[i];
		if (lastCh == '$' && ch == '{') {
		    char[] bufCopy = ttext.toCharArray();
		    if (bufCopy.length > 0) {
			new Node.TemplateText(bufCopy, start, current);
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
				err.getString("jsp.error.unterminated", "${"),
				locator);

			}
			ch = buf[i];
			if (lastCh == '\\' && (singleQ || doubleQ)) {
			    ttext.write(ch);
			    lastCh = 0;
			    continue;
			}
			if (ch == '}') {
			    new Node.ELExpression(ttext.toCharArray(), start, current);
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
	    char[] bufCopy = ttext.toCharArray();
	    if (bufCopy.length > 0) {
		new Node.TemplateText(bufCopy, start, current);
	    }
	}
    }

    /*
     * Receives notification of the end of an element.
     */
    public void endElement(String uri,
			   String localName,
			   String qName) throws SAXException {

	if (directivesOnly && !qName.startsWith(JSP_DIRECTIVE)) {
	    return;
	}

	if (current instanceof Node.NamedAttribute
	        && ((Node.NamedAttribute) current).isTrim()) {
	    // Ignore any whitespace (including spaces, carriage returns,
	    // line feeds, and tabs, that appear at the beginning and at the
	    // end of the body of the <jsp:attribute> action.
	    Node.Nodes subelems = ((Node.NamedAttribute) current).getBody();
	    Node firstNode = subelems.getNode(0);
	    if (firstNode instanceof Node.TemplateText) {
		((Node.TemplateText) firstNode).ltrim();
	    }
	    Node lastNode = subelems.getNode(subelems.size() - 1);
	    if (lastNode instanceof Node.TemplateText) {
		((Node.TemplateText) lastNode).rtrim();
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
	    char[] bufCopy = new char[len];
	    System.arraycopy(buf, offset, bufCopy, 0, len);
	    new Node.Comment(bufCopy, start, current);
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

    /*
     * Checks if the XML element with the given tag name is a custom action,
     * and returns the corresponding Node object.
     */
    private Node getCustomTag(String qName,
			      Attributes attrs,
			      Mark start,
			      Node parent) throws SAXException {
	int colon = qName.indexOf(':');
	if (colon == -1) {
	    return null;
	}

	String prefix = qName.substring(0, colon);
	String shortName = qName.substring(colon + 1);
	if (shortName.length() == 0) {
	    return null;
	}

	// Check if this is a user-defined (custom) tag
        TagLibraryInfo tagLibInfo = (TagLibraryInfo) taglibs.get(prefix);
        if (tagLibInfo == null) {
            return null;
	}
	TagInfo tagInfo = tagLibInfo.getTag(shortName);
        TagFileInfo tagFileInfo = tagLibInfo.getTagFile(shortName);
	if (tagInfo == null && tagFileInfo == null) {
	    throw new SAXException(err.getString("jsp.error.bad_tag",
						 shortName, prefix));
	}
	Class tagHandlerClass = null;
	if (tagFileInfo == null) {
	    try {
	        tagHandlerClass
		    = ctxt.getClassLoader().loadClass(tagInfo.getTagClassName());
	    } catch (Exception e) {
	        throw new SAXException(err.getString(
						"jsp.error.unable.loadclass",
						 shortName, prefix));
	    }
	} else {
            tagInfo = tagFileInfo.getTagInfo();
        }
       
	return new Node.CustomTag(attrs, start, qName, prefix, shortName,
				  tagInfo, tagFileInfo, tagHandlerClass,
				  parent);
    }

    /*
     * Parses the xmlns:prefix attributes from the jsp:root element and adds 
     * the corresponding TagLibraryInfo objects to the set of custom tag
     * libraries.  In the process, returns a new Attributes object that does
     * not contain any of the xmlns: attributes.
     */
    private Attributes addCustomTagLibraries(Attributes attrs)
	        throws JasperException 
    {
        AttributesImpl result = new AttributesImpl( attrs );
        int len = attrs.getLength();
        for (int i=len-1; i>=0; i--) {
	    String qName = attrs.getQName(i);
	    if (qName.startsWith( XMLNS ) 
                        && !qName.startsWith(XMLNS_JSP)
		        && !qName.startsWith(JSP_VERSION)) {

		// get the prefix
		String prefix = null;
		try {
		    prefix = qName.substring(XMLNS.length());
		} catch (StringIndexOutOfBoundsException e) {
		    continue;
		}

                if( taglibs.containsKey( prefix ) ) {
                    // Prefix already in taglib map.
                    throw new JasperException( err.getString(
                        "jsp.error.xmlns.redefinition.notimplemented",
                        prefix ) );
                }

		// get the uri
		String uri = attrs.getValue(i);

		TagLibraryInfo tagLibInfo = null;
		if (uri.startsWith(URN_JSPTAGDIR)) {
		    /*
		     * uri references tag file directory
		     * (is of the form "urn:jsptagdir:path")
		     */
		    String tagdir = uri.substring(URN_JSPTAGDIR.length());
		    tagLibInfo = new ImplicitTagLibraryInfo(ctxt,
							    parserController,
							    prefix, 
							    tagdir,
							    err);
		} else {
		    /*
		     * uri references TLD file
		     */
		    if (uri.startsWith(URN_JSPTLD)) {
			// uri is of the form "urn:jsptld:path"
			uri = uri.substring(URN_JSPTLD.length());
		    }

		    TldLocationsCache cache
			= ctxt.getOptions().getTldLocationsCache();
		    tagLibInfo = cache.getTagLibraryInfo(uri);
		    if (tagLibInfo == null) {
			// get the location
			String[] location = ctxt.getTldLocation(uri);
                
			tagLibInfo = new TagLibraryInfoImpl(ctxt,
							    parserController,
							    prefix,
							    uri,
							    location,
							    err);
		    }
		}
                
		taglibs.put(prefix, tagLibInfo);
		result.removeAttribute( i );
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
		    String elemType = JSP_SCRIPTLET;
		    if (scriptingElem instanceof Node.Declaration)
			elemType = JSP_DECLARATION;
		    if (scriptingElem instanceof Node.Expression)
			elemType = JSP_EXPRESSION;
		    String msg = err.getString(
                        "jsp.error.parse.xml.scripting.invalid.body",
			elemType);
		    throw new SAXException(msg);
		}
	    }
	}
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
     * Parses the given file that is being specified in the given include
     * directive.
     */
    private void processIncludeDirective(String fname, Node includeDir) 
		throws SAXException {

	if (fname == null) {
	    return;
	}

	try {
	    parserController.parse(fname, includeDir);
	} catch (FileNotFoundException fnfe) {
	    throw new SAXParseException(err.getString(
                                            "jsp.error.file.not.found", fname),
					locator, fnfe);
	} catch (Exception e) {
	    throw new SAXException(e);
	}
    }
}
