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

import java.util.Hashtable;
import java.util.Enumeration;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.ValidationMessage;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

import org.xml.sax.Attributes;

/**
 * Performs validation on the page elements.  Attributes are checked for
 * mandatory presence, entry value validity, and consistency.  As a
 * side effect, some page global value (such as those from page direcitves)
 * are stored, for later use.
 *
 * @author Kin-man Chung
 * @author Jan Luehe
 * @author Shawn Bayern
 * @author Mark Roth
 */
public class Validator {

    /**
     * A visitor to validate and extract page directive info
     */
    static class DirectiveVisitor extends Node.Visitor {

	private PageInfo pageInfo;
	private ErrorDispatcher err;

	private static final JspUtil.ValidAttribute[] pageDirectiveAttrs = {
	    new JspUtil.ValidAttribute("language"),
	    new JspUtil.ValidAttribute("extends"),
	    new JspUtil.ValidAttribute("import"),
	    new JspUtil.ValidAttribute("session"),
	    new JspUtil.ValidAttribute("buffer"),
	    new JspUtil.ValidAttribute("autoFlush"),
	    new JspUtil.ValidAttribute("isThreadSafe"),
	    new JspUtil.ValidAttribute("info"),
	    new JspUtil.ValidAttribute("errorPage"),
	    new JspUtil.ValidAttribute("isErrorPage"),
	    new JspUtil.ValidAttribute("contentType"),
	    new JspUtil.ValidAttribute("pageEncoding"),
	    new JspUtil.ValidAttribute("isScriptingEnabled"),
	    new JspUtil.ValidAttribute("isELEnabled")
	};

	private boolean languageSeen = false;
	private boolean extendsSeen = false;
	private boolean sessionSeen = false;
 	private boolean bufferSeen = false;
	private boolean autoFlushSeen = false;
	private boolean isThreadSafeSeen = false;
	private boolean errorPageSeen = false;
	private boolean isErrorPageSeen = false;
	private boolean contentTypeSeen = false;
	private boolean infoSeen = false;
	private boolean pageEncodingSeen = false;

	/*
	 * Constructor
	 */
	DirectiveVisitor(Compiler compiler) {
	    this.pageInfo = compiler.getPageInfo();
	    this.err = compiler.getErrorDispatcher();
	}

