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
package org.apache.tools.moo;

import java.net.URL;
import java.util.Properties;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.lang.SecurityException;
import java.net.MalformedURLException;

public class URLHelper {
    
    public static final String DefaultProtocol = "http";
    private static final boolean Debug = true;
    
    public static URL getURL(String file)
        throws MalformedURLException {
	
	//rewritten to avoid calls to System.getProperties which may
	//be unacceptable with SecurityManager
	  
	String protocol = new String(DefaultProtocol);
	String host = Main.DefaultHost;
	String port = Main.DefaultPort;
	int portInt = -1;
	
	// establish host
	// look for a system variable named test.hostName
	try {
	    host = System.getProperty(Main.HostName, Main.DefaultHost);
	} catch (SecurityException se) {
	    //host reverts to DefaultHost as in declaration
	    if (Debug) {
		se.printStackTrace();
            }
	}
	
	//establish port
	//look for a system variable named test.port
	try {
	    port = System.getProperty(Main.PortName, Main.DefaultPort);
	} catch (SecurityException se) {
	    //port reverts to DefaultPort as in declaration
	    if (Debug) {
		se.printStackTrace();
            }
	}
	
	try {
            portInt = Integer.valueOf(port).intValue();
	} catch (NumberFormatException nfe) {
	    //port defaults to -1 not test.port -- is this acceptable?
	    if (Debug) {
		nfe.printStackTrace();
	    }
	}
	
        if (file == null || file.trim().length() == 0) {
            file = new String("/");
        }
	
        URL url = new URL(protocol, host, portInt, file);
	
        return url;
    }
}
