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

import java.net.URL;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.logging.Logger;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

// EL interpreter (subject to change)
import javax.servlet.jsp.el.ExpressionEvaluator;
import org.apache.jasper.runtime.ExpressionEvaluatorManager;

/** 
 * This class has all the utility method(s).
 * Ideally should move all the bean containers here.
 *
 * @author Mandar Raje.
 * @author Rajiv Mordani.
 * @author Danno Ferrin
 * @author Pierre Delisle
 * @author Shawn Bayern
 * @author Mark Roth
 */
public class JspUtil {

    // Delimiters for request-time expressions (JSP and XML syntax)
    private static final String OPEN_EXPR  = "<%=";
    private static final String CLOSE_EXPR = "%>";
    private static final String OPEN_EXPR_XML  = "%=";
    private static final String CLOSE_EXPR_XML = "%";

    private static ErrorHandler errorHandler = new MyErrorHandler();
    private static EntityResolver entityResolver = new MyEntityResolver();
    private static int tempSequenceNumber = 0;

    public static char[] removeQuotes(char []chars) {
	CharArrayWriter caw = new CharArrayWriter();
	for (int i = 0; i < chars.length; i++) {
	    if (chars[i] == '%' && chars[i+1] == '\\' &&
		chars[i+2] == '\\' && chars[i+3] == '>') {
		caw.write('%');
		caw.write('>');
		i = i + 3;
	    }
	    else caw.write(chars[i]);
	}
	return caw.toCharArray();
    }

    public static char[] escapeQuotes (char []chars) {
        // Prescan to convert %\> to %>
        String s = new String(chars);
        while (true) {
            int n = s.indexOf("%\\>");
            if (n < 0)
                break;
            StringBuffer sb = new StringBuffer(s.substring(0, n));
            sb.append("%>");
            sb.append(s.substring(n + 3));
            s = sb.toString();
        }
        chars = s.toCharArray();
        return (chars);


        // Escape all backslashes not inside a Java string literal
        /*
        CharArrayWriter caw = new CharArrayWriter();
        boolean inJavaString = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '"') inJavaString = !inJavaString;
            // escape out the escape character
            if (!inJavaString && (chars[i] == '\\')) caw.write('\\');
            caw.write(chars[i]);
        }
        return caw.toCharArray();
        */
    }

