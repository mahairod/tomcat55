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

import java.io.FileNotFoundException;
import java.io.CharArrayWriter;
import java.util.Hashtable;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;

/**
 * This class implements a parser for a JSP page (non-xml view).
 * JSP page grammar is included here for reference.  The token '#'
 * that appears in the production indicates the current input token
 * location in the production.
 * 
 * @author Kin-man Chung
 * @author Shawn Bayern
 * @author Mark Roth
 */

public class Parser {

    private ParserController parserController;
    private JspCompilationContext ctxt;
    private JspReader reader;
    private String currentFile;
    private Mark start;
    private Hashtable taglibs;
    private ErrorDispatcher err;
    private int scriptlessCount;
    
    // Virtual body content types, to make parsing a little easier.
    // These are not accessible from outside the parser.
    private static final String JAVAX_BODY_CONTENT_PARAM = 
        "JAVAX_BODY_CONTENT_PARAM";
    private static final String JAVAX_BODY_CONTENT_PLUGIN = 
        "JAVAX_BODY_CONTENT_PLUGIN";

    /**
     * The constructor
     */
    private Parser(ParserController pc, JspReader reader) {
	this.parserController = pc;
	this.ctxt = pc.getJspCompilationContext();
	this.taglibs = pc.getCompiler().getPageInfo().getTagLibraries();
	this.err = pc.getCompiler().getErrorDispatcher();
	this.reader = reader;
	this.currentFile = reader.mark().getFile();
        this.scriptlessCount = 0;
        start = reader.mark();
    }

    /**
     * The main entry for Parser
     * 
     * @param pc The ParseController, use for getting other objects in compiler
     *		 and for parsing included pages
     * @param reader To read the page
     * @param parent The parent node to this page, null for top level page
     * @return list of nodes representing the parsed page
     */
    public static Node.Nodes parse(ParserController pc,
				   JspReader reader,
				   Node parent) throws JasperException {
	Parser parser = new Parser(pc, reader);

	Node.Root root = new Node.Root(null, reader.mark(), parent);

	while (reader.hasMoreInput()) {
	    parser.parseElements(root);
	}

	Node.Nodes page = new Node.Nodes(root);
	return page;
    }

    /**
     * Attributes ::= (S Attribute)* S?
     */
    Attributes parseAttributes() throws JasperException {
	AttributesImpl attrs = new AttributesImpl();

	reader.skipSpaces();
	while (parseAttribute(attrs))
	    reader.skipSpaces();

	return attrs;
    }

    /**
     * Parse Attributes for a reader, provided for external use
     */
    public static Attributes parseAttributes(ParserController pc,
					     JspReader reader)
		throws JasperException {
	Parser tmpParser = new Parser(pc, reader);
	return tmpParser.parseAttributes();
    }

    /**
     * Attribute ::= Name S? Eq S?
     *               (   '"<%=' RTAttributeValueDouble
     *                 | '"' AttributeValueDouble
     *                 | "'<%=" RTAttributeValueSingle
     *                 | "'" AttributeValueSingle
     *               }
     * Note: JSP and XML spec does not allow while spaces around Eq.  It is
     * added to be backward compatible with Tomcat, and with other xml parsers.
     */
    private boolean parseAttribute(AttributesImpl attrs) throws JasperException {
	String name = parseName();
	if (name == null)
	    return false;

 	reader.skipSpaces();
	if (!reader.matches("="))
	    err.jspError(reader.mark(), "jsp.error.attribute.noequal");

 	reader.skipSpaces();
	char quote = (char) reader.nextChar();
	if (quote != '\'' && quote != '"')
	    err.jspError(reader.mark(), "jsp.error.attribute.noquote");

 	String watchString = "";
	if (reader.matches("<%="))
	    watchString = "%>";
	watchString = watchString + quote;
	
	String attr = parseAttributeValue(watchString);
	attrs.addAttribute("", name, name, "CDATA", attr);
	return true;
    }

    /**
     * Name ::= (Letter | '_' | ':') (Letter | Digit | '.' | '_' | '-' | ':')*
     */
    private String parseName() throws JasperException {
	char ch = (char)reader.peekChar();
	if (Character.isLetter(ch) || ch == '_' || ch == ':') {
	    StringBuffer buf = new StringBuffer();
	    buf.append(ch);
	    reader.nextChar();
	    ch = (char)reader.peekChar();
	    while (Character.isLetter(ch) || Character.isDigit(ch) ||
			ch == '.' || ch == '_' || ch == '-' || ch == ':') {
		buf.append(ch);
		reader.nextChar();
		ch = (char) reader.peekChar();
	    }
	    return buf.toString();
	}
	return null;
    }

