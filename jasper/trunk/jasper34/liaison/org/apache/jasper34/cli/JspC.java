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

package org.apache.jasper34.cli;

import java.io.*;
import java.util.*;

import org.apache.jasper34.generator.*;
import org.apache.jasper34.core.Compiler;
import org.apache.jasper34.runtime.*;
import org.apache.jasper34.jsptree.*;
import org.apache.jasper34.core.*;
import org.apache.jasper34.javacompiler.*;
import org.apache.jasper34.liaison.*;

import org.apache.tomcat.util.log.*;

/**
 * Shell for the jspc compiler.  Handles all options associated with the 
 * command line and creates compilation contexts which it then compiles
 * according to the specified options.
 * @author Danno Ferrin
 */
public class JspC {

    public static final String SWITCH_VERBOSE = "-v";
    public static final String SWITCH_QUIET = "-q";
    public static final String SWITCH_OUTPUT_DIR = "-d";
    public static final String SWITCH_OUTPUT_SIMPLE_DIR = "-dd";
    public static final String SWITCH_IE_CLASS_ID = "-ieplugin";
    public static final String SWITCH_PACKAGE_NAME = "-p";
    public static final String SWITCH_CLASS_NAME = "-c";
    public static final String SWITCH_FULL_STOP = "--";
    public static final String SWITCH_URI_BASE = "-uribase";
    public static final String SWITCH_URI_ROOT = "-uriroot";
    public static final String SWITCH_FILE_WEBAPP = "-webapp";
    public static final String SWITCH_WEBAPP_INC = "-webinc";
    public static final String SWITCH_WEBAPP_XML = "-webxml";
    public static final String SWITCH_MAPPED = "-mapped";
    public static final String SWITCH_DIE = "-die";

    public static final int NO_WEBXML = 0;
    public static final int INC_WEBXML = 10;
    public static final int ALL_WEBXML = 20;

    public static final int DEFAULT_DIE_LEVEL = 1;
    public static final int NO_DIE_LEVEL = 0;

    // future direction
    //public static final String SWITCH_XML_OUTPUT = "-xml";
  
    Properties options=new Properties();
    OptionsProperties optionsP=new OptionsProperties(options);
    
    int jspVerbosityLevel = Log.INFORMATION;

    String targetPackage;
    
    String targetClassName;

    String uriBase;

    String uriRoot;

    String webxmlFile;

    int webxmlLevel;

    int dieLevel;
    boolean dieOnExit = false;
    static int die; // I realize it is duplication, but this is for
                    // the static main catch

    boolean dirset;

    Vector extensions;

    public void init() {
	options.put( Options.KEEP_GENERATED, "true" );
	options.put( Options.SEND_ERROR_TO_CLIENT, "true" );
	options.put( Options.CLASS_DEBUG_INFO, "false" );
        options.put( Options.CLASS_PATH,
			System.getProperty("java.class.path"));
    }

    public int getJspVerbosityLevel() {
        return jspVerbosityLevel;
    }

    // -------------------- Argument processing --------------------
    int argPos;
    // value set by beutifully obsfucscated java
    boolean fullstop = false;
    String args[];

    private void pushBackArg() {
        if (!fullstop) {
            argPos--;
        }
    }

    private String nextArg() {
        if ((argPos >= args.length)
            || (fullstop = SWITCH_FULL_STOP.equals(args[argPos]))) {
            return null;
        } else {
            return args[argPos++];
        }
    }
        
    private String nextFile() {
        if (fullstop) argPos++;
        if (argPos >= args.length) {
            return null;
        } else {
            return args[argPos++];
        }
    }

