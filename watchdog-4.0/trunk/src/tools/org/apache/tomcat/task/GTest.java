/**
* 
* @Author Costin, Ramesh.Mandava
*
*/
package org.apache.tomcat.task;

import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


// derived from Jsp

public class GTest extends Task {

    private static final String ZEROS        = "00000000";
    private static final int SHORTPADSIZE    = 4;
    private static final int BYTEPADSIZE     = 2;
    private static final int CARRIAGE_RETURN = 13;
    private static final int LINE_FEED       = 10;

    String prefix = "http";
    String host = "localhost";
    int port = 8080;
    int debug = 0;

    String description = "No description";

    String request;
    HashMap requestHeaders = new HashMap();
    String content;

    // Expected response
    boolean magnitude = true;
    boolean exactMatch = false;

    // expect a response body
    boolean expectResponseBody = true;

    // Match the body against a golden file
    String goldenFile;
    // Match the body against a string
    String responseMatch;
    // the response should include the following headers
    HashMap expectHeaders = new HashMap();

    // Headers that should not be found in response
    HashMap unexpectedHeaders = new HashMap();

    // Match request line
    String returnCode = "";
    String returnCodeMsg = "";

    // Actual response
    String responseLine;
    byte[] responseBody;
    HashMap headers;


    // For Report generation
    static String resultFileName = null;
    static FileOutputStream resultOut = null;
    boolean firstTask = false;
    boolean lastTask = false;
    String expectedString;
    String actualString;

    String testName;
    String assertion;
    String testStrategy;

    // For Session Tracking
    static	Hashtable sessionHash;
    static Hashtable cookieHash;

    String testSession;
    Vector cookieVector;
    URL requestURL;
    CookieController cookieController ;

    /**
     * Creates a new <code>GTest</code> instance.
     *
     */
    public GTest() {}

    /**
     * <code>setTestSession</code> adds a 
     * CookieController for the value of sessionName
     *
     * @param sessionName a <code>String</code> value
     */
    public void setTestSession( String sessionName ) {
        testSession = sessionName;

        if ( sessionHash == null ) {
            sessionHash = new Hashtable();
        } else if ( sessionHash.get( sessionName ) == null ) {
            sessionHash.put ( sessionName, new CookieController() );
        }
    }

    /**
     * <code>setTestName</code> sets the current test name.
     *
     * @param tn current testname.
     */
    public void setTestName ( String tn ) {
        testName = tn;
    }

    /**
     * <code>setAssertion</code> sets the assertion text
     * for the current test.
     *
     * @param assertion assertion text
     */
    public void setAssertion ( String assertion ) {
        this.assertion = assertion;
    }

    /**
     * <code>setTestStrategy</code> sets the test strategy
     * for the current test.
     *
     * @param strategy test strategy text
     */
    public void setTestStrategy ( String strategy ) {
        testStrategy = strategy;
    }

    /**
     * <code>getTestName</code> returns the current 
     * test name.
     *
     * @return a <code>String</code> value
     */
    public String getTestName( ) {
        return testName;
    }

    /**
     * <code>getAssertion</code> returns the current
     * assertion text.
     *
     * @return a <code>String</code> value
     */
    public String getAssertion( ) {
        return assertion;
    }

    /**
     * <code>getTestStrategy</code> returns the current
     * test strategy test.
     *
     * @return a <code>String</code> value
     */
    public String getTestStrategy( ) {
        return testStrategy;
    }

    /**
     * <code>setResultFileName</code> allows the user
     * to set the filename in which to write test results
     * to.
     *
     * @param fileName result filename
     * @exception IOException if an error occurs
     */
    public void setResultFileName( String fileName )
    throws IOException {
        // if ( firstTask ) {
            resultFileName = fileName;
            File passedFile = new File( fileName );
            System.out.println( "Full Path of Result File-> " + passedFile.getAbsolutePath() );
            resultOut = new FileOutputStream( passedFile );

            if ( resultOut == null ) {
                System.out.println( "ERROR: Not able to create FileOutputStream for result" );
            } else {
                resultOut.write( "<root>\n".getBytes() );
            }
            // }
    }

    /*
     *public void setFirstTask( boolean val ) {
     *   firstTask = val;
     *}
     *
     *public void setLastTask ( boolean val )
     *throws IOException {
     *   lastTask = val;
     *
     *}
     */

    /**
     * <code>setPrefix</code> sets the protocol
     * prefix.  Defaults to "http"
     *
     * @param prefix Either http or https
     */
    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    /**
     * <code>setHost</code> sets hostname where
     * the target server is running. Defaults to
     * "localhost"
     *
     * @param h a <code>String</code> value
     */
    public void setHost( String h ) {
        this.host = h;
    }

    /**
     * <code>setPort</code> sets the port
     * that the target server is listening on.
     * Defaults to "8080"
     *
     * @param portS a <code>String</code> value
     */
    public void setPort( String portS ) {
        this.port = Integer.valueOf( portS ).intValue();
    }

