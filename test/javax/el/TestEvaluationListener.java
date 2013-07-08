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

import java.util.List;

import javax.el.TesterEvaluationListener.Pair;

import org.junit.Assert;
import org.junit.Test;

public class TestEvaluationListener {


    @Test
    public void testPropertyResolved01() {
        ELContext context = new TesterELContext();
        ELResolver resolver = new BeanELResolver();
        TesterBean bean = new TesterBean("test");
        TesterEvaluationListener listener = new TesterEvaluationListener();

        context.addEvaluationListener(listener);

        Object result = resolver.getValue(context, bean, "name");

        Assert.assertTrue(context.isPropertyResolved());
        Assert.assertEquals("test", result);
        List<Pair> events = listener.getResolvedProperties();

        Assert.assertEquals(1, events.size());
        Pair p = events.get(0);
        Assert.assertEquals(bean, p.getBase());
        Assert.assertEquals("name", p.getProperty());
    }


    @Test
    public void testPropertyResolved02() {
        ELContext context = new TesterELContext();
        ELResolver resolver = new BeanELResolver();
        TesterBean bean = new TesterBean("test");
        TesterEvaluationListener listener = new TesterEvaluationListener();

        context.addEvaluationListener(listener);

        Exception exception = null;
        try {
            resolver.getValue(context, bean, "foo");
        } catch (PropertyNotFoundException e) {
            exception = e;
        }

        Assert.assertNotNull(exception);

        // Still expect the property to be resolved and the listener to fire
        // since the vent is at the time of resolution. The EL spec could be a
        // lot clear on this.
        Assert.assertTrue(context.isPropertyResolved());
        List<Pair> events = listener.getResolvedProperties();

        Assert.assertEquals(1, events.size());
        Pair p = events.get(0);
        Assert.assertEquals(bean, p.getBase());
        Assert.assertEquals("foo", p.getProperty());
    }
}
