
/*
 * $Id$
 */

/**
 * Sample test module.
 */

package tests.etc;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;

public class Test1 implements Testable {

    public String getDescription() {
        return "Test1";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();

        testResult.setStatus(true);
        testResult.setMessage("passed big time");

        return testResult;
    }
}
