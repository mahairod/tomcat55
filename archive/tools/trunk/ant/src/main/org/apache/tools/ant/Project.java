/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.tools.ant;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;

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
    private Vector visitedTargets = new Vector();

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
	} catch (Exception e) {
	    String msg = "Could not create task of type: "
		 + taskType + " due to " + e;
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

        // Check to see if the target has already been visited. If so, assume
        // it to be up to date and do not execute the target.

        if (!visitedTargets.contains(target)) {

            // make sure any dependencies on this target are executed
            // first

            Enumeration enum = target.getDependencies();
            while (enum.hasMoreElements()) {
                String dependency = (String)enum.nextElement();
                Target prereqTarget = (Target)targets.get(dependency);
                executeTarget(prereqTarget);
            }

            log("Executing Target: " + target.getName(), MSG_INFO);

            visitedTargets.addElement(target);
            target.execute();
        } else {

            // XXX
            // note that we aren't catching circular dependencies right now.
            // This log message is the only indicator of a circular dependency.

            log("Skipping previously visited Target: " + target.getName(),
                MSG_VERBOSE);
        }
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
    
    /**
        Translate a path into its native (platform specific)
        path. This should be extremely fast, code is 
        borrowed from ECS project.
        <p>
        All it does is translate the : into ; and / into \ 
        if needed. In other words, it isn't perfect.
        
        @returns translated string or empty string if to_process is null or empty
        @author Jon S. Stevens <a href="mailto:jon@clearink.com">jon@clearink.com</a>
    */
    public static String translatePath(String to_process) {
        if ( to_process == null || to_process.length() == 0 )
            return "";
    
        StringBuffer bs = new StringBuffer(to_process.length() + 50);
        StringCharacterIterator sci = new StringCharacterIterator(to_process);
        String path = System.getProperty("path.separator");
        String file = System.getProperty("file.separator");
        String tmp = null;
        for (char c = sci.first(); c != CharacterIterator.DONE; c = sci.next()) {
            tmp = String.valueOf(c);
            
            if (tmp.equals(":") || tmp.equals(";"))
                tmp = path;
            else if (tmp.equals("/") || tmp.equals ("\\"))
                tmp = file;
            bs.append(tmp);
        }
        return(bs.toString());
    }
}





