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

import java.lang.reflect.Method;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Enumeration;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.el.FunctionMapper;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

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
	JspConfig.JspProperty jspProperty;

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
	DirectiveVisitor(Compiler compiler) throws JasperException {
	    this.pageInfo = compiler.getPageInfo();
	    this.err = compiler.getErrorDispatcher();
	    JspCompilationContext ctxt = compiler.getCompilationContext();
	    JspConfig jspConfig = ctxt.getOptions().getJspConfig();
	    if (jspConfig != null) {
		this.jspProperty = jspConfig.findJspProperty(ctxt.getJspFile());
	    }
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
		    // Make sure the page-encoding specified in a 
		    // jsp-property-group (if present) matches that of the page
		    // directive
		    if (jspProperty != null) {
			String jspConfigPageEnc = jspProperty.getPageEncoding();
			if (jspConfigPageEnc != null
			            && !jspConfigPageEnc.equals(value)) {
			    err.jspError(n,
					 "jsp.error.page.pageencoding.conflict",
					 jspConfigPageEnc, value);
			}
		    }
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
            // Note: Most of the validation is done in TagFileProcessor
            // when it created a TagInfo object from the
            // tag file in which the directive appeared.
        
            // This method does additional processing to collect page info
            
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
		} else if ("pageEncoding".equals(attr)) {
		    if (pageEncodingSeen) 
			err.jspError(n, "jsp.error.page.multiple.pageencoding");
		    pageEncodingSeen = true;
		    pageInfo.setPageEncoding(value);
		}
	    }

	    // Attributes for imports for this node have been processed by
	    // the parsers, just add them to pageInfo.
	    pageInfo.addImports(n.getImports());
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
	private TagInfo tagInfo;
	private TagData tagData;
        private ClassLoader loader;

	// A FunctionMapper, used to validate EL expressions.
        private FunctionMapper functionMapper;

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

        private static final JspUtil.ValidAttribute[] invokeAttrs = {
            new JspUtil.ValidAttribute("fragment", true),
	    new JspUtil.ValidAttribute("var"),
	    new JspUtil.ValidAttribute("varReader"),
	    new JspUtil.ValidAttribute("scope") };

        private static final JspUtil.ValidAttribute[] doBodyAttrs = {
            new JspUtil.ValidAttribute("var"),
	    new JspUtil.ValidAttribute("varReader"),
	    new JspUtil.ValidAttribute("scope") };

	/*
	 * Constructor
	 */
	ValidateVisitor(Compiler compiler) {
	    this.pageInfo = compiler.getPageInfo();
	    this.err = compiler.getErrorDispatcher();
	    this.tagInfo = compiler.getCompilationContext().getTagInfo();
	    this.tagData = compiler.getCompilationContext().getTagData();
	    this.loader = compiler.getCompilationContext().getClassLoader();
            this.functionMapper = new ValidatorFunctionMapper( this.pageInfo, 
                this.err, this.loader );
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
                                       java.lang.String.class, null,
				       n, false));
            visitBody(n);
	}

	public void visit(Node.IncludeAction n) throws JasperException {
            JspUtil.checkAttributes("Include action", n,
                                    includeActionAttrs, err);
	    n.setPage(getJspAttribute("page", null, null,
				      n.getAttributeValue("page"), 
                                      java.lang.String.class, null, n, false));
	    visitBody(n);
        };

	public void visit(Node.ForwardAction n) throws JasperException {
            JspUtil.checkAttributes("Forward", n,
                                    forwardActionAttrs, err);
	    n.setPage(getJspAttribute("page", null, null,
				      n.getAttributeValue("page"), 
                                      java.lang.String.class, null, n, false));
	    visitBody(n);
	}

	public void visit(Node.GetProperty n) throws JasperException {
            JspUtil.checkAttributes("GetProperty", n,
                                    getPropertyAttrs, err);
	}

	public void visit(Node.SetProperty n) throws JasperException {
            JspUtil.checkAttributes("SetProperty", n,
                                    setPropertyAttrs, err);
	    String name = n.getTextAttribute("name");
	    String property = n.getTextAttribute("property");
	    String param = n.getTextAttribute("param");
	    String value = n.getAttributeValue("value");

            n.setValue(getJspAttribute("value", null, null, value, 
                java.lang.Object.class, null, n, false));

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

	    String name = n.getTextAttribute ("id");
	    String scope = n.getTextAttribute ("scope");
	    String className = n.getTextAttribute ("class");
	    String type = n.getTextAttribute ("type");
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
				  java.lang.String.class, null, n, false);
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

	    String type = n.getTextAttribute("type");
	    if (type == null)
		err.jspError(n, "jsp.error.plugin.notype");
	    if (!type.equals("bean") && !type.equals("applet"))
		err.jspError(n, "jsp.error.plugin.badtype");
	    if (n.getTextAttribute("code") == null)
		err.jspError(n, "jsp.error.plugin.nocode");
            
	    Node.JspAttribute width
		= getJspAttribute("width", null, null,
				  n.getAttributeValue("width"), 
                                  java.lang.String.class, null, n, false);
	    n.setWidth( width );
            
	    Node.JspAttribute height
		= getJspAttribute("height", null, null,
				  n.getAttributeValue("height"), 
                                  java.lang.String.class, null, n, false);
	    n.setHeight( height );

	    visitBody(n);
	}

	public void visit(Node.NamedAttribute n) throws JasperException {
	    JspUtil.checkAttributes("Attribute", n,
				    attributeAttrs, err);
            visitBody(n);
	}
        
	public void visit(Node.JspBody n) throws JasperException {
	    String defaultPrefix = null;
            JspUtil.checkAttributes("Body", n,
				    bodyAttrs, err);
            Node parent = n.getParent();
            if( parent instanceof Node.CustomTag ) {
                // Default prefix comes from parent custom tag's prefix.
                Node.CustomTag customTag = (Node.CustomTag)parent;
                defaultPrefix = customTag.getPrefix();
            }
	    n.setValue(getJspAttribute("value", null, null,
				       n.getAttributeValue("value"), 
                                       JspFragment.class, defaultPrefix, 
                                       n, false));
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
                JspUtil.validateExpressions(
                    n.getStart(),
                    "${" + new String(n.getText()) + "}", 
                    java.lang.String.class, // XXX - Should template text 
                                            // always evaluate to String?
                    this.functionMapper,
                    null,
                    err);
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
 	     * Also Make sure that the same attribute is not specified in
	     * both attributes or named attributes.
	     */
	    TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
	    Attributes attrs = n.getAttributes();
	    for (int i=0; i<tldAttrs.length; i++) {
		String attr = attrs.getValue(tldAttrs[i].getName());
		Node.NamedAttribute jspAttr =
			n.getNamedAttributeNode(tldAttrs[i].getName());
		
		if (tldAttrs[i].isRequired() &&
			attr == null && jspAttr == null) {
		    err.jspError(n, "jsp.error.missing_attribute",
				 tldAttrs[i].getName(), n.getShortName());
		}
		if (attr != null && jspAttr != null) {
		    err.jspError(n, "jsp.error.duplicate.name.jspattribute",
			tldAttrs[i].getName());
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
                            Class expectedType = String.class;
                            try {
                                String typeStr = tldAttrs[j].getTypeName();
                                if( tldAttrs[j].isFragment() ) {
                                    expectedType = JspFragment.class;
                                }
                                else if( typeStr != null ) {
                                    expectedType = JspUtil.toClass(typeStr,
								   loader);
                                }
                                jspAttrs[i]
                                    = getJspAttribute(attrs.getQName(i),
                                                      attrs.getURI(i),
                                                      attrs.getLocalName(i),
                                                      attrs.getValue(i),
                                                      expectedType,
                                                      n.getPrefix(),
                                                      n,
                                                      false);
                            }
                            catch( ClassNotFoundException e ) {
                                err.jspError(n, 
                                    "jsp.error.unknown_attribute_type",
                                    tldAttrs[j].getName(), 
                                    tldAttrs[j].getTypeName() );
                            }
			} else {
			    // Attribute does not accept any expressions.
			    // Make sure its value does not contain any.
			    if (isExpression(n, attrs.getValue(i))) {
                                err.jspError(n,
				        "jsp.error.attribute.non_rt_with_expr",
					     tldAttrs[j].getName());
			    }
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
						      java.lang.Object.class,
                                                      n.getPrefix(),
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
			jspAttrs[attrs.getLength() + i]
			    = new Node.JspAttribute(na, false);
                        tagDataAttrs.put(na.getName(),
                                         TagData.REQUEST_TIME_VALUE);
			found = true;
			break;
		    }
		}
		if (!found) {
		    if (tagInfo.hasDynamicAttributes()) {
			jspAttrs[attrs.getLength() + i]
			    = new Node.JspAttribute(na, true);
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

	public void visit(Node.JspElement n) throws JasperException {

	    Attributes attrs = n.getAttributes();
            Node.Nodes namedAttributeNodes = n.getNamedAttributeNodes();
	    Node.JspAttribute[] jspAttrs
		= new Node.JspAttribute[attrs.getLength()
				       + namedAttributeNodes.size()];

	    boolean nameSpecified = false;
	    for (int i=0; i<attrs.getLength(); i++) {
		if ("name".equals(attrs.getQName(i))) {
		    nameSpecified = true;
		    jspAttrs[i] = getJspAttribute("name", null, null,
						  n.getAttributeValue("name"), 
						  java.lang.String.class, null,
						  n, false);
		} else {
		    jspAttrs[i] = getJspAttribute(attrs.getQName(i),
						  attrs.getURI(i),
						  attrs.getLocalName(i),
						  attrs.getValue(i),
						  java.lang.Object.class,
						  null,
						  n,
						  false);
		}
	    }
	    for (int i=0; i<namedAttributeNodes.size(); i++) {
                Node.NamedAttribute na = 
                    (Node.NamedAttribute) namedAttributeNodes.getNode(i);
		if ("name".equals(na.getName())) {
		    nameSpecified = true;
		}
		jspAttrs[attrs.getLength() + i]
		    = new Node.JspAttribute(na, false);
	    }

	    if (!nameSpecified) {
		err.jspError(n, "jsp.error.jspelement.missing.name");
	    }

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
                                                  Class expectedType,
                                                  String defaultPrefix,
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
                    // The attribute can contain expressions but is not a
                    // scriptlet expression; thus, we want to run it through 
                    // the expression interpreter (final argument "true" in
                    // Node.JspAttribute constructor).

                    // validate expression syntax if string contains
                    // expression(s)
                    if (value.indexOf("${") != -1 && pageInfo.isELEnabled()) {
                        JspUtil.validateExpressions(
                            n.getStart(),
                            value, 
                            expectedType, 
                            this.functionMapper,
                            defaultPrefix,
                            this.err);
                        
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
                    result = new Node.JspAttribute(namedAttributeNode,
						   dynamic);
                }
            }

            return result;
        }

	/*
	 * Checks to see if the given attribute value represents a runtime or
	 * EL expression.
	 */
	private boolean isExpression(Node.CustomTag n, String value) {
	    if ((n.isXmlSyntax() && value.startsWith("%="))
		    || (!n.isXmlSyntax() && value.startsWith("<%="))
   		    || (value.indexOf("${") != -1 && pageInfo.isELEnabled()))
		return true;
	    else
		return false;
	}

	public void visit(Node.InvokeAction n) throws JasperException {

            JspUtil.checkAttributes("Invoke", n, invokeAttrs, err);
	    if (n.getAttributeValue("var") != null
		    && n.getAttributeValue("varReader") != null) {
		err.jspError(n, "jsp.error.invoke.varAndVarReader");
	    }

	    Node.Nodes subelements = n.getBody();
	    if (subelements != null) {
		for (int i=0; i<subelements.size(); i++) {
		    Node subelem = subelements.getNode(i);
		    if (!(subelem instanceof Node.ParamAction)) {
			err.jspError(n, "jsp.error.invoke.invalidBodyContent");
		    }
		}
	    }

	    /*
	     * One <jsp:param> element must be present for each variable
	     * declared using the variable directive that has a 'fragment'
	     * attribute equal to the name of the fragment being invoked.
	     */
	    TagVariableInfo[] tagVars = tagInfo.getTagVariableInfos();
	    if (tagVars != null) {
		String frag = n.getAttributeValue("fragment");
		for (int i=0; i<tagVars.length; i++) {
		    String varName = tagVars[i].getNameGiven();
		    if (varName == null) {
			varName = tagData.getAttributeString(
			                tagVars[i].getNameFromAttribute());
		    }
		    String tagVarFrag = tagVars[i].getFragment();
		    if (tagVarFrag == null || !tagVarFrag.equals(frag))
			continue;
		    if (subelements == null) {
			err.jspError(n, "jsp.error.invoke.missingParam",
				     varName);
		    }
		    boolean found = false;
		    for (int j=0; j<subelements.size() && !found; j++) {
			Node subelem = subelements.getNode(j);
			String paramName = subelem.getAttributeValue("name");
			if (varName.equals(paramName)) {
			    found = true;
			}
		    }
		    if (!found) {
			err.jspError(n, "jsp.error.invoke.missingParam",
				     varName);
		    }
		}
	    }

            visitBody(n);
	}

	public void visit(Node.DoBodyAction n) throws JasperException {

            JspUtil.checkAttributes("DoBody", n, doBodyAttrs, err);
	    if (n.getAttributeValue("var") != null
		    && n.getAttributeValue("varReader") != null) {
		err.jspError(n, "jsp.error.doBody.varAndVarReader");
	    }

	    Node.Nodes subelements = n.getBody();
	    if (subelements != null) {
		for (int i=0; i<subelements.size(); i++) {
		    Node subelem = subelements.getNode(i);
		    if (!(subelem instanceof Node.ParamAction)) {
			err.jspError(n, "jsp.error.doBody.invalidBodyContent");
		    }
		}
	    }

	    /*
	     * A translation error must occur if a <jsp:param> is specified
	     * with the same name as a variable with a scope of AT_BEGIN or
	     * NESTED.
	     */
	    TagVariableInfo[] tagVars = tagInfo.getTagVariableInfos();
	    if (tagVars != null && subelements != null) {
		for (int i=0; i<tagVars.length; i++) {
		    if (tagVars[i].getScope() == VariableInfo.AT_END)
			continue;
		    String varName = tagVars[i].getNameGiven();
		    if (varName == null) {
			varName = tagData.getAttributeString(
			                tagVars[i].getNameFromAttribute());
		    }
		    for (int j=0; j<subelements.size(); j++) {
			Node subelem = subelements.getNode(j);
			String paramName = subelem.getAttributeValue("name");
			if (varName.equals(paramName)) {
			    err.jspError(n, "jsp.error.doBody.invalidParam",
					 varName);
			}
		    }
		}
	    }	    

            visitBody(n);
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
	if (!compiler.getCompilationContext().isTagFile() && 
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
   
    /**
     * A Function Mapper to be used by the validator to help in parsing
     * EL Expressions.  
     */
    private static class ValidatorFunctionMapper 
        implements FunctionMapper
    {
        private PageInfo pageInfo;
        private ErrorDispatcher err;
        private ClassLoader loader;

        /**
         * HashMap of cached functions that we already looked up.
         * Key = prefix, value = HashMap, where key = localName,
         * value = Method.
         */
        private HashMap cachedFunctions = new HashMap();
        
        public ValidatorFunctionMapper( PageInfo pageInfo, 
            ErrorDispatcher err, ClassLoader loader ) 
        {
            this.pageInfo = pageInfo;
            this.err = err;
	    this.loader = loader;
        }
        
        public Method resolveFunction( String prefix, String localName ) {
            boolean cached = false;
            Method result = null;
            
            // Look up entry in cache:
            HashMap cachedMethods = (HashMap)this.cachedFunctions.get( prefix );
            if( cachedMethods != null ) {
                if( cachedMethods.containsKey( localName ) ) {
                    result = (Method)cachedMethods.get( localName );
                    cached = true;
                }
            }
            
            // If not cached, look it up:
            if( !cached ) {
                Hashtable taglibraries = pageInfo.getTagLibraries();
                TagLibraryInfo info = 
                    (TagLibraryInfo)taglibraries.get( prefix );
                if( info != null ) {
                    FunctionInfo fnInfo = 
                        (FunctionInfo)info.getFunction( localName );
                    if( fnInfo != null ) {
                        String clazz = fnInfo.getFunctionClass();
                        try {
                            JspUtil.FunctionSignature functionSignature = 
                                new JspUtil.FunctionSignature( 
                                fnInfo.getFunctionSignature(),
                                info.getShortName(), this.err, this.loader );
                            Class c = Class.forName( clazz );
                            result = c.getDeclaredMethod( 
                                functionSignature.getMethodName(),
                                functionSignature.getParameterTypes() );
                        }
                        catch( JasperException e ) {
                            // return null.
                            // XXX - If the EL API evolves to allow for
                            // an exception to be thrown, we should provide
                            // details here.
                        }
                        catch( ClassNotFoundException e ) {
                            // return null.
                            // XXX - See above comment regarding detailed
                            // error reporting.
                        }
                        catch( NoSuchMethodException e ) {
                            // return null.
                            // XXX - See above comment regarding detailed
                            // error reporting.
                        }
                    }
                }
                
                // Store result in cache:
                if( cachedMethods == null ) {
                    cachedMethods = new HashMap();
                    this.cachedFunctions.put( prefix, cachedMethods );
                }
                cachedMethods.put( localName, result );
            }
            
            return result;
        }
    }
}

