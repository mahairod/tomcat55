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
package org.apache.jasper34.javagen;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
//import org.apache.jasper34.generator.Mark;
    
/**
 * This is what is used to generate java sources. It knows how to indent
 * and will record line numbers and the match in the original source.
 *
 * @author Anil K. Vijendran
 * @author Costin Manolache
 */
public class JavaSourceGenerator {
    
    //    public static int TAB_WIDTH = 4;
    public static int INDENT = 2;
    public static String SPACES = "                              ";

    // Current indent level:
    protected int indent = 0;

    // line numbers start from 1, but we pre-increment
    protected int javaLine=0;
    
    // The sink writer:
    private PrintWriter writer;

    private Vector sourceFiles=new Vector();
    private int lineMappings[][];

    public JavaSourceGenerator(PrintWriter writer) {
	this.writer = writer;
    }

    public void close() throws IOException {
	writer.close();
    }

    // -------------------- Generator tunning --------------------
    boolean includeLineNumber=true;

    public void setGenerateLineNumbers(boolean b ) {
	
    }

    // -------------------- Access informations --------------------

    public int getJavaLine() {
	return javaLine;
    }

    // -------------------- Prepare generator --------------------

    // File components 
    String packageName;
    Vector imports=new Vector();
    Vector interfaces=new Vector();
    String className;
    String extendClass;

    public void setPackage( String p ) {
	this.packageName=p;
    }

    public void addImport( String s ) {
	imports.addElement( s );
    }

    public void setClassName( String classN ) {
	this.className=classN;
    }

    public void setExtendClass( String s ) {
	extendClass=s;
    }

    public void addInterface(String s) {
	interfaces.addElement( s );
    }

    // -------------------- High level generator --------------------
    public boolean needCompiler=false;
    
    public void generateHeader() {
	if( packageName != null && !"".equals(packageName)) {
	    println( "package " + packageName + ";");
	    println();
	}

	Enumeration e = imports.elements();
	while (e.hasMoreElements())
	    println("import "+(String) e.nextElement()+";");

	println();
    }

    public void generateClassHeader() {
	println("public class "+ className );
	if( extendClass != null ) {
	    pushIndent();
	    indent();
	    printlnNI( "extends " + extendClass );
	    popIndent();
	}

	if (interfaces.size() != 0) {
	    pushIndent();
	    indent();
	    print("implements ");
	    for(int i = 0; i < interfaces.size() - 1; i++)
		print(" "+interfaces.elementAt(i)+",");
	    printlnNI(" "+interfaces.elementAt(interfaces.size()-1));
	    popIndent();
	}

	println( "{" );
	pushIndent();
    }

    public void generateConstructor() {
        println("public "+className+"( ) {");
        println("}");
        println();
    }

    public void generateClassFooter() {
	// Generate line mapping 

	popIndent();
	println( "}" );
    }

    // -------------------- Line mapping --------------------
    

    

    // -------------------- Low level generator --------------------
    
    /** Increase indentation level for next lines.
     *  Maximum 30 spaces.
     */
    public void pushIndent() {
	if ((indent += INDENT) > SPACES.length())
	    indent = SPACES.length();
    }
    
    public void popIndent() {
	if ((indent -= INDENT) <= 0 )
	    indent = 0;
    }

    /**
     * Quote the given string to make it appear in a chunk of java code.
     * @param s The string to quote.
     * @return The quoted string.
     */
    public String quoteString(String s) {
	// Turn null string into quoted empty strings:
	if ( s == null )
	    return "null";
	// Hard work:
	if ( s.indexOf('"') < 0 && s.indexOf('\\') < 0 && s.indexOf ('\n') < 0
	     && s.indexOf ('\r') < 0)
	    return "\""+s+"\"";
	StringBuffer sb  = new StringBuffer();
	int          len = s.length();
	sb.append('"');
	for (int i = 0 ; i < len ; i++) {
	    char ch = s.charAt(i);
	    if ( ch == '\\' && i+1 < len) {
		sb.append('\\');
		sb.append('\\');
		sb.append(s.charAt(++i));
	    } else if ( ch == '"' ) {
		sb.append('\\');
		sb.append('"');
	    } else if (ch == '\n') {
	        sb.append ("\\n");
	    }else if (ch == '\r') {
	   	sb.append ("\\r");
	    }else {
		sb.append(ch);
	    }
	}
	sb.append('"');
	return sb.toString();
    }

    /** Add a line to the java source
     */
    public void println(String line) {
	javaLine++;
	if( line.indexOf( '\n' ) >= 0 ) {
	    System.out.println("Warning, wrong line " + line );
	    /*DEBUG*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
	}
	writer.println(SPACES.substring(0, indent)+line);
    }

    /** Println with no indentation
     */
    public void printlnNI(String line) {
	javaLine++;
	if( line.indexOf( '\n' ) >= 0 ) {
	    System.out.println("Warning, wrong line " + line );
	    /*DEBUG*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
	}
	writer.println(line);
    }

    /** Add an empty  line to the java source
     */ 
    public void println() {
	javaLine++;
	writer.println("");
    }

    public void indent() {
	writer.print(SPACES.substring(0, indent));
    }
    
    
    public void print(String s) {
	if( s.indexOf( '\n' ) >= 0 ) {
	    System.out.println("Warning, wrong line " + s );
	    /*DEBUG*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
	}
	writer.print(s);
    }

    /** Print a java fragment. 
     */
    public void printMultiLn(String multiline) {
	// Try to be smart (i.e. indent properly) at generating the code:
	BufferedReader reader = 
            new BufferedReader(new StringReader(multiline));
	try {
    	    for (String line = null ; (line = reader.readLine()) != null ; ) 
		println(line.trim());
	} catch (IOException ex) {
	    // Unlikely to happen, since we're acting on strings
	}
    }


}
