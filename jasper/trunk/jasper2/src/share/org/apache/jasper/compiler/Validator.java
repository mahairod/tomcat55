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

import java.util.ArrayList;
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

	private Node.Root oldPageDirectiveRoot = null;
	private Node.Root currentPageDirectiveRoot = null;

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

	    oldPageDirectiveRoot = currentPageDirectiveRoot;
	    currentPageDirectiveRoot = n.getRoot();
	    if (oldPageDirectiveRoot != currentPageDirectiveRoot) {
		pageEncodingSeen = false;
	    }

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
		} else if ("isThreadSafe".equals(attr)) {
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
			err.jspError(n,
				     "jsp.error.page.multiple.pageencoding");
		    pageEncodingSeen = true;
		    /*
		     * Report any encoding conflict, treating "UTF-16",
		     * "UTF-16BE", and "UTF-16LE" as identical.
		     */
		    comparePageEncodings(value, n);
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
		    n.getRoot().setPageEncoding(value);
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

	/*
	 * Compares the page encoding specified in the 'pageEncoding'
	 * attribute of the page directive with the encoding explicitly
	 * specified in the XML prolog (only for XML syntax) and the encoding
	 * specified in the JSP config element whose URL pattern matches the
	 * page, and throws an error in case of a mismatch.
	 */
	private void comparePageEncodings(String pageDirEnc,
					  Node.PageDirective n)
	            throws JasperException {

	    String configEnc = n.getRoot().getJspConfigPageEncoding();

	    if (configEnc != null && !pageDirEnc.equals(configEnc) 
		    && (!pageDirEnc.startsWith("UTF-16")
			|| !configEnc.startsWith("UTF-16"))) {
		err.jspError(n, "jsp.error.config_pagedir_encoding_mismatch",
			     configEnc, pageDirEnc);
	    }

	    if (n.getRoot().isXmlSyntax()
		    && n.getRoot().isEncodingSpecifiedInProlog()) {
		String pageEnc = n.getRoot().getPageEncoding();
		if (!pageDirEnc.equals(pageEnc) 
		        && (!pageDirEnc.startsWith("UTF-16")
			    || !pageEnc.startsWith("UTF-16"))) {
		    err.jspError(n, "jsp.error.prolog_pagedir_encoding_mismatch",
				 pageEnc, pageDirEnc);
		}
	    }
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
	    new JspUtil.ValidAttribute("omit-xml-declaration") };

	/*
	 * Constructor
	 */
	ValidateVisitor(Compiler compiler) {
	    this.pageInfo = compiler.getPageInfo();
	    this.taglibs = pageInfo.getTagLibraries();
	    this.err = compiler.getErrorDispatcher();
	    this.tagInfo = compiler.getCompilationContext().getTagInfo();
	    this.loader = compiler.getCompilationContext().getClassLoader();
	}

	public void visit(Node.JspRoot n) throws JasperException {
	    JspUtil.checkAttributes("Jsp:root", n,
				    jspRootAttrs, err);
	    String version = n.getTextAttribute("version");
	    if (!version.equals("1.2") && !version.equals("2.0")) {
		err.jspError(n, "jsp.error.jsproot.version.invalid", version);
	    }
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
	    // Either 'uri' or 'tagdir' attribute must be specified
	    String uri = n.getAttributeValue("uri");
	    String tagdir = n.getAttributeValue("tagdir");
	    if (uri == null && tagdir == null) {
		err.jspError(n, "jsp.error.taglibDirective.missing.location");
	    }
	    if (uri != null && tagdir != null) {
		err.jspError(n, "jsp.error.taglibDirective.both_uri_and_tagdir");
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
	    // Currently parseExpression does not validate functions, so
	    // a null FunctionMapper is passed.
            if ( !pageInfo.isELIgnored() ) {
		String expressions = "${" + new String(n.getText()) + "}";
		ELNode.Nodes el = ELParser.parse(expressions);
		validateFunctions(el, n);
                JspUtil.validateExpressions(
                    n.getStart(),
		    expressions,
                    java.lang.String.class, // XXX - Should template text 
                                            // always evaluate to String?
                    getFunctionMapper(el),
                    null,
                    err);
		n.setEL(el);
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
		err.jspError(n, "jsp.error.missing.tagInfo", n.getQName());
	    }

	    /*
	     * If the tag handler declares in the TLD that it supports dynamic
	     * attributes, it also must implement the DynamicAttributes
	     * interface.
	     */
	    if (tagInfo.hasDynamicAttributes()
		    && !n.implementsDynamicAttributes()) {
		err.jspError(n, "jsp.error.dynamic.attributes.not.implemented",
			     n.getQName());
	    }

	    /*
	     * Make sure all required attributes are present, either as
             * attributes or named attributes (<jsp:attribute>).
 	     * Also make sure that the same attribute is not specified in
	     * both attributes or named attributes.
	     */
	    TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
	    String customActionUri = n.getURI();
	    Attributes attrs = n.getAttributes();
	    for (int i=0; i<tldAttrs.length; i++) {
		String attr = attrs.getValue(tldAttrs[i].getName());
		if (attr == null) {
		    attr = attrs.getValue(customActionUri,
					  tldAttrs[i].getName());
		}
		Node.NamedAttribute na =
			n.getNamedAttributeNode(tldAttrs[i].getName());
		
		if (tldAttrs[i].isRequired() && attr == null && na == null) {
		    err.jspError(n, "jsp.error.missing_attribute",
				 tldAttrs[i].getName(), n.getLocalName());
		}
		if (attr != null && na != null) {
		    err.jspError(n, "jsp.error.duplicate.name.jspattribute",
			tldAttrs[i].getName());
		}
	    }

            Node.Nodes naNodes = n.getNamedAttributeNodes();
	    Node.JspAttribute[] jspAttrs
		= new Node.JspAttribute[attrs.getLength() + naNodes.size()];
	    Hashtable tagDataAttrs = new Hashtable(attrs.getLength());

	    checkXmlAttributes(n, jspAttrs, tagDataAttrs);
            checkNamedAttributes(n, jspAttrs, attrs.getLength(), tagDataAttrs);

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
			     n.getQName());
	    }

	    n.setTagData(tagData);
	    n.setJspAttributes(jspAttrs);

	    visitBody(n);
	}

	public void visit(Node.JspElement n) throws JasperException {

	    Attributes attrs = n.getAttributes();
	    int xmlAttrLen = attrs.getLength();
	    if (xmlAttrLen == 0) {
		err.jspError(n, "jsp.error.jspelement.missing.name");
	    }
            Node.Nodes namedAttrs = n.getNamedAttributeNodes();

	    // XML-style 'name' attribute, which is mandatory, must not be
	    // included in JspAttribute array
	    int jspAttrSize = xmlAttrLen-1 + namedAttrs.size();

	    Node.JspAttribute[] jspAttrs = new Node.JspAttribute[jspAttrSize];
	    int jspAttrIndex = 0;

	    // Process XML-style attributes
	    for (int i=0; i<xmlAttrLen; i++) {
		if ("name".equals(attrs.getLocalName(i))) {
		    n.setNameAttribute(getJspAttribute(attrs.getQName(i),
						       attrs.getURI(i),
						       attrs.getLocalName(i),
						       attrs.getValue(i),
						       java.lang.String.class,
						       null,
						       n,
						       false));
		} else {
		    if (jspAttrIndex<jspAttrSize) {
			jspAttrs[jspAttrIndex++]
			    = getJspAttribute(attrs.getQName(i),
					      attrs.getURI(i),
					      attrs.getLocalName(i),
					      attrs.getValue(i),
					      java.lang.Object.class,
					      null,
					      n,
					      false);
		    }
		}
	    }
	    if (n.getNameAttribute() == null) {
		err.jspError(n, "jsp.error.jspelement.missing.name");
	    }

	    // Process named attributes
	    for (int i=0; i<namedAttrs.size(); i++) {
                Node.NamedAttribute na = (Node.NamedAttribute) namedAttrs.getNode(i);
		jspAttrs[jspAttrIndex++] = new Node.JspAttribute(na, false);
	    }

	    n.setJspAttributes(jspAttrs);

	    visitBody(n);
	}

	public void visit(Node.JspOutput n) throws JasperException {
            JspUtil.checkAttributes("jsp:output", n, jspOutputAttrs, err);

	    if (n.getBody() != null) {
                err.jspError(n, "jsp.error.jspoutput.nonemptybody");
	    }

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

	    String var = n.getTextAttribute("var");
	    String varReader = n.getTextAttribute("varReader");
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

	    String var = n.getTextAttribute("var");
	    String varReader = n.getTextAttribute("varReader");
	    if (scope != null && var == null && varReader == null) {
		err.jspError(n, "jsp.error.missing_var_or_varReader");
	    }
	    if (var != null && varReader != null) {
		err.jspError(n, "jsp.error.var_and_varReader");
	    }
	}

	/*
	 * Make sure the given custom action does not have any invalid
	 * attributes.
	 *
	 * A custom action and its declared attributes always belong to the
	 * same namespace, which is identified by the prefix name of the
	 * custom tag invocation. For example, in this invocation:
	 *
	 *     <my:test a="1" b="2" c="3"/>, the action
	 *
	 * "test" and its attributes "a", "b", and "c" all belong to the
	 * namespace identified by the prefix "my". The above invocation would
	 * be equivalent to:
	 *
	 *     <my:test my:a="1" my:b="2" my:c="3"/>
	 *
	 * An action attribute may have a prefix different from that of the
	 * action invocation only if the underlying tag handler supports
	 * dynamic attributes, in which case the attribute with the different
	 * prefix is considered a dynamic attribute.
	 */
	private void checkXmlAttributes(Node.CustomTag n,
					Node.JspAttribute[] jspAttrs,
					Hashtable tagDataAttrs)
	        throws JasperException {

	    TagInfo tagInfo = n.getTagInfo();
	    if (tagInfo == null) {
		err.jspError(n, "jsp.error.missing.tagInfo", n.getQName());
	    }
	    TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
	    Attributes attrs = n.getAttributes();

	    for (int i=0; i<attrs.getLength(); i++) {
		boolean found = false;
		for (int j=0; tldAttrs != null && j<tldAttrs.length; j++) {
		    if (attrs.getLocalName(i).equals(tldAttrs[j].getName())
			    && (attrs.getURI(i) == null
				|| attrs.getURI(i).length() == 0
				|| attrs.getURI(i).equals(n.getURI()))) {
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
                            } catch (ClassNotFoundException e) {
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
							null,
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
				     attrs.getQName(i), n.getLocalName());
		    }
		}
	    }
	}

	/*
	 * Make sure the given custom action does not have any invalid named
	 * attributes
	 */
	private void checkNamedAttributes(Node.CustomTag n,
					  Node.JspAttribute[] jspAttrs,
					  int start,
					  Hashtable tagDataAttrs)
	        throws JasperException {

	    TagInfo tagInfo = n.getTagInfo();
	    if (tagInfo == null) {
		err.jspError(n, "jsp.error.missing.tagInfo", n.getQName());
	    }
	    TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            Node.Nodes naNodes = n.getNamedAttributeNodes();

	    for (int i=0; i<naNodes.size(); i++) {
                Node.NamedAttribute na = (Node.NamedAttribute)
		    naNodes.getNode(i);
		boolean found = false;
		for (int j=0; j<tldAttrs.length; j++) {
		    /*
		     * See above comment about namespace matches. For named
		     * attributes, we use the prefix instead of URI as the
		     * match criterion, because in the case of a JSP document,
		     * we'd have to keep track of which namespaces are in scope
		     * when parsing a named attribute, in order to determine
		     * the URI that the prefix of the named attribute's name
		     * matches to.
		     */
		    String attrPrefix = na.getPrefix();
		    if (na.getLocalName().equals(tldAttrs[j].getName())
			    && (attrPrefix == null || attrPrefix.length() == 0
				|| attrPrefix.equals(n.getPrefix()))) {
			jspAttrs[start + i] = new Node.JspAttribute(na, false);
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
			jspAttrs[start + i] = new Node.JspAttribute(na, true);
		    } else {
			err.jspError(n, "jsp.error.bad_attribute",
				     na.getName(), n.getLocalName());
		    }
		}
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
                if (n.getRoot().isXmlSyntax() && value.startsWith("%=")) {
                    result = new Node.JspAttribute(
                                        qName,
					uri,
					localName,
					value.substring(2, value.length()-1),
					true,
					null,
					dynamic);
                }
                else if(!n.getRoot().isXmlSyntax() && value.startsWith("<%=")) {
                    result = new Node.JspAttribute(
                                        qName,
					uri,
					localName,
					value.substring(3, value.length()-2),
					true,
					null,
					dynamic);
                }
                else {
                    // The attribute can contain expressions but is not a
                    // scriptlet expression; thus, we want to run it through 
                    // the expression interpreter (final argument "true" in
                    // Node.JspAttribute constructor).

                    // validate expression syntax if string contains
                    // expression(s)
                    ELNode.Nodes el = ELParser.parse(value);
                    if (el.containsEL() && !pageInfo.isELIgnored()) {
	                validateFunctions(el, n);
                        JspUtil.validateExpressions(
                            n.getStart(),
                            value, 
                            expectedType, 
                            getFunctionMapper(el),
                            defaultPrefix,
                            this.err);

                        
                        result = new Node.JspAttribute(qName, uri, localName,
						       value, false, el,
						       dynamic);
                    } else {
			value = value.replace(Constants.ESC, '$');
                        result = new Node.JspAttribute(qName, uri, localName,
						       value, false, null,
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
	    if ((n.getRoot().isXmlSyntax() && value.startsWith("%="))
		    || (!n.getRoot().isXmlSyntax() && value.startsWith("<%="))
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

	private String findUri(String prefix, Node n) {

	    for (Node p = n; p != null; p = p.getParent()) {
		Attributes attrs = p.getXmlnsAttributes();
		if (attrs == null) {
		    continue;
		}
		for (int i = 0; i < attrs.getLength(); i++) {
		    String name = attrs.getQName(i);
		    int k = name.indexOf(':');
		    if (prefix == null && k < 0) {
			// prefix not specified and a default ns found
			return attrs.getValue(i);
		    }   
		    if (prefix != null && k >= 0 &&
				prefix.equals(name.substring(k+1))) {
			return attrs.getValue(i);
		    }
		}
	    }
	    return null;
	}

	/**
	 * Validate functions in EL expressions
	 */
	private void validateFunctions(ELNode.Nodes el, Node n) 
		throws JasperException {

	    class FVVisitor extends ELNode.Visitor {

		Node n;

		FVVisitor(Node n) {
		    this.n = n;
		}

		public void visit(ELNode.Function func) throws JasperException {
		    String prefix = func.getPrefix();
		    String function = func.getName();
		    String uri = null;

		    if (n.getRoot().isXmlSyntax()) {
		        uri = findUri(prefix, n);
		    } else if (prefix != null) {
			Hashtable prefixMapper = pageInfo.getPrefixMapper();
			uri = (String) prefixMapper.get(prefix);
		    }

		    if (uri == null) {
			if (prefix == null) {
			    err.jspError(n, "jsp.error.noFunctionPrefix",
				function);
			}
			else {
			    err.jspError(n,
				"jsp.error.attribute.invalidPrefix", prefix);
			}
		    }
		    TagLibraryInfo taglib = 
					(TagLibraryInfo) taglibs.get(uri);
		    FunctionInfo funcInfo = null;
		    if (taglib != null) {
			funcInfo = taglib.getFunction(function);
		    }
		    if (funcInfo == null) {
			err.jspError(n, "jsp.error.noFunction", function);
		    }
		    // Skip TLD function uniqueness check.  Done by Schema ?
		    func.setUri(uri);
		    func.setFunctionInfo(funcInfo);
		    processSignature(func);
		}
	    }

	    el.visit(new FVVisitor(n));
	}

	private void processSignature(ELNode.Function func)
		throws JasperException {
	    func.setMethodName(getMethod(func));
	    func.setParameters(getParameters(func));
	}

	/**
	 * Get the method name from the signature.
	 */
	private String getMethod(ELNode.Function func)
		throws JasperException {
	    FunctionInfo funcInfo = func.getFunctionInfo();
	    String signature = funcInfo.getFunctionSignature();
	    
	    int start = signature.indexOf(' ');
	    if (start < 0) {
		err.jspError("jsp.error.tld.fn.invalid.signature",
			func.getPrefix(), func.getName());
	    }
	    int end = signature.indexOf('(');
	    if (end < 0) {
		err.jspError("jsp.error.tld.fn.invalid.signature.parenexpected",
			func.getPrefix(), func.getName());
	    }
	    return signature.substring(start+1, end).trim();
	}

	/**
	 * Get the parameters types from the function signature.
	 * @return An array of parameter class names
	 */
	private String[] getParameters(ELNode.Function func) 
		throws JasperException {
	    FunctionInfo funcInfo = func.getFunctionInfo();
	    String signature = funcInfo.getFunctionSignature();
	    ArrayList params = new ArrayList();
	    // Signature is of the form
	    // <return-type> S <method-name S? '('
	    // < <arg-type> ( ',' <arg-type> )* )? ')'
	    int start = signature.indexOf('(') + 1;
	    boolean lastArg = false;
	    while (true) {
		int p = signature.indexOf(',', start);
		if (p < 0) {
		    p = signature.indexOf(')', start);
		    if (p < 0) {
			err.jspError("jsp.error.tld.fn.invalid.signature",
				func.getPrefix(), func.getName());
		    }
		    lastArg = true;
		}
		params.add(signature.substring(start, p).trim());
		if (lastArg) {
		    break;
		}
		start = p+1;
	    }
	    return (String[]) params.toArray(new String[params.size()]);
	}

	private FunctionMapper getFunctionMapper(ELNode.Nodes el)
		throws JasperException {

	    class ValidateFunctionMapper implements FunctionMapper {

		private HashMap fnmap = new java.util.HashMap();
		public void mapFunction(String fnQName, Method method) {
		    fnmap.put(fnQName, method);
		}

		public Method resolveFunction(String prefix, String localName) {
		    return (Method) this.fnmap.get(prefix + ":" + localName);
		}
	    }

	    class MapperELVisitor extends ELNode.Visitor {
		ValidateFunctionMapper fmapper;

		MapperELVisitor(ValidateFunctionMapper fmapper) {
		    this.fmapper = fmapper;
		}

		public void visit(ELNode.Function n) throws JasperException {

		    Class c = null;
		    Method method = null;
		    try {
			c = loader.loadClass(
				n.getFunctionInfo().getFunctionClass());
		    } catch (ClassNotFoundException e) {
			err.jspError("jsp.error.function.classnotfound",
				n.getFunctionInfo().getFunctionClass(),
				n.getPrefix() + ':' + n.getName(),
				e.getMessage());
		    }
		    String paramTypes[] = n.getParameters();
		    int size = paramTypes.length;
		    Class params[] = new Class[size];
		    int i = 0;
		    try {
			for (i = 0; i < size; i++) {
			    params[i] = JspUtil.toClass(paramTypes[i], loader);
			}
			method = c.getDeclaredMethod(n.getMethodName(), params);
		    } catch (ClassNotFoundException e) {
			err.jspError("jsp.error.signature.classnotfound",
				paramTypes[i],
				n.getPrefix() + ':' + n.getName(),
				e.getMessage());
		    } catch (NoSuchMethodException e ) {
			err.jspError("jsp.error.noMethod", n.getName(),
				n.getMethodName(), c.getName());
		    }
		    fmapper.mapFunction(n.getPrefix() + ':' + n.getName(),
					method);
		}
	    }

	    ValidateFunctionMapper fmapper = new ValidateFunctionMapper();
	    el.visit(new MapperELVisitor(fmapper));
	    return fmapper;
	}
    } // End of ValidateVisitor

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
		err.jspError(n, "jsp.error.missing.tagInfo", n.getQName());
	    }

	    ValidationMessage[] errors = tagInfo.validate(n.getTagData());
            if (errors != null && errors.length != 0) {
		StringBuffer errMsg = new StringBuffer();
                errMsg.append("<h3>");
                errMsg.append(Localizer.getMessage("jsp.error.tei.invalid.attributes",
						   n.getQName()));
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
	if (contentType == null || contentType.indexOf("charset=") < 0) {
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
		charset = page.getRoot().getPageEncoding();
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
}

