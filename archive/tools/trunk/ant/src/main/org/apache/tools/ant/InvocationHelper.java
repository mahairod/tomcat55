package org.apache.tools.ant;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

// XXX error handling - we shouldn't depend on  BuildException

/**
 *  Utilities for using dynamic invocation.
 *
 * @author duncan@x180.com
 * @author costin@dnt.ro
 */
public class InvocationHelper {

    /** Get property name using getter ( getName), then getProperty(name)
     */
    public static String getProperty( Object o, String name ) {
	//	System.out.println("Setting Property " + o.getClass() + " " + name + "=" + value);
	try {
	    Method getMethod = (Method)getPropertyGetter(o, name);
	    if( getMethod!= null ) {
		// System.out.println("Set" + name);
		Object res=getMethod.invoke(o, new Object[] {});
		return (String)res;
	    }
	    getMethod = getMethod( o, "getProperty" );
	    if( getMethod != null ) {
		//System.out.println("SetProperty" + name);
		return (String)getMethod.invoke(o, new String[] {name});
	    }
	    
	    String msg = "Error getting " + name + " in " + o.getClass();
	    return null;
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " + name;
	    System.out.println("WARNING " + msg);
	    iae.printStackTrace();
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	}
	return null;
    }


        /** Set a property, using either the setXXX method or a generic setProperty(name, value)
     *  @returns true if success
     */
    public static void setProperty( Object o, String name, String value ) throws BuildException {
	//	System.out.println("Setting Property " + o.getClass() + " " + name + "=" + value);
	try {
	    Method setMethod = (Method)getPropertySetter(o, name);
	    if( setMethod!= null ) {
		// System.out.println("Set" + name);
		setMethod.invoke(o, new Object[] {value});
		return;
	    }
	    setMethod = getMethod( o, "setProperty" );
	    if( setMethod != null ) {
		//System.out.println("SetProperty" + name);
		setMethod.invoke(o, new String[] {name, value});
		return; 
	    }
	    
	    String msg = "Error setting " + name + " in " + o.getClass();
	    throw new BuildException(msg);
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " + name;
	    System.out.println("WARNING " + msg);
	    iae.printStackTrace();
	    throw new BuildException(msg);
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	    throw new BuildException(msg);		    
	}
    }

    /** Set an object property using setter or setAttribute(name).
     */
    public static void setAttribute( Object o, String name, Object v ) throws BuildException {
	//	System.out.println("Set Attribute " + o.getClass() + " " + name + " " + v );
	try {
	    Method setMethod = getPropertySetter(o, name);
	    if( setMethod!= null ) {
		//System.out.println("Set object " + name);
		// Avoid conflict with String (properties )
		Class[] ma =setMethod.getParameterTypes();
		if ( (ma.length == 1) && (! ma[0].getName().equals("java.lang.String"))) {
		    setMethod.invoke(o, new Object[] {v});
		    return;
		}
	    }
	    
	    setMethod = getMethod( o, "setAttribute" );
	    if( setMethod != null ) {
		setMethod.invoke(o, new Object[] {name, v});
		return; 
	    }
	    
	    String msg = "Error setting " + name + " in " + o.getClass();
	    throw new BuildException(msg);
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " +
		name;
	    iae.printStackTrace();
	    throw new BuildException(msg);
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	    throw new BuildException(msg);		    
	}
    }
    
    /** Calls addXXX( v ) then setAttribute( name, v).
     */
    public static void addAttribute( Object o, String name, Object v ) throws BuildException {
	try {
	    Method setMethod = getMethod(o, "add" + capitalize( name ));
	    if( setMethod!= null ) {
		//		System.out.println("Add object using addXXX " + name);
		// Avoid conflict with String (properties )
		Class[] ma =setMethod.getParameterTypes();
		if ( (ma.length == 1) && (! ma[0].getName().equals("java.lang.String"))) {
		    setMethod.invoke(o, new Object[] {v});
		    return;
		}
	    }
	    
	    // fallback to setAttribute
	    setAttribute( o, name, v);
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " +
		name;
	    iae.printStackTrace();
	    throw new BuildException(msg);
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	    throw new BuildException(msg);		    
	}
    }


    
    /** Return a new object of class cName, or null
     */
    public static Object getInstance( String cName ) {
	try {
	    //	    System.out.println("Loading " + cName );
	    Class c=Class.forName( cName );
	    Object o=c.newInstance();
	    return o;
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    return null;
	}
    }


    // -------------------- Utility - probably not so usefull outside ----------
    
    public static Hashtable getPropertySetters( Object o ) {
	// XXX cache introspection data !!!
	Hashtable propertySetters = new Hashtable();
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(o.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + o.getClass();
	    System.out.println("WARNING " + msg);
	    ie.printStackTrace();
	    return propertySetters;
	}

	PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
	for (int i = 0; i < pda.length; i++) {
	    PropertyDescriptor pd = pda[i];
	    String property = pd.getName();
	    //	    System.out.println("Property: " + property);
	    Method setMethod = pd.getWriteMethod();
	    if (setMethod != null) {
		propertySetters.put(property, setMethod);
	    }
	}
	return propertySetters;
    }

    /** Get a setter method or null
     */
    public static PropertyDescriptor getPropertyDescriptor( Object o, String prop ) {
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(o.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + o.getClass();
	    ie.printStackTrace();
	    System.out.println("WARNING " + msg);
	    return null;
	}

	PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
	for (int i = 0; i < pda.length; i++) {
	    PropertyDescriptor pd = pda[i];
	    String property = pd.getName();
	    //	    System.out.println("XXX" + prop + " " + property);
	    if( property.equals( prop )) {
		return pda[i];
	    }
	}
	return null;
    }

    /** Get a setter method or null
     */
    public static Method getPropertySetter( Object o, String prop ) {
	PropertyDescriptor pd=getPropertyDescriptor( o, prop );
	if( pd==null) return null;
	Method setMethod = pd.getWriteMethod();
	if (setMethod != null) {
	    return setMethod;
	}
	return null;
    }

    /** Get a setter method or null
     */
    public static Method getPropertyGetter( Object o, String prop )  {
	PropertyDescriptor pd=getPropertyDescriptor( o, prop );
	if( pd==null) return null;
	Method setMethod = pd.getReadMethod();
	if (setMethod != null) {
	    return setMethod;
	}
	return null;
    }

    /** Get a method or null
     */
    public static Method getMethod( Object o, String method ) {
	// XXX cache introspection data !!!
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(o.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + o.getClass();
	    ie.printStackTrace();
	    System.out.println("WARNING " + msg);
	    return null;
	}

	MethodDescriptor[] mda = beanInfo.getMethodDescriptors();
	for (int i = 0; i < mda.length; i++) {
	    MethodDescriptor pd = mda[i];
	    String m = pd.getName();

	    if( m.equals( method ) )
		return pd.getMethod();
	}
	return null;
    }

    /** Reverse of Introspector.decapitalize
     */
    public static String capitalize(String name) {
	if (name == null || name.length() == 0) {
	    return name;
	}
	char chars[] = name.toCharArray();
	chars[0] = Character.toUpperCase(chars[0]);
	return new String(chars);
    }


}









