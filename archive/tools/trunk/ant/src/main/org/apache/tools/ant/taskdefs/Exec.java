package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.IOException;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Exec extends Task {

    private String os;
    private String command;
    
    public void execute() throws BuildException {
	try {
	    // XXX if OS= current OS
	    Runtime.getRuntime().exec(command);
	} catch (IOException ioe) {
	    throw new BuildException("Error exec: " + command );
	}

    }

    public void setOs(String os) {
	this.os = os;
    }

    public void setCommand(String command) {
	this.command = command;
    }
}
