package org.apache.tools.ant;

/**
 * Signals an error condition.
 *
 * @author James Duncan Davidson
 */

public class BuildException extends Exception {
    public Exception cascade;
    
    public BuildException() {
	super();
    }

    public BuildException(String msg) {
	super(msg);
    }

    public BuildException(Exception cascade) {
	super();
	this.cascade=cascade;
    }
}
