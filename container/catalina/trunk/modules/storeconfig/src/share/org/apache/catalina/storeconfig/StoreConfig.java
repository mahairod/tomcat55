/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Store Server/Service/Host/Context at file or PrintWriter. Default server.xml
 * is at $catalina.base/conf/server.xml
 * 
 * @author Peter Rossbach
 *  
 */
public class StoreConfig implements IStoreConfig {
    private static Log log = LogFactory.getLog(StoreConfig.class);

    private String serverFilename = "conf/server.xml";

    private StoreRegistry registry;

    /**
     * get server.xml location
     * 
     * @return
     */
    public String getServerFilename() {
        return serverFilename;
    }

    /**
     * set new server.xml location
     * 
     * @param string
     */
    public void setServerFilename(String string) {
        serverFilename = string;
    }

    /*
     * Get the StoreRegistry with all factory to generate the
     * server.xml/context.xml files
     * 
     * @see org.apache.catalina.config.IStoreConfig#getRegistry()
     */
    public StoreRegistry getRegistry() {
        return registry;
    }

    /*
     * set StoreRegistry
     * 
     * @see org.apache.catalina.config.IStoreConfig#setRegistry(org.apache.catalina.config.ConfigurationRegistry)
     */
    public void setRegistry(StoreRegistry aRegistry) {
        registry = aRegistry;
    }

    /**
     * Store current Server
     * 
     * @see org.apache.catalina.ServerFactory#getServer()
     */
    public synchronized void storeConfig() {
        store(ServerFactory.getServer());
    }

    /**
     * Store Server from Object Name (Catalina:type=Server)
     * 
     * @param aServerName
     *            Server ObjectName
     * @param backup
     * @param externalAllowed
     *            s *
     * @throws MalformedObjectNameException
     */
    public synchronized void storeServer(String aServerName, boolean backup,
            boolean externalAllowed) throws MalformedObjectNameException {
        if (aServerName == null || aServerName.length() == 0) {
            if (log.isErrorEnabled())
                log.error("Please, call with a correct server ObjectName!");
            return;
        }
        MBeanServer mserver = MBeanUtils.createServer();
        ObjectName objectName = new ObjectName(aServerName);
        if (mserver.isRegistered(objectName)) {
            try {
                Server aServer = (Server) mserver.getAttribute(objectName,
                        "managedResource");
                StoreDescription desc = null;
                desc = getRegistry().findDescription(StandardContext.class);
                if (desc != null) {
                    boolean oldSeparate = desc.isStoreSeparate();
                    boolean oldBackup = desc.isBackup();
                    boolean oldExternalAllowed = desc.isExternalAllowed();
                    try {
                        desc.setStoreSeparate(true);
                        desc.setBackup(backup);
                        desc.setExternalAllowed(externalAllowed);
                        store((Server) aServer);
                    } finally {
                        desc.setStoreSeparate(oldSeparate);
                        desc.setBackup(oldBackup);
                        desc.setExternalAllowed(oldExternalAllowed);
                    }
                } else
                    store((Server) aServer);
            } catch (Exception e) {
                if (log.isInfoEnabled())
                    log.info("Object " + aServerName
                            + " is no a Server instance or store exception", e);
            }
        } else if (log.isInfoEnabled())
            log.info("Server " + aServerName + " not found!");
    }

