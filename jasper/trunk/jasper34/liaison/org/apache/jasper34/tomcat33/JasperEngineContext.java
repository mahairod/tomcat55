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
package org.apache.jasper34.tomcat33;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.jasper34.generator.*;
import org.apache.jasper34.core.Compiler;
import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.*;
import org.apache.jasper34.parser.*;
import org.apache.jasper34.javacompiler.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.liaison.*;

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.compat.*;
    

/** Container liaison for tomcat33
 *
 * @author Costin Manolache
 */
public class JasperEngineContext extends ContainerLiaison
{
    String cpath;  // cache

    Context ctx;
    
    public JasperEngineContext(Context ctx)
    {
	this.ctx=ctx;
	debug=ctx.getDebug();
    }

    /**
     * Extract the class path from the Context
     */
    public String getClassPath() {
	if( cpath != null )
	    return cpath;
	String separator = System.getProperty("path.separator", ":");
	URL classP[]=ctx.getClassPath();
        cpath= JavaCompiler.extractClassPath(classP);

	Jdk11Compat jdkProxy=Jdk11Compat.getJdkCompat();
        URL appsCP[];
        URL commonCP[];
        ClassLoader parentLoader=ctx.getClassLoader();
        appsCP=jdkProxy.getURLs(parentLoader,1);
        commonCP=jdkProxy.getURLs(parentLoader,2);
	if( appsCP!=null ) 
	    cpath+=separator+  JavaCompiler.extractClassPath(appsCP);
	if( commonCP!=null ) 
	    cpath+=separator+ JavaCompiler.extractClassPath(commonCP);
	return cpath;
    }
    
    /**
     * What class loader to use for loading classes while compiling
     * this JSP? I don't think this is used right now -- akv. 
     */
    public ClassLoader getClassLoader() {
	if( debug>0 ) log("getLoader " + ctx.getClassLoader() );
        return ctx.getClassLoader();
    }

    /**
     * What is the scratch directory we are generating code into?
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
	if( debug>0 )
	    log("getOutputDir " +  ctx.getWorkDir().getAbsolutePath() );
	return ctx.getWorkDir().getAbsolutePath();
    }

    public String resolveRelativeUri(String uri, String baseURI)
    {
	if( debug>0 ) log("resolveRelativeUri " + uri);
	if (uri.charAt(0) == '/') {
	    return uri;
        } else {
	    //String baseURI = jspFile.substring(0, jspFile.lastIndexOf('/'));
            return baseURI + '/' + uri;
        }
    }

    public java.io.InputStream getResourceAsStream(String res)
    {
	if( debug>0 ) log("getResourceAsStream " + res);
	return ((ServletContext)ctx.getFacade()).getResourceAsStream(res);
    }

    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path)
    {
	String rpath=((ServletContext)ctx.getFacade()).getRealPath( path );
	if( debug>0 ) log("getRealPath " + path + " = " + rpath );
	return rpath;
    }

    public void readWebXml( TagLibraries tli )
	throws IOException, JasperException
    {
	TagLibReader reader=new TagLibReader( this, tli );
	reader.readWebXml( tli );
    }

    /** Read a tag lib descriptor ( tld ). You can use the default
	implementation ( TagLibReader ).
    */
    public void readTLD( TagLibraries libs,
			 TagLibraryInfoImpl tl, String prefix, String uri,
			 String uriBase )
    	throws IOException, JasperException
    {
	TagLibReader reader=new TagLibReader( this, libs );
	reader.readTLD( tl, prefix, uri, uriBase );
    }

    /** Hook called after a JSP page has been detected and processed.
     *  The container may register the page as if it would be a web.xml
     *  servlet and avoid the future overhead of going through the
     *  jsp module.
     */
    public boolean addJspServlet( String uri, String servletName ) {
	return false;
    }

    public boolean addDependency( String servletName, String file ) {
	return false;
    }
    
    
    // -------------------- development tracing --------------------
    private int debug=0;
    public void log( String s ) {
	ctx.log("JasperEngineContext: "+ s);
    }
}
