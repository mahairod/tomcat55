/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
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
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.catalina.startup;


import java.io.File;
import java.lang.reflect.Method;
import org.apache.catalina.loader.FileClassLoader;


/**
 * Boostrap loader for Catalina.  This application constructs a class loader
 * for use in loading the Catalina internal classes (by accumulating all of the
 * JAR files found in the "server" directory under "catalina.home"), and
 * starts the regular execution of the container.  The purpose of this
 * roundabout approach is to keep the Catalina internal classes (and any
 * other classes they depend on, such as an XML parser) out of the system
 * class path and therefore not visible to application level classes.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class Bootstrap {


    // ----------------------------------------------------------- Main Program


    /**
     * The main program for the bootstrap.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

	// Construct a new class loader for our internal classes
        Bootstrap dummy = new Bootstrap();
	FileClassLoader loader =
            new FileClassLoader(dummy.getClass().getClassLoader());

	// Add the "classes" subdirectory underneath "catalina.home"
	File classes = new File(System.getProperty("catalina.home"),
				"classes");
	if (classes.exists() && classes.canRead() &&
	    classes.isDirectory()) {
	    loader.addRepository(classes.getAbsolutePath());
	}

	// Add the JAR files in the "server" subdirectory as well
	File directory = new File(System.getProperty("catalina.home"),
				  "server");
	if (!directory.exists() || !directory.canRead() ||
	    !directory.isDirectory()) {
	    System.out.println("No 'server' directory to be processed");
	    System.exit(1);
	}
	String filenames[] = directory.list();
	for (int i = 0; i < filenames.length; i++) {
	    if (!filenames[i].toLowerCase().endsWith(".jar"))
		continue;
	    File file = new File(directory, filenames[i]);
	    loader.addRepository(file.getAbsolutePath());
	}

	// Load our startup class and call its process() method
	try {
	    Class startupClass =
		loader.loadClass("org.apache.catalina.startup.Catalina");
	    Object startupInstance = startupClass.newInstance();
	    String methodName = "process";
	    Class paramTypes[] = new Class[1];
	    paramTypes[0] = args.getClass();
	    Object paramValues[] = new Object[1];
	    paramValues[0] = args;
	    Method method =
		startupInstance.getClass().getMethod(methodName, paramTypes);
	    method.invoke(startupInstance, paramValues);
	} catch (Exception e) {
	    System.out.println("Exception during startup processing");
	    e.printStackTrace(System.out);
	    System.exit(2);
	}

	System.exit(0);

    }


}
