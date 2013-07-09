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
package org.apache.jasper.servlet;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;

import org.junit.Assert;
import org.junit.Test;

public class TestJspCServletContext {

    @Test
    public void testWebapp() throws Exception {
        File appDir = new File("test/webapp");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(3, context.getEffectiveMajorVersion());
        Assert.assertEquals(1, context.getEffectiveMinorVersion());
        JspConfigDescriptor jspConfigDescriptor =
                context.getJspConfigDescriptor();
        Assert.assertTrue(jspConfigDescriptor.getTaglibs().isEmpty());
        Collection<JspPropertyGroupDescriptor> propertyGroups =
                jspConfigDescriptor.getJspPropertyGroups();
        Assert.assertEquals(1, propertyGroups.size());
        JspPropertyGroupDescriptor groupDescriptor =
                propertyGroups.iterator().next();
        Assert.assertEquals("text/plain",
                groupDescriptor.getDefaultContentType());
        Collection<String> urlPatterns =groupDescriptor.getUrlPatterns();
        Assert.assertEquals(2, urlPatterns.size());
        Iterator<String> iterator = urlPatterns.iterator();
        Assert.assertEquals("/bug49nnn/bug49726a.jsp", iterator.next());
        Assert.assertEquals("/bug49nnn/bug49726b.jsp", iterator.next());
    }

    @Test
    public void testWebapp_2_3() throws Exception {
        File appDir = new File("test/webapp-2.3");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(2, context.getEffectiveMajorVersion());
        Assert.assertEquals(3, context.getEffectiveMinorVersion());
    }

    @Test
    public void testWebapp_2_4() throws Exception {
        File appDir = new File("test/webapp-2.4");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(2, context.getEffectiveMajorVersion());
        Assert.assertEquals(4, context.getEffectiveMinorVersion());
    }

    @Test
    public void testWebapp_2_5() throws Exception {
        File appDir = new File("test/webapp-2.5");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(2, context.getEffectiveMajorVersion());
        Assert.assertEquals(5, context.getEffectiveMinorVersion());
    }

    @Test
    public void testWebapp_3_0() throws Exception {
        File appDir = new File("test/webapp-3.0");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(3, context.getEffectiveMajorVersion());
        Assert.assertEquals(0, context.getEffectiveMinorVersion());
    }

    @Test
    public void testWebapp_3_1() throws Exception {
        File appDir = new File("test/webapp-3.1");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(3, context.getEffectiveMajorVersion());
        Assert.assertEquals(1, context.getEffectiveMinorVersion());
    }

    @Test
    public void testWebresources() throws Exception {
        File appDir = new File("test/webresources/dir1");
        JspCServletContext context =
                new JspCServletContext(null, appDir.toURI().toURL());
        Assert.assertEquals(3, context.getEffectiveMajorVersion());
        Assert.assertEquals(1, context.getEffectiveMinorVersion());
    }
}
