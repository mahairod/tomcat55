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
import java.io.CharArrayWriter;
import javax.servlet.jsp.tagext.TagData;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;

/**
 * An internal data representation of a JSP page or a JSP docuement (XML).
 * Also included here is a visitor class for tranversing nodes.
 *
 * @author Kin-man Chung
 * @author Jan Luehe
 */

public abstract class Node {
    
    private Attributes attrs;
    private Nodes body;
    private char[] text;
    private Mark startMark;
    private int beginJavaLine;
    private int endJavaLine;
    protected Node parent;

    /**
     * The root node of this page.
     */
    private static Root currentRoot;

    /**
     * Constructor.
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(Mark start, Node parent) {
	this.startMark = start;
	addToParent(parent);
    }

    /**
     * Constructor.
     * @param attrs The attributes for this node
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(Attributes attrs, Mark start, Node parent) {
	this.attrs = attrs;
	this.startMark = start;
	addToParent(parent);
    }

    /*
     * Constructor.
     * @param text The text associated with this node
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(char[] text, Mark start, Node parent) {
	this.text = text;
	this.startMark = start;
	addToParent(parent);
    }

    public Attributes getAttributes() {
	return attrs;
    }

    public void setAttributes(Attributes attrs) {
	this.attrs = attrs;
    }

    public String getAttributeValue(String name) {
	return (attrs == null) ? null : attrs.getValue(name);
    }

    public Nodes getBody() {
	return body;
    }

    public void setBody(Nodes body) {
	this.body = body;
    }

    public char[] getText() {
	return text;
    }

    public Mark getStart() {
	return startMark;
    }

    public Node getParent() {
	return parent;
    }

    public Node getCurrentRoot() {
	return currentRoot;
    }

    public int getBeginJavaLine() {
	return beginJavaLine;
    }

    public void setBeginJavaLine(int begin) {
	beginJavaLine = begin;
    }

    public int getEndJavaLine() {
	return endJavaLine;
    }

    public void setEndJavaLine(int end) {
	endJavaLine = end;
    }

    /**
     * Warning: This method is valid only when used from the correct context,
     * namely when the Root node for the current page has been visited first,
     * and the proper bookkeeping (<tt>pushCurrentRoot/popCurrentRoot</tt>)
     * has been performed.
     *
     * @return true if the current page is in xml syntax, false otherwise.
     */
    public boolean isXmlSyntax() {
	return currentRoot.isXmlSyntax();
    }

    public static void pushCurrentRoot(Root root) {
	root.setParentRoot(currentRoot);
	currentRoot = root;
    }

    public static Root popCurrentRoot() {
	currentRoot = currentRoot.getParentRoot();
	return currentRoot;
    }

    /**
     * Selects and invokes a method in the visitor class based on the node
     * type.  This is abstract and should be overrode by the extending classes.
     * @param v The visitor class
     */
    abstract void accept(Visitor v) throws JasperException;


    //*********************************************************************
    // Private utility methods

    /*
     * Adds this Node to the body of the given parent.
     */
    private void addToParent(Node parent) {
	if (parent != null) {
	    this.parent = parent;
	    Nodes parentBody = parent.getBody();
	    if (parentBody == null) {
		parentBody = new Nodes();
		parent.setBody(parentBody);
	    }
	    parentBody.add(this);
	}
    }


    /*********************************************************************
     * Child classes
     */
    
    /**
     * Represents the root of a Jsp page or Jsp document
     */
    public static class Root extends Node {

	private Root parentRoot;

	Root(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public boolean isXmlSyntax() {
	    return false;
	}

	public Root getParentRoot() {
	    return parentRoot;
	}

	public void setParentRoot(Root root) {
	    parentRoot = root;
	}
    }
    
    /**
     * Represents the root of a Jsp document (XML syntax)
     */
    public static class JspRoot extends Root {

	public JspRoot(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public boolean isXmlSyntax() {
	    return true;
	}

    }

    /**
     * Represents a page directive
     */
    public static class PageDirective extends Node {

	private Vector imports;

	public PageDirective(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	    imports = new Vector();
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	/**
	 * Parses the comma-separated list of class or package names in the
	 * given attribute value and adds each component to this
	 * PageDirective's vector of imported classes and packages.
	 * @param value A comma-separated string of imports.
	 */
	public void addImport(String value) {
	    int start = 0;
	    int index;
	    while ((index = value.indexOf(',', start)) != -1) {
		imports.add(value.substring(start, index).trim());
		start = index + 1;
	    }
	    if (start == 0) {
		// No comma found
		imports.add(value.trim());
	    } else {
		imports.add(value.substring(start).trim());
	    }
	}

	public List getImports() {
	    return imports;
	}
    }

    /**
     * Represents an include directive
     */
    public static class IncludeDirective extends Node {

