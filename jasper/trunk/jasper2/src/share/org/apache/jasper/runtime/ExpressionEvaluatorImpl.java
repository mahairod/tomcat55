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

import java.util.Map;
import javax.servlet.jsp.*;
import javax.servlet.jsp.el.*;
import org.apache.jasper.runtime.el.jstl.ELEvaluator;

/**
 * <p>An adapter for the JSTL Expression Evaluator.</p>
 * 
 * <p>Encapsulates and delegates to the JSTL evaluator, until the
 * JSTL evaluator APIs are up to date with JSP 2.0.</p>
 *
 * <p>Note: This is quite a hack at the moment.  This entire class needs to
 * be rewritten (and indeed may be obsoleted) once the EL interpreter moves
 * out of JSTL and in to its own project.
 *
 * @author Mark Roth
 */

public class ExpressionEvaluatorImpl 
    extends ExpressionEvaluator
{
    private PageContextImpl pageContext;

    /**
     * Create a new expression evaluator that delegates to the 
     * given evaluator.
     */
    public ExpressionEvaluatorImpl( PageContextImpl pageContext ) {
        this.pageContext = pageContext;
    }
    
    public Object evaluate(String expression, 
			   Class expectedType, 
			   VariableResolver vResolver,
			   FunctionMapper fMapper,
			   String defaultPrefix) 
        throws ELException 
    {
        org.apache.jasper.runtime.el.jstl.VariableResolver
            resolver = new JSTLVariableResolverWrapper( vResolver );
        Map fMapperMap = new FunctionMapperMap( fMapper );
        
        // XXX - This is currently inefficient.  A new evaluator,
        // JSTLVariableResolverWrapper, and FuntionMapperMap is created for 
        // each evaluate call.  Things should get better once the JSTL 
        // implementation is moved out of JSTL into its own project.
        try {
            return new ELEvaluator( resolver ).evaluate( 
                expression, this.pageContext, expectedType, fMapperMap,
                defaultPrefix );
        }
        catch( org.apache.jasper.runtime.el.jstl.ELException e ) {
            throw new ELException( e );
        }
    }

    public Expression parseExpression(String expression, 
				      Class expectedType, 
				      FunctionMapper fMapper,
				      String defaultPrefix) 
        throws ELException 
    {
        // Validate and then create an Expression object.
        String errorMessage =
            new org.apache.jasper.runtime.el.jstl.Evaluator().validate( 
            "<unknown>", expression );
        if( errorMessage != null ) {
            // Failed validation.  Tell user why.
            throw new ELException( errorMessage );
        }
        
        // Create an Expression object that knows how to evaluate this.
        return new JSTLExpression( expression, expectedType, fMapper, 
            defaultPrefix );
    }
    
    /**
     * Exposes a JSP 2.0 VariableResolver using the JSTL VariableResolver
     * interface.
     *
     * XXX - This class should be removed as soon as the EL implementation
     * can be moved out of JSTL into its own project.
     */
    private static class JSTLVariableResolverWrapper 
        implements org.apache.jasper.runtime.el.jstl.VariableResolver
    {
        private VariableResolver delegate;
        
        public JSTLVariableResolverWrapper( VariableResolver delegate ) {
            this.delegate = delegate;
        }
        
        public Object resolveVariable( String pName, Object pContext ) 
            throws org.apache.jasper.runtime.el.jstl.ELException
        {
            // pContext parameter is going away in JSP 2.0
            Object result;
            try {
                result = delegate.resolveVariable( pName );
            }
            catch( ELException e ) {
                throw new org.apache.jasper.runtime.el.jstl.ELException( 
                    e.getMessage() );
            }
            return result;
        }
    }

    /**
     * Exposes a FunctionMapper as a read-only Map.
     * Keys are Strings of the form 'prefix:localName' and values are of 
     * the type java.lang.reflect.Method.
     * Only get() and containsKey() are implemented.  The rest of the 
     * methods throw UnsupportedOperationException.
     *
     * XXX - This class should be removed as soon as the EL implementation
     * can be moved out of JSTL into its own project.
     * XXX - Strings are not i18n-ed, mainly because this class is going away
     * ant the Strings are for internal errors only.
     */
    private static class FunctionMapperMap
        implements Map 
    {
        private FunctionMapper delegate;
        
        public FunctionMapperMap( FunctionMapper delegate ) {
            this.delegate = delegate;
        }
        
        public void clear() {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.clear not implemented" );
        }
        
        public boolean containsKey(Object obj) {
            return get( obj ) != null;
        }
        
        public boolean containsValue(Object obj) {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.containsValue not implemented" );
        }
        
        public java.util.Set entrySet() {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.entrySet not implemented" );
        }
        
        public Object get(Object obj) {
            String key = (String)obj;
            int index = key.indexOf( ':' );
            String prefix = key.substring( 0, index );
            String localName = key.substring( index + 1 );
            return delegate.resolveFunction( prefix, localName );
        }
        
        public boolean isEmpty() {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.isEmpty not implemented" );
        }
        
        public java.util.Set keySet() {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.keySet not implemented" );
        }
        
        public Object put(Object obj, Object obj1) {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.put not implemented" );
        }
        
        public void putAll(java.util.Map map) {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.putAll not implemented" );
        }
        
        public Object remove(Object obj) {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.remove not implemented" );
        }
        
        public int size() {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.size not implemented" );
        }
        
        public java.util.Collection values() {
            throw new UnsupportedOperationException( 
                "FunctionMapperMap.values not implemented" );
        }
    }
    
    /**
     * An object that encapsulates an expression to be evaluated by 
     * the JSTL evaluator.
     *
     * XXX - This class should be removed as soon as the EL implementation
     * can be moved out of JSTL into its own project.
     */
    private class JSTLExpression 
        extends Expression
    {
        private String expression;
        private Class expectedType;
        private FunctionMapperMap fMapperMap;
        private String defaultPrefix;
        
        public JSTLExpression(String expression, Class expectedType, 
            FunctionMapper fMapper, String defaultPrefix)         
        {
            this.expression = expression;
            this.expectedType = expectedType;
            this.fMapperMap = new FunctionMapperMap( fMapper );
            this.defaultPrefix = defaultPrefix;
        }
        
        public Object evaluate( VariableResolver vResolver )
            throws ELException
        {
            org.apache.jasper.runtime.el.jstl.VariableResolver
                resolver = new JSTLVariableResolverWrapper( vResolver );

            // XXX - This is currently inefficient.  A new evaluator and
            // JSTLVariableResolverWrapper is created for 
            // each evaluate call.  Things should get better once the JSTL 
            // implementation is moved out of JSTL into its own project.
            try {
                return new ELEvaluator( resolver ).evaluate( 
                    this.expression, ExpressionEvaluatorImpl.this.pageContext, 
                    this.expectedType, this.fMapperMap,
                    this.defaultPrefix );
            }
            catch( org.apache.jasper.runtime.el.jstl.ELException e ) {
                throw new ELException( e );
            }
        }
    }
}
