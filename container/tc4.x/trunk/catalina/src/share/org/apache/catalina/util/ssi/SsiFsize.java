/*
 * SsiFsize.java
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
import java.net.URL;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;

/**
 * Return the size of a given file, subject of formatting.
 *
 * @author Bip Thelin
 * @author Paul Speed
 * @version $Revision$, $Date$
 */
public final class SsiFsize implements SsiCommand {

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
        FileReference ref = null;

        String value = ssiEnv.substituteVariables( argVals[0] );

        if (argNames[0].equals("file"))
            ref = ssiEnv.getFileReference( value, false );
        else if (argNames[0].equals("virtual"))
            ref = ssiEnv.getFileReference( value, true );

        if (ref == null)
            throw new SsiCommandException( "Path not found:" + value );

        long fileSize;

        try {
            URL u = ref.getResource();
            fileSize = u.openConnection().getContentLength();
        } catch (Exception e) {
            throw new SsiCommandException( e.toString() );
        }

        out.print( formatSize(String.valueOf(fileSize),
                              ssiEnv.getConfiguration("sizefmt")) );
    }

    //----------------- Private methods

    /**
     * Format the size with the correct format, either
     * abbrev or bytes.
     *
     * @param length The variable to format
     * @param format The pattern to use when formatting
     * @return The result
     */
    private String formatSize(String length, String format) {
        String retString = "";

        if(format.equalsIgnoreCase("bytes")) {
            retString = commaFormat(length);
        } else {
            double lParse = (new Long(length)).doubleValue();

            if(lParse>=1048576) {
                double abbrevSize = lParse/1048576;
                long splitSize = (long)abbrevSize;
                int  catSize = (int)(100 * (abbrevSize - splitSize));

                retString =
                    commaFormat((new Long(splitSize)).toString())+
                    "."+catSize+" MB";
            } else if(lParse>=1024) {
                double abbrevSize = lParse/1024;
                long splitSize = (long)abbrevSize;
                int  catSize = (int)(100 * (abbrevSize - splitSize));

                retString =
                    commaFormat((new Long(splitSize)).toString())+
                    "."+catSize+" KB";
            } else {
                retString =
                    commaFormat(length)+" bytes";
            }
        }

        return retString;
    }

    /**
     * Modify the supplied variable to be returned comma formated.
     *
     * @param length The variable to modify
     * @return The modified result
     */
    private String commaFormat(String length) {
        String retString = "";

        for(int i = length.length();i-1>=0;i--) {
            retString = (length.substring(i-1,i)).concat(retString);
            if((length.length()-(i-1))%3==0&&
               retString.length()<length.length())
                retString = ",".concat(retString);
        }

        return retString;
    }
}
