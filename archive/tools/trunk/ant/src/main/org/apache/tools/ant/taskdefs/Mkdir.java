
package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.File;


/**
 * Creates a given directory.
 *
 * @author duncan@x180.com
 */

public class Mkdir extends Task {

    private String dirName;
    
    public void execute() throws BuildException {
	File dir = project.resolveFile(dirName);
	if (!dir.exists()) {
	    boolean result = dir.mkdirs();
	    if (result == false) {
		String msg = "Directory " + dirName + " creation was not " +
		    "succesful for an unknown reason";
		throw new BuildException(msg);
	    }
	    project.log("Created dir: " + dir.getAbsolutePath());
	}
    }

    public void setDir(String dirName) {
	this.dirName = dirName;
    }
}
