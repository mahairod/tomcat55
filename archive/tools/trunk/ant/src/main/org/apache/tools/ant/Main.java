package org.apache.tools.ant;

import java.io.File;
import java.util.Properties;
import java.util.Enumeration;
/**
 * Command line entry point into BuildTool.
 *
 * @author duncan@x180.com
 */

public class Main {

    private static int msgOutputLevel = Project.MSG_INFO;
    private static File buildFile = new File("build.xml");
    private static String target = null;

    private static Properties defines=new Properties();
    /**
     * 
     *
     * @param args Command line args.
     */
    
   public static void main(String[] args) {

	// cycle through given args
	
	for (int i = 0; i < args.length; i++) {
	    String arg = args[i];
	    if (arg.equals("-help") || arg.equals("help")) {
		printUsage();
		return;
	    } else if (arg.equals("-quiet") || arg.equals("-q") ||
		       arg.equals("q")) {
		msgOutputLevel = Project.MSG_WARN;
	    } else if (arg.equals("-verbose") || arg.equals("-v") ||
		       arg.equals("v")) {
		msgOutputLevel = Project.MSG_VERBOSE;
	    } else if (arg.equals("-define") || arg.equals("-d")) {
		String n=args[i+1];
		String v=args[i+2];
		i+=2;
		defines.put( n, v );
	    } else if (arg.equals("-buildfile") || arg.equals("-file")) {
		try {
		    buildFile = new File(args[i+1]);
		    i++;
		} catch (ArrayIndexOutOfBoundsException aioobe) {
		    String msg = "You must specify a buildfile when " +
			"using the -buildfile argument";
		    System.out.println(msg);
		    return;
		}
	    } else if (arg.startsWith("-")) {
		// we don't have any more args to recognize!
		String msg = "Unknown arg: " + arg;
		System.out.println(msg);
		printUsage();
		return;
	    } else {
		// if it's no other arg, it may be the target
		target = arg;
	    }
	}

	// make sure buildfile exists

	if (!buildFile.exists()) {
	    System.out.println("Buildfile: " + buildFile + " does not exist!");
	    return;
	}

	// make sure it's not a directory (this falls into the ultra
	// paranoid lets check everything catagory

	if (buildFile.isDirectory()) {
	    System.out.println("What? Buildfile: " + buildFile + " is a dir!");
	    return;
	}

	// ok, so if we've made it here, let's run the damn build allready

	runBuild();
    }

    private static void runBuild() {
	long startTime = System.currentTimeMillis();
	if (msgOutputLevel >= Project.MSG_INFO) {
	    System.out.println("Buildfile: " + buildFile);
	}

	Project project = new Project();
	Enumeration preDef=defines.keys();
	while( preDef.hasMoreElements() ) {
	    String n=(String)preDef.nextElement();
	    String v=(String)defines.get( n );
	    project.setProperty( n, v );
	}

	project.setOutputLevel(msgOutputLevel);

	try {
	    ProjectHelper.configureProject(project, buildFile);
	} catch (BuildException be) {
	    String msg = "STOP: ";
	    System.out.println(msg + be.getMessage());
	    return;
	}
	
	if (target == null) {
	    target = project.getDefaultTarget();
	}

	try {
	    project.executeTarget(target);
	} catch (BuildException be) {
	    String msg = "STOP: ";
	    System.out.println(msg + be.getMessage());
	    return;
	}
	
	long finishTime = System.currentTimeMillis();
	long elapsedTime = finishTime - startTime;
	if (msgOutputLevel >= Project.MSG_INFO) {
	    System.out.println("Completed in " + (elapsedTime/1000)
			       + " seconds");
	}
    }

    private static void printUsage() {
	String msg = "javab [-help] [-quiet] [-verbose] " +
	    "[-buildfile buildfile] target";
	System.out.println(msg);
    }
}