    public JspC(String[] arg, PrintStream log) {
        args = arg;
        String tok;

        int verbosityLevel = Log.WARNING;
        dieLevel = NO_DIE_LEVEL;
        die = dieLevel;

	// Hack for Runtime package
	String rt=System.getProperty( "jsp.runtime.package" );
	if( rt!=null ) {
	    Constants.JSP_RUNTIME_PACKAGE=rt;
	    Constants.JSP_SERVLET_BASE=rt+".HttpJspBase";
	}
	
	while ((tok = nextArg()) != null) {
            if (tok.equals(SWITCH_QUIET)) {
                verbosityLevel = Log.WARNING;
            } else if (tok.equals(SWITCH_VERBOSE)) {
                verbosityLevel = Log.INFORMATION;
            } else if (tok.startsWith(SWITCH_VERBOSE)) {
                try {
                    verbosityLevel
			= Integer.parseInt(tok.substring(SWITCH_VERBOSE.length()));
                } catch (NumberFormatException nfe) {
                    log.println(
				"Verbosity level " 
				+ tok.substring(SWITCH_VERBOSE.length()) 
				+ " is not valid.  Option ignored.");
                }
            } else if (tok.equals(SWITCH_OUTPUT_DIR)) {
                tok = nextArg();
                if (tok != null) {
                    String scratch=new File(tok).getAbsolutePath();
		    options.put(Options.SCRATCH_DIR, scratch );
                    dirset = true;
                } 
	} else if (tok.equals(SWITCH_OUTPUT_SIMPLE_DIR)) {
                tok = nextArg();
                if (tok != null) {
		    String scratch=new File(tok).getAbsolutePath();
		    options.put(Options.SCRATCH_DIR, scratch );
                } 
            } else if (tok.equals(SWITCH_PACKAGE_NAME)) {
                targetPackage = nextArg();
            } else if (tok.equals(SWITCH_CLASS_NAME)) {
                targetClassName = nextArg();
            } else if (tok.equals(SWITCH_URI_BASE)) {
                uriBase = nextArg();
            } else if (tok.equals(SWITCH_URI_ROOT)) {
                uriRoot = nextArg();
            } else if (tok.equals(SWITCH_WEBAPP_INC)) {
                webxmlFile = nextArg();
                if (webxmlFile != null) {
                    webxmlLevel = INC_WEBXML;
                }
            } else if (tok.equals(SWITCH_WEBAPP_XML)) {
                webxmlFile = nextArg();
                if (webxmlFile != null) {
                    webxmlLevel = ALL_WEBXML;
                }
            } else if (tok.equals(SWITCH_MAPPED)) {
                options.put( Options.MAPPED_FILE, "true");
            } else if (tok.startsWith(SWITCH_DIE)) {
                try {
                    dieLevel = Integer.parseInt(
                        tok.substring(SWITCH_DIE.length()));
                } catch (NumberFormatException nfe) {
                    dieLevel = DEFAULT_DIE_LEVEL;
                }
                die = dieLevel;
            } else {
                pushBackArg();
                // Not a recognized Option?  Start treting them as JSP Pages
                break;
            }
        }
     }

    // -------------------- Process a file --------------------
    
    public boolean parseFile(PrintStream log, String file,
			     Writer servletout, Writer mappingout)
    {
        try {
            CommandLineContext clctxt =
		new CommandLineContext(optionsP.getClassPath(), file, uriBase,
				       uriRoot, false,  optionsP );
            if (dirset) {
                clctxt.setOutputInDirs(true);
            }

            Compiler compiler = new Compiler(clctxt);
	    ManglerCli mangler=new ManglerCli();
	    
	    mangler.init(file,
			 optionsP.getScratchDir().getAbsolutePath(),
			 clctxt.isOutputInDirs());

	    JspPageInfo pageInfo=new JspPageInfo( clctxt,optionsP, mangler);
	    
	    compiler.jsp2java( pageInfo );

	    String javaFile=
		pageInfo.getMangler().getJavaFileName();
	    
	    JavaCompiler javaC=
		compiler.createJavaCompiler( pageInfo );
	    compiler.prepareCompiler( javaC, pageInfo );
	    boolean status = javaC.compile( javaFile );

	    if (status == false) {
		String msg = javaC.getCompilerMessage();
		throw new JasperException("Unable to compile "
					  + msg);
	    }
	    
	    // remove java file if !keepgenerated, etc
	    compiler.postCompile(pageInfo);

            targetClassName = null;
            String thisServletName;
            if  (mangler.getPackageName() == null) {
                thisServletName = mangler.getClassName();
             } else {
                thisServletName = mangler.getPackageName()
                    + '.' + mangler.getClassName();
            }
            if (servletout != null) {
                servletout.write("\n\t<servlet>\n\t\t<servlet-name>");
                servletout.write(thisServletName);
                servletout.write("</servlet-name>\n\t\t<servlet-class>");
                servletout.write(thisServletName);
                servletout.write("</servlet-class>\n\t</servlet>\n");
            }
            if (mappingout != null) {
                mappingout.write("\n\t<servlet-mapping>\n\t\t<url-pattern>");
                mappingout.write(file);
                mappingout.write("</url-pattern>\n\t\t<servlet-name>");
                mappingout.write(thisServletName);
                mappingout.write("</servlet-name>\n\t</servlet-mapping>\n");
            }
            return true;
        } catch (JasperException je) {
            //je.printStackTrace(log);
            ContainerLiaison.message("jspc.error.jasperException", 
                    new Object[] {file, je}, Log.ERROR);
            if (dieLevel != NO_DIE_LEVEL) {
                dieOnExit = true;
            }
        } catch (FileNotFoundException fne) {
                ContainerLiaison.message("jspc.error.fileDoesNotExist", 
                        new Object[] {fne.getMessage()}, Log.WARNING);
        } catch (Exception e) {
            ContainerLiaison.message("jspc.error.generalException", 
                    new Object[] {file, e}, Log.ERROR);
            if (dieLevel != NO_DIE_LEVEL) {
                dieOnExit = true;
            }
        }
        return false;
    }

