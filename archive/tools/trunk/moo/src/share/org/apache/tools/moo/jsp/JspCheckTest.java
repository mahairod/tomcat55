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
package org.apache.tools.moo.jsp;

import org.apache.tools.moo.*;
import org.apache.tools.moo.jsp.*;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.lang.NullPointerException;


/**
 * JspCheckTest is the base class for all compatibility client tests
 * They need to override runTest() as well as the description field
 */
public abstract class JspCheckTest
implements Testable {

    MapManager mapManager;
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
     * establishes and returns an HTTP Connection with the HTTP method
     * set to method.  There are no user-defined headers, nor a query string
     */
    public HttpURLConnection
    getConnection(String method)
    throws Exception {
        return getConnection(null, null, null, method);
    }

    public HttpURLConnection
    getConnection(String method, boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getConnection(null, null, null, method);
    }

    /**

    /**
     * establishes and returns an HTTP Connection (HTTP GET).
     * This method does not set any headers nor a query string
     */
    public HttpURLConnection
    getConnection ()
    throws Exception {
        return getConnection(null, null, null, null);
    }

    public HttpURLConnection
    getConnection (boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getConnection(null, null, null, null);
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

    public HttpURLConnection
    getConnection (Hashtable headers, boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getConnection(headers, null, null, null);
    }

    public HttpURLConnection
    getConnection(Hashtable headers, Hashtable queryString, String pathInfo)
    throws Exception {
        return getConnection(headers, queryString, pathInfo, null);
    }

    public HttpURLConnection
    getConnection(Hashtable headers, Hashtable queryString, String pathInfo, boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getConnection(headers, queryString, pathInfo, null);
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
     * it must be one of the following strings GET, POST, HEAD, OPTIONS, PUT, DELETE,
     * TRACE
     */
    public HttpURLConnection
    getConnection(Hashtable headers, Hashtable queryString, String pathInfo,
                  String method)
    throws Exception {
        return getCon(headers, doQueryString(queryString), pathInfo, method);
    }

    public HttpURLConnection
    getConnection(Hashtable headers, Hashtable queryString, String pathInfo,
                  String method, boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getCon(headers, doQueryString(queryString), pathInfo, method);
    }

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
     * it must be one of the following strings: GET, POST, HEAD, OPTIONS, PUT, DELETE,
     * TRACE
     */
    public HttpURLConnection
    getQueryConnection(Hashtable headers, String queryString, String pathInfo,
                       String method)
    throws Exception {
        return getCon(headers, "?" + queryString, pathInfo, method);
    }

    public HttpURLConnection
    getQueryConnection(Hashtable headers, String queryString, String pathInfo,
                       String method, boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getCon(headers, "?" + queryString, pathInfo, method);
    }

    public HttpURLConnection
    getCon(Hashtable headers, String query, String pathInfo, String method, boolean useCookie)
    throws Exception {
        this.useCookie = useCookie;
        return getCon(headers, query, pathInfo, method);
    }


    /**
     * Grandaddy of all calls
     * query is passed in as a query String
     */
    public HttpURLConnection
    getCon(Hashtable headers, String query, String pathInfo, String method)
    throws Exception {

        HttpURLConnection connection = null;

        String mapResource = this.getClass().getName();
        this.mapManager = MapManagerImpl.getMapManager();
        String testResource = mapManager.get(mapResource);

        if (testResource == null) {
            throw new NullPointerException("bad resource: " + mapResource
                                           + ".  Can't map client test to server test");
        }

        //opens an http connection to the server specified by the property
        //Main.hostName at the port specified by Main.portName to the file
        //located at testResource with the query-string query
        String toConnect = testResource + (pathInfo!=null ? "/"+pathInfo : "") +
                           query;

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
     * and the hashtable values are the HTTP form values (in a vector), and converts them
     * into a string, which is returned
     *
     * if queryString is null, this returns the empty string, ""
     */
    private String doQueryString(Hashtable queryString) {
        if (queryString == null) return "";

        //assert: queryString not null
        //hold the querystring to be generated in sb
        StringBuffer sb = new StringBuffer();
        Enumeration enum = queryString.keys();

        while (enum.hasMoreElements()) {
            String key = (String)enum.nextElement();
            Vector values = new Vector();
            //(Vector)queryString.get(key);
            values.add(0,queryString.get(key));

            Enumeration valuesEnum = values.elements();

            while (valuesEnum.hasMoreElements()) {
                String value = (String)valuesEnum.nextElement();

                //why is this code inside the while loop?
                //so that the '?' isn't appended if there are no elements?
                if (sb.length() == 0) {
                    sb.append("?");
                }

                sb.append(URLEncoder.encode(key));
                sb.append("=");
                sb.append(URLEncoder.encode(value));
            }
        }

        return sb.toString();
    }

    protected void setCookieHeader(HttpURLConnection connection) {
        String savedCookies =
          mapManager.getCookieJar().applyRelevantCookies(this.url);
        if (savedCookies != null) {
            connection.setRequestProperty("Cookie", savedCookies);
        }
    }

    protected void saveCookies(HttpURLConnection connection) {
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
     *
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

        if (code >= 400) {
            testResult.setStatus(false);
            testResult.setMessage(message);
        }
        else { //assume request was OK

            //System.out.println("loading props");

            BufferedReader in = new BufferedReader
                                (new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null ) {
                out.println (line);
            }

            connection.disconnect();
            String statusStr =
              props.getProperty(Constants.Response.Status, "false");

            out.println("Status: " + statusStr);
            if(statusStr.equals("false"))
                out.println(props.getProperty(Constants.Response.Message,""));

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

        return testResult;
    }

    public void setStream(OutputStream err) {
        out.setOutputStream(err);
    }

    protected Logger out = Logger.getLogger();
}
