/*
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

package org.apache.jasper34.jsptree;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper34.core.*;
import org.apache.jasper34.generator.*;

import org.apache.jasper34.runtime.JasperException;
import org.apache.tomcat.util.log.*;
import org.apache.jasper34.parser.*;

/**
 * Internal representation of the JSP page, including all collected
 * directives and options. The data is stored in a tree of JspNodes
 * ( XXX right now we're stil using tree of Generators ) and external
 * visitors will generate the code ( or execute the page ).
 *
 * During page parsing we'll construct a PageInfo, with all the
 * associated informations.
 *
 * Refactored from JspParseEventListener, generators.
 *
 * @author Anil K. Vijendran
 * @author Costin Manolache
 */
public class JspPageInfo {
    // XXX public fields will be replaced with API
    public String servletClassName;
    
    public String jspServletBase = Constants.JSP_SERVLET_BASE;
    public String serviceMethodName = Constants.SERVICE_METHOD_NAME;
    public String servletContentType = Constants.SERVLET_CONTENT_TYPE;

    public String extendsClass = "";
    public Vector interfaces = new Vector();
    public Vector imports = new Vector();

    public String error = "";

    public boolean genSessionVariable = true;
    public boolean singleThreaded = false;
    public boolean autoFlush = true;

    public Vector generators = new Vector();

    public BeanRepository beanInfo;

    public int bufferSize = Constants.DEFAULT_BUFFER_SIZE;

    // a set of boolean variables to check if there are multiple attr-val
    // pairs for jsp directive.
    public boolean languageDir = false, extendsDir = false, sessionDir = false;
    public boolean bufferDir = false, threadsafeDir = false, errorpageDir = false;
    public boolean iserrorpageDir = false, infoDir = false, autoFlushDir = false;
    public boolean contentTypeDir = false;


    /* support for large files */
    public int stringId = 0;
    public Vector vector = new Vector();
    public String dataFile;

    public TagLibraries libraries;

    // Variables shared by all TagBeginGenerator and TagEndGenerator instances
    // to keep track of nested tags and variable names
    private Stack tagHandlerStack;
    private Hashtable tagVarNumbers;

    // This variable keeps track of tag pools.  We only need
    // one tag pool per tag reuse scope.
    public Vector tagPools = new Vector();
    
    public JspCompilationContext ctxt;

    public JspPageInfo(JspCompilationContext ctxt) {
	this.beanInfo = new BeanRepository(ctxt.getClassLoader());
        this.libraries = new TagLibraries(ctxt);
        this.ctxt = ctxt;

        // FIXME: Is this good enough? (I'm just taking the easy way out - akv)
        if (ctxt.getOptions().getLargeFile())
            dataFile = ctxt.getOutputDir() + File.separatorChar +
                ctxt.getServletPackageName() + "_" +
                ctxt.getServletClassName() + ".dat";
    }

    // -------------------- Getters --------------------
    // XXX remove all public fields.

    public Options getOptions() {
	return ctxt.getOptions();
    }
    
    // -------------------- Setters --------------------
    
    public final void addGenerator(GeneratorBase gen) throws JasperException {
        gen.init(ctxt);
        generators.addElement(gen);
    }

    public Stack getTagHandlerStack() {
        if (tagHandlerStack == null) {
            tagHandlerStack = new Stack();
        }
        return tagHandlerStack;
    }

    public Hashtable getTagVarNumbers() {
        if (tagVarNumbers == null) {
            tagVarNumbers = new Hashtable();
        }
        return tagVarNumbers;
    }

    public TagLibraries getTagLibraries() {
	return libraries;
    }

    // -------------------- Accessors --------------------

    // this could go directly to the JavaCodeGenerator

    public void addImport( String str ) {
	imports.addElement( str );
    }


    public String getServletClassName() {
	return ctxt.getServletClassName();
    }

    public String getServletPackageName() {
	return ctxt.getServletPackageName();
    }

    public boolean isErrorPage() {
	return ctxt.isErrorPage();
    }
}
