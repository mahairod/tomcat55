/*
 * $Header$
 * $Revision$
 * $Date$
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.jasper;

import java.io.File;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 */
public interface Options {
    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated();
    
    /**
     * Are we supporting large files?
     */
    public boolean getLargeFile();

    /**
     * Are we supporting HTML mapped servlets?
     */
    public boolean getMappedFile();
    
    
    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean getSendErrorToClient();
 
    /**
     * Should we include debug information in compiled class?
     */
    public boolean getClassDebugInfo();

    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String getIeClassId();
    
    /**
     * What is my scratch dir?
     */
    public File getScratchDir();

    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath();

    /**
     * What compiler plugin should I use to compile the servlets
     * generated from JSP files?
     */
    public Class getJspCompilerPlugin();

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public String getJspCompilerPath();
    
    /**
     * ProtectionDomain for this JSP Context when using a SecurityManager
     */
    public Object getProtectionDomain();

    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    public String getJavaEncoding();
}
