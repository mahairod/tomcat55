
/*
 * $Id$
 */

/**
 * 
 *
 */

package tests.jsp;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.SocketHelper;
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Jsp extends TestableBase {

    public String getDescription() {
        return "JSP Tests"; 
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();
        props = new Properties();

        init();

        boolean status = true;
        StringBuffer msg = new StringBuffer("");
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
  
            boolean debugSaved = debug;
            while (testNames.hasMoreElements()) {
                String testId = (String)testNames.nextElement();
                String debugThis = props.getProperty("test." + testId + ".debug");
                if (debugThis != null)
                    debug = Boolean.valueOf(debugThis).booleanValue();

                if (! test(testId)) {
                     status = false;
                     String description = props.getProperty("test." + testId +
                        ".request");
                     msg.append("\tTest " + testId + " : " +
                         description + "\n");
                }
                debug = debugSaved;
            }
        }

        testResult.setStatus(status);

        if (msg.length() > 0) {
            testResult.setMessage(msg.toString());
        }

        return testResult;
    }

    private void init() {
        
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

    private boolean test(String testId) {

        String magnitude = props.getProperty("test." + testId + ".magnitude", "true");
        boolean testCondition = Boolean.valueOf(magnitude).booleanValue(); 
        boolean responseStatus = dispatch(testId, testCondition);

        return (testCondition) ? responseStatus : ! responseStatus;
    }

    private boolean dispatch(String testId, boolean testCondition) {
        boolean responseStatus = false;

        openIO();

        if (ready()) {
            try {
                writeRequest(testId);
                if (this.debug) 
                    System.out.println("<--BEG---");
                responseStatus = getResponse(testId, testCondition);
                if (this.debug)
                    System.out.println("---END-->");

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        closeIO();

        return responseStatus; 
    }

    private void openIO() {
        if (ready()) {
            closeIO();
        }
        
        try {
            s = SocketHelper.getSocket();
            pw = new PrintWriter(new OutputStreamWriter(
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
        return (s != null && pw != null && br != null);
    }

    private void closeIO() {
        try {
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        pw.close();

        try {
            s.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void writeRequest(String testId)
    throws IOException {
        String request = props.getProperty("test." + testId + ".request");
        if (this.debug) 
            System.out.println(testId + ". " + request);
        if (request == null) {
            return;
        }
        pw.println(request);
        pw.println("");
        pw.flush();
    
    }

    private boolean getResponse(String testId, boolean testCondition)
    throws IOException {
        String line = null;

        // Match the return code if defined 
        boolean returnCode = checkReturnCode(testId);

        getServerHeader(testId);
    
        // test only the return code
        if (props.getProperty("test." + testId + ".golden") == null)
            return returnCode;

        // else do content matching as well
        StringBuffer result = getServerBody();

        // Get the expected result from the "golden" file.
        StringBuffer expResult = getExpectedResult (testId);
        //System.out.println("Expected: " + expResult + ".");
        //System.out.println("REveived: " + result + ".");

        // Compare the results and set the status
        return compare(result.toString(), expResult.toString(), 
            testCondition) && returnCode;
    }

    private boolean checkReturnCode(String testId) {
        boolean responseStatus = true;
        String returnCode = props.getProperty("test." + testId + ".returnCode");
        if (returnCode != null) {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException ioe) {
            }

            if (line != null) {
                if (this.debug)
                    System.out.println(line);
                responseStatus = (line.indexOf(returnCode) > -1) 
                    ? true : false;
            } else
                responseStatus = false;
        }
        return responseStatus;
    }

    private void getServerHeader(String testId) {
        boolean showHeader = false;
        if (this.debug) {
            String headerTag = props.getProperty("test." + testId + ".showHeader");
            if (headerTag != null)
                if (headerTag.trim().toLowerCase().equals("true"))
                    showHeader = true;
        }

        String line;
        try {
            while ((line = br.readLine()) != null) {
                // ignore header
                if (line.length() == 0)
                    break;
                if (showHeader)
                    System.out.println(line);
            }
        } catch (IOException ioe) {
        }
    }

    private StringBuffer getServerBody() {
        StringBuffer result = new StringBuffer("");
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (this.debug)
                    System.out.println("\t" + line);
                // Tokenize the line
                StringTokenizer tok = new StringTokenizer(line);
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    result.append("  " + token);
                };
            }
        } catch (IOException ioe) {
        }
        return result;
    }

    // Parse a file into a String.
    private StringBuffer getExpectedResult(String testId)
    throws IOException{
        BufferedReader bin = null;
        StringBuffer expResult = new StringBuffer("");

        String goldenFile = this.GoldenDirName + "/" + 
            props.getProperty("test." + testId + ".golden");
        try {
            InputStream in = 
                this.getClass().getResourceAsStream(goldenFile.trim());
            bin = new BufferedReader(new InputStreamReader(in));
        } catch (Exception ex) {
            System.out.println("\tGolden file not found: " + goldenFile);
            return expResult;
        }
        if (bin != null) {
            String line = null;

            while ((line = bin.readLine()) != null ) {
                StringTokenizer tok = new StringTokenizer(line);
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    expResult.append("  " + token);
                }
            }
        }
        //System.out.println("expResult: " + expResult);
        return expResult;
    }

    // Compare the actual result and the expected result.
    // Should employ a more sophasticated mechanism for comparison.
    private boolean compare(String str1, String str2, boolean testCondition) {

        StringTokenizer st1=new StringTokenizer(str1);
        StringTokenizer st2=new StringTokenizer(str2);

        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            String tok1 = st1.nextToken();
            String tok2 = st2.nextToken();
            if (!tok1.equals(tok2)) {
                if (testCondition == true && debug == true)
                    System.out.println("\tFAIL*** : Rtok1 = " + tok1 
                        + ", Etok2 = " + tok2);
                return false;
            }
        }

        if (st1.hasMoreTokens() || st2.hasMoreTokens()) {
            return false;
        } else {
            return true;
        }
    }


    private Socket s = null;
    private PrintWriter pw = null;
    private BufferedReader br = null;
    private Properties props = null;
        
    private static final String PropFileName = "jsp.properties"; 
    private static final String GoldenDirName = "Golden"; 
    private boolean debug = false;
}
