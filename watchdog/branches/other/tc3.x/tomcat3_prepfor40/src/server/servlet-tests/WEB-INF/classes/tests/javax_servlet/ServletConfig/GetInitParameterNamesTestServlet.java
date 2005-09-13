/*
 * $Header$
 * $Date$
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


package tests.javax_servlet.ServletConfig;


import org.apache.tools.moo.servlet.ServletTest;
import org.apache.tools.moo.servlet.Constants;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import java.util.Properties;
import java.util.Enumeration;
import javax.servlet.ServletException;
import java.io.IOException;


/**
 *	Test for getInitParameter(String) method
 */


public class GetInitParameterNamesTestServlet extends ServletTest {


/**
 *	getInitParameterNames returns an Enumeration of values
 *	associated with the init parameters
 */
	public String getTitle() {

		return "GetInitParameterNamesTestServlet";
	}

	public String getDescription() {
		return "GetInitParameterNamesTestServlet";
	}

	public Properties doTest (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Properties props = new Properties();
		props.put(Constants.Response.Title, getTitle());
		props.put(Constants.Response.Description,getDescription());

		String msg = "Problem getting init param names";
		String status = "false";

		ServletConfig config = this.getServletConfig();
		Enumeration enum = config.getInitParameterNames();

		int count =0;

		//count should be 2

		while(enum.hasMoreElements()) {

			enum.nextElement();
			count++;
		}

		if(count==2) {

			count=0;

			enum = config.getInitParameterNames();

			while(enum.hasMoreElements()) {

				String name=(String)enum.nextElement();

				if(name.equals("Team")||name.equals("org")) {

					msg="OK";
					status="true";
					count++;
				}
				else {

					msg="Problem getting InitParam Names";
					status="false";
				}
			}
			if(count!=2) {

				status="false";
				msg="problem gettting InitParam Names";
			}

		}

		props.put(Constants.Response.Status, status);
		props.put(Constants.Response.Message, msg);
		return props;
	}
}
