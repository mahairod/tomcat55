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
package org.apache.catalina.connector;

import java.net.URI;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestResponsePerformance {
    @Test
    public void testToAbsolutePerformance() throws Exception {
        Request req = new TesterToAbsoluteRequest();
        Response resp = new Response();
        resp.setRequest(req);
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            resp.toAbsolute("bar.html");
        }
        long homebrew = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            URI base = URI.create("http://localhost:8080/foo.html");
            base.resolve(URI.create("bar.html")).toASCIIString();
        }
        long uri = System.currentTimeMillis() - start;
        
        System.out.println("Current 'home-brew': " + homebrew +
                "ms, Using URI: " + uri + "ms");
        assertTrue(homebrew < uri);
    }

    
    private static class TesterToAbsoluteRequest extends Request {

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String getServerName() {
            return "localhost";
        }

        @Override
        public int getServerPort() {
            return 8080;
        }

        @Override
        public String getDecodedRequestURI() {
            return "/foo.html";
        }
    }
}
