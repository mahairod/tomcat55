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


package org.apache.catalina.valves;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.security.cert.CertificateFactory;
import javax.security.cert.X509Certificate;
import javax.servlet.ServletException;
import org.apache.catalina.Globals;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.RequestWrapper;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of a Valve that checks if the socket underlying this
 * request is an SSLSocket or not.  If it is, and if the client has presented
 * a certificate chain to authenticate itself, the array of certificates is
 * exposed as a request attribute.
 * <p>
 * In addition, this Valve exposes the cipher suite and key size being used
 * on this SSL connection as request attributes.  Although this function is
 * unrelated to certificates, the two tasks have been combined here to minimize
 * the amount of code that has to check for the existence of JSSE classes.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class CertificatesValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * A mapping table to determine the number of effective bits in the key
     * when using a cipher suite containing the specified cipher name.  The
     * underlying data came from the TLS Specification (RFC 2246), Appendix C.
     */
    private static final CipherData ciphers[] = {
        new CipherData("_WITH_NULL_", 0),
        new CipherData("_WITH_IDEA_CBC_", 128),
        new CipherData("_WITH_RC2_CBC_40_", 40),
        new CipherData("_WITH_RC4_40_", 40),
        new CipherData("_WITH_RC4_128_", 128),
        new CipherData("_WITH_DES40_CBC_", 40),
        new CipherData("_WITH_DES_CBC_", 56),
        new CipherData("_WITH_3DES_EDE_CBC_", 168)
    };


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
	"org.apache.catalina.valves.CertificatesValve/1.0";


    /**
     * The StringManager for this package.
     */
    protected static StringManager sm =
	StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

	return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Expose the certificates chain if one was included on this request.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
	throws IOException, ServletException {

        // Identify the underlying request if this request was wrapped
        Request actual = request;
        while (actual instanceof RequestWrapper)
            actual = ((RequestWrapper) actual).getWrappedRequest();

        // Expose the certificate chain if appropriate
        expose(request, actual);

        // Invoke the next Valve in our Pipeline
        invokeNext(request, response);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Expose the certificate chain for this request, if there is one.
     *
     * @param request The possibly wrapped Request being processed
     * @param actual The actual underlying Request object
     */
    private void expose(Request request, Request actual) {

	// Ensure that this request came in on an SSLSocket
        if (actual.getSocket() == null)
            return;
        if (!(actual.getSocket() instanceof SSLSocket))
            return;
        SSLSocket socket = (SSLSocket) actual.getSocket();

	// Look up the current SSLSession
        SSLSession session = socket.getSession();
        if (session == null)
            return;

        // Expose the cipher suite and key size
        String cipherSuite = session.getCipherSuite();
        if (cipherSuite != null)
            request.getRequest().setAttribute(Globals.CIPHER_SUITE_ATTR,
                                              cipherSuite);
        Integer keySize = (Integer) session.getValue(Globals.KEY_SIZE_ATTR);
        if (keySize == null) {
            int size = 0;
            for (int i = 0; i < ciphers.length; i++) {
                if (cipherSuite.indexOf(ciphers[i].phrase) >= 0) {
                    size = ciphers[i].keySize;
                    break;
                }
            }
            keySize = new Integer(size);
            session.putValue(Globals.KEY_SIZE_ATTR, keySize);
        }
        request.getRequest().setAttribute(Globals.KEY_SIZE_ATTR,
                                          keySize);

	// If we have cached certificates, return them
	Object cached = session.getValue(Globals.CERTIFICATES_ATTR);
	if (cached != null) {
	    request.getRequest().setAttribute(Globals.CERTIFICATES_ATTR,
	                                      cached);
	    return;
        }

	// Convert JSSE's certificate format to the ones we need
        X509Certificate jsseCerts[] = null;
	java.security.cert.X509Certificate x509Certs[] = null;
        try {
            jsseCerts = session.getPeerCertificateChain();
	    if (jsseCerts == null)
	        jsseCerts = new X509Certificate[0];
	    x509Certs =
              new java.security.cert.X509Certificate[jsseCerts.length];
	    for (int i = 0; i < x509Certs.length; i++) {
		byte buffer[] = jsseCerts[i].getEncoded();
		CertificateFactory cf =
		  CertificateFactory.getInstance("X.509");
		ByteArrayInputStream stream =
		  new ByteArrayInputStream(buffer);
		x509Certs[i] = (java.security.cert.X509Certificate)
		  cf.generateCertificate(stream);
	    }
        } catch (Throwable t) {
            return;
        }

	// Expose these certificates as a request attribute
        if ((x509Certs == null) || (x509Certs.length < 1))
            return;
        session.putValue(Globals.CERTIFICATES_ATTR, x509Certs);
        request.getRequest().setAttribute(Globals.CERTIFICATES_ATTR,
                                          x509Certs);

    }


}


// ------------------------------------------------------------ Private Classes


/**
 * Simple data class that represents the cipher being used, along with the
 * corresponding effective key size.  The specified phrase must appear in the
 * name of the cipher suite to be recognized.
 */

final class CipherData {

    String phrase = null;

    int keySize = 0;

    public CipherData(String phrase, int keySize) {
        this.phrase = phrase;
        this.keySize = keySize;
    }

}
