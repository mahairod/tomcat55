package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;

import java.io.*;
import java.util.*;

/**
 *
 *
 * @author costin@eng.sun.com
 */

public class Chmod extends Task {

    public File srcFile;
    public String mod;
    
    public void setSrc(String src) {
	srcFile = project.resolveFile(src);
    }

    public void setPerm(String perm) {
	mod=perm;
    }

    public void execute() throws BuildException {
	try {
	    // XXX if OS=unix
	    Runtime.getRuntime().exec("chmod " + mod + " " + srcFile );
	} catch (IOException ioe) {
	    // ignore, but warn
	    System.out.println("Error chmod" + ioe.toString() );
	}
    }
}
