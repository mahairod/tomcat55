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

package org.apache.jasper.runtime;

import java.io.IOException;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * Implementation of a JSP Context Wrapper.
 *
 * The JSP Context Wrapper is a JspContext created and maintained by a tag
 * handler implementation. It wraps the Invoking JSP Context, that is, the
 * JspContext instance passed to the tag handler by the invoking page via
 * setJspContext().
 *
 * @author Kin-man Chung
 */
public class JspContextWrapper extends PageContext implements VariableResolver {

    private PageContext pageContext;
    private transient Hashtable	pageAttributes;

    public JspContextWrapper(JspContext jspContext) {
        this.pageContext = (PageContext) jspContext;
	this.pageAttributes = new Hashtable(16);
    }

    public void initialize(Servlet servlet, ServletRequest request,
                           ServletResponse response, String errorPageURL,
                           boolean needsSession, int bufferSize,
                           boolean autoFlush)
        throws IOException, IllegalStateException, IllegalArgumentException
    {
    }
    
    public Object getAttribute(String name) {
	return pageAttributes.get(name);
    }

    public Object getAttribute(String name, int scope) {
	if (scope == PAGE_SCOPE) {
	    return pageAttributes.get(name);
	}

	return pageContext.getAttribute(name, scope);
    }

    public void setAttribute(String name, Object attribute) {
	pageAttributes.put(name, attribute);
    }

    public void setAttribute(String name, Object o, int scope) {
	if (scope == PAGE_SCOPE) {
	    pageAttributes.put(name, o);
	} else {
	    pageContext.setAttribute(name, o, scope);
	}
    }

    public Object findAttribute(String name) {
        Object o = pageAttributes.get(name);
        if (o != null)
            return o;

	return pageContext.findAttribute(name);
    }

    public void removeAttribute(String name, int scope) {
	if (scope == PAGE_SCOPE){
	    pageAttributes.remove(name);
	} else {
	    pageContext.removeAttribute(name, scope);
	}
    }

    public void removeAttribute(String name) {
	removeAttribute(name, PAGE_SCOPE);
	pageContext.removeAttribute(name);
    }

    public int getAttributesScope(String name) {
	if (pageAttributes.get(name) != null) {
	    return PAGE_SCOPE;
	} else {
	    return pageContext.getAttributesScope(name);
	}
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        if (scope == PAGE_SCOPE) {
            return pageAttributes.keys();
	}

	return pageContext.getAttributeNamesInScope(scope);
    }

    public void release() {
	pageContext.release();
    }

    public JspWriter getOut() {
	return pageContext.getOut();
    }

    public HttpSession getSession() {
	return pageContext.getSession();
    }

    public Object getPage() {
	return pageContext.getPage();
    }

    public ServletRequest getRequest() {
	return pageContext.getRequest();
    }

    public ServletResponse getResponse() {
	return pageContext.getResponse();
    }

    public Exception getException() {
	return pageContext.getException();
    }

    public ServletConfig getServletConfig() {
	return pageContext.getServletConfig();
    }

    public ServletContext getServletContext() {
	return pageContext.getServletContext();
    }

    public void forward(String relativeUrlPath)
        throws ServletException, IOException
    {
	pageContext.forward(relativeUrlPath);
    }

    public void include(String relativeUrlPath)
	throws ServletException, IOException
    {
	pageContext.include(relativeUrlPath);
    }

    public void include(String relativeUrlPath, boolean flush) 
	    throws ServletException, IOException {
	include(relativeUrlPath, false); // XXX
    }

    public VariableResolver getVariableResolver() {
	return null; // XXX
    }

    public BodyContent pushBody() {
	return pageContext.pushBody();
    }

    public JspWriter pushBody(Writer writer) {
	return pageContext.pushBody(writer);
    }

    public JspWriter popBody() {
        return pageContext.popBody();
    }

    public ExpressionEvaluator getExpressionEvaluator() {
	return pageContext.getExpressionEvaluator();
    }

    public void handlePageException(Exception ex)
        throws IOException, ServletException 
    {
	// Should never be called since handleException() called with a
	// Throwable in the generated servlet.
	handlePageException((Throwable) ex);
    }

    public void handlePageException(Throwable t)
        throws IOException, ServletException 
    {
	pageContext.handlePageException(t);
    }

    /**
     * VariableResolver interface
     */
    public Object resolveVariable( String pName, Object pContext )
        throws ELException
    {
	if (pageContext instanceof PageContextImpl) {
	    return ((PageContextImpl)pageContext).
			resolveVariable(pName, pContext);
	}

	return ((JspContextWrapper)pageContext).
			resolveVariable(pName, pContext);
    }
}
