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

import java.io.*;

import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.TagLibraries;
import org.apache.jasper.compiler.CommandLineCompiler;
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
 */
public class CommandLineContext implements JspCompilationContext {

    String classPath;
    JspReader reader;
    ServletWriter writer;
    URLClassLoader loader;
    boolean errPage;
    String jspFile;
    String servletClassName;
    String servletPackageName;
    String servletJavaFileName;
    String contentType;
    Options options;

    String uriBase;
    File uriRoot;

    boolean outputInDirs;

    public CommandLineContext(String newClassPath,
                              String newJspFile, String newUriBase,
                              String newUriRoot, boolean newErrPage,
                              Options newOptions)
    throws JasperException
    {
        classPath = newClassPath;
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

    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
        return classPath;
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
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
        return options.getScratchDir().toString();
    }
    
    /**
     * The scratch directory to generate code into for javac.
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getJavacOutputDir() {
        return options.getScratchDir().toString();
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
     * The package name for the generated class.
     * The final package is assembled from the one specified in -p, and
     * the one derived from the path to jsp file.
     */
    public String getServletPackageName() {
        //Get the path to the jsp file.  Note that the jspFile, by the
	//time it gets here, would have been normalized to use '/'
	//as file separator.

	int indexOfSlash = getJspFile().lastIndexOf('/');
        String pathName;
        if (indexOfSlash != -1) {
            pathName = getJspFile().substring(0, indexOfSlash);
        } else {
            pathName = "/";
        }

        //Assemble the package name from the base package name specified on
        //the command line and the package name derived from the path to
        //the jsp file
        String packageName = "";
        if (servletPackageName != null) {
            packageName = servletPackageName;
        }
        packageName += pathName.replace('/', '.');

        return CommandLineCompiler.manglePackage(packageName);
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
    public Compiler createCompiler() throws JasperException {
        return new CommandLineCompiler(this);
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
	throws MalformedURLException
    {
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
}

