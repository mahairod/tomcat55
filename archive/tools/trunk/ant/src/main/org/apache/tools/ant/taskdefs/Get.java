package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;
import java.net.*;
/**
 * Get a particular source. 
 *
 * @author costin@dnt.ro
 */
public class Get extends Task {
    String source; // required
    String dest; // required
    String verbose;
    
    public void execute() throws BuildException {
	try {
	    URL url=new URL( source );
	    System.out.println("<log:get src=\"" + source + "\">");
	    File destF=new File(dest);
	    FileOutputStream fos = new FileOutputStream(destF);

	    InputStream is=url.openStream();
	    byte[] buffer = new byte[100 * 1024];
	    int length;
	    
	    while ((length = is.read(buffer)) >= 0) {
		fos.write(buffer, 0, length);
		if( "true".equals(verbose)) System.out.print(".");
	    }
	    if( "true".equals(verbose)) System.out.println();
	    System.out.println("</log:get>");
	    fos.close();
	    is.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }

    public void setSrc(String d) {
	this.source=d;
    }

    public void setDest(String dest) {
	this.dest = dest;
    }

    public void setVerbose(String v) {
	verbose=v;
    }
}
