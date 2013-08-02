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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.WsSession;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.apache.tomcat.websocket.pojo.PojoEndpointServer;
import org.apache.tomcat.websocket.pojo.PojoMethodMapping;

/**
 * Provides a per class loader (i.e. per web application) instance of a
 * ServerContainer. Web application wide defaults may be configured by setting
 * the following servlet context initialisation parameters to the desired
 * values.
 * <ul>
 * <li>{@link Constants#BINARY_BUFFER_SIZE_SERVLET_CONTEXT_INIT_PARAM}</li>
 * <li>{@link Constants#TEXT_BUFFER_SIZE_SERVLET_CONTEXT_INIT_PARAM}</li>
 * </ul>
 */
public class WsServerContainer extends WsWebSocketContainer
        implements ServerContainer {

    private static final StringManager sm =
            StringManager.getManager(Constants.PACKAGE_NAME);
    private static final CloseReason AUTHENTICATED_HTTP_SESSION_CLOSED =
            new CloseReason(CloseCodes.VIOLATED_POLICY,
                    "This connection was established under an authenticated " +
                    "HTTP session that has ended.");

    private final WsWriteTimeout wsWriteTimeout = new WsWriteTimeout();

    private final ServletContext servletContext;
    private final Map<String,ServerEndpointConfig> configExactMatchMap =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer,SortedSet<TemplatePathMatch>>
            configTemplateMatchMap = new ConcurrentHashMap<>();
    private volatile boolean enforceNoAddAfterHandshake =
            org.apache.tomcat.websocket.Constants.STRICT_SPEC_COMPLIANCE;
    private volatile boolean addAllowed = true;
    private final ConcurrentHashMap<String,Set<WsSession>> authenticatedSessions =
            new ConcurrentHashMap<>();

    WsServerContainer(ServletContext servletContext) {

        this.servletContext = servletContext;

        // Configure servlet context wide defaults
        String value = servletContext.getInitParameter(
                Constants.BINARY_BUFFER_SIZE_SERVLET_CONTEXT_INIT_PARAM);
        if (value != null) {
            setDefaultMaxBinaryMessageBufferSize(Integer.parseInt(value));
        }

        value = servletContext.getInitParameter(
                Constants.TEXT_BUFFER_SIZE_SERVLET_CONTEXT_INIT_PARAM);
        if (value != null) {
            setDefaultMaxTextMessageBufferSize(Integer.parseInt(value));
        }

        value = servletContext.getInitParameter(
                Constants.ENFORCE_NO_ADD_AFTER_HANDSHAKE_CONTEXT_INIT_PARAM);
        if (value != null) {
            setEnforceNoAddAfterHandshake(Boolean.parseBoolean(value));
        }

        FilterRegistration fr = servletContext.addFilter(
                WsFilter.class.getName(), new WsFilter(this));

        EnumSet<DispatcherType> types = EnumSet.of(DispatcherType.REQUEST,
                DispatcherType.FORWARD);

        fr.addMappingForUrlPatterns(types, true, "/*");
    }


    /**
     * Published the provided endpoint implementation at the specified path with
     * the specified configuration. {@link #WsServerContainer(ServletContext)}
     * must be called before calling this method.
     *
     * @param sec   The configuration to use when creating endpoint instances
     * @throws DeploymentException
     */
    @Override
    public void addEndpoint(ServerEndpointConfig sec)
            throws DeploymentException {

        if (enforceNoAddAfterHandshake && !addAllowed) {
            throw new DeploymentException(
                    sm.getString("serverContainer.addNotAllowed"));
        }

        if (servletContext == null) {
            throw new DeploymentException(
                    sm.getString("serverContainer.servletContextMissing"));
        }
        String path = sec.getPath();

        UriTemplate uriTemplate = new UriTemplate(path);
        if (uriTemplate.hasParameters()) {
            Integer key = Integer.valueOf(uriTemplate.getSegmentCount());
            SortedSet<TemplatePathMatch> templateMatches =
                    configTemplateMatchMap.get(key);
            if (templateMatches == null) {
                // Ensure that if concurrent threads execute this block they
                // both end up using the same TreeSet instance
                templateMatches = new TreeSet<>(
                        TemplatePathMatchComparator.getInstance());
                configTemplateMatchMap.putIfAbsent(key, templateMatches);
                templateMatches = configTemplateMatchMap.get(key);
            }
            if (!templateMatches.add(new TemplatePathMatch(sec, uriTemplate))) {
                // Duplicate uriTemplate;
                throw new DeploymentException(
                        sm.getString("serverContainer.duplicatePaths", path));
            }
        } else {
            // Exact match
            ServerEndpointConfig old = configExactMatchMap.put(path, sec);
            if (old != null) {
                // Duplicate path mappings
                throw new DeploymentException(
                        sm.getString("serverContainer.duplicatePaths", path));
            }
        }
    }


    /**
     * Provides the equivalent of {@link #addEndpoint(ServerEndpointConfig)}
     * for publishing plain old java objects (POJOs) that have been annotated as
     * WebSocket endpoints.
     *
     * @param pojo   The annotated POJO
     */
    @Override
    public void addEndpoint(Class<?> pojo) throws DeploymentException {

        ServerEndpoint annotation = pojo.getAnnotation(ServerEndpoint.class);
        if (annotation == null) {
            throw new DeploymentException(
                    sm.getString("serverContainer.missingAnnotation",
                            pojo.getName()));
        }
        String path = annotation.value();

        // Validate encoders
        validateEncoders(annotation.encoders());

        // Method mapping
        PojoMethodMapping methodMapping = new PojoMethodMapping(pojo,
                annotation.decoders(), path);

        // ServerEndpointConfig
        ServerEndpointConfig sec;
        Class<? extends Configurator> configuratorClazz =
                annotation.configurator();
        Configurator configurator = null;
        if (!configuratorClazz.equals(Configurator.class)) {
            try {
                configurator = annotation.configurator().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new DeploymentException(sm.getString(
                        "serverContainer.configuratorFail",
                        annotation.configurator().getName(),
                        pojo.getClass().getName()), e);
            }
        }
        sec = ServerEndpointConfig.Builder.create(pojo, path).
                decoders(Arrays.asList(annotation.decoders())).
                encoders(Arrays.asList(annotation.encoders())).
                subprotocols(Arrays.asList(annotation.subprotocols())).
                configurator(configurator).
                build();
        sec.getUserProperties().put(
                PojoEndpointServer.POJO_METHOD_MAPPING_KEY,
                methodMapping);

        addEndpoint(sec);
    }


    public WsMappingResult findMapping(String path) {

        // Prevent registering additional endpoints once the first attempt has
        // been made to use one
        if (addAllowed) {
            addAllowed = false;
        }

        // Check an exact match. Simple case as there are no templates.
        ServerEndpointConfig sec = configExactMatchMap.get(path);
        if (sec != null) {
            return new WsMappingResult(sec, Collections.EMPTY_MAP);
        }

        // No exact match. Need to look for template matches.
        UriTemplate pathUriTemplate = null;
        try {
            pathUriTemplate = new UriTemplate(path);
        } catch (DeploymentException e) {
            // Path is not valid so can't be matched to a WebSocketEndpoint
            return null;
        }

        // Number of segments has to match
        Integer key = Integer.valueOf(pathUriTemplate.getSegmentCount());
        SortedSet<TemplatePathMatch> templateMatches =
                configTemplateMatchMap.get(key);

        if (templateMatches == null) {
            // No templates with an equal number of segments so there will be
            // no matches
            return null;
        }

        // List is in alphabetical order of normalised templates.
        // Correct match is the first one that matches.
        Map<String,String> pathParams = null;
        for (TemplatePathMatch templateMatch : templateMatches) {
            pathParams = templateMatch.getUriTemplate().match(pathUriTemplate);
            if (pathParams != null) {
                sec = templateMatch.getConfig();
                break;
            }
        }

        if (sec == null) {
            // No match
            return null;
        }

        if (!PojoEndpointServer.class.isAssignableFrom(sec.getEndpointClass())) {
            // Need to make path params available to POJO
            sec.getUserProperties().put(
                    PojoEndpointServer.POJO_PATH_PARAM_KEY,
                    pathParams);
        }

        return new WsMappingResult(sec, pathParams);
    }



    public boolean isEnforceNoAddAfterHandshake() {
        return enforceNoAddAfterHandshake;
    }


    public void setEnforceNoAddAfterHandshake(
            boolean enforceNoAddAfterHandshake) {
        this.enforceNoAddAfterHandshake = enforceNoAddAfterHandshake;
    }


    protected WsWriteTimeout getTimeout() {
        return wsWriteTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * Overridden to make it visible to other classes in this package.
     */
    @Override
    protected void registerSession(Endpoint endpoint, WsSession wsSession) {
        super.registerSession(endpoint, wsSession);
        if (wsSession.getUserPrincipal() != null &&
                wsSession.getHttpSessionId() != null) {
            registerAuthenticatedSession(wsSession,
                    wsSession.getHttpSessionId());
        }
    }


    /**
     * {@inheritDoc}
     *
     * Overridden to make it visible to other classes in this package.
     */
    @Override
    protected void unregisterSession(Endpoint endpoint, WsSession wsSession) {
        if (wsSession.getUserPrincipal() != null &&
                wsSession.getHttpSessionId() != null) {
            unregisterAuthenticatedSession(wsSession,
                    wsSession.getHttpSessionId());
        }
        super.unregisterSession(endpoint, wsSession);
    }


    private void registerAuthenticatedSession(WsSession wsSession,
            String httpSessionId) {
        Set<WsSession> wsSessions = authenticatedSessions.get(httpSessionId);
        if (wsSessions == null) {
            wsSessions = Collections.newSetFromMap(
                     new ConcurrentHashMap<WsSession,Boolean>());
             authenticatedSessions.putIfAbsent(httpSessionId, wsSessions);
             wsSessions = authenticatedSessions.get(httpSessionId);
        }
        wsSessions.add(wsSession);
    }


    private void unregisterAuthenticatedSession(WsSession wsSession,
            String httpSessionId) {
        Set<WsSession> wsSessions = authenticatedSessions.get(httpSessionId);
        wsSessions.remove(wsSession);
    }


    public void closeAuthenticatedSession(String httpSessionId) {
        Set<WsSession> wsSessions = authenticatedSessions.remove(httpSessionId);

        if (wsSessions != null && !wsSessions.isEmpty()) {
            for (WsSession wsSession : wsSessions) {
                try {
                    wsSession.close(AUTHENTICATED_HTTP_SESSION_CLOSED);
                } catch (IOException e) {
                    // Any IOExceptions during close will have been caught and the
                    // onError method called.
                }
            }
        }
    }

    private static void validateEncoders(Class<? extends Encoder>[] encoders)
            throws DeploymentException {

        for (Class<? extends Encoder> encoder : encoders) {
            // Need to instantiate decoder to ensure it is valid and that
            // deployment can be failed if it is not
            @SuppressWarnings("unused")
            Encoder instance;
            try {
                encoder.newInstance();
            } catch(InstantiationException | IllegalAccessException e) {
                throw new DeploymentException(sm.getString(
                        "serverContainer.encoderFail", encoder.getName()), e);
            }
        }
    }

    private static class TemplatePathMatch {
        private final ServerEndpointConfig config;
        private final UriTemplate uriTemplate;

        public TemplatePathMatch(ServerEndpointConfig config,
                UriTemplate uriTemplate) {
            this.config = config;
            this.uriTemplate = uriTemplate;
        }


        public ServerEndpointConfig getConfig() {
            return config;
        }


        public UriTemplate getUriTemplate() {
            return uriTemplate;
        }
    }


    /**
     * This Comparator implementation is thread-safe so only create a single
     * instance.
     */
    private static class TemplatePathMatchComparator
            implements Comparator<TemplatePathMatch> {

        private static final TemplatePathMatchComparator INSTANCE =
                new TemplatePathMatchComparator();

        public static TemplatePathMatchComparator getInstance() {
            return INSTANCE;
        }

        private TemplatePathMatchComparator() {
            // Hide default constructor
        }

        @Override
        public int compare(TemplatePathMatch tpm1, TemplatePathMatch tpm2) {
            return tpm1.getUriTemplate().getNormalizedPath().compareTo(
                    tpm2.getUriTemplate().getNormalizedPath());
        }
    }
}