    // -------------------- Process all files --------------------
    private void prepareScratchDir( ) {
        // set up a scratch/output dir if none is provided
        if (optionsP.getScratchDir() == null) {
            String temp = System.getProperty("java.io.tempdir");
            if (temp == null) {
                temp = "";
            }
            options.put( Options.SCRATCH_DIR,  
			 new File(new File(temp).getAbsolutePath()));
        }
    }

    /** Search for uriRoot
     */
    private void findUriRoot( File f ) {
	String tUriBase = uriBase;
	if (tUriBase == null) {
	    tUriBase = "/";
	}
	try {
	    if (f.exists()) {
		f = new File(f.getCanonicalPath());
		while (f != null) {
		    File g = new File(f, "WEB-INF");
		    if (g.exists() && g.isDirectory()) {
			uriRoot = f.getCanonicalPath();
			uriBase = tUriBase;
			ContainerLiaison.message("jspc.implicit.uriRoot",
						 new Object[] { uriRoot },
						 Log.INFORMATION);
			break;
		    }
		    if (f.exists() && f.isDirectory()) {
			tUriBase = "/" + f.getName() + "/" + tUriBase;
		    }
		    
		    String fParent = f.getParent();
		    if (fParent == null) {
			f = new File(args[argPos]);
			fParent = f.getParent();
			if (fParent == null) {
			    fParent = File.separator;
			}
			uriRoot = new File(fParent).getCanonicalPath();
			uriBase = "/";
			break;
                        } else {
                            f = new File(fParent);
                        }
		    
		    // If there is no acceptible candidate, uriRoot will
		    // remain null to indicate to the CompilerContext to
		    // use the current working/user dir.
		}
	    }
	} catch (IOException ioe) {
	    // since this is an optional default and a null value
	    // for uriRoot has a non-error meaning, we can just
	    // pass straight through
	}
    }
    File froot=null;
    
    public void parseFiles(PrintStream log)
	throws JasperException, IOException
    {
	prepareScratchDir();

        File f = new File(args[argPos]);
        while (!f.exists()) {
            boolean webApp = false;
            if (SWITCH_FILE_WEBAPP.equals(args[argPos])) {
                webApp = true;
                if (args.length > argPos + 1) {
                    f = new File(args[argPos + 1]);
                } else {
                    // end of arguments, nothing left to parse
                    ContainerLiaison.message("jspc.error.emptyWebApp", 
                            Log.ERROR);
                    return;
                }
            }
            if (!f.exists()) {
                ContainerLiaison.message("jspc.error.fileDoesNotExist", 
                        new Object[] {f}, Log.WARNING);
                argPos++;
                if (webApp) {
                    argPos++;
                }
                if (argPos >= args.length) {
                    // end of arguments, nothing left to parse
                    return;
                } else {
                    f = new File(args[argPos]);
                }
            }
        }

	if (uriRoot == null) {
            if (SWITCH_FILE_WEBAPP.equals(args[argPos])) {
                if (args.length > argPos + 1) {
                    f = new File(args[argPos + 1]);
                } else {
                    // end of arguments, nothing left to parse
                    return;
                }
            }
            // set up the uri root if none is explicitly set
	    findUriRoot(f);
        }


        String file = nextFile();
	froot = new File(uriRoot);
        String ubase = null;
        try {
            ubase = froot.getCanonicalPath();
        } catch (IOException ioe) {
            // if we cannot get the base, leave it null
        }

        while (file != null) {
            if (SWITCH_FILE_WEBAPP.equals(file)) {
                String base = nextFile();
		processWebApp( base, uriRoot, ubase, log );
            } else {
                try {
                    if (ubase != null) {
                        File fjsp = new File(file);
                        String s = fjsp.getCanonicalPath();
                        if (s.startsWith(ubase)) {
                            file = s.substring(ubase.length());
                        }
                    }
                } catch (IOException ioe) {
                     // if we got problems dont change the file name
                }

                parseFile(log, file, null, null);
            }
            file = nextFile();
        }
        if (dieOnExit) {
            System.exit(die);
        }
    }

