package org.apache.tools.ant;

/**
 * Signals an error condition.
 *
 * @author James Duncan Davidson
 */

public class BuildException extends Exception {

    public BuildException() {
	super();
    }

    public BuildException(String msg) {
	super(msg);
    }
}
