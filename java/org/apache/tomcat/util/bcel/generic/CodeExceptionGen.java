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
package org.apache.tomcat.util.bcel.generic;

import org.apache.tomcat.util.bcel.classfile.CodeException;

/** 
 * This class represents an exception handler, i.e., specifies the  region where
 * a handler is active and an instruction where the actual handling is done.
 * pool as parameters. Opposed to the JVM specification the end of the handled
 * region is set to be inclusive, i.e. all instructions between start and end
 * are protected including the start and end instructions (handles) themselves.
 * The end of the region is automatically mapped to be exclusive when calling
 * getCodeException(), i.e., there is no difference semantically.
 *
 * @version $Id$
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 * @see     MethodGen
 * @see     CodeException
 * @see     InstructionHandle
 */
public final class CodeExceptionGen implements InstructionTargeter, Cloneable, java.io.Serializable {

    private InstructionHandle start_pc;
    private InstructionHandle end_pc;
    private InstructionHandle handler_pc;


    /**
     * Add an exception handler, i.e., specify region where a handler is active and an
     * instruction where the actual handling is done.
     *
     * @param start_pc Start of handled region (inclusive)
     * @param end_pc End of handled region (inclusive)
     * @param handler_pc Where handling is done
     * @param catch_type which exception is handled, null for ANY
     */
    public CodeExceptionGen(InstructionHandle start_pc, InstructionHandle end_pc,
            InstructionHandle handler_pc, ObjectType catch_type) {
        setStartPC(start_pc);
        setEndPC(end_pc);
        setHandlerPC(handler_pc);
    }


    


    /* Set start of handler 
     * @param start_pc Start of handled region (inclusive)
     */
    public void setStartPC( InstructionHandle start_pc ) {
        BranchInstruction.notifyTarget(this.start_pc, start_pc, this);
        this.start_pc = start_pc;
    }


    /* Set end of handler 
     * @param end_pc End of handled region (inclusive)
     */
    public void setEndPC( InstructionHandle end_pc ) {
        BranchInstruction.notifyTarget(this.end_pc, end_pc, this);
        this.end_pc = end_pc;
    }


    /* Set handler code
     * @param handler_pc Start of handler
     */
    public void setHandlerPC( InstructionHandle handler_pc ) {
        BranchInstruction.notifyTarget(this.handler_pc, handler_pc, this);
        this.handler_pc = handler_pc;
    }


    


    


    


    


    


    


    


    public String toString() {
        return "CodeExceptionGen(" + start_pc + ", " + end_pc + ", " + handler_pc + ")";
    }


    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println(e);
            return null;
        }
    }
}
