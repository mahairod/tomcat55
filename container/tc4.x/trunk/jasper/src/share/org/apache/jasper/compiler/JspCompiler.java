/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * mangling and code generation scheme.
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


    /**
     * Convert the final path component to a valid base class name, maintaining
     * uniqueness as required.
     */
    private final String getBaseClassName() {

        int iSep = jsp.lastIndexOf('/') + 1;
        int iEnd = jsp.length();
        StringBuffer modifiedClassName = new StringBuffer(jsp.length() - iSep);
	if (!Character.isJavaIdentifierStart(jsp.charAt(iSep))) {
	    // If the first char is not a legal Java letter or digit,
	    // prepend a '$'.
	    modifiedClassName.append('$');
	}
        for (int i = iSep; i < iEnd; i++) {
            char ch = jsp.charAt(i);
            if (Character.isLetterOrDigit(ch))
                modifiedClassName.append(ch);
            else if (ch == '.')
                modifiedClassName.append('$');
            else
                modifiedClassName.append(mangleChar(ch));
        }
        return (modifiedClassName.toString());

    }


    /**
     * Mangle the specified character to create a legal Java class name.
     */
    private static final String mangleChar(char ch) {

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

