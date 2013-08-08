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
package org.apache.catalina.startup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * A variation of Java's JAR ServiceLoader that respects exclusion rules for
 * web applications.
 * <p/>
 * Primarily intended for use loading ServletContainerInitializers as defined
 * by Servlet 8.2.4. This implementation does not attempt lazy loading as the
 * container is required to introspect all implementations discovered.
 * <p/>
 * If the ServletContext defines ORDERED_LIBS, then only JARs in WEB-INF/lib
 * that are named in that set will be included in the search for
 * provider configuration files; if ORDERED_LIBS is not defined then
 * all JARs will be searched for provider configuration files. Providers
 * defined by resources in the parent ClassLoader will always be returned.
 * <p/>
 * Provider classes will be loaded using the context's ClassLoader.
 *
 * @see javax.servlet.ServletContainerInitializer
 * @see java.util.ServiceLoader
 */
public class WebappServiceLoader<T> {
    private static final String LIB = "/WEB-INF/lib/";
    private static final String SERVICES = "META-INF/services/";

    private final ServletContext context;

    /**
     * Construct a loader to load services from a ServletContext.
     *
     * @param context the context to use
     */
    public WebappServiceLoader(ServletContext context) {
        this.context = context;
    }

    /**
     * Load the providers for a service type.
     *
     * @param serviceType the type of service to load
     * @return an unmodifiable collection of service providers
     * @throws IOException if there was a problem loading any service
     */
    public Collection<T> load(Class<T> serviceType) throws IOException {
        String configFile = SERVICES + serviceType.getName();

        Set<String> servicesFound = new HashSet<>();
        ClassLoader loader = context.getClassLoader();

        // if the ServletContext has ORDERED_LIBS, then use that to specify the
        // set of JARs from WEB-INF/lib that should be used for loading services
        @SuppressWarnings("unchecked")
        List<String> orderedLibs =
                (List<String>) context.getAttribute(ServletContext.ORDERED_LIBS);
        if (orderedLibs != null) {
            // handle ordered libs directly, ...
            for (String lib : orderedLibs) {
                URL jarUrl = context.getResource(LIB + lib);
                if (jarUrl == null) {
                    // should not happen, just ignore
                    continue;
                }

                String base = jarUrl.toExternalForm();
                URL url;
                if (base.endsWith("/")) {
                    url = new URL(base + configFile);
                } else {
                    url = new URL("jar:" + base + "!/" + configFile);
                }
                try {
                    parseConfigFile(servicesFound, url);
                } catch (FileNotFoundException e) {
                    // no provider file found, this is OK
                }
            }

            // and the parent ClassLoader for all others
            loader = loader.getParent();
        }

        Enumeration<URL> resources;
        if (loader == null) {
            resources = ClassLoader.getSystemResources(configFile);
        } else {
            resources = loader.getResources(configFile);
        }
        while (resources.hasMoreElements()) {
            parseConfigFile(servicesFound, resources.nextElement());
        }

        // load the discovered services
        if (servicesFound.isEmpty()) {
            return Collections.emptyList();
        }
        return loadServices(serviceType, servicesFound);
    }

    void parseConfigFile(Set<String> servicesFound, URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            InputStreamReader in =
                    new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(in);
            String line;
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf('#');
                if (i >= 0) {
                    line = line.substring(0, i);
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                servicesFound.add(line);
            }
        }
    }

    Collection<T> loadServices(Class<T> serviceType, Set<String> servicesFound)
            throws IOException {
        ClassLoader loader = context.getClassLoader();
        List<T> services = new ArrayList<>(servicesFound.size());
        for (String serviceClass : servicesFound) {
            try {
                Class<?> clazz = Class.forName(serviceClass, true, loader);
                services.add(serviceType.cast(clazz.newInstance()));
            } catch (ClassNotFoundException | InstantiationException |
                    IllegalAccessException | ClassCastException e) {
                throw new IOException(e);
            }
        }
        return Collections.unmodifiableCollection(services);
    }
}
