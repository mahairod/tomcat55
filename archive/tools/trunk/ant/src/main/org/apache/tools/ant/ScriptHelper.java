package org.apache.tools.ant;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class ScriptHelper {

    /** Replace ${NAME} with the value of "NAME" property.
     *  Works for both getNAME() and getProperty("NAME").
     */
    public static String replaceProperties( String value, Object container )
	throws BuildException
    {
	return replaceProperties( value, container, null );
    }

    
    /** Replace ${NAME} with the value of "NAME" property.
     *  If not found, try to get it from def.
     *  Works for both getNAME() and getProperty("NAME").
     */
    public static String replaceProperties( String value ,Object proj, Object def)
	throws BuildException
    {
	StringBuffer sb=new StringBuffer();
	int i=0;
	int prev=0;
	// assert value!=nil
	int pos;
	while( (pos=value.indexOf( "$", prev )) >= 0 ) {
	    if(pos>0)
		sb.append( value.substring( prev, pos ) );
	    if( value.charAt( pos + 1 ) != '{' ) {
		sb.append( value.charAt( pos + 1 ) );
		prev=pos+2; 
	    } else {
		int endName=value.indexOf( '}', pos );
		if( endName < 0 ) {
		    throw new BuildException("Syntax error in prop: " + value );
		}
		String n=value.substring( pos+2, endName );
		String v= null;
		if(proj!=null) v= InvocationHelper.getProperty( proj,  n );
		if( v==null && def!=null) v= InvocationHelper.getProperty( def, n);
		//System.out.println("N: " + n + " " + " V:" + v);
		if(v==null) v="";
		sb.append( v );
		prev=endName+1;
	    }
	}
	if( prev < value.length() ) sb.append( value.substring( prev ) );
	//	System.out.println("After replace: " + sb.toString());
	//	System.out.println("Before replace: " + value);
	return sb.toString();
    }
}









