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

import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.TagLibraries;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.jasper.compiler.Compiler;

/**
 * A place holder for various things that are used through out the JSP
 * engine. This is a per-request/per-context data structure. Some of
 * the instance variables are set at different points.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Pierre Delisle
 */
public interface JspCompilationContext {

    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath();
    
    /**
     * Get the input reader for the JSP text. 
     */
    public JspReader getReader();
    
    /**
     * Where is the servlet being generated?
     */
    public ServletWriter getWriter();
    
    /**
     * What class loader to use for loading classes while compiling
     * this JSP?
     */
    public ClassLoader getClassLoader();

    /**
     * Are we processing something that has been declared as an
     * errorpage? 
     */
    public boolean isErrorPage();
    
    /**
     * The scratch directory to generate code into.
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir();
    
    /**
     * The scratch directory to generate code into for javac.
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getJavacOutputDir();

    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file. 
     */
    public String getJspFile();
    
    /**
     * Just the class name (does not include package name) of the
     * generated class. 
     */
    public String getServletClassName();
    
    /**
     * The package name into which the servlet class is generated.
     */
    public String getServletPackageName();

    /**
     * Full path name of the Java file into which the servlet is being
     * generated. 
     */
    public String getServletJavaFileName();

    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated();

    /**
     * The content type of this JSP.
     *
     * Content type includes content type and encoding. 
     */
    public String getContentType();

    /**
     * Get hold of the Options object for this context. 
     */
    public Options getOptions();

    public void setContentType(String contentType);

    public void setReader(JspReader reader);
    
    public void setWriter(ServletWriter writer);
    
    void setServletClassName(String servletClassName);
    
    public void setServletPackageName(String servletPackageName);

    public void setServletJavaFileName(String servletJavaFileName);
    
    public void setErrorPage(boolean isErrPage);

    /**
     * Create a "Compiler" object based on some init param data. This
     * is not done yet. Right now we're just hardcoding the actual
     * compilers that are created. 
     */
    public Compiler createCompiler() throws JasperException;


    /** 
     * Get the full value of a URI relative to this compilations context
     */
    public String resolveRelativeUri(String uri);

    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     * @return a null if the resource cannot be found or represented 
     *         as an InputStream.
     */
    public java.io.InputStream getResourceAsStream(String res);

    public java.net.URL getResource(String res) throws MalformedURLException;

    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path);

    static interface Interface1 {
    }

    /**
     * Get the 'location' of the TLD associated with 
     * a given taglib 'uri'.
     * 
     * @return An array of two Strings. The first one is
     * real path to the TLD. If the path to the TLD points
     * to a jar file, then the second string is the
     * name of the entry for the TLD in the jar file.
     * Returns null if the uri is not associated to
     * a tag library 'exposed' in the web application.
     * A tag library is 'exposed' either explicitely in 
     * web.xml or implicitely via the uri tag in the TLD 
     * of a taglib deployed in a jar file (WEB-INF/lib).
     */
    public String[] getTldLocation(String uri) throws JasperException;
}

