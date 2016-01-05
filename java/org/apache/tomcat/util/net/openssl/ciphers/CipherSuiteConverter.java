/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.util.net.openssl.ciphers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

/**
 * Converts a Java cipher suite string to an OpenSSL cipher suite string and vice versa.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Cipher_suite">Wikipedia page about cipher suite</a>
 */
public final class CipherSuiteConverter {

    private static final Log logger = LogFactory.getLog(CipherSuiteConverter.class);
    private static final StringManager sm = StringManager.getManager(CipherSuiteConverter.class);

    /**
     * A_B_WITH_C_D, where:
     *
     * A - TLS or SSL (protocol)
     * B - handshake algorithm (key exchange and authentication algorithms to be precise)
     * C - bulk cipher
     * D - HMAC algorithm
     *
     * This regular expression assumes that:
     *
     * 1) A is always TLS or SSL, and
     * 2) D is always a single word.
     */
    private static final Pattern JAVA_CIPHERSUITE_PATTERN =
            Pattern.compile("^(?:TLS|SSL)_((?:(?!_WITH_).)+)_WITH_(.*)_(.*)$");

    /**
     * A-B-C, where:
     *
     * A - handshake algorithm (key exchange and authentication algorithms to be precise)
     * B - bulk cipher
     * C - HMAC algorithm
     *
     * This regular expression assumes that:
     *
     * 1) A has some deterministic pattern as shown below, and
     * 2) C is always a single word
     */
    private static final Pattern OPENSSL_CIPHERSUITE_PATTERN =
            // Be very careful not to break the indentation while editing.
            Pattern.compile(
                    "^(?:(" + // BEGIN handshake algorithm
                        "(?:(?:EXP-)?" +
                            "(?:" +
                                "(?:DH|DHE|EDH|ECDH|ECDHE|SRP)-(?:DSS|RSA|ECDSA|PSK)|" +
                                "(?:ADH|AECDH|KRB5|PSK|SRP)" +
                            ')' +
                        ")|" +
                        "EXP" +
                    ")-)?" +  // END handshake algorithm
                    "(.*)-(.*)$");

    private static final Pattern JAVA_AES_CBC_PATTERN = Pattern.compile("^(AES)_([0-9]+)_CBC$");
    private static final Pattern JAVA_AES_PATTERN = Pattern.compile("^(AES)_([0-9]+)_(.*)$");
    private static final Pattern OPENSSL_AES_CBC_PATTERN = Pattern.compile("^(AES)([0-9]+)$");
    private static final Pattern OPENSSL_AES_PATTERN = Pattern.compile("^(AES)([0-9]+)-(.*)$");

    /**
     * Java-to-OpenSSL cipher suite conversion map
     * Note that the Java cipher suite has the protocol prefix (TLS_, SSL_)
     */
    private static final ConcurrentMap<String, String> j2o = new ConcurrentHashMap<>();

    /**
     * OpenSSL-to-Java cipher suite conversion map.
     * Note that one OpenSSL cipher suite can be converted to more than one Java cipher suites because
     * a Java cipher suite has the protocol name prefix (TLS_, SSL_)
     */
    private static final ConcurrentMap<String, Map<String, String>> o2j = new ConcurrentHashMap<>();

    /**
     * Clears the cache for testing purpose.
     */
    static void clearCache() {
        j2o.clear();
        o2j.clear();
    }

    /**
     * Tests if the specified key-value pair has been cached in Java-to-OpenSSL cache.
     */
    static boolean isJ2OCached(String key, String value) {
        return value.equals(j2o.get(key));
    }

    /**
     * Tests if the specified key-value pair has been cached in OpenSSL-to-Java cache.
     */
    static boolean isO2JCached(String key, String protocol, String value) {
        Map<String, String> p2j = o2j.get(key);
        if (p2j == null) {
            return false;
        } else {
            return value.equals(p2j.get(protocol));
        }
    }

