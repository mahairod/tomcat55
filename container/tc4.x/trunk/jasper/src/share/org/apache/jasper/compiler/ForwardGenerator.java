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
import java.net.URLEncoder;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;

import org.xml.sax.Attributes;

/**
 * Generator for <jsp:forward>
 *
 * @author Anil K. Vijendran
 * @author Danno Ferrin
 */
public class ForwardGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    String page;
    boolean isExpression = false;
    Hashtable params;
    boolean isXml;
    
    public ForwardGenerator(Mark start, Attributes attrs, Hashtable param,
                            boolean isXml)
	throws JasperException {
	    if (attrs.getLength() != 1)
		throw new JasperException(Constants.getString("jsp.error.invalid.forward"));
	    
	    page = attrs.getValue("page");
	    if (page == null)
		throw new CompileException(start,
					   Constants.getString("jsp.error.invalid.forward"));
	    
	    this.params = param;
            this.isXml = isXml;
	    isExpression = JspUtil.isExpression (page, isXml);
    }
    
    public void generate(ServletWriter writer, Class phase) {
	char sep = '?';	
        writer.println("if (true) {");
        writer.pushIndent();
        writer.println("out.clear();");
	writer.println("String _jspx_qfStr = \"\";");
	
	if (params != null && params.size() > 0) {
	    Enumeration en = params.keys();
	    while (en.hasMoreElements()) {
		String key = (String) en.nextElement();
		String []value = (String []) params.get(key);
		
		for (int i = 0; i < value.length; i++) {
		    String v;
		    if (JspUtil.isExpression(value[i], isXml))
			v = JspUtil.getExpr(value[i], isXml);
		    else
			v = "\"" + URLEncoder.encode(value[i]) + "\"";
		    writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
			       key + "=\" +" + v + ";");
		    sep = '&';			    
		}
	    }
	}
	if (!isExpression)
            writer.println("pageContext.forward(" +
			   writer.quoteString(page) + " +  _jspx_qfStr);");
	else
            writer.println("pageContext.forward(" +
			   JspUtil.getExpr (page, isXml) +  " +  _jspx_qfStr);");
	
        writer.println("return;");
        writer.popIndent();
        writer.println("}");
    }
}
