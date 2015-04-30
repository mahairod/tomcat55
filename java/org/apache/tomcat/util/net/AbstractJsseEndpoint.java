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
package org.apache.tomcat.util.net;

import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509KeyManager;

import org.apache.tomcat.util.net.SSLHostConfig.Type;
import org.apache.tomcat.util.net.jsse.NioX509KeyManager;

public abstract class AbstractJsseEndpoint<S> extends AbstractEndpoint<S> {

    private SSLImplementation sslImplementation = null;

    public SSLImplementation getSslImplementation() {
        return sslImplementation;
    }


    @Override
    protected Type getSslConfigType() {
        return SSLHostConfig.Type.JSSE;
    }


    protected void initialiseSsl() throws Exception {
        if (isSSLEnabled()) {
            sslImplementation = SSLImplementation.getInstance(getSslImplementationName());

            // TODO: Temp code until config refactoring is complete. Remove once
            //       refactoring is complete.
            if (sslHostConfigs.size() == 0) {
                SSLHostConfig defaultSslHostConfig = new SSLHostConfig();
                defaultSslHostConfig.setHostName(SSLHostConfig.DEFAULT_SSL_HOST_NAME);
                sslHostConfigs.put(SSLHostConfig.DEFAULT_SSL_HOST_NAME, defaultSslHostConfig);
            }

            for (SSLHostConfig sslHostConfig : sslHostConfigs.values()) {
                SSLUtil sslUtil = sslImplementation.getSSLUtil(this, sslHostConfig);
                SSLContext sslContext = sslUtil.createSSLContext();
                sslContext.init(wrap(sslUtil.getKeyManagers()),
                        sslUtil.getTrustManagers(), null);

                SSLSessionContext sessionContext =
                    sslContext.getServerSessionContext();
                if (sessionContext != null) {
                    sslUtil.configureSessionContext(sessionContext);
                }
                SSLContextWrapper sslContextWrapper = new SSLContextWrapper(sslContext, sslUtil);
                sslHostConfig.setSslContext(sslContextWrapper);
            }
        }
    }


    protected SSLEngine createSSLEngine(String sniHostName) {
        SSLHostConfig sslHostConfig = getSSLHostConfig(sniHostName);
        SSLContextWrapper sslContextWrapper = (SSLContextWrapper) sslHostConfig.getSslContext();
        if (sslContextWrapper == null) {
            // TODO i18n
            throw new IllegalStateException();
        }

        SSLEngine engine = sslContextWrapper.getSSLContext().createSSLEngine();
        switch (sslHostConfig.getCertificateVerification()) {
        case NONE:
            engine.setNeedClientAuth(false);
            engine.setWantClientAuth(false);
            break;
        case OPTIONAL:
        case OPTIONAL_NO_CA:
            engine.setWantClientAuth(true);
            break;
        case REQUIRED:
            engine.setNeedClientAuth(true);
            break;
        }
        engine.setUseClientMode(false);
        engine.setEnabledCipherSuites(sslContextWrapper.getEnabledCiphers());
        engine.setEnabledProtocols(sslContextWrapper.getEnabledProtocols());

        configureUseServerCipherSuitesOrder(engine);

        return engine;
    }


    @Override
    public void unbind() throws Exception {
        for (SSLHostConfig sslHostConfig : sslHostConfigs.values()) {
            sslHostConfig.setSslContext(null);
        }
    }


    /**
     * Configures SSLEngine to honor cipher suites ordering based upon
     * endpoint configuration.
     */
    private void configureUseServerCipherSuitesOrder(SSLEngine engine) {
        String useServerCipherSuitesOrderStr = this
                .getUseServerCipherSuitesOrder().trim();

        SSLParameters sslParameters = engine.getSSLParameters();
        boolean useServerCipherSuitesOrder =
            ("true".equalsIgnoreCase(useServerCipherSuitesOrderStr)
                || "yes".equalsIgnoreCase(useServerCipherSuitesOrderStr));

        sslParameters.setUseCipherSuitesOrder(useServerCipherSuitesOrder);
        engine.setSSLParameters(sslParameters);
    }


    private KeyManager[] wrap(KeyManager[] managers) {
        if (managers==null) return null;
        KeyManager[] result = new KeyManager[managers.length];
        for (int i=0; i<result.length; i++) {
            if (managers[i] instanceof X509KeyManager && getKeyAlias() != null) {
                String keyAlias = getKeyAlias();
                // JKS keystores always convert the alias name to lower case
                if ("jks".equalsIgnoreCase(getKeystoreType())) {
                    keyAlias = keyAlias.toLowerCase(Locale.ENGLISH);
                }
                result[i] = new NioX509KeyManager((X509KeyManager) managers[i], keyAlias);
            } else {
                result[i] = managers[i];
            }
        }
        return result;
    }


    private static class SSLContextWrapper {

        private final SSLContext sslContext;
        private final String[] enabledCiphers;
        private final String[] enabledProtocols;

        public SSLContextWrapper(SSLContext sslContext, SSLUtil sslUtil) {
            this.sslContext = sslContext;
            // Determine which cipher suites and protocols to enable
            enabledCiphers = sslUtil.getEnableableCiphers(sslContext);
            enabledProtocols = sslUtil.getEnableableProtocols(sslContext);
        }

        public SSLContext getSSLContext() { return sslContext;}
        public String[] getEnabledCiphers() { return enabledCiphers; }
        public String[] getEnabledProtocols() { return enabledProtocols; }
    }
}
