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

import org.apache.jasper.Constants;
import javax.servlet.jsp.tagext.TagLibraryInfo;

/**
 * A repository for various info about the translation unit under compilation.
 *
 * @author Kin-man Chung
 */

class PageInfo {

    private Vector imports;
    private Vector dependants;

    private BeanRepository beanRepository;
    private HashMap taglibsMap;
    private Hashtable prefixMapper;

    private String language = "java";
    private String xtends = Constants.JSP_SERVLET_BASE;
    private String contentType = null;
    private boolean session = true;
    private int buffer = 8*1024;	// XXX confirm
    private boolean autoFlush = true;
    private boolean threadSafe = true;
    private boolean isErrorPage = false;
    private String errorPage = null;

    private int maxTagNesting = 0;
    private boolean scriptless = false;
    private boolean scriptingInvalid = false;
    private boolean elIgnored = false;
    private boolean elIgnoredSpecified = false;
    private String omitXmlDecl = null;

    // true if there is an is-xml element in the jsp-config
    private boolean isXmlConfigSpecified = false;	

    // The value of the is-xml element in the jsp-config
    private boolean isXmlConfig = false;

    // A custom tag is a tag file
    private boolean hasTagFile = false;

    private boolean isJspPrefixHijacked;

    // Set of all element and attribute prefixes used in this translation unit
    private HashSet prefixes;

    private boolean hasJspRoot = false;
    private Vector includePrelude;
    private Vector includeCoda;
    private Vector pluginDcls;		// Id's for tagplugin declarations


    PageInfo(BeanRepository beanRepository) {
	this.beanRepository = beanRepository;
	this.taglibsMap = new HashMap();
	this.prefixMapper = new Hashtable();
	this.imports = new Vector();
        this.dependants = new Vector();
	this.includePrelude = new Vector();
	this.includeCoda = new Vector();
	this.pluginDcls = new Vector();
	this.prefixes = new HashSet();

	// Enter standard imports
	for(int i = 0; i < Constants.STANDARD_IMPORTS.length; i++)
	    imports.add(Constants.STANDARD_IMPORTS[i]);
    }

    /**
     * Check if the plugin ID has been previously declared.  Make a not
     * that this Id is now declared.
     * @return true if Id has been declared.
    */
    public boolean isPluginDeclared(String id) {
	if (pluginDcls.contains(id))
	    return true;
	pluginDcls.add(id);
	return false;
    }

    public void addImports(List imports) {
	this.imports.addAll(imports);
    }

    public void addImport(String imp) {
	this.imports.add(imp);
    }

    public List getImports() {
	return imports;
    }

    public void addDependant(String d) {
	if (!dependants.contains(d))
            dependants.add(d);
    }
     
    public List getDependants() {
        return dependants;
    }

    public BeanRepository getBeanRepository() {
	return beanRepository;
    }

    public String getLanguage() {
	return language;
    }

    public void setLanguage(String language) {
	this.language = language;
    }

    public String getExtends() {
	return xtends;
    }

    public void setExtends(String xtends) {
	this.xtends = xtends;
    }

    public String getContentType() {
	return contentType;
    }

    public void setContentType(String contentType) {
	this.contentType = contentType;
    }

    public String getErrorPage() {
	return errorPage;
    }

    public void setErrorPage(String errorPage) {
	this.errorPage = errorPage;
    }

    public int getBuffer() {
	return buffer;
    }

    public void setBuffer(int buffer) {
	this.buffer = buffer;
    }

    public boolean isSession() {
	return session;
    }

    public void setSession(boolean session) {
	this.session = session;
    }

    public boolean isAutoFlush() {
	return autoFlush;
    }

    public void setAutoFlush(boolean autoFlush) {
	this.autoFlush = autoFlush;
    }

    public boolean isThreadSafe() {
	return threadSafe;
    }

    public void setThreadSafe(boolean threadSafe) {
	this.threadSafe = threadSafe;
    }

    public boolean isIsErrorPage() {
	return isErrorPage;
    }

    public void setIsErrorPage(boolean isErrorPage) {
	this.isErrorPage = isErrorPage;
    }

    public int getMaxTagNesting() {
        return maxTagNesting;
    }