	public void visit(Node.PageDirective n) throws JasperException {    

            JspUtil.checkAttributes("Page directive", n,
                                    pageDirectiveAttrs, err);

	    // JSP.2.10.1
	    Attributes attrs = n.getAttributes();
	    for (int i = 0; i < attrs.getLength(); i++) {
		String attr = attrs.getQName(i);
		String value = attrs.getValue(i);

		if ("language".equals(attr)) {
		    if (languageSeen)
			err.jspError(n, "jsp.error.page.multiple.language");
		    languageSeen = true;
		    if (!"java".equalsIgnoreCase(value))
			err.jspError(n, "jsp.error.language.nonjava");
		    pageInfo.setLanguage(value);
		} else if ("extends".equals(attr)) {
		    if (extendsSeen)
			err.jspError(n, "jsp.error.page.multiple.extends");
		    extendsSeen = true;
		    pageInfo.setExtends(value);
		    /*
		     * If page superclass is top level class (i.e. not in a
		     * pkg) explicitly import it. If this is not done, the
		     * compiler will assume the extended class is in the same
		     * pkg as the generated servlet.
		     */
		    if (value.indexOf('.') < 0)
			n.addImport(value);
		} else if ("contentType".equals(attr)) {
		    if (contentTypeSeen) 
			err.jspError(n, "jsp.error.page.multiple.contenttypes");
		    contentTypeSeen = true;
		    pageInfo.setContentType(value);
		} else if ("session".equals(attr)) {
		    if (sessionSeen)
			err.jspError(n, "jsp.error.session.multiple");
		    sessionSeen = true;
		    if ("true".equalsIgnoreCase(value))
			pageInfo.setSession(true);
		    else if ("false".equalsIgnoreCase(value))
			pageInfo.setSession(false);
		    else
			err.jspError(n, "jsp.error.session.invalid");
		} else if ("buffer".equals(attr)) {
		    if (bufferSeen)
			err.jspError(n, "jsp.error.page.multiple.buffer");
		    bufferSeen = true;

		    if ("none".equalsIgnoreCase(value))
			pageInfo.setBuffer(0);
		    else {
			if (value == null || !value.endsWith("kb"))
			    err.jspError(n, "jsp.error.buffer.invalid");

			try {
			    Integer k = new Integer(
			        value.substring(0, value.length()-2));
			    pageInfo.setBuffer(k.intValue()*1024);
			} catch (NumberFormatException e) {
			    err.jspError(n, "jsp.error.buffer.invalid");
			}
		    }
		} else if ("autoFlush".equals(attr)) {
		    if (autoFlushSeen)
			err.jspError(n, "jsp.error.page.multiple.autoflush");
		    autoFlushSeen = true;
		    if ("true".equalsIgnoreCase(value))
			pageInfo.setAutoFlush(true);
		    else if ("false".equalsIgnoreCase(value))
			pageInfo.setAutoFlush(false);
		    else
			err.jspError(n, "jsp.error.autoFlush.invalid");
		} else if ("isthreadSafe".equals(attr)) {
		    if (isThreadSafeSeen)
			err.jspError(n, "jsp.error.page.multiple.threadsafe");
		    isThreadSafeSeen = true;
		    if ("true".equalsIgnoreCase(value))
			pageInfo.setThreadSafe(true);
		    else if ("false".equalsIgnoreCase(value))
			pageInfo.setThreadSafe(false);
		    else
			err.jspError(n, "jsp.error.isThreadSafe.invalid");
		} else if ("isScriptingEnabled".equals(attr)) {
		    // XXX Test for multiple occurrence?
		    if ("true".equalsIgnoreCase(value))
			pageInfo.setScriptingEnabled(true);
		    else if ("false".equalsIgnoreCase(value))
			pageInfo.setScriptingEnabled(false);
		    else
			err.jspError(n, "jsp.error.isScriptingEnabled.invalid");
		} else if ("isELEnabled".equals(attr)) {
		    // XXX Test for multiple occurrence?
		    if ("true".equalsIgnoreCase(value))
			pageInfo.setELEnabled(true);
		    else if ("false".equalsIgnoreCase(value))
			pageInfo.setELEnabled(false);
		    else
			err.jspError(n, "jsp.error.isELEnabled.invalid");
		} else if ("isErrorPage".equals(attr)) {
		    if (isErrorPageSeen)
			err.jspError(n, "jsp.error.page.multiple.iserrorpage");
		    isErrorPageSeen = true;
		    if ("true".equalsIgnoreCase(value))
			pageInfo.setIsErrorPage(true);
		    else if ("false".equalsIgnoreCase(value))
			pageInfo.setIsErrorPage(false);
		    else
			err.jspError(n, "jsp.error.isErrorPage.invalid");
		} else if ("errorPage".equals(attr)) {
		    if (errorPageSeen) 
			err.jspError(n, "jsp.error.page.multiple.errorpage");
		    errorPageSeen = true;
		    pageInfo.setErrorPage(value);
		} else if ("info".equals(attr)) {
		    if (infoSeen) 
			err.jspError(n, "jsp.error.info.multiple");
		    infoSeen = true;
		} else if ("pageEncoding".equals(attr)) {
		    if (pageEncodingSeen) 
			err.jspError(n, "jsp.error.page.multiple.pageencoding");
		    pageEncodingSeen = true;
		    pageInfo.setPageEncoding(value);
		}
	    }

	    // Check for bad combinations
	    if (pageInfo.getBuffer() == 0 && !pageInfo.isAutoFlush())
		err.jspError(n, "jsp.error.page.badCombo");

	    // Attributes for imports for this node have been processed by
	    // the parsers, just add them to pageInfo.
	    pageInfo.addImports(n.getImports());
	}

	public void visit(Node.TagDirective n) throws JasperException {
	    // Do nothing, since this tag directive has already been validated
	    // by TagFileProcessor when it created a TagInfo object from the
	    // tag file in which the directive appeared
	}

	public void visit(Node.AttributeDirective n) throws JasperException {
	    // Do nothing, since this attribute directive has already been
	    // validated by TagFileProcessor when it created a TagInfo object
	    // from the tag file in which the directive appeared
	}

	public void visit(Node.VariableDirective n) throws JasperException {
	    // Do nothing, since this variable directive has already been
	    // validated by TagFileProcessor when it created a TagInfo object
	    // from the tag file in which the directive appeared
	}
    }

