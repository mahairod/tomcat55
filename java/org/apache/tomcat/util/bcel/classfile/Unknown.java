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

/**
 * This class represents a reference to an unknown (i.e.,
 * application-specific) attribute of a class.  It is instantiated from the
 * <em>Attribute.readAttribute()</em> method.  Applications that need to
 * read in application-specific attributes should create an <a
 * href="./AttributeReader.html">AttributeReader</a> implementation and
 * attach it via <a
 * href="./Attribute.html#addAttributeReader(java.lang.String,
 * org.apache.tomcat.util.bcel.classfile.AttributeReader)">Attribute.addAttributeReader</a>.

 *
 * @version $Id$
 * @see org.apache.tomcat.util.bcel.classfile.Attribute
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public final class Unknown extends Attribute {

    private static final long serialVersionUID = -4152422704743201314L;


    /**
     * Construct object from file stream.
     * @param name_index Index in constant pool
     * @param length Content length in bytes
     * @param file Input stream
     * @param constant_pool Array of constants
     * @throws IOException
     */
    Unknown(int name_index, int length, DataInputStream file, ConstantPool constant_pool)
            throws IOException {
        super(name_index, length, constant_pool);
        if (length > 0) {
            byte[] bytes = new byte[length];
            file.readFully(bytes);
        }
    }
}
