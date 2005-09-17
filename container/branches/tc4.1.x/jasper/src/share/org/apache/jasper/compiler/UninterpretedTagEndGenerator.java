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

/**
 * Generates the start element of an uninterpreted tag.
 *
 * @author Pierre Delisle
 */
public class UninterpretedTagEndGenerator
    extends GeneratorBase
    implements ServiceMethodPhase
{
    private String tag;

    public UninterpretedTagEndGenerator(String tag) {
	this.tag = tag;
    }

    public void generate(ServletWriter writer, Class phase) {
	StringBuffer sb = new StringBuffer();
	writer.indent();
    	writer.print("out.write(\"");
        sb.append("</").append(tag).append(">");
	writer.print(sb.toString());
        writer.print("\");");
        writer.println();
    }
}
