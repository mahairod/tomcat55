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

import java.beans.FeatureDescriptor;
import java.util.Iterator;

/**
 * @since EL 3.0
 */
public class BeanNameELResolver extends ELResolver {

    private final BeanNameResolver beanNameResolver;

    public BeanNameELResolver(BeanNameResolver beanNameResolver) {
        this.beanNameResolver = beanNameResolver;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException {

        if (context == null) {
            throw new NullPointerException();
        }
        if (base != null || !(property instanceof String)) {
            return null;
        }

        String beanName = (String) property;

        if (beanNameResolver.isNameResolved(beanName)) {
            context.setPropertyResolved(true);
            return beanNameResolver.getBean((String) property);
        }

        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property,
            Object value) throws NullPointerException,
            PropertyNotFoundException, PropertyNotWritableException,
            ELException {

        if (context == null) {
            throw new NullPointerException();
        }
        if (base != null || !(property instanceof String)) {
            return;
        }

        String beanName = (String) property;

        if (beanNameResolver.isNameResolved(beanName) ||
                beanNameResolver.canCreateBean(beanName)) {
            context.setPropertyResolved(true);
            beanNameResolver.setBeanValue(beanName, value);
        }
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException {

        if (context == null) {
            throw new NullPointerException();
        }
        if (base != null || !(property instanceof String)) {
            return null;
        }

        String beanName = (String) property;

        if (beanNameResolver.isNameResolved(beanName)) {
            context.setPropertyResolved(true);
            beanNameResolver.getBean(beanName).getClass();
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException {


        if (context == null) {
            throw new NullPointerException();
        }
        if (base != null || !(property instanceof String)) {
            // Return value undefined
            return false;
        }

        String beanName = (String) property;

        if (beanNameResolver.isNameResolved(beanName)) {
            context.setPropertyResolved(true);
            return beanNameResolver.isReadOnly(beanName);
        }

        // Return value undefined
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
            Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return String.class;
    }
}
