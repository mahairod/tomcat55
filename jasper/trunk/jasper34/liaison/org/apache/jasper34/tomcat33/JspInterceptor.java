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
 * Plug in the JSP engine (a.k.a Jasper)!
 * Tomcat uses a "built-in" mapping for jsps ( *.jsp -> jsp ). "jsp"
 * can be either a real servlet (JspServlet) that compiles the jsp
 * and include the resource, or we can "intercept" and do the
 * compilation and mapping in requestMap stage.
 *
 * JspInterceptor will be invoked once per jsp, and will add an exact
 * mapping - all further invocation are identical with servlet invocations
 * with direct maps, with no extra overhead.
 *
 * Future - better abstraction for jsp->java converter ( jasper ), better
 * abstraction for java->class, plugin other jsp implementations,
 * better scalability.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public class JspInterceptor extends BaseInterceptor {
    static final String JIKES=
	"org.apache.jasper34.javacompiler.JikesJavaCompiler";
    static final String JSP_SERVLET=
	"org.apache.jasper34.servlet.JspServlet";
    
    Properties args=new Properties(); // args for jasper
    String jspServletCN=JSP_SERVLET;
    String runtimePackage;
    
    // -------------------- Jasper options --------------------

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

	// jspServlet uses it's own loader. We need to add workdir
	// to the context classpath to use URLLoader and normal
	// operation
	// XXX alternative: use WEB-INF/classes for generated files 
	try {
	    // Note: URLClassLoader in JDK1.2.2 doesn't work with file URLs
	    // that contain '\' characters.  Insure only '/' is used.
	    // jspServlet uses it's own mechanism
	    URL url=new URL( "file", null,
	       ctx.getWorkDir().getAbsolutePath().replace('\\','/') + "/");
	    ctx.addClassPath( url );
	    if( debug > 9 ) log( "Added to classpath: " + url );
	} catch( MalformedURLException ex ) {
	}
    }

    /** Do the needed initialization if jspServlet is used.
     *  It must be called after Web.xml is read ( WebXmlReader ).
     */
    public void contextInit(Context ctx)
	throws TomcatException
    {
	ctx.addServlet( new JspPrecompileH());
    }

    /** Set the HttpJspBase classloader before init,
     *  as required by Jasper
     */
    public void preServletInit( Context ctx, Handler sw )
	throws TomcatException
    {
	if( ! (sw instanceof ServletHandler) )
	    return;
	try {
	    // requires that everything is compiled
	    Servlet theServlet = ((ServletHandler)sw).getServlet();
	    if (theServlet instanceof HttpJspBase)  {
		if( debug > 9 )
		    log( "PreServletInit: HttpJspBase.setParentClassLoader" +
			 sw );
		HttpJspBase h = (HttpJspBase) theServlet;
		//h.setClassLoader(ctx.getClassLoader());
	    }
	} catch(Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    //-------------------- Main hook - compile the jsp file if needed
    
    /** Detect if the request is for a JSP page and if it is find
	the associated servlet name and compile if needed.

	That insures that init() will take place on the equivalent
	servlet - and behave exactly like a servlet.

	A request is for a JSP if:
	- the handler is a ServletHandler ( i.e. defined in web.xml
	or dynamically loaded servlet ) and it has a "path" instead of
	class name
	- the handler has a special name "jsp". That means a *.jsp -> jsp
	needs to be defined. This is a tomcat-specific mechanism ( not
	part of the standard ) and allow users to associate other extensions
	with JSP by using the "fictious" jsp handler.

	An (cleaner?) alternative for mapping other extensions would be
	to set them on JspInterceptor.
    */
    public int requestMap( Request req ) {
	Handler wrapper=req.getHandler();
	
	if( wrapper==null )
	    return 0;

	String wrapperName=wrapper.getName();
	ServletHandler handler=null;
	String jspFile=null;
	
	// Decide if we want to deal with this request
	// There are 2 cases:
	//     *.jsp extension mapped 
	//     exact map with a <servlet> with file-name declaration

	if( wrapper instanceof ServletHandler ) {
	    handler=(ServletHandler)wrapper;
	    String wrapperCN=handler.getServletClassName();

	    if( debug > 0 ) log("Found servlet handler " + handler + " "
				+ wrapperCN );
	    if( "jsp".equals( wrapperCN ) ) {
		// This is the dummy servlet for *.jsp mapping,
		// not ovverriden by users settings. It's us ?

		// continue - new *.jsp page
	    } else {
		// a servlet - could be generated servlet or explicit
		// *.jsp mapping by user
		jspFile=handler.getServletInfo().getJspFile();
		if( jspFile==null )
		    return 0; // not a jsp managed by JspInterceptor
		
		// continue - jsp page created by JspInterceptor
		// or explicit <servlet><jsp-file> declaration
		// XXX What if the user set a JspServlet mapping ?
	    }
	} else {
	    if( debug > 0 )
		log("Found internal handler" +
		    wrapperName );
	    
	    if( "jsp".equals(wrapperName) ) {

		// continue - new *.jsp page
	    } else {
		// handler for something else - not *.jsp and not generated 
		return 0;
	    }
	}

	if( jspFile == null ) {
	    // this is a new *.jsp servlet

	    jspFile=req.servletPath().toString();
	    
	    // extension mapped jsp - define a new handler,
	    // add the exact mapping to avoid future overhead
	    handler= mapJspPage( req.getContext(), jspFile );
	    req.setHandler( handler );
	}

	boolean pre_compile= checkPreCompile( req );
	
	// Each .jsp file is compiled to a servlet, and will
	// have a dependency to check if it's expired
	DependManager dep= handler.getServletInfo().getDependManager();
	
	if( dep!=null && ! dep.shouldReload()  ) {
	    // if the jspfile is older than the class - we're ok
	    // this happens if the .jsp file was compiled in a previous
	    // run of tomcat.
	    return 0;
	}

	// we need to compile... ( or find previous .class )
	processJspFile( req, jspFile, handler, args);

	dep= handler.getServletInfo().getDependManager();
	// we did a compilation or loaded new page 
	dep.reset();
	
	if( pre_compile ) {
	    // we may have compiled the page ( if needed ), but
	    // we can't execute it. The handler will just
	    // report that we detected the trick.

	    // Future: detail information about compile results
	    // and if indeed we had to do something or not
	    req.setHandler(  ctx.
			     getServletByName( "tomcat.jspPrecompileHandler"));
	}
	// continue - the servlet will be executed
	return 0;
    }

    // -------------------- Utils --------------------
    private boolean checkPreCompile( Request req ) {
	// if it's a jsp_precompile request, don't execute - just
	// compile ( if needed ). Since we'll compile the jsp on
	// the first request the only special thing is to not
	// execute the jsp if jsp_precompile param is in parameters.

	String qString=req.queryString().toString();
	// look for ?jsp_precompile or &jsp_precompile

	// quick test to see if we need to worry about params
	// ( preserve lazy eval for parameters )
	boolean pre_compile=false;
	int i=(qString==null) ? -1: qString.indexOf( "jsp_precompile" );
	if( i>= 0 ) {
	    // Probably we are in the problem case. 
	    req.parameters().handleQueryParameters();
	    String p=req.parameters().getParameter( "jsp_precompile");
	    if( p==null || p.equalsIgnoreCase("true")) {
		pre_compile=true;
	    }
	}
	return pre_compile;
    }

    
    private static final String SERVLET_NAME_PREFIX="TOMCAT/JSP";
    
    /** Add an exact map that will avoid *.jsp mapping and intermediate
     *  steps. It's equivalent with declaring
     *  <servlet-name>tomcat.jsp.[uri]</>
     *  <servlet-mapping><servlet-name>tomcat.jsp.[uri]</>
     *                   <url-pattern>[uri]</></>
     */
    private ServletHandler mapJspPage( Context ctx, String uri)
    {
	String servletName= SERVLET_NAME_PREFIX + uri;

	if( debug>0)
	    log( "mapJspPage " + ctx + " " + " " + servletName + " " +  uri  );

	Handler h=ctx.getServletByName( servletName );
	if( h!= null ) {
	    log( "Name already exists " + servletName +
		 " while mapping " + uri);
	    return (ServletHandler)h; // exception ?
	}
	
	ServletHandler wrapper=new ServletHandler();
	wrapper.setModule( this );
	wrapper.setContext(ctx);
	wrapper.setName(servletName);
	wrapper.getServletInfo().setJspFile( uri );
	
	// add the mapping - it's a "invoker" map ( i.e. it
	// can be removed to keep memory under control.
	// The memory usage is smaller than JspSerlvet anyway, but
	// can be further improved.
	try {
	    ctx.addServlet( wrapper );
	    ctx.addServletMapping( uri ,
				   servletName );
	    if( debug > 0 )
		log( "Added mapping " + uri + " path=" + servletName );
	} catch( TomcatException ex ) {
	    log("mapJspPage: ctx=" + ctx +
		", servletName=" + servletName, ex);
	    return null;
	}
	return wrapper;
    }


    /** Generate mangled names, check for previous versions,
     *  generate the .java file, compile it - all the expensive
     *  operations. This happens only once ( or when the jsp file
     *  changes ). 
     */
    int processJspFile( Request req, String jspFile,
		       ServletHandler handler, Properties args)
    {
	// ---------- Expensive part - compile and load
	
	// If dep==null, the handler was never used - we need
	// to either compile it or find the previous compiled version
	// If dep.isExpired() we need to recompile.

	if( debug > 10 ) log( "Before compile sync  " + jspFile );
	synchronized( handler ) {
	    
	    // double check - maybe another thread did that for us
	    DependManager depM= handler.getServletInfo().getDependManager();
	    if( depM!=null && ! depM.shouldReload() ) {
		// if the jspfile is older than the class - we're ok
		return 0;
	    }
	    if( debug > 0 ) 
		if( depM == null )
		    log( "DepM==null ");
		else
		    log( "DepM.shouldReload()" + depM.shouldReload());
	    Context ctx=req.getContext();
	    
	    // Mangle the names - expensive operation, but nothing
	    // compared with a compilation :-)
	    Mangler mangler=
		new Mangler33(ctx.getWorkDir().getAbsolutePath(),
			      ctx.getAbsolutePath(),
			      jspFile );

	    Options options=new OptionsProperties(args); 
	    
	    JasperEngineContext containerL =
		new JasperEngineContext(req.getContext());
	    containerL.setOptions( options );

	    JspPageInfo pageInfo=new JspPageInfo( containerL, options, mangler );
	    
	    //pageInfo.setServletClassName( mangler.getClassName());
	    pageInfo.setJspFile( jspFile );
	    
	    Compiler compiler=new Compiler(containerL);
	    
	    // register the handler as dependend of the jspfile 
	    if( depM==null ) {
		depM=setDependency( ctx, mangler, handler, pageInfo );
		// update the servlet class name
		handler.setServletClassName( mangler.getServletClassName() );

		// check again - maybe we just found a compiled class from
		// a previous run
		if( ! depM.shouldReload() ) {
		    addExtraDeps( depM, handler );		    
		    if( debug > 0 )
			log( "Loaded dependency, shouldReload = " +
				 depM.shouldReload() );
 		    return 0;
		}
	    }

	    //	    if( debug > 3) 
	    ctx.log( "Compiling: " + jspFile + " to " +
		     mangler.getServletClassName());
	    
	    //XXX old servlet -  destroy(); 
	    
	    // jump version number - the file needs to be recompiled
	    // reset the handler error, un-initialize the servlet
	    handler.setErrorException( null );
	    handler.setState( Handler.STATE_ADDED );
	    
	    // Move to the next class name
	    mangler.nextVersion();

	    // record time of attempted translate-and-compile
	    // if the compilation fails, we'll not try again
	    // until the jsp file changes
	    depM.setLastModified( System.currentTimeMillis() );

	    // Update the class name in wrapper
	    if( debug> 1 )
		log( "Update class Name " + mangler.getServletClassName());
	    handler.setServletClassName( mangler.getServletClassName() );

	    
	    try {
		synchronized ( mangler ) {
		    compiler.jsp2java(pageInfo);
		}
	    } catch( FileNotFoundException ex ) {
		log(ctx, "FileNotFound: file:" +
		    pageInfo.getJspFile() + " req="+req );
		handler.setErrorException(ex);
		handler.setState(Handler.STATE_DISABLED);
		// until the jsp cahnges, when it'll be enabled again
		return 404;
	    } catch( Exception ex ) {
		log(ctx, "jsp2java error: req="+req, ex);
		handler.setErrorException(ex);
		handler.setState(Handler.STATE_DISABLED);
		// until the jsp cahnges, when it'll be enabled again
		return 500;
	    }
	    
	    try {
		if( jspCompilerPluginS==null && tryJikes ) {
		    tryJikes(pageInfo, compiler );
		}
	
		JavaCompiler javaC=
		    compiler.createJavaCompiler( pageInfo,
						 jspCompilerPluginS );
		compiler.prepareCompiler( javaC, pageInfo );
		boolean status =
		    javaC.compile( pageInfo.getMangler().getJavaFileName() );

		if (status == true ) {
		    // remove java file if !keepgenerated, etc
		    compiler.postCompile(pageInfo);
		}

		String msg = javaC.getCompilerMessage();
		if( status == false && msg.length() > 0 ) {
		    // XXX parse and process the error message
		    log( ctx, "Compiler error: " + msg );
		    handler.setErrorException
			( new JasperException("Compiler error: " + msg + "X"));
		    handler.setState(Handler.STATE_DISABLED);
		    // until the jsp cahnges, when it'll be enabled again
		    return 500;
		}
		
		addExtraDeps( depM, handler );
	    } catch( Exception ex ) {
		log(ctx, "compile error: req=",  ex);
		handler.setErrorException(ex);
		handler.setState(Handler.STATE_DISABLED);
		// until the jsp cahnges, when it'll be enabled again
		return 500;
	    }

	    depM.setExpired( false );
	    
	}

	return 0;
    }

    static boolean tryJikes=true;
    static String jspCompilerPluginS;

    /** Called for the first jsp, if no compiler is specified.
     *  @return true if jikes was found and compilation is successfull.
     *  Side effect: tryJikes=false, set jspCompilerPlugin if successfull.
     */
    private void tryJikes( JspPageInfo pageInfo, Compiler compiler)   
    {
	JavaCompiler javaC=null;
	try {
	    tryJikes=false;
	    javaC=compiler.createJavaCompiler(pageInfo,
						    JspInterceptor.JIKES);
	    javaC.addDefaultClassPath();
	    String dirO=pageInfo.getContainerLiaison().getOutputDir();
	    File dirF=new File( dirO );
	    javaC.setOutputDir( dirO );
	    File a=new File( dirF, "a.java");
	    PrintWriter pw=new PrintWriter( new FileWriter( a ));
	    pw.println("public class a { public a() { } } ");
	    pw.close();
	    
	    if( debug > 0 ) log( "Compiling " + a.toString() );
	    boolean status = javaC.compile( a.toString() );
	    
	    if( debug > 0 )
		log( "Compiled  " + status + " " + javaC.getCompilerMessage());
	    
	    a.delete();
	    File aC=new File( dirF, "a.class");
	    if( aC.exists() )
		aC.delete();

	    if( status ) {
		jspCompilerPluginS=JspInterceptor.JIKES;
		log("Detected jikes");
	    }
	} catch( Exception ex ) {
	    if( javaC==null ) 
		log("Guess java compiler: no jikes " + ex.toString() );
	    else
		log("Guess java compiler: no jikes1 " +
		    javaC.getCompilerMessage() +
		    " " +   ex.toString() );
	}
    }
    
    private String getJspFilePath( String docBase, JspPageInfo pageInfo )
    {
	return FileUtil.safePath( docBase,
				  pageInfo.getJspFile());
    }
    
    // Add an "expire check" to the generated servlet.
    private DependManager setDependency( Context ctx, Mangler mangler,
					 ServletHandler handler,
					 JspPageInfo pageInfo )
    {
	ServletInfo info=handler.getServletInfo();
	// create a lastModified checker.
	if( debug>0) log("Registering dependency for " + handler );
	DependManager depM=new DependManager(4); // jsps have fewer deps
	depM.setDebug( debug );
	Dependency dep=new Dependency();
	File depFile=new File(getJspFilePath(ctx.getAbsolutePath(),
					     pageInfo));
	dep.setOrigin( depFile );
	if(debug>0 )
	    log("Create dependency " + depFile + " " + depFile.lastModified());
	dep.setTarget( handler );
	//dep.setLocal( true );
	File f=new File( pageInfo.getMangler().getClassFileName() );
	if( mangler.getVersion() > 0 ) {
	    // it has a previous version
	    dep.setLastModified(f.lastModified());
	    // update the "expired" variable
	    if(debug>0 )
		log("SetLastModified " + f + " " + f.lastModified());
	    dep.checkExpiry();
	} else {
	    dep.setLastModified( -1 );
	    dep.setExpired( true );
	}
	depM.addDependency( dep );
	
	if( debug>0 )
	    log( "file = " + pageInfo.getMangler().getClassFileName() + " " +
		     f.lastModified() );
	if( debug>0 )
	    log("origin = " + dep.getOrigin() + " " +
		    dep.getOrigin().lastModified());

	info.setDependManager( depM );
	return depM;
    }

    private void addExtraDeps( DependManager depM, ServletHandler handler )
    {
	try {
	    Servlet theServlet = handler.getServlet();
	    if (theServlet instanceof HttpJspBase)  {
		HttpJspBase httpJsp = (HttpJspBase) theServlet;
		String deps[]=httpJsp._getDepends();
		if( deps==null ) return;
		
		for( int i=0; i < deps.length ; i++ ) {
		    if( deps[i]==null ) continue;
		    Dependency d=new Dependency();
		    File f=new File( deps[i] );
		    d.setOrigin( f );
		    d.setTarget( handler );
		    d.setLastModified( f.lastModified() );
		    d.checkExpiry();
		    depM.addDependency( d );
		    log( "Adding dependency " + d  +
			     " " + depM.shouldReload() );
		}
	    }
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}

    }

    private void log( Context ctx, String msg, Throwable ex ) {
	if( ctx!=null )
	    ctx.log( msg, ex );
	else
	    log( msg, ex );
    }
    private void log( Context ctx, String msg ) {
	if( ctx!=null )
	    ctx.log( msg );
	else
	    log( msg );
    }
    
    
    }

// -------------------- Jsp_precompile handler --------------------

/** What to do for jsp precompile
 */
class JspPrecompileH extends Handler {
    static StringManager sm=StringManager.
	getManager("org.apache.tomcat.resources");
    
    JspPrecompileH() {
	name="tomcat.jspPrecompileHandler";
    }

    public void doService(Request req, Response res)
	throws Exception
    {
	res.setContentType("text/html");	

	String msg="<h1>Jsp Precompile Done</h1>";

	res.setContentLength(msg.length());

	res.getBuffer().write( msg );
    }
}