    /**
     * Converts the specified Java cipher suites to the OpenSSL specification.
     *
     * @param javaCipherSuites The JSSE cipher suite names to convert
     *
     * @return A list of colon-separated OpenSSL cipher suite names
     */
    public static String toOpenSsl(Iterable<String> javaCipherSuites) {
        final StringBuilder buf = new StringBuilder();
        for (String c: javaCipherSuites) {
            if (c == null) {
                break;
            }

            String converted = toOpenSsl(c);
            if (converted != null) {
                c = converted;
            }

            buf.append(c);
            buf.append(':');
        }

        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1);
            return buf.toString();
        } else {
            return "";
        }
    }

    /**
     * Converts the specified Java cipher suite to its corresponding OpenSSL cipher suite name.
     *
     * @param javaCipherSuite The JSSE cipher suite name to convert
     *
     * @return {@code null} if the conversion has failed
     */
    public static String toOpenSsl(String javaCipherSuite) {
        String converted = j2o.get(javaCipherSuite);
        if (converted != null) {
            return converted;
        } else {
            return cacheFromJava(javaCipherSuite);
        }
    }

    private static String cacheFromJava(String javaCipherSuite) {
        String openSslCipherSuite = toOpenSslUncached(javaCipherSuite);
        if (openSslCipherSuite == null) {
            return null;
        }

        // Cache the mapping.
        j2o.putIfAbsent(javaCipherSuite, openSslCipherSuite);

        // Cache the reverse mapping after stripping the protocol prefix (TLS_ or SSL_)
        final String javaCipherSuiteSuffix = javaCipherSuite.substring(4);
        Map<String, String> p2j = new HashMap<>(4);
        p2j.put("", javaCipherSuiteSuffix);
        p2j.put("SSL", "SSL_" + javaCipherSuiteSuffix);
        p2j.put("TLS", "TLS_" + javaCipherSuiteSuffix);
        o2j.put(openSslCipherSuite, p2j);

        if (logger.isDebugEnabled()) {
            logger.debug(sm.getString("converter.mapping", javaCipherSuite, openSslCipherSuite));
        }

        return openSslCipherSuite;
    }

    static String toOpenSslUncached(String javaCipherSuite) {
        Matcher m = JAVA_CIPHERSUITE_PATTERN.matcher(javaCipherSuite);
        if (!m.matches()) {
            return null;
        }

        String handshakeAlgo = toOpenSslHandshakeAlgo(m.group(1));
        String bulkCipher = toOpenSslBulkCipher(m.group(2));
        String hmacAlgo = toOpenSslHmacAlgo(m.group(3));
        if (handshakeAlgo.length() == 0) {
            return bulkCipher + '-' + hmacAlgo;
        } else {
            return handshakeAlgo + '-' + bulkCipher + '-' + hmacAlgo;
        }
    }

    private static String toOpenSslHandshakeAlgo(String handshakeAlgo) {
        final boolean export = handshakeAlgo.endsWith("_EXPORT");
        if (export) {
            handshakeAlgo = handshakeAlgo.substring(0, handshakeAlgo.length() - 7);
        }

        if ("RSA".equals(handshakeAlgo)) {
            handshakeAlgo = "";
        } else if (handshakeAlgo.endsWith("_anon")) {
            handshakeAlgo = 'A' + handshakeAlgo.substring(0, handshakeAlgo.length() - 5);
        }

        if (export) {
            if (handshakeAlgo.length() == 0) {
                handshakeAlgo = "EXP";
            } else {
                handshakeAlgo = "EXP-" + handshakeAlgo;
            }
        }

        return handshakeAlgo.replace('_', '-');
    }

    private static String toOpenSslBulkCipher(String bulkCipher) {
        if (bulkCipher.startsWith("AES_")) {
            Matcher m = JAVA_AES_CBC_PATTERN.matcher(bulkCipher);
            if (m.matches()) {
                return m.replaceFirst("$1$2");
            }

            m = JAVA_AES_PATTERN.matcher(bulkCipher);
            if (m.matches()) {
                return m.replaceFirst("$1$2-$3");
            }
        }

        if ("3DES_EDE_CBC".equals(bulkCipher)) {
            return "DES-CBC3";
        }

        if ("RC4_128".equals(bulkCipher) || "RC4_40".equals(bulkCipher)) {
            return "RC4";
        }

        if ("DES40_CBC".equals(bulkCipher) || "DES_CBC_40".equals(bulkCipher)) {
            return "DES-CBC";
        }

        if ("RC2_CBC_40".equals(bulkCipher)) {
            return "RC2-CBC";
        }

        return bulkCipher.replace('_', '-');
    }

    private static String toOpenSslHmacAlgo(String hmacAlgo) {
        // Java and OpenSSL use the same algorithm names for:
        //
        //   * SHA
        //   * SHA256
        //   * MD5
        //
        return hmacAlgo;
    }

    /**
     * Convert from OpenSSL cipher suite name convention to java cipher suite name convention.
     * @param openSslCipherSuite An OpenSSL cipher suite name.
     * @param protocol The cryptographic protocol (i.e. SSL, TLS, ...).
     * @return The translated cipher suite name according to java conventions. This will not be {@code null}.
     */
    public static String toJava(String openSslCipherSuite, String protocol) {
        Map<String, String> p2j = o2j.get(openSslCipherSuite);
        if (p2j == null) {
            p2j = cacheFromOpenSsl(openSslCipherSuite);
        }

        String javaCipherSuite = p2j.get(protocol);
        if (javaCipherSuite == null) {
            javaCipherSuite = protocol + '_' + p2j.get("");
        }

        return javaCipherSuite;
    }

    private static Map<String, String> cacheFromOpenSsl(String openSslCipherSuite) {
        String javaCipherSuiteSuffix = toJavaUncached(openSslCipherSuite);
        if (javaCipherSuiteSuffix == null) {
            return null;
        }

        final String javaCipherSuiteSsl = "SSL_" + javaCipherSuiteSuffix;
        final String javaCipherSuiteTls = "TLS_" + javaCipherSuiteSuffix;

        // Cache the mapping.
        Map<String, String> p2j = new HashMap<>(4);
        p2j.put("", javaCipherSuiteSuffix);
        p2j.put("SSL", javaCipherSuiteSsl);
        p2j.put("TLS", javaCipherSuiteTls);
        Map<String, String> p2jCurrent = o2j.putIfAbsent(openSslCipherSuite, p2j);
        if (p2jCurrent != null) {
            p2j = p2jCurrent;
        }

        // Cache the reverse mapping after adding the protocol prefix (TLS_ or SSL_)
        j2o.putIfAbsent(javaCipherSuiteTls, openSslCipherSuite);
        j2o.putIfAbsent(javaCipherSuiteSsl, openSslCipherSuite);

        if (logger.isDebugEnabled()) {
            logger.debug(sm.getString("converter.mapping", openSslCipherSuite, javaCipherSuiteTls));
            logger.debug(sm.getString("converter.mapping", openSslCipherSuite, javaCipherSuiteSsl));
        }

        return p2j;
    }

    static String toJavaUncached(String openSslCipherSuite) {
        Matcher m = OPENSSL_CIPHERSUITE_PATTERN.matcher(openSslCipherSuite);
        if (!m.matches()) {
            return null;
        }

        String handshakeAlgo = m.group(1);
        final boolean export;
        if (handshakeAlgo == null) {
            handshakeAlgo = "";
            export = false;
        } else if (handshakeAlgo.startsWith("EXP-")) {
            handshakeAlgo = handshakeAlgo.substring(4);
            export = true;
        } else if ("EXP".equals(handshakeAlgo)) {
            handshakeAlgo = "";
            export = true;
        } else {
            export = false;
        }

        handshakeAlgo = toJavaHandshakeAlgo(handshakeAlgo, export);
        String bulkCipher = toJavaBulkCipher(m.group(2), export);
        String hmacAlgo = toJavaHmacAlgo(m.group(3));

        return handshakeAlgo + "_WITH_" + bulkCipher + '_' + hmacAlgo;
    }

    private static String toJavaHandshakeAlgo(String handshakeAlgo, boolean export) {
        if (handshakeAlgo.length() == 0) {
            handshakeAlgo = "RSA";
        } else if ("ADH".equals(handshakeAlgo)) {
            handshakeAlgo = "DH_anon";
        } else if ("AECDH".equals(handshakeAlgo)) {
            handshakeAlgo = "ECDH_anon";
        }

        handshakeAlgo = handshakeAlgo.replace("EDH", "DHE");
        handshakeAlgo = handshakeAlgo.replace('-', '_');
        if (export) {
            return handshakeAlgo + "_EXPORT";
        } else {
            return handshakeAlgo;
        }
    }

    private static String toJavaBulkCipher(String bulkCipher, boolean export) {
        if (bulkCipher.startsWith("AES")) {
            Matcher m = OPENSSL_AES_CBC_PATTERN.matcher(bulkCipher);
            if (m.matches()) {
                return m.replaceFirst("$1_$2_CBC");
            }

            m = OPENSSL_AES_PATTERN.matcher(bulkCipher);
            if (m.matches()) {
                return m.replaceFirst("$1_$2_$3");
            }
        }

        if ("DES-CBC3".equals(bulkCipher)) {
            return "3DES_EDE_CBC";
        }

        if ("RC4".equals(bulkCipher)) {
            if (export) {
                return "RC4_40";
            } else {
                return "RC4_128";
            }
        }

        if ("DES-CBC".equals(bulkCipher)) {
            if (export) {
                return "DES40_CBC";
            } else {
                return "DES_CBC";
            }
        }

        if ("RC2-CBC".equals(bulkCipher)) {
            if (export) {
                return "RC2_CBC_40";
            } else {
                return "RC2_CBC";
            }
        }

        return bulkCipher.replace('-', '_');
    }

    private static String toJavaHmacAlgo(String hmacAlgo) {
        // Java and OpenSSL use the same algorithm names for:
        //
        //   * SHA
        //   * SHA256
        //   * MD5
        //
        return hmacAlgo;
    }

    private CipherSuiteConverter() { }
}
