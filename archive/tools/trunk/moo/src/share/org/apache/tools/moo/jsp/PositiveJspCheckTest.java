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
package org.apache.tools.moo.jsp;

import org.apache.tools.moo.jsp.Constants;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;
import org.apache.tools.moo.jsp.MapManager;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;




/**
 * PositiveJspCheckTest is the base class for all compatibility client tests
 * that need to compare the resulting html to the "golden file".
 */

public abstract class PositiveJspCheckTest
extends JspCheckTest {

    public String goldenFileName = null;

    // Getter and setter methods to be used by the actual clients.
    public String getGoldenFileName() {
        return goldenFileName;
    }

    public void setGoldenFileName(String name) {
        this.goldenFileName = name;
    }

    // connection will return the html data while will need to be
    // compared with the "goledn file".
    // This method overrides the getTestResult of the super class.

    public TestResult getTestResult(HttpURLConnection connection)
    throws Exception {

        TestResult testResult = new TestResult();

        //handle HTTP codes here
        int code = connection.getResponseCode();
        if (this.useCookie == true)
            saveCookies(connection);
        String message = connection.getResponseMessage();

        //http response in 400s signifies Client Request Incomplete/Doc Not found
        //http response in 500s signifies servlet error

        if (code >= 400) {
            testResult.setStatus(false);
            testResult.setMessage(message);
        }
        else { //assume request was OK

            // Get the "actual" result from the socket stream.
            BufferedReader in = new
                                BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer result = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null ) {
                // Tokenize the line
                StringTokenizer tok = new StringTokenizer(line);
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    result.append("  " + token);
                }
            }

            // Get the expected result from the "golden" file.
            StringBuffer expResult = getExpectedResult (getGoldenFileName());

            // Compare the results and set the status
            String diff = null;
            boolean status = compare(result.toString(), expResult.toString(), diff);
            testResult.setStatus(status);

            // Set the message (Check with SCheck.
            testResult.setMessage(diff);

            // Now free the connection.
            connection.disconnect();

        } //end else

        return testResult;
    }

    // Parse a file into a String.
    public StringBuffer getExpectedResult(String goldenFile)
    throws IOException{

        URL url;
        HttpURLConnection con;
        StringBuffer expResult = new StringBuffer();

        String mapResource = this.getClass().getName();

        try {

            String gfURL = mapManager.getGoldenfilePrefix(mapResource, goldenFile);
            url = URLHelper.getURL(gfURL);
            out.println("url = " + url.toString());

            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader
                                (new InputStreamReader(con.getInputStream()));

            String line = null;

            while ((line = in.readLine()) != null ) {
                StringTokenizer tok = new StringTokenizer(line);
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    expResult.append("  " + token);
                }
            }

        } catch (Exception ex) {
            out.println (ex.getMessage());
        }

        return expResult;
    }

    // Compare the actual result and the expected result.
    // Should employ a more sophasticated mechanism for comparison.
    public boolean compare(String str1, String str2, String diff) {

        StringTokenizer st1=new StringTokenizer(str1);
        StringTokenizer st2=new StringTokenizer(str2);

        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            String tok1 = st1.nextToken();
            String tok2 = st2.nextToken();
            if (!tok1.equals(tok2)) {
                try {
                    out.println("FAIL*** : tok1 = " + tok1 + ", tok2 = " + tok2);
                } catch (IOException ex) {
                    // eat the exception.
                }

                diff = "COMPARISON_FAIL";
                return false;
            }
        }

        if (st1.hasMoreTokens() || st2.hasMoreTokens()) {
            diff = "COMPARISON_FAIL";
            return false;
        } else {
            return true;
        }
    }

}
