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
package org.apache.coyote.http11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpUpgradeHandler;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Processor;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeProcessorExternal;
import org.apache.coyote.http11.upgrade.UpgradeProcessorInternal;
import org.apache.tomcat.util.buf.StringUtils;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;

public abstract class AbstractHttp11Protocol<S> extends AbstractProtocol<S> {

    protected static final StringManager sm =
            StringManager.getManager(AbstractHttp11Protocol.class);


    public AbstractHttp11Protocol(AbstractEndpoint<S,?> endpoint) {
        super(endpoint);
        setConnectionTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        ConnectionHandler<S> cHandler = new ConnectionHandler<>(this);
        setHandler(cHandler);
        getEndpoint().setHandler(cHandler);
    }


    @Override
    public void init() throws Exception {
        // Upgrade protocols have to be configured first since the endpoint
        // init (triggered via super.init() below) uses this list to configure
        // the list of ALPN protocols to advertise
        for (UpgradeProtocol upgradeProtocol : upgradeProtocols) {
            configureUpgradeProtocol(upgradeProtocol);
        }

        super.init();
    }


    @Override
    protected String getProtocolName() {
        return "Http";
    }


    /**
     * {@inheritDoc}
     * <p>
     * Over-ridden here to make the method visible to nested classes.
     */
    @Override
    protected AbstractEndpoint<S,?> getEndpoint() {
        return super.getEndpoint();
    }


    // ------------------------------------------------ HTTP specific properties
    // ------------------------------------------ managed in the ProtocolHandler

    private int maxSavePostSize = 4 * 1024;
    /**
     * Return the maximum size of the post which will be saved during FORM or
     * CLIENT-CERT authentication.
     *
     * @return The size in bytes
     */
    public int getMaxSavePostSize() { return maxSavePostSize; }
    /**
     * Set the maximum size of a POST which will be buffered during FORM or
     * CLIENT-CERT authentication. When a POST is received where the security
     * constraints require a client certificate, the POST body needs to be
     * buffered while an SSL handshake takes place to obtain the certificate. A
     * similar buffering is required during FDORM auth.
     *
     * @param maxSavePostSize The maximum size POST body to buffer in bytes
     */
    public void setMaxSavePostSize(int maxSavePostSize) {
        this.maxSavePostSize = maxSavePostSize;
    }


    /**
     * Maximum size of the HTTP message header.
     */
    private int maxHttpHeaderSize = 8 * 1024;
    public int getMaxHttpHeaderSize() { return maxHttpHeaderSize; }
    public void setMaxHttpHeaderSize(int valueI) { maxHttpHeaderSize = valueI; }


    private int connectionUploadTimeout = 300000;
    /**
     * Specifies a different (usually longer) connection timeout during data
     * upload. Default is 5 minutes as in Apache HTTPD server.
     *
     * @return The timeout in milliseconds
     */
    public int getConnectionUploadTimeout() { return connectionUploadTimeout; }
    /**
     * Set the upload timeout.
     *
     * @param timeout Upload timeout in milliseconds
     */
    public void setConnectionUploadTimeout(int timeout) {
        connectionUploadTimeout = timeout;
    }


    private boolean disableUploadTimeout = true;
    /**
     * Get the flag that controls upload time-outs. If true, the
     * connectionUploadTimeout will be ignored and the regular socket timeout
     * will be used for the full duration of the connection.
     *
     * @return {@code true} if the separate upload timeout is disabled
     */
    public boolean getDisableUploadTimeout() { return disableUploadTimeout; }
    /**
     * Set the flag to control whether a separate connection timeout is used
     * during upload of a request body.
     *
     * @param isDisabled {@code true} if the separate upload timeout should be
     *                   disabled
     */
    public void setDisableUploadTimeout(boolean isDisabled) {
        disableUploadTimeout = isDisabled;
    }


