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

/**
 * Encapsulates a portion of JSP code in an object that 
 * can be invoked as many times as needed.  JSP Fragments are defined 
 * using JSP syntax as the body of a tag or the body of a 
 * &lt;jsp:attribute&gt; standard action, during a tag invocation.
 * <p>
 * The definition of the JSP fragment must only contain template 
 * text and JSP action elements.  It must not contain, for example, 
 * scriptlets or scriptlet expressions.  At translation time, the 
 * container generates an implementation of the JspFragment interface
 * capable of executing the defined fragment.
 * <p>
 * A tag handler can invoke the fragment zero or more times, or 
 * pass it along to other tags, before returning.  JSP fragments accept 
 * parameters from the invoker, which are exposed as Expression Language 
 * variables to the JSP code that composes the fragment.  This allows the 
 * tag handler to parameterize the body each time it is invoked.
 * <p>
 * Note that tag library developers and page authors should not generate
 * JspFragment implementations manually.
 * <p>
 * <i>Implementation Note</i>: It is not necessary to generate a 
 * separate class for each fragment.  One possible implementation is 
 * to generate a single helper class for each page that implements 
 * JspFragment. Upon construction, a discriminator can be passed to 
 * select which fragment that instance will execute.
 */
public interface JspFragment {

    /**
     * Executes the fragment and directs all output to the given Writer,
     * or the JspWriter returned by the getOut() method of the JspContext
     * associated with the fragment if out is null.  The method accepts a 
     * parameter map, containing the body-input parameters passed to the 
     * body by its invoker (e.g. a tag handler).
     *
     * @param out The Writer to output the fragment to, or null if 
     *     output should be sent to JspContext.getOut().
     * @param params specifies the set of parameters to pass to the fragment.
     *     Keys in this map are parameter names, and the 
     *     values are parameter values.  This allows the invoker to 
     *     parameterize a fragment invocation.
     * @throws javax.servlet.jsp.JspException
     * @throws javax.servlet.jsp.SkipPageException Thrown if the page
     *     that (either directly or indirectly) invoked the tag handler that
     *     invoked this fragment is to cease evaluation.  The container
     *     must throw this exception if a Classic Tag Handler returned
     *     Tag.SKIP_PAGE or if a Simple Tag Handler threw SkipPageException.
     */
    public void invoke( java.io.Writer out, 
        java.util.Map params )
        throws javax.servlet.jsp.JspException;

}
