/*
 * $Header$
 * $Revision$
 * $Date$
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

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import javax.servlet.jsp.tagext.*;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;

/**
 * Implementation of the TagLibraryInfo class from the JSP spec. 
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 */
class TagLibraryInfoImpl extends TagLibraryInfo {

    private Hashtable jarEntries;
    private JspCompilationContext ctxt;
    private ErrorDispatcher err;
    private ParserController parserController;

    private final void print(String name, String value, PrintWriter w) {
        if (value != null) {
            w.print(name+" = {\n\t");
            w.print(value);
            w.print("\n}\n");
        }
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        print("tlibversion", tlibversion, out);
        print("jspversion", jspversion, out);
        print("shortname", shortname, out);
        print("urn", urn, out);
        print("info", info, out);
        print("uri", uri, out);
        print("tagLibraryValidator", tagLibraryValidator.toString(), out);

        for(int i = 0; i < tags.length; i++)
            out.println(tags[i].toString());
        
        for(int i = 0; i < tagFiles.length; i++)
            out.println(tagFiles[i].toString());
        
        for(int i = 0; i < functions.length; i++)
            out.println(functions[i].toString());
        
        return sw.toString();
    }
    
    // XXX FIXME
    // resolveRelativeUri and/or getResourceAsStream don't seem to properly
    // handle relative paths when dealing when home and getDocBase are set
    // the following is a workaround until these problems are resolved.
    private InputStream getResourceAsStream(String uri) 
        throws FileNotFoundException 
    {
        try {
            // see if file exists on the filesystem first
            String real = ctxt.getRealPath(uri);
            if (real == null) {
                return ctxt.getResourceAsStream(uri);
            } else {
                return new FileInputStream(real);
            }
        }
        catch (FileNotFoundException ex) {
            // if file not found on filesystem, get the resource through
            // the context
            return ctxt.getResourceAsStream(uri);
        }
       
    }

    /**
     * Constructor.
     */
    public TagLibraryInfoImpl(JspCompilationContext ctxt,
			      ParserController pc,
			      String prefix, 
			      String uriIn,
			      String[] location,
			      ErrorDispatcher err) throws JasperException {
        super(prefix, uriIn);

	this.ctxt = ctxt;
	this.parserController = pc;
	this.err = err;
        ZipInputStream zin;
        InputStream in = null;
        URL url = null;
        boolean relativeURL = false;

	if (location == null) {
	    // The URI points to the TLD itself or to a jar
	    // file where the TLD is located
	    int uriType = TldLocationsCache.uriType(uri);
	    if (uriType == TldLocationsCache.ABS_URI) {
		err.jspError("jsp.error.taglibDirective.absUriCannotBeResolved",
			     uri);
	    } else if (uriType == TldLocationsCache.NOROOT_REL_URI) {
		uri = ctxt.resolveRelativeUri(uri);
	    }
	    location = new String[2];
	    location[0] = uri;
	    if (uri.endsWith("jar")) {
		location[1] = "META-INF/taglib.tld";
	    }
	}

        if (!location[0].endsWith("jar")) {
	    // Location points to TLD file
	    try {
		in = getResourceAsStream(location[0]);
		if (in == null) {
		    throw new FileNotFoundException(location[0]);
		}
	    } catch (FileNotFoundException ex) {
		err.jspError("jsp.error.file.not.found", location[0]);
	    }
	    // Now parse the tld.
	    parseTLD(ctxt, location[0], in, null);
	    // Add the TLD to dependency list
	    PageInfo pageInfo = ctxt.createCompiler().getPageInfo();
	    if (pageInfo != null) {
		pageInfo.addDependant(location[0]);
	    }
	} else {
	    // Tag library is packaged in JAR file
	    JarFile jarFile = null;
	    ZipEntry jarEntry = null;
	    InputStream stream = null;
	    try {
                String path = location[0] ;
                if(ctxt.getClassLoader() != null &&
                   URLClassLoader.class.equals(ctxt.getClassLoader().getClass())
                       && path.startsWith("/"))
                   path = path.substring(1,path.length()) ;
                url = ctxt.getResource(path);
                if (url == null) return;
		url = new URL("jar:" + url.toString() + "!/");
		JarURLConnection conn = (JarURLConnection)
		    url.openConnection();
		conn.connect(); //@@@ necessary???
		jarFile = conn.getJarFile();
		jarEntry = jarFile.getEntry(location[1]);
		stream = jarFile.getInputStream(jarEntry);
		parseTLD(ctxt, location[0], stream, jarFile);
		// FIXME @@@
		// -- it seems that the JarURLConnection class caches JarFile 
		// objects for particular URLs, and there is no way to get 
		// it to release the cached entry, so
		// there's no way to redeploy from the same JAR file.  Wierd.
	    } catch (Exception ex) {
		if (stream != null) {
		    try {
			stream.close();
		    } catch (Throwable t) {}
		}
		if (jarFile != null) {
		    try {
			jarFile.close();
		    } catch (Throwable t) {}
		}
		throw new JasperException(ex);
	    }
	}
    }
    
