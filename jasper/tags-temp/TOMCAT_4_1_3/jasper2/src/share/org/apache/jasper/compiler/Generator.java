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
 * @author Denis Benoit
 */

public class Generator {

    private ServletWriter out;
    private ErrorDispatcher err;
    private BeanRepository beanInfo;
    private JspCompilationContext ctxt;
    private boolean breakAtLF;
    private PageInfo pageInfo;
    private int maxTagNesting;

    /**
     * @param s the input string
     * @return quoted and escaped string, per Java rule
     */
    private static String quote(String s) {

	if (s == null)
	    return "null";

	StringBuffer b = new StringBuffer();
	b.append('"');
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
	b.append('"');
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

	// Class body begins here

	out.pushIndent();
	generateDeclarations(page);
	out.println();

	// Static initializations here

        // Static data for getIncludes()
        out.printil("private static java.util.Vector _jspx_includes;");
        out.println();  
        out.println();
        List includes = pageInfo.getIncludes();
        iter = includes.iterator();
        if( !includes.isEmpty() ) {
            out.printil("static {");
            out.pushIndent();
            out.printin(
                "_jspx_includes = new java.util.Vector(");
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
            out.println();
        }

 	// Class fields declarations
     	
        maxTagNesting = pageInfo.getMaxTagNesting();
        if (maxTagNesting >= 0) {
            out.printil("private static final int RELEASE_ACTION         = 0;");
            out.printil("private static final int POP_AND_RELEASE_ACTION = 1;");
            out.println();
            out.println();
        }

	// Constructor (empty so far) here

	// Methods here

	// Method used to get compile time include file dependencies
        out.printil("public java.util.List getIncludes() {");
        out.pushIndent();
        out.printil("return _jspx_includes;");
        out.popIndent();
        out.printil("}");
        out.println();
        out.println();

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

     	// pseudo "Finally" state stack objects
        if (maxTagNesting >= 0) {
            String depth = Integer.toString(maxTagNesting + 1);
            out.printil("int   tagStackIndex = -1;");
            out.printin("int[] tagStackActions = new int[");
            out.print(depth);
            out.println("];");
            out.printin("javax.servlet.jsp.tagext.Tag[] tagStack = new javax.servlet.jsp.tagext.Tag[");
            out.print(depth);
            out.println("];");
            out.println();
        }

	out.printil("try {");
	out.pushIndent();

	out.printil("_jspxFactory = JspFactory.getDefaultFactory();");

	out.printin("response.setContentType(");
	out.print  (quote(pageInfo.getContentType()));
	out.println(");");

	out.printil("pageContext = _jspxFactory.getPageContext(" +
		    "this, request, response,");
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
	out.println();
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

	/**
	 * Constructor.
	 */
	public GenerateVisitor() {
	    handlerInfos = new Hashtable();
	    tagVarNumbers = new Hashtable();
	}

	/**
	 * Returns an attribute value, optionally URL encoded.  If
	 * the value is a runtime expression, the result is the string for
	 * the expression, otherwise the result is the string literal,
	 * quoted and escaped.
	 * @param attr An JspAttribute object
	 * @param encode true if to be URL encoded
	 */
	private String attributeValue(Node.JspAttribute attr, boolean encode) {
	    String v = attr.getValue();
	    if (attr.isExpression()) {
		if (encode) {
		    return "java.net.URLEncoder.encode(" + v + ")";
		}
		return v;
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
	private void printParams(Node n) throws JasperException {

	    class ParamVisitor extends Node.Visitor {
		char separator = '?';

		public void visit(Node.ParamAction n) throws JasperException {

		    out.print(" + \"");
		    out.print(separator);
		    out.print(n.getAttributeValue("name"));
		    out.print("=\" + ");
		    out.print(attributeValue(n.getValue(), true));

		    // The separator is '&' after the second use
		    separator = '&';
		}
	    }

	    if (n.getBody() != null) {
		n.getBody().visit(new ParamVisitor());
	    }
	}

        public void visit(Node.Expression n) throws JasperException {
	    n.setBeginJavaLine(out.getJavaLine());
	    out.printil("out.print(" + new String(n.getText()) + ");");
	    n.setEndJavaLine(out.getJavaLine());
        }

	public void visit(Node.Scriptlet n) throws JasperException {
	    n.setBeginJavaLine(out.getJavaLine());
	    out.printMultiLn(new String(n.getText()));
	    n.setEndJavaLine(out.getJavaLine());
	}

	public void visit(Node.IncludeAction n) throws JasperException {

	    String flush = n.getAttributeValue("flush");

	    boolean isFlush = false;	// default to false;
	    if ("true".equals(flush))
		isFlush = true;

	    n.setBeginJavaLine(out.getJavaLine());

	    out.printin("JspRuntimeLibrary.include(request, response, ");
	    out.print(attributeValue(n.getPage(), false));
	    printParams(n);
	    out.println(", out, " + isFlush + ");");

	    n.setEndJavaLine(out.getJavaLine());
        }

	public void visit(Node.ForwardAction n) throws JasperException {
	    String page = n.getAttributeValue("page");

	    n.setBeginJavaLine(out.getJavaLine());

	    out.printil("if (true) {");	// So that javac won't complain about
	    out.pushIndent();		// codes after "return"
	    out.printin("pageContext.forward(");
	    out.print  (attributeValue(n.getPage(), false));
	    printParams(n);
	    out.println(");");
	    out.printil("return;");
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
		out.printil("out.print(JspRuntimeLibrary.toString(" +
			    "(((" + beanName + ")pageContext.findAttribute(" +
			    "\"" + name + "\"))." + methodName + "())));");
	    } else {
		// The object could be a custom action with an associated
		// VariableInfo entry for this name.
		// Get the class name and then introspect at runtime.
		out.printil("out.print(JspRuntimeLibrary.toString" +
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
		out.print(attributeValue(value, false));
		out.println(");");
	    } else {
		out.printil("JspRuntimeLibrary.introspecthelper(" +
			    "pageContext.findAttribute(\"" + name + "\"), \"" +
			    property + "\",");
		out.print(attributeValue(value, false));
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
		    className = attributeValue(beanName, false);
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

		    String s0 = makeAttr("name", name) + " value=" +
			        attributeValue(n.getValue(), false);

		    if (ie) {
			s0 = "<PARAM" + s0 + '>';
		    }

		    n.setBeginJavaLine(out.getJavaLine());
		    out.printil("out.println(" + quote(s0) + ");");
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

	    if (iepluginurl == null)
		iepluginurl = Constants.IE_PLUGIN_URL;
	    if (nspluginurl == null)
		nspluginurl = Constants.NS_PLUGIN_URL;


	    n.setBeginJavaLine(out.getJavaLine());
	    // IE style plugin
	    // <OBJECT ...>
	    // First compose the runtime output string 
	    String s0 = "<OBJECT classid=\"" + ctxt.getOptions().getIeClassId()+
			"\"" + makeAttr("name", name);
	    String s1, s2;
	    if (width.isExpression()) {
		s1 = quote(s0 + " width=\"") + " + " + width.getValue() +
			" + " + quote("\"");
	    } else {
		s1 = quote(s0 + makeAttr("width", width.getValue()));
	    }
	    if (height.isExpression()) {
		s2 = quote(" height=\"") + " + " + height.getValue() +
			" + " + quote("\"");
	    } else {
		s2 = quote(makeAttr("height", height.getValue()));
	    }
	    String s3 = quote(makeAttr("hspace", hspace) +
				makeAttr("vspace", vspace) +
				makeAttr("align", align) +
				makeAttr("codebase", iepluginurl) +
				'>');
	    // Then print the output string to the java file
	    out.printil("out.println(" + s1 + " + " + s2 + " + " + s3 + ");");

	    // <PARAM > for java_code
	    s0 = "<PARAM name=\"java_code\"" + makeAttr("value", code) + '>';
	    out.printil("out.println(" + quote(s0) + ");");

	    // <PARAM > for java_codebase
	    if (codebase != null) {
		s0 = "<PARAM name=\"java_codebase\"" + 
		     makeAttr("value", codebase) +
		     '>';
		out.printil("out.println(" + quote(s0) + ");");
	    }

	    // <PARAM > for java_archive
	    if (archive != null) {
		s0 = "<PARAM name=\"java_archive\"" +
		     makeAttr("value", archive) +
		     '>';
		out.printil("out.println(" + quote(s0) + ");");
	    }

	    // <PARAM > for type
	    s0 = "<PARAM name=\"type\"" +
		 makeAttr("value", "application/x-java-" + type + ";" +
			  ((jreversion==null)? "": "version=" + jreversion)) +
		 '>';
	    out.printil("out.println(" + quote(s0) + ");");

	    /*
	     * generate a <PARAM> for each <jsp:param> in the plugin body
	     */
	    if (n.getBody() != null)
		n.getBody().visit(new ParamVisitor(true));

	    /*
	     * Netscape style plugin part
	     */
	    out.printil("out.println(" + quote("<COMMENT>") + ");");
	    s0 = "<EMBED" +
		 makeAttr("type", "application/x-java-" + type + ";" +
			  ((jreversion==null)? "": "version=" + jreversion)) +
		 makeAttr("name", name);
	    if (width.isExpression()) {
		s1 = quote(s0 + " width=\"") + " + " + width.getValue() +
			" + " + quote("\"");
	    } else {
		s1 = quote(s0 + makeAttr("width", width.getValue()));
	    }
	    if (height.isExpression()) {
		s2 = quote(" height=\"") + " + " + height.getValue() +
			" + " + quote("\"");
	    } else {
		s2 = quote(makeAttr("height", height.getValue()));
	    }
	    s3 = quote(makeAttr("hspace", hspace) +
			 makeAttr("vspace", vspace) +
			 makeAttr("align", align) +
			 makeAttr("pluginspage", nspluginurl) +
			 makeAttr("java_code", code) +
			 makeAttr("java_codebase", codebase) +
			 makeAttr("java_archive", archive));
	    out.printil("out.println(" + s1 + " + " + s2 + " + " + s3 + ");");
		 
	    /*
	     * Generate a 'attr = "value"' for each <jsp:param> in plugin body
	     */
	    if (n.getBody() != null)
		n.getBody().visit(new ParamVisitor(false)); 

	    out.printil("out.println(" + quote(">") + ");");

	    out.printil("out.println(" + quote("<NOEMBED>") + ");");
	    out.printil("out.println(" + quote("</COMMENT>") + ");");

	    /*
	     * Fallback
	     */
	    if (n.getBody() != null) {
		n.getBody().visit(new Node.Visitor() {
		    public void visit(Node.FallBackAction n) {
			n.setBeginJavaLine(out.getJavaLine());
			out.printil("out.println(" +
				    quote(new String(n.getText())) + ");");
			n.setEndJavaLine(out.getJavaLine());
		    }
		});
	    }

	    out.printil("out.println(" + quote("</NOEMBED></EMBED>") + ");");
	    out.printil("out.println(" + quote("</OBJECT>") + ");");

	    n.setEndJavaLine(out.getJavaLine());
	}

        public void visit(Node.CustomTag n) throws JasperException {

	    TagLibraryInfo tagLibInfo = (TagLibraryInfo)
		pageInfo.getTagLibraries().get(n.getPrefix());
	    TagInfo tagInfo = tagLibInfo.getTag(n.getShortName());

	    // Get info on scripting variables created/manipulated by tag
	    VariableInfo[] varInfos = tagInfo.getVariableInfo(n.getTagData());
	    TagVariableInfo[] tagVarInfos = tagInfo.getTagVariableInfos();

	    Hashtable handlerInfosByShortName
		= (Hashtable) handlerInfos.get(n.getPrefix());
	    if (handlerInfosByShortName == null) {
		handlerInfosByShortName = new Hashtable();
		handlerInfos.put(n.getPrefix(), handlerInfosByShortName);
	    }
	    TagHandlerInfo handlerInfo = (TagHandlerInfo)
		handlerInfosByShortName.get(n.getShortName());
	    if (handlerInfo == null) {
		handlerInfo = new TagHandlerInfo(n, tagInfo.getTagClassName(),
						 ctxt.getClassLoader(), err);
		handlerInfosByShortName.put(n.getShortName(), handlerInfo);
	    }

	    // Create variable names
	    String baseVar = createTagVarName(n.getName(), n.getPrefix(),
					      n.getShortName());
	    String tagEvalVar = "_jspx_eval_" + baseVar;
	    String tagHandlerVar = "_jspx_th_" + baseVar;

	    // Generate code for start tag, body, and end tag
	    generateCustomStart(n, varInfos, tagVarInfos, handlerInfo,
				tagHandlerVar, tagEvalVar);
	    String tmpParent = parent;
	    parent = tagHandlerVar;
	    visitBody(n);
	    parent = tmpParent;
	    generateCustomEnd(n, varInfos, tagVarInfos,
			      handlerInfo.getTagHandlerClass(), tagHandlerVar,
			      tagEvalVar);
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

	private void generateCustomStart(Node.CustomTag n,
					 VariableInfo[] varInfos,
					 TagVariableInfo[] tagVarInfos,
					 TagHandlerInfo handlerInfo,
					 String tagHandlerVar,
					 String tagEvalVar)
	                    throws JasperException {

	    n.setBeginJavaLine(out.getJavaLine());
	    out.printin("/* ----  ");
	    out.print(n.getName());
	    out.println(" ---- */");

	    Class tagHandlerClass = handlerInfo.getTagHandlerClass();

            boolean implementsTryCatchFinally =
                TryCatchFinally.class.isAssignableFrom(tagHandlerClass);

	    out.printin(tagHandlerClass.getName());
	    out.print(" ");
	    out.print(tagHandlerVar);
	    out.print(" = new ");
	    out.print(tagHandlerClass.getName());
	    out.println("();");
	    generateSetters(n, tagHandlerVar, handlerInfo);
	    
            if (implementsTryCatchFinally) {
                out.printil("try {");
                out.pushIndent();
            } else {
                out.printil("tagStackActions[++tagStackIndex] = RELEASE_ACTION;");
                out.printin("tagStack[tagStackIndex] = ");
                out.print(tagHandlerVar);
                out.println(";");
            }
	    out.printin("int ");
	    out.print(tagEvalVar);
	    out.print(" = ");
	    out.print(tagHandlerVar);
	    out.println(".doStartTag();");

	    boolean isBodyTag
		= BodyTag.class.isAssignableFrom(tagHandlerClass);

	    // Declare and synchronize AT_BEGIN scripting variables
	    syncScriptingVariables(varInfos, tagVarInfos, n.getTagData(),
				   VariableInfo.AT_BEGIN, true);
 
	    if (n.getBody() != null) {
		out.printin("if (");
		out.print(tagEvalVar);
		out.println(" != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {");
		out.pushIndent();
		
		if (isBodyTag) {
		    out.printin("if (");
		    out.print(tagEvalVar);
		    out.println(" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {");
		    // Assume EVAL_BODY_BUFFERED
		    out.pushIndent();
		    
		    out.printil("out = pageContext.pushBody();");
                    if (!implementsTryCatchFinally) {
                        out.printil("tagStackActions[tagStackIndex]" +
					" = POP_AND_RELEASE_ACTION;");
 		    }
		    out.printin(tagHandlerVar);
		    out.println(".setBodyContent((javax.servlet.jsp.tagext.BodyContent) out);");
		    out.printin(tagHandlerVar);
		    out.println(".doInitBody();");
		    
		    out.popIndent();
		    out.printil("}");
		}
		
		if (IterationTag.class.isAssignableFrom(tagHandlerClass)) {
		    out.printil("do {");
		    out.pushIndent();
		}
	    }


	    // Declare and synchronize NESTED scripting variables
	    syncScriptingVariables(varInfos, tagVarInfos, n.getTagData(),
 				   VariableInfo.NESTED, true);

	    // Synchronize AT_BEGIN scripting variables
	    syncScriptingVariables(varInfos, tagVarInfos, n.getTagData(),
				   VariableInfo.AT_BEGIN, false);
	};
	
	private void generateCustomEnd(Node.CustomTag n,
				       VariableInfo[] varInfos,
				       TagVariableInfo[] tagVarInfos,
				       Class tagHandlerClass,
				       String tagHandlerVar,
				       String tagEvalVar) {

	    boolean implementsIterationTag = 
		IterationTag.class.isAssignableFrom(tagHandlerClass);
	    boolean implementsBodyTag = 
		BodyTag.class.isAssignableFrom(tagHandlerClass);
	    boolean implementsTryCatchFinally = 
		TryCatchFinally.class.isAssignableFrom(tagHandlerClass);

	    if ((n.getBody() != null) && implementsIterationTag) {
		out.popIndent();
		out.printin("} while (");
		out.print(tagHandlerVar);
		out.println(".doAfterBody() == javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN);");
	    }

	    // Synchronize AT_BEGIN scripting variables
	    syncScriptingVariables(varInfos, tagVarInfos, n.getTagData(),
				   VariableInfo.AT_BEGIN, false);

	    if (n.getBody() != null) {
		if (implementsBodyTag) {
		    out.printin("if (");
		    out.print(tagEvalVar);
		    out.println(" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)");
		    out.pushIndent();
                    if (!implementsTryCatchFinally) {
                        out.printil("tagStackActions[tagStackIndex] = RELEASE_ACTION;");
 		    }
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
	    out.printil("return;");
	    out.popIndent();

	    // TryCatchFinally
	    if (implementsTryCatchFinally) {
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
                out.printin(tagHandlerVar);
                out.println(".release();");
                out.popIndent();
                out.printil("}");
            } else {
                out.printil("tagStackIndex--;");
                out.printin(tagHandlerVar);
                out.println(".release();");
	    }

	    // Declare and synchronize AT_END variables
	    syncScriptingVariables(varInfos, tagVarInfos, n.getTagData(),
				   VariableInfo.AT_END, true);

	    n.setEndJavaLine(out.getJavaLine());
	}

	private void syncScriptingVariables(VariableInfo[] varInfos,
					    TagVariableInfo[] tagVarInfos,
					    TagData tagData,
					    int scope,
					    boolean declare) {
	    if ((varInfos == null) && (tagVarInfos == null)) {
		return;
	    }
	    if (varInfos != null) {
		for (int i=0; i<varInfos.length; i++) {
		    if (varInfos[i].getScope() == scope) {
			if (declare && varInfos[i].getDeclare()) {
			    out.printin(varInfos[i].getClassName() + " ");
			}
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
		    String name = tagVarInfos[i].getNameGiven();
		    if (name == null) {
			name = tagData.getAttributeString(
                                        tagVarInfos[i].getNameFromAttribute());
		    }
		    if (tagVarInfos[i].getScope() == scope) {
			if (declare && tagVarInfos[i].getDeclare()) {
			    out.printin(tagVarInfos[i].getClassName() + " ");
			}
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

	private String replace(String name, char replace, String with) {
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
	
	/*
	 * Creates a tag variable name by concatenating the given prefix and
	 * shortName and replacing '-' with "$1", '.' with "$2", and ':' with
	 * "$3".
	 */
	private String createTagVarName(String fullName, String prefix,
					String shortName) {
	    if (prefix.indexOf('-') >= 0)
		prefix = replace(prefix, '-', "$1");
	    if (prefix.indexOf('.') >= 0)
		prefix = replace(prefix, '.', "$2");

	    if (shortName.indexOf('-') >= 0)
		shortName = replace(shortName, '-', "$1");
	    if (shortName.indexOf('.') >= 0)
		shortName = replace(shortName, '.', "$2");
	    if (shortName.indexOf(':') >= 0)
		shortName = replace(shortName, ':', "$3");

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

	private void generateSetters(Node.CustomTag n, String tagHandlerVar,
				     TagHandlerInfo handlerInfo)
	            throws JasperException {

	    out.printin(tagHandlerVar);
	    out.println(".setPageContext(pageContext);");
	    out.printin(tagHandlerVar);
	    out.print(".setParent(");
	    out.print(parent);
	    out.println(");");

	    Node.JspAttribute[] attrs = n.getJspAttributes();
	    for (int i=0; i<attrs.length; i++) {
		String attrValue = attrs[i].getValue();
		if (attrValue == null) {
		    continue;
		}
		String attrName = attrs[i].getName();
		Method m = handlerInfo.getSetterMethod(attrName);
		if (m == null) {
		    err.jspError(n, "jsp.error.unable.to_find_method",
				 attrName);
		}

		Class c[] = m.getParameterTypes();
		// XXX assert(c.length > 0)

		if (!attrs[i].isExpression()) {
		    attrValue = convertString(c[0], attrValue, attrName,
					      handlerInfo.getPropertyEditorClass(attrName));
		}
		
		out.printin(tagHandlerVar);
		out.print(".");
		out.print(m.getName());
		out.print("(");
		out.print(attrValue);
		out.println(");");
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
    }

    /**
     * Generates the ending part of the static portion of the servelet.
     */
    private void generatePostamble(Node.Nodes page) {
        out.popIndent();
        out.printil("} catch (Throwable t) {");
        out.pushIndent();
        out.printil("if (out != null && out.getBufferSize() != 0)");
        out.pushIndent();
        out.printil("out.clearBuffer();");
        out.popIndent();
        out.printil("if (pageContext != null) pageContext.handlePageException(t);");
        out.popIndent();
        out.printil("} finally {");
        out.pushIndent();

	// Cleanup the tags on the stack
        if (maxTagNesting >= 0) {
            out.printil("while (tagStackIndex >= 0) {");
            out.pushIndent();
            out.printil("if (POP_AND_RELEASE_ACTION == tagStackActions[tagStackIndex])");
            out.pushIndent();
            out.printil("out = pageContext.popBody();");
            out.popIndent();
            out.printil("tagStack[tagStackIndex--].release();");
            out.popIndent();
            out.printil("}");
        }

        out.printil("if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);");

        out.popIndent();
        out.printil("}");

        // Close the service method
        out.popIndent();
        out.printil("}");

        // Close the class definition
        out.popIndent();
        out.printil("}");
    }

    /**
     * Constructor.
     */
    Generator(ServletWriter out, Compiler compiler) {
	this.out = out;
	err = compiler.getErrorDispatcher();
	ctxt = compiler.getCompilationContext();
	pageInfo = compiler.getPageInfo();
	beanInfo = pageInfo.getBeanRepository();
	breakAtLF = ctxt.getOptions().getMappedFile();
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

	gen.generatePreamble(page);
	page.visit(gen.new GenerateVisitor());
	gen.generatePostamble(page);
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
	 * @param n The custom tag whose tag handler is to be loaded and
	 * introspected
	 * @param tagClassName Name of tag handler class to load
	 * @param loader Class loader to use
	 * @param err Error dispatcher
	 */
	TagHandlerInfo(Node n, String tagClassName, ClassLoader loader,
		       ErrorDispatcher err) throws JasperException {

	    // Load the tag handler class with the given name
	    tagHandlerClass = null;
	    try {
		tagHandlerClass = loader.loadClass(tagClassName);
	    } catch (Exception e) {
		err.jspError(n, "jsp.error.unable.loadclass", tagClassName, e);
	    }

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
			     tagClassName, ie);
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
}

