/*
 * $Header$ 
 * $Date$ 
 * $Revision$
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
package org.apache.tools.moo.servlet;

import org.apache.tools.moo.servlet.Constants;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.util.Properties;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * All Server-side tests subclass ServletTest.
 * They need to override the getTitle and getDescription methods,
 * as well as the doTest(HttpServletRequest, HttpServletResponse) which
 * handles the test
 */
public abstract class ServletTest
    extends HttpServlet {
    
    /* 
     * We should probably set the status codes of the response here appropriately
     */
    public void
	service(HttpServletRequest request, HttpServletResponse response) {
        Properties props = new Properties();
	ServletOutputStream sos = null;
	
        try {
            props = doTest(request, response);
	    response.setStatus(HttpServletResponse.SC_OK); //OK
	    sos = response.getOutputStream();
        } catch (ServletException se) {
	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
	    props.put(Constants.Response.Exception,
		      se.getMessage());
        } catch (IOException ioe) {
	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
	    props.put(Constants.Response.Exception,
		      ioe.getMessage());
        } catch (RuntimeException e) { //servlet crash?
	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
	    props.put(Constants.Response.Exception,
		      e.getMessage());
	}	
	
	props.put(Constants.Response.TestClass,
		  this.getClass().getName());
	
	//try {
	//this used to be props.store, but that was not JDK 1.1 compliant
	props.save(sos, this.getClass().getName());	 
	//} catch (IOException ioe) {
	//System.out.println(this.getClass().getName() +
	//   " exception: " + ioe);
	//}
    }
    
    /**
     * a short title to identify the test being run
     */
    public abstract String
	getTitle();
    
    /**
     * A more-verbose description of the server-side test than the title
     */
    public abstract String
	getDescription ();
    
    /**
     * This method returns a properties list whose keys are a subset of the
     * Constants.Response.* list.    
     *
     * I don't believe that this method should affect the response variable
     * as this needs to be a properties file which will be written by service()
     */
    public abstract Properties
	doTest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException;
}
