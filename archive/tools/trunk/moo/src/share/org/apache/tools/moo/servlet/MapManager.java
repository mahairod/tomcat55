/*
 * $Header$ 
 * $Date$ 
 * $Revision$
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
 * @author Mandar Raje [mandar@eng.sun.com]
 * @author Arun Jamwal [arunj@eng.sun.com]
 */
package org.apache.tools.moo.servlet;

import org.apache.tools.moo.servlet.Constants;
import org.apache.tools.moo.cookie.CookieJar;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.lang.NullPointerException;


/**
 * This class handles the mapping of client tests to server tests
 */
public class MapManager {

    private CookieJar cookieJar = new CookieJar();

    //maps is a hashtable from client test (key) to server test (value)
    private Hashtable maps = new Hashtable();

    //offers some configurability options such as the base directory of
    // server resources (ie server-side tests)
    private static final String ConfigFile = Constants.Config.propDir +  Constants.Config.Name;

    public
    MapManager() {

        String defaultResourceBase = "/servlet-tests";
        Properties props = new Properties();
        Properties tests = new Properties();

        props.put(Constants.Config.ResourceBase,
                  defaultResourceBase);

        try {

            //load configuration properties
            InputStream in =
              this.getClass().getResourceAsStream(ConfigFile);
            if (in == null)
                throw new Exception();
            props.load(in);
        } catch (Exception e) {
            System.out.println("Exception: can't find config file " +
                               ConfigFile);
        }

        String propFile = Constants.Config.propDir +  Constants.Config.mapFile;

        try {

            InputStream in =
              this.getClass().getResourceAsStream(propFile);
            if (in == null)
                throw new FileNotFoundException();
            tests.load(in);
            maps = (Hashtable)tests;
        } catch(FileNotFoundException e) {
            System.out.println("Servlet Could not find file: " + propFile);
        } catch (SecurityException e) {
            System.out.println("Security Exception while opening: " + propFile);
        } catch (IOException e) {
            System.out.println("Error loading properties file: " + propFile);
        }

        Enumeration e = maps.keys();
        String prefix = props.getProperty(Constants.Config.ResourceBase) + "/";
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String uri = (String)maps.get(key);
            if (uri.trim().charAt(0) != '/') {
                String value = prefix + uri;
                maps.put(key, value);
            }
        }
    }

    /**
     * returns the server-equivalent test for a client-test.  
     * The client test needs to be fully-qualified.
     * if not, or if it is not a valid name, this method will return null i believe
     */
    public String
    get(String testName) {
        return (String)maps.get(testName);
    }

    public CookieJar getCookieJar() {
        return this.cookieJar;
    }
}
