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

import org.apache.tomcat.util.bcel.Constants;
import org.apache.tomcat.util.bcel.ExceptionConstants;

/** 
 * INVOKESPECIAL - Invoke instance method; special handling for superclass, private
 * and instance initialization method invocations
 *
 * <PRE>Stack: ..., objectref, [arg1, [arg2 ...]] -&gt; ...</PRE>
 *
 * @version $Id$
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public class INVOKESPECIAL extends InvokeInstruction {

    /**
     * Empty constructor needed for the Class.newInstance() statement in
     * Instruction.readInstruction(). Not to be used otherwise.
     */
    INVOKESPECIAL() {
    }


    public INVOKESPECIAL(int index) {
        super(Constants.INVOKESPECIAL, index);
    }


    public Class[] getExceptions() {
        Class[] cs = new Class[4 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
        System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0, cs, 0,
                ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
        cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 3] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
        cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 2] = ExceptionConstants.ABSTRACT_METHOD_ERROR;
        cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length + 1] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
        cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] = ExceptionConstants.NULL_POINTER_EXCEPTION;
        return cs;
    }
}
