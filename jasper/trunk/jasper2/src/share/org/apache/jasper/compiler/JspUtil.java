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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.StringTokenizer;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.logging.Logger;

import org.xml.sax.Attributes;

// EL interpreter (subject to change)
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ELParseException;
import org.apache.jasper.runtime.ExpressionEvaluatorImpl;

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
class JspUtil {

    // Delimiters for request-time expressions (JSP and XML syntax)
    private static final String OPEN_EXPR  = "<%=";
    private static final String CLOSE_EXPR = "%>";
    private static final String OPEN_EXPR_XML  = "%=";
    private static final String CLOSE_EXPR_XML = "%";

    private static int tempSequenceNumber = 0;
    private static ExpressionEvaluatorImpl expressionEvaluator = 
        new ExpressionEvaluatorImpl( null );

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
     * Checks if all mandatory attributes are present and if all attributes
     * present have valid names.  Checks attributes specified as XML-style
     * attributes as well as attributes specified using the jsp:attribute
     * standard action. 
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

        // Add names of attributes specified using jsp:attribute
        Node.Nodes tagBody = n.getBody();
        if( tagBody != null ) {
            int numSubElements = tagBody.size();
            for( int i = 0; i < numSubElements; i++ ) {
                Node node = tagBody.getNode( i );
                if( node instanceof Node.NamedAttribute ) {
                    String attrName = node.getAttributeValue( "name" );
                    temp.addElement( attrName );
		    // Check if this value appear in the attribute of the node
		    if (n.getAttributeValue(attrName) != null) {
			err.jspError(n, "jsp.error.duplicate.name.jspattribute",
					attrName);
		    }
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
	boolean rtexprvalue;	// not used now

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
     * Returns the <tt>Class</tt> object associated with the class or
     * interface with the given string name.
     *
     * <p> The <tt>Class</tt> object is determined by passing the given string
     * name to the <tt>Class.forName()</tt> method, unless the given string
     * name represents a primitive type, in which case it is converted to a
     * <tt>Class</tt> object by appending ".class" to it (e.g., "int.class").
     */
    public static Class toClass(String type, ClassLoader loader)
	    throws ClassNotFoundException {
	if ("boolean".equals(type))
	    return boolean.class;
	else if ("char".equals(type))
	    return char.class;
	else if ("byte".equals(type))
	    return byte.class;
	else if ("short".equals(type))
	    return short.class;
	else if ("int".equals(type))
	    return int.class;
	else if ("long".equals(type))
	    return long.class;
	else if ("float".equals(type))
	    return float.class;
	else if ("double".equals(type))
	    return double.class;
	else
	    return loader.loadClass(type);
    }

    /**
     * Produces a String representing a call to the EL interpreter.
     * @param expression a String containing zero or more "${}" expressions
     * @param expectedType the expected type of the interpreted result
     * @param defaultPrefix Default prefix, or literal "null"
     * @param fnmapvar Variable pointing to a function map.
     * @return a String representing a call to the EL interpreter.
     */
    public static String interpreterCall(boolean isTagFile,
					 String expression,
                                         Class expectedType,
                                         String defaultPrefix,
                                         String fnmapvar ) 
    {
        /*
         * Determine which context object to use.
         */
	String jspCtxt = null;
	if (isTagFile)
	    jspCtxt = "getJspContext()";
	else
	    jspCtxt = "pageContext";

 	/*
         * Determine whether to use the expected type's textual name
 	 * or, if it's a primitive, the name of its correspondent boxed
 	 * type.
         */
 	String targetType = expectedType.getName();
 	String primitiveConverterMethod = null;
 	if (expectedType.isPrimitive()) {
 	    if (expectedType.equals(Boolean.TYPE)) {
 	        targetType = Boolean.class.getName();
 		primitiveConverterMethod = "booleanValue";
 	    } else if (expectedType.equals(Byte.TYPE)) {
 	        targetType = Byte.class.getName();
 		primitiveConverterMethod = "byteValue";
 	    } else if (expectedType.equals(Character.TYPE)) {
 	        targetType = Character.class.getName();
 		primitiveConverterMethod = "charValue";
 	    } else if (expectedType.equals(Short.TYPE)) {
 	        targetType = Short.class.getName();
 		primitiveConverterMethod = "shortValue";
 	    } else if (expectedType.equals(Integer.TYPE)) {
 	        targetType = Integer.class.getName();
 		primitiveConverterMethod = "intValue";
 	    } else if (expectedType.equals(Long.TYPE)) {
 	        targetType = Long.class.getName();
 		primitiveConverterMethod = "longValue";
 	    } else if (expectedType.equals(Float.TYPE)) {
 	        targetType = Float.class.getName();
 		primitiveConverterMethod = "floatValue";
 	    } else if (expectedType.equals(Double.TYPE)) { 
 	        targetType = Double.class.getName();
 		primitiveConverterMethod = "doubleValue";
 	    }
 	}
 
 	/*
         * Build up the base call to the interpreter.
         */
        // XXX - We use a proprietary call to the interpreter for now
        // as the current standard machinery is inefficient and requires
        // lots of wrappers and adapters.  This should all clear up once
        // the EL interpreter moves out of JSTL and into its own project.
        // In the future, this should be replaced by code that calls
        // ExpressionEvaluator.parseExpression() and then cache the resulting
        // expression objects.  The interpreterCall would simply select
        // one of the pre-cached expressions and evaluate it.
        // Note that PageContextImpl implements VariableResolver and
        // the generated Servlet/SimpleTag implements FunctionMapper, so
        // that machinery is already in place (mroth).
 	StringBuffer call = new StringBuffer(
             "(" + targetType + ") "
               + "org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate"
               + "(" + Generator.quote(expression) + ", "
               +       targetType + ".class, "
	       +       "(PageContext)" + jspCtxt 
               +       ", " + fnmapvar + ", "
               +       ((defaultPrefix == null) ? 
                            "null" : Generator.quote( defaultPrefix )) 
               + ")");
 
 	/*
         * Add the primitive converter method if we need to.
         */
 	if (primitiveConverterMethod != null) {
 	    call.insert(0, "(");
 	    call.append(")." + primitiveConverterMethod + "()");
 	}
 
 	return call.toString();
    }

    /**
     * Validates the syntax of all ${} expressions within the given string.
     * @param where the approximate location of the expressions in the JSP page
     * @param expressions a string containing zero or more "${}" expressions
     * @param err an error dispatcher to use
     */
    public static void validateExpressions(Mark where,
                                           String expressions,
                                           Class expectedType,
                                           FunctionMapper functionMapper,
                                           String defaultPrefix,
                                           ErrorDispatcher err)
            throws JasperException {
        // Just parse and check if any exceptions are thrown.
        try {
            JspUtil.expressionEvaluator.parseExpression( expressions, 
                expectedType, functionMapper, defaultPrefix );
        }
        catch( ELParseException e ) {
            err.jspError(where, "jsp.error.invalid.expression", expressions,
                e.toString() );
        }
        catch( ELException e ) {
            err.jspError(where, "jsp.error.invalid.expression", expressions,
                e.toString() );
        }
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
    
    /**
     * Parses and encapsulates a function signature, as would appear in
     * a TLD.
     */
    public static class FunctionSignature {
        private String returnType;
        private String methodName;
        private Class[] parameterTypes;
        
        /**
         * Parses a function signature, as would appear in the TLD
         *
         * @param signature The signature to parse
         * @param tagName Name of tag, for error messages.
         * @throws JasperException If an error occurred while parsing the 
         *     signature.
         */
        public FunctionSignature( String signature, String tagName,
            ErrorDispatcher err, ClassLoader loader )
            throws JasperException
        {
            try {
                // Parse function signature, assuming syntax:
                // <return-type> S <method-name> S? '('
                // ( <arg-type> ( ',' <arg-type> )* )? ')'
                String ws = " \t\n\r";
                StringTokenizer sigTokenizer = new StringTokenizer( 
                    signature, ws + "(),", true);

                // Return type:
                this.returnType = sigTokenizer.nextToken();

                // Skip whitespace and read <method-name>:
                do {
                    this.methodName = sigTokenizer.nextToken();
                } while( ws.indexOf( this.methodName ) != -1 );

                // Skip whitespace and read '(':
                String paren;
                do {
                    paren = sigTokenizer.nextToken();
                } while( ws.indexOf( paren ) != -1 );

                if( !paren.equals( "(" ) ) {
                    throw new JasperException( err.getString(
                        "jsp.error.tld.fn.invalid.signature.parenexpected",
                        tagName, this.methodName ) );
                }

                // ( <arg-type> S? ( ',' S? <arg-type> S? )* )? ')'

                // Skip whitespace and read <arg-type>:
                String argType;
                do {
                    argType = sigTokenizer.nextToken();
                } while( ws.indexOf( argType ) != -1 );

                if( !argType.equals( ")" ) ) {
                    ArrayList parameterTypes = new ArrayList();
                    do {
                        if( ",(".indexOf( argType ) != -1 ) {
                            throw new JasperException( err.getString(
                                "jsp.error.tld.fn.invalid.signature",
                                tagName, this.methodName ) );
                        }

                        parameterTypes.add(toClass(argType, loader));

                        String comma;
                        do {
                            comma = sigTokenizer.nextToken();
                        } while( ws.indexOf( comma ) != -1 );

                        if( comma.equals( ")" ) ) {
                            break;
                        }
                        if( !comma.equals( "," ) ) {
                            throw new JasperException( err.getString(
                             "jsp.error.tld.fn.invalid.signature.commaexpected",
                                tagName, this.methodName ) );
                        }

                        // <arg-type>
                        do {
                            argType = sigTokenizer.nextToken();
                        } while( ws.indexOf( argType ) != -1 );
                    } while( true );
                    this.parameterTypes = (Class[])parameterTypes.toArray( 
                        new Class[parameterTypes.size()] );
                }
            }
            catch( NoSuchElementException e ) {
                throw new JasperException( err.getString(
                    "jsp.error.tld.fn.invalid.signature",
                    tagName, this.methodName ) );
            }
            catch( ClassNotFoundException e ) {
                throw new JasperException( err.getString(
                    "jsp.error.tld.fn.invalid.signature.classnotfound",
                    e.getMessage(), tagName, this.methodName ) );
            }
        }
        
        public String getReturnType() {
            return this.returnType;
        }
        
        public String getMethodName() {
            return this.methodName;
        }
        
        public Class[] getParameterTypes() {
            return this.parameterTypes;
        }    
    }
}




