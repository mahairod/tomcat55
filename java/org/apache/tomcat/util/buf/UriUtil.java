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
package org.apache.tomcat.util.buf;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Utility class for working with URIs and URLs.
 */
public final class UriUtil {

    private static final char[] HEX =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final Pattern PATTERN_EXCLAMATION_MARK = Pattern.compile("!/");
    private static final Pattern PATTERN_CARET = Pattern.compile("\\^/");
    private static final Pattern PATTERN_ASTERISK = Pattern.compile("\\*/");
    private static final Pattern PATTERN_CUSTOM;
    private static final String REPLACE_CUSTOM;

    private static final String WAR_SEPARATOR;

    static {
        String custom = System.getProperty("org.apache.tomcat.util.buf.UriUtil.WAR_SEPARATOR");
        if (custom == null) {
            WAR_SEPARATOR = "*/";
            PATTERN_CUSTOM = null;
            REPLACE_CUSTOM = null;
        } else {
            WAR_SEPARATOR = custom + "/";
            PATTERN_CUSTOM = Pattern.compile(Pattern.quote(WAR_SEPARATOR));
            StringBuffer sb = new StringBuffer(custom.length() * 3);
            // Deliberately use the platform's default encoding
            byte[] ba = custom.getBytes();
            for (int j = 0; j < ba.length; j++) {
                // Converting each byte in the buffer
                byte toEncode = ba[j];
                sb.append('%');
                int low = toEncode & 0x0f;
                int high = (toEncode & 0xf0) >> 4;
                sb.append(HEX[high]);
                sb.append(HEX[low]);
            }
            REPLACE_CUSTOM = sb.toString();
        }
    }


    private UriUtil() {
        // Utility class. Hide default constructor
    }


    /**
     * Determine if the character is allowed in the scheme of a URI.
     * See RFC 2396, Section 3.1
     *
     * @param c The character to test
     *
     * @return {@code true} if a the character is allowed, otherwise {code
     *         @false}
     */
    private static boolean isSchemeChar(char c) {
        return Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.';
    }


    /**
     * Determine if a URI string has a <code>scheme</code> component.
     *
     * @param uri The URI to test
     *
     * @return {@code true} if a scheme is present, otherwise {code @false}
     */
    public static boolean hasScheme(CharSequence uri) {
        int len = uri.length();
        for(int i=0; i < len ; i++) {
            char c = uri.charAt(i);
            if(c == ':') {
                return i > 0;
            } else if(!UriUtil.isSchemeChar(c)) {
                return false;
            }
        }
        return false;
    }


    public static URL buildJarUrl(File jarFile) throws MalformedURLException {
        return buildJarUrl(jarFile, null);
    }


    public static URL buildJarUrl(File jarFile, String entryPath) throws MalformedURLException {
        return buildJarUrl(jarFile.toURI().toString(), entryPath);
    }


    public static URL buildJarUrl(String fileUrlString) throws MalformedURLException {
        return buildJarUrl(fileUrlString, null);
    }


    public static URL buildJarUrl(String fileUrlString, String entryPath) throws MalformedURLException {
        String safeString = makeSafeForJarUrl(fileUrlString);
        StringBuilder sb = new StringBuilder();
        sb.append("jar:");
        sb.append(safeString);
        sb.append("!/");
        if (entryPath != null) {
            sb.append(makeSafeForJarUrl(entryPath));
        }
        return new URL(sb.toString());
    }


    public static URL buildJarSafeUrl(File file) throws MalformedURLException {
        String safe = makeSafeForJarUrl(file.toURI().toString());
        return new URL(safe);
    }


    /*
     * When testing on markt's desktop each iteration was taking ~1420ns when
     * using String.replaceAll().
     *
     * Switching the implementation to use pre-compiled patterns and
     * Pattern.matcher(input).replaceAll(replacement) reduced this by ~10%.
     *
     * Note: Given the very small absolute time of a single iteration, even for
     *       a web application with 1000 JARs this is only going to add ~3ms.
     *       It is therefore unlikely that further optimisation will be
     *       necessary.
     */
    /*
     * Pulled out into a separate method in case we need to handle other unusual
     * sequences in the future.
     */
    private static String makeSafeForJarUrl(String input) {
        // Since "!/" has a special meaning in a JAR URL, make sure that the
        // sequence is properly escaped if present.
        String tmp = PATTERN_EXCLAMATION_MARK.matcher(input).replaceAll("%21/");
        // Tomcat's custom jar:war: URL handling treats */ and ^/ as special
        tmp = PATTERN_CARET.matcher(tmp).replaceAll("%5e/");
        tmp = PATTERN_ASTERISK.matcher(tmp).replaceAll("%2a/");
        if (PATTERN_CUSTOM != null) {
            tmp = PATTERN_CUSTOM.matcher(tmp).replaceAll(REPLACE_CUSTOM);
        }
        return tmp;
    }


    /**
     * Convert a URL of the form <code>war:file:...</code> to
     * <code>jar:file:...</code>.
     *
     * @param warUrl The WAR URL to convert
     *
     * @return The equivalent JAR URL
     *
     * @throws MalformedURLException If the conversion fails
     */
    public static URL warToJar(URL warUrl) throws MalformedURLException {
        // Assumes that the spec is absolute and starts war:file:/...
        String file = warUrl.getFile();
        if (file.contains("*/")) {
            file = file.replaceFirst("\\*/", "!/");
        } else if (file.contains("^/")) {
            file = file.replaceFirst("\\^/", "!/");
        } else if (PATTERN_CUSTOM != null) {
            file = file.replaceFirst(PATTERN_CUSTOM.pattern(), "!/");
        }

        return new URL("jar", warUrl.getHost(), warUrl.getPort(), file);
    }


    public static String getWarSeparator() {
        return WAR_SEPARATOR;
    }
}
