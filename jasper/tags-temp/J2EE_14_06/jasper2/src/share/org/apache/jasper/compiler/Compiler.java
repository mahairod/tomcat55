/*
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
import java.net.URL;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.jsp.tagext.TagInfo;

import org.xml.sax.Attributes;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;

import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.Options;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.util.SystemLogHandler;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.jasper.servlet.JspServletWrapper;

/**
 * Main JSP compiler class. This class uses Ant for compiling.
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Remy Maucherat
 * @author Mark Roth
 */
public class Compiler {
    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( Compiler.class );

    // ----------------------------------------------------------------- Static


    static {

        System.setErr(new SystemLogHandler(System.err));

    }

    // Some javac are not thread safe; use a lock to serialize compilation, 
    static Object javacLock = new Object();


    // ----------------------------------------------------- Instance Variables


    protected JspCompilationContext ctxt;

    private ErrorDispatcher errDispatcher;
    private PageInfo pageInfo;
    private JspServletWrapper jsw;
    private TagFileProcessor tfp;

    protected Project project=null;

    protected Options options;

    protected Node.Nodes pageNodes;
    // ------------------------------------------------------------ Constructor


    public Compiler(JspCompilationContext ctxt) {
        this(ctxt, null);
    }


    public Compiler(JspCompilationContext ctxt, JspServletWrapper jsw) {
        this.jsw = jsw;
        this.ctxt = ctxt;
	this.errDispatcher = new ErrorDispatcher();
        this.options = ctxt.getOptions();
    }

    // Lazy eval - if we don't need to compile we probably don't need the project
    private Project getProject() {
        if( project!=null ) return project;
        // Initializing project
        project = new Project();
        // XXX We should use a specialized logger to redirect to jasperlog
        //        DefaultLogger bl=new JasperAntLogger();
        DefaultLogger bl=new DefaultLogger();
        bl.setOutputPrintStream(System.err);
        bl.setErrorPrintStream(System.err);

        if( Constants.jasperLog.getVerbosityLevel() >= Logger.DEBUG ) {
            bl.setMessageOutputLevel( Project.MSG_VERBOSE );
        } else {
            bl.setMessageOutputLevel( Project.MSG_INFO );
        }
        if( log.isTraceEnabled() ) {
            bl.setMessageOutputLevel( Project.MSG_VERBOSE );
        }
        project.addBuildListener( bl );
        
        if( options.getCompiler() != null ) {
            if( log.isDebugEnabled() )
                log.debug("Compiler " + options.getCompiler() );
            project.setProperty("build.compiler", options.getCompiler() );
        }
        project.init();
//         Vector v=project.getBuildListeners();
//         if( v.size() > 0 ) {
//             BuildListener bl=(BuildListener)v.elementAt(0);
//             System.out.println("XXX " + bl );
//             ((DefaultLogger)bl).setMessageOutputLevel(Project.MSG_VERBOSE);
//         }
        return project;
    }

    static class JasperAntLogger extends DefaultLogger {
        private static org.apache.commons.logging.Log log=
            org.apache.commons.logging.LogFactory.getLog( Compiler.class );
        protected void printMessage(final String message,
                                    final PrintStream stream,
                                    final int priority) {
            log.info( message );
        }
    }

    // --------------------------------------------------------- Public Methods


