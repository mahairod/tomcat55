/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jasper.compiler;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;

import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.logging.Logger;

/**
 * SAX Parser to handle XJsp syntax.
 * (JSP page as XML document).
 *
 * Any SAX2.0 parser should do.
 *
 * @author Pierre Delisle
 */
public class ParserXJspSax {

    /* 
     * InputSource for XML document
     */
    private InputSource is;

    /*
     * File path that's used in parsing errors
     */
    private String filePath;

    /*
     * The 'parsing event handler' that does the real handling
     * of parse events (shared by both jsp and xjsp parsers)
     */
    private ParseEventListener jspHandler;

    /*
     * validation mode
     */
    static final private boolean validate = false;

    /*
     * Couple possible names for lexical reporting property name :-(
     */
    static final String[] lexicalHandlerPropNames = {
	"http://xml.org/sax/handlers/LexicalHandler",
	"http://xml.org/sax/properties/lexical-handler",
    };

    //*********************************************************************
    // Constructor

    public ParserXJspSax(String filePath,
			 InputStreamReader reader, 
			 ParseEventListener jspHandler)
    {
	this.filePath = filePath;
	this.is = new InputSource(reader);
	this.jspHandler = jspHandler;
    }
    
    //*********************************************************************
    // Parse
    
    public void parse() throws JasperException {
	//System.out.println("in ParserXJspSax");
        try {
	    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	    SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();
            ParserXJspSaxHandler handler =
                new ParserXJspSaxHandler(filePath, jspHandler);
	    
            parser.setContentHandler(handler);
	    parser.setEntityResolver(handler);
	    parser.setDTDHandler(handler);
	    parser.setErrorHandler(handler);

	    // set properties: lexical handler
	    for (int i=0; i<lexicalHandlerPropNames.length; i++) {
		if (setSaxProperty(parser, handler, 
				   lexicalHandlerPropNames[i])) break;
	    }
	    
            // Set features: validation and namespaces
            try {
                parser.setFeature(
                    "http://xml.org/sax/features/validation", validate);
                parser.setFeature(
                    "http://xml.org/sax/features/namespaces", false);
                parser.setFeature(
                    "http://xml.org/sax/features/namespace-prefixes", true);
            } catch (SAXNotSupportedException ex) {
		throw new JasperException(
		    Constants.getString("jsp.parser.sax.featurenotsupported",
					new Object[] {ex.getMessage()}));
            } catch (SAXNotRecognizedException ex) {
		throw new JasperException(
		    Constants.getString("jsp.parser.sax.featurenotrecognized",
					new Object[] {ex.getMessage()}));
	    }

            parser.parse(is);

        } catch (IOException ex) {
	    throw new JasperException(ex);
	} catch (ParserConfigurationException ex) {
	    throw new ParseException(ex.getMessage());
        } catch (SAXParseException ex) {
	    Mark mark = 
		new Mark(filePath, ex.getLineNumber(), ex.getColumnNumber());
	    String reason;
	    if (ex.getException() != null) {
		reason = ex.getException().getMessage();
	    } else {
		reason = ex.getMessage();
	    }
	    throw new ParseException(mark, reason);
        } catch (SAXException ex) {
	    Exception ex2 = ex;
	    if (ex.getException() != null) ex2 = ex.getException();
	    throw new JasperException(ex2);
        }
    }

    //*********************************************************************
    // Utility methods

    private boolean setSaxProperty(XMLReader parser, 
				   DefaultHandler handler,
				   String propName) 
    {
	try {
	    parser.setProperty(propName, handler);
	    return true;
	} catch (SAXNotSupportedException ex) {
	    Constants.message("jsp.parser.sax.propertynotsupported",
			      new Object[] {ex.getMessage()},
			      Logger.WARNING);
	    return false;
	} catch (SAXNotRecognizedException ex) {
	    Constants.message("jsp.parser.sax.propertynotrecognized",
			      new Object[] {ex.getMessage()},
			      Logger.WARNING);
	    return false;
	}
    }

    /* NOT COMPILED
    private void printException(SAXParseException ex) {
	System.out.println("SAXParseException");
	System.out.println("Public ID: " + ex.getPublicId());
	System.out.println("System ID: " + ex.getSystemId());
	System.out.println("line " + ex.getLineNumber() + 
			   ", col " + ex.getColumnNumber());
	ex.printStackTrace(System.out);
    }

    void p(String s) {
	System.out.println("[ParserXJspSax] " + s);
    }
    */
}