    /**
     * AttributeValueDouble ::= (QuotedChar - '"')*
     *				('"' | <TRANSLATION_ERROR>)
     * RTAttributeValueDouble ::= ((QuotedChar - '"')* - ((QuotedChar-'"')'%>"')
     *				  ('%>"' | TRANSLATION_ERROR)
     */
    private String parseAttributeValue(String watch) throws JasperException {
	Mark start = reader.mark();
	Mark stop = reader.skipUntilIgnoreEsc(watch);
	if (stop == null) {
	    err.jspError(start, "jsp.error.attribute.unterminated", watch);
	}

	String ret = parseQuoted(reader.getText(start, stop));
	if (watch.length() == 1)	// quote
	    return ret;

	// putback delimiter '<%=' and '%>', since they are needed if the
	// attribute does not allow RTexpression.
	return "<%=" + ret + "%>";
    }

    /**
     * QuotedChar ::=   '&apos;'
     *	              | '&quot;'
     *                | '\\'
     *                | '\"'
     *                | "\'"
     *                | '\>'
     *                | Char
     */
    private String parseQuoted(char[] tx) {
	StringBuffer buf = new StringBuffer();
	int size = tx.length;
	int i = 0;
	while (i < size) {
	    char ch = tx[i];
	    if (ch == '&') {
		if (i+5 < size && tx[i+1] == 'a' && tx[i+2] == 'p' &&
			tx[i+3] == 'o' && tx[i+4] == 's' && tx[i+5] == ';') {
		    buf.append('\'');
		    i += 6;
		} else if (i+5 < size && tx[i+1] == 'q' && tx[i+2] == 'u' &&
			tx[i+3] == 'o' && tx[i+4] == 't' && tx[i+5] == ';') {
		    buf.append('"');
		    i += 6;
		} else {
		    buf.append(ch);
		    ++i;
		}
	    } else if (ch == '\\' && i+1 < size) {
		ch = tx[i+1];
		if (ch == '\\' || ch == '\"' || ch == '\'' || ch == '>') {
		    buf.append(ch);
		    i += 2;
		} else {
		    buf.append('\\');
		    ++i;
		}
	    } else {
		buf.append(ch);
		++i;
	    }
	}
	return buf.toString();
    }

    private char[] parseScriptText(char[] tx) {
	CharArrayWriter cw = new CharArrayWriter();
	int size = tx.length;
	int i = 0;
	while (i < size) {
	    char ch = tx[i];
	    if (i+2 < size && ch == '%' && tx[i+1] == '\\' && tx[i+2] == '>') {
		cw.write('%');
		cw.write('>');
		i += 3;
	    } else {
		cw.write(ch);
		++i;
	    }
	}
	cw.close();
	return cw.toCharArray();
    }

    /*
     * Invokes parserController to parse the included page
     */
    private void processIncludeDirective(String file, Node parent) 
		throws JasperException {
	if (file == null) {
	    return;
	}

	try {
	    parserController.parse(file, parent);
	} catch (FileNotFoundException ex) {
	    err.jspError(start, "jsp.error.file.not.found", file);
	} catch (Exception ex) {
	    err.jspError(start, ex.getMessage());
	}
    }

    /*
     * Parses a page directive with the following syntax:
     *   PageDirective ::= ( S Attribute)*
     */
    private void parsePageDirective(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	Node.PageDirective n = new Node.PageDirective(attrs, start, parent);

	/*
	 * A page directive may contain multiple 'import' attributes, each of
	 * which consists of a comma-separated list of package names.
	 * Store each list with the node, where it is parsed.
	 */
	for (int i = 0; i < attrs.getLength(); i++) {
	    if ("import".equals(attrs.getQName(i))) {
		n.addImport(attrs.getValue(i));
	    }
	}
    }

    /*
     * Parses an include directive with the following syntax:
     *   IncludeDirective ::= ( S Attribute)*
     */
    private void parseIncludeDirective(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();

	// Included file expanded here
	Node includeNode = new Node.IncludeDirective(attrs, start, parent);
	processIncludeDirective(attrs.getValue("file"), includeNode);
    }

    /*
     * Parses a taglib directive with the following syntax:
     *   Directive ::= ( S Attribute)*
     */
    private void parseTaglibDirective(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	String uri = attrs.getValue("uri");
	String prefix = attrs.getValue("prefix");
	if (uri != null && prefix != null) {
	    // Errors to be checked in Validator
	    String[] location = ctxt.getTldLocation(uri);
	    TagLibraryInfo tl = new TagLibraryInfoImpl(ctxt, prefix, uri,
						   location, err);
	    taglibs.put(prefix, tl);
	}

	new Node.TaglibDirective(attrs, start, parent);
    }