    /**
     * A visitor for validating nodes other than page directives
     */
    static class ValidateVisitor extends Node.Visitor {

	private PageInfo pageInfo;
	private ErrorDispatcher err;

	private static final JspUtil.ValidAttribute[] jspRootAttrs = {
	    new JspUtil.ValidAttribute("version", true) };

	private static final JspUtil.ValidAttribute[] includeDirectiveAttrs = {
	    new JspUtil.ValidAttribute("file", true) };

	private static final JspUtil.ValidAttribute[] taglibDirectiveAttrs = {
	    new JspUtil.ValidAttribute("uri"),
	    new JspUtil.ValidAttribute("tagdir"),
	    new JspUtil.ValidAttribute("prefix", true) };

	private static final JspUtil.ValidAttribute[] includeActionAttrs = {
	    new JspUtil.ValidAttribute("page", true, true),
	    new JspUtil.ValidAttribute("flush") };

	private static final JspUtil.ValidAttribute[] paramActionAttrs = {
	    new JspUtil.ValidAttribute("name", true),
	    new JspUtil.ValidAttribute("value", true, true) };

	private static final JspUtil.ValidAttribute[] forwardActionAttrs = {
	    new JspUtil.ValidAttribute("page", true, true) };

	private static final JspUtil.ValidAttribute[] getPropertyAttrs = {
	    new JspUtil.ValidAttribute("name", true),
	    new JspUtil.ValidAttribute("property", true) };

	private static final JspUtil.ValidAttribute[] setPropertyAttrs = {
	    new JspUtil.ValidAttribute("name", true),
	    new JspUtil.ValidAttribute("property", true),
	    new JspUtil.ValidAttribute("value", false, true),
	    new JspUtil.ValidAttribute("param") };

	private static final JspUtil.ValidAttribute[] useBeanAttrs = {
	    new JspUtil.ValidAttribute("id", true),
	    new JspUtil.ValidAttribute("scope"),
	    new JspUtil.ValidAttribute("class"),
	    new JspUtil.ValidAttribute("type"),
	    new JspUtil.ValidAttribute("beanName", false, true) };

	private static final JspUtil.ValidAttribute[] plugInAttrs = {
	    new JspUtil.ValidAttribute("type",true),
	    new JspUtil.ValidAttribute("code", true),
	    new JspUtil.ValidAttribute("codebase"),
	    new JspUtil.ValidAttribute("align"),
	    new JspUtil.ValidAttribute("archive"),
	    new JspUtil.ValidAttribute("height", false, true),
	    new JspUtil.ValidAttribute("hspace"),
	    new JspUtil.ValidAttribute("jreversion"),
	    new JspUtil.ValidAttribute("name"),
	    new JspUtil.ValidAttribute("vspace"),
	    new JspUtil.ValidAttribute("width", false, true),
	    new JspUtil.ValidAttribute("nspluginurl"),
	    new JspUtil.ValidAttribute("iepluginurl") };
            
        private static final JspUtil.ValidAttribute[] attributeAttrs = {
            new JspUtil.ValidAttribute("name", true),
            new JspUtil.ValidAttribute("trim") };
            
        private static final JspUtil.ValidAttribute[] bodyAttrs = {
            new JspUtil.ValidAttribute("value") };

	/*
	 * Constructor
	 */
	ValidateVisitor(Compiler compiler) {
	    this.pageInfo = compiler.getPageInfo();
	    this.err = compiler.getErrorDispatcher();
	}

	public void visit(Node.JspRoot n) throws JasperException {
	    JspUtil.checkAttributes("Jsp:root", n,
				    jspRootAttrs, err);
	    visitBody(n);
	}

	public void visit(Node.IncludeDirective n) throws JasperException {
            JspUtil.checkAttributes("Include directive", n,
                                    includeDirectiveAttrs, err);
	    visitBody(n);
	}

	public void visit(Node.TaglibDirective n) throws JasperException {
            JspUtil.checkAttributes("Taglib directive", n,
                                    taglibDirectiveAttrs, err);
	    // Either 'uri' or 'tagdir' attribute must be present
	    if (n.getAttributeValue("uri") == null
		    && n.getAttributeValue("tagdir") == null) {
		err.jspError(n, "jsp.error.taglibDirective.missing.location");
	    }
	}

