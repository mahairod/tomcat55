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

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;

/**
  * A Plug-in class for specifying a 'jikes' compile.
  *
  * @author Jeffrey Chiu
  * @author Hans Bergsten <hans@gefionsoftware.com>
  */
public class JikesJavaCompiler implements JavaCompiler {

    static final int OUTPUT_BUFFER_SIZE = 1024;
    static final int BUFFER_SIZE = 512;

    /*
     * Contains extra classpath for Jikes use from Microsoft systems:
     * Microsoft does not report it's internal classpath in 
     * System.getProperty(java.class.path) which results in jikes to fail.  
     * (Internal classpath with other JVMs contains for instance rt.jar).
     */
     static StringBuffer MicrosoftClasspath = null;

    String encoding;
    String classpath;
    String compilerPath = "jikes";
    String outdir;
    OutputStream out;
    boolean classDebugInfo=false;

    /**
     * Specify where the compiler can be found
     */ 
    public void setCompilerPath(String compilerPath) {
	this.compilerPath = compilerPath;
    }

    /**
     * Set the encoding (character set) of the source
     */ 
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Set the class path for the compiler
     */ 
    public void setClasspath(String classpath) {
        //
        // normalize the paths in the classpath.  this
        // is really only an issue with jikes on windows.
        //
        // sometimes a path the looks like this:
        //    /c:/tomcat/webapps/WEB-INF/classes
        // will show up in the classpath.  in fact, this
        // *always* happens with tomcat4.  jikes on windows
        // will barf on this.  the following code will normalize
        // paths like this (all paths, actually) so that jikes
        // is happy :)
        //
        
        StringBuffer buf = new StringBuffer(classpath.length());
        StringTokenizer tok = new StringTokenizer(classpath,
                                                  File.pathSeparator);
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            File file = new File(token);
            buf.append(file.toString());
            buf.append(File.pathSeparator);
        }
        
        this.classpath = buf.toString();
    }

    /**
     * Set the output directory
     */ 
    public void setOutputDir(String outdir) {
      this.outdir = outdir;
    }

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    public void setMsgOutput(OutputStream out) {
      this.out = out;
    }

    /**
     * Set if you want debugging information in the class file
     */
    public void setClassDebugInfo(boolean classDebugInfo) {
        this.classDebugInfo = classDebugInfo;
    }

   /**
     * Execute the compiler
     * @param source - file name of the source to be compiled
     */ 
    public boolean compile(String source) {
	Process p;
	int exitValue = -1;
	String quote = "";

        // Used to dynamically load classpath if using Microsoft 
        // virtual machine
        if (MicrosoftClasspath==null) {
            MicrosoftClasspath = new StringBuffer(200);
            if (System.getProperty("java.vendor").startsWith("Microsoft")) {
                quote = "\"";
                //Get Microsoft classpath
                String javaHome = System.getProperty("java.home") + 
                                  "\\Packages";
                File libDir=new File(javaHome);
                String[] zips=libDir.list();
                for(int i=0;i<zips.length;i++) {
                    MicrosoftClasspath.append(";" + javaHome + "\\" + zips[i]);
                }                       
            } 
        }

        String[] compilerCmd = null;

        if( outdir != null ) {
            compilerCmd = new String[] {
        	quote + compilerPath + quote,
        	//XXX - add encoding once Jikes supports it
        	"-classpath", quote + classpath + MicrosoftClasspath + quote,
        	"-d", quote + outdir + quote,
        	"-nowarn",
                "+E",
        	quote + source + quote
            };
        } else {
            compilerCmd = new String[] {
                quote + compilerPath + quote,
                //XXX - add encoding once Jikes supports it
                "-classpath", quote + classpath + MicrosoftClasspath + quote,
                "-nowarn",                
                "+E",
                quote + source + quote    
            };
        }
        if( classDebugInfo ) {
            String[] compilerCmd2 = new String[compilerCmd.length + 1];
            compilerCmd2[0] = compilerCmd[0];
            compilerCmd2[1] = "-g";
            System.arraycopy(compilerCmd,1,compilerCmd2,2,compilerCmd.length - 1);
            compilerCmd=compilerCmd2;
        }

        ByteArrayOutputStream tmpErr = new ByteArrayOutputStream(OUTPUT_BUFFER_SIZE);
	try {
	    p = Runtime.getRuntime().exec(compilerCmd);
	    
	    BufferedInputStream compilerErr = new
		BufferedInputStream(p.getErrorStream());

	    StreamPumper errPumper = new StreamPumper(compilerErr, tmpErr);

	    errPumper.start();

            p.waitFor();
            exitValue = p.exitValue();

	    // Wait until the complete error stream has been read
            errPumper.join();
	    compilerErr.close();

	    p.destroy();

            // Write the compiler error messages, if any, to the real stream 
            tmpErr.close();
            tmpErr.writeTo(out);
            
	} catch (IOException ioe) {
	    return false;

	} catch (InterruptedException ie) {
	    return false;
	}

        boolean isOkay = exitValue == 0;
        // Jikes returns 0 even when there are some types of errors. 
        // Check if any error output as well
        if (tmpErr.size() > 0) {
            isOkay = false;
        }
        return isOkay;
    }

    // Inner class for continually pumping the input stream during
    // Process's runtime.
    class StreamPumper extends Thread {
	private BufferedInputStream stream;
	private boolean endOfStream = false;
	private boolean stopSignal  = false;
	private int SLEEP_TIME = 5;
	private OutputStream out;

	public StreamPumper(BufferedInputStream is, OutputStream out) {
	    this.stream = is;
	    this.out = out;
	}

	public void pumpStream()
	    throws IOException
	{
	    byte[] buf = new byte[BUFFER_SIZE];
	    if (!endOfStream) {
		int bytesRead=stream.read(buf, 0, BUFFER_SIZE);

		if (bytesRead > 0) {
		    out.write(buf, 0, bytesRead);
		} else if (bytesRead==-1) {
		    endOfStream=true;
		}
	    }
	}

	public void run() {
	    try {
		//while (!endOfStream || !stopSignal) {
		while (!endOfStream) {
		    pumpStream();
		    sleep(SLEEP_TIME);
		}
	    } catch (InterruptedException ie) {
	    } catch (IOException ioe) {
	    }
	}
    }
}