    /*
     * Parses a directive with the following syntax:
     *   Directive ::= S? (   'page' PageDirective
     *			    | 'include' IncludeDirective
     *			    | 'taglib' TagLibDirective)
     *		       S? '%>'
     */
    private void parseDirective(Node parent) throws JasperException {
	reader.skipSpaces();

	String directive = null;
	if (reader.matches("page")) {
	    directive = "<%@ page";
	    parsePageDirective(parent);
	} else if (reader.matches("include")) {
	    directive = "<%@ include";
	    parseIncludeDirective(parent);
	} else if (reader.matches("taglib")) {
	    directive = "<%@ taglib";
	    parseTaglibDirective(parent);
	} else {
	    err.jspError(reader.mark(), "jsp.error.invalid.directive");
	}

	reader.skipSpaces();
	if (!reader.matches("%>")) {
	    err.jspError(start, "jsp.error.unterminated", directive);
	}
    }
	
    /*
     * JSPCommentBody ::= (Char* - (Char* '--%>')) '--%>'
     */
    private void parseComment(Node parent) throws JasperException {	
	start = reader.mark();
	Mark stop = reader.skipUntil("--%>");
	if (stop == null) {
	    err.jspError(start, "jsp.error.unterminated", "<%--");
	}

	new Node.Comment(reader.getText(start, stop), start, parent);
    }

    /*
     * DeclarationBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseDeclaration(Node parent) throws JasperException {
	start = reader.mark();
	Mark stop = reader.skipUntil("%>");
	if (stop == null) {
	    err.jspError(start, "jsp.error.unterminated", "<%!");
	}

	new Node.Declaration(parseScriptText(reader.getText(start, stop)),
				start, parent);
    }

    /*
     * ExpressionBody ::= (Char* - (char* '%>')) '%>'
     */
    private void parseExpression(Node parent) throws JasperException {
	start = reader.mark();
	Mark stop = reader.skipUntil("%>");
	if (stop == null) {
	    err.jspError(start, "jsp.error.unterminated", "<%=");
	}

	new Node.Expression(parseScriptText(reader.getText(start, stop)),
				start, parent);
    }

    /*
     * ELExpressionBody
     * (following "${" to first unquoted "}")
     * // XXX add formal production and confirm implementation against it,
     * //     once it's decided
     */
    private void parseELExpression(Node parent) throws JasperException {
        start = reader.mark();
        Mark last = null;
        boolean singleQuoted = false, doubleQuoted = false;
        int currentChar;
        do {
            // XXX could move this logic to JspReader
            last = reader.mark();               // XXX somewhat wasteful
            currentChar = reader.nextChar();
            if (currentChar == '\\' && (singleQuoted || doubleQuoted)) {
                // skip character following '\' within quotes
                reader.nextChar();
                currentChar = reader.nextChar();
            }
            if (currentChar == -1)
                err.jspError(start, "jsp.error.unterminated", "${");
            if (currentChar == '"')
                doubleQuoted = !doubleQuoted;
            if (currentChar == '\'')
                singleQuoted = !singleQuoted;
        } while (currentChar != '}' || (singleQuoted || doubleQuoted));

        new Node.ELExpression(reader.getText(start, last), start, parent);
    }

    /*
     * Scriptlet ::= (Char* - (char* '%>')) '%>'
     */
    private void parseScriptlet(Node parent) throws JasperException {
	start = reader.mark();
	Mark stop = reader.skipUntil("%>");
	if (stop == null) {
	    err.jspError(start, "jsp.error.unterminated", "<%");
	}

	new Node.Scriptlet(parseScriptText(reader.getText(start, stop)),
				start, parent);
    }
	
    /**
     * Param ::= '<jsp:param' S Attributes S? EmptyBody S?
     */
    private void parseParam(Node parent) throws JasperException {
	if (!reader.matches("<jsp:param")) {
	    err.jspError(reader.mark(), "jsp.error.paramexpectedonly");
	}
	Attributes attrs = parseAttributes();
	reader.skipSpaces();
        
        Node paramActionNode = new Node.ParamAction( attrs, start, parent );
        
        parseEmptyBody( paramActionNode, "jsp:param" );
        
        reader.skipSpaces();
    }

