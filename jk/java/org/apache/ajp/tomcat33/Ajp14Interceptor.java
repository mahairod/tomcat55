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

package org.apache.ajp.tomcat33;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.ajp.*;

import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.core.*;

import org.apache.tomcat.util.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;

/** Note. PoolTcpConnector is a convenience base class used for
    TCP-based connectors in tomcat33. It allows all those modules
    to share the thread pool and listener code.

    In future it's likely other optimizations will be implemented in
    the PoolTcpConnector, so it's better to use it if you don't have
    a good reason not to ( like a connector for J2ME, where you want
    minimal footprint and don't care about high load )
*/

/** Tomcat 33 module implementing the Ajp14 protocol.
 *
 *  The actual protocol implementation is in Ajp14.java, this is just an
 *  adapter to plug it into tomcat.
 */
public class Ajp14Interceptor extends PoolTcpConnector
    implements  TcpConnectionHandler
{
    int ajp14_note=-1;
    String password;
    
    public Ajp14Interceptor()
    {
        super();
    }

    // initialization
    public void engineInit(ContextManager cm) throws TomcatException {
	super.engineInit( cm );
	ajp14_note=cm.getNoteId( ContextManager.REQUEST_NOTE, "ajp14" );
    }

    // -------------------- Ajp14 specific parameters --------------------

    public void setPassword( String s ) {
	log( "Password=" + s);
	this.password=s;
    }

    
    // -------------------- PoolTcpConnector --------------------

    /** Called by PoolTcpConnector to allow childs to init.
     */
    protected void localInit() throws Exception {
	ep.setConnectionHandler( this );
    }

    // -------------------- Handler implementation --------------------

    /*  The TcpConnectionHandler interface is used by the PoolTcpConnector to
     *  handle incoming connections.
     */

    /** Called by the thread pool when a new thread is added to the pool,
	in order to create the (expensive) objects that will be stored
	as thread data.
	XXX we should use a single object, not array ( several reasons ),
	XXX Ajp14 should be storead as a request note, to be available in
	all modules
    */
    public Object[] init()
    {
        Object thData[]=new Object[1];
	thData[0]=initRequest( null );
	return thData;
    }

    /** Construct the request object, with probably unnecesary
	sanity tests ( should work without thread pool - but that is
	not supported in PoolTcpConnector, maybe in future )
    */
    private Ajp14Request initRequest(Object thData[] ) {
	if( ajp14_note < 0 ) throw new RuntimeException( "assert: ajp14_note>0" );
	Ajp14Request req=null;
	if( thData != null ) {
	    req=(Ajp14Request)thData[0];
	}
	if( req != null ) {
	    Response res=req.getResponse();
	    req.recycle();
	    res.recycle();
	    // make the note available to other modules
	    req.setNote( ajp14_note, req.ajp14);
	    return req;
	}
	// either thData==null or broken ( req==null)
	Ajp14 ajp14=new Ajp14();
	ajp14.setContainerSignature( ContextManager.TOMCAT_NAME +
				     " v" + ContextManager.TOMCAT_VERSION);
	AjpRequest ajpreq=new AjpRequest();
	log( "Setting pass " + password );
	ajp14.setPassword( password );
	req=new Ajp14Request(ajp14, ajpreq);
	Ajp14Response res=new Ajp14Response(ajp14);
	cm.initRequest(req, res);
	return  req;
    }
    
    /** Called whenever a new TCP connection is received. The connection
	is reused.
     */
    public void processConnection(TcpConnection connection, Object thData[])
    {
        try {
            Socket socket = connection.getSocket();
	    // assert: socket!=null, connection!=null ( checked by PoolTcpEndpoint )
	    
            socket.setSoLinger( true, 100);

            Ajp14Request req=initRequest( thData );
            Ajp14Response res= (Ajp14Response)req.getResponse();
            Ajp14 ajp14=req.ajp14;
	    AjpRequest ajpReq=req.ajpReq;

            ajp14.setSocket(socket);

	    if( debug>0)
		log( "Received ajp14 connection ");

	    // first request should be the loginit.
	    int status=ajp14.receiveNextRequest( ajpReq );
	    if( status != 304 )  { // XXX use better codes
		log( "Failure in logInit ");
		return;
	    }

	    status=ajp14.receiveNextRequest( ajpReq );
	    if( status != 304 ) { // XXX use better codes
		log( "Failure in login ");
		return;
	    }
	    
            boolean moreRequests = true;
            while(moreRequests) {
		status=ajp14.receiveNextRequest( ajpReq );

		if( status==-2) {
		    // special case - shutdown
		    // XXX need better communication, refactor it
		    if( !doShutdown(socket.getLocalAddress(),
				    socket.getInetAddress())) {
			moreRequests = false;
			continue;
		    }                        
		}
		
		if( status  == 200)
		    cm.service(req, res);
		else if (status == 500) {
		    log( "Invalid request received " + req );
		    break;
		}
		
		req.recycle();
		res.recycle();
            }
            if( debug > 0 ) log("Closing ajp14 connection");
            ajp14.close();
	    socket.close();
        } catch (Exception e) {
	    log("Processing connection " + connection, e);
        }
    }

    // We don't need to check isSameAddress if we authenticate !!!
    protected boolean doShutdown(InetAddress serverAddr,
                                 InetAddress clientAddr)
    {
        try {
	    // close the socket connection before handling any signal
	    // but get the addresses first so they are not corrupted			
            if(isSameAddress(serverAddr, clientAddr)) {
		cm.stop();
		// same behavior as in past, because it seems that
		// stopping everything doesn't work - need to figure
		// out what happens with the threads ( XXX )

		// XXX It should work now - but will fail if servlets create
		// threads
		System.exit(0);
	    }
	} catch(Exception ignored) {
	    log("Ignored " + ignored);
	}
	log("Shutdown command ignored");
	return false;
    }

    // legacy, should be removed 
    public void setServer(Object contextM)
    {
        this.cm=(ContextManager)contextM;
    }
    

}


