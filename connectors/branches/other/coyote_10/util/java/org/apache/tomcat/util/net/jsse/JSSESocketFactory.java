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
package org.apache.tomcat.util.net.jsse;

import java.io.*;
import java.net.*;

import java.security.KeyStore;

import java.security.Security;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;

/*
  1. Make the JSSE's jars available, either as an installed
     extension (copy them into jre/lib/ext) or by adding
     them to the Tomcat classpath.
  2. keytool -genkey -alias tomcat -keyalg RSA
     Use "changeit" as password ( this is the default we use )
 */

/**
 * SSL server socket factory. It _requires_ a valid RSA key and
 * JSSE. 
 *
 * @author Harish Prabandham
 * @author Costin Manolache
 * @author Stefan Freyr Stefansson
 * @author EKR -- renamed to JSSESocketFactory
 */
public abstract class JSSESocketFactory
    extends org.apache.tomcat.util.net.ServerSocketFactory
{
    String keystoreType;

    static String defaultKeystoreType = "JKS";
    static String defaultProtocol = "TLS";
    static String defaultAlgorithm = "SunX509";
    static boolean defaultClientAuth = false;

    boolean clientAuth = false;
    SSLServerSocketFactory sslProxy = null;
    
    // defaults
    static String defaultKeystoreFile=System.getProperty("user.home") +
	"/.keystore";
    static String defaultKeyPass="changeit";

    
    public JSSESocketFactory () {
    }

    public ServerSocket createSocket (int port)
	throws IOException
    {
	if( sslProxy == null ) initProxy();
	ServerSocket socket = 
	    sslProxy.createServerSocket(port);
	initServerSocket(socket);
	return socket;
    }
    
    public ServerSocket createSocket (int port, int backlog)
	throws IOException
    {
	if( sslProxy == null ) initProxy();
	ServerSocket socket = 
	    sslProxy.createServerSocket(port, backlog);
	initServerSocket(socket);
	return socket;
    }
    
    public ServerSocket createSocket (int port, int backlog,
				      InetAddress ifAddress)
	throws IOException
    {	
	if( sslProxy == null ) initProxy();
	ServerSocket socket = 
	    sslProxy.createServerSocket(port, backlog, ifAddress);
	initServerSocket(socket);
	return socket;
    }
    
    
    // -------------------- Internal methods
    /** Read the keystore, init the SSL socket factory
     */
    abstract void initProxy() throws IOException;

    public Socket acceptSocket(ServerSocket socket)
	throws IOException
    {
	SSLSocket asock = null;
	try {
	     asock = (SSLSocket)socket.accept();
	     asock.setNeedClientAuth(clientAuth);
	} catch (SSLException e){
	  throw new SocketException("SSL handshake error" + e.toString());
	}
	return asock;
    }
     
    /** Set server socket properties ( accepted cipher suites, etc)
     */
    void initServerSocket(ServerSocket ssocket) {
	SSLServerSocket socket=(SSLServerSocket)ssocket;

	// We enable all cipher suites when the socket is
	// connected - XXX make this configurable 
	String cipherSuites[] = socket.getSupportedCipherSuites();
	socket.setEnabledCipherSuites(cipherSuites);

	// we don't know if client auth is needed -
	// after parsing the request we may re-handshake
	socket.setNeedClientAuth(clientAuth);
    }

    KeyStore initKeyStore( String keystoreFile,
				   String keyPass)
	throws IOException
    {
	InputStream istream = null;
	try {
	    KeyStore kstore=KeyStore.getInstance( keystoreType );
	    istream = new FileInputStream(keystoreFile);
	    kstore.load(istream, keyPass.toCharArray());
	    return kstore;
	}
	catch (FileNotFoundException fnfe) {
	    throw fnfe;
	}
	catch (IOException ioe) {
	    throw ioe;	    
	}
	catch(Exception ex) {
	    ex.printStackTrace();
	    throw new IOException( "Exception trying to load keystore " +
				   keystoreFile + ": " + ex.getMessage() );
	}
    }

    public void handshake(Socket sock)
	 throws IOException
    {
	((SSLSocket)sock).startHandshake();
    }
}
