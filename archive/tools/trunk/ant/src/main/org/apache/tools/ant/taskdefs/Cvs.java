package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.*;
import java.io.*;

/**
 *
 *
 * @author costin@dnt.ro
 */
public class Cvs extends Task {
    String cvsRoot;
    String dest;
    String pack;
    
    public void execute() throws BuildException {
	try {
	    String command="cvs -d " + cvsRoot + " co " + pack;
	    System.out.println(command);
	    Process proc=Runtime.getRuntime().exec(command);
	    
	    // ignore response
	    DataInputStream din = new DataInputStream( proc.getInputStream() );
	    String line;
	    while( (line=din.readLine()) != null ) {
		System.out.println(line);
	    }
	    
	    proc.waitFor();
	    int err=proc.exitValue();
	    if( err!=0 ) throw new BuildException( "Error " + err + " in " + command);
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	    throw new BuildException("Error checking out: " + pack );
	} catch (InterruptedException ex) {
	}
    }

    public void setCvsRoot(String root) {
	this.cvsRoot = root;
    }

    public void setDest(String dest) {
	this.dest = dest;
    }

    public void setPackage(String p) {
	this.pack = p;
    }
}