    /**
     * <code>setExactMatch</code> determines if a
     * byte-by-byte comparsion is made of the server's
     * response and the test's goldenFile, or if
     * a token comparison is made.  By default, only
     * a token comparison is made ("false").
     *
     * @param exact a <code>String</code> value
     */
    public void setExactMatch( String exact ) {
        exactMatch = Boolean.valueOf( exact ).booleanValue();
    }

    /**
     * <code>setContent</code> String value upon which
     * the request header Content-Length is based upon.
     *
     * @param s a <code>String</code> value
     */
    public void setContent( String s ) {
        this.content = s;
    }

    /**
     * <code>setDebug</code> enables debug output.
     * By default, this is disabled ( value of "0" ).
     *
     * @param debugS a <code>String</code> value
     */
    public void setDebug( String debugS ) {
        debug = Integer.valueOf( debugS ).intValue();
    }

    /**
     * <code>setMagnitude</code> Expected return
     * value of the test execution.
     * Defaults to "true"
     *
     * @param magnitudeS a <code>String</code> value
     */
    public void setMagnitude( String magnitudeS ) {
        magnitude = Boolean.valueOf( magnitudeS ).booleanValue();
    }

    /**
     * <code>setGoldenFile</code> Sets the goldenfile
     * that will be used to validate the server's response.
     *
     * @param s fully qualified path and filename
     */
    public void setGoldenFile( String s ) {
        this.goldenFile = s;
    }

    /**
     * <code>setExpectResponseBody</code> sets a flag
     * to indicate if a response body is expected from the
     * server or not
     *
     * @param b a <code>boolean</code> value
     */
    public void setExpectResponseBody( boolean b ) {
        this.expectResponseBody = b;
    }

    /**
     * <code>setExpectHeaders</code> Configures GTest
     * to look for the header passed in the server's
     * response.  
     *
     * @param s a <code>String</code> value in the 
     *          format of <header-field>:<header-value>
     */
    public void setExpectHeaders( String s ) {
        this.expectHeaders = new HashMap();
        getHeaderDetails( s, expectHeaders, false );
    }

    /**
     * <code>setUnexpectedHeaders</code> Configures GTest
     * to look for the header passed to validate that it
     * doesn't exist in the server's response.
     *
     * @param s a <code>String</code> value in the
     *          format of <header-field>:<header-value>
     */
    public void setUnexpectedHeaders( String s ) {
        this.unexpectedHeaders = new HashMap();
        getHeaderDetails( s, unexpectedHeaders, false );
    }

    /**
     * <code>setResponseMatch</code> Match the
     * passed value in the server's response.
     *
     * @param s a <code>String</code> value
     */
    public void setResponseMatch( String s ) {
        this.responseMatch = s;
    }

    /**
     * <code>setRequest</code> Sets the HTTP/HTTPS
     * request to be sent to the target server
     * Ex.
     *    GET /servlet_path/val HTTP/1.0
     *
     * @param s a <code>String</code> value in the form
     *          of METHOD PATH HTTP_VERSION
     * @exception Exception if an error occurs
     */
    public void setRequest ( String s ) throws Exception {
        this.request = s;
        String addressString = request.substring( request.indexOf( "/" ), request.indexOf( "HTTP" ) ).trim();

        if ( addressString.indexOf( "?" ) > -1 ) {
            addressString = addressString.substring( 0, addressString.indexOf( "?" ) ) ;
        }

        requestURL = new URL( "http", host, port, addressString );
    }

    /**
     * <code>setReturnCode</code> Sets the expected
     * return code from the server's response.
     *
     * @param code a valid HTTP response status code
     */
    public void setReturnCode( String code ) {
        this.returnCode = code;
    }

    /**
     * Describe <code>setReturnCodeMsg</code> Sets the expected
     * return message to be found in the server's
     * response.
     *
     * @param code a valid HTTP resonse status code
     * @param message a <code>String</code> value
     */
    public void setReturnCodeMsg( String message ) {
        this.returnCodeMsg = message;
    }

    /**
     * <code>setRequestHeaders</code> Configures the request
     * headers GTest should send to the target server.
     *
     * @param s a <code>String</code> value in for format
     *          of <field-name>:<field-value>
     */
    public void setRequestHeaders( String s ) {
        requestHeaders = new HashMap();
        getHeaderDetails( s, requestHeaders, true );
    }

