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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;

import org.xml.sax.Attributes;

// EL interpreter (subject to change)
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ELParseException;
import org.apache.commons.el.ExpressionEvaluatorImpl;

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

    private static final String WEB_INF_TAGS = "/WEB-INF/tags/";
    private static final String META_INF_TAGS = "/META-INF/tags/";

    // Delimiters for request-time expressions (JSP and XML syntax)
    private static final String OPEN_EXPR  = "<%=";
    private static final String CLOSE_EXPR = "%>";
    private static final String OPEN_EXPR_XML  = "%=";
    private static final String CLOSE_EXPR_XML = "%";

    private static int tempSequenceNumber = 0;
    private static ExpressionEvaluatorImpl expressionEvaluator
	= new ExpressionEvaluatorImpl();

    public static char[] removeQuotes(char []chars) {
        CharArrayWriter caw = new CharArrayWriter();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%' && chars[i+1] == '\\' &&
                chars[i+2] == '>') {
                caw.write('%');
                caw.write('>');
                i = i + 2;
            } else {
                caw.write(chars[i]);
            }
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

        return escapeXml(returnString.replace(Constants.ESC, '$'));
    }

    /**
     * Checks to see if the given scope is valid.
     *
     * @param scope The scope to be checked
     * @param n The Node containing the 'scope' attribute whose value is to be
     * checked
     * @param err error dispatcher
     *
     * @throws JasperException if scope is not null and different from
     * &quot;page&quot;, &quot;request&quot;, &quot;session&quot;, and
     * &quot;application&quot;
     */
    public static void checkScope(String scope, Node n, ErrorDispatcher err)
            throws JasperException {
	if (scope != null && !scope.equals("page") && !scope.equals("request")
		&& !scope.equals("session") && !scope.equals("application")) {
	    err.jspError(n, "jsp.error.invalid.scope", scope);
	}
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

	Class c = null;
	int i0 = type.indexOf('[');
	int dims = 0;
	if (i0 > 0) {
	    // This is an array.  Count the dimensions
	    for (int i = 0; i < type.length(); i++) {
		if (type.charAt(i) == '[')
		    dims++;
	    }
	    type = type.substring(0, i0);
	}

	if ("boolean".equals(type))
	    c = boolean.class;
	else if ("char".equals(type))
	    c = char.class;
	else if ("byte".equals(type))
	    c =  byte.class;
	else if ("short".equals(type))
	    c = short.class;
	else if ("int".equals(type))
	    c = int.class;
	else if ("long".equals(type))
	    c = long.class;
	else if ("float".equals(type))
	    c = float.class;
	else if ("double".equals(type))
	    c = double.class;
	else if (type.indexOf('[') < 0)
	    c = loader.loadClass(type);

	if (dims == 0)
	    return c;

	if (dims == 1)
	    return java.lang.reflect.Array.newInstance(c, 1).getClass();

	// Array of more than i dimension
	return java.lang.reflect.Array.newInstance(c, new int[dims]).getClass();
    }

    /**
     * Produces a String representing a call to the EL interpreter.
     * @param expression a String containing zero or more "${}" expressions
     * @param expectedType the expected type of the interpreted result
     * @param fnmapvar Variable pointing to a function map.
     * @param XmlEscape True if the result should do XML escaping
     * @return a String representing a call to the EL interpreter.
     */
    public static String interpreterCall(boolean isTagFile,
					 String expression,
                                         Class expectedType,
                                         String fnmapvar,
                                         boolean XmlEscape ) 
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
 
	if (primitiveConverterMethod != null) {
	    XmlEscape = false;
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
	targetType = toJavaSourceType(targetType);
	StringBuffer call = new StringBuffer(
             "(" + targetType + ") "
               + "org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate"
               + "(" + Generator.quote(expression) + ", "
               +       targetType + ".class, "
	       +       "(PageContext)" + jspCtxt 
               +       ", " + fnmapvar
	       + ", " + XmlEscape
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
                                           ErrorDispatcher err)
            throws JasperException {

        try {
            JspUtil.expressionEvaluator.parseExpression( expressions, 
                expectedType, null );
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

    public static String coerceToPrimitiveBoolean(String s,
						  boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToBoolean(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "false";
	    else
		return Boolean.valueOf(s).toString();
	}
    }

    public static String coerceToBoolean(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Boolean) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Boolean.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Boolean(false)";
	    } else {
		// Detect format error at translation time
		return "new Boolean(" + Boolean.valueOf(s).toString() + ")";
	    }
	}
    }

    public static String coerceToPrimitiveByte(String s,
					       boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToByte(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "(byte) 0";
	    else
		return "((byte)" + Byte.valueOf(s).toString() + ")";
	}
    }

    public static String coerceToByte(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Byte) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Byte.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Byte((byte) 0)";
	    } else {
		// Detect format error at translation time
		return "new Byte((byte)" + Byte.valueOf(s).toString() + ")";
	    }
	}
    }

    public static String coerceToChar(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToChar(" + s + ")";
	} else {
	    if (s == null || s.length() == 0) {
		return "(char) 0";
	    } else {
		char ch = s.charAt(0);
		// this trick avoids escaping issues
		return "((char) " + (int) ch + ")";
	    }
	}
    }

    public static String coerceToCharacter(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Character) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Character.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Character((char) 0)";
	    } else {
		char ch = s.charAt(0);
		// this trick avoids escaping issues
		return "new Character((char) " + (int) ch + ")";
	    }
	}
    }

    public static String coerceToPrimitiveDouble(String s,
						 boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToDouble(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "(double) 0";
	    else
		return Double.valueOf(s).toString();
	}
    }

    public static String coerceToDouble(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Double) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Double.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Double(0)";
	    } else {
		// Detect format error at translation time
		return "new Double(" + Double.valueOf(s).toString() + ")";
	    }
	}
    }

    public static String coerceToPrimitiveFloat(String s,
						boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToFloat(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "(float) 0";
	    else
		return Float.valueOf(s).toString() + "f";
	}
    }

    public static String coerceToFloat(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Float) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Float.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Float(0)";
	    } else {
		// Detect format error at translation time
		return "new Float(" + Float.valueOf(s).toString() + "f)";
	    }
	}
    }

    public static String coerceToInt(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToInt(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "0";
	    else
		return Integer.valueOf(s).toString();
	}
    }

    public static String coerceToInteger(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Integer) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Integer.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Integer(0)";
	    } else {
		// Detect format error at translation time
		return "new Integer(" + Integer.valueOf(s).toString() + ")";
	    }
	}
    }

    public static String coerceToPrimitiveShort(String s,
						boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToShort(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "(short) 0";
	    else
		return "((short) " + Short.valueOf(s).toString() + ")";
	}
    }
    
    public static String coerceToShort(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Short) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Short.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Short((short) 0)";
	    } else {
		// Detect format error at translation time
		return "new Short(\"" + Short.valueOf(s).toString() + "\")";
	    }
	}
    }
    
    public static String coerceToPrimitiveLong(String s,
					       boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToLong(" + s + ")";
	} else {
	    if (s == null || s.length() == 0)
		return "(long) 0";
	    else
		return Long.valueOf(s).toString() + "l";
	}
    }

    public static String coerceToLong(String s, boolean isNamedAttribute) {
	if (isNamedAttribute) {
	    return "(Long) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Long.class)";
	} else {
	    if (s == null || s.length() == 0) {
		return "new Long(0)";
	    } else {
		// Detect format error at translation time
		return "new Long(" + Long.valueOf(s).toString() + "l)";
	    }
	}
    }

    public static InputStream getInputStream(String fname, JarFile jarFile,
					     JspCompilationContext ctxt,
					     ErrorDispatcher err)
		throws JasperException, IOException {

        InputStream in = null;

	if (jarFile != null) {
	    String jarEntryName = fname.substring(1, fname.length());
	    ZipEntry jarEntry = jarFile.getEntry(jarEntryName);
	    if (jarEntry == null) {
		err.jspError("jsp.error.file.not.found", fname);
	    }
	    in = jarFile.getInputStream(jarEntry);
	} else {
	    in = ctxt.getResourceAsStream(fname);
	}

	if (in == null) {
	    err.jspError("jsp.error.file.not.found", fname);
	}

	return in;
    }

    /**
     * Gets the fully-qualified class name of the tag handler corresponding to
     * the given tag file path.
     *
     * @param path Tag file path
     * @param err Error dispatcher
     *
     * @return Fully-qualified class name of the tag handler corresponding to 
     * the given tag file path
     */
    public static String getTagHandlerClassName(String path,
						ErrorDispatcher err)
                throws JasperException {

	String className = null;
	int begin = 0;
	int index;
	
	// Remove ".tag" suffix
	index = path.lastIndexOf(".tag");
	if (index != -1) {
	    path = path.substring(0, index);
	} else {
	    err.jspError("jsp.error.tagfile.badSuffix", path);
	}

	index = path.indexOf(WEB_INF_TAGS);
	if (index != -1) {
	    className = "org.apache.jsp.tag.web.";
	    begin = index + WEB_INF_TAGS.length();
	} else {
	    index = path.indexOf(META_INF_TAGS);
	    if (index != -1) {
		className = "org.apache.jsp.tag.meta.";
		begin = index + META_INF_TAGS.length();
	    } else {
		err.jspError("jsp.error.tagfile.illegalPath", path);
	    }
	}

	className += path.substring(begin).replace('/', '.');

	return className;
    }

    static InputStreamReader getReader(String fname, String encoding,
				       JarFile jarFile,
				       JspCompilationContext ctxt,
				       ErrorDispatcher err)
		throws JasperException, IOException {

        InputStreamReader reader = null;
	InputStream in = getInputStream(fname, jarFile, ctxt, err);

	try {
            reader = new InputStreamReader(in, encoding);
	} catch (UnsupportedEncodingException ex) {
	    err.jspError("jsp.error.unsupported.encoding", encoding);
	}

	return reader;
    }

    /**
     * Class.getName() return arrays in the form "[[[<et>", where et,
     * the element type can be one of ZBCDFIJS or L<classname>;
     * It is converted into forms that can be understood by javac.
     */
    private static String toJavaSourceType(String type) {

	if (type.charAt(0) != '[') {
	    return type;
 	}

	int dims = 1;
	String t = null;
	for (int i = 1; i < type.length(); i++) {
	    if (type.charAt(i) == '[') {
		dims++;
	    } else {
		switch (type.charAt(i)) {
		case 'Z': t = "boolean"; break;
		case 'B': t = "byte"; break;
		case 'C': t = "char"; break;
		case 'D': t = "double"; break;
		case 'F': t = "float"; break;
		case 'I': t = "int"; break;
		case 'J': t = "long"; break;
		case 'S': t = "short"; break;
		case 'L': t = type.substring(i+1, type.indexOf(';')); break;
		}
		break;
	    }
	}
	StringBuffer resultType = new StringBuffer(t);
	for (; dims > 0; dims--) {
	    resultType.append("[]");
	}
	return resultType.toString();
    }
}

