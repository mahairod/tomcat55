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

package org.apache.coyote.http11;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.modeler.Registry;
import org.apache.coyote.ActionCode;
import org.apache.coyote.ActionHook;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.RequestGroupInfo;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.net.PoolTcpEndpoint;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.TcpConnection;
import org.apache.tomcat.util.net.TcpConnectionHandler;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadWithAttributes;


/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public class Http11Protocol implements ProtocolHandler, MBeanRegistration
{
    public Http11Protocol() {
        cHandler = new Http11ConnectionHandler( this );
        setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        setServerSoTimeout(Constants.DEFAULT_SERVER_SOCKET_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }

    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);

    /** Pass config info
     */
    public void setAttribute( String name, Object value ) {
        if( log.isTraceEnabled())
            log.trace(sm.getString("http11protocol.setattribute", name, value));

        attributes.put(name, value);
    }

    public Object getAttribute( String key ) {
        if( log.isTraceEnabled())
            log.trace(sm.getString("http11protocol.getattribute", key));
        return attributes.get(key);
    }

    /**
     * Set a property.
     */
    public void setProperty(String name, String value) {
        setAttribute(name, value);
    }

    /**
     * Get a property
     */
    public String getProperty(String name) {
        return (String)getAttribute(name);
    }

    /** The adapter, used to call the connector 
     */
    public void setAdapter(Adapter adapter) {
        this.adapter=adapter;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    
    /** Start the protocol
     */
    public void init() throws Exception {
        ep.setConnectionHandler( cHandler );
    try {
            checkSocketFactory();
        } catch( Exception ex ) {
            log.error(sm.getString("http11protocol.socketfactory.initerror"),
                      ex);
            throw ex;
        }

        if( socketFactory!=null ) {
            Enumeration attE=attributes.keys();
            while( attE.hasMoreElements() ) {
                String key=(String)attE.nextElement();
                Object v=attributes.get( key );
                socketFactory.setAttribute( key, v );
            }
        }

        // XXX get domain from registration
        try {
            ep.initEndpoint();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.initerror"), ex);
            throw ex;
        }
        log.info(sm.getString("http11protocol.init", getName()));

    }
    
    ObjectName tpOname;
    ObjectName rgOname;
    
    public void start() throws Exception {
        if( this.domain != null ) {
            try {
                // XXX We should be able to configure it separately
                // XXX It should be possible to use a single TP
                tpOname=new ObjectName
                    (domain + ":" + "type=ThreadPool,name=" + getName());
                Registry.getRegistry(null, null)
                    .registerComponent(tp, tpOname, null );
                tp.setName(getName());
                tp.setDaemon(false);
                tp.addThreadPoolListener(new MXPoolListener(this, tp));
            } catch (Exception e) {
                log.error("Can't register threadpool" );
            }
            rgOname=new ObjectName
                (domain + ":type=GlobalRequestProcessor,name=" + getName());
            Registry.getRegistry(null, null).registerComponent
                ( cHandler.global, rgOname, null );
        }

        try {
            ep.startEndpoint();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.starterror"), ex);
            throw ex;
        }
        log.info(sm.getString("http11protocol.start", getName()));
    }

    public void pause() throws Exception {
        try {
            ep.pauseEndpoint();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.pauseerror"), ex);
            throw ex;
        }
        log.info(sm.getString("http11protocol.pause", getName()));
    }

    public void resume() throws Exception {
        try {
            ep.resumeEndpoint();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.resumeerror"), ex);
            throw ex;
        }
        log.info(sm.getString("http11protocol.resume", getName()));
    }

    public void destroy() throws Exception {
        log.info(sm.getString("http11protocol.stop", getName()));
        ep.stopEndpoint();
        if( tpOname!=null ) 
            Registry.getRegistry(null, null).unregisterComponent(tpOname);
        if( rgOname != null ) 
            Registry.getRegistry(null, null).unregisterComponent(rgOname);
    }
    
    // -------------------- Properties--------------------
    protected ThreadPool tp=ThreadPool.createThreadPool(true);
    protected PoolTcpEndpoint ep=new PoolTcpEndpoint(tp);
    protected boolean secure;
    
    protected ServerSocketFactory socketFactory;
    protected SSLImplementation sslImplementation;
    // socket factory attriubtes ( XXX replace with normal setters ) 
    protected Hashtable attributes = new Hashtable();
    protected String socketFactoryName=null;
    protected String sslImplementationName=null;

    private int maxKeepAliveRequests=100; // as in Apache HTTPD server
    private int timeout = 300000;   // 5 minutes as in Apache HTTPD server
    private int maxPostSize = 2 * 1024 * 1024;
    private int maxHttpHeaderSize = 4 * 1024;
    private String reportedname;
    private int socketCloseDelay=-1;
    private boolean disableUploadTimeout = true;
    private int socketBuffer = 9000;
    private Adapter adapter;
    private Http11ConnectionHandler cHandler;

    /**
     * Compression value.
     */
    private String compression = "off";
    private String noCompressionUserAgents = null;
    private String restrictedUserAgents = null;
    private String compressableMimeTypes = "text/html,text/xml,text/plain";
    private int compressionMinSize    = 2048;
    
    // -------------------- Pool setup --------------------

    public boolean getPools(){
        return ep.isPoolOn();
    }
    
    public void setPools( boolean t ) {
        ep.setPoolOn(t);
        setAttribute("pools", "" + t);
    }

    public int getMaxThreads() {
        return ep.getMaxThreads();
    }
    
    public void setMaxThreads( int maxThreads ) {
        ep.setMaxThreads(maxThreads);
        setAttribute("maxThreads", "" + maxThreads);
    }

    public int getMaxSpareThreads() {
        return ep.getMaxSpareThreads();
    }
    
    public void setMaxSpareThreads( int maxThreads ) {
        ep.setMaxSpareThreads(maxThreads);
        setAttribute("maxSpareThreads", "" + maxThreads);
    }
    
    public int getMinSpareThreads() {
        return ep.getMinSpareThreads();
    }

    public void setMinSpareThreads( int minSpareThreads ) {
        ep.setMinSpareThreads(minSpareThreads);
        setAttribute("minSpareThreads", "" + minSpareThreads);
    }

    public void setThreadPriority(int threadPriority) {
      ep.setThreadPriority(threadPriority);
      setAttribute("threadPriority", "" + threadPriority);
    }

    public int getThreadPriority() {
      return ep.getThreadPriority();
    }

    // -------------------- Tcp setup --------------------

    public int getBacklog() {
        return ep.getBacklog();
    }
    
    public void setBacklog( int i ) {
        ep.setBacklog(i);
        setAttribute("backlog", "" + i);
    }
    
    public int getPort() {
        return ep.getPort();
    }
    
    public void setPort( int port ) {
        ep.setPort(port);
        setAttribute("port", "" + port);
        //this.port=port;
    }

    public InetAddress getAddress() {
        return ep.getAddress();
    }
    
    public void setAddress(InetAddress ia) {
        ep.setAddress( ia );
        setAttribute("address", "" + ia);
    }
    
    public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
            encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }
        return ("http-" + encodedAddr + ep.getPort());
    }
    
    // commenting out for now since it's not doing anything
    //public void setHostName( String name ) {
    // ??? Doesn't seem to be used in existing or prev code
    // vhost=name;
    //}

    public String getSocketFactory() {
        return socketFactoryName;
    }
    
    public void setSocketFactory( String valueS ) {
        socketFactoryName = valueS;
        setAttribute("socketFactory", valueS);
    }
    
    public String getSSLImplementation() {
        return sslImplementationName;
    }
    
    public void setSSLImplementation( String valueS) {
        sslImplementationName = valueS;
        setAttribute("sslImplementation", valueS);
    }
    
    public boolean getTcpNoDelay() {
        return ep.getTcpNoDelay();
    }
    
    public void setTcpNoDelay( boolean b ) {
        ep.setTcpNoDelay( b );
        setAttribute("tcpNoDelay", "" + b);
    }

    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }
    
    public void setDisableUploadTimeout(boolean isDisabled) {
        disableUploadTimeout = isDisabled;
    }

    public int getSocketBuffer() {
        return socketBuffer;
    }
    
    public void setSocketBuffer(int valueI) {
        socketBuffer = valueI;
    }

    public String getCompression() {
        return compression;
    }
    
    public void setCompression(String valueS) {
        compression = valueS;
        setAttribute("compression", valueS);
    }

    public int getMaxPostSize() {
        return maxPostSize;
    }
    
    public void setMaxPostSize(int valueI) {
        maxPostSize = valueI;
        setAttribute("maxPostSize", "" + valueI);
    }

    public int getMaxHttpHeaderSize() {
        return maxHttpHeaderSize;
    }
    
    public void setMaxHttpHeaderSize(int valueI) {
        maxHttpHeaderSize = valueI;
        setAttribute("maxHttpHeaderSize", "" + valueI);
    }

    public String getRestrictedUserAgents() {
        return restrictedUserAgents;
    }
    
    public void setRestrictedUserAgents(String valueS) {
        restrictedUserAgents = valueS;
        setAttribute("restrictedUserAgents", valueS);
    }

    public String getNoCompressionUserAgents() {
        return noCompressionUserAgents;
    }
    
    public void setNoCompressionUserAgents(String valueS) {
        noCompressionUserAgents = valueS;
        setAttribute("noCompressionUserAgents", valueS);
    }

    public String getCompressableMimeType() {
        return compressableMimeTypes;
    }
    
    public void setCompressableMimeType(String valueS) {
        compressableMimeTypes = valueS;
        setAttribute("compressableMimeTypes", valueS);
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }
    
    public void setCompressionMinSize(int valueI) {
        compressionMinSize = valueI;
        setAttribute("compressionMinSize", "" + valueI);
    }

    public int getSoLinger() {
        return ep.getSoLinger();
    }
    
    public void setSoLinger( int i ) {
        ep.setSoLinger( i );
        setAttribute("soLinger", "" + i);
    }

    public int getSoTimeout() {
        return ep.getSoTimeout();
    }
    
    public void setSoTimeout( int i ) {
        ep.setSoTimeout(i);
        setAttribute("soTimeout", "" + i);
    }
    
    public int getServerSoTimeout() {
        return ep.getServerSoTimeout();
    }
    
    public void setServerSoTimeout( int i ) {
        ep.setServerSoTimeout(i);
        setAttribute("serverSoTimeout", "" + i);
    }
    
    public String getKeystore() {
        return getProperty("keystore");
    }
    
    public void setKeystore( String k ) {
        setAttribute("keystore", k);
    }

    public String getKeypass() {
        return getProperty("keypass");
    }
    
    public void setKeypass( String k ) {
        attributes.put("keypass", k);
        //setAttribute("keypass", k);
    }
    
    public String getKeytype() {
        return getProperty("keystoreType");
    }
    
    public void setKeytype( String k ) {
        setAttribute("keystoreType", k);
    }

    public String getClientauth() {
        return getProperty("clientauth");
    }
    
    public void setClientauth( String k ) {
        setAttribute("clientauth", k);
    }

    public String getProtocol() {
        return getProperty("protocol");
    }
    
    public void setProtocol( String k ) {
        setAttribute("protocol", k);
    }

    public String getProtocols() {
        return getProperty("protocols");
    }
    
    public void setProtocols(String k) {
        setAttribute("protocols", k);
    }

    public String getAlgorithm() {
        return getProperty("algorithm");
    }
    
    public void setAlgorithm( String k ) {
        setAttribute("algorithm", k);
    }

    public boolean getSecure() {
        return secure;
    }
    
    public void setSecure( boolean b ) {
        secure=b;
        setAttribute("secure", "" + b);
    }

    public String getCiphers() {
        return getProperty("ciphers");
    }
    
    public void setCiphers(String ciphers) {
        setAttribute("ciphers", ciphers);
    }

    public String getKeyAlias() {
        return getProperty("keyAlias");
    }
    
    public void setKeyAlias(String keyAlias) {
        setAttribute("keyAlias", keyAlias);
    }

    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }
    
    /** Set the maximum number of Keep-Alive requests that we will honor.
     */
    public void setMaxKeepAliveRequests(int mkar) {
        maxKeepAliveRequests = mkar;
        setAttribute("maxKeepAliveRequests", "" + mkar);
    }

    public int getSocketCloseDelay() {
        return socketCloseDelay;
    }
    
    public void setSocketCloseDelay( int d ) {
        socketCloseDelay=d;
        setAttribute("socketCloseDelay", "" + d);
    }

    private static ServerSocketFactory string2SocketFactory( String val)
    throws ClassNotFoundException, IllegalAccessException,
    InstantiationException
    {
        Class chC=Class.forName( val );
        return (ServerSocketFactory)chC.newInstance();
    }

    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout( int timeouts ) {
        timeout = timeouts * 1000;
        setAttribute("timeout", "" + timeouts);
    }

    public String getReportedname() {
        return reportedname;
    }
    
    public void setReportedname( String reportedName) {
        reportedname = reportedName;
    }
    
    // --------------------  Connection handler --------------------
    public static final int THREAD_DATA_PROCESSOR=1;
    public static final int THREAD_DATA_OBJECT_NAME=2;
    
    
    static class MXPoolListener implements ThreadPool.ThreadPoolListener {
        MXPoolListener( Http11Protocol proto, ThreadPool control ) {
            
        }

        public void threadStart(ThreadPool tp, Thread t) {
        }

        public void threadEnd(ThreadPool tp, Thread t) {
            // Register our associated processor
            // TP uses only TWA
            ThreadWithAttributes ta=(ThreadWithAttributes)t;
            Object tpData[]=ta.getThreadData(tp);
            if( tpData==null ) return;
            // Weird artifact - it should be cleaned up, but that may break something
            // and it won't gain us too much
            if( tpData[1] instanceof Object[] ) {
                tpData=(Object [])tpData[1];
            }
            ObjectName oname=(ObjectName)tpData[Http11Protocol.THREAD_DATA_OBJECT_NAME];
            if( oname==null ) return;
            Registry.getRegistry(null, null).unregisterComponent(oname);
            Http11Processor processor = 
                (Http11Processor) tpData[Http11Protocol.THREAD_DATA_PROCESSOR];
            RequestInfo rp=processor.getRequest().getRequestProcessor();
            rp.setGlobalProcessor(null);
        }
    }

    static class Http11ConnectionHandler implements TcpConnectionHandler {
        Http11Protocol proto;
        static int count=0;
        RequestGroupInfo global=new RequestGroupInfo();

        Http11ConnectionHandler( Http11Protocol proto ) {
            this.proto=proto;
        }
        
        public void setAttribute( String name, Object value ) {
        }
        
        public void setServer( Object o ) {
        }
    
        public Object[] init() {
            Object thData[]=new Object[3];
            
            Http11Processor  processor = 
                new Http11Processor(proto.maxHttpHeaderSize);
            processor.setAdapter( proto.adapter );
            processor.setThreadPool( proto.tp );
            processor.setMaxKeepAliveRequests( proto.maxKeepAliveRequests );
            processor.setTimeout( proto.timeout );
            processor.setDisableUploadTimeout( proto.disableUploadTimeout );
            processor.setCompression( proto.compression );
            processor.setCompressionMinSize( proto.compressionMinSize);
            processor.setNoCompressionUserAgents( proto.noCompressionUserAgents);
            processor.setCompressableMimeTypes( proto.compressableMimeTypes);
            processor.setRestrictedUserAgents( proto.restrictedUserAgents);
            processor.setSocketBuffer( proto.socketBuffer );
            processor.setMaxPostSize( proto.maxPostSize );

            thData[Http11Protocol.THREAD_DATA_PROCESSOR]=processor;
            
            if( proto.getDomain() != null ) {
                try {
                    RequestInfo rp=processor.getRequest().getRequestProcessor();
                    rp.setGlobalProcessor(global);
                    ObjectName rpName=new ObjectName
                        (proto.getDomain() + ":type=RequestProcessor,worker=" 
                         + proto.getName() +",name=HttpRequest" + count++ );
                    Registry.getRegistry(null, null).registerComponent( rp, rpName, null);
                    thData[Http11Protocol.THREAD_DATA_OBJECT_NAME]=rpName;
                } catch( Exception ex ) {
                    log.warn("Error registering request");
                }
            }

            return  thData;
        }

        public void processConnection(TcpConnection connection,
                      Object thData[]) {
            Socket socket=null;
            Http11Processor  processor=null;
            try {
                processor=(Http11Processor)thData[Http11Protocol.THREAD_DATA_PROCESSOR];
                
                if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_START, null);
                }
                socket=connection.getSocket();
                
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                if( proto.secure ) {
                    SSLSupport sslSupport=null;
                    if(proto.sslImplementation != null)
                        sslSupport = proto.sslImplementation.getSSLSupport(socket);
                    processor.setSSLSupport(sslSupport);
                } else {
                    processor.setSSLSupport( null );
                }
                processor.setSocket( socket );
                
                processor.process(in, out);
                
                // If unread input arrives after the shutdownInput() call
                // below and before or during the socket.close(), an error
                // may be reported to the client.  To help troubleshoot this
                // type of error, provide a configurable delay to give the
                // unread input time to arrive so it can be successfully read
                // and discarded by shutdownInput().
                if( proto.socketCloseDelay >= 0 ) {
                    try {
                        Thread.sleep(proto.socketCloseDelay);
                    } catch (InterruptedException ie) { /* ignore */ }
                }
                
                TcpConnection.shutdownInput( socket );
            } catch(java.net.SocketException e) {
                // SocketExceptions are normal
                Http11Protocol.log.debug
                    (sm.getString
                     ("http11protocol.proto.socketexception.debug"), e);
            } catch (java.io.IOException e) {
                // IOExceptions are normal 
                Http11Protocol.log.debug
                    (sm.getString
                     ("http11protocol.proto.ioexception.debug"), e);
            }
            // Future developers: if you discover any other
            // rare-but-nonfatal exceptions, catch them here, and log as
            // above.
            catch (Throwable e) {
                // any other exception or error is odd. Here we log it
                // with "ERROR" level, so it will show up even on
                // less-than-verbose logs.
                Http11Protocol.log.error
                    (sm.getString("http11protocol.proto.error"), e);
            } finally {
                //       if(proto.adapter != null) proto.adapter.recycle();
                //                processor.recycle();
                
                if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_STOP, null);
                }
                // recycle kernel sockets ASAP
                try { if (socket != null) socket.close (); }
                catch (IOException e) { /* ignore */ }
            }
        }
    }

    protected static org.apache.commons.logging.Log log 
        = org.apache.commons.logging.LogFactory.getLog(Http11Protocol.class);

    // -------------------- Various implementation classes --------------------

    /** Sanity check and socketFactory setup.
     *  IMHO it is better to stop the show on a broken connector,
     *  then leave Tomcat running and broken.
     *  @exception TomcatException Unable to resolve classes
     */
    private void checkSocketFactory() throws Exception {
        if (secure) {
            try {
                // The SSL setup code has been moved into
                // SSLImplementation since SocketFactory doesn't
                // provide a wide enough interface
                sslImplementation =
                    SSLImplementation.getInstance(sslImplementationName);
                socketFactory = sslImplementation.getServerSocketFactory();
                ep.setServerSocketFactory(socketFactory);
            } catch (ClassNotFoundException e){
                throw e;
            }
        } else if (socketFactoryName != null) {
            try {
                socketFactory = string2SocketFactory(socketFactoryName);
                ep.setServerSocketFactory(socketFactory);
            } catch(Exception sfex) {
                throw sfex;
            }
        }
    }

    /*
    public boolean isKeystoreSet() {
        return (attributes.get("keystore") != null);
    }

    public boolean isKeypassSet() {
        return (attributes.get("keypass") != null);
    }

    public boolean isClientauthSet() {
        return (attributes.get("clientauth") != null);
    }

    public boolean isAttributeSet( String attr ) {
        return (attributes.get(attr) != null);
    }

    public boolean isSecure() {
        return secure;
    }
   
    public PoolTcpEndpoint getEndpoint() {
        return ep;
    }
    */
    
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
}
