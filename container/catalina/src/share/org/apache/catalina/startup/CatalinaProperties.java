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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * Utility class to read the bootstrap Catalina configuration.
 *
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class CatalinaProperties {


    // ------------------------------------------------------- Static Variables


    private static Properties properties = null;


    static {

        loadProperties();

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return specified property value.
     */
    public static String getProperty(String name) {

        return properties.getProperty(name);

    }


    /**
     * Return specified property value.
     */
    public static String getProperty(String name, String defaultValue) {

        return properties.getProperty(name, defaultValue);

    }


    /**
     * Sets the property with the specified key to the specified value.
     *
     * @param key the property key
     * @param value the property value
     * @return the previous value of the specified key in the property
     *         list, or <code>null</code> if it did not have one.
     */
    public static Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Load properties.
     */
    private static void loadProperties() {

        InputStream is = null;
        Throwable error = null;

        try {
            String configUrl = getConfigUrl();
            if (configUrl != null) {
                is = (new URL(configUrl)).openStream();
            }
        } catch (Throwable t) {
            // Ignore
        }

        if (is == null) {
            try {
                File home = new File(getCatalinaHome());
                File conf = new File(home, "conf");
                File properties = new File(conf, "catalina.properties");
                is = new FileInputStream(properties);
            } catch (Throwable t) {
                // Ignore
            }
        }

        if (is == null) {
            try {
                is = CatalinaProperties.class.getResourceAsStream
                    ("/org/apache/catalina/startup/catalina.properties");
            } catch (Throwable t) {
                // Ignore
            }
        }

        if (is != null) {
            try {
                properties = new Properties();
                properties.load(is);
                is.close();
            } catch (Throwable t) {
                error = t;
            }
        }

        if ((is == null) || (error != null)) {
            // Do something
            System.out.println("Error");
        }

    }


    /**
     * Get the value of the catalina.home environment variable.
     */
    private static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }


    /**
     * Get the value of the configuration URL.
     */
    private static String getConfigUrl() {
        return System.getProperty("catalina.config");
    }


}
