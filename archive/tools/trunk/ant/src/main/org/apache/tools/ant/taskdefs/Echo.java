package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;
import java.net.*;
/**
 * Echo
 *
 * @author costin@dnt.ro
 */
public class Echo extends Task {
    String message; // required
    
    public void execute() throws BuildException {
	System.out.println(message);
    }

    public void setMessage(String d) {
	this.message=d;
    }
}
