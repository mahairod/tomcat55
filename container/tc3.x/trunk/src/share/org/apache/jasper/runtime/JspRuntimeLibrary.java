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


package org.apache.jasper.runtime;

import java.io.IOException;
import java.util.Enumeration;

import java.lang.reflect.Method;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;


/**
 * Bunch of util methods that are used by code generated for useBean,
 * getProperty and setProperty.  
 *
 * The __begin, __end stuff is there so that the JSP engine can
 * actually parse this file and inline them if people don't want
 * runtime dependencies on this class. However, I'm not sure if that
 * works so well right now. It got forgotten at some point. -akv
 *
 * @author Mandar Raje
 */
public class JspRuntimeLibrary {

 
   // __begin convertMethod
    public static Object convert(String s, Class t) throws JasperException {
        try {
            if (s == null) {
                if (t.equals(Boolean.class) || t.equals(Boolean.TYPE))
                    s = "false";
                else
                    return null;
            }
    
            if ( t.equals(Boolean.class) || t.equals(Boolean.TYPE) ) {
                if (s.equalsIgnoreCase("on") || s.equalsIgnoreCase("true"))
                    s = "true";
                else
                    s = "false";
                return new Boolean(s);
            } else if ( t.equals(Byte.class) || t.equals(Byte.TYPE) ) {
                return new Byte(s);
            } else if (t.equals(Character.class) || t.equals(Character.TYPE)) {
                return s.length() > 0 ? new Character(s.charAt(0)) : null;
            } else if ( t.equals(Short.class) || t.equals(Short.TYPE) ) {
                return new Short(s);
            } else if ( t.equals(Integer.class) || t.equals(Integer.TYPE) ) {
                return new Integer(s);
            } else if ( t.equals(Float.class) || t.equals(Float.TYPE) ) {
                return new Float(s);
            } else if ( t.equals(Long.class) || t.equals(Long.TYPE) ) {
                return new Long(s);
            } else if ( t.equals(Double.class) || t.equals(Double.TYPE) ) {
                return new Double(s);
            } else if ( t.equals(String.class) ) {
                return s;
            } else if ( t.equals(java.io.File.class) ) {
                return new java.io.File(s);
            }
        } catch (Exception ex) {
	    throw new JasperException (ex);
        }
        return null;
    }
    // __end convertMethod

    // __begin introspectMethod
    public static void introspect(Object bean, ServletRequest request)
                                  throws JasperException
    {
	Enumeration e = request.getParameterNames();
	while ( e.hasMoreElements() ) {
	    String name  = (String) e.nextElement();
	    String value = request.getParameter(name);
	    if (value == null || value.equals(""))
		continue;
	    introspecthelper(bean, name, value, request, name, true);
	}
    }
    // __end introspectMethod
    
