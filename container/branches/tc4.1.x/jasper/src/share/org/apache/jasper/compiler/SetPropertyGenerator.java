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
import org.apache.jasper.JasperException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.beans.*;

import org.apache.jasper.Constants;

import org.xml.sax.Attributes;

/**
 * Generator for jsp:setProperty.
 *
 * @author Mandar Raje
 * @author Danno Ferrin
 */
public class SetPropertyGenerator
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    Attributes attrs;
    BeanRepository beanInfo;
    Mark start;
    boolean isXml;
    
    public SetPropertyGenerator (Mark start, Mark stop, Attributes attrs,
				 BeanRepository beanInfo, boolean isXml) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
	this.start = start;
        this.isXml = isXml;
    }
    
    public void generate (ServletWriter writer, Class phase) 
	throws JasperException {
	    String name     = getAttribute ("name");
	    String property = getAttribute ("property");
	    String param    = getAttribute ("param");
	    String value    = getAttribute ("value");
	    
	    if (property.equals("*")) {
		
		if (value != null) {
		    String m = Constants.getString("jsp.error.setproperty.invalidSyntax");
		    throw new CompileException(start, m);
		}
		
		// Set all the properties using name-value pairs in the request.
		writer.println("JspRuntimeLibrary.introspect(pageContext.findAttribute(" +
			       "\"" + name + "\"), request);");		
		
	    } else {
		
		if (value == null) {
		    
		    // Parameter name specified. If not same as property.
		    if (param == null) param = property;
		    
		    writer.println("JspRuntimeLibrary.introspecthelper(pageContext." +
				   "findAttribute(\"" + name + "\"), \"" + property +
				   "\", request.getParameter(\"" + param + "\"), " +
				   "request, \"" + param + "\", false);");
		} else {
		    
		    // value is a constant.
		    if (!JspUtil.isExpression (value, isXml)) {
			writer.println("JspRuntimeLibrary.introspecthelper(pageContext." +
				       "findAttribute(\"" + name + "\"), \"" + property +
				       "\",\"" + JspUtil.escapeQueryString(value) +
				       "\",null,null, false);");
		    } else {
			
			// This requires some careful handling.
			// int, boolean, ... are not Object(s).
			writer.println("JspRuntimeLibrary.handleSetProperty(pageContext." +
				       "findAttribute(\"" + name + "\"), \"" + property +
				       "\"," + JspUtil.getExpr(value, isXml) + ");");
		    }
		}
	    }
    }
    
    public String getAttribute(String name) {
	return (attrs != null) ? attrs.getValue(name) : null;
    }
}







