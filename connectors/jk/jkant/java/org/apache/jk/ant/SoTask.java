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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
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
 */

package org.apache.jk.ant;

import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.*;

import java.io.*;
import java.util.*;

/** Global properties

    Same idea as in javac, some user .properties will set the local preferences,
    including machine-specific. If one is not specified we'll guess it. The
    build file will be clean.

    TODO: can we get configure to generate such a file ? 

    build.native.cc=gcc
    # Path to libtool ( used as a backup )
    build.native.libtool=
    # Platform-specific flags for compilation.
    build.native.extra_cflags=


*/


/**
 * Task to generate a .so file, similar with ( or using ) libtool.
 * I hate libtool, so long term I would like to replace most of it
 * with decent java code. Short term it'll just use libtool and
 * hide some of the ugliness.
 * 
 * arguments:
 * <ul>
 * <li>source
 * </ul>
 *
 * <p>
 *
 * @author Costin Manolache
 */
public class SoTask extends Task {
    String apxs;
    // or FileSet ?
    FileSet src;
    Path includes;
    Path libs;
    String module;
    String soFile;
    String cflags;
    File buildDir;
    int debug;
    
    public SoTask() {};

    public void setSoFile(String s ) {
	soFile=s;
    }

    public void setDebug(int i) {
	debug=i;
    }
    
    public void setTarget(String s ) {
	soFile=s;
    }

    /** Directory where intermediary objects will be
     *  generated
     */
    public void setBuildDir( File s ) {
	buildDir=s;
    }

    public void setCflags(String s ) {
	cflags=s;
    }
    
    /** Directory where the .so file will be generated
     */
    public void setSoDir( String s ) {
	
    }

    public void addJniConfig( JniConfig jniCfg ) {

    }

    public void addApacheConfig( ApacheConfig apacheCfg ) {

    }
    
    
    /**
     * Source files ( .c )
     *
     * @return a nexted src element.
     */
    public void addSrc(FileSet fl) {
	src=fl;
    }

    /**
     * Include files
     */
    public Path createIncludes() {
	includes=new Path(project);
	return includes;
    }

    /**
     * Libraries ( .a, .so or .dll ) files to link to.
     */
    public Path createLibs() {
        libs=new Path(project);
	return libs;
    }
    
    
    /**
     * The name of the target file.
     * (XXX including extension - this should be automatically added )
     */
    public void setModule(String modName) {
        this.module = module;
    }

    // XXX Add specific code for Netware, Windows and platforms where libtool
    // is problematic

    // XXX Add specific code for Linux and platforms where things are
    // clean, libtool should be just a fallback.
    
    public void execute() throws BuildException {
	if( soFile==null )
	    throw new BuildException("No target ( .so file )");
	if (src == null) 
            throw new BuildException("No source files");

	DirectoryScanner ds=src.getDirectoryScanner( project );
        String [] list = ds.getIncludedFiles(); 
	if (list.length == 0) 
            throw new BuildException("No source files");

	Vector compileList=new Vector();

        for (int i = 0; i < list.length; i++) {
	    File srcFile = (File)project.resolveFile(list[i]);
            if (!srcFile.exists()) {
                throw new BuildException("Source \"" + srcFile.getPath() +
                                         "\" does not exist!", location);
            }

	    // Check the dependency

	    compileList.addElement( srcFile );
	}

        String [] includeList = ( includes==null ) ?
	    new String[] {} : includes.list(); 

	// XXX makedepend-type dependencies - how ??
	// We could generate a dummy Makefile and parse the content...
	Enumeration en=compileList.elements();
	while( en.hasMoreElements() ) {
	    File f=(File)en.nextElement();
	    executeLibtoolCompile(f.toString(), includeList );
	}
	

    }

    /** Generate the .so file using 'standard' gcc flags. This assume
     *  a 'current' gcc on a 'normal' platform. 
     */
    public void executeGcc() throws BuildException {
    }

    /** Generate the .so file using libtool.
     *  XXX check version, etc.
     */
    public void executeLibtoolCompile(String source, String includeList[]) throws BuildException {
	Commandline cmd = new Commandline();

	String libtool=project.getProperty("build.native.libtool");
	if(libtool==null) libtool="libtool";
	String cc=project.getProperty("build.native.cc");
	if(cc==null) cc="gcc";
	String extra_cflags=project.getProperty("build.native.extra_cflags");
	String localCflags=cflags;
	if( localCflags==null ) {
	    localCflags=extra_cflags;
	} else {
	    if( extra_cflags!=null ) {
		localCflags+=" " + extra_cflags;
	    }
 	}

	cmd.setExecutable( libtool );
	
	cmd.createArgument().setValue("--mode=compile");

	cmd.createArgument().setValue( cc );

	for( int i=0; i<includeList.length; i++ ) {
	    cmd.createArgument().setValue("-I");
	    cmd.createArgument().setValue(includeList[i] );
	}


	cmd.createArgument().setValue( "-c" );

	if( localCflags != null )
	    cmd.createArgument().setLine( localCflags );
	
	project.log( "Compiling " + source);

	cmd.createArgument().setValue( source );

	int result=execute( cmd );
	if( result!=0 ) {
	    log("Compile failed " + result + " " +  source );
	    log("Output:" );
	    if( outputstream!=null ) 
		log( outputstream.toString());
	    log("StdErr:" );
	    if( errorstream!=null ) 
		log( errorstream.toString());
	    
	    throw new BuildException("Compile failed " + source);
	}
	closeStreamHandler();
    }

    
    // ==================== Execution utils ==================== 

    ExecuteStreamHandler streamhandler = null;
    ByteArrayOutputStream outputstream = null;
    ByteArrayOutputStream errorstream = null;

    public int execute( Commandline cmd ) throws BuildException
    {
	createStreamHandler();
        Execute exe = new Execute(streamhandler, null);

        exe.setAntRun(project);

        if (buildDir == null) buildDir = project.getBaseDir();
        exe.setWorkingDirectory(buildDir);

        exe.setCommandline(cmd.getCommandline());
	int result=0;
	try {
            result=exe.execute();
        } catch (IOException e) {
            throw new BuildException(e, location);
        } 
	return result;
    }

    public void createStreamHandler()  throws BuildException {
	//	try {
	outputstream= new ByteArrayOutputStream();
	errorstream = new ByteArrayOutputStream();
	
	streamhandler =
	    new PumpStreamHandler(new PrintStream(outputstream),
				  new PrintStream(errorstream));
	//	}  catch (IOException e) {
	//	    throw new BuildException(e,location);
	//	}
    }

    public void closeStreamHandler() {
	try {
	    if (outputstream != null) 
		outputstream.close();
	    if (errorstream != null) 
		errorstream.close();
	    outputstream=null;
	    errorstream=null;
	} catch (IOException e) {}
    }
}

