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

import java.util.Hashtable;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

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
    
    /**
     */
    public Compiler(ContainerLiaison liaison) {
	this.containerL=liaison;
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
	// assert pageInfo has valid mangler, jspFile

        String javaFileName = pageInfo.getMangler().getJavaFileName();
        //pageInfo.setServletJavaFileName(javaFileName);

        containerL.message("jsp.message.java_file_name_is",
                          new Object[] { javaFileName },
                          Log.DEBUG);

        
        
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

	OutputStreamWriter osw; 
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

	pageInfo.setJavaEncoding( javaEncoding );
	
	ServletWriter writer = new ServletWriter(new PrintWriter(osw));

	//        ctxt.setReader(reader);
	//        ctxt.setWriter(writer);

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
	
	return true;
    }

    
    /** 
     * Compile the jsp file from the current engine context
     *
     * @return true if the class file was outdated the jsp file
     *         was recomp iled. 
     */
    public boolean compile(JspPageInfo pageInfo, JavaCompiler javac)
        throws FileNotFoundException, JasperException, Exception 
    {
	jsp2java( pageInfo );
	return javac( pageInfo, javac );
    }

    public void prepareCompiler( JavaCompiler javac, Options options,
				 JspPageInfo pageInfo)
    {
	String sep = System.getProperty("path.separator");
	String classpath = containerL.getClassPath(); 
        javac.setEncoding(pageInfo.getJavaEncoding());
        javac.setClasspath( System.getProperty("java.class.path")+ sep + 
                            System.getProperty("tc_path_add") + sep +
                            classpath + sep + containerL.getOutputDir());
        javac.setOutputDir(containerL.getOutputDir());
        javac.setClassDebugInfo(pageInfo.getOptions().getClassDebugInfo());

	String compilerPath = options.getJspCompilerPath();
        if (compilerPath != null)
            javac.setCompilerPath(compilerPath);

    }
    
    public boolean javac(JspPageInfo pageInfo, String javacName)
        throws FileNotFoundException, JasperException, Exception 
    {
	JavaCompiler javaC=null;
	try {
	    Class jspCompilerPlugin=Class.forName(javacName);
	    javaC=JavaCompiler.createJavaCompiler( containerL,
						   jspCompilerPlugin );
	} catch( Exception ex ) {
	    throw ex;
	}
	return javac( pageInfo, javaC );
    }


    public boolean javac(JspPageInfo pageInfo, JavaCompiler javac)
        throws FileNotFoundException, JasperException, Exception 
    {
	prepareCompiler( javac, pageInfo.getOptions(), pageInfo);
        ByteArrayOutputStream out = new ByteArrayOutputStream (256);

	prepareCompiler( javac, pageInfo.getOptions(), pageInfo );
	javac.setMsgOutput(out);
        /**
         * Execute the compiler
         */
        boolean status = javac.compile(pageInfo.getMangler().getJavaFileName());

        if (!containerL.getOptions().getKeepGenerated()) {
            File javaFile = new File(pageInfo.getMangler().getJavaFileName());
            javaFile.delete();
        }
    
        if (status == false) {
            String msg = out.toString ();
            throw new JasperException(containerL.getString("jsp.error.unable.compile")
                                      + msg);
        }

        String classFile = containerL.getOutputDir() + File.separatorChar;

        String pkgName = pageInfo.getMangler().getPackageName();
        containerL.message("jsp.message.package_name_is",
                          new Object[] { (pkgName==null)?
                                          "[default package]":pkgName },
			   Log.DEBUG);

	String className = pageInfo.getMangler().getClassName();
        //pageInfo.setServletClassName(className);
        containerL.message("jsp.message.class_name_is",
                          new Object[] { className },
                          Log.DEBUG);

	String classFileName = pageInfo.getMangler().getClassFileName();
        containerL.message("jsp.message.class_file_name_is",
                          new Object[] { classFileName },
                          Log.DEBUG);



        if (pkgName != null && !pkgName.equals(""))
            classFile = classFile + pkgName.replace('.', File.separatorChar) + 
                File.separatorChar;
        classFile = classFile + className + ".class";

        if (!classFile.equals(classFileName)) {
            File classFileObject = new File(classFile);
            File myClassFileObject = new File(classFileName);
            if (myClassFileObject.exists())
                myClassFileObject.delete();
            if (classFileObject.renameTo(myClassFileObject) == false)
                throw new JasperException(containerL.getString("jsp.error.unable.rename",
                                                              new Object[] { 
                                                                  classFileObject, 
                                                                  myClassFileObject
                                                              }));
        }

        return true;
    }


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
            pageInfo.getServletJavaFileName()
        };

        StringBuffer b = new StringBuffer();
        for(int i = 0; i < argv.length; i++) {
            b.append(argv[i]);
            b.append(" ");
        }

        containerL.message("jsp.message.compiling_with",
                          new Object[] { b.toString() },
                          Log.DEBUG);
	return b.toString();
    }


}


