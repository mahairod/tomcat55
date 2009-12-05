/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     initial implementation 
 * ******************************************************************/
package org.apache.tomcat.util.bcel.classfile;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.tomcat.util.bcel.Constants;

/**
 * This attribute exists for local or 
 * anonymous classes and ... there can be only one.
 */
public class EnclosingMethod extends Attribute {
	
	// Pointer to the CONSTANT_Class_info structure representing the 
	// innermost class that encloses the declaration of the current class.
	private int classIndex;
	
	// If the current class is not immediately enclosed by a method or 
	// constructor, then the value of the method_index item must be zero.  
	// Otherwise, the value of the  method_index item must point to a 
	// CONSTANT_NameAndType_info structure representing the name and the 
	// type of a method in the class referenced by the class we point 
	// to in the class_index.  *It is the compiler responsibility* to 
	// ensure that the method identified by this index is the closest 
	// lexically enclosing method that includes the local/anonymous class.
	private int methodIndex;

	// Ctors - and code to read an attribute in.
	public EnclosingMethod(int nameIndex, int len, DataInput dis, ConstantPool cpool) throws IOException {
		this(nameIndex, len, dis.readUnsignedShort(), dis.readUnsignedShort(), cpool);
	}

	private EnclosingMethod(int nameIndex, int len, int classIdx,int methodIdx, ConstantPool cpool) {
	    super(Constants.ATTR_ENCLOSING_METHOD, nameIndex, len, cpool);
	    classIndex  = classIdx;
	    methodIndex = methodIdx;
	}

	public void accept(Visitor v) {
	  v.visitEnclosingMethod(this);
	}

	public Attribute copy(ConstantPool constant_pool) {
		throw new RuntimeException("Not implemented yet!");
		// is this next line sufficient?
		// return (EnclosingMethod)clone();
	}
	
	  
	
	
	
	

	
	
	

    public final void dump(DataOutputStream file) throws IOException {
	    super.dump(file);
	    file.writeShort(classIndex);
	    file.writeShort(methodIndex);
    }    
}
