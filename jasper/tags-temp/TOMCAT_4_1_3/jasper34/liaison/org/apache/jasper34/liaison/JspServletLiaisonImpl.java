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

package org.apache.jasper34.liaison;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import java.util.*;
import java.io.*;

import org.apache.jasper34.runtime.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.generator.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.javacompiler.*;
import org.apache.jasper34.liaison.*;
import org.apache.jasper34.core.Compiler;

import org.apache.jasper34.servlet.*;

import org.apache.tomcat.util.log.Log;

/**
 * This is used by JspServlet to construct a link to jasper at runtime,
 * in a separated class loader.
 *
 * @author Costin Manolache
 */
public class JspServletLiaisonImpl extends JspServletLiaison {
    static final String INC_SERVLET_PATH =
	"javax.servlet.include.servlet_path";
    static final String PRECOMPILE = "jsp_precompile";


    protected ServletContext context = null;
    // config of jsp servlet
    protected ServletConfig config;
    protected Options options;
    Hashtable jsps=new Hashtable();
    
    public JspServletLiaisonImpl() {
    }

    /** Set to true to provide Too Much Information on errors */
    private final boolean insecure_TMI = false;

    public void init(ServletConfig config, ServletContext context )
	throws ServletException
    {
	this.config = config;
	this.context = config.getServletContext();
        
	options = new OptionsServletConfig(config, context);

	// XXX each generate servlet will check that - so it works with JSPC
	// and without JspServlet
	if( JspFactory.getDefaultFactory() ==null )
	    JspFactory.setDefaultFactory(new JspFactoryImpl());
    }


    public void service (HttpServletRequest request, 
    			 HttpServletResponse response)
	throws ServletException, IOException
    {
	try {
            String includeUri 
                = (String) request.getAttribute(INC_SERVLET_PATH);

            String jspUri;

            if (includeUri == null)
		jspUri = request.getServletPath();
            else
                jspUri = includeUri;

            boolean precompile = isPreCompile(request);

	    // XXX This seemed to be true all the time.
	    // When do we have exception ??
	    boolean isErrorPage = false; //exception != null;
	
	    JspServletWrapper wrapper = (JspServletWrapper) jsps.get(jspUri);
	    if( debug>0 )
		log("Service " + jspUri + " " + wrapper );
	    if (wrapper == null) {
		wrapper = new JspServletWrapper(jspUri, isErrorPage);
		jsps.put(jspUri, wrapper);
	    }

	    wrapper.initWrapper( context, options, request, response);

	    if( wrapper.isOutDated() ) {
		compileJsp(wrapper, wrapper.jspUri, wrapper.isErrorPage,
			   request, response);
	    }
	    

	    // If a page is to only to be precompiled return.
	    if (precompile)
		return;
	
	    wrapper.service(request, response);

	} catch (FileNotFoundException ex) {
	    handleFileNotFoundException( ex, response );
	} catch (RuntimeException e) {
	    throw e;
	} catch (ServletException e) {
	    throw e;
	} catch (IOException e) {
	    throw e;
	} catch (Throwable e) {
	    throw new ServletException(e);
	}
    }

    void compileJsp(JspServletWrapper jsw,String jspUri,  boolean isErrorPage,
		    HttpServletRequest req, HttpServletResponse res) 
	throws JasperException, FileNotFoundException 
    {
	synchronized ( this ) {
	    try {
		// second check - maybe it was compiled while we
		// waited for the sync 
		if ( ! jsw.isOutDated() )
		    return;
		
		Compiler compiler = new Compiler(jsw.containerL);
		compiler.jsp2java( jsw.pageInfo );
		String javaFile=
		    jsw.pageInfo.getMangler().getJavaFileName();
		
		JavaCompiler javaC=
		    compiler.createJavaCompiler( jsw.pageInfo );
		compiler.prepareCompiler( javaC, jsw.pageInfo );
		boolean status = javaC.compile( javaFile );
		
		if (status == false) {
		    String msg = javaC.getCompilerMessage();
		    throw new JasperException("Unable to compile " + msg);
		    // XXX remember this - until the file is changed !!
		}
		
		// remove java file if !keepgenerated, etc
		compiler.postCompile(jsw.pageInfo);
	       
		if( jsw.theServlet == null) {
		    // A (re)load happened - need to init the servlet
		    jsw.load( this.config );
		}
	    } catch (ClassNotFoundException cex) {
		throw new JasperException(
	       	  ContainerLiaison.getString("jsp.error.unable.load"),cex);
	    } catch (FileNotFoundException ex) {
		throw ex;
	    } catch (JasperException ex) {
		throw ex;
	    } catch (Exception ex) {
		throw new JasperException(
		 ContainerLiaison.getString("jsp.error.unable.compile"), ex);
	    }
	}
    }

    // XXX Do we need all this ??? 
    private void handleFileNotFoundException( IOException ex,
					      HttpServletResponse response)
	throws IOException, IllegalStateException
    {
	try {
	    if (insecure_TMI) {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, 
				       ContainerLiaison.getString
				   ("jsp.error.file.not.found.TMI", 
				    new Object[] { ex.getMessage() }));
	    } else {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, 
				   ContainerLiaison.getString
				   ("jsp.error.file.not.found", 
				    new Object[] {}));
	    }
	} catch (IllegalStateException ise) {
	    // logs are presumed to be secure,
	    //  thus the TMI info can be logged
	    ContainerLiaison.message(ContainerLiaison.getString
					 ("jsp.error.file.not.found.TMI",
					  new Object[] { ex.getMessage()}), 
				     Log.ERROR);
	    // rethrow FileNotFoundException so someone higher
	    // up can handle
	    if (insecure_TMI)
		throw ex;
	    else
		throw new FileNotFoundException(ContainerLiaison.getString
						("jsp.error.file.not.found", 
						     new Object[] {}));
	}
    }

    /** Destroy all generated Jsp Servlets
     */
    public void destroy() {
	ContainerLiaison.message("JspServlet.destroy()", Log.INFORMATION);

	Enumeration servlets = jsps.elements();
	while (servlets.hasMoreElements()) 
	    ((JspServletWrapper) servlets.nextElement()).destroy();
    }


    
    // -------------------- Utils --------------------

    /** Detect if we're in a precompile request
     */
    private boolean isPreCompile(HttpServletRequest request) 
        throws ServletException 
    {
        boolean precompile = false;
        String precom = request.getParameter(PRECOMPILE);
        String qString = request.getQueryString();
        
        if (precom != null) {
            if (precom.equals("true")) 
                precompile = true;
            else if (precom.equals("false")) 
                precompile = false;
            else {
		// This is illegal.
		throw new ServletException("Can't have request parameter " +
					   PRECOMPILE +
					   " set to " + precom);
	    }
	} else if (qString != null &&
		   (qString.startsWith(PRECOMPILE) ||
		    qString.indexOf("&" + PRECOMPILE) != -1))
            precompile = true;

        return precompile;
    }
    

    private int debug=10;
    public void log( String s ) {
	System.out.println("JspServlet: " + s );
    }


}
