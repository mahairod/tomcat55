/*
 * $Header$
 * $Date$
 *
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


package org.apache.jcheck.servlet.client.javax_servlet_http.HttpServletResponse;

//import com.sun.moo.servlet.ClientTest;
//import com.sun.moo.TestResult;
import java.util.Vector;
import java.util.Hashtable;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import org.apache.tools.moo.servlet.*;
import org.apache.tools.moo.*;
import org.apache.jcheck.servlet.util.StringManager;
import org.apache.jcheck.servlet.util.UtilConstants;

/**
 *	Client side for sendRedirect method
 */

public class SendRedirectTest extends ClientTest {

	public String getDescription() {

		StringManager sm = StringManager.getManager(UtilConstants.Package);
		return sm.getString("SendRedirectTest.description");
	}

	/**
	  * Overriding the ClientTest method because
	  * the getconnection method in ClientTest sets FollowRedirects tp false while the
	  * default is true.
	  */

	public HttpURLConnection getConnection(Hashtable Headers,
		String path,String query,String method) throws Exception {

		HttpURLConnection connection=null;

		String mapResource = this.getClass().getName(); //getting this class's name

		/*
		 * Maps client side test to the server side test
		 */

		MapManager mapManager = new MapManager();

		String testResource = mapManager.get(mapResource);

		if(testResource == null) {

			throw new NullPointerException("bad resource:");
		}

		/* Helps to get the absolute path */

		URL url = URLHelper.getURL(testResource);

		connection =(HttpURLConnection) url.openConnection();

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);

		try {
			connection.connect();
		}catch(IOException ioe) {

			out.println("Could not establish connection");
			throw ioe;
		}

		return connection;

	}

	/**
	 *  SendRedirect client side test:
	 *  checks for status code 302
	 *  which is a temporary redirect reponse from the servlet to the client
	 */

	public TestResult runTest() {
		TestResult testResult = null;
		try {
			HttpURLConnection connection = this.getConnection();

			/* only if it is 302 pass it */

			if(connection.getResponseCode()==302)
				testResult = getTestResult(connection);
			else {

				/**fails, setting an exception **/

				Exception e=new Exception("Problem with SendRedirect");
				testResult = getTestResult(testResult,e);
			}

		}catch(Exception e) {
			testResult = getTestResult(testResult,e);
		}
		return testResult;
 	}

	/* considering only statuscode 302
	 * if so setting the Status of the testResult to true
	 */
	public TestResult getTestResult(HttpURLConnection connection) throws Exception {


		TestResult testResult= new TestResult();

		int code = connection.getResponseCode();

		if(code==302) {

			testResult.setStatus(true);
		}

		return testResult;

	}
}
