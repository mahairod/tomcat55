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

package org.apache.catalina.util;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.Logger;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.Resource;

// JNDI Imports
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.Binding;


/**
 *  Ensures that all extension dependies are resolved for a WEB application
 *  are met. This class builds a master list of extensions available to an
 *  applicaiton and then validates those extensions.
 *
 *  See http://java.sun.com/j2se/1.4/docs/guide/extensions/spec.html for
 *  a detailed explanation of the extension mechanism in Java.
 *
 * @author Greg Murray
 * @author Justyna Horwat
 * @version $Revision$ $Date$
 *
 */
public final class ExtensionValidator {

    // ------------------------------------------------------------- Properties

    
    private static ExtensionValidator validator = null;
    private static HashMap containerAvailableExtensions = null;
    private static ArrayList containerManifestResources = null;
    private static ResourceBundle messages = null;
    
    /*
     *  Access to this class can only be made through the factory method
     *  getInstance()
     *
     *  This private constructor loads the container level extensions that are
     *  available to all web applications. This method scans all extension 
     *  directories available to via the "java.ext.dirs" System property. 
     *
     *  The System Class-Path is also scanned for jar files that may contain 
     *  available extensions. The system extensions are loaded only the 
     *  first time an instance of the ExtensionValidator is created.
     */
    private ExtensionValidator() {
       
        // load the container level extensions
        containerManifestResources = new ArrayList();
        // check for container level optional packages
        String systemClasspath = System.getProperties().
                                 getProperty("java.class.path");

        StringTokenizer strTok = new StringTokenizer(systemClasspath, 
                                                     File.pathSeparator);
        ArrayList items = new ArrayList();
        // build a list of jar files in the classpath
        while (strTok.hasMoreTokens()) {
            String classpathItem = strTok.nextToken();
            if (classpathItem.toLowerCase().endsWith(".jar")) {
                items.add(classpathItem);
            }
        }
        // get the files in the extensions directory
        String extensionsDir = System.getProperties().
                                getProperty("java.ext.dirs");
        StringTokenizer extensionsTok = null;
        if (extensionsDir != null) {
            extensionsTok = new StringTokenizer(extensionsDir, 
                                                 File.pathSeparator);
        }
        while ((extensionsTok != null) && extensionsTok.hasMoreTokens()) {
            String targetDir = extensionsTok.nextToken();
            // check if the directory exits
            if (((new File(targetDir)).exists()) && 
                (new File(targetDir)).isDirectory()) {
                // get a file list
                File[] files = (new File(targetDir)).listFiles();
                // see if any file is a jar file
                for (int loop = 0; loop < files.length; loop++) {
                    if (files[loop].getName().toLowerCase().endsWith(".jar")) {
                        items.add(files[loop].getAbsolutePath());
                        Manifest manifest = getManifest(files[loop]);
                        if (manifest != null)  {
                            ManifestResource mre = new ManifestResource
                                (files[loop].getAbsolutePath(), manifest, 
                                 ManifestResource.SYSTEM);
                            containerManifestResources.add(mre);
                        }
                    }
                }
            }
        }
    }

    // --------------------------------------------------------- Public Methods
        
