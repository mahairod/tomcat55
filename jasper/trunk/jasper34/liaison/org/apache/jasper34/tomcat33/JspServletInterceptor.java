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
package org.apache.jasper34.tomcat33;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.servlet.jsp.HttpJspPage;
import javax.servlet.jsp.JspFactory;

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.depend.*;
import org.apache.tomcat.util.compat.*;

import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.*;
import org.apache.jasper34.generator.*;
import org.apache.jasper34.liaison.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.javacompiler.*;
import org.apache.jasper34.core.Compiler;

import org.apache.tomcat.core.*;
import org.apache.tomcat.facade.*;
import org.apache.tomcat.util.io.*;

/**
 * Plug in jasper via JspServlet. Will map all jsps to JspServlet
 * and pass JspServlet init params.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public class JspServletInterceptor extends BaseInterceptor {
    static final String JIKES=
	"org.apache.jasper34.javacompiler.JikesJavaCompiler";

    static final String JSP_SERVLET=
	"org.apache.jasper34.servlet.JspServlet";
    
    Properties args=new Properties(); // args for jasper
    String jspServletCN=JSP_SERVLET;
    String runtimePackage;
    
    // -------------------- Jasper options --------------------
    // Options that affect jasper functionality. Will be set on
    // JspServlet ( if useJspServlet="true" ) or TomcatOptions.
    // IMPORTANT: periodically test for new jasper options
    
    /**
     * Are we keeping generated code around?
     */
    public void setKeepGenerated( String s ) {
	args.put( "keepgenerated", s );
    }

    /**
     * Are we supporting large files?
     */
    public void setLargeFile( String s ) {
	args.put( "largefile", s );
    }

    /**
     * Are we supporting HTML mapped servlets?
     */
    public void setMappedFile( String s ) {
	args.put( "mappedfile", s );
    }

    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public void setSendErrToClient( String s ) {
	args.put( "sendErrToClient", s );
    }

    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public void setIEClassId( String s ) {
	args.put( "ieClassId", s );
    }

    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public void setClassPath( String s ) {
	args.put( "classpath", s );
    }

    /**
     * What is my scratch dir?
     */
    public void setScratchdir( String s ) {
	args.put( "scratchdir", s );
    }

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public void setJspCompilerPath( String s ) {
	args.put( "jspCompilerPath", s );
    }

    /**
     * What compiler plugin should I use to compile the servlets
     * generated from JSP files?
     * @deprecated Use setJavaCompiler instead
     */
    public void setJspCompilerPlugin( String s ) {
	args.put( "jspCompilerPlugin", s );
    }

    /** Include debug information in generated classes
     */
    public void setClassDebugInfo( String s ) {
	args.put("classDebugInfo", s );
    }
    
    public void setProperty( String n, String v ) {
	args.put( n, v );
    }
    // -------------------- JspInterceptor properties --------------------

    /** Specify the implementation class of the jsp servlet.
     */
    public void setJspServlet( String  s ) {
	jspServletCN=s;
    }

    /**
     * What compiler should I use to compile the servlets
     * generated from JSP files? Default is "javac" ( you can use
     * "jikes" as a shortcut ).
     */
    public void setJavaCompiler( String type ) {
	if( "jikes".equals( type ) )
	    type=JIKES;
	if( "javac".equals( type ) )
	    type="org.apache.jasper34.javacompiler.SunJavaCompiler";
	
	args.put( "jspCompilerPlugin", type );
    }
    
    int pageContextPoolSize=JspFactoryImpl.DEFAULT_POOL_SIZE;

    /** Set the PageContext pool size for jasper factory.
	0 will disable pooling of PageContexts.
     */
    public void setPageContextPoolSize(int i) {
	pageContextPoolSize=i;
    }

    /** The generator will produce code using a different
	runtime ( default is org.apache.jasper.runtime ).
	The runtime must use the same names for classes as the
	default one, so the code will compile.
    */
    public void setRuntimePackage(String rp ) {
	runtimePackage=rp;
    }
    
    // -------------------- Hooks --------------------

    /**
     * Jasper-specific initializations, add work dir to classpath,
     */
    public void addContext(ContextManager cm, Context ctx)
	throws TomcatException 
    {
	if( runtimePackage!=null ) {
	    Constants.JSP_RUNTIME_PACKAGE=runtimePackage;
	    Constants.JSP_SERVLET_BASE=runtimePackage+".HttpJspBase";
	}

	JspFactoryImpl factory=new JspFactoryImpl(pageContextPoolSize);
	
	JspFactory.setDefaultFactory(factory);
    }

    /** Do the needed initialization if jspServlet is used.
     *  It must be called after Web.xml is read ( WebXmlReader ).
     */
    public void contextInit(Context ctx)
	throws TomcatException
    {
	// prepare jsp servlet. 
	Handler jasper=ctx.getServletByName( "jsp" );
	if ( debug>10) log( "Got jasper servlet " + jasper );

	ServletHandler jspServlet=(ServletHandler)jasper;
	if( jspServlet.getServletClassName() != null
	    && ! "jsp".equals( jspServlet.getServletClassName())) {
	    log( "Jsp already defined in web.xml " +
		 jspServlet.getServletClassName() );
	    return;
	}

	if( debug>-1)
	    log( "jspServlet=" +  jspServlet.getServletClassName());
	Enumeration enum=args.keys();
	while( enum.hasMoreElements() ) {
	    String s=(String)enum.nextElement();
	    String v=(String)args.get(s);
	    if( debug>0 ) log( "Setting " + s + "=" + v );
	    jspServlet.getServletInfo().addInitParam(s, v );
	}
	
	jspServlet.setServletClassName(jspServletCN);
    }
}
