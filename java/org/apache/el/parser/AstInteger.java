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
/* Generated By:JJTree: Do not edit this line. AstInteger.java */

package org.apache.el.parser;

import java.math.BigInteger;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 */
public final class AstInteger extends SimpleNode {
    public AstInteger(int id) {
        super(id);
    }

    private volatile Number number;

    protected Number getInteger() {
        if (this.number == null) {
            try {
                this.number = Long.valueOf(this.image);
            } catch (ArithmeticException e1) {
                this.number = new BigInteger(this.image);
            }
        }
        return number;
    }

    @Override
    public Class<?> getType(EvaluationContext ctx)
            throws ELException {
        return this.getInteger().getClass();
    }

    @Override
    public Object getValue(EvaluationContext ctx)
            throws ELException {
        return this.getInteger();
    }
}
