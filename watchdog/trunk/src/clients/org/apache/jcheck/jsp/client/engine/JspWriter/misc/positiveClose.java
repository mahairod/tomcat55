/*

   $Header: /home/cvs/jakarta-check/src/clients/org/apache/jcheck/jsp/client/engine/JspWriter/misc/positiveClose
 * 
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
 */








package org.apache.jcheck.jsp.client.engine.JspWriter.misc;


import org.apache.jcheck.jsp.util.*;
import org.apache.tools.moo.jsp.*;
import org.apache.tools.moo.TestResult;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class positiveClose extends PositiveJspCheckTest
{
      StringManager sm= StringManager.getManager(UtilConstants.Package);

      public String getDescription()
      {
            return sm.getString("positiveClose.description");
      }


      public TestResult runTest()      
      {
            TestResult testResult=new TestResult();
            int code=0;
            String message=null;
            HttpURLConnection connection;
            FileReader fis=null;
            BufferedReader in=null;
            File file=null;
            int i=0; 
            
            
            try
            {
            	  //  getting connection to jsp  //
            	
                  connection=getConnection(null,null,null,"GET");
            }
            catch(Exception k)
            {
                  testResult.setMessage("FAIL");
                  testResult.setStatus(false);
                  return testResult;
            }
            try
            {
                  code = connection.getResponseCode();
            }
            catch(IOException io)
            {
                  testResult.setMessage("FAIL");
                  testResult.setStatus(false);
                  return testResult;
            }
            
            try
            {
                  message = connection.getResponseMessage();
            }
            catch(IOException j)
            {
                  testResult.setMessage(message);
                  testResult.setStatus(false);
                  return testResult;
            }
            
            try
            {
                  out.println("HTTP code" + code);
                  out.println("Message: " + message);
            }
            catch(IOException h)
            {
                  testResult.setStatus(false);
                  testResult.setMessage(message);
            }            
            if(code >= 400) 
            {
                  testResult.setStatus(false);
                  testResult.setMessage(message);
            }
            else 
            {
                  try
                  {
                  	
                  	//Reading from the input stream  //
                  	
                        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                  }
                  catch(IOException u)
                  {
                        testResult.setStatus(false);
                        testResult.setMessage("error at buffered reader");
                  }    
                  StringBuffer result = new StringBuffer();
                  String line = null;
                  try
                  {
                        while ((line = in.readLine()) != null ) 
                        {
                              result.append(line);
                              out.println (line);
                        }
                  }
                  catch(IOException me)
                  {
                        testResult.setStatus(false);
                        testResult.setMessage("error in readline");
                  }       
            }     			
           
             // In the jsp, data is written into the 
             // output stream after closing the stream
             //Exception is thrown.In the catch block 
             //a file " positiveClose.err is 
             //  created and message is written
             
           
           
            String dir=System.getProperty("user.home");
            String sss=dir+System.getProperty("file.separator")+"positiveClose.err";
            
            
            //the file is opened and read
            
            
            file=new File(sss);
            try
            {
                  fis =new FileReader(file);
            }
            catch(Exception ee)
            {
                  
                  testResult.setStatus(false);
                  testResult.setMessage("error in filereader");
                  return testResult;
            }   
            
            
            
            char b[]=new char[15];
            
            
            
            
            try
            {
               fis.read(b);
               
            }
            catch(IOException u)
            {
                  
                  testResult.setStatus(false);
                  testResult.setMessage("unable to read from file");
                  return testResult;
            }  
            
            
            
            String str= new String(b);
            str = str.trim();
            
            
            // checking for the written message
            
            if(str.equals("out is not null"))
            {
                  testResult.setStatus(true);
                  testResult.setMessage("OK");
                  return testResult;             
            }
            else 
            {
                  testResult.setStatus(false);
                  testResult.setMessage("error");
                  return testResult;         	       	   
            }
      }
}