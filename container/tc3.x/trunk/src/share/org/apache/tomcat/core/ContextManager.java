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

package org.apache.tomcat.core;

import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
  ContextManager is the entry point and "controler" of the servlet execution.
  It maintains a list of Contexts ( web applications )  and a list of
  interceptors (hooks) that are set up to handle the actual execution.

  The execution is modeled after Apache2.0 and other web servers, with
  interceptors serving the same role as modules.

  The "core" is composed of representation of "Request", "Response",
  "Container" ( a set of URLs with shared properties - like a Location
  in Apache), "Context" ( web application - the deployment unit ).

  "ContextManager" will  store properties that are global to the servlet
  container - like root directory, install dir, work dir and act as the
  top level controler.
 
  The server functionality is implemented in modules ( "BaseInterceptor" ).

  Hooks are provided for request parsing and mapping, auth, autorization,
  pre/post service, actual invocation and logging - similar with Apache and all
  other web servers.

  <h2>Using tomcat</h2>

  <ol>
  <li> Create ContextManager.

  <li> Set properties for ContextManager ( home, debug, etc).

  <li> Add the initial set of modules. Modules can also be added and removed
     after the server is started. ( engineInit() and addInterceptor() hooks
     will be called ). An interceptor can add/remove other interceptors
     during the engineInit  hook, or change/set server properties.
     XXX web applications should be added at a later stage.
     
  <li> Add the initial set of web applications ( Contexts ). Applications can
     be added and removed after the server is started. ( addContext() hook
     will be called )

  <li>init(). All contexts will be ready to run ( contextInit() hook is called)

  <li>start(). engineStart() hook will be called, the connector modules
  should accept and serve requests.

  <li>While tomcat is running, you can add/remove interceptors ( you must
  insure the server is temporarily stoped ). engineInit(), addInterceptor()
  will be called, then addContext() for all existing contexts and
  initContext() if server is in INIT state. 

  <li>While tomcat is running, you can temporarily disable web applications,
  remove and add new applications. If an application is disabled you can
  add local modules, that will affect only that application. Modules can
  also add and remove webapplications automatically ( like ~user ).

  <li>stop() will stop the server ( engineStop() hook will be called )

  <li>shutdown() will clean up all resources in use by web applications
  ( contextShutdown() ) and remove all contexts ( removeContext() hook )
  </ol>

  @author James Duncan Davidson [duncan@eng.sun.com]
  @author James Todd [gonzo@eng.sun.com]
  @author Harish Prabandham
  @author Costin Manolache
  @author Hans Bergsten [hans@gefionsoftware.com]
 */
public final class ContextManager implements LogAware{
    /** Official name and version
     */
    public static final String TOMCAT_VERSION = "3.3 dev";
    public static final String TOMCAT_NAME = "Tomcat Web Server";
    
    /** System property used to set the base directory ( tomcat home ).
     *  use -DTOMCAT_HOME= in java command line or as a System.setProperty.
     *  XXX hack - setHome is better
     */
    public static final String TOMCAT_HOME="tomcat.home";

    // State

    /**
     *  Server is beeing configured - modules are added.
     *  This is the inital state. As soon as init() is called or the
     *  first context is added the server will move to init state.
     */
    public static final int STATE_CONFIG=0;

    /**
     *  Server is initialized, web applications can be added.
     */
    public static final int STATE_INIT=1;
 
    /**
     *  Server is able to run requests.
     */
    public static final int STATE_START=2;

    /** Engine is stoped ( temporarily disabled ). 
     */
    public static final int STATE_STOP=3;

    /** Engine is shutdown. This is the final state, modules should clean
	up all resources.
     */
    public static final int STATE_SHUTDOWN=4;
    
    // -------------------- local variables --------------------

    private int state=STATE_CONFIG;
    
    // All contexts managed by the server ( can belong to different
    // virtual hosts )
    private Vector contextsV=new Vector();

    private int debug=0;

    /** Private workspace for this server
     */
    private String workDir;

    /** The base directory where this instance runs.
     *  It can be different from the install directory to
     *  allow one install per system and multiple users
     */
    private String home;

    /** The directory where tomcat is installed
     */
    private String installDir;
    
    // Server properties ( interceptors, etc ) - it's one level above "/"
    private Container defaultContainer;

