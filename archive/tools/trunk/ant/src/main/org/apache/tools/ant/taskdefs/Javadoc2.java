/*
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
 */

package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;

import java.io.*;
import java.util.*;
import java.lang.SecurityManager;

/**
 * This Task makes it easy to generate javadoc 1.2 for a collection of source code.
   <p> 
   &lt;target name="javadoc"&gt;<br>
    &lt;mkdir dir="${javadoc.destdir}"/&gt;<br>
    &lt;javadoc2 sourcepath="${build.src}" destdir="${javadoc.destdir}"<br> 
    packagenames="org.apache.ecs, org.apache.ecs.html, org.apache.ecs.filter,<br> 
    org.apache.ecs.storage, org.apache.ecs.rtf, org.apache.ecs.xml"<br>     
    /&gt;<br>
    &lt;/target&gt;
    <p>
    It isn't perfect, but it works with the above example. If you would like to 
    improve and test it more, that would be great. ;-)
 *
 * @author Jon S. Stevens <a href="mailto:jon@clearink.com">jon@clearink.com</a>
 */
public class Javadoc2 extends Task {

    private File sourcePath = null;
    private File destDir = null;
    private File overviewFile = null;
    private String sourcefiles = null;
    private String packagenames = null;
    private String classnames = null;
// Items that are already true throw an error via javadoc util if defined true
// How stupid is that? ;-( Define them as false below.
//    private boolean pub = true;
//    private boolean prot = true;
//    private boolean pack = true;
//    private boolean author = true;
    private boolean pub = false;
    private boolean prot = false;
    private boolean pack = false;
    private boolean priv = false;
    private boolean author = false;
    private String doclet = null;
    private File docletpath = null;
    private boolean old = false;
    private String classpath = null;
    private String bootclasspath = null;
    private String extdirs = null;
    private boolean verbose = false;
    private String locale = null;
    private String encoding = null;
    private boolean version = true;
    private boolean use = false;
    private boolean splitindex = false;
    private String windowtitle = null;
    private String doctitle = null;
    private String header = null;
    private String footer = null;
    private String bottom = null;
    private String link = null;
    private String linkoffline = null;
    private String group = null;
    private boolean nodeprecated = false;
    private boolean nodeprecatedlist = false;
    private boolean notree = false;
    private boolean noindex = false;
    private boolean nohelp = false;
    private boolean nonavbar = false;
    private File stylesheetfile = null;
    private File helpfile = null;
    private String docencoding = null;

    private Vector compileList = new Vector();
   
    public void setSourcepath(String src) {
        sourcePath = project.resolveFile(src);
    }
    public void setDestdir(String src) {
        destDir = project.resolveFile(src);
    }
    public void setSourcefiles(String src) {
        sourcefiles = src;
    }
    public void setPackagenames(String src) {
        packagenames = src;
    }
    public void setClassnames(String src) {
        classnames = src;
    }
    
    public void setOverview(String src) {
        overviewFile = project.resolveFile(src);
    }
    public void setPublic(String src) {
        pub = new Boolean(src).booleanValue();
    }
    public void setProtected(String src) {
        prot = new Boolean(src).booleanValue();
    }
    public void setPackage(String src) {
        pack = new Boolean(src).booleanValue();
    }
    public void setPrivate(String src) {
        priv = new Boolean(src).booleanValue();
    }
    public void setDoclet(String src) {
        doclet = src;
    }
    public void setDocletPath(String src) {
        docletpath = project.resolveFile(src);
    }
    /**
        Build javadoc to look like JDK 1.1
    */
    public void setOld(String src) {
        old = new Boolean(src).booleanValue();
    }
    public void setClasspath(String src) {
        classpath = Project.translatePath(src);
    }
    public void setBootclasspath(String src) {
        bootclasspath = Project.translatePath(src);
    }
    public void setExtdirs(String src) {
        extdirs = src;
    }
    public void setVerbose(String src) {
        verbose = new Boolean(src).booleanValue();
    }
    public void setLocale(String src) {
        locale = src;
    }
    public void setEncoding(String src) {
        encoding = src;
    }
    public void setVersion(String src) {
        version = new Boolean(src).booleanValue();
    }
    public void setUse(String src) {
        use = new Boolean(src).booleanValue();
    }
    public void setAuthor(String src) {
        author = new Boolean(src).booleanValue();
    }
    public void setSplitindex(String src) {
        splitindex = new Boolean(src).booleanValue();
    }
    public void setWindowtitle(String src) {
        windowtitle = src;
    }
    public void setDoctitle(String src) {
        doctitle = src;
    }
    public void setHeader(String src) {
        header = src;
    }
    public void setFooter(String src) {
        footer = src;
    }
    public void setBottom(String src) {
        bottom = src;
    }
    public void setLink(String src) {
        link = src;
    }
    public void setLinkoffline(String src) {
        linkoffline = src;
    }
    public void setGroup(String src) {
        group = src;
    }
    public void setNodeprecated(String src) {
        nodeprecated = new Boolean(src).booleanValue();
    }
    public void setNodeprecatedlist(String src) {
        nodeprecatedlist = new Boolean(src).booleanValue();
    }
    public void setNotree(String src) {
        notree = new Boolean(src).booleanValue();
    }
    public void setNoindex(String src) {
        noindex = new Boolean(src).booleanValue();
    }
    public void setNohelp(String src) {
        nohelp = new Boolean(src).booleanValue();
    }
    public void setNonavbar(String src) {
        nonavbar = new Boolean(src).booleanValue();
    }
    public void setStylesheetfile(String src) {
        stylesheetfile = project.resolveFile(src);
    }
    public void setDocencoding(String src) {
        docencoding = src;
    }

