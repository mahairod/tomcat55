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
package org.apache.tomcat.facade;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Handler for servlets. It'll implement all servlet-specific
 * requirements ( init, Unavailable exception, etc).
 *
 * It is also used for Jsps ( since a Jsp is a servlet ), but
 * requires the Jsp interceptor to make sure that indeed a Jsp is
 * a servlet ( and set the class name ).
 * 
 * The old Jsp hack is no longer supported ( i.e. declaring a servlet
 * with the name jsp, mapping *.jsp -> jsp will work as required
 * by the servlet spec - no special hook is provided for initialization ).
 * Note that JspServlet doesn't work without special cases in ServletWrapper.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public final class ServletHandler extends Handler {

    /** 
     * If init() fails or preInit() detects the handler is still
     * unavailable.
     */
    public static final int STATE_DELAYED_INIT=2;

    /** The handler has been succesfully initialized and is ready to
     * serve requests. If the handler is not in this state a 500 error
     * should be reported. ( customize - may be 404 )
     * To ADDED by calling destroy()
     * FROM ADDED by calling init()
     */
    public static final int STATE_READY=3;

    // -------------------- Properties --------------------
    
    // extra informations - if the servlet is declared in web.xml
    private ServletInfo sw;

    private String servletClassName;
    protected Class servletClass;
    protected Servlet servlet;
    protected Context context;
    
    // If init() fails, Handler.errorException will hold the reason.
    // In the case of an UnavailableException, this field will hold
    // the expiration time if UnavailableException is not permanent.
    long unavailableTime=-1;
    
    public ServletHandler() {
	super();
    }

    public String toString() {
	return "ServletH " + name + "(" + sw  + ")";
    }

    public void setServletInfo( ServletInfo sw ) {
	this.sw=sw;
    }
    
    public ServletInfo getServletInfo() {
	if( sw==null ) {
	    // it is possible to create a handler without ServletInfo
	    // defaults are used.
	    sw=new ServletInfo(this);
	}
	return sw;
    }

    /**
     */
    public void setContext( Context context) {
        this.context = context;
    }

    /** Return the context associated with the handler
     */
    public Context getContext() {
	return context;
    }


    public void setServletClassName( String servletClassName) {
	servlet=null; // reset the servlet, if it was set
	servletClass=null;
	this.servletClassName=servletClassName;
	if( debug>0 && sw.getJspFile()!=null)
	    log( "setServletClassName for " + sw.getJspFile() +
		 ": " + servletClassName);
    }

    public String getServletClassName() {
	if( servletClassName == null )
	    servletClassName=name;
        return servletClassName;
    }

    // -------------------- Init/destroy --------------------
    // from Handler

    /** Destroy a handler, and notify all the interested interceptors
     */
    public final void destroy() {
	if ( state!=STATE_READY ) {
	    // reset exception
	    errorException = null;
	    return;// already destroyed or not init.
	}
	setState( STATE_ADDED );

	// XXX post will not be called if any error happens in destroy.
	// That's how tomcat worked before - I think it's a bug !
	try {
	    doDestroy();
	} catch( Exception ex ) {
	    log( "Error during destroy ", ex );
	}
	

	errorException=null;
    }


    /** Call the init method, and notify all interested listeners.
     *  This is a final method to insure consistent behavior on errors.
     *  It also saves handlers from dealing with synchronization issues.
     */
    public final void init()
    {
	// we use getState() as a workaround for bugs in VMs
	
	if( getState() == STATE_READY || getState() == STATE_DISABLED )
	    return;

	synchronized( this ) {
	    // check again - if 2 threads are in init(), the first one will
	    // init and the second will enter the sync block after that
	    if( getState() == STATE_READY ) 
		return;

	    // if exception present, then we were sync blocked when first
	    // init() failed or an interceptor set an inital exeception
	    // A different thread got an error in init() - throw
	    // the same error.
	    if (getState() == STATE_DISABLED )
		return; //throw errorException;

	    try {
		// special preInit() hook
		preInit();
		// preInit may either throw exception or setState DELAYED_INIT
	    } catch( Exception ex ) {
		// save error, assume permanent
		log("Exception in preInit  " + ex.getMessage(), ex );
		setErrorException(ex);
		setState(STATE_DISABLED);
		return;
	    }
	    
	    // we'll try again later 
	    if( getState() == STATE_DELAYED_INIT ||
		getState()==STATE_DISABLED ) { // or disabled 
		return;
	    }
	    // preInit have no exceptions and doesn't delay us
	    // We can run init hooks and init

	    // Call pre, doInit and post
	    BaseInterceptor cI[]=context.getContainer().getInterceptors();
	    for( int i=0; i< cI.length; i++ ) {
		try {
		    cI[i].preServletInit( context, this );
		} catch( TomcatException ex) {
		    // log, but ignore.
		    log("preServletInit" , ex);
		}
	    }
		
	    try {
		doInit();
		// if success, we are ready to serve
	    } catch( Exception ex ) {
		// save error, assume permanent
		log("Exception in init  " + ex.getMessage(), ex );
		setErrorException(ex);
		state=STATE_DISABLED;
	    }
	    
	    for( int i=0; i< cI.length; i++ ) {
		try {
		    cI[i].postServletInit( context, this );
		} catch( TomcatException ex) {
		    log("postServletInit" , ex);
		}
	    }

	    // Now that both pre/post hooks have been called, the
	    // servlet is ready to serve.

	    // We are still in the sync block, that means other threads
	    // are waiting for this to be over.

	    // if no error happened and if doInit didn't put us in
	    // a special state, we are ready
	    if( state!=STATE_DISABLED &&
		getErrorException() != null ) {
		state=STATE_READY;
	    }
	}
    }

    
    
    // -------------------- --------------------

    public void reload() {
	if( getState()==STATE_READY ) {
	    try {
		destroy();
	    } catch(Exception ex ) {
		log( "Error in destroy ", ex );
	    }
	}

	if( sw.getServletClassName() != null ) {
	    // I can survive reload
	    servlet=null;
	    servletClass=null;
	}
	setState( STATE_ADDED );
    }
    
    // -------------------- 

    public Servlet getServlet() 
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException
    {
	if(servlet!=null)
	    return servlet;

	if( debug>0)
	    log("LoadServlet " + name + " " + sw.getServletName() + " " +
		sw.getServletClassName() + " " + servletClass );

	// default
	if( servletClassName==null )
	    servletClassName=name;
	
	if (servletClass == null) {
	    servletClass=context.getClassLoader().loadClass(servletClassName);
	}
	
	servlet = (Servlet)servletClass.newInstance();
	return servlet;
    }

    // -------------------- Destroy --------------------
    
    protected void doDestroy() throws TomcatException {
	synchronized (this) {
	    try {
		if( servlet!=null) {
		    BaseInterceptor cI[]=context.
			getContainer().getInterceptors();
		    for( int i=0; i< cI.length; i++ ) {
			try {
			    cI[i].preServletDestroy( context, this );
			} catch( TomcatException ex) {
			    log("preServletDestroy", ex);
			}
		    }
		    servlet.destroy();

		    for( int i=0; i< cI.length; i++ ) {
			try {
			    cI[i].postServletDestroy( context, this );
			} catch( TomcatException ex) {
			    log("postServletDestroy", ex);
			}
		    }
		}
	    } catch(Exception ex) {
		// Should never come here...
		log( "Error in destroy ", ex );
	    }
	}
    }

    // Special hook
    protected void preInit() throws Exception
    {
	if( debug > 0 )
	    log( "preInit " + servlet + " " + sw.getJspFile() + " " +
		 servletClassName);

	// Deal with Unavailable errors
	if( ! checkAvailable() ) {
	    // init can't proceed
	    setState( STATE_DELAYED_INIT );
	    return;
	}
	
	// For JSPs we rely on JspInterceptor to set the servlet class name.
	// We make no distinction between servlets and jsps.
	getServlet();
    }

    
    protected void doInit()
	throws Exception
    {
	try {
	    servlet.init( getServletInfo().getServletConfig() );
	} catch( UnavailableException ex ) {
	    // Implement the "UnavailableException" specification
	    // servlet exception state
	    setErrorException( ex );
	    if ( ex.isPermanent() ) {
		setState( STATE_DISABLED );
	    } else {
		setState( STATE_DELAYED_INIT );
		setServletUnavailable( ex );
	    }
	    servlet=null;
	    throw ex;
	    // it's a normal exception, servlet will
	    // not be initialized.
	}

	// other exceptions are just thrown -
	// init() will deal with them.
    }

    // Overrides the default handler
    public void service ( Request req, Response res ) {
	if( state!=STATE_READY ) {
	    if( state!= STATE_DISABLED ) {
		init();
	    }
	    if( state== STATE_DISABLED || state==STATE_DELAYED_INIT ) {
		// the init failed because of an exception
		Exception ex=getErrorException();
		// save error state on request and response
		saveError( req, res, ex );
		// if in included, defer handling to higher level
		if (res.isIncluded()) return;
		// handle init error since at top level
		if( ex instanceof ClassNotFoundException )
		    contextM.handleStatus( req, res, 404 );
		else
		    contextM.handleError( req, res, ex );
		return;
	    } 
	}

	super.service( req, res );
    }

    
    protected void doService(Request req, Response res)
	throws Exception
    {
	// <servlet><jsp-file> case
	String jspFile=sw.getJspFile();
	if( jspFile!=null ) {
	    if( jspFile.startsWith("/"))
		req.setAttribute( "javax.servlet.include.request_uri",
				  req.getContext().getPath()  + jspFile );
	    else
		req.setAttribute( "javax.servlet.include.request_uri",
				  req.getContext().getPath()  + "/" +jspFile );
	    req.setAttribute( "javax.servlet.include.servlet_path", jspFile );
	}


	// if unavailable
	if( ! checkAvailable() ) {
	    // if error still present
	    Exception ex=getErrorException();
	    if( ex!=null ) {
		// save error state on request and response
		saveError( req, res, ex );
	    }
	    // if we have an error on this request
	    if ( req.isExceptionPresent()) {
		// if in included, defer handling to higher level
		if ( res.isIncluded() )
		    return;
		// otherwise handle error
		contextM.handleError( req, res, getErrorException());
	    }
	    return; // we can't handle it
	}

	// Get facades - each req have one facade per context
	// the facade itself is very light.

	// For big servers ( with >100s of webapps ) we can
	// use a pool or other technique. Right now there
	// are many other ( much more expensive ) resources
	// associated with contexts ( like the session thread)

	// XXX
	HttpServletRequest reqF= (HttpServletRequest)req.getFacade();
	HttpServletResponse resF= (HttpServletResponse)res.getFacade();
	if( reqF == null || resF == null ||
	    ! (reqF instanceof HttpServletRequestFacade) ) {
	    reqF=new HttpServletRequestFacade(req);
	    resF=new HttpServletResponseFacade(res);
	    req.setFacade( reqF );
	    res.setFacade( resF );
	}

	try {
	    // We are initialized and fine
	    if (servlet instanceof SingleThreadModel) {
		synchronized(servlet) {
		    servlet.service(reqF, resF);
		}
	    } else {
		servlet.service(reqF, resF);
	    }
	} catch ( UnavailableException ex ) {
	    if ( ex.isPermanent() ) {
		setState( STATE_DISABLED );
	    } else {
		setServletUnavailable((UnavailableException)ex );
	    }
	    if ( null != getErrorException() ) {
		synchronized(this) {
		    if ( null!= getErrorException() ) {
			// servlet exception state
			setErrorException( ex );
		    }
		}
	    }
	}
	// other exceptions will be thrown
    }

    // -------------------- Unavailable --------------------

    /** Set unavailable time
     */
    private void setServletUnavailable( UnavailableException ex ) {
	unavailableTime=System.currentTimeMillis() +
	    ex.getUnavailableSeconds() * 1000;
	setErrorException( ex );
    }

    /** Check if error exception is present and if so, has the error
	expired.  Sets error on request and response if un-expired
	error found.
     */
    private boolean checkAvailable() {
	if( unavailableTime == -1 )
	    return true;
	
	// if permanent exception this code isn't called
	// if timer not expired
	if ( (unavailableTime - System.currentTimeMillis()) < 0) {
	    // disable the error - it expired
	    unavailableTime=-1;
	    setErrorException(null);
	    context.log(getName() +
			" unavailable time expired, trying again ");
	    return true;
	}
	// still unavailable
	return false;
    }


}