    // the embedding application loader. @see getParentLoader
    private ClassLoader parentLoader;

    // Store Loggers that are used in this server
    // XXX use Log.getLog() instead!!
    private Hashtable loggers=new Hashtable();

    private Hashtable properties=new Hashtable();
    
    /**
     * Construct a new ContextManager instance with default values.
     */
    public ContextManager() {
        defaultContainer=new Container();
        defaultContainer.setContext( null );
	defaultContainer.setContextManager( this );
        defaultContainer.setPath( null ); // default container
    }

    // -------------------- setable properties --------------------

    /**
     *  The home of the tomcat instance - you can have multiple
     *  users running tomcat, with a shared install directory.
     *  
     *  Home is used as a base for logs, webapps, local config.
     *  Install dir ( if different ) is used to find lib ( jar
     *   files ).
     *
     *  The "tomcat.home" system property is used if no explicit
     *  value is set. XXX
     */
    public final void setHome(String home) {
	this.home=home;
    }

    public final String getHome() {
	return home;
    }

    /**
     *  Get installation directory. This is used to locate
     *  jar files ( lib ). If tomcat instance is shared,
     *  home is used for webapps, logs, config.
     *  If either home or install is not set, the other
     *  is used as default.
     * 
     */
    public final String getInstallDir() {
	return installDir;
    }

    public final void setInstallDir( String tH ) {
	installDir=tH;
    }

    /**
     * WorkDir property - where all working files will be created
     */
    public final void setWorkDir( String wd ) {
	if(debug>0) log("set work dir " + wd);
	this.workDir=wd;
    }

    public final String getWorkDir() {
	return workDir;
    }


    /** Debug level
     */
    public final void setDebug( int level ) {
	if( level != debug )
	    log( "Setting debug level to " + level);
	debug=level;
    }

    public final int getDebug() {
	return debug;
    }

    //  XmlMapper will call setProperty(name,value) if no explicit setter
    // is found - it's better to use this mechanism for special
    // properties ( that are not generic enough and used by few specific
    // module )

    /** Generic properties support. You can get properties like
     *  "showDebugInfo", "randomClass", etc.
     */
    public String getProperty( String name ) {
	return (String)properties.get( name );
    }

    public void setProperty( String name, String value ) {
	properties.put( name, value );
    }
    
    // -------------------- Other properties --------------------

    /** Return the current state of the tomcat server.
     */
    public final int getState() {
	return state;
    }

    /**
     *  Parent loader is the "base" class loader of the
     *	application that starts tomcat, and includes no
     *	tomcat classes.
     * 
     *  Each web applications will use a loader that will have it as
     *  a parent loader, so all classes visible to parentLoader
     *  will be available to servlets.
     *
     *  Tomcat will add the right servlet.jar and Facade.
     *
     *  Trusted applications will also see the internal tomcat
     *  classes.
     *
     *  Interceptors may also add custom classes to a webapp,
     *  based on tomcat configuration.
     *
     *  Tomcat.core and all internal classes will be loaded by
     *  another class loader, having the same parentLoader.
     *
     *  <pre>
     *  parentLoader  -> tomcat.core.loader [ -> trusted.webapp.loader ]
     *                -> webapp.loaders
     *  </pre>
     */
    public final void setParentLoader( ClassLoader cl ) {
	parentLoader=cl;
    }

    public final ClassLoader getParentLoader() {
	return parentLoader;
    }

    /** Default container. The interceptors for this container will
	be called for all requests, and it will be associated with
	invalid requests ( where context can't be found ).
     */
    public final Container getContainer() {
        return defaultContainer;
    }

    public final void setContainer(Container newDefaultContainer) {
        defaultContainer = newDefaultContainer;
    }

    /** Add a global interceptor. It's hooks will be called for
     *  all requests.
     */
    public final void addInterceptor( BaseInterceptor ri )
	throws TomcatException
    {
	ri.setContextManager( this );

	BaseInterceptor existingI[]=defaultContainer.getInterceptors();
	for( int i=0; i<existingI.length; i++ ) {
	    existingI[i].addInterceptor( this, null, ri );
	}
	ri.addInterceptor( this, null, ri ); // should know about itself
	
        defaultContainer.addInterceptor(ri);

	if( state==STATE_CONFIG ) return;

	// we are at least initialized, call engineInit hook
	ri.engineInit( this );

	// make sure the interceptor knows about all existing contexts.
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    Context ctx = (Context)enum.nextElement();
	    try {
		ri.addContext( this, ctx ); 
		if( state==STATE_START ) {
		    ri.contextInit( ctx );
		}
	    } catch( TomcatException ex ) {
		log( "Error adding context " +ctx + " to " + ri );
		// ignore it
	    }
	}

