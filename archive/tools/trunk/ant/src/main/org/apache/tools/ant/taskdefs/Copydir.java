package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;

import java.io.*;
import java.util.*;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Copydir extends Task {

    public File srcDir;
    public File destDir;

    private Hashtable filecopyList = new Hashtable();

    public void setSrc(String src) {
	srcDir = project.resolveFile(src);
    }

    public void setDest(String dest) {
	destDir = project.resolveFile(dest);
    }
    
    public void execute() throws BuildException {
	scanDir(srcDir, destDir);
	if (filecopyList.size() > 0) {
	    project.log("Copying " + filecopyList.size() + " files to "
			+ destDir.getAbsolutePath());
	    Enumeration enum = filecopyList.keys();
	    while (enum.hasMoreElements()) {
		String fromFile = (String)enum.nextElement();
		String toFile = (String)filecopyList.get(fromFile);
		try {
		    copyFile(fromFile, toFile);
		} catch (IOException ioe) {
		    String msg = "Failed to copy " + fromFile + " to " + toFile
			+ " due to " + ioe.getMessage();
		    throw new BuildException(msg);
		}
	    }
	}
    }

    private void scanDir(File from, File to) {
	String[] list = from.list(new DesirableFilter());
	if (list == null) {
	    project.log("Source directory " + srcDir.getAbsolutePath()
			+ " does not exist.", "copydir", Project.MSG_WARN);
	    return;
	}
	for (int i = 0; i < list.length; i++) {
	    String filename = list[i];
	    File srcFile = new File(from, filename);
	    File destFile = new File(to, filename);
	    if (srcFile.isDirectory()) {
		scanDir(srcFile, destFile);
	    } else {
		if (srcFile.lastModified() > destFile.lastModified()) {
		    filecopyList.put(srcFile.getAbsolutePath(),
				     destFile.getAbsolutePath());
		}
	    }
	}
    }
}