    private int compressionLevel = 0;
    /**
     * Set compression level.
     *
     * @param compression One of <code>on</code>, <code>force</code>,
     *                    <code>off</code> or the minimum compression size in
     *                    bytes which implies <code>on</code>
     */
    public void setCompression(String compression) {
        if (compression.equals("on")) {
            this.compressionLevel = 1;
        } else if (compression.equals("force")) {
            this.compressionLevel = 2;
        } else if (compression.equals("off")) {
            this.compressionLevel = 0;
        } else {
            try {
                // Try to parse compression as an int, which would give the
                // minimum compression size
                setCompressionMinSize(Integer.parseInt(compression));
                this.compressionLevel = 1;
            } catch (Exception e) {
                this.compressionLevel = 0;
            }
        }
    }


    /**
     * Return compression level.
     *
     * @return The current compression level in string form (off/on/force)
     */
    public String getCompression() {
        switch (compressionLevel) {
        case 0:
            return "off";
        case 1:
            return "on";
        case 2:
            return "force";
        }
        return "off";
    }
    protected int getCompressionLevel() {
        return compressionLevel;
    }


    private Pattern noCompressionUserAgents = null;
    /**
     * Obtain the String form of the regular expression that defines the user
     * agents to not use gzip with.
     *
     * @return The regular expression as a String
     */
    public String getNoCompressionUserAgents() {
        if (noCompressionUserAgents == null) {
            return null;
        } else {
            return noCompressionUserAgents.toString();
        }
    }
    protected Pattern getNoCompressionUserAgentsPattern() {
        return noCompressionUserAgents;
    }
    /**
     * Set no compression user agent pattern. Regular expression as supported
     * by {@link Pattern}. e.g.: <code>gorilla|desesplorer|tigrus</code>.
     *
     * @param noCompressionUserAgents The regular expression for user agent
     *                                strings for which compression should not
     *                                be applied
     */
    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        if (noCompressionUserAgents == null || noCompressionUserAgents.length() == 0) {
            this.noCompressionUserAgents = null;
        } else {
            this.noCompressionUserAgents =
                Pattern.compile(noCompressionUserAgents);
        }
    }


    private String compressibleMimeType = "text/html,text/xml,text/plain,text/css," +
                    "text/javascript,application/javascript,application/json,application/xml";
    private String[] compressibleMimeTypes = null;
    public String getCompressibleMimeType() { return compressibleMimeType; }
    public void setCompressibleMimeType(String valueS) {
        compressibleMimeType = valueS;
        compressibleMimeTypes = null;
    }
    public String[] getCompressibleMimeTypes() {
        String[] result = compressibleMimeTypes;
        if (result != null) {
            return result;
        }
        List<String> values = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(compressibleMimeType, ",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (token.length() > 0) {
                values.add(token);
            }
        }
        result = values.toArray(new String[values.size()]);
        compressibleMimeTypes = result;
        return result;
    }


    private int compressionMinSize = 2048;
    public int getCompressionMinSize() { return compressionMinSize; }
    /**
     * Set Minimum size to trigger compression.
     *
     * @param compressionMinSize The minimum content length required for
     *                           compression in bytes
     */
    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }


    private Pattern restrictedUserAgents = null;
    /**
     * Get the string form of the regular expression that defines the User
     * agents which should be restricted to HTTP/1.0 support.
     *
     * @return The regular expression as a String
     */
    public String getRestrictedUserAgents() {
        if (restrictedUserAgents == null) {
            return null;
        } else {
            return restrictedUserAgents.toString();
        }
    }
    protected Pattern getRestrictedUserAgentsPattern() {
        return restrictedUserAgents;
    }
    /**
     * Set restricted user agent list (which will downgrade the connector
     * to HTTP/1.0 mode). Regular expression as supported by {@link Pattern}.
     *
     * @param restrictedUserAgents The regular expression as supported by
     *                             {@link Pattern} for the user agents e.g.
     *                             "gorilla|desesplorer|tigrus"
     */
    public void setRestrictedUserAgents(String restrictedUserAgents) {
        if (restrictedUserAgents == null || restrictedUserAgents.length() == 0) {
            this.restrictedUserAgents = null;
        } else {
            this.restrictedUserAgents = Pattern.compile(restrictedUserAgents);
        }
    }


    private String server;
    public String getServer() { return server; }
    /**
     * Set the server header name.
     *
     * @param server The new value to use for the server header
     */
    public void setServer(String server) {
        this.server = server;
    }


    private boolean serverRemoveAppProvidedValues = false;
    /**
     * Should application provider values for the HTTP Server header be removed.
     * Note that if {@link #server} is set, any application provided value will
     * be over-ridden.
     *
     * @return {@code true} if application provided values should be removed,
     *         otherwise {@code false}
     */
    public boolean getServerRemoveAppProvidedValues() { return serverRemoveAppProvidedValues; }
    public void setServerRemoveAppProvidedValues(boolean serverRemoveAppProvidedValues) {
        this.serverRemoveAppProvidedValues = serverRemoveAppProvidedValues;
    }


    /**
     * Maximum size of trailing headers in bytes
     */
    private int maxTrailerSize = 8192;
    public int getMaxTrailerSize() { return maxTrailerSize; }
    public void setMaxTrailerSize(int maxTrailerSize) {
        this.maxTrailerSize = maxTrailerSize;
    }


    /**
     * Maximum size of extension information in chunked encoding
     */
    private int maxExtensionSize = 8192;
    public int getMaxExtensionSize() { return maxExtensionSize; }
    public void setMaxExtensionSize(int maxExtensionSize) {
        this.maxExtensionSize = maxExtensionSize;
    }


    /**
     * Maximum amount of request body to swallow.
     */
    private int maxSwallowSize = 2 * 1024 * 1024;
    public int getMaxSwallowSize() { return maxSwallowSize; }
    public void setMaxSwallowSize(int maxSwallowSize) {
        this.maxSwallowSize = maxSwallowSize;
    }


    /**
     * This field indicates if the protocol is treated as if it is secure. This
     * normally means https is being used but can be used to fake https e.g
     * behind a reverse proxy.
     */
    private boolean secure;
    public boolean getSecure() { return secure; }
    public void setSecure(boolean b) {
        secure = b;
    }


    /**
     * The names of headers that are allowed to be sent via a trailer when using
     * chunked encoding. They are stored in lower case.
     */
    private Set<String> allowedTrailerHeaders =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    public void setAllowedTrailerHeaders(String commaSeparatedHeaders) {
        // Jump through some hoops so we don't end up with an empty set while
        // doing updates.
        Set<String> toRemove = new HashSet<>();
        toRemove.addAll(allowedTrailerHeaders);
        if (commaSeparatedHeaders != null) {
            String[] headers = commaSeparatedHeaders.split(",");
            for (String header : headers) {
                String trimmedHeader = header.trim().toLowerCase(Locale.ENGLISH);
                if (toRemove.contains(trimmedHeader)) {
                    toRemove.remove(trimmedHeader);
                } else {
                    allowedTrailerHeaders.add(trimmedHeader);
                }
            }
            allowedTrailerHeaders.removeAll(toRemove);
        }
    }
    protected Set<String> getAllowedTrailerHeadersInternal() {
        return allowedTrailerHeaders;
    }
    public String getAllowedTrailerHeaders() {
        // Chances of a size change between these lines are small enough that a
        // sync is unnecessary.
        List<String> copy = new ArrayList<>(allowedTrailerHeaders.size());
        copy.addAll(allowedTrailerHeaders);
        return StringUtils.join(copy);
    }
    public void addAllowedTrailerHeader(String header) {
        if (header != null) {
            allowedTrailerHeaders.add(header.trim().toLowerCase(Locale.ENGLISH));
        }
    }
    public void removeAllowedTrailerHeader(String header) {
        if (header != null) {
            allowedTrailerHeaders.remove(header.trim().toLowerCase(Locale.ENGLISH));
        }
    }


    /**
     * The upgrade protocol instances configured.
     */
    private final List<UpgradeProtocol> upgradeProtocols = new ArrayList<>();
    @Override
    public void addUpgradeProtocol(UpgradeProtocol upgradeProtocol) {
        upgradeProtocols.add(upgradeProtocol);
    }
    @Override
    public UpgradeProtocol[] findUpgradeProtocols() {
        return upgradeProtocols.toArray(new UpgradeProtocol[0]);
    }


    /**
     * The protocols that are available via internal Tomcat support for access
     * via HTTP upgrade.
     */
    private final Map<String,UpgradeProtocol> httpUpgradeProtocols = new HashMap<>();
    /**
     * The protocols that are available via internal Tomcat support for access
     * via ALPN negotiation.
     */
    private final Map<String,UpgradeProtocol> negotiatedProtocols = new HashMap<>();
    private void configureUpgradeProtocol(UpgradeProtocol upgradeProtocol) {
        // HTTP Upgrade
        String httpUpgradeName = upgradeProtocol.getHttpUpgradeName(getEndpoint().isSSLEnabled());
        boolean httpUpgradeConfigured = false;
        if (httpUpgradeName != null && httpUpgradeName.length() > 0) {
            httpUpgradeProtocols.put(httpUpgradeName, upgradeProtocol);
            httpUpgradeConfigured = true;
            getLog().info(sm.getString("abstractHttp11Protocol.httpUpgradeConfigured",
                    getName(), httpUpgradeName));
        }


        // ALPN
        String alpnName = upgradeProtocol.getAlpnName();
        if (alpnName != null && alpnName.length() > 0) {
            if (getEndpoint().isAlpnSupported()) {
                negotiatedProtocols.put(alpnName, upgradeProtocol);
                getEndpoint().addNegotiatedProtocol(alpnName);
                getLog().info(sm.getString("abstractHttp11Protocol.alpnConfigured",
                        getName(), alpnName));
            } else {
                if (!httpUpgradeConfigured) {
                    // ALPN is not supported by this connector and the upgrade
                    // protocol implementation does not support standard HTTP
                    // upgrade so there is no way available to enable support
                    // for this protocol.
                    getLog().error(sm.getString("abstractHttp11Protocol.alpnWithNoAlpn",
                            upgradeProtocol.getClass().getName(), alpnName, getName()));
                }
            }
        }
    }
    @Override
    public UpgradeProtocol getNegotiatedProtocol(String negotiatedName) {
        return negotiatedProtocols.get(negotiatedName);
    }
    @Override
    public UpgradeProtocol getUpgradeProtocol(String upgradedName) {
        return httpUpgradeProtocols.get(upgradedName);
    }


    // ------------------------------------------------ HTTP specific properties
    // ------------------------------------------ passed through to the EndPoint

    public boolean isSSLEnabled() { return getEndpoint().isSSLEnabled();}
    public void setSSLEnabled(boolean SSLEnabled) {
        getEndpoint().setSSLEnabled(SSLEnabled);
    }


    public boolean getUseSendfile() { return getEndpoint().getUseSendfile(); }
    public void setUseSendfile(boolean useSendfile) { getEndpoint().setUseSendfile(useSendfile); }


    /**
     * @return The maximum number of requests which can be performed over a
     *         keep-alive connection. The default is the same as for Apache HTTP
     *         Server (100).
     */
    public int getMaxKeepAliveRequests() {
        return getEndpoint().getMaxKeepAliveRequests();
    }
    /**
     * Set the maximum number of Keep-Alive requests to allow.
     * This is to safeguard from DoS attacks. Setting to a negative
     * value disables the limit.
     *
     * @param mkar The new maximum number of Keep-Alive requests allowed
     */
    public void setMaxKeepAliveRequests(int mkar) {
        getEndpoint().setMaxKeepAliveRequests(mkar);
    }


    // ----------------------------------------------- HTTPS specific properties
    // ------------------------------------------ passed through to the EndPoint

    public String getDefaultSSLHostConfigName() {
        return getEndpoint().getDefaultSSLHostConfigName();
    }
    public void setDefaultSSLHostConfigName(String defaultSSLHostConfigName) {
        getEndpoint().setDefaultSSLHostConfigName(defaultSSLHostConfigName);
        if (defaultSSLHostConfig != null) {
            defaultSSLHostConfig.setHostName(defaultSSLHostConfigName);
        }
    }


    @Override
    public void addSslHostConfig(SSLHostConfig sslHostConfig) {
        getEndpoint().addSslHostConfig(sslHostConfig);
    }

    @Override
    public SSLHostConfig[] findSslHostConfigs() {
        return getEndpoint().findSslHostConfigs();
    }

    // ----------------------------------------------- HTTPS specific properties
    // -------------------------------------------- Handled via an SSLHostConfig

    private SSLHostConfig defaultSSLHostConfig = null;
    private void registerDefaultSSLHostConfig() {
        if (defaultSSLHostConfig == null) {
            for (SSLHostConfig sslHostConfig : findSslHostConfigs()) {
                if (getDefaultSSLHostConfigName().equals(sslHostConfig.getHostName())) {
                    defaultSSLHostConfig = sslHostConfig;
                    break;
                }
            }
            if (defaultSSLHostConfig == null) {
                defaultSSLHostConfig = new SSLHostConfig();
                defaultSSLHostConfig.setHostName(getDefaultSSLHostConfigName());
                getEndpoint().addSslHostConfig(defaultSSLHostConfig);
            }
        }
    }


    // TODO: All of these SSL getters and setters can be removed once it is no
    // longer necessary to support the old configuration attributes (Tomcat 10?)

    public String getSslEnabledProtocols() {
        registerDefaultSSLHostConfig();
        return StringUtils.join(defaultSSLHostConfig.getEnabledProtocols());
    }
    public void setSslEnabledProtocols(String enabledProtocols) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setProtocols(enabledProtocols);
    }
    public String getSSLProtocol() {
        registerDefaultSSLHostConfig();
        return StringUtils.join(defaultSSLHostConfig.getEnabledProtocols());
    }
    public void setSSLProtocol(String sslProtocol) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setProtocols(sslProtocol);
    }


    public String getKeystoreFile() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeystoreFile();
    }
    public void setKeystoreFile(String keystoreFile) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeystoreFile(keystoreFile);
    }
    public String getSSLCertificateChainFile() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateChainFile();
    }
    public void setSSLCertificateChainFile(String certificateChainFile) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateChainFile(certificateChainFile);
    }
    public String getSSLCertificateFile() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateFile();
    }
    public void setSSLCertificateFile(String certificateFile) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateFile(certificateFile);
    }
    public String getSSLCertificateKeyFile() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeyFile();
    }
    public void setSSLCertificateKeyFile(String certificateKeyFile) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeyFile(certificateKeyFile);
    }


    public String getAlgorithm() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getKeyManagerAlgorithm();
    }
    public void setAlgorithm(String keyManagerAlgorithm) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setKeyManagerAlgorithm(keyManagerAlgorithm);
    }


    public String getClientAuth() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateVerification().toString();
    }
    public void setClientAuth(String certificateVerification) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateVerification(certificateVerification);
    }


    public String getSSLVerifyClient() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateVerification().toString();
    }
    public void setSSLVerifyClient(String certificateVerification) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateVerification(certificateVerification);
    }


    public int getTrustMaxCertLength(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateVerificationDepth();
    }
    public void setTrustMaxCertLength(int certificateVerificationDepth){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateVerificationDepth(certificateVerificationDepth);
    }
    public int getSSLVerifyDepth() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateVerificationDepth();
    }
    public void setSSLVerifyDepth(int certificateVerificationDepth) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateVerificationDepth(certificateVerificationDepth);
    }


    public boolean getUseServerCipherSuitesOrder() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getHonorCipherOrder();
    }
    public void setUseServerCipherSuitesOrder(boolean honorCipherOrder) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setHonorCipherOrder(honorCipherOrder);
    }
    public boolean getSSLHonorCipherOrder() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getHonorCipherOrder();
    }
    public void setSSLHonorCipherOrder(boolean honorCipherOrder) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setHonorCipherOrder(honorCipherOrder);
    }


    public String getCiphers() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCiphers();
    }
    public void setCiphers(String ciphers) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCiphers(ciphers);
    }
    public String getSSLCipherSuite() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCiphers();
    }
    public void setSSLCipherSuite(String ciphers) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCiphers(ciphers);
    }


    public String getKeystorePass() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeystorePassword();
    }
    public void setKeystorePass(String certificateKeystorePassword) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeystorePassword(certificateKeystorePassword);
    }


    public String getKeyPass() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeyPassword();
    }
    public void setKeyPass(String certificateKeyPassword) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeyPassword(certificateKeyPassword);
    }
    public String getSSLPassword() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeyPassword();
    }
    public void setSSLPassword(String certificateKeyPassword) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeyPassword(certificateKeyPassword);
    }


    public String getCrlFile(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateRevocationListFile();
    }
    public void setCrlFile(String certificateRevocationListFile){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateRevocationListFile(certificateRevocationListFile);
    }
    public String getSSLCARevocationFile() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateRevocationListFile();
    }
    public void setSSLCARevocationFile(String certificateRevocationListFile) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateRevocationListFile(certificateRevocationListFile);
    }
    public String getSSLCARevocationPath() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateRevocationListPath();
    }
    public void setSSLCARevocationPath(String certificateRevocationListPath) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateRevocationListPath(certificateRevocationListPath);
    }


    public String getKeystoreType() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeystoreType();
    }
    public void setKeystoreType(String certificateKeystoreType) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeystoreType(certificateKeystoreType);
    }


    public String getKeystoreProvider() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeystoreProvider();
    }
    public void setKeystoreProvider(String certificateKeystoreProvider) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeystoreProvider(certificateKeystoreProvider);
    }


    public String getKeyAlias() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCertificateKeyAlias();
    }
    public void setKeyAlias(String certificateKeyAlias) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCertificateKeyAlias(certificateKeyAlias);
    }


    public String getTruststoreAlgorithm(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getTruststoreAlgorithm();
    }
    public void setTruststoreAlgorithm(String truststoreAlgorithm){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setTruststoreAlgorithm(truststoreAlgorithm);
    }


    public String getTruststoreFile(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getTruststoreFile();
    }
    public void setTruststoreFile(String truststoreFile){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setTruststoreFile(truststoreFile);
    }


    public String getTruststorePass(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getTruststorePassword();
    }
    public void setTruststorePass(String truststorePassword){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setTruststorePassword(truststorePassword);
    }


    public String getTruststoreType(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getTruststoreType();
    }
    public void setTruststoreType(String truststoreType){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setTruststoreType(truststoreType);
    }


    public String getTruststoreProvider(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getTruststoreProvider();
    }
    public void setTruststoreProvider(String truststoreProvider){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setTruststoreProvider(truststoreProvider);
    }


    public String getSslProtocol() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getSslProtocol();
    }
    public void setSslProtocol(String sslProtocol) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setSslProtocol(sslProtocol);
    }


    public int getSessionCacheSize(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getSessionCacheSize();
    }
    public void setSessionCacheSize(int sessionCacheSize){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setSessionCacheSize(sessionCacheSize);
    }


    public int getSessionTimeout(){
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getSessionTimeout();
    }
    public void setSessionTimeout(int sessionTimeout){
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setSessionTimeout(sessionTimeout);
    }


    public String getSSLCACertificatePath() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCaCertificatePath();
    }
    public void setSSLCACertificatePath(String caCertificatePath) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCaCertificatePath(caCertificatePath);
    }


    public String getSSLCACertificateFile() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getCaCertificateFile();
    }
    public void setSSLCACertificateFile(String caCertificateFile) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setCaCertificateFile(caCertificateFile);
    }


    public boolean getSSLDisableCompression() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getDisableCompression();
    }
    public void setSSLDisableCompression(boolean disableCompression) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setDisableCompression(disableCompression);
    }


    public boolean getSSLDisableSessionTickets() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getDisableSessionTickets();
    }
    public void setSSLDisableSessionTickets(boolean disableSessionTickets) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setDisableSessionTickets(disableSessionTickets);
    }


    public String getTrustManagerClassName() {
        registerDefaultSSLHostConfig();
        return defaultSSLHostConfig.getTrustManagerClassName();
    }
    public void setTrustManagerClassName(String trustManagerClassName) {
        registerDefaultSSLHostConfig();
        defaultSSLHostConfig.setTrustManagerClassName(trustManagerClassName);
    }


    // ------------------------------------------------------------- Common code

    @Override
    protected Processor createProcessor() {
        Http11Processor processor = new Http11Processor(this, adapter);
        return processor;
    }


    @Override
    protected Processor createUpgradeProcessor(
            SocketWrapperBase<?> socket,
            UpgradeToken upgradeToken) {
        HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
        if (httpUpgradeHandler instanceof InternalHttpUpgradeHandler) {
            return new UpgradeProcessorInternal(socket, upgradeToken);
        } else {
            return new UpgradeProcessorExternal(socket, upgradeToken);
        }
    }
}
