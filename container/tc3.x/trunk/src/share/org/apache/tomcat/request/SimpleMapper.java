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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.util.*;
import java.util.Hashtable;

public class SimpleMapper  implements  RequestInterceptor {
    int debug=0;
    
    public SimpleMapper() {
    }

    public void setDebug( int level ) {
	debug=level;
    }

    void log( String msg ) {
	System.out.println("SimpleMapper: " + msg );
    }
    
    public int handleRequest(Request req) {
	Context context=req.getContext();
	String path=req.getLookupPath();
        ServletWrapper wrapper = null;

	if(debug>0) log( "Mapping: " + req );
	//	/*XXX*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}

	// try an exact match
        wrapper = getPathMatch(context, path, req);

	// try a prefix match
	if( wrapper == null ) 
	    wrapper = getPrefixMatch(context, path, req);

	// try an extension match
	if (wrapper == null) 
	    wrapper = getExtensionMatch(context, path, req);

	// set default wrapper, return
	if (wrapper == null) {
	    wrapper = context.getDefaultServlet();
	    req.setWrapper( wrapper );
	    req.setServletPath( "" );
	    req.setPathInfo( path);
	    if(debug>0) log("Default mapper " + "\n    " + req);
	    return OK;
	} 

	req.setWrapper( wrapper );

	if(debug>0) log("Found wrapper using getMapPath " + "\n    " + req);

	return OK;
    }



    /** Get an exact match ( /catalog ) - rule 1 in 10.1
     */
    private ServletWrapper getPathMatch(Context context, String path, Request req) {
        ServletWrapper wrapper = null;
	wrapper = (ServletWrapper)context.getPathMap().get(path);

	if (wrapper != null) {
	    req.setServletPath( path );
	    // No path info - it's an exact match
	    if(debug>1) log("path match " + path );
	}
        return wrapper;
    }


    /** Match a prefix rule - /foo/bar/index.html/abc
     */
    private ServletWrapper getPrefixMatch(Context context, String path, Request req) {
	ServletWrapper wrapper = null;
        String s = path;

	// /baz/== /baz ==/baz/* 
	if( s.endsWith( "/" ))
	    s=removeLast(s);
	
	while (s.length() > 0) {
	    // XXX we can remove /* in prefix map when we add it, so no need
	    // for another string creation
	    wrapper = (ServletWrapper)context.getPrefixMap().get(s + "/*" );
	    
	    if (wrapper == null)
		s=removeLast( s );
	    else
		break;
	}
		
	// Set servlet path and path info
	if( wrapper != null ) {
	    // Found a match !
	    req.setServletPath( s );
	    String pathI = path.substring(s.length(), path.length());
	    if( ! "".equals(pathI) ) 
		req.setPathInfo(pathI);
	    if(debug>0) log("prefix match " + path );
	}
	return wrapper;
    }

    // It looks like it's broken: try /foo/bar.jsp/test/a.baz -> will not match it
    // as baz, but neither as .jsp, which is wrong.
    // XXX Fix this code - I don't think evolution will work in this class.
    private ServletWrapper getExtensionMatch(Context context, String path, Request req) {
	String extension=getExtension( path );
	if( extension == null ) return null;

	// XXX need to store the extensions without *, to avoid extra
	// string creation
	ServletWrapper wrapper= (ServletWrapper)context.getExtensionMap().get("*" + extension);

	if (wrapper == null)
	    return null;

	// fix paths
	// /a/b/c.jsp/d/e
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf("/");
	if (j > i) {
	    int k = i + path.substring(i).indexOf("/");
	    String s = path.substring(0, k);
	    req.setServletPath( s );

	    s = path.substring(k);
	    req.setPathInfo(  s  );
	} else {
	    req.setServletPath( path );
	}
		
	if(debug>0) log("extension match " + path );
	return wrapper; 
    }

    // -------------------- String utilities --------------------

    private String getExtension( String path ) {
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf("/");

	if (i > -1) {
	    String extension = path.substring(i);
	    int k = extension.indexOf("/");
	    if( k>=0 )
		extension = extension.substring(0, k);
	    return extension;
	}
	return null;
    }
    
    private String removeLast( String s) {
	int i = s.lastIndexOf("/");
	
	if (i > 0) {
	    s = s.substring(0, i);
	} else if (i == 0 && ! s.equals("/")) {
	    s = "/";
	} else {
	    s = "";
	}
	return s;
    }

    String getFirst( String path ) {
	if (path.startsWith("/")) 
	    path = path.substring(1);
	
	int i = path.indexOf("/");
	if (i > -1) {
	    path = path.substring(0, i);
	}

	return  "/" + path;
    }

}
    
