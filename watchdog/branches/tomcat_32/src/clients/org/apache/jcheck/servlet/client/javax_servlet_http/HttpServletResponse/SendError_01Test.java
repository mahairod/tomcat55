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

import org.apache.tools.moo.servlet.ClientTest;
import org.apache.tools.moo.TestResult;
import java.net.HttpURLConnection;
import org.apache.jcheck.servlet.util.StringManager;
import org.apache.jcheck.servlet.util.UtilConstants;
import java.io.InputStream;
import java.io.FileNotFoundException;

/**
 *	Test for SendError(int) method
 */

public class SendError_01Test extends ClientTest {


/**
 *	Sends an error code and commits the Response Code
 */

	public String getDescription() {

		StringManager sm = StringManager.getManager(UtilConstants.Package);
		return sm.getString("SendError_01Test.description");
	}
	public TestResult runTest() {

		TestResult testResult = null;

		HttpURLConnection connection=null;

		try {
			connection = getConnection();

			InputStream is = connection.getInputStream();

			StringBuffer sb = new StringBuffer();
			byte buffer[] = new byte[64];
			int count=0;
			String st=null;

			int status =connection.getResponseCode();

			//reading response

			do {
				st = new String(buffer,0,count);
				sb.append(st);
				count=is.read(buffer,0,buffer.length);
			}while(count!=-1);

			System.out.println(st);

			st = sb.toString();

			//if 100 is there in the read dsta pass it
			// we SentError with code 100

			if(st.indexOf("100")>-1) {
				testResult = getTestResult(connection);
			}
			else
				testResult=getTestResult(testResult,new Exception(" Wrong status code"));



		} catch(Exception e) {
			testResult = getTestResult(testResult,e);
		}
		return testResult;
 	}
}
