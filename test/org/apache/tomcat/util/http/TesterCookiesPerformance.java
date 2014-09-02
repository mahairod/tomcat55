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
package org.apache.tomcat.util.http;

import org.junit.Assert;
import org.junit.Test;

import org.apache.tomcat.util.buf.MessageBytes;

public class TesterCookiesPerformance {

    @Test
    public void testPerformance01() throws Exception {
        final int cookieCount = 100;
        final int parsingLoops = 200000;

        MimeHeaders mimeHeaders = new MimeHeaders();

        StringBuilder cookieHeader = new StringBuilder();
        // Create cookies
        for (int i = 0; i < cookieCount; i++) {
            cookieHeader.append("name");
            cookieHeader.append(i);
            cookieHeader.append('=');
            cookieHeader.append("value");
            cookieHeader.append(i);
            cookieHeader.append(';');
        }

        byte[] cookieHeaderBytes = cookieHeader.toString().getBytes("UTF-8");

        MessageBytes headerValue = mimeHeaders.addValue("Cookie");
        headerValue.setBytes(cookieHeaderBytes, 0, cookieHeaderBytes.length);

        Cookies cookies = new Cookies(mimeHeaders);
        // warm up
        for (int i = 0; i < parsingLoops; i++) {
            Assert.assertEquals(cookieCount, cookies.getCookieCount());
            cookies.recycle();
        }

        long oldStart = System.nanoTime();
        for (int i = 0; i < parsingLoops; i++) {
            cookies.setUseRfc6265(false);
            Assert.assertEquals(cookieCount, cookies.getCookieCount());
            cookies.recycle();
        }
        long oldDuration = System.nanoTime() - oldStart;

        long newStart = System.nanoTime();
        for (int i = 0; i < parsingLoops; i++) {
            cookies.setUseRfc6265(true);
            Assert.assertEquals(cookieCount, cookies.getCookieCount());
            cookies.recycle();
        }
        long newDuration = System.nanoTime() - newStart;

        System.out.println("Old duration: " + oldDuration);
        System.out.println("New duration: " + newDuration);
    }
}
