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
package org.apache.coyote.http2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Section 3.2 of
 * <a href="https://tools.ietf.org/html/rfc7540">RFC 7540</a>.
 * <br>
 * The order of tests in this class is aligned with the order of the
 * requirements in the RFC.
 */
public class TestHttp2Section_3_2 extends Http2TestBase {

    // Note: Tests for zero/multiple HTTP2-Settings fields can be found in
    //       TestHttp2Section_3_2_1

    // TODO: Test initial requests with bodies of various sizes

    @Test
    public void testConnectionNoHttp2Support() throws Exception {
        configureAndStartWebApplication();
        openClientConnection();
        doHttpUpgrade("h2c", false);
        parseHttp11Response();
    }


    @Test
    public void testConnectionUpgradeWrongProtocol() throws Exception {
        enableHttp2();
        configureAndStartWebApplication();
        openClientConnection();
        doHttpUpgrade("h2", false);
        parseHttp11Response();
    }


    @Test(timeout=10000)
    public void testConnectionNoPreface() throws Exception {
        setupAsFarAsUpgrade();

        // If we don't send the preface the server should kill the connection.
        try {
            // Make the parser read something.
            parser.readFrame(true);
        } catch (IOException ioe) {
            // Expected because the server is going to drop the connection.
        }
    }


    @Test(timeout=10000)
    public void testConnectionIncompletePrefaceStart() throws Exception {
        setupAsFarAsUpgrade();

        // If we send an incomplete preface the server should kill the
        // connection.
        os.write("PRI * HTTP/2.0\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        os.flush();
        try {
            // Make the parser read something.
            parser.readFrame(true);
        } catch (IOException ioe) {
            // Expected because the server is going to drop the connection.
        }
    }


    @Test(timeout=10000)
    public void testConnectionInvalidPrefaceStart() throws Exception {
        setupAsFarAsUpgrade();

        // If we send an incomplete preface the server should kill the
        // connection.
        os.write("xxxxxxxxx-xxxxxxxxx-xxxxxxxxx-xxxxxxxxxx".getBytes(
                StandardCharsets.ISO_8859_1));
        os.flush();
        try {
            // Make the parser read something.
            parser.readFrame(true);
        } catch (IOException ioe) {
            // Expected because the server is going to drop the connection.
        }
    }


    // TODO: Test if server sends settings frame

    // TODO: Test if client doesn't send SETTINGS as part of the preface

    // TODO: Test response is received on stream 1


    private void setupAsFarAsUpgrade() throws Exception {
        enableHttp2();
        configureAndStartWebApplication();
        openClientConnection();
        doHttpUpgrade("h2c", true);
    }


    private void parseHttp11Response() throws IOException {
        String[] responseHeaders = readHttpResponseHeaders();
        Assert.assertTrue(responseHeaders[0], responseHeaders[0].startsWith("HTTP/1.1 200"));

        // Find the content length (chunked responses not handled)
        for (int i = 1; i < responseHeaders.length; i++) {
            if (responseHeaders[i].toLowerCase(Locale.ENGLISH).startsWith("content-length")) {
                String cl = responseHeaders[i];
                int pos = cl.indexOf(':');
                if (pos == -1) {
                    throw new IOException("Invalid: [" + cl + "]");
                }
                int len = Integer.parseInt(cl.substring(pos + 1).trim());
                byte[] content = new byte[len];
                input.fill(content, true);
                return;
            }
        }
        throw new IOException("No content-length");
    }
}
