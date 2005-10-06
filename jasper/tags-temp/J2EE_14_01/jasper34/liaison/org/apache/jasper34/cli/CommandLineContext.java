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

package org.apache.jasper34.cli;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.jasper34.generator.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.core.Compiler;
import org.apache.jasper34.runtime.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.liaison.*;
import org.apache.jasper34.parser.*;

//import org.apache.jasper.runtime.JspLoader;
// Use the jasper loader - the only function used is to add a jar
//import org.apache.jasper34.servlet.JasperLoader;
import org.apache.jasper34.runtime.JasperException;

/**
 * Holds data used on a per-page compilation context that would otherwise spill
 * over to other pages being compiled.  Things like the taglib classloaders
 * and directives.
 *
 *@author Danno Ferrin
 */
public class CommandLineContext extends ContainerLiaison
{

    String classPath;
    JspReader reader;
    ServletWriter writer;
    ClassLoader loader;
    boolean errPage;
    String jspFile;
    String contentType;
    Options options;

    String uriBase;
    File uriRoot;

    //    boolean packageNameLocked;
    //    boolean classNameLocked;
    boolean outputInDirs;

    public CommandLineContext(String newClassPath,
                              String newJspFile, String newUriBase,
                              String newUriRoot, boolean newErrPage,
                              Options newOptions)
    throws JasperException
    {
        classPath = newClassPath;
        uriBase = newUriBase;
        String tUriRoot = newUriRoot;
        jspFile = newJspFile;
        errPage = newErrPage;
        options = newOptions;

        if (uriBase == null) {
            uriBase = "/";
        } else if (uriBase.charAt(0) != '/') {
            // strip the basde slash since it will be combined with the
            // uriBase to generate a file
            uriBase = "/" + uriBase;
        }

        if (uriBase.charAt(uriBase.length() - 1) != '/') {
            uriBase += '/';
        }

        if (tUriRoot == null) {
            uriRoot = new File("");
        } else {
            uriRoot = new File(tUriRoot);
            if (!uriRoot.exists() || !uriRoot.isDirectory()) {
               throw new JasperException(
                        ContainerLiaison.getString("jsp.error.jspc.uriroot_not_dir"));
            }
        }
    }

    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
        return classPath;
    }
    
    /**
     * Get the input reader for the JSP text. 
     */
    public JspReader getReader() {
        return reader;
    }
    
    /**
     * Where is the servlet being generated?
     */
    public ServletWriter getWriter() {
        return writer;
    }
    
    /**
     * What class loader to use for loading classes while compiling
     * this JSP? I don't think this is used right now -- akv. 
     */
    public ClassLoader getClassLoader() {
	// Construct a loader
	if( loader != null ) return loader;
	File uriDir = new File(this.getRealPath("/"));

	ClassLoader parentLoader=getClass().getClassLoader();

	try {
	    URL urls[]=getWebAppClassPath(uriDir);
	    // XXX compat XXX
	    loader= new URLClassLoader( urls,
					parentLoader );
	} catch( IOException ex ) {
	    ex.printStackTrace();
	}
	return loader;
    }

    // XXX compat XXX 
    private URL[] getWebAppClassPath( File uriDir )
	throws IOException
    {
	String uriRoot=uriDir.toString();
	if(debug>0) log( "URIRoot " + uriRoot );
	Vector urls=new Vector();
	
	urls.add( options.getScratchDir().toURL());
	if(debug>0) log("CP: " + options.getScratchDir().toURL());
	
	File classes = new File(uriRoot + "/WEB-INF/classes");
	if (classes.exists()) {
	    if(debug>0) log("CP: " + classes.toURL());
	    urls.add(classes.toURL());
	}

	File lib = new File( uriRoot + "/WEB-INF/lib");
	if (lib.exists() && lib.isDirectory()) {
	    String[] libs = lib.list();
	    for (int i = 0; i < libs.length; i++) {
		File libFile = new File(lib.toString()
					+ File.separator
					+ libs[i]);
		urls.add(libFile.toURL());
		if(debug>0) log("CP: " + libFile.toURL());
	    }
	}
	
	URL urlA[]=new URL[ urls.size() ];
	urls.toArray( urlA );
	return urlA;
    }

    /**
     * Are we processing something that has been declared as an
     * errorpage? 
     */
    public boolean isErrorPage() {
        return errPage;
    }
    
    /**
     * What is the scratch directory we are generating code into?
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
        return options.getScratchDir().toString();
    }
    
    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated() {
        return options.getKeepGenerated();
    }

    /**
     * What's the content type of this JSP? Content type includes
     * content type and encoding. 
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get hold of the Options object for this context. 
     */
    public Options getOptions() {
        return options;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setReader(JspReader reader) {
        this.reader = reader;
    }
    
    public void setWriter(ServletWriter writer) {
        this.writer = writer;
    }
    
    public void setErrorPage(boolean isErrPage) {
        errPage = isErrPage;
    }

    // What is this ?
    //     public void lockPackageName() {
    //         packageNameLocked = true;
    //     }
    
    //     public void lockClassName() {
    //         classNameLocked = true;
    //     }

    public void setOutputInDirs(boolean newValue) {
        outputInDirs = true;
    }

    public boolean isOutputInDirs() {
        return outputInDirs;
    }


    /** 
     * Get the full value of a URI relative to this compilations context
     * uses current file as the base.
     */
    public String resolveRelativeUri(String uri, String uriBase ) {
        if (uri.startsWith("/")) {
            return uri;
        } else {
            return uriBase + uri;
        }
    }


    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     *@returns a null if the resource cannot be found or represented 
     *         as an InputStream.
     */
    public java.io.InputStream getResourceAsStream(String res) {
        InputStream in;
        // fisrt try and get it from the URI
        try {
            in = new FileInputStream(getRealPath(res));
        } catch (IOException ioe) {
            in = null;
        }
        // next, try it as an absolute name
        if (in == null) try {
            in = new FileInputStream(res);
        } catch (IOException ioe) {
            in = null;
        }
        // that dind't work, last chance is to try the classloaders
        if (in == null) {
            in = getClassLoader().getResourceAsStream(res);
        }
        return in;
    }


    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path) {
        path = resolveRelativeUri(path, uriBase);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File f = new File(uriRoot, path.replace('/', File.separatorChar));
        return f.getAbsolutePath();
    }

    
    public void readWebXml( TagLibraries tli )
	throws IOException, JasperException
    {
	TagLibReader reader=new TagLibReader( this, tli );
	reader.readWebXml( tli );
    }

    /** Read a tag lib descriptor ( tld ). You can use the default
	implementation ( TagLibReader ).
    */
    public void readTLD( TagLibraries libs,
			 TagLibraryInfoImpl tl, String prefix, String uri,
			 String uriBase )
    	throws IOException, JasperException
    {
	TagLibReader reader=new TagLibReader( this, libs );
	reader.readTLD( tl, prefix, uri, uriBase );
    }

    private static int debug=1;
}

