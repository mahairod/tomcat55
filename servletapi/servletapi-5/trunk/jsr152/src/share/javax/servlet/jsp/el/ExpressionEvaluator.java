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
 */ 

package javax.servlet.jsp.el;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.Map;

/**
 * <p>The abstract base class for an expression-language evaluator.
 * Classes that implement an expression language expose their functionality
 * via this abstract class.</p>
 *
 * <p>An instance of the ExpressionEvaluator can be obtained via the 
 * JspContext / PageContext</p>
 *
 * <p>The parseExpression() and evaluate() methods must be thread-safe.  That is,
 * multiple threads may call these methods on the same ExpressionEvaluator
 * object simultaneously.  Implementations should synchronize access if
 * they depend on transient state.  Implementations should not, however,
 * assume that only one object of each ExpressionEvaluator type will be
 * instantiated; global caching should therefore be static.</p>
 *
 * <p>For JSP EL expressions, an expression string without '${' and '}' 
 * tokens is considered to be a static string.  One or more occurrences 
 * of '${' and '}' can be used in the expression string to delimit 
 * dynamic expressions.  Examples:
 * <ul>
 *   <li><code>${lastName}</code></li>
 *   <li><code>${8 * 8}</code></li>
 *   <li><code>Version ${major}.${minor}</code></li>
 *   <li><code>${my:reverse('hello')}</code></li>
 * </ul>
 * </p>
 *
 * @since 2.0
 */
public abstract class ExpressionEvaluator {

    /**
     * Prepare an expression for later evaluation.  This method should perform
     * syntactic validation of the expression; if in doing so it detects 
     * errors, it should raise an ELParseException.
     *
     * @param expression The expression to be evaluated.
     * @param expectedType The expected type of the result of the evaluation
     * @param fMapper A FunctionMapper to resolve functions found in 
     *     the expression.  It can be null, in which case no functions 
     *     are supported for this invocation.  The FunctionMapper will be
     *     invoked one or more times between parsing the expression and
     *     evaluating it, and must return a consistent value each time
     *     it is invoked.
     * @param defaultPrefix The default prefix to use when a function is
     *     encountered with no prefix.
     * @return The Expression object encapsulating the arguments.
     *
     * @exception ELException Thrown if parsing errors were found.
     */ 
    public abstract Expression parseExpression( String expression, 
				       Class expectedType, 
				       FunctionMapper fMapper,
				       String defaultPrefix ) 
      throws ELException; 


    /** 
     * Evaluates an expression.  This method may perform some syntactic 
     * validation and, if so, it should raise an ELParseException error if 
     * it encounters syntactic errors.  EL evaluation errors should cause 
     * an ELException to be raised.
     *
     * @param expression The expression to be evaluated.
     * @param expectedType The expected type of the result of the evaluation
     * @param vResolver A VariableResolver instance that can be used at 
     *     runtime to resolve the name of implicit objects into Objects.
     * @param fMapper A FunctionMapper to resolve functions found in 
     *     the expression.  It can be null, in which case no functions 
     *     are supported for this invocation.  The FunctionMapper will be
     *     invoked one or more times between parsing the expression and
     *     evaluating it, and must return a consistent value each time
     *     it is invoked.
     * @param defaultPrefix The default prefix to use when a function is
     *     encountered with no prefix.
     * @return The result of the expression evaluation.
     *
     * @exception ELException Thrown if the expression evaluation failed.
     */ 
    public abstract Object evaluate( String expression, 
			    Class expectedType, 
			    VariableResolver vResolver,
			    FunctionMapper fMapper,
			    String defaultPrefix ) 
      throws ELException; 
}

