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
            try {
                removeDir(dir);
            } catch (IOException ioe) {
                String msg = "Unable to delete " + dir.getAbsolutePath();
                throw new BuildException(msg);
            }
        }
    }
    
    private void removeDir(File dir) throws IOException {

        // check to make sure that the given dir isn't a symlink
        // the comparison of absolute path and canonical path
        // catches this
        
        if (dir.getCanonicalPath().equals(dir.getAbsolutePath())) {
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
        }
        dir.delete();
    }
}