    /*
     * For Include:
     * StdActionContent ::= Attributes ParamBody
     *
     * ParamBody ::=   EmptyBody
     *               | ( '>' S? ( '<jsp:attribute' NamedAttributes )?
     *                   '<jsp:body'
     *                   (JspBodyParam | <TRANSLATION_ERROR> )
     *                   S? ETag
     *                 )
     *               | ( '>' S? Param* ETag )
     *
     * EmptyBody ::=   '/>'
     *               | ( '>' ETag )
     *               | ( '>' S? '<jsp:attribute' NamedAttributes ETag )
     *
     * JspBodyParam ::= S? '>' Param* '</jsp:body>'
     */
    private void parseInclude(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	reader.skipSpaces();

        Node includeNode = new Node.IncludeAction( attrs, start, parent );
        
        parseOptionalBody( includeNode, "jsp:include", 
            JAVAX_BODY_CONTENT_PARAM );
    }

    /*
     * For Forward:
     * StdActionContent ::= Attributes ParamBody
     */
    private void parseForward(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	reader.skipSpaces();

        Node forwardNode = new Node.ForwardAction( attrs, start, parent );
        
        parseOptionalBody( forwardNode, "jsp:forward", 
            JAVAX_BODY_CONTENT_PARAM );
    }

    /*
     * For GetProperty:
     * StdActionContent ::= Attributes EmptyBody
     */
    private void parseGetProperty(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	reader.skipSpaces();

        Node getPropertyNode = new Node.GetProperty( attrs, start, parent );
        
        parseEmptyBody( getPropertyNode, "jsp:getProperty" );
    }

    /*
     * For SetProperty:
     * StdActionContent ::= Attributes EmptyBody
     */
    private void parseSetProperty(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	reader.skipSpaces();

        Node setPropertyNode = new Node.SetProperty( attrs, start, parent );
        
        parseEmptyBody( setPropertyNode, "jsp:setProperty" );
    }

    /*
     * EmptyBody ::=   '/>'
     *               | ( '>' ETag )
     *               | ( '>' S? '<jsp:attribute' NamedAttributes ETag )
     */
    private void parseEmptyBody( Node parent, String tag ) 
        throws JasperException
    {
	if( reader.matches("/>") ) {
            // Done
        }
        else if( reader.matches( ">" ) ) {
            if( reader.matchesETag( tag ) ) {
                // Done
            }
            else if( reader.matchesOptionalSpacesFollowedBy(
                "<jsp:attribute" ) )
            {
                // Parse the one or more named attribute nodes
                parseNamedAttributes( parent );
                if( !reader.matchesETag( tag ) ) {
                    // Body not allowed
                    err.jspError(reader.mark(),
                        "jsp.error.jspbody.emptybody.only",
                        "<" + tag );
                }
            }
            else {
                err.jspError(reader.mark(), "jsp.error.jspbody.emptybody.only",
                    "<" + tag );
            }
        }
        else {
	    err.jspError(reader.mark(), "jsp.error.unterminated",
                "<" + tag );
        }
    }

    /*
     * For UseBean:
     * StdActionContent ::= Attributes OptionalBody
     */
    private void parseUseBean(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	reader.skipSpaces();
        
        Node useBeanNode = new Node.UseBean( attrs, start, parent );
        
        parseOptionalBody( useBeanNode, "jsp:useBean", 
            TagInfo.BODY_CONTENT_JSP );
    }

    /*
     * Parses OptionalBody, but also reused to parse bodies for plugin
     * and param since the syntax is identical (the only thing that
     * differs substantially is how to process the body, and thus
     * we accept the body type as a parameter).
     *
     * OptionalBody ::= EmptyBody | ActionBody
     *
     * ScriptlessOptionalBody ::= EmptyBody | ScriptlessActionBody
     *
     * TagDependentOptionalBody ::= EmptyBody | TagDependentActionBody
     *
     * EmptyBody ::=   '/>'
     *               | ( '>' ETag )
     *               | ( '>' S? '<jsp:attribute' NamedAttributes ETag )
     *
     * ActionBody ::=   JspAttributeAndBody
     *                | ( '>' Body ETag )
     *
     * ScriptlessActionBody ::=   JspAttributeAndBody 
     *                          | ( '>' ScriptlessBody ETag )
     * 
     * TagDependentActionBody ::=   JspAttributeAndBody
     *                            | ( '>' TagDependentBody ETag )
     *
     */
    private void parseOptionalBody( Node parent, String tag, String bodyType ) 
        throws JasperException 
    {
	if (reader.matches("/>")) {
	    // EmptyBody
	    return;
	}

	if (!reader.matches(">")) {
	    err.jspError(reader.mark(), "jsp.error.unterminated",
			 "<" + tag );
	}
        
        if( reader.matchesETag( tag ) ) {
            // EmptyBody
            return;
        }
        
        if( !parseJspAttributeAndBody( parent, tag, bodyType ) ) {
            // Must be ( '>' # Body ETag )
            parseBody(parent, tag, bodyType );
        }
    }
    
