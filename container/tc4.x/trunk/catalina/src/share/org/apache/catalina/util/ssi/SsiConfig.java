/*
 * SsiConfig.java
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

import java.util.Properties;

import javax.servlet.ServletOutputStream;

/**
 * Implementation of the SsiCommand config, example of usage:
 * &lt;!--#config sizefmt="abbrev" errmsg="An error occured!"--&gt;
 *
 * @author Bip Thelin
 * @author Paul Speed
 * @version $Revision$, $Date$
 */
public final class SsiConfig extends AbstractSsiCommand {

    /**
     * Variable to hold the patterns for translation
     */
    private static Properties translate;

    /**
     * Initialize our pattern translation
     */
    static {
        translate = new Properties();
        translate.put("a","EEE");
        translate.put("A","EEEE");
        translate.put("b","MMM");
        translate.put("h","MMM");
        translate.put("B","MMMM");
        translate.put("d","dd");
        translate.put("D","MM/dd/yy");
        translate.put("e","d");
        translate.put("H","HH");
        translate.put("I","hh");
        translate.put("j","E");
        translate.put("m","M");
        translate.put("M","m");
        translate.put("p","a");
        translate.put("r","hh:mm:ss a");
        translate.put("S","s");
        translate.put("T","HH:mm:ss");
        translate.put("U","w");
        translate.put("W","w");
        translate.put("w","E");
        translate.put("y","yy");
        translate.put("Y","yyyy");
        translate.put("z","z");
    }

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
                                    throws SsiCommandException {

        // Set the configuration variables that we understand
        for (int i = 0; i < argNames.length; i++) {
            String name = argNames[i];
            String value = argVals[i];
            if ("errmsg".equals(name)) {
                ssiEnv.setConfiguration( name,
                                         ssiEnv.substituteVariables(value) );
            } else if ("sizefmt".equals(name)) {
                ssiEnv.setConfiguration( name,
                                         ssiEnv.substituteVariables(value) );
            } else if ("timefmt".equals(name)) {
                value = convertFormat( ssiEnv.substituteVariables(value) );
                ssiEnv.setConfiguration( name, value );
            }
        }
    }

    /**
     * Search the provided pattern and get the C standard
     * Date/Time formatting rules and convert them to the
     * Java equivalent.
     *
     * @param pattern The pattern to search
     * @return The modified pattern
     */
    private String convertFormat(String pattern) {
        boolean inside = false;
        boolean mark = false;
        StringBuffer retString = new StringBuffer();
        String sRetString = "";

        for(int i = 0; i<pattern.length();i++) {
            if(pattern.charAt(i)=='%'&&!mark) {
                mark=true;
                continue;
            }

            if(pattern.charAt(i)=='%'&&mark) {
                mark=false;
            }

            if(mark) {
                if(inside) {
                    retString.append("'");
                    inside=false;
                }

                retString.append(translateCommand(pattern.charAt(i)));
                mark=false;
                continue;
            }

            if(!inside) {
                retString.append("'");
                inside = true;
            }

            retString.append(pattern.charAt(i));
        }

        sRetString = retString.toString();

        if(!sRetString.endsWith("'")&&inside)
            sRetString = sRetString.concat("'");

        return sRetString;
    }

    /**
     * try to get the Java Date/Time formating associated with
     * the C standard provided
     *
     * @param c The C equivalent to translate
     * @return The Java formatting rule to use
     */
    private String translateCommand(char c) {
        String retCommand = translate.getProperty("".valueOf(c));

        return retCommand==null?"":retCommand;
    }
}
