package org.apache.tools.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Base class for all tasks in the 
 *
 * @author duncan@x180.com
 */

public abstract class Task {

    protected Project project = null;
    Target target;

    /**
     * Sets the project object of this task. This method is used by
     * project when a task is added to it so that the task has
     * access to the functions of the project. It should not be used
     * for any other purpose.
     *
     * @param project Project in whose scope this task belongs.
     */

    void setProject(Project project) {
	this.project = project;
    }

    public void setAttribute( String name, Object v) {
	if("target".equals( name ) ) {
	    Target t=(Target)v;
	    target=t;
	    project=t.getProject();
	    return;
	}
	// 	System.out.println("Set Att " +name + " = " + v );
	// 	if( v!=null) System.out.println(v.getClass());
    }
    
    /**
     * Called by the project to let the task do it's work.
     *
     * @throws BuildException if someting goes wrong with the build
     */
    
    public abstract void execute() throws BuildException;

    /**
     * Convienence method to copy a file from a source to a destination
     *
     * @throws IOException
     */

    protected void copyFile(String sourceFile, String destFile)
	throws IOException
    {
	copyFile(new File(sourceFile), new File(destFile));
    }
    
    /**
     * Convienence method to copy a file from a source to a destination.
     *
     * @throws IOException
     */

    protected void copyFile(File sourceFile,File destFile) throws IOException {

	if (destFile.lastModified() < sourceFile.lastModified()) {
	    project.log("Copy: " + sourceFile.getAbsolutePath() + " > "
		    + destFile.getAbsolutePath(), project.MSG_VERBOSE);

	    // ensure that parent dir of dest file exists!
	    // not using getParentFile method to stay 1.1 compat

	    File parent = new File(destFile.getParent());
	    if (!parent.exists()) {
		parent.mkdirs();
	    }

	    // open up streams and copy using a decent buffer

	    FileInputStream in = new FileInputStream(sourceFile);
	    FileOutputStream out = new FileOutputStream(destFile);
	    byte[] buffer = new byte[8 * 1024];
	    int count = 0;
	    do {
		out.write(buffer, 0, count);
		count = in.read(buffer, 0, buffer.length);
	    } while (count != -1);
	    in.close();
	    out.close();
	}
    }
}