    /**
     * Store a Context from ObjectName
     * 
     * @param aContextName
     *            MBean ObjectName
     * @param backup
     * @param externalAllowed
     * @throws MalformedObjectNameException
     */
    public synchronized void storeContext(String aContextName, boolean backup,
            boolean externalAllowed) throws MalformedObjectNameException {
        if (aContextName == null || aContextName.length() == 0) {
            if (log.isErrorEnabled())
                log.error("Please, call with a correct context ObjectName!");
            return;
        }
        MBeanServer mserver = MBeanUtils.createServer();
        ObjectName objectName = new ObjectName(aContextName);
        if (mserver.isRegistered(objectName)) {
            try {
                Context aContext = (Context) mserver.getAttribute(objectName,
                        "managedResource");
                String configFile = aContext.getConfigFile();
                if (configFile != null) {
                    try {
                        StoreDescription desc = null;
                        desc = getRegistry().findDescription(
                                aContext.getClass());
                        if (desc != null) {
                            boolean oldSeparate = desc.isStoreSeparate();
                            boolean oldBackup = desc.isBackup();
                            boolean oldExternalAllowed = desc
                                    .isExternalAllowed();
                            try {
                                desc.setStoreSeparate(true);
                                desc.setBackup(backup);
                                desc.setExternalAllowed(externalAllowed);
                                desc.getStoreFactory()
                                        .store(null, -2, aContext);
                            } finally {
                                desc.setStoreSeparate(oldSeparate);
                                desc.setBackup(oldBackup);
                                desc.setBackup(oldExternalAllowed);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else
                    log.error("Missing configFile at Context "
                            + aContext.getPath() + " to store!");
            } catch (Exception e) {
                if (log.isInfoEnabled())
                    log
                            .info(
                                    "Object "
                                            + aContextName
                                            + " is no a context instance or store exception",
                                    e);
            }
        } else if (log.isInfoEnabled())
            log.info("Context " + aContextName + " not found!");
    }

    /**
     * Write the configuration information for this entire <code>Server</code>
     * out to the server.xml configuration file.
     *  
     */
    public synchronized void store(Server aServer) {

        StoreFileMover mover = new StoreFileMover(System
                .getProperty("catalina.base"), getServerFilename(),
                getRegistry().getEncoding());
        // Open an output writer for the new configuration file
        try {
            PrintWriter writer = mover.getWriter();

            try {
                store(writer, -2, aServer);
            } finally {
                // Flush and close the output file
                try {
                    writer.flush();
                } catch (Exception e) {
                    log.error(e);
                }
                try {
                    writer.close();
                } catch (Exception e) {
                    throw (e);
                }
            }
            mover.move();
        } catch (Exception e) {
            log.error(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.config.IStoreConfig#store(org.apache.catalina.Context)
     */
    public synchronized void store(Context aContext) {
        String configFile = aContext.getConfigFile();
        if (configFile != null) {
            try {
                StoreDescription desc = null;
                desc = getRegistry().findDescription(aContext.getClass());
                if (desc != null) {
                    boolean old = desc.isStoreSeparate();
                    try {
                        desc.setStoreSeparate(true);
                        desc.getStoreFactory().store(null, -2, aContext);
                    } finally {
                        desc.setStoreSeparate(old);
                    }
                }
            } catch (Exception e) {
                log.error(e);
            }
        } else
            log.error("Missing configFile at Context " + aContext.getPath());

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.config.IStoreConfig#store(java.io.PrintWriter,
     *      int, org.apache.catalina.Context)
     */
    public synchronized void store(PrintWriter aWriter, int indent,
            Context aContext) {
        boolean oldSeparate = true;
        StoreDescription desc = null;
        try {
            desc = getRegistry().findDescription(aContext.getClass());
            oldSeparate = desc.isStoreSeparate();
            desc.setStoreSeparate(false);
            desc.getStoreFactory().store(aWriter, indent, aContext);
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (desc != null)
                desc.setStoreSeparate(oldSeparate);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.config.IStoreConfig#store(java.io.PrintWriter,
     *      int, org.apache.catalina.Host)
     */
    public synchronized void store(PrintWriter aWriter, int indent, Host aHost) {
        try {
            StoreDescription desc = getRegistry().findDescription(
                    aHost.getClass());
            desc.getStoreFactory().store(aWriter, indent, aHost);
        } catch (Exception e) {
            log.error(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.config.IStoreConfig#store(java.io.PrintWriter,
     *      int, org.apache.catalina.Service)
     */
    public synchronized void store(PrintWriter aWriter, int indent,
            Service aService) {
        try {
            StoreDescription desc = getRegistry().findDescription(
                    aService.getClass());
            desc.getStoreFactory().store(aWriter, indent, aService);
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Store the state of this Server MBean (which will recursively store
     * everything)
     * 
     * @param writer
     * @param indent
     * @param aServer
     */
    public synchronized void store(PrintWriter writer, int indent,
            Server aServer) {
        try {
            StoreDescription desc = getRegistry().findDescription(
                    aServer.getClass());
            desc.getStoreFactory().store(writer, indent, aServer);
        } catch (Exception e) {
            log.error(e);
        }
    }

}