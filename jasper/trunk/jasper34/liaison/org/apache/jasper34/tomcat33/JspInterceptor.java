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
    static final String JIKES="org.apache.jasper34.javacompiler.JikesJavaCompiler";
    static final String JSP_SERVLET="org.apache.jasper34.servlet.JspServlet";
    
    Properties args=new Properties(); // args for jasper
    boolean useJspServlet=false; 
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

    /** Use the old JspServlet to execute Jsps, instead of the
	new code. Note that init() never worked (AFAIK) and it'll
	be slower - but given the stability of JspServlet it may
	be a safe option. This will significantly slow down jsps.
	Default is false.
    */
    public void setUseJspServlet( boolean b ) {
	useJspServlet=b;
    }

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
	if( ! useJspServlet ) {
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
    }

    /** Do the needed initialization if jspServlet is used.
     *  It must be called after Web.xml is read ( WebXmlReader ).
     */
    public void contextInit(Context ctx)
	throws TomcatException
    {
	if( useJspServlet ) {
	    // prepare jsp servlet. 
	    Handler jasper=ctx.getServletByName( "jsp" );
	    if ( debug>10) log( "Got jasper servlet " + jasper );

	    ServletHandler jspServlet=(ServletHandler)jasper;
	    if( jspServlet.getServletClassName() == null ) {
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

	    // XXX redo - when containerL becomes one per/context 
// 	    if( debug > 0 ) {
// 		//enable jasperServlet logging
// 		log( "Seetting debug on jsp servlet");
// 		containerL.setLog(loghelper);
// 		// 		org.apache.jasper.containerL.jasperLog.
// 		// 		    setVerbosityLevel("debug");
// 	    }

	    jspServlet.setServletClassName(jspServletCN);
	} else {
	    ctx.addServlet( new JspPrecompileH());
	}
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
		h.setClassLoader(ctx.getClassLoader());
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
	if( useJspServlet ) {
	    // no further processing - jspServlet will take care
	    // of the processing as before ( all processing
	    // will happen in the handle() pipeline.
	    return 0;
	}

	Handler wrapper=req.getHandler();
	
	if( wrapper==null )
	    return 0;

	// It's not a jsp if it's not "*.jsp" mapped or a servlet
	if( (! "jsp".equals( wrapper.getName())) &&
	    (! (wrapper instanceof ServletHandler)) ) {
	    return 0;
	}

	ServletHandler handler=null;
	String jspFile=null;

	// There are 2 cases: extension mapped and exact map with
	// a <servlet> with file-name declaration

	// note that this code is called only the first time
	// the jsp page is called - all other calls will treat the jsp
	// as a regular servlet, nothing is special except the initial
	// processing.

	// XXX deal with jsp_compile
	
	if( "jsp".equals( wrapper.getName())) {
	    // if it's an extension mapped file, construct and map a handler
	    jspFile=req.servletPath().toString();
	    
	    // extension mapped jsp - define a new handler,
	    // add the exact mapping to avoid future overhead
	    handler= mapJspPage( req.getContext(), jspFile );
	    req.setHandler( handler );
	} else if( wrapper instanceof ServletHandler) {
	    // if it's a simple servlet, we don't care about it
	    handler=(ServletHandler)wrapper;
	    jspFile=handler.getServletInfo().getJspFile();
	    if( jspFile==null )
		return 0; // not a jsp
	}

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
	
	// Each .jsp file is compiled to a servlet, and will
	// have a dependency to check if it's expired
	DependManager dep= handler.getServletInfo().getDependManager();
	if( dep!=null && ! dep.shouldReload()  ) {
	    if( debug > 0  )
		log( "ShouldReload = " + dep.shouldReload());
	    // if the jspfile is older than the class - we're ok
	    // this happens if the .jsp file was compiled in a previous
	    // run of tomcat.
	    return 0;
	}

	// we need to compile... ( or find previous .class )
	JasperLiaison liasion=new JasperLiaison(getLog(), debug);
	liasion.processJspFile( req, jspFile, handler, args);

	dep= handler.getServletInfo().getDependManager();
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
	
	return 0;
    }

    // -------------------- Utils --------------------
    
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




// -------------------- The main Jasper Liaison --------------------

final class JasperLiaison {
    Log log;
    final int debug;
    //    ContainerLiaison containerL;
    
    JasperLiaison(  Log log, int debug ) {
	this.log=log;
	this.debug=debug;
	//	this.containerL=containerL;
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

	if( debug > 10 ) log.log( "Before compile sync  " + jspFile );
	synchronized( handler ) {
	    
	    // double check - maybe another thread did that for us
	    DependManager depM= handler.getServletInfo().getDependManager();
	    if( depM!=null && ! depM.shouldReload() ) {
		// if the jspfile is older than the class - we're ok
		return 0;
	    }
	    if( debug > 0 ) 
		if( depM == null )
		    log.log( "DepM==null ");
		else
		    log.log( "DepM.shouldReload()" + depM.shouldReload());
	    Context ctx=req.getContext();
	    
	    // Mangle the names - expensive operation, but nothing
	    // compared with a compilation :-)
	    Mangler mangler=
		new Mangler33(ctx.getWorkDir().getAbsolutePath(),
			      ctx.getAbsolutePath(),
			      jspFile );

	    Options options=new OptionsProperties(args); 
	    
	    JasperEngineContext ctxt = new JasperEngineContext();
	    ctxt.setClassPath( computeClassPath( req.getContext()) );
	    ctxt.setServletContext( req.getContext().getFacade());
	    ctxt.setOptions( options );
	    ctxt.setClassLoader( req.getContext().getClassLoader());
	    ctxt.setOutputDir(req.getContext().getWorkDir().getAbsolutePath());
	    
	    JspPageInfo pageInfo=new JspPageInfo( ctxt, options, mangler );
	    
	    //pageInfo.setServletClassName( mangler.getClassName());
	    pageInfo.setJspFile( jspFile );
	    
	    Compiler compiler=new Compiler(ctxt);
	    
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
			log.log( "Loaded dependency, shouldReload = " +
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
		log.log( "Update class Name " + mangler.getServletClassName());
	    handler.setServletClassName( mangler.getServletClassName() );

	    
	    try {

		jsp2java( compiler, mangler, ctxt, pageInfo );

		javac( compiler, pageInfo, options, ctxt, mangler );
	    
		if(debug>0)log.log( "Generated " +
				    mangler.getClassFileName() );

		addExtraDeps( depM, handler );
	    } catch( Exception ex ) {
		if( ctx!=null )
		    ctx.log("compile error: req="+req, ex);
		else
		    log.log("compile error: req="+req, ex);
		handler.setErrorException(ex);
		handler.setState(Handler.STATE_DISABLED);
		// until the jsp cahnges, when it'll be enabled again
		return 500;
	    }

	    depM.setExpired( false );
	    
	}

	return 0;
    }

    /** Convert the .jsp file to a java file, then compile it to class
     */
    void jsp2java(Compiler compiler, Mangler mangler,
		  JasperEngineContext ctxt, JspPageInfo pageInfo )
	throws Exception
    {
	if( debug > 0 ) log.log( "Generating " + mangler.getJavaFileName());
	// make sure we have the directories
	String javaFileName=mangler.getJavaFileName();
	
	File javaFile=new File(javaFileName);
	
	// make sure the directory is created
	new File( javaFile.getParent()).mkdirs();
	
	// we will compile ourself
	// compiler.setJavaCompiler( null );
	
	
	synchronized ( mangler ) {
	    compiler.jsp2java(pageInfo);
	}
	if( debug > 0 ) {
	    File f = new File( mangler.getJavaFileName());
	    log.log( "Created file : " + f +  " " + f.lastModified());
	    
	}
    }
    
    String javaEncoding = "UTF8";           // perhaps debatable?
    static String sep = System.getProperty("path.separator");

    static boolean tryJikes=true;
    static Class jspCompilerPlugin = null;
    static String jspCompilerPluginS;
    
    /** Compile a java to class. This should be moved to util, togheter
	with JavaCompiler - it's a general purpose code, no need to
	keep it part of jasper
    */
    void javac(Compiler compiler,
	       JspPageInfo pageInfo,
	       Options options,
	       ContainerLiaison containerL,
	       Mangler mangler)
	throws JasperException
    {
	String javaFileName = mangler.getJavaFileName();
	if( debug>0 ) log.log( "Compiling java file " + javaFileName);

	boolean status=true;
	if( jspCompilerPluginS == null ) {
	    jspCompilerPluginS=options.getJspCompilerPlugin();
	}
	
	// If no explicit compiler, and we never tried before
	if( jspCompilerPlugin==null && tryJikes ) {

	    ByteArrayOutputStream out = new ByteArrayOutputStream (256);
	    try {
		jspCompilerPlugin=Class.forName(JspInterceptor.JIKES);
		JavaCompiler javaC=
		    JavaCompiler.createJavaCompiler( containerL,
						     jspCompilerPlugin );
		
		compiler.prepareCompiler( javaC, options, pageInfo );
		javaC.setMsgOutput(out);
		status = compiler.javac(pageInfo, javaC );
	    } catch( Exception ex ) {	
		log.log("Guess java compiler: no jikes " + ex.toString());
		status=false;
	    }
	    if( status==false ) {
		log.log("Guess java compiler: no jikes ");
		log.log("Guess java compiler: OUT " + out.toString());
		jspCompilerPlugin=null;
		tryJikes=false;
	    } else {
		log.log("Guess java compiler: using jikes ");
	    }
	}

	JavaCompiler javaC=
	    JavaCompiler.createJavaCompiler( containerL, jspCompilerPlugin );
	compiler.prepareCompiler( javaC, options, pageInfo );
	ByteArrayOutputStream out = new ByteArrayOutputStream (256);
	javaC.setMsgOutput(out);
	
	status = javaC.compile(javaFileName);

        if (!containerL.getOptions().getKeepGenerated()) {
            File javaFile = new File(javaFileName);
            javaFile.delete();
        }
    
        if (status == false) {
            String msg = out.toString ();
            throw new JasperException("Unable to compile "
                                      + msg);
        }
	if( debug > 0 ) log.log("Compiled ok");
    }

    private String computeClassPath(Context ctx) {
	String separator = System.getProperty("path.separator", ":");
	URL classP[]=ctx.getClassPath();
        String cpath = "";
        cpath+=extractClassPath(classP);
        Jdk11Compat jdkProxy=Jdk11Compat.getJdkCompat();
        URL appsCP[];
        URL commonCP[];
        ClassLoader parentLoader=ctx.getContextManager().getParentLoader();
        appsCP=jdkProxy.getParentURLs(parentLoader);
        commonCP=jdkProxy.getURLs(parentLoader);
	if( appsCP!=null ) 
	    cpath+=separator+extractClassPath(appsCP);
	if( commonCP!=null ) 
	    cpath+=separator+extractClassPath(commonCP);
	return cpath;
    }
    String extractClassPath(URL urls[]){
	String separator = System.getProperty("path.separator", ":");
        String cpath="";
        for(int i=0; i< urls.length; i++ ) {
            URL cp = urls[i];
	    if( cp == null ) {
		continue;
	    }
            File f = new File( cp.getFile());
            if (cpath.length()>0) cpath += separator;
            cpath += f;
        }
        return cpath;
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
		    log.log( "Adding dependency " + d  +
			     " " + depM.shouldReload() );
		}
	    }
	} catch( Exception ex ) {
	    ex.printStackTrace();
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
	if( debug>0) log.log("Registering dependency for " + handler );
	DependManager depM=new DependManager(4); // jsps have fewer deps
	depM.setDebug( debug );
	Dependency dep=new Dependency();
	File depFile=new File(getJspFilePath(ctx.getAbsolutePath(),
					     pageInfo));
	dep.setOrigin( depFile );
	System.out.println("XXX " + depFile + " " + depFile.lastModified());
	dep.setTarget( handler );
	//dep.setLocal( true );
	File f=new File( mangler.getClassFileName() );
	if( mangler.getVersion() > 0 ) {
	    // it has a previous version
	    dep.setLastModified(f.lastModified());
	    // update the "expired" variable
	    System.out.println("XXY " + f + " " + f.lastModified());
	    dep.checkExpiry();
	} else {
	    dep.setLastModified( -1 );
	    dep.setExpired( true );
	}
	depM.addDependency( dep );
	
	if( debug>0 )
	    log.log( "file = " + mangler.getClassFileName() + " " +
		     f.lastModified() );
	if( debug>0 )
	    log.log("origin = " + dep.getOrigin() + " " +
		    dep.getOrigin().lastModified());
	/* This would add a dependency on the whole application,
	   causing a reload of the context when a jsp changes.
	*/
	/*
	try {
	    DependManager dm=(DependManager)ctx.getContainer().
		getNote("DependManager");
	    if( dm!=null ) {
		dm.addDependency( dep );
	    }
	} catch( TomcatException ex ) {
	    ex.printStackTrace();
	}
	*/
	info.setDependManager( depM );
	return depM;
    }


}
