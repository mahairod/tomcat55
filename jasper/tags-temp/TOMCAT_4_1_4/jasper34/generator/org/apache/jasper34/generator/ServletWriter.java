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
    ContainerLiaison containerL;
    
    public ServletWriter(PrintWriter writer) {
	super( writer );
    }

    public void generateServlet(JspPageInfo pageInfo )
	throws JasperException
    {
	containerL=pageInfo.getContainerLiaison();
	// Do we need that ??
	//???        ctxt.setContentType(pageInfo.servletContentType);

	generateHeader(pageInfo);

	generateClassDeclarations(pageInfo );
	
	generateStaticInit(pageInfo );

	generateDepends(pageInfo );
	
	generateChunks(pageInfo );

	generateConstructor(pageInfo );
	
	generateJspxInit( pageInfo );

	generateGetPageContext(  pageInfo );

	generateServiceMethod( pageInfo );
	
	generateJspService(pageInfo);

	generateLineMap(pageInfo);

	this.generateClassFooter();

	// Generate additional file for large chunks
	generateChunksDat( pageInfo );
    }

    
    // -------------------- Code generators --------------------

    private void generateHeader(JspPageInfo pageInfo)
	throws JasperException
    {
        String servletPackageName =
	    pageInfo.getMangler().getPackageName();
        String servletClassName =
	    pageInfo.getMangler().getClassName();
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
	this.println("public void _jspService( PageContext pageContext,");
	this.println( "\t\tHttpServletRequest request, ");
	this.println("\t\tHttpServletResponse  response)");
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
	    generateStartComment(pageInfo,gen);
	    gen.generateServiceMethod(this);
	    generateEndComment(pageInfo,gen);
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
        this.println("public "+
		     pageInfo.getMangler().getClassName()+"( ) {");
        this.println("}");
        this.println();
    }

    
    private void generateJspxInit(JspPageInfo pageInfo )
	throws JasperException
    {
        this.println("private boolean _jspx_inited = false;");
        this.println();

	this.println("public final synchronized void _jspx_init()");
        this.pushIndent();
	this.println("throws " +
		     Constants.JSP_RUNTIME_PACKAGE + ".JasperException ");
	this.popIndent();
	this.println("{");
	
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


    private void generateDepends(JspPageInfo pageInfo )
	throws JasperException
    {
        this.println("private static final String _depends[] = { ");
	this.pushIndent();

	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    gen.generateDepends(this);
	}

	this.println("null");
		
	this.popIndent();
	this.println("}; ");
        this.println();
	this.println("public final String[] _getDepends() " +
	" { return _depends; }");
        this.println();

    }

    private void generateChunks(JspPageInfo pageInfo )
	throws JasperException
    {
        this.println("private static final String _chunks[] = { ");
	this.pushIndent();

	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    gen.generateChunks(this);
	}

	this.println("null");
	
	this.popIndent();
	this.println("}; ");
        this.println();

	this.println("public final String[] _getChunks() " +
	" { return _chunks; }");
        this.println();

    }



    private void generateGetPageContext(JspPageInfo pageInfo )
	throws JasperException
    {
	this.println("public final PageContext " +
		     "_getPageContext(HttpServletRequest request,");
	this.pushIndent();
	this.println("HttpServletResponse response)");
	this.popIndent();
	
	this.println( "{" );
	this.pushIndent();
	
	// protected field _jspxFactory already defined in HttpJspBase
	if( ! pageInfo.extendsClass.equals("") )
	    this.println("JspFactory _jspxFactory = JspFactory.getDefaultFactory();");

	
	this.println("return _jspxFactory.getPageContext(this, " +
		     " request, response,");
	this.println("\t\t\t"
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
	
        if (pageInfo.getOptions().getLargeFile()) {
            try {
		FileOutputStream fos=
		    new FileOutputStream(pageInfo.getDataFile());
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
                throw new JasperException(containerL.getString("jsp.error.data.file.write"), ex);
            }
	}
    }

    // -------------------- Generate comments --------------------
    // The code generator also maintains line number info.

    // experimental - ServletWriter can generate the line numbers
    // ( the way it generates the comments )
    StringBuffer internalLineMap=new StringBuffer();
    StringBuffer internalFileMap=new StringBuffer();
    Vector internalFileRegister=new Vector();

    /**
     * Generates "start-of the JSP-embedded code block" comment
     *
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException 
     */
    public void generateStartComment(JspPageInfo pageInfo,
				     GeneratorBase generator )
        throws JasperException 
    {
	// XXX Use emacs style or something common
	Mark start=generator.start;
	Mark stop=generator.stop;
	String html = "";
	int javaStart=this.getJavaLine() + 1;

	if( pageInfo.getOptions().getGenerateCommentMapping() ) {
	    if (generator instanceof CharDataGenerator) {
		html = "// HTML " + javaStart;
	    } else {
		html = "// " + javaStart;
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
	}
	
	javaStart=this.getJavaLine();
	internalLineMap.append("{").append(javaStart ).append(",");
	
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
    public void generateEndComment(JspPageInfo pageInfo,
				   GeneratorBase generator)
	throws JasperException
    {
	//	this.popIndent();
	int javaEnd=this.getJavaLine();
	internalLineMap.append(javaEnd ).append(",");
	// We have javaStart, javaEnd: add the mapping

	Mark start=generator.start;
	Mark stop=generator.stop;

	internalLineMap.append( registerFile( start.getSystemId() ) ).
	    append(",");
	internalLineMap.append( start.getLineNumber() ).append(",");
	internalLineMap.append( start.getColumnNumber() ).append(",");

	internalLineMap.append( registerFile( stop.getSystemId() ) ).
	    append(",");
	internalLineMap.append( stop.getLineNumber() ).append(",");
	internalLineMap.append( stop.getColumnNumber() );
	
	internalLineMap.append("},\n");
	
	if( pageInfo.getOptions().getGenerateCommentMapping() ) {
	    this.println("// end " + javaEnd );
	}
    }
    
    private void generateLineMap(JspPageInfo pageInfo )
	throws JasperException
    {
	this.pushIndent();
        this.println("private static final int _lineMap[][] = { ");
	this.pushIndent();

	this.printMultiLn( internalLineMap.toString() );
	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    gen.generateLineMap(this);
	}

	this.println("null");
		
	this.popIndent();
	this.println("}; ");
        this.println();
	this.println("public final int[][] _getLineMap() " +
	" { return _lineMap; }");
        this.println();

	// -------------------- File map --------------------
        this.println("private static final String _fileMap[] = { ");
	this.pushIndent();

	this.printMultiLn( internalFileMap.toString() );
	for(int i = 0; i < pageInfo.generators.size(); i++) {
	    GeneratorBase gen=(GeneratorBase)pageInfo.generators.elementAt(i);
	    gen.generateFileMap(this);
	}

	this.println("null");
		
	this.popIndent();
	this.println("}; ");
        this.println();
	this.println("public final String[] _getFileMap() " +
	" { return _fileMap; }");
        this.println();

	this.popIndent();

    }



    private int registerFile( String s ) {
	int idx=internalFileRegister.indexOf( s );
	// 	System.out.println("ServletWriter.registerFile found " +
	// 			   idx + " " + s );
	if( idx>=0 ) return idx;

	s=s.replace( '\\', '/' );
	internalFileRegister.addElement( s );
	idx=internalFileRegister.size() -1 ; // added item
	internalFileMap.append("\"").append( s ).append("\" ,\n");
	// System.out.println("ServletWriter.registerFile " + idx + " " + s );
	return idx;
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
