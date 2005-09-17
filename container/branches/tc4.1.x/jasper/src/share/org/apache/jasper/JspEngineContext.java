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





package org.apache.jasper;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.servlet.JasperLoader;
import org.apache.jasper.compiler.TagLibraries;

import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.compiler.JspCompiler;
import org.apache.jasper.compiler.SunJavaCompiler;
import org.apache.jasper.compiler.JavaCompiler;

import org.apache.jasper.logging.Logger;
/**
 * A place holder for various things that are used through out the JSP
 * engine. This is a per-request/per-context data structure. Some of
 * the instance variables are set at different points.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Pierre Delisle
 */
public class JspEngineContext implements JspCompilationContext {
    JspReader reader;
    ServletWriter writer;
    ServletContext context;
    URLClassLoader loader;
    String classpath; // for compiling JSPs.
    boolean isErrPage;
    String jspFile;
    String outDir;
    String servletClassName;
    String servletPackageName = Constants.JSP_PACKAGE_NAME;
    String servletJavaFileName;
    String contentType;
    Options options;
    HttpServletRequest req;
    HttpServletResponse res;
    

    public JspEngineContext(URLClassLoader loader, String classpath, 
                            ServletContext context, String jspFile, String outDir,
                            boolean isErrPage, Options options,
                            HttpServletRequest req, HttpServletResponse res) 
    {
        this.loader = loader;
        this.classpath = classpath;
        this.context = context;
        this.jspFile = jspFile;
	this.outDir = outDir;
        this.isErrPage = isErrPage;
        this.options = options;
        this.req = req;
        this.res = res;
    }

    /**
     * Get the http request we are servicing now...
     */
    public HttpServletRequest getRequest() {
        return req;
    }
    

    /**
     * Get the http response we are using now...
     */
    public HttpServletResponse getResponse() {
        return res;
    }

    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
	URL [] urls = loader.getURLs();
        StringBuffer cpath = new StringBuffer();
        String sep = System.getProperty("path.separator");

        for(int i = 0; i < urls.length; i++) {
	    // Tomcat 4 can use URL's other than file URL's,
	    // a protocol other than file: will generate a
	    // bad file system path, so only add file:
	    // protocol URL's to the classpath.
	    if( urls[i].getProtocol().equals("file") ) {
                cpath.append((String)urls[i].getFile()+sep);
	    }
        }
         
        return cpath.toString() + classpath;
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
     * What class loader to use for loading classes while compiling
     * this JSP.
     *
     * I don't think this is used right now -- akv. 
     */
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Are we processing something that has been declared as an
     * errorpage? 
     */
    public boolean isErrorPage() {
        return isErrPage;
    }
    
    /**
     * Get the scratch directory to place generated code.
     *
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
        return jspFile;
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

        if (compilerPath != null)
            javac.setCompilerPath(compilerPath);

        Compiler jspCompiler = new JspCompiler(this);
	jspCompiler.setJavaCompiler(javac);
         
        return jspCompiler;
    }
    
    /** 
     * Get the full value of a URI relative to this compilations context
     */
    public String resolveRelativeUri(String uri)
    {
        if (uri.charAt(0) == '/')
        {
            return uri;
        }
        else
        {
            String actURI =  req.getServletPath();
            String baseURI = actURI.substring(0, actURI.lastIndexOf('/'));
            return baseURI + '/' + uri;
        }
    }    

    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     * @return a null if the resource cannot be found or represented 
     *         as an InputStream.
     */
    public java.io.InputStream getResourceAsStream(String res)
    {
        return context.getResourceAsStream(res);
    }

    public URL getResource(String res)
	throws MalformedURLException
    {
        return context.getResource(res);
    }

    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path)
    {
        if (context != null)
        {
            return context.getRealPath(path);
        }
        else
        {
            return path;
        }
    }

    public String[] getTldLocation(String uri) throws JasperException {
	String[] location = 
	    options.getTldLocationsCache().getLocation(uri);
	return location;
    }
}
