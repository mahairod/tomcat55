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
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.CommandLineContext;

/**
 * Overrides some methods so that we get the desired effects.
 *@author Danno Ferrin
 */
public class CommandLineCompiler extends Compiler implements Mangler {

    String javaFileName;
    String classFileName;
    String packageName;
    String pkgName;
    String className;
    File jsp;
    String outputDir;

    public CommandLineCompiler(CommandLineContext ctxt) {
        super(ctxt);

        jsp = new File(ctxt.getJspFile());
        outputDir =  ctxt.getOptions().getScratchDir().getAbsolutePath();
	packageName = ctxt.getServletPackageName();
	pkgName = packageName;
        setMangler(this);

        className = getBaseClassName();
        // yes this is kind of messed up ... but it works
        if (ctxt.isOutputInDirs()) {
            String tmpDir = outputDir
                   + File.separatorChar
                   + pkgName.replace('.', File.separatorChar);
            File f = new File(tmpDir);
            if (!f.exists()) {
                if (f.mkdirs()) {
                    outputDir = tmpDir;
                }
            } else {
                outputDir = tmpDir;
            }
        }
        computeClassFileName();
        computeJavaFileName();
    };


    /**
     * Always outDated.  (Of course we are, this is an explicit invocation
     * @return true
     */
    public boolean isOutDated() {
        return true;
    };


    public final void computeJavaFileName() {
	javaFileName = ctxt.getServletClassName() + ".java";
	if ("null.java".equals(javaFileName)) {
    	    javaFileName = getBaseClassName() + ".java";
    	};
	if (outputDir != null && !outputDir.equals(""))
	    javaFileName = outputDir + File.separatorChar + javaFileName;
    }

    void computeClassFileName() {
        classFileName = getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
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

    private final String getInitialClassName() {
        return getBaseClassName();
    }

    private final String getBaseClassName() {
	String className = ctxt.getServletClassName();

	if (className == null) {
            if (jsp.getName().endsWith(".jsp"))
                className = jsp.getName().substring(0, jsp.getName().length() - 4);
            else
                className = jsp.getName();

        }
	return mangleName(className);
    }
	
    private static final String mangleName(String name) {

	// since we don't mangle extensions like the servlet does,
	// we need to check for keywords as class names
	for (int i = 0; i < keywords.length; i++) {
	    if (name.equals(keywords[i])) {
		name += "%";
		break;
	    };
	};
	
	// Fix for invalid characters. If you think of more add to the list.
	StringBuffer modifiedName = new StringBuffer();
	if (Character.isJavaIdentifierStart(name.charAt(0)))
	    modifiedName.append(name.charAt(0));
	else
	    modifiedName.append(mangleChar(name.charAt(0)));
	for (int i = 1; i < name.length(); i++) {
	    if (Character.isJavaIdentifierPart(name.charAt(i)))
		modifiedName.append(name.charAt(i));
	    else
		modifiedName.append(mangleChar(name.charAt(i)));
	}
	
	return modifiedName.toString();
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
     * Make sure that the package name is a legal Java name
     *
     * @param name The input string, containing arbitary chars separated by
     *             '.'s, with possible leading, trailing, or double '.'s
     * @return legal Java package name.
     */
    public static String manglePackage(String name) {
        boolean first = true;

        StringBuffer b = new StringBuffer();
        StringTokenizer t = new StringTokenizer(name, ".");
        while (t.hasMoreTokens()) {
            String nt = t.nextToken();
            if (nt.length() > 0) {
                if (b.length() > 0)
                    b.append('.');
                b.append(mangleName(nt));
            }
        }
        return b.toString();
    }

    public final String getClassName() {
        return className;
    }

    public final String getPackageName() {
        return packageName;
    }

    public final String getJavaFileName() {
        return javaFileName;
    }

    public final String getClassFileName() {
        return classFileName;
    }


}