    /**
     * <code>execute</code> Executes the test.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {

        try {

            if ( resultOut != null ) {
                resultOut.write( "<test>".getBytes() );
                resultOut.write( ( "\n<testName>" + testName + "</testName>" ).getBytes() );
                resultOut.write( ( "\n<assertion>" + assertion + "</assertion>" ).getBytes() );
                resultOut.write( ( "\n<testStrategy>" + testStrategy + "</testStrategy>\n" ).getBytes() );
            }

            dispatch( request, requestHeaders );
            //dispatch(request, null);

            boolean result = checkResponse( magnitude );

            if ( result ) {
                if ( resultOut != null ) {
                    resultOut.write( "<result>PASS</result>\n".getBytes() );
                }

                if ( "No description".equals( description ) ) {
                    System.out.println( " OK " + request );
                } else
                    System.out.println( " OK " + description + " (" + request + ")" );
            } else {
                if ( resultOut != null ) {
                    resultOut.write( "<result>FAIL</result>\n".getBytes() );
                }

                if ( "No description".equals( description ) ) {
                    System.out.println( " FAIL " + request );
                } else
                    System.out.println( " FAIL " + description + " (" + request + ")" );
            }

            if ( resultOut != null ) {
                resultOut.write( "</test>\n".getBytes() );

                if ( lastTask == true ) {
                    resultOut.write( "</root>\n".getBytes() );
                    resultOut.close();
                }
            }

        } catch ( Exception ex ) {
            if ( "No description".equals( description ) ) {
                System.out.println( " FAIL " + request );
            } else
                System.out.println( " FAIL " + description + " (" + request + ")" );

            ex.printStackTrace();
        }
    }

    /**
     * <code>checkResponse</code> Executes various response
     * checking mechanisms against the server's response.
     * Checks include:
     * <ul>
     *    <li>expected headers
     *    <li>unexpected headers
     *    <li>return codes and messages in the Status-Line
     *    <li>response body comparison againt a goldenfile
     * </ul>
     *
     * @param testCondition a <code>boolean</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    private boolean checkResponse( boolean testCondition )
    throws Exception {
        boolean responseStatus = true;
	boolean match = false;

	if ( responseLine != null ) {
        // If returnCode doesn't match
	    if ( responseLine.indexOf( "HTTP/1." ) > -1 ) {
		
		if ( !returnCode.equals( "" ) ) {
		    boolean resCode = ( responseLine.indexOf( returnCode ) > -1 );
		    boolean resMsg  = ( responseLine.indexOf( returnCodeMsg ) > -1 );

		    if ( returnCodeMsg.equals( "" ) ) {
			match = resCode;
		    } else {
			match = ( resCode && resMsg );
		    }

		    if ( match != testCondition ) {
			responseStatus = false;
			System.out.println( " Error in: " + request );
			System.out.println( "    Expected Status-Line with one or all of the following values:" );
			System.out.println( "    Status-Code: " + returnCode );
			System.out.println( "    Reason-Phrase: " + returnCodeMsg );
			System.out.println( "    Received: " + responseLine );

			if ( resultOut != null ) {
			    String expectedStatusCode = "<expectedStatusCode>" + returnCode + "</expectedReturnCode>\n";
			    String expectedReasonPhrase = "<expectedReasonPhrase>" + returnCodeMsg + "</expectedReasonPhrase>";
			    actualString = "<actualStatusLine>" + responseLine + "</actualStatusLine>\n";
			    resultOut.write( expectedStatusCode.getBytes() );
			    resultOut.write( expectedReasonPhrase.getBytes() );
			    resultOut.write( actualString.getBytes() );
			}
		    } else {
			if ( debug > 0 ) {
			    System.out.println( " Expected values found in Status-Line" );
			}
		    }
		}
	    } else {
		responseStatus = false;
		System.out.println( "  Error:  Received invalid HTTP version in response header from target Server" );
		System.out.println( "         Target server must support HTTP 1.0 or HTTP 1.1" );
		System.out.println( "         Response from server: " + responseLine );
	    }
	} else {
	    responseStatus = false;
	    System.out.println( " Error in: " + request );
	    System.out.println( "        Expecting response from server, received null" );
	}

	/* 
	 * Check for headers the test expects to be in the server's response
	 */

	// Duplicate set of response headers
	HashMap copiedHeaders = cloneHeaders( headers );

	// used for error reporting
	String currentHeaderField = null;
	String currentHeaderValue = null;

        if ( !expectHeaders.isEmpty() ) {
	    boolean found = false;
	    String expHeader = null;

            if ( debug > 0 ) {
		System.out.println( " Looking for expected response headers..." );
	    }

	    if ( !headers.isEmpty() ) {
		Iterator expectIterator = expectHeaders.keySet().iterator();
                while ( expectIterator.hasNext() ) {
                    found = false;
                    String expFieldName = (String) expectIterator.next();
                    currentHeaderField = expFieldName;
                    ArrayList expectValues = (ArrayList) expectHeaders.get( expFieldName );
                    Iterator headersIterator = copiedHeaders.keySet().iterator();

                    while( headersIterator.hasNext() ) {
                        String headerFieldName = (String) headersIterator.next();
                        ArrayList headerValues = (ArrayList) copiedHeaders.get( headerFieldName );
              
                        // compare field names and values in an HTTP 1.x compliant fashion
                        if ( ( headerFieldName.equalsIgnoreCase( expFieldName ) ) ) {
                            int hSize = headerValues.size();
                            int eSize = expectValues.size();

                            // number of expected headers found in server response
                            int numberFound = 0;
             
                            for ( int i = 0; i < eSize; i++ ) {
                                currentHeaderValue = (String) expectValues.get( i );

                                if ( headerValues.contains( currentHeaderValue ) ) {
                                    numberFound++;
                                    headerValues.remove( headerValues.indexOf( currentHeaderValue ) );
                                }
                            }
                            if ( numberFound == eSize ) {
                                found = true;
                            }
                        }
                    }
                    if ( !found ) {
                        /*
                         * Expected headers not found in server response.
                         * Break the processing loop.
                         */
                        break;
                    }
                }
            }

	    if ( !found ) {
		StringBuffer actualBuffer = new StringBuffer( 128 );
                System.out.println( " Unable to find the expected header: '" + currentHeaderField + ": " + currentHeaderValue + "' in the server's response." );
		if ( resultOut != null ) {
		    expectedString = "<expectedHeader>" + currentHeaderField + ": " + currentHeaderValue + "</expectedHeader>\n";
		}
                if ( !headers.isEmpty() ) {
                    System.out.println( " The following headers were received: " );
                    Iterator iter = headers.keySet().iterator();
                    while ( iter.hasNext() ) {
                        String headerName = (String) iter.next();
                        ArrayList vals = (ArrayList) headers.get( headerName );
                        String[] val = (String[]) vals.toArray( new String[ vals.size() ] );
                        for ( int i = 0; i < val.length; i++ ) {
                            System.out.println( "\tHEADER -> " + headerName + ": " + val[ i ] );
			    if ( resultOut != null ) {
				actualBuffer.append( "<actualHeader>" + headerName + ": " + val[ i ] + "</actualHeader>\n" );
			    }
                        }
                    }
		    if ( resultOut != null ) {
			resultOut.write( expectedString.getBytes() );
			resultOut.write( actualBuffer.toString().getBytes() );
		    }
                }
                return false;
            }
        }

