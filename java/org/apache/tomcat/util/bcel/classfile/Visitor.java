/*
 * Copyright  2000-2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

/**
 * Interface to make use of the Visitor pattern programming style. I.e. a class
 * that implements this interface can traverse the contents of a Java class just
 * by calling the `accept' method which all classes have.
 * 
 * @version $Id$
 * @author <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public interface Visitor
{
	public void visitCode(Code obj);

	

	public void visitConstantClass(ConstantClass obj);

	public void visitConstantDouble(ConstantDouble obj);

	public void visitConstantFieldref(ConstantFieldref obj);

	public void visitConstantFloat(ConstantFloat obj);

	public void visitConstantInteger(ConstantInteger obj);

	public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj);

	public void visitConstantLong(ConstantLong obj);

	public void visitConstantMethodref(ConstantMethodref obj);

	public void visitConstantNameAndType(ConstantNameAndType obj);

	

	public void visitConstantString(ConstantString obj);

	public void visitConstantUtf8(ConstantUtf8 obj);

	public void visitConstantValue(ConstantValue obj);

	public void visitDeprecated(Deprecated obj);

	public void visitExceptionTable(ExceptionTable obj);

	

	

	public void visitInnerClasses(InnerClasses obj);

	

	

	public void visitLineNumberTable(LineNumberTable obj);

	

	public void visitLocalVariableTable(LocalVariableTable obj);

	

	public void visitSignature(Signature obj);

	public void visitSourceFile(SourceFile obj);

	public void visitSynthetic(Synthetic obj);

	public void visitUnknown(Unknown obj);

	public void visitStackMap(StackMap obj);

	

	public void visitStackMapTable(StackMapTable obj);

	

	public void visitAnnotation(Annotations obj);

	

	

	

	public void visitLocalVariableTypeTable(LocalVariableTypeTable obj);

	public void visitEnclosingMethod(EnclosingMethod obj);
}
