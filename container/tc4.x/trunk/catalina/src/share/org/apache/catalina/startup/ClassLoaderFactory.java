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
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.catalina.loader.StandardClassLoader;


/**
 * <p>Utility class for building class loaders for Catalina.  The factory
 * method requires the following parameters in order to build a new class
 * loader (with suitable defaults in all cases):</p>
 * <ul>
 * <li>A set of directories containing unpacked classes (and resources)
 *     that should be included in the class loader's
 *     repositories, <strong>unless</strong> a trigger class (see below)
 *     is discovered in that directory.</li>
 * <li>A set of directories containing classes and resources in JAR files.
 *     Each readable JAR file discovered in these directories will be
 *     added to the class loader's repositories, <strong>unless</strong> a
 *     trigger class (see below) is discovered in that directory.</li>
 * <li><code>ClassLoader</code> instance that should become the parent of
 *     the new class loader.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class ClassLoaderFactory {


    // ------------------------------------------------------- Static Variables


    /**
     * Debugging detail level for processing the startup.
     */
    private static int debug = 0;


    /**
     * The set of trigger classes that will cause a proposed repository not
     * to be added if this class is visible to the class loader that loaded
     * this factory class.  Typically, trigger classes will be listed for
     * components that have been integrated into the JDK for later versions,
     * but where the corresponding JAR files are required to run on
     * earlier versions.
     */
    private static String[] triggers = {
        "com.sun.jndi.ldap.LdapCtxFactory",      // LDAP      added in 1.3
        "com.sun.net.ssl.internal.ssl.Provider", // JSSE      added in 1.4
        "javax.naming.Context",                  // JNDI      added in 1.3
        "javax.net.SocketFactory",               // JSSE      added in 1.4
        "javax.security.cert.X509Certificate",   // JSSE      added in 1.4
        "javax.sql.DataSource",                  // JDBC ext. added in 1.4
        // "javax.xml.parsers.DocumentBuilder",     // JAXP      added in 1.4
        "org.apache.catalina.startup.Bootstrap", // Don't load ourselves
        // "org.apache.crimson.jaxp.DocumentBuilderImpl",
                                                 // Crimson   added in 1.4
    };


    // ------------------------------------------------------ Static Properties


    /**
     * Return the debugging detail level.
     */
    public static int getDebug() {

        return (debug);

    }


    /**
     * Set the debugging detail level.
     *
     * @param newDebug The new debugging detail level
     */
    public static void setDebug(int newDebug) {

        debug = newDebug;

    }


    /**
     * Return the trigger class names that we check for.
     */
    public static String[] getTriggers() {

        return (triggers);

    }


    /**
     * Set the trigger class names that we check for.
     *
     * @param newTriggers The new trigger class names
     */
    public static void setTriggers(String newTriggers[]) {

        triggers = newTriggers;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param unpacked Array of pathnames to unpacked directories that should
     *  be added to the repositories of the class loader unless they contain
     *  one of the trigger classes, or <code>null</code> for no unpacked
     *  directories to be considered
     * @param packed Array of pathnames to directories containing JAR files
     *  that should be added to the repositories of the class loader unless
     *  they contain one of the trigger classes, or <code>null</code> for no
     *  directories of JAR files to be considered
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(File unpacked[],
                                                File packed[],
                                                ClassLoader parent)
        throws Exception {

        if (debug >= 1)
            log("Creating new class loader");

        // Construct the "class path" for this class loader
        ArrayList list = new ArrayList();

        // Add unpacked directories that do not contain trigger classes
        if (unpacked != null) {
            for (int i = 0; i < unpacked.length; i++)  {
                File file = unpacked[i];
                if (!file.isDirectory() || !file.exists() || !file.canRead())
                    continue;
                if (!validateDirectory(file)) {
                    if (debug >= 1)
                        log("  Skipping directory " + file.getAbsolutePath());
                    continue;
                }
                if (debug >= 1)
                    log("  Including directory " + file.getAbsolutePath());
                URL url = new URL("file", null,
                                  file.getCanonicalPath() + File.separator);
                list.add(url.toString());
            }
        }

        // Add packed directory JAR files that do not contain trigger classes
        if (packed != null) {
            for (int i = 0; i < packed.length; i++) {
                File directory = packed[i];
                if (!directory.isDirectory() || !directory.exists() ||
                    !directory.canRead())
                    continue;
                String filenames[] = directory.list();
                for (int j = 0; j < filenames.length; j++) {
                    String filename = filenames[j].toLowerCase();
                    if (!filename.endsWith(".jar"))
                        continue;
                    File file = new File(directory, filenames[j]);
                    if (!validateJarFile(file)) {
                        if (debug >= 1)
                            log("  Skipping jar file " +
                                file.getAbsolutePath());
                        continue;
                    }
                    if (debug >= 1)
                        log("  Including jar file " + file.getAbsolutePath());
                    URL url = new URL("file", null,
                                      file.getCanonicalPath());
                    list.add(url.toString());
                }
            }
        }

        // Construct the class loader itself
        String array[] = (String[]) list.toArray(new String[list.size()]);
        ClassLoader classLoader = null;
        if (parent == null)
            classLoader = new StandardClassLoader(array);
        else
            classLoader = new StandardClassLoader(array, parent);
        return (classLoader);

    }



    /**
     * Check the specified directory, and return <code>true</code> if it does
     * not contain any of the trigger classes.
     *
     * @param directory The directory to be checked
     *
     * @exception IOException if an input/output error occurs
     */
    public static boolean validateDirectory(File directory)
        throws IOException {

        if (triggers == null)
            return (true);
        for (int i = 0; i < triggers.length; i++) {
            Class clazz = null;
            try {
                clazz = Class.forName(triggers[i]);
            } catch (Throwable t) {
                clazz = null;
            }
            if (clazz == null)
                continue;
            File file = new File(directory,
                                 triggers[i].replace('.', File.separatorChar) +
                                 ".class");
            if (debug >= 2)
                log(" Checking for " + file.getAbsolutePath());
            if (file.exists() && file.canRead())
                return (false);
        }
        return (true);

    }


    /**
     * Check the specified JAR file, and return <code>true</code> if it does
     * not contain any of the trigger classes.
     *
     * @param jarfile The JAR file to be checked
     *
     * @exception IOException if an input/output error occurs
     */
    public static boolean validateJarFile(File jarfile)
        throws IOException {

        if (triggers == null)
            return (true);
        JarFile jarFile = new JarFile(jarfile);
        for (int i = 0; i < triggers.length; i++) {
            Class clazz = null;
            try {
                clazz = Class.forName(triggers[i]);
            } catch (Throwable t) {
                clazz = null;
            }
            if (clazz == null)
                continue;
            String name = triggers[i].replace('.', '/') + ".class";
            if (debug >= 2)
                log(" Checking for " + name);
            JarEntry jarEntry = jarFile.getJarEntry(name);
            if (jarEntry != null) {
                jarFile.close();
                return (false);
            }
        }
        jarFile.close();
        return (true);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message for this class.
     *
     * @param message Message to be logged
     */
    private static void log(String message) {

        System.out.print("ClassLoaderFactory:  ");
        System.out.println(message);

    }


    /**
     * Log a message and exception for this class.
     *
     * @param message Message to be logged
     * @param exception Exception to be logged
     */
    private static void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }




}
