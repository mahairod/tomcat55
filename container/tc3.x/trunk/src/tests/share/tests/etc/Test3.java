
/*
 * $Id$
 */

/**
 * Sample test module.
 */

package tests.etc;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;

public class Test3 implements Testable {

    public String getDescription() {
        return "Test3";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();

        testResult.setStatus(false);
        testResult.setMessage("failed big time");

        return testResult;
    }
}