	public void visit(Node.ParamAction n) throws JasperException {
            JspUtil.checkAttributes("Param action", n,
                                    paramActionAttrs, err);
	    n.setValue(getJspAttribute("value", null, null,
				       n.getAttributeValue("value"),
				       n, false));
            visitBody(n);
	}

	public void visit(Node.IncludeAction n) throws JasperException {
            JspUtil.checkAttributes("Include action", n,
                                    includeActionAttrs, err);
	    n.setPage(getJspAttribute("page", null, null,
				      n.getAttributeValue("page"), n, false));
	    visitBody(n);
        };

	public void visit(Node.ForwardAction n) throws JasperException {
            JspUtil.checkAttributes("Forward", n,
                                    forwardActionAttrs, err);
	    n.setPage(getJspAttribute("page", null, null,
				      n.getAttributeValue("page"), n, false));
	    visitBody(n);
	}

	public void visit(Node.GetProperty n) throws JasperException {
            JspUtil.checkAttributes("GetProperty", n,
                                    getPropertyAttrs, err);
	}

	public void visit(Node.SetProperty n) throws JasperException {
            JspUtil.checkAttributes("SetProperty", n,
                                    setPropertyAttrs, err);
	    String name = n.getAttributeValue("name");
	    String property = n.getAttributeValue("property");
	    String param = n.getAttributeValue("param");
	    String value = n.getAttributeValue("value");

            n.setValue(getJspAttribute("value", null, null, value, n, false));

            boolean valueSpecified = n.getValue() != null;

	    if ("*".equals(property)) { 
                if (param != null || valueSpecified)
		    err.jspError(n, "jsp.error.setProperty.invalid");
		
            } else if (param != null && valueSpecified) {
		err.jspError(n, "jsp.error.setProperty.invalid");
	    }
            
            visitBody(n);
	}

	public void visit(Node.UseBean n) throws JasperException {
            JspUtil.checkAttributes("UseBean", n,
                                    useBeanAttrs, err);

	    String name = n.getAttributeValue ("id");
	    String scope = n.getAttributeValue ("scope");
	    String className = n.getAttributeValue ("class");
	    String type = n.getAttributeValue ("type");
	    BeanRepository beanInfo = pageInfo.getBeanRepository();

	    if (className == null && type == null)
		err.jspError(n, "jsp.error.useBean.missingType");

	    if (beanInfo.checkVariable(name))
		err.jspError(n, "jsp.error.useBean.duplicate");

	    if ("session".equals(scope) && !pageInfo.isSession())
		err.jspError(n, "jsp.error.useBean.noSession");

	    Node.JspAttribute jattr
		= getJspAttribute("beanName", null, null,
				  n.getAttributeValue("beanName"),
				  n, false);
	    n.setBeanName(jattr);
	    if (className != null && jattr != null)
		err.jspError(n, "jsp.error.useBean.notBoth");

	    if (className == null)
		className = type;

	    if (scope == null || scope.equals("page")) {
		beanInfo.addPageBean(name, className);
	    } else if (scope.equals("request")) {
		beanInfo.addRequestBean(name, className);
	    } else if (scope.equals("session")) {
		beanInfo.addSessionBean(name,className);
	    } else if (scope.equals("application")) {
		beanInfo.addApplicationBean(name,className);
	    } else 
		err.jspError(n, "jsp.error.useBean.badScope");

	    visitBody(n);
	}

	public void visit(Node.PlugIn n) throws JasperException {
            JspUtil.checkAttributes("Plugin", n, plugInAttrs, err);

	    String type = n.getAttributeValue("type");
	    if (type == null)
		err.jspError(n, "jsp.error.plugin.notype");
	    if (!type.equals("bean") && !type.equals("applet"))
		err.jspError(n, "jsp.error.plugin.badtype");
	    if (n.getAttributeValue("code") == null)
		err.jspError(n, "jsp.error.plugin.nocode");
            
	    Node.JspAttribute width
		= getJspAttribute("width", null, null,
				  n.getAttributeValue("width"), n, false);
	    n.setWidth( width );
            
	    Node.JspAttribute height
		= getJspAttribute("height", null, null,
				  n.getAttributeValue("height"), n, false);
	    n.setHeight( height );

	    n.setHeight(getJspAttribute("height", null, null,
					n.getAttributeValue("height"), n,
					false));
	    n.setWidth(getJspAttribute("width", null, null,
				       n.getAttributeValue("width"), n,
				       false));
	    visitBody(n);
	}

