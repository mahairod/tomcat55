/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.util.bcel.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * This attribute exists for local or
 * anonymous classes and ... there can be only one.
 */
public class EnclosingMethod extends Attribute {

    private static final long serialVersionUID = 6755214228300933233L;

    // Ctors - and code to read an attribute in.
    public EnclosingMethod(int nameIndex, int len, DataInputStream dis,
            ConstantPool cpool) throws IOException {
        super(nameIndex, len, cpool);
        // Unused class index
        dis.readUnsignedShort();
        // Unused method index
        dis.readUnsignedShort();
    }

    @Override
    public Attribute copy(ConstantPool constant_pool) {
        throw new RuntimeException("Not implemented yet!");
    }
}
