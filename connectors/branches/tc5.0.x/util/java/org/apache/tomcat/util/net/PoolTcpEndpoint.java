/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.AccessControlException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadPoolRunnable;

/* Similar with MPM module in Apache2.0. Handles all the details related with
   "tcp server" functionality - thread management, accept policy, etc.
   It should do nothing more - as soon as it get a socket ( and all socket options
   are set, etc), it just handle the stream to ConnectionHandler.processConnection. (costin)
*/



/**
 * Handle incoming TCP connections.
 *
 * This class implement a simple server model: one listener thread accepts on a socket and
 * creates a new worker thread for each incoming connection.
 *
 * More advanced Endpoints will reuse the threads, use queues, etc.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin@eng.sun.com
 * @author Gal Shachor [shachor@il.ibm.com]
 * @author Yoav Shapira <yoavs@apache.org>
 */
public class PoolTcpEndpoint { // implements Endpoint {

    private StringManager sm = 
        StringManager.getManager("org.apache.tomcat.util.net.res");

    private static final int BACKLOG = 100;
    private static final int TIMEOUT = 1000;

    private final Object threadSync = new Object();

    private boolean isPool = true;

    private int backlog = BACKLOG;
    private int serverTimeout = TIMEOUT;

    TcpConnectionHandler handler;

    private InetAddress inet;
    private int port;

    private ServerSocketFactory factory;
    private ServerSocket serverSocket;

    ThreadPoolRunnable listener;
    private volatile boolean running = false;
    private volatile boolean paused = false;
    private boolean initialized = false;
    private boolean reinitializing = false;
    static final int debug=0;

    ThreadPool tp;

    static Log log=LogFactory.getLog(PoolTcpEndpoint.class );

    protected boolean tcpNoDelay=false;
    protected int linger=100;
    protected int socketTimeout=-1;
    
    public PoolTcpEndpoint() {
	tp = new ThreadPool();
    }

    public PoolTcpEndpoint( ThreadPool tp ) {
        this.tp=tp;
    }

    // -------------------- Configuration --------------------

    public void setPoolOn(boolean isPool) {
        this.isPool = isPool;
    }

    public boolean isPoolOn() {
        return isPool;
    }

    public void setMaxThreads(int maxThreads) {
	if( maxThreads > 0)
	    tp.setMaxThreads(maxThreads);
    }

    public int getMaxThreads() {
        return tp.getMaxThreads();
    }

    public void setMaxSpareThreads(int maxThreads) {
	if(maxThreads > 0) 
	    tp.setMaxSpareThreads(maxThreads);
    }

    public int getMaxSpareThreads() {
        return tp.getMaxSpareThreads();
    }

    public void setMinSpareThreads(int minThreads) {
	if(minThreads > 0) 
	    tp.setMinSpareThreads(minThreads);
    }

    public int getMinSpareThreads() {
        return tp.getMinSpareThreads();
    }

    public void setThreadPriority(int threadPriority) {
      tp.setThreadPriority(threadPriority);
    }

    public int getThreadPriority() {
      return tp.getThreadPriority();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port ) {
        this.port=port;
    }

    public InetAddress getAddress() {
	    return inet;
    }

    public void setAddress(InetAddress inet) {
	    this.inet=inet;
    }

    public void setServerSocket(ServerSocket ss) {
	    serverSocket = ss;
    }

    public void setServerSocketFactory(  ServerSocketFactory factory ) {
	    this.factory=factory;
    }

   ServerSocketFactory getServerSocketFactory() {
 	    return factory;
   }

    public void setConnectionHandler( TcpConnectionHandler handler ) {
    	this.handler=handler;
    }

    public TcpConnectionHandler getConnectionHandler() {
	    return handler;
    }

    public boolean isRunning() {
	return running;
    }
    
    public boolean isPaused() {
	return paused;
    }
    
    /**
     * Allows the server developer to specify the backlog that
     * should be used for server sockets. By default, this value
     * is 100.
     */
    public void setBacklog(int backlog) {
	if( backlog>0)
	    this.backlog = backlog;
    }

    public int getBacklog() {
        return backlog;
    }

    /**
     * Sets the timeout in ms of the server sockets created by this
     * server. This method allows the developer to make servers
     * more or less responsive to having their server sockets
     * shut down.
     *
     * <p>By default this value is 1000ms.
     */
    public void setServerTimeout(int timeout) {
	this.serverTimeout = timeout;
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }
    
    public void setTcpNoDelay( boolean b ) {
	tcpNoDelay=b;
    }

    public int getSoLinger() {
        return linger;
    }
    
