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
/* Generated By:JJTree: Do not edit this line. AstString.java */

package org.apache.el.parser;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$Date$$Author$
 */
public final class AstString extends SimpleNode {
    public AstString(int id) {
        super(id);
    }

    private String string;

    public String getString() {
        if (this.string == null) {
            this.string = this.image.substring(1, this.image.length() - 1);
        }
        return this.string;
    }

    @Override
    public Class<?> getType(EvaluationContext ctx)
            throws ELException {
        return String.class;
    }

    @Override
    public Object getValue(EvaluationContext ctx)
            throws ELException {
        return this.getString();
    }

    @Override
    public void setImage(String image) {
        if (image.indexOf('\\') == -1) {
            this.image = image;
            return;
        }
        int size = image.length();
        StringBuilder buf = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            char c = image.charAt(i);
            if (c == '\\' && i + 1 < size) {
                char c1 = image.charAt(i + 1);
                if (c1 == '\\' || c1 == '"' || c1 == '\'' || c1 == '#'
                        || c1 == '$') {
                    c = c1;
                    i++;
                }
            }
            buf.append(c);
        }
        this.image = buf.toString();
    }
}
