/*
 * SsiExec.java
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

package org.apache.catalina.util.ssi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletOutputStream;

/**
 * @author Bip Thelin
 * @author Amy Roh
 * @author Paul Speed
 * @version $Revision$, $Date$
 *
 */
public final class SsiExec implements SsiCommand {

    /**
     *  Runs this command using the specified parameters.
     *
     *  @param cmdName  The name that was used to lookup this
     *                  command instance.
     *  @param argNames String array containing the parameter
     *                  names for the command.
     *  @param argVals  String array containing the paramater
     *                  values for the command.
     *  @param ssiEnv   The environment to use for command
     *                  execution.
     *  @param out      A convenient place for commands to
     *                  write their output.
     */
    public void execute( String cmdName, String[] argNames,
                         String[] argVals, SsiEnvironment ssiEnv,
                         ServletOutputStream out )
                                    throws IOException,
                                           SsiCommandException {

        if ("cgi".equals(argNames[0])) {
            String path = getCGIPath(argVals[0], ssiEnv.getContextPath());
            if (path == null)
                throw new SsiCommandException( "Invalid path:" + argVals[0] );

            // Stream the CGI output back to the client
            URL u = new URL(path);
            InputStream istream = u.openStream();
            int i;
            while ((i = istream.read()) != -1) {
                out.write(i);
            }
        } else if ("cmd".equals(argNames[0])) {
            String path = getCommandPath(argVals[0]);
            if (path == null)
                throw new SsiCommandException( "Invalid path:" + argVals[0] );

            BufferedReader commandsStdOut = null;
            BufferedReader commandsStdErr = null;
            BufferedOutputStream commandsStdIn = null;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            //byte[] bBuf = new byte[1024];
            char[] cBuf = new char[1024];
            int bufRead = -1;

            Runtime rt = null;
            Process proc = null;

            try {
                rt = Runtime.getRuntime();
                proc = rt.exec(path);

                commandsStdIn = new BufferedOutputStream(proc.getOutputStream());
                //boolean isRunning = true;
                commandsStdOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                commandsStdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                BufferedWriter servletContainerStdout = null;

                while ((bufRead = commandsStdErr.read(cBuf)) != -1) {
                    writer.write(cBuf, 0, bufRead);
                }

                cBuf = new char[1024];
                while ((bufRead = commandsStdOut.read(cBuf)) != -1) {
                    writer.write(cBuf, 0, bufRead);
                }

                out.flush();

                proc.exitValue();
            } catch (IOException ex) {
            }
        }
    }

    protected String getCGIPath( String path, String contextPath ) {

        String cgibinStr = "/cgi-bin/";

        if (path == null)
            return null;

        if (!path.startsWith(cgibinStr)) {
            return null;
        } else {
            //normalized = normalized.substring(1, cgibinStr.length());
            //normalized = cgiPathPrefix + File.separator + normalized;
            path = "http://localhost:8080" + contextPath + path;
        }
        return (path);
    }

    protected String getCommandPath(String path) {

        String commandShellStr = "/bin/sh";

        if (path == null)
            return null;

        if (!path.startsWith("/"))
            path = "/" + path;
        path = commandShellStr + path;
        return (path);
    }
}
