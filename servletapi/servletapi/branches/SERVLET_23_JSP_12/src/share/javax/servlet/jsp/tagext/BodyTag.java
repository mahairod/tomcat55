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
 * The BodyTag interface extends IterationTag by defining additional
 * methods that let a tag handler manipulate the content of evaluating its body.
 *
 * <p>
 * It is the responsibility of the tag handler to manipulate the body
 * content.  For example the tag handler may take the body content,
 * convert it into a String using the bodyContent.getString
 * method and then use it.  Or the tag handler may take the body
 * content and write it out into its enclosing JspWriter using
 * the bodyContent.writeOut method.
 *
 * <p> A tag handler that implements BodyTag is treated as one that
 * implements IterationTag, except that the doStartTag method can
 * return SKIP_BODY, EVAL_BODY_INCLUDE or EVAL_BODY_BUFFERED.
 *
 * <p>
 * If EVAL_BODY_INCLUDE is returned, then evaluation happens
 * as in IterationTag.
 *
 * <p>
 * If EVAL_BODY_BUFFERED is returned, then a BodyContent object will be
 * created to capture the body evaluation. This object is obtained by
 * calling the pushBody method of the current pageContext, which
 * additionally has the effect of saving the previous out value.  The
 * object is returned through a call to the popBody method of the
 * PageContext class; the call also restores the value of out.
 *
 * <p>
 * The interface provides one new property with a setter method and one
 * new action method.
 *
 * <p> The new property is bodyContent, to contain the BodyContent
 * object, where the JSP Page implementation object will place the
 * evaluation (and reevaluation, if appropriate) of the body.  The setter
 * method (setBodyContent) will only be invoked if doStartTag() returns
 * EVAL_BODY_BUFFERED.
 *
 * <p> The new action methods is doInitBody(), which is invoked right after
 * setBodyContent() and before the body evaluation.  This method is only
 * invoked if doStartTag() returns EVAL_BODY_BUFFERED.
 *
 */

public interface BodyTag extends IterationTag {

    /**
     * Deprecated constant that has the same value as EVAL_BODY_BUFFERED
     * and EVAL_BODY_AGAIN.  This name has been marked as deprecated
     * to encourage the use of the two different terms, which are much
     * more descriptive.
     *
     * @deprecated	As of Java JSP API 1.2, use BodyTag.EVAL_BODY_BUFFERED
     * or IterationTag.EVAL_BODY_AGAIN.
     */
 
    public final static int EVAL_BODY_TAG = 2;

    /**
     * Request the creation of new buffer, a BodyContent on which to
     * evaluate the body of this tag.
     *
     * Returned from doStartTag when it implements BodyTag.
     * This is an illegal return value for doStartTag when the class
     * does not implement BodyTag.
     */

    public final static int EVAL_BODY_BUFFERED = 2;


    /**
     * Set the bodyContent property.
     * This method is invoked by the JSP page implementation object at
     * most once per action invocation.  The method will be invoked before
     * doInitBody and it will not be invoked if there is no body evaluation
     * (for example if doStartTag() returns EVAL_BODY_INCLUDE
     * or SKIP_BODY).
     *
     * <p>
     * When setBodyContent is invoked, the value of the implicit object out
     * has already been changed in the pageContext object.  The BodyContent
     * object passed will have not data on it but may have been reused
     * (and cleared) from some previous invocation.
     *
     * <p>
     * The BodyContent object is available and with the appropriate content
     * until after the invocation of the doEndTag method, at which case it
     * may be reused.
     *
     * @param b the BodyContent
     * @seealso #doInitBody
     * @seealso #doAfterBody
     */

    void setBodyContent(BodyContent b);


    /**
     * Prepare for evaluation of the body.
     * This method is invoked by the JSP page implementation object
     * after setBodyContent and before the first time
     * the body is to be evaluated. The method will not be invoked if there
     * is no body evaluation.
     *
     * <p>
     * The JSP container will resynchronize
     * any variable values that are indicated as so in TagExtraInfo after the
     * invocation of doInitBody().
     *
     * @throws JspException
     * @seealso #doAfterBody
     */

    void doInitBody() throws JspException;

}
