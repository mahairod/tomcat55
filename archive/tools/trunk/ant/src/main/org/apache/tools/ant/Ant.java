package org.apache.tools.ant;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import com.sun.xml.parser.Resolver;
import com.sun.xml.tree.XmlDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;
import org.apache.tools.ant.*;
/**
 *
 *
 * @author duncan@x180.com
 * @author costin@dnt.ro
 */
public class Ant {
    public static final int MSG_ERR = 0;
    public static final int MSG_WARN = 1;
    public static final int MSG_INFO = 2;
    public static final int MSG_VERBOSE = 3;

    public int msgOutputLevel = MSG_INFO;
    private Properties definedProps = new Properties();
    public String usage="No usage help";
    public String vopt[] = null;
    Vector targets=new Vector();

    static StringBuffer msg;
    static {
	String lSep = System.getProperty("line.separator");
	msg = new StringBuffer();
        msg.append("ant [options] [target]").append(lSep);
        msg.append("Options: ").append(lSep);
	msg.append("  -help                  print this message");
	msg.append(lSep);
	msg.append("  -quiet                 be extra quiet");
	msg.append(lSep);
	msg.append("  -verbose               be extra verbose");
	msg.append(lSep);
	msg.append("  -buildfile <file>      use given buildfile");
	msg.append(lSep);
        msg.append("  -D<property>=<value>   use value for given property");
	msg.append(lSep);     
    }
    
    public static void main(String args[] ) {
	try {
	    Ant ant=new Ant();
	    ant.setValidOptions( new String[] { "buildfile" } );
	    
	    if( ! ant.processArgs( args ) ) {
		return;
	    }

	    Properties props=ant.getProperties();

	    String fname=props.getProperty("buildfile");
	    if(fname==null) fname=props.getProperty("file", "build.xml");
	    File f=new File(fname);

	    TagMap mapper=new TagMap( "org.apache.tools.ant.taskdefs");
	    mapper.addMap( "project", "org.apache.tools.ant.Project");
	    mapper.addMap( "target", "org.apache.tools.ant.Target");

	    Project project=(Project)XmlHelper.readXml(f, props, mapper, null);
	    
	    Enumeration e = props.keys();
	    while (e.hasMoreElements()) {
		String arg = (String)e.nextElement();
		String value = (String)props.get(arg);
		project.setProperty(arg, value);
	    }

	    String args1[] = ant.getArgs();
	    String target = project.getDefaultTarget();
	    if( args1!=null && args1.length>0 )
		target=args1[0];

	    project.executeTarget(target);

	} catch(Exception ex ) {
	    ex.printStackTrace();
	}

    }

    // -------------------- Argument processing --------------------
    // XXX move it to a Helper class2
    public void setValidOptions( String vopt[] ) {
	this.vopt=vopt;
    }

    public Properties getProperties() {
	return definedProps;
    }

    public String[] getArgs() {
	String sa[]=new String[ targets.size() ];
	for( int i=0; i< sa.length; i++ ) {
	    sa[i]=(String)targets.elementAt(i);
	}
	return sa;
    }
    
    public void printUsage() {
	System.out.println(usage);
    }
    
    public  boolean processArgs(String[] args) {
	for (int i = 0; i < args.length; i++) {
	    String arg = args[i];
            
	    if (arg.equals("-help") || arg.equals("help")) {
		printUsage();
		return false;
	    } else if (arg.equals("-quiet") || arg.equals("-q") ||
		       arg.equals("q")) {
		msgOutputLevel = MSG_WARN;
	    } else if (arg.equals("-verbose") || arg.equals("-v") ||
		       arg.equals("v")) {
		msgOutputLevel = MSG_VERBOSE;
	    } else if (arg.startsWith("-D")) {
                arg = arg.substring(2, arg.length());
                String value = args[++i];
                definedProps.put(arg, value);
            } else if (arg.startsWith("-")) {
		String arg1=arg.substring(1);
		int type=0;
		if( vopt != null ) {
		    for( int j=0; j<vopt.length; i++ ) {
			if( vopt[j].equalsIgnoreCase(arg1) ) {
			    type=1;
			    break;
			}
		    }
		    if(type==0) {
			// we don't have any more args to recognize!
			String msg = "Unknown arg: " + arg;
			System.out.println(msg);
			printUsage();
			return false;
		    }
		}
		if( i == args.length) {
		    System.out.println( "Missing argument");
		    printUsage();
		    return false;
		}
		String v=args[i+1];
		i++;
		definedProps.put( arg1, v);
	    } else {
		// if it's no other arg, it may be the target
		targets.addElement( arg );
	    }
	}
	return true;
    }        

}

/** Maps a tag name using a package prefix and a number of static mappings.
 */
class TagMap   implements Map {
    String defaultPackage;
    Hashtable maps=new Hashtable();
    
    
    public TagMap(String defP) {
	defaultPackage=defP + ".";
    }

    public void addMap( String tname, String jname ) {
	maps.put( tname, jname );
    }

    public Object get( Object key ) {
	String tname=(String)key;
	String cName=(String)maps.get(tname);
	if(cName!=null) return InvocationHelper.getInstance( cName );
	// default
	cName= defaultPackage + InvocationHelper.capitalize( tname );
	return InvocationHelper.getInstance( cName); 
    }
}
