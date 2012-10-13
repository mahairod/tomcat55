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
 *
 */
package org.apache.tomcat.util.bcel.classfile;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.tomcat.util.bcel.util.BCELComparator;

/**
 * This class represents the method info structure, i.e., the representation
 * for a method in the class. See JVM specification for details.
 * A method has access flags, a name, a signature and a number of attributes.
 *
 * @version $Id$
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public final class Method extends FieldOrMethod {

    private static final long serialVersionUID = -7447828891136739513L;
    private static BCELComparator _cmp = new BCELComparator() {

        @Override
        public boolean equals( Object o1, Object o2 ) {
            Method THIS = (Method) o1;
            Method THAT = (Method) o2;
            return THIS.getName().equals(THAT.getName())
                    && THIS.getSignature().equals(THAT.getSignature());
        }


        @Override
        public int hashCode( Object o ) {
            Method THIS = (Method) o;
            return THIS.getSignature().hashCode() ^ THIS.getName().hashCode();
        }
    };


    /**
     * Empty constructor, all attributes have to be defined via `setXXX'
     * methods. Use at your own risk.
     */
    public Method() {
    }


    /**
     * Construct object from file stream.
     * @param file Input stream
     * @throws IOException
     * @throws ClassFormatException
     */
    Method(DataInputStream file, ConstantPool constant_pool) throws IOException,
            ClassFormatException {
        super(file, constant_pool);
    }


    /**
     * Return value as defined by given BCELComparator strategy.
     * By default two method objects are said to be equal when
     * their names and signatures are equal.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        return _cmp.equals(this, obj);
    }


    /**
     * Return value as defined by given BCELComparator strategy.
     * By default return the hashcode of the method's name XOR signature.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return _cmp.hashCode(this);
    }
}
