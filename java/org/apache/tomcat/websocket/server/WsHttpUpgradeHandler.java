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
package org.apache.tomcat.websocket.server;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.WebConnection;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;

import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.Transformation;
import org.apache.tomcat.websocket.WsIOException;
import org.apache.tomcat.websocket.WsSession;

/**
 * Servlet 3.1 HTTP upgrade handler for WebSocket connections.
 */
public class WsHttpUpgradeHandler implements InternalHttpUpgradeHandler {

    private static final Log log =
            LogFactory.getLog(WsHttpUpgradeHandler.class);
    private static final StringManager sm =
            StringManager.getManager(Constants.PACKAGE_NAME);

    private final ClassLoader applicationClassLoader;

    private Endpoint ep;
    private EndpointConfig endpointConfig;
    private WsServerContainer webSocketContainer;
    private WsHandshakeRequest handshakeRequest;
    private List<Extension> negotiatedExtensions;
    private String subProtocol;
    private Transformation transformation;
    private Map<String,String> pathParameters;
    private boolean secure;
    private WebConnection connection;

    private WsSession wsSession;


    public WsHttpUpgradeHandler() {
        applicationClassLoader = Thread.currentThread().getContextClassLoader();
    }


    public void preInit(Endpoint ep, EndpointConfig endpointConfig,
            WsServerContainer wsc, WsHandshakeRequest handshakeRequest,
            List<Extension> negotiatedExtensionsPhase2, String subProtocol,
            Transformation transformation, Map<String,String> pathParameters,
            boolean secure) {
        this.ep = ep;
        this.endpointConfig = endpointConfig;
        this.webSocketContainer = wsc;
        this.handshakeRequest = handshakeRequest;
        this.negotiatedExtensions = negotiatedExtensionsPhase2;
        this.subProtocol = subProtocol;
        this.transformation = transformation;
        this.pathParameters = pathParameters;
        this.secure = secure;
    }


    @Override
    public void init(WebConnection connection) {
        if (ep == null) {
            throw new IllegalStateException(
                    sm.getString("wsHttpUpgradeHandler.noPreInit"));
        }

        this.connection = connection;

        ServletInputStream sis;
        ServletOutputStream sos;
        try {
            sis = connection.getInputStream();
            sos = connection.getOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        String httpSessionId = null;
        Object session = handshakeRequest.getHttpSession();
        if (session != null ) {
            httpSessionId = ((HttpSession) session).getId();
        }

        // Need to call onOpen using the web application's class loader
        // Create the frame using the application's class loader so it can pick
        // up application specific config from the ServerContainerImpl
        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader(applicationClassLoader);
        try {
            WsRemoteEndpointImplServer wsRemoteEndpointServer =
                    new WsRemoteEndpointImplServer(sis, sos, webSocketContainer);
            wsSession = new WsSession(ep, wsRemoteEndpointServer,
                    webSocketContainer, handshakeRequest.getRequestURI(),
                    handshakeRequest.getParameterMap(),
                    handshakeRequest.getQueryString(),
                    handshakeRequest.getUserPrincipal(), httpSessionId,
                    negotiatedExtensions, subProtocol, pathParameters, secure,
                    endpointConfig);
            WsFrameServer wsFrame = new WsFrameServer(sis, wsSession, transformation);
            sos.setWriteListener(new WsWriteListener(this, wsRemoteEndpointServer));
            // WsFrame adds the necessary final transformations. Copy the
            // completed transformation chain to the remote end point.
            wsRemoteEndpointServer.setTransformation(wsFrame.getTransformation());
            ep.onOpen(wsSession, endpointConfig);
            webSocketContainer.registerSession(ep, wsSession);
            try {
                sis.setReadListener(new WsReadListener(this, wsFrame));
            } catch (IllegalStateException e) {
                // It is not impossible that the stream is already closed during onOpen
            }
        } catch (DeploymentException e) {
            throw new IllegalArgumentException(e);
        } finally {
            t.setContextClassLoader(cl);
        }
    }


    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error(sm.getString("wsHttpUpgradeHandler.destroyFailed"), e);
            }
        }
    }


    private void onError(Throwable throwable) {
        // Need to call onError using the web application's class loader
        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader(applicationClassLoader);
        try {
            ep.onError(wsSession, throwable);
        } finally {
            t.setContextClassLoader(cl);
        }
    }


    private void close(CloseReason cr) {
        /*
         * Any call to this method is a result of a problem reading from the
         * client. At this point that state of the connection is unknown.
         * Attempt to send a close frame to the client and then close the socket
         * immediately. There is no point in waiting for a close frame from the
         * client because there is no guarantee that we can recover from
         * whatever messed up state the client put the connection into.
         */
        wsSession.onClose(cr);
    }


    private static class WsReadListener implements ReadListener {

        private final WsHttpUpgradeHandler wsProtocolHandler;
        private final WsFrameServer wsFrame;


        private WsReadListener(WsHttpUpgradeHandler wsProtocolHandler,
                WsFrameServer wsFrame) {
            this.wsProtocolHandler = wsProtocolHandler;
            this.wsFrame = wsFrame;
        }


        @Override
        public void onDataAvailable() {
            try {
                wsFrame.onDataAvailable();
            } catch (WsIOException ws) {
                wsProtocolHandler.close(ws.getCloseReason());
            } catch (EOFException eof) {
                CloseReason cr = new CloseReason(
                        CloseCodes.CLOSED_ABNORMALLY, eof.getMessage());
                wsProtocolHandler.close(cr);
            } catch (IOException ioe) {
                onError(ioe);
                CloseReason cr = new CloseReason(
                        CloseCodes.CLOSED_ABNORMALLY, ioe.getMessage());
                wsProtocolHandler.close(cr);
            }
        }


        @Override
        public void onAllDataRead() {
            // NO-OP
        }


        @Override
        public void onError(Throwable throwable) {
            wsProtocolHandler.onError(throwable);
        }
    }


    private static class WsWriteListener implements WriteListener {

        private final WsHttpUpgradeHandler wsProtocolHandler;
        private final WsRemoteEndpointImplServer wsRemoteEndpointServer;

        private WsWriteListener(WsHttpUpgradeHandler wsProtocolHandler,
                WsRemoteEndpointImplServer wsRemoteEndpointServer) {
            this.wsProtocolHandler = wsProtocolHandler;
            this.wsRemoteEndpointServer = wsRemoteEndpointServer;
        }


        @Override
        public void onWritePossible() {
            // Triggered by the poller so this isn't the same thread that
            // triggered the write so no need for a dispatch
            wsRemoteEndpointServer.onWritePossible(false);
        }


        @Override
        public void onError(Throwable throwable) {
            wsProtocolHandler.onError(throwable);
            wsRemoteEndpointServer.close();
        }
    }
}
