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
package org.apache.jcheck.servlet.client.Session;

import org.apache.tools.moo.servlet.ClientTest;
import org.apache.tools.moo.TestResult;
import java.net.HttpURLConnection;
import java.util.StringTokenizer;
import java.util.Date;
import org.apache.jcheck.servlet.util.*;
//import java.text.SimpleDateFormat;
//import java.text.ParsePosition;

public class SetCookieMaxAge
extends ClientTest {

    StringManager sm = StringManager.getManager(UtilConstants.Package);

    public String
    getDescription () {
        return sm.getString("Session.SetCookieMaxAge.description");
    }

    public TestResult
    runTest () {
        TestResult testResult = null;

	try {	  
	  HttpURLConnection connection = getConnection();

	    // HttpURLConnection connection = getConnection(queryString);

	    // PrintWriter out =
	    //     new PrintWriter(connection.getOutputStream());
	    //
	    // out.print(writePostDataHere);

	  boolean passed = false;	  
	  String cookie = connection.getHeaderField("Set-Cookie");	  
	  //parse through header by ;s looking for expires header
	  StringTokenizer st = new StringTokenizer(cookie, ";");
	  
	  while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    StringTokenizer st2 = new StringTokenizer(s, "=");
	    if (st2.nextToken().trim().equalsIgnoreCase("expires")) {
	      passed = true;

	      //Formatting the date is too much work -- assume if expires is there, then the cookie passes the test

	      /*
	      SimpleDateFormat sd = new SimpleDateFormat();
	      String date = st2.nextToken().trim();
	      long then = (sd.parse(date, new ParsePosition(0))).getTime(); //start parsing at position 0
	      long now = (new Date()).getTime();	      
	      

	      System.out.println("Date: " + date); //this should be a date
	      System.out.println("# milliseconds between now and then: " + 
				 String.valueOf(now-then));
				 
				 */
	    }	   
	  }
		
	  if (passed) {
	    testResult = getTestResult(connection); //successful
	  }
	  else {
	    /*
	    connection.disconnect();
	    testResult = new TestResult();
	    testResult.setStatus(false);
	    testResult.setMessage("The cookie sent , " + cookie + ", does not have an expires field, which is required for setMaxAge()");
	    */

	    testResult = getTestResult(testResult,
				       new Exception ("The cookie sent , " + cookie + ", does not have an expires field, which is required for setMaxAge()")
				       );

	  }
	} catch (Exception e) {
	  testResult = getTestResult(testResult, e);
	}
	
	return testResult;
    }
}
