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





package org.apache.jasper;

import java.io.*;
import java.util.StringTokenizer;

import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.Compiler;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
 * Holds data used on a per-page compilation context that would otherwise spill
 * over to other pages being compiled.  Things like the taglib classloaders
 * and directives.
 *
 * @author Danno Ferrin
 * @author Pierre Delisle
 * @author Remy Maucherat
 */
public class CommandLineContext implements JspCompilationContext {


    // ----------------------------------------------------- Instance Variables


    String classPath;
    JspReader reader;
    ServletWriter writer;
    URLClassLoader loader;
    boolean errPage;
    String jspFile;
    String servletClassName;
    String servletPackageName = Constants.JSP_PACKAGE_NAME;;
    String servletJavaFileName;
    String contentType;
    Options options;
    private String classFileName;
    private String jspPath;
    private String outputDir;

    String uriBase;
    File uriRoot;

    boolean outputInDirs;


    // ------------------------------------------------------------ Constructor


    public CommandLineContext(String newJspFile, String newUriBase,
                              String newUriRoot, boolean newErrPage,
                              Options newOptions)
        throws JasperException {

        uriBase = newUriBase;
        String tUriRoot = newUriRoot;
        jspFile = newJspFile;
        // hack fix for resolveRelativeURI
        errPage = newErrPage;
        options = newOptions;

        if (uriBase == null) {
            uriBase = "/";
        } else if (uriBase.charAt(0) != '/') {
            // strip the basde slash since it will be combined with the
            // uriBase to generate a file
            uriBase = "/" + uriBase;
        }

        if (uriBase.charAt(uriBase.length() - 1) != '/') {
            uriBase += '/';
        }

        if (tUriRoot == null) {
            uriRoot = new File("");
        } else {
            uriRoot = new File(tUriRoot);
            if (!uriRoot.exists() || !uriRoot.isDirectory()) {
               throw new JasperException(
                        Constants.getString("jsp.error.jspc.uriroot_not_dir"));
            }
        }

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Resolve relative path, and create output directories.
     */
    public void setupContext() {

        outputDir = options.getScratchDir().toString();

        if (isOutputInDirs()) {
            int indexOfSlash = getJspFile().lastIndexOf('/');
            String pathName = "";
            if (indexOfSlash != -1) {
                pathName = getJspFile().substring(0, indexOfSlash);
            }
            String tmpDir = outputDir + File.separatorChar + pathName;
            File f = new File(tmpDir);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

    }


    // ------------------------------------------ JspCompilationContext Methods


    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
        return classPath;
    }


    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }


    /**
     * Get the input reader for the JSP text. 
     */
    public JspReader getReader() {
        return reader;
    }


    /**
     * Where is the servlet being generated?
     */
    public ServletWriter getWriter() {
        return writer;
    }


    /**
     * What class loader to use for loading classes while compiling
     * this JSP?
     */
    public ClassLoader getClassLoader() {
        return loader;
    }


    /**
     * Are we processing something that has been declared as an
     * errorpage? 
     */
    public boolean isErrorPage() {
        return errPage;
    }


    /**
     * The scratch directory to generate code into.
     */
    public String getOutputDir() {
        return outputDir;
    }


    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file. 
     */
    public String getJspFile() {
        return jspFile;
    }


    /**
     * Path of the JSP relative to the work directory.
     */
    public String getJspPath() {
        if (jspPath != null) {
            return jspPath;
        }
        String dirName = getJspFile();
        int pos = dirName.lastIndexOf('/');
        if (pos > 0) {
            dirName = dirName.substring(0, pos + 1);
        } else {
            dirName = "";
        }
        jspPath = dirName + getServletClassName() + ".java";
        if (jspPath.startsWith("/")) {
            jspPath = jspPath.substring(1);
        }
        return jspPath;
    }


