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
	ZipEntry ze = new ZipEntry(vPath);
	zOut.putNextEntry(ze);

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










