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

package org.apache.catalina.loader;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.JreMemoryLeakPreventionListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.scan.StandardJarScanner;

public class TestVirtualContext extends TomcatBaseTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tomcat tomcat = getTomcatInstance();

        // BZ 49218: The test fails if JreMemoryLeakPreventionListener is not
        // present. The listener affects the JVM, and thus not only the current,
        // but also the subsequent tests that are run in the same JVM. So it is
        // fair to add it in every test.
        tomcat.getServer().addLifecycleListener(
            new JreMemoryLeakPreventionListener());
    }

    @Test
    public void testVirtualClassLoader() throws Exception {
        Tomcat tomcat = getTomcatInstance();

        File appDir = new File("test/webapp-3.0-virtual-webapp/src/main/webapp");
        // app dir is relative to server home
        StandardContext ctx = (StandardContext) tomcat.addWebapp(null, "/test",
            appDir.getAbsolutePath());

        ctx.setResources(new StandardRoot(ctx));
        File f1 = new File("test/webapp-3.0-virtual-webapp/target/classes");
        File f2 = new File("test/webapp-3.0-virtual-library/target/classes");
        File f3 = new File(
                "test/webapp-3.0-virtual-webapp/src/main/webapp/WEB-INF/classes");
        File f4 = new File(
                "test/webapp-3.0-virtual-webapp/src/main/webapp2/WEB-INF/classes");
        File f5 = new File("test/webapp-3.0-virtual-webapp/src/main/misc");
        File f6 = new File("test/webapp-3.0-virtual-webapp/src/main/webapp2");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f1.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f2.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f3.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f4.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f5.getAbsolutePath(),
                "/other", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f6.getAbsolutePath(),
                "/", "/");

        StandardJarScanner jarScanner = new StandardJarScanner();
        jarScanner.setScanAllDirectories(true);
        ctx.setJarScanner(jarScanner);
        ctx.setAddWebinfClassesResources(true);

        tomcat.start();

        assertPageContains("/test/classpathGetResourceAsStream.jsp?path=nonexistent",
            "resourceAInWebInfClasses=true", 404);

        assertPageContains(
            "/test/classpathGetResourceAsStream.jsp?path=rsrc/resourceA.properties",
            "resourceAInWebInfClasses=true");
        assertPageContains(
            "/test/classpathGetResourceUrlThenGetStream.jsp?path=rsrc/resourceA.properties",
            "resourceAInWebInfClasses=true");

        assertPageContains(
            "/test/classpathGetResourceAsStream.jsp?path=rsrc/resourceB.properties",
            "resourceBInTargetClasses=true");
        assertPageContains(
            "/test/classpathGetResourceUrlThenGetStream.jsp?path=rsrc/resourceB.properties",
            "resourceBInTargetClasses=true");

        assertPageContains(
            "/test/classpathGetResourceAsStream.jsp?path=rsrc/resourceC.properties",
            "resourceCInDependentLibraryTargetClasses=true");
        assertPageContains(
            "/test/classpathGetResourceUrlThenGetStream.jsp?path=rsrc/resourceC.properties",
            "resourceCInDependentLibraryTargetClasses=true");

        assertPageContains(
            "/test/classpathGetResourceAsStream.jsp?path=rsrc/resourceD.properties",
            "resourceDInPackagedJarInWebInfLib=true");
        assertPageContains(
            "/test/classpathGetResourceUrlThenGetStream.jsp?path=rsrc/resourceD.properties",
            "resourceDInPackagedJarInWebInfLib=true");

        assertPageContains(
            "/test/classpathGetResourceAsStream.jsp?path=rsrc/resourceG.properties",
            "resourceGInWebInfClasses=true");
        assertPageContains(
            "/test/classpathGetResourceUrlThenGetStream.jsp?path=rsrc/resourceG.properties",
            "resourceGInWebInfClasses=true");

        // test listing all possible paths for a classpath resource
        String allUrls =
            getUrl(
                "http://localhost:" + getPort() +
                    "/test/classpathGetResources.jsp?path=rsrc/").toString();
        assertTrue(
            allUrls,
            allUrls.indexOf("/test/webapp-3.0-virtual-webapp/src/main/webapp/WEB-INF/classes/rsrc") > 0);
        assertTrue(
            allUrls,
            allUrls.indexOf("/test/webapp-3.0-virtual-webapp/src/main/webapp2/WEB-INF/classes/rsrc") > 0);
        assertTrue(
            allUrls,
            allUrls.indexOf("/test/webapp-3.0-virtual-webapp/src/main/webapp/WEB-INF/lib/rsrc.jar!/rsrc") > 0);
        assertTrue(
            allUrls,
            allUrls.indexOf("/test/webapp-3.0-virtual-webapp/target/classes/rsrc") > 0);
        assertTrue(
            allUrls,
            allUrls.indexOf("/test/webapp-3.0-virtual-library/target/classes/rsrc") > 0);

        // check that there's no duplicate in the URLs
        String[] allUrlsArray = allUrls.split("\\s+");
        Assert.assertEquals(new HashSet<>(Arrays.asList(allUrlsArray)).size(),
            allUrlsArray.length);

        String allRsrsc2ClasspathUrls =
            getUrl(
                "http://localhost:" + getPort() +
                    "/test/classpathGetResources.jsp?path=rsrc2/").toString();
        assertTrue(
            allRsrsc2ClasspathUrls,
            allRsrsc2ClasspathUrls.indexOf("/test/webapp-3.0-virtual-webapp/src/main/webapp2/WEB-INF/classes/rsrc2") > 0);

        // tests context.getRealPath

        // the following fails because getRealPath always return a non-null path
        // even if there's no such resource
        // assertPageContains("/test/contextGetRealPath.jsp?path=nonexistent",
        // "resourceAInWebInfClasses=true", 404);

        // Real paths depend on the OS and this test has to work on all
        // platforms so use File to convert the path to a platform specific form
        File f = new File(
            "test/webapp-3.0-virtual-webapp/src/main/webapp/rsrc/resourceF.properties");
        assertPageContains(
            "/test/contextGetRealPath.jsp?path=/rsrc/resourceF.properties",
            f.getPath());

        // tests context.getResource then the content

        assertPageContains("/test/contextGetResource.jsp?path=nonexistent",
            "resourceAInWebInfClasses=true", 404);
        assertPageContains(
            "/test/contextGetResource.jsp?path=/WEB-INF/classes/rsrc/resourceA.properties",
            "resourceAInWebInfClasses=true");
        assertPageContains(
            "/test/contextGetResource.jsp?path=/WEB-INF/classes/rsrc/resourceG.properties",
            "resourceGInWebInfClasses=true");
        assertPageContains(
            "/test/contextGetResource.jsp?path=/rsrc/resourceE.properties",
            "resourceEInDependentLibraryTargetClasses=true");
        assertPageContains(
            "/test/contextGetResource.jsp?path=/other/resourceI.properties",
            "resourceIInWebapp=true");
        assertPageContains(
            "/test/contextGetResource.jsp?path=/rsrc2/resourceJ.properties",
            "resourceJInWebapp=true");

        String allRsrcPaths =
            getUrl(
                "http://localhost:" + getPort() +
                    "/test/contextGetResourcePaths.jsp?path=/rsrc/").toString();
        assertTrue(
            allRsrcPaths,
            allRsrcPaths.indexOf("/rsrc/resourceF.properties") > 0);
        assertTrue(
            allRsrcPaths,
            allRsrcPaths.indexOf("/rsrc/resourceE.properties") > 0);
        assertTrue(
            allRsrcPaths,
            allRsrcPaths.indexOf("/rsrc/resourceH.properties") > 0);

        // check that there's no duplicate in the URLs
        String[] allRsrcPathsArray = allRsrcPaths.split("\\s+");
        Assert.assertEquals(new HashSet<>(Arrays.asList(allRsrcPathsArray)).size(),
            allRsrcPathsArray.length);

        String allRsrc2Paths =
            getUrl(
                "http://localhost:" + getPort() +
                    "/test/contextGetResourcePaths.jsp?path=/rsrc2/").toString();
        assertTrue(
            allRsrc2Paths,
            allRsrc2Paths.indexOf("/rsrc2/resourceJ.properties") > 0);

        assertPageContains(
            "/test/testTlds.jsp",
            "worldA");
        assertPageContains(
            "/test/testTlds.jsp",
            "worldB");
        assertPageContains(
            "/test/testTlds.jsp",
            "worldC");
        assertPageContains(
            "/test/testTlds.jsp",
            "worldD");
    }

    @Test
    public void testAdditionalWebInfClassesPaths() throws Exception {
        Tomcat tomcat = getTomcatInstance();

        File appDir = new File("test/webapp-3.0-virtual-webapp/src/main/webapp");
        // app dir is relative to server home
        StandardContext ctx = (StandardContext) tomcat.addWebapp(null, "/test",
            appDir.getAbsolutePath());
        File tempFile = File.createTempFile("virtualWebInfClasses", null);

        File additionWebInfClasses = new File(tempFile.getAbsolutePath() + ".dir");
        Assert.assertTrue(additionWebInfClasses.mkdirs());
        File targetPackageForAnnotatedClass =
            new File(additionWebInfClasses,
                MyAnnotatedServlet.class.getPackage().getName().replace('.', '/'));
        Assert.assertTrue(targetPackageForAnnotatedClass.mkdirs());
        InputStream annotatedServletClassInputStream =
            this.getClass().getResourceAsStream(
                MyAnnotatedServlet.class.getSimpleName() + ".class");
        FileOutputStream annotatedServletClassOutputStream =
            new FileOutputStream(new File(targetPackageForAnnotatedClass,
                MyAnnotatedServlet.class.getSimpleName() + ".class"));
        IOUtils.copy(annotatedServletClassInputStream, annotatedServletClassOutputStream);
        annotatedServletClassInputStream.close();
        annotatedServletClassOutputStream.close();

        ctx.setResources(new StandardRoot(ctx));
        File f1 = new File("test/webapp-3.0-virtual-webapp/target/classes");
        File f2 = new File("test/webapp-3.0-virtual-library/target/classes");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f1.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f2.getAbsolutePath(),
                "/WEB-INF/classes", "/");

        tomcat.start();
        // first test that without the setting on StandardContext the annotated
        // servlet is not detected
        assertPageContains("/test/annotatedServlet", MyAnnotatedServlet.MESSAGE, 404);

        tomcat.stop();

        // then test that if we configure StandardContext with the additional
        // path, the servlet is detected
        ctx.setResources(new StandardRoot(ctx));
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f1.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST, f2.getAbsolutePath(),
                "/WEB-INF/classes", "/");
        ctx.getResources().createWebResourceSet(
                WebResourceRoot.ResourceSetType.POST,
                additionWebInfClasses.getAbsolutePath(),
                "/WEB-INF/classes", "/");

        tomcat.start();
        assertPageContains("/test/annotatedServlet", MyAnnotatedServlet.MESSAGE);
        tomcat.stop();
        FileUtils.deleteDirectory(additionWebInfClasses);
        tempFile.delete();
    }

    private void assertPageContains(String pageUrl, String expectedBody)
        throws IOException {

        assertPageContains(pageUrl, expectedBody, 200);
    }

    private void assertPageContains(String pageUrl, String expectedBody,
        int expectedStatus) throws IOException {
        ByteChunk res = new ByteChunk();
        int sc = getUrl("http://localhost:" + getPort() + pageUrl, res, 3000,
                null, null);

        assertEquals(expectedStatus, sc);

        if (expectedStatus == 200) {
            String result = res.toString();
            assertTrue(result, result.indexOf(expectedBody) >= 0);
        }
    }
}
