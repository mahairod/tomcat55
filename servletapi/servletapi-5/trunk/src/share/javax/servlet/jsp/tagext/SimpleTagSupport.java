/*
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

import javax.servlet.jsp.JspContext;

/**
 * A base class for defining tag handlers implemeting SimpleTag.
 * <p>
 * The SimpleTagSupport class is a utility class intended to be used
 * as the base class for new simple tag handlers.  The SimpleTagSupport
 * class implements the SimpleTag interface and adds additional
 * convenience methods including getter methods for the properties in
 * SimpleTag.
 */
public class SimpleTagSupport 
    implements SimpleTag
{
    /** Reference to the enclosing tag */
    private JspTag parentTag;
    
    /** The JSP context for the upcoming tag invocation */
    private JspContext jspContext;
    
    /** The body of the tag */
    private JspFragment jspBody;
    
    /** 
     * Default processing of the tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     *
     * @see SimpleTag#doTag()
     */ 
    public int doTag() 
        throws javax.servlet.jsp.JspException 
    {
        return EVAL_PAGE;
    }
    
    /**
     * Sets the parent of this tag, for collaboration purposes.
     */
    public void setParent( JspTag parent ) {
        this.parentTag = parent;
    }
    
    /**
     * Returns the parent of this tag, for collaboration purposes.
     */ 
    public JspTag getParent() {
        return this.parentTag;
    }
    
    /**
     * Stores the provided page context in the protected 
     * jspContext field.
     * 
     * @see SimpleTag#setJspContext
     */
    public void setJspContext( JspContext pc ) {
        this.jspContext = pc;
    }
    
    /**
     * Returns the page context passed in by the container via 
     * setJspContext.
     */
    public JspContext getJspContext() {
        return this.jspContext;
    }
                
    /** 
     * Stores the provided JspFragment.
     *
     * @see SimpleTag#setJspBody
     */ 
    public void setJspBody( JspFragment jspBody ) {
        this.jspBody = jspBody;
    }
    
    /**
     * Returns the body pass in by the container via setJspBody
     */
    public JspFragment getJspBody() {
        return this.jspBody;
    }

    /**
     * Find the instance of a given class type that is closest to a given
     * instance.
     * This method uses the getParent method from the Tag and/or SimpleTag
     * interfaces.  This method is used for coordination among 
     * cooperating tags.
     *
     * <p>
     * The current version of the specification only provides one formal
     * way of indicating the observable type of a tag handler: its
     * tag handler implementation class, described in the tag-class
     * subelement of the tag element.  This is extended in an
     * informal manner by allowing the tag library author to
     * indicate in the description subelement an observable type.
     * The type should be a subtype of the tag handler implementation
     * class or void.
     * This addititional constraint can be exploited by a
     * specialized container that knows about that specific tag library,
     * as in the case of the JSP standard tag library.
     *
     * <p>
     * When a tag library author provides information on the
     * observable type of a tag handler, client programmatic code
     * should adhere to that constraint.  Specifically, the Class
     * passed to findAncestorWithClass should be a subtype of the
     * observable type.
     * 
     *
     * @param from The instance from where to start looking.
     * @param klass The subclass of JspTag or interface to be matched
     * @return the nearest ancestor that implements the interface
     * or is an instance of the class specified
     */
    public static final JspTag findAncestorWithClass(
	JspTag from, Class klass) 
    {
	boolean isInterface = false;

	if (from == null ||
	    klass == null ||
	    (!JspTag.class.isAssignableFrom(klass) &&
	     !(isInterface = klass.isInterface()))) {
	    return null;
	}

	for (;;) {
	    JspTag tag = null;
	    if( from instanceof SimpleTag ) {
		tag = ((SimpleTag)from).getParent();
	    }
	    else if( from instanceof Tag ) {
		tag = ((Tag)from).getParent();
	    }

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
}
