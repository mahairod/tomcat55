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
import java.io.FileNotFoundException;

import javax.servlet.jsp.tagext.*;

import org.apache.jasper.JasperException;

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

        private ErrorDispatcher err;

        private String name = null;
        private String tagclass = null;
        private TagExtraInfo tei = null;
        private String bodycontent = "JSP"; // Default body content is JSP
        private String info = null;
        private String displayName = null;
        private String smallIcon = null;
        private String largeIcon = null;
        private boolean dynamicAttributes = false;

        private Vector attributeVector = new Vector();
        private Vector variableVector = new Vector();
        private Vector fragmentAttributeVector = new Vector();
        private Map fragmentAttributesMap = new Hashtable();

        private static final JspUtil.ValidAttribute[] tagDirectiveAttrs = {
            new JspUtil.ValidAttribute("name"),
            new JspUtil.ValidAttribute("dispaly-name"),
            new JspUtil.ValidAttribute("body-content"),
            new JspUtil.ValidAttribute("small-icon"),
            new JspUtil.ValidAttribute("large-icon"),
            new JspUtil.ValidAttribute("description"),
            new JspUtil.ValidAttribute("example"),
            new JspUtil.ValidAttribute("pageEncoding") };

        private static final JspUtil.ValidAttribute[] attributeDirectiveAttrs =
{
            new JspUtil.ValidAttribute("name", true),
            new JspUtil.ValidAttribute("required"),
            new JspUtil.ValidAttribute("fragment"),
            new JspUtil.ValidAttribute("rtexprvalue"),
            new JspUtil.ValidAttribute("type"),
            new JspUtil.ValidAttribute("description")
        };

        private static final JspUtil.ValidAttribute[] variableDirectiveAttrs = {
            new JspUtil.ValidAttribute("name-given"),
            new JspUtil.ValidAttribute("name-from"),
            new JspUtil.ValidAttribute("variable-class"),
            new JspUtil.ValidAttribute("scope"),
            new JspUtil.ValidAttribute("declare"),
            new JspUtil.ValidAttribute("description")
        };

        private static final JspUtil.ValidAttribute[] fragmentInputDirectiveAttrs = {
            new JspUtil.ValidAttribute("name", true),
            new JspUtil.ValidAttribute("fragment", true),
            new JspUtil.ValidAttribute("required"),
            new JspUtil.ValidAttribute("type"),
            new JspUtil.ValidAttribute("description")
        };

        public TagFileVisitor(Compiler compiler) {
            err = compiler.getErrorDispatcher();
        }

        public void visit(Node.TagDirective n) throws JasperException {

            JspUtil.checkAttributes("Tag directive", n,
                                    tagDirectiveAttrs, err);

	    name = n.getAttributeValue("name");
            bodycontent = n.getAttributeValue("body-content");
            dynamicAttributes= JspUtil.booleanValue(
			n.getAttributeValue("dynamic-attributes"));
            smallIcon = n.getAttributeValue("small-icon");
            largeIcon = n.getAttributeValue("large-icon");
            info = n.getAttributeValue("description");
            displayName = n.getAttributeValue("display-name");
        }

        public void visit(Node.AttributeDirective n) throws JasperException {

            JspUtil.checkAttributes("Attribute directive", n,
                                    attributeDirectiveAttrs, err);

            String name = n.getAttributeValue("name");
            boolean required = JspUtil.booleanValue(
					n.getAttributeValue("required"));
            boolean rtexprvalue = JspUtil.booleanValue(
					n.getAttributeValue("rtexprvalue"));
            boolean fragment = JspUtil.booleanValue(
					n.getAttributeValue("fragment"));
	    String type = n.getAttributeValue("type");
            if (type == null)
                type = "java.lang.String";

            if (fragment) {
                n.setFragmentInputs(new Vector());
                fragmentAttributesMap.put(name, n);
            } else {
                attributeVector.addElement(
                    new TagAttributeInfo(name, required, type, rtexprvalue));
            }
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
                    scope = VariableInfo.NESTED;
                } else if ("AT_BEGIN".equals(scopeStr)) {
                    scope = VariableInfo.AT_BEGIN;
                } else if ("AT_END".equals(scopeStr)) {
                    scope = VariableInfo.AT_END;
                }
            }
            variableVector.addElement(
                    new TagVariableInfo(nameGiven, nameFromAttribute,
                                        className, declare, scope));
        }

        public void visit(Node.FragmentInputDirective n) throws JasperException{

            JspUtil.checkAttributes("Fragment-input directive", n,
                                    fragmentInputDirectiveAttrs, err);

            String name = n.getAttributeValue("name");
            String fragment = n.getAttributeValue("fragment");
            String type = n.getAttributeValue("type");
            String description = n.getAttributeValue("description");
            boolean required = JspUtil.booleanValue(n.getAttributeValue("required"));
            // Find the attribute node with matching name
            Node.AttributeDirective attributeDirective =
                (Node.AttributeDirective) fragmentAttributesMap.get(name);
            if (attributeDirective == null) {
                err.jspError(n, "jsp.error.nomatching.fragment", name);
            }
            attributeDirective.getFragmentInputs().addElement(
                        new TagFragmentAttributeInfo.FragmentInput(name, type,
                                                          description));
        }

        public TagInfo getTagInfo() {

            TagVariableInfo[] tagVariableInfos
                    = new TagVariableInfo[variableVector.size()];
            variableVector.copyInto(tagVariableInfos);

            TagAttributeInfo[] tagAttributeInfo
                    = new TagAttributeInfo[attributeVector.size()];
            attributeVector.copyInto(tagAttributeInfo);

            // For each fragment attribute, construct the fragment inputs first
            TagFragmentAttributeInfo [] fragmentAttributes
                = new TagFragmentAttributeInfo[fragmentAttributesMap.size()];
            Iterator iter = fragmentAttributesMap.values().iterator();
            int i = 0;
            while (iter.hasNext()) {
                Node.AttributeDirective n = (Node.AttributeDirective)iter.next();
                TagFragmentAttributeInfo.FragmentInput[] fragmentInputs =
                    new TagFragmentAttributeInfo.FragmentInput[
                                           n.getFragmentInputs().size()];
                n.getFragmentInputs().copyInto(fragmentInputs);
                String name = n.getAttributeValue("name");
                boolean required = JspUtil.booleanValue(
                                        n.getAttributeValue("required"));

                fragmentAttributes[i++] = new TagFragmentAttributeInfo(
                                                  name, required, "",
                                                  fragmentInputs);
            }

            return new TagInfo(name, tagclass, bodycontent,
                               info, null,
                               tei,
                               tagAttributeInfo,
                               displayName,
                               smallIcon,
                               largeIcon,
                               tagVariableInfos,
                               fragmentAttributes,
                               dynamicAttributes);
        }

    }

    public static TagInfo parseTagFile(ParserController pc, String tagfile)
                throws FileNotFoundException, JasperException, Exception {

        Node.Nodes page = pc.parse(tagfile);
        TagFileVisitor tagFileVisitor = new TagFileVisitor(pc.getCompiler());
        page.visit(tagFileVisitor);
        return tagFileVisitor.getTagInfo();
    }
}

