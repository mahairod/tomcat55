package org.apache.tools.ant;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Central representation of an Ant project. This class defines a
 * Ant project with all of it's targets and tasks. It also provides
 * the mechanism to kick off a build using a particular target name.
 * <p>
 * This class also encapsulates methods which allow Files to be refered
 * to using abstract path names which are translated to native system
 * file paths at runtime as well as defining various project properties.
 * 
 * @author duncan@x180.com
 */

public class Project {

    public static final int MSG_ERR = 0;
    public static final int MSG_WARN = 1;
    public static final int MSG_INFO = 2;
    public static final int MSG_VERBOSE = 3;

    private static String javaVersion;

    private String name;
    private PrintStream out = System.out;
    private int msgOutputLevel = MSG_INFO;

    private Hashtable properties = new Hashtable();
    private String defaultTarget;
    private Hashtable taskClassDefinitions = new Hashtable();
    private Hashtable targets = new Hashtable();
    private File baseDir;
    
    public Project() {
        detectJavaVersion();
	String defs = "/org/apache/tools/ant/taskdefs/defaults.properties";
	try {
	    Properties props = new Properties();
	    InputStream in = this.getClass()
		.getResourceAsStream(defs);
	    props.load(in);
	    in.close();
	    Enumeration enum = props.propertyNames();
	    while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();
		String value = props.getProperty(key);
		try {
		    Class taskClass = Class.forName(value);
		    addTaskDefinition(key, taskClass);
		} catch (ClassNotFoundException cnfe) {
		    // ignore...
		}
	    }
       	} catch (IOException ioe) {
	    String msg = "Can't load default task list";
	    System.out.println(msg);
	    System.exit(1);
	}
    }
    
    public void setOutput(PrintStream out) {
	this.out = out;
    }

    public void setOutputLevel(int msgOutputLevel) {
	this.msgOutputLevel = msgOutputLevel;
    }
    
    public void log(String msg) {
	log(msg, MSG_INFO);
    }

    public void log(String msg, int msgLevel) {
	if (msgLevel <= msgOutputLevel) {
	    out.println(msg);
	}
    }

    public void log(String msg, String tag, int msgLevel) {
	if (msgLevel <= msgOutputLevel) {
	    out.println(msg);
	}
    }

    public void setProperty(String name, String value) {
        log("Setting project property: " + name + " to " +
            value, MSG_VERBOSE);
	properties.put(name, value);
    }

    public String getProperty(String name) {
	String property = (String)properties.get(name);
	if (property == null) {
	    property = System.getProperty(name);
	}
	return property;
    }
    
    public void setDefaultTarget(String defaultTarget) {
	this.defaultTarget = defaultTarget;
    }

    // deprecated, use setDefault
    public String getDefaultTarget() {
	return defaultTarget;
    }

    // match the attribute name
    public void setDefault(String defaultTarget) {
	this.defaultTarget = defaultTarget;
    }
    

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    // match basedir attribute in xml
    public void setBasedir( String baseD ) throws BuildException {
	try {
	    setBaseDir(new File( new File(baseD).getCanonicalPath()));
	} catch (IOException ioe) {
	    String msg = "Can't set basedir " + baseDir + " due to " +
		ioe.getMessage();
	    throw new BuildException(msg);
	}
    }
    
    public void setBaseDir(File baseDir) {
	this.baseDir = baseDir;
	String msg = "Project base dir set to: " + baseDir;
	log(msg, MSG_INFO);
    }

    public File getBaseDir() {
	if(baseDir==null) {
	    try {
		setBasedir(".");
	    } catch(BuildException ex) {ex.printStackTrace();}
	}
	return baseDir;
    }
    

    public static String getJavaVersion() {
        return javaVersion;
    }

    private void detectJavaVersion() {

        // Determine the Java version by looking at available classes
        // java.lang.StrictMath was introduced in JDK 1.3
        // java.lang.ThreadLocal was introduced in JDK 1.2
        // java.lang.Void was introduced in JDK 1.1
        // Count up version until a NoClassDefFoundError ends the try

        try {
            javaVersion = "1.0";
            Class.forName("java.lang.Void");
            javaVersion = "1.1";
            Class.forName("java.lang.ThreadLocal");  
            javaVersion = "1.2";
            Class.forName("java.lang.StrictMath");
            javaVersion = "1.3";
        }
        catch (ClassNotFoundException cnfe) {
            // swallow as we've hit the max class version that
            // we have
        }
        log("Detected Java Version: " + javaVersion);
    }

    public void addTaskDefinition(String taskName, Class taskClass) {
	String msg = " +User task: " + taskName + "     " +
	    taskClass.getName();
	log(msg, MSG_VERBOSE);
	taskClassDefinitions.put(taskName, taskClass);
    }

    public void addTarget(Target target) {
	String msg = " +Target: " + target.getName();
	log(msg, MSG_VERBOSE);
	targets.put(target.getName(), target);
    }
    
    public void addTarget(String targetName, Target target) {
	String msg = " +Target: " + targetName;
	log(msg, MSG_VERBOSE);
	targets.put(targetName, target);
    }

    public Task createTask(String taskType) throws BuildException {
	Class c = (Class)taskClassDefinitions.get(taskType);

	// XXX
	// check for nulls, other sanity

	try {
	    Task task = (Task)c.newInstance();
	    task.setProject(this);
	    String msg = "   +Task: " + taskType;
	    log (msg, MSG_VERBOSE);
	    return task;
	} catch (IllegalAccessException iae) {
	    String msg = "Could not create task of type: "
		 + taskType + " due to " + iae;
	    throw new BuildException(msg);
	} catch (InstantiationException ie) {
	    String msg = "Could not create task of type: "
		 + taskType + " due to " + ie;
	    throw new BuildException(msg);
	}
	
    }
    
    public void executeTarget(String targetName) throws BuildException {

	// sanity check ourselves, if we've been asked to build nothing
	// then we should complain
	
	if (targetName == null) {
	    String msg = "No target specified";
	    throw new BuildException(msg);
	}

	Target target = (Target)targets.get(targetName);

	if (target == null) {
	    String msg = "Target " + targetName + " does not exist in this " +
		"project";
	    throw new BuildException(msg);
	}
	executeTarget(target);
    }

    public void executeTarget(Target target) throws BuildException {

	// make sure any dependencies on this target are executed
	// first

	// XXX
	// note that we aren't catching circular dependencies
	// right now... The user will get a hell of an error message
	// so it's not too bad..
	
	Enumeration enum = target.getDependencies();
	while (enum.hasMoreElements()) {
	    String dependency = (String)enum.nextElement();
	    Target prereqTarget = (Target)targets.get(dependency);
	    executeTarget(prereqTarget);
	}
	
	log("Executing Target: " + target.getName(), MSG_INFO);

	target.execute();
    }

    public File resolveFile(String fileName) {
	// deal with absolute files
	if(fileName.startsWith("/") ) return new File( fileName );
	
	File file = new File(baseDir.getAbsolutePath());
	StringTokenizer tok = new StringTokenizer(fileName, "/", false);
	while (tok.hasMoreTokens()) {
	    String part = tok.nextToken();
	    if (part.equals("..")) {
		file = new File(file.getParent());
	    } else {
		file = new File(file, part);
	    }
	}

	try {
	    return new File(file.getCanonicalPath());
	}
	catch (IOException e) {
	    log("IOException getting canonical path for " + file + ": " +
                e.getMessage(), MSG_ERR);
	    return new File(file.getAbsolutePath());
	}
    }
}










