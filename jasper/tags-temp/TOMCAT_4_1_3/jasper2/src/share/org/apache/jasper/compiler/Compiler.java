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
 */ 
package org.apache.jasper.compiler;

import java.util.*;
import java.io.*;
import javax.servlet.jsp.tagext.TagInfo;

import org.xml.sax.Attributes;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.Options;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.util.SystemLogHandler;

/**
 * Main JSP compiler class. This class uses Ant for compiling.
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Remy Maucherat
 */
public class Compiler {


    // ----------------------------------------------------------------- Static


    static {

        System.setErr(new SystemLogHandler(System.err));

    }


    // ----------------------------------------------------- Instance Variables


    protected Mangler mangler;
    protected JspCompilationContext ctxt;

    private ErrorDispatcher errDispatcher;
    private PageInfo pageInfo;

    protected Project project;

    protected Options options;


    // ------------------------------------------------------------ Constructor


    public Compiler(JspCompilationContext ctxt) {
        this.ctxt = ctxt;
	this.errDispatcher = new ErrorDispatcher();
        this.options = ctxt.getOptions();
        // Initializing project
        project = new Project();
        project.init();
    }


    // --------------------------------------------------------- Public Methods


    /** 
     * Compile the jsp file from the current engine context
     */
    public void compile()
        throws FileNotFoundException, JasperException, Exception {

	// Setup page info area
	pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader()));

        String javaFileName = ctxt.getServletJavaFileName();

        // Setup the ServletWriter
	// We try UTF8 by default. If it fails, we use the java encoding 
	// specified for JspServlet init parameter "javaEncoding".

	String javaEncoding = "UTF8"; 
	OutputStreamWriter osw = null; 
	try {
	    osw = new OutputStreamWriter(new FileOutputStream(javaFileName),
					 javaEncoding);
	} catch (UnsupportedEncodingException ex) {
	    // Try to get the java encoding from the "javaEncoding"
	    // init parameter for JspServlet.
	    javaEncoding = ctxt.getOptions().getJavaEncoding();
	    if (javaEncoding != null) {
		try {
		    osw = new OutputStreamWriter
                        (new FileOutputStream(javaFileName),javaEncoding);
		} catch (UnsupportedEncodingException ex2) {
		    // no luck :-(
		    errDispatcher.jspError("jsp.error.invalid.javaEncoding",
					   "UTF8", javaEncoding);
		}
	    } else {
		errDispatcher.jspError("jsp.error.needAlternateJavaEncoding",
				       "UTF8");
	    }
	}

	ServletWriter writer = new ServletWriter(new PrintWriter(osw));
        ctxt.setWriter(writer);

	// Parse the file
	ParserController parserCtl = new ParserController(ctxt, this);
	Node.Nodes pageNodes = parserCtl.parse(ctxt.getJspFile());

	// Validate and process attributes
	Validator.validate(this, pageNodes);

	// Dump out the page (for debugging)
	// Dumper.dump(pageNodes);

	// generate servlet .java file
	Generator.generate(writer, this, pageNodes);
        writer.close();

        String classpath = ctxt.getClassPath(); 

        String sep = System.getProperty("path.separator");

        String errorReport = null;
        boolean success = true;

        // Initializing javac task
        Javac javac = (Javac) project.createTask("javac");

        // Initializing classpath
        Path path = new Path(project);
        path.setPath(System.getProperty("java.class.path") + sep
                     + classpath);

        // Initializing sourcepath
        Path srcPath = new Path(project);
        srcPath.setPath(options.getScratchDir().getAbsolutePath());

        // Configure the compiler object
        javac.setEncoding(javaEncoding);
        javac.setClasspath(path);
        javac.setDebug(ctxt.getOptions().getClassDebugInfo());
        javac.setSrcdir(srcPath);

        // Build includes path
        String dirName = ctxt.getJspFile();
        int pos = dirName.lastIndexOf('/');
        if (pos > 0) {
            dirName = dirName.substring(0, pos + 1);
        } else {
            dirName = "";
        }
        String includePath = dirName + ctxt.getServletClassName() + ".java";
        if (includePath.startsWith("/")) {
            includePath = includePath.substring(1);
        }
        javac.setIncludes(includePath);

        // Start capturing the System.err output for this thread
        SystemLogHandler.setThread();

        try {
            javac.execute();
        } catch (BuildException e) {
            success = false;
        }

        // Stop capturing the System.err output for this thread
        errorReport = SystemLogHandler.unsetThread();

        if (!ctxt.keepGenerated()) {
            File javaFile = new File(javaFileName);
            javaFile.delete();
        }

        if (!success) {
            errDispatcher.javacError(errorReport, javaFileName, pageNodes);
        }

    }


    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation. 
     */
    public boolean isOutDated() {
	return true;
    }

    
    /**
     * Set java compiler info
     */
    public void setJavaCompiler(JavaCompiler javac) {
    }


    /**
     * Set Mangler which will be used as part of compile().
     */
    public void setMangler(Mangler mangler) {
        this.mangler = mangler;
        ctxt.setServletJavaFileName(mangler.getJavaFileName());
    }


    /**
     * Gets the error dispatcher.
     */
    public ErrorDispatcher getErrorDispatcher() {
	return errDispatcher;
    }


    /**
     * Gets the info about the page under compilation
     */
    public PageInfo getPageInfo() {
	return pageInfo;
    }


    public JspCompilationContext getCompilationContext() {
	return ctxt;
    }


    /**
     * Change the encoding for the reader if specified.
     */
    public String changeEncodingIfNecessary(JspReader tmpReader)
	        throws JasperException {

	// A lot of code replicated from Parser.java
	// Main aim is to "get-it-to-work".
	while (tmpReader.skipUntil("<%@") != null) {

	    tmpReader.skipSpaces();

	    // check if it is a page directive.
	    if (tmpReader.matches("page")) {

		tmpReader.advance(4);
		tmpReader.skipSpaces();
		
		try {
		    Attributes attrs = tmpReader.parseTagAttributes();
		    String ct = attrs.getValue("contentType");
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
    public void removeGeneratedFiles() {
        try {
            String classFileName = mangler.getClassFileName();
            if (classFileName != null) {
                File classFile = new File(classFileName);
                classFile.delete();
            }
        } catch (Exception e) {
            //Remove as much as possible, ignore possible exceptions
        }
        try {
            String javaFileName = mangler.getJavaFileName();
            if (javaFileName != null) {
                File javaFile = new File(javaFileName);
                javaFile.delete();
            }
        } catch (Exception e) {
            //Remove as much as possible, ignore possible exceptions
        }
    }


}
