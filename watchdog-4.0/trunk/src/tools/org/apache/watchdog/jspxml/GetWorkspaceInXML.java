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

import java.io.*;
import java.util.*;

/**
 * This is a utility class which helps running the entire JSP test suite with
 * the XML view of JSP pages. 
 */

public class GetWorkspaceInXML {

    public static void main (String[] args ) {
        System.out.println("\n--> Executing getWorkspaceInXML.main()");
        String jsp_file = null;
        String xml_file = null ;
        String jsp_root_dir = System.getProperty("JSP_ROOT");
        String watchdog_home = System.getProperty("WATCHDOG_HOME");
        String file_separator = System.getProperty("file.separator");

        if (jsp_root_dir == null) {
            System.out.println("JSP_ROOT variable is not set...exiting");
            return ;
        }

        if (watchdog_home == null) {
            System.out.println("WATCHDOG_HOME variable is not set...exiting");
            return ;
        }

        String extension = "jsp" ; //files with extension .jsp
		String jsp_root_file = new File(jsp_root_dir).toString();
		int jsp_root_length = jsp_root_file==null ? 0 : jsp_root_file.length();

        FileLister lister = new FileLister(jsp_root_dir , extension, "XML") ;
        Object[] files = lister.listFiles();
        System.out.println(files.length + " files to process in " + 
						   jsp_root_dir);                           

		int already_converted = 0;
        for (int i = 0; i < files.length;i++) {
            jsp_file = (String)files[i];
            int index = jsp_file.lastIndexOf(".jsp");

            xml_file = jsp_file.substring(0, index) + "XML" + ".jsp";

            //it should convert only if the jsp file is newer than the XML file

            File file_jsp = new File(jsp_file) ;
            File file_xml = new File(xml_file);

            if (file_xml.exists()) {  
                //there was already a conversion
                if (file_xml.lastModified() > file_jsp.lastModified()) {
					already_converted++;
                    continue;
				}
            }

            //if we are here means we need to convert the jsp file to xml
			String xml_file_name = xml_file.toString();
			int path_index = xml_file_name.indexOf(jsp_root_file);
			if (path_index > 0) {
				String relative_path =
					xml_file_name.substring(path_index + jsp_root_length + 1);
				System.out.println("  " + relative_path);
			}
			else
				System.out.println("  " + xml_file);

            jsp2XML jsp_converter = new jsp2XML(jsp_file);
            String xml = jsp_converter.ConvertJsp2XML();

            try {
                FileWriter fw = new FileWriter(xml_file);
                fw.write(xml);
                fw.close();
            } catch (IOException ioe) {
                System.err.println("Error writing to file" + xml_file);
            }

        }  //end for
		if (already_converted > 0)
			System.out.println(already_converted +
							   " files previously converted");

        //we generated the workspace in XML...now we need to create the
        //jsp-gtest-xml file that has targets for running the tests against
        //the XML view of the JSP pages

        File jsp_target = new File(watchdog_home + file_separator + "conf" +
                                   file_separator + "jsp-gtest.xml");

        File xml_target = new File(watchdog_home + file_separator + "conf" +
                                   file_separator + "jsp-gtest-xml.xml");

        if ( xml_target.exists() ) {
            //the converted target file exists
            if (jsp_target.lastModified() < xml_target.lastModified()) //do nothing
                return ;
        }

        //The jsp file is latest so we need a conversion
        jsp2XMLTarget xml_gtest = new jsp2XMLTarget(watchdog_home + file_separator +
                                  "conf" + file_separator + "jsp-gtest.xml");

        String targets_in_xml = xml_gtest.getXMLTarget();

        try {
            FileWriter fw = new FileWriter(watchdog_home + file_separator + "conf" +
                                           file_separator + "jsp-gtest-xml.xml");
            fw.write(targets_in_xml);
            fw.close();
        } catch (IOException ioe) {
            System.err.println("Error writing to XML Gtest file");
        }
    }
} //end GetWorkspaceInXML 
