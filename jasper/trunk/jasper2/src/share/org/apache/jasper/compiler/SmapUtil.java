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

package org.apache.jasper.compiler;

import java.io.*;
import java.util.*;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.Node;

/**
 * Contains static utilities for generating SMAP data based on the
 * current version of Jasper.
 * 
 * @author Jayson Falkner
 * @author Shawn Bayern
 * @author Robert Field (inner SDEInstaller class)
 * @author Mark Roth
 */
public class SmapUtil {

    static final boolean verbose = false;

    //*********************************************************************
    // Constants

    public static final String SMAP_ENCODING = "UTF-8";

    //*********************************************************************
    // Public entry points

    /**
     * Generates an appropriate SMAP representing the current compilation
     * context and optionally installs in the target .class file.  (JSR-045.)
     * If the keepGenerated flag is set in the compilation context, the
     * generated .smap file will remain.  Otherwise, it will be deleted.
     *
     * @param install True if the SourceDebugExtension is to be installed
     *     in the generated .class file, or false if not.
     */
    public static void generateSmap(JspCompilationContext ctxt,
                                              Node.Nodes pageNodes,
					      boolean install )
        throws IOException 
    {
	// set up our SMAP generator
	SmapGenerator g = new SmapGenerator();

	// determine if we have an input SMAP
	String smapPath = inputSmapPath(ctxt.getRealPath(ctxt.getJspFile()));
        File inputSmap = new File(smapPath);
        if (inputSmap.exists()) {
        byte[] embeddedSmap = null;
	    byte[] subSmap = SDEInstaller.readWhole(inputSmap);
	    String subSmapString = new String(subSmap, SMAP_ENCODING);
	    g.addSmap(subSmapString, "JSP");
	}

        // now, assemble info about our own stratum (JSP) using JspLineMap
        SmapStratum s = new SmapStratum("JSP");

        g.setOutputFileName(unqualify(ctxt.getServletJavaFileName()));
        // recursively map out Node.Nodes, it seems ugly..but its the only way??
        evaluateNodes(pageNodes, s);
        g.addStratum(s, true);

	/*
         * Save the output to a temporary file (just so as to use
         * Robert's interface -- so that I don't have to keep changing
         * this code if he updates it).  TODO:  We could do this more
         * gracefully later.  But writing out the SMAP to a known
	 * filename (servlet.class.smap) will also make troubleshooting
         * easier.
	 */

        File outSmap = new File(ctxt.getClassFileName() + ".smap");
	PrintWriter so = new PrintWriter(
	    new OutputStreamWriter(new FileOutputStream(outSmap),
				   SMAP_ENCODING));
	so.print(g.getString());
	so.close();
    }
    
    public static void installSmap(JspCompilationContext ctxt) throws IOException {
        File outSmap = new File(ctxt.getClassFileName() + ".smap");
        File outServlet = new File(ctxt.getClassFileName());
        SDEInstaller.install(outServlet, outSmap);
        if( !ctxt.keepGenerated() ) {
            outSmap.delete();
        }
      
    }


    //*********************************************************************
    // Private utilities

