/*
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

package org.apache.jasper34.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;
import javax.servlet.jsp.JspFactory;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.jasper34.runtime.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.generator.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.liaison.*;
import org.apache.jasper34.core.Compiler;

import org.apache.tomcat.util.log.Log;

/** Stores extra informations about the generated servlet.
 */
public class JspServletWrapper {
    Servlet theServlet;
    
    String jspUri;
    boolean isErrorPage;
    // ServletWrapper will set this 
    Class servletClass;

    ContainerLiaison containerL;
    Mangler mangler;
    JspPageInfo pageInfo;
    
    JspServletWrapper(String jspUri, boolean isErrorPage) {
	this.jspUri = jspUri;
	this.isErrorPage = isErrorPage;
	this.theServlet = null;
    }

    void load(ServletConfig config) throws JasperException, ServletException {
	try {
	    String className=pageInfo.getMangler().getServletClassName();
	    // XXX Implement Glenn's one class loader / jsp 
	    ClassLoader loader=containerL.getClassLoader();
	    
	    servletClass =loader.loadClass(className);
	    
	    // Class servletClass = (Class) loadedJSPs.get(jspUri);
	    // This is to maintain the original protocol.
	    destroy();
	    
	    theServlet = (Servlet) servletClass.newInstance();
	} catch (Exception ex) {
	    throw new JasperException(ex);
	}
	theServlet.init(config);
	// 	if (theServlet instanceof HttpJspBase)  {
	// 	    HttpJspBase h = (HttpJspBase) theServlet;
	// 	    h.setClassLoader(JspServlet.this.parentClassLoader);
	// 	}
    }


    void initWrapper( ServletContext context, Options options, 
		      HttpServletRequest req, HttpServletResponse res) 
    {
	if( containerL == null )
	    containerL=new JspEngineContext( options.getClassPath(),
					     context, jspUri, 
					     isErrorPage, options, req, res);
	if( mangler==null )
	    mangler=new Mangler33(containerL.getOutputDir(),
				  containerL.getRealPath("/"), jspUri);
	//	if( pageInfo==null ) {
	pageInfo=new JspPageInfo( containerL, options, mangler );
	pageInfo.setJspFile( jspUri );
	pageInfo.setErrorPage( isErrorPage );
	//	}

    }
    

    
    /**
     * Determines whether the current JSP class is older than the JSP file
     * from whence it came
     */
    boolean isOutDated()
    {
	// XXX remember if the compilation was attempted and failed -
	// and return the old message
	boolean outDated=false;
	File jsp=new File( jspUri );
        File jspReal = null;

	if( theServlet==null ) return true;
	if( servletClass==null ) return true;
	
        String realPath = containerL.getRealPath(jsp.getPath());
        if (realPath == null) {
	    System.out.println("JspServletWrapper Special case:"
			       + " realPath == null " +
			       jsp.getPath());
	    return true;
	}

        jspReal = new File(realPath);
	
	if(!jspReal.exists()){
	    System.out.println("JspServletWrapper Special case:"
			       + " jspReal doesn't exist " +
			       jsp.getPath());
	    return true;
	}
	
	File classFile = new File(mangler.getClassFileName());
        if (classFile.exists()) {
            outDated = classFile.lastModified() < jspReal.lastModified();
        } else {
            outDated = true;
        }

        return outDated;
    }

    public void service(HttpServletRequest request, 
			HttpServletResponse response)
	throws ServletException, IOException
    {
	if (theServlet instanceof SingleThreadModel) {
	    // sync on the wrapper so that the freshness
		// of the page is determined right before servicing
	    synchronized (this) {
		theServlet.service(request, response);
	    }
	} else {
	    theServlet.service(request, response);
	}
	return;
    }
    
    public void destroy() {
	if (theServlet != null)
	    theServlet.destroy();
    }
}