    public void setMaxTagNesting(int maxTagNesting) {
        this.maxTagNesting = maxTagNesting;
    }

    public void setScriptless(boolean s) {
	scriptless = s;
    }

    public boolean isScriptless() {
	return scriptless;
    }

    public void setScriptingInvalid(boolean s) {
	scriptingInvalid = s;
    }

    public boolean isScriptingInvalid() {
	return scriptingInvalid;
    }

    public void setELIgnored(boolean s) {
	elIgnored = s;
    }

    public boolean isELIgnored() {
	return elIgnored;
    }

    public void setELIgnoredSpecified(boolean s) {
	elIgnoredSpecified = s;
    }

    public boolean isELIgnoredSpecified() {
	return elIgnoredSpecified;
    }

    public boolean isXmlConfig() {
	return isXmlConfig;
    }

    public void setIsXmlConfig(boolean xml) {
	isXmlConfig = xml;
    }

    public boolean isXmlConfigSpecified() {
	return isXmlConfigSpecified;
    }

    public void setIsXmlConfigSpecified(boolean xmlSpecified) {
	isXmlConfigSpecified = xmlSpecified;
    }

    public List getIncludePrelude() {
	return includePrelude;
    }

    public void setIncludePrelude(Vector prelude) {
	includePrelude = prelude;
    }

    public List getIncludeCoda() {
	return includeCoda;
    }

    public void setIncludeCoda(Vector coda) {
	includeCoda = coda;
    }

    public void setHasTagFile(boolean hasTag) {
	hasTagFile = hasTag;
    }

    public boolean hasTagFile() {
	return hasTagFile;
    }

    public void setHasJspRoot(boolean s) {
	hasJspRoot = s;
    }

    public boolean hasJspRoot() {
	return hasJspRoot;
    }

    public String getOmitXmlDecl() {
	return omitXmlDecl;
    }

    public void setOmitXmlDecl(String omit) {
	omitXmlDecl = omit;
    }


    /* Tag library and XML namespace management methods */

    public void setIsJspPrefixHijacked(boolean isHijacked) {
	isJspPrefixHijacked = isHijacked;
    }

    public boolean isJspPrefixHijacked() {
	return isJspPrefixHijacked;
    }

    /*
     * Adds the given prefix to the set of prefixes of this translation unit.
     * 
     * @param prefix The prefix to add
     */
    public void addPrefix(String prefix) {
	prefixes.add(prefix);
    }

    /*
     * Checks to see if this translation unit contains the given prefix.
     *
     * @param prefix The prefix to check
     *
     * @return true if this translation unit contains the given prefix, false
     * otherwise
     */
    public boolean containsPrefix(String prefix) {
	return prefixes.contains(prefix);
    }

    /*
     * Maps the given URI to the given tag library.
     *
     * @param uri The URI to map
     * @param info The tag library to be associated with the given URI
     */
    public void addTaglib(String uri, TagLibraryInfo info) {
	taglibsMap.put(uri, info);
    }

    /*
     * Gets the tag library corresponding to the given URI.
     *
     * @return Tag library corresponding to the given URI
     */
    public TagLibraryInfo getTaglib(String uri) {
	return (TagLibraryInfo) taglibsMap.get(uri);
    }

    /*
     * Gets the collection of tag libraries that are associated with a URI
     *
     * @return Collection of tag libraries that are associated with a URI
     */
    public Collection getTaglibs() {
	return taglibsMap.values();
    }

    /*
     * Checks to see if the given URI is mapped to a tag library.
     *
     * @param uri The URI to map
     *
     * @return true if the given URI is mapped to a tag library, false
     * otherwise
     */
    public boolean hasTaglib(String uri) {
	return taglibsMap.containsKey(uri);
    }

    /*
     * Maps the given prefix to the given URI.
     *
     * @param prefix The prefix to map
     * @param uri The URI to be associated with the given prefix
     */
    public void addPrefixToURIMapping(String prefix, String uri) {
	prefixMapper.put(prefix, uri);
    }

    /*
     * Maps the given prefix to its URI.
     *
     * @param prefix The prefix to map
     *
     * @return The URI to which the given prefix maps
     */
    public String getURI(String prefix) {
	return (String) prefixMapper.get(prefix);
    }

}