    private void processJspFiles( Vector pages, String ubase,
				  PrintStream log,
				  Writer servletout, Writer mappingout)
	throws IOException
    {
	Enumeration e = pages.elements();
	while (e.hasMoreElements()) {
	    String nextjsp = e.nextElement().toString();
	    try {
		if (ubase != null) {
		    File fjsp = new File(nextjsp);
		    String s = fjsp.getCanonicalPath();
		    //System.out.println("**" + s);
		    if (s.startsWith(ubase)) {
			nextjsp = s.substring(ubase.length());
		    }
		}
	    } catch (IOException ioe) {
		// if we got problems dont change the file name
	    }

	    if (nextjsp.startsWith("." + File.separatorChar)) {
		nextjsp = nextjsp.substring(2);
	    }

	    parseFile(log, nextjsp, servletout, mappingout);
	}
    }
    
    public void processWebApp( String base, String uriRoot, String ubase,
			       PrintStream log)
	throws IOException
    {
	if (base == null) {
	    ContainerLiaison.message("jspc.error.emptyWebApp", 
				     Log.ERROR);
	    return;
	}

	String oldRoot = uriRoot;
        boolean urirootSet = (uriRoot != null);
	if (!urirootSet) {
	    uriRoot = base;
	}
	if (extensions == null) {
	    extensions = new Vector();
	    extensions.addElement("jsp");
	}
	
	Vector pages=findByExtension( base, extensions );
	
	String ubaseOld = ubase;
	File frootOld = froot;
	froot = new File(uriRoot);

	try {
	    ubase = froot.getCanonicalPath();
	} catch (IOException ioe) {
	    // if we cannot get the base, leave it null
	}

	Writer mapout;
	CharArrayWriter servletout, mappingout;
	try {
	    if (webxmlLevel >= INC_WEBXML) {
		File fmapings = new File(webxmlFile);
		mapout = new FileWriter(fmapings);
		servletout = new CharArrayWriter();
		mappingout = new CharArrayWriter();
	    } else {
		mapout = null;
		servletout = null;
		mappingout = null;
	    }
	    if (webxmlLevel >= ALL_WEBXML) {
		mapout.write(ContainerLiaison.getString("jspc.webxml.header"));
	    } else if (webxmlLevel>= INC_WEBXML) {
		mapout.write(ContainerLiaison.getString("jspc.webinc.header"));
	    }
	} catch (IOException ioe) {
	    mapout = null;
	    servletout = null;
	    mappingout = null;
	}
	
	processJspFiles( pages, ubase, log, servletout, mappingout );
	
	uriRoot = oldRoot;
	ubase = ubaseOld;
	froot = frootOld;
	
	if (mapout != null) {
	    try {
		servletout.writeTo(mapout);
		mappingout.writeTo(mapout);
		if (webxmlLevel >= ALL_WEBXML) {
		    mapout.write(ContainerLiaison.getString("jspc.webxml.footer"));
		} else if (webxmlLevel >= INC_WEBXML) {
		    mapout.write(ContainerLiaison.getString("jspc.webinc.footer"));
		}
		mapout.close();
	    } catch (IOException ioe) {
		// noting to do if it fails since we are done with it
	    }
	}
    }
    
    public static void main(String arg[]) {
        if (arg.length == 0) {
           System.out.println(ContainerLiaison.getString("jspc.usage"));
        } else {
            try {
                JspC jspc = new JspC(arg, System.out);
                jspc.parseFiles(System.out);
            } catch (Exception je) {
                System.err.print("error:");
                System.err.println(je.getMessage());
                if (die != NO_DIE_LEVEL) {
                    System.exit(die);
                }
            }
        }
    }

    private static final int debug=1;
    private void log( String s ) {
	System.out.println("JspC: " + s );
    }

    // -------------------- utils --------------------


    public Vector findByExtension(String base, Vector extensions )
	throws IOException
    {
	Vector pages = new Vector();

	Stack dirs = new Stack();

	dirs.push(base);
	while (!dirs.isEmpty()) {
	    String s = dirs.pop().toString();
	    //System.out.println("--" + s);
	    File f = new File(s);
	    if (f.exists() && f.isDirectory()) {
		String[] files = f.list();
		String ext;
		for (int i = 0; i < files.length; i++) {
		    File f2 = new File(s, files[i]);
		    //System.out.println(":" + f2.getPath());
		    if (f2.isDirectory()) {
			dirs.push(f2.getPath());
			//System.out.println("++" + f2.getPath());
		    } else {
			ext = files[i].substring(
						 files[i].lastIndexOf('.') + 1);
			if (extensions.contains(ext)) {
			    //System.out.println(s + "?" + files[i]);
			    pages.addElement(
					     s + File.separatorChar + files[i]);
			} else {
			    //System.out.println("not done:" + ext);
			}
		    }
		}
	    }
	}
	return pages;
    }


}