	public void visit(Node.NamedAttribute n) throws JasperException {
	    JspUtil.checkAttributes("Attribute", n,
				    attributeAttrs, err);
            visitBody(n);
	}
        
	public void visit(Node.JspBody n) throws JasperException {
	    JspUtil.checkAttributes("Body", n,
				    bodyAttrs, err);
	    n.setValue(getJspAttribute("value", null, null,
				       n.getAttributeValue("value"), n,
				       false));
            visitBody(n);
	}
        
	public void visit(Node.Declaration n) throws JasperException {
	    if (! pageInfo.isScriptingEnabled()) {
		err.jspError(n.getStart(), "jsp.error.no.scriptlets");
	    }
	}

        public void visit(Node.Expression n) throws JasperException {
	    if (! pageInfo.isScriptingEnabled()) {
		err.jspError(n.getStart(), "jsp.error.no.scriptlets");
	    }
	}

        public void visit(Node.Scriptlet n) throws JasperException {
	    if (! pageInfo.isScriptingEnabled()) {
		err.jspError(n.getStart(), "jsp.error.no.scriptlets");
	    }
	}

	public void visit(Node.ELExpression n) throws JasperException {
            if ( pageInfo.isELEnabled() ) {
                JspUtil.validateExpressions(n.getStart(),
                    "${" + new String(n.getText()) + "}", err);
            }
        }

	public void visit(Node.CustomTag n) throws JasperException {
	    TagInfo tagInfo = n.getTagInfo();
	    if (tagInfo == null) {
		err.jspError(n, "jsp.error.missing.tagInfo", n.getName());
	    }

	    /*
	     * If the tag handler declares in the TLD that it supports dynamic
	     * attributes, it also must implement the DynamicAttributes
	     * interface.
	     */
	    if (tagInfo.hasDynamicAttributes()
		    && !n.implementsDynamicAttributes()) {
		err.jspError(n, "jsp.error.dynamic.attributes.not.implemented",
			     n.getName());
	    }
		
	    /*
	     * Make sure all required attributes are present, either as
             * attributes or named attributes (<jsp:attribute>).
	     */
	    TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
	    Attributes attrs = n.getAttributes();
	    for (int i=0; i<tldAttrs.length; i++) {
		if (tldAttrs[i].isRequired() &&
		    (attrs.getValue(tldAttrs[i].getName()) == null) &&
                    (n.getNamedAttributeNode(tldAttrs[i].getName()) == null) )
                {
		    err.jspError(n, "jsp.error.missing_attribute",
				 tldAttrs[i].getName(), n.getShortName());
		}
	    }

	    /*
	     * Make sure there are no invalid attributes
	     */
            Node.Nodes namedAttributeNodes = n.getNamedAttributeNodes();
	    Node.JspAttribute[] jspAttrs
		= new Node.JspAttribute[attrs.getLength()
				       + namedAttributeNodes.size()];
	    Hashtable tagDataAttrs = new Hashtable(attrs.getLength());
	    for (int i=0; i<attrs.getLength(); i++) {
		boolean found = false;
		for (int j=0; j<tldAttrs.length; j++) {
		    if (attrs.getQName(i).equals(tldAttrs[j].getName())) {
			if (tldAttrs[j].canBeRequestTime()) {
			    jspAttrs[i]
				= getJspAttribute(attrs.getQName(i),
						  attrs.getURI(i),
						  attrs.getLocalName(i),
						  attrs.getValue(i),
						  n,
						  false);
			} else {
			    jspAttrs[i]
				= new Node.JspAttribute(attrs.getQName(i),
							attrs.getURI(i),
							attrs.getLocalName(i),
							attrs.getValue(i),
							false,
							false,
							false);
			}
			if (jspAttrs[i].isExpression()) {
			    tagDataAttrs.put(attrs.getQName(i),
					     TagData.REQUEST_TIME_VALUE);
			} else {
			    tagDataAttrs.put(attrs.getQName(i),
					     attrs.getValue(i));
			}
			found = true;
			break;
		    }
		}
		if (!found) {
		    if (tagInfo.hasDynamicAttributes()) {
			jspAttrs[i] = getJspAttribute(attrs.getQName(i),
						      attrs.getURI(i),
						      attrs.getLocalName(i),
						      attrs.getValue(i),
						      n,
						      true);
		    } else {
			err.jspError(n, "jsp.error.bad_attribute",
				     attrs.getQName(i));
		    }
		}
	    }
            
	    /*
	     * Make sure there are no invalid named attributes
	     */
	    for (int i=0; i<namedAttributeNodes.size(); i++) {
                Node.NamedAttribute na = 
                    (Node.NamedAttribute)namedAttributeNodes.getNode( i );
		boolean found = false;
		for (int j=0; j<tldAttrs.length; j++) {
		    if (na.getName().equals(tldAttrs[j].getName())) {
			if (tldAttrs[j].canBeRequestTime()) {
			    jspAttrs[attrs.getLength() + i]
				= getJspAttribute(na.getName(), null, null,
						  null, n, false);
			} else {
                            err.jspError( n, 
                                "jsp.error.named.attribute.not.rt",
                                na.getName() );
			}
                        tagDataAttrs.put(na.getName(),
                                         TagData.REQUEST_TIME_VALUE);
			found = true;
			break;
		    }
		}
		if (!found) {
		    if (tagInfo.hasDynamicAttributes()) {
			jspAttrs[attrs.getLength() + i]
			    = getJspAttribute(na.getName(), null, null, null,
					      n, true);
		    } else {
			err.jspError(n, "jsp.error.bad_attribute",
				     na.getName());
		    }
		}
	    }

	    TagData tagData = new TagData(tagDataAttrs);
	    n.setTagData(tagData);
	    n.setJspAttributes(jspAttrs);

	    visitBody(n);
	}

