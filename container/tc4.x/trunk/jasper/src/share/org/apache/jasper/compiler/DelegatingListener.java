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
import org.apache.jasper.Constants;

import org.xml.sax.Attributes;

/** 
 * Simple util class.... see usage in Parser.Parser(). Not intended for anything
 * other than that purpose.... 
 *
 * @author Anil K. Vijendran
 * @author Danno Ferrin
 */
final class DelegatingListener implements ParseEventListener {
    ParseEventListener delegate;
    Parser.Action action;
    Mark tmplStart, tmplStop;
    
    DelegatingListener(ParseEventListener delegate, Parser.Action action) {
        this.delegate = delegate;
        this.action = action;
    }

    public void setDefault(boolean isXml) {
        delegate.setDefault(isXml);
    }

    public void setReader(JspReader reader) {
	delegate.setReader(reader);
    }

    void doAction(Mark start, Mark stop) throws JasperException {
        action.execute(start, stop);
    }

    public void setTemplateInfo(Mark start, Mark stop) {
	this.tmplStart = start;
	this.tmplStop = stop;
    }

    public void beginPageProcessing() throws JasperException {
        delegate.beginPageProcessing();
    }
    
    public void endPageProcessing() throws JasperException {
        delegate.endPageProcessing();
    }
    
    public void handleComment(Mark start, Mark stop, char[] text) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleComment(start, stop, text);
    }

    public void handleDirective(String directive,Mark start,Mark stop,Attributes attrs) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleDirective(directive, start, stop, attrs);
    }
    
    public void handleDeclaration(Mark start,Mark stop,Attributes attrs,char[] text) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleDeclaration(start, stop, attrs, text);
    }
    
    public void handleScriptlet(Mark start,Mark stop,Attributes attrs,char[] text) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleScriptlet(start, stop, attrs, text);
    }
    
    public void handleExpression(Mark start,Mark stop,Attributes attrs,char[] text) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleExpression(start, stop, attrs, text);
    }

    public void handleBean(Mark start,Mark stop,Attributes attrs) throws JasperException
    {
        handleBean(start, stop, attrs, false);
    }

    public void handleBean(Mark start,Mark stop,Attributes attrs, boolean isXml) throws JasperException
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleBean(start, stop, attrs, isXml);
    }

    public void handleBeanEnd(Mark start,Mark stop,Attributes attrs) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleBeanEnd(start, stop, attrs);
    }

    public void handleGetProperty(Mark start,Mark stop,Attributes attrs) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleGetProperty(start, stop, attrs);
    }
    
    public void handleSetProperty(Mark start,Mark stop,Attributes attrs) throws JasperException 
    {
        handleSetProperty(start, stop, attrs, false);
    }

    public void handleSetProperty(Mark start,Mark stop,Attributes attrs,boolean isXml) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleSetProperty(start, stop, attrs, isXml);
    }
    
    public void handlePlugin(Mark start,Mark stop,Attributes attrs,Hashtable param,String fallback) throws JasperException 
    {
        handlePlugin(start, stop, attrs, param, fallback, false);
    }

    public void handlePlugin(Mark start,Mark stop,Attributes attrs,Hashtable param,String fallback, boolean isXml) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handlePlugin(start, stop, attrs, param, fallback, isXml);
    }
    
    public void handleCharData(Mark start, Mark stop, char[] chars) throws JasperException {
        delegate.handleCharData(start, stop, chars);
    }

    public void handleForward(Mark start,Mark stop,Attributes attrs,Hashtable param) throws JasperException 
    {
        handleForward(start, stop, attrs, param, false);
    }

    public void handleForward(Mark start,Mark stop,Attributes attrs,Hashtable param, boolean isXml) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleForward(start, stop, attrs, param, isXml);
    }

    public void handleInclude(Mark start,Mark stop,Attributes attrs,Hashtable param) throws JasperException 
    {
        handleInclude(start, stop, attrs, param, false);
    }

    public void handleInclude(Mark start,Mark stop,Attributes attrs,Hashtable param, boolean isXml) throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleInclude(start, stop, attrs, param, isXml);
    }

    public void handleTagBegin(Mark start,Mark stop,Attributes attrs,String prefix,String shortTagName,TagLibraryInfo tli,TagInfo ti, boolean hasBody) throws JasperException
    {
        handleTagBegin(start, stop, attrs, prefix, shortTagName, tli, ti, hasBody, false);
    }
    public void handleTagBegin(Mark start,Mark stop,Attributes attrs,String prefix,String shortTagName,TagLibraryInfo tli,TagInfo ti, boolean hasBody, boolean isXml) throws JasperException
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleTagBegin(start, stop, attrs, prefix, shortTagName, tli, ti, hasBody, isXml);
    }
    
    public void handleTagEnd(Mark start,Mark stop,String prefix,String shortTagName,Attributes attrs,TagLibraryInfo tli,TagInfo ti, boolean hasBody) throws JasperException
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleTagEnd(start, stop, prefix, shortTagName, attrs, tli, ti, hasBody);
    }
    
    public TagLibraries getTagLibraries() {
        return delegate.getTagLibraries();
    }

    public void handleRootBegin(Attributes attrs) throws JasperException {}
    public void handleRootEnd() {}

    public void handleUninterpretedTagBegin(Mark start, Mark stop,
                                            String rawName,Attributes attrs) 
        throws JasperException {}
    public void handleUninterpretedTagEnd(Mark start, Mark stop,
                                          String rawName, char[] data)
        throws JasperException {}

    public void handleJspCdata(Mark start, Mark stop, char[] data)
        throws JasperException {
        delegate.handleJspCdata(start, stop, data);
    }
}