    /**
     * Just the class name (does not include package name) of the
     * generated class. 
     */
    public String getServletClassName() {
        if (servletClassName != null) {
            return servletClassName;
        }
        int iSep = jspFile.lastIndexOf('/') + 1;
        int iEnd = jspFile.length();
        StringBuffer modifiedClassName = 
            new StringBuffer(jspFile.length() - iSep);
	if (!Character.isJavaIdentifierStart(jspFile.charAt(iSep))) {
	    // If the first char is not a legal Java letter or digit,
	    // prepend a '$'.
	    modifiedClassName.append('$');
	}
        for (int i = iSep; i < iEnd; i++) {
            char ch = jspFile.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                modifiedClassName.append(ch);
            } else if (ch == '.') {
                modifiedClassName.append('$');
            } else {
                modifiedClassName.append(mangleChar(ch));
            }
        }
        servletClassName = modifiedClassName.toString();
        return servletClassName;
    }


    /**
     * The package name for the generated class.
     * The final package is assembled from the one specified in -p, and
     * the one derived from the path to jsp file.
     */
    public String getServletPackageName() {
        return servletPackageName;
    }


    /**
     * Full path name of the Java file into which the servlet is being
     * generated. 
     */
    public String getServletJavaFileName() {

        if (servletJavaFileName != null) {
            return servletJavaFileName;
        }

        String outputDir = getOutputDir();
        //servletJavaFileName = getServletClassName() + ".java";
        servletJavaFileName = "/" + getJspPath();
 	if (outputDir != null && !outputDir.equals("")) {
	    servletJavaFileName = outputDir + servletJavaFileName;
        }
	return servletJavaFileName;

    }


    public String getClassFileName() {

        if (classFileName != null) {
            return classFileName;
        }

        String outputDir = getOutputDir();
        classFileName = getServletClassName() + ".class";
        if (outputDir != null && !outputDir.equals("")) {
            classFileName = outputDir + File.separatorChar + classFileName;
        }
        return classFileName;

    }


    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated() {
        return options.getKeepGenerated();
    }


    /**
     * The content type of this JSP.
     *
     * Content type includes content type and encoding. 
     */
    public String getContentType() {
        return contentType;
    }


    /**
     * Get hold of the Options object for this context. 
     */
    public Options getOptions() {
        return options;
    }


    public void setClassLoader(URLClassLoader loader) {
	this.loader = loader;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public void setReader(JspReader reader) {
        this.reader = reader;
    }


    public void setWriter(ServletWriter writer) {
        this.writer = writer;
    }


    public void setServletClassName(String servletClassName) {
        this.servletClassName = servletClassName;
    }


    public void setServletPackageName(String servletPackageName) {
        this.servletPackageName = servletPackageName;
    }


    public void setServletJavaFileName(String servletJavaFileName) {
        this.servletJavaFileName = servletJavaFileName;
    }


    public void setErrorPage(boolean isErrPage) {
        errPage = isErrPage;
    }


    public void setOutputInDirs(boolean newValue) {
        outputInDirs = true;
    }


    public boolean isOutputInDirs() {
        return outputInDirs;
    }


    /**
     * Create a "Compiler" object based on some init param data. This
     * is not done yet. Right now we're just hardcoding the actual
     * compilers that are created. 
     */
    public Compiler createCompiler()
        throws JasperException {

        return new Compiler(this);

    }


    /** 
     * Get the full value of a URI relative to this compilations context
     * uses current file as the base.
     */
    public String resolveRelativeUri(String uri) {
        // sometimes we get uri's massaged from File(String), so check for
        // a root directory deperator char
        if (uri.startsWith("/") || uri.startsWith(File.separator)) {
            return uri;
        } else {
            return uriBase + uri;
        }
    }


    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     * @return a null if the resource cannot be found or represented 
     *         as an InputStream.
     */
    public java.io.InputStream getResourceAsStream(String res) {
        InputStream in;
        // fisrt try and get it from the URI
        try {
            in = new FileInputStream(getRealPath(res));
        } catch (IOException ioe) {
            in = null;
        }
        // next, try it as an absolute name
        if (in == null) try {
            in = new FileInputStream(res);
        } catch (IOException ioe) {
            in = null;
        }
        // that dind't work, last chance is to try the classloaders
        if (in == null) {
            in = loader.getResourceAsStream(res);
        }
        return in;
    }


    public URL getResource(String res) 
	throws MalformedURLException {
	return loader.getResource(res);
    }


    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path) {
        path = resolveRelativeUri(path);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File f = new File(uriRoot, path.replace('/', File.separatorChar));
        return f.getAbsolutePath();
    }


    public String[] getTldLocation(String uri) throws JasperException {
	String[] location = 
	    options.getTldLocationsCache().getLocation(uri);
	return location;
    }


    public String getClassName() {
        String outputDir = getOutputDir();
        String classFileName = getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
        return classFileName;
    }


    // -------------------------------------------------------- Private Methods


    private static final String [] keywords = {
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


    private final String getBaseClassName() {
        String className = getServletClassName();
        String prefix = null;
        if (className == null) {
            String jsp = getJspFile();
            int indexOfSlash = jsp.lastIndexOf('/');
            if (indexOfSlash != -1) {
                prefix = jsp.substring(0, indexOfSlash + 1);
                jsp = jsp.substring(indexOfSlash + 1);
            }
            if (jsp.endsWith(".jsp")) {
                className = jsp.substring(0, jsp.length() - 4);
            } else {
                className = jsp;
            }
        }
        if (prefix != null) {
            return prefix + mangleName(className);
        } else {
            return mangleName(className);
        }
    }


    private static final String mangleName(String name) {

        // since we don't mangle extensions like the servlet does,
        // we need to check for keywords as class names
        for (int i = 0; i < keywords.length; i++) {
            if (name.equals(keywords[i])) {
                name += "%";
                break;
            }
        }

        // Fix for invalid characters. If you think of more add to the list.
        StringBuffer modifiedName = new StringBuffer();
        if (Character.isJavaIdentifierStart(name.charAt(0))) {
            modifiedName.append(name.charAt(0));
        } else {
            modifiedName.append(mangleChar(name.charAt(0)));
        }
        for (int i = 1; i < name.length(); i++) {
            if (Character.isJavaIdentifierPart(name.charAt(i))) {
                modifiedName.append(name.charAt(i));
            } else {
                modifiedName.append(mangleChar(name.charAt(i)));
            }
        }

        return modifiedName.toString();

    }

    private static final String mangleChar(char ch) {

        if (ch == File.separatorChar) {
            ch = '/';
        }
        String s = Integer.toHexString(ch);
        int nzeros = 5 - s.length();
        char[] result = new char[6];
        result[0] = '_';
        for (int i = 1; i <= nzeros; i++) {
            result[i] = '0';
        }
        for (int i = nzeros+1, j = 0; i < 6; i++, j++) {
            result[i] = s.charAt(j);
        }
        return new String(result);

    }


}

