package org.apache.jk.apr;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.apache.jk.core.*;

// Hack for Catalina 4.1 who hungs the calling thread.
// Also avoids delays in apache initialization ( tomcat can take a while )

/** 
 * Start some tomcat.
 * 
 */
public class TomcatStarter implements Runnable {
    Class c;
    String args[];
    
    public static String mainClasses[]={ "org.apache.tomcat.startup.Main",
                                         "org.apache.catalina.startup.BootstrapService",
                                         "org.apache.catalina.startup.Bootstrap"};

    public static final int TC_STARTING=1;    
    public static final int TC_STARTED=2;    
    // If someone has time - we can also guess the classpath and do other
    // fancy guessings.
    
    public static void main( String args[], String stdout, String stderr ) {
        System.err.println("TomcatStarter: main()");
        
        try {
            try{ 
                if( stdout!=null ){
                    System.setOut( new PrintStream(new FileOutputStream(stdout)));
                }
                if( stderr!=null ){
                    System.setErr( new PrintStream(new FileOutputStream(stderr)));
                }                                                 
            }catch (Throwable th){
            }
            AprImpl.jniMode();
            
            // Find the class
            Class c=null;
            for( int i=0; i<mainClasses.length; i++ ) {
                try {
                    System.err.println("Try  " + mainClasses[i]);
                    c=Class.forName( mainClasses[i] );
                } catch( ClassNotFoundException ex  ) {
                    continue;
                }
                if( c!= null ) {
                    Thread startThread=new Thread( new TomcatStarter(c, args));
                    c=null;
                    startThread.start();
                    break;
                }
            }
            System.err.println("No class found  ");

        } catch (Throwable t ) {
            t.printStackTrace(System.err);
        }
    }

    public TomcatStarter( Class c, String args[] ) {
        this.c=c;
        this.args=args;
    }
    
    public void run() {
        System.err.println("Starting " + c.getName());
        try {
            AprImpl.jniStatus(TC_STARTING);
            Class argClass=args.getClass();
            Method m=c.getMethod( "main", new Class[] {argClass} );
            m.invoke( c, new Object[] { args } );
            AprImpl.jniStatus(TC_STARTED);
            System.out.println("TomcatStarter: Done");
        } catch( Throwable t ) {
            t.printStackTrace(System.err);
        }
    }
}
