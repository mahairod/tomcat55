/*
 * $Id$
 */

/**
 *
 * @author Mandar Raje [mandar@eng.sun.com]
 */

package org.apache.jcheck.jsp.client.core_syntax.beantests.setProperty;

import org.apache.jcheck.jsp.util.*;
import org.apache.tools.moo.jsp.*;
import org.apache.tools.moo.TestResult;
import java.net.HttpURLConnection;

public class positiveSetPropSingleQuotes
extends PositiveJspCheckTest {

    StringManager sm = StringManager.getManager(UtilConstants.Package);

    public String getDescription () {
	return sm.getString("positiveSetPropSingleQuotes.description");
    }

    public String getGoldenFile () {
	return sm.getString("positiveSetPropSingleQuotes.goldenFile");
    }	

    public TestResult
    runTest () {
        TestResult testResult = null;

	try {
		
	    
	    setGoldenFileName (getGoldenFile());
	    HttpURLConnection connection = getConnection();
	    testResult = getTestResult(connection);
	} catch (Exception e) {
		
	      testResult = getTestResult(testResult, e);
	}

	return testResult;
    }
}
