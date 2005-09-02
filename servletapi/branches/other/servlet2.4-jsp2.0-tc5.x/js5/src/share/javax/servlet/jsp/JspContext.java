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
 * To facilitate Simple Tag Extensions, the <code>pushPageScope()</code>,
 * <code>popPageScope()</code> and <code>peekPageScope()</code> methods are
 * added.
 *
 * <p><B>Methods Intended for JSP authors</B>
 * <p>
 * The following methods provide <B>convenient access</B> to implicit objects:
 * <ul>
 * <code>getOut()</code>
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

    /**
     * Register the name and object specified with page scope semantics.
     *
     * @param name the name of the attribute to set
     * @param attribute  the object to associate with the name
     * 
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
     * Return the object associated with the name in the page scope or null
     * if not found.
     *
     * @param name the name of the attribute to get
     * 
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the scope is invalid
     */

    abstract public Object getAttribute(String name);

    /**
     * Return the object associated with the name in the specified
     * scope or null if not found.
     *
     * @param name the name of the attribute to set
     * @param scope the scope with which to associate the name/object
     * 
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the scope is invalid */

    abstract public Object getAttribute(String name, int scope);

    /**
     * Searches for the named attribute in page, request, session (if valid),
     * and application scope(s) in order and returns the value associated or
     * null.
     *
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
     */

    abstract public void removeAttribute(String name, int scope);

    /**
     * Get the scope where a given attribute is defined.
     *
     * @return the scope of the object associated with the name specified or 0
     */

    abstract public int getAttributesScope(String name);

    /**
     * Enumerate all the attributes in a given scope
     *
     * @return an enumeration of names (java.lang.String) of all the attributes the specified scope
     */


    abstract public Enumeration getAttributeNamesInScope(int scope);

    /**
     * The current value of the out object (a JspWriter).
     *
     * @return the current JspWriter stream being used for client response
     */

    abstract public JspWriter getOut();

    /** 
     * Pops the page scope from the stack. After calling this method, the 
     * PageScope will appear the same as it was before the last call to 
     * pushPageScope. 
     * 
     * @return A Map representing the state of the page scope just before 
     *     it was popped.  This object can be passed to pushPageScope to 
     *     restore this state.  The keys of the returned Map are Strings 
     *     representing attribute names.  The values are the values of 
     *     those attributes. 
     * @throws java.util.EmptyStackException if this is the last page scope on the
     *     stack.
     */ 
    public abstract java.util.Map popPageScope() 
        throws java.util.EmptyStackException;

    /** 
     * Pushes a page scope on the stack.  The scopeState cannot be arbitrary.
     * Only a page scope returned from popPageScope() or peekPageScope() may 
     * be passed in.
     *
     * @param scopeState If null, a new, empty, page scope is pushed. 
     *     Otherwise, the state of the page scope is restored to the 
     *     contents of the provided Map. 
     */ 
    public abstract void pushPageScope( java.util.Map scopeState );
    
    /** 
     * Peeks at the top element of the page scope stack.  This value is 
     * the current state of the page scope.  Does not modify the state of 
     * the stack or copy any objects. 
     * 
     * @return A Map representing the state of the page scope currently 
     *     at the top of the stack.  This object can be passed to 
     *     pushPageScope to restore this state.  The keys of the returned 
     *     Map are Strings representing attribute names.  The values are 
     *     the values of those attributes. 
     */ 
    public abstract java.util.Map peekPageScope();
    
    /**
     * Provides programmatic access to the ExpressionEvaluator.
     * The JSP Container must return a valid instance of an 
     * ExpressionEvaluator that can parse EL expressions.
     */
    public abstract ExpressionEvaluator getExpressionEvaluator();
    
}
