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

package org.apache.catalina.util.ssi;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletOutputStream;

/**
 *  Dispatcher class used to run SSI commands.  The idea is that
 *  each SsiInokerServlet instance will have its own SsiDispatcher
 *  and can therefore configure certain context-global settings.
 *
 *  @version   $Revision$, $Date$
 *  @author    Paul Speed
 */
public class SsiDispatcher {

    /**
     *  Determines how to treate unknown command references.
     */
    private boolean ignoreUnsupportedDirective = true;

    /**
     *  Contains the SSI command instances.  This is shared
     *  across all dispatcher instances.
     */
    private static Hashtable ssiCommands;

    /**
     *  Initialize the pool of SsiCommands.
     */
    static {
        ssiCommands = new Hashtable(6);
        ssiCommands.put("config", new SsiConfig());
        ssiCommands.put("include", new SsiInclude());
        ssiCommands.put("echo", new SsiEcho());
        ssiCommands.put("fsize", new SsiFsize());
        ssiCommands.put("flastmod", new SsiFlastmod());
        ssiCommands.put("exec", new SsiExec());
        ssiCommands.put("set", new SsiSet());
    }

    /**
     *  Set to true to ignore unknown commands.
     */
    public void setIgnoreUnsupportedDirective(boolean flag) {
        this.ignoreUnsupportedDirective = flag;
    }

    /**
     * Set to true to consider the webapp as root.
    public void setIsVirtualWebappRelative(boolean flag) {
        this.isVirtualWebappRelative = flag;
    }

    /**
     *  Returns true if the dispatcher ignores unknown commands.
     */
    public boolean ignoreUnsupportedDirective() {
        return ignoreUnsupportedDirective;
    }

    /**
     *  Runs the specified command using the specified arguments.
     *
     *  @param cmdName  The name of the command to run.
     *  @param argNames String array containing the parameter
     *                  names for the command.
     *  @param argVals  String array containing the paramater
     *                  values for the command.
     *  @param ssiEnv   The environment to use for command
     *                  execution.
     *  @param out      A convenient place for commands to
     *                  write their output.
     */
    public void runCommand( String cmdName,
                            String[] argNames,
                            String[] argVals,
                            SsiEnvironment ssiEnv,
                            ServletOutputStream out )
                                    throws IOException {
        // Lookup the command
        SsiCommand cmd = (SsiCommand)ssiCommands.get(cmdName);
        if (cmd == null) {
            if (!ignoreUnsupportedDirective)
                out.print( ssiEnv.getConfiguration("errmsg") );
            return;
        }

        try {
            // Run the command
            cmd.execute( cmdName, argNames, argVals, ssiEnv, out );
        } catch (SsiCommandException e) {
            // Write the error
            out.print( ssiEnv.getConfiguration("errmsg") );
        }
    }
}
