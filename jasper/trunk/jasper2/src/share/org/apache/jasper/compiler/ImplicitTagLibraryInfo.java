/*
 * $Header$
 * $Revision$
 * $Date$
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
package org.apache.jasper.compiler;

import java.util.*;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;

/**
 * Class responsible for generating an implicit tag library containing tag
 * handlers corresponding to the tag files in "/WEB-INF/tags/" or a 
 * subdirectory of it.
 *
 * @author Jan Luehe
 */
public class ImplicitTagLibraryInfo extends TagLibraryInfo {

    private static final String WEB_INF_TAGS = "/WEB-INF/tags/";
    private static final String TLD_SUFFIX = ".tld";
    private static final String TAG_FILE_SUFFIX = ".tag";
    private static final String TAGS_SHORTNAME = "tags";
    private static final String TLIB_VERSION = "1.0";
    private static final String JSP_VERSION = "2.0";

    private String tldFile;

    /**
     * Constructor.
     */
    public ImplicitTagLibraryInfo(JspCompilationContext ctxt,
				  ParserController pc,
				  String prefix,
				  String tagdir,
				  ErrorDispatcher err) throws JasperException {
        super(prefix, tagdir);
	
	tlibversion = TLIB_VERSION;
	jspversion = JSP_VERSION;

	if (!tagdir.startsWith(WEB_INF_TAGS)) {
	    err.jspError("jsp.error.invalid.tagdir", tagdir);
	}
	
	// Determine the value of the <short-name> subelement of the
	// "imaginary" <taglib> element
	if (tagdir.equals(WEB_INF_TAGS)) {
	    shortname = TAGS_SHORTNAME;
	} else {
	    shortname = tagdir.substring(WEB_INF_TAGS.length());
	    shortname = shortname.replace('/', '-');
	}

	Set dirList = ctxt.getResourcePaths(tagdir);
	if (dirList != null) {
	    Vector vec = new Vector();
	    Iterator it = dirList.iterator();
	    while (it.hasNext()) {
		String path = (String) it.next();
		if (path.endsWith(TLD_SUFFIX)) {
		    tldFile = path;
		    break;
		} else if (path.endsWith(TAG_FILE_SUFFIX)) {
		    // use the filename of the tag file, without the .tag
		    // extension, as the <name> subelement of the "imaginary"
		    // <tag-file> element
		    String tagName = path.substring(path.lastIndexOf("/") + 1);
		    tagName = tagName.substring(0,
						tagName.lastIndexOf(TAG_FILE_SUFFIX));
		    TagInfo tagInfo = TagFileProcessor.parseTagFile(pc,
								    tagName,
								    path,
								    this); 
		    vec.addElement(new TagFileInfo(tagName, path, tagInfo));
		}
	    }

	    this.tagFiles = new TagFileInfo[vec.size()];
	    vec.copyInto(this.tagFiles);
	}
    }

    public static TagLibraryInfo getTabLibraryInfo(JspCompilationContext ctxt,
						   ParserController pc,
						   String prefix, 
						   String tagdir,
						   ErrorDispatcher err)
	    throws JasperException {

	TagLibraryInfo tagLibInfo = new ImplicitTagLibraryInfo(ctxt, pc,
							       prefix, tagdir,
							       err);
	if (((ImplicitTagLibraryInfo) tagLibInfo).tldFile != null) {
	    // tagdir contains TLD file
	    String[] location = new String[2];
	    location[0] = ((ImplicitTagLibraryInfo) tagLibInfo).tldFile;
	    tagLibInfo = new TagLibraryInfoImpl(ctxt, pc, prefix, tagdir,
						location, err);
	}
	
	return tagLibInfo;
    }
}
