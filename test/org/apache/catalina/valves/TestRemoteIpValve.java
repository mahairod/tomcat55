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

package org.apache.catalina.valves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * {@link RemoteIpValve} Tests
 */
public class TestRemoteIpValve extends TestCase {
    
    static class RemoteAddrAndHostTrackerValve extends ValveBase {
        private String remoteAddr;
        private String remoteHost;
        private String scheme;
        private boolean secure;
        private int serverPort;
        
        public String getRemoteAddr() {
            return remoteAddr;
        }
        
        public String getRemoteHost() {
            return remoteHost;
        }
        
        public String getScheme() {
            return scheme;
        }

        public int getServerPort() {
            return serverPort;
        }

        public boolean isSecure() {
            return secure;
        }
        
        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            this.remoteHost = request.getRemoteHost();
            this.remoteAddr = request.getRemoteAddr();
            this.scheme = request.getScheme();
            this.secure = request.isSecure();
            this.serverPort = request.getServerPort();
        }
    }
    
    public void testCommaDelimitedListToStringArray() {
        List<String> elements = Arrays.asList("element1", "element2", "element3");
        String actual = RemoteIpValve.listToCommaDelimitedString(elements);
        assertEquals("element1, element2, element3", actual);
    }
    
    public void testCommaDelimitedListToStringArrayEmptyList() {
        List<String> elements = new ArrayList<String>();
        String actual = RemoteIpValve.listToCommaDelimitedString(elements);
        assertEquals("", actual);
    }
    
    public void testCommaDelimitedListToStringArrayNullList() {
        String actual = RemoteIpValve.listToCommaDelimitedString(null);
        assertEquals("", actual);
    }
    
    public void testInvokeAllowedRemoteAddrWithNullRemoteIpHeader() throws Exception {
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        remoteIpValve.setTrustedProxies("proxy1, proxy2, proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("x-forwarded-by must be null", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "192.168.0.10", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "remote-host-original-value", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "remote-host-original-value", actualPostInvokeRemoteHost);
        
    }
    
    public void testInvokeAllProxiesAreTrusted() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("192\\.168\\.0\\.10|192\\.168\\.0\\.11");
        remoteIpValve.setTrustedProxies("proxy1|proxy2|proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130, proxy1, proxy2");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("all proxies are trusted, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertEquals("all proxies are trusted, they must appear in x-forwarded-by", "proxy1, proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "remote-host-original-value", actualPostInvokeRemoteHost);
    }
    
    public void testInvokeAllProxiesAreTrustedOrInternal() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("192\\.168\\.0\\.10|192\\.168\\.0\\.11");
        remoteIpValve.setTrustedProxies("proxy1|proxy2|proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for")
            .setString("140.211.11.130, proxy1, proxy2, 192.168.0.10, 192.168.0.11");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("all proxies are trusted, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertEquals("all proxies are trusted, they must appear in x-forwarded-by", "proxy1, proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "remote-host-original-value", actualPostInvokeRemoteHost);
    }
    
    public void testInvokeAllProxiesAreInternal() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("192\\.168\\.0\\.10|192\\.168\\.0\\.11");
        remoteIpValve.setTrustedProxies("proxy1|proxy2|proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130, 192.168.0.10, 192.168.0.11");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("all proxies are internal, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("all proxies are internal, x-forwarded-by must be null", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "remote-host-original-value", actualPostInvokeRemoteHost);
    }
    
    public void testInvokeAllProxiesAreTrustedAndRemoteAddrMatchRegexp() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("127\\.0\\.0\\.1|192\\.168\\..*|another-internal-proxy");
        remoteIpValve.setTrustedProxies("proxy1|proxy2|proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("proxy1");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("proxy2");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("all proxies are trusted, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertEquals("all proxies are trusted, they must appear in x-forwarded-by", "proxy1, proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "remote-host-original-value", actualPostInvokeRemoteHost);
    }
    
    public void testInvokeXforwardedProtoSaysHttpsForIncomingHttpRequest() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProtocolHeader("x-forwarded-proto");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        // client ip
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("192.168.0.10");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130");
        // protocol
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-proto").setString("https");
        request.setSecure(false);
        request.setServerPort(8080);
        request.getCoyoteRequest().scheme().setString("http");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        // client ip
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("no intermediate non-trusted proxy, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("no intermediate trusted proxy", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteHost);
        
        // protocol
        String actualScheme = remoteAddrAndHostTrackerValve.getScheme();
        assertEquals("x-forwarded-proto says https", "https", actualScheme);
        
        int actualServerPort = remoteAddrAndHostTrackerValve.getServerPort();
        assertEquals("x-forwarded-proto says https", 443, actualServerPort);
        
        boolean actualSecure = remoteAddrAndHostTrackerValve.isSecure();
        assertEquals("x-forwarded-proto says https", true, actualSecure);

        boolean actualPostInvokeSecure = request.isSecure();
        assertEquals("postInvoke secure", false, actualPostInvokeSecure);

        int actualPostInvokeServerPort = request.getServerPort();
        assertEquals("postInvoke serverPort", 8080, actualPostInvokeServerPort);

        String actualPostInvokeScheme = request.getScheme();
        assertEquals("postInvoke scheme", "http", actualPostInvokeScheme);
    }
    
    public void testInvokeXforwardedProtoIsNullForIncomingHttpRequest() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProtocolHeader("x-forwarded-proto");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        // client ip
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("192.168.0.10");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130");
        // protocol
        // null "x-forwarded-proto"
        request.setSecure(false);
        request.setServerPort(8080);
        request.getCoyoteRequest().scheme().setString("http");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        // client ip
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("no intermediate non-trusted proxy, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("no intermediate trusted proxy", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteHost);
        
        // protocol
        String actualScheme = remoteAddrAndHostTrackerValve.getScheme();
        assertEquals("x-forwarded-proto is null", "http", actualScheme);
        
        int actualServerPort = remoteAddrAndHostTrackerValve.getServerPort();
        assertEquals("x-forwarded-proto is null", 8080, actualServerPort);
        
        boolean actualSecure = remoteAddrAndHostTrackerValve.isSecure();
        assertEquals("x-forwarded-proto is null", false, actualSecure);

        boolean actualPostInvokeSecure = request.isSecure();
        assertEquals("postInvoke secure", false, actualPostInvokeSecure);

        int actualPostInvokeServerPort = request.getServerPort();
        assertEquals("postInvoke serverPort", 8080, actualPostInvokeServerPort);

        String actualPostInvokeScheme = request.getScheme();
        assertEquals("postInvoke scheme", "http", actualPostInvokeScheme);
    }
    
    public void testInvokeXforwardedProtoSaysHttpForIncomingHttpsRequest() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProtocolHeader("x-forwarded-proto");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        // client ip
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("192.168.0.10");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130");
        // protocol
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-proto").setString("http");
        request.setSecure(true);
        request.setServerPort(8443);
        request.getCoyoteRequest().scheme().setString("https");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        // client ip
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("no intermediate non-trusted proxy, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("no intermediate trusted proxy", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteHost);
        
        // protocol
        String actualScheme = remoteAddrAndHostTrackerValve.getScheme();
        assertEquals("x-forwarded-proto says http", "http", actualScheme);
        
        int actualServerPort = remoteAddrAndHostTrackerValve.getServerPort();
        assertEquals("x-forwarded-proto says http", 80, actualServerPort);
        
        boolean actualSecure = remoteAddrAndHostTrackerValve.isSecure();
        assertEquals("x-forwarded-proto says http", false, actualSecure);

        boolean actualPostInvokeSecure = request.isSecure();
        assertEquals("postInvoke secure", true, actualPostInvokeSecure);

        int actualPostInvokeServerPort = request.getServerPort();
        assertEquals("postInvoke serverPort", 8443, actualPostInvokeServerPort);

        String actualPostInvokeScheme = request.getScheme();
        assertEquals("postInvoke scheme", "https", actualPostInvokeScheme);
    }
    
    public void testInvokeXforwardedProtoIsNullForIncomingHttpsRequest() throws Exception {
        
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProtocolHeader("x-forwarded-proto");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        // client ip
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("192.168.0.10");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130");
        // protocol
        // Don't declare "x-forwarded-proto"
        request.setSecure(true);
        request.setServerPort(8443);
        request.getCoyoteRequest().scheme().setString("https");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        // client ip
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("no intermediate non-trusted proxy, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("no intermediate trusted proxy", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteHost);
        
        // protocol
        String actualScheme = remoteAddrAndHostTrackerValve.getScheme();
        assertEquals("x-forwarded-proto is null", "https", actualScheme);
        
        int actualServerPort = remoteAddrAndHostTrackerValve.getServerPort();
        assertEquals("x-forwarded-proto is null", 8443, actualServerPort);
        
        boolean actualSecure = remoteAddrAndHostTrackerValve.isSecure();
        assertEquals("x-forwarded-proto is null", true, actualSecure);

        boolean actualPostInvokeSecure = request.isSecure();
        assertEquals("postInvoke secure", true, actualPostInvokeSecure);

        int actualPostInvokeServerPort = request.getServerPort();
        assertEquals("postInvoke serverPort", 8443, actualPostInvokeServerPort);

        String actualPostInvokeScheme = request.getScheme();
        assertEquals("postInvoke scheme", "https", actualPostInvokeScheme);
    }
    
    public void testInvokeNotAllowedRemoteAddr() throws Exception {
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("192\\.168\\.0\\.10|192\\.168\\.0\\.11");
        remoteIpValve.setTrustedProxies("proxy1|proxy2|proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("not-allowed-internal-proxy");
        request.setRemoteHost("not-allowed-internal-proxy-host");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for").setString("140.211.11.130, proxy1, proxy2");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertEquals("x-forwarded-for must be unchanged", "140.211.11.130, proxy1, proxy2", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("x-forwarded-by must be null", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "not-allowed-internal-proxy", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "not-allowed-internal-proxy-host", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "not-allowed-internal-proxy", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "not-allowed-internal-proxy-host", actualPostInvokeRemoteHost);
    }
    
    public void testInvokeUntrustedProxyInTheChain() throws Exception {
        // PREPARE
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setInternalProxies("192\\.168\\.0\\.10|192\\.168\\.0\\.11");
        remoteIpValve.setTrustedProxies("proxy1|proxy2|proxy3");
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProxiesHeader("x-forwarded-by");
        RemoteAddrAndHostTrackerValve remoteAddrAndHostTrackerValve = new RemoteAddrAndHostTrackerValve();
        remoteIpValve.setNext(remoteAddrAndHostTrackerValve);
        
        Request request = new Request();
        request.setCoyoteRequest(new org.apache.coyote.Request());
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.getCoyoteRequest().getMimeHeaders().addValue("x-forwarded-for")
            .setString("140.211.11.130, proxy1, untrusted-proxy, proxy2");
        
        // TEST
        remoteIpValve.invoke(request, null);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertEquals("ip/host before untrusted-proxy must appear in x-forwarded-for", "140.211.11.130, proxy1", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertEquals("ip/host after untrusted-proxy must appear in  x-forwarded-by", "proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = remoteAddrAndHostTrackerValve.getRemoteAddr();
        assertEquals("remoteAddr", "untrusted-proxy", actualRemoteAddr);
        
        String actualRemoteHost = remoteAddrAndHostTrackerValve.getRemoteHost();
        assertEquals("remoteHost", "untrusted-proxy", actualRemoteHost);
        
        String actualPostInvokeRemoteAddr = request.getRemoteAddr();
        assertEquals("postInvoke remoteAddr", "192.168.0.10", actualPostInvokeRemoteAddr);
        
        String actualPostInvokeRemoteHost = request.getRemoteHost();
        assertEquals("postInvoke remoteAddr", "remote-host-original-value", actualPostInvokeRemoteHost);
    }
    
    public void testListToCommaDelimitedString() {
        String[] actual = RemoteIpValve.commaDelimitedListToStringArray("element1, element2, element3");
        String[] expected = new String[] {
            "element1", "element2", "element3"
        };
        assertArrayEquals(expected, actual);
    }
    
    public void testListToCommaDelimitedStringMixedSpaceChars() {
        String[] actual = RemoteIpValve.commaDelimitedListToStringArray("element1  , element2,\t element3");
        String[] expected = new String[] {
            "element1", "element2", "element3"
        };
        assertArrayEquals(expected, actual);
    }
    
    private void assertArrayEquals(String[] expected, String[] actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.length, actual.length);
        List<String> e = new ArrayList<String>();
        e.addAll(Arrays.asList(expected));
        List<String> a = new ArrayList<String>();
        a.addAll(Arrays.asList(actual));
        
        for (String entry : e) {
            assertTrue(a.remove(entry));
        }
        assertTrue(a.isEmpty());
    }
}
