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
package javax.el;

import java.io.File;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.buf.ByteChunk;

public class TestCompositeELResolver extends TomcatBaseTest {

    @Test
    public void testBug50408() throws Exception {
        Tomcat tomcat = getTomcatInstance();

        File appDir =  new File("test/webapp");
        // app dir is relative to server home
        StandardContext ctxt = (StandardContext) tomcat.addWebapp(null,
                "/test", appDir.getAbsolutePath());

        // This test needs the JSTL libraries
        File lib = new File("webapps/examples/WEB-INF/lib");
        ctxt.setResources(new StandardRoot(ctxt));
        ctxt.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, "/WEB-INF/lib",
                lib.getAbsolutePath(), null, "/");

        tomcat.start();

        int rc = getUrl("http://localhost:" + getPort() +
                "/test/bug5nnnn/bug50408.jsp", new ByteChunk(), null);

        assertEquals(HttpServletResponse.SC_OK, rc);
    }
}
