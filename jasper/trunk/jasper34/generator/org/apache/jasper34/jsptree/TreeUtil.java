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
package org.apache.jasper34.jsptree;

import java.net.URL;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Enumeration;


import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.JasperException;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import org.apache.jasper34.jsptree.*;

/** 
 * This class has all the utility method(s).
 * Ideally should move all the bean containers here.
 *
 * @author Mandar Raje.
 * @author Rajiv Mordani.
 */
public class TreeUtil {
    // Parses the XML document contained in the InputStream.
    public static Document parseXMLDoc(InputStream in, String dtdResource, 
    					  String dtdId) throws JasperException 
    {
	return parseXMLDocJaxp(in, dtdResource, dtdId );
    }

    // Parses the XML document contained in the InputStream.
    public static Document parseXMLDocJaxp(InputStream in, String dtdResource, 
					   String dtdId)
	throws JasperException
    {
	try {
	    Document tld;
	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.
		newInstance();
	    docFactory.setValidating(true);
	    docFactory.setNamespaceAware(true);
	    DocumentBuilder builder = docFactory.newDocumentBuilder();
	    
	    /***
	     * These lines make sure that we have an internal catalog entry for
	     * the taglib.dtdfile; this is so that jasper can run standalone 
	     * without running out to the net to pick up the taglib.dtd file.
	     */
	    MyEntityResolver resolver =
		new MyEntityResolver(dtdId, dtdResource);
	    builder.setEntityResolver(resolver);
	    tld = builder.parse(in);
	    return tld;
	} catch( ParserConfigurationException ex ) {
            throw new JasperException(Constants.
				      getString("jsp.error.parse.error.in.TLD",
						new Object[] {
						    ex.getMessage()
						}));
	} catch ( SAXException sx ) {
            throw new JasperException(Constants.
				      getString("jsp.error.parse.error.in.TLD",
						new Object[] {
						    sx.getMessage()
						}));
        } catch (IOException io) {
            throw new JasperException(Constants.
				      getString("jsp.error.unable.to.open.TLD",
						new Object[] {
						    io.getMessage() }));
	}
    }

}

class MyEntityResolver implements EntityResolver {

    String dtdId;
    String dtdResource;
    
    public MyEntityResolver(String id, String resource) {
	this.dtdId = id;
	this.dtdResource = resource;
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException, IOException
    {
	//System.out.println ("publicId = " + publicId);
	//System.out.println ("systemId is " + systemId);
	//System.out.println ("resource is " + dtdResource);
	if (publicId.equals(dtdId)) {
	    InputStream input =
		this.getClass().getResourceAsStream(dtdResource);
	    InputSource isrc =
		new InputSource(input);
	    return isrc;
	}
	else {
	    //System.out.println ("returning null though dtdURL is " + dtdURL);
	    return null;
	}
    }
}




