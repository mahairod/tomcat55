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

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Constants;

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
class Validator {

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
	    new JspUtil.ValidAttribute("isELIgnored")
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
		} else if ("isELIgnored".equals(attr)) {
		    if (! pageInfo.isELIgnoredSpecified()) {
			// If specified in jsp-config, use it
			if ("true".equalsIgnoreCase(value))
			    pageInfo.setELIgnored(true);
			else if ("false".equalsIgnoreCase(value))
			    pageInfo.setELIgnored(false);
			else
			    err.jspError(n, "jsp.error.isELIgnored.invalid");
		    }
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
		    /*
		     * It is a translation-time error to name different page
		     * character encodings in two or more of the following:
		     * the XML prolog of a JSP page, the pageEncoding
		     * attribute of the page directive of the JSP page, and in
		     * a JSP configuration element (whose URL pattern matches
		     * the page).
		     *
		     * At this point, we've already verified (in 
		     * ParserController.parse()) that the page character
		     * encodings specified in a JSP config element and XML
		     * prolog match.
		     *
		     * Treat "UTF-16", "UTF-16BE", and "UTF-16LE" as identical.
		     */
		    String compareEnc = pageInfo.getPageEncoding();
		    if (!value.equals(compareEnc) 
			    && (!value.startsWith("UTF-16")
				|| !compareEnc.startsWith("UTF-16"))) {
			if (pageInfo.isXml()) {
			    err.jspError(n,
					 "jsp.error.prolog_pagedir_encoding_mismatch",
					 compareEnc, value);
			} else {
			    err.jspError(n,
					 "jsp.error.config_pagedir_encoding_mismatch",
					 compareEnc, value);
			}
		    }
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
		} else if ("isELIgnored".equals(attr)) {
		    if (! pageInfo.isELIgnoredSpecified()) {
			// If specified in jsp-config, use it
			if ("true".equalsIgnoreCase(value))
			    pageInfo.setELIgnored(true);
			else if ("false".equalsIgnoreCase(value))
			    pageInfo.setELIgnored(false);
			else
			    err.jspError(n, "jsp.error.isELIgnored.invalid");
		    }
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
        private ClassLoader loader;
	private Hashtable taglibs;

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
            
        private static final JspUtil.ValidAttribute[] invokeAttrs = {
            new JspUtil.ValidAttribute("fragment", true),
	    new JspUtil.ValidAttribute("var"),
	    new JspUtil.ValidAttribute("varReader"),
	    new JspUtil.ValidAttribute("scope") };

        private static final JspUtil.ValidAttribute[] doBodyAttrs = {
            new JspUtil.ValidAttribute("var"),
	    new JspUtil.ValidAttribute("varReader"),
	    new JspUtil.ValidAttribute("scope") };

	private static final JspUtil.ValidAttribute[] jspOutputAttrs = {
	    new JspUtil.ValidAttribute("omit-xml-declaration", true) };

	/*
	 * Constructor
	 */
	ValidateVisitor(Compiler compiler) {
	    this.pageInfo = compiler.getPageInfo();
	    this.taglibs = pageInfo.getTagLibraries();
	    this.err = compiler.getErrorDispatcher();
	    this.tagInfo = compiler.getCompilationContext().getTagInfo();
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
	    // make sure the value of the 'name' attribute is not a
	    // request-time expression
	    throwErrorIfExpression(n, "name", "jsp:param");
	    n.setValue(getJspAttribute("value", null, null,
				       n.getAttributeValue("value"),
                                       java.lang.String.class, null,
				       n, false));
            visitBody(n);
	}

	public void visit(Node.ParamsAction n) throws JasperException {
	    // Make sure we've got at least one nested jsp:param
            Node.Nodes subElems = n.getBody();
            if (subElems == null) {
		err.jspError(n, "jsp.error.params.emptyBody");
	    }
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
	    JspUtil.checkScope(scope, n, err);
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

	    beanInfo.addBean(n, name, className, scope);

	    visitBody(n);
	}

	public void visit(Node.PlugIn n) throws JasperException {
            JspUtil.checkAttributes("Plugin", n, plugInAttrs, err);

	    throwErrorIfExpression(n, "type", "jsp:plugin");
	    throwErrorIfExpression(n, "code", "jsp:plugin");
	    throwErrorIfExpression(n, "codebase", "jsp:plugin");
	    throwErrorIfExpression(n, "align", "jsp:plugin");
	    throwErrorIfExpression(n, "archive", "jsp:plugin");
	    throwErrorIfExpression(n, "hspace", "jsp:plugin");
	    throwErrorIfExpression(n, "jreversion", "jsp:plugin");
	    throwErrorIfExpression(n, "name", "jsp:plugin");
	    throwErrorIfExpression(n, "vspace", "jsp:plugin");
	    throwErrorIfExpression(n, "nspluginurl", "jsp:plugin");
	    throwErrorIfExpression(n, "iepluginurl", "jsp:plugin");

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
            visitBody(n);
	}
        
	public void visit(Node.Declaration n) throws JasperException {
	    if (pageInfo.isScriptingInvalid()) {
		err.jspError(n.getStart(), "jsp.error.no.scriptlets");
	    }
	}

        public void visit(Node.Expression n) throws JasperException {
	    if (pageInfo.isScriptingInvalid()) {
		err.jspError(n.getStart(), "jsp.error.no.scriptlets");
	    }
	}

        public void visit(Node.Scriptlet n) throws JasperException {
	    if (pageInfo.isScriptingInvalid()) {
		err.jspError(n.getStart(), "jsp.error.no.scriptlets");
	    }
	}

	public void visit(Node.ELExpression n) throws JasperException {
            if ( !pageInfo.isELIgnored() ) {
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

	public void visit(Node.UninterpretedTag n) throws JasperException {
            if (n.getNamedAttributeNodes().size() != 0) {
		err.jspError(n, "jsp.error.namedAttribute.invalidUse");
            }

	    visitBody(n);
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

	    // Get custom actions's namespace, which is used to validate the
	    // namespaces of any custom action attributes with qualified names
	    String customActionUri =
		((TagLibraryInfo) taglibs.get(n.getPrefix())).getURI();
		
	    /*
	     * Make sure all required attributes are present, either as
             * attributes or named attributes (<jsp:attribute>).
 	     * Also make sure that the same attribute is not specified in
	     * both attributes or named attributes.
	     */
	    TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
	    Attributes attrs = n.getAttributes();
	    for (int i=0; i<tldAttrs.length; i++) {
		String attr = attrs.getValue(tldAttrs[i].getName());
		if (attr == null) {
		    attr = attrs.getValue(customActionUri,
					  tldAttrs[i].getName());
		}
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
		for (int j=0; tldAttrs != null && j<tldAttrs.length; j++) {
		    /*
		     * A custom action and its declared attributes always
		     * belong to the same namespace, which is identified by
		     * the prefix name of the custom tag invocation.
		     * For example, in this invocation:
		     *     <my:test a="1" b="2" c="3"/>, the action
		     * "test" and its attributes "a", "b", and "c" all belong
		     * to the namespace identified by the prefix "my".
		     * The above invocation would be equivalent to:
		     *     <my:test my:a="1" my:b="2" my:c="3"/>
		     * An action attribute may have a prefix different from
		     * that of the action invocation only if the underlying
		     * tag handler supports dynamic attributes, in which case
		     * the attribute with the different prefix is considered a
		     * dynamic attribute.
		     */
		    if (attrs.getLocalName(i).equals(tldAttrs[j].getName())
			    && (attrs.getURI(i) == null
				|| attrs.getURI(i).length() == 0
				|| attrs.getURI(i) == customActionUri)) {
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
				        "jsp.error.attribute.custom.non_rt_with_expr",
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
				     attrs.getQName(i), n.getShortName());
		    }
		}
	    }
            
	    /*
	     * Make sure there are no invalid named attributes
	     */
	    for (int i=0; i<namedAttributeNodes.size(); i++) {
                Node.NamedAttribute na = 
                    (Node.NamedAttribute)namedAttributeNodes.getNode( i );
		String uri = "";
		if (na.getPrefix() != null) {
		    TagLibraryInfo tagLibInfo =
			(TagLibraryInfo) taglibs.get(na.getPrefix());
		    if (tagLibInfo == null) {
			err.jspError(n, "jsp.error.attribute.invalidPrefix",
				     na.getPrefix());
		    }
		    uri = tagLibInfo.getURI();
		}
		boolean found = false;
		for (int j=0; j<tldAttrs.length; j++) {
		    // See above comment about namespace matches
		    if (na.getLocalName().equals(tldAttrs[j].getName())
			    && (uri == null || uri.length() == 0
				|| uri == customActionUri)) {
			jspAttrs[attrs.getLength() + i]
			    = new Node.JspAttribute(na, false);
			NamedAttributeVisitor nav = null;
			if (na.getBody() != null) {
			    nav = new NamedAttributeVisitor();
			    na.getBody().visit(nav);
			}
			if (nav != null && nav.hasDynamicContent()) {
			    tagDataAttrs.put(na.getName(),
					     TagData.REQUEST_TIME_VALUE);
			} else {
			    tagDataAttrs.put(na.getName(), na.getText());    
			}
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
				     na.getName(), n.getShortName());
		    }
		}
	    }

	    TagData tagData = new TagData(tagDataAttrs);

	    // JSP.C1: It is a (translation time) error for an action that
	    // has one or more variable subelements to have a TagExtraInfo
	    // class that returns a non-null object.
	    TagExtraInfo tei = tagInfo.getTagExtraInfo();
	    if (tei != null
		    && tei.getVariableInfo(tagData) != null
		    && tei.getVariableInfo(tagData).length > 0
		    && tagInfo.getTagVariableInfos().length > 0) {
		err.jspError("jsp.error.non_null_tei_and_var_subelems",
			     n.getName());
	    }

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

	public void visit(Node.JspOutput n) throws JasperException {
            JspUtil.checkAttributes("jsp:output", n, jspOutputAttrs, err);

	    if (pageInfo.getOmitXmlDecl() != null) {
                err.jspError(n, "jsp.error.multiple.jspoutput");
	    }

	    pageInfo.setOmitXmlDecl(
			n.getAttributeValue("omit-xml-declaration"));
	}

	public void visit(Node.InvokeAction n) throws JasperException {

            JspUtil.checkAttributes("Invoke", n, invokeAttrs, err);

	    String scope = n.getTextAttribute ("scope");
	    JspUtil.checkScope(scope, n, err);

	    String var = n.getAttributeValue("var");
	    String varReader = n.getAttributeValue("varReader");
	    if (scope != null && var == null && varReader == null) {
		err.jspError(n, "jsp.error.missing_var_or_varReader");
	    }
	    if (var != null && varReader != null) {
		err.jspError(n, "jsp.error.var_and_varReader");
	    }
	}

	public void visit(Node.DoBodyAction n) throws JasperException {

            JspUtil.checkAttributes("DoBody", n, doBodyAttrs, err);

	    String scope = n.getTextAttribute ("scope");
	    JspUtil.checkScope(scope, n, err);

	    String var = n.getAttributeValue("var");
	    String varReader = n.getAttributeValue("varReader");
	    if (scope != null && var == null && varReader == null) {
		err.jspError(n, "jsp.error.missing_var_or_varReader");
	    }
	    if (var != null && varReader != null) {
		err.jspError(n, "jsp.error.var_and_varReader");
	    }
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
                    if (value.indexOf("${") != -1 && !pageInfo.isELIgnored()) {
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
			value = value.replace(Constants.ESC, '$');
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
	private boolean isExpression(Node n, String value) {
	    if ((n.isXmlSyntax() && value.startsWith("%="))
		    || (!n.isXmlSyntax() && value.startsWith("<%="))
   		    || (value.indexOf("${") != -1 && !pageInfo.isELIgnored()))
		return true;
	    else
		return false;
	}

	/*
	 * Throws exception if the value of the attribute with the given
	 * name in the given node is given as an RT or EL expression, but the
	 * spec requires a static value.
	 */
	private void throwErrorIfExpression(Node n, String attrName,
					    String actionName)
	            throws JasperException {
	    if (n.getAttributes() != null
		    && n.getAttributes().getValue(attrName) != null
		    && isExpression(n, n.getAttributes().getValue(attrName))) {
		err.jspError(n,
			     "jsp.error.attribute.standard.non_rt_with_expr",
			     attrName, actionName);
	    }
	}

	private static class NamedAttributeVisitor extends Node.Visitor {
	    private boolean hasDynamicContent;

	    public void doVisit(Node n) throws JasperException {
		if (!(n instanceof Node.JspText)
		        && !(n instanceof Node.TemplateText)) {
		    hasDynamicContent = true;
		}
		visitBody(n);
	    }
	    
	    public boolean hasDynamicContent() {
		return hasDynamicContent;
	    }
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
                errMsg.append(Localizer.getMessage("jsp.error.tei.invalid.attributes",
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

	// Determine the default output content type
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

	    String charset = null;
	    if (isXml) {
		charset = "UTF-8";
	    } else {
		charset = pageInfo.getPageEncoding();
		// The resulting charset might be null
	    }

	    if (charset != null) {
		pageInfo.setContentType(defaultType + ";charset=" + charset);
	    } else {
		pageInfo.setContentType(defaultType);
	    }
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
	validateXmlView(new PageDataImpl(page, compiler), compiler);

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
                errMsg.append(Localizer.getMessage("jsp.error.tlv.invalid.page",
						   tli.getShortName()));
                errMsg.append("</h3>");
                for (int i=0; i<errors.length; i++) {
		    if (errors[i] != null) {
			errMsg.append("<p>");
			errMsg.append(errors[i].getId());
			errMsg.append(": ");
			errMsg.append(errors[i].getMessage());
			errMsg.append("</p>");
		    }
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