	/*
         * Check to see if we're looking for unexpected headers.
         * If we are, compare the values in the unexectedHeaders
         * ArrayList against the headers from the server response.
         * if the unexpected header is found, then return false.
         */

        if ( !unexpectedHeaders.isEmpty() ) {
            boolean found = false;
            String unExpHeader = null;
	    if ( debug > 0 ) {
		System.out.println( " looking for unexpected headers..." );
	    }

            // Check if we got any unexpected headers

            if ( !copiedHeaders.isEmpty() ) {
                Iterator unexpectedIterator = unexpectedHeaders.keySet().iterator();
                while ( unexpectedIterator.hasNext() ) {
                    found = false;
                    String unexpectedFieldName = (String) unexpectedIterator.next();
                    ArrayList unexpectedValues = (ArrayList) unexpectedHeaders.get( unexpectedFieldName );
                    Iterator headersIterator = copiedHeaders.keySet().iterator();

                    while ( headersIterator.hasNext() ) {
                        String headerFieldName = (String) headersIterator.next();
                        ArrayList headerValues = (ArrayList) copiedHeaders.get( headerFieldName );
                        
                        // compare field names and values in an HTTP 1.x compliant fashion
                        if ( ( headerFieldName.equalsIgnoreCase( unexpectedFieldName ) ) ) {
                            int hSize = headerValues.size();
                            int eSize = unexpectedValues.size();
                            int numberFound = 0;
                            for ( int i = 0; i < eSize; i++ ) {
                                if ( headerValues.contains( unexpectedValues.get( i ) ) ) {
                                    numberFound++;
                                    headerValues.remove( headerValues.indexOf( headerFieldName ) );
                                }
                            }
                            if ( numberFound == eSize ) {
                                found = true;
                            }
                        }
                    }
                    if ( !found ) {
                        /*
                         * Expected headers not found in server response.
                         * Break the processing loop.
                         */
                        break;
                    }
                }
            }

            if ( found ) {
                System.out.println( " Unexpected header received from server: " + unExpHeader );
                return false;
            }
	}

           

        if ( responseMatch != null ) {
            // check if we got the string we wanted
            if ( expectResponseBody && responseBody == null ) {
                System.out.println( " ERROR: got no response, expecting " + responseMatch );
                return false;
            }
	    String responseBodyString = new String( responseBody );
            if ( responseBodyString.indexOf( responseMatch ) < 0 ) {
                System.out.println( " ERROR: expecting match on " + responseMatch );
                System.out.println( "Received: " );
                System.out.println( responseBodyString );
            }
        }

	if ( !expectResponseBody && responseBody != null ) {
	    if ( debug > 0 ) {
		System.out.println( "Received a response body from the server where none was expected" );
	    }
	    return false;
	}

        // compare the body
        if ( goldenFile == null )
            return responseStatus;

        // Get the expected result from the "golden" file.
        byte[] expResult = getExpectedResult();

        // Compare the results and set the status
        boolean cmp = true;

        if ( exactMatch ) {
            cmp = compare( responseBody, expResult );
	} else {
            cmp = compareWeak( responseBody, expResult );
	}

        if ( cmp != testCondition ) {
            responseStatus = false;

            if ( resultOut != null ) {
                expectedString = "<expectedBody>" + new String( expResult ) + "</expectedBody>\n";
                actualString = "<actualBody>" + new String( responseBody ) + "</actualBody>\n";
                resultOut.write( expectedString.getBytes() );
                resultOut.write( actualString.getBytes() );
            }
        }

