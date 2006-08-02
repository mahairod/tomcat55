/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache;

import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 1.17 $
 */
public interface AnnotationProcessor {
    public void postConstruct(Object instance)
        throws IllegalAccessException, InvocationTargetException;
    public void preDestroy(Object instance)
        throws IllegalAccessException, InvocationTargetException;
    public void processAnnotations(Object instance)
        throws IllegalAccessException, InvocationTargetException, NamingException;
}
