
/*
 * $Id$
 */

/**
 * test various URI requests per servlet 2.2/10
 *
 * @author James Todd  [gonzo@eng.sun.com]
 */

package tests.request;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.SocketHelper;
import java.net.URL;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.io.UnsupportedEncodingException;

public class RequestMap implements Testable {
    private Socket s = null;
    private PrintWriter pw = null;
    private BufferedWriter bw = null;
    private BufferedReader br = null;
    private static final String PropFileName = "requestMap.properties";
    private boolean debug = false;

    public String getDescription() {
        return "URI Request Map Test";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();
	Properties props = new Properties();

	init(props);

	boolean status = true;
	StringBuffer msg = new StringBuffer();
	String testsKey = props.getProperty("tests");
	String debugS = props.getProperty("Debug");
        debug = Boolean.valueOf(debugS).booleanValue(); 

	if (testsKey != null) {
	    Vector tests = new Vector();

	    StringTokenizer stok = new StringTokenizer(testsKey, ",");

	    while (stok.hasMoreTokens()) {
	        tests.addElement(stok.nextToken().trim());
	    }

	    Enumeration testNames = tests.elements();

	    while (testNames.hasMoreElements()) {
                boolean localStatus = true;

	        String testId = (String)testNames.nextElement();
                String description = props.getProperty("test." + testId +
                    ".description");
		    String request = props.getProperty("test." + testId +
                    ".request");
		    String response = props.getProperty("test." + testId +
                    ".response");
		    String magnitude = props.getProperty("test." + testId +
                    ".magnitude", "true");

                try {
                    if (request.indexOf("://") > -1) {
                        request = getTestFromURL(request);
                    }

                    if (response.indexOf("://") > -1) {
                        response = getTestFromURL(response);
                    }
                } catch (MalformedURLException mue) {
                    if (this.debug) {
                        mue.printStackTrace();
                    }

                    localStatus = false;
                } catch (IOException ioe) {
                    if (this.debug) {
                        ioe.printStackTrace();
                    }

                    localStatus = false;
                }

                if (this.debug) {
                    System.out.println(testId + ". Request: " + request +
                        " Response: " + response); 
                }

                if (! localStatus || ! test(request, response,
                    Boolean.valueOf(magnitude).booleanValue())) {
                    status = false;
                    msg.append("can't run test " + testId + " : " +
                        description);
	        }
	    }
	}

	testResult.setStatus(status);

	if (msg.length() > 0) {
	    testResult.setMessage(msg.toString());
	}

	return testResult;
    }

    private void init(Properties props) {
        InputStream in = 
            this.getClass().getResourceAsStream(PropFileName); 
        if (in != null) {
	        try {
		        props.load(in);
                in.close();
	        } catch (IOException ioe) {
	            if (this.debug) {
		            ioe.printStackTrace();
		        }
	        }
        } else
            System.out.println("Resource file not found: " + PropFileName);
    }

    private String getTestFromURL(String url)
    throws MalformedURLException, IOException {
        return getTestFromURL(new URL(url.trim()));
    }

    private String getTestFromURL(URL url)
    throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStreamReader isr = new InputStreamReader(url.openStream());
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line.trim()); 
        }

        isr.close();
        br.close();

        return sb.toString();
    }

    private boolean test(String request, String responseKey,
        boolean testCondition) {
        boolean returnStatus = false;

	if (request == null || responseKey == null) {
	    return ! testCondition;
	}

        String response = dispatch(request);
	boolean responseStatus =
	    (response.indexOf(responseKey) > -1) ? true : false;

	return (testCondition) ? responseStatus : ! responseStatus;
    }

    private String dispatch(String request) {
        String response = null;

        openIO();

	if (ready()) {
	    try {
	        writeRequest(request);

		    response = getResponse();
            
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	    }
	}

	closeIO();

	return response;
    }

    private void openIO() {
        if (ready()) {
	    closeIO();
        }

	try {
	    s = SocketHelper.getSocket();
	    pw = new PrintWriter(new OutputStreamWriter(
	        s.getOutputStream()));
	    bw = new BufferedWriter(new OutputStreamWriter(
	        s.getOutputStream()));
	    br = new BufferedReader(new InputStreamReader(
	        s.getInputStream()));
	} catch (UnknownHostException uhe) {
	    uhe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private boolean ready() {
        return (s != null && bw != null && br != null);
    }

    private void closeIO() {
        try {
	    br.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}

	try {
	    bw.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}

	try {
	    s.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }

    private void writeRequest(String request)
    throws IOException {
        bw.write(request);
	bw.write('\r');
	bw.write('\n');
	bw.write('\n');

	bw.flush();
    }

    private String getResponse()
    throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = null;

        if (this.debug)
	        System.out.println("<--------"); 
        while ((line = br.readLine()) != null) {
        if (this.debug)
            System.out.println("\t" + line);
	    sb.append(line);
	    sb.append('\n');
        }
        if (this.debug)
	        System.out.println("-------->"); 

	return sb.toString();
    }
}