	if( state==STATE_START )
	    ri.engineStart(this);
    }

    public final void removeInterceptor( BaseInterceptor ri )
	throws TomcatException
    {
	BaseInterceptor existingI[]=defaultContainer.getInterceptors();
	for( int i=0; i<existingI.length; i++ ) {
	    existingI[i].removeInterceptor( this, null, ri );
	}
	ri.removeInterceptor( this, null, ri );
	
	defaultContainer.removeInterceptor( ri );

	if( state==STATE_CONFIG ) return;
	
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    Context ctx = (Context)enum.nextElement();
	    try {
		if( state==STATE_START ) {
		    ri.contextShutdown( ctx );
		}
		ri.removeContext( this, ctx ); 
	    } catch( TomcatException ex ) {
		log( "Error adding context " +ctx + " to " + ri );
		// ignore it
	    }
	}

	if( state==STATE_START )
	    ri.engineStop(this);
    }

    // -------------------- Server functions --------------------

    /**
     *  Init() is called after the context manager is set up (properties)
     *  and configured ( modules ).
     *
     *  All engineInit() hooks will be called and the server will 
     *  move to state= INIT
     * 
     */
    public final void init()  throws TomcatException {
	if( state == STATE_INIT  )
	    return;
	if(debug>0 ) log( "Tomcat init");
	
	BaseInterceptor existingI[]=defaultContainer.getInterceptors();
	for( int i=0; i<existingI.length; i++ ) {
	    existingI[i].engineInit( this );
	}

	state=STATE_INIT;
    }

    /** Will start the connectors and begin serving requests.
     *  It must be called after init.
     */
    public final void start() throws TomcatException {
	if( state==STATE_CONFIG ) {
	    init();
	}

	// make sure all context are ready
	Enumeration enum = getContexts();
	while (enum.hasMoreElements()) {
	    Context ctx = (Context)enum.nextElement();
	    try {
		ctx.init();
	    } catch( TomcatException ex ) {
		// just log the error, the context will not serve
		// requests but we go on
		log( "Error initializing " + ctx , ex );
		continue; 
	    }
	}

	//XXX before or after state=START ?
	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineStart( this );
	}

	// requests can be processed now
	state=STATE_START;
    }

    /** Will stop all connectors
     */
    public final void stop() throws Exception {
	state=STATE_INIT; // initialized, but not accepting connections
	
	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineStop( this );
	}

	// XXX we shouldn't call shutdown in stop !
	shutdown();
    }

    /** Remove all contexts.
     *  - call removeContext ( that will call Interceptor.removeContext hooks )
     *  - call Interceptor.engineShutdown() hooks.
     */
    public final void shutdown() throws TomcatException {
	while (!contextsV.isEmpty()) {
	    removeContext((Context)contextsV.firstElement());
	}

	BaseInterceptor cI[]=defaultContainer.getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].engineShutdown( this );
	}
    }

    // -------------------- Contexts --------------------

    /** Return the list of contexts managed by this server.
     *  Tomcat can handle multiple virtual hosts. 
     *
     *  All contexts are stored in ContextManager when added.
     *  Modules can use the information in context ( when addContext
     *  hook is called ) and prepare mapping tables.
     */
    public final Enumeration getContexts() {
	return contextsV.elements();
    }

    /**
     * Adds a new Context to the set managed by this ContextManager.
     *
     * If the server is initialized ( ContextManager.init() was called )
     * the addContext() hook will be called, otherwise the call will
     * be delayed until the server enters INIT state.
     *
     * The context will be in DISABLED state until start() is called.
     *
     * @param ctx context to be added.
     */
    public final void addContext( Context ctx ) throws TomcatException {
	if( state==STATE_CONFIG) {
	    init();
	}
	// Make sure context knows about its manager.
	// this will also initialized all context-specific modules.
	ctx.setContextManager( this );
	ctx.setState( Context.STATE_NEW );

	contextsV.addElement( ctx );

	try {
	    BaseInterceptor cI[]=ctx.getContainer().getInterceptors();
	    for( int i=0; i< cI.length; i++ ) {
		// If an exception is thrown, context will remain in
		// NEW state.
		cI[i].addContext( this, ctx ); 
	    }
	    ctx.setState( Context.STATE_ADDED );
	    log("Adding context " +  ctx.toString());
	} catch (TomcatException ex ) {
	    log( "Context not added " + ctx , ex );
	    throw ex;
	}
    }


    /** Shut down and removes a context from service.
     */
    public final void removeContext( Context context ) throws TomcatException {
	if( context==null ) return;

	log( "Removing context " + context.toString());

	contextsV.removeElement(context);

	// if it's already disabled - or was never activated,
	// no need to shutdown and remove
	if( context.getState() == Context.STATE_NEW )
	    return;
	
	// disable the context - it it is running 
	if( context.getState() == Context.STATE_READY )
	    context.shutdown();

	// remove it from operation - notify interceptors that
	// this context is no longer active
	BaseInterceptor cI[]=context.getContainer().getInterceptors();
	for( int i=0; i< cI.length; i++ ) {
	    cI[i].removeContext( this, context );
	}

	// mark the context as "not active" 
	context.setState( Context.STATE_NEW );
    }


    // -------------------- Request processing / subRequest ------------------
    // -------------------- Main request processing methods ------------------

    /** Prepare the req/resp pair for use in tomcat.
     *  Call it after you create the request/response objects.
     *  ( for example in a connector, or when an internal sub-request is
     *  created )
     */
    public final void initRequest( Request req, Response resp ) {
	// We may add other special calls here.
	resp.setRequest( req );
	req.setResponse( resp );
	req.setContextManager( this );
	resp.init();
    }

    /** This is the entry point in tomcat - the connectors ( or any other
     *  component able to generate Request/Response implementations ) will
     *  call this method to get it processed.
     */
    public final void service( Request req, Response res ) {
	if( state!=STATE_START ) {
	    // A request was received before all components are
	    // in started state. Than can happen if the adapter was
	    // started too soon or of the server is temporarily
	    // disabled.
	    req.setAttribute("javax.servlet.error.message",
			     "Server is starting");
	    handleStatus( req, res, 503 ); // service unavailable
	} else {
	    internalService( req, res );
	}
	
	// clean up
	try {
	    res.finish();
	} catch( Throwable ex ) {
	    handleError( req, res, ex );
	}
	finally {
	    BaseInterceptor reqI[]= req.getContainer().
		getInterceptors(Container.H_postRequest);

	    for( int i=0; i< reqI.length; i++ ) {
		reqI[i].postRequest( req, res );
	    }
	    req.recycle();
	    res.recycle();
	}
	return;
    }

    // Request processing steps and behavior
    private final void internalService( Request req, Response res ) {
	try {
	    /* assert req/res are set up
	       corectly - have cm, and one-one relation
	    */
	    // wrong request - parsing error
	    int status=res.getStatus();

	    if( status >= 400 ) {
		if( debug > 0)
		    log( "Error reading request " + req + " " + status);
		handleStatus( req, res, status ); 
		return;
	    }

	    status= processRequest( req );

	    if( status != 0 ) {
		if( debug > 0)
		    log("Error mapping the request " + req + " " + status);
		handleStatus( req, res, status );
		return;
	    }

	    if( req.getHandler() == null ) {
		status=404;
		if( debug > 0)
		    log("No handler for request " + req + " " + status);
		handleStatus( req, res, status );
		return;
	    }

	    String roles[]=req.getRequiredRoles();
	    if(roles != null ) {
		status=0;
		BaseInterceptor reqI[]= req.getContext().getContainer().
                getInterceptors(Container.H_authorize);

		// Call all authorization callbacks. 
		for( int i=0; i< reqI.length; i++ ) {
		    status = reqI[i].authorize( req, res, roles );
		    if ( status != 0 ) {
			break;
		    }
		}
	    }
	    if( status > 200 ) {
		if( debug > 0)
		    log("Authorize error " + req + " " + status);
		handleStatus( req, res, status );
		return;
	    }

	    req.getHandler().service(req, res);

	} catch (Throwable t) {
	    handleError( req, res, t );
	}
    }

    /** Will find the Handler for a servlet, assuming we already have
     *  the Context. This is also used by Dispatcher and getResource -
     *  where the Context is already known.
     *
     *  This method will only map the request, it'll not do authorization
     *  or authentication.
     */
    public final int processRequest( Request req ) {
	if(debug>9) log("Before processRequest(): "+req.toString());

	int status=0;
        BaseInterceptor ri[];
	ri=defaultContainer.getInterceptors(Container.H_contextMap);
	
	for( int i=0; i< ri.length; i++ ) {
	    status=ri[i].contextMap( req );
	    if( status!=0 ) return status;
	}
	req.setState(Request.STATE_CONTEXT_MAPPED );
	
	ri=defaultContainer.getInterceptors(Container.H_requestMap);
	for( int i=0; i< ri.length; i++ ) {
	    if( debug > 1 )
		log( "RequestMap " + ri[i] );
	    status=ri[i].requestMap( req );
	    if( status!=0 ) return status;
	}
	req.setState(Request.STATE_MAPPED );

	if(debug>9) log("After processRequest(): "+req.toString());

	return 0;
    }


    // -------------------- Sub-Request mechanism --------------------

    /** Create a new sub-request in a given context, set the context "hint"
     *  This is a particular case of sub-request that can't get out of
     *  a context ( and we know the context before - so no need to compute it
     *  again)
     *
     *  Note that session and all stuff will still be computed.
     */
    public final Request createRequest( Context ctx, String urlPath ) {
	// assert urlPath!=null

	// deal with paths with parameters in it
	String contextPath=ctx.getPath();
	String origPath=urlPath;

	// append context path
	if( !"".equals(contextPath) && !"/".equals(contextPath)) {
	    if( urlPath.startsWith("/" ) )
		urlPath=contextPath + urlPath;
	    else
		urlPath=contextPath + "/" + urlPath;
	} else {
	    // root context
	    if( !urlPath.startsWith("/" ) )
		urlPath= "/" + urlPath;
	}

	if( debug >4 ) log("createRequest " + origPath + " " + urlPath  );
	Request req= createSubRequest( ctx.getHost(), urlPath );
	req.setContext( ctx );
	return req;
    }

    /** Create a new sub-request, deal with query string
     */
    private final Request createSubRequest( String host, String urlPath ) {
	String queryString=null;
	int i = urlPath.indexOf("?");
	int len=urlPath.length();
	if (i>-1) {
	    if(i<len)
		queryString =urlPath.substring(i + 1, urlPath.length());
	    urlPath = urlPath.substring(0, i);
	}

	Request lr = new Request();
	lr.setContextManager( this );
	lr.requestURI().setString( urlPath );
	lr.queryString().setString(queryString );

	if( host != null) lr.serverName().setString( host );

	return lr;
    }

    /**
     *   Find a context by doing a sub-request and mapping the request
     *   against the active rules ( that means you could use a /~costin
     *   if a UserHomeInterceptor is present )
     *
     *   The new context will be in the same virtual host as base.
     *
     */
    public final  Context getContext(Context base, String path) {
	// XXX Servlet checks should be done in facade
	if (! path.startsWith("/")) {
	    return null; // according to spec, null is returned
	    // if we can't  return a servlet, so it's more probable
	    // servlets will check for null than IllegalArgument
	}
	// absolute path
	Request lr=this.createSubRequest( base.getHost(), path );
	this.processRequest(lr);
        return lr.getContext();
    }


    // -------------------- Error handling --------------------

    /** Called for error-codes. Will call the error hook with a status code.
     */
    public final void handleStatus( Request req, Response res, int code ) {
	if( code!=0 )
	    res.setStatus( code );
	
	BaseInterceptor ri[];
	int status;
	ri=req.getContainer().getInterceptors( Container.H_handleError );
	
	for( int i=0; i< ri.length; i++ ) {
	    status=ri[i].handleError( req, res, null );
	    if( status!=0 ) return;
	}
    }

    /**
     *  Call error hook with an exception code.
     */
    public final void handleError( Request req, Response res , Throwable t  ) {
	BaseInterceptor ri[];
	int status;
	ri=req.getContainer().getInterceptors( Container.H_handleError );
	
	for( int i=0; i< ri.length; i++ ) {
	    status=ri[i].handleError( req, res, t );
	    if( status!=0 ) return;
	}
    }

    // -------------------- Support for notes --------------------

    /** Note id counters. Synchronized access is not necesarily needed
     *  ( the initialization is in one thread ), but anyway we do it
     */
    public static final int NOTE_COUNT=5;
    private  int noteId[]=new int[NOTE_COUNT];

    /** Maximum number of notes supported
     */
    public static final int MAX_NOTES=32;
    public static final int RESERVED=3;

    public static final int SERVER_NOTE=0;
    public static final int CONTAINER_NOTE=1;
    public static final int REQUEST_NOTE=2;
    public static final int HANDLER_NOTE=3;
    
    public static final int REQ_RE_NOTE=0;

    private String noteName[][]=new String[NOTE_COUNT][MAX_NOTES];
    
    /** used to allow interceptors to set specific per/request, per/container
     * and per/CM informations.
     *
     * This will allow us to remove all "specialized" methods in
     * Request and Container/Context, without losing the functionality.
     * Remember - Interceptors are not supposed to have internal state
     * and minimal configuration, all setup is part of the "core", under
     *  central control.
     *  We use indexed notes instead of attributes for performance -
     * this is internal to tomcat and most of the time in critical path
     */

    /** Create a new note id. Interceptors will get an Id at init time for
     *  all notes that it needs.
     *
     *  Throws exception if too many notes are set ( shouldn't happen in
     *  normal use ).
     *  @param noteType The note will be associated with the server,
     *   container or request.
     *  @param name the name of the note.
     */
    public final synchronized int getNoteId( int noteType, String name )
	throws TomcatException
    {
	// find if we already have a note with this name
	// ( this is in init(), not critical )
	for( int i=0; i< noteId[noteType] ; i++ ) {
	    if( name.equals( noteName[noteType][i] ) )
		return i;
	}

	if( noteId[noteType] >= MAX_NOTES )
	    throw new TomcatException( "Too many notes ");

	// make sure the note id is > RESERVED
	if( noteId[noteType] < RESERVED ) noteId[noteType]=RESERVED;

	noteName[noteType][ noteId[noteType] ]=name;
	return noteId[noteType]++;
    }

    public final String getNoteName( int noteType, int noteId ) {
	return noteName[noteType][noteId];
    }

    // -------------------- Per-server notes --------------------
    private Object notes[]=new Object[MAX_NOTES];
    
    public final void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public final Object getNote( int pos ) {
	return notes[pos];
    }

    public Object getNote( String name ) throws TomcatException {
	int id=getNoteId( SERVER_NOTE, name );
	return getNote( id );
    }

    public void setNote( String name, Object value ) throws TomcatException {
	int id=getNoteId( SERVER_NOTE, name );
	setNote( id, value );
    }
    
    // -------------------- Logging and debug --------------------
    private Log loghelper = new Log("tc_log", "ContextManager");

    /**
     * Get the Logger object that the context manager is writing to (necessary?)
     **/
    public final Logger getLogger() {
	return loghelper.getLogger();
    }

    /**
     * So other classes can piggyback on the context manager's log
     * stream, using Logger.Helper.setProxy()
     **/
    public final Log getLog() {
	return loghelper;
    }
 
    /**
     * Force this object to use the given Logger.
     **/
    public final void setLogger( Logger logger ) {
	log("!!!! setLogger: " + logger, Logger.DEBUG);
	loghelper.setLogger(logger);
    }

    public final void addLogger(Logger l) {
	if (debug>20)
	    log("addLogger: " + l, new Throwable("trace"), Logger.DEBUG);
        loggers.put(l.toString(),l);
    }

    public final Hashtable getLoggers(){
        return loggers;
    }

    public final void log(String msg) {
	loghelper.log(msg);
    }

    public final void log(String msg, Throwable t) {
        loghelper.log(msg, t);
    }

    public final void log(String msg, int level) {
        loghelper.log(msg, level);
    }

    public final void log(String msg, Throwable t, int level) {
        loghelper.log(msg, t, level);
    }
}
