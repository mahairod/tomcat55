package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.zip.*;
/**
 * Unzip a file. 
 *
 * @author costin@dnt.ro
 */
public class Expand extends Task {
    String dest; // req
    String source; // req
    String verbose;
    
    // XXX move it to util or tools
    public void execute() throws BuildException {
	try {
	    File srcF=new File( source );
	    File dir=new File(dest);
	    
	    System.out.println("<log:expand src=\"" + source + "\" dest=\"" + dest + "\">");
	    // code from WarExpand
	    ZipInputStream zis = new ZipInputStream(new FileInputStream(srcF));
	    ZipEntry ze = null;
	    
	    while ((ze = zis.getNextEntry()) != null) {
		try {
		    File f = new File(dir, ze.getName());
		     if(  "true".equals(verbose)) System.out.println("<log:expand-file name=\"" + ze.getName() + "\" />");
		    if (ze.isDirectory()) {
			f.mkdirs(); 
		    } else {
			byte[] buffer = new byte[1024];
			int length = 0;
			FileOutputStream fos = new FileOutputStream(f);
			
			while ((length = zis.read(buffer)) >= 0) {
			    fos.write(buffer, 0, length);
			}
			
			fos.close();
		    }
		} catch( FileNotFoundException ex ) {
		    System.out.println("WARUtil: FileNotFoundException: " +  ze.getName()  );
		}
	    }
	    System.out.println("</log:expand>");
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }

    public void setDest(String d) {
	this.dest=d;
    }

    public void setSrc(String s) {
	this.source = s;
    }

    public void setVerbose(String v) {
	verbose=v;
    }

}
