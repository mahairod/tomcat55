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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.jasper.Constants;

/**
 * A Plug-in class for specifying a 'kjc' compiler.
 *
 * Please link $CATALINA_HOME/jasper/jasper-compiler.jar and kjc.jar
 * (or kopi.jar) to $CATALINA_HOME/lib before use.
 +
 * Most of code in this class is copied from SunJavaCompiler.java.
 *
 * @author Anil K. Vijendran
 * @author Takashi Okamoto <tora@debian.org>
 * @author teik <teik@rd5.so-net.ne.jp>
 */
public class KjcJavaCompiler implements JavaCompiler {

    String encoding;
    String classpath; // ignored
    String compilerPath;
    String outdir; // ignored
    OutputStream out;
    boolean classDebugInfo=false;

    /**
     * Specify where the compiler can be found
     */
    public void setCompilerPath(String compilerPath) {
        // not used by the KjcJavaCompiler
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
      this.classpath = classpath;
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
     * Set where you want the compiler output (messages) to go
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    public boolean compile(String source) {
	char spr = File.separatorChar;
	String outputdir = source.substring(0, source.lastIndexOf(spr));
	String[] args = new String[]
	{
            "-encoding", encoding,
            "-classpath", classpath,
            "-d", outputdir,
            source
	};

        try {
            Class c = Class.forName("at.dms.kjc.Main");

            Constructor cons = c.getConstructor(null);
            Object compiler = cons.newInstance(null);

            Method compile = c.getMethod
                ("run", new Class [] {String.class, PrintWriter.class,
                                      String[].class});

            Boolean ok = (Boolean)compile.invoke
                (compiler, new Object[] {
                    (String)null,
                    new PrintWriter(new OutputStreamWriter(out, encoding)),
                    args});

            String packageName = Constants.JSP_PACKAGE_NAME;
            if(packageName != null) {
                packageName = spr + packageName.replace('.', spr);
            } else {
                packageName = "";
            }
            String className = source.substring
                (source.lastIndexOf(spr), source.lastIndexOf(".java")) 
                + ".class";
            File classFile = new File
                (outputdir + packageName + spr +  className);
            classFile.renameTo(new File(outputdir + spr + className));

            return ok.booleanValue();
        } catch (ClassNotFoundException e) {
	    try {
		out.write(":kjc can't find. please check kjc installation.".getBytes());
	    } catch (Exception e2) {
	    }
	    return false;
        } catch (InvocationTargetException ei) {
	    try {
		out.write(":maybe kjc setup is invalid. please check gnu.getopt.jar installation.".getBytes());
	    } catch (Exception e2) {
	    }
	    return false;
	} catch (Exception e){
	    try {
		out.write(":unknown error occurred while compiling jsp with kjc.".getBytes());
	    } catch (Exception e2) {
	    }
	    return false;
	}
    }

}