    /**
     * Checks if the token is a runtime expression.
     * In standard JSP syntax, a runtime expression starts with '<%' and
     * ends with '%>'. When the JSP document is in XML syntax, a runtime
     * expression starts with '%=' and ends with '%'.
     *
     * @param token The token to be checked
     * return whether the token is a runtime expression or not.
     */
    public static boolean isExpression(String token, boolean isXml) {
	String openExpr;
	String closeExpr;
	if (isXml) {
	    openExpr = OPEN_EXPR_XML;
	    closeExpr = CLOSE_EXPR_XML;
	} else {
	    openExpr = OPEN_EXPR;
	    closeExpr = CLOSE_EXPR;
	}
	if (token.startsWith(openExpr) && token.endsWith(closeExpr)) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * @return the "expression" part of a runtime expression, 
     * taking the delimiters out.
     */
    public static String getExpr (String expression, boolean isXml) {
	String returnString;
	String openExpr;
	String closeExpr;
	if (isXml) {
	    openExpr = OPEN_EXPR_XML;
	    closeExpr = CLOSE_EXPR_XML;
	} else {
	    openExpr = OPEN_EXPR;
	    closeExpr = CLOSE_EXPR;
	}
	int length = expression.length();
	if (expression.startsWith(openExpr) && 
                expression.endsWith(closeExpr)) {
	    returnString = expression.substring(
                               openExpr.length(), length - closeExpr.length());
	} else {
	    returnString = "";
	}
	return returnString;
    }

    /**
     * Takes a potential expression and converts it into XML form
     */
    public static String getExprInXml(String expression) {
        String returnString;
        int length = expression.length();

        if (expression.startsWith(OPEN_EXPR) 
                && expression.endsWith(CLOSE_EXPR)) {
            returnString = expression.substring (1, length - 1);
        } else {
            returnString = expression;
        }

        return escapeXml(returnString);
    }

    /**
     * Parses the XML document contained in the InputStream.
     *
     * @deprecated Use ParserUtils.parseXMLDocument() instead
     */
    public static Document parseXMLDoc(String uri, InputStream in) 
	throws JasperException 
    {
	return parseXMLDocJaxp(uri, in);
    }

    /**
     * Parses the XML document contained in the InputStream.
     * This XML document is either web.xml or a tld.
     * [The TLD has to be cached internally (see MyEntityResolver)]
     *
     * @deprecated Use ParserUtils.parseXMLDocument() instead
     */
    public static Document parseXMLDocJaxp(String uri, InputStream in)
	throws JasperException
    {
	try {
	    Document doc;
	    DocumentBuilderFactory docFactory = 
		DocumentBuilderFactory.newInstance();
	    docFactory.setValidating(true);
	    docFactory.setNamespaceAware(true);
	    DocumentBuilder builder = docFactory.newDocumentBuilder();
	    builder.setEntityResolver(entityResolver);
	    builder.setErrorHandler(errorHandler);
	    doc = builder.parse(in);
	    return doc;
	} catch (ParserConfigurationException ex) {
            throw new JasperException(
	        Constants.getString("jsp.error.parse.xml",
				    new Object[]{uri, ex.getMessage()}));
	} catch (SAXParseException ex) {
            throw new JasperException(
	        Constants.getString("jsp.error.parse.xml.line",
				    new Object[]{uri,
						 new Integer(ex.getLineNumber()),
						 new Integer(ex.getColumnNumber()),
						 ex.getMessage()}));
	} catch (SAXException sx) {
            throw new JasperException(
                Constants.getString("jsp.error.parse.xml",
				    new Object[]{uri, sx.getMessage()}));
        } catch (IOException io) {
            throw new JasperException(
                Constants.getString("jsp.error.parse.xml",
				    new Object[]{uri, io.toString()}));
	}
    }

    /**
     * Checks if all mandatory attributes are present and if all attributes
     * present have valid names.  Checks attributes specified as XML-style
     * attributes as well as attributes specified using the jsp:attribute
     * standard action.  Also verifies that any attributes specified
     * via jsp:attribute are rtexprvalue attributes.
     */
    public static void checkAttributes(String typeOfTag,
				       Node n,
				       ValidAttribute[] validAttributes,
				       ErrorDispatcher err)
	                        throws JasperException {
        Attributes attrs = n.getAttributes();
        Mark start = n.getStart();
	boolean valid = true;

        // AttributesImpl.removeAttribute is broken, so we do this...
        int tempLength = (attrs == null) ? 0 : attrs.getLength();
	Vector temp = new Vector(tempLength, 1);
        for (int i = 0; i < tempLength; i++) {
            String qName = attrs.getQName(i);
            if ((!qName.equals("xmlns")) && (!qName.startsWith("xmlns:")))
                temp.addElement(qName);
        }

        // Add names of attributes specified using jsp:attribute and
        // check that they are rtexprvalues while we're at it.
        Node.Nodes tagBody = n.getBody();
        if( tagBody != null ) {
            int numSubElements = tagBody.size();
            for( int i = 0; i < numSubElements; i++ ) {
                Node node = tagBody.getNode( i );
                if( node instanceof Node.NamedAttribute ) {
                    String attrName = node.getAttributeValue( "name" );
                    // Verify that this node is an rtexprvalue.
                    for( int j = 0; j < validAttributes.length; j++ ) {
                        if( validAttributes[j].name.equals( attrName ) &&
                            !validAttributes[j].rtexprvalue )
                        {
                            valid = false;
                            err.jspError(start,
                                         "jsp.error.named.attribute.not.rt",
                                         attrName );
                            break;
                        }
                    }
                    if( valid ) {
                        temp.addElement( attrName );
                    }
                    valid = true;
                }
                else {
                    // Nothing can come before jsp:attribute, and only
                    // jsp:body can come after it.
                    break;
                }
            }
        }

	/*
	 * First check to see if all the mandatory attributes are present.
	 * If so only then proceed to see if the other attributes are valid
	 * for the particular tag.
	 */
	String missingAttribute = null;

	for (int i = 0; i < validAttributes.length; i++) {
	    int attrPos;    
	    if (validAttributes[i].mandatory) {
                attrPos = temp.indexOf(validAttributes[i].name);
	        if (attrPos != -1) {
	            temp.remove(attrPos);
		    valid = true;
		} else {
		    valid = false;
		    missingAttribute = validAttributes[i].name;
		    break;
		}
	    }
	}

	// If mandatory attribute is missing then the exception is thrown
	if (!valid)
	    err.jspError(start, "jsp.error.mandatory.attribute", typeOfTag,
			 missingAttribute);

	// Check to see if there are any more attributes for the specified tag.
        int attrLeftLength = temp.size();
	if (attrLeftLength == 0)
	    return;

	// Now check to see if the rest of the attributes are valid too.
	String attribute = null;

	for (int j = 0; j < attrLeftLength; j++) {
	    valid = false;
	    attribute = (String) temp.elementAt(j);
	    for (int i = 0; i < validAttributes.length; i++) {
	        if (attribute.equals(validAttributes[i].name)) {
		    valid = true;
		    break;
		}
	    }
	    if (!valid)
	        err.jspError(start, "jsp.error.invalid.attribute", typeOfTag,
			     attribute);
	}
	// XXX *could* move EL-syntax validation here... (sb)
    }
    
    public static String escapeQueryString(String unescString) {
	if ( unescString == null )
	    return null;
	
	String escString    = "";
	String shellSpChars = "\\\"";
	
	for(int index=0; index<unescString.length(); index++) {
	    char nextChar = unescString.charAt(index);
	    
	    if( shellSpChars.indexOf(nextChar) != -1 )
		escString += "\\";
	    
	    escString += nextChar;
	}
	return escString;
    }
 
    /**
     *  Escape the 5 entities defined by XML.
     */
    public static String escapeXml(String s) {
        if (s == null) return null;
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Replaces any occurrences of the character <tt>replace</tt> with the
     * string <tt>with</tt>.
     */
    public static String replace(String name, char replace, String with) {
	StringBuffer buf = new StringBuffer();
	int begin = 0;
	int end;
	int last = name.length();

	while (true) {
	    end = name.indexOf(replace, begin);
	    if (end < 0) {
		end = last;
	    }
	    buf.append(name.substring(begin, end));
	    if (end == last) {
		break;
	    }
	    buf.append(with);
	    begin = end + 1;
	}
	
	return buf.toString();
    }

    public static class ValidAttribute {
   	String name;
	boolean mandatory;
	boolean rtexprvalue;

	public ValidAttribute (String name, boolean mandatory,
            boolean rtexprvalue )
        {
	    this.name = name;
	    this.mandatory = mandatory;
            this.rtexprvalue = rtexprvalue;
        }

       public ValidAttribute (String name, boolean mandatory) {
            this( name, mandatory, false );
	}

	public ValidAttribute (String name) {
	    this (name, false);
	}
    }
    
    public static Hashtable attrsToHashtable(Attributes attrs) {
        int len = attrs.getLength();
        Hashtable table = new Hashtable(len);
        for (int i=0; i<len; i++) {
            table.put(attrs.getQName(i), attrs.getValue(i));
        }
        return table;
    }

    /**
     * Get the data for the first child associated with the
     * Element provided as argument. It is assumed that this
     * first child is of type Text.
     *
     * @param e the DOM Element to read from 
     * @return the data associated with the first child of the DOM
     *  element.
     */
    public static String getElementChildTextData(Element e) {
	String s = null;
	Text t = (Text)e.getFirstChild();
	if (t != null) {
	    s = t.getData();
	    if (s != null) {
		s = s.trim();
	    }
	}
	return s;
    }

    /**
     * Convert a String value to 'boolean'.
     * Besides the standard conversions done by
     * Boolean.valueOf(s).booleanValue(), the value "yes"
     * (ignore case) is also converted to 'true'. 
     * If 's' is null, then 'false' is returned.
     *
     * @param s the string to be converted
     * @return the boolean value associated with the string s
     */
    public static boolean booleanValue(String s) {
	boolean b = false;
	if (s != null) {
	    if (s.equalsIgnoreCase("yes")) {
		b = true;
	    } else {
		b = Boolean.valueOf(s).booleanValue();
	    }
	}
	return b;
    }

    /**
     * Produces a String representing a call to the EL interpreter.
     * @param expression a String containing zero or more "${}" expressions
     * @param expectedType the expected type of the interpreted result
     * @param fnMap Variable name containing function map, or literal "null"
     * @param defaultPrefix Default prefix, or literal "null"
     * @return a String representing a call to the EL interpreter.
     */
    public static String interpreterCall(boolean isTagFile,
					 String expression,
                                         Class expectedType,
					 String prefixMap,
                                         String fnMap,
                                         String defaultPrefix) 
    {
	String jspCtxt = null;
	if (isTagFile)
	    jspCtxt = "getJspContext()";
	else
	    jspCtxt = "pageContext";

        return "(" + expectedType.getName() + ") "
               + Constants.EL_INTERPRETER_CONDUIT_CLASS + "."
               + Constants.EL_INTERPRETER_CONDUIT_METHOD
               + "(" + Generator.quote(expression) + ", "
               +       expectedType.getName() + ".class, "
	       +       jspCtxt + ", "
               +       prefixMap + ", "
               +       fnMap + ", "
               +       Generator.quote( defaultPrefix ) + ")";
    }

    /**
     * Validates the syntax of all ${} expressions within the given string.
     * @param where the approximate location of the expressions in the JSP page
     * @param expressions a string containing zero or more "${}" expressions
     * @param err an error dispatcher to use
     * @param extraInfo info (such as the name of the current attribute)
     *        to be included in any error messages
     */
    public static void validateExpressions(Mark where,
                                           String expressions,
                                           ErrorDispatcher err)
            throws JasperException {
        ExpressionEvaluator el = null;
        try {
            // XXX when the EL moves to Jakarta Commons, this can
            //     be replaced with a *much* cleaner interface.  I apologize
            //     for it for the moment! (SB)
            el = ExpressionEvaluatorManager.getEvaluatorByName(
                    ExpressionEvaluatorManager.EVALUATOR_CLASS);
        } catch (javax.servlet.jsp.JspException uglyEx) {
            err.jspError(where, "jsp.error.internal.evaluator_not_found");
        }
        String errMsg = el.validate(expressions);
        if (errMsg != null)
            err.jspError(where, "jsp.error.invalid.expression", expressions,
                errMsg);
    }

    /**
     * Resets the temporary variable name.
     * (not thread-safe)
     */
    public static void resetTemporaryVariableName() {
        tempSequenceNumber = 0;
    }

    /**
     * Generates a new temporary variable name.
     * (not thread-safe)
     */
    public static String nextTemporaryVariableName() {
        return Constants.TEMP_VARIABLE_NAME_PREFIX + (tempSequenceNumber++);
    }
}


class MyEntityResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException
    {
	for (int i=0; i<Constants.CACHED_DTD_PUBLIC_IDS.length; i++) {
	    String cachedDtdPublicId = Constants.CACHED_DTD_PUBLIC_IDS[i];
	    if (cachedDtdPublicId.equals(publicId)) {
		String resourcePath = Constants.CACHED_DTD_RESOURCE_PATHS[i];
		InputStream input =
		    this.getClass().getResourceAsStream(resourcePath);
		if (input == null) {
		    throw new SAXException(
                        Constants.getString("jsp.error.internal.filenotfound", 
					    new Object[]{resourcePath}));
		}
		InputSource isrc =
		    new InputSource(input);
		return isrc;
	    }
	}
	Constants.message("jsp.error.parse.xml.invalidPublicId",
				new Object[]{publicId}, Logger.ERROR);
        return null;
    }
}

class MyErrorHandler implements ErrorHandler {
    public void warning(SAXParseException ex)
	throws SAXException
    {
	// We ignore warnings
    }

    public void error(SAXParseException ex)
	throws SAXException
    {
	throw ex;
    }

    public void fatalError(SAXParseException ex)
	throws SAXException
    {
	throw ex;
    }
}




