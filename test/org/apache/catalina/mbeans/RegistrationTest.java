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
package org.apache.catalina.mbeans;

import java.io.File;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;
import org.apache.tomcat.util.modeler.Registry;

/**
 * General tests around the process of registration and de-registration that
 * don't necessarily apply to one specific Tomcat class.
 *
 */
public class RegistrationTest extends TomcatBaseTest {

    /**
     * Test verifying that Tomcat correctly de-registers the MBeans it has
     * registered.
     * @author Marc Guillemot
     */
	public void testMBeanDeregistration() throws Exception {
		final MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
        Set<ObjectName> onames = mbeanServer.queryNames(new ObjectName("Catalina:*"), null);
        assertEquals("Remaining: " + onames, 0, onames.size());

        final Tomcat tomcat = getTomcatInstance();
        // need to register a ServerLifecycleListener otherwise only a few MBeans are registered 
        tomcat.getServer().addLifecycleListener(new ServerLifecycleListener());
        final File contextDir = new File("output/webappFoo");
        contextDir.mkdir();
        tomcat.addContext("/foo", contextDir.getAbsolutePath());
        tomcat.start();
        
        // Verify there are no Catalina MBeans
        onames = mbeanServer.queryNames(new ObjectName("Catalina:*"), null);
        assertEquals("Found: " + onames, 0, onames.size());

        // Verify there are some Tomcat MBeans
        onames = mbeanServer.queryNames(new ObjectName("Tomcat:*"), null);
        assertTrue("No Tomcat MBeans", onames.size() > 0);

        tomcat.stop();

        // Verify there are no Tomcat MBeans
        onames = mbeanServer.queryNames(new ObjectName("Catalina:*"), null);
        assertEquals("Remaining: " + onames, 0, onames.size());

        // add a new host
        StandardHost host = new StandardHost();
        host.setName("otherhost");
        tomcat.getEngine().addChild(host);

        final File contextDir2 = new File("output/webappFoo2");
        contextDir2.mkdir();
        tomcat.addContext(host, "/foo2", contextDir2.getAbsolutePath());
        
        tomcat.start();
        tomcat.stop();

        onames = mbeanServer.queryNames(new ObjectName("Catalina:*"), null);
        assertEquals("Remaining: " + onames, 0, onames.size());
	}
	
}