	/**
	 * Preprocess attributes that can be expressions.  Expression
	 * delimiters are stripped.
         * <p>
         * If value is null, checks if there are any
         * NamedAttribute subelements in the tree node, and if so,
         * constructs a JspAttribute out of a child NamedAttribute node.
	 */
	private Node.JspAttribute getJspAttribute(String qName,
						  String uri,
						  String localName,
						  String value,
                                                  Node n,
						  boolean dynamic)
                throws JasperException {

            Node.JspAttribute result = null;

	    // XXX Is it an error to see "%=foo%" in non-Xml page?
	    // (We won't see "<%=foo%> in xml page because '<' is not a
	    // valid attribute value in xml).

            if (value != null) {
                if (n.isXmlSyntax() && value.startsWith("%=")) {
                    result = new Node.JspAttribute(
                                        qName,
					uri,
					localName,
					value.substring(2, value.length()-1),
					true,
					false,
					dynamic);
                }
                else if(!n.isXmlSyntax() && value.startsWith("<%=")) {
                    result = new Node.JspAttribute(
                                        qName,
					uri,
					localName,
					value.substring(3, value.length()-2),
					true,
					false,
					dynamic);
                }
                else {
                    // The attribute can contain expressions but is not an
                    // rtexprvalue; thus, we want to run it through the
                    // expression interpreter (final argument "true" in
                    // Node.JspAttribute constructor).
                    // XXX Optimize by directing generator to pass expressions
                    //     through interpreter only if they contain at least
                    //     one "${"?  (But ensure consistent type conversions
                    //     in JSP 1.3!)

                    // validate expression syntax if string contains
                    // expression(s)
                    if (value.indexOf("${") != -1 /* && isELEnabled */) {
                        JspUtil.validateExpressions(n.getStart(), value, err);
                        result = new Node.JspAttribute(qName, uri, localName,
						       value, false, true,
						       dynamic);
                    } else {
                        result = new Node.JspAttribute(qName, uri, localName,
						       value, false, false,
						       dynamic);
                    }
                }
            }
            else {
                // Value is null.  Check for any NamedAttribute subnodes
                // that might contain the value for this attribute.
                // Otherwise, the attribute wasn't found so we return null.

                Node.NamedAttribute namedAttributeNode =
                    n.getNamedAttributeNode( qName );
                if( namedAttributeNode != null ) {
                    result = new Node.JspAttribute(qName, namedAttributeNode,
						   dynamic);
                }
            }

            return result;
        }
    }

    /**
     * A visitor for validating TagExtraInfo classes of all tags
     */
    static class TagExtraInfoVisitor extends Node.Visitor {

	private PageInfo pageInfo;
	private ErrorDispatcher err;