    /**
     * Attempts to parse 'JspAttributeAndBody' production.  Returns true if
     * it matched, or false if not.  Assumes EmptyBody is okay as well.
     *
     * JspAttributeAndBody ::=
     *                  ( '>' # S? ( '<jsp:attribute' NamedAttributes )?
     *                    '<jsp:body'
     *                    ( JspBodyBody | <TRANSLATION_ERROR> )
     *                    S? ETag
     *                  )
     */
    private boolean parseJspAttributeAndBody( Node parent, String tag, 
        String bodyType ) 
        throws JasperException
    {
        boolean result = false;
        
        if( reader.matchesOptionalSpacesFollowedBy( "<jsp:attribute" ) ) {
            // May be an EmptyBody, depending on whether
            // There's a "<jsp:body" before the ETag
            
            // First, parse <jsp:attribute> elements:
            parseNamedAttributes( parent );
            
            result = true;
        }
        
        if( reader.matchesOptionalSpacesFollowedBy( "<jsp:body" ) ) {
            // ActionBody
            parseJspBody( parent, bodyType );
            reader.skipSpaces();
            if( !reader.matchesETag( tag ) ) {
                err.jspError(reader.mark(), "jsp.error.unterminated", 
                    "<" + tag );
            }
            
            result = true;
        }
        else if( result && !reader.matchesETag( tag ) ) {
            // If we have <jsp:attribute> but something other than
            // <jsp:body> or the end tag, translation error.
            err.jspError(reader.mark(), "jsp.error.jspbody.required", 
                "<" + tag );
        }
        
        return result;
    }

    /*
     * Params ::=   '/>'
     *            | ( '>' S? Param* '</jsp:params>' )
     */
    private void parseJspParams(Node parent) throws JasperException {
        if( reader.matches( "/>" ) ) {
            // No elements, don't create node.
        }
        else if( reader.matches( ">" ) ) {
            reader.skipSpaces();
            Node jspParamsNode = new Node.ParamsAction(start, parent);
            parseBody(jspParamsNode, "jsp:params", JAVAX_BODY_CONTENT_PARAM );
        }
        else {
            err.jspError(reader.mark(), "jsp.error.unterminated",
                "<jsp:params" );
        }
    }

    /*
     * Fallback ::=   '/>'
     *              | ( '>'
     *                  ( Char* - ( Char* '</jsp:fallback>' ) )
     *                  '</jsp:fallback>'
     *                )
     */
    private void parseFallBack(Node parent) throws JasperException {
        if( reader.matches( "/>" ) ) {
            // No elements, don't create node.
        }
        else if( reader.matches( ">" ) ) {
            Mark bodyStart = reader.mark();
            Mark bodyEnd = reader.skipUntilETag("jsp:fallback");
            if (bodyEnd == null) {
                err.jspError(start, "jsp.error.unterminated", 
                    "<jsp:fallback");
            }
            char[] text = reader.getText(bodyStart, bodyEnd);
            new Node.FallBackAction(start, text, parent);
        }
        else {
            err.jspError( reader.mark(), "jsp.error.unterminated",
                "<jsp:fallback" );
        }
    }

    /*
     * For Plugin:
     * StdActionContent ::= Attributes PluginBody
     *
     * PluginBody ::=   EmptyBody 
     *                | ( '>' S? ( '<jsp:attribute' NamedAttributes )?
     *                    '<jsp:body'
     *                    ( JspBodyPluginTags | <TRANSLATION_ERROR> )
     *                    S? ETag
     *                  )
     *                | ( '>' S? PluginTags ETag )
     *
     * EmptyBody ::=   '/>'
     *               | ( '>' ETag )
     *               | ( '>' S? '<jsp:attribute' NamedAttributes ETag )
     *
     */
    private void parsePlugin(Node parent) throws JasperException {
	Attributes attrs = parseAttributes();
	reader.skipSpaces();
        
	Node pluginNode = new Node.PlugIn(attrs, start, parent);
        
        parseOptionalBody( pluginNode, "jsp:plugin", 
            JAVAX_BODY_CONTENT_PLUGIN );
    }

    /*
     * PluginTags ::= ( '<jsp:params' Params S? )?
     *                ( '<jsp:fallback' Fallback? S? )?
     */
    private void parsePluginTags( Node parent ) throws JasperException {
        reader.skipSpaces();
        
        if( reader.matches( "<jsp:params" ) ) {
            parseJspParams( parent );
            reader.skipSpaces();
        }
        
        if( reader.matches( "<jsp:fallback" ) ) {
            parseFallBack( parent );
            reader.skipSpaces();
        }
    }
        
