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
package org.apache.jasper34.generator;

import java.io.*;
import java.util.*;

import org.apache.jasper34.javagen.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.runtime.*;
import org.apache.jasper34.parser.*;
import org.apache.jasper34.jsptree.*;

/**
 * This is what is used to generate servlets.
 * Derived from the generic java code generator.
 *
 * @author Anil K. Vijendran
 * @author Costin Manolache
 */
public class ServletWriter extends JavaSourceGenerator {
    
    public ServletWriter(PrintWriter writer) {
	super( writer );
    }

    public void generateServlet(JspCompilationContext ctxt,
				JspPageInfo pageInfo )
	throws JasperException
    {
	// Do we need that ??
        ctxt.setContentType(pageInfo.servletContentType);

	generateHeader(pageInfo);

	generateClassDeclarations(pageInfo );
	
	generateStaticInit(pageInfo );

	generateConstructor(pageInfo );
	
	generateJspxInit( pageInfo );

	generateGetPageContext(  pageInfo );

	generateServiceMethod( pageInfo );
	
	generateJspService(pageInfo);

	this.generateClassFooter();

	// Generate additional file for large chunks
	generateChunksDat( pageInfo );
    }

    
    // -------------------- Code generators --------------------

    private void generateHeader(JspPageInfo pageInfo)
	throws JasperException
    {
        String servletPackageName = pageInfo.getServletPackageName();
        String servletClassName = pageInfo.getServletClassName();
	// First the package name:
	this.setPackage( servletPackageName );
	
	Enumeration e = pageInfo.imports.elements();
	while (e.hasMoreElements())
	    this.addImport((String) e.nextElement());

	this.generateHeader();
	
	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    //gen.startComment(this);
	    gen.generateFileDeclaration(this);
	    //gen.endComment(this);
	}

	this.setClassName( servletClassName );
	this.setExtendClass( pageInfo.extendsClass.equals("") ?
			     pageInfo.jspServletBase : pageInfo.extendsClass);
	
	if (pageInfo.singleThreaded)
	    pageInfo.interfaces.addElement("SingleThreadModel");
	
	if (pageInfo.interfaces.size() != 0) {
	    for(int i = 0; i < pageInfo.interfaces.size() ; i++)
		this.addInterface((String)pageInfo.interfaces.elementAt(i));
	}
	
