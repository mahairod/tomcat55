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

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * The JSP engine (a.k.a Jasper)!
 * 
 * There are 2 use cases for JspServlet:
 * - "cooperating" servlet container. JspServlet is used to handle jsps,
 * internaly. The container will register a mapping from *.jsp to JspServlet,
 * allowing webapps to override this.
 * 
 * - use in "foreign" containers. Assuming a container using a different jsp
 * implementation, you can set your web applications to use jasper, overriding
 * the container's impl. In order to do that you must install all jasper
 * binaries in WEB-INF/lib ( or in a common directory accessible to all
 * webapps ) and map *.jsp to JspServlet in your web.xml.
 *
 * Note that JspServlet is not needed for precompiled jsps ( using jspc ),
 * in neither case ( unless you want to do runtime recompilation - but then
 * why use jspc ? ).
 *
 * <b>Class loader issues. </b> In a servlet container that uses jasper
 * and jspservlet you have 2 options for the required jasper classes.
 * The servlet itself _must_ be visible in all applications class loader's.
 * It depends on a minimal set of classes:
 *  - org.apache.tomcat.util.compat.*
 * The jasper implementation and it's dependencies can (should)  be installed
 * in a separate directory, hidden from webapplications ( that avoids
 * class colision and improve security ). You must pass the
 * JASPER_CLASSPATH_OPTION init parameter to jasper.
 *
 * On foreign servers or if you don't use the JASPER_CLASSPATH_OPTION all 
 * required jasper classes ( including xml parser, etc ) must be
 * visible in the webapp class loader.
 *
 * The runtime also depends on a number of attributes in context:
 * XXX TODO
 *
 * The following init parameters could be used to tune jasper's operation:
 * XXX TODO
 * 
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public class JspServlet extends HttpServlet {
    /** Init parameter to pass the classpath to the jasper runtime
     */
    public static final String JASPER_CLASSPATH_OPTION="jasper.classpath";

    public static final String JSP_SERVLET_IMPL=
	"org.apache.jasper34.liaison.JspServletLiaisonImpl";
    
    protected ServletContext context = null;

    // XXX Is it the config of JspServlet or of the jsp page ?
    protected ServletConfig config;

    protected Hashtable jsps = new Hashtable();

    protected JspServletLiaison jasperLiaison;

    /** Set to true to provide Too Much Information on errors */
    private final boolean insecure_TMI = false;

    private void loadJspServletLiaison( ServletConfig config,
				    ServletContext context )
	throws ServletException
    {
	String cpath= config.getInitParameter( "jasper.classpath" );
	ClassLoader cloader=this.getClass().getClassLoader();
	try {
	    URL[] urls=cpath2urls( cpath ); // XXX use compat
	    
	    if( urls != null ) {
		cloader=new URLClassLoader( urls, cloader );
	    }
	    Class c=
		cloader.loadClass( JSP_SERVLET_IMPL );
	    jasperLiaison=(JspServletLiaison)c.newInstance();
	    
	    jasperLiaison.init( config, context);
	} catch( Exception ex ) {
	    ex.printStackTrace();
	    throw new ServletException( ex );
	}
    }

    private URL [] cpath2urls( String cpath ) {
	if( cpath == null ) return null;
	return null;
    }
    
    public void init(ServletConfig config)
	throws ServletException
    {
	super.init(config);
	this.config = config;
	this.context = config.getServletContext();

	loadJspServletLiaison( config, context );
    }

    public void service (HttpServletRequest request, 
    			 HttpServletResponse response)
	throws ServletException, IOException
    {
	if( jasperLiaison==null )
	    throw new ServletException( "Initialization error" );

	jasperLiaison.service( request, response );
    }

    /** Destroy all generated Jsp Servlets
     */
    public void destroy() {
	if( jasperLiaison!=null )
	    jasperLiaison.destroy();
    }
}