//-------------------- Glue code for request/response.
// Probably not needed ( or can be simplified ), but it's
// not that bad.

class Ajp14Request extends Request 
{
    Ajp14 ajp14;
    AjpRequest ajpReq;
    
    public Ajp14Request(Ajp14 ajp14, AjpRequest ajpReq) 
    {
        this.ajp14=ajp14;
	this.ajpReq=ajpReq;
    }

    // XXX This should go away if we introduce an InputBuffer.
    // We almost have it as result of encoding fixes, but for now
    // just keep this here, doesn't hurt too much.
    public int doRead() throws IOException 
    {
	if( available <= 0 )
	    return -1;
	available--;
	return ajp14.doRead();
    }
    
    public int doRead(byte[] b, int off, int len) throws IOException 
    {
	if( available <= 0 )
	    return -1;
	int rd=ajp14.doRead( b,off, len );
	available -= rd;
	return rd;
    }
    
    public void recycle() 
    {
        super.recycle();
	if( ajp14!=null) ajp14.recycle();
    }
}

class Ajp14Response extends Response 
{
    Ajp14 ajp14;
    boolean finished=false;
    
    public Ajp14Response(Ajp14 ajp14) 
    {
	super();
	this.ajp14=ajp14;
    }

    public void recycle() {
	super.recycle();
	finished=false;
    }

    // XXX if more headers that MAX_SIZE, send 2 packets!
    // XXX Can be implemented using module notification, no need to extend
    public void endHeaders() throws IOException 
    {
        super.endHeaders();
    
        if (request.protocol().isNull()) {
            return;
        }

	ajp14.sendHeaders(getStatus(), getMimeHeaders());
    } 

    // XXX Can be implemented using module notification, no need to extend
    public void finish() throws IOException 
    {
	if(!finished) {
	    super.finish();
		finished = true; // Avoid END_OF_RESPONSE sent 2 times
	    ajp14.finish();
	}
    }

    // XXX Can be implemented using the buffers, no need to extend
    public void doWrite(  byte b[], int off, int len) throws IOException 
    {
	ajp14.doWrite(b, off, len );
    }
    
}