    /*
     * StandardAction ::=   'include'       StdActionContent
     *                    | 'forward'       StdActionContent
     *                    | 'getProperty'   StdActionContent
     *                    | 'setProperty'   StdActionContent
     *                    | 'useBean'       StdActionContent
     *                    | 'plugin'        StdActionContent
     */
    private void parseAction(Node parent) throws JasperException {
	Mark start = reader.mark();

	if (reader.matches("include")) {
	    parseInclude(parent);
	} else if (reader.matches("forward")) {
	    parseForward(parent);
	} else if (reader.matches("getProperty")) {
	    parseGetProperty(parent);
	} else if (reader.matches("setProperty")) {
	    parseSetProperty(parent);
	} else if (reader.matches("useBean")) {
	    parseUseBean(parent);
	} else if (reader.matches("plugin")) {
	    parsePlugin(parent);
	} else {
	    err.jspError(start, "jsp.error.badaction");
	}
    }

    /*
     * # '<' CustomAction CustomActionBody
     *
     * CustomAction ::= TagPrefix ':' CustomActionName
     *
     * TagPrefix ::= Name
     *
     * CustomActionName ::= Name
     *
     * CustomActionBody ::=   ( Attributes CustomActionEnd )
     *                      | <TRANSLATION_ERROR>
     *
     * Attributes ::= ( S Attribute )* S?
     *
     * CustomActionEnd ::=   CustomActionTagDependent
     *                     | CustomActionJSPContent
     *                     | CustomActionScriptlessContent
     *
     * CustomActionTagDependent ::= TagDependentOptionalBody
     *
     * CustomActionJSPContent ::= OptionalBody
     *
     * CustomActionScriptlessContent ::= ScriptlessOptionalBody
     */
    private boolean parseCustomTag(Node parent) throws JasperException {

	if (reader.peekChar() != '<') {
	    return false;
	}

        // Parse 'CustomAction' production (tag prefix and custom action name)
	reader.nextChar();	// skip '<'
	String tagName = reader.parseToken(false);
	int i = tagName.indexOf(':');
	if (i == -1) {
	    reader.reset(start);
	    return false;
	}

	String prefix = tagName.substring(0, i);
	String shortTagName = tagName.substring(i+1);

	// Check if this is a user-defined tag.
        TagLibraryInfo tagLibInfo = (TagLibraryInfo) taglibs.get(prefix);
        if (tagLibInfo == null) {
	    reader.reset(start);
	    return false;
	}
	TagInfo tagInfo = tagLibInfo.getTag(shortTagName);
	if (tagInfo == null) {
	    err.jspError(start, "jsp.error.bad_tag", shortTagName, prefix);
	}
	Class tagHandlerClass = null;
	try {
	    tagHandlerClass
		= ctxt.getClassLoader().loadClass(tagInfo.getTagClassName());
	} catch (Exception e) {
	    err.jspError(start, "jsp.error.unable.loadclass", shortTagName,
			 prefix);
	}

        // Parse 'CustomActionBody' production:
        // At this point we are committed - if anything fails, we produce
        // a translation error.

        // Parse 'Attributes' production:
	Attributes attrs = parseAttributes();
	reader.skipSpaces();
	
        // Parse 'CustomActionEnd' production:
	if (reader.matches("/>")) {
	    new Node.CustomTag(attrs, start, tagName, prefix, shortTagName,
			       tagInfo, tagHandlerClass, parent);
	    return true;
	}
	
        // Now we parse one of 'CustomActionTagDependent', 
        // 'CustomActionJSPContent', or 'CustomActionScriptlessContent'.
        // depending on body-content in TLD.

	// Looking for a body, it still can be empty; but if there is a
	// a tag body, its syntax would be dependent on the type of
	// body content declared in TLD.
	String bc = ((TagLibraryInfo)taglibs.get(prefix)).getTag(
            shortTagName).getBodyContent();

	Node tagNode = new Node.CustomTag(attrs, start, tagName, prefix,
					  shortTagName, tagInfo,
					  tagHandlerClass, parent);
	parseOptionalBody( tagNode, tagName, bc );

	return true;
    }

    /*
     *
     */
    private void parseTemplateText(Node parent) throws JasperException {
	// Note except for the beginning of a page, the current char is '<'.
	// Quoting in template text is handled here.
	// JSP2.6 "A literal <% is quoted by <\%"
	if (reader.matches("<\\%")) {
	    char[] content = reader.nextContent();
	    char[] text = new char[content.length + 2];
	    text[0] = '<';
	    text[1] = '%';
	    System.arraycopy(content, 0, text, 2, content.length);
	    new Node.TemplateText(text, start, parent);
	} else {
	    new Node.TemplateText(reader.nextContent(), start, parent);
	}
    }
    
