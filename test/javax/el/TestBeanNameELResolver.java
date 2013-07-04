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

import org.junit.Assert;
import org.junit.Test;

public class TestBeanNameELResolver {

    private static final String BEAN01_NAME = "bean01";
    private static final TesterBean BEAN01 = new TesterBean(BEAN01_NAME);
    private static final String BEAN02_NAME = "bean02";
    private static final TesterBean BEAN02 = new TesterBean(BEAN02_NAME);
    private static final String BEAN99_NAME = "bean99";
    private static final TesterBean BEAN99 = new TesterBean(BEAN99_NAME);

    /**
     * Creates the resolver that is used for the test. All the tests use a
     * resolver with the same configuration.
     */
    private BeanNameELResolver createBeanNameELResolver() {
        return createBeanNameELResolver(true);
    }

    private BeanNameELResolver createBeanNameELResolver(boolean allowCreate) {

        TesterBeanNameResolver beanNameResolver = new TesterBeanNameResolver();
        beanNameResolver.setBeanValue(BEAN01_NAME, BEAN01);
        beanNameResolver.setBeanValue(BEAN02_NAME, BEAN02);
        beanNameResolver.setAllowCreate(allowCreate);

        BeanNameELResolver beanNameELResolver =
                new BeanNameELResolver(beanNameResolver);
        return beanNameELResolver;
    }


    /**
     * Tests that a null context results in an NPE as per EL Javadoc.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValue01() {
        BeanNameELResolver resolver = createBeanNameELResolver();
        resolver.getValue(null, new Object(), new Object());
    }


    /**
     * Tests that a valid bean is resolved.
     */
    @Test
    public void testGetValue02() {

        BeanNameELResolver resolver = createBeanNameELResolver();
        ELContext context =
                new StandardELContext(ELManager.getExpressionFactory());

        Object result = resolver.getValue(context, null, BEAN01_NAME);

        Assert.assertEquals(BEAN01, result);
        Assert.assertTrue(context.isPropertyResolved());
    }


    /**
     * Tests that a valid bean is not resolved if base is non-null.
     */
    @Test
    public void testGetValue03() {

        BeanNameELResolver resolver = createBeanNameELResolver();
        ELContext context =
                new StandardELContext(ELManager.getExpressionFactory());

        Object result = resolver.getValue(context, new Object(), BEAN01_NAME);

        Assert.assertNull(result);
        Assert.assertFalse(context.isPropertyResolved());
    }


    /**
     * Tests that a valid bean is not resolved if property is not a String even
     * if it can be coerced to a valid bean name.
     */
    @Test
    public void testGetValue04() {

        BeanNameELResolver resolver = createBeanNameELResolver();
        ELContext context =
                new StandardELContext(ELManager.getExpressionFactory());

        Object property = new Object() {
            @Override
            public String toString() {
                return BEAN01_NAME;
            }
        };

        Object result = resolver.getValue(context, null, property);

        Assert.assertNull(result);
        Assert.assertFalse(context.isPropertyResolved());
    }


    /**
     * Beans that don't exist shouldn't return anything
     */
    @Test
    public void testGetValue05() {

        BeanNameELResolver resolver = createBeanNameELResolver();
        ELContext context =
                new StandardELContext(ELManager.getExpressionFactory());

        Object result = resolver.getValue(context, null, BEAN99_NAME);

        Assert.assertNull(result);
        Assert.assertFalse(context.isPropertyResolved());
    }


    /**
     * Exception during resolution should be wrapped and re-thrown.
     */
    @Test
    public void testGetValue06() {
        doThrowableTest(TesterBeanNameResolver.EXCEPTION_TRIGGER_NAME,
                MethodUnderTest.GET_VALUE);
    }


    /**
     * Throwable during resolution should be wrapped and re-thrown.
     */
    @Test
    public void testGetValue07() {
        doThrowableTest(TesterBeanNameResolver.THROWABLE_TRIGGER_NAME,
                MethodUnderTest.GET_VALUE);
    }


    /**
     * Tests that a null context results in an NPE as per EL Javadoc.
     */
    @Test(expected=NullPointerException.class)
    public void testSetValue01() {
        BeanNameELResolver resolver = createBeanNameELResolver();
        resolver.setValue(null, new Object(), new Object(), new Object());
    }


