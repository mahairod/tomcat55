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
import org.apache.jasper34.core.*;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.beans.*;

import org.apache.jasper34.runtime.JasperException;

/**
 * Generator for <jsp:setProperty .../>
 *
 * @author Mandar Raje
 */
public class SetPropertyGenerator
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    Hashtable attrs;
    BeanRepository beanInfo;
    Mark start;
    
    public SetPropertyGenerator (Mark start, Mark stop, Hashtable attrs,
				 BeanRepository beanInfo) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
	this.start = start;
    }
    
    public void generate (ServletWriter writer, Class phase) 
	throws JasperException {
	    String name     = getAttribute ("name");
	    String property = getAttribute ("property");
	    String param    = getAttribute ("param");
	    String value    = getAttribute ("value");
	    
	    if (property.equals("*")) {
		
		if (value != null) {
		    String m = Constants.getString("jsp.error.setproperty.invalidSyantx");
		    throw new CompileException(start, m);
		}
		
		// Set all the properties using name-value pairs in the request.
		writer.println(Constants.JSP_RUNTIME_PACKAGE +
			       ".JspRuntimeLibrary.introspect(pageContext.findAttribute(" +
			       "\"" + name + "\"), request);");		
		
	    } else {
		
		if (value == null) {
		    
		    // Parameter name specified. If not same as property.
		    if (param == null) param = property;
		    
		    writer.println(Constants.JSP_RUNTIME_PACKAGE +
				   ".JspRuntimeLibrary.introspecthelper(pageContext." +
				   "findAttribute(\"" + name + "\"), \"" + property +
				   "\", request.getParameter(\"" + param + "\"), " +
				   "request, \"" + param + "\", false);");
		} else {
		    
		    // value is a constant.
		    if (!JspUtil.isExpression (value)) {
			writer.println(Constants.JSP_RUNTIME_PACKAGE +
				       ".JspRuntimeLibrary.introspecthelper(pageContext." +
				       "findAttribute(\"" + name + "\"), \"" + property +
				       "\",\"" + JspUtil.escapeQueryString(value) +
				       "\",null,null, false);");
		    } else {
			
			// This requires some careful handling.
			// int, boolean, ... are not Object(s).
			writer.println(Constants.JSP_RUNTIME_PACKAGE +
				       ".JspRuntimeLibrary.handleSetProperty(pageContext." +
				       "findAttribute(\"" + name + "\"), \"" + property +
				       "\"," + JspUtil.getExpr(value) + ");");
		    }
		}
	    }
    }
    
    public String getAttribute(String name) {
	return (attrs != null) ? (String) attrs.get(name) : null;
    }
}







