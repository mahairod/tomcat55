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

package org.apache.catalina.storeconfig;

import java.io.PrintWriter;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;

/**
 * Store server.xml Element Service and all childs 
 * @author Peter Rossbach (pero@apache.org)
 */
public class StandardServiceSF extends StoreFactoryBase {

    /**
     * Store Childs from this StandardService description
     * 
     * @param aWriter
     * @param indent
     * @param aService
     * @throws Exception
     */
    public void storeChilds(PrintWriter aWriter, int indent, Object aService,
            StoreDescription parentDesc) throws Exception {
        if (aService instanceof StandardService) {
            StandardService service = (StandardService) aService;
            // Store nested <Listener> elements
            if (service instanceof Lifecycle) {
                LifecycleListener listeners[] = ((Lifecycle) service)
                        .findLifecycleListeners();
                storeElementArray(aWriter, indent, listeners);
            }

            Connector connectors[] = service.findConnectors();
            storeElementArray(aWriter, indent, connectors);

            // Store nested <Engine> element (or other appropriate container)
            Container container = service.getContainer();
            if (container != null) {
                StoreDescription elementDesc = getRegistry().findDescription(
                        container.getClass());
                if (elementDesc != null) {
                    IStoreFactory factory = elementDesc.getStoreFactory();
                    factory.store(aWriter, indent, container);
                }
            }
        }

    }

}