    /**
     * Test replace with create enabled.
     */
    @Test
    public void testSetValue02() {
        doSetValueCreateReplaceTest(true, BEAN01_NAME);
    }


    /**
     * Test replace with create disabled.
     */
    @Test
    public void testSetValue03() {
        doSetValueCreateReplaceTest(false, BEAN01_NAME);
    }


    /**
     * Test create with create enabled.
     */
    @Test
    public void testSetValue04() {
        doSetValueCreateReplaceTest(true, BEAN99_NAME);
    }


    /**
     * Test create with create disabled.
     */
    @Test
    public void testSetValue05() {
        doSetValueCreateReplaceTest(false, BEAN99_NAME);
    }


    /**
     * Test replacing a read-only bean with create enabled.
     */
    @Test(expected=PropertyNotWritableException.class)
    public void testSetValue06() {
        doSetValueCreateReplaceTest(true,
                TesterBeanNameResolver.READ_ONLY_NAME);
    }


    /**
     * Test replacing a read-only bean with create disable.
     */
    @Test(expected=PropertyNotWritableException.class)
    public void testSetValue07() {
        doSetValueCreateReplaceTest(false,
                TesterBeanNameResolver.READ_ONLY_NAME);
    }


    /**
     * Exception during resolution should be wrapped and re-thrown.
     */
    @Test
    public void testGetValue08() {
        doThrowableTest(TesterBeanNameResolver.EXCEPTION_TRIGGER_NAME,
                MethodUnderTest.SET_VALUE);
    }


    /**
     * Throwable during resolution should be wrapped and re-thrown.
     */
    @Test
    public void testGetValue09() {
        doThrowableTest(TesterBeanNameResolver.THROWABLE_TRIGGER_NAME,
                MethodUnderTest.SET_VALUE);
    }


    private void doThrowableTest(String trigger, MethodUnderTest method) {
        BeanNameELResolver resolver = createBeanNameELResolver();
        ELContext context =
                new StandardELContext(ELManager.getExpressionFactory());

        ELException elException = null;
        try {
            switch (method) {
                case GET_VALUE: {
                    resolver.getValue(context, null, trigger);
                    break;
                }
                case SET_VALUE: {
                    resolver.setValue(context, null, trigger, new Object());
                    break;
                }
                default: {
                    // Should never happen
                    Assert.fail("Missing case for method");
                }
            }

        } catch (ELException e) {
            elException = e;
        }

        Assert.assertFalse(context.isPropertyResolved());
        Assert.assertNotNull(elException);

        @SuppressWarnings("null") // Can't be null due to assertion above
        Throwable cause = elException.getCause();
        Assert.assertNotNull(cause);
    }


    /**
     * Tests adding/replacing beans beans
     */
    private void doSetValueCreateReplaceTest(boolean canCreate,
            String beanName) {
        BeanNameELResolver resolver = createBeanNameELResolver(canCreate);
        ELContext context =
                new StandardELContext(ELManager.getExpressionFactory());

        // Get bean one to be sure it has been replaced when testing replace
        Object bean = resolver.getValue(context, null, BEAN01_NAME);

        Assert.assertTrue(context.isPropertyResolved());
        Assert.assertEquals(BEAN01, bean);

        // Reset context
        context.setPropertyResolved(false);

        // Replace BEAN01
        resolver.setValue(context, null, beanName, BEAN99);
        if (canCreate || BEAN01_NAME.equals(beanName)) {
            Assert.assertTrue(context.isPropertyResolved());

            // Obtain BEAN01 again
            context.setPropertyResolved(false);
            bean = resolver.getValue(context, null, beanName);

            Assert.assertTrue(context.isPropertyResolved());
            Assert.assertEquals(BEAN99, bean);
        } else {
            Assert.assertFalse(context.isPropertyResolved());

            // Obtain BEAN01 again
            context.setPropertyResolved(false);
            bean = resolver.getValue(context, null, BEAN01_NAME);

            Assert.assertTrue(context.isPropertyResolved());
            Assert.assertEquals(BEAN01, bean);
        }
    }


    private static enum MethodUnderTest {
        GET_VALUE,
        SET_VALUE
    }
}
