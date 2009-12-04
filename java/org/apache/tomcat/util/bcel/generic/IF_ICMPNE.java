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

/** 
 * IF_ICMPNE - Branch if int comparison doesn't succeed
 *
 * <PRE>Stack: ..., value1, value2 -&gt; ...</PRE>
 *
 * @version $Id$
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public class IF_ICMPNE extends IfInstruction {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    IF_ICMPNE() {
    }


    public IF_ICMPNE(InstructionHandle target) {
        super(org.apache.tomcat.util.bcel.Constants.IF_ICMPNE, target);
    }


    /**
     * @return negation of instruction
     */
    public IfInstruction negate() {
        return new IF_ICMPEQ(target);
    }
}
