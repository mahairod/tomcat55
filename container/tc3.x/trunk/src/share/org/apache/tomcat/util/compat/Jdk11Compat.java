/*
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

package org.apache.tomcat.util.compat;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.tomcat.util.depend.DependClassLoader;

/** General-purpose utility to provide backward-compatibility and JDK
    independence. This allow use of JDK1.2 ( or higher ) facilities if
    available, while maintaining the code compatible with older VMs.

    The goal is to make backward-compatiblity reasonably easy.

    The base class supports JDK1.1 behavior. 
*/
public class Jdk11Compat {
    static org.apache.commons.logging.Log logger =
	org.apache.commons.logging.LogFactory.getLog(Jdk11Compat.class);
    
    /** Return java version as a string
     */
    public static String getJavaVersion() {
	return javaVersion;
    }

    public static boolean isJava2() {
	return java2;
    }    

    /** Return a class loader. For JDK1.2+ will return a URLClassLoader.
     *  For JDK1.1 will return a substitute ( util.SimpleClassLoader )
     */
    public ClassLoader newClassLoaderInstance( URL urls[],
					       ClassLoader parent )
    {
	return new SimpleClassLoader( urls, parent );
    }

    public Object getAccessControlContext() throws Exception {
	return null;
    }
    
    /** Do a priviledged action. For java2 a wrapper will be provided
	and the AccesscController will be called.
     */
    public Object doPrivileged( Action action, Object acc ) throws Exception {
	// ( using util's permissions !)
	return action.run();
    }

    /** Set the context class loader - if possible.
     */
    public void setContextClassLoader( ClassLoader cl ) {
	// nothing
    }

    public void refreshPolicy() {
	// nothing
    }
    
    /** Get the context class loader, if java2.
     */
    public ClassLoader getContextClassLoader() {
	return null;
    }

    public ClassLoader getParentLoader( ClassLoader cl ) {
	if( cl instanceof DependClassLoader ) {
	    return ((DependClassLoader)cl).getParentLoader();
	}
	if( cl instanceof SimpleClassLoader ) {
	    return ((SimpleClassLoader)cl).getParentLoader();
		    }
	return null;
    }
    
    public URL[] getURLs(ClassLoader cl,int depth){
        int c=0;
        do{
            while( cl instanceof DependClassLoader && cl!=null)
	      cl=((DependClassLoader)cl).getParentLoader();
            if (depth==c) return ((SimpleClassLoader)cl).getURLs();
            c++;
            cl=((SimpleClassLoader)cl).getParentLoader();
        }while((cl!=null) && ( depth >= c ));
        return null;
    }

    // Other methods, as needed

    public java.util.ResourceBundle getBundle(String name, Locale loc, ClassLoader cl ) {
	return ResourceBundle.getBundle(name, loc);
    }

    public Object getX509Certificates( byte x509[] ) throws Exception {
	// No x509 certificate in JDK1.1
	return null;
    }
    

    // -------------------- Factory -------------------- 
    /** Get a compatibiliy helper class.
     */
    public static Jdk11Compat getJdkCompat() {
	return compat;
    }
 
    // -------------------- Implementation --------------------
    
    // from ant
    public static final String JAVA_1_0 = "1.0";
    public static final String JAVA_1_1 = "1.1";
    public static final String JAVA_1_2 = "1.2";
    public static final String JAVA_1_3 = "1.3";
    public static final String JAVA_1_4 = "1.4";

    static String javaVersion;
    static boolean java2=false;
    static Jdk11Compat compat;
    
    static {
	init();
    }

    // class providing java2 support
    static final String JAVA2_SUPPORT=
	"org.apache.tomcat.util.compat.Jdk12Support";

    private static void init() {
        try {
            javaVersion = JAVA_1_0;
            Class.forName("java.lang.Void");
            javaVersion = JAVA_1_1;
            Class.forName("java.lang.ThreadLocal");
	    java2=true;
            javaVersion = JAVA_1_2;
            Class.forName("java.lang.StrictMath");
            javaVersion = JAVA_1_3;
	    Class.forName("java.lang.CharSequence");
	    javaVersion = JAVA_1_4;
        } catch (ClassNotFoundException cnfe) {
            // swallow as we've hit the max class version that we have
        }
	if( java2 ) {
	    try {
		Class c=Class.forName(JAVA2_SUPPORT);
		compat=(Jdk11Compat)c.newInstance();
	    } catch( Exception ex ) {
		compat=new Jdk11Compat();
	    }
	} else {
	    compat=new Jdk11Compat();
	    // Install jar handler if none installed
	    try {
		URL url=new URL( "jar:file:/test.jar!/foo");
	    } catch( MalformedURLException ex ) {
		if( logger.isDebugEnabled() ) 
		    logger.debug( "Installing jar protocol handler ");
		String handlers=System.getProperty("java.protocol.handler.pkgs");
		if( handlers==null ) {
		    handlers=URL_COMPAT_HANDLERS;
		} else {
		    if( handlers.indexOf( URL_COMPAT_HANDLERS) < 0 ) {
			handlers+=":" + URL_COMPAT_HANDLERS;
		    }
		}
		System.getProperties().put("java.protocol.handler.pkgs", handlers);
		if( logger.isDebugEnabled() ) {
		    try {
			URL url=new URL( "jar:file:/test.jar!/foo");
		    } catch( MalformedURLException ex1 ) {
			logger.debug("Jar protocol failing ", ex1);
		    }
		}
	    }
	}
    }

    private static final String URL_COMPAT_HANDLERS=
	"org.apache.tomcat.util.compat";

}
