package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;


/**
 *
 *
 * @author duncan@x180.com
 */

public class Exec extends Task {

    private String os;
    private String command;
    
    public void execute() throws BuildException {

    }

    public void setOs(String os) {
	this.os = os;
    }

    public void setCommand(String command) {
	this.command = command;
    }
}