        return responseStatus;
    }

    
    /**
     * <code>dispatch</code> sends the request and any
     * configured request headers to the target server.
     *
     * @param request a <code>String</code> value
     * @param requestHeaders a <code>HashMap</code> value
     */
    private void dispatch( String request, HashMap requestHeaders ) 
    throws Exception {
        // XXX headers are ignored
        Socket socket = new Socket( host, port );

        InputStream is = new CRBufferedInputStream( socket.getInputStream() );

        // Write the request
        socket.setSoLinger( true, 1000 );

        PrintWriter pw = new PrintWriter(
			     new BufferedWriter(
			         new OutputStreamWriter( socket.getOutputStream() ) ) );

        try {
            pw.println( request );

            // Now sending any specified request headers
            if ( !requestHeaders.isEmpty() ) {
                Iterator iter = requestHeaders.keySet().iterator();
		
                while ( iter.hasNext() ) {
                    String headerKey = ( String ) iter.next();
		    ArrayList values = (ArrayList) requestHeaders.get( headerKey );
		    String[] value = (String[]) values.toArray( new String[ values.size() ] );
		    for ( int i = 0; i < value.length; i++ ) {
			StringBuffer sb = new StringBuffer( 25 );
			sb.append( headerKey );
			sb.append( ": " );
			sb.append( value[ i ] );
			pw.println( sb.toString() );
			if ( debug > 0 ) {
			    System.out.println( sb.toString() );
			}
		    }
                }
            }

            if ( testSession != null ) {
                cookieController = ( CookieController ) sessionHash.get( testSession );

                if ( cookieController != null ) {

                    String releventCookieString = cookieController.applyRelevantCookies( requestURL );

                    if ( ( releventCookieString != null ) && ( !releventCookieString.trim().equals( "" ) ) ) {
                        String cookieHeader = "Cookie : " + releventCookieString ;

                        if ( debug > 0 ) {
                            System.out.println( " Sending Cookie Header:: " + cookieHeader );
                        }

                        pw.println( cookieHeader );
                    }
                }

            }

            /*

            if ( ( testSession != null ) && ( sessionHash.get( testSession ) != null ) )
        {
            System.out.println("Sending Session Id : " + (String)sessionHash.get( testSession ) );
            pw.println("JSESSIONID:" + (String)sessionHash.get( testSession) );
        }

            */

            if ( content != null ) {
                pw.println( "Content-Length: " + content.length() );
            }

            if ( request.indexOf( "HTTP/1." ) > -1 )
                pw.println( "" );

            if ( content != null ) {
                pw.print( content );
                // XXX no /n at the end -see HTTP specs!
            }

            pw.flush();
        } catch ( Exception ex1 ) {
            System.out.println( " Error writing request " + ex1 );
	    if ( debug > 0 ) {
		System.out.println( "Message: " + ex1.getMessage() );
		ex1.printStackTrace();
	    }
        }

        try {
  
	    responseLine = read( is );

	    if ( debug > 0 ) {
		System.out.println( " RESPONSE STATUS-LINE: " + responseLine );
	    }

	    headers = parseHeaders( is );
           
            byte[] result = readBody( is );

            if ( result != null ) {
                responseBody = result;
		if ( debug > 0 ) {
		    System.out.println( " RESPONSE BODY:\n" + new String( responseBody ) );
		}
	    }
		
        } catch ( SocketException ex ) {
            System.out.println( " Socket Exception: " + ex );
            ex.printStackTrace();
        } finally {
	    if ( debug > 0 ) {
		System.out.println( " closing socket" );
	    }
	    socket.close();
	    socket = null;
	}
    }

    
    /**
     * <code>getExpectedResult</code> returns a byte array
     * containing the content of the configured goldenfile
     *
     * @return goldenfile as a byte[]
     * @exception IOException if an error occurs
     */
    private byte[] getExpectedResult()
    throws IOException {
        byte[] expResult = { 'N','O',' ',
                             'G','O','L','D','E','N','F','I','L','E',' ',
                             'F','O','U','N','D' };
                            
        try {
            InputStream in = new BufferedInputStream(
			         new FileInputStream( goldenFile ) );
            return readBody ( in );
        } catch ( Exception ex ) {
            System.out.println( "Golden file not found: " + goldenFile );
            return expResult;
        }
    }

    /**
     * <code>compare</code> compares the two byte arrays passed
     * in to verify that the lengths of the arrays are equal, and
     * that the content of the two arrays, byte for byte are equal.
     *
     * @param fromServer a <code>byte[]</code> value
     * @param fromGoldenFile a <code>byte[]</code> value
     * @return <code>boolean</code> true if equal, otherwise false
     */
    private boolean compare( byte[] fromServer, byte[] fromGoldenFile ) {
        if ( fromServer == null || fromGoldenFile == null ) {
            return false;
	}

	/*
         * Check to see that the respose and golden file lengths
         * are equal.  If they are not, dump the hex and don't
         * bother comparing the bytes.  If they are equal,
         * iterate through the byte arrays and compare each byte.
         * If the bytes don't match, dump the hex representation
         * of the server response and the goldenfile and return
         * false.
         */
	if ( fromServer.length != fromGoldenFile.length ) {
            StringBuffer sb = new StringBuffer( 50 );
            sb.append( " Response and golden files lengths do not match!\n" );
            sb.append( " Server response length: " );
            sb.append( fromServer.length );
            sb.append( "\n Goldenfile length: " );
            sb.append( fromGoldenFile.length );
            System.out.println( sb.toString() );
            sb = null;
            // dump the hex representation of the byte arrays
            dumpHex( fromServer, fromGoldenFile );

            return false;
        } else {

            int i = 0;
            int j = 0;

            while ( ( i < fromServer.length ) && ( j < fromGoldenFile.length ) ) {
                if ( fromServer[ i ] != fromGoldenFile[ j ] ) {
                    System.out.println( " Error at position " + ( i + 1 ) );
                    // dump the hex representation of the byte arrays
                    dumpHex( fromServer, fromGoldenFile );

                    return false;
                }

                i++;
                j++;
            }
        }

        return true;
    }

    /**
     * <code>compareWeak</code> creates new Strings from the passed arrays
     * and then uses a StringTokenizer to compare non-whitespace tokens.
     *
     * @param fromServer a <code>byte[]</code> value
     * @param fromGoldenFile a <code>byte[]</code> value
     * @return a <code>boolean</code> value
     */
    private boolean compareWeak( byte[] fromServer, byte[] fromGoldenFile ) {
        if ( fromServer == null || fromGoldenFile == null ) {
            return false;
	    }

        boolean status = true;

        String server = new String( fromServer );
        String golden = new String( fromGoldenFile );

        StringTokenizer st1 = new StringTokenizer( server );

        StringTokenizer st2 = new StringTokenizer( golden );

        while ( st1.hasMoreTokens() && st2.hasMoreTokens() ) {
            String tok1 = st1.nextToken();
            String tok2 = st2.nextToken();

            if ( !tok1.equals( tok2 ) ) {
                System.out.println( "\t FAIL*** : Rtok1 = " + tok1
                                    + ", Etok2 = " + tok2 );
                status = false;
            }
        }

        if ( st1.hasMoreTokens() || st2.hasMoreTokens() ) {
             status = false;
        }

        if ( !status ) {
            StringBuffer sb = new StringBuffer( 255 );
            sb.append( "ERROR: Server's response and configured goldenfile do not match!\n" );
            sb.append( "Response received from server:\n" );
            sb.append( "---------------------------------------------------------\n" );
            sb.append( server );
            sb.append( "\nContent of Goldenfile:\n" );
            sb.append( "---------------------------------------------------------\n" );
            sb.append( golden );
            sb.append( "\n" );
            System.out.println( sb.toString() );
        }
        return status;
    }

    /**
     * <code>readBody</code> reads the body of the response
     * from the InputStream.
     *
     * @param input an <code>InputStream</code>
     * @return a <code>byte[]</code> representation of the response
     */
    private byte[] readBody( InputStream input ) {
        StringBuffer sb = new StringBuffer( 255 );
        while ( true ) {
            try {
		int ch = input.read();

		if ( ch < 0 ) {
                    if ( sb.length() == 0 ) {
                        return ( null );
                    } else {
                        break;
		    }
		}
		sb.append( ( char ) ch );
		 
            } catch ( IOException ex ) {
                return null;
            }
        }
        return sb.toString().getBytes();
    }

    /**
     * <code>getHeaderDetails</code> Wrapper method for parseHeader.
     * Allows easy addition of headers to the specified
     * HashMap
     *
     * @param line a <code>String</code> value
     * @param headerMap a <code>HashMap</code> value
     * @param isRequest a <code>boolean</code> indicating if the passed Header 
     *                  HashMap is for request headers
     */
    private void getHeaderDetails( String line, HashMap headerHash, boolean isRequest ) {
        StringTokenizer stk = new StringTokenizer( line, "##" );

        while ( stk.hasMoreElements( ) ) {
            String presentHeader = stk.nextToken();
            parseHeader( presentHeader, headerHash, isRequest );
        }
    }

    // ==================== Code from JSERV !!! ====================
    /**
     * Parse the incoming HTTP request headers, and set the corresponding
     * request properties.
     *
     *
     * @exception IOException if an input/output error occurs
     */
    private HashMap parseHeaders( InputStream is ) throws IOException {
        HashMap headers = new HashMap();
        cookieVector = new Vector();

        while ( true ) {
            // Read the next header line
            String line = read( is );

            if ( ( line == null ) || ( line.length() < 1 ) ) {
                break;
            }

            parseHeader( line, headers, false );

            if ( debug > 0 ) {
                System.out.println( " RESPONSE HEADER: " + line );
	    }

        }

        if ( testSession != null ) {
            cookieController = ( CookieController ) sessionHash.get( testSession );

            if ( cookieController != null ) {
                cookieController.recordAnyCookies( cookieVector, requestURL );
            }
        }

        return headers;
    }

    /**
     * <code>parseHeader</code> parses input headers in format of "key:value"
     * The parsed header field-name will be used as a key in the passed 
     * HashMap object, and the values found will be stored in an ArrayList
     * associated with the field-name key.
     *
     * @param line String representation of an HTTP header line.
     * @param headers a<code>HashMap</code> to store key/value header objects.
     * @param isRequest set to true if the headers being processed are 
     *        requestHeaders.
     */
    private void parseHeader( String line, HashMap headerMap, boolean isRequest ) {
        // Parse the header name and value
        int colon = line.indexOf( ":" );

        if ( colon < 0 ) {
            System.out.println( " ERROR: Header is in incorrect format: " + line );
            return ;
        }

        String name = line.substring( 0, colon ).trim();
        String value = line.substring( colon + 1 ).trim();

        if ( ( cookieVector != null ) && ( name.equalsIgnoreCase( "Set-Cookie" ) ) ) {
            cookieVector.addElement( value );
            /*
            if ( ( value.indexOf("JSESSIONID") > -1 ) || (value.indexOf("jsessionid")  > -1 ) )
        {
                 String sessionId= value.substring( value.indexOf("=")+1);
                 if ( testSession != null )
                 {
                 	sessionHash.put( testSession, sessionId );
                 }
                 System.out.println("Got Session-ID : " + sessionId );
        }
            */
        }

        //	System.out.println("HEADER: " +name + " " + value);

	ArrayList values = (ArrayList) headerMap.get( name );
	if ( values == null ) {
	    values = new ArrayList();
	}
	// HACK
	if ( value.indexOf( ',' ) > -1 && !isRequest && !name.equalsIgnoreCase( "Date" ) ) {
	    StringTokenizer st = new StringTokenizer( value, "," );
	    while ( st.hasMoreElements() ) {
		values.add( st.nextToken() );
	    }
	} else {
	    values.add( value );
	}
	
        headerMap.put( name, values );
    }

    /**
     * Read a line from the specified servlet input stream, and strip off
     * the trailing carriage return and newline (if any).  Return the remaining
     * characters that were read as a string.7
     *
     * @returns The line that was read, or <code>null</code> if end of file
     *  was encountered
     *
     * @exception IOException if an input/output error occurred
     */
    private String read( InputStream input ) throws IOException {
        // Read the next line from the input stream
        StringBuffer sb = new StringBuffer();

        while ( true ) {
            try {
                int ch = input.read();
                //		System.out.println("XXX " + (char)ch );
                if ( ch < 0 ) {
                    if ( sb.length() == 0 ) {
                        if ( debug > 0 )
                            System.out.println( " Error reading line " + ch + " " + sb.toString() );
                        return "";
                    } else {
                        break;
                    }
                } else if ( ch == LINE_FEED ) {
                    break;
                }

                sb.append( ( char ) ch );
            } catch ( IOException ex ) {
                System.out.println( " Error reading : " + ex );
                debug = 1;

                if ( debug > 0 ) {
                    System.out.println( "Partial read: " + sb.toString() );
		    ex.printStackTrace();
		}
            }
        }
        return  sb.toString();
    }

    /**
     * <code>dumpHex</code> helper method to dump formatted
     * hex output of the server response and the goldenfile.
     *
     * @param serverResponse a <code>byte[]</code> value
     * @param goldenFile a <code>byte[]</code> value
     */
    private void dumpHex( byte[] serverResponse, byte[] goldenFile ) {
        StringBuffer outBuf = new StringBuffer( ( serverResponse.length + goldenFile.length ) * 2 );

        String fromServerString = getHexValue( serverResponse, 0, serverResponse.length );
        String fromGoldenFileString = getHexValue( goldenFile, 0, goldenFile.length );

        outBuf.append( " Hex dump of server response and goldenfile below.\n\n### RESPONSE FROM SERVER ###\n" );
        outBuf.append( "----------------------------\n" );
        outBuf.append( fromServerString );
        outBuf.append( "\n\n### GOLDEN FILE ###\n" );
        outBuf.append( "-------------------\n" );
        outBuf.append( fromGoldenFileString );
        outBuf.append( "\n\n### END OF DUMP ###\n" );

        System.out.println( outBuf.toString() );

    }

    /**
     * <code>getHexValue</code> displays a formatted hex
     * representation of the passed byte array.  It also
     * allows for only a specified offset and length of 
     * a particular array to be returned.
     *
     * @param bytes <code>byte[]</code> array to process.
     * @param pos <code>int</code> specifies offset to begin processing.
     * @param len <code>int</code> specifies the number of bytes to process.
     * @return <code>String</code> formatted hex representation of processed 
     *         array.
     */
    private String getHexValue( byte[] bytes, int pos, int len ) {
        StringBuffer outBuf = new StringBuffer( bytes.length * 2 );
        int bytesPerLine = 36;
        int cnt = 1;
        int groups = 4;
        int curPos = pos;
        int linePos = 1;
        boolean displayOffset = true;

        while ( len-- > 0 ) {
            if ( displayOffset ) {

                outBuf.append( "\n" + paddedHexString( pos, SHORTPADSIZE,
                                                       true ) + ": " );
                displayOffset = false;
            }

            outBuf.append(
                paddedHexString( ( int ) bytes[ pos ], BYTEPADSIZE, false ) );
            linePos += 2;  // Byte is padded to 2 characters

            if ( ( cnt % 4 ) == 0 ) {
                outBuf.append( " " );
                linePos++;
            }

            // Now display the characters that are printable
            if ( ( cnt % ( groups * 4 ) ) == 0 ) {
                outBuf.append( " " );

                while ( curPos <= pos ) {
                    if ( !Character.isWhitespace( ( char ) bytes[ curPos ] ) ) {
                        outBuf.append( ( char ) bytes[ curPos ] );
                    } else {
                        outBuf.append( "." );
                    }

                    curPos++;
                }

                curPos = pos + 1;
                linePos = 1;
                displayOffset = true;
            }

            cnt++;
            pos++;
        }

        // pad out the line with spaces
        while ( linePos++ <= bytesPerLine ) {
            outBuf.append( " " );
        }

        outBuf.append( " " );
        // Now display the printable characters for the trailing bytes
        while ( curPos < pos ) {
            if ( !Character.isWhitespace( ( char ) bytes[ curPos ] ) ) {
                outBuf.append( ( char ) bytes[ curPos ] );
            } else {
                outBuf.append( "." );
            }

            curPos++;
        }

        return outBuf.toString();
    }

    /**
     * <code>paddedHexString</code> pads the passed value
     * based on the specified wordsize and the value of the
     * prefixFlag.
     *
     * @param val an <code>int</code> value
     * @param wordsize an <code>int</code> value
     * @param prefixFlag a <code>boolean</code> value
     * @return a <code>String</code> value
     */
    private String paddedHexString( int val, int wordsize,
                                    boolean prefixFlag ) {

        String prefix = prefixFlag ? "0x" : "" ;
        String hexVal = Integer.toHexString( val );

        if ( hexVal.length() > wordsize )
            hexVal = hexVal.substring( hexVal.length() - wordsize );

        return ( prefix + ( wordsize > hexVal.length() ?
                            ZEROS.substring( 0, wordsize - hexVal.length() ) : "" ) + hexVal );
    }

    /**
     * <code>cloneHeaders</code> returns a "cloned"
     * HashMap of the map passed in.
     *
     * @param map a <code>HashMap</code> value
     * @return a <code>HashMap</code> value
     */
    private HashMap cloneHeaders( HashMap map ) {
	HashMap dupMap = new HashMap();
	Iterator iter = map.keySet().iterator();
	
	while ( iter.hasNext() ) {
	    String key = new String( (String) iter.next() );
	    ArrayList origValues = (ArrayList) map.get( key );
	    ArrayList dupValues = new ArrayList();

	    String[] dupVal = (String[]) origValues.toArray( new String[ origValues.size() ] );
	    for ( int i = 0; i < dupVal.length; i++ ) {
		dupValues.add( new String( dupVal[ i ] ) );
	    }
	    
	    dupMap.put( key, dupValues );
	}
	return dupMap;
    }

    /**
     * <code>CRBufferedInputStream</code> is a modified version of
     * the java.io.BufferedInputStream class.  The fill code is 
     * the same, but the read is modified in that if a carriage return
     * is found in the response stream from the target server, 
     * it will skip that byte and return the next in the stream.
     */
    private class CRBufferedInputStream extends BufferedInputStream {
        
	private static final int DEFAULT_BUFFER = 2048;

        /**
	 * Creates a new <code>CRBufferedInputStream</code> instance.
	 *
	 * @param in an <code>InputStream</code> value
	 */
	public CRBufferedInputStream( InputStream in ) {
            super( in, DEFAULT_BUFFER );
        }

        /**
	 * <code>read</code> reads a single byte value per call.
	 * If, the byte read, is a carriage return, the next byte
	 * in the stream in returned instead.
	 *
	 * @return an <code>int</code> value
	 * @exception IOException if an error occurs
	 */
	public int read() throws IOException {
            if ( in == null ) {
                throw new IOException ( "Stream closed" );
            }
            if ( pos >= count ) {
                fill();
                if ( pos >= count ) {
                    return -1;
                }
            }
            int val = buf[pos++] & 0xff;
            if ( val == CARRIAGE_RETURN ) {
                return buf[pos++] & 0xff;
            }
            return val;
        }

        /**
	 * <code>fill</code> is used to fill the internal
	 * buffer used by this BufferedInputStream class.
	 *
	 * @exception IOException if an error occurs
	 */
	private void fill() throws IOException {
            if (markpos < 0)
                pos = 0;        /* no mark: throw away the buffer */
            else if (pos >= buf.length)  /* no room left in buffer */
                if (markpos > 0) {  /* can throw away early part of the buffer */
                    int sz = pos - markpos;
                    System.arraycopy(buf, markpos, buf, 0, sz);
                    pos = sz;
                    markpos = 0;
                } else if (buf.length >= marklimit) {
                    markpos = -1;   /* buffer got too big, invalidate mark */
                    pos = 0;    /* drop buffer contents */
                } else {        /* grow buffer */
                    int nsz = pos * 2;
                    if (nsz > marklimit)
                        nsz = marklimit;
                    byte nbuf[] = new byte[nsz];
                    System.arraycopy(buf, 0, nbuf, 0, pos);
                    buf = nbuf;
                }
                count = pos;
                int n = in.read(buf, pos, buf.length - pos); 
                if (n > 0)
                count = n + pos;
        }
    }
}
