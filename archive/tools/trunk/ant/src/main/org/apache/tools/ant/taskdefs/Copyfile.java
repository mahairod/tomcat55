package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;

import java.io.*;
import java.util.*;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Copyfile extends Task {

    public File srcFile;
    public File destFile;

    public void setSrc(String src) {
	srcFile = project.resolveFile(src);
    }

    public void setDest(String dest) {
	destFile = project.resolveFile(dest);
    }

    public void execute() throws BuildException {
	if (srcFile.lastModified() > destFile.lastModified()) {
	    try {
		copyFile(srcFile, destFile);
	    } catch (IOException ioe) {
		String msg = "Error copying file: " + srcFile.getAbsolutePath()
		    + " due to " + ioe.getMessage();
		throw new BuildException(msg);
	    }
	}
    }
}
