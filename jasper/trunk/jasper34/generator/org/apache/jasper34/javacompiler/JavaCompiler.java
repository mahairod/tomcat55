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

package org.apache.jasper34.javacompiler;

import java.io.*;

// Temp ( ? )
import org.apache.jasper34.core.*;

/**
 * If you want to plugin your own Java compiler, you probably want to
 * write a class that implements this interface. 
 *
 * @author Anil K. Vijendran
 * @author Sam Ruby
 * @author Costin Manolache
 */
public abstract class JavaCompiler {
    protected String encoding;
    protected String classpath;
    protected String compilerPath = "jikes";
    protected String outdir;
    protected OutputStream out;
    protected boolean classDebugInfo=false;

    /**
     * Specify where the compiler can be found
     */ 
    public void setCompilerPath(String compilerPath) {
	this.compilerPath = compilerPath;
    }


    /**
     * Set the encoding (character set) of the source
     */ 
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Set the class path for the compiler
     */ 
    public void setClasspath(String classpath) {
      this.classpath = classpath;
    }

    /**
     * Set the output directory
     */ 
    public void setOutputDir(String outdir) {
      this.outdir = outdir;
    }

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    public void setMsgOutput(OutputStream out) {
      this.out = out;
    }

    /**
     * Set if you want debugging information in the class file 
     */ 
    public void setClassDebugInfo(boolean classDebugInfo) {
	this.classDebugInfo = classDebugInfo;
    }

    // -------------------- Compile method --------------------
    /**
     * Execute the compiler
     * @param source - file name of the source to be compiled
     */ 
    public abstract boolean compile(String source);

    // -------------------- Utils --------------------

    public static Class getCompilerPluginClass( String s ) {
	try {
	    Class c=Class.forName( s );
	    return c;
	} catch( Exception ex ) {
	    return null;
	}
    }
    public static JavaCompiler createJavaCompiler(ContainerLiaison containerL,
						  String jspCompilerPluginS )
    {
	Class c=getCompilerPluginClass( jspCompilerPluginS );
	if( c==null ) return new SunJavaCompiler();
	return createJavaCompiler( containerL, c );
    }
	
    /** tool for customizing javac.
     */
    public static JavaCompiler createJavaCompiler(ContainerLiaison containerL,
						  Class jspCompilerPlugin )
	//	throws JasperException
    {
        JavaCompiler javac;

	if (jspCompilerPlugin != null) {
            try {
                javac = (JavaCompiler) jspCompilerPlugin.newInstance();
            } catch (Exception ex) {
		// containerL.message("jsp.warning.compiler.class.cantcreate",
		// 	         new Object[] { jspCompilerPlugin, ex }, 
		// 		  Log.FATAL);
                javac = new SunJavaCompiler();
	    }
	} else {
            javac = new SunJavaCompiler();
	}

	return javac;
    }


    public static JavaCompiler getDefaultCompiler() {
	return new SunJavaCompiler();
    }

}

