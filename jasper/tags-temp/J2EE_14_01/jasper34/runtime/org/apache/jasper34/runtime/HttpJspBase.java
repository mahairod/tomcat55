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

package org.apache.jasper34.runtime;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

//import org.apache.jasper.JasperException;


/**
 * This is the subclass of all JSP-generated servlets.
 *
 * @author Anil K. Vijendran
 */
public abstract class HttpJspBase 
    extends HttpServlet 
    implements HttpJspPage 
{
    protected static JspFactory _jspxFactory = null;

    protected HttpJspBase() {
    }

    public final void init(ServletConfig config) 
	throws ServletException 
    {
        super.init(config);
	_jspxFactory = JspFactory.getDefaultFactory();
	jspInit();
    }
    
    public String getServletInfo() {
	return JspFactoryImpl.getString ("jsp.engine.info");
    }

    public final void destroy() {
	jspDestroy();
    }

    /**
     * Entry point into service.
     */
    public final void service(HttpServletRequest request,
			      HttpServletResponse response) 
	throws ServletException, IOException 
    {

	PageContext pageContext=null;
	JspWriter out=null;
	try {
	    try {
		_jspx_init(); // need to be in init !
		pageContext = _getPageContext( request, response ); 
		out=pageContext.getOut();
		_jspService(pageContext, request, response );
	    } catch (Exception ex) {
		// Experimental/test line mappings
		JspRuntimeLibrary.handleExceptionMapping( this, 
							  ex );

		if (pageContext != null)
		    pageContext.handlePageException(ex);
	    } catch (Error error) {
		throw error;
	    } catch (Throwable throwable) {
		throw new ServletException(throwable);
	    }
	} finally {
	    IOException err=null;
	    if( pageContext!=null ) {
		try {
		    // it can also be BodyContent !
		    // XXX how do we flush body content ?
		    // We only flush the top level out, what if we have
		    // a stack ?
		    if( out instanceof JspWriterImpl )
			((JspWriterImpl)out).flushBuffer();
		} catch( IOException ex ) {
		    err=ex;
		    // handlePageException( ex );
		    // This was a bug in previous implementations:
		    // if flushBuffer throws exceptions release() is not
		    // corectly called !( this was part of the generated code )
		}
	    }
	    _jspxFactory.releasePageContext(pageContext);
	    if( err!=null ) throw err;
	}
    }
    
    public void jspInit() {
    }
    
    public void jspDestroy() {
    }

    public abstract void _jspx_init()
	throws Throwable;

    public abstract PageContext _getPageContext(HttpServletRequest request, 
					       HttpServletResponse response);

    
    public abstract void _jspService(PageContext pageContext,
				     HttpServletRequest request, 
				     HttpServletResponse response) 
	throws Throwable;

    public void _jspService(HttpServletRequest request, 
			    HttpServletResponse response)
	throws ServletException, IOException {
    }


    /** Return extra dependencies for this file ( TLDs, included files )
     */
    public String[] _getDepends() { return null; }

    /** Return the static chunks to be used with sendChunk()
     */
    public String[] _getChunks() { return null; }

    /** line mapping - the files used in lineMap
     */
    public String[] _getFileMap() { return null; }

    /** Line map.
	{ java_start, java_end,
	  jsp_file_start_idx, jsp_file_start_line, jsp_file_start_col,
	  jsp_file_end_idx, jsp_file_end_line, jsp_file_end_col
	}
	@see GeneratorBase.generateLineMap 
    */
    public int[][] _getLineMap() { return null; }
}
