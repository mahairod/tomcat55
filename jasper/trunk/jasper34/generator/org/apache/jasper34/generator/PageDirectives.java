/*
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

package org.apache.jasper34.generator;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper34.core.*;

import org.apache.jasper34.parser.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.JasperException;
import org.apache.tomcat.util.log.*;
import org.apache.jasper34.jsptree.*;

/**
 */
public class PageDirectives  {
    ContainerLiaison containerL;
    
    public PageDirectives(ContainerLiaison containerL) {
	this.containerL=containerL;
    }
    
    public void handlePageDirective( JspParseEventListener listener,
				     String directive, Mark start,
				     Mark stop, Hashtable attrs)
	throws JasperException
    {
	Enumeration e = attrs.keys();
	String attr;
	while (e.hasMoreElements()) {
	    attr = (String) e.nextElement();
	    for(int i = 0; i < pdhis.length; i++) {
		PageDirectiveHandlerInfo pdhi = pdhis[i];
		if (attr.equals(pdhi.attribute)) {
                        String value = (String) attrs.get(pdhi.attribute);
                        pdhi.handler.handlePageDirectiveAttribute(listener, value,
                                                                  start, stop);
		}
	    }
	}

	// Do some validations...
        if (listener.pageInfo.bufferSize == 0 && listener.pageInfo.autoFlush == false)
            throw new CompileException(start, containerL.getString(
	    				"jsp.error.page.bad_b_and_a_combo"));


    }

    
    interface PageDirectiveHandler {
        void handlePageDirectiveAttribute(JspParseEventListener listener,
                                          String value,
                                          Mark start, Mark stop)
            throws JasperException;
    }
    
    static final class PageDirectiveHandlerInfo {
        String attribute;
        PageDirectiveHandler handler;
        PageDirectiveHandlerInfo(String attribute, PageDirectiveHandler handler) {
            this.attribute = attribute;
            this.handler = handler;
        }
    }
    
    static final String languageStr = "language";
    static final String extendsStr = "extends";
    static final String importStr = "import";
    static final String sessionStr = "session";
    static final String bufferStr = "buffer";
    static final String autoFlushStr = "autoFlush";
    static final String isThreadSafeStr = "isThreadSafe";
    static final String infoStr = "info";
    static final String errorPageStr = "errorPage";
    static final String isErrorPageStr = "isErrorPage";
    static final String contentTypeStr = "contentType";


    PageDirectiveHandlerInfo[] pdhis = new PageDirectiveHandlerInfo[] {
        new PageDirectiveHandlerInfo(languageStr, new LanguageHandler()),
        new PageDirectiveHandlerInfo(extendsStr, new ExtendsHandler()),
        new PageDirectiveHandlerInfo(importStr, new ImportsHandler()),
        new PageDirectiveHandlerInfo(sessionStr, new SessionHandler()),
        new PageDirectiveHandlerInfo(bufferStr, new BufferHandler()),
        new PageDirectiveHandlerInfo(autoFlushStr, new AutoFlushHandler()),
        new PageDirectiveHandlerInfo(isThreadSafeStr, new IsThreadSafeHandler()),
        new PageDirectiveHandlerInfo(infoStr, new InfoHandler()),
        new PageDirectiveHandlerInfo(isErrorPageStr, new IsErrorPageHandler()),
        new PageDirectiveHandlerInfo(contentTypeStr, new ContentTypeHandler()),
        new PageDirectiveHandlerInfo(errorPageStr, new ErrorPageHandler())
    };
    
    // FIXME: Need to further refine these abstractions by moving the code
    // to handle duplicate directive instance checks to outside.
    