	this.generateClassHeader();
    }
    
    private void generateClassDeclarations(JspPageInfo pageInfo)
	throws JasperException
    {
	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    //gen.startComment(this);
	    gen.generateClassDeclaration(this);
	    //gen.endComment(this);
	}

	this.println();
    }

    private void generateJspService(JspPageInfo pageInfo)
	throws JasperException
    {
	this.println("public void _jspService("+
		       "PageContext pageContext, " + 
		       "HttpServletRequest request, "+
		       "HttpServletResponse  response)");
	this.println("    throws Throwable ");
	this.println("{");

	this.pushIndent();
 	if (pageInfo.contentTypeDir == true)
	    this.println("response.setContentType(" +
			   this.quoteString(pageInfo.servletContentType)
			   + ");");
	else
	    this.println("response.setContentType(\"" +
			   pageInfo.servletContentType +
			   ";charset=8859_1\");");

	if (pageInfo.isErrorPage())
            this.println("Throwable exception = (Throwable) request.getAttribute(\"javax.servlet.jsp.jspException\");");
        this.println("Object page = this;");
	this.println("String  _value = null;");
	this.println("ServletContext application = pageContext.getServletContext();");
	this.println("ServletConfig config = pageContext.getServletConfig();");
        this.println("JspWriter out = pageContext.getOut();");

	if (pageInfo.genSessionVariable)
	    this.println("HttpSession session = pageContext.getSession();");

	this.println();

	// We can use tc hooks
	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    generateStartComment(gen);
	    gen.generateServiceMethod(this);
	    generateEndComment(gen);
	}
	this.println();
	this.popIndent();

	// Close the class definition:
	this.popIndent();
	this.println("}");
    }

    private void generateStaticInit(JspPageInfo pageInfo )
	throws JasperException
    {
	this.println("static {");
	this.pushIndent();

	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    //gen.startComment(this);
	    gen.generateStaticInitializer(this);
	    //gen.endComment(this);
	}

	this.popIndent();
	this.println("}");
    }

    private void generateConstructor(JspPageInfo pageInfo )
	throws JasperException
    {
        this.println("public "+ pageInfo.getServletClassName()+"( ) {");
        this.println("}");
        this.println();
    }

    
    private void generateJspxInit(JspPageInfo pageInfo )
	throws JasperException
    {
        this.println("private boolean _jspx_inited = false;");
        this.println();

	this.println("public final synchronized void _jspx_init() throws " +
		       Constants.JSP_RUNTIME_PACKAGE + ".JasperException {");
        this.pushIndent();
        this.println("if (! _jspx_inited) {");
        this.pushIndent();

	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    //gen.startComment(this);
	    gen.generateInitMethod(this);
	    //gen.endComment(this);
	}

        this.println("_jspx_inited = true;");
        this.popIndent();
        this.println("}");
        this.popIndent();
        this.println("}");
        this.println();
    }



    private void generateGetPageContext(JspPageInfo pageInfo )
	throws JasperException
    {
	this.println("public final PageContext _getPageContext(HttpServletRequest request, " + 
					       " HttpServletResponse response)");
	
	this.println( "{" );
	this.pushIndent();
	
	// protected field _jspxFactory already defined in HttpJspBase
	if( ! pageInfo.extendsClass.equals("") )
	    this.println("JspFactory _jspxFactory = JspFactory.getDefaultFactory();");

	
	this.println("return _jspxFactory.getPageContext(this, request, response,\n"
		       + "\t\t\t"
		       + this.quoteString(pageInfo.error) + ", "
		       + pageInfo.genSessionVariable + ", "
		       + pageInfo.bufferSize + ", "
		       + pageInfo.autoFlush
		       + ");");
	this.popIndent();
	this.println("}");
	this.println();
    }


    /**  Generate serviceMethod - as required by the spec
	 Used only if the page extends something else than HttpJspBase
	 If HttpJspBase is used, the code is not needed, as it
	 replicates the code in it.
    */
    private void generateServiceMethod(JspPageInfo pageInfo )
	throws JasperException
    {
	// if extends HttpJspBase, this method is already defined in supper
	if( pageInfo.extendsClass.equals("") )
	    return;
	    
	this.println();
    
	this.println("public void "+pageInfo.serviceMethodName+"("+
		     "HttpServletRequest request, "+
		     "HttpServletResponse  response)");
	this.println("    throws java.io.IOException, ServletException {");
	this.pushIndent();
    
	this.println();
	this.println("JspFactory _jspxFactory = JspFactory.getDefaultFactory();");
	this.println("PageContext pageContext = null;");
	this.println("JspWriter out=null;");
	
	this.println("try {");
	this.pushIndent();
        this.println("try {");
        this.pushIndent();
        
	this.println();
        this.println("_jspx_init();");
	
	this.println("pageContext = _getPageContext(request, response);");
	this.println("out=pageContext.getOut();");
	this.println();
	this.println("_jspService( pageContext, request, response );");
	//writer.println("} catch (Throwable t) {");
	this.popIndent();
  	this.println("} catch (Exception ex) {");
	this.pushIndent();
	// Used to have a clearBuffer here, but it's moved in handlePageEx
	this.println("if (pageContext != null) pageContext.handlePageException(ex);");
	this.popIndent();
        this.println("} catch (Error error) {");
        this.pushIndent();
        this.println("throw error;");
        this.popIndent();
        this.println("} catch (Throwable throwable) {");
        this.pushIndent();
        this.println("throw new ServletException(throwable);");
        this.popIndent();
        this.println("}");
        this.popIndent();
	this.println("} finally {");
	this.pushIndent();
	// Do stuff here for finally actions... 
	//writer.println("out.close();");
	
	// Use flush buffer ( which just empty JspWriterImpl buffer )
	// instead of commiting the response.
	this.println("if( out instanceof " + Constants.JSP_RUNTIME_PACKAGE +
		     ".JspWriterImpl )");
	this.pushIndent();
	this.println("((" +  Constants.JSP_RUNTIME_PACKAGE +
		     ".JspWriterImpl)out).flushBuffer();");
	this.popIndent();
	this.println("if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);");
	this.popIndent();
	this.println("}");
	// Close the service method:
	this.popIndent();
	this.println("}");
    
	this.println();
    }
    
    
        
