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

import java.io.File;
import java.io.FileNotFoundException;

import javax.servlet.ServletContext;

import org.apache.jasper.JspEngineContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

/**
 * JspCompiler is an implementation of Compiler with a funky code
 * mangling and code generation scheme! 
 *
 * @author Anil K. Vijendran
 */
public class JspCompiler extends Compiler implements Mangler {
    
    String pkgName, className, javaFileName, classFileName;

    File jsp;
    String outputDir;

    ClassFileData cfd;
    boolean outDated;


    public JspCompiler(JspEngineContext ctxt) throws JasperException {
        super(ctxt);
        
        this.jsp = new File(ctxt.getJspFile());
        this.outputDir = ctxt.getOutputDir();
        this.outDated = false;
        setMangler(this);
        computePackageName();
        computeClassFileName();
        computeClassFileData();
        computeJavaFileName();
    }

    public final String getPackageName() {
        return pkgName;
    }
    
    public final String getClassName() {
        return className;
    }

    public final String getJavaFileName() {
        return javaFileName;
    }
    
    public final String getClassFileName() {
        return classFileName;
    }

    
    /**
     * Return true if the .class file is outdated w.r.t
     * the JSP file. 
     *
     * Can (meant to) be overridden by subclasses of JspCompiler. 
     */
    protected boolean isOutDated() {
        return outDated;
    }

    public static String [] keywords = { 
        "abstract", "boolean", "break", "byte",
        "case", "catch", "char", "class",
        "const", "continue", "default", "do",
        "double", "else", "extends", "final",
        "finally", "float", "for", "goto",
        "if", "implements", "import",
        "instanceof", "int", "interface",
        "long", "native", "new", "package",
        "private", "protected", "public",
        "return", "short", "static", "super",
        "switch", "synchronized", "this",
        "throw", "throws", "transient", 
        "try", "void", "volatile", "while" 
    };

    void computePackageName() {
	String pathName = jsp.getPath();
	StringBuffer modifiedpkgName = new StringBuffer ();
        int indexOfSepChar = pathName.lastIndexOf(File.separatorChar);
        
	if (indexOfSepChar == -1 || indexOfSepChar == 0)
	    pkgName = null;
	else {
	    for (int i = 0; i < keywords.length; i++) {
		char fs = File.separatorChar;
		int index1 = pathName.indexOf(fs + keywords[i]);
		int index2 = pathName.indexOf(keywords[i]);
		if (index1 == -1 && index2 == -1) continue;
		int index = (index2 == -1) ? index1 : index2;
		while (index != -1) {
		    String tmpathName = pathName.substring (0,index+1) + '%';
		    pathName = tmpathName + pathName.substring (index+2);
		    index = pathName.indexOf(fs + keywords[i]);
		}
	    }
	    
	    // XXX fix for paths containing '.'.
	    // Need to be more elegant here.
            pathName = pathName.replace('.','_');
	    
	    pkgName = pathName.substring(0, pathName.lastIndexOf(
	    		File.separatorChar)).replace(File.separatorChar, '.');
	    for (int i=0; i<pkgName.length(); i++) 
		if (Character.isLetter(pkgName.charAt(i)) == true ||
		    pkgName.charAt(i) == '.') {
		    modifiedpkgName.append(pkgName.substring(i,i+1));
		}
		else
		    modifiedpkgName.append(mangleChar(pkgName.charAt(i)));

	    if (modifiedpkgName.charAt(0) == '.') {
                String modifiedpkgNameString = modifiedpkgName.toString();
                pkgName = modifiedpkgNameString.substring(1, 
                                                         modifiedpkgName.length ());
            }
	    else 
	        pkgName = modifiedpkgName.toString();
	}

    }

    public final void computeJavaFileName() {
	javaFileName = className + ".java";
	if (outputDir != null && !outputDir.equals(""))
	    javaFileName = outputDir + File.separatorChar + javaFileName;
    }

    void computeClassFileName() {
        String prefix = getPrefix(jsp.getPath());
        classFileName = prefix + getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
    }
					 
    private final String getInitialClassName() {
        String prefix = getPrefix(jsp.getPath());
        
        return prefix + getBaseClassName() + Constants.JSP_TOKEN + "0";
    }
    
