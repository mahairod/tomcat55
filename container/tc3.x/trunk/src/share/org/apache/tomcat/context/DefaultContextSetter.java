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


package org.apache.tomcat.context;

import org.apache.tomcat.core.*;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.util.*;
import org.apache.tomcat.deployment.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;


/**
 * Interceptor that sets a number of default properties in context.
 *
 * @author costin@dnt.ro
 */
public class DefaultContextSetter {

    public DefaultContextSetter() {
    }

    // sets: engineHeader, requestSecurityProvider, workDir, sessionManager, classPath, libPath
    public int handleContextInit(Context ctx) {
	setEngineHeader( ctx );
	if( ctx.getRequestSecurityProvider() == null ) 
	    ctx.setRequestSecurityProvider(DefaultRequestSecurityProvider.
					   getInstance());
	
	if( ctx.getWorkDir() == null)
	    setWorkDir(ctx);
	
	if( ctx.getSessionManager() == null ) 
	    ctx.setSessionManager( org.apache.tomcat.session.ServerSessionManager.getManager());

	

	//Was:        for (int i = 0; i < Constants.Context.CLASS_PATHS.length; i++) {

	// The names are defined by the spec, and if that change - a lot will
	// also have too change - there is no advantage in keeping them in Constants,
	ctx.addClassPath("WEB-INF/classes");
	ctx.addLibPath("WEB-INF/lib");
	return 0;
    }

	
    // -------------------- implementation
    /** Encoded ContextManager.getWorkDir() + host + port + path
     */
    private void setWorkDir(Context ctx ) {
	ContextManager cm=ctx.getContextManager();
	
	StringBuffer sb=new StringBuffer();
	sb.append(cm.getWorkDir());
	sb.append(File.separator);
	sb.append(cm.getHostName() );
	sb.append("_").append(cm.getPort());
	sb.append(URLEncoder.encode( ctx.getPath() ));
	
	ctx.setWorkDir( new File(sb.toString()));
    }
    
    private void setEngineHeader(Context ctx) {
        String engineHeader=ctx.getEngineHeader();

	if( engineHeader==null) {
	    /*
	     * Whoever modifies this needs to check this modification is
	     * ok with the code in com.jsp.runtime.ServletEngine or talk
	     * to akv before you check it in. 
	     */
	    // Default value for engine header
	    // no longer use core.properties - the configuration comes from
	    // server.xml or web.xml - no more properties.
	    StringBuffer sb=new StringBuffer();
	    sb.append(Constants.TOMCAT_NAME).append("/").append(Constants.TOMCAT_VERSION);
	    sb.append(" (").append(Constants.JSP_NAME).append(" ").append(Constants.JSP_VERSION);
	    sb.append("; ").append(Constants.SERVLET_NAME).append(" ");
	    sb.append(Constants.SERVLET_MAJOR).append(".").append(Constants.SERVLET_MINOR);
	    sb.append( "; Java " );
	    sb.append(System.getProperty("java.version")).append("; ");
	    sb.append(System.getProperty("os.name") + " ");
	    sb.append(System.getProperty("os.version") + " ");
	    sb.append(System.getProperty("os.arch") + "; java.vendor=");
	    sb.append(System.getProperty("java.vendor")).append(")");
	    engineHeader=sb.toString();
	}
	ctx.setEngineHeader( engineHeader );
    }
}