//     /**
//      * Print a standard comment for echo outputed chunk.
//      * @param start The starting position of the JSP chunk being processed. 
//      * @param stop  The ending position of the JSP chunk being processed. 
//      */
//     public void printComment(Mark start, Mark stop, char[] chars) {
//         if (start != null && stop != null) {
// 	    println("// from="+start);
//             println("//   to="+stop);
//         }
        
//         if (chars != null)
//             for(int i = 0; i < chars.length;) {
//                 indent();
//                 print("// ");
//                 while (chars[i] != '\n' && i < chars.length)
//                     writer.print(chars[i++]);
// 		println();
//             }
//     }

    private void generateChunksDat(JspPageInfo pageInfo )
	throws JasperException
    {
	
        if (pageInfo.ctxt.getOptions().getLargeFile()) {
            try {
		FileOutputStream fos=new FileOutputStream(pageInfo.dataFile);
                ObjectOutputStream o
                    = new ObjectOutputStream(fos);
		
                /*
                 * Serialize an array of char[]'s instead of an
                 * array of String's because there is a limitation
                 * on the size of Strings that can be serialized.
                 */
                char[][] tempCharArray = new char[pageInfo.vector.size()][];
                pageInfo.vector.copyInto(tempCharArray);
                o.writeObject(tempCharArray);
                o.close();
                this.close();
            } catch (IOException ex) {
                throw new JasperException(Constants.getString("jsp.error.data.file.write"), ex);
            }
	}
    }

    // -------------------- Generate comments --------------------
    // The code generator also maintains line number info. Right now we generate
    // some comments, later we'll add real mappings

    /**
     * Generates "start-of the JSP-embedded code block" comment
     *
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException 
     */
    public void generateStartComment(GeneratorBase generator )
        throws JasperException 
    {
	// XXX Use emacs style or something common
	Mark start=generator.start;
	Mark stop=generator.stop;
	String html = "";
        if (generator instanceof CharDataGenerator) {
	   html = "// HTML ";
	}
 	if (start != null && stop != null) {
	    if (start.getFile().equals( stop.getFile())) {
		String fileName = this.quoteString(start.getFile ());
		this.println(html + "// begin [file=" + fileName+";from=" +
			     toShortString(start) + ";to=" +
			     toShortString(stop) + "]");
	    } else {
		this.println(html + "// begin [from="+toString(start)+
			     ";to="+toString(stop)+"]");
            }
	} else {
	    this.println(html + "// begin");
        }

	//      this.pushIndent();
    }

   /**
     * Generates "end-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException
     */
    public void generateEndComment(GeneratorBase generator)
	throws JasperException
    {
	//	this.popIndent();
        this.println("// end");
    }

    // The format may change
    private String toShortString( Mark mark ) {
        return "("+mark.getLineNumber() + ","+mark.getColumnNumber() +")";
    }

    // 
    private String toString( Mark mark ) {
	return mark.getSystemId()+"("+mark.getLineNumber()+","+mark.getColumnNumber() +")";
    }
    
    
}