    // __begin introspecthelperMethod
    public static void introspecthelper(Object bean, String prop,
					String value, ServletRequest request,
					String param, boolean ignoreMethodNF) 
					throws JasperException
    {
        java.lang.reflect.Method method = null;
        Class                    type   = null;
	try {
	    java.beans.BeanInfo info
		= java.beans.Introspector.getBeanInfo(bean.getClass());
	    if ( info != null ) {
		java.beans.PropertyDescriptor pd[]
		    = info.getPropertyDescriptors();
		for (int i = 0 ; i < pd.length ; i++) {
		    if ( pd[i].getName().equals(prop) ) {
			method = pd[i].getWriteMethod();
			type   = pd[i].getPropertyType();
			break;
		    }
		}
	    }
	    if ( method != null ) {
		if (type.isArray()) {
                    if (request == null) {
                        throw new JasperException(Constants.getString(
                                "jsp.error.beans.setproperty.noindexset",
                                new Object[] {}));
                    };
		    Class t = type.getComponentType();
		    String[] values = request.getParameterValues(param);
		    //XXX Please check.
		    if(values == null) return;
		    if(t.equals(String.class)) {
			method.invoke(bean, new Object[] { values });
		    } else {
			Object tmpval = null;
			createTypedArray (bean, method, values, t); 
		    }
		} else {
		    //XXX please check.
		    if(value == null || value.equals("")) return;
		    Object oval = convert(value, type);
		    if ( oval != null )
			method.invoke(bean, new Object[] { oval });
		}
	    }
	} catch (Exception ex) {
	    throw new JasperException (ex);
	}
        if (!ignoreMethodNF && (method == null)) {
            if (type == null) {
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.noproperty",
                        new Object[] {prop, bean.getClass().getName()}));
            } else {
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.nomethod.setproperty",
                        new Object[] {prop, bean.getClass().getName()}));
            }
        }
    }
    // __end introspecthelperMethod
    
    //-------------------------------------------------------------------
    // functions to convert builtin Java data types to string.
    //-------------------------------------------------------------------
    // __begin toStringMethod
    public static String toString(Object o) {
        return (o == null) ? "" : o.toString();
    }

    public static String toString(byte b) {
        return new Byte(b).toString();
    }

    public static String toString(boolean b) {
        return new Boolean(b).toString();
    }

    public static String toString(short s) {
        return new Short(s).toString();
    }

    public static String toString(int i) {
        return new Integer(i).toString();
    }

    public static String toString(float f) {
        return new Float(f).toString();
    }

    public static String toString(long l) {
        return new Long(l).toString();
    }

    public static String toString(double d) {
        return new Double(d).toString();
    }

    public static String toString(char c) {
        return new Character(c).toString();
    }
    // __end toStringMethod


    /**
     * Create a typed array.
     * This is a special case where params are passed through
     * the request and the property is indexed.
     */
    public static void createTypedArray (Object bean, Method method, String []values, Class t)
    throws JasperException {
	try {
	    if (t.equals(Integer.class)) {
		Integer []tmpval = new Integer[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] =  new Integer (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Byte.class)) {
		Byte[] tmpval = new Byte[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Byte (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Boolean.class)) {
		Boolean[] tmpval = new Boolean[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Boolean (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Short.class)) {
		Short[] tmpval = new Short[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Short (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Long.class)) {
		Long[] tmpval = new Long[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Long (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Double.class)) {
		Double[] tmpval = new Double[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Double (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Float.class)) {
		Float[] tmpval = new Float[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Float (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(Character.class)) {
		Character[] tmpval = new Character[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = new Character(values[i].charAt(0));
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(int.class)) {
		int []tmpval = new int[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Integer.parseInt (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(byte.class)) {
		byte[] tmpval = new byte[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Byte.parseByte (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(boolean.class)) {
		boolean[] tmpval = new boolean[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Boolean.getBoolean (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(short.class)) {
		short[] tmpval = new short[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Short.parseShort (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(long.class)) {
		long[] tmpval = new long[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Long.parseLong (values[i]);
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(double.class)) {
		double[] tmpval = new double[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Double.valueOf(values[i]).doubleValue();
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(float.class)) {
		float[] tmpval = new float[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = Float.valueOf(values[i]).floatValue();
		method.invoke (bean, new Object[] {tmpval});
	    } else if (t.equals(char.class)) {
		char[] tmpval = new char[values.length];
		for (int i = 0 ; i < values.length; i++)
		    tmpval[i] = values[i].charAt(0);
		method.invoke (bean, new Object[] {tmpval});
	    }
	} catch (Exception ex) {
	    throw new JasperException ("error in invoking method");
	}

	
    }

    /**
     * Escape special shell characters.
     * @param unescString The string to shell-escape
     * @return The escaped shell string.
     */

    public static String escapeQueryString(String unescString) {
    if ( unescString == null )
        return null;
   
    String escString    = "";
    String shellSpChars = "&;`'\"|*?~<>^()[]{}$\\\n";
   
    for(int index=0; index<unescString.length(); index++) {
        char nextChar = unescString.charAt(index);

        if( shellSpChars.indexOf(nextChar) != -1 )
        escString += "\\";

        escString += nextChar;
    }
    return escString;
    }

    /**
     * Decode an URL formatted string.
     * @param s The string to decode.
     * @return The decoded string.
     */

    public static String decode(String encoded) {
        // speedily leave if we're not needed
    if (encoded == null) return null;
        if (encoded.indexOf('%') == -1 && encoded.indexOf('+') == -1)
        return encoded;

    //allocate the buffer - use byte[] to avoid calls to new.
        byte holdbuffer[] = new byte[encoded.length()];

        char holdchar;
        int bufcount = 0;

        for (int count = 0; count < encoded.length(); count++) {
        char cur = encoded.charAt(count);
            if (cur == '%') {
            holdbuffer[bufcount++] =
          (byte)Integer.parseInt(encoded.substring(count+1,count+3),16);
                if (count + 2 >= encoded.length())
                    count = encoded.length();
                else
                    count += 2;
            } else if (cur == '+') {
        holdbuffer[bufcount++] = (byte) ' ';
        } else {
            holdbuffer[bufcount++] = (byte) cur;
            }
        }
	// REVISIT -- remedy for Deprecated warning.
    //return new String(holdbuffer,0,0,bufcount);
    return new String(holdbuffer,0,bufcount);
    }

    // __begin lookupReadMethodMethod
    public static Object handleGetProperty(Object o, String prop)
    throws JasperException {
        if (o == null)        {
            throw new JasperException(Constants.getString(
                    "jsp.error.beans.nullbean",
                    new Object[] {}));
        }
	Object value = null;
        try {
            java.lang.reflect.Method method = 
                    getReadMethod(o.getClass(), prop);
	    value = method.invoke(o, null);
        } catch (Exception ex) {
	    throw new JasperException (ex);
        }
        return value;
    }
    // __end lookupReadMethodMethod

    public static void handleSetProperty(Object bean, String prop,
					 Object value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { value });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}
    }
    
    public static void handleSetProperty(Object bean, String prop,
					 int value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Integer(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }
    
    public static void handleSetProperty(Object bean, String prop,
					 short value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Short(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }
    
    public static void handleSetProperty(Object bean, String prop,
					 long value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Long(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    } 
    
    public static void handleSetProperty(Object bean, String prop,
					 double value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Double(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }
    
    public static void handleSetProperty(Object bean, String prop,
					 float value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Float(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }
    
    public static void handleSetProperty(Object bean, String prop,
					 char value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Character(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }

    public static void handleSetProperty(Object bean, String prop,
					 byte value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Byte(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }
    
    public static void handleSetProperty(Object bean, String prop,
					 boolean value)
	throws JasperException
    {
	try {
            Method method = getWriteMethod(bean.getClass(), prop);
	    method.invoke(bean, new Object[] { new Boolean(value) });
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}	
    }
    
    public static java.lang.reflect.Method getWriteMethod(Class beanClass, String prop)
    throws JasperException {
	java.lang.reflect.Method method = null;	
        Class type = null;
	try {
	    java.beans.BeanInfo info
                = java.beans.Introspector.getBeanInfo(beanClass);
	    if ( info != null ) {
		java.beans.PropertyDescriptor pd[]
		    = info.getPropertyDescriptors();
		for (int i = 0 ; i < pd.length ; i++) {
		    if ( pd[i].getName().equals(prop) ) {
			method = pd[i].getWriteMethod();
			type   = pd[i].getPropertyType();
			break;
		    }
		}
            } else {        
                // just in case introspection silently fails.
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.nobeaninfo",
                        new Object[] {beanClass.getName()}));
            }
        } catch (Exception ex) {
            throw new JasperException (ex);
        }
        if (method == null) {
            if (type == null) {
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.noproperty",
                        new Object[] {prop, beanClass.getName()}));
            } else {
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.nomethod.setproperty",
                        new Object[] {prop, beanClass.getName()}));
            }
        }
        return method;
    }

    public static java.lang.reflect.Method getReadMethod(Class beanClass, String prop)
    throws JasperException {
        java.lang.reflect.Method method = null;        
        Class type = null;
        try {
            java.beans.BeanInfo info
                = java.beans.Introspector.getBeanInfo(beanClass);
            if ( info != null ) {
                java.beans.PropertyDescriptor pd[]
                    = info.getPropertyDescriptors();
                for (int i = 0 ; i < pd.length ; i++) {
                    if ( pd[i].getName().equals(prop) ) {
                        method = pd[i].getReadMethod();
                        type   = pd[i].getPropertyType();
                        break;
                    }
                }
            } else {        
                // just in case introspection silently fails.
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.nobeaninfo",
                        new Object[] {beanClass.getName()}));
	    }
	} catch (Exception ex) {
	    throw new JasperException (ex);
	}
        if (method == null) {
            if (type == null) {
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.noproperty",
                        new Object[] {prop, beanClass.getName()}));
            } else {
                throw new JasperException(Constants.getString(
                        "jsp.error.beans.nomethod",
                        new Object[] {prop, beanClass.getName()}));
            }
        }

	return method;
    }
    
}




