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
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.*;

/**
 *
 *
 * @author duncan@x180.com
 */

public class Jar extends Task {

    private File jarFile;
    private File baseDir;
    private Vector items;
    private File manifest;    

    public void setJarfile(String jarFilename) {
	jarFile = project.resolveFile(jarFilename);
    }

    public void setBasedir(String baseDirname) {
	baseDir = project.resolveFile(baseDirname);
    }

    public void setItems(String itemString) {
	items = new Vector();
	StringTokenizer tok = new StringTokenizer(itemString, ",", false);
	while (tok.hasMoreTokens()) {
	    items.addElement(tok.nextToken().trim());
	}
    }

    public void setManifest(String manifestFilename) {
	manifest = project.resolveFile(manifestFilename);
    }

    public void execute() throws BuildException {
	project.log("Building jar: " + jarFile.getAbsolutePath());

	try {
	    FileOutputStream out = new FileOutputStream(jarFile);
	    ZipOutputStream zOut = new ZipOutputStream(out);
	    zOut.setMethod(ZipOutputStream.DEFLATED);
	    
	    // add manifest first
	    if (manifest != null) {
		ZipEntry ze = new ZipEntry("META-INF/");
		zOut.putNextEntry(ze);
		jarFile(manifest, zOut, "META-INF/MANIFEST.MF");
	    } else {
		ZipEntry ze = new ZipEntry("META-INF/");
		zOut.putNextEntry(ze);
		String s = "/org/apache/tools/ant/defaultManifest.mf";
		InputStream in = this.getClass().getResourceAsStream(s);
        if ( in == null )
            throw new BuildException ( "Could not find: " + s );
		jarFile(in, zOut, "META-INF/MANIFEST.MF");
	    }
	    
	    // add items
	    
	    Enumeration e = items.elements();
	    while (e.hasMoreElements()) {
		String s = (String)e.nextElement();
		File f = new File(baseDir, s);
		if (f.isDirectory()) {
		    jarDir(f, zOut, s + "/");
		} else {
		    jarFile(f, zOut, s);
		}
	    }

	    // close up
	    
	    zOut.close();
	    out.close();
	} catch (IOException ioe) {
	    String msg = "Problem creating jar " + ioe.getMessage();
	    throw new BuildException(msg);
	}
    }

    private void jarDir(File dir, ZipOutputStream zOut, String vPath)
	throws IOException
    {
	// First add directory to zip entry
	if( ! "META-INF/".equals(vPath) ) {
	    // we already added a META-INF
	    ZipEntry ze = new ZipEntry(vPath);
	    zOut.putNextEntry(ze);
	}
	
	String[] list = dir.list();
	for (int i = 0; i < list.length; i++) {
	    String f = list[i];
	    File file = new File(dir, f);
	    if (file.isDirectory()) {
		jarDir(file, zOut, vPath + f + "/");
	    } else {
		jarFile(file, zOut, vPath + f);
	    }
	}
    }

    private void jarFile(InputStream in, ZipOutputStream zOut, String vPath)
	throws IOException
    {
	ZipEntry ze = new ZipEntry(vPath);
	zOut.putNextEntry(ze);
	
	byte[] buffer = new byte[8 * 1024];
	int count = 0;
	do {
	    zOut.write(buffer, 0, count);
	    count = in.read(buffer, 0, buffer.length);
	} while (count != -1);
    }
    
    private void jarFile(File file, ZipOutputStream zOut, String vPath)
        throws IOException
    {
	FileInputStream fIn = new FileInputStream(file);
	jarFile(fIn, zOut, vPath);
	fIn.close();
    }
}










