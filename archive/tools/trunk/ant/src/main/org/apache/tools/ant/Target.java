package org.apache.tools.ant;

import java.util.Enumeration;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Target {

    private String name;
    private Vector dependencies = new Vector();
    private Vector tasks = new Vector();
    Project project;
    
    public void setProject( Project project) {
	this.project=project;
    }

    public Project getProject() {
	return project;
    }

    public void setDepends( String depS ) {
	if (depS.length() > 0) {
	    StringTokenizer tok =
		new StringTokenizer(depS, ",", false);
	    while (tok.hasMoreTokens()) {
		addDependency(tok.nextToken().trim());
	    }
	}
    }
    
    public void setAttribute(String name, Object value) {
	// XXX 
	if( value instanceof Task)
	    addTask( (Task)value);
	
    }
    
    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void addTask(Task task) {
	tasks.addElement(task);
    }

    public void addDependency(String dependency) {
	dependencies.addElement(dependency);
    }

    public Enumeration getDependencies() {
	return dependencies.elements();
    }

    public void execute() throws BuildException {
	Enumeration enum = tasks.elements();
	while (enum.hasMoreElements()) {
	    Task task = (Task)enum.nextElement();
	    task.execute();
	}
    }
}
