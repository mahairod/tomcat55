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
 
package javax.servlet.jsp;

import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * <p>
 * <code>JspContext</code> serves as the base class for the 
 * PageContext class and abstracts all information that is not specific
 * to servlets.  This allows for Simple Tag Extensions to be used
 * outside of the context of a request/response Servlet.
 * <p>
 * The JspContext provides a number of facilities to the 
 * page/component author and page implementor, including:
 * <ul>
 * <li>a single API to manage the various scoped namespaces
 * <li>a mechanism to obtain the JspWriter for output
 * <li>a mechanism to expose page directive attributes to the 
 *     scripting environment
 * </ul>
 *
 * <p><B>Methods Intended for Container Generated Code</B>
 * <p>
 * The following methods enable the <B>management of nested</B> JspWriter 
 * streams to implement Tag Extensions: <code>pushBody()</code> and
 * <code>popBody()</code>
 *
 * <p><B>Methods Intended for JSP authors</B>
 * <p>
 * The following methods provide <B>convenient access</B> to implicit objects:
 * <ul>
 * <code>getOut()</code>
 *
 * @since JSP2.0
 */

public abstract class JspContext {

    /**
     * Page scope: (this is the default) the named reference remains available
     * in this JspContext until the return from the current Servlet.service()
     * invocation.
     */

    public static final int PAGE_SCOPE		= 1;

    /**
     * Request scope: the named reference remains available from the 
     * ServletRequest associated with the Servlet until the current 
     * request is completed.
     */

    public static final int REQUEST_SCOPE	= 2;

    /**
     * Session scope (only valid if this page participates in a session):
     * the named reference remains available from the HttpSession (if any)
     * associated with the Servlet until the HttpSession is invalidated.
     */

    public static final int SESSION_SCOPE	= 3;

    /**
     * Application scope: named reference remains available in the 
     * ServletContext until it is reclaimed.
     */

    public static final int APPLICATION_SCOPE	= 4;

    /** Register the name and object specified with page scope semantics.
     *
     * @param name the name of the attribute to set
     * @param attribute the object to associate with the name
     * @throws NullPointerException if the name or object is null
     */

    abstract public void setAttribute(String name, Object attribute);

    /**
     * register the name and object specified with appropriate scope semantics
     * 
     * @param name the name of the attribute to set
     * @param o    the object to associate with the name
     * @param scope the scope with which to associate the name/object
     * 
     * @throws NullPointerException if the name or object is null
     * @throws IllegalArgumentException if the scope is invalid
     *
     */

    abstract public void setAttribute(String name, Object o, int scope);

    /**
     * Returns the object associated with the name in the page scope or null
     * if not found.
     *
     * @param name the name of the attribute to get
     * @return the object associated with the name in the page scope 
     *     or null if not found.
     * 
     * @throws NullPointerException if the name is null
     */

    abstract public Object getAttribute(String name);

    /**
     * Return the object associated with the name in the specified
     * scope or null if not found.
     *
     * @param name the name of the attribute to set
     * @param scope the scope with which to associate the name/object
     * @return the object associated with the name in the specified
     *     scope or null if not found.
     * 
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the scope is invalid 
     */

    abstract public Object getAttribute(String name, int scope);

    /**
     * Searches for the named attribute in page, request, session (if valid),
     * and application scope(s) in order and returns the value associated or
     * null.
     *
     * @param name the name of the attribute to search for
     * @return the value associated or null
     */

    abstract public Object findAttribute(String name);

    /**
     * Remove the object reference associated with the given name,
     * look in all scopes in the scope order.
     *
     * @param name The name of the object to remove.
     */

    abstract public void removeAttribute(String name);

    /**
     * Remove the object reference associated with the specified name
     * in the given scope.
     *
     * @param name The name of the object to remove.
     * @param scope The scope where to look.
     * @throws IllegalArgumentException if the scope is invalid
     */

    abstract public void removeAttribute(String name, int scope);

    /**
     * Get the scope where a given attribute is defined.
     *
     * @param name the name of the attribute to return the scope for
     * @return the scope of the object associated with the name specified or 0
     */

    abstract public int getAttributesScope(String name);

    /**
     * Enumerate all the attributes in a given scope
     *
     * @param scope the scope to enumerate all the attributes for
     * @return an enumeration of names (java.lang.String) of all the 
     *     attributes the specified scope
     * @throws IllegalArgumentException if the scope is invalid
     */

    abstract public Enumeration getAttributeNamesInScope(int scope);

    /**
     * The current value of the out object (a JspWriter).
     *
     * @return the current JspWriter stream being used for client response
     */
    abstract public JspWriter getOut();
    
    /**
     * Provides programmatic access to the ExpressionEvaluator.
     * The JSP Container must return a valid instance of an 
     * ExpressionEvaluator that can parse EL expressions.
     *
     * @return A valid instance of an ExpressionEvaluator.
     * @since JSP2.0
     */
    public abstract ExpressionEvaluator getExpressionEvaluator();
    
    /**
     * Returns an instance of a VariableResolver that provides access to the
     * implicit objects specified in the JSP specification using this JspContext
     * as the context object
     *
     * @return A valid instance of a VariableResolver.
     * @since JSP2.0
     */
    public abstract VariableResolver getVariableResolver();
    
    /**
     * Return a new JspWriter object that sends output to the
     * provided Writer.  Saves the current "out" JspWriter,
     * and updates the value of the "out" attribute in the
     * page scope attribute namespace of the JspContext.
     * <p>The returned JspWriter must implement all methods and
     * behave as though it were unbuffered.  More specifically:
     * <ul>
     *   <li>clear() must throw an IOException</li>
     *   <li>clearBuffer() does nothing</li>
     *   <li>getBufferSize() always returns 0</li>
     *   <li>getRemaining() always returns 0</li>
     * </ul>
     * </p>
     *
     * @param writer The Writer for the returned JspWriter to send
     *     output to.
     * @return a new JspWriter that writes to the given Writer.
     * @since JSP2.0
     */
    public JspWriter pushBody( java.io.Writer writer ) {
        return null; // XXX to implement
    }
    
    /**
     * Return the previous JspWriter "out" saved by the matching
     * pushBody(), and update the value of the "out" attribute in
     * the page scope attribute namespace of the JspContext
     *
     * @return the saved JspWriter.
     */
    public JspWriter popBody() {
        return null; // XXX to implement
    }
}
