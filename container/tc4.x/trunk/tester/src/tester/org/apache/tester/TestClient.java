/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *         Copyright (c) 1999, 2000  The Apache Software Foundation.         *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Tomcat",  and  "Apache  Software *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

package org.apache.tester;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * <p>This class contains a <strong>Task</strong> for Ant that is used to
 * send HTTP requests to a servlet container, and examine the responses.
 * It is similar in purpose to the <code>GTest</code> task in Watchdog,
 * but uses the JDK's HttpURLConnection for underlying connectivity.</p>
 *
 * <p>The task is registered with Ant using a <code>taskdef</code> directive:
 * <pre>
 *   &lt;taskdef name="tester" classname="org.apache.tester.TestClient"&gt;
 * </pre>
 * and accepts the following configuration properties:</p>
 * <ul>
 * <li><strong>host</strong> - The server name to which this request will be
 *     sent.  Defaults to <code>localhost</code> if not specified.</li>
 * <li><strong>inContent</strong> - The data content that will be submitted
 *     with this request.  The test client will transparently add a carriage
 *     return and line feed, and set the content length header, if this is
 *     specified.  Otherwise, no content will be included in the request.</li>
 * <li><strong>inHeaders</strong> - The set of one or more HTTP headers that
 *     will be included on the request.</li>
 * <li><strong>message</strong> - The HTTP response message that is expected
 *     in the response from the server.  No check is made if no message
 *     is specified.</li>
 * <li><strong>method</strong> - The HTTP request method to be used on this
 *     request.  Defaults to <ocde>GET</code> if not specified.</li>
 * <li><strong>outContent</strong> - The first line of the response data
 *     content that we expect to receive.  No check is made if no content is
 *     specified.</li>
 * <li><strong>outHeaders</strong> - The set of one or more HTTP headers that
 *     are expected in the response (order independent).</li>
 * <li><strong>port</strong> - The port number to which this request will be
 *     sent.  Defaults to <code>8080</code> if not specified.</li>
 * <li><strong>request</strong> - The request URI to be transmitted for this
 *     request.  This value should start with a slash character ("/"), and
 *     be the server-relative URI of the requested resource.</li>
 * <li><strong>status</strong> - The HTTP status code that is expected in the
 *     response from the server.  Defaults to <code>200</code> if not
 *     specified.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class TestClient extends Task {


    // ------------------------------------------------------------- Properties


    /**
     * The debugging detail level for this execution.
     */
    protected int debug = 0;

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }


    /**
     * The host name to which we will connect.
     */
    protected String host = "localhost";

    public String getHost() {
        return (this.host);
    }

    public void setHost(String host) {
        this.host = host;
    }


    /**
     * The first line of the request data that will be included on this
     * request.
     */
    protected String inContent = null;

    public String getInContent() {
        return (this.inContent);
    }

    public void setInContent(String inContent) {
        this.inContent = inContent;
    }


    /**
     * The HTTP headers to be included on the request.  Syntax is
     * <code>{name}:{value}[##{name}:{value}] ...</code>.
     */
    protected String inHeaders = null;

    public String getInHeaders() {
        return (this.inHeaders);
    }

    public void setInHeaders(String inHeaders) {
        this.inHeaders = inHeaders;
    }


    /**
     * The HTTP response message to be expected in the response.
     */
    protected String message = null;

    public String getMessage() {
        return (this.message);
    }

    public void setMessage(String message) {
        this.message = message;
    }


    /**
     * The HTTP request method that will be used.
     */
    protected String method = "GET";

    public String getMethod() {
        return (this.method);
    }

    public void setMethod(String method) {
        this.method = method;
    }


    /**
     * The first line of the response data content that we expect to receive.
     */
    protected String outContent = null;

    public String getOutContent() {
        return (this.outContent);
    }

    public void setOutContent(String outContent) {
        this.outContent = outContent;
    }


    /**
     * The HTTP headers to be checked on the response.  Syntax is
     * <code>{name}:{value}[##{name}:{value}] ...</code>.
     */
    protected String outHeaders = null;

    public String getOutHeaders() {
        return (this.outHeaders);
    }

    public void setOutHeaders(String outHeaders) {
        this.outHeaders = outHeaders;
    }


    /**
     * The port number to which we will connect.
     */
    protected int port = 8080;

    public int getPort() {
        return (this.port);
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * The request URI to be sent to the server.  This value is required.
     */
    protected String request = null;

    public String getRequest() {
        return (this.request);
    }

    public void setRequest(String request) {
        this.request = request;
    }


    /**
     * The HTTP status code expected on the response.
     */
    protected int status = 200;

    public int getStatus() {
        return (this.status);
    }

    public void setStatus(int status) {
        this.status = status;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute the test that has been configured by our property settings.
     */
    public void execute() throws BuildException {

        // Construct a summary of the request we will be sending
        String summary = "[" + method + " " + request + "]";
        if (debug >= 1)
            System.out.println("RQST: " + summary);
        boolean success = true;
        String result = null;
        Throwable throwable = null;
        HttpURLConnection conn = null;

        try {

            // Configure an HttpURLConnection for this request
            URL url = new URL("http", host, port, request);
            conn = (HttpURLConnection) url.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setDoInput(true);
            if (inContent != null) {
                conn.setDoOutput(true);
                inContent += "\r\n";
                conn.setRequestProperty("Content-Length",
                                        "" + inContent.length());
            } else {
                conn.setDoOutput(false);
            }
            conn.setFollowRedirects(false);
            conn.setRequestMethod(method);
            if (inHeaders != null) {
                String headers = inHeaders;
                while (headers.length() > 0) {
                    int delimiter = headers.indexOf("##");
                    String header = null;
                    if (delimiter < 0) {
                        header = headers;
                        headers = "";
                    } else {
                        header = headers.substring(0, delimiter);
                        headers = headers.substring(delimiter + 2);
                    }
                    int colon = header.indexOf(":");
                    if (colon < 0)
                        break;
                    String name = header.substring(0, colon).trim();
                    String value = header.substring(colon + 1).trim();
                    conn.setRequestProperty(name, value);
                }
            }

            // Connect to the server and send our output if necessary
            conn.connect();
            if (inContent != null) {
                OutputStream os = conn.getOutputStream();
                for (int i = 0; i < inContent.length(); i++)
                    os.write(inContent.charAt(i));
                os.close();
            }

            // Acquire the response data, if there is any
            String outData = "";
            String outText = "";
            boolean eol = false;
            InputStream is = conn.getInputStream();
            if (is != null) {
                while (true) {
                    int b = is.read();
                    if (b < 0)
                        break;
                    char ch = (char) b;
                    if ((ch == '\r') || (ch == '\n'))
                        eol = true;
                    if (!eol)
                        outData += ch;
                    else
                        outText += ch;
                }
                is.close();
            }

            // Dump out the response stuff
            if (debug >= 1) {
                System.out.println("RESP: " + conn.getResponseCode() + " " +
                                   conn.getResponseMessage());
                for (int i = 0; i < 1000; i++) {
                    String name = conn.getHeaderFieldKey(i);
                    String value = conn.getHeaderField(i);
                    if ((name == null) || (value == null))
                        break;
                    System.out.println("HEAD: " + name + ": " + value);
                }
                System.out.println("DATA: " + outData);
                if (outText.length() > 2)
                    System.out.println("TEXT: " + outText);
            }

            // Validate the response against our criteria
            if (status != conn.getResponseCode()) {
                success = false;
                result = "Expected status=" + status + ", got status=" +
                    conn.getResponseCode();
            } else if ((message != null) &&
                       !message.equals(conn.getResponseMessage())) {
                success = false;
                result = "Expected message='" + message + "', got message='" +
                    conn.getResponseMessage() + "'";
            } else if ((outContent != null) &&
                       !outData.startsWith(outContent)) {
                success = false;
                result = outData;
            }

        } catch (Throwable t) {
            if (t instanceof FileNotFoundException) {
                if (status == 404) {
                    success = true;
                    result = "Not Found";
                    throwable = null;
                } else {
                    success = false;
                    try {
                        result = "Status=" + conn.getResponseCode() +
                            ", Message=" + conn.getResponseMessage();
                    } catch (IOException e) {
                        result = e.toString();
                    }
                    throwable = null;
                }
            } else if (t instanceof ConnectException) {
                success = false;
                result = t.getMessage();
                throwable = null;
            } else {
                success = false;
                result = t.getMessage();
                throwable = t;
            }
        }

        // Log the results of executing this request
        if (success)
            System.out.println("OK " + summary);
        else {
            System.out.println("FAIL " + summary + " " + result);
            if (throwable != null)
                throwable.printStackTrace(System.out);
        }

    }


}
