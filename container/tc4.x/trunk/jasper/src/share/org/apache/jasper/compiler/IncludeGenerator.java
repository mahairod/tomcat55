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
import java.io.File;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;

import org.xml.sax.Attributes;

/**
 * Generator for jsp:include.
 *
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Danno Ferrin
 */
public class IncludeGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    String page;
    boolean isExpression = false;
    boolean flush;
    Hashtable params;
    boolean isXml;
    
    public IncludeGenerator(Mark start, Attributes attrs, Hashtable param,
                            boolean isXml) 
        throws JasperException 
    {
	if (attrs.getLength() > 2) {
	    throw new CompileException(
                start,
		Constants.getString("jsp.error.include.tag"));
	}

        page = attrs.getValue("page");
        if (page == null) {
	    throw new CompileException(
                start,
		Constants.getString("jsp.error.include.tag"));
	}

        String flushString = attrs.getValue("flush");
	if (flushString == null && attrs.getLength() != 1) {
	    throw new CompileException(
               start,
	       Constants.getString("jsp.error.include.tag"));
	}
        if (flushString == null || flushString.equalsIgnoreCase("false")) {
            flush = false;
        } else if (flushString.equalsIgnoreCase("true")) {
            flush = true;
        } else {
            throw new CompileException(
               start,
	       Constants.getString("jsp.error.include.flush.invalid.value",
				   new Object[]{flushString}));
        }
	this.params = param;
	this.isXml = isXml;
	isExpression = JspUtil.isExpression (page, isXml);
    }
    
    public void generate(ServletWriter writer, Class phase) {
	boolean initial = true;
	String sep = "?";
	writer.println("{");
	writer.pushIndent();
	writer.println("String _jspx_qStr = \"\";");
        /*
	if (flush) {
	    writer.println("out.flush();");
	}
        */
	if (params != null && params.size() > 0) {
	    Enumeration en = params.keys();
	    while (en.hasMoreElements()) {
		String key = (String) en.nextElement();
		String []value = (String []) params.get(key);
		if (initial == true) {
		    sep = "?";
		    initial = false;
		} else sep = "&";
		
		if (value.length == 1 && JspUtil.isExpression(value[0], isXml)) {
		    writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
				   key + "=\" + " + JspUtil.getExpr(value[0], isXml) + ";");
		} else {
		    if (value.length == 1) {
			writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
				       key + "=\" + \"" + value[0] + "\";");
		    } else {
			//@@@No need for this@@@ writer.println("String [] _tmpS = new String[" + value.length +"];");
			for (int i = 0; i < value.length; i++) {
			    if (!JspUtil.isExpression(value[i], isXml))
				writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
					       key + "=\" + \"" + value[i] + "\";");
			    else
				writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
					       key + "=\" +" + JspUtil.getExpr(value[i], isXml)+ ";");
			    if (sep.equals("?")) sep = "&";
			    
			}
		    }
		}
	    }
	}
        /*
	if (!isExpression) 
	    writer.println("pageContext.include(" +
			   writer.quoteString(page) + " + _jspx_qStr);");
	else
	    writer.println ("pageContext.include(" + 
			    JspUtil.getExpr(page, isXml) + " + _jspx_qStr);");
        */
        if (!isExpression)
            writer.println("JspRuntimeLibrary.include(request, response, " +
                           writer.quoteString(page) + " + _jspx_qStr, " +
                           "out, " + flush + ");");
        else
            writer.println("JspRuntimeLibrary.include(request, response, " +
                           JspUtil.getExpr(page, isXml) + " + _jspx_qStr, " +
                           "out, " + flush + ");");

	// If there is a forward in the include chain, quit.
	writer.println("if (\"true\".equals(request.getAttribute(\"" +
		Constants.FORWARD_SEEN + "\")))");
	writer.pushIndent();
	writer.println("return;");
	writer.popIndent();

	writer.popIndent();
	writer.println("}");
    }
}
