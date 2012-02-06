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
package org.apache.catalina.authenticator;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.filters.TesterResponse;
import org.apache.catalina.startup.TestTomcat.MapRealm;
import org.apache.catalina.util.MD5Encoder;

public class TesterDigestAuthenticatorPerformance {

    private static String USER = "user";
    private static String PWD = "pwd";
    private static String ROLE = "role";
    private static String METHOD = "GET";
    private static String URI = "/protected";
    private static String CONTEXT_PATH = "/foo";
    private static String CLIENT_AUTH_HEADER = "authorization";
    private static String REALM = "TestRealm";
    private static String QOP = "auth";

    private DigestAuthenticator authenticator = new DigestAuthenticator();


    @Test
    public void testSimple() throws Exception {
        doTest(100, 1000000);
    }

    public void doTest(int threadCount, int requestCount) throws Exception {

        TesterRunnable runnables[] = new TesterRunnable[threadCount];
        Thread threads[] = new Thread[threadCount];

        // Create the runnables & threads
        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new TesterRunnable(requestCount);
            threads[i] = new Thread(runnables[i]);
        }

        long start = System.currentTimeMillis();

        // Start the threads
        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
        }

        // Wait for the threads to finish
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        double wallTime = System.currentTimeMillis() - start;

        // Gather the results...
        double totalTime = 0;
        int totalSuccess = 0;
        for (int i = 0; i < threadCount; i++) {
            System.out.println("Thread: " + i + " Success: " +
                    runnables[i].getSuccess());
            totalSuccess = totalSuccess + runnables[i].getSuccess();
            totalTime = totalTime + runnables[i].getTime();
        }

        System.out.println("Average time per request (user): " +
                totalTime/(threadCount * requestCount));
        System.out.println("Average time per request (wall): " +
                wallTime/(threadCount * requestCount));

        assertEquals(requestCount * threadCount, totalSuccess);
    }

    @Before
    public void setUp() throws Exception {

        // Configure the Realm
        MapRealm realm = new MapRealm();
        realm.addUser(USER, PWD);
        realm.addUserRole(USER, ROLE);

        // Add the Realm to the Context
        Context context = new StandardContext();
        context.setName(CONTEXT_PATH);
        context.setRealm(realm);

        // Configure the Login config
        LoginConfig config = new LoginConfig();
        config.setRealmName(REALM);
        context.setLoginConfig(config);

        // Make the Context and Realm visible to the Authenticator
        authenticator.setContainer(context);

        // Prevent caching of cnonces so we can the same one for all requests
        authenticator.setCnonceCacheSize(0);
        authenticator.start();
    }


    private class TesterRunnable implements Runnable {

        // Number of valid requests required
        private int requestCount;

        private int success = 0;
        private long time = 0;

        private TesterDigestRequest request;
        private HttpServletResponse response;

        // All init code should be in here. run() needs to be quick
        public TesterRunnable(int requestCount) throws Exception {
            this.requestCount = requestCount;

            request = new TesterDigestRequest();
            String nonce = authenticator.generateNonce(request);
            request.setAuthHeader(buildDigestResponse(nonce));
            request.setContext(authenticator.context);

            response = new TesterResponse();
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            for (int i = 0; i < requestCount; i++) {
                try {
                    if (authenticator.authenticate(request, response)) {
                        success++;
                    }
                } catch (IOException ioe) {
                    // Ignore
                }
            }
            time = System.currentTimeMillis() - start;
        }

        public int getSuccess() {
            return success;
        }

        public long getTime() {
            return time;
        }

        private String buildDigestResponse(String nonce)
                throws NoSuchAlgorithmException {

            String ncString = "00000001";
            String cnonce = "cnonce";

            String a1 = USER + ":" + REALM + ":" + PWD;
            String a2 = METHOD + ":" + CONTEXT_PATH + URI;

            MessageDigest digester = MessageDigest.getInstance("MD5");
            MD5Encoder encoder = new MD5Encoder();

            String md5a1 = encoder.encode(digester.digest(a1.getBytes()));
            String md5a2 = encoder.encode(digester.digest(a2.getBytes()));

            String response = md5a1 + ":" + nonce + ":" + ncString + ":" +
                    cnonce + ":" + QOP + ":" + md5a2;

            String md5response =
                encoder.encode(digester.digest(response.getBytes()));

            StringBuilder auth = new StringBuilder();
            auth.append("Digest username=\"");
            auth.append(USER);
            auth.append("\", realm=\"");
            auth.append(REALM);
            auth.append("\", nonce=\"");
            auth.append(nonce);
            auth.append("\", uri=\"");
            auth.append(CONTEXT_PATH + URI);
            auth.append("\", opaque=\"");
            auth.append(authenticator.getOpaque());
            auth.append("\", response=\"");
            auth.append(md5response);
            auth.append("\"");
            auth.append(", qop=\"");
            auth.append(QOP);
            auth.append("\"");
            auth.append(", nc=\"");
            auth.append(ncString);
            auth.append("\"");
            auth.append(", cnonce=\"");
            auth.append(cnonce);
            auth.append("\"");

            return auth.toString();
        }
    }


    private static class TesterDigestRequest extends Request {

        private String authHeader = null;

        @Override
        public String getRemoteAddr() {
            return "127.0.0.1";
        }

        public void setAuthHeader(String authHeader) {
            this.authHeader = authHeader;
        }

        @Override
        public String getHeader(String name) {
            if (CLIENT_AUTH_HEADER.equalsIgnoreCase(name)) {
                return authHeader;
            } else {
                return super.getHeader(name);
            }
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return CONTEXT_PATH + URI;
        }

    }
}