	public IncludeDirective(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a custom taglib directive
     */
    public static class TaglibDirective extends Node {

	public TaglibDirective(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a Jsp comment
     * Comments are kept for completeness.
     */
    public static class Comment extends Node {

	public Comment(char[] text, Mark start, Node parent) {
	    super(text, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents an expression, declaration, or scriptlet
     */
    public static abstract class ScriptingElement extends Node {

	public ScriptingElement(char[] text, Mark start, Node parent) {
	    super(text, start, parent);
	}

	public ScriptingElement(Mark start, Node parent) {
	    super(start, parent);
	}

	/**
	 * When this node was created from a JSP page in JSP syntax, its text
	 * was stored as a String in the "text" field, whereas when this node
	 * was created from a JSP document, its text was stored as one or more
	 * TemplateText nodes in its body. This method handles either case.
	 * @return The text string
	 */
	public char[] getText() {
	    char[] ret = text;
	    if ((ret == null) && (body != null)) {
		CharArrayWriter chars = new CharArrayWriter();
		int size = body.size();
		for (int i=0; i<size; i++) {
		    chars.write(body.getNode(i).getText(), 0,
				body.getNode(i).getText().length);
		}
		ret = chars.toCharArray();
	    }
	    return ret;
	}
    }

    /**
     * Represents a declaration
     */
    public static class Declaration extends ScriptingElement {

	public Declaration(char[] text, Mark start, Node parent) {
	    super(text, start, parent);
	}

	public Declaration(Mark start, Node parent) {
	    super(start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents an expression.  Expressions in attributes are embedded
     * in the attribute string and not here.
     */
    public static class Expression extends ScriptingElement {

	public Expression(char[] text, Mark start, Node parent) {
	    super(text, start, parent);
	}

	public Expression(Mark start, Node parent) {
	    super(start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a scriptlet
     */
    public static class Scriptlet extends ScriptingElement {

	public Scriptlet(char[] text, Mark start, Node parent) {
	    super(text, start, parent);
	}

	public Scriptlet(Mark start, Node parent) {
	    super(start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a param action
     */
    public static class ParamAction extends Node {

	JspAttribute value;

	public ParamAction(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setValue(JspAttribute value) {
	    this.value = value;
	}

	public JspAttribute getValue() {
	    return value;
	}
    }

    /**
     * Represents a params action
     */
    public static class ParamsAction extends Node {

	public ParamsAction(Mark start, Node parent) {
	    super(start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a fallback action
     */
    public static class FallBackAction extends Node {

	public FallBackAction(Mark start, char[] text, Node parent) {
	    super(text, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents an include action
     */
    public static class IncludeAction extends Node {

	private JspAttribute page;

	public IncludeAction(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setPage(JspAttribute page) {
	    this.page = page;
	}

	public JspAttribute getPage() {
	    return page;
	}
    }

    /**
     * Represents a forward action
     */
    public static class ForwardAction extends Node {

	private JspAttribute page;

	public ForwardAction(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setPage(JspAttribute page) {
	    this.page = page;
	}

	public JspAttribute getPage() {
	    return page;
	}
    }

    /**
     * Represents a getProperty action
     */
    public static class GetProperty extends Node {

	public GetProperty(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a setProperty action
     */
    public static class SetProperty extends Node {

	private JspAttribute value;

	public SetProperty(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setValue(JspAttribute value) {
	    this.value = value;
	}

	public JspAttribute getValue() {
	    return value;
	}
    }

    /**
     * Represents a useBean action
     */
    public static class UseBean extends Node {

	JspAttribute beanName;

	public UseBean(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setBeanName(JspAttribute beanName) {
	    this.beanName = beanName;
	}

	public JspAttribute getBeanName() {
	    return beanName;
	}
    }

    /**
     * Represents a plugin action
     */
    public static class PlugIn extends Node {

	public PlugIn(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents an uninterpreted tag, from a Jsp document
     */
    public static class UninterpretedTag extends Node {
	private String tagName;

	public UninterpretedTag(Attributes attrs, Mark start, String name,
				Node parent) {
	    super(attrs, start, parent);
	    tagName = name;
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public String getName() {
	    return tagName;
	}
    }

    /**
     * Represents a custom tag
     */
    public static class CustomTag extends Node {
	private String name;
	private String prefix;
	private String shortName;
	private JspAttribute[] jspAttrs;
	private TagData tagData;

	public CustomTag(Attributes attrs, Mark start, String name,
			 String prefix, String shortName, Node parent) {
	    super(attrs, start, parent);
	    this.name = name;
	    this.prefix = prefix;
	    this.shortName = shortName;
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	/**
	 * @return The full tag name
	 */
	public String getName() {
	    return name;
	}

	/**
	 * @return The tag prefix
	 */
	public String getPrefix() {
	    return prefix;
	}

	/**
	 * @return The tag name without prefix
	 */
	public String getShortName() {
	    return shortName;
	}

	public void setJspAttributes(JspAttribute[] jspAttrs) {
	    this.jspAttrs = jspAttrs;
	}

	public JspAttribute[] getJspAttributes() {
	    return jspAttrs;
	}
	
	public void setTagData(TagData tagData) {
	    this.tagData = tagData;
	}

	public TagData getTagData() {
	    return tagData;
	}
    }

    /**
     * Represents the body of a <jsp:text> element
     */
    public static class JspText extends Node {

	public JspText(Mark start, Node parent) {
	    super(start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a template text string
     */
    public static class TemplateText extends Node {

	public TemplateText(char[] text, Mark start, Node parent) {
	    super(text, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /*********************************************************************
     * Auxillary classes used in Node
     */

    /**
     * Represents attributes that can be runtime expressions.
     */

    public static class JspAttribute {

	private String name;
	private String value;
	private boolean expression;

	JspAttribute(String name, String value, boolean expr) {
	    this.name = name;
	    this.value = value;
	    this.expression = expr;
	}

	/**
 	 * @return The name of the attribute
	 */
	public String getName() {
	    return name;
	}

	/**
	 * @return the value for the attribute, or the expression string
	 *	   (stripped of "<%=", "%>", "%=", or "%")
	 */
	public String getValue() {
	    return value;
	}

	/**
	 * @return true is the value represents an expression
	 */
	public boolean isExpression() {
	    return expression;
	}
    }

    /**
     * An ordered list of Node, used to represent the body of an element, or
     * a jsp page of jsp document.
     */
    public static class Nodes {

	private List list;
	private Node.Root root;

	public Nodes() {
	    list = new Vector();
	}

	public Nodes(Node.Root root) {
	    this.root = root;
	    list = new Vector();
	    list.add(root);
	}

	/**
	 * Appends a node to the list
	 * @param n The node to add
	 */
	public void add(Node n) {
	    list.add(n);
	}

	/**
	 * Visit the nodes in the list with the supplied visitor
	 * @param v The visitor used
	 */
	public void visit(Visitor v) throws JasperException {
	    Iterator iter = list.iterator();
	    while (iter.hasNext()) {
		Node n = (Node) iter.next();
		n.accept(v);
	    }
	}

	public int size() {
	    return list.size();
	}

	public Node getNode(int index) {
	    Node n = null;
	    try {
		n = (Node) list.get(index);
	    } catch (ArrayIndexOutOfBoundsException e) {
	    }
	    return n;
	}
	
	public Node.Root getRoot() {
	    return root;
	}
    }

    /**
     * A visitor class for visiting the node.  This class also provides the
     * default action (i.e. nop) for each of the child class of the Node.
     * An actual visitor should extend this class and supply the visit
     * method for the nodes that it cares.
     */
    public static class Visitor {

	/**
	 * The method provides a place to put actions that are common to
	 * all nodes.  Override this in the child visitor class if need to.
	 */
	protected void doVisit(Node n) throws JasperException {
	}

	/**
	 * Visit the body of a node, using the current visitor
	 */
	protected void visitBody(Node n) throws JasperException {
	    if (n.getBody() != null) {
		n.getBody().visit(this);
	    }
	}

	public void visit(Root n) throws JasperException {
	    Node.pushCurrentRoot(n);
	    doVisit(n);
	    visitBody(n);
	    Node.popCurrentRoot();
	}

	public void visit(JspRoot n) throws JasperException {
	    Node.pushCurrentRoot(n);
	    doVisit(n);
	    visitBody(n);
	    Node.popCurrentRoot();
	}

	public void visit(PageDirective n) throws JasperException {
	    doVisit(n);
	}

	public void visit(IncludeDirective n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(TaglibDirective n) throws JasperException {
	    doVisit(n);
	}

	public void visit(Comment n) throws JasperException {
	    doVisit(n);
	}

	public void visit(Declaration n) throws JasperException {
	    doVisit(n);
	}

	public void visit(Expression n) throws JasperException {
	    doVisit(n);
	}

	public void visit(Scriptlet n) throws JasperException {
	    doVisit(n);
	}

	public void visit(IncludeAction n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(ForwardAction n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(GetProperty n) throws JasperException {
	    doVisit(n);
	}

	public void visit(SetProperty n) throws JasperException {
	    doVisit(n);
	}

	public void visit(ParamAction n) throws JasperException {
	    doVisit(n);
	}

	public void visit(ParamsAction n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(FallBackAction n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(UseBean n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(PlugIn n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(CustomTag n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(UninterpretedTag n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(JspText n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(TemplateText n) throws JasperException {
	    doVisit(n);
	}
    }
}