    private void parseTLD(JspCompilationContext ctxt,
                          String uri, InputStream in, JarFile jarFile) 
        throws JasperException
    {
        Vector tagVector = new Vector();
        Vector tagFileVector = new Vector();
        Hashtable functionTable = new Hashtable();

        // Create an iterator over the child elements of our <taglib> element
        ParserUtils pu = new ParserUtils();
        TreeNode tld = pu.parseXMLDocument(uri, in);
        Iterator list = tld.findChildren();

        // Process each child element of our <taglib> element
        while (list.hasNext()) {

            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();

            if ("tlibversion".equals(tname)                    // JSP 1.1
		        || "tlib-version".equals(tname)) {     // JSP 1.2
                this.tlibversion = element.getBody();
            } else if ("jspversion".equals(tname)
		        || "jsp-version".equals(tname)) {
                this.jspversion = element.getBody();
            } else if ("shortname".equals(tname) ||
                     "short-name".equals(tname))
                this.shortname = element.getBody();
            else if ("uri".equals(tname))
                this.urn = element.getBody();
            else if ("info".equals(tname) ||
                     "description".equals(tname))
                this.info = element.getBody();
            else if ("validator".equals(tname))
                this.tagLibraryValidator = createValidator(element);
            else if ("tag".equals(tname))
                tagVector.addElement(createTagInfo(element));
            else if ("tag-file".equals(tname))
                tagFileVector.addElement(createTagFileInfo(element, uri,
							   jarFile));
            else if ("function".equals(tname)) {         // JSP2.0
		FunctionInfo funcInfo = createFunctionInfo(element);
		String funcName = funcInfo.getName();
		if (functionTable.containsKey(funcName)) {
		    err.jspError("jsp.error.tld.fn.duplicate.name",
				 funcName, uri);

		}
                functionTable.put(funcName, funcInfo);
            } else if ("display-name".equals(tname) ||    // Ignored elements
                     "small-icon".equals(tname) ||
                     "large-icon".equals(tname) ||
                     "listener".equals(tname)) {
                ;
	    } else if ("taglib-extension".equals(tname)) {
		// Recognized but ignored
            } else {
                Constants.message("jsp.warning.unknown.element.in.TLD", 
                                  new Object[] {tname},
                                  Logger.WARNING
                                  );
            }

        }

	if (tlibversion == null) {
	    err.jspError("jsp.error.tld.mandatory.element.missing", 
			 "tlib-version");
	}
	if (jspversion == null) {
	    err.jspError("jsp.error.tld.mandatory.element.missing",
			 "jsp-version");
	}

        this.tags = new TagInfo[tagVector.size()];
        tagVector.copyInto (this.tags);

        this.tagFiles = new TagFileInfo[tagFileVector.size()];
        tagFileVector.copyInto (this.tagFiles);

        this.functions = new FunctionInfo[functionTable.size()];
	int i=0;
        Enumeration enum = functionTable.elements();
	while (enum.hasMoreElements()) {
	    this.functions[i++] = (FunctionInfo) enum.nextElement();
	}
    }

    private TagInfo createTagInfo(TreeNode elem) throws JasperException {
        String name = null;
	String tagclass = null;
	String teiclass = null;
        String bodycontent = "JSP"; // Default body content is JSP
	String info = null;
	String displayName = null;
	String smallIcon = null;
	String largeIcon = null;
        boolean dynamicAttributes = false;
        
        Vector attributeVector = new Vector();
        Vector variableVector = new Vector();
        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();

            if ("name".equals(tname)) {
                name = element.getBody();
            } else if ("tagclass".equals(tname) ||
                     "tag-class".equals(tname)) {
                tagclass = element.getBody();
            } else if ("teiclass".equals(tname) ||
                     "tei-class".equals(tname)) {
                teiclass = element.getBody();
            } else if ("bodycontent".equals(tname) ||
                     "body-content".equals(tname)) {
                bodycontent = element.getBody();
            } else if ("display-name".equals(tname)) {
                displayName = element.getBody();
            } else if ("small-icon".equals(tname)) {
                smallIcon = element.getBody();
            } else if ("large-icon".equals(tname)) {
                largeIcon = element.getBody();
            } else if ("info".equals(tname) ||
                     "description".equals(tname)) {
                info = element.getBody();
            } else if ("variable".equals(tname)) {
                variableVector.addElement(createVariable(element));
            } else if ("attribute".equals(tname)) {
                attributeVector.addElement(createAttribute(element));
            } else if ("dynamic-attributes".equals(tname)) {
                dynamicAttributes = JspUtil.booleanValue(element.getBody());
            } else if ("example".equals(tname)) {
                // Ignored elements
	    } else if ("tag-extension".equals(tname)) {
		// Ignored
            } else {
                Constants.message("jsp.warning.unknown.element.in.tag", 
                                  new Object[] {tname},
                                  Logger.WARNING
                                  );
	    }
	}

