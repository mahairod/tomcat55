/*
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

package org.apache.jasper34.jsptree;

import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.JasperException;

import java.lang.reflect.*;
import java.util.*;

import org.apache.jasper34.parser.*;
import org.apache.jasper34.generator.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

// XXX Not used right now - it's work in progress, part of generator
// refactoring to allow multiple generation schemes and improve modularity

/**
 * AST-like representation of a JSP page.
 * 
 * Refactored from GeneratorBase and generators - part of moving
 * to a visitor pattern and decoupling code generators from the rest
 * of the processing ( later we'll have multiple code generators -
 * some pages can be directly translated )
 *
 * It has all the properties that were stored in the Generator trees.
 * ( we'll revisit this later )
 *
 * @author Anil K. Vijendran
 * @author Costin Manolache
 */
public class JspNode {
    // This will be used to pick the right generator
    String type; 
    
    // Location in the source
    Mark start, stop;

    Hashtable attrs;
    char body[];

    // Do we need this ?
    JspPageInfo pageInfo;
    Hashtable params; // for Plugin, Forward, Include
    String fallback;  // for Plugin

    // tag info
    String prefix;
    String shortTagName;
    TagLibraryInfo tli;
    TagInfo ti;

    /* We may need to store the current instance of:
       - pageInfo.vector  // CharData
       - pageInfo.dataFile // CharData
       - pageInfo.stringId // CharData

       - pageInfo.beanInfo // Bean, GetProperty, SetProperty
       - pageInfo.sessionVariable // Bean

       - pageInfo.getTagHandlerStack() // Tag
       - pageInfo.getTagVarNumbers()
       - pageInfo.libraries


    */
    
    JspNode( JspPageInfo pageInfo, char body[],
	     Hashtable attrs,
	     Mark start, Mark stop) {
	this.pageInfo=pageInfo;
	this.start = start;
	this.stop = stop;
	this.body = body;
	this.attrs=attrs;
    }

    // --------------------

}