    public void setSoLinger( int i ) {
	linger=i;
    }

    public int getSoTimeout() {
        return socketTimeout;
    }
    
    public void setSoTimeout( int i ) {
	socketTimeout=i;
    }
    
    public int getServerSoTimeout() {
        return serverTimeout;
    }  
    
    public void setServerSoTimeout( int i ) {
	serverTimeout=i;
    }

    // -------------------- Public methods --------------------

    public void initEndpoint() throws IOException, InstantiationException {
	try {
	    if(factory==null)
		factory=ServerSocketFactory.getDefault();
	    if(serverSocket==null) {
                try {
                    if (inet == null) {
                        serverSocket = factory.createSocket(port, backlog);
                    } else {
                        serverSocket = factory.createSocket(port, backlog, inet);
                    }
                } catch ( BindException be ) {
                    throw new BindException(be.getMessage() + ":" + port);
                }
	    }
            if( serverTimeout >= 0 )
		serverSocket.setSoTimeout( serverTimeout );
	} catch( IOException ex ) {
	    //	    log("couldn't start endpoint", ex, Logger.DEBUG);
            throw ex;
	} catch( InstantiationException ex1 ) {
	    //	    log("couldn't start endpoint", ex1, Logger.DEBUG);
            throw ex1;
	}
        initialized = true;
    }

    public void startEndpoint() throws IOException, InstantiationException {
        if (!initialized) {
            initEndpoint();
        }
	if(isPool) {
	    tp.start();
	}
	running = true;
        paused = false;
        if(isPool) {
    	    listener = new TcpWorkerThread(this);
            tp.runIt(listener);
        } else {
	    log.error("XXX Error - need pool !");
	}
    }

    public void pauseEndpoint() {
        if (running && !paused) {
            paused = true;
            unlockAccept();
        }
    }

    public void resumeEndpoint() {
        if (running) {
            paused = false;
        }
    }

    public void stopEndpoint() {
	if (running) {
	    tp.shutdown();
	    running = false;
            if (serverSocket != null) {
                closeServerSocket();
            }
	}
    }

    protected void closeServerSocket() {
        if (!paused)
            unlockAccept();
        try {
            if( serverSocket!=null)
                serverSocket.close();
        } catch(Exception e) {
            log.error("Caught exception trying to close socket.", e);
        }
        serverSocket = null;
    }

