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

package org.apache.jasper.runtime;

import java.util.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.el.*;
import javax.servlet.jsp.tagext.*;

/**
 * <p>A conduit to the JSTL EL.</p>
 * 
 * <p>This is a stripped-down version of the expression evaluator manager
 * found in the JSTL implementation.</p>
 *
 * @author Shawn Bayern
 * @author Mark Roth
 */
public class ExpressionEvaluatorManager { 

    //*********************************************************************
    // Constants

    public static final String EVALUATOR_CLASS =
        "org.apache.jasper.runtime.ExpressionEvaluatorImpl";

    //*********************************************************************
    // Internal, static state

    private static HashMap nameMap = new HashMap();

    //*********************************************************************
    // Public static methods

    /** 
     * Invokes the evaluate() method on the "active" ExpressionEvaluator
     * for the given pageContext.
     */ 
    public static Object evaluate( String expression, 
                                   Class expectedType, 
                                   JspContext jspContext,
				   Map prefixMap,
                                   Map elFunctions,
                                   String defaultPrefix ) 
	throws JspException // XXX EL (should throw ELException)
    {

        // the evaluator we'll use
        ExpressionEvaluator target = getEvaluatorByName(EVALUATOR_CLASS);

        // delegate the call
        Object ret = null;
	try {
	    ret = (target.evaluate(expression,
				   expectedType,
				   jspContext.getVariableResolver(),
				   null, // XXX EL (FunctionMapper)
				   defaultPrefix));
	} catch (ELException ele) {
	    throw new JspException(ele);
	}

	return ret;
    }

    /**
     * Gets an ExpressionEvaluator from the cache, or seeds the cache
     * if we haven't seen a particular ExpressionEvaluator before.
     */
    public static synchronized
	    ExpressionEvaluator getEvaluatorByName(String name)
            throws JspException {
        try {

            Object oEvaluator = nameMap.get(name);
            if (oEvaluator == null) {
                ExpressionEvaluator e = (ExpressionEvaluator)
                    Class.forName(name).newInstance();
                nameMap.put(name, e);
                return (e);
            } else
                return ((ExpressionEvaluator) oEvaluator);

        } catch (ClassCastException ex) {
            // just to display a better error message
            throw new JspException("invalid ExpressionEvaluator: " +
                ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            throw new JspException("couldn't find ExpressionEvaluator: " +
                ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            throw new JspException("couldn't access ExpressionEvaluator: " +
                ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            throw new JspException(
                "couldn't instantiate ExpressionEvaluator: " +
                ex.getMessage(), ex);
        }
    }

} 
