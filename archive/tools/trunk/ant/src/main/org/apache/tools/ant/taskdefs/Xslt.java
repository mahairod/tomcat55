/*
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
 */

package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * XSLT tranformation.
 * 
 * Input file is converted to output file using a given XSLT tranformation sheet.
 * Do not use input file as output file since it may create runtime exceptions.
 *
 * @author Stefano Mazzocchi <a href="mailto:stefano@pache.org">stefano@apache.org</a>
 */
public class Xslt extends Task {
    
    public static String tranformerName = "org.apache.xalan.xslt.Process";
    
    private File source = null;
    private File dest = null;
    private File sheet = null;
    
    /**
     * Do the execution.
     */
    public void execute() throws BuildException {
        
        project.log("Performing XSLT Transformation");
        
        if ( source == null || dest == null || sheet == null) {
            project.log("Source, destination and stylesheet must not be null");
            return;            
        }

        /*
         * This code is rather different from normal java coding 
         * approaches, in fact it is the un-typed equivalent of
         * 
         *   (new Process()).main(arguments());
         *
         * but uses complete reflection and does not require you to 
         * have Xalan in your classpath for complete compilation.
         */
         try {
            Class transformerClass = Class.forName(tranformerName);
        
            Method[] methods = transformerClass.getMethods();
            Method main = null;
        
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("main")) main = methods[i];
            }
        
            Object[] arguments = { arguments() };
            
            if (main != null) {
                main.invoke(transformerClass.newInstance(), arguments);
            } else {
                new BuildException("Could not find main() method.");
            }
        } catch (Exception e) {
            new BuildException(e);
        }
    }
    
    /**
     * Set the source file.
     */
    public void setSrc(String s) {
        this.source=project.resolveFile(s);
    }

    /**
     * Set the destination file.
     */
    public void setDest(String dest) {
        this.dest = project.resolveFile(dest);
    }

    /**
     * Set the stylesheet file.
     */
    public void setSheet(String sheet) {
        this.sheet = project.resolveFile(sheet);
    }

    String[] arguments() {
        String[] arguments = { "-in", source.toString(), 
                                "-out", dest.toString(),
                                "-xsl", sheet.toString() };
        return arguments;
    }
}