    protected void unlockAccept() {
        Socket s = null;
        try {
            // Need to create a connection to unlock the accept();
            if (inet == null) {
                s=new Socket("127.0.0.1", port );
            }else{
                s=new Socket(inet, port );
                    // setting soLinger to a small value will help shutdown the
                    // connection quicker
                s.setSoLinger(true, 0);
            }
        } catch(Exception e) {
            log.debug("Caught exception trying to unlock accept on " + port
                      + " " + e.toString());
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    // -------------------- Private methods

    Socket acceptSocket() {
        if( !running || serverSocket==null ) return null;

        Socket accepted = null;

    	try {
            if(factory==null) {
                accepted = serverSocket.accept();
            } else {
                accepted = factory.acceptSocket(serverSocket);
            }
            if (null == accepted) {
                log.warn("Null socket returned by accept");
            } else {
                if (!running) {
                    accepted.close();  // rude, but unlikely!
                    accepted = null;
                } else if (factory != null) {
                    factory.initSocket( accepted );
                }
            }
        }
        catch(InterruptedIOException iioe) {
            // normal part -- should happen regularly so
            // that the endpoint can release if the server
            // is shutdown.
        }
        catch (AccessControlException ace) {
            // When using the Java SecurityManager this exception
            // can be thrown if you are restricting access to the
            // socket with SocketPermission's.
            // Log the unauthorized access and continue
            String msg = sm.getString("endpoint.warn.security",
                                      serverSocket,ace);
            log.warn(msg);
        }
        catch (IOException e) {

            String msg = null;

            if (running) {
                msg = sm.getString("endpoint.err.nonfatal",
                        serverSocket, e);
                log.error(msg, e);
            }

            if (accepted != null) {
                try {
                    accepted.close();
                } catch(Throwable ex) {
                    msg = sm.getString("endpoint.err.nonfatal",
                                       accepted, ex);
                    log.warn(msg, ex);
                }
                accepted = null;
            }

            if( ! running ) return null;
            reinitializing = true;
            // Restart endpoint when getting an IOException during accept
            synchronized (threadSync) {
                if (reinitializing) {
                    reinitializing = false;
                    // 1) Attempt to close server socket
                    closeServerSocket();
                    initialized = false;
                    // 2) Reinit endpoint (recreate server socket)
                    try {
                        msg = sm.getString("endpoint.warn.reinit");
                        log.warn(msg);
                        initEndpoint();
                    } catch (Throwable t) {
                        msg = sm.getString("endpoint.err.nonfatal",
                                           serverSocket, t);
                        log.error(msg, t);
                    }
                    // 3) If failed, attempt to restart endpoint
                    if (!initialized) {
                        msg = sm.getString("endpoint.warn.restart");
                        log.warn(msg);
                        try {
                            stopEndpoint();
                            initEndpoint();
                            startEndpoint();
                        } catch (Throwable t) {
                            msg = sm.getString("endpoint.err.fatal",
                                               serverSocket, t);
                            log.error(msg, t);
                        } finally {
                            // Current thread is now invalid: kill it
                            throw new ThreadDeath();
                        }
                    }
                }
            }

        }

        return accepted;
    }

    /** @deprecated
     */
    public void log(String msg)
    {
	log.info(msg);
    }

    /** @deprecated
     */
    public void log(String msg, Throwable t)
    {
	log.error( msg, t );
    }

    /** @deprecated
     */
    public void log(String msg, int level)
    {
	log.info( msg );
    }

    /** @deprecated
     */
    public void log(String msg, Throwable t, int level) {
    	log.error( msg, t );
    }

    void setSocketOptions(Socket socket)
        throws SocketException {
        if(linger >= 0 ) 
            socket.setSoLinger( true, linger);
        if( tcpNoDelay )
            socket.setTcpNoDelay(tcpNoDelay);
        if( socketTimeout > 0 )
            socket.setSoTimeout( socketTimeout );
    }

}

// -------------------- Threads --------------------

/*
 * I switched the threading model here.
 *
 * We used to have a "listener" thread and a "connection"
 * thread, this results in code simplicity but also a needless
 * thread switch.
 *
 * Instead I am now using a pool of threads, all the threads are
 * simmetric in their execution and no thread switch is needed.
 */
class TcpWorkerThread implements ThreadPoolRunnable {
    /* This is not a normal Runnable - it gets attached to an existing
       thread, runs and when run() ends - the thread keeps running.

       It's better to keep the name ThreadPoolRunnable - avoid confusion.
       We also want to use per/thread data and avoid sync wherever possible.
    */
    PoolTcpEndpoint endpoint;
    
    public TcpWorkerThread(PoolTcpEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Object[] getInitData() {
        // no synchronization overhead, but 2 array access 
        Object obj[]=new Object[2];
        obj[1]= endpoint.getConnectionHandler().init();
        obj[0]=new TcpConnection();
        return obj;
    }
    
    public void runIt(Object perThrData[]) {

        // Create per-thread cache
        if (endpoint.isRunning()) {

            // Loop if endpoint is paused
            while (endpoint.isPaused()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }

            // Accept a new connection
            Socket s = null;
            try {
                s = endpoint.acceptSocket();
            } finally {
                // Continue accepting on another thread...
                if (endpoint.isRunning()) {
                    endpoint.tp.runIt(this);
                }
            }

            // Process the connection
            if (null != s) {
                TcpConnection con = null;
                int step = 1;
                try {

                    // 1: Set socket options: timeout, linger, etc
                    endpoint.setSocketOptions(s);

                    // 2: SSL handshake
                    step = 2;
                    if (endpoint.getServerSocketFactory() != null) {
                        endpoint.getServerSocketFactory().handshake(s);
                    }

                    // 3: Process the connection
                    step = 3;
                    con = (TcpConnection) perThrData[0];
                    con.setEndpoint(endpoint);
                    con.setSocket(s);
                    endpoint.getConnectionHandler().processConnection(
                        con,
                        (Object[]) perThrData[1]);

                } catch (SocketException se) {
                    PoolTcpEndpoint.log.error(
                        "Remote Host "
                            + s.getInetAddress()
                            + " SocketException: "
                            + se.getMessage());
                    // Try to close the socket
                    try {
                        s.close();
                    } catch (IOException e) {
                    }
                } catch (Throwable t) {
                    if (step == 2) {
                        PoolTcpEndpoint.log.debug("Handshake failed", t);
                    } else {
                        PoolTcpEndpoint.log.error("Unexpected error", t);
                    }
                    // Try to close the socket
                    try {
                        s.close();
                    } catch (IOException e) {
                    }
                } finally {
                    if (con != null) {
                        con.recycle();
                    }
                }
            }

        }
    }
    
}
