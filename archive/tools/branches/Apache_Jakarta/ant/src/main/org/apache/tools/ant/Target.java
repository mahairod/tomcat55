package org.apache.tools.ant;

import java.util.Enumeration;
import java.util.Vector;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Target {

    private String name;
    private Vector dependencies = new Vector();
    private Vector tasks = new Vector();

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
