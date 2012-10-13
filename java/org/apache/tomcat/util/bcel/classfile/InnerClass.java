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

import java.io.DataInput;
import java.io.IOException;
import java.io.Serializable;

/**
 * This class represents a inner class attribute, i.e., the class
 * indices of the inner and outer classes, the name and the attributes
 * of the inner class.
 *
 * @version $Id$
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 * @see InnerClasses
 */
public final class InnerClass implements Cloneable, Serializable {

    private static final long serialVersionUID = -4964694103982806087L;

    /**
     * Construct object from file stream.
     * @param file Input stream
     * @throws IOException
     */
    InnerClass(DataInput file) throws IOException {
        this(file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), file
                .readUnsignedShort());
    }


    public InnerClass(int inner_class_index, int outer_class_index, int inner_name_index,
            int inner_access_flags) {
    }


    /**
     * @return deep copy of this object
     */
    public InnerClass copy() {
        try {
            return (InnerClass) clone();
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }
}
