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
package org.apache.jasper34.generator11.generators;

import org.apache.jasper34.generator11.phase.*;
import org.apache.jasper34.generator11.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.parser11.*;
import org.apache.jasper34.generator11.util.*;

import java.util.Hashtable;
import java.lang.reflect.Method;

//import org.apache.jasper.runtime.JspRuntimeLibrary;

/**
 * Generator for <jsp:getProperty.../>
 *
 * @author Mandar Raje
 */
public class GetPropertyGenerator 
    extends GeneratorBase 
    implements ServiceMethodPhase 
{
    Hashtable attrs;
    BeanRepository beanInfo;
    
    public GetPropertyGenerator (Mark start, Mark stop, Hashtable attrs,
				 BeanRepository beanInfo) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
    }
    
    public void generate (ServletWriter writer, Class phase) 
	throws JasperException	{
	    String name     = getAttribute ("name");
	    String property = getAttribute ("property");

	    // Should ideally throw exception here if the bean
	    // is not present in the pageContext.
	    
	    if (beanInfo.checkVariable(name)) {
		// Bean is defined using useBean.
                // introspect at compile time
                Class cls = beanInfo.getBeanType(name);
		String clsName = cls.getName();
                java.lang.reflect.Method meth = JspRuntimeLibrary.getReadMethod(cls, property);
                
                String methodName = meth.getName();
		writer.println("out.print(" +
			       Constants.JSP_RUNTIME_PACKAGE +
			       ".JspRuntimeLibrary.toString(" +
			       "(((" + clsName + ")pageContext.findAttribute(" +
                               "\"" + name + "\"))." + methodName + "())));");
	    } else {
                // Get the class name and then introspect at runtime.
		writer.println("out.print(" +
			       Constants.JSP_RUNTIME_PACKAGE +
			       ".JspRuntimeLibrary.toString(JspRuntimeLibrary." +
			       "handleGetProperty(pageContext.findAttribute(" +
			       "\"" + name + "\"), \"" + property + "\")));");
	    }
    }
    
    public String getAttribute(String name) {
	return (attrs != null) ? (String) attrs.get(name) : null;
    }
}

