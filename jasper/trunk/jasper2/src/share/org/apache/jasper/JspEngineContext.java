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
import org.apache.jasper.compiler.JspCompiler;
import org.apache.jasper.compiler.SunJavaCompiler;
import org.apache.jasper.compiler.JavaCompiler;
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
 */
public class JspEngineContext implements JspCompilationContext {
    private JspReader reader;
    private ServletWriter writer;
    private ServletContext context;
    private URLClassLoader jspLoader;
    private Compiler jspCompiler;
    private boolean isErrPage;
    private String jspUri;
    private String baseURI;
    private String outDir;
    private URL [] outUrls = new URL[1];
    private Class servletClass;
    private String servletClassName;
    private String servletPackageName = Constants.JSP_PACKAGE_NAME;
    private String servletJavaFileName;
    private String contentType;
    private Options options;
    private JspRuntimeContext rctxt;
    private boolean reload = true;
    private int removed = 0;
    private JspServletWrapper jsw;

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

    private void createOutdir() {
        File outDir = null;
        try {
            URL outURL = options.getScratchDir().toURL();
            String outURI = outURL.toString();
            if( outURI.endsWith("/") ) {
                outURI = outURI +
                         jspUri.substring(1,jspUri.lastIndexOf("/")+1);
            } else {
                outURI = outURI +
                         jspUri.substring(0,jspUri.lastIndexOf("/")+1);;
            }
            outURL = new URL(outURI);
            outDir = new File(normalize(outURL.getFile()));
            if( !outDir.exists() ) {
                outDir.mkdirs();
            }
            this.outDir = outDir.toString() + File.separator;
            outUrls[0] = new URL(outDir.toURL().toString() + File.separator);
        } catch(Exception e) {
            throw new IllegalStateException("No output directory: " +
                                            e.getMessage());
        }
    }

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
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
        return outDir;
    }

    /**
     * Get the scratch directory to place generated code for javac.
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getJavacOutputDir() {
        return null;
    }

    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file. 
     */
    public String getJspFile() {
        return jspUri;
    }
    
    /**
     * Just the class name (does not include package name) of the
     * generated class. 
     */
    public String getServletClassName() {
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

    /**
     * Create a "Compiler" object based on some init param data. If	
     * jspCompilerPlugin is not specified or is not available, the 
     * SunJavaCompiler is used.
     */
    public Compiler createCompiler() throws JasperException {

        if (jspCompiler != null ) {
            return jspCompiler;
        }

	String compilerPath = options.getJspCompilerPath();
	Class jspCompilerPlugin = options.getJspCompilerPlugin();
        JavaCompiler javac;

	if (jspCompilerPlugin != null) {
            try {
                javac = (JavaCompiler) jspCompilerPlugin.newInstance();
            } catch (Exception ex) {
		Constants.message("jsp.warning.compiler.class.cantcreate",
				  new Object[] { jspCompilerPlugin, ex }, 
				  Logger.FATAL);
                javac = new SunJavaCompiler();
	    }
	} else {
            javac = new SunJavaCompiler();
	}

        if (compilerPath != null) {
            javac.setCompilerPath(compilerPath);
        }

        jspCompiler = new JspCompiler(this,jsw);
	jspCompiler.setJavaCompiler(javac);
         
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

    public Class load() throws JasperException, FileNotFoundException {

        try {
            if (servletClass == null || options.getDevelopment()) {
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

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    protected String normalize(String path) {

        if (path == null) {
            return null;
        }

        String normalized = path;

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0) {
            normalized = normalized.replace('\\', '/');
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            if (index == 0) {
                return (null);  // Trying to go outside our context
            }
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }

}
