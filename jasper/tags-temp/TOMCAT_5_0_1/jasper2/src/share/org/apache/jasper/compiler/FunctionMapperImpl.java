/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * 
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
package org.apache.jasper.compiler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.el.FunctionMapper;
import org.apache.jasper.JasperException;

class FunctionMapperImpl implements FunctionMapper {

    // Maps "prefix:name" to java.lang.Method objects
    private HashMap fnMap;

    // Maps "prefix:name" to FunctionSignature
    private HashMap fnSigMap;

    // Maps "prefix:name" to FunctionInfo
    private HashMap fnInfoMap;

    /*
     * Constructor
     */
    FunctionMapperImpl(Compiler compiler) throws JasperException {

	this.fnMap = new java.util.HashMap();
	this.fnSigMap = new java.util.HashMap();
	this.fnInfoMap = new java.util.HashMap();

	ErrorDispatcher err = compiler.getErrorDispatcher();
	Hashtable taglibs = compiler.getPageInfo().getTagLibraries();
	Iterator iter = taglibs.keySet().iterator();
	ClassLoader loader = compiler.getCompilationContext().getClassLoader();
	while (iter.hasNext()) {
	    String uri = (String) iter.next();
	    TagLibraryInfo tli = (TagLibraryInfo) taglibs.get(uri);
	    FunctionInfo[] fnInfos = tli.getFunctions();
	    for (int i = 0; fnInfos != null && i < fnInfos.length; i++) {
		FunctionSignature fnSig = 
		    new FunctionSignature(fnInfos[i].getFunctionSignature(),
					  tli.getShortName(), err, loader);
		Method method = null;
		try {
		    Class c = loader.loadClass(fnInfos[i].getFunctionClass());
		    method = c.getDeclaredMethod(fnSig.getMethodName(),
						 fnSig.getParameterTypes());
		} catch (Exception e) {
		    err.jspError(e);
		}

		String key = tli.getPrefixString() + ":"
		    + fnInfos[i].getName();
		this.fnMap.put(key, method);
		this.fnSigMap.put(key, fnSig);
		this.fnInfoMap.put(key, fnInfos[i]);
	    }
	}
    }

    /**
     * Resolves the specified local name and prefix into a Java.lang.Method.
     * Returns null if the prefix and local name are not found.
     * 
     * @param prefix the prefix of the function
     * @param localName the short name of the function
     * @return the result of the method mapping.  Null means no entry found.
     */
    public Method resolveFunction(String prefix, String localName) {
	return (Method) fnMap.get(prefix + ":" + localName);
    }

    /*
     */
    String getMethodName(String fnQName) {

	String result = null;

	FunctionSignature fnSig = (FunctionSignature) fnSigMap.get(fnQName);
	if (fnSig != null) {
	    result = fnSig.getMethodName();
	}

	return result;
    }

    /*
     */
    Class[] getParameterTypes(String fnQName) {

	Class[] result = null;

	FunctionSignature fnSig = (FunctionSignature) fnSigMap.get(fnQName);
	if (fnSig != null) {
	    result = fnSig.getParameterTypes();
	}

	return result;
    }

    /*
     */
    String getFunctionClass(String fnQName) {
	
	String result = null;

	FunctionInfo fnInfo = (FunctionInfo) this.fnInfoMap.get(fnQName);
	if (fnInfo != null) {
	    result = fnInfo.getFunctionClass();
	}

	return result;
    }

    Set keySet() {
	return fnMap.keySet();
    }

    boolean isEmpty() {
	return fnMap.isEmpty();
    }

    /**
     * Parses and encapsulates a function signature, as would appear in
     * a TLD.
     */
    private static class FunctionSignature {

        private String returnType;
        private String methodName;
        private Class[] parameterTypes;
        
        /**
         * Parses a function signature, as would appear in the TLD
         *
         * @param signature The signature to parse
         * @param tagName Name of tag, for error messages.
         * @throws JasperException If an error occurred while parsing the 
         *     signature.
         */
        public FunctionSignature(String signature, String tagName,
				 ErrorDispatcher err, ClassLoader loader)
            throws JasperException
        {
            try {
                // Parse function signature, assuming syntax:
                // <return-type> S <method-name> S? '('
                // ( <arg-type> ( ',' <arg-type> )* )? ')'
                String ws = " \t\n\r";
                StringTokenizer sigTokenizer = new StringTokenizer( 
                    signature, ws + "(),", true);

                // Return type:
                this.returnType = sigTokenizer.nextToken();

                // Skip whitespace and read <method-name>:
                do {
                    this.methodName = sigTokenizer.nextToken();
                } while( ws.indexOf( this.methodName ) != -1 );

                // Skip whitespace and read '(':
                String paren;
                do {
                    paren = sigTokenizer.nextToken();
                } while( ws.indexOf( paren ) != -1 );

                if( !paren.equals( "(" ) ) {
                    err.jspError("jsp.error.tld.fn.invalid.signature.parenexpected",
				 tagName, this.methodName);
                }

                // ( <arg-type> S? ( ',' S? <arg-type> S? )* )? ')'

                // Skip whitespace and read <arg-type>:
                String argType;
                do {
                    argType = sigTokenizer.nextToken();
                } while( ws.indexOf( argType ) != -1 );

                if( !argType.equals( ")" ) ) {
                    ArrayList parameterTypes = new ArrayList();
                    do {
                        if( ",(".indexOf( argType ) != -1 ) {
                            err.jspError("jsp.error.tld.fn.invalid.signature",
					 tagName, this.methodName);
                        }

                        parameterTypes.add(JspUtil.toClass(argType, loader));

                        String comma;
                        do {
                            comma = sigTokenizer.nextToken();
                        } while( ws.indexOf( comma ) != -1 );

                        if( comma.equals( ")" ) ) {
                            break;
                        }
                        if( !comma.equals( "," ) ) {
                            err.jspError("jsp.error.tld.fn.invalid.signature.commaexpected",
					 tagName, this.methodName);
                        }

                        // <arg-type>
                        do {
                            argType = sigTokenizer.nextToken();
                        } while( ws.indexOf( argType ) != -1 );
                    } while( true );
                    this.parameterTypes = (Class[])parameterTypes.toArray( 
                        new Class[parameterTypes.size()] );
                }
            }
            catch( NoSuchElementException e ) {
                err.jspError("jsp.error.tld.fn.invalid.signature",
			     tagName, this.methodName);
            }
            catch( ClassNotFoundException e ) {
                err.jspError("jsp.error.tld.fn.invalid.signature.classnotfound",
			     e.getMessage(), tagName, this.methodName);
            }
        }
        
        public String getReturnType() {
            return this.returnType;
        }
        
        public String getMethodName() {
            return this.methodName;
        }
        
        public Class[] getParameterTypes() {
            return this.parameterTypes;
        }    
    }
}
