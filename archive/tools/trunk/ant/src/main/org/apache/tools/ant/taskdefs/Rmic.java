package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Rmic extends Task {

    private String base;
    private String classname;
    
    public void setBase(String base) {
	this.base = base;
    }

    public void setClass(String classname) {
	this.classname = classname;
    }

    public void execute() throws BuildException {
	String pathsep = System.getProperty("path.separator");
	StringBuffer classpath = new StringBuffer();
	File baseFile = project.resolveFile(base);
	classpath.append(baseFile.getAbsolutePath());
	classpath.append(pathsep);

        classpath.append(System.getProperty("java.class.path"));
        
        // in jdk 1.2, the system classes are not on the visible classpath.
        
        if (Project.getJavaVersion().startsWith("1.2")) {
            String bootcp = System.getProperty("sun.boot.class.path");
            if (bootcp != null) {
                classpath.append(pathsep);
                classpath.append(bootcp);
            }
        }
	
	// XXX
	// need to provide an input stream that we read in from!

	sun.rmi.rmic.Main compiler = new sun.rmi.rmic.Main(System.out, "rmic");
        String[] args = new String[5];
        args[0] = "-d";
        args[1] = baseFile.getAbsolutePath();
        args[2] = "-classpath";
        args[3] = classpath.toString();
        args[4] = classname;
        compiler.compile(args);
    }

    
}

