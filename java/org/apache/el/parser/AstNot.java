/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Generated By:JJTree: Do not edit this line. AstNot.java */

package org.apache.el.parser;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Id$
 */
public final class AstNot extends SimpleNode {
    public AstNot(int id) {
        super(id);
    }

    @Override
    public Class<?> getType(EvaluationContext ctx)
            throws ELException {
        return Boolean.class;
    }

    @Override
    public Object getValue(EvaluationContext ctx)
            throws ELException {
        Object obj = this.children[0].getValue(ctx);
        Boolean b = coerceToBoolean(obj, true);
        return Boolean.valueOf(!b.booleanValue());
    }
}
