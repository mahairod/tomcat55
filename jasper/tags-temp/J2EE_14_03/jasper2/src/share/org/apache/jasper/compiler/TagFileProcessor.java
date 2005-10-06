/*
 * $Header: /home/cvs/jakarta-tomcat-jasper/jasper2/src/share/org/apache/jasper/
compiler/TagFileProcessor.java,v 1.16 2002/05/24 23:57:42 kinman Exp $
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
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.jsp.tagext.*;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.servlet.JspServletWrapper;

/**
 * Processes and extracts the directive info in a tag file.
 *
 * @author Kin-man Chung
 */

public class TagFileProcessor {

    /**
     * A visitor the tag file
     */
    static class TagFileVisitor extends Node.Visitor {

        private static final JspUtil.ValidAttribute[] tagDirectiveAttrs = {
            new JspUtil.ValidAttribute("display-name"),
            new JspUtil.ValidAttribute("body-content"),
            new JspUtil.ValidAttribute("dynamic-attributes"),
            new JspUtil.ValidAttribute("small-icon"),
            new JspUtil.ValidAttribute("large-icon"),
            new JspUtil.ValidAttribute("description"),
            new JspUtil.ValidAttribute("example"),
            new JspUtil.ValidAttribute("pageEncoding"),
            new JspUtil.ValidAttribute("language"),
            new JspUtil.ValidAttribute("import"),
            new JspUtil.ValidAttribute("isScriptingEnabled"),
            new JspUtil.ValidAttribute("isELEnabled") };

	private static final JspUtil.ValidAttribute[] attributeDirectiveAttrs = {
	    new JspUtil.ValidAttribute("name", true),
	    new JspUtil.ValidAttribute("required"),
	    new JspUtil.ValidAttribute("fragment"),
	    new JspUtil.ValidAttribute("rtexprvalue"),
	    new JspUtil.ValidAttribute("type"),
	    new JspUtil.ValidAttribute("description")
	};

	private static final JspUtil.ValidAttribute[] variableDirectiveAttrs = {
	    new JspUtil.ValidAttribute("name-given"),
	    new JspUtil.ValidAttribute("name-from-attribute"),
	    new JspUtil.ValidAttribute("variable-class"),
	    new JspUtil.ValidAttribute("scope"),
	    new JspUtil.ValidAttribute("declare"),
	    new JspUtil.ValidAttribute("fragment"),
	    new JspUtil.ValidAttribute("description")
	};

	private static final JspUtil.ValidAttribute[] fragmentInputDirectiveAttrs = {
	    new JspUtil.ValidAttribute("name", true),
	    new JspUtil.ValidAttribute("fragment", true),
	    new JspUtil.ValidAttribute("required"),
	    new JspUtil.ValidAttribute("type"),
	    new JspUtil.ValidAttribute("description")
	};

        private ErrorDispatcher err;
	private TagLibraryInfo tagLibInfo;

        private String name = null;
        private String tagclass = null;
        private TagExtraInfo tei = null;
        private String bodycontent = null;
        private String description = null;
        private String displayName = null;
        private String smallIcon = null;
        private String largeIcon = null;
        private boolean dynamicAttributes = false;

        private Vector attributeVector = new Vector();
        private Vector variableVector = new Vector();
        private Map fragmentAttributesMap = new Hashtable();

        public TagFileVisitor(Compiler compiler, TagLibraryInfo tagLibInfo,
			      String name) {
            err = compiler.getErrorDispatcher();
	    this.tagLibInfo = tagLibInfo;
	    this.name = name;
        }

