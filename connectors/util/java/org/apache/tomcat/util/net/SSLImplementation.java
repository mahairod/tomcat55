/*
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

package org.apache.tomcat.util.net;

import java.io.*;
import java.net.*;

/* SSLImplementation:

   Abstract factory and base class for all SSL implementations.

   @author EKR
*/
abstract public class SSLImplementation {
    private static org.apache.commons.logging.Log logger =
        org.apache.commons.logging.LogFactory.getLog(SSLImplementation.class);

    // The default implementations in our search path
    private static final String PureTLSImplementationClass=
	"org.apache.tomcat.util.net.puretls.PureTLSImplementation";
    private static final String JSSEImplementationClass=
	"org.apache.tomcat.util.net.jsse.JSSEImplementation";
    
    private static final String[] implementations=
    {
        PureTLSImplementationClass,
        JSSEImplementationClass
    };

    public static SSLImplementation getInstance() throws ClassNotFoundException
    {
	for(int i=0;i<implementations.length;i++){
	    try {
               SSLImplementation impl=
		    getInstance(implementations[i]);
		return impl;
	    } catch (Exception e) {
		if(logger.isTraceEnabled()) 
		    logger.trace("Error creating " + implementations[i],e);
	    }
	}

	// If we can't instantiate any of these
	throw new ClassNotFoundException("Can't find any SSL implementation");
    }

    public static SSLImplementation getInstance(String className)
	throws ClassNotFoundException
    {
	if(className==null) return getInstance();

	try {
	    // Workaround for the J2SE 1.4.x classloading problem (under Solaris).
	    // Class.forName(..) fails without creating class using new.
	    // This is an ugly workaround. 
	    if( JSSEImplementationClass.equals(className) ) {
		return new org.apache.tomcat.util.net.jsse.JSSEImplementation();
	    }
	    Class clazz=Class.forName(className);
	    return (SSLImplementation)clazz.newInstance();
	} catch (Exception e){
	    if(logger.isDebugEnabled())
		logger.debug("Error loading SSL Implementation "
			     +className, e);
	    throw new ClassNotFoundException("Error loading SSL Implementation "
				      +className+ " :" +e.toString());
	}
    }

    abstract public String getImplementationName();
    abstract public ServerSocketFactory getServerSocketFactory();
    abstract public SSLSupport getSSLSupport(Socket sock);
}    
