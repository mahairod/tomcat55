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
import org.apache.jasper34.runtime.JasperException;
import org.apache.tomcat.util.log.*;
import org.apache.jasper34.jsptree.*;

/**
 * JSP code generator "backend". It will receive parse events and
 * collect all informations about the page in a JspPageInfo object.
 *
 * @author Anil K. Vijendran
 * @author Costin Manolache
 */
public class JspParseEventListener implements ParseEventListener {
    
    //    JspCompilationContext ctxt;
    ContainerLiaison containerL;

    protected JspReader reader;
    protected ServletWriter writer;

    JspPageInfo pageInfo;
    PageDirectives pageD;


    /*
     * Package private since I want everyone to come in through
     * org.apache.jasper.compiler.Main.
     */
    public JspParseEventListener(ContainerLiaison containerL,
				 JspReader reader, ServletWriter writer,
				 JspPageInfo pageInfo)
    {
	this.containerL=containerL;
	this.reader=reader;
	this.writer=writer;
	this.pageInfo=pageInfo;
	pageD=new PageDirectives(containerL);
	//reader=ctxt.getReader();
	//writer=ctxt.getWriter();
	//	pageInfo=new JspPageInfo(containerL, ctxt);
	//        this.ctxt = ctxt;
    }

    public ContainerLiaison getContainerLiaison() {
	return containerL;
    }

    public void setTemplateInfo(Mark start, Mark stop) {
    }


    public void beginPageProcessing() throws JasperException {
	// XXX not needed, standard imports should be part of JspPageInfo
	for(int i = 0; i < Constants.STANDARD_IMPORTS.length; i++)
	    pageInfo.addImport(Constants.STANDARD_IMPORTS[i]);
    }

    public void endPageProcessing() throws JasperException {
	writer.generateServlet(pageInfo);
    }
    

    // -------------------- Normal event listeners --------------------
    
    public void handleComment(Mark start, Mark stop) throws JasperException {
        containerL.message("jsp.message.htmlcomment",
                          new Object[] { reader.getChars(start, stop) },
                          Log.DEBUG);
    }

    public void handleDirective(String directive, Mark start,
				Mark stop, Hashtable attrs)
	throws JasperException
    {
        containerL.message("jsp.message.handling_directive",
                          new Object[] { directive, attrs },
                          Log.DEBUG);

	if (directive.equals("page")) {
	    pageD.handlePageDirective(this, directive,start, stop, attrs);
        }

	if (directive.equals("taglib")) {
            String uri = (String) attrs.get("uri");
            String prefix = (String) attrs.get("prefix");
            try {
		pageInfo.libraries.addTagLibrary( prefix, uri,
						  pageInfo.getJspFile() );
            } catch (Exception ex) {
                Object[] args = new Object[] { uri, ex.getMessage() };
                throw new CompileException(start, containerL.getString("jsp.error.badtaglib",
                                                              args));
            }
	}

	if (directive.equals("include")) {
	    String file = (String) attrs.get("file");
	    if (file == null)
		throw new CompileException(start,
					   containerL.getString("jsp.error.include.missing.file"));

	    
            // jsp.error.include.bad.file needs taking care of here??
            try {
                reader.pushFile(file);
		// Add an IncludeGenerator - only for deps
		DependGenerator dg=new DependGenerator( start,stop,
						reader.getCurrentFile());
		pageInfo.addGenerator( dg );
            } catch (FileNotFoundException fnfe) {
                throw new CompileException(start,
					   containerL.getString("jsp.error.include.bad.file"));
            }
	}
    }

