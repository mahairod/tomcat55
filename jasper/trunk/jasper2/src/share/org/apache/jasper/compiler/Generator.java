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

class Generator {

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

    private void declareTemporaryScriptingVars(Node.Nodes page)
	    throws JasperException {

	class ScriptingVarVisitor extends Node.Visitor {

	    private Vector vars;

	    ScriptingVarVisitor() {
		vars = new Vector();
	    }

	    public void visit(Node.CustomTag n) throws JasperException {

		if (n.getCustomNestingLevel() > 0) {
		    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
		    VariableInfo[] varInfos = n.getVariableInfos();

		    if (varInfos != null) {
			for (int i=0; i<varInfos.length; i++) {
			    String varName = varInfos[i].getVarName();
			    String tmpVarName = "_jspx_" + varName + "_"
				+ n.getCustomNestingLevel();
			    if (!vars.contains(tmpVarName)) {
				vars.add(tmpVarName);
				out.printin(varInfos[i].getClassName());
				out.print(" ");
				out.print(tmpVarName);
				out.print(" = ");
				out.print(null);
				out.println(";");
			    }
			}
		    } else if (tagVarInfos != null) {
			for (int i=0; i<tagVarInfos.length; i++) {
			    String varName = tagVarInfos[i].getNameGiven();
			    if (varName == null) {
				varName = n.getTagData().getAttributeString(
			                tagVarInfos[i].getNameFromAttribute());
			    }
			    String tmpVarName = "_jspx_" + varName + "_"
				+ n.getCustomNestingLevel();
			    if (!vars.contains(tmpVarName)) {
				vars.add(tmpVarName);
				out.printin(tagVarInfos[i].getClassName());
				out.print(" ");
				out.print(tmpVarName);
				out.print(" = ");
				out.print(null);
				out.println(";");
			    }
			}
		    }
		}

		visitBody(n);
	    }
	}

	page.visit(new ScriptingVarVisitor());
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
     * Generate preamble package name
     * (shared by servlet and tag handler preamble generation)
     */
    private void genPreamblePackage( String packageName ) 
        throws JasperException
    {
	if (! "".equals(packageName) && packageName != null) {
	    out.printil("package " + packageName + ";");
	    out.println();
        }
    }
    
    /**
     * Generate preamble imports
     * (shared by servlet and tag handler preamble generation)
     */
    private void genPreambleImports() 
        throws JasperException
    {
	Iterator iter = pageInfo.getImports().iterator();
	while (iter.hasNext()) {
	    out.printin("import ");
	    out.print  ((String)iter.next());
	    out.println(";");
	}
	out.println();
    }

    /**
     * Generation of static initializers in preamble.
     * For example, dependant list, el function map, prefix map.
     * (shared by servlet and tag handler preamble generation)
     */
    private void genPreambleStaticInitializers() 
        throws JasperException
    {
        // Static data for getDependants()
        out.printil("private static java.util.Vector _jspx_dependants;");
        out.println();
        List dependants = pageInfo.getDependants();
        Iterator iter = dependants.iterator();
        if( !dependants.isEmpty() ) {
            out.printil("static {");
            out.pushIndent();
            out.printin("_jspx_dependants = new java.util.Vector(");
            out.print(""+dependants.size());
            out.println(");");
            while (iter.hasNext()) {
                out.printin("_jspx_dependants.add(\"");
                out.print((String)iter.next());
                out.println("\");");
            }
            out.popIndent();                     
            out.printil("}");
            out.println();
        }
        
        // Static data for EL function and prefix maps:
        generateELFunctionMap();
        generateFunctionMapper();
    }

    /**
     * Declare tag handler pools (tags of the same type and with the same
     * attribute set share the same tag handler pool)
     * (shared by servlet and tag handler preamble generation)
     */
    private void genPreambleClassVariableDeclarations( String className ) 
        throws JasperException
    {
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
	    generateConstructor(className);
	}
    }

    /**
     * Declare general-purpose methods
     * (shared by servlet and tag handler preamble generation)
     */
    private void genPreambleMethods() 
        throws JasperException
    {
	// Method used to get compile time file dependencies
        out.printil("public java.util.List getDependants() {");
        out.pushIndent();
        out.printil("return _jspx_dependants;");
        out.popIndent();
        out.printil("}");
        out.println();

	if (ctxt.getOptions().isPoolingEnabled()
	        && !tagHandlerPoolNames.isEmpty()) {
	    generateDestroy();
	}
    }
    