        public void visit(Node.TagDirective n) throws JasperException {

            JspUtil.checkAttributes("Tag directive", n, tagDirectiveAttrs,
				    err);

            bodycontent = n.getAttributeValue("body-content");
            if (bodycontent != null &&
                    !bodycontent.equals(TagInfo.BODY_CONTENT_EMPTY) &&
                    !bodycontent.equals(TagInfo.BODY_CONTENT_TAG_DEPENDENT) &&
                    !bodycontent.equals(TagInfo.BODY_CONTENT_SCRIPTLESS)) {
                err.jspError("jsp.error.tagdirective.badbodycontent",
                             bodycontent);
            }
            dynamicAttributes= JspUtil.booleanValue(
			n.getAttributeValue("dynamic-attributes"));
            smallIcon = n.getAttributeValue("small-icon");
            largeIcon = n.getAttributeValue("large-icon");
            description = n.getAttributeValue("description");
            displayName = n.getAttributeValue("display-name");
        }

        public void visit(Node.AttributeDirective n) throws JasperException {

            JspUtil.checkAttributes("Attribute directive", n,
                                    attributeDirectiveAttrs, err);

            String attrName = n.getAttributeValue("name");
            boolean required = JspUtil.booleanValue(
					n.getAttributeValue("required"));
            String rtexprvalueString = n.getAttributeValue("rtexprvalue");
            boolean rtexprvalue = JspUtil.booleanValue( rtexprvalueString );
            boolean fragment = JspUtil.booleanValue(
					n.getAttributeValue("fragment"));
	    String type = n.getAttributeValue("type");
            if (fragment) {
                fragmentAttributesMap.put(attrName, n);
                // type is fixed to "JspFragment" and a translation error
                // must occur if specified.
                if (type != null) {
                    err.jspError("jsp.error.fragmentwithtype");
                }
                // rtexprvalue is fixed to "true" and a translation error
                // must occur if specified.
                rtexprvalue = true;
                if( rtexprvalueString != null ) {
                    err.jspError("jsp.error.frgmentwithrtexprvalue" );
                }
            } else {
                if (type == null)
                    type = "java.lang.String";
	    }

	    attributeVector.addElement(
                    new TagAttributeInfo(attrName, required, type, rtexprvalue,
					 fragment));
        }

        public void visit(Node.VariableDirective n) throws JasperException {

            JspUtil.checkAttributes("Variable directive", n,
                                    variableDirectiveAttrs, err);

            String nameGiven = n.getAttributeValue("name-given");
            String nameFromAttribute = n.getAttributeValue("name-from-attribute");
            String className = n.getAttributeValue("variable-class");
            if (className == null)
                className = "java.lang.String";

            String declareStr = n.getAttributeValue("declare");
            boolean declare = true;
            if (declareStr != null)
                declare = JspUtil.booleanValue(declareStr);

            int scope = VariableInfo.NESTED;
            String scopeStr = n.getAttributeValue("scope");
            if (scopeStr != null) {
                if ("NESTED".equals(scopeStr)) {
                    // Already the default
                } else if ("AT_BEGIN".equals(scopeStr)) {
                    scope = VariableInfo.AT_BEGIN;
                } else if ("AT_END".equals(scopeStr)) {
                    scope = VariableInfo.AT_END;
                }
            }

            String fragment = n.getAttributeValue("fragment");
	    if (fragment != null) {
		if (declareStr != null || scopeStr != null) {
		    err.jspError(n, "jsp.error.fragmentWithDeclareOrScope");
		}

		// Find the attribute node with matching name
		Node.AttributeDirective attributeDirective =
		    (Node.AttributeDirective) fragmentAttributesMap.get(fragment);
		if (attributeDirective == null) {
		    err.jspError(n, "jsp.error.nomatching.fragment", fragment);
		}
		variableVector.addElement(
                    new TagVariableInfo(nameGiven, nameFromAttribute,
                                        className, declare, scope, fragment));
	    } else {
		variableVector.addElement(
                    new TagVariableInfo(nameGiven, nameFromAttribute,
                                        className, declare, scope));
	    }
        }

