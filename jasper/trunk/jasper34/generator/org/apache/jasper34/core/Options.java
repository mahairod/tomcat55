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
 */ 

package org.apache.jasper34.core;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

// Made it a base class, backed by a simple properties-like adapter interface.
// This should work for both Properties and ServletConfig ( or other apis )
// Added static final strings with all the known properties.

/**
 * A class to hold all init parameters specific to the JSP engine.
 *
 * The container liaison will implement this interface to pass options
 * or use one of the existing implementations. We  have support for
 * using Properties, command line options, ServletConfig and setters
 * ( might need some refactoring )
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 */
public class Options {

    public static interface PropertyAdapter {
	public String getProperty( String key, String defaultValue );
	public void setProperty( String key, String value );
    }
    
    PropertyAdapter args;
    
    public Options(PropertyAdapter args) {
	this.args=args;
    }
    // -------------------- Access to repository --------------------

    public PropertyAdapter getPropertyAdapter() {
	return args;
    }

    // -------------------- Known properties --------------------
    
    public static final String KEEP_GENERATED="keepgenerated";
    public static final String SEND_ERROR_TO_CLIENT="sendErrToClient";
    public static final String CLASS_DEBUG_INFO="classDebugInfo";
    public static final String CLASS_PATH="classpath";
    public static final String SCRATCH_DIR="scratchdir";    
    public static final String MAPPED_FILE="mappedfile";

    static final String DEFAULT_IE_CLASS_ID =
	"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";


    // -------------------- Getter methods --------------------
    
    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated() {
	return s2b( args.getProperty(KEEP_GENERATED, "true") );
    }
    
    /**
     * Do you want support for "large" files? What this essentially
     * means is that we generated code so that the HTML data in a JSP
     * file is stored separately as opposed to those constant string
     * data being used literally in the generated servlet. 
     */
    public boolean getLargeFile() {
        return s2b( args.getProperty("largefile", "false"));
    }

    /**
     * Do you want support for "mapped" files? This will generate
     * servlet that has a print statement per line of the JSP file.
     * This seems like a really nice feature to have for debugging.
     */
    public boolean getMappedFile() {
        return s2b( args.getProperty(MAPPED_FILE, "false"));
    }
    
    /**
     * Do you want stack traces and such displayed in the client's
     * browser? If this is false, such messages go to the standard
     * error or a log file if the standard error is redirected. 
     */
    public boolean getSendErrorToClient() {
	return s2b( args.getProperty( SEND_ERROR_TO_CLIENT, "false" ));
    }
 
    /**
     * Should we include debug information in compiled class?
     */
    public boolean getClassDebugInfo() {
        return s2b( args.getProperty( CLASS_DEBUG_INFO, "false" ));
    }

    /**
     * Need to have this as is for versions 4 and 5 of IE. Can be set from
     * the initParams so if it changes in the future all that is needed is
     * to have a jsp initParam of type ieClassId="<value>"
     */
    public String getIeClassId() {
	return args.getProperty( "ieClassId" , DEFAULT_IE_CLASS_ID);
    }

    // Validation should be done by the caller.
    /**
     * I want to see my generated servlets. Which directory are they
     * in?
     */
    public File getScratchDir() {
	// Special case: args should try to get it from context
	String sdProp=args.getProperty( SCRATCH_DIR, null );
	if( sdProp==null ) {
	    sdProp=System.getProperty("java.io.tmpdir");
	}
	if( sdProp==null ) return null;
	return new File(sdProp);

	/* XXX integrate ( from Embeded )
	scratchDir = (File) context.getAttribute(Constants.TMP_DIR);
	if (dir != null)
	scratchDir = new File(dir);
	*/

	/*
	  if (!(scratchDir.exists() && scratchDir.canRead() &&
	  scratchDir.canWrite() && scratchDir.isDirectory()))
	*/
    }

    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath() {
	return args.getProperty( CLASS_PATH , null );
    }

    // Used to be Class - keep this simple, the caller should be able to
    // handle
    /**
     * What compiler plugin should I use to compile the servlets
     * generated from JSP files?
     */
    public String getJspCompilerPlugin() {
	String type=args.getProperty( "jspCompilerPlugin", null );
	return type;
    }

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public String getJspCompilerPath() {
	return args.getProperty( "jspCompilerPath", null );
    }
    
//     /**
//      * ProtectionDomain for this JSP Context when using a SecurityManager
//      */
//     public Object getProtectionDomain();

    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    public String getJavaEncoding() {
	return args.getProperty("javaEncoding", "UTF8");
    }

    /** Generate comment-style line number mappings
     */
    public boolean getGenerateCommentMapping() {
	return s2b(args.getProperty( "generateCommentMapping", "false"));
    }

    // -------------------- Helpers --------------------

    protected boolean s2b( String s ) {
	return new Boolean( s ).booleanValue();
	// XXX Add validation
    }

}
