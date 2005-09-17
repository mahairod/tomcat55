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

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;

/**
 * JSP Comment Generator
 *
 * @author Mandar Raje [Patch submitted by Yury Kamen]
 */

public interface CommentGenerator {
    
    /**
     * Generates "start-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException
     */
    void generateStartComment(Generator generator, ServletWriter out, Mark start, Mark stop) throws JasperException;

    /**
     * Generates "end-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException 
     */
    void generateEndComment(Generator generator, ServletWriter out, Mark start, Mark stop) throws JasperException;
}
