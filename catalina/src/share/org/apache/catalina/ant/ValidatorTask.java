/*
 * Copyright 2002,2004 The Apache Software Foundation.
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


package org.apache.catalina.ant;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.DigesterFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.InputSource;


/**
 * Task for validating a web application deployment descriptor, using XML 
 * schema validation.
 *
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 * @since 5.0
 */

public class ValidatorTask extends Task {


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    /**
     * The path to the webapp directory.
     */
    protected String path = null;

    public String getPath() {
        return (this.path);
    }

    public void setPath(String path) {
        this.path = path;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute the specified command.  This logic only performs the common
     * attribute validation required by all subclasses; it does not perform
     * any functional logic directly.
     *
     * @exception BuildException if a validation error occurs
     */
    public void execute() throws BuildException {

        if (path == null) {
            throw new BuildException("Must specify 'path'");
        }

        File file = new File(path, Constants.ApplicationWebXml);
        if ((!file.exists()) || (!file.canRead())) {
            throw new BuildException("Cannot find web.xml");
        }

        // Commons-logging likes having the context classloader set
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader
            (ValidatorTask.class.getClassLoader());

        Digester digester = DigesterFactory.newDigester(true, true, null);
        try {
            file = file.getCanonicalFile();
            InputStream stream = 
                new BufferedInputStream(new FileInputStream(file));
            InputSource is = new InputSource(file.toURL().toExternalForm());
            is.setByteStream(stream);
            digester.parse(is);
            System.out.println("web.xml validated");
        } catch (Throwable t) {
            throw new BuildException("Validation failure", t);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }

    }


}
