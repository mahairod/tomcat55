/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

package org.apache.jasper34.generator;

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.JasperException;
import org.apache.jasper34.parser.*;
import org.apache.jasper34.jsptree.*;

/**
 * Generator for <jsp:forward>
 *
 * @author Anil K. Vijendran
 */
public class ForwardGenerator extends GeneratorBase
{
    String page;
    boolean isExpression = false;
    Hashtable params;
    
    public ForwardGenerator(Mark start, Hashtable attrs, Hashtable param)
	throws JasperException {
	    if (attrs.size() != 1)
		throw new JasperException(Constants.getString("jsp.error.invalid.forward"));
	    
	    page = (String) attrs.get("page");
	    if (page == null)
		throw new CompileException(start,
					   Constants.getString("jsp.error.invalid.forward"));
	    
	    this.params = param;
	    isExpression = JspUtil.isExpression (page);
    }
    
    public void generateServiceMethod(ServletWriter writer) {
	boolean initial = true;
	String sep = "?";	
        writer.println("if (true) {");
        writer.pushIndent();
        writer.println("out.clear();");
	writer.println("String _jspx_qfStr = \"\";");
	
	if (params.size() > 0) {
	    Enumeration en = params.keys();
	    while (en.hasMoreElements()) {
		String key = (String) en.nextElement();
		String []value = (String []) params.get(key);
		if (initial == true) {
		    sep = "?";
		    initial = false;
		} else sep = "&";
		
		if (value.length == 1 && JspUtil.isExpression(value[0]))
		    writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
				   key + "=\" + " + JspUtil.getExpr(value[0]) + ";");
		else {
		    if (value.length == 1)
			writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
				       key + "=\" + \"" + value[0] + "\";");			
		    else {
			for (int i = 0; i < value.length; i++) {
			    if (!JspUtil.isExpression(value[i]))
				writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
					       key + "=\" + \"" + value[i] + "\";");
			    else
				writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
					       key + "=\" +" + JspUtil.getExpr(value[i])+ ";");
			    if (sep.equals("?")) sep = "&";			    
			}
		    }
		}
	    }
	}
	if (!isExpression)
            writer.println("pageContext.forward(" +
			   writer.quoteString(page) + " +  _jspx_qfStr);");
	else
            writer.println("pageContext.forward(" +
			   JspUtil.getExpr (page) +  " +  _jspx_qfStr);");
	
        writer.println("return;");
        writer.popIndent();
        writer.println("}");
    }
}
