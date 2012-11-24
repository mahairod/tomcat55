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
package org.apache.websocket;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.WebSocketEndpoint;

@HandlesTypes({WebSocketEndpoint.class})
public class WsSci implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> clazzes, ServletContext ctx)
            throws ServletException {
        if (clazzes == null || clazzes.size() == 0) {
            return;
        }
        ServerContainerImpl sc = ServerContainerImpl.getServerContainer();

        for (Class<?> clazz : clazzes) {
            WebSocketEndpoint anotation =
                    clazz.getAnnotation(WebSocketEndpoint.class);
            String mappingPath = Util.getServletMappingPath(anotation.value());
            sc.publishServer(clazz, mappingPath);
        }
    }

}
