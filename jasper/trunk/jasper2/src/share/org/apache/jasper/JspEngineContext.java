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
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.servlet.JasperLoader;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.jasper.compiler.JspRuntimeContext;

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
    extends JspCompilationContext {


    // ----------------------------------------------------- Instance Variables

    private URLClassLoader jspLoader;
    private URL [] outUrls = new URL[1];
    private Class servletClass;

    // ------------------------------------------------------------ Constructor


    public JspEngineContext(JspRuntimeContext rctxt, ServletContext context,
                            String jspUri, JspServletWrapper jsw,
                            boolean isErrPage, Options options)
        throws JasperException {

        super( jspUri, isErrPage, options, context, jsw, rctxt );
        
        createOutdir();
    }


    // ------------------------------------------ JspCompilationContext Methods


    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
        return rctxt.getClassPath();
    }

    /**
     * The class loader to use for loading classes while compiling
     * this JSP.
     */
    public ClassLoader getClassLoader() {
        return rctxt.getParentClassLoader();
    }

    public Class load() 
        throws JasperException, FileNotFoundException
    {

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


    // -------------------------------------------------------- Private Methods


    private void createOutdir() {
        File outDirF = null;
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
            outDirF = new File(outURL.getFile());
            if (!outDirF.exists()) {
                outDirF.mkdirs();
            }
            this.setOutputDir(  outDirF.toString() + File.separator );
            
            outUrls[0] = new URL(outDirF.toURL().toString() + File.separator);
        } catch (Exception e) {
            throw new IllegalStateException("No output directory: " +
                                            e.getMessage());
        }
    }

}