    /**
     *  Runtime validation of a Web Applicaiton.
     *
     *  This method uses JNDI to look up the resources located under a 
     *  <code>DirContext</code>. It locates Web Application MANIFEST.MF 
     *  file in the /META-INF/ directory of the application and all 
     *  MANIFEST.MF files in each JAR file located in the WEB-INF/lib 
     *  directory and creates an <code>ArrayList</code> of 
     *  <code>ManifestResorce<code> objects. These objects are then passed 
     *  to the validateManifestResources method for validation.
     *
     *  @param DirContext The JNDI root of the Web Application
     *  @param StandardContext The context from which the Logger and path 
     *                         to the application
     *
     *  @return true if all required extensions satisfied
     *
     */
    public static synchronized boolean validateApplication(
                                           DirContext dirContext, 
                                           StandardContext context) {
        String appName = context.getPath();
        Logger logger = context.getLogger();
        ArrayList appManifestResources = new ArrayList();
        ManifestResource appManifestResource = null;
        // If the application context is null it does not exist and 
        // therefore is not valid
        if (dirContext == null) return false;
        // Find the Mainfest for the Web Applicaiton
        try {
            NamingEnumeration  wne = null;
            wne = dirContext.listBindings("/META-INF/");
            Binding binding = (Binding)wne.nextElement();
            if (binding.getName().toUpperCase().equals("MANIFEST.MF")) {
                Resource resource = (Resource)dirContext.lookup
                                    ("/META-INF/" + binding.getName());
                InputStream inputStream = resource.streamContent();
                Manifest manifest = new Manifest(inputStream);
                ManifestResource mre = new ManifestResource
                    (getMessage("extensionValidator.web-application-manifest", 
                     logger), manifest, ManifestResource.WAR);
                appManifestResources.add(mre);
            } 
        } catch (javax.naming.NamingException nex) {
            // Application does not contain a MANIFEST.MF file
        }  catch (java.io.IOException iox) {
            // Unable to load MANIFEST.MF file
        }
        // Locate the Manifests for all necessary Jar Files in the Application
        ArrayList jarEntries = new ArrayList();
        NamingEnumeration  ne = null;
        try {
            if (dirContext != null) ne = dirContext.listBindings("WEB-INF/lib/");
                while ((ne != null) && ne.hasMoreElements()) {
                    Binding  binding = (Binding)ne.nextElement();
                    if (binding.getName().toLowerCase().endsWith(".jar")) {
                        Resource resource = (Resource)dirContext.lookup
                            ("/WEB-INF/lib/" + binding.getName());
                        try {
                            InputStream in = resource.streamContent();
                            JarInputStream jin = new JarInputStream(in);
                            Manifest jmanifest = jin.getManifest();
                            if (jmanifest != null) {
                                ManifestResource mre = new ManifestResource
                                    (binding.getName(), jmanifest, 
                                    ManifestResource.APPLICATION);
                                appManifestResources.add(mre);
                            }
                        } catch (java.io.IOException iox) {
                            // do not do anything... go to the next entry
                        }
                    }
                }
        } catch (javax.naming.NamingException nex) {
            // Jump out of the check for this application because it 
            // has no resources
        }
        return validateManifestResources(appName,appManifestResources, logger);
    }
    
    /**
     *  Return an instance of the ExtensionValidator. 
     *  The ExtensionValidator is a singleton.
     */
    public static ExtensionValidator getInstance() {
        if (validator == null) {
          validator = new ExtensionValidator();
        }
        return validator;
    }
    
    // -------------------------------------------------------- Private Methods

    /**
     * Validates a <code>ArrayList</code> of <code>ManifestResource</code> 
     * objects. This method requires an application name (which is the 
     * context root of the application at runtime).  
     *
     * A <code>Logger</code> is required for the output of error messages. 
     * <code>false</false> is returned if the extension depeendencies
     * represented by any given <code>ManifestResource</code> objects 
     * is not met.
     *
     * This method should also provide static validation of a Web Applicaiton 
     * if provided with the necessary parameters.
     *
     * @param String The name of the Application that will appear in the 
     *               error messages
     * @param ArrayList A list of <code>ManifestResource</code> objects 
     *                  to be validated.
     * @param Logger A logger to which failure messages are logged.
     *
     * @return true if manifest resource file requirements are met
     *
     */
    private static boolean validateManifestResources(String appName, 
                                                     ArrayList resources, 
                                                     Logger logger) {
        boolean passes = true;
        int failureCount = 0;
        
        HashMap availableExtensions = null;
        Iterator it = resources.iterator();
        // iterate through the list
        while (it.hasNext()) {
            ManifestResource mre = (ManifestResource)it.next();
            // check if the resource requires extensions
            if (mre.requiresExtensions()) {
                // build the list of available extensions if necessary
                if (availableExtensions == null) {
                    availableExtensions = buildAvailableExtensionsMap(resources);
                }
                // load the container level resource map if it has not 
                // been built yet
                if (containerAvailableExtensions == null) {
                    containerAvailableExtensions = buildAvailableExtensionsMap(
                        containerManifestResources);
                }
                // get a list of the required extensions
                ArrayList requiredList = mre.getRequiredExtensions();
                Iterator rit = requiredList.iterator();
                // iterate through the list of required extensions
                while (rit.hasNext()) {
                    Extension requiredExtension = (Extension)rit.next();
                    String key = requiredExtension.getUniqueId();
                    // check in the applicaion first for the extension
                    if (availableExtensions.containsKey(key)) {
                       // check if the desired extension is compatible 
                       // with the required extension
                       Extension targetExtension = (Extension)
                           ((ManifestResource)availableExtensions.get(key)).
                            getAvailableExtensions().get(key);
                       // check if the desired extension is valid
                       if (targetExtension.isCompatibleWith(requiredExtension)) {
                           // extension requirements have passed
                           requiredExtension.setFulfilled(true);
                       }
                    // check the container level list for the extension
                    } else if (containerAvailableExtensions.containsKey(key)) {
                       // check if the desired extension is compatible 
                       // with the required extension
                       Extension targetExtension = (Extension)
                           ((ManifestResource)containerAvailableExtensions.
                            get(key)).getAvailableExtensions().get(key);
                       // check if the desired extension is valid
                       if (targetExtension.isCompatibleWith(requiredExtension)) {
                           // extension requirements have passed
                           requiredExtension.setFulfilled(true);
                       }
                    } else {
                        // FAILURE has occured
                        String[] args = {appName, mre.getResourceName(), 
                            requiredExtension.getExtensionName() };
                        logMessage("extensionValidator.extension-not-found-error", 
                            args, logger);
                        passes =  false;
                        failureCount++;
                    }
                }
            }
        }
        if (!passes) {
            String[] args = {appName,failureCount + "" };
            logMessage("extensionValidator.extension-validation-error", 
                args, logger);
        }
        return passes;
    }
    
