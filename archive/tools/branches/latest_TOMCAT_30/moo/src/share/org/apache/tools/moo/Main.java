/*
 * $Header$ 
 * $Date$ 
 * $Revision$
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

public class Main {
    
    private static final String ConfigFileName = "testlist.txt";
    private static final String HelpArg = "-help";
    private static final String DebugArg = "-debug";
    private static final String ConfigFileArg = "-testfile";
    public static final String HostName = "test.hostName";
    public static final String PortName = "test.port";
    public static final String DefaultHost = "localhost";
    public static final String DefaultPort = "8080";
    private static final int TrimSize = 35;
    private static final int PageSize = 75;
    private String[] args = null;
    private boolean debug = true;
    
    public Main(String[] args) {
        long startTime = System.currentTimeMillis();
        this.args = args;
	
        if (argExists(this.DebugArg)) {
            this.debug = true;
        }
	
        if (argExists(this.HelpArg)) {
            System.out.print("usage: ");
	    System.out.print(this.getClass().getName());
            System.out.println(" [-options]");
            System.out.println("");
            System.out.println("where options include:");
            System.out.println("    -help             print out this message");
            System.out.println("    -debug            print debug messages");
            System.out.println("    -testfile <file>  set test config file");
	    
            System.exit(0);
        }
	
        setDefaults();
	
        Vector tests = getTests(getArg(this.ConfigFileArg));
	Logger log = Logger.getLogger();
	
        System.out.println();
        System.out.println("testing " + tests.size() + " module" +
			   ((tests.size() != 1) ? "s" : ""));
        System.out.println();
	
	try {
	    FileOutputStream err = new FileOutputStream("errorLog");
	    doTest(tests.elements(),err);
	} catch (IOException ex) {
	    System.out.println(ex.getMessage());
	    return;
	}
	
        long elapsedTime = System.currentTimeMillis() - startTime;
        String runTime = (elapsedTime / 1000) + "." + (elapsedTime % 1000);

        System.out.println();
        System.out.println("complete in : " + runTime + " seconds");
        System.out.println();
    }
    
    public static void main(String[] args) {
        try {
            new Main(args);
        } catch (Exception e) {
            System.out.println("can't instantiate : Main");
            e.printStackTrace();
	}
    }

    private void setDefaults() {
        Properties sysProps = null;

        try {
            sysProps = System.getProperties();
        } catch (SecurityException se) {
            if (this.debug) {
                se.printStackTrace();
            }

            sysProps = new Properties();
        }

        sysProps.put(this.HostName,
            sysProps.getProperty(this.HostName, this.DefaultHost));
        sysProps.put(this.PortName,
            sysProps.getProperty(this.PortName, this.DefaultPort));

        try {
            System.setProperties(sysProps);
        } catch (SecurityException se) {
            if (this.debug) {
                se.printStackTrace();
            }
        }
    }
    
    private Vector getTests(String configFile) {
        BufferedReader br = null;
        Vector tests = new Vector();
	
        br = getReader(configFile);
	
        if (br == null) {
            br = getReader(this.ConfigFileName);
        }
	
        if (br != null) {
            String line = null;
	    
            try {
                while ((line = br.readLine()) != null) {
                    line = line.trim();
		    
                    if (line.length() > 0 && ! line.startsWith("#")) {
                        tests.addElement(line.trim());
                    }
                }
            } catch (IOException ioe) {
	        if (this.debug) {
                    ioe.printStackTrace();
	        }
            }
        }
	
        return tests;
    }
    
    private BufferedReader getReader(String fileName) {
        BufferedReader br = null;
	
        if (fileName != null && fileName.trim().length() > 0) {
            try {
                br = new BufferedReader(new FileReader(fileName));
            } catch (FileNotFoundException fnfe) {
                if (this.debug) {
                    fnfe.printStackTrace();
                }
            }
        }
	
        return br;
    }
    
    private void doTest(Enumeration tests, OutputStream err) {
	int passNumber = 0;
	int failNumber = 0;
	
	while (tests.hasMoreElements()) {
            Object obj = null;
	    
            try {
	        String test = (String)tests.nextElement();
		
                obj = Class.forName(test).newInstance();
            } catch (ClassNotFoundException cnfe) {
	        if (this.debug) {
                    cnfe.printStackTrace();
                }
		// Continue with the next element.
		continue;
		
            } catch (IllegalAccessException iae) {
	        if (this.debug) {
                    iae.printStackTrace();
                }
		
		continue;
            } catch (InstantiationException ie) {
	        if (this.debug) {
                    ie.printStackTrace();
                }
		
                continue;
            }
	    
            StringBuffer sb = new StringBuffer("  ");
	    
            sb.append("  ");
	    
            if (obj instanceof Testable) {
                TestResult testResult = null;
                String description = null;
                boolean status = false;
		
                try {
		    ((Testable)obj).setStream(err);
                    description = ((Testable)obj).getDescription();
                    testResult = ((Testable)obj).runTest();
                    status = testResult.getStatus();
                } catch (Exception e) {
		    if (this.debug) {
                        e.printStackTrace();
                    }
                }
		
		if (description.length() > this.TrimSize) {
		    sb.append(description.substring(0, this.TrimSize - 3));
		    sb.append("...");
		} else {
		    sb.append(description);
		    
		    for (int i = description.length(); i < this.TrimSize;
			 i++) {
		        sb.append(" ");
		    }
		}
		
		sb.append(" : ");
		
                if (status) {
                    sb.append("OK");
		    passNumber++;
                } else {
                    sb.append("FAIL");
		    failNumber++;
                }
		
                if (testResult != null) {
		    String message = testResult.getMessage();
		    
		    if (message != null &&
                        message.trim().length() > 0) {
			sb.append(" - ");
			
			int maxMessageSize = this.PageSize - this.TrimSize;
			
			if (message.length() > maxMessageSize) {
			    sb.append(System.getProperty("line.separator"));
			    sb.append("      ");
			}
			
			sb.append(message);
		    }
                }
            } else {
                sb.append("FAIL");
                sb.append(" - not testable");
		failNumber++;
            }
	    
            System.out.println(sb);
        }
	
	System.out.println();
	
	if (failNumber == 0) {
	    System.out.println("<<< PASS >>>");
	} else {
	    System.out.println("<<< FAIL >>>");
	}
	
	System.out.println("Number of tests Passed: " + passNumber);
	System.out.println("Number of tests Failed: " + failNumber);
    }
    
    private boolean argExists(String arg) {
        for (int i = 0; i < args.length; i++) {
            String thisArg = args[i];
	    
            if (thisArg.equals(arg)) {
                return true;
            }
        }
	
        return false;
    }
    
    /**
     * Gets the value associated with the given argument. If the argument
     * doesn't have a value, or if the argument doesn't exist in the set
     * of arguments, then return null.
     *   
     * @param arg
     */  
    
    private String getArg(String arg) {
        for (int i = 0; i < args.length; i++) {
            String thisArg = args[i];
	    
            if (thisArg.equals(arg)) {
                try {
                    String nextArg = args[i + 1];
		    
                    return nextArg;
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }    
        }
	
        return null;
    }
}