    static final class ContentTypeHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String contentType,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.contentTypeDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.contenttypes"));
            listener.pageInfo.contentTypeDir = true;
            if (contentType == null)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.contenttype"));
            listener.pageInfo.servletContentType = contentType;
        }
    }
    
    static final class SessionHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String session,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.sessionDir == true)
                throw new CompileException (start,
					    listener.getContainerLiaison().getString("jsp.error.page.multiple.session"));
            listener.pageInfo.sessionDir = true;
            if (session == null)
                throw new CompileException (start,
					    listener.getContainerLiaison().getString("jsp.error.page.invalid.session"));
            if (session.equalsIgnoreCase("true"))
                listener.pageInfo.genSessionVariable = true;
            else if (session.equalsIgnoreCase("false"))
                listener.pageInfo.genSessionVariable = false;
            else
                throw new CompileException(start, "Invalid value for session");
        }
    }

    static final class BufferHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String buffer,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.bufferDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.buffer"));
            listener.pageInfo.bufferDir = true;
            if (buffer != null) {
                if (buffer.equalsIgnoreCase("none"))
                    listener.pageInfo.bufferSize = 0;
                else {
                    Integer i = null;
                    try {
                        int ind = buffer.indexOf("k");
                        String num;
                        if (ind == -1)
                            num = buffer;
                        else
                            num = buffer.substring(0, ind);
                        i = new Integer(num);
                    } catch (NumberFormatException n) {
                        throw new CompileException(start, listener.getContainerLiaison().getString(
					"jsp.error.page.invalid.buffer"));
                    }
                    listener.pageInfo.bufferSize = i.intValue()*1024;
                }
            }
            else
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.buffer"));
        }
    }

    static final class AutoFlushHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String autoflush,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.autoFlushDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.autoflush"));

            listener.pageInfo.autoFlushDir = true;
            if (autoflush == null)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.autoflush"));

            if (autoflush.equalsIgnoreCase("true"))
                listener.pageInfo.autoFlush = true;
            else if (autoflush.equalsIgnoreCase("false"))
                listener.pageInfo.autoFlush = false;
            else
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.autoflush"));
        }
    }

    static final class IsThreadSafeHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String threadsafe,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.threadsafeDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.threadsafe"));

            listener.pageInfo.threadsafeDir = true;
            if (threadsafe == null)
                throw new CompileException (start,
					    listener.getContainerLiaison().getString("jsp.error.page.invalid.threadsafe"));

            if (threadsafe.equalsIgnoreCase("true"))
                listener.pageInfo.singleThreaded = false;
            else if (threadsafe.equalsIgnoreCase("false"))
                listener.pageInfo.singleThreaded = true;
            else
                throw new CompileException (start,
					    listener.getContainerLiaison().getString("jsp.error.page.invalid.threadsafe"));
        }
    }

    static final class InfoHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String info,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.infoDir == true)
                throw new CompileException (start,
					    listener.getContainerLiaison().getString("jsp.error.page.multiple.info"));

            listener.pageInfo.infoDir = true;
            if (info == null)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.info"));

            GeneratorBase gen = new InfoGenerator(info);
	    gen.setMark( start, stop );

            listener.pageInfo.addGenerator(gen);
        }
    }

    static final class IsErrorPageHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String iserrorpage,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.iserrorpageDir == true)
                throw new CompileException (start,
					    listener.getContainerLiaison().getString("jsp.error.page.multiple.iserrorpage"));

            listener.pageInfo.iserrorpageDir = true;
            if (iserrorpage == null)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.iserrorpage"));

            if (iserrorpage.equalsIgnoreCase("true"))
                listener.pageInfo.setErrorPage(true);
            else if (iserrorpage.equalsIgnoreCase("false"))
                listener.pageInfo.setErrorPage(false);
            else
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.invalid.iserrorpage"));
        }
    }

    static final class ErrorPageHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String errorpage,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.errorpageDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.errorpage"));

            listener.pageInfo.errorpageDir = true;
            if (errorpage != null)
                listener.pageInfo.error = errorpage;
        }
    }

    static final class LanguageHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String language,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.languageDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.language"));

            listener.pageInfo.languageDir = true;
            if (language != null)
                if (!language.equalsIgnoreCase("java"))
                    throw new CompileException(start,
					       listener.getContainerLiaison().getString("jsp.error.page.nomapping.language")+language);
        }
    }

    static final class ImportsHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String importPkgs,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (importPkgs != null) {
                StringTokenizer tokenizer = new StringTokenizer(importPkgs, ",");
                while (tokenizer.hasMoreTokens())
                    listener.pageInfo.imports.addElement(tokenizer.nextToken());
            }
        }
    }

    static final class ExtendsHandler implements PageDirectiveHandler {
        public void handlePageDirectiveAttribute(JspParseEventListener listener,
                                                 String extendsClzz,
                                                 Mark start, Mark stop)
            throws JasperException
        {
            if (listener.pageInfo.extendsDir == true)
                throw new CompileException(start,
					   listener.getContainerLiaison().getString("jsp.error.page.multiple.extends"));

            listener.pageInfo.extendsDir = true;
            if (extendsClzz != null) {
                listener.pageInfo.extendsClass = extendsClzz;

		/*
		 * If page superclass is top level class (i.e. not in a pkg)
		 * explicitly import it. If this is not done, the compiler
		 * will assume the extended class is in the same pkg as
		 * the generated servlet.
		 */
		if (extendsClzz.indexOf(".") == -1)  {
                    listener.pageInfo.imports.addElement(extendsClzz);
		}
            }
        }
    }

}
