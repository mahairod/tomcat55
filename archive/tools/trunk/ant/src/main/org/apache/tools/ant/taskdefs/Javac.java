
package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * 
 * @author duncan@x180.com
 */

public class Javac extends Task {

    private File srcDir;
    private File destDir;
    private String compileClasspath;
    private boolean debug = false;
    private boolean optimize = false;
    private boolean deprecation = true;
    private String target = "1.1";
    private String bootclasspath;
    private String extdirs;

    private Vector compileList = new Vector();
    private Hashtable filecopyList = new Hashtable();

    public void setSrcdir(String srcDirName) {
	srcDir = new File(srcDirName);
    }

    public void setDestdir(String destDirName) {
	destDir = new File(destDirName);
    }

    public void setClasspath(String classpath) {
	compileClasspath = classpath;
    }

    public void setDebug(String debugString) {
	if (debugString.equalsIgnoreCase("on")) {
	    debug = true;
	}
    }

    public void execute() throws BuildException {

	// first off, make sure that we've got a srcdir and destdir

	if (srcDir == null || destDir == null ) {
	    String msg = "srcDir and destDir attributes must be set!";
	    throw new BuildException(msg);
	}

	// scan source and dest dirs to build up both copy lists and
	// compile lists

	scanDir(srcDir, destDir);
	
	// compile the source files

	String compiler = project.getProperty("build.compiler");
	if (compiler == null) {
	    String javaVersion = System.getProperty("java.version");
	    if (javaVersion.startsWith("1.3")) {
		compiler = "modern";
	    } else {
		compiler = "classic";
	    }
	}

	if (compileList.size() > 0) {
            try {
                project.log("Compiling " + compileList.size() +
			    " source files to " + destDir.getCanonicalPath());
            } catch (IOException e) {
                throw new BuildException("IOException reading filesystem: " + 
                                         e.getMessage());
            }
	    
	    if (compiler.equalsIgnoreCase("classic")) {
		doClassicCompile();
	    } else if (compiler.equalsIgnoreCase("modern")) {
		doModernCompile();
	    } else if (compiler.equalsIgnoreCase("jikes")) {
		doJikesCompile();
	    } else {
		String msg = "Don't know how to use compiler " + compiler;
		throw new BuildException(msg);
	    }
	}
	
	// copy the support files

	if (filecopyList.size() > 0) {
	    project.log("Copying " + filecopyList.size() +
			" support files to " + destDir.getAbsolutePath());
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

    private void scanDir(File srcDir, File destDir) {

	String[] list = srcDir.list();
	for (int i = 0; i < list.length; i++) {
	    String filename = list[i];
	    File srcFile = new File(srcDir, filename);
	    File destFile = new File(destDir, filename);
	    if (srcFile.isDirectory()) {
		// it's a dir, scan that recursively
		scanDir(srcFile, destFile);
	    } else {
		// it's a file, see if we compile it or just copy it
		if (filename.endsWith(".java")) {
		    File classFile =
			new File(destDir,
				 filename.substring(0,
						    filename.indexOf(".java"))
						    + ".class");
		    if (srcFile.lastModified() > classFile.lastModified()) {
			compileList.addElement(srcFile.getAbsolutePath());
		    }
		} else {
		    if (isFileDesirable(filename) &&
			srcFile.lastModified() > destFile.lastModified()) {
			filecopyList.put(srcFile.getAbsolutePath(),
					 destFile.getAbsolutePath());
		    }
		}
	    }
	}
    }

    private String getCompileClasspath() {
	StringBuffer classpath = new StringBuffer();

	// add dest dir to classpath so that previously compiled and
	// untouched classes are on classpath

	//classpath.append(sourceDir.getAbsolutePath());
	//classpath.append(File.pathSeparator);
	classpath.append(destDir.getAbsolutePath());
	classpath.append(File.pathSeparator);

	// add our classpath to the mix

	if (compileClasspath != null) {
	    StringTokenizer tok = new StringTokenizer(compileClasspath, ":",
						      false);
	    while (tok.hasMoreTokens()) {
		File f = project.resolveFile(tok.nextToken());
		classpath.append(f.getAbsolutePath());
		classpath.append(File.pathSeparator);
	    }
	}

	// add the system classpath

	classpath.append(System.getProperty("java.class.path"));
	return classpath.toString();
    }
    
    
    private void doClassicCompile() throws BuildException {
	project.log("Using classic compiler", project.MSG_VERBOSE);
	String classpath = getCompileClasspath();
	Vector argList = new Vector();
	argList.addElement("-d");
	argList.addElement(destDir.getAbsolutePath());
	argList.addElement("-classpath");
	// Just add "sourcepath" to classpath ( for JDK1.1 )
	String javaVersion = System.getProperty("java.version");
	if (javaVersion.startsWith("1.1")) {
	    argList.addElement(classpath + File.pathSeparator + srcDir.getAbsolutePath());
	} else {
	    argList.addElement(classpath);
	    argList.addElement("-sourcepath");
	    argList.addElement(srcDir.getAbsolutePath());
	    argList.addElement("-target");
	    argList.addElement(target);
	}
	if (debug) {
	    argList.addElement("-g");
	}
	if (optimize) {
	    argList.addElement("-O");
	}
	if (bootclasspath != null) {
	    argList.addElement("-bootclasspath");
	    argList.addElement(bootclasspath);
	}
	if (extdirs != null) {
	    argList.addElement("-extdirs");
	    argList.addElement(extdirs);
	}

	project.log("Compilation args: " + argList.toString(),
		    project.MSG_VERBOSE);
	
	String[] args = new String[argList.size() + compileList.size()];
	int counter = 0; 
	
	for (int i = 0; i < argList.size(); i++) {
	    args[i] = (String)argList.elementAt(i);
	    counter++;
	}

	// XXX
	// should be using system independent line feed!
	
	StringBuffer niceSourceList = new StringBuffer("Files to be compiled:"
						       + "\r\n");

	Enumeration enum = compileList.elements();
	while (enum.hasMoreElements()) {
	    args[counter] = (String)enum.nextElement();
	    niceSourceList.append("    " + args[counter] + "\r\n");
	    counter++;
	}

	project.log(niceSourceList.toString(), project.MSG_VERBOSE);

	// XXX
	// provide the compiler a different message sink - namely our own
	
	sun.tools.javac.Main compiler =
	    new sun.tools.javac.Main(System.out, "javac");
	compiler.compile(args);
    } 

    private void doModernCompile() throws BuildException {
	project.log("Performing a Modern Compile");
    }

    private void doJikesCompile() throws BuildException {
	project.log("Performing a Jikes COmpile");
    }
}
