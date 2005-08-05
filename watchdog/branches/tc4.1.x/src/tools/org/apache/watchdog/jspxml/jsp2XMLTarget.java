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
 * This utility class will convert the jsp-gtest.xml to
 * execute the XML view JSP tests.
 *
 * @author Santosh Singh
 * @version $Version$
 */

import java.io.*;

public class jsp2XMLTarget {
    private String input_jsp ;

    public jsp2XMLTarget(String jsp_file) {
        input_jsp = ReadFileintoString(jsp_file);
    }

    public static String ReadFileintoString(String jsp_file) {
        if (jsp_file == null)
            return null;

        String new_line = System.getProperty("line.separator");
        String input_jsp = "";

        try {
            FileReader in_file = new FileReader(jsp_file);
            BufferedReader br = new BufferedReader(in_file);
            String line = br.readLine() ;


            while (line != null) {
                line = line + new_line ; //readLine removes new line
                input_jsp += line;
                line = br.readLine() ;

            }
            br.close();
            in_file.close();

        } catch (IOException ioex) {
            System.out.println("I/O Error in Reading");
        }
        return input_jsp;
    }

    public String getXMLTarget() {

        int start_index = 0;
        int end_index = 0;
        String start_element = "GET" ;
        String end_element = "HTTP/1.0" ;
        String xml_target = "" ;

        //anything between GET and HTTP/1.0 is a URI

        start_index = input_jsp.indexOf(start_element , end_index);

        while ( start_index > 0 ) {
            xml_target += input_jsp.substring(end_index , start_index);
            end_index = input_jsp.indexOf(end_element , start_index);
            String uri = input_jsp.substring(start_index , end_index);
            String param_string = null;

            //Parse the URI
            int param_index = uri.indexOf('?') ; //params in request

            if (param_index >= 0) {
                param_string = uri.substring(param_index);
                uri = uri.substring(0, param_index);
            }

            //Now we have only Request String
            int file_index = uri.lastIndexOf('/');  //this is the path separator in XML file
            int jsp_index = uri.lastIndexOf(".jsp");


            if ((file_index >= 0 ) && (jsp_index >= 0) ) {
                String file_name = uri.substring(file_index + 1, jsp_index);

                if (param_string == null)
                    param_string = uri.substring(jsp_index + 4); //points beyond .jsp

                uri = uri.substring(0, file_index + 1);
                file_name = file_name + "XML" + ".jsp" ;
                uri = uri + file_name + param_string ;

                xml_target += uri;
                int last_index = input_jsp.indexOf("\"", end_index) ;
                xml_target += input_jsp.substring(end_index, last_index + 1) ;
                end_index = last_index + 1;
                start_index = input_jsp.indexOf(start_element , end_index);
                continue;
            }
            //Unfortunately it was a template text ..should be written as it is
            end_index = end_index + end_element.length();
            xml_target += input_jsp.substring(start_index, end_index);
            start_index = input_jsp.indexOf(start_element , end_index);

        } //end while

        xml_target += input_jsp.substring(end_index);
        return xml_target ;
    }
} //end jsp2XMLTarget
