/*
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
package org.apache.tools.moo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.SecurityException;
import java.io.OutputStream;
import java.io.FileOutputStream;

/** Run an individual Moo test
 * Usage: RunTest [-d] [-p port] [-h host] class ");
 */
public class RunTest {
    String host="localhost";
    int port=8080;
    boolean debug = true;
    String testClassName;
    String testKey = null;
    
    public RunTest() {

    }

    void run(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        processArgs(args);

	//set system properties - XXX ugly
        setDefaults();

	doTest();
	
        long elapsedTime = System.currentTimeMillis() - startTime;

	//        String runTime = (elapsedTime / 1000) + "." + (elapsedTime % 1000);
    }
    
    public static void main(String[] args) {
        try {
            RunTest rt=new RunTest();
	    rt.run(args);
        } catch (Exception e) {
            System.out.println("Error in RunTest:");
            e.printStackTrace();
	}
    }

    // XXX ugly hack - but it's used internaly by Moo
    // XXX XXX XXX need to clean up moo !!!!!! (costin)
    private void setDefaults() {
        try {
	    Properties sysProps = new Properties();
            sysProps = System.getProperties();
	    sysProps.put("test.hostName",host);
	    sysProps.put("test.port", new Integer(port).toString());
            System.setProperties(sysProps);
        } catch (SecurityException se) {
	    se.printStackTrace();
        }
    }

    private void doTest() throws Exception {
	Testable obj=null;
	try {
	    obj = (Testable)Class.forName(testClassName).newInstance();

            if (obj instanceof ParameterizedTest) {
                ((ParameterizedTest)obj).setKey(testKey);
            }
	} catch (ClassNotFoundException cnfe) {
	    cnfe.printStackTrace();
	} catch (IllegalAccessException iae) {
	    iae.printStackTrace();
	} catch (InstantiationException ie) {
	    ie.printStackTrace();
	}
	    
	TestResult testResult = null;
	String description = null;
	boolean status = false;
	FileOutputStream err = new FileOutputStream("errorLog");

	try {
	    obj.setStream(err);
	    description = obj.getDescription();
	    testResult = obj.runTest();
	    status = testResult.getStatus();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	StringBuffer sb = new StringBuffer();
	if (status) {
	    sb.append("OK  ");
	} else {
	    sb.append("FAIL");
	}
	sb.append(" ").append(testClassName).append(" (");
	sb.append(description).append(") :");
		    
	
	if (testResult == null) {
	    System.out.println("BAD TEST - returns null");
	    return;
	}
	    
	String message = testResult.getMessage();
		    
	if (message != null ) 
	    sb.append(message);

	System.out.println(sb);
    }
    
    /**
     * Gets the value associated with the given argument. If the argument
     * doesn't have a value, or if the argument doesn't exist in the set
     * of arguments, then return null.
     *   
     * @param arg
     */  
    private void processArgs(String args[]) {
	
        for (int i = 0; i < args.length; i++) {
	    String arg = args[i];
            
	    if (arg.equals("-h")) {
		i++;
		host=args[i];
            } else if (arg.equals("-?")) {
		System.out.println("Usage: RunTest [-d] [-p port] [-h host] class ");
		return;
            } else if (arg.equals("-d")) {
		this.debug=true;
	    } else if (arg.equals("-p")) {
		i++;
		String portS=args[i];
		port=Integer.valueOf( portS).intValue();
	    } else if (arg.equals("-key")) {
		i++;
		testKey=args[i];
            } else {
		testClassName=arg;
	    }
        }
    }
}









