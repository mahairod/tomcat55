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
 * Interface for defining "simple tag handlers." 
 * <p>
 * Instead of supporting <code>doStartTag()</code> and <code>doEndTag()</code>, 
 * the <code>SimpleTag</code> interface provides a simple 
 * <code>doTag()</code> method, which is called once and only once for any 
 * given tag invocation.  All tag logic, iteration, body evaluations, etc. 
 * are to be performed in this single method.  Thus, simple tag handlers 
 * have the equivalent power of <code>IterationTag</code>, but with a much 
 * simpler lifecycle and interface.
 * <p>
 * To support body content, the <code>setJspBody()</code> 
 * method is provided.  The container invokes the <code>setJspBody()</code> 
 * method with a <code>JspFragment</code> object encapsulating the body of 
 * the tag.  The tag handler implementation can call 
 * <code>invoke()</code> on that fragment to evaluate the body as
 * many times as it needs.
 * 
 * @see SimpleTagSupport
 */

public interface SimpleTag extends JspTag {
    
    /**
     * Skip the rest of the page.
     * Valid return value for doTag().
     */
    public final static int SKIP_PAGE = 5;

    /**
     * Continue evaluating the page.
     * Valid return value for doTag().
     */
    public final static int EVAL_PAGE = 6;
    
    /** 
     * Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library
     * developer, and handles all tag processing, body iteration, etc.
     * 
     * @return SKIP_PAGE to abort the processing, or EVAL_PAGE to continue. 
     */ 
    public int doTag() 
        throws javax.servlet.jsp.JspException; 
    
    /**
     * Sets the parent of this tag, for collaboration purposes.
     */
    public void setParent( JspTag parent );
    
    /**
     * Returns the parent of this tag, for collaboration purposes.
     */ 
    public JspTag getParent();
    
    /**
     * Stores the provided page context in the protected 
     * jspContext field.
     * 
     * @see Tag#setPageContext
     */
    public void setJspContext( JspContext pc );
                
    /** 
     * Provides the body of this tag as a JspFragment object, able to be 
     * invoked zero or more times by the tag handler. 
     * <p>
     * This method is invoked by the JSP page implementation 
     * object prior to <code>doTag()</code>. 
     * 
     * @param body The fragment encapsulating the body of this tag. 
     */ 
    public void setJspBody( JspFragment jspBody );

    
}
