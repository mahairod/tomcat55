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

import java.util.*;
import java.beans.*;
import java.net.URLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import javax.servlet.jsp.tagext.*;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.runtime.JspRuntimeLibrary;
import org.apache.jasper.Constants;

/**
 * Generate Java source from Nodes
 *
 * @author Anil K. Vijendran
 * @author Danno Ferrin
 * @author Mandar Raje
 * @author Rajiv Mordani
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Jan Luehe
 * @author Shawn Bayern
 * @author Mark Roth
 * @author Denis Benoit
 */

public class Generator {

    private static final Class[] OBJECT_CLASS = { Object.class};
    private ServletWriter out;
    private MethodsBuffer methodsBuffer;
    private FragmentHelperClass fragmentHelperClass;
    private ErrorDispatcher err;
    private BeanRepository beanInfo;
    private JspCompilationContext ctxt;
    private boolean breakAtLF;
    private PageInfo pageInfo;
    private int maxTagNesting;
    private Vector tagHandlerPoolNames;

    /**
     * @param s the input string
     * @return quoted and escaped string, per Java rule
     */
    static String quote(String s) {

	if (s == null)
	    return "null";
        
        return '"' + escape( s ) + '"';
    }
    
    /**
     * @param s the input string
     * @return escaped string, per Java rule
     */
    static String escape(String s) {

	if (s == null)
	    return "";

	StringBuffer b = new StringBuffer();
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (c == '"') 
		b.append('\\').append('"');
	    else if (c == '\\')
		b.append('\\').append('\\');
	    else if (c == '\n')
		b.append('\\').append('n');
	    else if (c == '\r')
		b.append('\\').append('r');
	    else 
		b.append(c);
	}
	return b.toString();
    }

    /**
     * Generates declarations.  This includes "info" of the page directive,
     * and scriptlet declarations.
     */
    private void generateDeclarations(Node.Nodes page) throws JasperException {

	class DeclarationVisitor extends Node.Visitor {

	    public void visit(Node.PageDirective n) throws JasperException {
		String info = n.getAttributeValue("info");
		if (info == null)
		    return;

		out.printil("public String getServletInfo() {");
		out.pushIndent();
		out.printin("return ");
		out.print  (quote(info));
		out.println(";");
		out.popIndent();
		out.print  ('}');
		out.println();
	    }

	    public void visit(Node.Declaration n) throws JasperException {
		out.printMultiLn(new String(n.getText()));
	    }
	}

	out.println();
	page.visit(new DeclarationVisitor());
    }

    /**
     * Compiles list of tag handler pool names.
     */
    private void compileTagHandlerPoolList(Node.Nodes page)
	    throws JasperException {

	class TagHandlerPoolVisitor extends Node.Visitor {

	    private Vector names;

	    /*
	     * Constructor
	     *
	     * @param v Vector of tag handler pool names to populate
	     */
	    TagHandlerPoolVisitor(Vector v) {
		names = v;
	    }

	    /*
	     * Gets the name of the tag handler pool for the given custom tag
	     * and adds it to the list of tag handler pool names unless it is
	     * already contained in it.
	     */
	    public void visit(Node.CustomTag n) throws JasperException {

		if (!n.implementsSimpleTag()) {
		    String name = createTagHandlerPoolName(n.getPrefix(),
							   n.getShortName(),
							   n.getAttributes());
		    n.setTagHandlerPoolName(name);
		    if (!names.contains(name)) {
			names.add(name);
		    }
		}
		visitBody(n);
	    }

	    /*
	     * Creates the name of the tag handler pool whose tag handlers may
	     * be (re)used to service this action.
	     *
	     * @return The name of the tag handler pool
	     */
	    private String createTagHandlerPoolName(String prefix,
						    String shortName,
						    Attributes attrs) {
		String poolName = null;

		if (prefix.indexOf('-') >= 0)
		    prefix = JspUtil.replace(prefix, '-', "$1");
		if (prefix.indexOf('.') >= 0)
		    prefix = JspUtil.replace(prefix, '.', "$2");

		if (shortName.indexOf('-') >= 0)
		    shortName = JspUtil.replace(shortName, '-', "$1");
		if (shortName.indexOf('.') >= 0)
		    shortName = JspUtil.replace(shortName, '.', "$2");
		if (shortName.indexOf(':') >= 0)
		    shortName = JspUtil.replace(shortName, ':', "$3");

		poolName = "_jspx_tagPool_" + prefix + "_" + shortName;
		if (attrs != null) {
		    String[] attrNames = new String[attrs.getLength()];
		    for (int i=0; i<attrNames.length; i++) {
			attrNames[i] = attrs.getQName(i);
		    }
		    Arrays.sort(attrNames, Collections.reverseOrder());
		    for (int i=0; i<attrNames.length; i++) {
			poolName = poolName + "_" + attrNames[i];
		    }
		}
		return poolName;
	    }
	}

	page.visit(new TagHandlerPoolVisitor(tagHandlerPoolNames));
    }
 
    /*
     * For every custom tag, declares its scripting variables with AT_BEGIN
     * and AT_END scopes.
     */
    private void declareAtBeginAtEndScriptingVariables(Node.Nodes page)
	    throws JasperException {

	class ScriptingVariableDeclarationVisitor extends Node.Visitor {

	    /*
	     * Vector keeping track of which scripting variables have already
	     * been declared
	     */
	    private Vector scriptVars;

	    /*
	     * Constructor.
	     */
	    public ScriptingVariableDeclarationVisitor() {
		scriptVars = new Vector();
	    }

	    public void visit(Node.CustomTag n) throws JasperException {

		TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
		VariableInfo[] varInfos = n.getVariableInfos();

		if ((varInfos == null) && (tagVarInfos == null)) {
		    visitBody(n);
		}

		if (varInfos != null) {
		    for (int i=0; i<varInfos.length; i++) {
			int scope = varInfos[i].getScope();
			String varName = varInfos[i].getVarName();
			if (((scope == VariableInfo.AT_BEGIN)
			                     || (scope == VariableInfo.AT_END))
			        && varInfos[i].getDeclare()
			        && !scriptVars.contains(varName)) {
			    out.printin(varInfos[i].getClassName());
			    out.print(" ");
			    out.print(varName);
			    out.println(" = null;");
			    scriptVars.add(varName);
			}
		    }
		} else {
		    for (int i=0; i<tagVarInfos.length; i++) {
			int scope = tagVarInfos[i].getScope();
			String varName = tagVarInfos[i].getNameGiven();
			if (varName == null) {
			    varName = n.getTagData().getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
			}
			if (((scope == VariableInfo.AT_BEGIN)
			                     || (scope == VariableInfo.AT_END))
			        && tagVarInfos[i].getDeclare()
			        && !scriptVars.contains(varName)) {
			    out.printin(tagVarInfos[i].getClassName());
			    out.print(" ");
			    out.print(varName);
			    out.println(" = null;");
			    scriptVars.add(varName);
			}
		    }
		}

		visitBody(n);
	    }
	}

	page.visit(new ScriptingVariableDeclarationVisitor());
    }

    /**
     * Generates the destroy() method which is responsible for calling the
     * release() method on every tag handler in any of the tag handler pools.
     */
    private void generateDestroy() {
	out.printil("public void jspDestroy() {");
	out.pushIndent();
	for (int i=0; i<tagHandlerPoolNames.size(); i++) {
	    out.printin((String) tagHandlerPoolNames.elementAt(i));
	    out.println(".release();");
	}
	out.popIndent();	
	out.printil("}");
	out.println();
    }

    /**
     * Generates the beginning of the static portion of the servelet.
     */
    private void generatePreamble(Node.Nodes page) throws JasperException {

	String servletPackageName = ctxt.getServletPackageName();
	String servletClassName = ctxt.getServletClassName();
	String serviceMethodName = Constants.SERVICE_METHOD_NAME;

	// First the package name:

	if (! "".equals(servletPackageName) && servletPackageName != null) {
	    out.printil("package " + servletPackageName + ";");
	    out.println();
        }

	// Generate imports

	Iterator iter = pageInfo.getImports().iterator();
	while (iter.hasNext()) {
	    out.printin("import ");
	    out.print  ((String)iter.next());
	    out.println(";");
	}
	out.println();

	// Generate class declaration

	out.printin("public class ");
	out.print  (servletClassName);
	out.print  (" extends ");
	out.print  (pageInfo.getExtends());
	if (!pageInfo.isThreadSafe()) {
	    out.print("implements SingleThreadModel");
	}
	out.println(" {");
	out.pushIndent();

	// Class body begins here

	generateDeclarations(page);
	out.println();

	// Static initializations here

        // Static data for getIncludes()
        out.printil("private static java.util.Vector _jspx_includes;");
        out.println();
        List includes = pageInfo.getIncludes();
        iter = includes.iterator();
        if( !includes.isEmpty() ) {
            out.printil("static {");
            out.pushIndent();
            out.printin("_jspx_includes = new java.util.Vector(");
            out.print(""+includes.size());
            out.println(");");
            while (iter.hasNext()) {
                out.printin("_jspx_includes.add(\"");
                out.print((String)iter.next());
                out.println("\");");
            }
            out.popIndent();                     
            out.printil("}");
            out.println();
        }
        
        // Static data for EL function maps:
        generateELFunctionMap();

 	// Class variable declarations
     	
	/*
	 * Declare tag handler pools (tags of the same type and with the same
	 * attribute set share the same tag handler pool)
	 */
	if (ctxt.getOptions().isPoolingEnabled()
	        && !tagHandlerPoolNames.isEmpty()) {
	    for (int i=0; i<tagHandlerPoolNames.size(); i++) {
		out.printil("private org.apache.jasper.runtime.TagHandlerPool "
			    + tagHandlerPoolNames.elementAt(i) + ";");
	    }
            out.println();
	}
 
	// Constructor
	if (ctxt.getOptions().isPoolingEnabled()
	        && !tagHandlerPoolNames.isEmpty()) {
	    generateServletConstructor(servletClassName);
	}
 
	// Methods here

	// Method used to get compile time include file dependencies
        out.printil("public java.util.List getIncludes() {");
        out.pushIndent();
        out.printil("return _jspx_includes;");
        out.popIndent();
        out.printil("}");
        out.println();

	if (ctxt.getOptions().isPoolingEnabled()
	        && !tagHandlerPoolNames.isEmpty()) {
	    generateDestroy();
	}
 
	// Now the service method
	out.printin("public void ");
	out.print  (serviceMethodName);
	out.println("(HttpServletRequest request, HttpServletResponse response)");
	out.println("        throws java.io.IOException, ServletException {");

	out.pushIndent();
	out.println();

	// Local variable declarations
	out.printil("JspFactory _jspxFactory = null;");
	out.printil("PageContext pageContext = null;");
	if (pageInfo.isSession())
	    out.printil("HttpSession session = null;");

	if (pageInfo.isIsErrorPage())
            out.printil("Throwable exception = (Throwable) request.getAttribute(\"javax.servlet.jsp.jspException\");");

	out.printil("ServletContext application = null;");
	out.printil("ServletConfig config = null;");
	out.printil("JspWriter out = null;");
	out.printil("Object page = this;");

     	// Number of tag object that need to be popped
	// XXX TODO: use a better criteria
	maxTagNesting = pageInfo.getMaxTagNesting();
/*
        if (maxTagNesting > 0) {
	    out.printil("JspxState _jspxState = new JspxState();");
        }
*/
        out.printil("JspWriter _jspx_out = null;");
	out.println();

	declareAtBeginAtEndScriptingVariables(page);
	out.println();

	out.printil("try {");
	out.pushIndent();

	out.printil("_jspxFactory = JspFactory.getDefaultFactory();");

	out.printin("response.setContentType(");
	out.print  (quote(pageInfo.getContentType()));
	out.println(");");

	out.printil("pageContext = _jspxFactory.getPageContext(this, request, response,");
	out.printin("\t\t\t");
	out.print  (quote(pageInfo.getErrorPage()));
	out.print  (", " + pageInfo.isSession());
	out.print  (", " + pageInfo.getBuffer());
	out.print  (", " + pageInfo.isAutoFlush());
	out.println(");");

	out.printil("application = pageContext.getServletContext();");
	out.printil("config = pageContext.getServletConfig();");

	if (pageInfo.isSession())
	    out.printil("session = pageContext.getSession();");
	out.printil("out = pageContext.getOut();");
        out.printil("_jspx_out = out;");
	out.println();
    }

    /**
     * Generates EL Function map section
     */
    private void generateELFunctionMap() 
        throws JasperException
    {
        Hashtable taglibs = pageInfo.getTagLibraries();
        Iterator iter = taglibs.keySet().iterator();
        boolean fnPresent = false;
        
        // Check to see if at least one function is present.
        while( iter.hasNext() ) {
            String key = (String)iter.next();
            TagLibraryInfo tli = (TagLibraryInfo)taglibs.get( key );
            if( tli.getFunctions().length > 0 ) {
                fnPresent = true;
                break;
            }
        }
        
        out.printil("private static java.util.HashMap _jspx_fnmap = null;");
        if( fnPresent ) {
            iter = taglibs.keySet().iterator();
            out.println();
            out.printil("static {");
            out.pushIndent();
            out.printil("_jspx_fnmap = new java.util.HashMap();");
            out.printil( "try {" );
            out.pushIndent();
            while( iter.hasNext() ) {
                String key = (String)iter.next();
                TagLibraryInfo tli = (TagLibraryInfo)taglibs.get( key );
                FunctionInfo[] fnInfo = tli.getFunctions();
                String fnPrefix = tli.getPrefixString();
                out.printil( "// Functions for " + tli.getShortName() );
                for( int i = 0; i < fnInfo.length; i++ ) {
                    String fnName = fnPrefix + ":" + fnInfo[i].getName();
                    String fnSignature = fnInfo[i].getFunctionSignature();
                    out.printin("_jspx_fnmap.put(");
                    out.print(quote(fnName));
                    out.print(", ");
                    out.print(fnInfo[i].getFunctionClass() + 
                        ".class.getDeclaredMethod(");
                    
                    try {
                        // Parse function signature, assuming syntax:
                        // <return-type> S <method-name> S? '('
                        // ( <arg-type> ( ',' <arg-type> )* )? ')'
                        String ws = " \t\n\r";
                        StringTokenizer sigTokenizer = new StringTokenizer( 
                            fnSignature, ws + "(),", true);

                        // Skip <arg-type>:
                        sigTokenizer.nextToken();

                        // Skip whitespace and read <method-name>:
                        String methodName;
                        do {
                            methodName = sigTokenizer.nextToken();
                        } while( ws.indexOf( methodName ) != -1 );

                        out.print( quote( methodName ) );
                        out.print( ", new Class[] {" );
                        
                        // Skip whitespace and read '(':
                        String paren;
                        do {
                            paren = sigTokenizer.nextToken();
                        } while( ws.indexOf( paren ) != -1 );

                        if( !paren.equals( "(" ) ) {
                            throw new JasperException( err.getString(
                                "jsp.error.tld.fn.invalid.signature",
                                tli.getShortName(), fnName ) );
                        }

                        // ( <arg-type> S? ( ',' S? <arg-type> S? )* )? ')'
                        
                        // Skip whitespace and read <arg-type>:
                        String argType;
                        do {
                            argType = sigTokenizer.nextToken();
                        } while( ws.indexOf( argType ) != -1 );

                        if( !argType.equals( ")" ) ) {
                            do {
                                if( ",(".indexOf( argType ) != -1 ) {
                                    throw new JasperException( err.getString(
                                        "jsp.error.tld.fn.invalid.signature",
                                        tli.getShortName(), fnName ) );
                                }

                                out.print( argType + ".class" );

                                String comma;
                                do {
                                    comma = sigTokenizer.nextToken();
                                } while( ws.indexOf( comma ) != -1 );

                                if( comma.equals( ")" ) ) {
                                    break;
                                }
                                if( !comma.equals( "," ) ) {
                                    throw new JasperException( err.getString(
                                        "jsp.error.tld.fn.invalid.signature",
                                        tli.getShortName(), fnName ) );
                                }

                                out.print( ", " );

                                // <arg-type>
                                do {
                                    argType = sigTokenizer.nextToken();
                                } while( ws.indexOf( argType ) != -1 );
                            } while( true );
                        }
                        
                        out.println( "} ) );" );
                    }
                    catch( NoSuchElementException e ) {
                        throw new JasperException( err.getString(
                            "jsp.error.tld.fn.invalid.signature",
                            tli.getShortName(), fnName ) );
                    }
                }
            }
            out.popIndent();
            out.printil( "}" );
            out.printil( "catch( NoSuchMethodException e ) {" );
            out.pushIndent();
            out.printil( "throw new RuntimeException( \"" +
                "Invalid function mapping - no such method: \" + " +
                "e.getMessage(), e );" );
            out.popIndent();
            out.printil( "}" );
            out.popIndent();
            out.printil("}");
            out.println();
        }
    }

    /*
     * Generates the servlet constructor.
     */
    private void generateServletConstructor(String servletClassName) {
	out.printil("public " + servletClassName + "() {");
	out.pushIndent();
	for (int i=0; i<tagHandlerPoolNames.size(); i++) {
	    out.printin((String) tagHandlerPoolNames.elementAt(i));
	    out.println(" = new org.apache.jasper.runtime.TagHandlerPool();");
	}
	out.popIndent();
	out.printil("}");
	out.println();
    }

    /**
     * Generate codes defining the classes used in the servlet.
     * 1. Servlet state object, used to pass servlet info round methods.
     */
    private void generateJspState() {
/*
	out.println();
	out.printil("static final class JspxState {");
	out.pushIndent();
	out.printil("public JspWriter out;");
	out.println();
	out.printil("public JspxState() {");
	out.pushIndent();
	out.popIndent();
	out.printil("}");
	out.popIndent();
	out.printil("}");
*/
    }

    /**
     * A visitor that generates codes for the elements in the page.
     */
    class GenerateVisitor extends Node.Visitor {

	/*
	 * Hashtable containing introspection information on tag handlers:
	 *   <key>: tag prefix
	 *   <value>: hashtable containing introspection on tag handlers:
	 *              <key>: tag short name
	 *              <value>: introspection info of tag handler for 
	 *                       <prefix:shortName> tag
	 */
	private Hashtable handlerInfos;

	private Hashtable tagVarNumbers;
	private String parent;
	private String simpleTagHandlerVar;
	private boolean isSimpleTagHandler;

	private ServletWriter out;
	private MethodsBuffer methodsBuffer;
	private FragmentHelperClass fragmentHelperClass;
	private int methodNesting;

	/**
	 * Constructor.
	 */
	public GenerateVisitor(ServletWriter out, 
            MethodsBuffer methodsBuffer, 
            FragmentHelperClass fragmentHelperClass ) 
        {
	    this.out = out;
	    this.methodsBuffer = methodsBuffer;
	    this.fragmentHelperClass = fragmentHelperClass;
	    methodNesting = 0;
	    handlerInfos = new Hashtable();
	    tagVarNumbers = new Hashtable();
	}

	/**
	 * Returns an attribute value, optionally URL encoded.  If
         * the value is a runtime expression, the result is the expression
         * itself, as a string.  If the result is an EL expression, we insert
         * a call to the interpreter.  If the result is a Named Attribute
         * we insert the generated variable name.  Otherwise the result is a
         * string literal, quoted and escaped.
         *
	 * @param attr An JspAttribute object
	 * @param encode true if to be URL encoded
         * @param expectedType the expected type for an EL evaluation
         *        (ignored for attributes that aren't EL expressions)
         * @param defaultPrefix the default prefix for any EL functions
	 */
        private String attributeValue(Node.JspAttribute attr,
                                      boolean encode,
                                      Class expectedType,
                                      String defaultPrefix ) 
        {
	    String v = attr.getValue();
	    if (!attr.isNamedAttribute() && (v == null))
		return "";

            if (attr.isExpression() || attr.isELInterpreterInput()) {
		if (attr.isELInterpreterInput()) {
		    v = JspUtil.interpreterCall( attr.getValue(), 
                        expectedType, "_jspx_fnmap", defaultPrefix );
		}
		if (encode) {
		    return "java.net.URLEncoder.encode(" + v + ")";
		}
		return v;
            } else if( attr.isNamedAttribute() ) {
                return attr.getNamedAttributeNode().getTemporaryVariableName();
	    } else {
		if (encode) {
		    v = URLEncoder.encode(v);
		}
		return quote(v);
	    }
	}

	/**
	 * Prints the attribute value specified in the param action, in the
	 * form of name=value string.
	 *
	 * @param n the parent node for the param action nodes.
	 */
	private void printParams(Node n, String pageParam, boolean literal)
						throws JasperException {

	    class ParamVisitor extends Node.Visitor {
		String separator;

		ParamVisitor(String separator){
		    this.separator = separator;
		}

		public void visit(Node.ParamAction n) throws JasperException {

		    out.print(" + ");
		    out.print(separator);
		    out.print(" + \"");
		    out.print(n.getAttributeValue("name"));
		    out.print("=\" + ");
		    out.print(attributeValue(n.getValue(), true, String.class,
                        "null" ));

		    // The separator is '&' after the second use
		    separator = "\"&\"";
		}
	    }

	    String sep;
	    if (literal) {
		sep = pageParam.indexOf('?')>0? "\"&\"": "\"?\"";
	    } else {
		sep = "((" + pageParam + ").indexOf('?')>0? '&': '?')";
	    }
	    if (n.getBody() != null) {
		n.getBody().visit(new ParamVisitor(sep));
	    }
	}

        public void visit(Node.Expression n) throws JasperException {
	    n.setBeginJavaLine(out.getJavaLine());
	    out.printil("out.write(String.valueOf("
			+ new String(n.getText()) + "));");
	    n.setEndJavaLine(out.getJavaLine());
        }

	public void visit(Node.Scriptlet n) throws JasperException {
	    n.setBeginJavaLine(out.getJavaLine());
	    out.printMultiLn(new String(n.getText()));
	    n.setEndJavaLine(out.getJavaLine());
	}

        public void visit(Node.ELExpression n) throws JasperException {
            n.setBeginJavaLine(out.getJavaLine());
            if ( true /*isELEnabled*/ ) {
                out.printil(
                    "out.write("
                      + JspUtil.interpreterCall(
                      "${" + new String(n.getText()) + "}", String.class,
                      "_jspx_fnmap", "null" )
                    + ");");
            } else {
                out.printil("out.write(" +
                    quote("${" + new String(n.getText()) + "}") +
                    ");");
            }
            n.setEndJavaLine(out.getJavaLine());
        }

	public void visit(Node.IncludeAction n) throws JasperException {

	    String flush = n.getAttributeValue("flush");
	    Node.JspAttribute page = n.getPage();

	    boolean isFlush = false;	// default to false;
	    if ("true".equals(flush))
		isFlush = true;

	    n.setBeginJavaLine(out.getJavaLine());
            
            String pageParam;
            if( page.isNamedAttribute() ) {
                // If the page for jsp:include was specified via
                // jsp:attribute, first generate code to evaluate
                // that body.
                pageParam = generateNamedAttributeValue( 
                    page.getNamedAttributeNode() );
            }
            else {
                pageParam = attributeValue(page, false, String.class, "null");
            }
            
            // If any of the params have their values specified by
            // jsp:attribute, prepare those values first.
	    Node jspBody = findJspBody(n);
	    if (jspBody != null) {
		prepareParams(jspBody);
	    } else {
		prepareParams(n);
	    }
            
            out.printin("JspRuntimeLibrary.include(request, response, " +
                pageParam );
	    printParams(n, pageParam, page.isLiteral());
	    out.println(", out, " + isFlush + ");");

	    n.setEndJavaLine(out.getJavaLine());
        }
        
        /**
         * Scans through all child nodes of the given parent for
         * <param> subelements.  For each <param> element, if its value
         * is specified via a Named Attribute (<jsp:attribute>), 
         * generate the code to evaluate those bodies first.
         * <p>
         * If parent is null, simply returns.
         */
        private void prepareParams( Node parent ) throws JasperException {
            if( parent == null ) return;
            
            Node.Nodes subelements = parent.getBody();
            if( subelements != null ) {
                for( int i = 0; i < subelements.size(); i++ ) {
                    Node n = subelements.getNode( i );
                    if( n instanceof Node.ParamAction ) {
                        Node.Nodes paramSubElements = n.getBody();
                        for( int j = 0; (paramSubElements != null) &&
                            (j < paramSubElements.size()); j++ ) 
                        {
                            Node m = paramSubElements.getNode( j );
                            if( m instanceof Node.NamedAttribute ) {
                                generateNamedAttributeValue( 
                                    (Node.NamedAttribute)m );
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * Finds the <jsp:body> subelement of the given parent node.
         * If not found, null is returned.
         */
        private Node.JspBody findJspBody( Node parent ) 
            throws JasperException 
        {
            Node.JspBody result = null;
            
            Node.Nodes subelements = parent.getBody();
            for( int i = 0; (subelements != null) &&
                (i < subelements.size()); i++ )
            {
                Node n = subelements.getNode( i );
                if( n instanceof Node.JspBody ) {
                    result = (Node.JspBody)n;
                    break;
                }
            }
            
            return result;
        }

	public void visit(Node.ForwardAction n) throws JasperException {
	    Node.JspAttribute page = n.getPage();

	    n.setBeginJavaLine(out.getJavaLine());

	    out.printil("if (true) {");	// So that javac won't complain about
	    out.pushIndent();		// codes after "return"

            String pageParam;
            if( page.isNamedAttribute() ) {
                // If the page for jsp:forward was specified via
                // jsp:attribute, first generate code to evaluate
                // that body.
                pageParam = generateNamedAttributeValue(
                    page.getNamedAttributeNode() );
            }
            else {
                pageParam = attributeValue(page, false, String.class, "null");
            }
            
            // If any of the params have their values specified by
            // jsp:attribute, prepare those values first.
	    Node jspBody = findJspBody(n);
	    if (jspBody != null) {
		prepareParams(jspBody);
	    } else {
		prepareParams(n);
	    }
            
	    out.printin("pageContext.forward(");
	    out.print( pageParam );
	    printParams(n, pageParam, page.isLiteral());
	    out.println(");");
	    out.printil((methodNesting > 0)? "return true;": "return;");
	    out.popIndent();
	    out.printil("}");

	    n.setEndJavaLine(out.getJavaLine());
	    // XXX Not sure if we can eliminate dead codes after this.
	}

	public void visit(Node.GetProperty n) throws JasperException {
	    String name = n.getAttributeValue("name");
	    String property = n.getAttributeValue("property");

	    n.setBeginJavaLine(out.getJavaLine());

	    if (beanInfo.checkVariable(name)) {
		// Bean is defined using useBean, introspect at compile time
		Class bean = beanInfo.getBeanType(name);
		String beanName = bean.getName();
		java.lang.reflect.Method meth =
		    JspRuntimeLibrary.getReadMethod(bean, property);
		String methodName = meth.getName();
		out.printil("out.write(JspRuntimeLibrary.toString(" +
			    "(((" + beanName + ")pageContext.findAttribute(" +
			    "\"" + name + "\"))." + methodName + "())));");
	    } else {
		// The object could be a custom action with an associated
		// VariableInfo entry for this name.
		// Get the class name and then introspect at runtime.
		out.printil("out.write(JspRuntimeLibrary.toString" +
			    "(JspRuntimeLibrary.handleGetProperty" +
			    "(pageContext.findAttribute(\"" +
			    name + "\"), \"" + property + "\")));");
            }

	    n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.SetProperty n) throws JasperException {
	    String name = n.getAttributeValue("name");
	    String property = n.getAttributeValue("property");
	    String param = n.getAttributeValue("param");
	    Node.JspAttribute value = n.getValue();

	    n.setBeginJavaLine(out.getJavaLine());

	    if ("*".equals(property)){
		out.printil("JspRuntimeLibrary.introspect(" +
			    "pageContext.findAttribute(" +
			    "\"" + name + "\"), request);");
	    } else if (value == null) {
		if (param == null)
		    param = property;	// default to same as property
		out.printil("JspRuntimeLibrary.introspecthelper(" +
			    "pageContext.findAttribute(\"" + name + "\"), \"" +
			    property + "\", request.getParameter(\"" + param +
			    "\"), " + "request, \"" + param + "\", false);");
	    } else if (value.isExpression()) {
		out.printil("JspRuntimeLibrary.handleSetProperty(" + 
			    "pageContext.findAttribute(\""  + name + "\"), \""
			    + property + "\","); 
		out.print(attributeValue(value, false, null, "null"));
		out.println(");");
            } else if (value.isELInterpreterInput()) {
                // We've got to resolve the very call to the interpreter
                // at runtime since we don't know what type to expect
                // in the general case; we thus can't hard-wire the call
                // into the generated code.  (XXX We could, however,
                // optimize the case where the bean is exposed with
                // <jsp:useBean>, much as the code here does for
                // getProperty.)
                out.printil("JspRuntimeLibrary.handleSetPropertyExpression(" +
                    "pageContext.findAttribute(\""  + name + "\"), \""
                    + property + "\", "
                    + quote(value.getValue()) + ", "
                    + "pageContext, _jspx_fnmap);");
            } else if( value.isNamedAttribute() ) {
                // If the value for setProperty was specified via
                // jsp:attribute, first generate code to evaluate
                // that body.
                String valueVarName = generateNamedAttributeValue(
                    value.getNamedAttributeNode() );
                out.println("JspRuntimeLibrary.handleSetProperty(" +
                            "pageContext.findAttribute(\""  + name + "\"), \""
                            + property + "\", " + valueVarName + " );" );

	    } else {
		out.printil("JspRuntimeLibrary.introspecthelper(" +
			    "pageContext.findAttribute(\"" + name + "\"), \"" +
    			    property + "\",");
		out.print(attributeValue(value, false, null, "null"));
		out.println(",null, null, false);");
	    }

	    n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.UseBean n) throws JasperException {

	    String name = n.getAttributeValue ("id");
	    String scope = n.getAttributeValue ("scope");
	    String klass = n.getAttributeValue ("class");
	    String type = n.getAttributeValue ("type");
	    Node.JspAttribute beanName = n.getBeanName();

	    if (type == null)	// if unspecified, use class as type of bean 
		type = klass;

	    String scopename = "PageContext.PAGE_SCOPE"; // Default to page
	    String lock = "pageContext";

	    if ("request".equals(scope)) {
		scopename = "PageContext.REQUEST_SCOPE";
		lock = "request";
	    } else if ("session".equals(scope)) {
		scopename = "PageContext.SESSION_SCOPE";
		lock = "session";
	    } else if ("application".equals(scope)) {
		scopename = "PageContext.APPLICATION_SCOPE";
		lock = "application";
	    }

	    n.setBeginJavaLine(out.getJavaLine());

	    // Declare bean
	    out.printin(type);
	    out.print  (' ');
	    out.print  (name);
	    out.println(" = null;");

	    // Lock while getting or creating bean
	    out.printin("synchronized (");
	    out.print  (lock);
	    out.println(") {");
	    out.pushIndent();

	    // Locate bean from context
	    out.printin(name);
	    out.print  (" = (");
	    out.print  (type);
	    out.print  (") pageContext.getAttribute(");
	    out.print  (quote(name));
	    out.print  (", ");
	    out.print  (scopename);
	    out.println(");");

	    // Create bean
	    /*
	     * Check if bean is alredy there
	     */
	    out.printin("if (");
	    out.print  (name);
	    out.println(" == null){");
	    out.pushIndent();
	    if (klass == null && beanName == null) {
		/*
		 * If both class name and beanName is not specified, the bean
		 * must be found locally, otherwise it's an error
		 */
		out.printin("throw new java.lang.InstantiationException(\"bean ");
		out.print  (name);
		out.println(" not found within scope\");");
	    } else {
		/*
		 * Instantiate bean if not there
		 */
		String className;
		if (beanName != null) {
                    if( beanName.isNamedAttribute() ) {
                        // If the value for beanName was specified via
                        // jsp:attribute, first generate code to evaluate
                        // that body.
                        className = generateNamedAttributeValue(
                            beanName.getNamedAttributeNode() );
                    }
                    else {
                        className = attributeValue(beanName, false,
                            String.class, "null");
                    }
		}
		else {
		    // Implies klass is not null
		    className = quote(klass);
		}
		out.printil("try {");
		out.pushIndent();
		out.printin(name);
		out.print  (" = (");
		out.print  (type);
		out.print  (") java.beans.Beans.instantiate(");
		out.print  ("this.getClass().getClassLoader(), ");
		out.print  (className);
		out.println(");");
		out.popIndent();
		/*
		 * Note: Beans.instantiate throws ClassNotFoundException
		 * if the bean class is abstract.
		 */
		out.printil("} catch (ClassNotFoundException exc) {");
		out.pushIndent();
		out.printil("throw new InstantiationException(exc.getMessage());");
		out.popIndent();
		out.printil("} catch (Exception exc) {");
		out.pushIndent();
		out.printin("throw new ServletException(");
		out.print  ("\"Cannot create bean of class \" + ");
		out.print  (className);
		out.println(", exc);");
		out.popIndent();
		out.printil("}");	// close of try
		/*
		 * Set attribute for bean in the specified scope
		 */
		out.printin("pageContext.setAttribute(");
		out.print  (quote(name));
		out.print  (", ");
		out.print  (name);
		out.print  (", ");
		out.print  (scopename);
		out.println(");");

		// Only visit the body when bean is instantiated
		visitBody(n);
	    }
	    out.popIndent();
	    out.printil("}");

	    // End of lock block
	    out.popIndent();
	    out.printil("}");

	    n.setEndJavaLine(out.getJavaLine());
        }
	
	/**
	 * @return a string for the form 'attr = "value"'
	 */
	private String makeAttr(String attr, String value) {
	    if (value == null)
		return "";

	    return " " + attr + "=\"" + value + '\"';
	}

        public void visit(Node.PlugIn n) throws JasperException {

	    /**
	     * A visitor to handle <jsp:param> in a plugin
	     */
	    class ParamVisitor extends Node.Visitor {

		private boolean ie;

		ParamVisitor(boolean ie) {
		    this.ie = ie;
		}

                public void visit(Node.ParamAction n) throws JasperException {

		    String name = n.getAttributeValue("name");
		    if (name.equalsIgnoreCase("object"))
			name = "java_object";
		    else if (name.equalsIgnoreCase ("type"))
			name = "java_type";

		    n.setBeginJavaLine(out.getJavaLine());
                    // XXX - Fixed a bug here - value used to be output
                    // inline, which is only okay if value is not an EL
                    // expression.  Also, key/value pairs for the
                    // embed tag were not being generated correctly.
                    // Double check that this is now the correct behavior.
                    if( ie ) {
                        // We want something of the form
                        // out.println( "<PARAM name=\"blah\" 
                        //     value=\"" + ... + "\">" );
                        out.printil( "out.write( \"<PARAM name=\\\"" +
                            escape( name ) + "\\\" value=\\\"\" + " +
                            attributeValue( n.getValue(), false, String.class, 
                            "null" ) + " + \"\\\">\" );" );
                        out.printil("out.write(\"\\n\");");
                    }
                    else {
                        // We want something of the form
                        // out.print( " blah=\"" + ... + "\"" );
                        out.printil( "out.write( \" " + escape( name ) +
                            "=\\\"\" + " + 
                            attributeValue( n.getValue(), false, String.class,
                            "null" ) + " + \"\\\"\" );" );
                    }
                    
		    n.setEndJavaLine(out.getJavaLine());
		}
	    }

	    String type = n.getAttributeValue("type");
	    String code = n.getAttributeValue("code");
	    String name = n.getAttributeValue("name");
	    Node.JspAttribute height = n.getHeight();
	    Node.JspAttribute width = n.getWidth();
	    String hspace = n.getAttributeValue("hspace");
	    String vspace = n.getAttributeValue("vspace");
	    String align = n.getAttributeValue("align");
	    String iepluginurl = n.getAttributeValue("iepluginurl");
	    String nspluginurl = n.getAttributeValue("nspluginurl");
	    String codebase = n.getAttributeValue("codebase");
	    String archive = n.getAttributeValue("archive");
	    String jreversion = n.getAttributeValue("jreversion");
            
            String widthStr = null;
            if( width != null ) {
                if( width.isNamedAttribute() ) {
                    widthStr = generateNamedAttributeValue(
                        width.getNamedAttributeNode() );
                }
                else {
                    widthStr = attributeValue( width, false, String.class,
                        "null" );
                }
            }
            
            String heightStr = null;
            if( height != null ) {
                if( height.isNamedAttribute() ) {
                    heightStr = generateNamedAttributeValue(
                        height.getNamedAttributeNode() );
                }
                else {
                    heightStr = attributeValue( height, false, String.class,
                        "null" );
                }
            }

	    if (iepluginurl == null)
		iepluginurl = Constants.IE_PLUGIN_URL;
	    if (nspluginurl == null)
		nspluginurl = Constants.NS_PLUGIN_URL;


	    n.setBeginJavaLine(out.getJavaLine());
            
            // If any of the params have their values specified by
            // jsp:attribute, prepare those values first.
            // Look for a params node and prepare its param subelements:
            Node.JspBody jspBody = findJspBody( n );
            if( jspBody != null ) {
                Node.Nodes subelements = jspBody.getBody();
                if( subelements != null ) {
                    for( int i = 0; i < subelements.size(); i++ ) {
                        Node m = subelements.getNode( i );
                        if( m instanceof Node.ParamsAction ) {
                            prepareParams( m );
                            break;
                        }
                    }
                }
            }
            
            // XXX - Fixed a bug here - width and height can be set 
            // dynamically.  Double-check if this generation is correct.
            
	    // IE style plugin
	    // <OBJECT ...>
	    // First compose the runtime output string 
	    String s0 = "<OBJECT classid=" + ctxt.getOptions().getIeClassId() +
			makeAttr("name", name);
            
            String s1 = "";
            if( width != null ) {
                s1 = " + \" width=\\\"\" + " + widthStr + " + \"\\\"\"";
            }
            
            String s2 = "";
            if( height != null ) {
                s2 = " + \" height=\\\"\" + " + heightStr + " + \"\\\"\"";
            }
                        
            String s3 = makeAttr("hspace", hspace) +
			makeAttr("vspace", vspace) +
			makeAttr("align", align) +
			makeAttr("codebase", iepluginurl) +
			'>';
            
	    // Then print the output string to the java file
	    out.printil("out.write(" + 
                quote(s0) + s1 + s2 + " + " + quote(s3) + ");");
	    out.printil("out.write(\"\\n\");");

	    // <PARAM > for java_code
	    s0 = "<PARAM name=\"java_code\"" + makeAttr("value", code) + '>';
	    out.printil("out.write(" + quote(s0) + ");");
	    out.printil("out.write(\"\\n\");");

	    // <PARAM > for java_codebase
	    if (codebase != null) {
		s0 = "<PARAM name=\"java_codebase\"" + 
		     makeAttr("value", codebase) +
		     '>';
		out.printil("out.write(" + quote(s0) + ");");
		out.printil("out.write(\"\\n\");");
	    }

	    // <PARAM > for java_archive
	    if (archive != null) {
		s0 = "<PARAM name=\"java_archive\"" +
		     makeAttr("value", archive) +
		     '>';
		out.printil("out.write(" + quote(s0) + ");");
		out.printil("out.write(\"\\n\");");
	    }

	    // <PARAM > for type
	    s0 = "<PARAM name=\"type\"" +
		 makeAttr("value", "application/x-java-" + type + ";" +
			  ((jreversion==null)? "": "version=" + jreversion)) +
		 '>';
	    out.printil("out.write(" + quote(s0) + ");");
	    out.printil("out.write(\"\\n\");");

	    /*
	     * generate a <PARAM> for each <jsp:param> in the plugin body
	     */
	    if (n.getBody() != null)
		n.getBody().visit(new ParamVisitor(true));

	    /*
	     * Netscape style plugin part
	     */
	    out.printil("out.write(" + quote("<COMMENT>") + ");");
	    out.printil("out.write(\"\\n\");");
	    s0 = "<EMBED" +
		 makeAttr("type", "application/x-java-" + type + ";" +
			  ((jreversion==null)? "": "version=" + jreversion)) +
		 makeAttr("name", name);
                         
            // s1 and s2 are the same as before.
            
	    s3 = makeAttr("hspace", hspace) +
		 makeAttr("vspace", vspace) +
		 makeAttr("align", align) +
		 makeAttr("pluginspage", nspluginurl) +
                 makeAttr("java_code", code) +
		 makeAttr("java_codebase", codebase) +
		 makeAttr("java_archive", archive);
	    out.printil("out.write(" + 
                quote(s0) + s1 + s2 + " + " + quote(s3) + ");");
	    out.printil("out.write(\"\\n\");");
		 
	    /*
	     * Generate a 'attr = "value"' for each <jsp:param> in plugin body
	     */
	    if (n.getBody() != null)
		n.getBody().visit(new ParamVisitor(false)); 

	    out.printil("out.write(" + quote(">") + ");");
	    out.printil("out.write(\"\\n\");");

	    out.printil("out.write(" + quote("<NOEMBED>") + ");");
	    out.printil("out.write(\"\\n\");");
	    out.printil("out.write(" + quote("</COMMENT>") + ");");
	    out.printil("out.write(\"\\n\");");

	    /*
	     * Fallback
	     */
	    if (n.getBody() != null) {
		n.getBody().visit(new Node.Visitor() {
		    public void visit(Node.FallBackAction n) {
			n.setBeginJavaLine(out.getJavaLine());
			out.printil("out.write(" +
				    quote(new String(n.getText())) + ");");
			out.printil("out.write(\"\\n\");");
			n.setEndJavaLine(out.getJavaLine());
		    }
		});
	    }

	    out.printil("out.write(" + quote("</NOEMBED></EMBED>") + ");");
	    out.printil("out.write(\"\\n\");");
	    out.printil("out.write(" + quote("</OBJECT>") + ");");
	    out.printil("out.write(\"\\n\");");

	    n.setEndJavaLine(out.getJavaLine());
	}

        public void visit(Node.NamedAttribute n) throws JasperException {
            // Don't visit body of this tag - we already did earlier.
        }

        public void visit(Node.CustomTag n) throws JasperException {

	    Hashtable handlerInfosByShortName = (Hashtable)
		handlerInfos.get(n.getPrefix());
	    if (handlerInfosByShortName == null) {
		handlerInfosByShortName = new Hashtable();
		handlerInfos.put(n.getPrefix(), handlerInfosByShortName);
	    }
	    TagHandlerInfo handlerInfo = (TagHandlerInfo)
		handlerInfosByShortName.get(n.getShortName());
	    if (handlerInfo == null) {
		handlerInfo = new TagHandlerInfo(n,
						 n.getTagHandlerClass(),
						 err);
		handlerInfosByShortName.put(n.getShortName(), handlerInfo);
	    }

	    // Create variable names
	    String baseVar = createTagVarName(n.getName(), n.getPrefix(),
					      n.getShortName());
	    String tagEvalVar = "_jspx_eval_" + baseVar;
	    String tagHandlerVar = "_jspx_th_" + baseVar;

	    Class tagHandlerClass = n.getTagHandlerClass();

	    // If the tag contains no scripting element, generate its codes
	    // to a method.
	    ServletWriter outSave = null;
	    MethodsBuffer methodsBufferSave = null;
            Node.ChildInfo ci = n.getChildInfo();
	    if (n.implementsSimpleTag()
		    || (ci.isScriptless() && !ci.hasScriptingVars())) {
		// The tag handler and its body code can reside in a separate
		// method if it is scriptless and does not have any scripting
		// variable defined.

		String tagMethod = "_jspx_meth_" + baseVar;

		// Generate a call to this method
		out.printin("if (");
		out.print(tagMethod);
		out.print("(");
		if (parent != null) {
		    out.print(parent);
		    out.print(", ");
		}
//		out.println("pageContext, _jspxState)");
		out.println("pageContext))");
		out.pushIndent();
		out.printil((methodNesting > 0)? "return true;": "return;");
		out.popIndent();

		// Set up new buffer for the method
		outSave = out;
		out = methodsBuffer.getOut();
		methodsBufferSave = methodsBuffer;
		methodsBuffer = new MethodsBuffer();

		methodNesting++;
		// Generate code for method declaration
		out.println();
		out.pushIndent();
		out.printin("private boolean ");
		out.print(tagMethod);
		out.print("(");
		if (parent != null) {
		    out.print("javax.servlet.jsp.tagext.Tag ");
		    out.print(parent);
		    out.print(", ");
		}
//		out.println("PageContext pageContext, JspxState _jspxState)");
		out.println("PageContext pageContext)");
		out.printil("        throws Throwable {");
		out.pushIndent();

		// Initilaize local variables used in this method.
		out.printil("JspWriter out = pageContext.getOut();");
                generateLocalVariables( out, n );
            }

	    if (n.implementsSimpleTag()) {
		generateCustomDoTag(n, handlerInfo, tagHandlerVar);
	    } else {
		/*
		 * Classic tag handler: Generate code for start element, body,
		 * and end element
		 */
		generateCustomStart(n, handlerInfo, tagHandlerVar, tagEvalVar);

		// visit body
		String tmpParent = parent;
		parent = tagHandlerVar;
		boolean tmpIsSimpleTagHandler = isSimpleTagHandler;
		isSimpleTagHandler = false;
		visitBody(n);
		parent = tmpParent;
		isSimpleTagHandler = tmpIsSimpleTagHandler;

		generateCustomEnd(n, tagHandlerVar, tagEvalVar);
	    }

	    if (n.implementsSimpleTag()
		    || (ci.isScriptless() && !ci.hasScriptingVars())) {
		// Generate end of method
		if (methodNesting > 0) {
		    out.printil("return false;");
		}
		out.popIndent();
		out.printil("}");
		out.popIndent();

		methodNesting--;

		// Append any methods that got generated in the body to the
		// current buffer
		out.print(methodsBuffer.toString());

		// restore previous buffer
		methodsBuffer = methodsBufferSave;
		out = outSave;
	    }
        }

	private static final String SINGLE_QUOTE = "'";
	private static final String DOUBLE_QUOTE = "\\\"";

	public void visit(Node.UninterpretedTag n) throws JasperException {

	    /*
	     * Write begin tag
	     */
	    out.printin("out.write(\"<");
	    out.print(n.getName());
	    Attributes attrs = n.getAttributes();
	    if (attrs != null) {
		int attrsLength = attrs.getLength();
		for (int i=0; i<attrsLength; i++) {
		    String quote = DOUBLE_QUOTE;
		    String value = attrs.getValue(i);
		    if (value.indexOf('"') != -1) {
			quote = SINGLE_QUOTE;
		    }
		    out.print(" ");
		    out.print(attrs.getQName(i));
		    out.print("=");
		    out.print(quote);
		    out.print(value);
		    out.print(quote);
		}
	    }

	    if (n.getBody() != null) {
		out.println(">\");");
		
		// Visit tag body
		visitBody(n);

		/*
		 * Write end tag
		 */
		out.printin("out.write(\"</");
		out.print(n.getName());
		out.println(">\");");
	    } else {
		out.println("/>\");");
	    }
	}

	private static final int CHUNKSIZE = 1024;

	public void visit(Node.TemplateText n) throws JasperException {

	    char[] chars = n.getText();
	    int size = chars.length;

	    n.setBeginJavaLine(out.getJavaLine());

	    out.printin();
	    StringBuffer sb = new StringBuffer("out.write(\"");
	    int initLength = sb.length();
	    int count = CHUNKSIZE;
	    for (int i = 0 ; i < size ; i++) {
		char ch = chars[i];
		--count;
		switch(ch) {
		case '"':
		    sb.append('\\').append('\"');
		    break;
		case '\\':
		    sb.append('\\').append('\\');
		    break;
		case '\r':
		    sb.append('\\').append('r');
		    break;
		case '\n':
		    sb.append('\\').append('n');

		    if (breakAtLF || count < 0) {
			// Generate an out.write() when see a '\n' in template
			sb.append("\");");
			out.println(sb.toString());
			out.printin();
			sb.setLength(initLength);
			count = CHUNKSIZE;
		    }
		    break;
		case '\t':	// Not sure we need this
		    sb.append('\\').append('t');
		    break;
		default:
		    sb.append(ch);
	        }
	    }

	    if (sb.length() > initLength) {
		sb.append("\");");
  		out.println(sb.toString());
	    }

	    n.setEndJavaLine(out.getJavaLine());
	}

	public void visit(Node.JspBody n) throws JasperException {
	    if (isSimpleTagHandler) {
		out.printin(simpleTagHandlerVar);
		out.print(".setJspBody(");

		Node.JspAttribute value = n.getValue();
		if (value != null) {
		    out.print(attributeValue(value, false, JspFragment.class,
                        "null" ));
		} else {
		    generateJspFragment(n, simpleTagHandlerVar);
		}
		out.println(");");
	    } else {
		visitBody(n);
	    }
	}

        public void visit(Node.InvokeAction n) throws JasperException {

	    /**
	     * A visitor to handle <jsp:param> in a <jsp:invoke>
	     */
	    class ParamVisitor extends Node.Visitor {

                public void visit(Node.ParamAction n) throws JasperException {
		    out.printin("params.put(");
		    out.print(n.getAttributeValue("name"));
		    out.print(", ");
		    out.print(attributeValue(n.getValue(), false,
					     String.class, "null"));
		    out.println(");");
		}
	    }

	    // Assemble parameter map
	    out.printil("params = new java.util.HashMap();");
	    if (n.getBody() != null) {
		prepareParams(n);
		n.getBody().visit(new ParamVisitor());
	    }
	    
	    // Invoke fragment with parameter map
	    String getterMethodName
		= getAccessorMethodName(n.getAttributeValue("fragment"),
					true);
	    String varReader = n.getAttributeValue("varReader");
	    if (varReader != null) {
		out.printil("sout = new java.io.StringWriter();");
		out.printin(getterMethodName);
		out.println("().invoke(sout, params);");
	    } else {
		out.printin(getterMethodName);
		out.println("().invoke(null, params);");
	    }
	    if (varReader != null) {
		out.printin("jspContext.setAttribute(\"");
		out.print(varReader);
		out.print("\", new java.io.StringReader(sout.toString()));");
		// XXX evaluate scope
	    }
	}

        public void visit(Node.DoBodyAction n) throws JasperException {

	    /**
	     * A visitor to handle <jsp:param> in a <jsp:doBody>
	     */
	    class ParamVisitor extends Node.Visitor {

                public void visit(Node.ParamAction n) throws JasperException {
		    out.printin("params.put(");
		    out.print(n.getAttributeValue("name"));
		    out.print(", ");
		    out.print(attributeValue(n.getValue(), false,
					     String.class, "null"));
		    out.println(");");
		}
	    }

	    // Assemble parameter map
	    out.printil("params = new java.util.HashMap();");
	    if (n.getBody() != null) {
		prepareParams(n);
		n.getBody().visit(new ParamVisitor());
	    }

	    // XXX Add scripting variables to parameter map

	    // Invoke body with parameter map
	    String varReader = n.getAttributeValue("varReader");
	    if (varReader != null) {
		out.printil("sout = new java.io.StringWriter();");
		out.printil("getJspBody().invoke(sout, params);");
	    } else {
		out.printil("getJspBody().invoke(null, params);");
	    }
	}

	private void generateCustomStart(Node.CustomTag n,
					 TagHandlerInfo handlerInfo,
					 String tagHandlerVar,
					 String tagEvalVar)
	                    throws JasperException {

	    Class tagHandlerClass = handlerInfo.getTagHandlerClass();

	    n.setBeginJavaLine(out.getJavaLine());
	    out.printin("/* ----  ");
	    out.print(n.getName());
	    out.println(" ---- */");
	    out.printil("{");
	    out.pushIndent();

	    // Declare scripting variables with NESTED scope
	    declareNestedScriptingVariables(n);

	    /*
	     * Save current values of scripting variables, so that the 
	     * scripting variables may be synchronized without affecting their
	     * original values
	     */
	    saveScriptingVariables(n);

	    out.printin(tagHandlerClass.getName());
	    out.print(" ");
	    out.print(tagHandlerVar);
	    out.print(" = ");
	    if (ctxt.getOptions().isPoolingEnabled()) {
		out.print("(");
		out.print(tagHandlerClass.getName());
		out.print(") ");
		out.print(n.getTagHandlerPoolName());
		out.print(".get(");
		out.print(tagHandlerClass.getName());
		out.println(".class);");
	    } else {
		out.print("new ");
		out.print(tagHandlerClass.getName());
		out.println("();");
	    }

	    generateSetters(n, tagHandlerVar, handlerInfo, false);
	    
            if (n.implementsTryCatchFinally()) {
                out.printil("try {");
                out.pushIndent();
            }
	    out.printin("int ");
	    out.print(tagEvalVar);
	    out.print(" = ");
	    out.print(tagHandlerVar);
	    out.println(".doStartTag();");

	    // Synchronize AT_BEGIN and NESTED scripting variables
	    if (!n.implementsBodyTag()) {
		syncScriptingVariables(n, VariableInfo.AT_BEGIN);
		syncScriptingVariables(n, VariableInfo.NESTED);
	    }

	    if (n.getBody() != null) {
		out.printin("if (");
		out.print(tagEvalVar);
		out.println(" != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {");
		out.pushIndent();
		
		if (n.implementsBodyTag()) {
		    out.printin("if (");
		    out.print(tagEvalVar);
		    out.println(" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {");
		    // Assume EVAL_BODY_BUFFERED
		    out.pushIndent();
                    out.printil("out = pageContext.pushBody();");
		    out.printin(tagHandlerVar);
		    out.println(".setBodyContent((javax.servlet.jsp.tagext.BodyContent) out);");
		    out.printin(tagHandlerVar);
		    out.println(".doInitBody();");

		    // Synchronize AT_BEGIN and NESTED scripting variables
		    syncScriptingVariables(n, VariableInfo.AT_BEGIN);
		    syncScriptingVariables(n, VariableInfo.NESTED);
		    
		    out.popIndent();
		    out.printil("}");
		}
		
		if (n.implementsIterationTag()) {
		    out.printil("do {");
		    out.pushIndent();
		}
	    }
	};
	
	private void generateCustomEnd(Node.CustomTag n,
				       String tagHandlerVar,
				       String tagEvalVar) {

	    VariableInfo[] varInfos = n.getVariableInfos();
	    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();

	    if ((n.getBody() != null) && n.implementsIterationTag()) {
		out.printin("int evalDoAfterBody = ");
		out.print(tagHandlerVar);
		out.println(".doAfterBody();");

		// Synchronize AT_BEGIN and NESTED scripting variables
		if (n.implementsBodyTag()) {
		    syncScriptingVariables(n, VariableInfo.AT_BEGIN);
		    syncScriptingVariables(n, VariableInfo.NESTED);
		}

		out.printil("if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)");
		out.pushIndent();
		out.printil("break;");
		out.popIndent();

		out.popIndent();
		out.printil("} while (true);");
	    }

	    if (n.getBody() != null) {
		if (n.implementsBodyTag()) {
		    out.printin("if (");
		    out.print(tagEvalVar);
		    out.println(" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)");
		    out.pushIndent();
                    out.printil("out = pageContext.popBody();");
		    out.popIndent();
		}

		out.popIndent(); // EVAL_BODY
		out.printil("}");
	    }

	    out.printin("if (");
	    out.print(tagHandlerVar);
	    out.println(".doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)");
	    out.pushIndent();
	    out.printil((methodNesting > 0)? "return true;": "return;");
	    out.popIndent();

	    // Synchronize AT_BEGIN and AT_END scripting variables
	    syncScriptingVariables(n, VariableInfo.AT_BEGIN);
	    syncScriptingVariables(n, VariableInfo.AT_END);

	    // TryCatchFinally
	    if (n.implementsTryCatchFinally()) {
                out.popIndent(); // try
		out.printil("} catch (Throwable _jspx_exception) {");
		out.pushIndent();
		out.printin(tagHandlerVar);
		out.println(".doCatch(_jspx_exception);");
		out.popIndent();
                out.printil("} finally {");
                out.pushIndent();
		out.printin(tagHandlerVar);
		out.println(".doFinally();");
	    }

	    if (ctxt.getOptions().isPoolingEnabled()) {
                out.printin(n.getTagHandlerPoolName());
                out.print(".reuse(");
		out.print(tagHandlerVar);
		out.println(");");
	    }

	    if (n.implementsTryCatchFinally()) {
                out.popIndent();
                out.println("}");
	    }

	    restoreScriptingVariables(n);
	    out.popIndent();
	    out.printil("}");

	    n.setEndJavaLine(out.getJavaLine());
	}

	private void generateCustomDoTag(Node.CustomTag n,
					 TagHandlerInfo handlerInfo,
					 String tagHandlerVar)
	                    throws JasperException {

	    Class tagHandlerClass = handlerInfo.getTagHandlerClass();

	    n.setBeginJavaLine(out.getJavaLine());
	    out.printin("/* ----  ");
	    out.print(n.getName());
	    out.println(" ---- */");
	    out.printil("{");
	    out.pushIndent();

	    /*
	     * Save current values of scripting variables, so that the 
	     * scripting variables may be synchronized without affecting their
	     * original values
	     */
	    saveScriptingVariables(n);

	    out.printin(tagHandlerClass.getName());
	    out.print(" ");
	    out.print(tagHandlerVar);
	    out.print(" = ");
	    out.print("new ");
	    out.print(tagHandlerClass.getName());
	    out.println("();");

	    generateSetters(n, tagHandlerVar, handlerInfo, true);

            if (n.implementsTryCatchFinally()) {
                out.printil("try {");
                out.pushIndent();
            }
	    
	    // Set the body
	    if (findJspBody(n) == null) {
		/*
		 * Encapsulate body of custom tag invocation in JspFragment
		 * and pass it to tag handler's setJspBody()
		 */
		out.printin(tagHandlerVar);
		out.print(".setJspBody(");
		generateJspFragment(n, tagHandlerVar);
		out.println(");");
	    } else {
		/*
		 * Body of tag is the body of the <jsp:body> element.
		 * The visit method for that element is going to encapsulate
		 * that element's body in a JspFragment and pass it to
		 * the tag handler's setJspBody()
		 */
		String tmpTagHandlerVar = simpleTagHandlerVar;
		simpleTagHandlerVar = tagHandlerVar;
		boolean tmpIsSimpleTagHandler = isSimpleTagHandler;
		isSimpleTagHandler = true;
		visitBody(n);
		simpleTagHandlerVar = tmpTagHandlerVar;
		isSimpleTagHandler = tmpIsSimpleTagHandler;
	    }

	    out.printin("if (");
	    out.print(tagHandlerVar);
	    out.println(".doTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)");
	    out.pushIndent();
	    out.printil((methodNesting > 0)? "return true;": "return;");
	    out.popIndent();

	    // Synchronize AT_BEGIN and AT_END scripting variables
	    syncScriptingVariables(n, VariableInfo.AT_BEGIN);
	    syncScriptingVariables(n, VariableInfo.AT_END);

	    // TryCatchFinally
	    if (n.implementsTryCatchFinally()) {
                out.popIndent(); // try
		out.printil("} catch (Throwable _jspx_exception) {");
		out.pushIndent();
		out.printin(tagHandlerVar);
		out.println(".doCatch(_jspx_exception);");
		out.popIndent();
                out.printil("} finally {");
                out.pushIndent();
		out.printin(tagHandlerVar);
		out.println(".doFinally();");
                out.popIndent();
                out.println("}");
	    }

	    restoreScriptingVariables(n);
	    out.popIndent();
	    out.printil("}");

	    n.setEndJavaLine(out.getJavaLine());
	}

	/*
	 * Declares any NESTED scripting variables of the given custom tag.
	 */
	private void declareNestedScriptingVariables(Node.CustomTag n) {

	    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
	    VariableInfo[] nestedVarInfos = n.getNestedVariableInfos();
	    if ((nestedVarInfos == null) && (tagVarInfos == null)) {
		return;
	    }

	    if (nestedVarInfos != null) {
		for (int i=0; i<nestedVarInfos.length; i++) {
		    String name = nestedVarInfos[i].getVarName();
		    out.printin(nestedVarInfos[i].getClassName());
		    out.print(" ");
		    out.print(name);
		    out.println(" = null;");
		}
	    } else {
		for (int i=0; i<tagVarInfos.length; i++) {
		    if ((tagVarInfos[i].getScope() == VariableInfo.NESTED)
			    && tagVarInfos[i].getDeclare()) {
			String name = tagVarInfos[i].getNameGiven();
			if (name == null) {
			    name = n.getTagData().getAttributeString(
                                    tagVarInfos[i].getNameFromAttribute());
			}
			out.printin(tagVarInfos[i].getClassName());
			out.print(" ");
			out.print(name);
			out.println(" = null;");
		    }
		}
	    }
	}

	/*
	 * This method is called as part of the custom tag's start element.
	 *
	 * If the given custom tag has a custom nesting level greater than 0,
	 * save the current values of its scripting variables to 
	 * temporary variables, so those values may be restored in the tag's
	 * end element. This way, the scripting variables may be synchronized
	 * by the given tag without affecting their original values.
	 */
	private void saveScriptingVariables(Node.CustomTag n) {
	    if (n.getCustomNestingLevel() == 0) {
		return;
	    }

	    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
	    VariableInfo[] varInfos = n.getVariableInfos();
	    if ((varInfos == null) && (tagVarInfos == null)) {
		return;
	    }

	    if (varInfos != null) {
		for (int i=0; i<varInfos.length; i++) {
		    String varName = varInfos[i].getVarName();
		    String tmpVarName = "_jspx_" + varName + "_"
			+ n.getCustomNestingLevel();
		    out.printin(varInfos[i].getClassName());
		    out.print(" ");
		    out.print(tmpVarName);
		    out.print(" = ");
		    out.print(varName);
		    out.println(";");
		}
	    } else {
		for (int i=0; i<tagVarInfos.length; i++) {
		    String varName = tagVarInfos[i].getNameGiven();
		    if (varName == null) {
			varName = n.getTagData().getAttributeString(
			                tagVarInfos[i].getNameFromAttribute());
		    }
		    String tmpVarName = "_jspx_" + varName + "_"
			+ n.getCustomNestingLevel();
		    out.printin(tagVarInfos[i].getClassName());
		    out.print(" ");
		    out.print(tmpVarName);
		    out.print(" = ");
		    out.print(varName);
		    out.println(";");
		}
	    }
	}

	/*
	 * This method is called as part of the custom tag's end element.
	 *
	 * If the given custom tag has a custom nesting level greater than 0,
	 * restore its scripting variables to their original values that were
	 * saved in the tag's start element.
	 */
	private void restoreScriptingVariables(Node.CustomTag n) {
	    if (n.getCustomNestingLevel() == 0) {
		return;
	    }

	    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
	    VariableInfo[] varInfos = n.getVariableInfos();
	    if ((varInfos == null) && (tagVarInfos == null)) {
		return;
	    }

	    if (varInfos != null) {
		for (int i=0; i<varInfos.length; i++) {
		    String varName = varInfos[i].getVarName();
		    String tmpVarName = "_jspx_" + varName + "_"
			+ n.getCustomNestingLevel();
		    out.printin(varName);
		    out.print(" = ");
		    out.print(tmpVarName);
		    out.println(";");
		}
	    } else {
		for (int i=0; i<tagVarInfos.length; i++) {
		    String varName = tagVarInfos[i].getNameGiven();
		    if (varName == null) {
			varName = n.getTagData().getAttributeString(
                                tagVarInfos[i].getNameFromAttribute());
		    }
		    String tmpVarName = "_jspx_" + varName + "_"
			+ n.getCustomNestingLevel();
		    out.printin(varName);
		    out.print(" = ");
		    out.print(tmpVarName);
		    out.println(";");
		}
	    }
	}

	/*
	 * Synchronizes the scripting variables of the given custom tag for
	 * the given scope.
	 */
	private void syncScriptingVariables(Node.CustomTag n, int scope) {
	    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
	    VariableInfo[] varInfos = n.getVariableInfos();

	    if ((varInfos == null) && (tagVarInfos == null)) {
		return;
	    }

	    if (varInfos != null) {
		for (int i=0; i<varInfos.length; i++) {
		    if (varInfos[i].getScope() == scope) {
			out.printin(varInfos[i].getVarName());
			out.print(" = (");
			out.print(varInfos[i].getClassName());
			out.print(") pageContext.findAttribute(");
			out.print(quote(varInfos[i].getVarName()));
			out.println(");");
		    }
		}
	    } else {
		for (int i=0; i<tagVarInfos.length; i++) {
		    if (tagVarInfos[i].getScope() == scope) {
			String name = tagVarInfos[i].getNameGiven();
			if (name == null) {
			    name = n.getTagData().getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
			}
			out.printin(name);
			out.print(" = (");
			out.print(tagVarInfos[i].getClassName());
			out.print(") pageContext.findAttribute(");
			out.print(quote(name));
			out.println(");");
		    }
		}
	    }
	}

	/*
	 * Creates a tag variable name by concatenating the given prefix and
	 * shortName and replacing '-' with "$1", '.' with "$2", and ':' with
	 * "$3".
	 */
	private String createTagVarName(String fullName, String prefix,
					String shortName) {
	    if (prefix.indexOf('-') >= 0)
		prefix = JspUtil.replace(prefix, '-', "$1");
	    if (prefix.indexOf('.') >= 0)
		prefix = JspUtil.replace(prefix, '.', "$2");

	    if (shortName.indexOf('-') >= 0)
		shortName = JspUtil.replace(shortName, '-', "$1");
	    if (shortName.indexOf('.') >= 0)
		shortName = JspUtil.replace(shortName, '.', "$2");
	    if (shortName.indexOf(':') >= 0)
		shortName = JspUtil.replace(shortName, ':', "$3");

	    synchronized (tagVarNumbers) {
		String varName = prefix + "_" + shortName + "_";
		if (tagVarNumbers.get(fullName) != null) {
		    Integer i = (Integer) tagVarNumbers.get(fullName);
		    varName = varName + i.intValue();
		    tagVarNumbers.put(fullName, new Integer(i.intValue() + 1));
		    return varName;
		} else {
		    tagVarNumbers.put(fullName, new Integer(1));
		    return varName + "0";
		}
	    }
	}

	private void generateSetters(Node.CustomTag n,
				     String tagHandlerVar,
				     TagHandlerInfo handlerInfo,
				     boolean simpleTag)
	            throws JasperException {

	    out.printin(tagHandlerVar);
	    if (simpleTag) {
		out.println(".setJspContext(pageContext);");
	    } else {
		out.println(".setPageContext(pageContext);");
	    }
	    out.printin(tagHandlerVar);
	    out.print(".setParent(");
	    out.print(parent);
	    out.println(");");

	    Node.JspAttribute[] attrs = n.getJspAttributes();
	    for (int i=0; i<attrs.length; i++) {
		String attrValue = attrs[i].getValue();
		if (attrValue == null) {
                    if( attrs[i].isNamedAttribute() ) {
                        if( n.checkIfAttributeIsJspFragment( 
                            attrs[i].getName() ) ) 
                        {
                            // XXX - no need to generate temporary variable 
                            // here
                            attrValue = generateNamedAttributeJspFragment( 
                                attrs[i].getNamedAttributeNode(),
                                tagHandlerVar );
                        }
                        else {
                            attrValue = generateNamedAttributeValue( 
                                attrs[i].getNamedAttributeNode() );
                        }
                    } 
                    else {
                        continue;
                    }
		}
		String attrName = attrs[i].getName();

		Method m = null;
		Class[] c = null;
		if (attrs[i].isDynamic()) {
		    c = OBJECT_CLASS;
		} else {
		    m = handlerInfo.getSetterMethod(attrName);
		    if (m == null) {
			err.jspError(n, "jsp.error.unable.to_find_method",
				     attrName);
		    }
		    c = m.getParameterTypes();
		    // XXX assert(c.length > 0)
		}

		if (attrs[i].isExpression() || attrs[i].isNamedAttribute()) {
		    // Do nothing
		} else if (attrs[i].isELInterpreterInput()) {
                    // run attrValue through the expression interpreter
                    attrValue = JspUtil.interpreterCall( attrValue,
                        c[0], "_jspx_fnmap", n.getPrefix() );
                } else {
		    attrValue = convertString(
                                c[0], attrValue, attrName,
				handlerInfo.getPropertyEditorClass(attrName));
		}
		
		if (attrs[i].isDynamic()) {
		    out.printil("try {");
		    out.pushIndent();
		    out.printin(tagHandlerVar);
		    out.print(".");
		    out.print("setDynamicAttribute(\"");
		    out.print(attrs[i].getURI());
		    out.print("\", \"");
		    out.print(attrs[i].getLocalName());
		    out.print("\", ");
		    out.print(attrValue);
		    out.println(");");
		    out.popIndent();
		    out.printin("}"); // catch
		    out.println(" catch (javax.servlet.jsp.tagext.AttributeNotSupportedException e) {");
		    out.pushIndent();
		    out.printil("throw new javax.servlet.jsp.JspException(e);");
		    out.popIndent();
		    out.printil("}"); // catch
		} else {
		    out.printin(tagHandlerVar);
		    out.print(".");
		    out.print(m.getName());
		    out.print("(");
		    out.print(attrValue);
		    out.println(");");
		}
	    }
	}

	private String convertString(Class c, String s, String attrName,
				     Class propEditorClass)
	            throws JasperException {
	    
	    if (propEditorClass != null) {
		return "(" + c.getName()
		    + ")JspRuntimeLibrary.getValueFromBeanInfoPropertyEditor("
		    + c.getName() + ".class, \"" + attrName + "\", "
		    + quote(s) + ", "
		    + propEditorClass.getName() + ".class)";
	    } else if (c == String.class) {
		return quote(s);
	    } else if (c == boolean.class) {
		return Boolean.valueOf(s).toString();
	    } else if (c == Boolean.class) {
		return "new Boolean(" + Boolean.valueOf(s).toString() + ")";
	    } else if (c == byte.class) {
		return "((byte)" + Byte.valueOf(s).toString() + ")";
	    } else if (c == Byte.class) {
		return "new Byte((byte)" + Byte.valueOf(s).toString() + ")";
	    } else if (c == char.class) {
		// non-normative (normative method would fail to compile)
		if (s.length() > 0) {
		    char ch = s.charAt(0);
		    // this trick avoids escaping issues
		    return "((char) " + (int) ch + ")";
		} else {
		    throw new NumberFormatException(
                        err.getString("jsp.error.bad_string_char"));
		}
	    } else if (c == Character.class) {
		// non-normative (normative method would fail to compile)
		if (s.length() > 0) {
		    char ch = s.charAt(0);
		    // this trick avoids escaping issues
		    return "new Character((char) " + (int) ch + ")";
		} else {
		    throw new NumberFormatException(
                        err.getString("jsp.error.bad_string_Character"));
		}
	    } else if (c == double.class) {
		return Double.valueOf(s).toString();
	    } else if (c == Double.class) {
		return "new Double(" + Double.valueOf(s).toString() + ")";
	    } else if (c == float.class) {
		return Float.valueOf(s).toString() + "f";
	    } else if (c == Float.class) {
		return "new Float(" + Float.valueOf(s).toString() + "f)";
	    } else if (c == int.class) {
		return Integer.valueOf(s).toString();
	    } else if (c == Integer.class) {
		return "new Integer(" + Integer.valueOf(s).toString() + ")";
	    } else if (c == short.class) {
		return "((short) " + Short.valueOf(s).toString() + ")";
	    } else if (c == Short.class) {
		return "new Short(" + Short.valueOf(s).toString() + ")";
	    } else if (c == long.class) {
		return Long.valueOf(s).toString() + "l";
	    } else if (c == Long.class) {
		return "new Long(" + Long.valueOf(s).toString() + "l)";
	    } else if (c == Object.class) {
		return "new String(" + quote(s) + ")";
	    } else {
		return "(" + c.getName()
		    + ")JspRuntimeLibrary.getValueFromPropertyEditorManager("
		    + c.getName() + ".class, \"" + attrName + "\", "
		    + quote(s) + ")";
	    }
	}   

	/**
	 * Generates anonymous JspFragment inner class which is passed as an
	 * argument to SimpleTag.setJspBody().
	 */
	private void generateJspFragment(Node n, String tagHandlerVar) 
            throws JasperException
        {
            // XXX - A possible optimization here would be to check to see
            // if the old child of the parent node is TemplateText.  If so,
            // we know there won't be any parameters, etc, so we can 
            // generate a low-overhead JspFragment that just echoes its
            // body.  The implementation of this fragment can come from
            // the org.apache.jasper.runtime package as a support class.
            FragmentHelperClass.Fragment fragment = 
                fragmentHelperClass.openFragment( n );
            ServletWriter outSave = out;
	    out = fragment.getMethodsBuffer().getOut();
            visitBody( n );
            out = outSave;
	    fragmentHelperClass.closeFragment( fragment );
            // XXX - Need to change pageContext to jspContext if
            // we're not in a place where pageContext is defined (e.g.
            // in a fragment or in a tag file.
	    out.print( "new " + fragmentHelperClass.getClassName() + 
		       "( " + fragment.getId() + ", pageContext, " + 
		       tagHandlerVar + ")" );
	}
        
        /**
         * Generate the code required to obtain the runtime value of the
         * given named attribute.
         *
         * @return The name of the temporary variable the result is stored in.
         */
        public String generateNamedAttributeValue( Node.NamedAttribute n )
            throws JasperException
        {
            String varName = n.getTemporaryVariableName();

            // If the only body element for this named attribute node is
            // template text, we need not generate an extra call to
            // pushBody and popBody.  Maybe we can further optimize
            // here by getting rid of the temporary variable, but in
            // reality it looks like javac does this for us.
            boolean templateTextOptimization = false;
            Node.Nodes body = n.getBody();
            if( body.size() == 1 ) {
                Node bodyElement = body.getNode( 0 );
                if( bodyElement instanceof Node.TemplateText ) {
                    templateTextOptimization = true;
                    out.printil( "String " + varName + " = " +
                        quote( new String(
                        ((Node.TemplateText)bodyElement).getText() ) ) + ";" );
                }
            }

            // XXX - Another possible optimization would be for
            // lone EL expressions (no need to pushBody here either).

            if( !templateTextOptimization ) {
                out.printil( "out = pageContext.pushBody();" );
                visitBody( n );
                out.printil( "String " + varName + " = " +
			     "((javax.servlet.jsp.tagext.BodyContent)" +
			     "out).getString();" );
                out.printil( "out = pageContext.popBody();" );
            }

            return varName;
        }

        /**
         * Similar to generateNamedAttributeValue, but create a JspFragment
         * instead.
         *
         * @param n The parent node of the named attribute
         * @param tagHandlerVar The variable the tag handler is stored in,
         *     so the fragment knows its parent tag.
         * @return The name of the temporary variable the fragment 
         *     is stored in.
         */
        public String generateNamedAttributeJspFragment( 
            Node.NamedAttribute n, String tagHandlerVar )
            throws JasperException
        {
            String varName = n.getTemporaryVariableName();
            
            out.printin( "javax.servlet.jsp.tagext.JspFragment " + varName + 
                " = " );
            generateJspFragment( n, tagHandlerVar );
            out.println( ";" );
            
            return varName;
        }
    }
    
    private static void generateLocalVariables( ServletWriter out, Node n ) 
        throws JasperException
    {
        Node.ChildInfo ci;
        if( n instanceof Node.CustomTag ) {
            ci = ((Node.CustomTag)n).getChildInfo();
        }
        else if( n instanceof Node.JspBody ) {
            ci = ((Node.JspBody)n).getChildInfo();
        }
        else if( n instanceof Node.NamedAttribute ) {
            ci = ((Node.NamedAttribute)n).getChildInfo();
        }
        else {
            // Cannot access err since this method is static, but at
            // least flag an error.
            throw new JasperException( "Unexpected Node Type" );
            //err.getString( 
            //    "jsp.error.internal.unexpected_node_type" ) );
        }
        
        if (ci.isHasUsebean()) {
            out.println("HttpSession session = pageContext.getSession();");
            out.println("ServletContext application = pageContext.getServletContext();");
        }
        if (ci.isHasUsebean() || ci.isHasIncludeAction() || ci.isHasSetProperty()) {
            out.println("HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();");
        }
        if (ci.isHasIncludeAction()) {
            out.println("ServletResponse response = pageContext.getResponse();");
        }
    }
    
    /**
     * Generates the ending part of the static portion of the servlet.
     */
    private void generatePostamble(Node.Nodes page) {
        out.popIndent();
        out.printil("} catch (Throwable t) {");
        out.pushIndent();

        out.printil("out = _jspx_out;");
        out.printil("if (out != null && out.getBufferSize() != 0)");
        out.pushIndent();
        out.printil("out.clearBuffer();");
        out.popIndent();

        out.printil("if (pageContext != null) pageContext.handlePageException(t);");

        out.popIndent();
        out.printil("} finally {");
        out.pushIndent();

        out.printil("if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);");

        out.popIndent();
        out.printil("}");

        // Close the service method
        out.popIndent();
        out.printil("}");

	// Append any methods that were generated
	out.print(methodsBuffer.toString());
        
        // Append the helper class
        if( fragmentHelperClass.isUsed() ) {
            fragmentHelperClass.generatePostamble();
            out.printMultiLn(fragmentHelperClass.toString());
        }

	// generate class definition for JspxState
        if (maxTagNesting > 0) {
	    generateJspState();
	}

        // Close the class definition
        out.popIndent();
        out.printil("}");
    }

    /**
     * Constructor.
     */
    Generator(ServletWriter out, Compiler compiler) {
	this.out = out;
	methodsBuffer = new MethodsBuffer();
	err = compiler.getErrorDispatcher();
	ctxt = compiler.getCompilationContext();
	fragmentHelperClass = new FragmentHelperClass( 
            ctxt.getServletClassName() + "Helper" );
        pageInfo = compiler.getPageInfo();
	beanInfo = pageInfo.getBeanRepository();
	breakAtLF = ctxt.getOptions().getMappedFile();
	if (ctxt.getOptions().isPoolingEnabled()) {
	    tagHandlerPoolNames = new Vector();
	}
    }

    /**
     * The main entry for Generator.
     * @param out The servlet output writer
     * @param compiler The compiler
     * @param page The input page
     */
    public static void generate(ServletWriter out, Compiler compiler,
				Node.Nodes page) throws JasperException {
	Generator gen = new Generator(out, compiler);

	if (gen.ctxt.getOptions().isPoolingEnabled()) {
	    gen.compileTagHandlerPoolList(page);
	}
	gen.generatePreamble(page);
	gen.fragmentHelperClass.generatePreamble();
	page.visit(gen.new GenerateVisitor(out, gen.methodsBuffer, 
					   gen.fragmentHelperClass));
	gen.generatePostamble(page);
    }

    /**
     * XXX
     */
    public static void generateTagHandler(ServletWriter out,
					  Compiler compiler,
					  Node.Nodes page)
	                throws JasperException {
	Generator gen = new Generator(out, compiler);
	gen.generateTagHandlerPreamble(page);
	page.visit(gen.new GenerateVisitor(out, gen.methodsBuffer, null));
	gen.generateTagHandlerPostamble(page);
    }

    /*
     * XXX
     */
    private void generateTagHandlerPreamble(Node.Nodes page)
	    throws JasperException {

	// Generate class declaration
	out.printin("public class ");
	out.print("XXX");
	out.print(" extends javax.servlet.jsp.tagext.SimpleTagSupport {");
	out.pushIndent();
	
	// Class body begins here
	MethodsBuffer accessorsBuf = new MethodsBuffer();
	MethodsBuffer setAttributeBuf = new MethodsBuffer();
	generateTagHandlerDeclarations(page, accessorsBuf, setAttributeBuf);
	out.printMultiLn(accessorsBuf.toString());
	out.printil("public int doTag() throws JspException {");
	out.printil("javax.servlet.jsp.JspWriter out = jspContext.getOut();");
	out.printil("jspContext.pushPageScope();");
	// create page-scope attributes for each tag attribute
	out.printMultiLn(setAttributeBuf.toString());
	out.printil("try {");
	out.pushIndent();
    }

    private void generateTagHandlerPostamble(Node.Nodes page) {
        out.popIndent();
        out.printil("} finally {");
        out.pushIndent();
        out.printil("jspContext.popPageScope();");
        out.popIndent();
	out.printil("}");
	out.println();
	out.printil("return EVAL_PAGE;");
    }

    /**
     * Generates declarations for tag handler attributes.
     */
    private void generateTagHandlerDeclarations(Node.Nodes page,
						MethodsBuffer accessorsBuf,
						MethodsBuffer setAttributeBuf)
	        throws JasperException {

	class DeclarationVisitor extends Node.Visitor {

	    private MethodsBuffer accessorsBuf;
	    private MethodsBuffer setAttributeBuf;

	    public DeclarationVisitor(MethodsBuffer accessorsBuf,
				      MethodsBuffer setAttributeBuf) {
		this.accessorsBuf = accessorsBuf;
		this.setAttributeBuf = setAttributeBuf;
	    }

	    public void visit(Node.AttributeDirective n)
		    throws JasperException {

		boolean isFragment
		    = "true".equalsIgnoreCase(n.getAttributeValue("fragment"));
		if (isFragment)
		    out.printin("private javax.servlet.jsp.tagext.JspFragment ");
		else
		    out.printin("private String ");
		String attrName = n.getAttributeValue("name");
		out.print(attrName);
		out.println(";");

		// generate getter and setter methods
		ServletWriter outSave = out;
		out = accessorsBuf.getOut();
		if (isFragment)
		    out.printin("public javax.servlet.jsp.tagext.JspFragment get");
		else
		    out.printin("public String get");
		String getterMethodName = getAccessorMethodName(attrName,
								true);
		out.print(getterMethodName);
		out.println("() {");
		out.pushIndent();
		out.printin("return this.");
		out.print(attrName);
		out.println(";");
		out.popIndent();
		out.printil("}");
		out.println();
		out.printin("public void set");
		out.print(getAccessorMethodName(attrName, false));
		if (isFragment)
		    out.printin("javax.servlet.jsp.tagext.JspFragment ");
		else
		    out.printin("String ");
		out.print(attrName);
		out.println(") {");
		out.pushIndent();
		out.printin("this.");
		out.print(attrName);
		out.print(" = ");
		out.print(attrName);
		out.println(";");
		out.popIndent();
		out.printil("}");

		// set attribute in JspContext
		out = setAttributeBuf.getOut();
		out.printin("this.jspContext.setAttribute(\"");
		out.print(attrName);
		out.print("\", ");
		out.print(getterMethodName);
		out.println("());");

		out = outSave;
	    }
	}

	out.println();

	// Parameter map for fragment/body invocation
	out.println("java.util.Map params = null;");

	// Used for storing result of fragment/body invocation if 'varReader'
	// attribute is specified
	out.println("java.io.Writer sout = null;");

	page.visit(new DeclarationVisitor(accessorsBuf, setAttributeBuf));
    }

    private String getAccessorMethodName(String attrName, boolean getter) {
	char[] attrChars = attrName.toCharArray();
	attrChars[0] = Character.toUpperCase(attrChars[0]);
	if (getter)
	    return "get" + new String(attrChars);
	else
	    return "set" + new String(attrChars);
    }

    /**
     * Class storing the result of introspecting a custom tag handler.
     */
    private static class TagHandlerInfo {

	private Hashtable methodMaps;
	private Hashtable propertyEditorMaps;
	private Class tagHandlerClass;
    
	/**
	 * Constructor.
	 *
	 * @param n The custom tag whose tag handler class is to be
	 * introspected
	 * @param tagHandlerClass Tag handler class
	 * @param err Error dispatcher
	 */
	TagHandlerInfo(Node n, Class tagHandlerClass, ErrorDispatcher err)
	    throws JasperException
	{
	    this.tagHandlerClass = tagHandlerClass;
	    this.methodMaps = new Hashtable();
	    this.propertyEditorMaps = new Hashtable();

	    try {
		BeanInfo tagClassInfo
		    = Introspector.getBeanInfo(tagHandlerClass);
		PropertyDescriptor[] pd
		    = tagClassInfo.getPropertyDescriptors();
		for (int i=0; i<pd.length; i++) {
		    /*
		     * FIXME: should probably be checking for things like
		     *        pageContext, bodyContent, and parent here -akv
		     */
		    if (pd[i].getWriteMethod() != null) {
			methodMaps.put(pd[i].getName(),
				       pd[i].getWriteMethod());
		    }
		    if (pd[i].getPropertyEditorClass() != null)
			propertyEditorMaps.put(pd[i].getName(),
					       pd[i].getPropertyEditorClass());
		}
	    } catch (IntrospectionException ie) {
		err.jspError(n, "jsp.error.introspect.taghandler",
			     tagHandlerClass.getName(), ie);
	    }
	}

	/**
	 * XXX
	 */
	public Method getSetterMethod(String attrName) {
	    return (Method) methodMaps.get(attrName);
	}

	/**
	 * XXX
	 */
	public Class getPropertyEditorClass(String attrName) {
	    return (Class) propertyEditorMaps.get(attrName);
	}

	/**
	 * XXX
	 */
	public Class getTagHandlerClass() {
	    return tagHandlerClass;
	}
    }

    private static class MethodsBuffer {

	private java.io.CharArrayWriter charWriter;
	protected ServletWriter out;

	MethodsBuffer() {
	    charWriter = new java.io.CharArrayWriter();
	    out = new ServletWriter(new java.io.PrintWriter(charWriter));
	}

	public ServletWriter getOut() {
	    return out;
	}

	public String toString() {
	    return charWriter.toString();
	}
    }

    /**
     * Keeps track of the generated Fragment Helper Class
     */
    private static class FragmentHelperClass {
        
        private static class Fragment {
            private MethodsBuffer methodsBuffer;
            private int id;
            
            public Fragment( int id ) {
                this.id = id;
                methodsBuffer = new MethodsBuffer();
            }
            
            public MethodsBuffer getMethodsBuffer() {
                return this.methodsBuffer;
            }
            
            public int getId() {
                return this.id;
            }
        }
        
        // True if the helper class should be generated.
        private boolean used = false;
        
        private ArrayList fragments = new ArrayList();
        
        private String className;

        // Buffer for entire helper class
        private MethodsBuffer classBuffer = new MethodsBuffer();
        
	public FragmentHelperClass( String className ) {
            this.className = className;
	}
        
        public String getClassName() {
            return this.className;
        }
        
        public boolean isUsed() {
            return this.used;
        }
        
        public void generatePreamble() {
	    ServletWriter out = this.classBuffer.getOut();
            out.println();
            out.pushIndent();
            // Note: cannot be static, as we need to reference things like
	    // _jspx_meth_*
	    out.printil( "class " + className );
            out.printil( "    extends " +
                "org.apache.jasper.runtime.JspFragmentHelper" );
            out.printil( "{" );
            out.pushIndent();
            out.printil( "public " + className + 
                "( int discriminator, JspContext jspContext, " +
                "Object parentTag ) {" );
            out.pushIndent();
            out.printil( "super( discriminator, jspContext, parentTag );" );
            out.popIndent();
            out.printil( "}" );
        }
        
	public Fragment openFragment( Node parent ) 
            throws JasperException 
        {
            Fragment result = new Fragment( fragments.size() );
            fragments.add( result );
            this.used = true;

            ServletWriter out = result.getMethodsBuffer().getOut();
            out.pushIndent();
            out.pushIndent();
            // XXX - Returns boolean because if a tag is invoked from
            // within this fragment, the Generator sometimes might
            // generate code like "return true".  This is ignored for now,
            // meaning only the fragment is skipped.  The JSR-152
            // expert group is currently discussing what to do in this case.
            // See comment in closeFragment()
            out.printil( "public boolean invoke" + result.getId() + "( " +
                "java.io.Writer out, java.util.Map params ) " );
            out.pushIndent();
            // Note: Throwable required because methods like _jspx_meth_*
            // throw Throwable.
            out.printil( "throws Throwable" );
            out.popIndent();
            out.printil( "{" );
            out.pushIndent();
            generateLocalVariables( out, parent );
            
            return result;
        }
        
        public void closeFragment( Fragment fragment ) {
            ServletWriter out = fragment.getMethodsBuffer().getOut();
            // XXX - See comment in openFragment()
            out.printil( "return false;" );
            out.popIndent();
            out.printil( "}" );
        }
        
        public void generatePostamble() {
            ServletWriter out = this.classBuffer.getOut();
            // Generate all fragment methods:
            for( int i = 0; i < fragments.size(); i++ ) {
                Fragment fragment = (Fragment)fragments.get( i );
                out.printMultiLn( fragment.getMethodsBuffer().toString() );
            }
            
            // Generate postamble:
            out.printil( "public void invoke( java.io.Writer out, " +
			 "java.util.Map params )" );
            out.pushIndent();
            out.printil( "throws javax.servlet.jsp.JspException" );
            out.popIndent();
            out.printil( "{" );
            out.pushIndent();
            out.printil( 
                "this.jspContext.pushPageScope( this.originalPageScope );" );
            out.printil( "java.util.Map _jspx_originalValues = null;" );
            out.printil( "if( params != null ) {" );
            out.pushIndent();
            out.printil( "_jspx_originalValues = preparePageScope( params );");
            out.popIndent();
            out.printil( "}" );
            out.printil( "if( out == null ) {" );
            out.pushIndent();
            out.printil( "out = this.jspContext.getOut();" );
            out.popIndent();
            out.printil( "}" );
            out.printil( "try {" );
            out.pushIndent();
            out.printil( "switch( this.discriminator ) {" );
            out.pushIndent();
	    for( int i = 0; i < fragments.size(); i++ ) {
                out.printil( "case " + i + ":" );
                out.pushIndent();
                out.printil( "invoke" + i + "( out, params );" );
                out.printil( "break;" );
                out.popIndent();
            }
            out.popIndent();
            out.printil( "}" ); // switch
            out.popIndent();
            out.printil( "}" ); // try
	    out.printil( "catch( Throwable e ) {" );
            out.pushIndent();
            out.printil( "throw new javax.servlet.jsp.JspException( e );" );
            out.popIndent();
            out.printil( "}" ); // catch
            out.printil( "finally {" );
            out.pushIndent();
            out.printil( "if( params != null ) {" );
            out.pushIndent();
            out.printil( "restorePageScope( _jspx_originalValues );");
            out.popIndent();
            out.printil( "}" );
            out.printil( "this.jspContext.popPageScope();" );
            out.popIndent();
            out.printil( "}" ); // finally
            out.popIndent();
            out.printil( "}" ); // invoke method
            out.popIndent();
            out.printil( "}" ); // helper class
            out.popIndent();
        }
        
        public String toString() {
            return classBuffer.toString();
        }
    }
}

