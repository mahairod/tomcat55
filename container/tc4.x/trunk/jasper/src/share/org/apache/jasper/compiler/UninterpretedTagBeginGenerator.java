/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.jasper.compiler;

import java.util.Hashtable;
import java.util.Enumeration;

import org.xml.sax.Attributes;

/**
 * Generates the start element of an uninterpreted tag.
 *
 * @author Pierre Delisle
 * @author Danno Ferrin
 */
public class UninterpretedTagBeginGenerator
    extends GeneratorBase
    implements ServiceMethodPhase
{
    private static final String singleQuote = "'";
    private static final String doubleQuote = "\\\"";

    private String tag;
    private Attributes attrs;

    public UninterpretedTagBeginGenerator(String tag, Attributes attrs) {
	this.tag = tag;
	this.attrs = attrs;
    }

    public void generate(ServletWriter writer, Class phase) {
	writer.indent();
    	writer.print("out.write(\"");

	StringBuffer sb = new StringBuffer();
        sb.append("<").append(tag);
        if (attrs == null) {
            sb.append(">");
        } else {
            int attrsLength = attrs.getLength();
            for (int i = 0; i < attrsLength; i++) {
		String quote = doubleQuote;
                String name = attrs.getQName(i);
                String value = attrs.getValue(i);
		if (value.indexOf('"') != -1) quote = singleQuote;
                sb.append(" ").append(name).append("=").append(quote);
		sb.append(value).append(quote);
            }
            sb.append(">");
        }
	writer.print(sb.toString());
        writer.print("\");");
        writer.println();
    }
}
