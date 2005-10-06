/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.startup;


import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

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
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public final class Bootstrap {


    // -------------------------------------------------------------- Constants


    protected static final String CATALINA_HOME_TOKEN = "${catalina.home}";
    protected static final String CATALINA_BASE_TOKEN = "${catalina.base}";


    // ------------------------------------------------------- Static Variables


    private static final String JMX_ERROR_MESSAGE =
        "Due to new licensing guidelines mandated by the Apache Software\n"
        + "Foundation Board of Directors, a JMX implementation can no longer\n"
        + "be distributed with the Apache Tomcat binaries. As a result, you \n"
        + "must download a JMX 1.2 implementation (such as the Sun Reference\n"
        + "Implementation) and copy the JAR containing the API and \n"
        + "implementation of the JMX specification to: \n" 
        + "${catalina.home}/bin/jmx.jar";


    /**
     * Daemon object used by main.
     */
    private static Bootstrap daemon = null;


    // -------------------------------------------------------------- Variables


    /**
     * Debugging detail level for processing the startup.
     */
    protected int debug = 0;


    /**
     * Daemon reference.
     */
    private Object catalinaDaemon = null;


    protected ClassLoader commonLoader = null;
    protected ClassLoader catalinaLoader = null;
    protected ClassLoader sharedLoader = null;


    // -------------------------------------------------------- Private Methods


    private void initClassLoaders() {
        try {
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
        ArrayList urlList = new ArrayList();

        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreElements()) {
            String repository = tokenizer.nextToken();

            // Local repository
            boolean packed = false;
            if (repository.startsWith(CATALINA_HOME_TOKEN)) {
                repository = getCatalinaHome() 
                    + repository.substring(CATALINA_HOME_TOKEN.length());
            } else if (repository.startsWith(CATALINA_BASE_TOKEN)) {
                repository = getCatalinaBase() 
                    + repository.substring(CATALINA_BASE_TOKEN.length());
            }

            // Check for a JAR URL repository
            try {
                urlList.add(new URL(repository));
                continue;
            } catch (MalformedURLException e) {
                // Ignore
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
        URL[] urls = (URL[]) urlList.toArray(new URL[0]);

        ClassLoader classLoader = ClassLoaderFactory.createClassLoader
            (unpacked, packed, urls, parent);

        // Retrieving MBean server
        MBeanServer mBeanServer = null;
        if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
            mBeanServer = 
                (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
        } else {
            mBeanServer = MBeanServerFactory.createMBeanServer();
        }

        // Register the server classloader
        ObjectName objectName = 
            new ObjectName("Catalina:type=ServerClassLoader,name=" + name);
        mBeanServer.registerMBean(classLoader, objectName);

        return classLoader;

    }


    /**
     * Initialize daemon.
     */
    public void init()
        throws Exception
    {

        // Set Catalina path
        setCatalinaHome();
        setCatalinaBase();

        initClassLoaders();

        Thread.currentThread().setContextClassLoader(catalinaLoader);

        SecurityClassLoad.securityClassLoad(catalinaLoader);

        // Load our startup class and call its process() method
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

        catalinaDaemon = startupInstance;

    }


    /**
     * Load daemon.
     */
    private void load(String[] arguments)
        throws Exception {

        // Call the load() method
        String methodName = "load";
        Object param[];
        Class paramTypes[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method = 
            catalinaDaemon.getClass().getMethod(methodName, paramTypes);
        if (debug >= 1)
            log("Calling startup class " + method);
        method.invoke(catalinaDaemon, param);

    }


    // ----------------------------------------------------------- Main Program


    /**
     * Load the Catalina daemon.
     */
    public void init(String[] arguments)
        throws Exception {

        // Read the arguments
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-debug")) {
                    debug = 1;
                }
            }
        }

        init();
        load(arguments);

    }


    /**
     * Start the Catalina daemon.
     */
    public void start()
        throws Exception {
        if( catalinaDaemon==null ) init();

        Method method = catalinaDaemon.getClass().getMethod("start", null);
        method.invoke(catalinaDaemon, null);

    }


    /**
     * Stop the Catalina Daemon.
     */
    public void stop()
        throws Exception {

        Method method = catalinaDaemon.getClass().getMethod("stop", null);
        method.invoke(catalinaDaemon, null);

    }


    /**
     * Stop the standlone server.
     */
    public void stopServer()
        throws Exception {

        Method method = 
            catalinaDaemon.getClass().getMethod("stopServer", null);
        method.invoke(catalinaDaemon, null);

    }


   /**
     * Stop the standlone server.
     */
    public void stopServer(String[] arguments)
        throws Exception {

        Object param[];
        Class paramTypes[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method = 
            catalinaDaemon.getClass().getMethod("stopServer", paramTypes);
        method.invoke(catalinaDaemon, param);

    }


    /**
     * Set flag.
     */
    public void setAwait(boolean await)
        throws Exception {

        Class paramTypes[] = new Class[1];
        paramTypes[0] = Boolean.TYPE;
        Object paramValues[] = new Object[1];
        paramValues[0] = new Boolean(await);
        Method method = 
            catalinaDaemon.getClass().getMethod("setAwait", paramTypes);
        method.invoke(catalinaDaemon, paramValues);

    }

    public boolean getAwait()
        throws Exception
    {
        Class paramTypes[] = new Class[0];
        Object paramValues[] = new Object[0];
        Method method =
            catalinaDaemon.getClass().getMethod("getAwait", paramTypes);
        Boolean b=(Boolean)method.invoke(catalinaDaemon, paramValues);
        return b.booleanValue();
    }


    /**
     * Destroy the Catalina Daemon.
     */
    public void destroy() {

        // FIXME

    }


    /**
     * Main method, used for testing only.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

        try {
            // Attempt to load JMX class
            new ObjectName("test:foo=bar");
        } catch (Throwable t) {
            System.out.println(JMX_ERROR_MESSAGE);
            try {
                // Give users some time to read the message before exiting
                Thread.sleep(5000);
            } catch (Exception ex) {
            }
            return;
        }

        if (daemon == null) {
            daemon = new Bootstrap();
            try {
                daemon.init();
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }
        }

        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }

            if (command.equals("startd")) {
                args[0] = "start";
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stopd")) {
                args[0] = "stop";
                daemon.stop();
            } else if (command.equals("start")) {
                daemon.setAwait(true);
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stop")) {
                daemon.stopServer(args);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public void setCatalinaHome(String s) {
        System.setProperty( "catalina.home", s );
    }

    public void setCatalinaBase(String s) {
        System.setProperty( "catalina.base", s );
    }


    /**
     * Set the <code>catalina.base</code> System property to the current
     * working directory if it has not been set.
     */
    private void setCatalinaBase() {

        if (System.getProperty("catalina.base") != null)
            return;
        if (System.getProperty("catalina.home") != null)
            System.setProperty("catalina.base",
                               System.getProperty("catalina.home"));
        else
            System.setProperty("catalina.base",
                               System.getProperty("user.dir"));

    }


    /**
     * Set the <code>catalina.home</code> System property to the current
     * working directory if it has not been set.
     */
    private void setCatalinaHome() {

        if (System.getProperty("catalina.home") != null)
            return;
        File bootstrapJar = 
            new File(System.getProperty("user.dir"), "bootstrap.jar");
        if (bootstrapJar.exists()) {
            try {
                System.setProperty
                    ("catalina.home", 
                     (new File(System.getProperty("user.dir"), ".."))
                     .getCanonicalPath());
            } catch (Exception e) {
                // Ignore
                System.setProperty("catalina.home",
                                   System.getProperty("user.dir"));
            }
        } else {
            System.setProperty("catalina.home",
                               System.getProperty("user.dir"));
        }

    }


    /**
     * Get the value of the catalina.home environment variable.
     */
    public static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
    public static String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }


    /**
     * Log a debugging detail message.
     *
     * @param message The message to be logged
     */
    protected static void log(String message) {

        System.out.print("Bootstrap: ");
        System.out.println(message);

    }


    /**
     * Log a debugging detail message with an exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    protected static void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }


}