    /*
     * AllBody ::=	  ( '<%--' JSPCommentBody )
     *			| ( '<%@' DirectiveBody )
     *			| ( '<%!' DeclarationBody )
     *			| ( '<%=' ExpressionBody )
     *                  | ( '${' ELExpressionBody )
     *			| ( '<%' ScriptletBody )
     *			| ( '<jsp:' StandardAction )
     *			| ( '<' CustomAction CustomActionBody )
     *			| TemplateText
     */
    private void parseElements(Node parent) 
        throws JasperException 
    {
        if( scriptlessCount > 0 ) {
            // vc: ScriptlessBody
            // We must follow the ScriptlessBody production if one of
            // our parents is ScriptlessBody.
            parseElementsScriptless( parent );
            return;
        }
        
	start = reader.mark();
	if (reader.matches("<%--")) {
	    parseComment(parent);
	} else if (reader.matches("<%@")) {
	    parseDirective(parent);
	} else if (reader.matches("<%!")) {
	    parseDeclaration(parent);
	} else if (reader.matches("<%=")) {
	    parseExpression(parent);
	} else if (reader.matches("<%")) {
	    parseScriptlet(parent);
        } else if (reader.matches("${")) {
            parseELExpression(parent);
	} else if (reader.matches("<jsp:")) {
	    parseAction(parent);
	} else if (!parseCustomTag(parent)) {
	    parseTemplateText(parent);
	}
    }

    /*
     * ScriptlessBody ::=   ( '<%--' JSPCommentBody )
     *			  | ( '<%@' <TRANSLATION_ERROR> )
     *			  | ( '<%!' <TRANSLATION_ERROR> )
     *			  | ( '<%=' <TRANSLATION_ERROR> )
     *			  | ( '<%' <TRANSLATION_ERROR> )
     *                    | ( '${' ELExpressionBody )
     *	  		  | ( '<jsp:' StandardAction )
     *			  | ( '<' CustomAction CustomActionBody )
     *			  | TemplateText
     */
    private void parseElementsScriptless(Node parent) 
        throws JasperException 
    {
        // Keep track of how many scriptless nodes we've encountered
        // so we know whether our child nodes are forced scriptless
        scriptlessCount++;
        
	start = reader.mark();
	if (reader.matches("<%--")) {
	    parseComment(parent);
	} else if (reader.matches("<%@")) {
	    parseDirective(parent);
	} else if (reader.matches("<%!")) {
	    err.jspError( reader.mark(), "jsp.error.no.scriptlets" );
	} else if (reader.matches("<%=")) {
	    err.jspError( reader.mark(), "jsp.error.no.scriptlets" );
	} else if (reader.matches("<%")) {
	    err.jspError( reader.mark(), "jsp.error.no.scriptlets" );
	} else if (reader.matches("${")) {
	    parseELExpression(parent);
	} else if (reader.matches("<jsp:")) {
	    parseAction(parent);
	} else if (!parseCustomTag(parent)) {
	    parseTemplateText(parent);
	}
        
        scriptlessCount--;
    }
    
    /*
     *
     */
    private void parseBodyText(Node parent, String tag) throws JasperException{
	Mark bodyStart = reader.mark();
	Mark bodyEnd = reader.skipUntilETag(tag);
	if (bodyEnd == null) {
	    err.jspError(start, "jsp.error.unterminated", "<"+tag );
	}
	new Node.TemplateText(reader.getText(bodyStart, bodyEnd), bodyStart,
			      parent);
    }

    /*
     * JspBodyBody ::=      ( S? '>' 
     *                        ( (   ScriptlessBody 
     *                            | Body
     *                            | TagDependentBody
     *                          ) - ''
     *                        ) '</jsp:body>' 
     *                      )
     *                  |   ( ATTR[ !value ] S? JspBodyEmptyBody )
     *
     * JspBodyEmptyBody ::=     '/>'
     *                      |   '></jsp:body>'
     *                      |   <TRANSLATION_ERROR>
     *
     */
    private void parseJspBody(Node parent, String bodyType) 
        throws JasperException 
    {
        Mark start = reader.mark();
        reader.skipSpaces();

        if( reader.matches( ">" ) ) {
            Node bodyNode = new Node.JspBody( null, start, parent );
            if( reader.matches( "</jsp:body>" ) ) {
                // Body was empty.  This is illegal, according to the grammar.
                err.jspError(reader.mark(),
                    "jsp.error.empty.body.not.allowed",
                    "<jsp:body>" );
            }
            else {
                parseBody( bodyNode, "jsp:body", bodyType );
            }
        }
        else {
            Attributes attrs = parseAttributes();
            reader.skipSpaces();
            new Node.JspBody( attrs, start, parent );

            if( !reader.matches( "/>" ) &&
                !reader.matches( "></jsp:body>" ) )
            {
                err.jspError(reader.mark(), 
                    "jsp.error.jspbody.body.not.allowed.with.value",
                    "<jsp:body>" );
            }
        }
    }

