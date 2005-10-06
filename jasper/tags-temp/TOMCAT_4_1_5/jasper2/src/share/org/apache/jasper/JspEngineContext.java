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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.servlet.JasperLoader;
import org.apache.jasper.servlet.JspServletWrapper;

/**
 * The context data and methods required to compile a
 * specific JSP page.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Pierre Delisle
 * @author Glenn Nielsen
 * @author Remy Maucherat
 */
public class JspEngineContext 
    implements JspCompilationContext {


    // ----------------------------------------------------- Instance Variables


    private JspReader reader;
    private ServletWriter writer;
    private ServletContext context;
    private URLClassLoader jspLoader;
    private Compiler jspCompiler;
    private boolean isErrPage;
    private String jspUri;
    private String jspPath;
    private String baseURI;
    private String outDir;
    private URL [] outUrls = new URL[1];
    private Class servletClass;
    private String servletClassName;
    private String classFileName;
    private String servletPackageName = Constants.JSP_PACKAGE_NAME;
    private String servletJavaFileName;
    private String contentType;
    private Options options;
    private JspRuntimeContext rctxt;
    private boolean reload = true;
    private int removed = 0;
    private JspServletWrapper jsw;


    // ------------------------------------------------------------ Constructor


    public JspEngineContext(JspRuntimeContext rctxt, ServletContext context,
                            String jspUri, JspServletWrapper jsw,
                            boolean isErrPage, Options options)
        throws JasperException {

        this.rctxt = rctxt;
        this.context = context;
        this.jspUri = jspUri;
        this.jsw = jsw;
        baseURI = jspUri.substring(0, jspUri.lastIndexOf('/') + 1);
        this.isErrPage = isErrPage;
        this.options = options;

        createOutdir();
        createCompiler();

    }


    // ------------------------------------------ JspCompilationContext Methods


    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
        return rctxt.getClassPath();
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
     * Get the ServletContext for the JSP we're processing now. 
     */
    public ServletContext getServletContext() {
        return context;
    }


    /**
     * The class loader to use for loading classes while compiling
     * this JSP.
     */
    public ClassLoader getClassLoader() {
        return rctxt.getParentClassLoader();
    }


    /**
     * Return true if the current page is an errorpage.
     */
    public boolean isErrorPage() {
        return isErrPage;
    }


    /**
     * What is the scratch directory we are generating code into?
     */
    public String getOutputDir() {
        return outDir;
    }


    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file. 
     */
    public String getJspFile() {
        return jspUri;
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
        int iSep = jspUri.lastIndexOf('/') + 1;
        int iEnd = jspUri.length();
        StringBuffer modifiedClassName = 
            new StringBuffer(jspUri.length() - iSep);
	if (!Character.isJavaIdentifierStart(jspUri.charAt(iSep))) {
	    // If the first char is not a legal Java letter or digit,
	    // prepend a '$'.
	    modifiedClassName.append('$');
	}
        for (int i = iSep; i < iEnd; i++) {
            char ch = jspUri.charAt(i);
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
     * Package name for the generated class.
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
        servletJavaFileName = getServletClassName() + ".java";
 	if (outputDir != null && !outputDir.equals("")) {
	    servletJavaFileName = outputDir + servletJavaFileName;
        }
	return servletJavaFileName;

    }


    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated() {
        return options.getKeepGenerated();
    }


    /**
     * Get the content type of this JSP.
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
        this.isErrPage = isErrPage;
    }


    public Compiler createCompiler() throws JasperException {

        if (jspCompiler != null ) {
            return jspCompiler;
        }

        jspCompiler = new Compiler(this, jsw);
        return jspCompiler;

    }


    public void compile() throws JasperException, FileNotFoundException {

        if (jspCompiler.isOutDated()) {
            try {
                jspCompiler.compile();
                reload = true;
            } catch (Exception ex) {
                throw new JasperException(
                    Constants.getString("jsp.error.unable.compile"),ex);
            }
        }
    }


    public Class load() 
        throws JasperException, FileNotFoundException {

        try {
            if (servletClass == null && !options.getDevelopment()) {
                compile();
            }
            jspLoader = new JasperLoader
                (outUrls,
                 getServletPackageName() + "." + getServletClassName(),
                 rctxt.getParentClassLoader(),
                 rctxt.getPermissionCollection(),
                 rctxt.getCodeSource());
            servletClass = jspLoader.loadClass(
                 getServletPackageName() + "." + getServletClassName());
        } catch (FileNotFoundException ex) {
            jspCompiler.removeGeneratedFiles();
            throw ex;
        } catch (ClassNotFoundException cex) {
            throw new JasperException(
                Constants.getString("jsp.error.unable.load"),cex);
        } catch (JasperException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JasperException
                (Constants.getString("jsp.error.unable.compile"), ex);
        }
        removed = 0;
        reload = false;
        return servletClass;
    }


    public boolean isReload() {
        return reload;
    }


    public void incrementRemoved() {
        if (removed > 1) {
            jspCompiler.removeGeneratedFiles();
            rctxt.removeWrapper(jspUri);
        }
        removed++;
    }


    public boolean isRemoved() {
        if (removed > 1 ) {
            return true;
        }
        return false;
    }


    /** 
     * Get the full value of a URI relative to this compilations context
     */
    public String resolveRelativeUri(String uri) {
        if (uri.charAt(0) == '/') {
            return uri;
        }
        return baseURI + uri;
    }


    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     * @return a null if the resource cannot be found or represented 
     *         as an InputStream.
     */
    public java.io.InputStream getResourceAsStream(String res) {
        return context.getResourceAsStream(res);
    }


    public URL getResource(String res) throws MalformedURLException {
        return context.getResource(res);
    }


    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path) {
        if (context != null) {
            return context.getRealPath(path);
        }
        return path;
    }


    public String[] getTldLocation(String uri) throws JasperException {
	String[] location = 
	    options.getTldLocationsCache().getLocation(uri);
	return location;
    }


    public final String getClassFileName() {

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


    // -------------------------------------------------------- Private Methods


    private void createOutdir() {
        File outDir = null;
        try {
            URL outURL = options.getScratchDir().toURL();
            String outURI = outURL.toString();
            if (outURI.endsWith("/")) {
                outURI = outURI 
                    + jspUri.substring(1,jspUri.lastIndexOf("/")+1);
            } else {
                outURI = outURI 
                    + jspUri.substring(0,jspUri.lastIndexOf("/")+1);
            }
            outURL = new URL(outURI);
            outDir = new File(outURL.getFile());
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            this.outDir = outDir.toString() + File.separator;
            outUrls[0] = new URL(outDir.toURL().toString() + File.separator);
        } catch (Exception e) {
            throw new IllegalStateException("No output directory: " +
                                            e.getMessage());
        }
    }


    /**
     * Mangle the specified character to create a legal Java class name.
     */
    private static final String mangleChar(char ch) {

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
