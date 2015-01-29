/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.net.jsse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.WeakHashMap;

import javax.net.ssl.SSLSession;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SSLSessionManager;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.res.StringManager;

/** JSSESupport

   Concrete implementation class for JSSE
   Support classes.

   This will only work with JDK 1.2 and up since it
   depends on JDK 1.2's certificate support

   @author EKR
   @author Craig R. McClanahan
   Parts cribbed from JSSECertCompat
   Parts cribbed from CertificatesValve
*/

class JSSESupport implements SSLSupport, SSLSessionManager {

    private static final Log log = LogFactory.getLog(JSSESupport.class);

    private static final StringManager sm =
        StringManager.getManager("org.apache.tomcat.util.net.jsse.res");

    private static final Map<SSLSession,Integer> keySizeCache = new WeakHashMap<>();

    protected SSLSession session;


    JSSESupport(SSLSession session) {
        this.session = session;
    }

    @Override
    public String getCipherSuite() throws IOException {
        // Look up the current SSLSession
        if (session == null)
            return null;
        return session.getCipherSuite();
    }

    @Override
    public java.security.cert.X509Certificate[] getPeerCertificateChain() throws IOException {
        // Look up the current SSLSession
        if (session == null)
            return null;

        Certificate [] certs=null;
        try {
            certs = session.getPeerCertificates();
        } catch( Throwable t ) {
            log.debug(sm.getString("jsseSupport.clientCertError"), t);
            return null;
        }
        if( certs==null ) return null;

        java.security.cert.X509Certificate [] x509Certs =
            new java.security.cert.X509Certificate[certs.length];
        for(int i=0; i < certs.length; i++) {
            if (certs[i] instanceof java.security.cert.X509Certificate ) {
                // always currently true with the JSSE 1.1.x
                x509Certs[i] = (java.security.cert.X509Certificate) certs[i];
            } else {
                try {
                    byte [] buffer = certs[i].getEncoded();
                    CertificateFactory cf =
                        CertificateFactory.getInstance("X.509");
                    ByteArrayInputStream stream =
                        new ByteArrayInputStream(buffer);
                    x509Certs[i] = (java.security.cert.X509Certificate)
                            cf.generateCertificate(stream);
                } catch(Exception ex) {
                    log.info(sm.getString(
                            "jseeSupport.certTranslationError", certs[i]), ex);
                    return null;
                }
            }
            if(log.isTraceEnabled())
                log.trace("Cert #" + i + " = " + x509Certs[i]);
        }
        if(x509Certs.length < 1)
            return null;
        return x509Certs;
    }


    /**
     * Copied from <code>org.apache.catalina.valves.CertificateValve</code>
     */
    @Override
    public Integer getKeySize()
        throws IOException {
        // Look up the current SSLSession
        SSLSupport.CipherData c_aux[]=ciphers;
        if (session == null)
            return null;

        Integer keySize = null;
        synchronized(keySizeCache) {
            keySize = keySizeCache.get(session);
        }

        if (keySize == null) {
            int size = 0;
            String cipherSuite = session.getCipherSuite();
            for (int i = 0; i < c_aux.length; i++) {
                if (cipherSuite.indexOf(c_aux[i].phrase) >= 0) {
                    size = c_aux[i].keySize;
                    break;
                }
            }
            keySize = Integer.valueOf(size);
            synchronized(keySizeCache) {
                keySizeCache.put(session, keySize);
            }
        }
        return keySize;
    }

    @Override
    public String getSessionId()
        throws IOException {
        // Look up the current SSLSession
        if (session == null)
            return null;
        // Expose ssl_session (getId)
        byte [] ssl_session = session.getId();
        if ( ssl_session == null)
            return null;
        StringBuilder buf=new StringBuilder();
        for(int x=0; x<ssl_session.length; x++) {
            String digit=Integer.toHexString(ssl_session[x]);
            if (digit.length()<2) buf.append('0');
            if (digit.length()>2) digit=digit.substring(digit.length()-2);
            buf.append(digit);
        }
        return buf.toString();
    }


    /**
     * Invalidate the session this support object is associated with.
     */
    @Override
    public void invalidateSession() {
        session.invalidate();
    }
}

