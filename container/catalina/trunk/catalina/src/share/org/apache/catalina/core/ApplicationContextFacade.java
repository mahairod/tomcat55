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
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.catalina.core;


import java.io.InputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Binding;
import javax.naming.directory.DirContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.http.HttpServletRequest;


/**
 * Facade object which masks the internal <code>ApplicationContext</code>
 * object from the web application.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Revision$ $Date$
 */

public final class ApplicationContextFacade
    implements ServletContext {
        
    // ---------------------------------------------------------- Attributes
    /**
     * Cache Class object used for reflection.
     */
    private HashMap classCache;
    
    
    /**
     * Cache method object.
     */
    private HashMap objectCache;
    
    
    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( ApplicationContextFacade.class );

        
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContextFacade(ApplicationContext context) {
        super();
        this.context = context;
        
        classCache = new HashMap();
        objectCache = new HashMap();
        initClassCache();
    }
    
    
    private void initClassCache(){
        Class[] clazz = new Class[]{String.class};
        classCache.put("getContext", clazz);
        classCache.put("getMimeType", clazz);
        classCache.put("getResourcePaths", clazz);
        classCache.put("getResource", clazz);
        classCache.put("getResourceAsStream", clazz);
        classCache.put("getRequestDispatcher", clazz);
        classCache.put("getNamedDispatcher", clazz);
        classCache.put("getServlet", clazz);
        classCache.put("getInitParameter", clazz);
        classCache.put("setAttribute", new Class[]{String.class, Object.class});
        classCache.put("removeAttribute", clazz);
        classCache.put("getRealPath", clazz);
        classCache.put("getAttribute", clazz);
        classCache.put("log", clazz);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped application context.
     */
    private ApplicationContext context = null;
    


    // ------------------------------------------------- ServletContext Methods


    public ServletContext getContext(String uripath) {
        ServletContext theContext = (ServletContext)
            doPrivileged("getContext", new Object[]{uripath});
        if ((theContext != null) &&
            (theContext instanceof ApplicationContext)){
            theContext = ((ApplicationContext)theContext).getFacade();
        }
        return (theContext);
    }


    public int getMajorVersion() {
        return context.getMajorVersion();
    }


    public int getMinorVersion() {
        return context.getMinorVersion();
    }


    public String getMimeType(String file) {
        return (String)doPrivileged("getMimeType", new Object[]{file});
    }


    public Set getResourcePaths(String path) {
         return (Set)doPrivileged("getResourcePaths", new Object[]{path});
    }


    public URL getResource(String path)
        throws MalformedURLException {
        return (URL)doPrivileged("getResource", new Object[]{path});
    }


    public InputStream getResourceAsStream(String path) {
        return (InputStream)doPrivileged("getResourceAsStream", new Object[]{path});
    }


    public RequestDispatcher getRequestDispatcher(final String path) {
        return (RequestDispatcher)doPrivileged("getRequestDispatcher", new Object[]{path});
    }


    public RequestDispatcher getNamedDispatcher(String name) {
        return (RequestDispatcher)doPrivileged("getNamedDispatcher", new Object[]{name});
    }


    public Servlet getServlet(String name)
        throws ServletException {
       return (Servlet)doPrivileged("getServlet", new Object[]{name});
    }


    public Enumeration getServlets() {
        return (Enumeration)doPrivileged("getServlets", null);
    }


    public Enumeration getServletNames() {
        return (Enumeration)doPrivileged("getServletNames", null);
   }


    public void log(String msg) {
        doPrivileged("log", new Object[]{msg} );
    }


    public void log(Exception exception, String msg) {
        doPrivileged("log", new Class[]{Exception.class, String.class}, new Object[]{exception,msg});
    }


    public void log(String message, Throwable throwable) {
        doPrivileged("log", new Class[]{String.class, Throwable.class}, new Object[]{message, throwable});
    }


    public String getRealPath(String path) {
        return (String)doPrivileged("getRealPath", new Object[]{path});
    }


    public String getServerInfo() {
        return (String)doPrivileged("getServerInfo", null);
    }


    public String getInitParameter(String name) {
        return (String)doPrivileged("getInitParameter", new Object[]{name});
    }


    public Enumeration getInitParameterNames() {
        return (Enumeration)doPrivileged("getInitParameterNames", null);
    }


    public Object getAttribute(String name) {
        return doPrivileged("getAttribute",new Object[]{name});
     }


    public Enumeration getAttributeNames() {
        return (Enumeration)doPrivileged("getAttributeNames", null);
    }


    public void setAttribute(String name, Object object) {
        doPrivileged("setAttribute", new Object[]{name,object});
    }


    public void removeAttribute(String name) {
        doPrivileged("removeAttribute", new Object[]{name});
    }


    public String getServletContextName() {
        return (String)doPrivileged("getServletContextName", null);
    }

       
    private Object doPrivileged(final String methodName, final Object[] params){
        return doPrivileged(context, methodName,params);
    }

    
    /**
     * Use reflection to invoke the requested method. Cache the method object 
     * to speed up the process
     * @param appContext The AppliationContext object on which the method
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private Object doPrivileged(ApplicationContext appContext,
                                final String methodName, 
                                final Object[] params){
        try{
            Method method = (Method)objectCache.get(methodName);
            if (method == null){
                method = appContext.getClass()
                    .getMethod(methodName, (Class[])classCache.get(methodName));
                objectCache.put(methodName, method);
            }
            
            return executeMethod(method,appContext,params);
        } catch (Throwable ex){
            Throwable exception;
            if (ex instanceof InvocationTargetException){
                exception = ((InvocationTargetException)ex).getTargetException();
            } else if (ex instanceof PrivilegedActionException){
                exception = ((PrivilegedActionException)ex).getException();
            } else {
                exception = ex;
            }   
            
            if (log.isErrorEnabled()){
                log.error("doPrivileged", exception);
            }
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    /**
     * Use reflection to invoke the requested method. Cache the method object 
     * to speed up the process
     * @param appContext The AppliationContext object on which the method
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */    
    private Object doPrivileged(final String methodName, 
                                final Class[] clazz,
                                final Object[] params){
        try{
            Method method = context.getClass()
                    .getMethod(methodName, (Class[])clazz);
            return executeMethod(method,context,params);
        } catch (Throwable ex){
            Throwable exception;
            if (ex instanceof InvocationTargetException){
                exception = ((InvocationTargetException)ex).getTargetException();
            } else if (ex instanceof PrivilegedActionException){
                exception = ((PrivilegedActionException)ex).getException();
            } else {
                exception = ex;
            }   
            
            if (log.isErrorEnabled()){
                log.error("doPrivileged", exception);
            }
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    
    /**
     * Executes the method of the specified <code>ApplicationContext</code>
     * @param method The method object to be invoked.
     * @param context The AppliationContext object on which the method
     *                   will be invoked
     * @param params The arguments passed to the called method.
     */
    private Object executeMethod(final Method method, 
                                 final ApplicationContext context,
                                 final Object[] params) 
            throws PrivilegedActionException, 
                   IllegalAccessException,
                   InvocationTargetException {
                                     
        if (System.getSecurityManager() != null){
           return AccessController.doPrivileged(new PrivilegedExceptionAction(){
                public Object run() throws IllegalAccessException, InvocationTargetException{
                    return method.invoke(context,  params);
                }
            });
        } else {
            return method.invoke(context, params);
        }        
    }
}