    /** 
     * Compile the jsp file from the current engine context
     */
    public void generateJava()
        throws Exception
    {
        long t1=System.currentTimeMillis();
	// Setup page info area
	pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader(),
						   errDispatcher));
	JspConfig jspConfig = options.getJspConfig();
	JspConfig.JspProperty jspProperty =
			jspConfig.findJspProperty(ctxt.getJspFile());
	if (jspProperty != null) {
	    // If the current uri is matched by a pattern specified in
	    // a jsp-property-group in web.xml, initialize pageInfo with
	    // those properties.
	    if (jspProperty.isXml() != null) {
		pageInfo.setIsXmlSpecified(true);
	    }
	    if (jspProperty.isELIgnored() != null) {
		pageInfo.setELIgnoredSpecified(true);
	    }
	    pageInfo.setIsXml(JspUtil.booleanValue(jspProperty.isXml()));
	    pageInfo.setPageEncoding(jspProperty.getPageEncoding());
	    pageInfo.setELIgnored(JspUtil.booleanValue(jspProperty.isELIgnored()));
	    pageInfo.setScriptingInvalid(JspUtil.booleanValue(jspProperty.isScriptingInvalid()));
	    pageInfo.setIncludePrelude(jspProperty.getIncludePrelude());
	    pageInfo.setIncludeCoda(jspProperty.getIncludeCoda());
	}

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

        // Reset the temporary variable counter for the generator.
        JspUtil.resetTemporaryVariableName();

	// Parse the file
	ParserController parserCtl = new ParserController(ctxt, this);
	pageNodes = parserCtl.parse(ctxt.getJspFile());

	if (ctxt.isPrototypeMode()) {
	    // generate prototype .java file for the tag file
	    Generator.generate(writer, this, pageNodes);
            writer.close();
	    return;
	}

	// Validate and process attributes
	Validator.validate(this, pageNodes);

        long t2=System.currentTimeMillis();
	// Dump out the page (for debugging)
	// Dumper.dump(pageNodes);

	// Collect page info
	Collector.collect(this, pageNodes);

	// Compile (if necessary) and load the tag files referenced in
	// this compilation unit.
	tfp = new TagFileProcessor();
	tfp.loadTagFiles(this, pageNodes);

        long t3=System.currentTimeMillis();
        
	// Determine which custom tag needs to declare which scripting vars
	ScriptingVariabler.set(pageNodes, errDispatcher);

	// generate servlet .java file
	Generator.generate(writer, this, pageNodes);
        writer.close();

        long t4=System.currentTimeMillis();
        if( t4-t1 > 500 ) {
            log.debug("Generated "+ javaFileName + " total=" +
                      (t4-t1) + " generate=" + ( t4-t3 ) + " validate=" + ( t2-t1 ));
        }
        
        //JSR45 Support - note this needs to be checked by a JSR45 guru
	SmapUtil.generateSmap(ctxt, pageNodes, true);

	// If any proto type .java and .class files was generated,
	// the prototype .java may have been replaced by the current
	// compilation (if the tag file is self referencing), but the
	// .class file need to be removed, to make sure that javac would
	// generate .class again from the new .java file just generated.

	tfp.removeProtoTypeFiles(ctxt.getClassFileName());
    }

    /** 
     * Compile the jsp file from the current engine context
     */
    public void generateClass()
        throws FileNotFoundException, JasperException, Exception {

        long t1=System.currentTimeMillis();
	String javaEncoding = "UTF8"; 
        String javaFileName = ctxt.getServletJavaFileName();
        String classpath = ctxt.getClassPath(); 

        String sep = System.getProperty("path.separator");

        String errorReport = null;
        boolean success = true;

        // Start capturing the System.err output for this thread
        SystemLogHandler.setThread();

        // Initializing javac task
        getProject();
        Javac javac = (Javac) project.createTask("javac");

        // Initializing classpath
        Path path = new Path(project);
        path.setPath(System.getProperty("java.class.path"));
        StringTokenizer tokenizer = new StringTokenizer(classpath, sep);
        while (tokenizer.hasMoreElements()) {
            String pathElement = tokenizer.nextToken();
            File repository = new File(pathElement);
            path.setLocation(repository);
        }

        if( log.isDebugEnabled() )
            log.debug( "Using classpath: " + System.getProperty("java.class.path") + sep
                       + classpath);
        
        // Initializing sourcepath
        Path srcPath = new Path(project);
        srcPath.setLocation(options.getScratchDir());

        // Configure the compiler object
        javac.setEncoding(javaEncoding);
        javac.setClasspath(path);
        javac.setDebug(ctxt.getOptions().getClassDebugInfo());
        javac.setSrcdir(srcPath);
        javac.setOptimize(! ctxt.getOptions().getClassDebugInfo() );

        // Set the Java compiler to use
        if (options.getCompiler() != null) {
            javac.setCompiler(options.getCompiler());
        }

        // Build includes path
        PatternSet.NameEntry includes = javac.createInclude();
        includes.setName(ctxt.getJspPath());

        try {
            synchronized(javacLock) {
                javac.execute();
            }
        } catch (BuildException e) {
            log.error( "Javac execption ", e);
            success = false;
        }

        // Stop capturing the System.err output for this thread
        errorReport = SystemLogHandler.unsetThread();
        
        if (!ctxt.keepGenerated()) {
            File javaFile = new File(javaFileName);
            javaFile.delete();
        }

        if (!success) {
            log.error( "Error compiling file: " + javaFileName + " " + errorReport);
            if(errorReport!=null ) 
                errDispatcher.javacError(errorReport, javaFileName, pageNodes);
        }

        long t2=System.currentTimeMillis();
        if( t2-t1 > 500 ) {
            log.debug( "Compiled " + javaFileName + " " + (t2-t1));
        }

	if (ctxt.isPrototypeMode()) {
	    return;
	}

        //JSR45 Support - note this needs to be checked by a JSR45 guru
	SmapUtil.installSmap(ctxt);
    }

    /** 
     * Compile the jsp file from the current engine context
     */
    public void compile()
        throws FileNotFoundException, JasperException, Exception
    {
        generateJava();
        generateClass();
	if (tfp != null) {
	    tfp.removeProtoTypeFiles(null);
	}
    }

    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation. 
     */
    public boolean isOutDated() {
        return isOutDated( true );
    }

    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation.
     * @param checkClass Verify the class file if true, only the .java file if false.
     */
    public boolean isOutDated(boolean checkClass) {

        String jsp = ctxt.getJspFile();

        long jspRealLastModified = 0;
        try {
            URL jspUrl = ctxt.getResource(jsp);
            if (jspUrl == null) {
                ctxt.incrementRemoved();
                return false;
            }
            jspRealLastModified = jspUrl.openConnection().getLastModified();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        long targetLastModified;
        File targetFile;
        
        if( checkClass ) {
            targetFile = new File(ctxt.getClassFileName());
        } else {
            targetFile = new File(ctxt.getServletJavaFileName());
        }
        
        if (!targetFile.exists()) {
            return true;
        }

        targetLastModified = targetFile.lastModified();
        if (targetLastModified < jspRealLastModified) {
            if( log.isDebugEnabled() )
                log.debug("Compiler: outdated: " + targetFile + " " + targetLastModified );
            return true;
        }

        // determine if source dependent files (e.g. includes using include
	// directives) have been changed.
        if( jsw==null ) {
            return false;
        }

        List depends = jsw.getDependants();
        if (depends == null) {
            return false;
        }

        Iterator it = depends.iterator();
        while (it.hasNext()) {
            String include = (String)it.next();
            try {
                URL includeUrl = ctxt.getResource(include);
                if (includeUrl == null) {
                    //System.out.println("Compiler: outdated, no includeUri " + include );
                    return true;
                }
                if (includeUrl.openConnection().getLastModified() >
                    targetLastModified) {
                    //System.out.println("Compiler: outdated, include old " + include );
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
        return false;

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
     * Remove generated files
     */
    public void removeGeneratedFiles() {
        try {
            String classFileName = ctxt.getClassFileName();
            if (classFileName != null) {
                File classFile = new File(classFileName);
                if( log.isDebugEnabled() )
                    log.debug( "Deleting " + classFile );
                classFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
        try {
            String javaFileName = ctxt.getServletJavaFileName();
            if (javaFileName != null) {
                File javaFile = new File(javaFileName);
                if( log.isDebugEnabled() )
                    log.debug( "Deleting " + javaFile );
                javaFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
    }

    public void removeGeneratedClassFiles() {
        try {
            String classFileName = ctxt.getClassFileName();
            if (classFileName != null) {
                File classFile = new File(classFileName);
                if( log.isDebugEnabled() )
                    log.debug( "Deleting " + classFile );
                classFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
    }
}
