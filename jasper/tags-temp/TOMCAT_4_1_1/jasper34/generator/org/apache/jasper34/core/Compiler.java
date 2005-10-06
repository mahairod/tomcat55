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

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.jasper34.core.*;
import org.apache.jasper34.parser.*;
import org.apache.jasper34.generator.*;
import org.apache.jasper34.runtime.JasperException;
import org.apache.jasper34.javacompiler.*;
import org.apache.jasper34.jsptree.*;

import org.apache.tomcat.util.log.*;

// XXX will be refactored

// This is the entry point in jasper, the API needs to be simplified,
// we'll use one instance of compiler per web application, better
// integration with the container, etc.


/**
 * If you want to customize JSP compilation aspects, this class is
 * something you should take a look at. 
 * 
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class Compiler {
    protected ContainerLiaison containerL;
    protected int debug=0;
    
    /**
     */
    public Compiler(ContainerLiaison liaison) {
	this.containerL=liaison;
    }

    public void setDebug( int d ) {
	debug=d;
    }
    
    // -------------------- Conversion methods --------------------

    /** 
     * Compile the jsp file from the current engine context
     *
     * @return true if the class file was outdated the jsp file
     *         was recomp iled. 
     */
    public boolean jsp2java(JspPageInfo pageInfo)
        throws FileNotFoundException, JasperException, Exception 
    {
        String javaFileName = pageInfo.getMangler().getJavaFileName();

	// XXX
	if( debug > 0 )
	    containerL.logKey("jsp.message.java_file_name_is",javaFileName );

        
        // Need the encoding specified in the JSP 'page' directive for
        //  - reading the JSP page
        //  - writing the JSP servlet source
        //  - compiling the generated servlets (pass -encoding to javac).
        // XXX - There are really three encodings of interest.

        String jspEncoding = "8859_1";          // default per JSP spec

	// We try UTF8 by default. If it fails, we use the java encoding 
	// specified for JspServlet init parameter "javaEncoding".
        String javaEncoding = "UTF8";

	// This seems to be a reasonable point to scan the JSP file
	// for a 'contentType' directive. If it found then the set
	// the value of 'jspEncoding to reflect the value specified.
	// Note: if (true) is convenience programming. It can be
	// taken out once we have a more efficient method.

	if (true) {
	    JspReader tmpReader = JspReader.
		createJspReader(pageInfo.getJspFile(), containerL,jspEncoding);
	    String newEncode = changeEncodingIfNecessary(tmpReader);
	    if (newEncode != null) jspEncoding = newEncode;
	}

        JspReader reader = JspReader.
	    createJspReader(pageInfo.getJspFile(),containerL,jspEncoding);

// 	PrintWriter pW =
// 	    createPrintWriter( javaFileName, javaEncoding,
// 			       containerL.getOptions().getJavaEncoding());
	// make sure the directory is created
	File javaFile=new File(javaFileName);
	new File( javaFile.getParent()).mkdirs();
	
	OutputStreamWriter osw=null; 
	try {
	    osw = new OutputStreamWriter(
		      new FileOutputStream(javaFileName),javaEncoding);
	} catch (java.io.UnsupportedEncodingException ex) {
	    // Try to get the java encoding from the "javaEncoding"
	    // init parameter for JspServlet.
	    javaEncoding = containerL.getOptions().getJavaEncoding();
	    if (javaEncoding != null) {
		try {
		    osw = new OutputStreamWriter(
			      new FileOutputStream(javaFileName),javaEncoding);
		} catch (java.io.UnsupportedEncodingException ex2) {
		    // no luck :-(
		    throw new JasperException(
			containerL.getString("jsp.error.invalid.javaEncoding",
					    new Object[] { 
						"UTF8", 
						javaEncoding,
					    }));
		}
	    } else {
		throw new JasperException(
		    containerL.getString("jsp.error.needAlternateJavaEncoding",
					new Object[] { "UTF8" }));		
	    }
	}
	//return new PrintWriter( osw );
	pageInfo.setJavaEncoding( javaEncoding );
	
	ServletWriter writer = new ServletWriter(new PrintWriter(osw));

        ParseEventListener listener =
	    new JspParseEventListener(containerL,
				      reader,
				      writer,
				      pageInfo);
        
        Parser p = new Parser(containerL,reader, listener);
        listener.beginPageProcessing();
        p.parse();
        listener.endPageProcessing();
        writer.close();

	if( debug > 0 ) {
	    File f = new File( pageInfo.getMangler().getJavaFileName());
	    containerL.log( "Created file : " + f +  " " + f.lastModified());
	}
	
	return true;
    }

    
    public void prepareCompiler( JavaCompiler javac, JspPageInfo pageInfo)
    {
	Options options=pageInfo.getOptions();
	if( debug > 0 ) logCompileInfo( pageInfo );

        javac.setEncoding(pageInfo.getJavaEncoding());
        javac.setClasspath( computeCompilerClassPath( pageInfo ) );
        javac.setOutputDir(containerL.getOutputDir());
        javac.setClassDebugInfo(pageInfo.getOptions().getClassDebugInfo());
	javac.setCompilerPath(options.getJspCompilerPath());
    }

    public void logCompileInfo( JspPageInfo pageInfo ) {
	containerL.log( "Compiling java file " +
			pageInfo.getMangler().getJavaFileName());
	Options options=pageInfo.getOptions();
	containerL.log( "CLASSPATH= " + computeCompilerClassPath( pageInfo) );
	if( debug > 2) {
	    containerL.log( "Encoding= " + pageInfo.getJavaEncoding() );
	    containerL.log( "OutputDir= " + containerL.getOutputDir() );
	}
	containerL.log( "DebugInfp= " +
			pageInfo.getOptions().getClassDebugInfo());
	if( options.getJspCompilerPath()!=null )
	    containerL.log( "CompilerPath= " + options.getJspCompilerPath()); 
    }

    static String CPSEP = System.getProperty("path.separator");
    
    public String computeCompilerClassPath( JspPageInfo pageInfo ) {
	StringBuffer sb=new StringBuffer();
	String cp=System.getProperty("java.class.path");
	sb.append( cp );
	cp=System.getProperty("tc_path_add");
	if( cp!=null )
	    sb.append( CPSEP ).append(cp);
	cp=containerL.getClassPath();
	if( cp!=null )
	    sb.append( CPSEP ).append(cp);
	cp=pageInfo.getOptions().getClassPath();
	if( cp!=null )
	    sb.append( CPSEP ).append(cp);

	sb.append( CPSEP ).append(containerL.getOutputDir());
	// XXX cache it in ContainerLiaison
	return sb.toString();
    }

    
    /** Create a compier based on our options
     */
    public JavaCompiler createJavaCompiler(JspPageInfo pageInfo) {
	return createJavaCompiler( pageInfo, null );
    }
    
    /** Create a compier using a certain plugin
     */
    public JavaCompiler createJavaCompiler(JspPageInfo pageInfo,
					   String javacName)
    {
	if( javacName==null ) 
	    javacName=pageInfo.getOptions().getJspCompilerPlugin();
	    
	JavaCompiler javaC=JavaCompiler.createJavaCompiler( containerL,
							    javacName );
	return javaC;
    }
    

    public void postCompile( JspPageInfo pageInfo ) {
	if(debug>0) containerL.log( "Generated " +
				    pageInfo.getMangler().getClassFileName() );

        if (!pageInfo.getOptions().getKeepGenerated()) {
            File javaFile = new File(pageInfo.getMangler().getJavaFileName());
            javaFile.delete();
        }
    }

    /* To compile a page:
        - createJavaCompiler
	- prepareCompiler
	- compiler.compile()
	- postCompile
	- report error ( javac.getCompilerMessage() )
    */

    // XXX move to parser
    /**
     * Change the encoding for the reader if specified.
     */
    private String changeEncodingIfNecessary(JspReader tmpReader)
	throws ParseException
    {

	// A lot of code replicated from Parser.java
	// Main aim is to "get-it-to-work".
	while (tmpReader.skipUntil("<%@") != null) {

	    tmpReader.skipSpaces();

	    // check if it is a page directive.
	    if (tmpReader.matches("page")) {

		tmpReader.advance(4);
		tmpReader.skipSpaces();
		
		try {
		    Hashtable attrs = tmpReader.parseTagAttributes();
		    String ct = (String) attrs.get("contentType");
		    if (ct != null) {
			int loc = ct.indexOf("charset=");
			if (loc > 0) {
			    String encoding = ct.substring(loc + 8);
			    return encoding;
			}
		    }
		} catch (ParseException ex) {
		    // Ignore the exception here, it will be caught later.
		    return null;
		}
	    }
	}
	return null;
    }

    /**
     * Remove generated files
     */
    public void removeGeneratedFiles(JspPageInfo pageInfo )
    {
	try{
	    // XXX Should we delete the generated .java file too?
	    String classFileName = pageInfo.getMangler().getClassFileName();
	    if(classFileName != null){
		File classFile = new File(classFileName);
		classFile.delete();
	    }
	}catch(Exception e){
	}
    }

    // For debug mostly
    public String getJavacCommand(JspPageInfo pageInfo) {
	String classpath = containerL.getClassPath();
	
        String sep = System.getProperty("path.separator");
        String[] argv = new String[] 
        {
            "-encoding",
            pageInfo.getJavaEncoding(),
            "-classpath",
	    System.getProperty("java.class.path")+ sep + classpath + sep +
	    System.getProperty("tc_path_add") + sep +
	    containerL.getOutputDir(),
            "-d", containerL.getOutputDir(),
            pageInfo.getMangler().getJavaFileName()
        };

        StringBuffer b = new StringBuffer();
        for(int i = 0; i < argv.length; i++) {
            b.append(argv[i]);
            b.append(" ");
        }

        if( debug > 0 )
	    containerL.logKey("jsp.message.compiling_with",  b.toString() );
	return b.toString();
    }


}


