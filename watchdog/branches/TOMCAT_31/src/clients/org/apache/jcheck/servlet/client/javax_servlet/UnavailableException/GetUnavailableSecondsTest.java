package org.apache.jcheck.servlet.client.javax_servlet.UnavailableException;

import org.apache.tools.moo.servlet.ClientTest;
import org.apache.tools.moo.TestResult;
import java.net.HttpURLConnection;
import org.apache.jcheck.servlet.util.StringManager;
import org.apache.jcheck.servlet.util.UtilConstants;

/**
 *	Positive Test for UnavailableException.GetUnavailableSeconds()
 */

public class GetUnavailableSecondsTest extends ClientTest {

	/** IsPermanent retruns the Unavailabilty of
	 *  the Servlet if it is permanent otherwise it
	 *  returns a negative value.
	 *  Testing the  UnavailableException(int,Servlet,String)
	 */

	public String getDescription() {

		StringManager sm = StringManager.getManager(UtilConstants.Package);
		return sm.getString("GetUnavailableSecondsTest.description");
	}
	public TestResult runTest() {
		TestResult testResult = null;
		try {
			HttpURLConnection connection = getConnection();
			testResult = getTestResult(connection);
		}catch(Exception e) {
			testResult = getTestResult(testResult,e);
		}
		return testResult;
 	}
}
