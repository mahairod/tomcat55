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

import javax.servlet.jsp.*;


/**
 * Wraps any Object and exposes it using a Tag interface.  This is used
 * to allow collaboration between classic Tag handlers and SimpleTag
 * handlers.
 * <p>
 * Because SimpleTag does not extend Tag, and because Tag.setParent()
 * only accepts a Tag instance, a classic tag handler (one
 * that implements Tag) cannot have a SimpleTag as its parent.  To remedy
 * this, a TagAdapter is created to wrap the SimpleTag parent, and the
 * adapter is passed to setParent() instead.  A classic Tag Handler can
 * call getAdaptee() to retrieve the encapsulated SimpleTag instance.
 */

public class TagAdapter 
    implements Tag
{
    /** The tag that encloses this Tag */
    private Tag parentTag;
    
    /** The tag that's being adapted */
    private Object adaptee;
    
    /**
     * Creates a new TagAdapter that wraps the given tag and 
     * returns the given parent tag when getParent() is called.
     */
    public TagAdapter( Object adaptee, Tag parentTag ) {
        this.adaptee = adaptee;
        this.parentTag = parentTag;
    }
    
    /**
     * Must not be called.
     *
     * @throws UnsupportedOperationException
     */
    public void setPageContext(PageContext pc) {
        throw new UnsupportedOperationException( 
            "Illegal to invoke setPageContext() on TagAdapter wrapper" );
    }


    /**
     * Sets the value to be returned by getParent()
     *
     * @param t The parent tag, or null.
     */
    public void setParent( Tag parentTag ) {
        this.parentTag = parentTag;
    }


    /**
     * Returns the value passed to setParent().  
     * This will either be the enclosing Tag (if parent implements Tag),
     * or an adapter to the enclosing Tag (if parent does
     * not implement Tag).
     */
    public Tag getParent() {
        return this.parentTag;
    }
    
    /**
     * Sets the tag that is being adapted to the Tag interface.  
     * This should be an instance of SimpleTag in JSP 2.0, but room
     * is left for other kinds of tags in future spec versions.
     */
    public void setAdaptee( Object adaptee ) {
        this.adaptee = adaptee;
    }
    
    /**
     * Gets the tag that is being adapted to the Tag interface.
     * This should be an instance of SimpleTag in JSP 2.0, but room
     * is left for other kinds of tags in future spec versions.
     */
    public Object getAdaptee() {
        return this.adaptee;
    }

    /**
     * Must not be called.
     *
     * @throws UnsupportedOperationException
     */
    public int doStartTag() throws JspException {
        throw new UnsupportedOperationException( 
            "Illegal to invoke doStartTag() on TagAdapter wrapper" );
    }
 
    /**
     * Must not be called.
     *
     * @throws UnsupportedOperationException
     */
    public int doEndTag() throws JspException {
        throw new UnsupportedOperationException( 
            "Illegal to invoke doEndTag() on TagAdapter wrapper" );
    }

    /**
     * Must not be called.
     *
     * @throws UnsupportedOperationException
     */
    public void release() {
        throw new UnsupportedOperationException( 
            "Illegal to invoke release() on TagAdapter wrapper" );
    }

}
