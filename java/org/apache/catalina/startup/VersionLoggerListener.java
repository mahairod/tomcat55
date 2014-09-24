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

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.ServerInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

/**
 * Logs version information on startup.
 */
public class VersionLoggerListener implements LifecycleListener {

    private static final Log log = LogFactory.getLog(VersionLoggerListener.class);

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Constants.Package);


    public VersionLoggerListener() {
        // The log message is generated here to ensure that it appears before
        // any log messages from the APRLifecycleListener. This won't be logged
        // on shutdown because only the Server element in server.xml is
        // processed on shutdown.
        log();
    }


    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        // NO-OP
    }


    private void log() {
        log.info(sm.getString("versionLoggerListener.serverInfo.server.version",
                ServerInfo.getServerInfo()));
        log.info(sm.getString("versionLoggerListener.serverInfo.server.built",
                ServerInfo.getServerBuilt()));
        log.info(sm.getString("versionLoggerListener.serverInfo.server.number",
                ServerInfo.getServerNumber()));
        log.info(sm.getString("versionLoggerListener.serverInfo.os.name",
                System.getProperty("os.name")));
        log.info(sm.getString("versionLoggerListener.serverInfo.os.version",
                System.getProperty("os.version")));
        log.info(sm.getString("versionLoggerListener.serverInfo.os.arch",
                System.getProperty("os.arch")));
        log.info(sm.getString("versionLoggerListener.serverInfo.vm.version",
                System.getProperty("java.runtime.version")));
        log.info(sm.getString("versionLoggerListener.serverInfo.vm.vendor",
                System.getProperty("java.vm.vendor")));
    }
}
