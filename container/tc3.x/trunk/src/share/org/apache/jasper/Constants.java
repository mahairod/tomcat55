/*
 * $Header$
 * $Revision$
 * $Date$
 *
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
 *
 */ 

package org.apache.jasper;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * Some constants and other global data that are used by the compiler and the runtime.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class Constants {
    /**
     * The base class of the generated servlets. 
     */
    public static final String JSP_SERVLET_BASE = "HttpJspBase";

    /**
     * _jspService is the name of the method that is called by 
     * HttpJspBase.service(). This is where most of the code generated
     * from JSPs go.
     */
    public static final String SERVICE_METHOD_NAME = "_jspService";

    /**
     * Default servlet content type.
     */
    public static final String SERVLET_CONTENT_TYPE = "text/html";

    /**
     * These classes/packages are automatically imported by the
     * generated code. 
     *
     * Should java.io.* be in here?
     */
    public static final String[] STANDARD_IMPORTS = { 
	"javax.servlet.*", "javax.servlet.http.*", "javax.servlet.jsp.*", 
        "javax.servlet.jsp.tagext.*",
	"java.io.PrintWriter", "java.io.IOException", "java.io.FileInputStream",
        "java.io.ObjectInputStream", "java.util.Vector",
	"org.apache.jasper.runtime.*", "java.beans.*",
	"org.apache.jasper.JasperException"
    };

    /**
     * Codes that indicate how verbose you want the JSP engine to be. 
     */
    public static final int FATAL_ERRORS = Integer.MIN_VALUE;
    public static final int WARNING = 0;
    public static final int LOW_VERBOSITY = 1;
    public static final int MED_VERBOSITY = 2;
    public static final int HIGH_VERBOSITY = 3;

    public static int jspVerbosityLevel = 0;


    /**
     * Default size of the JSP buffer.
     */
    public static final int K = 1024;
    public static final int DEFAULT_BUFFER_SIZE = 8*K;

    /**
     * The query parameter that causes the JSP engine to just
     * pregenerated the servlet but not invoke it. 
     */
    public static final String PRECOMPILE = "jsp_precompile";

    /**
     * Servlet context and request attributes that the JSP engine
     * uses. 
     */
    public static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";
    public static final String TMP_DIR = "javax.servlet.context.tempdir";

    /**
     * A token which is embedded in file names of the generated
     * servlet. 
     */
    public static final String JSP_TOKEN = "_jsp_";

    /**
     * ID and location of the DTD for tag library descriptors. 
     */
    public static final String 
        TAGLIB_DTD_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    public static final String
        TAGLIB_DTD_RESOURCE = "/org/apache/jasper/resources/taglib.dtd";

    /**
     * Default URLs to download the pluging for Netscape and IE.
     */
    public static final String NS_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/";

    public static final String IE_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";


    /**
     * This is where all our error messages and such are stored. 
     */
    private static ResourceBundle resources;
    
    private static void initResources() {
	try {
	    resources =
		ResourceBundle.getBundle("org.apache.jasper.resources.messages");
	} catch (MissingResourceException e) {
	    throw new Error("Fatal Error: missing resource bundle: "+e.getClassName());
	}
    }

    /**
     * Get hold of a "message" or any string from our resources
     * database. 
     */
    public static final String getString(String key) {
        return getString(key, null);
    }

    /**
     * Format the string that is looked up using "key" using "args". 
     */
    public static final String getString(String key, Object[] args) {
        if (resources == null) 
            initResources();
        
        try {
            String msg = resources.getString(key);
            if (args == null)
                return msg;
            MessageFormat form = new MessageFormat(msg);
            return form.format(args);
        } catch (MissingResourceException ignore) {
            throw new Error("Fatal Error: missing resource: "+ignore.getClassName());
        }
    }

    /**
     * Tells you if the current verbosity level allow output of
     * messages with a certain verbosity level. 
     */
    public static final boolean matchVerbosity(int verbosityLevel) {
        return verbosityLevel <= jspVerbosityLevel;
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
    public static final void message(String key, int verbosityLevel) {
        message(key, null, verbosityLevel);
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
    public static final void message(String key, Object[] args, int verbosityLevel) {
        if (matchVerbosity(verbosityLevel))
            System.err.println(getString(key, args));
    }
}

