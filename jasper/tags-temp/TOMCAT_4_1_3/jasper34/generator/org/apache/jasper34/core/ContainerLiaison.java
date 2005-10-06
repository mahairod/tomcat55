/*
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
 *
 */ 

package org.apache.jasper34.core;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.res.StringManager;

import org.apache.jasper34.jsptree.*;
import java.io.IOException;
import org.apache.jasper34.runtime.JasperException;
// XXX Not used yet - will replace part of Constants and JspCompilationCtx


/**
 * The container must override this to provide services to jasper.
 *
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public abstract class ContainerLiaison {
    protected Options options;
    
    protected ContainerLiaison() {
    }
    

    // -------------------- Options --------------------
    /**
     * Get hold of the Options object for this context. 
     */
    public Options getOptions() {
	return options;
    }

    public void setOptions( Options opt ) {
	options=opt;
    }

    // --------------------  resource access --------------------
    
    /** 
     * Get the full value of a URI relative to a compiled page.
     * Used only by taglib code to find the taglib descriptor relative
     * to the JSP file.
     */
    public abstract String resolveRelativeUri(String uri, String jspBase);

    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     *@returns a null if the resource cannot be found or represented 
     *         as an InputStream.
     */
    public abstract java.io.InputStream getResourceAsStream(String res);

    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     * Used to get a real file relative to the base context.
     * Used in JspReader, CommandLineContext, TagLibReader, JspServlet
     */
    public abstract String getRealPath(String path);

    // -------------------- WEB-INF access --------------------

    /** Read web.xml and add all the taglib locations to the
	TagLibraries ( if it wasn't done already ).
	It'll call back addTaglibLocation.
	You can use the default implementation ( TagLibReader )
	or container specific code.
    */
    public abstract void readWebXml( TagLibraries tli )
	throws IOException, JasperException;

    /** Read a tag lib descriptor ( tld ). You can use the default
	implementation ( TagLibReader ).
    */
    public abstract void readTLD( TagLibraries libs, TagLibraryInfoImpl tl,
				  String prefix, String uri, String jspBase )
	throws IOException, JasperException;


    // -------------------- Classpath and class loader

    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public abstract String getClassPath();

    /**
     * What class loader to use for loading classes while compiling
     * this JSP? I don't think this is used right now -- akv. 
     */
    public abstract  ClassLoader getClassLoader();

    // -------------------- Locations --------------------

    /**
     * What is the scratch directory we are generating code into?
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public abstract String getOutputDir();

    // -------------------- New messages --------------------

    public void log( String msg ) {
	log( msg, null );
    }

    public void log( String msg, Throwable t ) {
	System.out.println("ContainerLiaison: " + msg );
	if( t!=null )
	    t.printStackTrace();
    }

    public void logKey( String key, String param1 ) {
	// XXX optimize
	String msg= getString( key, new Object[] { param1 });
	log( msg );
    }
    
//     public abstract void log( String msg, Throwable t );
    
    // -------------------- messages --------------------
    // XXX TODO

    /**
     * Get hold of a "message" or any string from our resources
     * database. 
     */
    public static String getString(String key) {
        return getString(key, null);
    }

    /**
     * Format the string that is looked up using "key" using "args". 
     */
    public static String getString(String key, Object[] args) {
	return resources.getString(key,args);
    }

    
    /** 
     * Print a message into standard error with a certain verbosity
     * level. 
     * 
     * @param key is used to look up the text for the message (using
     *            getString()). 
     * @param verbosityLevel is used to determine if this output is
     *                       appropriate for the current verbosity
     *                       level. 
     */
    public static void message(String key, int verbosityLevel) {
	message( key, null, verbosityLevel );
    }


    /**
     * Print a message into standard error with a certain verbosity
     * level after formatting it using "args". 
     *
     * @param key is used to look up the message. 
     * @param args is used to format the message. 
     * @param verbosityLevel is used to determine if this output is
     *                       appropriate for the current verbosity
     *                       level. 
     */
    public static void message(String key, Object[] args, int verbosityLevel) {
	if (jasperLog == null)
	    jasperLog = Log.getLog("JASPER_LOG", null);
	
	if (jasperLog != null){
	    String msg = getString(key,args);
	    msg=(msg==null)?key:msg;
	    jasperLog.log(msg, verbosityLevel);
        }
    }

    // -------------------- Implementation --------------------
    // XXX will be moved to base impl.

    public void setLog( Log l ) {
	jasperLog=l;
    }
    
    /**
     * This is where all our error messages and such are stored. 
     */
    private static StringManager resources=
	StringManager.getManager("org.apache.jasper34.runtime.res");

    private static Log jasperLog = null;    

    // -------------------- Default liaison --------------------
    
    static ContainerLiaison liaison;

    public static ContainerLiaison getContainerLiaison() {
	return liaison;
    }

    public static void setContainerLiaison( ContainerLiaison l ) {
	liaison=l;
    }



}