        public TagInfo getTagInfo() {

            if (name == null) {
                // XXX Get it from tag file name
	    }

            if (bodycontent == null) {
                bodycontent = TagInfo.BODY_CONTENT_SCRIPTLESS;
            }

            tagclass = Constants.TAG_FILE_PACKAGE_NAME + "." + name;

            TagVariableInfo[] tagVariableInfos
                    = new TagVariableInfo[variableVector.size()];
            variableVector.copyInto(tagVariableInfos);

            TagAttributeInfo[] tagAttributeInfo
                    = new TagAttributeInfo[attributeVector.size()];
            attributeVector.copyInto(tagAttributeInfo);

            return new TagInfo(name,
			       tagclass,
			       bodycontent,
                               description,
			       tagLibInfo,
                               tei,
                               tagAttributeInfo,
                               displayName,
                               smallIcon,
                               largeIcon,
                               tagVariableInfos,
                               dynamicAttributes);
	}
    }

    /**
     * Parses the tag file, and collects information on the directives included
     * in it.  The method is used to obtain the info on the tag file, when the 
     * handler that it represents is referenced.  The tag file is not compiled
     * here.
     * @param pc the current ParserController used in this compilation
     * @param name the tag name as specified in the TLD
     * @param tagfile the path for the tagfile
     * @param tagLibInfo the TagLibraryInfo object associated with this TagInfo
     * @return a TagInfo object assembled from the directives in the tag file.
     */
    public static TagInfo parseTagFile(ParserController pc,
                                       String name,
                                       String tagfile,
				       TagLibraryInfo tagLibInfo)
                throws JasperException {

        Node.Nodes page = null;
	try {
	    page = pc.parse(tagfile);
	} catch (FileNotFoundException e) {
	    pc.getCompiler().getErrorDispatcher().jspError(
                                        "jsp.error.file.not.found", tagfile);
	} catch (IOException e) {
	    pc.getCompiler().getErrorDispatcher().jspError(
                                        "jsp.error.file.not.found", tagfile);
	}

        TagFileVisitor tagFileVisitor = new TagFileVisitor(pc.getCompiler(),
							   tagLibInfo, name);
        page.visit(tagFileVisitor);

        return tagFileVisitor.getTagInfo();
    }

    /**
     * Compiles and loads a tagfile.
     */
    public static Class loadTagFile(JspCompilationContext ctxt,
				    String tagFilePath, TagInfo tagInfo,
				    TagData tagData)
	throws JasperException {

	JspRuntimeContext rctxt = ctxt.getRuntimeContext();
        JspServletWrapper wrapper =
		(JspServletWrapper) rctxt.getWrapper(tagFilePath);
	if (wrapper == null) {
	    synchronized(rctxt) {
		wrapper = (JspServletWrapper) rctxt.getWrapper(tagFilePath);
		if (wrapper == null) {
		    wrapper = new JspServletWrapper(ctxt.getServletContext(),
						    ctxt.getOptions(),
						    tagFilePath,
                                                    tagInfo,
						    tagData,
						    ctxt.getRuntimeContext(),
						    ctxt.getTagFileJars());
		}
	    }
	}
	return wrapper.loadTagFile();
    }

    /*
     * A visitor that scan the page, looking for tag handlers that are tag
     * files and compile (if necessary) and load them.
     */ 

    static class TagFileLoaderVisitor extends Node.Visitor {

	private JspCompilationContext ctxt;

	TagFileLoaderVisitor(JspCompilationContext ctxt) {
	    this.ctxt = ctxt;
	}

        public void visit(Node.CustomTag n) throws JasperException {
	    TagFileInfo tagFileInfo = n.getTagFileInfo();
	    if (tagFileInfo != null) {
		String tagFilePath = tagFileInfo.getPath();
		Class c = loadTagFile(ctxt, tagFilePath, n.getTagInfo(),
				      n.getTagData());
		n.setTagHandlerClass(c);
	    }
	}
    }

    public static void loadTagFiles(Compiler compiler, Node.Nodes page)
		throws JasperException {

	JspCompilationContext ctxt = compiler.getCompilationContext();
	page.visit(new TagFileLoaderVisitor(ctxt));
    }
	
}