    public void handleDeclaration(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen
            = new DeclarationGenerator(reader.getChars(start, stop));
	
	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleScriptlet(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen
            = new ScriptletGenerator(reader.getChars(start, stop));
	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleExpression(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen
            = new ExpressionGenerator(reader.getChars( start, stop));
	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleBean(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen
            = new BeanGenerator(start, attrs, pageInfo.beanInfo, pageInfo.genSessionVariable);

	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleBeanEnd(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen= new BeanEndGenerator();
	// End the block started by useBean body.
	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleGetProperty(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen
            = new GetPropertyGenerator(start, stop, attrs,pageInfo.beanInfo);
	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleSetProperty(Mark start, Mark stop, Hashtable attrs)
	throws JasperException
    {
        GeneratorBase gen
            = new SetPropertyGenerator(start, stop, attrs,pageInfo.beanInfo);

	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handlePlugin(Mark start, Mark stop, Hashtable attrs,
    				Hashtable param, String fallback)
	throws JasperException
    {
        containerL.message("jsp.message.handling_plugin",
                          new Object[] { attrs },
                          Log.DEBUG);

	GeneratorBase gen = new PluginGenerator (start, attrs,
					      param, fallback);
	gen.setMark( start, stop );
	pageInfo.addGenerator (gen);
    }

    public void handleForward(Mark start, Mark stop, Hashtable attrs,
			      Hashtable param)
	throws JasperException
    {
        GeneratorBase gen
            = new ForwardGenerator(start, attrs, param);

	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    public void handleInclude(Mark start, Mark stop, Hashtable attrs, Hashtable param)
	throws JasperException
    {
        GeneratorBase gen
            = new IncludeGenerator(start, attrs, param);

	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }


    public void handleCharData(Mark start, Mark stop, char[] chars) throws JasperException {
        GeneratorBase cdg;

        if (pageInfo.getOptions().getLargeFile())
            cdg = new StoredCharDataGenerator(pageInfo.vector,
					      pageInfo.getDataFile(),
					      pageInfo.stringId++, chars);
        else if(pageInfo.getOptions().getMappedFile())
            cdg = new MappedCharDataGenerator(chars);
	else
	    cdg = new CharDataGenerator(chars);


        GeneratorBase gen=cdg;
	gen.setMark( start, stop );

	pageInfo.addGenerator(gen);
    }

    public void handleTagBegin(Mark start, Mark stop, Hashtable attrs, String prefix,
			       String shortTagName, TagLibraryInfo tli,
			       TagInfo ti)
	throws JasperException
    {
        TagBeginGenerator tbg = new TagBeginGenerator(start, prefix, shortTagName, attrs,
	    tli, ti, pageInfo.libraries, pageInfo.getTagHandlerStack(), pageInfo.getTagVarNumbers());
        GeneratorBase gen = tbg;

	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
	
		
        // If this is the first tag, then generate code to store reference
        // to tag pool manager.
        if (pageInfo.tagPools.size() == 0) {
	    GeneratorBase tpg=new TagPoolManagerGenerator();
	    tpg.setMark( start, stop );
            pageInfo.addGenerator(tpg);
        }

        // if we haven't added a tag pool generator for this tag, then add one
        String tagPoolVarName = TagPoolGenerator.getPoolName(tli, ti, attrs);
        if (! pageInfo.tagPools.contains(tagPoolVarName)) {
            pageInfo.tagPools.addElement(tagPoolVarName);
            TagPoolGenerator tpg = new TagPoolGenerator(prefix, shortTagName, attrs, tli, ti);
            gen = tpg;
	    gen.setMark( start, stop );
	    pageInfo.addGenerator(gen);
        }
    }

    public void handleTagEnd(Mark start, Mark stop, String prefix,
			     String shortTagName, Hashtable attrs,
                             TagLibraryInfo tli, TagInfo ti)
	throws JasperException
    {
        TagEndGenerator teg = new TagEndGenerator(prefix, shortTagName, attrs,
	    tli, ti, pageInfo.libraries, pageInfo.getTagHandlerStack(), pageInfo.getTagVarNumbers());
        GeneratorBase gen = teg;
	gen.setMark( start, stop );
	pageInfo.addGenerator(gen);
    }

    // -------------------- Old methods --------------------
    public TagLibraries getTagLibraries() {
	return pageInfo.getTagLibraries();
    }


    /** @deprecated
     */
//     public static void setCommentGenerator(CommentGenerator generator) {
// 	if ( null == commentGenerator) {
// 	    throw new IllegalArgumentException("null == generator");
// 	}
// 	commentGenerator = generator;
//     }


}