    /**
     * Returns an unqualified version of the given file path.
     */
    private static String unqualify(String path) {
	return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * Returns a file path corresponding to a potential SMAP input
     * for the given compilation input (JSP file).
     */
    private static String inputSmapPath(String path) {
        return path.substring(0, path.lastIndexOf('.') + 1) + "smap";
    }


    //*********************************************************************
    // Installation logic (from Robert Field, JSR-045 spec lead)
    private static class SDEInstaller {

        static final String nameSDE = "SourceDebugExtension";

        byte[] orig;
        byte[] sdeAttr;
        byte[] gen;

        int origPos = 0;
        int genPos = 0;

        int sdeIndex;

        public static void main(String[] args) throws IOException {
            if (args.length == 2) {
                install(new File(args[0]), new File(args[1]));
            } else if (args.length == 3) {
                install(new File(args[0]), new File(args[1]), new File(args[2]));
            } else {
                abort("Usage: <command> <input class file> " + 
                                   "<attribute file> <output class file name>\n" +
                      "<command> <input/output class file> <attribute file>");
            }
        }

        static void install(File inClassFile, File attrFile, File outClassFile)
                                                                throws IOException {
            new SDEInstaller(inClassFile, attrFile, outClassFile);
        }

        static void install(File inOutClassFile, File attrFile) throws IOException {
            File tmpFile = new File(inOutClassFile.getPath() + "tmp");
            new SDEInstaller(inOutClassFile, attrFile, tmpFile);
            if (!inOutClassFile.delete()) {
                throw new IOException("inOutClassFile.delete() failed");
            }
            if (!tmpFile.renameTo(inOutClassFile)) {
                throw new IOException("tmpFile.renameTo(inOutClassFile) failed");
            }
        }

        static void abort(String msg) {
            System.err.println(msg);
            System.exit(1);
        }

        SDEInstaller(File inClassFile, File attrFile, File outClassFile) throws IOException {
            if (!inClassFile.exists()) {
                abort("no such file: " + inClassFile);
            }
            if (!attrFile.exists()) {
                abort("no such file: " + attrFile);
            }
 
            // get the bytes
            orig = readWhole(inClassFile);
            sdeAttr = readWhole(attrFile);
            gen = new byte[orig.length + sdeAttr.length + 100];
    
            // do it
            addSDE();
        
            // write result
            FileOutputStream outStream = new FileOutputStream(outClassFile);
            outStream.write(gen, 0, genPos);
            outStream.close();
        }

        static byte[] readWhole(File input) throws IOException {
            FileInputStream inStream = new FileInputStream(input);
            int len = (int)input.length();
            byte[] bytes = new byte[len];
            if (inStream.read(bytes, 0, len) != len) {
                abort("expected size: " + len);
            }
            inStream.close();
            return bytes;
        }

        void addSDE() throws UnsupportedEncodingException {
            int i;
            copy(4 + 2 + 2); // magic min/maj version
            int constantPoolCountPos = genPos;
            int constantPoolCount = readU2();
            writeU2(constantPoolCount);
            // copy old constant pool return index of SDE symbol, if found
            sdeIndex = copyConstantPool(constantPoolCount);
            if (sdeIndex < 0) {
                // if "SourceDebugExtension" symbol not there add it
                writeUtf8ForSDE();
                
                // increment the countantPoolCount
                sdeIndex = constantPoolCount;
                ++constantPoolCount;
                randomAccessWriteU2(constantPoolCountPos, constantPoolCount);
            
                if (verbose) {
                    System.out.println("SourceDebugExtension not found, installed at: " +
                                       sdeIndex);
                }
            } else {
                if (verbose) {
                    System.out.println("SourceDebugExtension found at: " +
                                       sdeIndex);
                }
            }
            copy(2 + 2 + 2);  // access, this, super
            int interfaceCount = readU2();
            writeU2(interfaceCount);
            if (verbose) {
                System.out.println("interfaceCount: " + interfaceCount);
            }
            copy(interfaceCount * 2);
            copyMembers(); // fields
            copyMembers(); // methods
            int attrCountPos = genPos;
            int attrCount = readU2();
            writeU2(attrCount);
            if (verbose) {
                System.out.println("class attrCount: " + attrCount);
            }
            // copy the class attributes, return true if SDE attr found (not copied)
            if (!copyAttrs(attrCount)) {
                // we will be adding SDE and it isn't already counted
                ++attrCount;
                randomAccessWriteU2(attrCountPos, attrCount);
                if (verbose) {
                    System.out.println("class attrCount incremented");
                }
            }
            writeAttrForSDE(sdeIndex);
        }

        void copyMembers() {
            int count = readU2();
            writeU2(count);
            if (verbose) {
                System.out.println("members count: " + count);
            }
            for (int i = 0; i < count; ++i) {
                copy(6); // access, name, descriptor
                int attrCount = readU2();
                writeU2(attrCount);
                if (verbose) {
                    System.out.println("member attr count: " + attrCount);
                }
                copyAttrs(attrCount);
            }
        }

        boolean copyAttrs(int attrCount) {
            boolean sdeFound = false;
            for (int i = 0; i < attrCount; ++i) {
                int nameIndex = readU2();
                // don't write old SDE
                if (nameIndex == sdeIndex) {
                    sdeFound = true;
                    if (verbose) {
                        System.out.println("SDE attr found");
                    }
                } else {
                    writeU2(nameIndex);  // name
                    int len = readU4();
                    writeU4(len);
                    copy(len);
                    if (verbose) {
                        System.out.println("attr len: " + len);
                    }
                }
            }
            return sdeFound;
        }

        void writeAttrForSDE(int index) {
            writeU2(index);
            writeU4(sdeAttr.length);
            for (int i = 0; i < sdeAttr.length; ++i) {
                writeU1(sdeAttr[i]);
            }
        }

        void randomAccessWriteU2(int pos, int val) {
            int savePos = genPos;
            genPos = pos;
            writeU2(val);
            genPos = savePos;
        }

        int readU1() {
            return ((int)orig[origPos++]) & 0xFF;
        }

        int readU2() {
            int res = readU1();
            return (res << 8) + readU1();
       }
     
        int readU4() {
            int res = readU2();
            return (res << 16) + readU2();
        }

        void writeU1(int val) {
            gen[genPos++] = (byte)val;
        }

        void writeU2(int val) {
            writeU1(val >> 8);
            writeU1(val & 0xFF);
        }

        void writeU4(int val) {
            writeU2(val >> 16);
            writeU2(val & 0xFFFF);
        }
    
        void copy(int count) {
            for (int i = 0; i < count; ++i) {
                gen[genPos++] = orig[origPos++];
            }
        }
    
        byte[] readBytes(int count) {
            byte[] bytes = new byte[count];
            for (int i = 0; i < count; ++i) {
                bytes[i] = orig[origPos++];
            }
            return bytes;
        }
    
        void writeBytes(byte[] bytes) {
            for (int i = 0; i < bytes.length; ++i) {
                gen[genPos++] = bytes[i];
            }
        }
    
        int copyConstantPool(int constantPoolCount) throws UnsupportedEncodingException {
            int sdeIndex = -1;
            // copy const pool index zero not in class file
            for (int i = 1; i < constantPoolCount; ++i) {
                int tag = readU1();
                writeU1(tag);
                switch (tag) {
                    case 7:  // Class
                    case 8:  // String
                        copy(2); 
                        break;
                    case 9:  // Field
                    case 10: // Method
                    case 11: // InterfaceMethod
                    case 3:  // Integer
                    case 4:  // Float
                    case 12: // NameAndType
                        copy(4); 
                        break;
                    case 5:  // Long
                    case 6:  // Double
                        copy(8); 
                        break;
                    case 1:  // Utf8
                        int len = readU2(); 
                        writeU2(len);
                        byte[] utf8 = readBytes(len);
                        String str = new String(utf8, "UTF-8");
                        if (verbose) {
                            System.out.println(i + " read class attr -- '" + str + "'");
                        }
                        if (str.equals(nameSDE)) {
                            sdeIndex = i;
                        }
                        writeBytes(utf8);
                        break;
                    default: 
                        abort("unexpected tag: " + tag); 
                        break;
                }
            }
            return sdeIndex;
        }

        void writeUtf8ForSDE() {
            int len = nameSDE.length();
            writeU1(1); // Utf8 tag
            writeU2(len);
            for (int i = 0; i < len; ++i) {
                writeU1(nameSDE.charAt(i));
            }
        }
    }
    public static void evaluateNodes(Node.Nodes nodes, SmapStratum s) {
      if( nodes != null && nodes.size()>0) {
        int numChildNodes = nodes.size();
        for( int i = 0; i < numChildNodes; i++ ) {
          Node n = nodes.getNode( i );
          Mark mark = n.getStart();

          if (verbose) {
            System.out.println("Mark(start): line="+ mark.getLineNumber() +
                " col="+mark.getColumnNumber() +"Node: begLine="+
                n.getBeginJavaLine() +" endLine="+n.getEndJavaLine());
          }
          String unqualifiedName = unqualify(mark.getFile());
          s.addFile(unqualifiedName);
          s.addLineData(mark.getLineNumber(),
                        unqualifiedName,
                        1,
                        n.getBeginJavaLine(),
                        n.getEndJavaLine() - n.getBeginJavaLine());
          evaluateNodes(nodes.getNode(i).getBody(), s);

/*
int inputStartLine,
String inputFileName,
int inputLineCount,
int outputStartLine,
int outputLineIncrement
*/
        }
      }
    }

}
