
/*

   $Header: /home/cvs/jakarta-check/src/clients/org/apache/jcheck/jsp/client/core_syntax/directives/page/buffer/positiveBuffCreate
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








/*   positiveBuffCreate  */





package org.apache.jcheck.jsp.client.core_syntax.directives.page.buffer;

import org.apache.jcheck.jsp.util.*;
import org.apache.tools.moo.jsp.*;
import org.apache.tools.moo.TestResult;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;



public class positiveBuffCreate extends PositiveJspCheckTest{

     StringManager sm= StringManager.getManager(UtilConstants.Package);
     
     public String getDescription()
       {
          return sm.getString("positiveBuffCreate.description");
       }
       
           
       
     public TestResult runTest()      
     
        {
            TestResult testResult=new TestResult();
            
            try{
            
                 
                 HttpURLConnection connection=getConnection(null,null,null,"GET");
                    
                 int code = connection.getResponseCode();
	         String message = connection.getResponseMessage();
	             
	      
	         out.println("HTTP code" + code);
	         out.println("Message: " + message);
	    
	    if (code >= 400) {
		testResult.setStatus(false);
		testResult.setMessage(message);
	    }
	    else {
	    	
	    	 //assume request was OK
		
		// Get the "actual" result from the socket stream.
		
		BufferedReader in = new
		BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer result = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null ) {
		    result.append(line);
		    out.println (line);
		}
                    
                    
                String str=result.toString();     
                
                 
                  
                int beg=str.indexOf("01");
                
                    
                           
                                  
                int last=str.indexOf("999");
                
                                  
                if((beg>=0) && (last>0))
             
                   {
                         testResult.setMessage("OK");
                         testResult.setStatus(true);
                   }
                      
                }                                      
                    
                    
                }catch(Exception e){
                    
                      testResult.setMessage("FAIL");
                      testResult.setStatus(false);                                            
                 }
                 
               return testResult;
                                   
         } 
   }                         