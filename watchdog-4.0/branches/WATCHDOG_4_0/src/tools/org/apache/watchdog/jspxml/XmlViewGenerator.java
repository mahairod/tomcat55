/*
 * $Header$ 
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 * THIS SOFTWARE IS PROVIDED AS IS'' AND ANY EXPRESSED OR IMPLIED
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
 */
 
package org.apache.watchdog.jspxml;

/**
 *	This program modifies a general JSP sysntax file ( with well formed HTML tags )
 *  to a XML view of the file.
 *
 *	Different Uses
 *	1. java XmlViewGenerator <inputJspFile> <outputJspWithXmlView>
 *	   Used in this way, a JSP in standard syntax can be converted to an XML 
 *     equivalent.
 *
 *	2. java XmlViewGenerator <directoryName>
 *      Given a directory as an argument, each JSP file found will be converted
 *      to the XML equivalent.   
 *
 *	@Author: Ramesh.Mandava
 */

import java.io.*;

public class XmlViewGenerator implements FilenameFilter {

    public static void main (String[ ] args) {
        if (args.length == 0) {
            usage( );
            System.exit(0);
        } else if (args.length == 1) {
            System.out.println("Have one argument, which is assumed as directory");
            File passedDir = new File(args[0]);
            if ( !passedDir.exists( ) ) {
                System.out.println(args[0] + ": doesn't exist");
                System.exit(0);
            } else if ( !passedDir.isDirectory() ) {
                System.out.println("ERROR: Directory expected");
                System.out.println(args[0] + " : is not a directory");
            } else {
                XmlViewGenerator myGenerator = new XmlViewGenerator();
                myGenerator.generateXmlViewForDirectoryContents( passedDir );
            }
        } else if (args.length == 2) {
            System.out.println("Got two arguments: first is assumed as input file and second is OUTPUTFILE");
            File INPUTFILE = new File(args[0]);
            if (!INPUTFILE.exists( )) {
                System.out.println(args[0] + ": doesn't exist");
                System.exit(0);
            } else {
                XmlViewGenerator myGenerator = new XmlViewGenerator();
                myGenerator.generateXMLView( INPUTFILE, args[1] );
            }
        }
    }

    public void generateXmlViewForDirectoryContents(File passedDir) {
        String[] fileList = passedDir.list(this);
        System.out.println("In generateXml.. fileList -> " + fileList);
        System.out.println("\nNumber -> " + fileList.length);
        for ( int i = 0; i < fileList.length; i++ ) {
            System.out.println("File[" + i + "] -> " + fileList[i]);
            File presentFile = new File (passedDir.getAbsolutePath() + File.separator + fileList[i]);
            System.out.println("Absolute Path -> " + presentFile.getAbsolutePath());

            if (presentFile.isDirectory( )) {
                System.out.println(" Generating XML view for directory contents : " + presentFile.getName());
                generateXmlViewForDirectoryContents( presentFile );
            } else {
                generateXMLView( presentFile );
            }
        }
    }

    public boolean accept(File dir, String name) {
        File myFile = new File(dir.getAbsolutePath() + File.separator + name);
        return ((myFile.isDirectory()) || ((name.endsWith(".jsp")) && (!name.endsWith("XMLView.jsp"))));
    }

    public static void usage() {
        System.out.println("USAGE: java XmlViewGenerator <inputJspFile> <outputXmlViewJsp>");
        System.out.println(" (OR) \n java XmlViewGenerator <inputDirectory> ");

    }

    public void generateXMLView (File currentFile) {
        String fileName = currentFile.getAbsolutePath();
        String outFileName = fileName.substring( 0, fileName.indexOf(".jsp")) + "XMLView.jsp";
        System.out.println("Input Name -> " + fileName);

        System.out.println("Output Name -> " + outFileName);
        generateXMLView( currentFile, outFileName);
    }

    public void generateXMLView(File currentFile, String outFileName) {
        jsp2XML jspConverter = new jsp2XML(currentFile.getAbsolutePath());
        String xmlView = jspConverter.ConvertJsp2XML();
        try {
            FileWriter fw = new FileWriter(outFileName);
            fw.write(xmlView);
            fw.close();
        } catch (IOException ioe) {
            System.err.println("Error writing to file" + outFileName );
        }
    }
}// end XmlViewGenerator
