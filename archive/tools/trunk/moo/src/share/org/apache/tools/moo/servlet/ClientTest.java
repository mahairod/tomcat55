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
 * @author Mandar Raje [mandar@eng.sun.com]
 * @author Arun Jamwal [arunj@eng.sun.com]
 */
package org.apache.tools.moo.servlet;

import org.apache.tools.moo.*;
import org.apache.tools.moo.servlet.*;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.lang.NullPointerException;
import java.io.OutputStream;
import java.io.PrintWriter;


/**
 * ClientTest is the base class for all compatibility client tests
 * They need to override runTest() as well as the description field
 */
public abstract class ClientTest
    implements Testable {

	private MapManager mapManager = null;
	URL url = null;
    boolean useCookie = false;

    
    /**
     * returns a description of the client test.
     * This method needs to be overridden by the specific test
     */
    public abstract String
	getDescription();
    
    /**
     * returns the results of running the compatibility test in the form of a 
     * TestResult object.  
     * This method needs to be overridden by the client
     */
    public abstract TestResult
	runTest();
    
    /**
     * establishes and returns an HTTP Connection (HTTP GET).
     * This method does not set any headers nor a query string
     */
    public HttpURLConnection
	getConnection()
	throws Exception {
	return getConnection(null, null, null, null);
    }
    
    /**
     * establishes and returns an HTTP Connection (HTTP GET).
     * This method does not set any headers nor a query string
     */
    public HttpURLConnection
	getConnection(boolean useCookie)
	throws Exception {
	this.useCookie = useCookie;
	return getConnection(null, null, null, null);
    }
    
    /**
     * establishes and returns an HTTP Connection with the HTTP method
     * set to method.  There are no user-defined headers, nor a query string
     */
    public HttpURLConnection
	getConnection(String method) 
	throws Exception {
	return getConnection(null, null, null, method);
    }
    
    /**
     * establishes and returns an HTTP Connection with the HTTP method
     * set to method.  There are no user-defined headers, nor a query string
     */
    public HttpURLConnection
	getConnection(String method, boolean useCookie) 
	throws Exception {
	this.useCookie = useCookie;
	return getConnection(null, null, null, method);
    }
    
    /**
     * establishes and returns an HTTP Connection (HTTP GET).
     * The headers arg should be set up with the keys as HTTP headers fields and 
     * the values as string values.
     * This method does not set the query string
     */
    public HttpURLConnection
	getConnection (Hashtable headers)
	throws Exception {
        return getConnection(headers, null, null, null);
    }

    /**
     * establishes and returns an HTTP Connection (HTTP GET).
     * The headers arg should be set up with the keys as HTTP headers fields and 
     * the values as string values.
     * This method does not set the query string
     */
    public HttpURLConnection
	getConnection (Hashtable headers, boolean useCookie)
	throws Exception {
	this.useCookie = useCookie;
        return getConnection(headers, null, null, null);
    }
    
    
    public HttpURLConnection
	getConnection(Hashtable headers, String query, String pathInfo, String method, boolean useCookie)
	throws Exception {
	    this.useCookie = useCookie;
	    return getConnection(headers, query, pathInfo, method);
	}
    /**
     * establishes and returns an HTTP Connection.
     * This connects to the value in the MapManager
     * This method sets the headers where the keys from the hashtable
     * are HTTP headers and the values the HTTP header values
     * The query string should be done the same way, where the values are a
     * java.util.Vector
     * The method argument is the request method to be used.  If it is null
     * this returns a connection with an HTTP GET
     * it must be one of the following strings: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE
     */
    
    
    /**
     * This is the less-prefered method, but allows you to input query
     * string arguments as a string (allowing a key without a corresponding
     * value (ie http://localhost/servlet/mine?Hello)
     * thus it is called a different name to avoid ambiguity with the 
     * parameters being passed in as nulls
     *
     * establishes and returns an HTTP Connection.
     * This connects to the value in the MapManager
     * This method sets the headers where the keys from the hashtable
     * are HTTP headers and the values the HTTP header values
     * The query string should be done the same way, where the values are a
     * java.util.Vector
     * The method argument is the request method to be used.  If it is null
     * this returns a connection with an HTTP GET
     * it must be one of the following strings: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE
     */
    
    /**
     * Grandaddy of all calls
     * query is passed in as a query String (without the prepended ?)
     */
    public HttpURLConnection
	getConnection(Hashtable headers, String query, String pathInfo, String method)
	throws Exception {
	
        HttpURLConnection connection = null;
	String mapResource = this.getClass().getName();
	
	mapManager = MapManagerImpl.getMapManager();

	String testResource = mapManager.get(mapResource);
	//associated server-side class for the client

	if (testResource == null) {
	    throw new NullPointerException("bad resource: " + mapResource
					   + ".  Can't map client test to server test");
	}
	
	//opens an http connection to the server specified by the property
	//Main.hostName at the port specified by Main.portName to the file
	//located at testResource with the query-string query
	String queryAndPath = (pathInfo!=null ? "/"+pathInfo : "") +
	    (query!=null ? "?" + query : "");
	
	String toConnect = testResource + queryAndPath;
	
	url = URLHelper.getURL(toConnect);
	String host = url.getHost();
	String port = String.valueOf(url.getPort());
	String protocol = url.getProtocol();
	String file = url.getFile();	
	
	try {	  
	    connection = (HttpURLConnection)url.openConnection();
	} catch (IOException e) {
	    out.println("Could not retrieve file " + file + " on " + host
			+ " on port number " + port + " via " + protocol + " protocol");
	    throw e;
	}
	
	//set the request method
	if (method != null) {
	    try {
		connection.setRequestMethod(method);
	    }catch (ProtocolException e) {
		out.println("Method: " + method + " not valid for " + protocol);
		throw e;
	    }//end catch
	} //end if	    
	
	//set cookie header
	if (this.useCookie == true)
	    setCookieHeader(connection);
	
	//establish the headers
	doHeaders(headers, connection);
	
	//set general properties
	connection.setDoOutput(true);
	connection.setDoInput(true);
	connection.setUseCaches(false);
	connection.setFollowRedirects(false);
	
	//establish connection
	try {
	    connection.connect();
	}catch (IOException e) {
	    out.println("Could not establish a connection to file " + file
			+ " on " + host + " on port number " + port + " via "
			+ protocol + " protocol");	  
	    throw e;
	} //end catch
	
	out.println("Connected to "+ url.toString());
		
	return connection;
    }
    
    
    /**
     * Takes in a hashtable, where the keys are the names of HTTP form fields
     * (must be in a java.lang.String object) 
     * and the hashtable values are the HTTP form values (in a vector or a string), 
     * and converts them into a string, which is returned
     *
     * If the value of the hashtable is not a vector (or a string), the key is ignored.
     * Likewise if the key is not a string
     *
     * if queryString is null, this returns the empty string, ""
     */
    public String doQueryString(Hashtable queryString) {
	if (queryString == null) return "";
	
	// assert: queryString not null
	// hold the querystring to be generated in sb
	StringBuffer sb = new StringBuffer();
	Enumeration keys = queryString.keys();
	
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();      
	    Object val = queryString.get(key);
	    boolean isString = val instanceof String;
	    boolean isVector = val instanceof Vector;
	    String value;
	    
	    if (isString) {
		value = (String)val;
		sb.append( URLEncoder.encode(key) );
		sb.append( "=" );
		sb.append( URLEncoder.encode(value) );
	    }
	    
	    else if (isVector) {
		Enumeration vals = ((Vector)val).elements();
		while (vals.hasMoreElements()) {
		    value = (String)vals.nextElement();
		    sb.append(URLEncoder.encode(key));
		    sb.append("=");
		    sb.append(URLEncoder.encode(value));	  
		}
	    } //end else
	    else continue;
	    //next iteration -- don't really need this as this is the end of the while loop
	} //end while (keys ...
	
	return sb.toString();
	
    }
    
 
	private void setCookieHeader(HttpURLConnection connection) {
        String savedCookies = 
	        mapManager.getCookieJar().applyRelevantCookies(this.url);
        if (savedCookies != null) {
            connection.setRequestProperty("Cookie", savedCookies);
        }
	}

    private void saveCookies(HttpURLConnection connection) {
        String recvCookies = connection.getHeaderField("Set-Cookie");
        if (recvCookies != null) {
            Vector receivedCookies = new Vector();
            receivedCookies.addElement(recvCookies);
	        mapManager.getCookieJar().recordAnyCookies(receivedCookies, this.url);
        }
    }
    
    /**
     * adds the headers from the headers Hashtable 
     * (key=HTTP header, value=HTTP value)
     * to the connection
     *
     * if headers is null, this method leaves connection unchanged
     * 
     * modifies: connection 
     */
    private void doHeaders(Hashtable headers, HttpURLConnection connection) {
	if (headers != null) {
	    Enumeration enum = headers.keys();
	    
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();
		String value = (String)headers.get(key);
		
		if (key != null &
		    value != null) {
		    connection.setRequestProperty(key, value);
		} //end if
	    } //end while
	} //end if
    } //end doHeaders
    
    
    
    /**
     * runs another test (ie a precursor to the current test)
     * this is used for example to set state on the server, then test
     * additional functionality with a later test
     *
     * if class does not exist, throw ClassNotFoundException
     * if the class is not an instance of moo.Testable, throw ClassCastException
     * if test passes, return a TestResult with status true
     * if test fails or throws an exception, return a TestResult with a false status
     *
     * @param testName  the fully-qualified class name of the test to run
     *                  must be an instance of org.apache.tools.moo.Testable
     *
     *
     * At some point, this may need to be edited, as the second test may need
     * access to the initial connection.  The reason for this is in setting a
     * session for example, when the second test needs to know which session id
     * was established by the first test.
     *
     */
    
    protected TestResult runAnotherTest(String testName) 
	throws ClassNotFoundException, ClassCastException      
    {
	Class c = Class.forName(testName);
	try {
	    Testable test = (Testable)c.newInstance();
	    return test.runTest();
	} 
	catch (java.lang.IllegalAccessException e) {
	    e.printStackTrace(new PrintStream(Logger.getLogger().getOutputStream()));
	    return new TestResult(false, e.toString());
	}
	catch (java.lang.InstantiationException e) {
	    e.printStackTrace(new PrintStream(Logger.getLogger().getOutputStream()));
	    return new TestResult(false, e.toString());
	}    
    }
    
    
    /**
     * requires: that the corresponding server returns a response in the
     * form of a properties file.  Additionally, there must be a key in the
     * properties file named Constants.Response.Status, which has a string
     * value of either "true" or "false".  If not, then false is returned.  
     * This mechanism may need to be extended. 
     *
     * reads the response from the server as a properties file
     * it then checks the Constants.Response.Status property
     * to see if the test succeeded.  
     *
     * this should be called by the subclass client test's runTest() method.
     */
    public TestResult
	getTestResult (HttpURLConnection connection)
	throws Exception {
        TestResult testResult = new TestResult();
	Properties props = new Properties();
	
	//handle HTTP codes here
	int code = connection.getResponseCode();
	String message = connection.getResponseMessage();

	if (this.useCookie == true)
        saveCookies(connection);

	//http response in 400s signifies Client Request Incomplete/Doc Not found ...
	//http response in 500s signifies servlet error

	if (code >= 400) {
	    testResult.setStatus(false);
	    testResult.setMessage(message);
	}
	else { //assume request was OK
	    
	  props.load(connection.getInputStream());
	  connection.disconnect();
	  
	  String statusStr =
	      props.getProperty(Constants.Response.Status, "false");
	  
	  out.println("Response Status = " + statusStr);
	  if(statusStr.equals("false"))
	      out.println("Message: " + props.getProperty(Constants.Response.Message,""));
	  
	  testResult.setStatus(Boolean.valueOf(statusStr).booleanValue());
	  testResult.setMessage(
				props.getProperty(Constants.Response.Message, ""));
	} //end else
	
	return testResult;
    }
    
    /**
     * this is called if the test results in an exception.
     * this should be called by the subclass client test' runTest() method.
     *
     * modifies: the status and message properties of testResult
     */
    public TestResult
	getTestResult (TestResult testResult, Exception e) {
        if (testResult == null) { 
	    testResult = new TestResult(); 
	} 
	
	testResult.setStatus(false);
	testResult.setMessage(this.getClass().getName() +
			      " exception: " + e);
	
	e.printStackTrace(new PrintWriter(out.getOutputStream()));
    
	return testResult;
    }
    
    public void setStream (OutputStream err) {
	out.setOutputStream(err);
    }
    
    protected Logger out = Logger.getLogger();
    
}
