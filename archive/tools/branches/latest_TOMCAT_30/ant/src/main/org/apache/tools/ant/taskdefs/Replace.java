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

/**
 * Replaces all the occurrences of the given string token with the given
 * string value of the indicated file.
 *
 * @author Stefano Mazzocchi <a href="mailto:stefano@apache.org">stefano@apache.org</a>
 */
public class Replace extends Task {
    
    private File src = null;
    private File dest = null;
    private String token = null;
    private String value = "";
    
    /**
     * Do the execution.
     */
    public void execute() throws BuildException {
        
        project.log("Replacing " + token + " --> " + value);
        
        if (src == null || token == null ) {
            project.log("File and token must not be null");
            return;            
        }
        
        if (dest == null) {
            throw new BuildException("Error creating temp file.");
        }
                
        try {
            BufferedReader br = new BufferedReader(new FileReader(src));
            BufferedWriter bw = new BufferedWriter(new FileWriter(dest));

            String line;
            
            while (true) {
                line = br.readLine();
                if (line == null) break;
                if (line.length() != 0) bw.write(replace(line));
                bw.newLine();
            }
             
            bw.flush();
            bw.close();
            br.close();
            
            src.delete();
            dest.renameTo(src);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }       
    }
    
    /**
     * Set the source file.
     */
    public void setFile(String file) {
        this.src = project.resolveFile(file);
        this.dest = project.resolveFile(file + ".temp");
    }

    /**
     * Set the string token to replace.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Set the string value to use as token replacement.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Perform the token substitution.
     */    
    private String replace (String orig) {
        StringBuffer buffer = new StringBuffer();
        int start = 0, end = 0;
        
        while ((end = orig.indexOf(token, start)) > -1) {
            buffer.append(orig.substring(start, end));
            buffer.append(value);
            start = end + token.length();
        }
        
        buffer.append(orig.substring(start));
        
        return buffer.toString();            
    }
}