   /* 
    * Build this list of available extensions so that we do not have to 
    * re-build this list every time we iterate through the list of required 
    * extensions. All available extensions in all of the 
    * <code>MainfestResource</code> objects will be added to a 
    * <code>HashMap</code>which is returned on the first dependency list
    * processing pass. 
    *
    * The key is the name + implementation version.
    *
    * NOTE: A list is built only if there is a dependency that needs 
    * to be checked (performace optimization).
    *
    * @param ArrayList A list of <code>ManifestResource</code> objects
    *
    * @return HashMap Map of available extensions
    */
    private static HashMap buildAvailableExtensionsMap(ArrayList resources) {
        HashMap availableMap = new HashMap();
        Iterator it = resources.iterator();
        // iterate through the list
        while (it.hasNext()) {
            ManifestResource mre = (ManifestResource)it.next();
            if (mre.requiresExtensions()) {
                HashMap map = mre.getAvailableExtensions();
                Iterator kit = map.keySet().iterator();
                while (kit.hasNext()) {
                    String key = (String)kit.next();
                    Extension ext = (Extension)map.get(key);
                    // mre is needed for error reporting if a match is not made
                    // it has access to the extensions
                    if (!availableMap.containsKey(key)) {
                        availableMap.put(ext.getUniqueId(), mre);
                    }
                }
            }
        }
        return availableMap;
    }
    
   /**
     * Return the Manifest from a jar file or war file
     *
     * @param File is a war file or a jar file
     * @return java.util.jar.Manifest
     *
     */
    private static Manifest getManifest(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            JarInputStream jin = new JarInputStream(fis);
            return jin.getManifest();
        } catch (java.io.IOException iox) {
               return null;
        }
    }
    
    /**
     *  Standardized method of logging localized messages by the 
     *  ExtensionValidator.
     *  
     * @param String The key of the message in the ResourceBundle
     * @param String[] The Arguments to be applied to the messages 
     *                 if any (null) if none
     * @param Logger The logger to which the messages will be logged
     *
     */
    private static void logMessage(String messageId, String[] args, Logger logger) {
       String message = getMessage(messageId, logger);
        if (args != null) {
            logger.log(MessageFormat.format(message, args));
        } else {
            logger.log(message);
        }
    }
    
    /**
     *  Standardized method of obtaining a localized message.
     *  
     * @param String The key of the message in the ResourceBundle
     * @param Logger The logger to which error messages encounter looking 
     *               up resources will be logged
     *
     * @return String message string
     */
    
    private static String getMessage(String messageId, Logger logger) {
        // load localized messages if necessary
        if (messages == null) {
            try {
                messages = ResourceBundle.getBundle(
                    "org.apache.catalina.util.LocalStrings");
            } catch (java.util.MissingResourceException mrx) {
                logger.log(
                    "Unable to load localized resources for ExtensionValidator");
                // jump out of method
                return null;
            }
        }
        String message = null;
        try {
            return  messages.getString(messageId);
        } catch (java.util.MissingResourceException mrx) {
            logger.log("Unable to load resources for ExtensionValidator");
            return null;
        }
    }
}


