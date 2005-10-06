/*
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

package org.apache.jasper34.liaison;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.jasper34.core.*;

import org.apache.tomcat.util.log.*;

// Old EmbededServletOptions

/**
 * A class to hold all init parameters specific to the JSP engine.
 * Backed by ServletConfig
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 * @author Costin Manolache
 */
public final class OptionsServletConfig extends Options {
    
    /**
     * Create an EmbededServletOptions object using data available from
     * ServletConfig and ServletContext. 
     */
    public OptionsServletConfig(ServletConfig config,
				ServletContext context)
    {
	super( new ServletConfigPropertyAdapter( config, context ));
    }

    static class ServletConfigPropertyAdapter
	implements Options.PropertyAdapter
    {
	ServletContext context;
	ServletConfig config;

	ServletConfigPropertyAdapter( ServletConfig config,
				      ServletContext context )
	{
	    this.context=context;
	    this.config=config;
	}

	public void setProperty(String k, String v ) {
	    throw new RuntimeException( "Operation not supported ");
	}

	public String getProperty( String s, String def ) {
	    //	    System.out.println("GetOption: " + s + " " + def ); 
	    // Special cases
	    if( Options.SCRATCH_DIR.equals( s ) ) {
		String sd=config.getInitParameter(s);
		if( sd==null ) {
		    File f=(File)context.getAttribute( Constants.TMP_DIR );
		    if( f==null ) return null;
		    sd=f.toString();
		}
		// Options will also try java.io.tmpdir
		return sd;
	    }
	    
	    if( Options.CLASS_PATH.equals( s ) ) {
		String sd=config.getInitParameter(s);
		if( sd==null ) {
		    sd=(String)context.
			getAttribute( Constants.SERVLET_CLASSPATH );
		}
		// Options will also try java.io.tmpdir
		return sd;
	    }
	    
	    String v=config.getInitParameter( s );
	    if( v==null ) return def;
	    return v;
	}
    }
}

