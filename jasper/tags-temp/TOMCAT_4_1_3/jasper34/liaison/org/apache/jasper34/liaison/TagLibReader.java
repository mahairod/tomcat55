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

package org.apache.jasper34.liaison;

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

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.apache.jasper34.core.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.runtime.JasperException;
//import org.apache.jasper.runtime.JspLoader;

import org.apache.tomcat.util.log.*;

/**
 * Reader for TagLibInfo. One per context, will cache web.xml
 * reading. Container may pre-set web.xml data ( since it already
 * parsed the file ), if not we'll read it.
 * Container may also pre-load all the tag lib info and set it into
 * TagLibraries - in which case jasper will not do any additional
 * reading.
 *
 * For non-cooperating containers this class can do all that's needed.
 * Treat it as a black box, it should be rewriten using XmlMapper if
 * we want more.
 *
 * One TagLibReader per context, so we can cache web.xml and lib info
 * instead of reading it for each page.
 * 
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class TagLibReader {
    static private final String TLD = "META-INF/taglib.tld";
    static private final String WEBAPP_INF = "/WEB-INF/web.xml";

    ContainerLiaison containerL;
    TagLibraries libs;

    public TagLibReader(ContainerLiaison containerL, TagLibraries libs) {
	this.containerL=containerL;
	this.libs=libs;
    }
    
    public void readTLD(TagLibraryInfoImpl tli,
			   String prefix, String uriIn, String jspBase) 
        throws IOException, JasperException
    {
	tli.setURI( uriIn );

	String uri=libs.findLocation( uriIn );

        // Try to resolve URI relative to the current JSP page
        if (!uri.startsWith("/") && isRelativeURI(uri))
            uri = containerL.resolveRelativeUri(uri, jspBase);

	tli.setURI( uri ); // ?? as in the original code

	parseTLD( uri, tli );
	
    }

    private void parseTLD( String uri, TagLibraryInfoImpl tli )
	throws IOException, JasperException
    {
	InputStream in = null;
        if (!uri.endsWith("jar")) {
	    in = getResourceAsStream(uri);
	    
	    if (in == null)
		throw new JasperException(containerL
					  .getString("jsp.error.tld_not_found",
						     new Object[] {uri}));
	    // Now parse the tld.
	    parseTLD(in, tli);
	}
	
	//	Hashtable jarEntries; // XXX doesn't seem to be used
        ZipInputStream zin;
        URL url = null;
        boolean relativeURL = false;

	// FIXME Take this stuff out when taglib changes are thoroughly tested.
        // 2000.11.15 commented out the 'copy to work dir' section,
        // which I believe is what this FIXME comment referred to. (pierred)
	if (uri.endsWith("jar")) {
	    
	    if (!isRelativeURI(uri)) {
		url = new URL(uri);
		in = url.openStream();
	    } else {
		relativeURL = true;
		in = getResourceAsStream(uri);
	    }
	    
	    zin = new ZipInputStream(in);
	    
	    //	    this.jarEntries = new Hashtable();
	    //this.containerL = containerL;
	    
            /* NOT COMPILED
	    // First copy this file into our work directory! 
	    {
		File jspFile = new File(containerL.getJspFile());
                String parent = jspFile.getParent();
                String jarFileName = containerL.getOutputDir();
                if (parent != null) {
                   jarFileName = jarFileName + File.separatorChar +
                       parent;
                }
                File jspDir = new File(jarFileName);
		jspDir.mkdirs();
	    
		if (relativeURL)
		    jarFileName = jarFileName+File.separatorChar+new File(uri).getName();
		else                    
		    jarFileName = jarFileName+File.separatorChar+
			new File(url.getFile()).getName();
	    
		containerL.message("jsp.message.copyinguri", 
	                          new Object[] { uri, jarFileName },
				  Log.DEBUG);
	    
		if (relativeURL)
		    copy(getResourceAsStream(uri),
			 jarFileName);
		else
		    copy(url.openStream(), jarFileName);
	    
	        containerL.addJar(jarFileName);
	    }
            */ // END NOT COMPILED
	    boolean tldFound = false;
	    ZipEntry entry;
	    while ((entry = zin.getNextEntry()) != null) {
		if (entry.getName().equals(TLD)) {
		    /*******
		     * This hack is necessary because XML reads until the end 
		     * of an inputstream -- does not use available()
		     * -- and closes the inputstream when it can't
		     * read no more.
		     */
		    
		    // BEGIN HACK
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    int b;
		    while (zin.available() != 0) {
			b = zin.read();
			if (b == -1)
			    break;
			baos.write(b);
		    }

		    baos.close();
		    ByteArrayInputStream bais 
			= new ByteArrayInputStream(baos.toByteArray());
		    // END HACK
		    tldFound = true;
		    parseTLD(bais, tli);
		} else {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    int b;
		    while (zin.available() != 0) {
			b = zin.read();
			if (b == -1)
			    break;
			baos.write(b);
		    }
		    baos.close();
		    //jarEntries.put(entry.getName(), baos.toByteArray());
		}
		zin.closeEntry();
	    }
	    
	    if (!tldFound)
		throw new JasperException(containerL.getString("jsp.error.tld_not_found",
							      new Object[] {
		    TLD
			}
							      ));
	} // Take this out (END of if(endsWith("jar")))
    }
	

    
    private void parseTLD(InputStream in, TagLibraryInfoImpl tli) 
        throws JasperException
    {
	Document tld;
	tld = parseXMLDoc(containerL, in, Constants.TAGLIB_DTD_RESOURCE,
			  Constants.TAGLIB_DTD_PUBLIC_ID);
	
        NodeList list = tld.getElementsByTagName("taglib");

        if (list.getLength() != 1)
            throw new JasperException(containerL.getString("jsp.error.more.than.one.taglib"));

        Element elem = (Element) list.item(0);
        list = elem.getChildNodes();

        for(int i = 0; i < list.getLength(); i++) {
	    Node tmp = list.item(i);
	    if (! (tmp instanceof Element)) continue;
            Element e = (Element) tmp;
            String tname = e.getTagName();
            if (tname.equals("tlibversion")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tli.setVersion(  t.getData().trim() );
            } else if (tname.equals("jspversion")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tli.setRequiredVersion(t.getData().trim());
		// jspversion 
            } else if (tname.equals("shortname")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tli.setShortName( t.getData().trim());
            } else if (tname.equals("uri")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tli.setReliableURN( t.getData().trim());
            } else if (tname.equals("info")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tli.setInfoString(t.getData().trim());
            } else if (tname.equals("tag")) {
                tli.addTagInfo( createTagInfo(e, tli));
	    } else
                containerL.message("jsp.warning.unknown.element.in.TLD", 
                                  new Object[] {
                                      e.getTagName()
                                  },
                                  Log.WARNING
                                  );
        }
    }

    private TagInfoImpl createTagInfo(Element elem, TagLibraryInfoImpl tli)
	throws JasperException
    {
        String name = null, tagclass = null, teiclass = null;
        String bodycontent = "JSP"; // Default body content is JSP
	String info = null;
        
        Vector attributeVector = new Vector();
        NodeList list = elem.getChildNodes();
        for(int i = 0; i < list.getLength(); i++) {
            Node tmp  =  list.item(i);
	    if (! (tmp instanceof Element)) continue;
	    Element e = (Element) tmp;
            String tname = e.getTagName();
            if (tname.equals("name")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    name = t.getData().trim();
            } else if (tname.equals("tagclass")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    tagclass = t.getData().trim();
            } else if (tname.equals("teiclass")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    teiclass = t.getData().trim();
            } else if (tname.equals("bodycontent")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    bodycontent = t.getData().trim();
            } else if (tname.equals("info")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    info = t.getData().trim();
            } else if (tname.equals("attribute"))
                attributeVector.addElement(createAttribute(e));
            else 
                containerL.message("jsp.warning.unknown.element.in.tag", 
                                  new Object[] {
                                      e.getTagName()
                                  },
                                  Log.WARNING
                                  );
        }
	TagAttributeInfo[] tagAttributeInfo 
            = new TagAttributeInfo[attributeVector.size()];
	attributeVector.copyInto (tagAttributeInfo);

        TagExtraInfo tei = null;

        if (teiclass != null && !teiclass.equals(""))
            try {
                Class teiClass = containerL.getClassLoader().loadClass(teiclass);
                tei = (TagExtraInfo) teiClass.newInstance();
	    } catch (ClassNotFoundException cex) {
                containerL.message("jsp.warning.teiclass.is.null",
                                  new Object[] {
                                      teiclass, cex.getMessage()
                                  },
                                  Log.WARNING
                                  );
            } catch (IllegalAccessException iae) {
                containerL.message("jsp.warning.teiclass.is.null",
                                  new Object[] {
                                      teiclass, iae.getMessage()
                                  },
                                  Log.WARNING
                                  );
            } catch (InstantiationException ie) {
                containerL.message("jsp.warning.teiclass.is.null",
                                  new Object[] {
                                      teiclass, ie.getMessage()
                                  },
                                  Log.WARNING
                                  );
            }

        TagInfoImpl taginfo = new TagInfoImpl(containerL,
					      name, tagclass, bodycontent,
					      info, tli, 
					      tei,
					      tagAttributeInfo);
        return taginfo;
    }

    TagAttributeInfo createAttribute(Element elem) {
        String name = null;
        boolean required = false, rtexprvalue = false, reqTime = false;
        String type = null;
        
        NodeList list = elem.getChildNodes();
        for(int i = 0; i < list.getLength(); i++) {
            Node tmp  = list.item(i);
	    if (! (tmp instanceof Element)) continue;
	    Element e = (Element) tmp;
            String tname = e.getTagName();
            if (tname.equals("name"))  {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    name = t.getData().trim();
            } else if (tname.equals("required"))  {
                Text t = (Text) e.getFirstChild();
                if (t != null) {
                    required = Boolean.valueOf(t.getData().trim()).booleanValue();
                    if( t.getData().equalsIgnoreCase("yes") )
                        required = true;
                }
            } else if (tname.equals("rtexprvalue")) {
                Text t = (Text) e.getFirstChild();
                if (t != null) {
                    rtexprvalue = Boolean.valueOf(t.getData().trim()).booleanValue();
                    if( t.getData().equalsIgnoreCase("yes") )
                        rtexprvalue = true;
                }
            } else if (tname.equals("type")) {
                Text t = (Text) e.getFirstChild();
                if (t != null)
                    type = t.getData().trim();
            } else 
                containerL.message("jsp.warning.unknown.element.in.attribute", 
                                  new Object[] {
                                      e.getTagName()
                                  },
                                  Log.WARNING
                                  );
        }
        
	//     return new TagAttributeInfo(name, required, rtexprvalue, type);
        return new TagAttributeInfo(name, required, type, rtexprvalue);
    }

    // XXX FIXME
    // resolveRelativeUri and/or getResourceAsStream don't seem to properly
    // handle relative paths when dealing when home and getDocBase are set
    // the following is a workaround until these problems are resolved.
    private InputStream getResourceAsStream(String uri) 
        throws FileNotFoundException 
    {
        if (uri.indexOf(":") > 0) {
            // may be fully qualified (Windows) or may be a URL.  Let
            // getResourceAsStream deal with it.
            return containerL.getResourceAsStream(uri);
        } else {
            // assume it translates to a real file, and use getRealPath
            String real = containerL.getRealPath(uri);
            return (real == null) ? null : new FileInputStream(real);
        }
    }

    
    /* Unused
       static void copy(InputStream in, String fileName) 
        throws IOException, FileNotFoundException 
    {
        byte[] buf = new byte[1024];

        FileOutputStream out = new FileOutputStream(fileName);
        int nRead;
        while ((nRead = in.read(buf, 0, buf.length)) != -1)
            out.write(buf, 0, nRead);
    }

    */
    /** Returns true if the given URI is relative in this web application,
     *  false if it is an internet URI.
     */
    private boolean isRelativeURI(String uri) {
        return (uri.indexOf(':') == -1);
    }
    

    // -------------------- Utils from JspUtil/TreeUtil --------------------
    
    // Parses the XML document contained in the InputStream.
    public static Document parseXMLDoc(ContainerLiaison containerL,
				       InputStream in, String dtdResource, 
				       String dtdId)
	throws JasperException 
    {
	return parseXMLDocJaxp(containerL, in, dtdResource, dtdId );
    }

    // Parses the XML document contained in the InputStream.
    public static Document parseXMLDocJaxp(ContainerLiaison containerL,
					   InputStream in, String dtdResource, 
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
            throw new JasperException(containerL.
				      getString("jsp.error.parse.error.in.TLD",
						new Object[] {
						    ex.getMessage()
						}));
	} catch ( SAXException sx ) {
            throw new JasperException(containerL.
				      getString("jsp.error.parse.error.in.TLD",
						new Object[] {
						    sx.getMessage()
						}));
        } catch (IOException io) {
            throw new JasperException(containerL.
				      getString("jsp.error.unable.to.open.TLD",
						new Object[] {
						    io.getMessage() }));
	}
    }

    // -------------------- Web.xml stuff.
    // XXX Shuld be separated, more API to allow containers to pass info

    public void readWebXml( TagLibraries libs )
	throws IOException, JasperException
    {
        // Parse web.xml.
        InputStream is = getResourceAsStream(WEBAPP_INF);

        if (is != null) {
            Document webtld =
                parseXMLDoc(containerL, is,Constants.WEBAPP_DTD_RESOURCE,
			    Constants.WEBAPP_DTD_PUBLIC_ID);
	    
            NodeList nList =  webtld.getElementsByTagName("taglib");

            if (nList.getLength() != 0) {
                for(int i = 0; i < nList.getLength(); i++) {
                    String tagLoc = null;
                    boolean match = false;
                    Element e =  (Element) nList.item(i);

                    // Assume only one entry for location and uri.
                    NodeList uriList = e.getElementsByTagName("taglib-uri");
                    Element uriElem = (Element) uriList.item(0);
                    Text t = (Text) uriElem.getFirstChild();

                    if (t != null) {
                        String tmpUri = t.getData();
                        if (tmpUri != null) {
                            tmpUri = tmpUri.trim();
			    //if (tmpUri.equals(uriIn)) {
			    //match = true;
			    NodeList locList = e.getElementsByTagName
				("taglib-location");
			    Element locElem = (Element) locList.item(0);
			    Text tl = (Text) locElem.getFirstChild();
			    if (tl != null) {
				tagLoc = tl.getData();
				if (tagLoc != null)
				    tagLoc = tagLoc.trim();
			    }
			    if (!tagLoc.startsWith("/")
				&& isRelativeURI(tagLoc))
				tagLoc = "/WEB-INF/"+tagLoc;
			    if( tagLoc!=null )
				libs.addTaglibLocation( tmpUri, tagLoc );
                        }
                    }
                }
            }
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
