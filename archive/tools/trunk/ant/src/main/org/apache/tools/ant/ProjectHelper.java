package org.apache.tools.ant;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import com.sun.xml.parser.Resolver;
import com.sun.xml.tree.XmlDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
    
/**
 *
 *
 * @author duncan@x180.com
 */

class ProjectHelper {

    static void configureProject(Project project, File buildFile)
	throws BuildException
    {

	// XXX
	// need to make this a bit more parser independent...
	
	InputSource input;
	XmlDocument doc;

	try {
	    input = Resolver.createInputSource(buildFile);
	    doc = XmlDocument.createXmlDocument(input, false);
	} catch (IOException ioe) {
	    String msg = "Can't open config file: " + buildFile +
		" due to: " + ioe;
	    throw new BuildException(msg);
	} catch (SAXException se) {
	    String msg = "Can't open config file: " + buildFile +
		" due to: " + se;
	    throw new BuildException(msg);
	}

	Element root = doc.getDocumentElement();

	// sanity check, make sure that we have the right element
	// as we aren't validating the input

	if (!root.getTagName().equals("project")) {
	    String msg = "Config file is not of expected XML type";
	    throw new BuildException(msg);
	}

	project.setName(root.getAttribute("name"));
	project.setDefaultTarget(root.getAttribute("default"));

	String baseDir = root.getAttribute("basedir");
	if (!baseDir.equals("")) {
	    try {
		project.setBaseDir(new File(baseDir).getCanonicalFile());
	    } catch (IOException ioe) {
		String msg = "Can't set basedir " + baseDir + " due to " +
		    ioe.getMessage();
		throw new BuildException(msg);
	    }
	} else {
	    project.setBaseDir(buildFile.getAbsoluteFile().getParentFile());
	}

	// set up any properties that may be in the config file

	configureProperties(project, root);
	
	// set up any task defs that may be in the config file

	configureTaskDefs(project, root);

	// set up the targets into the project

	configureTargets(project, root);
    }

    private static void configureProperties(Project project, Element root)
	throws BuildException 
    {
	NodeList list = root.getElementsByTagName("property");
	for (int i = 0; i < list.getLength(); i++) {
	    Element element = (Element)list.item(i);
	    String propertyName = element.getAttribute("name");
	    String propertyValue = element.getAttribute("value");

	    // sanity check
	    if (propertyName.equals("") || propertyValue.equals("")) {
		String msg = "name or value attributes of property element " +
		    "are undefined";
		throw new BuildException(msg);
	    }

	    project.setProperty(propertyName, propertyValue);
	}
    }

    private static void configureTaskDefs(Project project, Element root)
	throws BuildException
    {
	NodeList list = root.getElementsByTagName("taskdef");
	for (int i = 0; i < list.getLength(); i++) {
	    Element element = (Element)list.item(i);
	    String taskName = element.getAttribute("name");
	    String taskClassName = element.getAttribute("class");
	    
	    // sanity check
	    if (taskName.equals("") || taskClassName.equals("")) {
		String msg = "name or class attributes of taskdef element "
		    + "are undefined";
		throw new BuildException(msg);
	    }
	    
	    try {
		Class taskClass = Class.forName(taskClassName);
		project.addTaskDefinition(taskName, taskClass);
	    } catch (ClassNotFoundException cnfe) {
		String msg = "taskdef class " + taskClassName +
		    " cannot be found";
		throw new BuildException(msg);
	    }
	}
    }

    private static void configureTargets(Project project, Element root)
	throws BuildException
    {
	NodeList list = root.getElementsByTagName("target");
	for (int i = 0; i < list.getLength(); i++) {
	    Element element = (Element)list.item(i);
	    String targetName = element.getAttribute("name");
	    String targetDep = element.getAttribute("depends");

	    // all targets must have a name
	    if (targetName.equals("")) {
		String msg = "target element appears without a name attribute";
		throw new BuildException(msg);
	    }

	    Target target = new Target();
	    target.setName(targetName);
	    project.addTarget(targetName, target);
	    
	    // take care of dependencies
	    
	    if (targetDep.length() > 0) {
		StringTokenizer tok =
		    new StringTokenizer(targetDep, ",", false);
		while (tok.hasMoreTokens()) {
		    target.addDependency(tok.nextToken().trim());
		}
	    }

	    // populate target with tasks

	    configureTasks(project, target, element);
	}
    }

    private static void configureTasks(Project project,
				       Target target,
				       Element targetElement)
	throws BuildException
    {
	NodeList list = targetElement.getChildNodes();
	for (int i = 0; i < list.getLength(); i++) {
	    Node node = list.item(i);

	    // right now, all we are interested in is element nodes
	    // not quite sure what to do with others except drop 'em

	    if (node.getNodeType() == Node.ELEMENT_NODE) {
		Element element = (Element)node;
		String taskType = element.getTagName();
		
		// XXX
		// put in some sanity checking
		
		Task task = project.createTask(taskType);

		// get the attributes of this element and reflect them
		// into the task

		NamedNodeMap nodeMap = element.getAttributes();
		configureTask(task, nodeMap);
		target.addTask(task);
	    }
	}
    }

    private static void configureTask(Task task, NamedNodeMap nodeMap)
	throws BuildException
    {
	// XXX
	// instead of doing this introspection each time around, I
	// should have a helper class to keep this info around for
	// each kind of class
	
	Hashtable propertySetters = new Hashtable();
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(task.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + task.getClass();
	    throw new BuildException(msg);
	}

	PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
	for (int i = 0; i < pda.length; i++) {
	    PropertyDescriptor pd = pda[i];
	    String property = pd.getName();
	    Method setMethod = pd.getWriteMethod();
	    if (setMethod != null) {

		// make sure that there's only 1 param and that it
		// takes a String object, all other setMethods need
		// to get screened out

		Class[] ma =setMethod.getParameterTypes();
		if (ma.length == 1) {
		    Class c = ma[0];
		    if (c.getName().equals("java.lang.String")) {
			propertySetters.put(property, setMethod);
		    }
		}
	    }
	}

	for (int i = 0; i < nodeMap.getLength(); i++) {
	    Node node = nodeMap.item(i);

	    // these should only be attribs, we won't see anything
	    // else here.

	    if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
		Attr attr = (Attr)node;

		// reflect these into the task

		Method setMethod = (Method)propertySetters.get(attr.getName());
		if (setMethod == null) {
		    String msg = "Configuration property \"" + attr.getName() +
			"\" does not have a setMethod in " + task.getClass();
		    throw new BuildException(msg);
		}
		
		try {
		    setMethod.invoke(task, new String[] {attr.getValue()});
		} catch (IllegalAccessException iae) {
		    String msg = "Error setting value for attrib: " +
			attr.getName();
		    throw new BuildException(msg);
		} catch (InvocationTargetException ie) {
		    String msg = "Error setting value for attrib: " +
			attr.getName();
		    throw new BuildException(msg);		    
		}
	    }
	}
    }
}









