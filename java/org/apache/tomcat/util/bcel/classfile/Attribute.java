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
import java.io.Serializable;

import org.apache.tomcat.util.bcel.Constants;

/**
 * Abstract super class for <em>Attribute</em> objects. Currently the
 * <em>ConstantValue</em>, <em>SourceFile</em>, <em>Code</em>,
 * <em>ExceptionTable</em>, <em>LineNumberTable</em>,
 * <em>LocalVariableTable</em>, <em>InnerClasses</em> and
 * <em>Synthetic</em> attributes are supported. The <em>Unknown</em>
 * attribute stands for non-standard-attributes.
 *
 * @author <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public abstract class Attribute implements Serializable
{
    private static final long serialVersionUID = 1514136303496688899L;

    protected int name_index; // Points to attribute name in constant pool

    protected int length; // Content length of attribute field

    protected ConstantPool constant_pool;

    protected Attribute(int name_index, int length,
            ConstantPool constant_pool)
    {
        this.name_index = name_index;
        this.length = length;
        this.constant_pool = constant_pool;
    }

    /*
     * Class method reads one attribute from the input data stream. This method
     * must not be accessible from the outside. It is called by the Field and
     * Method constructor methods.
     *
     * @see Field
     * @see Method @param file Input stream @param constant_pool Array of
     *      constants @return Attribute @throws IOException @throws
     *      ClassFormatException
     */
    public static final Attribute readAttribute(DataInputStream file,
            ConstantPool constant_pool) throws IOException,
            ClassFormatException
    {
        ConstantUtf8 c;
        String name;
        int name_index;
        int length;
        byte tag = Constants.ATTR_UNKNOWN; // Unknown attribute
        // Get class name from constant pool via `name_index' indirection
        name_index = file.readUnsignedShort();
        c = (ConstantUtf8) constant_pool.getConstant(name_index,
                Constants.CONSTANT_Utf8);
        name = c.getBytes();
        // Length of data in bytes
        length = file.readInt();
        // Compare strings to find known attribute
        // System.out.println(name);
        for (byte i = 0; i < Constants.KNOWN_ATTRIBUTES; i++)
        {
            if (name.equals(Constants.ATTRIBUTE_NAMES[i]))
            {
                tag = i; // found!
                break;
            }
        }
        // Call proper constructor, depending on `tag'
        switch (tag)
        {
        case Constants.ATTR_UNKNOWN:
            Utility.swallowUnknownAttribute(file, length);
            return null;
        case Constants.ATTR_CONSTANT_VALUE:
            Utility.swallowConstantValue(file);
            return null;
        case Constants.ATTR_SOURCE_FILE:
            Utility.swallowSourceFile(file);
            return null;
        case Constants.ATTR_CODE:
            Utility.swallowCode(file, constant_pool);
            return null;
        case Constants.ATTR_EXCEPTIONS:
            Utility.swallowExceptionTable(file);
            return null;
        case Constants.ATTR_LINE_NUMBER_TABLE:
            Utility.swallowLineNumberTable(file);
            return null;
        case Constants.ATTR_LOCAL_VARIABLE_TABLE:
            Utility.swallowLocalVariableTable(file);
            return null;
        case Constants.ATTR_INNER_CLASSES:
            Utility.swallowInnerClasses(file);
            return null;
        case Constants.ATTR_SYNTHETIC:
            Utility.swallowSynthetic(length);
            return null;
        case Constants.ATTR_DEPRECATED:
            Utility.swallowDeprecated(length);
            return null;
        case Constants.ATTR_PMG:
            Utility.swallowPMCClass(file);
            return null;
        case Constants.ATTR_SIGNATURE:
            Utility.swallowSignature(file);
            return null;
        case Constants.ATTR_STACK_MAP:
            Utility.swallowStackMap(file);
            return null;
        case Constants.ATTR_RUNTIME_VISIBLE_ANNOTATIONS:
            return new RuntimeVisibleAnnotations(name_index, length, file,
                    constant_pool);
        case Constants.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS:
            Utility.swallowAnnotations(file, constant_pool);
            return null;
        case Constants.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
            return new RuntimeVisibleParameterAnnotations(name_index, length,
                    file, constant_pool);
        case Constants.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
            Utility.swallowAnnotations(file, constant_pool);
            return null;
        case Constants.ATTR_ANNOTATION_DEFAULT:
            return new AnnotationDefault(name_index, length, file,
                    constant_pool);
        case Constants.ATTR_LOCAL_VARIABLE_TYPE_TABLE:
            Utility.swallowLocalVariableTypeTable(file);
            return null;
        case Constants.ATTR_ENCLOSING_METHOD:
            Utility.swallowEnclosingMethod(file);
            return null;
        case Constants.ATTR_STACK_MAP_TABLE:
            Utility.swallowStackMapTable(file);
            return null;
        case Constants.ATTR_BOOTSTRAP_METHODS:
            Utility.swallowBootstrapMethods(file);
            return null;
        case Constants.ATTR_METHOD_PARAMETERS:
            Utility.swallowMethodParameters(file);
            return null;
        default: // Never reached
            throw new IllegalStateException("Unrecognized attribute type tag parsed: " + tag);
        }
    }

    /**
     * @return Name of attribute
     */
    public String getName()
    {
        ConstantUtf8 c = (ConstantUtf8) constant_pool.getConstant(name_index,
                Constants.CONSTANT_Utf8);
        return c.getBytes();
    }
}
