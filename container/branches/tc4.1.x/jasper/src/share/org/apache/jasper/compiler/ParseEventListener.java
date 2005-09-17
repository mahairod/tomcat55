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

import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;

import org.xml.sax.Attributes;

/**
 * Interface for the JSP code generation backend. At some point should
 * probably try and make this a SAX (XML) listener. 
 *
 * @author Anil K. Vijendran
 * @author Pierre Delisle
 * @author Danno Ferrin 
 */
public interface ParseEventListener {
    /**
     * The reader associated with the listener.
     * As of JSP1.2, each part of the JSP tranlation
     * unit is parsed with a new instance of a parser
     * and the reader is different for each one.
     */
    public void setReader(JspReader reader);
    public void setDefault(boolean isXml);

    void setTemplateInfo(Mark start, Mark stop);
    void beginPageProcessing() throws JasperException;

    void handleComment(Mark start, Mark stop, char[] text) throws JasperException;
    void handleDirective(String directive, 
                         Mark start, Mark stop, 
                         Attributes attrs) 
        throws JasperException;
    void handleDeclaration(Mark start, Mark stop, Attributes attrs, char[] text) 
        throws JasperException;
    void handleScriptlet(Mark start, Mark stop, Attributes attrs, char[] text) 
        throws JasperException;
    void handleExpression(Mark start, Mark stop, Attributes attrs, char[] text)
        throws JasperException;
    void handleBean(Mark start, Mark stop, Attributes attrs) 
        throws JasperException;
    void handleBean(Mark start, Mark stop, Attributes attrs, boolean isXml) 
        throws JasperException;
    void handleBeanEnd (Mark start, Mark stop, Attributes attrs)
        throws JasperException;
    void handleGetProperty(Mark start, Mark stop, Attributes attrs) 
        throws JasperException;
    void handleSetProperty(Mark start, Mark stop, Attributes attrs) 
        throws JasperException;
    void handleSetProperty(Mark start, Mark stop, Attributes attrs, 
                           boolean isXml) 
        throws JasperException;
    void handlePlugin(Mark start, Mark stop, Attributes attrs, Hashtable param, 
                      String fallback) 
        throws JasperException;
    void handlePlugin(Mark start, Mark stop, Attributes attrs, Hashtable param, 
                      String fallback, boolean isXml) 
        throws JasperException;
    void handleCharData(Mark start, Mark stop, char[] chars) 
        throws JasperException;


    /*
     * Custom tag support
     */
    TagLibraries getTagLibraries();

    /*
     * start: is either the start position at "<" if content type is JSP or empty, or
     *        is the start of the body after the "/>" if content type is tag dependent
     * stop: can be null if the body contained JSP tags... 
     */
    void handleTagBegin(Mark start, Mark stop, Attributes attrs, String prefix, String shortTagName,
                        TagLibraryInfo tli, TagInfo ti, boolean hasBody) 
        throws JasperException;
    void handleTagBegin(Mark start, Mark stop, Attributes attrs, String prefix, String shortTagName,
                        TagLibraryInfo tli, TagInfo ti, boolean hasBody, boolean isXml) 
        throws JasperException;

    void handleTagEnd(Mark start, Mark stop, String prefix, String shortTagName,
                      Attributes attrs, TagLibraryInfo tli, TagInfo ti, boolean hasBody)
        throws JasperException;

    void handleForward(Mark start, Mark stop, Attributes attrs, Hashtable param)
        throws JasperException;
    void handleForward(Mark start, Mark stop, Attributes attrs, Hashtable param, boolean isXml)
        throws JasperException;
    void handleInclude(Mark start, Mark stop, Attributes attrs, Hashtable param)
        throws JasperException;
    void handleInclude(Mark start, Mark stop, Attributes attrs, Hashtable param, boolean isXml)
        throws JasperException;

    void endPageProcessing() throws JasperException;

    public void handleRootBegin(Attributes attrs) throws JasperException;
    public void handleRootEnd();

    public void handleUninterpretedTagBegin(Mark start, Mark stop,
                                            String rawName, Attributes attrs)
        throws JasperException;
    public void handleUninterpretedTagEnd(Mark start, Mark stop,
                                          String rawName, char[] data)
        throws JasperException;

    public void handleJspCdata(Mark start, Mark stop, char[] data)
        throws JasperException;
}

