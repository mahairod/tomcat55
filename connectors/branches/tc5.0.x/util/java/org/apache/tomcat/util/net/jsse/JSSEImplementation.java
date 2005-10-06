/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.net.jsse;

import java.net.Socket;

import org.apache.tomcat.util.compat.JdkCompat;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.ServerSocketFactory;

/* JSSEImplementation:

   Concrete implementation class for JSSE

   @author EKR
*/
        
public class JSSEImplementation extends SSLImplementation
{
    static final String JSSE14Factory = 
        "org.apache.tomcat.util.net.jsse.JSSE14Factory";
    static final String JSSE13Factory = 
        "org.apache.tomcat.util.net.jsse.JSSE13Support";
    static final String SSLSocketClass = "javax.net.ssl.SSLSocket";

    static org.apache.commons.logging.Log logger = 
        org.apache.commons.logging.LogFactory.getLog(JSSEImplementation.class);

    private JSSEFactory factory;

    public JSSEImplementation() throws ClassNotFoundException {
        // Check to see if JSSE is floating around somewhere
        Class.forName(SSLSocketClass);
	if( JdkCompat.isJava14() ) {
	    try {
		Class factcl = Class.forName(JSSE14Factory);
		factory = (JSSEFactory)factcl.newInstance();
	    } catch(Exception ex) {
		factory = new JSSE13Factory();
		if(logger.isDebugEnabled()) {
		    logger.debug("Error getting factory: " + JSSE14Factory, ex);
		}
	    }
	} else {
	    factory = new JSSE13Factory();
	}
    }


    public String getImplementationName(){
      return "JSSE";
    }
      
    public ServerSocketFactory getServerSocketFactory()  {
        ServerSocketFactory ssf = factory.getSocketFactory();
        return ssf;
    } 

    public SSLSupport getSSLSupport(Socket s) {
        SSLSupport ssls = factory.getSSLSupport(s);
        return ssls;
    }
}
