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
import javax.servlet.jsp.tagext.*;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;

/**
 * An internal data representation of a JSP page or a JSP docuement (XML).
 * Also included here is a visitor class for tranversing nodes.
 *
 * @author Kin-man Chung
 * @author Jan Luehe
 * @author Shawn Bayern
 * @author Mark Roth
 */

abstract class Node {
    
    protected Attributes attrs;
    protected Nodes body;
    protected char[] text;
    protected Mark startMark;
    protected int beginJavaLine;
    protected int endJavaLine;
    protected Node parent;
    protected Nodes namedAttributeNodes; // cached for performance

    private boolean isDummy;

    /**
     * Constructor.
     * @param start The location of the jsp page
     * @param parent The enclosing node
     */
    public Node(Mark start, Node parent) {
	this.startMark = start;
	this.isDummy = (start == null);
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
	this.isDummy = (start == null);
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
	this.isDummy = (start == null);
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

    /**
     * Get the attribute that is non requrest time expression, either
     * from the attribute of the node, or from a jsp:attrbute 
     */
    public String getTextAttribute(String name) {
	class AttributeVisitor extends Visitor {
	    String attrValue = null;
	    public void visit(TemplateText txt) {
		attrValue = new String(txt.getText());
	    }

	    public String getAttrValue() {
		return attrValue;
	    }
	}

	String attr = getAttributeValue(name);
	if (attr != null) {
	    return attr;
	}

	NamedAttribute namedAttribute = getNamedAttributeNode(name);
	if (namedAttribute == null) {
	    return null;
	}

	// Get the attribute value from named attribute (jsp:attribute)
	// Since this method is only for attributes that are not rtexpr,
	// we can assume the body of the jsp:attribute is a template text
	if (namedAttribute.getBody() != null) {
	    AttributeVisitor attributeVisitor = new AttributeVisitor();
	    try {
		namedAttribute.getBody().visit(attributeVisitor);
	    } catch (JasperException e) {
	    }
	    attr = attributeVisitor.getAttrValue();
	}

	return attr;
    }

    /**
     * Searches all subnodes of this node for jsp:attribute standard
     * actions with the given name, and returns the NamedAttribute node
     * of the matching named attribute, nor null if no such node is found.
     * <p>
     * This should always be called and only be called for nodes that
     * accept dynamic runtime attribute expressions.
     */
    public NamedAttribute getNamedAttributeNode( String name ) {
        NamedAttribute result = null;
        
        // Look for the attribute in NamedAttribute children
        Nodes nodes = getNamedAttributeNodes();
        int numChildNodes = nodes.size();
        for( int i = 0; i < numChildNodes; i++ ) {
            NamedAttribute na = (NamedAttribute)nodes.getNode( i );
            if( na.getName().equals( name ) ) {
                result = na;
                break;
            }
        }
        
        return result;
    }

    /**
     * Searches all subnodes of this node for jsp:attribute standard
     * actions, and returns that set of nodes as a Node.Nodes object.
     */
    public Node.Nodes getNamedAttributeNodes() {

	if (namedAttributeNodes != null) {
	    return namedAttributeNodes;
	}

        Node.Nodes result = new Node.Nodes();
        
        // Look for the attribute in NamedAttribute children
        Nodes nodes = getBody();
        if( nodes != null ) {
            int numChildNodes = nodes.size();
            for( int i = 0; i < numChildNodes; i++ ) {
                Node n = nodes.getNode( i );
                if( n instanceof NamedAttribute ) {
                    result.add( n );
                }
                else {
                    // Nothing can come before jsp:attribute, and only
                    // jsp:body can come after it.
                    break;
                }
            }
        }

	namedAttributeNodes = result;
        return result;
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

    public boolean isDummy() {
	return isDummy;
    }

    /**
     * @return true if the current page is in xml syntax, false otherwise.
     */
    public boolean isXmlSyntax() {
	Node r = this;
	while (!(r instanceof Node.Root)) {
	    r = r.getParent();
	    if (r == null)
		return false;
	}

	return r.isXmlSyntax();
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

	    // Figure out and set the parent root
	    Node r = parent;
	    while ((r != null) && !(r instanceof Node.Root))
		r = r.getParent();
	    parentRoot = (Node.Root) r;
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public boolean isXmlSyntax() {
	    return false;
	}

	/**
	 * @return The enclosing root to this root.  Usually represents the
	 * page that includes this one.
	 */
	public Root getParentRoot() {
	    return parentRoot;
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
     * Represents a tag directive
     */
    public static class TagDirective extends Node {
        private Vector imports;

	public TagDirective(Attributes attrs, Mark start, Node parent) {
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
     * Represents an attribute directive
     */
    public static class AttributeDirective extends Node {

	public AttributeDirective(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a variable directive
     */
    public static class VariableDirective extends Node {

	public VariableDirective(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a <jsp:invoke> tag file action
     */
    public static class InvokeAction extends Node {

	public InvokeAction(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}
    }

    /**
     * Represents a <jsp:doBody> tag file action
     */
    public static class DoBodyAction extends Node {

	public DoBodyAction(Attributes attrs, Mark start, Node parent) {
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
     * Represents an EL expression.  Expressions in attributes are embedded
     * in the attribute string and not here.
     */
    public static class ELExpression extends Node {

        public ELExpression(char[] text, Mark start, Node parent) {
            super(text, start, parent);
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

        private JspAttribute width;
        private JspAttribute height;
        
	public PlugIn(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setHeight(JspAttribute height) {
	    this.height = height;
	}

	public void setWidth(JspAttribute width) {
	    this.width = width;
	}

	public JspAttribute getHeight() {
	    return height;
	}

	public JspAttribute getWidth() {
	    return width;
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
     * Represents a <jsp:element>.
     */
    public static class JspElement extends Node {

	private JspAttribute[] jspAttrs;

	public JspElement(Attributes attrs, Mark start, Node parent) {
	    super(attrs, start, parent);
	}

	public void accept(Visitor v) throws JasperException {
	    v.visit(this);
	}

	public void setJspAttributes(JspAttribute[] jspAttrs) {
	    this.jspAttrs = jspAttrs;
	}

	public JspAttribute[] getJspAttributes() {
	    return jspAttrs;
	}
    }

    /**
     * Collected information about child elements.  Used by nodes like
     * CustomTag, JspBody, and NamedAttribute.  The information is 
     * set in the Collector.
     */
    public static class ChildInfo {
	private boolean scriptless;	// true if the tag and its body
					// contians no scripting elements.
	private boolean hasUseBean;
	private boolean hasIncludeAction;
	private boolean hasSetProperty;
	private boolean hasScriptingVars;

	public void setScriptless(boolean s) {
	    scriptless = s;
	}

	public boolean isScriptless() {
	    return scriptless;
	}

	public void setHasUseBean(boolean u) {
	    hasUseBean = u;
	}

	public boolean hasUseBean() {
	    return hasUseBean;
	}

	public void setHasIncludeAction(boolean i) {
	    hasIncludeAction = i;
	}

	public boolean hasIncludeAction() {
	    return hasIncludeAction;
	}

	public void setHasSetProperty(boolean s) {
	    hasSetProperty = s;
	}

	public boolean hasSetProperty() {
	    return hasSetProperty;
	}
        
	public void setHasScriptingVars(boolean s) {
	    hasScriptingVars = s;
	}

	public boolean hasScriptingVars() {
	    return hasScriptingVars;
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
	private String tagHandlerPoolName;
	private TagInfo tagInfo;
	private TagFileInfo tagFileInfo;
	private Class tagHandlerClass;
	private VariableInfo[] varInfos;
	private int customNestingLevel;
        private ChildInfo childInfo;
	private boolean implementsIterationTag;
	private boolean implementsBodyTag;
	private boolean implementsTryCatchFinally;
	private boolean implementsSimpleTag;
	private boolean implementsDynamicAttributes;
	private Vector atBeginScriptingVars;
	private Vector atEndScriptingVars;
	private Vector nestedScriptingVars;
	private Node.CustomTag customTagParent;
	private Integer numCount;

	public CustomTag(Attributes attrs, Mark start, String name,
			 String prefix, String shortName,
			 TagInfo tagInfo, TagFileInfo tagFileInfo,
			 Class tagHandlerClass, Node parent) {
	    super(attrs, start, parent);
	    this.name = name;
	    this.prefix = prefix;
	    this.shortName = shortName;
	    this.tagInfo = tagInfo;
	    this.tagFileInfo = tagFileInfo;
	    this.tagHandlerClass = tagHandlerClass;
	    this.customNestingLevel = makeCustomNestingLevel();
            this.childInfo = new ChildInfo();

	    if (tagHandlerClass != null) {
		this.implementsIterationTag = 
		    IterationTag.class.isAssignableFrom(tagHandlerClass);
		this.implementsBodyTag =
		    BodyTag.class.isAssignableFrom(tagHandlerClass);
		this.implementsTryCatchFinally = 
		    TryCatchFinally.class.isAssignableFrom(tagHandlerClass);
		this.implementsSimpleTag = 
		    SimpleTag.class.isAssignableFrom(tagHandlerClass);
		this.implementsDynamicAttributes = 
		    DynamicAttributes.class.isAssignableFrom(tagHandlerClass);
	    } else if (tagFileInfo != null) {
		// get the info from tag file
		this.implementsIterationTag = false;
		this.implementsBodyTag = false;
		this.implementsTryCatchFinally = false;
		this.implementsSimpleTag = true;
		this.implementsDynamicAttributes =
                        tagInfo.hasDynamicAttributes();
	    }
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
        
        public ChildInfo getChildInfo() {
            return childInfo;
        }
	
	public void setTagData(TagData tagData) {
	    this.tagData = tagData;
	    this.varInfos = tagInfo.getVariableInfo(tagData);
	}

	public TagData getTagData() {
	    return tagData;
	}

	public void setTagHandlerPoolName(String s) {
	    tagHandlerPoolName = s;
	}

	public String getTagHandlerPoolName() {
	    return tagHandlerPoolName;
	}

	public TagInfo getTagInfo() {
	    return tagInfo;
	}

	public TagFileInfo getTagFileInfo() {
	    return tagFileInfo;
	}

	public Class getTagHandlerClass() {
	    return tagHandlerClass;
	}

	public void setTagHandlerClass(Class hc) {
	    tagHandlerClass = hc;
	}

	public boolean implementsIterationTag() {
	    return implementsIterationTag;
	}

	public boolean implementsBodyTag() {
	    return implementsBodyTag;
	}

	public boolean implementsTryCatchFinally() {
	    return implementsTryCatchFinally;
	}

	public boolean implementsSimpleTag() {
	    return implementsSimpleTag;
	}

	public boolean implementsDynamicAttributes() {
	    return implementsDynamicAttributes;
	}

	public TagVariableInfo[] getTagVariableInfos() {
	    return tagInfo.getTagVariableInfos();
 	}
 
	public VariableInfo[] getVariableInfos() {
	    return varInfos;
	}

	public void setCustomTagParent(Node.CustomTag n) {
	    this.customTagParent = n;
	}

	public Node.CustomTag getCustomTagParent() {
	    return this.customTagParent;
	}

	public void setNumCount(Integer count) {
	    this.numCount = count;
	}

	public Integer getNumCount() {
	    return this.numCount;
	}

	public void setScriptingVars(Vector vec, int scope) {
	    switch (scope) {
	    case VariableInfo.AT_BEGIN:
		this.atBeginScriptingVars = vec;
		break;
	    case VariableInfo.AT_END:
		this.atEndScriptingVars = vec;
		break;
	    case VariableInfo.NESTED:
		this.nestedScriptingVars = vec;
		break;
	    }
	}

	/*
	 * Gets the scripting variables for the given scope that need to be
	 * declared.
	 */
	public Vector getScriptingVars(int scope) {
	    Vector vec = null;

	    switch (scope) {
	    case VariableInfo.AT_BEGIN:
		vec = this.atBeginScriptingVars;
		break;
	    case VariableInfo.AT_END:
		vec = this.atEndScriptingVars;
		break;
	    case VariableInfo.NESTED:
		vec = this.nestedScriptingVars;
		break;
	    }

	    return vec;
	}

	/*
	 * Gets this custom tag's custom nesting level, which is given as
	 * the number of times this custom tag is nested inside itself.
	 */
	public int getCustomNestingLevel() {
	    return customNestingLevel;
	}

        /**
         * Checks to see if the attribute of the given name is of type
	 * JspFragment.
         */
        public boolean checkIfAttributeIsJspFragment( String name ) {
            boolean result = false;

	    TagAttributeInfo[] attributes = tagInfo.getAttributes();
	    for (int i = 0; attributes != null && i < attributes.length; i++) {
		if (attributes[i].getName().equals(name) &&
		            attributes[i].isFragment()) {
		    result = true;
		    break;
		}
	    }
            
            return result;
        }
        
	/*
	 * Computes this custom tag's custom nesting level, which corresponds
	 * to the number of times this custom tag is nested inside itself.
	 *
	 * Example:
	 * 
	 *  <g:h>
	 *    <a:b> -- nesting level 0
	 *      <c:d>
	 *        <e:f>
	 *          <a:b> -- nesting level 1
	 *            <a:b> -- nesting level 2
	 *            </a:b>
	 *          </a:b>
	 *          <a:b> -- nesting level 1
	 *          </a:b>
	 *        </e:f>
	 *      </c:d>
	 *    </a:b>
	 *  </g:h>
	 * 
	 * @return Custom tag's nesting level
	 */
	private int makeCustomNestingLevel() {
	    int n = 0;
	    Node p = parent;
	    while (p != null) {
		if ((p instanceof Node.CustomTag)
		        && name.equals(((Node.CustomTag) p).name)) {
		    n++;
		}
		p = p.parent;
	    }
	    return n;
	}
    }

    /**
     * Represents the body of a &lt;jsp:text&gt; element
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
     * Represents a Named Attribute (&lt;jsp:attribute&gt;)
     */
    public static class NamedAttribute extends Node {

        // A unique temporary variable name suitable for code generation
        private String temporaryVariableName;

        // True if this node is to be trimmed, or false otherwise
        private boolean trim = true;
        
        private ChildInfo childInfo;

        public NamedAttribute( Attributes attrs, Mark start, Node parent) {
            super( attrs, start, parent );
            this.temporaryVariableName = JspUtil.nextTemporaryVariableName();
            if( "false".equals( this.getAttributeValue( "trim" ) ) ) {
                // (if null or true, leave default of true)
                trim = false;
            }
            this.childInfo = new ChildInfo();
        }

        public void accept(Visitor v) throws JasperException {
            v.visit(this);
        }

        public String getName() {
            return this.getAttributeValue( "name" );
        }
        
        public ChildInfo getChildInfo() {
            return this.childInfo;
        }

        public boolean isTrim() {
            return trim;
        }

        /**
         * @return A unique temporary variable name to store the result in.
         *      (this probably could go elsewhere, but it's convenient here)
         */
        public String getTemporaryVariableName() {
            return temporaryVariableName;
        }
    }

    /**
     * Represents a JspBody node (&lt;jsp:body&gt;)
     */
    public static class JspBody extends Node {

	private JspAttribute value;
        private ChildInfo childInfo;

        public JspBody( Attributes attrs, Mark start, Node parent) {
            super( attrs, start, parent );
            this.childInfo = new ChildInfo();
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
        
        public ChildInfo getChildInfo() {
            return childInfo;
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

        /**
         * Trim all whitespace from the left of the template text
         */
        public void ltrim() {
            // Whitespace logic borrowed from JspReader.isSpace
	    int index = 0;
            while ((index < text.length) && (text[index] <= ' ')) {
		index++;
            }
	    int size = text.length - index;
            char[] newText = new char[size];
            System.arraycopy(text, index, newText, 0, size);
            text = newText;
        }

        /**
         * Trim all whitespace from the right of the template text
         */
        public void rtrim() {
            // Whitespace logic borrowed from JspReader.isSpace
            int size = text.length;
            while( (size > 0) && (text[size-1] <= ' ') ) {
                size--;
            }
            char[] newText = new char[size];
            System.arraycopy( text, 0, newText, 0, size );
            text = newText;
        }
    }

    /*********************************************************************
     * Auxillary classes used in Node
     */

    /**
     * Represents attributes that can be request time expressions.
     *
     * Can either be a plain attribute, an attribute that represents a
     * request time expression value, or a named attribute (specified using
     * the jsp:attribute standard action).
     */

    public static class JspAttribute {

	private String qName;
	private String uri;
	private String localName;
	private String value;
	private boolean expression;
        private boolean el;
	private boolean dynamic;

        // If true, this JspAttribute represents a <jsp:attribute>
        private boolean namedAttribute;
        // The node in the parse tree for the NamedAttribute
        private NamedAttribute namedAttributeNode;

        JspAttribute(String qName, String uri, String localName, String value,
		     boolean expr, boolean el, boolean dyn ) {
	    this.qName = qName;
	    this.uri = uri;
	    this.localName = localName;
	    this.value = value;
            this.namedAttributeNode = null;
	    this.expression = expr;
            this.el = el;
	    this.dynamic = dyn;
            this.namedAttribute = false;
	}

        /**
         * Use this constructor if the JspAttribute represents a
         * named attribute.  In this case, we have to store the nodes of
         * the body of the attribute.
         */
        JspAttribute(NamedAttribute namedAttributeNode, boolean dyn) {
            this.qName = namedAttributeNode.getName();
	    this.localName = this.qName;
            this.value = null;
            this.namedAttributeNode = namedAttributeNode;
            this.expression = false;
            this.el = false;
	    this.dynamic = dyn;
            this.namedAttribute = true;
        }

	/**
 	 * @return The name of the attribute
	 */
	public String getName() {
	    return qName;
	}

	/**
 	 * @return The local name of the attribute
	 */
	public String getLocalName() {
	    return localName;
	}

	/**
 	 * @return The namespace of the attribute, or null if in the default
	 * namespace
	 */
	public String getURI() {
	    return uri;
	}

	/**
         * Only makes sense if namedAttribute is false.
         *
         * @return the value for the attribute, or the expression string
         *         (stripped of "<%=", "%>", "%=", or "%"
         *          but containing "${" and "}" for EL expressions)
	 */
	public String getValue() {
	    return value;
	}

        /**
         * Only makes sense if namedAttribute is true.
         *
         * @return the nodes that evaluate to the body of this attribute.
         */
        public NamedAttribute getNamedAttributeNode() {
            return namedAttributeNode;
        }

	/**
         * @return true if the value represents a traditional rtexprvalue
	 */
	public boolean isExpression() {
	    return expression;
	}

        /**
         * @return true if the value represents a NamedAttribute value.
         */
        public boolean isNamedAttribute() {
            return namedAttribute;
        }

        /**
         * @return true if the value represents an expression that should
         * be fed to the expression interpreter
         * @return false for string literals or rtexprvalues that should
         * not be interpreter or reevaluated
         */
        public boolean isELInterpreterInput() {
            return el;
        }

	/**
	 * @return true if the value is a string literal know at translation
	 * time.
	 */
	public boolean isLiteral() {
	    return !expression && !el && !namedAttribute;
	}

	/**
	 * XXX
	 */
	public boolean isDynamic() {
	    return dynamic;
	}
    }

    /**
     * An ordered list of Node, used to represent the body of an element, or
     * a jsp page of jsp document.
     */
    public static class Nodes {

	private List list;
	private Node.Root root;		// null if this is not a page

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
	    root = null;
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
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(JspRoot n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(PageDirective n) throws JasperException {
	    doVisit(n);
	}

	public void visit(TagDirective n) throws JasperException {
	    doVisit(n);
	}

	public void visit(IncludeDirective n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(TaglibDirective n) throws JasperException {
	    doVisit(n);
	}

	public void visit(AttributeDirective n) throws JasperException {
	    doVisit(n);
	}

	public void visit(VariableDirective n) throws JasperException {
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

        public void visit(ELExpression n) throws JasperException {
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
	    visitBody(n);
	}

	public void visit(SetProperty n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(ParamAction n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
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

	public void visit(JspElement n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

	public void visit(JspText n) throws JasperException {
	    doVisit(n);
	    visitBody(n);
	}

        public void visit(NamedAttribute n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(JspBody n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(InvokeAction n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

        public void visit(DoBodyAction n) throws JasperException {
            doVisit(n);
            visitBody(n);
        }

	public void visit(TemplateText n) throws JasperException {
	    doVisit(n);
	}
    }
}
