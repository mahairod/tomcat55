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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

import org.apache.jasper.logging.Logger;

/**
 * JspCompiler is an implementation of Compiler with a funky code
 * mangling and code generation scheme!
 *
 * The reason that it is both a sub-class of compiler and an implementation
 * of mangler is because the isOutDated method that is overridden and the
 * name mangulation both depend on the actual existance of other class and
 * java files.  I.e. the value of a mangled name is a function of both the
 * name to be mangled and also of the state of the scratchdir.
 *
 * @author Anil K. Vijendran
 */
public class JspCompiler extends Compiler implements Mangler {
    
    String javaFileName, classFileName;
    String realClassName;

    String jsp;
    String outputDir;

    //    ClassFileData cfd;
    boolean outDated;

    Logger.Helper loghelper = new Logger.Helper("JASPER_LOG", "JspCompiler");
    
    public JspCompiler(JspCompilationContext ctxt) throws JasperException {
        super(ctxt);
        
        this.jsp = ctxt.getJspFile();
        this.outputDir = ctxt.getOutputDir();
        this.outDated = false;
        setMangler(this);
    }

    public final String getClassName() {
	if( realClassName == null )
	    realClassName = getBaseClassName();
        return realClassName;
    }

    public final String getJavaFileName() {
        if( javaFileName!=null ) return javaFileName;
	javaFileName = getClassName() + ".java";
 	if (outputDir != null && !outputDir.equals(""))
 	    javaFileName = outputDir + File.separatorChar + javaFileName;
	return javaFileName;
    }
    
    public final String getClassFileName() {
        if( classFileName!=null) return classFileName;

        classFileName = getClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
	return classFileName;
    }

    private final String getBaseClassName() {
	String className;
        
        if (jsp.endsWith(".jsp"))
            className = jsp.substring(0, jsp.length() - 4);
        else
            className = jsp;
            
	
	// Fix for invalid characters. If you think of more add to the list.
	StringBuffer modifiedClassName = new StringBuffer();
	for (int i = 0; i < className.length(); i++) {
	    if (Character.isLetterOrDigit(className.charAt(i)) == true)
		modifiedClassName.append(className.substring(i,i+1));
	    else
		modifiedClassName.append(mangleChar(className.charAt(i)));
	}
	modifiedClassName.append("_jsp");
	return modifiedClassName.toString();
    }

    private static final String mangleChar(char ch) {

        if(ch == File.separatorChar) {
	    ch = '/';
	}	
	String s = Integer.toHexString(ch);
	int nzeros = 5 - s.length();
	char[] result = new char[6];
	result[0] = '_';
	for (int i = 1; i <= nzeros; i++)
	    result[i] = '0';
	for (int i = nzeros+1, j = 0; i < 6; i++, j++)
	    result[i] = s.charAt(j);
	return new String(result);
    }

    /**
     * Determines whether the current JSP class is older than the JSP file
     * from whence it came
     */
    public boolean isOutDated() {
        long jspRealLastModified = 0;

        try {
            URL jspUrl = ctxt.getResource(jsp);
            if (jspUrl == null)
                return true;
            jspRealLastModified = jspUrl.openConnection().getLastModified();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        File classFile = new File(getClassFileName());
        if (classFile.exists()) {
            outDated = classFile.lastModified() < jspRealLastModified;
        } else {
            outDated = true;
        }

        return outDated;
    }
}

