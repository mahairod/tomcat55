/*
 * $Header$ $Date$ $Revision$
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
package javax.servlet.jsp.tagext;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import javax.servlet.*;

import java.io.Writer;
import java.io.Serializable;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Actions in a Tag Library are defined through subclasses of Tag.
 */

public class TagSupport implements Tag, Serializable {

    /**
     * Find the instance of a given class type that is closest to a given
     * instance.
     * This class is used for coordination among cooperating tags.
     *
     * @param the subclass of Tag or interface to be matched
     * @return the nearest ancestor that implements the interface
     * or is an instance of the class specified
     */

    public static final Tag findAncestorWithClass(Tag from, Class klass) {
	boolean isInterface = false;

	if (from == null ||
	    klass == null ||
	    (!Tag.class.isAssignableFrom(klass) &&
	     !(isInterface = klass.isInterface()))) {
	    return null;
	}

	for (;;) {
	    Tag tag = from.getParent();

	    if (tag == null) {
		return null;
	    }

	    if ((isInterface && klass.isInstance(tag)) ||
	        klass.isAssignableFrom(tag.getClass()))
		return tag;
	    else
		from = tag;
	}
    }

    /**
     * Default constructor, all subclasses are required to only define
     * a public constructor with the same signature, and to call the
     * superclass constructor.
     *
     * This constructor is called by the code generated by the JSP
     * translator.
     *
     * @param libraryPrefix The namespace prefix used for this library.
     * For example "jsp:".
     * @param tagName The name of the element or yag, for example "useBean"
     */

    public TagSupport() { }

    /**
     * doStartTag(), doEndTag() are most basic.
     * setBodyOut(), doBeforeBody(), and doAfterBody() deal with body
     * 
     * In many cases not all of them are redefined.
     */
  
    // Actions for basic start/end processing.

    /**
     * Process the start tag for this instance.
     *
     * The doStartTag() method assumes that all setter methods have been
     * invoked before.
     *
     * When this method is invoked, the body has not yet been invoked.
     *
     * @returns EVAL_BODY_INCLUDE if the tag wants to process body, SKIP_BODY if it
     * does ont want to process it.
     */
 
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Process the end tag. This method will be called on all Tag objects.
     *
     * All instance state associated with this instance must be reset.
     */

    public int doEndTag() throws JspException {
	return EVAL_PAGE;
    }

    /**
     * release() called after doEndTag() to reset state
     */

    public void release() {
	parent          = null;
    }

    /**
     * Methods to access state
     */

    /**
     * Set the nesting tag of this tag.
     */

    public void setParent(Tag t) {
	parent = t;
    }

    /**
     * The Tag instance enclosing this tag instance.
     *
     * @return the parent tag instance or null
     */

    public Tag getParent() {
	return parent;
    }

    /**
     * Set the id attribute
     */

    public void setId(String id) {
	this.id = id;
    }

    /**
     * The value of the id attribute of this tag; or null.
     *
     * @return the value of the id attribute, or null
     */
    
    public String getId() {
	return id;
    }

    /**
     * set the page context
     */

    public void setPageContext(PageContext pageContext) {
	this.pageContext = pageContext;
    }

    /**
     * Set a value
     */

    public void setValue(String k, Object o) {
	if (values == null) {
	    values = new Hashtable();
	}
	values.put(k, o);
    }

    /**
     * Get a value
     */

    public Object getValue(String k) {
	if (values == null) {
	    return null;
	} else {
	    return values.get(k);
	}
    }

    /**
     * Remove a value
     */

    public void removeValue(String k) {
	if (values != null) {
	    values.remove(k);
	}
    }

    /**
     * Enumerate the values
     */

    public Enumeration getValues() {
	if (values == null) {
	    return null;
	}
	return values.keys();
    }

    // private fields

    private   Tag         parent;
    private   Hashtable   values;
    protected String	  id;

    // protected fields

    protected PageContext pageContext;
}