    public void execute() throws BuildException {
        if (sourcePath == null && destDir == null ) {
            String msg = "sourcePath and destDir attributes must be set!";
            throw new BuildException(msg);
        }
        generate();
    }
    
    private void generate() throws BuildException {
        project.log("Generating JavaDoc", project.MSG_INFO);
        Vector argList = new Vector();

        if (overviewFile != null) {
            argList.addElement("-overview");
            argList.addElement(overviewFile.getAbsolutePath());
        }
        if (pub)
            argList.addElement ("-public");
        if (prot)
            argList.addElement ("-protected");
        if (pack)
            argList.addElement ("-package");
        if (priv)
            argList.addElement ("-private");
        if (old)
            argList.addElement ("-1.1");
        if (verbose)
            argList.addElement ("-verbose");
        if (version)
            argList.addElement ("-version");
        if (use)
            argList.addElement ("-use");
        if (author)
            argList.addElement ("-author");
        if (splitindex)
            argList.addElement ("-splitindex");
        if (nodeprecated)
            argList.addElement ("-nodeprecated");
        if (nodeprecatedlist)
            argList.addElement ("-nodeprecatedlist");
        if (notree)
            argList.addElement ("-notree");
        if (noindex)
            argList.addElement ("-noindex");
        if (nohelp)
            argList.addElement ("-nohelp");
        if (nonavbar)
            argList.addElement ("-nonavbar");
            
        if (doclet != null) {
            argList.addElement("-doclet");
            argList.addElement(doclet);
        }
        argList.addElement("-classpath");
        if (classpath != null) {
            argList.addElement(classpath);
        } else {
            argList.addElement(System.getProperty("java.class.path"));
        }
        if (bootclasspath != null) {
            argList.addElement("-bootclasspath");
            argList.addElement(bootclasspath);
        }
        if (extdirs != null) {
            argList.addElement("-extdirs");
            argList.addElement(extdirs);
        }
        if (locale != null) {
            argList.addElement("-locale");
            argList.addElement(locale);
        }
        if (encoding != null) {
            argList.addElement("-encoding");
            argList.addElement(encoding);
        }
        if (windowtitle != null) {
            argList.addElement("-windowtitle");
            argList.addElement(windowtitle);
        }
        if (doctitle != null) {
            argList.addElement("-doctitle");
            argList.addElement(doctitle);
        }
        if (header != null) {
            argList.addElement("-header");
            argList.addElement(header);
        }
        if (footer != null) {
            argList.addElement("-footer");
            argList.addElement(footer);
        }
        if (bottom != null) {
            argList.addElement("-bottom");
            argList.addElement(bottom);
        }
        if (link != null) {
            argList.addElement("-link");
            argList.addElement(link);
        }
        if (linkoffline != null) {
            argList.addElement("-linkoffline");
            argList.addElement(linkoffline);
        }
        if (group != null) {
            argList.addElement("-group");
            argList.addElement(group);
        }
        if (stylesheetfile != null) {
            argList.addElement("-stylesheetfile");
            argList.addElement(stylesheetfile.getAbsolutePath());
        }
        if (helpfile != null) {
            argList.addElement("-helpfile");
            argList.addElement(helpfile.getAbsolutePath());
        }
        if (docencoding != null) {
            argList.addElement("-docencoding");
            argList.addElement(docencoding);
        }

        argList.addElement("-sourcepath");
        argList.addElement(sourcePath.getAbsolutePath());
        argList.addElement("-d");
        argList.addElement(destDir.getAbsolutePath());
        
        // must be after options
        if ( packagenames != null ) {
            if (packagenames.length() > 0) {
                StringTokenizer tok =
                new StringTokenizer(packagenames, ", ", false);
                while (tok.hasMoreTokens()) {
                    argList.addElement ( tok.nextToken().trim() );
                }
            }
        }
        if ( sourcefiles != null ) {
            if (sourcefiles.length() > 0) {
                StringTokenizer tok =
                new StringTokenizer(sourcefiles, ", ", false);
                while (tok.hasMoreTokens()) {
                    argList.addElement ( tok.nextToken().trim() );
                }
            }
        }
        if ( classnames != null ) {
            if (classnames.length() > 0) {
                StringTokenizer tok =
                new StringTokenizer(classnames, ", ", false);
                while (tok.hasMoreTokens()) {
                    argList.addElement ( tok.nextToken().trim() );
                }
            }
        }
        
        project.log("Javadoc args: " + argList.toString(),
                project.MSG_INFO);
        
        String[] args = new String[argList.size()];
        for (int i = 0; i < argList.size(); i++) {
            args[i] = (String)argList.elementAt(i);            
        }        

        // Assumes that this is in your classpath. Rightnow, it is 
        // in the javac.jar file that comes with Ant, but James says that 
        // this probably won't be the case in the future. In that case, then 
        // we will need to have tools.jar in our classpath in order to find
        // this utility. We could also try executing it via the command line
        // javadoc utility, but that would suck because that would mean that 
        // we would have two JVM's running just to generate this stuff. We would 
        // also have to pass in a classpath specific for the project. Arg.
        // Why can't Sun make our life easy and allow us to distribute 
        // javac.jar with javadoc utility in it?
        
/*        SecurityManager saveSecurityManager = System.getSecurityManager();
        try {

            System.setSecurityManager(new NoExitSecurityManager());
*/
            com.sun.tools.javadoc.Main compiler =
                new com.sun.tools.javadoc.Main();
            compiler.main(args);
//        } finally {
//            System.setSecurityManager(saveSecurityManager);
//        }

    }
}