	TagAttributeInfo[] tagAttributeInfo
	    = new TagAttributeInfo[attributeVector.size()];
	attributeVector.copyInto(tagAttributeInfo);

	TagVariableInfo[] tagVariableInfos
	    = new TagVariableInfo[variableVector.size()];
	variableVector.copyInto(tagVariableInfos);

        TagExtraInfo tei = null;
        if (teiclass != null && !teiclass.equals("")) {
            try {
                Class teiClass = ctxt.getClassLoader().loadClass(teiclass);
                tei = (TagExtraInfo) teiClass.newInstance();
	    } catch (Exception e) {
                err.jspError("jsp.error.teiclass.instantiation", teiclass, e);
            }
	}

        TagInfo taginfo = new TagInfo(name, tagclass, bodycontent,
                                      info, this, 
                                      tei,
                                      tagAttributeInfo,
				      displayName,
				      smallIcon,
				      largeIcon,
				      tagVariableInfos,
                                      dynamicAttributes);
        return taginfo;
    }

    /*
     * Parses the tag file directives of the given TagFile and turns them into
     * a TagInfo.
     *
     * @param elem The <tag-file> element in the TLD
     * @param uri The location of the TLD, in case the tag file is specified
     * relative to it
     * @param jarFile The JAR file, in case the tag file is packaged in a JAR
     *
     * @return TagInfo correspoding to tag file directives
     */
    private TagFileInfo createTagFileInfo(TreeNode elem, String uri,
					  JarFile jarFile)
	        throws JasperException {

	String name = null;
	String path = null;

        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode child = (TreeNode) list.next();
            String tname = child.getName();
	    if ("name".equals(tname)) {
		name = child.getBody();
            } else if ("path".equals(tname)) {
		path = child.getBody();
	    } else {
                Constants.message("jsp.warning.unknown.element.in.attribute", 
                                  new Object[] {tname},
                                  Logger.WARNING
                                  );
            }
	}

	if (path.startsWith("/META-INF/tags")) {
	    // Tag file packaged in JAR
	    ctxt.getTagFileJars().put(path, jarFile);
	} else if (!path.startsWith("/WEB-INF/tags")) {
	    err.jspError("jsp.error.tagfile.illegalPath", path);
	}

	TagInfo tagInfo
	    = TagFileProcessor.parseTagFileDirectives(parserController, name,
						      path, this);
	return new TagFileInfo(name, path, tagInfo);
    }

    TagAttributeInfo createAttribute(TreeNode elem) {
        String name = null;
        String type = null;
        boolean required = false, rtexprvalue = false, reqTime = false,
	    isFragment = false;
        
        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();

            if ("name".equals(tname)) {
                name = element.getBody();
            } else if ("required".equals(tname)) {
                String s = element.getBody();
                if (s != null)
                    required = JspUtil.booleanValue(s);
            } else if ("rtexprvalue".equals(tname)) {
                String s = element.getBody();
                if (s != null)
                    rtexprvalue = JspUtil.booleanValue(s);
            } else if ("type".equals(tname)) {
                type = element.getBody();
            } else if ("fragment".equals(tname)) {
                String s = element.getBody();
                if (s != null)
                    isFragment = JspUtil.booleanValue(s);
            } else if ("description".equals(tname) ||    // Ignored elements
		       false) {
		;
            } else {
                Constants.message("jsp.warning.unknown.element.in.attribute", 
                                  new Object[] {tname},
                                  Logger.WARNING
                                  );
            }
        }
        
	if (!rtexprvalue) {
	    // According to JSP spec, for static values (those determined at
	    // translation time) the type is fixed at java.lang.String.
	    type = "java.lang.String";
	}

        return new TagAttributeInfo(name, required, type, rtexprvalue,
				    isFragment);
    }

    TagVariableInfo createVariable(TreeNode elem) {
        String nameGiven = null;
        String nameFromAttribute = null;
	String className = "java.lang.String";
	boolean declare = true;
	int scope = VariableInfo.NESTED;

        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();
            if ("name-given".equals(tname))
                nameGiven = element.getBody();
            else if ("name-from-attribute".equals(tname))
                nameFromAttribute = element.getBody();
            else if ("variable-class".equals(tname))
                className = element.getBody();
            else if ("declare".equals(tname)) {
                String s = element.getBody();
                if (s != null)
                    declare = JspUtil.booleanValue(s);
            } else if ("scope".equals(tname)) {
                String s = element.getBody();
                if (s != null) {
		    if ("NESTED".equals(s)) {
			scope = VariableInfo.NESTED;
		    } else if ("AT_BEGIN".equals(s)) {
			scope = VariableInfo.AT_BEGIN;
		    } else if ("AT_END".equals(s)) {
			scope = VariableInfo.AT_END;
		    }
		}
	    } else if ("description".equals(tname) ||    // Ignored elements
		     false ) {
            } else {
                Constants.message("jsp.warning.unknown.element.in.variable",
                                  new Object[] {tname},
                                  Logger.WARNING);
	    }
        }
        return new TagVariableInfo(nameGiven, nameFromAttribute,
				   className, declare, scope);
    }

    private TagLibraryValidator createValidator(TreeNode elem)
            throws JasperException {

        String validatorClass = null;
	Map initParams = new Hashtable();

        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();
            if ("validator-class".equals(tname))
                validatorClass = element.getBody();
            else if ("init-param".equals(tname)) {
		String[] initParam = createInitParam(element);
		initParams.put(initParam[0], initParam[1]);
            } else if ("description".equals(tname) ||    // Ignored elements
		     false ) {
            } else {
                Constants.message("jsp.warning.unknown.element.in.validator", //@@@ add in properties
                                  new Object[] {tname},
                                  Logger.WARNING);
	    }
        }

        TagLibraryValidator tlv = null;
        if (validatorClass != null && !validatorClass.equals("")) {
            try {
                Class tlvClass = 
		    ctxt.getClassLoader().loadClass(validatorClass);
                tlv = (TagLibraryValidator)tlvClass.newInstance();
            } catch (Exception e) {
                err.jspError("jsp.error.tlvclass.instantiation",
			     validatorClass, e);
            }
        }
	if (tlv != null) {
	    tlv.setInitParameters(initParams);
	}
	return tlv;
    }

    String[] createInitParam(TreeNode elem) {
        String[] initParam = new String[2];
        
        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();
            if ("param-name".equals(tname))
                initParam[0] = element.getBody();
            else if ("param-value".equals(tname))
                initParam[1] = element.getBody();
            else if ("description".equals(tname))
                ; // Do nothing
            else {
                Constants.message("jsp.warning.unknown.element.in.initParam", //@@@ properties
                                  new Object[] {tname},
                                  Logger.WARNING);
	    }
        }
	return initParam;
    }

    FunctionInfo createFunctionInfo(TreeNode elem) {

        String name = null;
        String klass = null;
        String signature = null;

        Iterator list = elem.findChildren();
        while (list.hasNext()) {
            TreeNode element = (TreeNode) list.next();
            String tname = element.getName();

            if ("name".equals(tname)) {
                name = element.getBody();
            } else if ("function-class".equals(tname)) {
                klass = element.getBody();
            } else if ("function-signature".equals(tname)) {
                signature = element.getBody();
            } else if ("display-name".equals(tname) ||    // Ignored elements
                     "small-icon".equals(tname) ||
                     "large-icon".equals(tname) ||
                     "description".equals(tname) || 
                     "example".equals(tname)) {
            } else {
                Constants.message("jsp.warning.unknown.element.in.function",
                                  new Object[] {tname},
                                  Logger.WARNING);
	    }
        }

        return new FunctionInfo(name, klass, signature);
    }


    //*********************************************************************
    // Until javax.servlet.jsp.tagext.TagLibraryInfo is fixed

    /**
     * The instance (if any) for the TagLibraryValidator class.
     * 
     * @return The TagLibraryValidator instance, if any.
     */
    public TagLibraryValidator getTagLibraryValidator() {
	return tagLibraryValidator;
    }

    /**
     * Translation-time validation of the XML document
     * associated with the JSP page.
     * This is a convenience method on the associated 
     * TagLibraryValidator class.
     *
     * @param thePage The JSP page object
     * @return A string indicating whether the page is valid or not.
     */
    public ValidationMessage[] validate(PageData thePage) {
	TagLibraryValidator tlv = getTagLibraryValidator();
	if (tlv == null) return null;
	return tlv.validate(getPrefixString(), getURI(), thePage);
    }

    protected TagLibraryValidator tagLibraryValidator; 
}
