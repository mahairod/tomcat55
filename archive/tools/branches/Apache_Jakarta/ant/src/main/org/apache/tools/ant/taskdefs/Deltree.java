package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Deltree extends Task {

    private File dir;

    public void setDir(String dirName) {
	dir = project.resolveFile(dirName);
    }
    
    public void execute() throws BuildException {
	project.log("Deleting: " + dir.getAbsolutePath());

	if (dir.exists()) {
	    if (!dir.isDirectory()) {
		String msg = "Given dir: " + dir.getAbsolutePath() +
		    " is not a dir";
		throw new BuildException(msg);
	    }
	    removeDir(dir);
	}
    }

    
    private void removeDir(File dir) {
	String[] list = dir.list();
	for (int i = 0; i < list.length; i++) {
	    String s = list[i];
	    File f = new File(dir, s);
	    if (f.isDirectory()) {
		removeDir(f);
	    } else {
		f.delete();
	    }
	}
	dir.delete();
    }

}

