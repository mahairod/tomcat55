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

import java.util.Stack;
import java.util.Hashtable;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;

/**
 * Common stuff for use with TagBegin and TagEndGenerators.
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten [hans@gefionsoftware.com] (changed all statics to
 *         regular instance vars and methods to avoid corrupt state and
 *         multi-threading issues)
 */
abstract class TagGeneratorBase extends GeneratorBase {
    private Stack tagHandlerStack;
    private Hashtable tagVarNumbers;

    class TagVariableData {
        String tagHandlerInstanceName;
        String tagEvalVarName;
        TagVariableData(String tagHandlerInstanceName, String tagEvalVarName) {
            this.tagHandlerInstanceName = tagHandlerInstanceName;
            this.tagEvalVarName = tagEvalVarName;
        }
    }

    /**
     * Sets the tag handler nesting stack for the current page.
     * Called when an instance is created.
     */
    protected void setTagHandlerStack(Stack tagHandlerStack) {
        this.tagHandlerStack = tagHandlerStack;
    }

    /**
     * Sets the tag variable number repository for the current page.
     * Called when an instance is created.
     */
    protected void setTagVarNumbers(Hashtable tagVarNumbers) {
        this.tagVarNumbers = tagVarNumbers;
    }

    protected void tagBegin(TagVariableData tvd) {
	tagHandlerStack.push(tvd);
    }

    protected TagVariableData tagEnd() {
	return (TagVariableData) tagHandlerStack.pop();
    }

    protected TagVariableData topTag() {
	if (tagHandlerStack.empty())
	    return null;
	return (TagVariableData) tagHandlerStack.peek();
    }


    private String substitute(String name, char from, String to) {
        StringBuffer s = new StringBuffer();
        int begin = 0;
        int last = name.length();
        int end;
        while (true) {
            end = name.indexOf(from, begin);
            if (end < 0)
                end = last;
            s.append(name.substring(begin, end));
            if (end == last)
                break;
            s.append(to);
            begin = end + 1;
        }
        return (s.toString());
    }

    /**
     * Return a tag variable name from the given prefix and shortTagName.
     * Not all NMTOKEN's are legal Java identifiers, since they may contain
     * '-', '.', or ':'.  We use the following mapping: substitute '-' with
     * "$1", '.' with "$2", and ':' with "$3".
     */
    protected String getTagVarName(String prefix, String shortTagName) {
        if (prefix.indexOf('-') >= 0)
            prefix = substitute(prefix, '-', "$1");
        if (prefix.indexOf('.') >= 0)
            prefix = substitute(prefix, '.', "$2");

        if (shortTagName.indexOf('-') >= 0)
            shortTagName = substitute(shortTagName, '-', "$1");
        if (shortTagName.indexOf('.') >= 0)
            shortTagName = substitute(shortTagName, '.', "$2");
        if (shortTagName.indexOf(':') >= 0)
            shortTagName = substitute(shortTagName, ':', "$3");
	// Fix: Can probably remove the synchronization now when no vars or method is static
	synchronized (tagVarNumbers) {
	    String tag = prefix+":"+shortTagName;
	    String varName = prefix+"_"+shortTagName+"_";
	    if (tagVarNumbers.get(tag) != null) {
		Integer i = (Integer) tagVarNumbers.get(tag);
		varName = varName + i.intValue();
		tagVarNumbers.put(tag, new Integer(i.intValue()+1));
		return varName;
	    } else {
		tagVarNumbers.put(tag, new Integer(1));
		return varName+"0";
	    }
	}
    }

    protected void declareVariables(ServletWriter writer, 
				    VariableInfo[] vi,
				    TagVariableInfo[] tvi,
				    TagData tagData,
				    boolean declare, boolean update, int scope)
    {
        if (vi != null) {
            for(int i = 0; i < vi.length; i++) {
                if (vi[i].getScope() == scope) {
                    if (vi[i].getDeclare() && declare) {
                        writer.println(vi[i].getClassName() +
				       " " + vi[i].getVarName() +
				       " = null;");
		    }
                    if (update) {
                        writer.println(vi[i].getVarName() + " = (" +
                                       vi[i].getClassName() +
				       ") pageContext.findAttribute(" +
                                       writer.quoteString(vi[i].getVarName())+");");
		    }
                }
	    }
	} else if (tvi != null) {
            for(int i = 0; i < tvi.length; i++) {
		String name;
		if (tvi[i].getNameGiven() != null) {
		    name = tvi[i].getNameGiven();
		} else {
		    name = tagData.getAttributeString(tvi[i].getNameFromAttribute());
		}
                if (tvi[i].getScope() == scope) {
                    if (tvi[i].getDeclare() && declare) {
                        writer.println(tvi[i].getClassName() + 
				       " " + name + " = null;");
		    }
                    if (update) {
                        writer.println(name + " = (" +
                                       tvi[i].getClassName() +
				       ") pageContext.findAttribute(" +
                                       writer.quoteString(name) + ");");
		    }
                }
	    }
	}
    }
}
