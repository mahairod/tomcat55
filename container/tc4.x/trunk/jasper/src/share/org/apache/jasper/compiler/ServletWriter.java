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

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

/**
 * This is what is used to generate servlets. 
 *
 * @author Anil K. Vijendran
 */
public class ServletWriter {
    public static int TAB_WIDTH = 2;
    public static String SPACES = "                              ";

    // Current indent level:
    private int indent = 0;
    private int virtual_indent = 0;

    // The sink writer:
    PrintWriter writer;
    
    // servlet line numbers start from 1, but we pre-increment
    private int javaLine = 0;
    private JspLineMap lineMap = new JspLineMap();


    public ServletWriter(PrintWriter writer) {
	this.writer = writer;
    }

    public void close() throws IOException {
	writer.close();
    }
    
    // -------------------- Access informations --------------------

    public int getJavaLine() {
        return javaLine;
    }

    public void setLineMap(JspLineMap map) {
        this.lineMap = map;
    }

    public JspLineMap getLineMap() {
        return lineMap;
    }

    // -------------------- Formatting --------------------

    public void pushIndent() {
	virtual_indent += TAB_WIDTH;
	if (virtual_indent >= 0 && virtual_indent <= SPACES.length())
	    indent = virtual_indent;
    }

    public void popIndent() {
	virtual_indent -= TAB_WIDTH;
	if (virtual_indent >= 0 && virtual_indent <= SPACES.length())
	    indent = virtual_indent;
    }

    /**
     * Print a standard comment for echo outputed chunk.
     * @param start The starting position of the JSP chunk being processed. 
     * @param stop  The ending position of the JSP chunk being processed. 
     */
    public void printComment(Mark start, Mark stop, char[] chars) {
        if (start != null && stop != null) {
            println("// from="+start);
            println("//   to="+stop);
        }
        
        if (chars != null)
            for(int i = 0; i < chars.length;) {
                indent();
                print("// ");
                while (chars[i] != '\n' && i < chars.length)
                    writer.print(chars[i++]);
            }
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

    public void println(String line) {
        javaLine++;
	writer.println(SPACES.substring(0, indent)+line);
    }

    public void println() {
        javaLine++;
	writer.println("");
    }

    public void indent() {
	writer.print(SPACES.substring(0, indent));
    }
    

    public void print(String s) {
        int index = 0;

        // look for hidden newlines inside strings
        while ((index=s.indexOf('\n',index)) > -1 ) {
            javaLine++;
            index++;
        }

	writer.print(s);
    }

    public void printMultiLn(String multiline) {
	// Try to be smart (i.e. indent properly) at generating the code:
	BufferedReader reader = 
            new BufferedReader(new StringReader(multiline));
	try {
    	    for (String line = null ; (line = reader.readLine()) != null ; ) {
		//		println(SPACES.substring(0, indent)+line);
		println(line);
            }
	} catch (IOException ex) {
	    // Unlikely to happen, since we're acting on strings
	}
    }


}