    /*
     * Parse the body as JSP content.
     * @param tag The name of the tag whose end tag would terminate the body
     * @param bodyType One of the TagInfo body types
     */
    private void parseBody(Node parent, String tag, String bodyType) 
        throws JasperException 
    {
        if( bodyType.equalsIgnoreCase( TagInfo.BODY_CONTENT_TAG_DEPENDENT ) ) {
            parseBodyText( parent, tag );
        }
        else if( bodyType.equalsIgnoreCase( TagInfo.BODY_CONTENT_EMPTY ) ) {
            if( !reader.matchesETag( tag ) ) {
		err.jspError(start, "jasper.error.emptybodycontent.nonempty");
            }
        }
        else if( bodyType == JAVAX_BODY_CONTENT_PLUGIN ) {
            // (note the == since we won't recognize JAVAX_* 
            // from outside this module).
            parsePluginTags(parent);
            if( !reader.matchesETag( tag ) ) {
                err.jspError( reader.mark(), "jsp.error.unterminated",
                    "<" + tag  );
            }
        }
        else if( bodyType.equalsIgnoreCase( TagInfo.BODY_CONTENT_JSP ) ||
            bodyType.equalsIgnoreCase( TagInfo.BODY_CONTENT_SCRIPTLESS ) ||
            (bodyType == JAVAX_BODY_CONTENT_PARAM) )
        {
            while (reader.hasMoreInput()) {
                if (reader.matchesETag(tag)) {
                    return;
                }
                
                if( bodyType.equalsIgnoreCase( TagInfo.BODY_CONTENT_JSP ) ) {
                    parseElements( parent );
                }
                else if( bodyType.equalsIgnoreCase( 
                    TagInfo.BODY_CONTENT_SCRIPTLESS ) ) 
                {
                    parseElementsScriptless( parent );
                }
                else if( bodyType == JAVAX_BODY_CONTENT_PARAM ) {
                    // (note the == since we won't recognize JAVAX_* 
                    // from outside this module).
                    reader.skipSpaces();
                    parseParam( parent );
                }
            }
            err.jspError(start, "jsp.error.unterminated", "<"+tag );
        }
        else {
	    err.jspError(start, "jasper.error.bad.bodycontent.type");
        }
    }

    /*
     * NamedAttributes ::= AttributeBody S?
     *                     ( '<jsp:attribute' AttributeBody S? )*
     *
     * AttributeBody   ::= ATTR[ !name, trim ] S? AttributeBodyRequired
     *
     * AttributeBodyRequired ::= '>' ( ScriptlessBody - '' ) '</jsp:attribute>'
     */
    private void parseNamedAttributes(Node parent) throws JasperException {
        do {
            Mark start = reader.mark();
            Attributes attrs = parseAttributes();
            reader.skipSpaces();

            Node.NamedAttribute namedAttributeNode =
                new Node.NamedAttribute( attrs, start, parent );

            if( reader.matches( "/>" ) ) {
                // Body was empty.  This is illegal, according to the grammar.
                err.jspError(reader.mark(), "jsp.error.empty.body.not.allowed", 
                    "<jsp:attribute" );
            }
            else if( !reader.matches( ">" ) ) {
                err.jspError(reader.mark(), "jsp.error.unterminated",
                    "<jsp:attribute");
            }

            if( reader.matches( "</jsp:attribute" ) ) {
                // Body was empty.  This is illegal, according to the grammar.
                err.jspError(reader.mark(), "jsp.error.empty.body.not.allowed", 
                    "<jsp:attribute" );
            }
            else {
                if( namedAttributeNode.isTrim() ) {
                    reader.skipSpaces();
                }
                parseBody( namedAttributeNode, "jsp:attribute", 
                    TagInfo.BODY_CONTENT_SCRIPTLESS );
                if( namedAttributeNode.isTrim() ) {
                    Node.Nodes subelements = namedAttributeNode.getBody();
                    Node lastNode = subelements.getNode(
                        subelements.size() - 1 );
                    if( lastNode instanceof Node.TemplateText ) {
                        ((Node.TemplateText)lastNode).rtrim();
                    }
                }
            }
            reader.skipSpaces();
        } while( reader.matches( "<jsp:attribute" ) );
    }

}

