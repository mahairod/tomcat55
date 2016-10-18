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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Section 8.1 of
 * <a href="https://tools.ietf.org/html/rfc7540">RFC 7540</a>.
 * <br>
 * The order of tests in this class is aligned with the order of the
 * examples in the RFC.
 */
public class TestHttp2Section_8_1 extends Http2TestBase {

    @Test
    public void testSendAck() throws Exception {
        http2Connect();

        byte[] headersFrameHeader = new byte[9];
        ByteBuffer headersPayload = ByteBuffer.allocate(128);
        byte[] dataFrameHeader = new byte[9];
        ByteBuffer dataPayload = ByteBuffer.allocate(256);

        buildPostRequest(headersFrameHeader, headersPayload, true,
                dataFrameHeader, dataPayload, null, 3);

        // Write the headers
        writeFrame(headersFrameHeader, headersPayload);

        parser.readFrame(true);

        Assert.assertEquals("3-HeadersStart\n" +
                "3-Header-[:status]-[100]\n" +
                "3-Header-[date]-["+ DEFAULT_DATE + "]\n" +
                "3-HeadersEnd\n",
                output.getTrace());
        output.clearTrace();

        // Write the body
        writeFrame(dataFrameHeader, dataPayload);

        parser.readFrame(true);
        parser.readFrame(true);
        parser.readFrame(true);
        parser.readFrame(true);

        Assert.assertEquals("0-WindowSize-[256]\n" +
                "3-WindowSize-[256]\n" +
                "3-HeadersStart\n" +
                "3-Header-[:status]-[200]\n" +
                "3-Header-[date]-["+ DEFAULT_DATE + "]\n" +
                "3-HeadersEnd\n" +
                "3-Body-256\n" +
                "3-EndOfStream\n",
                output.getTrace());
    }


    @Test
    public void testUndefinedPseudoHeader() throws Exception {
        List<Header> headers = new ArrayList<>(3);
        headers.add(new Header(":method", "GET"));
        headers.add(new Header(":path", "/simple"));
        headers.add(new Header(":authority", "localhost:" + getPort()));
        headers.add(new Header(":foo", "bar"));

        doInvalidPseudoHeaderTest(headers);
    }


    @Test
    public void testInvalidPseudoHeader() throws Exception {
        List<Header> headers = new ArrayList<>(3);
        headers.add(new Header(":method", "GET"));
        headers.add(new Header(":path", "/simple"));
        headers.add(new Header(":authority", "localhost:" + getPort()));
        headers.add(new Header(":status", "200"));

        doInvalidPseudoHeaderTest(headers);
    }


    @Test
    public void testPseudoHeaderOrder() throws Exception {
        // Need to do this in two frames because HPACK encoder automatically
        // re-orders fields

        http2Connect();

        List<Header> headers = new ArrayList<>(3);
        headers.add(new Header(":method", "GET"));
        headers.add(new Header(":path", "/simple"));
        headers.add(new Header("x-test", "test"));

        byte[] headersFrameHeader = new byte[9];
        ByteBuffer headersPayload = ByteBuffer.allocate(128);

        buildSimpleGetRequestPart1(headersFrameHeader, headersPayload, headers , 3);

        writeFrame(headersFrameHeader, headersPayload);

        headers.clear();
        headers.add(new Header(":authority", "localhost:" + getPort()));
        headersPayload.clear();

        buildSimpleGetRequestPart2(headersFrameHeader, headersPayload, headers , 3);

        writeFrame(headersFrameHeader, headersPayload);


        parser.readFrame(true);

        Assert.assertEquals("3-RST-[1]\n", output.getTrace());
    }


    private void doInvalidPseudoHeaderTest(List<Header> headers) throws Exception {
        http2Connect();

        byte[] headersFrameHeader = new byte[9];
        ByteBuffer headersPayload = ByteBuffer.allocate(128);

        buildGetRequest(headersFrameHeader, headersPayload, null, headers , 3);

        // Write the headers
        writeFrame(headersFrameHeader, headersPayload);

        parser.readFrame(true);

        Assert.assertEquals("3-RST-[1]\n", output.getTrace());
    }
}
