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

package javax.servlet.jsp.el;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.Map;

/**
 * <p>The interface for an expression-language validator and evaluator.
 * Classes that implement an expression language expose their functionality
 * via this interface.</p>
 *
 * <p>An instance of the ExpressionEvaluator can be obtained via the 
 * JspContext / PageContext</p>
 *
 * <p>The validate() and evaluate() methods must be thread-safe.  That is,
 * multiple threads may call these methods on the same ExpressionEvaluator
 * object simultaneously.  Implementations should synchronize access if
 * they depend on transient state.  Implementations should not, however,
 * assume that only one object of each ExpressionEvaluator type will be
 * instantiated; global caching should therefore be static.</p>
 *
 * <p>There are two variants of the evaluation method.  The most general one
 * uses a VariableResolver instance to resolve names into objects.  Most invocations
 * will likely use the variant that uses a jspContext object and uses the default
 * resolution rules.</p>
 */
public interface ExpressionEvaluator {

    /** 
     * Translation time validation of an expression. 
     *
     * @param expression The expression to be validated
     * @return null String if the expression 
     *     is valid; otherwise an error message. 
     */ 
    public String validate( String expression ); 


    /**
     * Evaluates the expression at request time.   This variant uses a JspContext object
     * that (implicitly) uses the default VariableResolver defined in the JSP 2.0 specification.
     * 
     * if the jspContext parameter is not a PageContext, the only implicit object available
     * is pageScope.
     * 
     * If the jspContext parameter is a PageContext, all the implicit objects described in
     * the specification are available.
     *
     * @param expression The expression to be evaluated
     * @param expectedType The expected type of the result of the evaluation
     * @param jspContext The context of the current evaluation, providing
     *      the source of data for implicit objects.
     * @param prefixMap A Map with keys containing prefixes and values being
     *     the URI corresponding to that prefix in the taglib machinery.
     * @param functionMap A Map with keys containing function names of
     *      the form "namespaceURI:function" and values as instances of
     *      java.lang.reflect.Method objects indicating the method to
     *      be invoked.  Can be null, in which case functions are not
     *      supported for this invocation.
     * @param defaultURI The default URI to use when a function is
     *      encountered with no namespace.
     * @exception JspException Thrown if the expression evaluation failed.
     */ 
    public Object evaluate( String expression, 
                            Class expectedType, 
                            JspContext jspContext,
                            Map prefixMap,
			    Map functionMap,
                            String defaultURI ) 
       throws JspException; 


    /** 
     * Evaluates the expression at request time.  This is the most general version that
     * uses a VariableResolver object.
     *
     * @param expression The expression to be evaluated
     * @param expectedType The expected type of the result of the evaluation
     * @param resolver The variableResolver object to use, providing
     *      the source of data for implicit objects.
     * @param prefixMap A Map with keys containing prefixes and values being
     *     the URI corresponding to that prefix in the taglib machinery.
     * @param functionMap A Map with keys containing function names of
     *      the form "namespaceURI:function" and values as instances of
     *      java.lang.reflect.Method objects indicating the method to
     *      be invoked.  Can be null, in which case functions are not
     *      supported for this invocation.
     * @param defaultURI  default URI to use when a function is
     *      encountered with no namespace.
     * @exception JspException Thrown if the expression evaluation failed.
     */ 
    public Object evaluate( String expression, 
                            Class expectedType, 
                            VariableResolver resolver,
                            Map prefixMap,
			    Map functionMap,
                            String defaultURI )
       throws JspException; 
} 