    private final String getBaseClassName() {
	String className;
        
        if (jsp.getName().endsWith(".jsp"))
            className = jsp.getName().substring(0, jsp.getName().length() - 4);
        else
            className = jsp.getName();
            
	
	// Fix for invalid characters. If you think of more add to the list.
	StringBuffer modifiedClassName = new StringBuffer();
	for (int i = 0; i < className.length(); i++) {
	    if (Character.isLetterOrDigit(className.charAt(i)) == true)
		modifiedClassName.append(className.substring(i,i+1));
	    else
		modifiedClassName.append(mangleChar(className.charAt(i)));
	}
	
	return modifiedClassName.toString();
    }

    private final String getPrefix(String pathName) {
	if (pathName != null) {
	    StringBuffer modifiedName = new StringBuffer();
	    for (int i = 0; i < pathName.length(); i++) {
		if (Character.isLetter(pathName.charAt(i)) == true)
		    modifiedName.append(pathName.substring(i,i+1));
		else
		    modifiedName.append(mangleChar(pathName.charAt(i)));
 	    }
	    return modifiedName.toString();
	}
	else 
            return "";
    }

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


    private final void computeClassFileData()
	throws JasperException
    {
        ServletContext ctx = ctxt.getServletContext();
        
	File jspReal = null;

        if (ctx != null) 
            jspReal = new File(ctx.getRealPath(jsp.getPath()));
        else
            jspReal = jsp;

        File classFile = new File(classFileName);
        
        if (!classFile.exists()) {
            className = getInitialClassName();
            cfd = new ClassFileData(true, classFileName, className);
            outDated = true;
        } else  {
            outDated = classFile.lastModified() < jspReal.lastModified();
	    if (outDated) {
                String classNameFromFile = ClassName.getClassName(classFileName);
		cfd = new ClassFileData(outDated, classFileName, className);
                cfd.incrementNumber();
                
                String cn = cfd.getClassName();
                int lastDot = cn.lastIndexOf('.');
                if (lastDot != -1)
                    className = cn.substring(lastDot+1,
                                             classNameFromFile.length());
                else
                    className = cn;
	    } else {
		// cn ( Class Name ) is extracted from class data.
		// If the file is not out-dated, it is not needed, and will
		// be computed only if someone ask for it.
		cfd = new ClassFileData(outDated, classFileName, null);
	    }
        }
    }
}

class ClassFileData {
    boolean outDated;
    String className;
    String classFile;
    String baseClassName;
    int number;

    /** 
     * Lazy - find the class name ( by reading the .class file and extracting the
     * information ) only if some method need this info.
     *
     *  In "normal" usage, this method is not called - if the .class is not outdated,
     *  nobody needs the real class name or other info.
     *  If this will change - this method needs to be revisited.
     */
    private void findClassName() {
        try {
            className = ClassName.getClassName(classFile);
        } catch( JasperException ex) {
            // ops, getClassName should throw something
            ex.printStackTrace();
        }
        baseClassName = className.substring(0, className.lastIndexOf(Constants.JSP_TOKEN));
        this.number
            = Integer.valueOf(className.substring(className.lastIndexOf(Constants.JSP_TOKEN)+
                                                  Constants.JSP_TOKEN.length(), 
                                                  className.length())).intValue();
    }
	
    public boolean isOutDated() {
        return outDated;
    }
	
    public String getClassName() {
        if(className==null)
            findClassName();
        return className;
    }

    public String getClassNameSansNumber() {
        if(className==null)
            findClassName();
        return baseClassName;
    }
	
    public String getClassFileName() {
        return classFile;
    }
	
    public void incrementNumber() {
        if(className == null)
            findClassName();
        number++;
        className = baseClassName + Constants.JSP_TOKEN + number;
    }

    public int getNumber() {
        if(className==null)
            findClassName();
        return number;
    }

    public ClassFileData(boolean outDated, String cf, String cn) {
        this.outDated = outDated;
        this.className = cn;
        this.classFile = cf;
        if (cn != null) {
            this.baseClassName = cn.substring(0, cn.lastIndexOf(Constants.JSP_TOKEN));
            this.number = Integer.valueOf(cn.substring(cn.lastIndexOf(Constants.JSP_TOKEN)+
                                                       Constants.JSP_TOKEN.length(), 
                                                       cn.length())).intValue();
        } else {
            baseClassName = null;
            number = -1;
        }
    }
}
