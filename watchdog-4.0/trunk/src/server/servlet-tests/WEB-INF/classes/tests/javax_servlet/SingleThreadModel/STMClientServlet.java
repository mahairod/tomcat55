/*
 * $Header$
 * $Revision$
 * $Date$
 * ====================================================================
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
 * THIS SOFTWARE IS PROVIDED AS IS'' AND ANY EXPRESSED OR IMPLIED
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

package tests.javax_servlet.SingleThreadModel;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

public class STMClientServlet extends HttpServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void destory() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

	int errors = 0;
	try {
	    int threadCount = Integer.valueOf( request.getParameter( "count" ) ).intValue();
	    String path = "/servlet-tests/" + request.getParameter( "path" );
	    ThreadClient tc = new ThreadClient( request.getServerName(), request.getServerPort(),
					    threadCount, path );
	    errors = tc.runTest();	   
	} catch ( Exception e ) {
	    throw new ServletException( "SingleThreadModel Test FAILED!", e );
	}
	if ( errors > 0 ) {
	    throw new ServletException( "SingleThreadModel Test FAILED!" );
	}
    }
    
    public String getServletInfo() {
        return "SingleThreadModel Client Servlet";
    }

    private class ThreadClient {
        
        private final int SLEEPTIME      = 5000;
	private final int NUM_REQUESTS   = 3;

	// For Thread Synchronization
	private int threadCount          = 0;
	private int threadsDone          = 0;
	private int errors               = 0;
	private int port                 = 0;
	private Object lock              = new Object();
	private Object startLock         = new Object();
	private Object workLock          = new Object();
	private String hostname          = null;
	private String requestPath       = null;

	public ThreadClient( String hostname, int port, int threadCount, String requestPath ) {
	    this.hostname    = hostname;
	    this.port        = port;
	    this.threadCount = threadCount;
	    this.requestPath  = requestPath;
	}

	public int runTest() throws Exception {

	    try {
		Thread[] testThread = new Thread[ threadCount ];

		for ( int i = 0; i < threadCount; i++ ) {
		    testThread[ i ] = new Thread( new TestThread( i ), "TestThread-"
+ i );
		    testThread[ i ].setPriority( Thread.MAX_PRIORITY );
		    testThread[ i ].start();
		}

		synchronized( lock ) {
		    while ( threadsDone < testThread.length ) {
			lock.wait();
		    }

		    try {
			Thread.sleep( SLEEPTIME );
		    } catch ( Exception e ) {
			;
		    }
		}

		//notify all to start
		synchronized( startLock ) {
		    threadsDone = 0;
		    startLock.notifyAll();
		}
		//wait for completion
		synchronized( lock ) {
		    while ( threadsDone < testThread.length ) {
			lock.wait();
		    }
		}

		if ( errors > 0 ) {
		    log( "[STMClient] Number of Errors: " + errors );
		    log( "[STMClient] Test FAILED" );
		} else {
		    log( "[STMClient] No Errors.  Test PASSED" );
		}
	    } catch ( Exception e ) {
		throw e;
	    }
	    return errors;
	}

	private class TestThread implements Runnable {

	    // Instance variables
	    private int threadNum       = 0;
	    private boolean synchronize = true;

	    public TestThread( int threadNum ) {
		this.threadNum = threadNum;
	    }

	    public void run() {

		synchronized( lock ) {
		    ++threadsDone;
		    lock.notifyAll();
		}

		synchronized( startLock ) {
		    try {
			startLock.wait();
		    } catch ( InterruptedException ie ) {
			;
		    }
		}
		this.runSingleThreadModelTest();
		
		synchronized( lock ) {
		    ++threadsDone;
		    lock.notifyAll();
		}
	    }

	    public void runSingleThreadModelTest() {
		
		for ( int i = 0; i < 3; i++ ) {
		    try {
			URL url = new URL( "http://" + hostname + ":" + port +
					   requestPath );
			HttpURLConnection conn = (HttpURLConnection)
			    url.openConnection();
			conn.setRequestMethod( "GET" );
			conn.connect();
			int code = conn.getResponseCode();
			if ( code != HttpURLConnection.HTTP_OK ) {
			    synchronized( lock ) {
				++errors;
			    }
			}
			Thread.sleep( 1000 );
		    } catch ( Exception e ) {
			log( "[STMClient] Unexpected Exception in runSingleThreadModelTest()!" );
			log( "[STMClient] Exception: " + e.toString() );
			log( "[STMClient] Message: " + e.getMessage() );
			e.printStackTrace();
			synchronized( lock ) {
			    ++errors;
			}
		    }
		}
		
	    }
	}
    }
}
