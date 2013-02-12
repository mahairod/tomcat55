/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.websocket;

import javax.websocket.ContainerProvider;

public class WsContainerProvider extends ContainerProvider {

    @Override
    protected <T> T getContainer(Class<T> containerClass) {
        if (containerClass.isAssignableFrom(WsWebSocketContainer.class)) {
            @SuppressWarnings("unchecked")
            T result = (T) new WsWebSocketContainer();
            return result;
        } else {
            // Not supported
            return null;
        }
    }
}
