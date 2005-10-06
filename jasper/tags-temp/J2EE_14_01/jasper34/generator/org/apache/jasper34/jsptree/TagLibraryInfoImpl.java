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

package org.apache.jasper34.jsptree;

import java.net.URL;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.jasper34.core.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.JasperException;
//import org.apache.jasper.runtime.JspLoader;

import org.apache.tomcat.util.log.*;

/**
 * Implementation of the TagLibraryInfo class from the JSP spec. 
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class TagLibraryInfoImpl extends TagLibraryInfo {

    public TagLibraryInfoImpl(String prefix, String uriIn) 
        throws IOException, JasperException
    {
        super(prefix, uriIn);
    }

    // -------------------- Setter methods --------------------
    // Used by the tag reader to set the fields
    // May be used by XmlMapper, or container-specific code

    public void setURI( String uri ) {
	this.uri=uri;
    }

    public void setPrefixString( String p ) {
	prefix=p;
    }

    public void setShortName( String s ) {
	shortname=s;
    }

    public void setReliableURN( String s ) {
	urn=s;
    }

    public void setInfoString( String s ) {
	info=s;
    }

    public void setRequiredVersion( String s ) {
	jspversion=s;
    }

    // We can add only TagInfoImpl, we need the extra fields
    // for correct processing. You can't mix other implementations,
    // those things work togheter
    public void addTagInfo( TagInfoImpl ti ) {
	tagsH.put( ti.getTagName(), ti );
	tags=null; // clear previous cached values
    }

    // override the base method in javax.servlet.jsp
    public TagInfo getTag( String shortname ) {
	return (TagInfo)tagsH.get( shortname );
    }
    
    // overrid base method 
    public TagInfo[] getTagInfo() {
	if( tags==null ) {
	    int size=tagsH.size();
	    if( size==0 ) return null;
	    tags=new TagInfo[ size ];
	    Enumeration els=tagsH.elements();
	    for( int i=0; els.hasMoreElements(); i++ ) {
		tags[i]=(TagInfo)els.nextElement();
	    }
	}
	return tags;
    }

    public void setVersion( String s ) {
	tlibversion=s;
    }
    
    // Special internal representation for tags[]
    // Use a hashtable instead of linear search
    // 

    Hashtable tagsH=new Hashtable();
    
    
    // -------------------- Debug info --------------------
    
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        print("tlibversion", tlibversion, out);
        print("jspversion", jspversion, out);
        print("shortname", shortname, out);
        print("urn", urn, out);
        print("info", info, out);
        print("uri", uri, out);

        for(int i = 0; i < tags.length; i++)
            out.println(tags[i].toString());
        
        return sw.toString();
    }
    
    private final void print(String name, String value, PrintWriter w) {
        if (value != null) {
            w.print(name+" = {\n\t");
            w.print(value);
            w.print("\n}\n");
        }
    }


}
