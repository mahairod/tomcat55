package org.apache.tools.ant;

import java.io.File;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Command line entry point into Ant. This class is entered via the
 * cannonical `public static void main` entry point and reads the
 * command line arguments. It then assembles and executes an Ant
 * project.
 * <p>
 * If you integrating Ant into some other tool, this is not the class
 * to use as an entry point. Please see the source code of this
 * class to see how it manipulates the Ant project classes.
 *
 * @author duncan@x180.com
 */

public class Main {
    
    private static int msgOutputLevel = Project.MSG_INFO;
    private static File buildFile = new File("build.xml");
    private static String target = null;
    private static Properties definedProps = new Properties();

    /**
     * Command line entry point. This method kicks off the building
     * of a project object and executes a build using either a given
     * target or the default target.
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
	    } else if (arg.startsWith("-D")) {

		/* Interestingly enough, we get to here when a user
		 * uses -Dname=value. However, the JDK goes ahead
		 * and parses this out to args {"-Dname", "value"}
		 * so instead of parsing on "=", we just make the "-D"
		 * characters go away and skip one argument forward.
		 */
		
                String name = arg.substring(2, arg.length());
		String value = args[++i];
                definedProps.put(name, value);
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

    /**
     * Executes the build.
     */
    
    private static void runBuild() {

        // track when we started
        
	long startTime = System.currentTimeMillis();
	if (msgOutputLevel >= Project.MSG_INFO) {
	    System.out.println("Buildfile: " + buildFile);
	}
        
	Project project = new Project();
	project.setOutputLevel(msgOutputLevel);

        // first use the ProjectHelper to create the project object
        // from the given build file.
        
	try {
	    ProjectHelper.configureProject(project, buildFile);
	} catch (BuildException be) {
	    String msg = "BUILD CONFIG ERROR: ";
	    System.out.println(msg + be.getMessage());
	    System.exit(1);
	}

        // cycle through command line defined properties after the
        // build.xml file properties have been set so that command line
        // props take precedence
        
        Enumeration e = definedProps.keys();
        while (e.hasMoreElements()) {
            String arg = (String)e.nextElement();
            String value = (String)definedProps.get(arg);
            project.setProperty(arg, value);
        }

        // make sure that we have a target to execute
        
	if (target == null) {
	    target = project.getDefaultTarget();
	}

        // actually do some work
        
	try {
	    project.executeTarget(target);
	} catch (BuildException be) {
	    String msg = "BUILD FATAL ERROR: ";
	    System.out.println(msg + be.getMessage());
	    return;
	}

        // track our stop time and let the user know how long things
        // took.
        
	long finishTime = System.currentTimeMillis();
	long elapsedTime = finishTime - startTime;
	if (msgOutputLevel >= Project.MSG_INFO) {
	    System.out.println("Completed in " + (elapsedTime/1000)
			       + " seconds");
	}
    }

    /**
     * Prints the usage of how to use this class to System.out
     */
    
    private static void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("ant [options] [target]" + lSep);
        msg.append("Options: " + lSep);
        msg.append("  -help                  print this message" + lSep);
        msg.append("  -quiet                 be extra quiet" + lSep);
        msg.append("  -verbose               be extra verbose" + lSep);
        msg.append("  -buildfile <file>      use given buildfile" + lSep);
        msg.append("  -D<property>=<value>   use value for given property"
                   + lSep);     
	System.out.println(msg.toString());
    }
}



