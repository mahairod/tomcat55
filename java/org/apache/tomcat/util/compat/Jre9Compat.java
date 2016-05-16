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
package org.apache.tomcat.util.compat;

class Jre9Compat extends JreCompat {

    private static final Class<?> inaccessibleObjectExceptionClazz;


    static {
        Class<?> c1 = null;
        try {
            c1 = Class.forName("java.lang.reflect.InaccessibleObjectException");
        } catch (SecurityException e) {
            // Should never happen
        } catch (ClassNotFoundException e) {
            // Must be Java 8
        }
        inaccessibleObjectExceptionClazz = c1;
    }


    static boolean isSupported() {
        return inaccessibleObjectExceptionClazz != null;
    }


    @Override
    public boolean isInstanceOfInaccessibleObjectException(Exception e) {
        if (e == null) {
            return false;
        }

        return inaccessibleObjectExceptionClazz.isAssignableFrom(e.getClass());
    }
}
