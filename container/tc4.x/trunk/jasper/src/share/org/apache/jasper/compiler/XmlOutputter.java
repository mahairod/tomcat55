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

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import javax.servlet.jsp.tagext.PageInfo;

/**
 * Class responsible for generating the XML stream representing
 * the JSP translation unit being compiled.
 *
 * @author Pierre Delisle
 */
public class XmlOutputter {

    /* 
     * XML data is appended to the stream as we parse and process
     * the JSP translation unit
     */
    private StringBuffer sb;

    /* 
     * The root attributes of all the <jsp:root> tags encountered
     * in the translation unit 
     */
    private Hashtable rootAttrs;
    
    //*********************************************************************
    // Constructor

    XmlOutputter() {
	sb = new StringBuffer();
	rootAttrs = new Hashtable();
    }

    //*********************************************************************
    // update methods to the XML stream

    /**
     * A translation unit (JSP source file and any files included via 
     * the include directive) may encounter multiple <jsp:root>
     * tags. This method cumulates all attributes for the
     * <jsp:root> tag.
     */
    void addRootAttrs(Hashtable attrs) {
        Enumeration enum = attrs.keys();
        while (enum.hasMoreElements()) {
            Object name = enum.nextElement();
            Object value = attrs.get(name);
            rootAttrs.put(name, value);
        }
    }
    
    /**
     * Append the cdata to the XML stream.
     */
    void append(char[] text) {
        sb.append("<![CDATA[\n");
        sb.append(text);
        sb.append("]]>\n");
    }
    
    /**
     * Append the start tag along with its attributes to the
     * XML stream.
     */
    void append(String tag, Hashtable attrs) {
        append(tag, attrs, sb);
    }
    
    /**
     * Append the start tag along with its attributes to the
     * specific XML stream. 
     * [StringBuffer is an argument so we can reuse the method
     * to generate the "header" in a different stream. The header
     * can only be generated once we've processed all parts
     * of the translation unit]
     */
    void append(String tag, Hashtable attrs, StringBuffer buff) {
        buff.append("<").append(tag);
        if (attrs == null) {
            buff.append(">");
        } else {
            buff.append("\n");
            Enumeration enum = attrs.keys();
            while (enum.hasMoreElements()) {
                String name = (String)enum.nextElement();
                String value = (String)attrs.get(name);
                buff.append("  ").append(name).append("=\"");
		buff.append(value).append("\"\n");
            }
            buff.append(">\n");
        }
    }

    /**
     * Append the start tag along with its attributes and body
     * to the XML stream.
     */
    void append(String tag, Hashtable attrs, char[] text) {
        append(tag, attrs);
        append(text);
        sb.append("</").append(tag).append(">\n");
    }
    
    /**
     * Append the end tag to the xml stream.
     */
    void append(String tag) {
        sb.append("</").append(tag).append(">");
    }

    //*********************************************************************
    // Outputting the XML stream

    private static final String PROLOG =
	"<!DOCTYPE jsp:root\n  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaServer Pages Version 1.1//EN\"\n  \"http://java.sun.com/products/jsp/dtd/jspcore_1_2.dtd\">\n";

    PageInfo getPageInfo() {
	StringBuffer buff = new StringBuffer();

        buff.append(PROLOG);
        append("jsp:root", rootAttrs, buff);
	buff.append(sb.toString());
	InputStream is = 
	    new ByteArrayInputStream(buff.toString().getBytes());
	PageInfo pageInfo = new PageInfoImpl(is);
        return pageInfo;
    }
}    