	/*
	 * Constructor
	 */
	TagExtraInfoVisitor(Compiler compiler) {
	    this.pageInfo = compiler.getPageInfo();
	    this.err = compiler.getErrorDispatcher();
	}

	public void visit(Node.CustomTag n) throws JasperException {
	    TagInfo tagInfo = n.getTagInfo();
	    if (tagInfo == null) {
		err.jspError(n, "jsp.error.missing.tagInfo", n.getName());
	    }

	    ValidationMessage[] errors = tagInfo.validate(n.getTagData());
            if (errors != null && errors.length != 0) {
		StringBuffer errMsg = new StringBuffer();
                errMsg.append("<h3>");
                errMsg.append(err.getString("jsp.error.tei.invalid.attributes",
					    n.getName()));
                errMsg.append("</h3>");
                for (int i=0; i<errors.length; i++) {
                    errMsg.append("<p>");
		    if (errors[i].getId() != null) {
			errMsg.append(errors[i].getId());
			errMsg.append(": ");
		    }
                    errMsg.append(errors[i].getMessage());
                    errMsg.append("</p>");
                }

		err.jspError(n, errMsg.toString());
            }

	    visitBody(n);
	}
    }

    public static void validate(Compiler compiler,
				Node.Nodes page) throws JasperException {

	/*
	 * Visit the page/tag directives first, as they are global to the page
	 * and are position independent.
	 */
	page.visit(new DirectiveVisitor(compiler));

	// Determine the default output content type, per errata_a
	// http://jcp.org/aboutJava/communityprocess/maintenance/jsr053/errata_1_2_a_20020321.html
	PageInfo pageInfo = compiler.getPageInfo();
	String contentType = pageInfo.getContentType();
	if (!pageInfo.isTagFile() && 
		(contentType == null || contentType.indexOf("charset=") < 0)) {
	    boolean isXml = page.getRoot().isXmlSyntax();
	    String defaultType;
	    if (contentType == null) {
		defaultType = isXml? "text/xml": "text/html";
	    } else {
		defaultType = contentType;
	    }
	    String charset = pageInfo.getPageEncoding();
	    if (charset == null)
		charset = isXml? "UTF-8": "ISO-8859-1";
	    pageInfo.setContentType(defaultType + ";charset=" + charset);
	}

	/*
	 * Validate all other nodes.
	 * This validation step includes checking a custom tag's mandatory and
	 * optional attributes against information in the TLD (first validation
	 * step for custom tags according to JSP.10.5).
	 */
	page.visit(new ValidateVisitor(compiler));

	/*
	 * Invoke TagLibraryValidator classes of all imported tags
	 * (second validation step for custom tags according to JSP.10.5).
	 */
	validateXmlView(new PageDataImpl(page), compiler);

	/*
	 * Invoke TagExtraInfo method isValid() for all imported tags 
	 * (third validation step for custom tags according to JSP.10.5).
	 */
	page.visit(new TagExtraInfoVisitor(compiler));

    }


    //*********************************************************************
    // Private (utility) methods

    /**
     * Validate XML view against the TagLibraryValidator classes of all
     * imported tag libraries.
     */
    private static void validateXmlView(PageData xmlView, Compiler compiler)
	        throws JasperException {

	StringBuffer errMsg = null;
	ErrorDispatcher errDisp = compiler.getErrorDispatcher();

        Enumeration enum = compiler.getPageInfo().getTagLibraries().elements();
        while (enum.hasMoreElements()) {
            TagLibraryInfo tli = (TagLibraryInfo) enum.nextElement();
	    if (!(tli instanceof TagLibraryInfoImpl))
		continue;
	    ValidationMessage[] errors
		= ((TagLibraryInfoImpl) tli).validate(xmlView);
            if ((errors != null) && (errors.length != 0)) {
                if (errMsg == null) {
		    errMsg = new StringBuffer();
		}
                errMsg.append("<h3>");
                errMsg.append(errDisp.getString("jsp.error.tlv.invalid.page",
						tli.getShortName()));
                errMsg.append("</h3>");
                for (int i=0; i<errors.length; i++) {
                    errMsg.append("<p>");
                    errMsg.append(errors[i].getId());
                    errMsg.append(": ");
                    errMsg.append(errors[i].getMessage());
                    errMsg.append("</p>");
                }
            }
        }

	if (errMsg != null) {
            errDisp.jspError(errMsg.toString());
	}
    }
}

