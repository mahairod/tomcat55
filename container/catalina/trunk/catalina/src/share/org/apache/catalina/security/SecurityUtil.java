/** 
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.catalina.security;


import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.apache.catalina.util.StringManager;

/**
 * This utility class associates a <code>Subject</code> to the current 
 * <code>AccessControlContext</code>. When a <code>SecurityManager</code> is used, 
 * the container will always associate the called thread with an AccessControlContext
 * containing only the principal of the requested Servlet/Filter.
 *
 * This class uses reflection to invoke the invoke methods.
 *
 * @author Jean-Francois Arcand
 */

public class SecurityUtil{
    
    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( SecurityUtil.class );
    
    private static String PACKAGE = "org.apache.catalina.util";
    
    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(PACKAGE);    
    
    
    /**
     * Perform work as a particular </code>Subject</code>. Here the work
     * will be granted to a <code>null</code> subject. 
     *
     * @param methodName the method to apply the security restriction
     * @param targetObject the <code>Servlet</code> on which the method will be called.
     */
    public static void doAsPrivilege(final String methodName, 
                                     final Servlet targetObject) throws java.lang.Exception{
         doAsPrivilege(methodName, targetObject, null, null, null);                                
    }

    
    /**
     * Perform work as a particular </code>Subject</code>. Here the work
     * will be granted to a <code>null</code> subject. 
     *
     * @param methodName the method to apply the security restriction
     * @param targetObject the <code>Servlet</code> on which the method will be called.
     * @param targetType <code>Class</code> array used to instanciate a <code>Method</code> object.
     * @param targetObject <code>Object</code> array contains the runtime parameters instance.
     */
    public static void doAsPrivilege(final String methodName, 
                                     final Servlet targetObject, 
                                     final Class[] targetType,
                                     final Object[] targetArguments) throws java.lang.Exception{    
         doAsPrivilege(methodName, targetObject, targetType, targetArguments, null);                                
    }
    
    
    /**
     * Perform work as a particular </code>Subject</code>. Here the work
     * will be granted to a <code>null</code> subject. 
     *
     * @param methodName the method to apply the security restriction
     * @param targetObject the <code>Servlet</code> on which the method will be called.
     * @param targetType <code>Class</code> array used to instanciate a <code>Method</code> object.
     * @param targetObject <code>Object</code> array contains the runtime parameters instance.
     * @param principal the <code>Principal</code> to which the security privilege apply..
     */    
    public static void doAsPrivilege(final String methodName, 
                                     final Servlet targetObject, 
                                     final Class[] targetType,
                                     final Object[] targetArguments,
                                     Principal principal) throws java.lang.Exception{
       final Method method = targetObject.getClass().getMethod(methodName, targetType);   
       execute(method, targetObject, targetArguments, principal);
    }
 
    
    /**
     * Perform work as a particular </code>Subject</code>. Here the work
     * will be granted to a <code>null</code> subject. 
     *
     * @param methodName the method to apply the security restriction
     * @param targetObject the <code>Filter</code> on which the method will be called.
     */    
    public static void doAsPrivilege(final String methodName, 
                                     final Filter targetObject) throws java.lang.Exception{
         doAsPrivilege(methodName, targetObject, null, null);                                
    }
 
    
    /**
     * Perform work as a particular </code>Subject</code>. Here the work
     * will be granted to a <code>null</code> subject. 
     *
     * @param methodName the method to apply the security restriction
     * @param targetObject the <code>Filter</code> on which the method will be called.
     */    
    public static void doAsPrivilege(final String methodName, 
                                     final Filter targetObject, 
                                     final Class[] targetType,
                                     final Object[] targetArguments) throws java.lang.Exception{
       final Method method = targetObject.getClass().getMethod(methodName, targetType);   
       execute(method, targetObject, targetArguments, null);
    }
    
    
    /**
     * Perform work as a particular </code>Subject</code>. Here the work
     * will be granted to a <code>null</code> subject. 
     *
     * @param methodName the method to apply the security restriction
     * @param targetObject the <code>Servlet</code> on which the method will be called.
     * @param targetType <code>Class</code> array used to instanciate a <code>Method</code> object.
     * @param targetObject <code>Object</code> array contains the runtime parameters instance.
     * @param principal the <code>Principal</code> to which the security privilege apply..
     */    
    private static void execute(final Method method,
                                final Object targetObject, 
                                final Object[] targetArguments,
                                Principal principal) throws java.lang.Exception{
       try{   
           Subject subject = null;
           SecurityManager securityManager = System.getSecurityManager();
                PrivilegedExceptionAction pea = new PrivilegedExceptionAction()
                        {
                            public Object run() throws Exception{
                               method.invoke(targetObject, targetArguments);
                               return null;
                            }
                        };

            if (principal != null){
                subject = new Subject();
                subject.getPrincipals().add(principal);         
            }  

            Subject.doAsPrivileged(subject, pea, null);       
       } catch( PrivilegedActionException pe) {
            Throwable e = ((InvocationTargetException)pe.getException()).getTargetException();
            
            if (log.isDebugEnabled()){
                log.debug(sm.getString("SecurityUtil.doAsPrivilege"), e); 
            }
            
            if (e instanceof UnavailableException)
                throw (UnavailableException) e;
            else if (e instanceof ServletException)
                throw (ServletException) e;
            else if (e instanceof IOException)
                throw (IOException) e;
            else if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new ServletException(e.getMessage(), e);
        }  
    }
}