    /**
     * Generates the beginning of the static portion of the servlet.
     */
    private void generatePreamble(Node.Nodes page) throws JasperException {

	String servletPackageName = ctxt.getServletPackageName();
	String servletClassName = ctxt.getServletClassName();
	String serviceMethodName = Constants.SERVICE_METHOD_NAME;

	// First the package name:
        genPreamblePackage( servletPackageName );

	// Generate imports
        genPreambleImports();

	// Generate class declaration
	out.printin("public final class ");
	out.print  (servletClassName);
	out.print  (" extends ");
	out.println(pageInfo.getExtends());
	out.printil("    implements javax.servlet.jsp.el.FunctionMapper, ");
	out.printin("               org.apache.jasper.runtime.JspSourceDependent");
	if (!pageInfo.isThreadSafe()) {
	    out.println(",");
	    out.printin("                 SingleThreadModel");
	}
	out.println(" {");
	out.pushIndent();

	// Class body begins here
	generateDeclarations(page);

	// Static initializations here
        genPreambleStaticInitializers();

 	// Class variable declarations
        genPreambleClassVariableDeclarations( servletClassName );
 
	// Methods here
        genPreambleMethods();
 
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

	if (pageInfo.isIsErrorPage()) {
            out.printil("Throwable exception = (Throwable) request.getAttribute(\"javax.servlet.jsp.jspException\");");
	    out.printil("request.setAttribute(\"javax.servlet.error.exception\", exception);");
	}

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

	declareTemporaryScriptingVars(page);
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
            if( tli.getFunctions() != null && tli.getFunctions().length > 0 ) {
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
                    
                    JspUtil.FunctionSignature functionSignature = 
                        new JspUtil.FunctionSignature( fnSignature, 
                        tli.getShortName(), err, ctxt.getClassLoader() );
                    out.print( quote( functionSignature.getMethodName() ) );
                    out.print(", ");
                    Class[] args = functionSignature.getParameterTypes();
		    if (args != null) {
			out.print("new Class[] {" );
			for( int j = 0; j < args.length; j++ ) {
			    out.print( args[j].getName() + ".class" );
			    if( j < (args.length - 1) ) {
				out.print( ", " );
			    }
			}
			out.print("} ");
		    } else {
			out.print("null");
		    }
                    out.println("));");
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
    
    /**
     * Generates the method needed to implement FunctionMapper
     */
    private void generateFunctionMapper() 
        throws JasperException 
    {
        out.printil( "public java.lang.reflect.Method resolveFunction(" );
        out.printil( "    String prefix, String localName )" );
        out.printil( "{" );
        out.pushIndent();
        out.printil( "return (java.lang.reflect.Method)_jspx_fnmap.get( " );
        out.printil( "    prefix + \":\" + localName );" );
        out.popIndent();
        out.printil( "}" );
        out.println();
    }


    /*
     * Generates the constructor.
     * (shared by servlet and tag handler preamble generation)
     */
    private void generateConstructor(String className) {
	out.printil("public " + className + "() {");
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
	private boolean isFragment;
	private boolean isTagFile;
	private ServletWriter out;
	private MethodsBuffer methodsBuffer;
	private FragmentHelperClass fragmentHelperClass;
	private int methodNesting;
	private TagInfo tagInfo;
	private ClassLoader loader;

	/**
	 * Constructor.
	 */
	public GenerateVisitor(boolean isTagFile,
			       ServletWriter out, 
			       MethodsBuffer methodsBuffer, 
			       FragmentHelperClass fragmentHelperClass,
			       ClassLoader loader,
			       TagInfo tagInfo) {
	    this.isTagFile = isTagFile;
	    this.out = out;
	    this.methodsBuffer = methodsBuffer;
	    this.fragmentHelperClass = fragmentHelperClass;
	    this.loader = loader;
	    this.tagInfo = tagInfo;
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
		    v = JspUtil.interpreterCall(this.isTagFile,
		        attr.getValue(), expectedType, defaultPrefix,
			"_jspx_fnmap" );
		}
		if (encode) {
		    return "java.net.URLEncoder.encode(\"\" + " + v + ")";
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
		    out.print(n.getTextAttribute("name"));
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
	    out.println();
	    n.setEndJavaLine(out.getJavaLine());
	}

        public void visit(Node.ELExpression n) throws JasperException {
            n.setBeginJavaLine(out.getJavaLine());
            if ( !pageInfo.isELIgnored() ) {
                out.printil(
                    "out.write("
		    + JspUtil.interpreterCall(this.isTagFile,
                        "${" + new String(n.getText()) + "}", String.class,
			null, "_jspx_fnmap" )
                    + ");");
            } else {
                out.printil("out.write(" +
                    quote("${" + new String(n.getText()) + "}") +
                    ");");
            }
            n.setEndJavaLine(out.getJavaLine());
        }

	public void visit(Node.IncludeAction n) throws JasperException {

	    String flush = n.getTextAttribute("flush");
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
            
            out.printin("org.apache.jasper.runtime.JspRuntimeLibrary.include(request, response, " +
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
	    if (isTagFile || isFragment) {
		out.printil("throw new javax.servlet.jsp.SkipPageException();");
	    } else {
		out.printil((methodNesting > 0)? "return true;": "return;");
	    }
	    out.popIndent();
	    out.printil("}");

	    n.setEndJavaLine(out.getJavaLine());
	    // XXX Not sure if we can eliminate dead codes after this.
	}

	public void visit(Node.GetProperty n) throws JasperException {
	    String name = n.getTextAttribute("name");
	    String property = n.getTextAttribute("property");

	    n.setBeginJavaLine(out.getJavaLine());

	    if (beanInfo.checkVariable(name)) {
		// Bean is defined using useBean, introspect at compile time
		Class bean = beanInfo.getBeanType(name);
		String beanName = bean.getName();
		java.lang.reflect.Method meth =
		    JspRuntimeLibrary.getReadMethod(bean, property);
		String methodName = meth.getName();
		out.printil("out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString(" +
			    "(((" + beanName + ")pageContext.findAttribute(" +
			    "\"" + name + "\"))." + methodName + "())));");
	    } else {
		// The object could be a custom action with an associated
		// VariableInfo entry for this name.
		// Get the class name and then introspect at runtime.
		out.printil("out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString" +
			    "(org.apache.jasper.runtime.JspRuntimeLibrary.handleGetProperty" +
			    "(pageContext.findAttribute(\"" +
			    name + "\"), \"" + property + "\")));");
            }

	    n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.SetProperty n) throws JasperException {
	    String name = n.getTextAttribute("name");
	    String property = n.getTextAttribute("property");
	    String param = n.getTextAttribute("param");
	    Node.JspAttribute value = n.getValue();

	    n.setBeginJavaLine(out.getJavaLine());

	    if ("*".equals(property)){
		out.printil("org.apache.jasper.runtime.JspRuntimeLibrary.introspect(" +
			    "pageContext.findAttribute(" +
			    "\"" + name + "\"), request);");
	    } else if (value == null) {
		if (param == null)
		    param = property;	// default to same as property
		out.printil("org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper(" +
			    "pageContext.findAttribute(\"" + name + "\"), \"" +
			    property + "\", request.getParameter(\"" + param +
			    "\"), " + "request, \"" + param + "\", false);");
	    } else if (value.isExpression()) {
		out.printil("org.apache.jasper.runtime.JspRuntimeLibrary.handleSetProperty(" + 
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

		// The following holds true for the arguments passed to
		// JspRuntimeLibrary.handleSetPropertyExpression():
		// - 'pageContext' is a VariableResolver.
		// - 'this' (either the generated Servlet or the generated tag
		//   handler for Tag files) is a FunctionMapper.
                out.printil("org.apache.jasper.runtime.JspRuntimeLibrary.handleSetPropertyExpression(" +
                    "pageContext.findAttribute(\""  + name + "\"), \""
                    + property + "\", "
                    + quote(value.getValue()) + ", "
                    + "pageContext, "
                    + "(javax.servlet.jsp.el.VariableResolver) pageContext, "
                    + "(javax.servlet.jsp.el.FunctionMapper) this );");
            } else if( value.isNamedAttribute() ) {
                // If the value for setProperty was specified via
                // jsp:attribute, first generate code to evaluate
                // that body.
                String valueVarName = generateNamedAttributeValue(
                    value.getNamedAttributeNode() );
                out.printil("org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper(" +
                            "pageContext.findAttribute(\""  + name + "\"), \""
                            + property + "\", "
			    + valueVarName
			    + ", null, null, false);");
	    } else {
		out.printin("org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper(" +
			    "pageContext.findAttribute(\"" + name + "\"), \""
			    + property + "\", ");
		out.print(attributeValue(value, false, null, "null"));
		out.println(", null, null, false);");
	    }

	    n.setEndJavaLine(out.getJavaLine());
        }

        public void visit(Node.UseBean n) throws JasperException {

	    String name = n.getTextAttribute ("id");
	    String scope = n.getTextAttribute ("scope");
	    String klass = n.getTextAttribute ("class");
	    String type = n.getTextAttribute ("type");
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

		    String name = n.getTextAttribute("name");
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

	    String type = n.getTextAttribute("type");
	    String code = n.getTextAttribute("code");
	    String name = n.getTextAttribute("name");
	    Node.JspAttribute height = n.getHeight();
	    Node.JspAttribute width = n.getWidth();
	    String hspace = n.getTextAttribute("hspace");
	    String vspace = n.getTextAttribute("vspace");
	    String align = n.getTextAttribute("align");
	    String iepluginurl = n.getTextAttribute("iepluginurl");
	    String nspluginurl = n.getTextAttribute("nspluginurl");
	    String codebase = n.getTextAttribute("codebase");
	    String archive = n.getTextAttribute("archive");
	    String jreversion = n.getTextAttribute("jreversion");
            
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

	    // If the tag contains no scripting element, generate its codes
	    // to a method.
	    ServletWriter outSave = null;
	    MethodsBuffer methodsBufferSave = null;
            Node.ChildInfo ci = n.getChildInfo();
	    if (ci.isScriptless() && !ci.hasScriptingVars()) {
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
		    out.print("javax.servlet.jsp.tagext.JspTag ");
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

	    if (ci.isScriptless() && !ci.hasScriptingVars()) {
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

	public void visit(Node.JspElement n) throws JasperException {

	    // Hashtable for storing attribute name/value combinations
	    Hashtable map = new Hashtable();

	    Node.JspAttribute[] attrs = n.getJspAttributes();
	    for (int i=0; i<attrs.length; i++) {
		String attrStr = null;
		if (attrs[i].isNamedAttribute()) {
		    attrStr = generateNamedAttributeValue(
                            attrs[i].getNamedAttributeNode());
		} else {
		    if ("name".equals(attrs[i].getName())) {
			attrStr = attributeValue(attrs[i], false, String.class,
						 "null");
		    } else {
			attrStr = attributeValue(attrs[i], false, Object.class,
						 "null");
		    }
		}
		String s = null;
		if ("name".equals(attrs[i].getName())) {
		    s = " + " + attrStr;
		} else {
		    s = " + \" " + attrs[i].getName() + "=\\\"\" + "
			+ attrStr + " + \"\\\"\"";
		}
		map.put(attrs[i].getName(), s);
	    }
            
	    // Write begin tag
	    out.printin("out.write(\"<\"");
	    // Write 'name' attribute
	    out.print((String) map.get("name"));
	    // Write remaining attributes
	    Enumeration enum = map.keys();
	    while (enum.hasMoreElements()) {
		String attrName = (String) enum.nextElement();
		if ("name".equals(attrName))
		    continue;
		out.print((String) map.get(attrName));
	    }

	    // Does the <jsp:element> have nested tags other than
	    // <jsp:attribute>
	    boolean hasBody = false;
            Node.Nodes subelements = n.getBody();
	    if (subelements != null) {
		for (int i = 0; i<subelements.size(); i++) {
		    Node subelem = subelements.getNode(i);
		    if (!(subelem instanceof Node.NamedAttribute)) {
			hasBody = true;
			break;
		    }
		}
	    }
	    if (hasBody) {
		out.println(" + \">\");");
		
		// Visit tag body
		visitBody(n);

		// Write end tag
		out.printin("out.write(\"</\"");
		out.print((String) map.get("name"));
		out.println(" + \">\");");
	    } else {
		out.println(" + \"/>\");");
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
            Node.JspAttribute value = n.getValue();
	    if (isSimpleTagHandler) {
		out.printin(simpleTagHandlerVar);
		out.print(".setJspBody(");

		if (value != null) {
		    out.print(attributeValue(value, false, JspFragment.class,
                        "null" ));
		} else {
		    generateJspFragment(n, simpleTagHandlerVar);
		}
		out.println(");");
	    } else {
                Node parent = n.getParent();
                if( (parent instanceof Node.CustomTag) && (value != null) ) {
                    Node.CustomTag customTag = (Node.CustomTag)parent;
                    
                    // Classic tag handler invoked with <jsp:body value="...">
                    // Generate a tag body that evaluates the given
                    // fragment.
                    
                    // First, generate a Map with all the AT_BEGIN and 
                    // NESTED variables so the body can access them.
                    VariableInfo[] varInfos = customTag.getVariableInfos();
                    TagVariableInfo[] tagVarInfos = 
                        customTag.getTagVariableInfos();
                    
                    String var = JspUtil.nextTemporaryVariableName();
                    out.printil( "java.util.HashMap " + var + 
                        " = new java.util.HashMap();" );
                    
                    if( varInfos != null ) {
                        for( int i = 0; i < varInfos.length; i++ ) {
                            if( (varInfos[i].getScope() == 
                                    VariableInfo.AT_BEGIN) ||
                                (varInfos[i].getScope() == 
                                    VariableInfo.NESTED) ) 
                            {
                                out.printil( var + ".put( \"" + 
                                    varInfos[i].getVarName() + "\", " +
                                    varInfos[i].getVarName() + " );" );
                            }
                        }
                    }

                    if( tagVarInfos != null ) {
                        for( int i = 0; i < tagVarInfos.length; i++ ) {
                            if( (tagVarInfos[i].getScope() == 
                                    VariableInfo.AT_BEGIN) || 
                                (tagVarInfos[i].getScope() ==
                                    VariableInfo.NESTED) )
                            {
                                out.printin( var + ".put( \"" );
                                String name = tagVarInfos[i].getNameGiven();
                                if( name == null ) {
                                    name = customTag.getTagData().
                                        getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
                                }
                                out.println( name + "\", " + name + " );" );
                            }
                        }
                    }
                    
                    out.printil("(" + 
                        attributeValue(value, false, JspFragment.class,
                        "null" ) + ").invoke( out, " + var + " );" );
		} else {
                    visitBody(n);
		}
	    }
	}

        public void visit(Node.InvokeAction n) throws JasperException {

	    /**
	     * A visitor to handle <jsp:param> in a <jsp:invoke>
	     */
	    class ParamVisitor extends Node.Visitor {

		// The name of the fragment to which the <jsp:param> applies
		private String fragName;

		public ParamVisitor(String fragName) {
		    this.fragName = fragName;
		}

                public void visit(Node.ParamAction n) throws JasperException {
		    out.printin("_jspx_params.put(");
		    out.print(quote(n.getTextAttribute("name")));
		    out.print(", ");
		    out.print(attributeValue(n.getValue(), false,
					     getParamClass(n), "null"));
		    out.println(");");
		}

		/*
		 * Checks to see if the given <jsp:param> matches a tag file
		 * variable scoped to the same fragment as the enclosing
		 * <jsp:invoke>. If a match is found, the class specified in
		 * the variable directive's 'variable-class' attribute (if 
		 * present) is loaded and returned.
		 */
		private Class getParamClass(Node.ParamAction n)
		            throws JasperException {

		    Class clazz = String.class;

		    TagVariableInfo[] tagVars = tagInfo.getTagVariableInfos();
		    if (tagVars != null) {
			String paramName = n.getTextAttribute("name");
			for (int i=0; i<tagVars.length; i++) {
			    String varName = tagVars[i].getNameGiven();
			    if (varName == null) {
				// XXX name-from-attribute:
				// What to do in this case?
			    }
			    String tagVarFrag = tagVars[i].getFragment();
			    if (!paramName.equals(varName)
				    || tagVarFrag == null
				    || !this.fragName.equals(tagVarFrag))
				continue;
			    try {
				clazz = JspUtil.toClass(tagVars[i].getClassName(), loader);
			    } catch (ClassNotFoundException cnfe) {
				throw new JasperException(cnfe);
			    }
			}
		    }

		    return clazz;
		}
	    }

	    // Assemble parameter map
	    out.printil("_jspx_params = new java.util.HashMap();");
	    if (n.getBody() != null) {
		prepareParams(n);
		n.getBody().visit(new ParamVisitor(
                                            n.getTextAttribute("fragment")));
	    }
	    
	    // Invoke fragment with parameter map
	    String varReaderAttr = n.getTextAttribute("varReader");
	    String varAttr = n.getTextAttribute("var");
	    if (varReaderAttr != null || varAttr != null) {
		out.printil("_jspx_sout = new java.io.StringWriter();");
		out.printin(toGetterMethod(n.getTextAttribute("fragment")));
		out.println(".invoke(_jspx_sout, _jspx_params);");
	    } else {
		out.printin(toGetterMethod(n.getTextAttribute("fragment")));
		out.println(".invoke(null, _jspx_params);");
	    }

	    // Store varReader in appropriate scope
	    if (varReaderAttr != null || varAttr != null) {
		String scopeName = n.getTextAttribute("scope");
		out.printin("pageContext.setAttribute(");
		if (varReaderAttr != null) {
		    out.print(quote(varReaderAttr));
		    out.print(", new java.io.StringReader(_jspx_sout.toString())");
		} else {
		    out.print(quote(varAttr));
		    out.print(", _jspx_sout.toString()");
		}		    
		if (scopeName != null) {
		    out.print(", ");
		    out.print(getScopeConstant(scopeName));
		}
		out.println(");");
	    }
	}

        public void visit(Node.DoBodyAction n) throws JasperException {

	    /**
	     * A visitor to handle <jsp:param> in a <jsp:doBody>
	     */
	    class ParamVisitor extends Node.Visitor {

                public void visit(Node.ParamAction n) throws JasperException {
		    out.printin("_jspx_params.put(");
		    out.print(quote(n.getTextAttribute("name")));
		    out.print(", ");
		    out.print(attributeValue(n.getValue(), false,
					     String.class, "null"));
		    out.println(");");
		}
	    }

	    // Assemble parameter map
	    out.printil("_jspx_params = new java.util.HashMap();");
	    if (n.getBody() != null) {
		prepareParams(n);
		n.getBody().visit(new ParamVisitor());
	    }

	    // Add AT_BEGIN and NESTED scripting variables (that are not 
	    // scoped to any fragment) to parameter map
	    TagVariableInfo[] tagVars = tagInfo.getTagVariableInfos();
	    if (tagVars != null) {
		for (int i=0; i<tagVars.length; i++) {
		    if (tagVars[i].getFragment() != null) {
			continue;
		    }
		    int scope = tagVars[i].getScope();
		    if (scope != VariableInfo.AT_BEGIN
			    && scope != VariableInfo.NESTED) {
			continue;
		    }
		    out.printin("_jspx_params.put(");
		    String name = tagVars[i].getNameGiven();
		    if (name != null) {
			out.print(quote(name));
			out.print(", pageContext.getAttribute(");
			out.print(quote(name));
			out.println("));");
		    } else {
			String getter = toGetterMethod(tagVars[i].getNameFromAttribute());
			out.print(getter);
			out.print(", pageContext.getAttribute(");
			out.print(getter);
			out.println("));");
		    }
		}
	    }

	    // Invoke body with parameter map
	    String varReaderAttr = n.getTextAttribute("varReader");
	    String varAttr = n.getTextAttribute("var");
	    if (varReaderAttr != null || varAttr != null) {
		out.printil("_jspx_sout = new java.io.StringWriter();");
		out.printil("getJspBody().invoke(_jspx_sout, _jspx_params);");
	    } else {
		out.printil("getJspBody().invoke(null, _jspx_params);");
	    }

	    // Store varReader in appropriate scope
	    if (varReaderAttr != null || varAttr != null) {
		String scopeName = n.getTextAttribute("scope");
		out.printin("pageContext.setAttribute(");
		if (varReaderAttr != null) {
		    out.print(quote(varReaderAttr));
		    out.print(", new java.io.StringReader(_jspx_sout.toString())");
		} else {
		    out.print(quote(varAttr));
		    out.print(", _jspx_sout.toString()");
		}
		if (scopeName != null) {
		    out.print(", ");
		    out.print(getScopeConstant(scopeName));
		}
		out.println(");");
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

	    // Declare AT_BEGIN scripting variables
	    declareScriptingVars(n, VariableInfo.AT_BEGIN);
	    saveScriptingVars(n, VariableInfo.AT_BEGIN);

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

	    if (!n.implementsBodyTag()) {
		// Synchronize AT_BEGIN scripting variables
		syncScriptingVars(n, VariableInfo.AT_BEGIN);
	    }

	    if (n.getBody() != null) {
		out.printin("if (");
		out.print(tagEvalVar);
		out.println(" != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {");
		out.pushIndent();
		
		// Declare NESTED scripting variables
		declareScriptingVars(n, VariableInfo.NESTED);
		saveScriptingVars(n, VariableInfo.NESTED);

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
		    syncScriptingVars(n, VariableInfo.AT_BEGIN);
		    syncScriptingVars(n, VariableInfo.NESTED);

		    out.popIndent();
		    out.printil("}");
		} else {
		    // Synchronize NESTED scripting variables
		    syncScriptingVars(n, VariableInfo.NESTED);
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

	    if (n.getBody() != null) {
		if (n.implementsIterationTag()) {
		    out.printin("int evalDoAfterBody = ");
		    out.print(tagHandlerVar);
		    out.println(".doAfterBody();");

		    // Synchronize AT_BEGIN and NESTED scripting variables
		    syncScriptingVars(n, VariableInfo.AT_BEGIN);
		    syncScriptingVars(n, VariableInfo.NESTED);

		    out.printil("if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)");
		    out.pushIndent();
		    out.printil("break;");
		    out.popIndent();

		    out.popIndent();
		    out.printil("} while (true);");
		}

		restoreScriptingVars(n, VariableInfo.NESTED);

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
	    if (isTagFile || isFragment) {
		out.printil("throw new javax.servlet.jsp.SkipPageException();");
	    } else {
		out.printil((methodNesting > 0)? "return true;": "return;");
	    }
	    out.popIndent();

	    // Synchronize AT_BEGIN scripting variables
	    syncScriptingVars(n, VariableInfo.AT_BEGIN);

	    // Declare and synchronize AT_END scripting variables
	    declareScriptingVars(n, VariableInfo.AT_END);
	    syncScriptingVars(n, VariableInfo.AT_END);

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

	    restoreScriptingVars(n, VariableInfo.AT_BEGIN);

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
            
            // Declare AT_BEGIN scripting variables
	    declareScriptingVars(n, VariableInfo.AT_BEGIN);
	    saveScriptingVars(n, VariableInfo.AT_BEGIN);

	    out.printin(tagHandlerClass.getName());
	    out.print(" ");
	    out.print(tagHandlerVar);
	    out.print(" = ");
	    out.print("new ");
	    out.print(tagHandlerClass.getName());
	    out.println("();");

	    generateSetters(n, tagHandlerVar, handlerInfo, true);

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

	    out.printin(tagHandlerVar);
	    out.println(".doTag();");

	    restoreScriptingVars(n, VariableInfo.AT_BEGIN);
            
	    // Synchronize AT_BEGIN scripting variables
	    syncScriptingVars(n, VariableInfo.AT_BEGIN);
            
	    // Declare and synchronize AT_END scripting variables
	    declareScriptingVars(n, VariableInfo.AT_END);
	    syncScriptingVars(n, VariableInfo.AT_END);

	    n.setEndJavaLine(out.getJavaLine());
	}

	private void declareScriptingVars(Node.CustomTag n, int scope) {
	    
	    Vector vec = n.getScriptingVars(scope);
	    if (vec != null) {
		for (int i=0; i<vec.size(); i++) {
		    Object elem = vec.elementAt(i);
		    if (elem instanceof VariableInfo) {
			VariableInfo varInfo = (VariableInfo) elem;
                        if( varInfo.getDeclare() ) {
                            out.printin(varInfo.getClassName());
                            out.print(" ");
                            out.print(varInfo.getVarName());
                            out.println(" = null;");
                        }
		    } else {
			TagVariableInfo tagVarInfo = (TagVariableInfo) elem;
                        if( tagVarInfo.getDeclare() ) {
                            String varName = tagVarInfo.getNameGiven();
                            if (varName == null) {
    			    varName = n.getTagData().getAttributeString(
                                            tagVarInfo.getNameFromAttribute());
                            }
                            out.printin(tagVarInfo.getClassName());
                            out.print(" ");
                            out.print(varName);
                            out.println(" = null;");
                        }
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
	private void saveScriptingVars(Node.CustomTag n, int scope) {
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
		    if (varInfos[i].getScope() != scope)
			continue;
		    String varName = varInfos[i].getVarName();
		    String tmpVarName = "_jspx_" + varName + "_"
			+ n.getCustomNestingLevel();
		    out.printin(tmpVarName);
		    out.print(" = ");
		    out.print(varName);
		    out.println(";");
		}
	    } else {
		for (int i=0; i<tagVarInfos.length; i++) {
		    if (tagVarInfos[i].getScope() != scope)
			continue;
		    String varName = tagVarInfos[i].getNameGiven();
		    if (varName == null) {
			varName = n.getTagData().getAttributeString(
			                tagVarInfos[i].getNameFromAttribute());
		    }
		    String tmpVarName = "_jspx_" + varName + "_"
			+ n.getCustomNestingLevel();
		    out.printin(tmpVarName);
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
	private void restoreScriptingVars(Node.CustomTag n, int scope) {
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
		    if (varInfos[i].getScope() != scope)
			continue;
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
		    if (tagVarInfos[i].getScope() != scope)
			continue;
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
	private void syncScriptingVars(Node.CustomTag n, int scope) {
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

	    // Set context
	    out.printin(tagHandlerVar);
	    if (simpleTag) {
		out.println(".setJspContext(pageContext);");
	    } else {
		out.println(".setPageContext(pageContext);");
	    }

	    // Set parent
	    if (!simpleTag) {
		if (parent != null) {
		    out.printin("if (");
		    out.print(parent);
		    out.println(" instanceof javax.servlet.jsp.tagext.SimpleTag)");
		    out.pushIndent();
		    out.printin(tagHandlerVar);
		    out.print(".setParent(");
		    out.print("new javax.servlet.jsp.tagext.TagAdapter(");
		    out.print("(javax.servlet.jsp.tagext.SimpleTag) ");
		    out.print(parent);
		    out.println("));");
		    out.popIndent();
		    out.printil("else");
		    out.pushIndent();
		    out.printin(tagHandlerVar);
		    out.print(".setParent((javax.servlet.jsp.tagext.Tag) ");
		    out.print(parent);
		    out.println(");");
		    out.popIndent();
		} else {
		    out.printin(tagHandlerVar);
		    out.print(".setParent(");
		    out.print(parent);
		    out.println(");");
		}
	    } else {
		out.printin(tagHandlerVar);
		out.print(".setParent(");
		out.print(parent);
		out.println(");");
	    }

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

		if (attrs[i].isExpression()) {
		    // Do nothing
		} else if (attrs[i].isNamedAttribute()) {
		    if (!n.checkIfAttributeIsJspFragment(attrs[i].getName())
			    && !attrs[i].isDynamic()) {
			attrValue = convertString(
                                c[0], attrValue, attrName,
				handlerInfo.getPropertyEditorClass(attrName),
				false);
		    }
		} else if (attrs[i].isELInterpreterInput()) {
                    // run attrValue through the expression interpreter
                    attrValue = JspUtil.interpreterCall(this.isTagFile,
                        attrValue, c[0], n.getPrefix(), "_jspx_fnmap" );
                } else {
		    attrValue = convertString(
                                c[0], attrValue, attrName,
				handlerInfo.getPropertyEditorClass(attrName),
				true);
		}
		
		if (attrs[i].isDynamic()) {
		    out.printin(tagHandlerVar);
		    out.print(".");
		    out.print("setDynamicAttribute(");
                    String uri = attrs[i].getURI();
                    if( "".equals( uri ) || (uri == null) ) {
                        out.print( "null" );
                    }
                    else {
                        out.print("\"" + attrs[i].getURI() + "\"");
                    }
		    out.print(", \"");
		    out.print(attrs[i].getLocalName());
		    out.print("\", ");
		    out.print(attrValue);
		    out.println(");");
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
				     Class propEditorClass, boolean quote)
	            throws JasperException {

	    String quoted = s;
	    if (quote) {
		quoted = quote(s);
	    }

	    if (propEditorClass != null) {
		return "(" + c.getName()
		    + ")org.apache.jasper.runtime.JspRuntimeLibrary.getValueFromBeanInfoPropertyEditor("
		    + c.getName() + ".class, \"" + attrName + "\", "
		    + quoted + ", "
		    + propEditorClass.getName() + ".class)";
	    } else if (c == String.class) {
		return quoted;
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
		return "new String(" + quoted + ")";
	    } else {
		return "(" + c.getName()
		    + ")org.apache.jasper.runtime.JspRuntimeLibrary.getValueFromPropertyEditorManager("
		    + c.getName() + ".class, \"" + attrName + "\", "
		    + quoted + ")";
	    }
	}   

	/*
	 * Converts the scope string representation, whose possible values
	 * are "page", "request", "session", and "application", to the
	 * corresponding scope constant.
	 */
	private String getScopeConstant(String scope) {
	    String scopeName = "PageContext.PAGE_SCOPE"; // Default to page

	    if ("request".equals(scope)) {
		scopeName = "PageContext.REQUEST_SCOPE";
	    } else if ("session".equals(scope)) {
		scopeName = "PageContext.SESSION_SCOPE";
	    } else if ("application".equals(scope)) {
		scopeName = "PageContext.APPLICATION_SCOPE";
	    }

	    return scopeName;
	}

	/**
	 * Generates anonymous JspFragment inner class which is passed as an
	 * argument to SimpleTag.setJspBody().
	 */
	private void generateJspFragment(Node n, String tagHandlerVar) 
            throws JasperException
        {
            // XXX - A possible optimization here would be to check to see
            // if the only child of the parent node is TemplateText.  If so,
            // we know there won't be any parameters, etc, so we can 
            // generate a low-overhead JspFragment that just echoes its
            // body.  The implementation of this fragment can come from
            // the org.apache.jasper.runtime package as a support class.
            FragmentHelperClass.Fragment fragment = 
                fragmentHelperClass.openFragment(n, tagHandlerVar,
						 methodNesting);
            ServletWriter outSave = out;
	    out = fragment.getMethodsBuffer().getOut();
	    String tmpParent = parent;
	    parent = tagHandlerVar;
	    boolean tmpIsFragment = isFragment;
	    isFragment = true;
            visitBody( n );
            out = outSave;
	    parent = tmpParent;
	    isFragment = tmpIsFragment;
	    fragmentHelperClass.closeFragment(fragment, methodNesting);
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
        
        if (ci.hasUseBean()) {
            out.printil("HttpSession session = pageContext.getSession();");
            out.printil("ServletContext application = pageContext.getServletContext();");
        }
        if (ci.hasUseBean() || ci.hasIncludeAction() || ci.hasSetProperty()) {
            out.printil("HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();");
        }
        if (ci.hasIncludeAction()) {
            out.printil("HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();");
        }
    }
    
    /**
     * Common part of postamble, shared by both servlets and tag files.
     */
    private void genCommonPostamble() {
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
     * Generates the ending part of the static portion of the servlet.
     */
    private void generatePostamble(Node.Nodes page) {
        out.popIndent();
        out.printil("} catch (Throwable t) {");
        out.pushIndent();
	out.printil("if (!(t instanceof javax.servlet.jsp.SkipPageException)){");
        out.pushIndent();
        out.printil("out = _jspx_out;");
        out.printil("if (out != null && out.getBufferSize() != 0)");
        out.pushIndent();
        out.printil("out.clearBuffer();");
        out.popIndent();

        out.printil("if (pageContext != null) pageContext.handlePageException(t);");
        out.popIndent();
	out.printil("}");
        out.popIndent();
        out.printil("} finally {");
        out.pushIndent();

        out.printil("if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);");

        out.popIndent();
        out.printil("}");

        // Close the service method
        out.popIndent();
        out.printil("}");

        // Generated methods, helper classes, etc.
        genCommonPostamble();
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
	if (gen.ctxt.isTagFile()) {
	    TagInfo tagInfo = gen.ctxt.getTagInfo();
	    gen.generateTagHandlerPreamble(tagInfo, page);

	    if (gen.ctxt.isPrototypeMode()) {
		return;
	    }

	    gen.fragmentHelperClass.generatePreamble();
	    page.visit(gen.new GenerateVisitor(gen.ctxt.isTagFile(),
					       out,
					       gen.methodsBuffer,
					       gen.fragmentHelperClass,
					       gen.ctxt.getClassLoader(),
					       tagInfo));
	    gen.generateTagHandlerPostamble(  tagInfo );
	} else {
	    gen.generatePreamble(page);
	    gen.fragmentHelperClass.generatePreamble();
	    page.visit(gen.new GenerateVisitor(gen.ctxt.isTagFile(),
					       out,
					       gen.methodsBuffer, 
					       gen.fragmentHelperClass,
					       gen.ctxt.getClassLoader(),
					       null));
	    gen.generatePostamble(page);
	}
    }

    /*
     * XXX
     */
    private void generateTagHandlerPreamble(TagInfo tagInfo, Node.Nodes tag )
        throws JasperException 
    {

	// Generate package declaration
	String className = tagInfo.getTagClassName();
	if (className.indexOf('.') != -1) {
	    String pkgName
		= className.substring(0, className.lastIndexOf("."));
            genPreamblePackage( pkgName );
	}

	// Generate imports
        genPreambleImports();

	// Generate class declaration
	out.printin("public final class ");
	out.print(tagInfo.getTagName());
	out.println(" extends javax.servlet.jsp.tagext.SimpleTagSupport");
	out.printil("    implements javax.servlet.jsp.el.FunctionMapper, ");
	out.printin("               org.apache.jasper.runtime.JspSourceDependent");
	if (tagInfo.hasDynamicAttributes()) {
	    out.println(",");
	    out.printin("                 javax.servlet.jsp.tagext.DynamicAttributes");
	}
	out.println(" {");
	out.println();
	out.pushIndent();
	
	/*
	 * Class body begins here
	 */

	// Declare parameter map for fragment/body invocation. This must be
	// declared as an instance variable (as opposed to a local variable in
	// doTag()), so that it is accessible:
	//
	// - from the JspFragmentHelper subclass specific to the automatically
	//   generated tag handler, in case the fragment/body invocation is
	//   contained in a fragment body, as in:
	//     <my:simple>
	//       <jsp:invoke fragment="frag"/>
	//     </my:simple>
	//
	// - from the invocation of a classic tag handler that is separated out
        //   into its own method, if the fragment/body invocation is
	//   encapsulated in a custom action, as in:
	//     <my:classic>
	//       <jsp:invoke fragment="frag"/>
	//     </my:classic>
	out.printil("private java.util.Map _jspx_params = null;");

	generateDeclarations(tag);

	// Static initializations here
        genPreambleStaticInitializers();

 	// Class variable declarations
        genPreambleClassVariableDeclarations( tagInfo.getTagName() );
        
        // Tag-handler specific declarations:
	generateTagHandlerDeclarations(tagInfo);

	if (tagInfo.hasDynamicAttributes())
	    generateSetDynamicAttribute();

	// Methods here
        genPreambleMethods();
        
        // Now the doTag() method
	out.printil("public void doTag() throws javax.servlet.jsp.JspException, java.io.IOException {");

	if (ctxt.isPrototypeMode()) {
	    out.printil("}");
	    out.popIndent();
	    out.printil("}");
	    return;
	}

	out.pushIndent();
	out.printil("PageContext pageContext = (PageContext)jspContext;");
        
        // Declare implicit objects.  
        // XXX - Note that the current JSP 2.0 PFD 
        // spec is unclear about whether these are required
        // XXX - Optimization: Check scriptlets and expressions for the
        // use of any of these.  They're not likely to be used.  If they're
        // not used, get rid of them.
        out.printil( "javax.servlet.ServletRequest request = " +
            "pageContext.getRequest();" );
        out.printil( "javax.servlet.ServletResponse response = " +
            "pageContext.getResponse();" );
        out.printil( "javax.servlet.http.HttpSession session = " +
            "pageContext.getSession();" );
        out.printil( "javax.servlet.ServletContext application = " +
            "pageContext.getServletContext();" );
	out.printil("javax.servlet.ServletConfig config = " +
            "pageContext.getServletConfig();");
        
	// Declare writer used for storing result of fragment/body invocation
	// if 'varReader' or 'var' attribute is specified
	out.printil("java.io.Writer _jspx_sout = null;");

	out.printil("javax.servlet.jsp.JspWriter out = jspContext.getOut();");
	generatePageScopedVariables(tagInfo);
        
     	// Number of tag object that need to be popped
	// XXX TODO: use a better criteria
	maxTagNesting = pageInfo.getMaxTagNesting();
        
	declareTemporaryScriptingVars(tag);
	out.println();
        
	out.printil("try {");
	out.pushIndent();
    }

    private void generateTagHandlerPostamble( TagInfo tagInfo ) {
        // Note: Before this point, the page author must have updated the
        // scoped variables with the synced versions.  We now transfer any
        // of those scoped variables that are in the locally-scoped page
        // context to the "real" page context of the calling code.
        out.printil( "// Sync up variables with caller's page context:" );
        
        TagVariableInfo[] tagVariableInfo = tagInfo.getTagVariableInfos();
        for( int i = 0; tagVariableInfo != null
		 && i < tagVariableInfo.length; i++ ) {
            // XXX - Spec bug: Note, we don't know the value of 
            // this attribute at translation time, because we're defining
            // the tag, and we don't know how page authors will call it.
            // Instead, we make a best guess at runtime of what the
            // name of the attribute is.  There are lots of potential 
            // problems with this approach and this implementation, but
            // we're expecting the spec to change.
            if( ( tagVariableInfo[i].getScope() == VariableInfo.AT_BEGIN ) ||
                ( tagVariableInfo[i].getScope() == VariableInfo.AT_END ) )
            {
                String var = tagVariableInfo[i].getNameFromAttribute();
                if( var == null ) {
                    var = "\"" + tagVariableInfo[i].getNameGiven() + "\"";
                }
                out.printil( "if( jspContext.getAttributesScope( " + var + 
                    " ) == JspContext.PAGE_SCOPE ) {" );
                out.pushIndent();
                out.printil( "super.jspContext.setAttribute( " + var + 
                    ", jspContext.getAttribute( " + var + " ) );" );
                out.popIndent();
                out.printil( "}" );
            }
        }
        
        out.popIndent();
        
        // Have to catch Throwable because a classic tag handler
        // helper method is declared to throw Throwable.
        out.printil( "} catch( Throwable t ) {" );
        out.pushIndent();
        out.printil( "if( t instanceof javax.servlet.jsp.SkipPageException )" );
        out.printil( "    throw (javax.servlet.jsp.SkipPageException) t;" );
        out.printil( "if( t instanceof java.io.IOException )" );
        out.printil( "    throw (java.io.IOException) t;" );
        out.printil( "if( t instanceof javax.servlet.jsp.JspException )" );
        out.printil( "    throw (javax.servlet.jsp.JspException) t;" );
        out.printil("throw new javax.servlet.jsp.JspException(t);" );
        out.popIndent();
        out.printil( "}" );
        
        // Close the doTag method
        out.popIndent();
        out.printil("}");

        // Generated methods, helper classes, etc.
        genCommonPostamble();
    }

    /**
     * Generates declarations for tag handler attributes, and defines the
     * getter and setter methods for each.
     */
    private void generateTagHandlerDeclarations(TagInfo tagInfo)
	        throws JasperException {

	if (tagInfo.hasDynamicAttributes()) {
	    out.printil("java.util.HashMap dynamicAttrs = new java.util.HashMap();");
	}

	TagAttributeInfo[] attrInfos = tagInfo.getAttributes();

	// Declare attributes
	if (attrInfos != null) {
	    for (int i=0; i<attrInfos.length; i++) {
		out.printin("private ");
		if (attrInfos[i].isFragment()) {
		    out.print("javax.servlet.jsp.tagext.JspFragment ");
		} else {
		    out.print(attrInfos[i].getTypeName());
		    out.print(" ");
		}
		out.print(attrInfos[i].getName());
		out.println(";");
	    }
	    out.println();
	}

	// Define attribute getter and setter methods
	if (attrInfos != null) {
	    for (int i=0; i<attrInfos.length; i++) {
		// getter method
		out.printin("public ");
		if (attrInfos[i].isFragment()) {
		    out.print("javax.servlet.jsp.tagext.JspFragment ");
		} else {
		    out.print(attrInfos[i].getTypeName());
		    out.print(" ");
		}
		out.print(toGetterMethod(attrInfos[i].getName()));
		out.println(" {");
		out.pushIndent();
		out.printin("return this.");
		out.print(attrInfos[i].getName());
		out.println(";");
		out.popIndent();
		out.printil("}");
		out.println();

		// setter method
		out.printin("public void ");
		out.print(toSetterMethodName(attrInfos[i].getName()));
		if (attrInfos[i].isFragment()) {
		    out.print("(javax.servlet.jsp.tagext.JspFragment ");
		} else {
		    out.print("(");
		    out.print(attrInfos[i].getTypeName());
		    out.print(" ");
		}
		out.print(attrInfos[i].getName());
		out.println(") {");
		out.pushIndent();
		out.printin("this.");
		out.print(attrInfos[i].getName());
		out.print(" = ");
		out.print(attrInfos[i].getName());
		out.println(";");
		out.popIndent();
		out.printil("}");
		out.println();
	    }
	}
        
        // Define setter for JspContext so we can create a wrapper and
        // store both the original and the wrapper.  We need the wrapper
        // to maask the page context from the tag file and simulate a 
        // fresh page context.  We need the original to do things like
        // sync AT_BEGIN and AT_END scripting variables.
        out.printil( "protected JspContext jspContext;" );
        out.println();
        out.printil( "public void setJspContext( JspContext ctx ) {" );
        out.pushIndent();
        out.printil( "super.setJspContext( ctx );" );
        out.printil( "this.jspContext = new org.apache.jasper.runtime.JspContextWrapper( ctx );" );
        out.popIndent();
        out.printil( "}" );
        out.println();
        out.printil( "public JspContext getJspContext() {" );
        out.pushIndent();
        out.printil( "return this.jspContext;" );
        out.popIndent();
        out.printil( "}" );
    }

    /*
     * Generates implementation of
     * javax.servlet.jsp.tagext.DynamicAttributes.setDynamicAttribute() method,
     * which saves each dynamic attribute that is passed in so that a scoped
     * variable can later be created for it.
     */
    public void generateSetDynamicAttribute() {
        out.printil("public void setDynamicAttribute(String uri, String localName, Object value) throws javax.servlet.jsp.JspException {");
	out.pushIndent();
	out.printil("if (uri != null)");
	out.pushIndent();
	// XXX Specification still needs to clarify what the variable name
	// looks like. Assume <uri>_<localName> for now.
	out.printil("dynamicAttrs.put(uri + \"_\" + localName, value);");
	out.popIndent();
	out.printil("else");
	out.pushIndent();
	out.printil("dynamicAttrs.put(localName, value);");
	out.popIndent();
	out.popIndent();
	out.printil("}");
    }

    /*
     * Creates a page-scoped variable for each declared tag attribute.
     * Also, if the tag accepts dynamic attributes, a page-scoped variable
     * is made available for each dynamic attribute that was passed in.
     */
    private void generatePageScopedVariables(TagInfo tagInfo) {

	// "normal" attributes
	TagAttributeInfo[] attrInfos = tagInfo.getAttributes();
	if (attrInfos != null) {
	    for (int i=0; i<attrInfos.length; i++) {
		String attrName = attrInfos[i].getName();
                out.printil("if( " + toGetterMethod(attrName) + " != null ) " );
		out.pushIndent();
                out.printin("pageContext.setAttribute(");
		out.print(quote(attrName));
		out.print(", ");
		out.print(toGetterMethod(attrName));
		out.println(");");
		out.popIndent();
	    }
	}

	// dynamic attributes
	if (tagInfo.hasDynamicAttributes()) {
	    out.printil("for (java.util.Iterator i = dynamicAttrs.entrySet().iterator(); i.hasNext(); ) {");
	    out.pushIndent();
	    out.printil("java.util.Map.Entry e = (java.util.Map.Entry) i.next();");
	    out.printil("pageContext.setAttribute((String) e.getKey(), e.getValue());");
	    out.popIndent();
	    out.printil("}");
	}
    }

    /*
     * Generates the getter method for the given attribute name.
     */
    private String toGetterMethod(String attrName) {
	char[] attrChars = attrName.toCharArray();
	attrChars[0] = Character.toUpperCase(attrChars[0]);
	return "get" + new String(attrChars) + "()";
    }

    /*
     * Generates the setter method name for the given attribute name.
     */
    private String toSetterMethodName(String attrName) {
	char[] attrChars = attrName.toCharArray();
	attrChars[0] = Character.toUpperCase(attrChars[0]);
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
	    out.printil( "private class " + className );
            out.printil( "    extends " +
                "org.apache.jasper.runtime.JspFragmentHelper" );
            out.printil( "{" );
            out.pushIndent();
	    out.printil("private javax.servlet.jsp.tagext.JspTag parentTag;");
	    out.println();
            out.printil( "public " + className + 
                "( int discriminator, JspContext jspContext, " +
                "javax.servlet.jsp.tagext.JspTag parentTag ) {" );
            out.pushIndent();
            out.printil( "super( discriminator, jspContext, parentTag );" );
            out.printil( "this.parentTag = parentTag;" );
            out.popIndent();
            out.printil( "}" );
        }
        
	public Fragment openFragment(Node parent, String tagHandlerVar,
				     int methodNesting) 
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
	    if (methodNesting > 0) {
		out.printin("public boolean invoke");
	    } else {
		out.printin("public void invoke");
	    }
	    out.println(result.getId() + "( " +
			"java.io.Writer out, java.util.Map params ) " );
            out.pushIndent();
            // Note: Throwable required because methods like _jspx_meth_*
            // throw Throwable.
            out.printil( "throws Throwable" );
            out.popIndent();
            out.printil( "{" );
            out.pushIndent();
            generateLocalVariables( out, parent );
	    out.printin("javax.servlet.jsp.tagext.JspTag ");
	    out.print(tagHandlerVar);
	    out.println(" = parentTag;");
            
            return result;
        }
        
        public void closeFragment( Fragment fragment, int methodNesting ) {
            ServletWriter out = fragment.getMethodsBuffer().getOut();
            // XXX - See comment in openFragment()
	    if (methodNesting > 0) {
		out.printil( "return false;" );
	    } else {
		out.printil("return;");
	    }
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
            out.printil( "public void invoke( java.io.Writer writer, " +
			 "java.util.Map params )" );
            out.pushIndent();
            out.printil( "throws javax.servlet.jsp.JspException" );
            out.popIndent();
            out.printil( "{" );
            out.pushIndent();
            out.printil( "java.util.Map _jspx_originalValues = null;" );
            out.printil( "if( params != null ) {" );
            out.pushIndent();
            out.printil( "_jspx_originalValues = preparePageScope( params );");
            out.popIndent();
            out.printil( "}" );
	    out.printil( "java.io.Writer out = null;" );
            out.printil( "if( writer != null ) {" );
            out.pushIndent();
	    out.printil( "out = this.jspContext.pushBody(writer);" );
            out.popIndent();
            out.printil( "} else {" );
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
            out.printil("if (e instanceof javax.servlet.jsp.SkipPageException)");
            out.printil("    throw (javax.servlet.jsp.SkipPageException) e;");
            out.printil( "throw new javax.servlet.jsp.JspException( e );" );
            out.popIndent();
            out.printil( "}" ); // catch
            out.printil( "finally {" );
            out.pushIndent();

            out.printil( "if( writer != null ) {" );
            out.pushIndent();
            out.printil( "this.jspContext.popBody();");
            out.popIndent();
            out.printil( "}" );
	    
            out.printil( "if( params != null ) {" );
            out.pushIndent();
            out.printil( "restorePageScope( _jspx_originalValues );");
            out.popIndent();
            out.printil( "}" );
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

