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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.catalina.loader.StandardClassLoader;
import org.apache.catalina.security.SecurityClassLoad;


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

    /**
     * Debugging detail level for processing the startup.
     */
    protected int debug = 0;
    protected String args[];

    // Construct the class loaders we will need
    protected ClassLoader commonLoader = null;
    protected ClassLoader catalinaLoader = null;
    protected ClassLoader sharedLoader = null;

    protected static final String CATALINA_TOKEN = "${catalina.home}";
    
    public void setDebug( int debug ) {
        this.debug=debug;
    }

    public void setArgs(String args[] ) {
        this.args=args;
    }


    // ----------------------------------------------------------- Main Program


    public void initClassLoaders() {
        try {

            /*
            File unpacked[] = new File[1];
            File packed[] = new File[1];
            File packed2[] = new File[2];
            ClassLoaderFactory.setDebug(debug);

            unpacked[0] = new File(getCatalinaHome(),
                                   "common" + File.separator + "classes");
            packed2[0] = new File(getCatalinaHome(),
                                  "common" + File.separator + "endorsed");
            packed2[1] = new File(getCatalinaHome(),
                                  "common" + File.separator + "lib");
            commonLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed2, null);

            unpacked[0] = new File(getCatalinaHome(),
                                   "server" + File.separator + "classes");
            packed[0] = new File(getCatalinaHome(),
                                 "server" + File.separator + "lib");
            catalinaLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);

            unpacked[0] = new File(getCatalinaBase(),
                                   "shared" + File.separator + "classes");
            packed[0] = new File(getCatalinaBase(),
                                 "shared" + File.separator + "lib");
            sharedLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);
            */

            ClassLoaderFactory.setDebug(debug);
            commonLoader = createClassLoader("common", null);
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);

        } catch (Throwable t) {

            log("Class loader creation threw exception", t);
            System.exit(1);

        }
    }


    private ClassLoader createClassLoader(String name, ClassLoader parent)
        throws Exception {

        String value = CatalinaProperties.getProperty(name + ".loader");
        if ((value == null) || (value.equals("")))
            return parent;

        ArrayList unpackedList = new ArrayList();
        ArrayList packedList = new ArrayList();

        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreElements()) {
            String repository = tokenizer.nextToken();
            boolean packed = false;
            if (repository.startsWith(CATALINA_TOKEN)) {
                repository = getCatalinaHome() 
                    + repository.substring(CATALINA_TOKEN.length());
            }
            if (repository.endsWith("*.jar")) {
                packed = true;
                repository = repository.substring
                    (0, repository.length() - "*.jar".length());
            }
            if (packed) {
                packedList.add(new File(repository));
            } else {
                unpackedList.add(new File(repository));
            }
        }

        File[] unpacked = (File[]) unpackedList.toArray(new File[0]);
        File[] packed = (File[]) packedList.toArray(new File[0]);

        return ClassLoaderFactory.createClassLoader(unpacked, packed, parent);

    }


    // ----------------------------------------------------------- Main Program

    public void execute() {
        // Set the debug flag appropriately
        for (int i = 0; i < args.length; i++)  {
            if ("-debug".equals(args[i]))
                setDebug( 1 );
        }
        
        // Configure catalina.base from catalina.home if not yet set
        if (System.getProperty("catalina.base") == null)
            System.setProperty("catalina.base", getCatalinaHome());

        this.initClassLoaders();

        Thread.currentThread().setContextClassLoader(catalinaLoader);

 
        // Load our startup class and call its process() method
        try {
            SecurityClassLoad.securityClassLoad(catalinaLoader);

            // Instantiate a startup class instance
            if (debug >= 1)
                log("Loading startup class");
            Class startupClass =
                catalinaLoader.loadClass
                ("org.apache.catalina.startup.Catalina");
            Object startupInstance = startupClass.newInstance();

            // Set the shared extensions class loader
            if (debug >= 1)
                log("Setting startup class properties");
            String methodName = "setParentClassLoader";
            Class paramTypes[] = new Class[1];
            paramTypes[0] = Class.forName("java.lang.ClassLoader");
            Object paramValues[] = new Object[1];
            paramValues[0] = sharedLoader;
            Method method =
                startupInstance.getClass().getMethod(methodName, paramTypes);
            method.invoke(startupInstance, paramValues);

            // Call the process() method
            if (debug >= 1)
                log("Calling startup class process() method");
            methodName = "process";
            paramTypes = new Class[1];
            paramTypes[0] = args.getClass();
            paramValues = new Object[1];
            paramValues[0] = args;
            method =
                startupInstance.getClass().getMethod(methodName, paramTypes);
            method.invoke(startupInstance, paramValues);

        } catch (Exception e) {
            System.out.println("Exception during startup processing");
            e.printStackTrace(System.out);
            System.exit(2);
        }
    }

    /**
     * The main program for the bootstrap.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {
        Bootstrap bootstrap=new Bootstrap();

        bootstrap.setArgs( args );
        bootstrap.execute();
    }


    /**
     * Get the value of the catalina.home environment variable.
     */
    protected String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
    protected String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }


    /**
     * Log a debugging detail message.
     *
     * @param message The message to be logged
     */
    protected void log(String message) {

        System.out.print("Bootstrap: ");
        System.out.println(message);

    }


    /**
     * Log a debugging detail message with an exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    protected void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }


}
