/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.jk.ant.compilers;

import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.jk.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.*;

import java.util.*;
import java.io.*;

/* Modeled after javac
 */

/**
 * s/javac/C compiler/
 *
 * The interface that all compiler adapters must adher to.  
 *
 * <p>A compiler adapter is an adapter that interprets the javac's
 * parameters in preperation to be passed off to the compier this
 * adapter represents.  As all the necessary values are stored in the
 * Javac task itself, the only thing all adapters need is the javac
 * task, the execute command and a parameterless constructor (for
 * reflection).</p>
 *
 * @author Jay Dickon Glanville <a href="mailto:jayglanville@home.com">jayglanville@home.com</a>
 * @author Costin Manolache
 */
public abstract class CompilerAdapter extends SoTask {
    SoTask so;

    public CompilerAdapter() {
	so=this;
    };

    public void setSoTask(SoTask so ) {
	this.so=so;
	so.duplicateTo( this );
    }

    public GlobPatternMapper getOMapper() {
        return null;
    }

    public void execute() throws BuildException {
	super.findCompileList();
	compile( compileList );
    }

    public void compile(Vector compileList ) throws BuildException {
	so.duplicateTo(this);
	Enumeration en=compileList.elements();
	while( en.hasMoreElements() ) {
	    Source source=(Source)en.nextElement();
	    compileSingleFile(source);
	}
    }
    
    /** Compile single file
     */
    public void compileSingleFile(Source sourceObj) throws BuildException {
    }


    protected void displayError( int result, String source, Commandline cmd )
	throws BuildException
    {
	log("Compile failed " + result + " " +  source );
	log("Command:" + cmd.toString());
	log("Output:" );
	if( outputstream!=null ) 
	    log( outputstream.toString());
	log("StdErr:" );
	if( errorstream!=null ) 
	    log( errorstream.toString());
	
	throw new BuildException("Compile failed " + source);
    }

    protected void addIncludes(Commandline cmd) {
	String [] includeList = ( includes==null ) ?
	    new String[] {} : includes.getIncludePatterns(project); 
	for( int i=0; i<includeList.length; i++ ) {
	    cmd.createArgument().setValue("-I" + includeList[i] );
	}
    }

    /** Common cc parameters
     */
    protected void addExtraFlags(Commandline cmd )  {
	String extra_cflags=project.getProperty("build.native.extra_cflags");
	String localCflags=cflags;
	if( localCflags==null ) {
	    localCflags=extra_cflags;
	} else {
	    if( extra_cflags!=null ) {
		localCflags+=" " + extra_cflags;
	    }
 	}
	if( localCflags != null )
	    cmd.createArgument().setLine( localCflags );
    }

    protected void addDefines( Commandline cmd ) {
	if( defines.size() > 0 ) {
	    Enumeration defs=defines.elements();
	    while( defs.hasMoreElements() ) {
		Def d=(Def)defs.nextElement();
		String name=d.getName();
		String val=d.getValue();
		if( name==null ) continue;
		String arg="-D" + name;
		if( val!=null )
		    arg+= "=" + val;
		cmd.createArgument().setValue( arg );
		if( debug > 0 ) project.log(arg);
            }
        }
    }

    protected void addDebug(Commandline cmd) {
    }

    protected void addOptimize( Commandline cmd ) {
    }

    protected void addProfile( Commandline cmd ) {
    }
}
