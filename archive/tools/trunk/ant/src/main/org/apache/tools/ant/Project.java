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
 * 
 *
 * @author duncan@x180.com
 */

public class Project {

    public static final int MSG_ERR = 0;
    public static final int MSG_WARN = 1;
    public static final int MSG_INFO = 2;
    public static final int MSG_VERBOSE = 3;

    private String name;
    private PrintStream out = System.out;
    private int msgOutputLevel = MSG_INFO;

    private Hashtable properties = new Hashtable();
    private String defaultTarget;
    private Hashtable taskClassDefinitions = new Hashtable();
    private Hashtable targets = new Hashtable();
    private File baseDir;
    
    public Project() {
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

    public void setProperty(String name, String value) {
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
    
    public String getDefaultTarget() {
	return defaultTarget;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setBaseDir(File baseDir) {
	this.baseDir = baseDir;
	String msg = "Project base dir set to: " + baseDir;
	log(msg, MSG_INFO);
    }

    public File getBaseDir() {
	return baseDir;
    }
    
    public void addTaskDefinition(String taskName, Class taskClass) {
	String msg = " +User task: " + taskName + "     " +
	    taskClass.getName();
	log(msg, MSG_VERBOSE);
	taskClassDefinitions.put(taskName, taskClass);
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
	File file = baseDir.getAbsoluteFile();
	StringTokenizer tok = new StringTokenizer(fileName, "/", false);
	while (tok.hasMoreTokens()) {
	    String part = tok.nextToken();
	    if (part.equals("..")) {
		file = new File(file.getParent());
	    } else {
		file = new File(file, part);
	    }
	}
	return file.getAbsoluteFile();
    }
}










