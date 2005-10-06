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

import java.lang.reflect.Constructor;

import java.util.Hashtable;
import java.io.IOException;

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.Tag;


import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.JasperException;
import org.apache.jasper34.parser.*;

// XXX @deprecated Will be removed, the collection of TagLibrary will be
// part of JspPageInfo


/**
 * A container for all tag libraries that have been imported using
 * the taglib directive.
 *
 * One instance per web application.
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class TagLibraries {

    private Hashtable tagLibInfos;
    ContainerLiaison containerL;

    // used to keep web.xml taglib-locations
    private Hashtable locations=null;
    
    public TagLibraries(ContainerLiaison containerL)
    {
	this.containerL=containerL;
        this.tagLibInfos = new Hashtable();
    }

    /** Add a location mapping. The container may do so if it
	has parsed web.xml ( and wants to share this info with
	us ), so we'll not have to do that again.
    */
    public void addTaglibLocation( String uri, String loc ) {
	if(locations==null )
	    locations=new Hashtable();
	locations.put( uri, loc );
    }

    // Get the taglib uri from web.xml data
    public String findLocation( String uriIn )
	throws IOException, JasperException
    {
	if( locations==null )
	    containerL.readWebXml( this );
	
	// ignorecase or toLowerCase
	return (String)locations.get( uriIn );
    }


    
    /** Add a taglib prefix and the associated URI.
	This is the result of a taglib directive, we'll need to
	read the descriptor if not already there.

	@param uriBase the jsp page that loads the taglib, used to resolve
                       	relative uris.
    */
    public void addTagLibrary( String prefix, String uri, String uriBase )
	throws JasperException, IOException
    {
	if( tagLibInfos.get( prefix ) != null ) {
	    return;
	    // we already parsed this 
	}

	TagLibraryInfoImpl tl = new TagLibraryInfoImpl(prefix, uri);

	containerL.readTLD(  this, tl, prefix, uri, uriBase );


	addTagLibrary(prefix, tl);
    }
    
    public void addTagLibrary(String prefix, TagLibraryInfo tli) {
        tagLibInfos.put(prefix, tli);
    }
    
    public boolean isUserDefinedTag(String prefix, String shortTagName) 
        throws JasperException
    {
        TagLibraryInfo tli = (TagLibraryInfo) tagLibInfos.get(prefix);
        if (tli == null)
            return false;
        else if (tli.getTag(shortTagName) != null)
            return true;
        throw new JasperException(containerL.getString("jsp.error.bad_tag",
                                                      new Object[] {
                                                          shortTagName,
                                                          prefix
                                                      }
                                                      ));
    }
    
    public TagLibraryInfo getTagLibInfo(String prefix) {
        return (TagLibraryInfo) tagLibInfos.get(prefix);
    }

}

